/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * Represents a hsl color.
 */
public interface HSLColor extends CSSColor {

	/**
	 * Get the hue of this color.
	 * 
	 * @return the hue component.
	 */
	CSSPrimitiveValue getHue();

	/**
	 * Get the saturation of this color.
	 * 
	 * @return the saturation component.
	 */
	CSSPrimitiveValue getSaturation();

	/**
	 * Get the lightness of this color.
	 * 
	 * @return the lightness component.
	 */
	CSSPrimitiveValue getLightness();

	@Override
	HSLColor clone();

}
