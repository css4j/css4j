/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a HSL color value.
 */
public interface HSLColorValue extends CSSColorValue {

	/**
	 * Get the HSL(A) color represented by this value.
	 *
	 * @return the HSL color.
	 * @deprecated
	 * @see #getHSLColor()
	 */
	@Deprecated
	default HSLColor getHSLColorValue() {
		return getHSLColor();
	}

	/**
	 * Get the HSL(A) color represented by this value.
	 *
	 * @return the HSL color.
	 */
	HSLColor getHSLColor();

	@Override
	HSLColorValue clone();

}
