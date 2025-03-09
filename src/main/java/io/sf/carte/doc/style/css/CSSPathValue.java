/*

 Copyright (c) 2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a {@code path()} function.
 * <p>
 * This is an incubating interface and may change in the future.
 * </p>
 */
public interface CSSPathValue extends CSSShapeValue {

	/**
	 * The fill rule.
	 * 
	 * @return the fill rule, or {@code null} if none.
	 */
	CSSTypedValue getFillRule();

	/**
	 * The string representing the path.
	 * 
	 * @return the path.
	 */
	String getPath();

}
