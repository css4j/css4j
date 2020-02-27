/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * Contains methods related to media query conditions.
 */
class NSACMediaQueryFactory implements MediaQueryFactory {

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

	/**
	 * Create a media-feature operand condition.
	 * 
	 * @param featureName
	 *            the name of the media feature.
	 * 
	 * @return the condition.
	 */
	@Override
	public MediaFeaturePredicate createPredicate(String featureName) {
		return new MediaFeaturePredicateUnit(featureName);
	}

	@Override
	public BooleanCondition createMediaTypePredicate(String medium) {
		return new BooleanConditionUnit.Predicate(medium);
	}

	@Override
	public MediaQueryHandler createMediaQueryHandler(Node owner) {
		NSACMediaQueryList list = new NSACMediaQueryList();
		return list.new MyMediaQueryHandler(owner);
	}

	@Override
	public MediaQueryList createAllMedia() {
		return new NSACMediaQueryList();
	}

}
