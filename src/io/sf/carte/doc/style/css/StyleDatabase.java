/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * CSS Style database.
 * <p>
 * To use CSS in practice, you need to have information about the target rendering device:
 * available fonts, page sizes, etc. This information is supplied by an instance of the
 * <code>StyleDatabase</code> interface.
 * </p>
 * 
 * @author Carlos Amengual
 */
public interface StyleDatabase {

	/**
	 * Gives the initial (default) value for the 'color' property on this device.
	 * 
	 * @return the default color value.
	 */
	public CSSPrimitiveValue getInitialColor();

	/**
	 * Sets the initial (default) value for the 'color' property on this device.
	 * 
	 * @param initialColor
	 *            the String representing default color value.
	 */
	public void setInitialColor(String initialColor);

	/**
	 * Gets the name of the default font used when a generic font family (serif, sans-serif,
	 * monospace, cursive, fantasy) is specified.
	 * 
	 * @param genericFamily
	 *            the name of the logical font.
	 * @return the name of the real font to which the generic name is mapped to, or null if
	 *         none.
	 */
	public String getDefaultGenericFontFamily(String genericFamily);

	/**
	 * Gets the real name of the default font.
	 * <p>
	 * For example, if the default generic font name is 'serif', this method should return the
	 * same as getDefaultGenericFontFamily("serif").
	 * 
	 * @return the name of the default real font.
	 */
	public String getDefaultGenericFontFamily();

	/**
	 * Checks if a font family is available.
	 * 
	 * @param fontFamily
	 *            the font family name.
	 * @return <code>true</code> if the font is available, <code>false</code> otherwise.
	 */
	public boolean isFontFamilyAvailable(String fontFamily);

	/**
	 * Gets the font size from the given size identifier (small, medium, etc.), expressed in
	 * typographic points.
	 * 
	 * @param familyName
	 *            the font family name. Could be null.
	 * @param fontSizeIdentifier
	 *            the font size identifier.
	 * @return the font size.
	 * @throws DOMException
	 *             if the identifier is unknown.
	 */
	public int getFontSizeFromIdentifier(String familyName, String fontSizeIdentifier) throws DOMException;

	/**
	 * Gives the style declaration adequate to provide the given system font.
	 * 
	 * @param systemFontName
	 *            the system font name.
	 * @return the style declaration, or null if the system font name was not recognized.
	 */
	public String getSystemFontDeclaration(String systemFontName);

	/**
	 * Gets the identifier of the device's natural unit.
	 * 
	 * @return the unit identifier as in <code>CSSPrimitiveValue</code>.
	 */
	public short getNaturalUnit();

	/**
	 * Makes an unit conversion (for the units known to this device).
	 * 
	 * @param initialValue
	 *            the value to be converted, expressed in the initial unit.
	 * @param initialUnitType
	 *            the initial unit type.
	 * @param destUnitType
	 *            the destination unit type.
	 * @return the value, expressed in units of destUnitType.
	 * @throws DOMException
	 *             if the conversion cannot be done.
	 */
	public float floatValueConversion(float initialValue, short initialUnitType, short destUnitType)
			throws DOMException;

	/**
	 * Makes an unit conversion to the natural unit of this device.
	 * 
	 * @param initialValue
	 *            the value to be converted, expressed in the initial unit.
	 * @param initialUnitType
	 *            the initial unit type.
	 * @return the value, expressed in units of destUnitType.
	 * @throws DOMException
	 *             if the conversion cannot be done.
	 */
	public float floatValueConversion(float initialValue, short initialUnitType) throws DOMException;

	/**
	 * Gives the size of the 'ex' unit, expressed in 'pt' (typographic points) unit.
	 * <p>
	 * Although this type of information really belongs to <code>CSSCanvas</code>, having even
	 * an approximate value here is helpful.
	 * </p>
	 * 
	 * @param familyName
	 *            the font family name.
	 * @param size
	 *            the font size.
	 * @return the size of the 'ex' unit, in units of 'pt'.
	 */
	public int getExSizeInPt(String familyName, int size);

	/**
	 * Gets the size corresponding to the given identifier (thin, thick, medium).
	 * 
	 * @param widthIdentifier
	 *            the CSS width identifier.
	 * @param fontSize
	 *            the font size used by the box.
	 * @return the size.
	 * @throws DOMException
	 *             if the identifier is unknown.
	 */
	public float getWidthSize(String widthIdentifier, int fontSize) throws DOMException;

	/**
	 * Gives the number of bits allocated to colors (excluding the alpha channel) in the
	 * output device.
	 * <p>
	 * If the device is not a color device, the value is zero.
	 * </p>
	 * <p>
	 * From W3C's Screen interface.
	 * 
	 * @return the number of bits allocated to colors, or zero if the output device does not
	 *         support colors.
	 */
	public int getColorDepth();

	/**
	 * The pixelDepth attribute value.
	 * <p>
	 * From W3C's Screen interface.
	 * 
	 * @return the value of the pixelDepth attribute.
	 */
	public int getPixelDepth();

	/**
	 * Gets the height of the device, expressed in its natural unit.
	 * 
	 * @return the height of the rendering device.
	 */
	public float getDeviceHeight();

	/**
	 * Gets the width of the device, expressed in its natural unit.
	 * 
	 * @return the width of the rendering device.
	 */
	public float getDeviceWidth();
}