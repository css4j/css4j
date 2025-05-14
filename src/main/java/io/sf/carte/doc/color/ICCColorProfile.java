/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.color;

/**
 * An ICC color profile.
 */
public interface ICCColorProfile {

	/**
	 * The white point.
	 * 
	 * @return a 3-vector containing the white point, with {@code Y} normalized to
	 *         {@code 1}.
	 */
	double[] getWhitePoint();

}
