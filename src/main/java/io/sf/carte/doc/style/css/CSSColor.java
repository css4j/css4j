/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;

/**
 * A color.
 */
public interface CSSColor {

	/**
	 * Get the color model that this color uses.
	 * <p>
	 * If this methods returns the {@link ColorModel#PROFILE PROFILE} value, this
	 * color belongs to a custom profile.
	 * </p>
	 * 
	 * @return the color model.
	 */
	ColorModel getColorModel();

	/**
	 * Get the color space to which this color belongs.
	 * <p>
	 * See {@link ColorSpace}.
	 * </p>
	 * 
	 * @return the color space.
	 */
	String getColorSpace();

	/**
	 * Get the alpha channel of this color.
	 * 
	 * @return the alpha channel.
	 */
	CSSPrimitiveValue getAlpha();

	/**
	 * Get the component of this color located at {@code index}.
	 * <p>
	 * The alpha channel is considered the component {@code 0}, so the actual
	 * color component index starts at {@code 1}.
	 * </p>
	 * 
	 * @param index the index. {@code 0} is always the alpha channel.
	 * @return the component, or {@code null} if the index is out of range.
	 */
	CSSPrimitiveValue item(int index);

	/**
	 * The number of component values plus the alpha channel.
	 * 
	 * @return the number of component values plus the alpha channel.
	 */
	int getLength();

	/**
	 * Convert this color to the given CSS color space.
	 * 
	 * @param colorSpace the destination color space. The color spaces in
	 *                   {@link ColorSpace} are supported, as well as the other
	 *                   valid CSS space names ({@code hsl} and {@code hwb}).
	 * @return a color converted to the given color space.
	 * @throws DOMException          if the color space is not supported.
	 * @throws IllegalStateException if the color components have to be converted to
	 *                               typed values.
	 */
	CSSColor toColorSpace(String colorSpace) throws DOMException;

	/**
	 * Gives a minified string representation of this color.
	 * 
	 * @return a minified serialization.
	 */
	String toMinifiedString();

	/**
	 * Pack this color into a new CSS value.
	 * <p>
	 * Colors that come from a conversion sometimes aren't packed into a
	 * {@link CSSColorValue}, and this method provides a way to create one.
	 * </p>
	 * 
	 * @return the new CSS value packing this color.
	 */
	CSSColorValue packInValue();

	/**
	 * Clone this color.
	 * 
	 * @return a clone of this color.
	 */
	CSSColor clone();

}
