/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

/**
 * Media predicate.
 * <p>
 * Represents a predicate (any condition excluding booleans AND, OR, NOT)
 * present in a media query.
 */
public interface MediaPredicate extends BooleanCondition {

	/**
	 * Check whether the given predicate is partially or totaly contained by this
	 * one.
	 * <p>
	 * If predicate A matches B, then if a medium matches B it will also match A.
	 * The opposite may not be true.
	 * 
	 * @param otherPredicate the other predicate to check against.
	 * @return <code>true</code> if the other predicate is partially or totally
	 *         contained by this one.
	 */
	boolean matches(MediaPredicate otherPredicate);

}
