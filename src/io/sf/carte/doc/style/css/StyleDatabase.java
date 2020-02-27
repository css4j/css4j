/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

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
	CSSTypedValue getInitialColor();

	/**
	 * Sets the initial (default) value for the 'color' property on this device.
	 *
	 * @param initialColor
	 *            the String representing default color value.
	 */
	void setInitialColor(String initialColor);

	/**
	 * Gets the name of the default font used when a generic font family (serif, sans-serif,
	 * monospace, cursive, fantasy) is specified.
	 *
	 * @param genericFamily
	 *            the name of the logical font.
	 * @return the name of the real font to which the generic name is mapped to, or null if
	 *         none.
	 */
	String getDefaultGenericFontFamily(String genericFamily);

	/**
	 * Gets the real name of the default font.
	 * <p>
	 * For example, if the default generic font name is 'serif', this method should return the
	 * same as getDefaultGenericFontFamily("serif").
	 *
	 * @return the name of the default real font.
	 */
	String getDefaultGenericFontFamily();

	/**
	 * Gets the used font family name according to the given style.
	 *
	 * @param computedStyle the computed style.
	 * @return the name of the used font family.
	 */
	String getUsedFontFamily(CSSComputedProperties computedStyle);

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
	float getFontSizeFromIdentifier(String familyName, String fontSizeIdentifier) throws DOMException;

	/**
	 * Gives the style declaration adequate to provide the given system font.
	 *
	 * @param systemFontName
	 *            the system font name.
	 * @return the style declaration, or null if the system font name was not recognized.
	 */
	String getSystemFontDeclaration(String systemFontName);

	/**
	 * Is <code>requestedFamily</code> an available font family loaded by a font
	 * face rule?
	 *
	 * @param requestedFamily
	 *            the font family name in lower case.
	 * @return <code>true</code> if is an available font family, <code>false</code> otherwise.
	 */
	boolean isFontFaceName(String requestedFamily);

	/**
	 * Try to load the font family according to the given font face rule, and
	 * make it available to this object.
	 *
	 * @param rule
	 *            the font face rule.
	 */
	void loadFontFaceRule(CSSFontFaceRule rule);

	/**
	 * Gets the identifier of the device's natural unit.
	 *
	 * @return the unit identifier as in {@link CSSUnit}.
	 */
	short getNaturalUnit();

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
	 *            the font size in 'pt' units.
	 * @return the size of the 'ex' unit, in units of 'pt'.
	 */
	float getExSizeInPt(String familyName, float size);

	/**
	 * Gets the size corresponding to the given identifier (thin, thick, medium), in
	 * typographic points.
	 *
	 * @param widthIdentifier the CSS width identifier.
	 * @param fontSize        the font size used by the box, in typographic points.
	 * @return the size.
	 * @throws DOMException if the identifier is unknown.
	 */
	float getWidthSize(String widthIdentifier, float fontSize) throws DOMException;

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
	int getColorDepth();

	/**
	 * Gets the height of the device, expressed in its natural unit.
	 *
	 * @return the height of the rendering device.
	 */
	float getDeviceHeight();

	/**
	 * Gets the width of the device, expressed in its natural unit.
	 *
	 * @return the width of the rendering device.
	 */
	float getDeviceWidth();

	/**
	 * Does this medium support the given property-value pair?
	 *
	 * @param property
	 *            the property name.
	 * @param value
	 *            the optional property value to be tested against.
	 * @return <code>true</code> if the property (with the given value, if any) is supported,
	 *         <code>false</code> otherwise.
	 */
	boolean supports(String property, CSSValue value);

}
