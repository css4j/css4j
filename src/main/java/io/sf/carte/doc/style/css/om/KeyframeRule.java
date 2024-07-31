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

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSSKeyframeRule implementation.
 * 
 * @author Carlos Amengual
 * 
 */
public class KeyframeRule extends BaseCSSDeclarationRule implements CSSKeyframeRule {

	private static final long serialVersionUID = 1L;

	private final KeyframesRule parentRule;

	private String keyText;

	public KeyframeRule(KeyframesRule parentRule) {
		super(parentRule.getParentStyleSheet(), CSSRule.KEYFRAME_RULE, parentRule.getOrigin());
		this.parentRule = parentRule;
	}

	/* (non-Javadoc)
	 * @see io.sf.carte.doc.style.css.om.CSSKeyframeRule#getKeyText()
	 */
	@Override
	public String getKeyText() {
		return keyText;
	}

	void setKeyText(String keyText) {
		this.keyText = keyText;
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
	public String getMinifiedCssText() {
		return getKeyText() + '{' + getStyle().getMinifiedCssText() + '}';
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		context.startRule(wri, getPrecedingComments());
		wri.write(getKeyText());
		context.updateContext(this);
		context.writeLeftCurlyBracket(wri);
		context.startStyleDeclaration(wri);
		getStyle().writeCssText(wri, context);
		context.endCurrentContext(this);
		context.endStyleDeclaration(wri);
		context.writeRightCurlyBracket(wri);
		context.endRule(wri, getTrailingComments());
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		PropertyCSSHandler handler = new MyKFHandler();
		handler.setLexicalPropertyListener(getLexicalPropertyListener());

		Reader re = new StringReader(cssText);
		try {
			parseRule(re, handler);
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
	}

	@Override
	void parseRule(Reader reader, CSSHandler handler) throws IOException {
		// Create and configure a parser
		CSSParser parser = (CSSParser) createSACParser();
		// Allow only warnings
		CSSErrorHandler errorHandler = new AllowWarningsRuleErrorHandler();
		parser.setDocumentHandler(handler);
		// Use a more permissive error handler here
		parser.setErrorHandler(errorHandler);

		// Parse
		parseRule(reader, parser);
	}

	@Override
	void parseRule(Reader reader, Parser parser)
			throws DOMException, IOException {
		try {
			((CSSParser) parser).parseDeclarationRule(reader);
		} catch (CSSNamespaceParseException e) {
			DOMException ex = new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSBudgetException e) {
			DOMException ex = new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, "Parse error at ["
				+ e.getLineNumber() + ',' + e.getColumnNumber() + "]: " + e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_ACCESS_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (RuntimeException e) {
			String message = e.getMessage();
			AbstractCSSStyleSheet parentSS = getParentStyleSheet();
			if (parentSS != null) {
				String href = parentSS.getHref();
				if (href != null) {
					message = "Error in stylesheet at " + href + ": " + message;
				}
			}
			DOMException ex = new DOMException(DOMException.INVALID_STATE_ERR, message);
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public KeyframesRule getParentRule() {
		return parentRule;
	}

	@Override
	public AbstractCSSStyleSheet getParentStyleSheet() {
		return parentRule.getParentStyleSheet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((keyText == null) ? 0 : keyText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyframeRule other = (KeyframeRule) obj;
		if (keyText == null) {
			if (other.keyText != null)
				return false;
		} else if (!keyText.equals(other.keyText))
			return false;
		return true;
	}

	@Override
	public KeyframeRule clone(AbstractCSSStyleSheet parentSheet) {
		KeyframeRule rule = new KeyframeRule(getParentRule());
		rule.setKeyText(getKeyText());
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		return rule;
	}

	private class MyKFHandler extends DeclarationRuleCSSHandler {

		private MyKFHandler() {
			super();
		}

		@Override
		public void startAtRule(String name, String pseudoSelector) {
			String selector = getParentRule().keyframeSelector(pseudoSelector);
			setKeyText(selector);
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) {
			if (important) {
				// Declarations marked as important must be ignored
				CSSPropertyValueException ex = new CSSPropertyValueException(
						"Important declarations in a keyframe rule are not valid");
				ex.setValueText(value.toString() + " !important");
				getStyleDeclarationErrorHandler().wrongValue(name, ex);
			} else {
				super.property(name, value, important);
			}
		}

	}

}
