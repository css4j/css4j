/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a color in the LCh color model ({@code lch()} or {@code oklch()}).
 */
public interface LCHColor extends CSSColor {

	/**
	 * Get the lightness of this color.
	 * 
	 * @return the lightness component.
	 */
	CSSPrimitiveValue getLightness();

	/**
	 * Get the chroma of this color.
	 * 
	 * @return the chroma component.
	 */
	CSSPrimitiveValue getChroma();

	/**
	 * Get the hue of this color.
	 * 
	 * @return the hue component.
	 */
	CSSPrimitiveValue getHue();

	@Override
	LCHColor clone();

}
