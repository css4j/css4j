/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryFactory;
import io.sf.carte.doc.style.css.MediaQueryHandler;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.MediaQueryPredicate;

/**
 * Contains methods related to media query conditions.
 */
class NSACMediaQueryFactory extends ConditionFactoryImpl implements MediaQueryFactory {

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
	public MediaQueryPredicate createMediaTypePredicate(String medium) {
		return new MediaPredicateUnit(medium);
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
