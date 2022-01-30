/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * CSS numeric units.
 */
public interface CSSUnit {

	/**
	 * Dimensionless number.
	 */
	short CSS_NUMBER = 0;

	/**
	 * Number with an unknown dimension.
	 */
	short CSS_OTHER = 1;

	/**
	 * Percentage.
	 */
	short CSS_PERCENTAGE = 2;

	/**
	 * Length ({@code px}).
	 */
	short CSS_PX = 3;

	/**
	 * Length ({@code in}).
	 */
	short CSS_IN = 4;

	/**
	 * Length ({@code pc}).
	 */
	short CSS_PC = 5;

	/**
	 * Length ({@code pt}).
	 */
	short CSS_PT = 6;

	/**
	 * Length ({@code cm}).
	 */
	short CSS_CM = 7;

	/**
	 * Length ({@code mm}).
	 */
	short CSS_MM = 8;

	/**
     * Length ({@code Q}).
     */
	short CSS_QUARTER_MM = 9;

	/**
	 * Font-relative length ({@code em}).
	 */
	short CSS_EM = 20;

	/**
	 * Font-relative length ({@code ex}).
	 */
	short CSS_EX = 21;

	/**
	 * Font-relative length ({@code cap}).
	 */
	short CSS_CAP = 22;

	/**
	 * Font-relative length ({@code ch}).
	 */
	short CSS_CH = 23;

	/**
	 * Font-relative length ({@code ic}).
	 */
	short CSS_IC = 24;

	/**
	 * Font-relative length ({@code lh}).
	 */
	short CSS_LH = 25;

	/**
	 * Font-relative length ({@code rem}).
	 */
	short CSS_REM = 26;

	/**
	 * Font-relative length ({@code rlh}).
	 */
	short CSS_RLH = 27;

	/**
	 * Viewport-percentage length ({@code vb}).
	 */
	short CSS_VB = 40;

	/**
	 * Viewport-percentage length ({@code vh}).
	 */
	short CSS_VH = 41;

	/**
	 * Viewport-percentage length ({@code vi}).
	 */
	short CSS_VI = 42;

	/**
	 * Viewport-percentage length ({@code vmax}).
	 */
	short CSS_VMAX = 43;

	/**
	 * Viewport-percentage length ({@code vmin}).
	 */
	short CSS_VMIN = 44;

	/**
	 * Viewport-percentage length ({@code vw}).
	 */
	short CSS_VW = 45;

	/**
     * Resolution ({@code dpi}).
     */
	short CSS_DPI = 60;

	/**
     * Resolution ({@code dpcm}).
     */
	short CSS_DPCM = 61;

	/**
     * Resolution ({@code dppx}).
     */
	short CSS_DPPX = 62;

	/**
	 * Flexible length ({@code fr}). Note that it is <b>not</b> a length.
	 */
	short CSS_FR = 70;

	/**
	 * Angle ({@code deg}).
	 */
	short CSS_DEG = 80;

	/**
	 * Angle ({@code rad}).
	 */
	short CSS_RAD = 81;

	/**
	 * Angle ({@code grad}).
	 */
	short CSS_GRAD = 82;

	/**
     * Angle ({@code turn}).
     */
	short CSS_TURN = 83;

	/**
	 * Time ({@code s}).
	 */
	short CSS_S = 90;

	/**
	 * Time ({@code ms}).
	 */
	short CSS_MS = 91;

	/**
	 * Frequency ({@code Hz}).
	 */
	short CSS_HZ = 100;

	/**
	 * Frequency ({@code kHz}).
	 */
	short CSS_KHZ = 101;

	/**
	 * Invalid CSS unit.
	 */
	short CSS_INVALID = 255;

	/**
	 * Check whether the given unit is a length.
	 * <p>
	 * Percentage is not considered an explicit length.
	 * </p>
	 * 
	 * @param unitType the unit type.
	 * @return true if the unit is a length.
	 */
	static boolean isLengthUnitType(short unitType) {
		return unitType > 2 && unitType < 60;
	}

	/**
	 * Check whether the given unit is a relative length.
	 * 
	 * @param unitType the unit type.
	 * @return true if the unit is a relative length.
	 */
	static boolean isRelativeLengthUnitType(short unitType) {
		return unitType >= 20 && unitType < 60;
	}

	/**
	 * Check whether the given unit is an angle.
	 * 
	 * @param unitType the unit type.
	 * @return true if the unit is an angle.
	 */
	static boolean isAngleUnitType(short unitType) {
		return unitType > 79 && unitType < 90;
	}

	/**
	 * Check whether the given unit is a time.
	 * 
	 * @param unitType the unit type.
	 * @return true if the unit is a time.
	 */
	static boolean isTimeUnitType(short unitType) {
		return unitType == CSS_S || unitType == CSS_MS;
	}

	/**
	 * Check whether the given unit is a resolution unit.
	 * 
	 * @param unitType the unit type.
	 * @return true if the unit is a resolution unit.
	 */
	static boolean isResolutionUnitType(short unitType) {
		return unitType >= CSS_DPI && unitType <= CSS_DPPX;
	}

	/**
	 * Gives the dimension unit string associated to the given CSS unit type.
	 * 
	 * @param unitType the CSS primitive unit type.
	 * @return the unit string.
	 * @throws DOMException INVALID_ACCESS_ERR if the unit is not a {@link CSSUnit}
	 *                      one.
	 */
	static String dimensionUnitString(short unitType) throws DOMException {
		switch (unitType) {
		case CSS_EM:
			return "em";
		case CSS_EX:
			return "ex";
		case CSS_PX:
			return "px";
		case CSS_IN:
			return "in";
		case CSS_CM:
			return "cm";
		case CSS_MM:
			return "mm";
		case CSS_PT:
			return "pt";
		case CSS_PC:
			return "pc";
		case CSS_PERCENTAGE:
			return "%";
		case CSS_DEG:
			return "deg";
		case CSS_GRAD:
			return "grad";
		case CSS_RAD:
			return "rad";
		case CSS_MS:
			return "ms";
		case CSS_S:
			return "s";
		case CSS_HZ:
			return "Hz";
		case CSS_KHZ:
			return "kHz";
		case CSS_CAP:
			return "cap";
		case CSS_CH:
			return "ch";
		case CSS_FR:
			return "fr";
		case CSS_IC:
			return "ic";
		case CSS_LH:
			return "lh";
		case CSS_QUARTER_MM:
			return "Q";
		case CSS_REM:
			return "rem";
		case CSS_RLH:
			return "rlh";
		case CSS_VB:
			return "vb";
		case CSS_VH:
			return "vh";
		case CSS_VI:
			return "vi";
		case CSS_VMAX:
			return "vmax";
		case CSS_VMIN:
			return "vmin";
		case CSS_VW:
			return "vw";
		case CSS_DPI:
			return "dpi";
		case CSS_DPCM:
			return "dpcm";
		case CSS_DPPX:
			return "dppx";
		case CSS_NUMBER:
		case CSS_OTHER:
		case CSS_INVALID:
			return "";
		default:
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Unknown unit: " + unitType);
		}
	}

}
