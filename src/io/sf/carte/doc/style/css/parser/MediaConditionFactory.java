/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

/**
 * Contains methods related to media query conditions.
 */
public interface MediaConditionFactory extends BooleanConditionFactory {

	/**
	 * Create a operand condition.
	 * 
	 * @param featureName
	 *            the name of the media feature.
	 * 
	 * @return the condition.
	 */
	@Override
	BooleanCondition createPredicate(String featureName);

	/**
	 * Create a predicate that contains a medium type.
	 * 
	 * @param medium
	 *            the medium type.
	 * @return the predicate.
	 */
	BooleanCondition createMediaTypePredicate(String medium);

}
