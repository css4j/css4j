/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.MediaQueryPredicate;

/**
 * Media predicate.
 * <p>
 * Represents a predicate (any condition excluding booleans AND, OR, NOT)
 * present in a media query.
 * </p>
 */
abstract class MediaPredicate extends BooleanConditionImpl.Predicate
		implements MediaQueryPredicate {

	private static final long serialVersionUID = 2L;

	protected MediaPredicate(String name) {
		super(name);
	}

	/**
	 * Check whether the given predicate is partially or totaly contained by this
	 * one.
	 * <p>
	 * If predicate A matches B, then if a medium matches B it will also match A.
	 * The opposite may not be true.
	 * 
	 * @param otherPredicate the other predicate to check against.
	 * @param negatedQuery   <code>0</code> if it is a direct match, <code>1</code>
	 *                       if the this predicate is reverse (negated),
	 *                       <code>2</code> if the given predicate is negated,
	 *                       <code>3</code> if both are negated.
	 * @return <code>true</code> if the other predicate is partially or totally
	 *         contained by this one.
	 */
	abstract boolean matches(MediaPredicate otherPredicate, byte negatedQuery);

}
