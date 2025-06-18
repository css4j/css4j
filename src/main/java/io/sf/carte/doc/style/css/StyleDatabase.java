/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * CSS Style database.
 * <p>
 * To use CSS in practice, you need to have information about the target
 * rendering device: available fonts, page sizes, etc. This information is
 * supplied by an instance of the <code>StyleDatabase</code> interface.
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
	 * @param initialColor the String representing default color value.
	 */
	void setInitialColor(String initialColor);

	/**
	 * Gets the name of the default font used when a generic font family (serif,
	 * sans-serif, monospace, cursive, fantasy) is specified.
	 *
	 * @param genericFamily the name of the logical font.
	 * @return the name of the real font to which the generic name is mapped to, or
	 *         null if none.
	 */
	String getDefaultGenericFontFamily(String genericFamily);

	/**
	 * Gets the real name of the default font.
	 * <p>
	 * For example, if the default generic font name is 'serif', this method should
	 * return the same as getDefaultGenericFontFamily("serif").
	 *
	 * @return the name of the default real font.
	 */
	default String getDefaultGenericFontFamily() {
		return getDefaultGenericFontFamily("serif");
	}

	/**
	 * Gets the used font family name according to the given style.
	 *
	 * @param computedStyle the computed style.
	 * @return the name of the used font family.
	 */
	String getUsedFontFamily(CSSComputedProperties computedStyle);

	/**
	 * Gets the font size from the given size identifier (small, medium, etc.),
	 * expressed in typographic points.
	 *
	 * @param familyName         the font family name. Could be null.
	 * @param fontSizeIdentifier the font size identifier.
	 * @return the font size.
	 * @throws DOMException if the identifier is unknown.
	 */
	default float getFontSizeFromIdentifier(String familyName, String fontSizeIdentifier)
		throws DOMException {
		// Normalize to device resolution
		float factor = Math.max(0.9f, getDeviceWidth() / 595f);
		float sz;
		if (fontSizeIdentifier.equals("xx-small")) {
			sz = 8f * factor;
		} else if (fontSizeIdentifier.equals("x-small")) {
			sz = 9f * factor;
		} else if (fontSizeIdentifier.equals("small")) {
			sz = 10f * factor;
		} else if (fontSizeIdentifier.equals("medium")) {
			sz = 12f * factor;
		} else if (fontSizeIdentifier.equals("large")) {
			sz = 14f * factor;
		} else if (fontSizeIdentifier.equals("x-large")) {
			sz = 18f * factor;
		} else if (fontSizeIdentifier.equals("xx-large")) {
			sz = 24f * factor;
		} else {
			throw new DOMInvalidAccessException(
				"Unknown size identifier: " + fontSizeIdentifier);
		}
		return sz;
	}

	/**
	 * Gives the style declaration adequate to provide the given system font.
	 *
	 * @param systemFontName the system font name.
	 * @return the style declaration, or null if the system font name was not
	 *         recognized.
	 */
	default String getSystemFontDeclaration(String systemFontName) {
		return null;
	}

	/**
	 * Is <code>requestedFamily</code> an available font family loaded by a font
	 * face rule?
	 *
	 * @param requestedFamily the font family name in lower case.
	 * @return <code>true</code> if is an available font family, <code>false</code>
	 *         otherwise.
	 */
	boolean isFontFaceName(String requestedFamily);

	/**
	 * Try to load the font family according to the given font face rule, and make
	 * it available to this object.
	 *
	 * @param rule the font face rule.
	 */
	void loadFontFaceRule(CSSFontFaceRule rule);

	/**
	 * Gets the identifier of the device's natural unit.
	 *
	 * @return the unit identifier as in {@link CSSUnit}.
	 */
	default short getNaturalUnit() {
		return CSSUnit.CSS_PX;
	}

	/**
	 * Gives the size of the 'ex' unit, expressed in 'pt' (typographic points) unit.
	 * <p>
	 * Although this type of information really belongs to <code>CSSCanvas</code>,
	 * having even an approximate value here is helpful.
	 * </p>
	 *
	 * @param familyName the font family name.
	 * @param size       the font size in 'pt' units.
	 * @return the size of the 'ex' unit, in units of 'pt'.
	 */
	default float getExSizeInPt(String familyName, float size) {
		return Math.round(0.5f * size);
	}

	/**
	 * Gets the size corresponding to the given identifier (thin, thick, medium), in
	 * typographic points.
	 * <p>
	 * See CSSWG issue #7254.
	 * </p>
	 *
	 * @param widthIdentifier the CSS width identifier.
	 * @param fontSize        the font size used by the box, in typographic points.
	 * @return the size.
	 * @throws DOMException if the identifier is unknown.
	 * @deprecated Since May 2022 the identifiers have a fixed value (CSSWG issue
	 *             #7254).
	 */
	@Deprecated
	default float getWidthSize(String widthIdentifier, float fontSize) throws DOMException {
		if ("thin".equalsIgnoreCase(widthIdentifier)) {
			return 0.75f; // 1px
		} else if ("thick".equalsIgnoreCase(widthIdentifier)) {
			return 3.75f; // 5px
		} else if ("medium".equalsIgnoreCase(widthIdentifier)) {
			return 2.25f; // 3px
		} else {
			throw new DOMSyntaxException("Unknown identifier " + widthIdentifier);
		}
	}

	/**
	 * Gives the number of bits allocated to colors (excluding the alpha channel) in
	 * the output device.
	 * <p>
	 * If the device is not a color device, the value is zero.
	 * </p>
	 * <p>
	 * From W3C's Screen interface.
	 *
	 * @return the number of bits allocated to colors, or zero if the output device
	 *         does not support colors.
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
	 * Get the value of the given {@code env} variable.
	 * 
	 * @param envVarName the {@code env} variable name.
	 * @return the value, or {@code null} if that name has no value set.
	 */
	default CSSValue getEnvValue(String envVarName) {
		return null;
	}

	/**
	 * Does this agent support all of the given selectors?
	 * 
	 * @param selectors the selectors.
	 * @return <code>true</code> if supported.
	 */
	default boolean supports(SelectorList selectors) {
		return true;
	}

	/**
	 * Does this medium support the given property-value pair?
	 *
	 * @param property the property name.
	 * @param value    the optional property value to be tested against.
	 * @return <code>true</code> if the property (with the given value, if any) is
	 *         supported, <code>false</code> otherwise.
	 */
	default boolean supports(String property, LexicalUnit value) {
		return false;
	}

	/**
	 * Does this medium support the given property-value pair?
	 *
	 * @deprecated Use {@link #supports(String, LexicalUnit)}.
	 * @param property the property name.
	 * @param value    the optional property value to be tested against.
	 * @return <code>true</code> if the property (with the given value, if any) is
	 *         supported, <code>false</code> otherwise.
	 */
	@Deprecated(forRemoval = true)
	default boolean supports(String property, CSSValue value) {
		return false;
	}

}
