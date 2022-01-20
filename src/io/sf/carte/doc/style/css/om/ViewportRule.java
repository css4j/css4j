/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS Viewport rule implementation.
 * 
 * @author Carlos Amengual
 * 
 */
public class ViewportRule extends BaseCSSDeclarationRule {

	public ViewportRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, ExtendedCSSRule.VIEWPORT_RULE, origin);
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
		return "@viewport{" + getStyle().getMinifiedCssText() + '}';
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		context.startRule(wri);
		wri.write("@viewport");
		context.updateContext(this);
		context.writeLeftCurlyBracket(wri);
		context.startStyleDeclaration(wri);
		getStyle().writeCssText(wri, context);
		context.endCurrentContext(this);
		context.endStyleDeclaration(wri);
		context.writeRightCurlyBracket(wri);
		context.endRule(wri);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int len = cssText.length();
		int idx = cssText.indexOf('{');
		if (len < 15 || cssText.charAt(len - 1) != '}' || idx == -1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @viewport rule: " + cssText);
		}
		if (!ParseHelper.startsWithIgnoreCase(cssText, "@viewport")) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Not a @viewport rule: " + cssText);
		}
		String empty = cssText.substring(9, idx).trim();
		if (empty.length() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @viewport rule: " + cssText);
		}
		super.setCssText(cssText);
	}

	@Override
	public ViewportRule clone(AbstractCSSStyleSheet parentSheet) {
		ViewportRule rule = new ViewportRule(parentSheet, getOrigin());
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		return rule;
	}

}
