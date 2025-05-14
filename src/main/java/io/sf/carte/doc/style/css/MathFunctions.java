/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;

/**
 * Index of CSS mathematical functions.
 * 
 * @deprecated Please use the ordinals of {@link MathFunction} constants
 *             instead.
 */
@Deprecated(forRemoval = true)
public interface MathFunctions {

	/**
	 * Index for {@code abs()} function.
	 */
	int ABS = MathFunction.ABS.ordinal();

	/**
	 * Index for {@code clamp()} function.
	 */
	int CLAMP = MathFunction.CLAMP.ordinal();

	/**
	 * Index for {@code max()} function.
	 */
	int MAX = MathFunction.MAX.ordinal();

	/**
	 * Index for {@code min()} function.
	 */
	int MIN = MathFunction.MIN.ordinal();

	/**
	 * Index for {@code round()} function.
	 */
	int ROUND = MathFunction.ROUND.ordinal();

	/**
	 * Index for {@code mod()} function.
	 */
	int MOD = MathFunction.MOD.ordinal();

	/**
	 * Index for {@code rem()} function.
	 */
	int REM = MathFunction.REM.ordinal();

	/**
	 * Index for {@code hypot()} function.
	 */
	int HYPOT = MathFunction.HYPOT.ordinal();

	/**
	 * Index for {@code hypot2()} function.
	 */
	int HYPOT2 = MathFunction.HYPOT2.ordinal();

	/**
	 * Index for {@code log()} function.
	 */
	int LOG = MathFunction.LOG.ordinal();

	/**
	 * Index for {@code exp()} function.
	 */
	int EXP = MathFunction.EXP.ordinal();

	/**
	 * Index for {@code sqrt()} function.
	 */
	int SQRT = MathFunction.SQRT.ordinal();

	/**
	 * Index for {@code pow()} function.
	 */
	int POW = MathFunction.POW.ordinal();

	/**
	 * Index for {@code sign()} function.
	 */
	int SIGN = MathFunction.SIGN.ordinal();

	/**
	 * Index for {@code sin()} function.
	 */
	int SIN = MathFunction.SIN.ordinal();

	/**
	 * Index for {@code cos()} function.
	 */
	int COS = MathFunction.COS.ordinal();

	/**
	 * Index for {@code tan()} function.
	 */
	int TAN = MathFunction.TAN.ordinal();

	/**
	 * Index for {@code asin()} function.
	 */
	int ASIN = MathFunction.ASIN.ordinal();

	/**
	 * Index for {@code acos()} function.
	 */
	int ACOS = MathFunction.ACOS.ordinal();

	/**
	 * Index for {@code atan()} function.
	 */
	int ATAN = MathFunction.ATAN.ordinal();

	/**
	 * Index for {@code atan2()} function.
	 */
	int ATAN2 = MathFunction.ATAN2.ordinal();

	/**
	 * The number of indexes.
	 */
	int INDEX_COUNT = MathFunction.OTHER.ordinal();

}
