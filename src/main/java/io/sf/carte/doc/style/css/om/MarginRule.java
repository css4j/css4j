/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMarginRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSMarginRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class MarginRule extends BaseCSSDeclarationRule implements CSSMarginRule {

	private static final long serialVersionUID = 1L;

	private String ruleName = null;

	protected MarginRule(AbstractCSSStyleSheet parentSheet, byte origin, String ruleName) {
		super(parentSheet, MARGIN_RULE, origin);
		this.ruleName = ruleName;
	}

	@Override
	public PageRule getParentRule() {
		return (PageRule) super.getParentRule();
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(32 + getStyle().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		StringBuilder buf = new StringBuilder(96);
		buf.append('@').append(ruleName).append('{');
		buf.append(getStyle().getMinifiedCssText()).append('}');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		context.startRule(wri, getPrecedingComments());
		wri.write('@');
		wri.write(ruleName);
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
	public String getName() {
		return ruleName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ruleName == null) ? 0 : ruleName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MarginRule other = (MarginRule) obj;
		if (ruleName == null) {
			if (other.ruleName != null) {
				return false;
			}
		} else if (!ruleName.equals(other.ruleName)) {
			return false;
		}
		return true;
	}

	@Override
	public MarginRule clone(AbstractCSSStyleSheet parentSheet) {
		MarginRule rule = new MarginRule(parentSheet, getOrigin(), ruleName);
		rule.setParentRule(getParentRule());
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		return rule;
	}

}
