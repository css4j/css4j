/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2005-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;

/**
 * A typed value.
 * <p>
 * Typed values include strings, identifiers, lengths, etc.
 * </p>
 */
public interface CSSTypedValue extends CSSPrimitiveValue {

	/**
	 * If this value is numeric, set a float value with the given unit.
	 * 
	 * @param unitType   the unit type according to {@link CSSUnit}.
	 * @param floatValue the float value.
	 * @throws DOMException INVALID_ACCESS_ERR if the unit is not a {@link CSSUnit}
	 *                      one, or this value is not a number. <br/>
	 *                      NO_MODIFICATION_ALLOWED_ERR if this value is
	 *                      unmodifiable.
	 */
	default void setFloatValue(short unitType, float floatValue) throws DOMException {
		throw new DOMInvalidAccessException("Not a float value.");
	}

	/**
	 * If this is a number, get its float value in the requested unit.
	 * 
	 * @param unitType the requested unit type.
	 * @return the float value in the requested unit.
	 * @throws DOMException INVALID_ACCESS_ERR if this value is not a number value
	 *                      or it could not be transformed to the desired unit (for
	 *                      example a relative value converted to an absolute one,
	 *                      for which a context is needed).
	 */
	float getFloatValue(short unitType) throws DOMException;

	/**
	 * If this is a number, get its float value in the current unit.
	 * 
	 * @return The float value.
	 * @throws DOMException INVALID_ACCESS_ERR if this value is not a number value
	 *                      or it could not be transformed to the desired unit (for
	 *                      example a relative value converted to an absolute one,
	 *                      for which a context is needed).
	 */
	default float getFloatValue() {
		throw new DOMInvalidAccessException("Not a float value.");
	}

	/**
	 * If this value is a string, identifier, URI, unicode wildcard or element
	 * reference, set its value.
	 * <p>
	 * The string value has to be supplied unescaped and unquoted. If it is a
	 * unicode wildcard, it must not have the preceding {@code U+}.
	 * </p>
	 * 
	 * @param stringType  the type of value.
	 * @param stringValue the string value.
	 * @throws DOMException INVALID_ACCESS_ERR if the requested type of value is
	 *                      different to this one, or this value does not accept
	 *                      strings. <br/>
	 *                      NO_MODIFICATION_ALLOWED_ERR if this value is
	 *                      unmodifiable.
	 */
	default void setStringValue(Type stringType, String stringValue) throws DOMException {
		if (stringType != getPrimitiveType()) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Type not supported.");
		}
		throw new DOMInvalidAccessException("Cannot be modified as a string.");
	}

	/**
	 * If this value represents a string value, get it.
	 * <p>
	 * This method is useful for values like strings, identifiers, URIs, element
	 * references, etc. For functions, it returns the function name.
	 * </p>
	 * 
	 * @return the string value.
	 * @throws DOMException INVALID_ACCESS_ERR if this value is not a string.
	 */
	default String getStringValue() throws DOMException {
		throw new DOMInvalidAccessException("Not a string/ident/URI.");
	}

	/**
	 * If this value represents a color, get it or transform to a RGB color.
	 * <p>
	 * If the color does not map into the sRGB gamut, it is clamped.
	 * </p>
	 * <p>
	 * This method was intended to replace the old {@code getRGBColorValue()} and
	 * was named similarly, but to avoid confusions it was deprecated in favor of
	 * {@link #toRGBColor()}.
	 * </p>
	 *
	 * @return the RGBA color.
	 * @exception DOMException INVALID_ACCESS_ERR: if this value can't return a RGB
	 *                         color value (either is not a <code>COLOR</code> or
	 *                         not a typed value).<br/>
	 *                         NOT_SUPPORTED_ERR: if the conversion needs device
	 *                         color space information to be performed accurately.
	 * @deprecated
	 * @see #toRGBColor()
	 */
	@Deprecated
	default RGBAColor toRGBColorValue() throws DOMException {
		return toRGBColor();
	}

	/**
	 * If this value represents a color, get it or transform to a RGB color.
	 * <p>
	 * If the color does not map into the sRGB gamut, it is clamped.
	 * </p>
	 *
	 * @return the RGBA color.
	 * @exception DOMException INVALID_ACCESS_ERR: if this value can't return a RGB
	 *                         color value (either is not a <code>COLOR</code> or not
	 *                         a typed value).<br/>
	 *                         NOT_SUPPORTED_ERR: if the conversion needs device
	 *                         color space information to be performed accurately.
	 */
	default RGBAColor toRGBColor() throws DOMException {
		throw new DOMInvalidAccessException("Not a color.");
	}

	/**
	 * Test whether this is a numeric value that was the result of a
	 * <code>calc()</code> expression.
	 * 
	 * @return <code>true</code> if this is a numeric value, and it was produced as
	 *         the output of a calculation (instead of declared as a plain, constant
	 *         numeric value).
	 */
	default boolean isCalculatedNumber() {
		return false;
	}

	/**
	 * Is this value a number set to a value of zero ?
	 *
	 * @return <code>true</code> if this is a number and is set to zero.
	 */
	default boolean isNumberZero() {
		return false;
	}

	@Override
	CSSTypedValue clone();

}
