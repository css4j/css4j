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
	 * @see #getColor()
	 */
	@Deprecated
	default HSLColor getHSLColorValue() {
		return getColor();
	}

	/**
	 * Get the HSL(A) color represented by this value.
	 *
	 * @return the HSL color.
	 */
	@Override
	HSLColor getColor();

	@Override
	HSLColorValue clone();

}
