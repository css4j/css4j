/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a RGB color value.
 * <p>
 * Instead of using this interface, cast the result of
 * {@link CSSColorValue#getColor()} to the appropriate interface (like
 * {@link RGBAColor}) according to the given
 * {@link CSSColorValue#getColorModel()}.
 * </p>
 * 
 * @deprecated
 */
@Deprecated
public interface RGBColorValue extends CSSColorValue {

	/**
	 * Get the RGB(A) color represented by this value.
	 *
	 * @return the RGB color.
	 * @deprecated
	 * @see #getColor()
	 */
	@Deprecated
	default RGBAColor getRGBColorValue() {
		return getColor();
	}

	/**
	 * Get the RGB(A) color represented by this value.
	 *
	 * @return the RGB color.
	 */
	RGBAColor getColor();

	@Override
	RGBColorValue clone();

}
