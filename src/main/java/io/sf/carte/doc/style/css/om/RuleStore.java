/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.css.CSSRule;

/**
 * Implemented by classes which are CSS rule stores.
 * 
 * @author Carlos Amengual
 * 
 */
interface RuleStore {
	/**
	 * Insert the given CSS rule at the given index.
	 * 
	 * @param cssrule
	 *            the rule.
	 * @param index
	 *            the index at which to insert the rule.
	 * @return the index at which the rule was finally inserted.
	 */
	int insertRule(CSSRule cssrule, int index);
}
