/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMSyntaxException;
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
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet.Cascade;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS style rule.
 */
public class StyleRule extends GroupingRule implements CSSStyleRule, ExtendedCSSDeclarationRule {

	private static final long serialVersionUID = 2L;

	SelectorList selectorList = null;

	private SelectorList absSelectorList = null;

	String selectorText = "";

	private BaseCSSStyleDeclaration declaration = null;

	/*
	 * Lazily instantiated style declaration error handler
	 */
	private StyleDeclarationErrorHandler sdErrorHandler = null;

	public StyleRule(AbstractCSSStyleSheet parentSheet, int origin) {
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
			throw new DOMSyntaxException(e);
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
		updateAbsoluteSelectorList();
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

	private void updateAbsoluteSelectorList() {
		this.absSelectorList = selectorList;
		AbstractCSSRule parent = getParentRule();
		if (parent != null) {
			LinkedList<SelectorList> selStack = new LinkedList<>();
			AbstractCSSRule anc = null;
			do {
				if (parent.getType() == CSSRule.STYLE_RULE) {
					anc = parent;
					selStack.add(((StyleRule) anc).getSelectorList());
				}
				parent = getParentRule();
			} while (parent != null);
			if (anc != null) {
				Iterator<SelectorList> it = selStack.descendingIterator();
				this.absSelectorList = it.next();
				while (it.hasNext()) {
					SelectorList list = it.next();
					this.absSelectorList = list.replaceNested(absSelectorList);
				}
				this.absSelectorList = selectorList.replaceNested(absSelectorList);
			}
		}

		updateDescendantsAbsoluteSelectorList(absSelectorList);
	}

	@Override
	public SelectorList getSelectorList() {
		return this.selectorList;
	}

	SelectorList getAbsoluteSelectorList() {
		return absSelectorList;
	}

	void setAbsoluteSelectorList(SelectorList absSelectorList) {
		this.absSelectorList = absSelectorList;
	}

	@Override
	void prioritySplit(AbstractCSSStyleSheet importantSheet, AbstractCSSStyleSheet normalSheet,
			RuleStore importantStore, RuleStore normalStore) {
		AbstractCSSStyleDeclaration userImportantStyle = normalSheet.createStyleDeclaration();
		AbstractCSSStyleDeclaration userNormalStyle = normalSheet.createStyleDeclaration();
		prioritySplit(importantSheet, normalSheet, importantStore, normalStore, userImportantStyle,
				userNormalStyle);
	}

	void prioritySplit(AbstractCSSStyleSheet importantSheet, AbstractCSSStyleSheet normalSheet,
			RuleStore importantStore, RuleStore normalStore,
			AbstractCSSStyleDeclaration userImportantStyle,
			AbstractCSSStyleDeclaration userNormalStyle) {
		AbstractCSSStyleDeclaration st = getStyle();
		st.prioritySplit(userImportantStyle, userNormalStyle);

		StyleRule importantrule = null;
		if (!userImportantStyle.isEmpty()) {
			importantrule = importantSheet.createStyleRule();
			copySelectorsTo(importantrule);
			BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) importantrule.getStyle();
			style.setProperties((BaseCSSStyleDeclaration) userImportantStyle);
			importantStore.addRule(importantrule);
		}

		StyleRule normalrule = null;
		if (!userNormalStyle.isEmpty()) {
			normalrule = normalSheet.createStyleRule();
			copySelectorsTo(normalrule);
			BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) normalrule.getStyle();
			style.setProperties((BaseCSSStyleDeclaration) userNormalStyle);
			normalStore.addRule(normalrule);
		}

		if (cssRules != null) {
			if (importantrule == null) {
				importantrule = importantSheet.createStyleRule();
				copySelectorsTo(importantrule);
			}
			if (normalrule == null) {
				normalrule = normalSheet.createStyleRule();
				copySelectorsTo(normalrule);
			}

			super.prioritySplit(importantSheet, normalSheet, importantrule, normalrule);

			if (importantrule.getParentRule() == null && importantrule.getCssRules() != null) {
				importantStore.addRule(importantrule);
			}
			if (normalrule.getParentRule() == null && normalrule.getCssRules() != null) {
				normalStore.addRule(normalrule);
			}
		}
	}

	void copySelectorsTo(StyleRule otherRule) {
		otherRule.selectorList = getSelectorList();
		otherRule.selectorText = selectorText;
		otherRule.setAbsoluteSelectorList(getAbsoluteSelectorList());
	}

	@Override
	void cascade(Cascade cascade, SelectorMatcher matcher, ComputedCSSStyle style,
			String targetMedium) {
		int selIdx = matcher.matches(getAbsoluteSelectorList());
		if (selIdx != -1) {
			cascade.add(getSpecificity(selIdx, matcher));
		}
		if (cssRules != null) {
			cssRules.cascade(cascade, matcher, style, targetMedium);
		}
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
		return new RuleSpecificity(getAbsoluteSelectorList().item(index), matcher);
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
			// 0x1fff = 8191 is a Mersenne prime
			return (o2.getCSSStyleRule().getOrigin() - o1.getCSSStyleRule().getOrigin()) * 0x1fff
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
			if (cssRules != null) {
				cssRules.writeCssText(wri, context);
			}
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, getTrailingComments());
		}
	}

	@Override
	public String getMinifiedCssText() {
		String seltext = getSelectorText();
		int len = seltext.length();
		if (len != 0) {
			String ministyle = getStyle().getMinifiedCssText();
			int slen = ministyle.length();
			StringBuilder buf = new StringBuilder(len + slen + 3);
			buf.append(seltext).append('{');
			if (slen > 0) {
				buf.append(ministyle);
			}
			if (cssRules != null) {
				if (slen > 0) {
					buf.append(';');
				}
				buf.append(cssRules.toMinifiedString());
			}
			buf.append('}');
			return buf.toString();
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
		parameterTypes[1] = Integer.TYPE;
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
		rule.absSelectorList = getAbsoluteSelectorList();
		rule.setPrecedingComments(getPrecedingComments());
		rule.setTrailingComments(getTrailingComments());
		if (cssRules != null) {
			rule.cssRules = cloneRuleList(parentSheet, cssRules);
		}
		if (hasErrorsOrWarnings()) {
			rule.setStyleDeclarationErrorHandler(getStyleDeclarationErrorHandler());
		}
		return rule;
	}

}
