/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a HWB color value.
 */
public interface HWBColorValue extends CSSColorValue {

	/**
	 * Get the HWB color represented by this value.
	 *
	 * @return the HWB color.
	 * @deprecated
	 * @see #getHWBColor()
	 */
	@Deprecated
	default HWBColor getHWBColorValue() {
		return getHWBColor();
	}

	/**
	 * Get the HWB color represented by this value.
	 *
	 * @return the HWB color.
	 */
	HWBColor getHWBColor();

	@Override
	HWBColorValue clone();

}
