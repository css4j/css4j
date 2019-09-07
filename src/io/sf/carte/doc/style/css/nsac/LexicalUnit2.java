/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2018 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

import org.w3c.css.sac.LexicalUnit;

/**
 * Updates SAC's {@link LexicalUnit} interface.
 */
public interface LexicalUnit2 extends LexicalUnit {

	/**
	 * cap.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_CAP = 50;

	/**
	 * ch.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_CH = 51;

	/**
	 * ic.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_IC = 52;

	/**
	 * Root EM.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_REM = 53;

	/**
	 * lh.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_LH = 54;

	/**
	 * rlh.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_RLH = 55;

	/**
	 * vw.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_VW = 56;

	/**
	 * vh.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_VH = 57;

	/**
	 * vi.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_VI = 58;

	/**
	 * vb.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_VB = 59;

	/**
	 * vmin.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_VMIN = 60;

	/**
	 * vmax.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_VMAX = 61;

	/**
	 * Q.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_QUARTER_MILLIMETER = 62;

	/**
	 * turn.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_TURN = 63;

	/**
	 * dpi.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_DOTS_PER_INCH = 64;

	/**
	 * dpcm.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_DOTS_PER_CENTIMETER = 65;

	/**
	 * dppx.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_DOTS_PER_PIXEL = 66;

	/**
	 * Flex (fr).
	 * <p>
	 * Flexible length: a fraction of the leftover space in the grid container. Note
	 * that it is not a length.
	 *
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_FR = 67;

	/**
	 * [
	 */
	short SAC_LEFT_BRACKET = 68;

	/**
	 * ]
	 */
	short SAC_RIGHT_BRACKET = 69;

	/**
	 * Unicode range wildcard.
	 * <p>
	 * For example: <code>U+4??</code>.
	 * <p>
	 * The {@link #getStringValue()} method returns the wildcard without the
	 * preceding "U+".
	 */
	short SAC_UNICODE_WILDCARD = 70;

	/**
	 * Compat identifier: invalid value accepted for IE compatibility as an
	 * ident-like value.
	 *
	 * @see #getStringValue
	 */
	short SAC_COMPAT_IDENT = 71;

	/**
	 * Value with invalid priority accepted for IE compatibility, but it is
	 * interpreted as being of <code>!important</code> priority by the compatible
	 * browsers, which makes it different from <code>SAC_COMPAT_IDENT</code>.
	 *
	 * @see #getStringValue
	 */
	short SAC_COMPAT_PRIO = 72;

	/**
	 * An element reference.
	 * <p>
	 * For example: <code>element(#someId)</code>.
	 * <p>
	 *
	 * @see #getStringValue
	 */
	short SAC_ELEMENT_REFERENCE = 73;

	/**
	 * Get a parsable representation of this unit.
	 * <p>
	 * The serialization must only include this lexical unit, ignoring the next
	 * units if they exist.
	 *
	 * @return the parsable representation of this unit.
	 */
	String getCssText();

}
