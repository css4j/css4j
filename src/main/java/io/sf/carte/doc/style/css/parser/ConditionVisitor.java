/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.Selector.SelectorType;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * Subclasses can be used to visit a selector, a selector list or a condition.
 */
abstract public class ConditionVisitor {

	protected ConditionVisitor() {
		super();
	}

	/**
	 * Visit a list of selectors.
	 * 
	 * @param list the list of selectors.
	 */
	public void visit(SelectorList list) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Selector selector = list.item(i);
			visit(selector);
		}
	}

	/**
	 * Visit a selector.
	 * 
	 * @param selector the selector.
	 */
	public void visit(Selector selector) {
		if (selector.getSelectorType() == SelectorType.CONDITIONAL) {
			ConditionalSelector cond = (ConditionalSelector) selector;
			visit(cond.getCondition());
			SimpleSelector simple = cond.getSimpleSelector();
			if (simple != null) {
				visit(simple);
			}
		} else if (selector instanceof CombinatorSelector) {
			CombinatorSelector comb = (CombinatorSelector) selector;
			visit(comb.getSelector());
			visit(comb.getSecondSelector());
		}
	}

	/**
	 * Visit a condition.
	 * 
	 * @param condition the condition.
	 */
	abstract protected void visit(Condition condition);

}
