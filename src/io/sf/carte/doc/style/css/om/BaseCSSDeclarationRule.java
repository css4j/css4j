/*

 Copyright (c) 2005-2019, Carlos Amengual.

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

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

/**
 * Abstract class to be inherited by CSS rules which have a CSSStyleDeclaration.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class BaseCSSDeclarationRule extends BaseCSSRule implements CSSDeclarationRule {

	private AbstractCSSStyleDeclaration declaration = null;

	/*
	 * Lazily instantiated style declaration error handler
	 */
	private StyleDeclarationErrorHandler sdErrorHandler = null;

	protected BaseCSSDeclarationRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super(parentSheet, type, origin);
		declaration = parentSheet.createCSSStyleDeclaration(this);
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
		CSSParser parser;
		try {
			parser = (CSSParser) createSACParser();
		} catch (ClassCastException e) {
			parser = new CSSParser();
		}
		InputSource source = new InputSource();
		Reader re = new StringReader(cssText);
		source.setCharacterStream(re);
		PropertyDocumentHandler handler = createPropertyDocumentHandler();
		handler.setLexicalPropertyListener(getLexicalPropertyListener());
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(handler);
		try {
			parser.parseDeclarationRule(source);
		} catch (CSSException e) {
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
	}

	PropertyDocumentHandler createPropertyDocumentHandler() {
		return new DeclarationRuleDocumentHandler();
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
			if (other.declaration != null)
				return false;
		} else if (!declaration.equals(other.declaration))
			return false;
		return true;
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
		private StyleWrapper(BaseCSSStyleDeclaration copiedObject, String oldHrefContext) {
			super(copiedObject, oldHrefContext);
		}

		@Override
		public BaseCSSDeclarationRule getParentRule() {
			return BaseCSSDeclarationRule.this;
		}

	}

	private class CompatStyleWrapper extends CompatStyleDeclaration {
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

	class DeclarationRuleDocumentHandler extends PropertyDocumentHandler implements CSSParser.DeclarationRuleHandler {
		DeclarationRuleDocumentHandler() {
			super();
		}

		@Override
		public void startAtRule(String name, String pseudoSelector) {
		}

		@Override
		public void endAtRule() {
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) throws CSSException {
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
		public void warning(CSSParseException exception) throws CSSException {
			getStyleDeclarationErrorHandler().sacWarning(exception, getStyle().getLength() - 1);
		}

		@Override
		public void error(CSSParseException exception) throws CSSException {
			getStyleDeclarationErrorHandler().sacError(exception, getStyle().getLength() - 1);
		}

		@Override
		public void fatalError(CSSParseException exception) throws CSSException {
			getStyleDeclarationErrorHandler().sacFatalError(exception, getStyle().getLength() - 1);
		}

	}

}
