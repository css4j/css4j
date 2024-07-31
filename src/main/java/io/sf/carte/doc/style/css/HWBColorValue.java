/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a HWB color value.
 * <p>
 * Instead of using this interface, you may want to just cast the result of
 * {@link CSSColorValue#getColor()} to the appropriate interface (like
 * {@link HWBColor}) according to the given
 * {@link CSSColorValue#getColorModel()}.
 * </p>
 */
public interface HWBColorValue extends CSSColorValue {

	/**
	 * Get the HWB color represented by this value.
	 *
	 * @return the HWB color.
	 * @deprecated
	 * @see #getColor()
	 */
	@Deprecated
	default HWBColor getHWBColorValue() {
		return getColor();
	}

	/**
	 * Get the HWB color represented by this value.
	 *
	 * @return the HWB color.
	 */
	@Override
	HWBColor getColor();

	@Override
	HWBColorValue clone();

}
