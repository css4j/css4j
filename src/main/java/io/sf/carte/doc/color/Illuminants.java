/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.color;

/**
 * The reference white of a color space.
 */
public final class Illuminants {

	// ASTM E308-01 via http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html

	/**
	 * The D50 reference white.
	 */
	public static double[] whiteD50 = { 0.96422d, 1d, 0.82521d };

	/**
	 * The D65 reference white.
	 */
	public static double[] whiteD65 = { 0.95047d, 1d, 1.08883d };

	Illuminants() {
		// Prevent instantiation
	}

}
