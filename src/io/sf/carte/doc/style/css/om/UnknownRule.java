/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSUnknownRule;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.CommentRemover;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSUnknownRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class UnknownRule extends BaseCSSRule implements CSSUnknownRule {

	private String cssText = "";

	UnknownRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.UNKNOWN_RULE, origin);
	}

	UnknownRule(UnknownRule copyMe) {
		super(copyMe);
		cssText = copyMe.cssText;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		if (cssText == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null text.");
		}
		resetComments();
		this.cssText = cssText.trim();
	}

	@Override
	public String getCssText() {
		if (cssText.length() == 0) {
			return "";
		}
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(cssText.length());
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		return CommentRemover.removeComments(cssText).toString().trim();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (cssText.length() != 0) {
			context.startRule(wri, this.precedingComments);
			wri.write(cssText);
			context.endRule(wri, this.trailingComments);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cssText.hashCode();
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
		return cssText.equals(other.cssText);
	}

	@Override
	public UnknownRule clone(AbstractCSSStyleSheet parentSheet) {
		return new UnknownRule(this);
	}

}
