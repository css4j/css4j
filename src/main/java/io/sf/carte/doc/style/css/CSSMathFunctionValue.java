/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2023-2024 Carlos Amengual.
 */
/*
 * SPDX-License-Identifier: W3C-19980720
 */

package io.sf.carte.doc.style.css;

/**
 * A mathematical function value.
 * 
 * @see MathFunction
 */
public interface CSSMathFunctionValue extends CSSFunctionValue, CSSMathValue {

	/**
	 * Enumeration of mathematical functions.
	 */
	enum MathFunction {
		/** {@code abs()} */
		ABS,

		/** {@code max()} */
		MAX,

		/** {@code min()} */
		MIN,

		/** {@code clamp()} */
		CLAMP,

		/** {@code sign()} */
		SIGN,

		/** {@code sin()} */
		SIN,

		/** {@code cos()} */
		COS,

		/** {@code tan()} */
		TAN,

		/** {@code asin()} */
		ASIN,

		/** {@code acos()} */
		ACOS,

		/** {@code atan()} */
		ATAN,

		/** {@code atan2()} */
		ATAN2,

		/** {@code pow()} */
		POW,

		/** {@code sqrt()} */
		SQRT,

		/** {@code hypot()} */
		HYPOT
	}

	/**
	 * Get the function type.
	 * 
	 * @return the function type.
	 */
	MathFunction getFunction();

	/**
	 * Performs a dimensional analysis of this function and computes the unit type
	 * of the result.
	 * 
	 * @return the unit type of the result, as in {@link CSSUnit}.
	 */
	@Override
	short computeUnitType();

	@Override
	CSSMathFunctionValue clone();

}
