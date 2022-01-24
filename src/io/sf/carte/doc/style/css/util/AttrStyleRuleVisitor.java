/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.util;

import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.parser.AttributeConditionVisitor;
import io.sf.carte.util.Visitor;

/**
 * Visit style rules and apply an {@link AttributeConditionVisitor}.
 * 
 * @see AbstractCSSStyleSheet#acceptStyleRuleVisitor(Visitor)
 */
public class AttrStyleRuleVisitor implements Visitor<CSSStyleRule> {

	private final AttributeConditionVisitor visitor;

	/**
	 * Construct the rule visitor.
	 * 
	 * @param visitor the condition visitor.
	 */
	public AttrStyleRuleVisitor(AttributeConditionVisitor visitor) {
		super();
		this.visitor = visitor;
	}

	/**
	 * Visit the given style rule and apply the {@link AttributeConditionVisitor} to
	 * its selector list.
	 * 
	 * @param rule the style rule.
	 */
	@Override
	public void visit(CSSStyleRule rule) {
		SelectorList selist = rule.getSelectorList();
		visitor.visit(selist);
		rule.setSelectorList(selist); // Refresh serialization
	}

}
