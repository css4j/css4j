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

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Abstract class to be inherited by CSS rules which have a CSSStyleDeclaration.
 */
abstract public class BaseCSSDeclarationRule extends BaseCSSRule
		implements ExtendedCSSDeclarationRule {

	private static final long serialVersionUID = 2L;

	private BaseCSSStyleDeclaration declaration = null;

	/*
	 * Lazily instantiated style declaration error handler
	 */
	private StyleDeclarationErrorHandler sdErrorHandler = null;

	protected BaseCSSDeclarationRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super(parentSheet, type, origin);
		declaration = createStyleDeclaration(parentSheet);
	}

	BaseCSSStyleDeclaration createStyleDeclaration(AbstractCSSStyleSheet parentSheet) {
		return (BaseCSSStyleDeclaration) parentSheet.createStyleDeclaration(this);
	}

	/**
	 * Constructor used for stand-alone style rules.
	 */
	BaseCSSDeclarationRule() {
		super(null, CSSRule.STYLE_RULE, (byte) 0);
		this.declaration = new BaseCSSStyleDeclaration(this);
		this.sdErrorHandler = new DefaultStyleDeclarationErrorHandler();
	}

	@Override
	public AbstractCSSStyleDeclaration getStyle() {
		return declaration;
	}

	void clear() {
		declaration.clear();
		resetComments();
	}

	/**
	 * Gets the error handler.
	 * 
	 * @return the error handler.
	 */
	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		if (sdErrorHandler == null) {
			sdErrorHandler = getParentStyleSheet().getStyleSheetFactory()
					.createStyleDeclarationErrorHandler(this);
		}
		return sdErrorHandler;
	}

	@Override
	boolean hasErrorsOrWarnings() {
		return sdErrorHandler != null
				&& (sdErrorHandler.hasErrors() || sdErrorHandler.hasWarnings());
	}

	/**
	 * Sets the style declaration error handler.
	 * <p>
	 * If no handler is set, the one from the parent style sheet's factory will be
	 * used.
	 * </p>
	 * 
	 * @param handler the error handler.
	 */
	public void setStyleDeclarationErrorHandler(StyleDeclarationErrorHandler handler) {
		sdErrorHandler = handler;
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(10 + getStyle().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		getStyle().writeCssText(wri, context);
	}

	@Override
	public String getMinifiedCssText() {
		return getStyle().getMinifiedCssText();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = Short.hashCode(getType());
		result = prime * result + ((declaration == null) ? 0 : declaration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseCSSDeclarationRule other = (BaseCSSDeclarationRule) obj;
		if (declaration == null) {
			return other.declaration == null;
		} else {
			return declaration.equals(other.declaration);
		}
	}

	@Override
	public BaseCSSDeclarationRule clone(AbstractCSSStyleSheet parentSheet)
			throws IllegalArgumentException {
		Class<?>[] parameterTypes = new Class<?>[2];
		parameterTypes[0] = AbstractCSSStyleSheet.class;
		parameterTypes[1] = Byte.TYPE;
		Constructor<? extends BaseCSSDeclarationRule> ctor;
		try {
			ctor = getClass().getConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
		Object[] initargs = new Object[2];
		initargs[0] = parentSheet;
		initargs[1] = getOrigin();
		BaseCSSDeclarationRule rule;
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
		return rule;
	}

	/*
	 * Wrappers: any change to the code below should also be done to StyleRule.
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
		public BaseCSSDeclarationRule getParentRule() {
			return BaseCSSDeclarationRule.this;
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
		public BaseCSSDeclarationRule getParentRule() {
			return BaseCSSDeclarationRule.this;
		}

	}

	/*
	 * End wrappers.
	 */

}
