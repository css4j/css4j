/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

/**
 * Factory interface for boolean conditions.
 * 
 * @see BooleanCondition
 */
public interface BooleanConditionFactory {

	/**
	 * Create a boolean condition of the <code>and</code> type.
	 * 
	 * @return the condition.
	 */
	BooleanCondition createAndCondition();

	/**
	 * Create a boolean condition of the given type (<code>and</code>, <code>or</code>,
	 * <code>not</code>).
	 * 
	 * @return the condition.
	 */
	BooleanCondition createOrCondition();

	/**
	 * Create a boolean condition of the <code>not</code> type.
	 * 
	 * @return the condition.
	 */
	BooleanCondition createNotCondition();

	/**
	 * Create a predicate (operand) condition.
	 * 
	 * @param name
	 *            the name of the property or feature involved in the predicate.
	 * 
	 * @return the condition.
	 */
	BooleanCondition createPredicate(String name);

}
