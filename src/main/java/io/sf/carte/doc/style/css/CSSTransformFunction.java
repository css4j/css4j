/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * A transform function value.
 */
public interface CSSTransformFunction extends CSSFunctionValue {

	/**
	 * Get the transform function ID from {@link TransformFunctions}.
	 * 
	 * @return the function type.
	 */
	TransformFunctions getFunction();

	/**
	 * Gives the index of this mathematical function.
	 * 
	 * @return the function index.
	 */
	int getFunctionIndex();

	@Override
	CSSTransformFunction clone();

}
