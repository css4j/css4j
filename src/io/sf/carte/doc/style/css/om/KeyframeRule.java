/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
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

	private final KeyframesRule parentRule;

	private String keyText;

	public KeyframeRule(KeyframesRule parentRule) {
		super(parentRule.getParentStyleSheet(), ExtendedCSSRule.KEYFRAME_RULE, parentRule.getOrigin());
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
		context.startRule(wri, this.precedingComments);
		wri.write(getKeyText());
		context.updateContext(this);
		context.writeLeftCurlyBracket(wri);
		context.startStyleDeclaration(wri);
		getStyle().writeCssText(wri, context);
		context.endCurrentContext(this);
		context.endStyleDeclaration(wri);
		context.writeRightCurlyBracket(wri);
		context.endRule(wri, this.trailingComments);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int lenm1 = cssText.length() - 1;
		int endIdx = cssText.lastIndexOf('}');
		int idx = cssText.indexOf('{');
		if (idx < 2 || endIdx != lenm1 || idx == endIdx) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad keyframe rule: " + cssText);
		}
		String selector = cssText.substring(0, idx).trim();
		selector = getParentRule().keyframeSelector(selector);
		super.setCssText(cssText);
		// All seems OK, so we set the keyText
		keyText = selector;
	}

	@Override
	PropertyCSSHandler createPropertyDocumentHandler() {
		return new MyKFHandler();
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
		public void property(String name, LexicalUnit value, boolean important, int index) {
			if (important) {
				// Declarations marked as important must be ignored
				CSSPropertyValueException ex = new CSSPropertyValueException(
						"Important declarations in a keyframe rule are not valid");
				ex.setValueText(value.toString() + " !important");
				getStyleDeclarationErrorHandler().wrongValue(name, ex);
			} else {
				super.property(name, value, important, index);
			}
		}
	}

}
