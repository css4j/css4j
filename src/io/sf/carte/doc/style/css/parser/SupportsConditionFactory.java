/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.SupportsCondition;
import io.sf.carte.doc.style.css.SupportsCondition.ConditionType;
import io.sf.carte.doc.style.css.parser.SupportsConditionImpl.AndCondition;
import io.sf.carte.doc.style.css.parser.SupportsConditionImpl.MyDeclarationCondition;
import io.sf.carte.doc.style.css.parser.SupportsConditionImpl.NotCondition;
import io.sf.carte.doc.style.css.parser.SupportsConditionImpl.OrCondition;

/**
 * Contains static factory methods related to <code>{@literal @}supports</code>
 * conditions.
 */
public class SupportsConditionFactory {

	/**
	 * Create a boolean condition of the given type (<code>and</code>, <code>or</code>,
	 * <code>not</code>).
	 * 
	 * @param type
	 *            the condition type.
	 * 
	 * @return the condition.
	 */
	public static SupportsCondition createBooleanCondition(ConditionType type) {
		switch (type) {
		case AND_CONDITION:
			return new AndCondition();
		case OR_CONDITION:
			return new OrCondition();
		case NOT_CONDITION:
			return new NotCondition();
		default:
			return null;
		}
	}

	/**
	 * Create a declaration (operand) condition.
	 * <p>
	 * The value of the feature can be set later with
	 * {@link SupportsCondition.DeclarationCondition#setValue(io.sf.carte.doc.style.css.ExtendedCSSValue)
	 * DeclarationCondition.setValue(ExtendedCSSValue)}.
	 * 
	 * @param featureName
	 *            the name of the declared feature.
	 * 
	 * @return the condition.
	 */
	public static SupportsCondition createDeclarationCondition(String featureName) {
		return new MyDeclarationCondition(featureName);
	}

}
