/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.BooleanConditionFactory;

/**
 * Contains methods related to media query conditions.
 */
abstract class ConditionFactoryImpl implements BooleanConditionFactory {

	/**
	 * Create a boolean condition of the <code>and</code> type.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createAndCondition() {
		return new BooleanConditionUnit.AndCondition();
	}

	/**
	 * Create a boolean condition of the given type (<code>and</code>, <code>or</code>,
	 * <code>not</code>).
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createOrCondition() {
		return new BooleanConditionUnit.OrCondition();
	}

	/**
	 * Create a boolean condition of the <code>not</code> type.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createNotCondition() {
		return new BooleanConditionUnit.NotCondition();
	}

}
