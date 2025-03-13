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
		/**
		 * {@code abs()} function.
		 */
		ABS,

		/**
		 * {@code clamp()} function.
		 */
		CLAMP,

		/**
		 * {@code max()} function.
		 */
		MAX,

		/**
		 * {@code min()} function.
		 */
		MIN,

		/**
		 * {@code round()} function.
		 */
		ROUND,

		/**
		 * {@code mod()} function.
		 */
		MOD,

		/**
		 * {@code rem()} function.
		 */
		REM,

		/**
		 * {@code hypot()} function.
		 */
		HYPOT,

		/**
		 * {@code hypot2()} function.
		 */
		HYPOT2,

		/**
		 * {@code log()} function.
		 */
		LOG,

		/**
		 * {@code exp()} function.
		 */
		EXP,

		/**
		 * {@code sqrt()} function.
		 */
		SQRT,

		/**
		 * {@code pow()} function.
		 */
		POW,

		/**
		 * {@code sign()} function.
		 */
		SIGN,

		/**
		 * {@code sin()} function.
		 */
		SIN,

		/**
		 * {@code cos()} function.
		 */
		COS,

		/**
		 * {@code tan()} function.
		 */
		TAN,

		/**
		 * {@code asin()} function.
		 */
		ASIN,

		/**
		 * {@code acos()} function.
		 */
		ACOS,

		/**
		 * {@code atan()} function.
		 */
		ATAN,

		/**
		 * {@code atan2()} function.
		 */
		ATAN2,

		/**
		 * Mainly used to know the number of functions via the ordinal.
		 * <p>
		 * Must always be the last declared constant.
		 * </p>
		 */
		OTHER;
	}

	/**
	 * Get the function type.
	 * 
	 * @return the function type.
	 */
	MathFunction getFunction();

	/**
	 * Gives the index of this mathematical function.
	 * 
	 * @return the function index.
	 */
	int getFunctionIndex();

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
