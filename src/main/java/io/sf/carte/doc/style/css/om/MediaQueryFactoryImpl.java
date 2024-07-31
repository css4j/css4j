/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryFactory;
import io.sf.carte.doc.style.css.MediaQueryHandler;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.MediaQueryPredicate;

/**
 * Contains methods related to media query conditions.
 */
class MediaQueryFactoryImpl implements MediaQueryFactory {

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
	 * Create a media-feature operand condition.
	 * 
	 * @param featureName
	 *            the name of the media feature.
	 * 
	 * @return the condition.
	 */
	@Override
	public MediaFeaturePredicate createPredicate(String featureName) {
		return new MediaFeaturePredicateImpl(featureName);
	}

	@Override
	public MediaQueryPredicate createMediaTypePredicate(String medium) {
		return new MediaTypePredicate(medium);
	}

	@Override
	public MediaQueryHandler createMediaQueryHandler(Node owner) {
		MediaQueryListImpl list = new MediaQueryListImpl();
		return list.new MyMediaQueryHandler(owner);
	}

	@Override
	public MediaQueryList createAllMedia() {
		return new MediaQueryListImpl();
	}

}
