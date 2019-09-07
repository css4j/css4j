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

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Adds new methods and assigned units to W3C's {@link CSSPrimitiveValue}.
 */
public interface CSSPrimitiveValue2 extends CSSPrimitiveValue {

	/**
	 * The value is a font-relative length (cap). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_CAP = 26;

	/**
	 * The value is a font-relative length (ch). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_CH = 27;

	/**
	 * The value is a font-relative length (ic). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_IC = 28;

	/**
	 * The value is a font-relative length (lh). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_LH = 29;

	/**
     * The value is a length (Q). The value can be obtained by using the
     * <code>getFloatValue</code> method.
     */
	short CSS_QUARTER_MM = 30;

	/**
	 * The value is a font-relative length (rem). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_REM = 31;

	/**
	 * The value is a font-relative length (rlh). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_RLH = 32;

	/**
     * The value is an angle (turn). The value can be obtained by using the
     * <code>getFloatValue</code> method.
     */
	short CSS_TURN = 33;

	/**
	 * The value is a viewport-percentage length (vb). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_VB = 34;

	/**
	 * The value is a viewport-percentage length (vh). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_VH = 35;

	/**
	 * The value is a viewport-percentage length (vi). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_VI = 36;

	/**
	 * The value is a viewport-percentage length (vmax). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_VMAX = 37;

	/**
	 * The value is a viewport-percentage length (vmin). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_VMIN = 38;

	/**
	 * The value is a viewport-percentage length (vw). The value can be obtained by using the
	 * <code>getFloatValue</code> method.
	 */
	short CSS_VW = 39;

	/**
     * The value is a resolution (dpi). The value can be obtained by using the
     * <code>getFloatValue</code> method.
     */
	short CSS_DPI = 40;

	/**
     * The value is a resolution (dpcm). The value can be obtained by using the
     * <code>getFloatValue</code> method.
     */
	short CSS_DPCM = 41;

	/**
     * The value is a resolution (dppx). The value can be obtained by using the
     * <code>getFloatValue</code> method.
     */
	short CSS_DPPX = 42;

	/**
	 * The value is a flexible length (fr). Note that it is <b>not</b> a length. The value can
	 * be obtained by using the <code>getFloatValue</code> method.
	 */
	short CSS_FR = 43;

	/**
	 * An expression with algebraic syntax (i.e. <code>calc()</code>).
	 * <p>
	 * See {@link CSSExpressionValue}.
	 */
	short CSS_EXPRESSION = 101;

	/**
	 * Gradient function.
	 */
	short CSS_GRADIENT = 126;

	/**
	 * Function. See {@link CSSFunctionValue}.
	 * <p>
	 * On functions, {@link #getStringValue()} must return the function name.
	 */
	short CSS_FUNCTION = 127;

	/**
	 * Custom property. See {@link CSSCustomPropertyValue}.
	 * <p>
	 * On custom property values, {@link #getStringValue()} must return the custom property
	 * name.
	 */
	short CSS_CUSTOM_PROPERTY = 128;

	/**
	 * environment variable. See {@link CSSEnvVariableValue}.
	 * <p>
	 * On environment variable values, {@link #getStringValue()} must return the environment
	 * variable name.
	 */
	short CSS_ENV_VAR = 129;

	/**
	 * Unicode range. See {@link CSSUnicodeRangeValue}.
	 */
	short CSS_UNICODE_RANGE = 130;

	/**
	 * Unicode character. See {@link CSSUnicodeRangeValue.CSSUnicodeValue}.
	 */
	short CSS_UNICODE_CHARACTER = 131;

	/**
	 * Unicode wildcard. See {@link CSSUnicodeRangeValue}.
	 */
	short CSS_UNICODE_WILDCARD = 132;

	/**
	 * Element reference.
	 */
	short CSS_ELEMENT_REFERENCE = 133;

	/**
	 * CSS <code>counters()</code> function.
	 * <p>
	 * See {@link CSSCountersValue}.
	 */
	short CSS_COUNTERS = 134;

	/**
	 * CSS ratio value (media queries).
	 */
	short CSS_RATIO = 135;

	/**
	 * Get the RGBA color. If this CSS value doesn't contain a RGB color value, a
	 * <code>DOMException</code> is raised.
	 *
	 * @return the RGBA color value.
	 * @exception DOMException
	 *                INVALID_ACCESS_ERR: Raised if the attached property can't return a RGB
	 *                color value (e.g. this is not <code>CSS_RGBCOLOR</code>).
	 */
	@Override RGBAColor getRGBColorValue() throws DOMException;

}
