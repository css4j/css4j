/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a color value (not including color identifiers).
 */
public interface CSSColorValue extends CSSTypedValue {

	/**
	 * Enumeration of color spaces.
	 */
	enum ColorSpace {RGB, HSL, HWB}

	/**
	 * Get the color space with which this value was set.
	 * 
	 * @return the color space.
	 */
	ColorSpace getColorSpace();

	/**
	 * Get the RGB(A) color represented by this value.
	 *
	 * @return the RGBA color value.
	 */
	@Override
	RGBAColor toRGBColorValue();

	@Override
	CSSColorValue clone();

}
