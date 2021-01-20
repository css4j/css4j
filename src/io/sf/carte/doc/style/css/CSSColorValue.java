/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * Represents a color value (not including color identifiers).
 */
public interface CSSColorValue extends CSSTypedValue {

	/**
	 * Enumeration of color models.
	 */
	enum ColorModel {RGB, HSL, HWB, LAB, LCH}

	/**
	 * Get the color model with which this value was set.
	 * 
	 * @return the color model.
	 */
	ColorModel getColorModel();

	/**
	 * Get the RGB(A) color representation of this value.
	 * <p>
	 * The returned value is implicitly in the sRGB color space, unless stated
	 * otherwise.
	 * </p>
	 * 
	 * @param clamp {@code true} if the converted value has to be clamped when it
	 *              does not fall into the sRGB color gamut.
	 * 
	 * @return the RGBA color value.
	 * @exception DOMException INVALID_ACCESS_ERR: if this value can't return a RGB
	 *                         color value (either is not a <code>COLOR</code>, not
	 *                         a typed value, or {@code clamp} is false and the
	 *                         color does not map into the -implicit- sRGB color
	 *                         space).<br/>
	 *                         NOT_SUPPORTED_ERR: if the conversion needs device
	 *                         color space information to be performed accurately.
	 */
	default RGBAColor toRGBColorValue(boolean clamp) throws DOMException {
		return toRGBColorValue();
	}

	@Override
	CSSColorValue clone();

}
