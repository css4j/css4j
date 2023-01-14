/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.BooleanConditionFactory;
import io.sf.carte.doc.style.css.parser.DeclarationCondition;

/**
 * Contains factory methods related to <code>{@literal @}supports</code>
 * conditions.
 */
public class SupportsConditionFactory implements BooleanConditionFactory {

	/**
	 * Create a boolean condition of the <code>and</code> type.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createAndCondition() {
		return new AndCondition();
	}

	/**
	 * Create a boolean condition of the given type (<code>and</code>, <code>or</code>,
	 * <code>not</code>).
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createOrCondition() {
		return new OrCondition();
	}

	/**
	 * Create a boolean condition of the <code>not</code> type.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createNotCondition() {
		return new NotCondition();
	}

	/**
	 * Create a declaration (operand) condition.
	 * <p>
	 * The value of the feature can be set later with
	 * {@link DeclarationCondition#setValue(io.sf.carte.doc.style.css.CSSValue)
	 * DeclarationCondition.setValue(CSSValue)}.
	 * 
	 * @param featureName
	 *            the name of the declared feature.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createPredicate(String featureName) {
		return new DeclarationConditionImpl(featureName);
	}

}
