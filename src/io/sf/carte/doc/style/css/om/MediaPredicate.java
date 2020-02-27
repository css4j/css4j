/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

/**
 * Media predicate.
 * <p>
 * Represents a predicate (any condition excluding booleans AND, OR, NOT)
 * present in a media query.
 */
abstract class MediaPredicate extends BooleanConditionImpl.Predicate {

	static final short MEDIA_TYPE = 0;
	static final short MEDIA_FEATURE = 1;

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
