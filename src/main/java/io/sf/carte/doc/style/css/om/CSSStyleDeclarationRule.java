/*

 Copyright (c) 2005-2024, Carlos Amengual.

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

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Abstract class to be inherited by CSS rules which have both selectors and
 * a CSSStyleDeclaration.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class CSSStyleDeclarationRule extends BaseCSSDeclarationRule {

	private static final long serialVersionUID = 1L;

	private SelectorList selectorList = null;

	private String selectorText = "";

	protected CSSStyleDeclarationRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super(parentSheet, type, origin);
	}

	/**
	 * Constructor used for stand-alone style rules.
	 */
	CSSStyleDeclarationRule() {
		super();
	}

	public String getSelectorText() {
		return selectorText;
	}

	void setSelectorText(String selectorText) throws DOMException {
		this.selectorText = selectorText;
	}

	/**
	 * Set the selectors of this style rule.
	 * 
	 * @param selectorList the selector list.
	 * @throws NullPointerException if {@code selectorList} is null.
	 * @throws IllegalArgumentException if {@code selectorList} is empty.
	 */
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

	SelectorList getSelectorList() {
		return selectorList;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		clear();
		selectorList = null;
		selectorText = "";
		CSSParser parser = (CSSParser) createSACParser();
		Reader re = new StringReader(cssText);
		PropertyCSSHandler handler = new RuleHandler();
		handler.setLexicalPropertyListener(getLexicalPropertyListener());
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(handler);
		try {
			parseRule(re, parser);
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
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
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CSSStyleDeclarationRule))
			return false;
		CSSStyleDeclarationRule other = (CSSStyleDeclarationRule) obj;
		if (!super.equals(other))
			return false;
		if (selectorList == null) {
			if (other.selectorList != null)
				return false;
		} else if (!selectorList.equals(other.selectorList))
			return false;
		return true;
	}

	@Override
	public CSSStyleDeclarationRule clone(AbstractCSSStyleSheet parentSheet) throws IllegalArgumentException {
		Class<?>[] parameterTypes = new Class<?>[2];
		parameterTypes[0] = AbstractCSSStyleSheet.class;
		parameterTypes[1] = Byte.TYPE;
		Constructor<? extends CSSStyleDeclarationRule> ctor;
		try {
			ctor = getClass().getConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
		Object[] initargs = new Object[2];
		initargs[0] = parentSheet;
		initargs[1] = getOrigin();
		CSSStyleDeclarationRule rule;
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

	private class RuleHandler extends DeclarationRuleCSSHandler {

		private RuleHandler() {
			super();
		}

		@Override
		public void startSelector(SelectorList selectors) throws DOMException {
			if (selectorList != null) {
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
						"Rule already set, stream contains more than one rule");
			}
			setSelectorList(selectors);
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) {
			try {
				super.property(name, value, important);
			} catch (DOMException e) {
				CSSPropertyValueException ex = new CSSPropertyValueException(e);
				ex.setValueText(value.toString());
				getStyleDeclarationErrorHandler().wrongValue(name, ex);
			}
		}

		@Override
		public void warning(CSSParseException exception) throws CSSParseException {
			if (selectorList != null) {
				super.warning(exception);
			} else {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().ruleParseWarning(CSSStyleDeclarationRule.this, exception);
				}
			}
		}

		@Override
		public void error(CSSParseException exception) throws CSSParseException {
			if (selectorList != null) {
				super.error(exception);
			} else {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().ruleParseError(CSSStyleDeclarationRule.this, exception);
				}
			}
		}

	}

}
