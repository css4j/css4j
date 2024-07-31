/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSStyleException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * Convenience exception that contains a reference to a rule.
 * <p>
 * Intended for use by sheet's error handler, may be dropped in future releases.
 */
public class RuleParseException extends CSSStyleException {
	private static final long serialVersionUID = 1L;

	private final CSSRule rule;

	public RuleParseException(String message, CSSParseException cause, CSSRule rule) {
		super(message, cause);
		this.rule = rule;
	}

	public RuleParseException(CSSParseException cause, CSSRule rule) {
		super(cause);
		this.rule = rule;
	}

	@Override
	public CSSParseException getCause() {
		return (CSSParseException) super.getCause();
	}

	public CSSRule getRule() {
		return rule;
	}

	@Override
	public String toString() {
		CSSParseException ex = getCause();
		StringBuilder buf = new StringBuilder();
		buf.append("Rule: ").append(rule.getType()).append(", [").append(ex.getLineNumber()).append(':')
				.append(ex.getColumnNumber()).append(']').append(' ').append(" Message: ").append(ex.getMessage());
		return buf.toString();
	}

}
