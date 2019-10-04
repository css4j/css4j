/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * Contains factory methods related to media queries.
 */
public interface MediaQueryFactory extends BooleanConditionFactory {

	/**
	 * Create a media feature predicate.
	 * 
	 * @param featureName
	 *            the name of the media feature.
	 * 
	 * @return the condition.
	 */
	@Override
	MediaFeaturePredicate createPredicate(String featureName);

	/**
	 * Create a predicate that contains a medium type.
	 * 
	 * @param medium
	 *            the medium type.
	 * @return the predicate.
	 */
	BooleanCondition createMediaTypePredicate(String medium);

	/**
	 * Create a handler attached to a new media query list.
	 * 
	 * @param owner the node that owns the responsibility to handle the errors in
	 *              the query list.
	 * @return the media query handler.
	 */
	MediaQueryHandler createMediaQueryHandler(Node owner);

	/**
	 * Create a media query list for all media.
	 * 
	 * @return a new media query list for all media.
	 */
	MediaQueryList createAllMedia();

}
