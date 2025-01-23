/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * Contains factory methods related to <code>@supports</code> conditions.
 */
public interface SupportsConditionFactory extends BooleanConditionFactory {

	/**
	 * Create a condition that was not recognized by the parser and therefore will
	 * never match.
	 * <p>
	 * False conditions belong to the {@link BooleanCondition.Type#OTHER OTHER}
	 * type.
	 * </p>
	 * 
	 * @param conditionText the condition that was not recognized.
	 * @return a condition that never matches.
	 */
	BooleanCondition createFalseCondition(String conditionText);

	/**
	 * Create a selector function.
	 * 
	 * @param selectors the selectors.
	 * @return the condition.
	 */
	BooleanCondition createSelectorFunction(SelectorList selectors);

}
