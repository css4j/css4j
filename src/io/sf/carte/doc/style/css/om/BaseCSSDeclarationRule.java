/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Abstract class to be inherited by CSS rules which have a CSSStyleDeclaration.
 *
 * @author Carlos Amengual
 *
 */
abstract public class BaseCSSDeclarationRule extends BaseCSSRule implements CSSDeclarationRule {

	private static final long serialVersionUID = 1L;

	private AbstractCSSStyleDeclaration declaration = null;

	/*
	 * Lazily instantiated style declaration error handler
	 */
	private StyleDeclarationErrorHandler sdErrorHandler = null;

	protected BaseCSSDeclarationRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super(parentSheet, type, origin);
		declaration = parentSheet.createStyleDeclaration(this);
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

	@Override
	public void setCssText(String cssText) throws DOMException {
		clear();
		CSSParser parser = (CSSParser) createSACParser();
		Reader re = new StringReader(cssText);
		PropertyCSSHandler handler = createPropertyDocumentHandler();
		handler.setLexicalPropertyListener(getLexicalPropertyListener());
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(handler);
		try {
			parser.parseDeclarationRule(re);
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
	}

	void clear() {
		declaration.clear();
		resetComments();
	}

	void startAtRule(String name, String pseudoSelector) {
	}

	PropertyCSSHandler createPropertyDocumentHandler() {
		return new DeclarationRuleCSSHandler();
	}

	LexicalPropertyListener getLexicalPropertyListener() {
		return declaration;
	}

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
	public BaseCSSDeclarationRule clone(AbstractCSSStyleSheet parentSheet) throws IllegalArgumentException {
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

	class DeclarationRuleCSSHandler extends PropertyCSSHandler implements CSSParser.DeclarationRuleHandler {
		DeclarationRuleCSSHandler() {
			super();
		}

		@Override
		public void startAtRule(String name, String pseudoSelector) {
			BaseCSSDeclarationRule.this.startAtRule(name, pseudoSelector);
		}

		@Override
		public void endAtRule() {
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) {
			try {
				super.property(name, value, important);
			} catch (DOMException e) {
				if (getStyleDeclarationErrorHandler() != null) {
					CSSPropertyValueException ex = new CSSPropertyValueException(e);
					ex.setValueText(value.toString());
					getStyleDeclarationErrorHandler().wrongValue(name, ex);
				}
			}
		}

		@Override
		public void warning(CSSParseException exception) throws CSSParseException {
			getStyleDeclarationErrorHandler().sacWarning(exception, getStyle().getLength() - 1);
		}

		@Override
		public void error(CSSParseException exception) throws CSSParseException {
			getStyleDeclarationErrorHandler().sacError(exception, getStyle().getLength() - 1);
		}

	}

}
