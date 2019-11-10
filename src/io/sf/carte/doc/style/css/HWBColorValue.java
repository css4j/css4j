/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
	 * @return the HWB color value.
	 */
	HWBColor getHWBColorValue();

	@Override
	HWBColorValue clone();

}
