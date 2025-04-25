/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS style rule.
 * 
 * @author Carlos Amengual
 * 
 */
public class StyleRule extends GroupingRule implements CSSStyleRule, ExtendedCSSDeclarationRule {

	private static final long serialVersionUID = 1L;

	private SelectorList selectorList = null;

	private String selectorText = "";

	private BaseCSSStyleDeclaration declaration = null;

	/*
	 * Lazily instantiated style declaration error handler
	 */
	private StyleDeclarationErrorHandler sdErrorHandler = null;

	public StyleRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.STYLE_RULE, origin);
		declaration = createStyleDeclaration(parentSheet);
	}

	BaseCSSStyleDeclaration createStyleDeclaration(AbstractCSSStyleSheet parentSheet) {
		return (BaseCSSStyleDeclaration) parentSheet.createStyleDeclaration(this);
	}

	@Override
	public AbstractCSSStyleDeclaration getStyle() {
		return declaration;
	}

	/*
	 * Wrappers: any change to the code below should also be done to
	 * BaseCSSDeclarationRule.
	 */

	void setWrappedStyle(BaseCSSStyleDeclaration style, String oldHrefContext) {
		if (!(style instanceof CompatStyleDeclaration)) {
			declaration = new StyleWrapper(style, oldHrefContext);
		} else {
			declaration = new CompatStyleWrapper((CompatStyleDeclaration) style, oldHrefContext);
		}
	}

	private class StyleWrapper extends WrappedCSSStyleDeclaration {

		private static final long serialVersionUID = 1L;

		private StyleWrapper(BaseCSSStyleDeclaration copiedObject, String oldHrefContext) {
			super(copiedObject, oldHrefContext);
		}

		@Override
		public CSSDeclarationRule getParentRule() {
			return StyleRule.this;
		}

	}

	private class CompatStyleWrapper extends CompatStyleDeclaration {

		private static final long serialVersionUID = 1L;

		private final String hrefcontext;
		private final String oldHrefContext;

		private CompatStyleWrapper(CompatStyleDeclaration copiedObject, String oldHrefContext) {
			super(copiedObject);
			hrefcontext = WrappedCSSStyleDeclaration.getHrefContext(getParentRule());
			this.oldHrefContext = oldHrefContext;
		}

		@Override
		protected StyleValue getCSSValue(String propertyName) {
			StyleValue value = super.getCSSValue(propertyName);
			if (value != null) {
				value = WrappedCSSStyleDeclaration.wrapCSSValue(value, oldHrefContext, hrefcontext);
			}
			return value;
		}

		@Override
		public CSSDeclarationRule getParentRule() {
			return StyleRule.this;
		}

	}

	/*
	 * End wrappers.
	 */

	/**
	 * Gets the error handler.
	 * 
	 * @return the error handler.
	 */
	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		if (sdErrorHandler == null) {
			sdErrorHandler = getParentStyleSheet().getStyleSheetFactory().createStyleDeclarationErrorHandler(this);
		}
		return sdErrorHandler;
	}

	@Override
	boolean hasErrorsOrWarnings() {
		return sdErrorHandler != null && (sdErrorHandler.hasErrors() || sdErrorHandler.hasWarnings());
	}

	/**
	 * Sets the style declaration error handler.
	 * <p>
	 * If no handler is set, the one from the parent style sheet's factory will be used.
	 * </p>
	 * 
	 * @param handler
	 *            the error handler.
	 */
	public void setStyleDeclarationErrorHandler(StyleDeclarationErrorHandler handler) {
		sdErrorHandler = handler;
	}

	@Override
	public void setSelectorText(String selectorText) throws DOMException {
		Parser parser = createSACParser();
		SelectorList selist;
		try {
			selist = parser.parseSelectors(selectorText, getParentStyleSheet());
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		setSelectorList(selist);
	}

	@Override
	public String getSelectorText() {
		return selectorText;
	}

	/**
	 * Set the selectors of this style rule.
	 * 
	 * @param selectorList the selector list.
	 * @throws NullPointerException if {@code selectorList} is null.
	 * @throws IllegalArgumentException if {@code selectorList} is empty.
	 */
	@Override
	public void setSelectorList(SelectorList selectorList) {
		if (selectorList == null) {
			throw new NullPointerException("Null selector list");
		}
		if (selectorList.getLength() == 0) {
			throw new IllegalArgumentException("Empty selector list");
		}
		this.selectorList = selectorList;
		updateSelectorText();
	}

	void updateSelectorText() {
		int sz = selectorList.getLength();
		StringBuilder sb = new StringBuilder(sz * 7 + 5);
		if (sz > 0) {
			SelectorSerializer serializer = new SelectorSerializer(getParentStyleSheet());
			serializer.selectorListText(sb, selectorList, false, false);
		}
		this.selectorText = sb.toString();
	}

	@Override
	public SelectorList getSelectorList() {
		return this.selectorList;
	}

	/**
	 * Returns the specificity of this rule.
	 * <p>
	 * See Cascading Style Sheets, level 2 revision 1 CSS 2.1 Specification,
	 * paragraph 6.4.3.
	 * </p>
	 * 
	 * @param index the index of the selector in the selector list.
	 * @param matcher the selector matcher to apply in specificity computations.
	 * @return the specificity.
	 */
	RuleSpecificity getSpecificity(int index, SelectorMatcher matcher) {
		return new RuleSpecificity(getSelectorList().item(index), matcher);
	}

	class RuleSpecificity extends Specificity {

		private static final long serialVersionUID = 1L;

		public RuleSpecificity(Selector selector, SelectorMatcher matcher) {
			super(selector, matcher);
		}

		@Override
		public int hashCode() {
			return 31 * super.hashCode() + getCSSStyleRule().getOrigin();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			if (getCSSStyleRule().getOrigin() != ((RuleSpecificity) obj).getCSSStyleRule().getOrigin())
				return false;
			return true;
		}

		/**
		 * Gets the style rule to which this specificity applies.
		 * 
		 * @return the style rule.
		 */
		public StyleRule getCSSStyleRule() {
			return StyleRule.this;
		}
	}

	/*
	 * This comparator ignores rule insertion index, so any users of this class
	 * have to deal with that elsewhere.
	 */
	static class SpecificityComparator implements Comparator<RuleSpecificity> {

		/*
		 * Compares the two arguments for order. <p> Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.
		 */
		@Override
		public int compare(RuleSpecificity o1, RuleSpecificity o2) {
			return (o2.getCSSStyleRule().getOrigin() - o1.getCSSStyleRule().getOrigin()) * 131071
					+ Specificity.selectorCompare(o1, o2);
		}
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(50 + getStyle().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		String seltext = getSelectorText();
		if (seltext.length() != 0) {
			context.startRule(wri, getPrecedingComments());
			wri.write(seltext);
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, getTrailingComments());
		}
	}

	@Override
	public String getMinifiedCssText() {
		String seltext = getSelectorText();
		if (seltext.length() != 0) {
			return seltext + '{' + getStyle().getMinifiedCssText() + '}';
		}
		return "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((selectorList == null) ? 0 : selectorList.hashCode());
		// Declaration cannot be null
		result = prime * result + declaration.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StyleRule other = (StyleRule) obj;
		return Objects.equals(selectorList, other.selectorList)
				&& declaration.equals(other.declaration);
	}

	@Override
	public StyleRule clone(AbstractCSSStyleSheet parentSheet) throws IllegalArgumentException {
		Class<?>[] parameterTypes = new Class<?>[2];
		parameterTypes[0] = AbstractCSSStyleSheet.class;
		parameterTypes[1] = Byte.TYPE;
		Constructor<? extends StyleRule> ctor;
		try {
			ctor = getClass().getConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
		Object[] initargs = new Object[2];
		initargs[0] = parentSheet;
		initargs[1] = getOrigin();
		StyleRule rule;
		try {
			rule = ctor.newInstance(initargs);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		rule.selectorList = getSelectorList();
		rule.selectorText = getSelectorText();
		if (hasErrorsOrWarnings()) {
			rule.setStyleDeclarationErrorHandler(getStyleDeclarationErrorHandler());
		}
		return rule;
	}

}
