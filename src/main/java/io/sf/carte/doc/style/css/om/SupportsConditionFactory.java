/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SheetContext;

/**
 * Contains factory methods related to <code>{@literal @}supports</code>
 * conditions.
 */
public class SupportsConditionFactory
		implements io.sf.carte.doc.style.css.SupportsConditionFactory {

	private final SheetContext parentStyleSheet;

	/**
	 * Construct a new condition factory for rules belonging to the given style
	 * sheet context.
	 * 
	 * @param parentStyleSheet the style sheet context.
	 */
	public SupportsConditionFactory(SheetContext parentStyleSheet) {
		super();
		this.parentStyleSheet = parentStyleSheet;
	}

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
	 * Create a boolean condition of the given type (<code>and</code>,
	 * <code>or</code>, <code>not</code>).
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
	 * {@link io.sf.carte.doc.style.css.nsac.DeclarationPredicate#setValue(io.sf.carte.doc.style.css.nsac.LexicalUnit)
	 * DeclarationPredicate.setValue(LexicalUnit)}.
	 * 
	 * @param featureName the name of the declared feature.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createPredicate(String featureName) {
		return new DeclarationConditionImpl(featureName);
	}

	/**
	 * Create a selector function.
	 * 
	 * @param selectors the selectors.
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createSelectorFunction(SelectorList selectors) {
		return new SelectorFunctionImpl(parentStyleSheet, selectors);
	}

	@Override
	public BooleanCondition createFalseCondition(String condition) {
		return new FalseConditionImpl(condition);
	}

}
