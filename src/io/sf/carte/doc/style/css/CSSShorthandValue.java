/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.util.Set;

/**
 * Shorthand value.
 * <p>
 * These values are of <code>CSSValue.CSS_CUSTOM</code> type.
 */
public interface CSSShorthandValue extends ExtendedCSSValue {

	/**
	 * Is the value of important priority ?
	 * 
	 * @return <code>true</code> if the priority is important.
	 */
	boolean isImportant();

	/**
	 * Get the set of longhand properties that this shorthand is responsible for.
	 * 
	 * @return the set of longhand properties.
	 */
	Set<String> getLonghands();

}
