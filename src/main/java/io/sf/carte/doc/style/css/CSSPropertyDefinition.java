/*
 * This software includes interfaces defined by CSS Properties and Values API Level 1
 *  (https://www.w3.org/TR/css-properties-values-api-1/).
 * Copyright © 2020 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * A property definition.
 * <p>
 * See <a href=
 * "https://www.w3.org/TR/css-properties-values-api-1/#the-propertydefinition-dictionary">CSS
 * Properties and Values API Level 1</a>.
 * </p>
 */
public interface CSSPropertyDefinition {

	/**
	 * Gets the property name.
	 * 
	 * @return the property name.
	 */
	String getName();

	/**
	 * Whether the property inherits or not.
	 * 
	 * @return {@code true} if the property inherits.
	 */
	boolean inherits();

	/**
	 * The initial value associated with the property.
	 * 
	 * @return the initial value, or {@code null} if none was specified.
	 */
	CSSLexicalValue getInitialValue();

	/**
	 * The syntax associated with the property.
	 * 
	 * @return the syntax, or {@code null} if invalid.
	 */
	CSSValueSyntax getSyntax();

}
