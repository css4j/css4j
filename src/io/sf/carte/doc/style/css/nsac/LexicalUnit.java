/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

import io.sf.carte.doc.style.css.parser.BooleanCondition;

/**
 * Based on SAC's {@code LexicalUnit} interface by Philippe Le Hegaret.
 */
public interface LexicalUnit {

	/**
	 * ,
	 */
	short SAC_OPERATOR_COMMA = 0;

	/**
	 * +
	 */
	short SAC_OPERATOR_PLUS = 1;

	/**
	 * -
	 */
	short SAC_OPERATOR_MINUS = 2;

	/**
	 * *
	 */
	short SAC_OPERATOR_MULTIPLY = 3;

	/**
	 * /
	 */
	short SAC_OPERATOR_SLASH = 4;

	/**
	 * %
	 */
	short SAC_OPERATOR_MOD = 5;

	/**
	 * ^
	 */
	short SAC_OPERATOR_EXP = 6;

	/**
	 * <
	 */
	short SAC_OPERATOR_LT = 7;

	/**
	 * >
	 */
	short SAC_OPERATOR_GT = 8;

	/**
	 * <=
	 */
	short SAC_OPERATOR_LE = 9;

	/**
	 * >=
	 */
	short SAC_OPERATOR_GE = 10;

	/**
	 * ~
	 */
	short SAC_OPERATOR_TILDE = 11;

	/**
	 * Keyword <code>inherit</code>.
	 */
	short SAC_INHERIT = 12;

	/**
	 * Integers.
	 * 
	 * @see #getIntegerValue
	 */
	short SAC_INTEGER = 13;

	/**
	 * reals.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_REAL = 14;

	/**
	 * Relative length<code>em</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_EM = 15;

	/**
	 * Relative length<code>ex</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_EX = 16;

	/**
	 * Relative length <code>px</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_PIXEL = 17;

	/**
	 * Absolute length <code>in</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_INCH = 18;

	/**
	 * Absolute length <code>cm</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_CENTIMETER = 19;

	/**
	 * Absolute length <code>mm</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_MILLIMETER = 20;

	/**
	 * Absolute length <code>pt</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_POINT = 21;

	/**
	 * Absolute length <code>pc</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_PICA = 22;

	/**
	 * Percentage.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_PERCENTAGE = 23;

	/**
	 * URI: <code>uri(...)</code>.
	 * 
	 * @see #getStringValue
	 */
	short SAC_URI = 24;

	/**
	 * function <code>counter</code>.
	 * 
	 * @see #getFunctionName
	 * @see #getParameters
	 */
	short SAC_COUNTER_FUNCTION = 25;

	/**
	 * function <code>counters</code>.
	 * 
	 * @see #getFunctionName
	 * @see #getParameters
	 */
	short SAC_COUNTERS_FUNCTION = 26;

	/**
	 * RGB Colors. <code>rgb(0, 0, 0)</code> and <code>#000</code>
	 * 
	 * @see #getFunctionName
	 * @see #getParameters
	 */
	short SAC_RGBCOLOR = 27;

	/**
	 * Angle <code>deg</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_DEGREE = 28;

	/**
	 * Angle <code>grad</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_GRADIAN = 29;

	/**
	 * Angle <code>rad</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_RADIAN = 30;

	/**
	 * Time <code>ms</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_MILLISECOND = 31;

	/**
	 * Time <code>s</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_SECOND = 32;

	/**
	 * Frequency <code>Hz</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_HERTZ = 33;

	/**
	 * Frequency <code>kHz</code>.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_KILOHERTZ = 34;

	/**
	 * Custom identifier.
	 * 
	 * @see #getStringValue
	 */
	short SAC_IDENT = 35;

	/**
	 * A string.
	 * 
	 * @see #getStringValue
	 */
	short SAC_STRING_VALUE = 36;

	/**
	 * Attribute: <code>attr(...)</code>.
	 * 
	 * @see #getStringValue
	 */
	short SAC_ATTR = 37;

	/**
	 * function <code>rect</code>.
	 * 
	 * @see #getFunctionName
	 * @see #getParameters
	 */
	short SAC_RECT_FUNCTION = 38;

	/**
	 * A unicode range.
	 * 
	 * @see #getSubValues
	 */
	short SAC_UNICODERANGE = 39;

	/**
	 * sub expressions <code>(a)</code> <code>(a + b)</code>
	 * <code>(normal/none)</code>
	 * 
	 * @see #getSubValues
	 */
	short SAC_SUB_EXPRESSION = 40;

	/**
	 * A function.
	 * 
	 * @see #getFunctionName
	 * @see #getParameters
	 */
	short SAC_FUNCTION = 41;

	/**
	 * unknown dimension.
	 * 
	 * @see #getFloatValue
	 * @see #getDimensionUnitText
	 */
	short SAC_DIMENSION = 42;

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
	 * <code>AND</code> condition.
	 * <p>
	 * Can be cast to a {@link BooleanCondition} of type <code>AND</code>.
	 * </p>
	 */
	short SAC_CONDITION_AND = 100;

	/**
	 * <code>OR</code> condition.
	 * <p>
	 * Can be cast to a {@link BooleanCondition} of type <code>OR</code>.
	 * </p>
	 */
	short SAC_CONDITION_OR = 101;

	/**
	 * <code>NOT</code> condition.
	 * <p>
	 * Can be cast to a {@link BooleanCondition} of type <code>NOT</code>.
	 * </p>
	 */
	short SAC_CONDITION_NOT = 102;

	/**
	 * Fundamental <code>PREDICATE</code> inside a condition.
	 * <p>
	 * Can be cast to a {@link BooleanCondition} of type <code>PREDICATE</code>.
	 * </p>
	 */
	short SAC_CONDITION_PREDICATE = 103;

	/**
	 * An integer indicating the type of <code>LexicalUnit</code>.
	 */
	short getLexicalUnitType();

	/**
	 * Returns the next unit.
	 * 
	 * @return the next unit, or <code>null</code> if none.
	 */
	LexicalUnit getNextLexicalUnit();

	/**
	 * Returns the previous unit.
	 * 
	 * @return the previous unit, or <code>null</code> if none.
	 */
	LexicalUnit getPreviousLexicalUnit();

	/**
	 * Returns the integer value.
	 * 
	 * @see #SAC_INTEGER
	 */
	int getIntegerValue();

	/**
	 * Returns the float value.
	 */
	float getFloatValue();

	/**
	 * Returns the string representation of the unit.
	 * <p>
	 * if this lexical unit represents a float, the dimension is an empty string.
	 * </p>
	 */
	String getDimensionUnitText();

	/**
	 * Returns the name of the function.
	 * 
	 * @see #SAC_COUNTER_FUNCTION
	 * @see #SAC_COUNTERS_FUNCTION
	 * @see #SAC_RECT_FUNCTION
	 * @see #SAC_FUNCTION
	 * @see #SAC_RGBCOLOR
	 */
	String getFunctionName();

	/**
	 * The function parameters including operators (like the comma).
	 * <code>#000</code> is converted to <code>rgb(0, 0, 0)</code> can return
	 * <code>null</code> if <code>SAC_FUNCTION</code>.
	 * 
	 * @see #SAC_COUNTER_FUNCTION
	 * @see #SAC_COUNTERS_FUNCTION
	 * @see #SAC_RECT_FUNCTION
	 * @see #SAC_FUNCTION
	 * @see #SAC_RGBCOLOR
	 */
	LexicalUnit getParameters();

	/**
	 * Returns the string value.
	 * <p>
	 * If the type is <code>SAC_URI</code>, the return value doesn't contain
	 * <code>uri(....)</code> or quotes.
	 * <p>
	 * If the type is <code>SAC_ATTR</code>, the return value doesn't contain
	 * <code>attr(....)</code>.
	 * <p>
	 * If the type is <code>SAC_UNICODE_WILDCARD</code>, the return value is the
	 * wildcard without the preceding "U+".
	 *
	 * @see #SAC_URI
	 * @see #SAC_ATTR
	 * @see #SAC_IDENT
	 * @see #SAC_STRING_VALUE
	 * @see #SAC_UNICODE_WILDCARD
	 */
	String getStringValue();

	/**
	 * Returns a sequence of units inside the sub expression.
	 * 
	 * @see #SAC_SUB_EXPRESSION
	 */
	LexicalUnit getSubValues();

	/**
	 * Get a parsable representation of this unit.
	 * <p>
	 * The serialization must only include this lexical unit, ignoring the next
	 * units if they exist.
	 * </p>
	 * <p>
	 * The text should be close to how the value was specified (for example,
	 * preserving hex or functional notation in rgb colors) but must parse without
	 * errors (except for compatibility values like <code>SAC_COMPAT_IDENT</code>).
	 * </p>
	 *
	 * @return the parsable representation of this unit.
	 */
	String getCssText();

}
