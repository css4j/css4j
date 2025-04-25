/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
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

	private LexicalUnit keyframeSelector;

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

	void setKeyframeSelector(LexicalUnit keyframeSelector) throws DOMException {
		this.keyframeSelector = keyframeSelector;
		this.keyText = KeyframesRule.keyframeSelector(keyframeSelector);
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
		StringBuilder buffer = new StringBuilder(64);
		miniKeyframeSelector(buffer, keyframeSelector);
		buffer.append('{');
		buffer.append(getStyle().getMinifiedCssText());
		buffer.append('}');
		return buffer.toString();
	}

	private static String miniKeyframeSelector(StringBuilder buffer, LexicalUnit selunit) {
		appendMiniSelector(buffer, selunit);
		LexicalUnit lu = selunit.getNextLexicalUnit();
		while (lu != null) {
			LexicalUnit nextlu = lu.getNextLexicalUnit();
			buffer.append(',');
			appendMiniSelector(buffer, nextlu);
			lu = nextlu.getNextLexicalUnit();
		}
		return buffer.toString();
	}

	private static void appendMiniSelector(StringBuilder buffer, LexicalUnit selunit) {
		LexicalType type = selunit.getLexicalUnitType();
		if (type == LexicalType.IDENT || type == LexicalType.STRING) {
			buffer.append(selunit.getStringValue());
		} else if (type == LexicalType.PERCENTAGE) {
			float floatValue = selunit.getFloatValue();
			if (floatValue == 0f) {
				buffer.append('0');
				return;
			}
			if (floatValue % 1 != 0) {
				buffer.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				buffer.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			buffer.append('%');
		} else if (type == LexicalType.INTEGER && selunit.getIntegerValue() == 0) {
			buffer.append('0');
		} else {
			buffer.append('?');
		}
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
		handler.setLexicalPropertyListener(getStyle());

		Reader re = new StringReader("@keyframes x {" + cssText + "}");

		// Create and configure a parser
		Parser parser = createSACParser();
		parser.setErrorHandler(handler);
		parser.setDocumentHandler(handler);
		clear();
		try {
			parser.parseRule(re);
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSBudgetException e) {
			DOMException ex = new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
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
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
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
		rule.keyframeSelector = keyframeSelector;
		rule.keyText = keyText;
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		return rule;
	}

	private class MyKFHandler extends PropertyCSSHandler implements CSSHandler {

		private MyKFHandler() {
			super();
		}

		@Override
		public void startKeyframe(LexicalUnit keyframeSelector) {
			KeyframeRule.this.setKeyframeSelector(keyframeSelector);
		}

		@Override
		public void endKeyframe() {
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
