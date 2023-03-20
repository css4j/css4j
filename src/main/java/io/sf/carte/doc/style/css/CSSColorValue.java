/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.property.LABColorValue;
import io.sf.carte.doc.style.css.property.LCHColorValue;

/**
 * Represents a color value (not including color identifiers).
 * <p>
 * When the value is a {@link CSSValue.Type#COLOR COLOR}, cast it to this
 * interface, obtain the color with {@link #getColor()} and cast it to the
 * appropriate sub-interface (like {@link RGBAColor} or {@link LABColor})
 * according to the color model given by {@link #getColorModel()}.
 * </p>
 * <p>
 * For any color model, you can access the color components also through the
 * base {@link CSSColor} interface, without the need to cast it to any
 * sub-interface (and there is no sub-interface for {@link ColorModel#PROFILE
 * PROFILE} colors, so in that case you must use the base interface).
 * </p>
 */
public interface CSSColorValue extends CSSTypedValue {

	/**
	 * Enumeration of color models.
	 */
	enum ColorModel {
		/**
		 * RGB color model.
		 * <p>
		 * See {@link RGBAColor}.
		 * </p>
		 */
		RGB,

		/**
		 * HSL color model.
		 * <p>
		 * See {@link HSLColor}.
		 * </p>
		 */
		HSL,

		/**
		 * HWB color model.
		 * <p>
		 * See {@link HWBColor}.
		 * </p>
		 */
		HWB,

		/**
		 * Lab color model.
		 * <p>
		 * See {@link LABColor}.
		 * </p>
		 */
		LAB,

		/**
		 * LCh color model.
		 * <p>
		 * See {@link LCHColor}.
		 * </p>
		 */
		LCH,

		/**
		 * XYZ color model.
		 * <p>
		 * See {@link XYZColor}.
		 * </p>
		 */
		XYZ,

		/**
		 * A profiled color specified through the {@code color()} function.
		 * <p>
		 * See {@link CSSColor}.
		 * </p>
		 */
		PROFILE
	}

	/**
	 * Get the color represented by this value.
	 *
	 * @return the color.
	 */
	CSSColor getColor();

	/**
	 * Get the color model with which this value was set.
	 * 
	 * @return the color model.
	 */
	default ColorModel getColorModel() {
		return getColor().getColorModel();
	}

	/**
	 * Compute the difference to the given color, according to CIE Delta E 2000.
	 * 
	 * @param color the color to compute the delta from.
	 * @return the CIE Delta E 2000.
	 */
	float deltaE2000(CSSColorValue color);

	/**
	 * Convert this value to a {@link LABColorValue}, if possible.
	 * 
	 * @return the converted {@code LABColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	LABColorValue toLABColorValue() throws DOMException;

	/**
	 * Convert this value to a {@link LCHColorValue}, if possible.
	 * 
	 * @return the converted {@code LCHColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	LCHColorValue toLCHColorValue() throws DOMException;

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
	default RGBAColor toRGBColor(boolean clamp) throws DOMException {
		return toRGBColor();
	}

	/**
	 * Creates and returns a copy of this value.
	 * 
	 * @return a clone of this value.
	 */
	@Override
	CSSColorValue clone();

}
