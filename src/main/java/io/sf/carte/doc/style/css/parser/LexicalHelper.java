/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.TransformFunctions;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

/**
 * Helper methods related to CSS lexical units.
 */
public class LexicalHelper {

	/**
	 * Get the number of supported {@code <basic-shape>} functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported {@code <basic-shape>} functions.
	 */
	public static int getBasicShapeIndexCount() {
		return LexicalType.XYWH_FUNCTION.ordinal() - LexicalType.RECT_FUNCTION.ordinal() + 1;
	}

	/**
	 * Get the number of supported {@code <color>} functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported {@code <color>} functions.
	 */
	public static int getColorIndexCount() {
		return LexicalType.COLOR_MIX.ordinal() - LexicalType.RGBCOLOR.ordinal() + 1;
	}

	/**
	 * Get the number of supported {@code <counter>} functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported {@code <counter>} functions.
	 */
	public static int getCounterIndexCount() {
		return LexicalType.COUNTERS_FUNCTION.ordinal() - LexicalType.COUNTER_FUNCTION.ordinal() + 1;
	}

	/**
	 * Get the number of supported easing functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported easing functions.
	 */
	public static int getEasingFunctionIndexCount() {
		return LexicalType.STEPS_FUNCTION.ordinal() - LexicalType.CUBIC_BEZIER_FUNCTION.ordinal()
				+ 1;
	}

	/**
	 * Get the number of supported {@code <image>} functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported {@code <image>} functions.
	 */
	public static int getImageFunctionIndexCount() {
		return LexicalType.ELEMENT_REFERENCE.ordinal() - LexicalType.GRADIENT.ordinal() + 1;
	}

	/**
	 * Get the number of explicitly supported mathematical functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported mathematical functions.
	 */
	public static int getMathFunctionIndexCount() {
		return MathFunction.OTHER.ordinal();
	}

	/**
	 * Get the number of supported transform functions.
	 * <p>
	 * This method can be useful to dimension arrays.
	 * </p>
	 * 
	 * @return the number of supported transform functions.
	 */
	public static int getTransformFunctionIndexCount() {
		return TransformFunctions.OTHER.ordinal();
	}

}
