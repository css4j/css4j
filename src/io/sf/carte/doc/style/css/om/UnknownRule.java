/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSUnknownRule;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSUnknownRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class UnknownRule extends BaseCSSRule implements CSSUnknownRule {
	private String cssText = null;

	protected UnknownRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.UNKNOWN_RULE, origin);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		this.cssText = cssText;
	}

	@Override
	public String getCssText() {
		return cssText;
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		wri.write(cssText);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cssText == null) ? 0 : cssText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UnknownRule other = (UnknownRule) obj;
		if (cssText == null) {
			if (other.cssText != null) {
				return false;
			}
		} else if (!cssText.equals(other.cssText)) {
			return false;
		}
		return true;
	}

	@Override
	public UnknownRule clone(AbstractCSSStyleSheet parentSheet) {
		UnknownRule rule = new UnknownRule(parentSheet, getOrigin());
		rule.cssText = getCssText();
		return rule;
	}

}
