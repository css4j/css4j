/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2019 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * A CSS environment variable value.
 */
public interface CSSEnvVariableValue extends CSSPrimitiveValue {

	/**
	 * Get the name of this environment variable.
	 * 
	 * @return the name of this environment variable.
	 */
	String getName();

	/**
	 * Get the fallback value to be used if the referenced variable name is not known.
	 * 
	 * @return the fallback value, or null if there is no fallback value.
	 */
	CSSValue getFallback();

	@Override
	CSSEnvVariableValue clone();

}
