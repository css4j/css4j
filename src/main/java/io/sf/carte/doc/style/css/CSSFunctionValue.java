/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2017-2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * A function value.
 */
public interface CSSFunctionValue extends CSSTypedValue {

	/**
	 * Get the arguments of this function.
	 * 
	 * @return the list of arguments of this function.
	 */
	CSSValueList<? extends CSSValue> getArguments();

	/**
	 * Get the function name.
	 * 
	 * @return the function name.
	 */
	String getFunctionName();

	@Override
	CSSFunctionValue clone();

}
