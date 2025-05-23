/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * A predicate in a boolean expression from a Media Query.
 */
public interface MediaQueryPredicate extends BooleanCondition {

	/**
	 * Constant for media type (name) predicates.
	 */
	int MEDIA_TYPE = 0;

	/**
	 * Constant for media feature predicates.
	 * <p>
	 * You can cast predicates of this type to
	 * {@link io.sf.carte.doc.style.css.om.MediaFeature MediaFeature}.
	 * </p>
	 */
	int MEDIA_FEATURE = 1;

	/**
	 * Get a name associated with this predicate.
	 * <p>
	 * Examples: <code>screen</code>, <code>width</code>.
	 * </p>
	 *
	 * @return the name of o medium or media feature.
	 */
	String getName();

	/**
	 * An number indicative of the predicate type.
	 * 
	 * @return the predicate type, {@link #MEDIA_TYPE} for media names and
	 *         {@link #MEDIA_FEATURE} for media features.
	 */
	int getPredicateType();

}
