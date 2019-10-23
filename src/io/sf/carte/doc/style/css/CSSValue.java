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

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.util.SimpleWriter;

/**
 * A CSS style value.
 *
 */
public interface CSSValue extends Cloneable {

	/**
	 * The main categories of values.
	 */
	enum CssType {

		/**
		 * A CSS-wide keyword like {@code inherit}.
		 */
		KEYWORD,

		/**
		 * A proxy primitive value like {@code var()} or {@code attr()}.
		 */
		PROXY,

		/**
		 * A typed primitive value, includes numbers and identifiers.
		 */
		TYPED,

		/**
		 * A list of values.
		 */
		LIST,

		/**
		 * A shorthand property.
		 * <p>
		 * Declared shorthands can be retrieved from style declarations.
		 * </p>
		 */
		SHORTHAND
	}

	/**
	 * The type of value. For keywords, it is the keyword.
	 */
	enum Type {
		/**
		 * Unknown type, probably a system default or a compat value.
		 */
		UNKNOWN,

		/**
		 * {@code inherit} keyword.
		 */
		INHERIT,

		/**
		 * {@code initial} keyword.
		 */
		INITIAL,

		/**
		 * {@code unset} keyword.
		 */
		UNSET,

		/**
		 * {@code revert} keyword.
		 */
		REVERT,

		/**
		 * Numeric type.
		 */
		NUMERIC,

		/**
		 * String.
		 */
		STRING,

		/**
		 * Identifier.
		 */
		IDENT,

		/**
		 * RGB color.
		 */
		RGBCOLOR,

		/**
		 * URI.
		 */
		URI,

		/**
		 * {@code rect()} function.
		 * <p>
		 * See {@link CSSRectValue}.
		 */
		RECT,

		/**
		 * An expression with algebraic syntax (i.e. <code>calc()</code>).
		 * <p>
		 * See {@link CSSExpressionValue}.
		 */
		EXPRESSION,

		/**
		 * Gradient function.
		 */
		GRADIENT,

		/**
		 * CSS <code>counter()</code> function.
		 * <p>
		 * See {@link CSSCounterValue}.
		 */
		COUNTER,

		/**
		 * CSS <code>counters()</code> function.
		 * <p>
		 * See {@link CSSCountersValue}.
		 */
		COUNTERS,

		/**
		 * Function. See {@link CSSFunctionValue}.
		 * <p>
		 * On functions, {@link CSSTypedValue#getStringValue()} must return the function name.
		 */
		FUNCTION,

		/**
		 * Unicode range. See {@link CSSUnicodeRangeValue}.
		 */
		UNICODE_RANGE,

		/**
		 * Unicode character. See {@link CSSUnicodeRangeValue.CSSUnicodeValue}.
		 */
		UNICODE_CHARACTER,

		/**
		 * Unicode wildcard. See {@link CSSUnicodeRangeValue}.
		 */
		UNICODE_WILDCARD,

		/**
		 * Element reference.
		 */
		ELEMENT_REFERENCE,

		/**
		 * Ratio value.
		 */
		RATIO,

		/**
		 * {@code attr()} function.
		 */
		ATTR,

		/**
		 * Custom property reference. See {@link CSSCustomPropertyValue}.
		 */
		VAR,

		/**
		 * Environment variable. See {@link CSSEnvVariableValue}.
		 */
		ENV,

		/**
		 * Lexical value. See {@link CSSLexicalValue}.
		 */
		LEXICAL,

		/**
		 * For this library's internal use.
		 */
		INTERNAL,

		/**
		 * Invalid (non-primitive and non-keyword) value.
		 * <p>
		 * The value is either a list or a shorthand.
		 * </p>
		 */
		INVALID

	}

	/**
	 * Get the general category to which this value belongs.
	 * 
	 * @return the general value type.
	 */
	CssType getCssValueType();

	/**
	 * Get the primitive type.
	 * 
	 * @return the primitive type.
	 */
	Type getPrimitiveType();

	/**
	 * Get a parsable representation of this value.
	 * 
	 * @return the CSS serialization of this value.
	 */
	String getCssText();

	/**
	 * Set this value according to the given parsable text.
	 * 
	 * @param cssText the text value.
	 * @throws DOMException INVALID_MODIFICATION_ERR if the text value represents a
	 *                      different type or the value cannot be modified. <br/>
	 *                      INVALID_CHARACTER_ERR if an invalid character was found.
	 *                      <br/>
	 *                      SYNTAX_ERR if the string is invalid CSS.
	 */
	void setCssText(String cssText) throws DOMException;

	/**
	 * Creates and returns a copy of this value.
	 *
	 * @return a clone of this value.
	 */
	CSSValue clone();

	/**
	 * Gives a minified version of the css text of the property.
	 * <p>
	 * It may be customized for the given property name.
	 * </p>
	 *
	 * @param propertyName the property name.
	 * @return the minified css text.
	 */
	String getMinifiedCssText(String propertyName);

	/**
	 * Serialize this value to a {@link SimpleWriter}.
	 *
	 * @param wri the SimpleWriter.
	 * @throws IOException if an error happened while writing.
	 */
	void writeCssText(SimpleWriter wri) throws IOException;

}
