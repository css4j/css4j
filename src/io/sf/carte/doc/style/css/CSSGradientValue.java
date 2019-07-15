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
 * A gradient value.
 * 
 * @see GradientType
 */
public interface CSSGradientValue extends CSSFunctionValue {

	/**
	 * Enumeration of gradient types.
	 */
	public enum GradientType {
		LINEAR_GRADIENT, RADIAL_GRADIENT, CONIC_GRADIENT,
		REPEATING_LINEAR_GRADIENT, REPEATING_RADIAL_GRADIENT, REPEATING_CONIC_GRADIENT,
		OTHER_GRADIENT
	}

	/**
	 * Get the gradient type.
	 * 
	 * @return the gradient type.
	 */
	GradientType getGradientType();

}