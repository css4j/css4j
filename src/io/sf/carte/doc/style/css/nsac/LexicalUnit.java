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
	 * &lt;
	 */
	short SAC_OPERATOR_LT = 7;

	/**
	 * &gt;
	 */
	short SAC_OPERATOR_GT = 8;

	/**
	 * &lt;=
	 */
	short SAC_OPERATOR_LE = 9;

	/**
	 * &gt;=
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
	 * Keyword <code>initial</code>.
	 */
	short SAC_INITIAL = 74;

	/**
	 * Keyword <code>unset</code>.
	 */
	short SAC_UNSET = 75;

	/**
	 * Keyword <code>revert</code>.
	 */
	short SAC_REVERT = 76;

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
	 * Custom property value: <code>var(...)</code>.
	 * <p>
	 * See {@link #getStringValue} for the custom property name, and
	 * {@link #getParameters()} for the fallback.
	 * </p>
	 */
	short SAC_VAR = 77;

	/**
	 * An integer indicating the type of <code>LexicalUnit</code>.
	 * 
	 * @return the type of <code>LexicalUnit</code>.
	 */
	short getLexicalUnitType();

	/**
	 * An integer indicating the type of css unit that this lexical value
	 * represents.
	 * 
	 * @return an integer indicating the type of css unit. If this value does not
	 *         represent a dimension, must return an invalid unit identifier.
	 */
	short getCssUnit();

	/**
	 * The next lexical unit.
	 * <p>
	 * Lexical units can form chains of units which can be traversed with
	 * {@code getNextLexicalUnit()} and {@link #getPreviousLexicalUnit()}.
	 * </p>
	 * 
	 * @return the next lexical unit, or <code>null</code> if none.
	 */
	LexicalUnit getNextLexicalUnit();

	/**
	 * The previous lexical unit.
	 * <p>
	 * See also {@link #getNextLexicalUnit}.
	 * </p>
	 * 
	 * @return the previous lexical unit, or <code>null</code> if none.
	 */
	LexicalUnit getPreviousLexicalUnit();

	/**
	 * Insert the given unit as the next lexical unit.
	 * <p>
	 * After the insertion, {@code nextUnit} shall be the next lexical unit, and the
	 * former next lexical unit will be the next one after the last unit in the
	 * {@code nextUnit} unit chain.
	 * 
	 * @param nextUnit the lexical unit to be set as the next one.
	 */
	void insertNextLexicalUnit(LexicalUnit nextUnit);

	/**
	 * Replace this unit in the chain of lexical units.
	 * 
	 * @param replacementUnit the lexical unit that replaces this one.
	 * @return the unit that replaces this one.
	 */
	LexicalUnit replaceBy(LexicalUnit replacementUnit);

	/**
	 * Returns the integer value represented by this unit.
	 * 
	 * @return the integer value, or zero if this is not an integer unit.
	 * @see #SAC_INTEGER
	 */
	int getIntegerValue();

	/**
	 * Returns the float value represented by this unit.
	 * 
	 * @return the float value, or {@code NaN} if this value is not a float.
	 */
	float getFloatValue();

	/**
	 * If this unit is a {@link #SAC_DIMENSION}, returns the string representation
	 * of the CSS unit returned by {@link #getCssUnit()}.
	 * 
	 * @return the string representation of the CSS unit, or the empty string if
	 *         this lexical unit does not represent a {@link #SAC_DIMENSION}.
	 */
	String getDimensionUnitText();

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
	 * @return the string value, or <code>null</code> if this unit does not have a
	 *         string to return.
	 * 
	 * @see #SAC_URI
	 * @see #SAC_ATTR
	 * @see #SAC_IDENT
	 * @see #SAC_STRING_VALUE
	 * @see #SAC_UNICODE_WILDCARD
	 */
	String getStringValue();

	/**
	 * Returns the name of the function.
	 * 
	 * @return the function name, or <code>null</code> if this unit is not a
	 *         function.
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
	 * @return the parameters of this function, or <code>null</code> if this unit is
	 *         not a function.
	 * 
	 * @see #SAC_COUNTER_FUNCTION
	 * @see #SAC_COUNTERS_FUNCTION
	 * @see #SAC_RECT_FUNCTION
	 * @see #SAC_FUNCTION
	 * @see #SAC_RGBCOLOR
	 */
	LexicalUnit getParameters();

	/**
	 * Check if this lexical unit is a parameter that was made available through its
	 * parent's {@link #getParameters()} method.
	 * 
	 * @return {@code true} if this lexical unit is a parameter.
	 */
	boolean isParameter();

	/**
	 * Returns a sequence of units inside the sub-expression or unicode range.
	 * 
	 * @return the values in the sub-expression, or <code>null</code> if this unit
	 *         is not a sub-expression nor a unicode range.
	 * @see #SAC_SUB_EXPRESSION
	 * @see #SAC_UNICODERANGE
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

	/**
	 * Creates a deep copy of this lexical unit and the next ones, but with
	 * {@link #getPreviousLexicalUnit()} returning {@code null}.
	 * 
	 * @return a copy of this unit.
	 */
	LexicalUnit clone();

}
