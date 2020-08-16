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

	enum LexicalType {
		/**
		 * ,
		 */
		OPERATOR_COMMA,

		/**
		 * ;
		 */
		OPERATOR_SEMICOLON,

		/**
		 * +
		 */
		OPERATOR_PLUS,

		/**
		 * -
		 */
		OPERATOR_MINUS,

		/**
		 * *
		 */
		OPERATOR_MULTIPLY,

		/**
		 * /
		 */
		OPERATOR_SLASH,

		/**
		 * %
		 */
		OPERATOR_MOD,

		/**
		 * ^
		 */
		OPERATOR_EXP,

		/**
		 * &lt;
		 */
		OPERATOR_LT,

		/**
		 * &gt;
		 */
		OPERATOR_GT,

		/**
		 * &lt;=
		 */
		OPERATOR_LE,

		/**
		 * &gt;=
		 */
		OPERATOR_GE,

		/**
		 * ~
		 */
		OPERATOR_TILDE,

		/**
		 * Keyword <code>inherit</code>.
		 */
		INHERIT,

		/**
		 * Keyword <code>initial</code>.
		 */
		INITIAL,

		/**
		 * Keyword <code>unset</code>.
		 */
		UNSET,

		/**
		 * Keyword <code>revert</code>.
		 */
		REVERT,

		/**
		 * Integers.
		 * 
		 * @see #getIntegerValue
		 */
		INTEGER,

		/**
		 * Real numbers.
		 * 
		 * @see #getFloatValue
		 * @see #getDimensionUnitText
		 */
		REAL,

		/**
		 * Percentage.
		 * 
		 * @see #getFloatValue
		 * @see #getDimensionUnitText
		 */
		PERCENTAGE,

		/**
		 * Unknown dimension.
		 * 
		 * @see #getFloatValue
		 * @see #getDimensionUnitText
		 */
		DIMENSION,

		/**
		 * RGB colors in functional (<code>rgb(0, 0, 0)</code>) and hex
		 * (<code>#000</code>) notations.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		RGBCOLOR,

		/**
		 * HSL(A) colors, for example: <code>hsl(0 0% 0% / 0)</code> or
		 * <code>hsla(0, 0%, 0%, 0)</code>
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		HSLCOLOR,

		/**
		 * Identifier, both predefined and custom.
		 * 
		 * @see #getStringValue
		 */
		IDENT,

		/**
		 * A string.
		 * 
		 * @see #getStringValue
		 */
		STRING,

		/**
		 * URI: <code>uri(...)</code>.
		 * 
		 * @see #getStringValue
		 */
		URI,

		/**
		 * A unicode range.
		 * 
		 * @see #getSubValues
		 */
		UNICODE_RANGE,

		/**
		 * Unicode range wildcard.
		 * <p>
		 * For example: <code>U+4??</code>.
		 * <p>
		 * The {@link #getStringValue()} method returns the wildcard without the
		 * preceding "U+".
		 */
		UNICODE_WILDCARD,

		/**
		 * An element reference.
		 * <p>
		 * For example: <code>element(#someId)</code>.
		 * <p>
		 *
		 * @see #getStringValue
		 */
		ELEMENT_REFERENCE,

		/**
		 * Custom property value: <code>var(...)</code>.
		 * <p>
		 * See {@link #getStringValue} for the custom property name, and
		 * {@link #getParameters()} for the fallback.
		 * </p>
		 */
		VAR,

		/**
		 * Attribute: <code>attr(...)</code>.
		 * 
		 * @see #getStringValue
		 */
		ATTR,

		/**
		 * A <code>calc()</code> expression.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		CALC,

		/**
		 * function <code>counter</code>.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		COUNTER_FUNCTION,

		/**
		 * function <code>counters</code>.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		COUNTERS_FUNCTION,

		/**
		 * <code>cubic-bezier()</code> easing function.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		CUBIC_BEZIER_FUNCTION,

		/**
		 * <code>steps()</code> easing function.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		STEPS_FUNCTION,

		/**
		 * function <code>rect</code>.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		RECT_FUNCTION,

		/**
		 * A function.
		 * 
		 * @see #getFunctionName
		 * @see #getParameters
		 */
		FUNCTION,

		/**
		 * Sub-expressions <code>(a)</code> <code>(a + b)</code>
		 * 
		 * @see #getSubValues
		 */
		SUB_EXPRESSION,

		/**
		 * [
		 */
		LEFT_BRACKET,

		/**
		 * ]
		 */
		RIGHT_BRACKET,

		/**
		 * Compat identifier: invalid value accepted for IE compatibility as an
		 * ident-like value.
		 *
		 * @see #getStringValue
		 */
		COMPAT_IDENT,

		/**
		 * Value with invalid priority accepted for IE compatibility, but it is
		 * interpreted as being of <code>!important</code> priority by the compatible
		 * browsers, which makes it different from <code>COMPAT_IDENT</code>.
		 *
		 * @see #getStringValue
		 */
		COMPAT_PRIO,

		/**
		 * <code>AND</code> condition.
		 * <p>
		 * Can be cast to a {@link BooleanCondition} of type <code>AND</code>.
		 * </p>
		 */
		CONDITION_AND,

		/**
		 * <code>OR</code> condition.
		 * <p>
		 * Can be cast to a {@link BooleanCondition} of type <code>OR</code>.
		 * </p>
		 */
		CONDITION_OR,

		/**
		 * <code>NOT</code> condition.
		 * <p>
		 * Can be cast to a {@link BooleanCondition} of type <code>NOT</code>.
		 * </p>
		 */
		CONDITION_NOT,

		/**
		 * Fundamental <code>PREDICATE</code> inside a condition.
		 * <p>
		 * Can be cast to a {@link BooleanCondition} of type <code>PREDICATE</code>.
		 * </p>
		 */
		CONDITION_PREDICATE,

		/**
		 * A property set to no value.
		 * <p>
		 * The latest CSS syntax is accepting properties being set to no value (empty
		 * value), mainly for custom properties.
		 * </p>
		 */
		EMPTY,

		/**
		 * Useful as initial value and for external extensions.
		 */
		UNKNOWN,

		/**
		 * Useful for external extensions.
		 */
		EXT1, EXT2, EXT3, EXT4
	}

	/**
	 * Gives the type of <code>LexicalUnit</code>.
	 * 
	 * @return the type of <code>LexicalUnit</code>.
	 */
	LexicalType getLexicalUnitType();

	/**
	 * An integer indicating the type of CSS unit that this lexical value
	 * represents.
	 * 
	 * @return an integer indicating the type of CSS unit. If this value does not
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
	 * </p>
	 * 
	 * @param nextUnit the lexical unit to be set as the next one.
	 * @throws IllegalArgumentException if the argument is a parameter or has a
	 *                                  previous unit.
	 */
	void insertNextLexicalUnit(LexicalUnit nextUnit);

	/**
	 * Remove this unit from the chain of lexical units and replace it by the next
	 * one, if any.
	 * 
	 * @return the next lexical unit, or {@code null} if there is no next lexical
	 *         unit.
	 */
	LexicalUnit remove();

	/**
	 * Replace this unit in the chain of lexical units (by the unit or sequence of
	 * units in the argument) and return the count of replacement units.
	 * <p>
	 * The replaced unit always returns {@code null} for
	 * {@link #getNextLexicalUnit()} and {@link #getPreviousLexicalUnit()},
	 * {@code false} for {@link #isParameter()}.
	 * </p>
	 * 
	 * @param replacementUnit the lexical unit that replaces this one. If
	 *                        {@code null}, this unit is replaced by the next one.
	 * @return the number of units that replace this one (<em>i.e.</em> the
	 *         argument, or {@code 0} if the argument is {@code null}).
	 * @throws IllegalArgumentException if the replacement unit is a parameter or
	 *                                  has a previous unit.
	 * @throws CSSBudgetException       if the replacement unit has too many chained
	 *                                  units (to avoid potential DoS).
	 */
	int countReplaceBy(LexicalUnit replacementUnit) throws CSSBudgetException;

	/**
	 * Replace this unit in the chain of lexical units.
	 * <p>
	 * The replaced unit always returns {@code null} for
	 * {@link #getNextLexicalUnit()} and {@link #getPreviousLexicalUnit()},
	 * {@code false} for {@link #isParameter()}.
	 * </p>
	 * <p>
	 * This method is very similar to {@link #countReplaceBy(LexicalUnit)} which
	 * gives important information when you need to replace this unit by an
	 * arbitrary (and potentially large) chunk of values.
	 * </p>
	 * 
	 * @param replacementUnit the lexical unit that replaces this one. If
	 *                        {@code null}, this unit is replaced by the next one.
	 * @return the unit that replaces this one (<em>i.e.</em> the argument, or the
	 *         next lexical unit if the argument is {@code null}).
	 * @throws IllegalArgumentException if the replacement unit is a parameter or
	 *                                  has a previous unit.
	 * @throws CSSBudgetException       if the replacement unit has too many chained
	 *                                  units (to avoid potential DoS).
	 * @see #countReplaceBy(LexicalUnit)
	 */
	LexicalUnit replaceBy(LexicalUnit replacementUnit) throws CSSBudgetException;

	/**
	 * Returns the integer value represented by this unit.
	 * 
	 * @return the integer value, or zero if this is not an integer unit.
	 * @see LexicalType#INTEGER
	 */
	int getIntegerValue();

	/**
	 * Returns the float value represented by this unit.
	 * 
	 * @return the float value, or {@code NaN} if this value is not a float.
	 */
	float getFloatValue();

	/**
	 * If this unit is a {@link LexicalType#DIMENSION}, returns the string
	 * representation of the CSS unit returned by {@link #getCssUnit()}.
	 * 
	 * @return the string representation of the CSS unit, or the empty string if
	 *         this lexical unit does not represent a {@link LexicalType#DIMENSION}.
	 */
	String getDimensionUnitText();

	/**
	 * Returns the string value.
	 * <p>
	 * If the type is <code>URI</code>, the return value doesn't contain
	 * <code>uri(....)</code> or quotes.
	 * </p>
	 * <p>
	 * If the type is <code>ATTR</code>, the return value doesn't contain
	 * <code>attr(....)</code>.
	 * </p>
	 * <p>
	 * If the type is <code>UNICODE_WILDCARD</code>, the return value is the
	 * wildcard without the preceding "U+".
	 * </p>
	 * 
	 * @return the string value, or <code>null</code> if this unit does not have a
	 *         string to return.
	 * 
	 * @see LexicalType#URI
	 * @see LexicalType#ATTR
	 * @see LexicalType#IDENT
	 * @see LexicalType#STRING
	 * @see LexicalType#UNICODE_WILDCARD
	 */
	String getStringValue();

	/**
	 * Returns the name of the function.
	 * 
	 * @return the function name, or <code>null</code> if this unit is not a
	 *         function.
	 * 
	 * @see LexicalType#COUNTER_FUNCTION
	 * @see LexicalType#COUNTERS_FUNCTION
	 * @see LexicalType#RECT_FUNCTION
	 * @see LexicalType#FUNCTION
	 * @see LexicalType#RGBCOLOR
	 * @see LexicalType#HSLCOLOR
	 */
	String getFunctionName();

	/**
	 * The function parameters including operators (like the comma).
	 * <p>
	 * A RGB color like <code>#000</code> is converted to <code>rgb(0, 0, 0)</code>.
	 * </p>
	 * <p>
	 * May return <code>null</code> if type is <code>FUNCTION</code>.
	 * </p>
	 * 
	 * @return the parameters of this function, or <code>null</code> if this unit is
	 *         not a function, or an empty <code>FUNCTION</code>.
	 * 
	 * @see LexicalType#COUNTER_FUNCTION
	 * @see LexicalType#COUNTERS_FUNCTION
	 * @see LexicalType#RECT_FUNCTION
	 * @see LexicalType#FUNCTION
	 * @see LexicalType#RGBCOLOR
	 * @see LexicalType#HSLCOLOR
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
	 * @see LexicalType#SUB_EXPRESSION
	 * @see LexicalType#UNICODE_RANGE
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
	 * errors (except for compatibility values like <code>COMPAT_IDENT</code>).
	 * </p>
	 *
	 * @return the parsable serialization of this unit.
	 */
	String getCssText();

	/**
	 * Creates a deep copy of this lexical unit and the next ones, unlinked to any
	 * previous lexical unit.
	 * <p>
	 * The clone's {@link #getPreviousLexicalUnit()} returns {@code null} (and
	 * {@link #isParameter()} {@code false}) regardless of what the the original
	 * object returned.
	 * </p>
	 * 
	 * @return a copy of this unit.
	 */
	LexicalUnit clone();

	/**
	 * Creates a shallow copy of this lexical unit, ignoring the next ones, and
	 * unlinked to any previous lexical unit.
	 * <p>
	 * The shallow clone's {@link #getPreviousLexicalUnit()} and
	 * {@link #getNextLexicalUnit()} both return {@code null} (and
	 * {@link #isParameter()} {@code false}) regardless of what the the original
	 * object returned.
	 * </p>
	 * 
	 * @return a shallow copy of this unit.
	 */
	LexicalUnit shallowClone();

}
