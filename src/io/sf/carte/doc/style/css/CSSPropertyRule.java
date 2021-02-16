/*
 * This software includes interfaces defined by CSS Properties and Values API Level 1
 *  (https://drafts.css-houdini.org/css-properties-values-api-1/).
 * Copyright © 2020 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * <code>{@literal @}property</code> rule. @see <a href=
 * "https://drafts.css-houdini.org/css-properties-values-api-1/#at-property-rule">CSS
 * Properties and Values API Level 1</a>.
 */
public interface CSSPropertyRule extends CSSRule {

	/**
	 * Gets the custom property name.
	 * 
	 * @return the counter-style name.
	 */
	String getName();

	/**
	 * Whether the property inherits or not.
	 * 
	 * @return {@code true} if the property inherits.
	 */
	boolean inherits();

	/**
	 * The initial value associated with the <code>{@literal @}property</code> rule.
	 * 
	 * @return the initial value, or {@code null} if none was specified.
	 */
	CSSValue getInitialValue();

}
