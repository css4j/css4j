/*

 Copyright (c) 2005-2024, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css;

/**
 * Values involving mathematical computations.
 */
public interface CSSMathValue extends CSSTypedValue {

	/**
	 * Performs a dimensional analysis of this expression and computes the unit type
	 * of the result.
	 * 
	 * @return the unit type of the result, as in {@link CSSUnit}.
	 */
	short computeUnitType();

	/**
	 * Whether this value expects an integer result that must be rounded.
	 * 
	 * @return {@code true} if the result of this computation must be rounded to the
	 *         nearest integer.
	 */
	boolean expectsInteger();

}
