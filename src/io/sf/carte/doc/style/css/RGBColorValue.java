/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
 * @deprecated Top-level color values with the RGB color model are no longer
 *             guaranteed to implement this interface. Furthermore, this
 *             interface is not especially useful since the {@code getColor()}
 *             method became available. This interface is considered unsafe and
 *             error-prone, it will be removed in the future.
 */
@Deprecated(forRemoval=true)
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
	@Override
	RGBAColor getColor();

	@Override
	RGBColorValue clone();

}
