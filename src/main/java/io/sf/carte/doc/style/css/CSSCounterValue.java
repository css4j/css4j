/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * A CSS <code>counter()</code> function.
 */
public interface CSSCounterValue extends CSSTypedValue {

	/**
	 * Get the name of this counter.
	 * 
	 * @return the name of this counter.
	 */
	String getName();

	/**
	 * Get the counter style.
	 * 
	 * @return the counter style, or <code>null</code> if style is the default.
	 */
	CSSPrimitiveValue getCounterStyle();

	/**
	 * Get the separator.
	 * <p>
	 * In <code>counter()</code> values, returns the empty string.
	 * </p>
	 * @return the separator.
	 */
	default String getSeparator() {
		return "";
	}

}
