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
 * <code>{@literal @}property</code> rule.
 * @see <a href=
 * "https://www.w3.org/TR/css-properties-values-api-1/#at-property-rule">CSS
 * Properties and Values API Level 1</a>.
 */
public interface CSSPropertyRule extends CSSRule, CSSPropertyDefinition {

	/**
	 * Gets the custom property name.
	 * 
	 * @return the property name.
	 */
	@Override
	String getName();

	/**
	 * Whether the property inherits or not.
	 * 
	 * @return {@code true} if the property inherits.
	 */
	@Override
	boolean inherits();

	/**
	 * The initial value associated with the <code>{@literal @}property</code> rule.
	 * 
	 * @return the initial value, or {@code null} if none was specified.
	 */
	@Override
	CSSLexicalValue getInitialValue();

	/**
	 * The syntax associated with the <code>{@literal @}property</code> rule.
	 * 
	 * @return the syntax, or {@code null} if invalid.
	 */
	@Override
	CSSValueSyntax getSyntax();

}
