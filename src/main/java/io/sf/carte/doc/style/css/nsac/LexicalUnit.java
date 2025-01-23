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

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

/**
 * Based on SAC's {@code LexicalUnit} interface by Philippe Le Hegaret.
 */
public interface LexicalUnit {

	/**
	 * The lexical type.
	 * <p>
	 * The <code>ordinal</code> position is not guaranteed to be kept among
	 * releases.
	 * </p>
	 */
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
		 * <p>
		 * Note: integers larger than 2147483647 (or smaller than -2147483648) are
		 * parsed as {@link #REAL}.
		 * </p>
		 * 
		 * @see LexicalUnit#getIntegerValue
		 */
		INTEGER,

		/**
		 * Real numbers.
		 * 
		 * @see LexicalUnit#getFloatValue
		 * @see LexicalUnit#getDimensionUnitText
		 */
		REAL,

		/**
		 * Percentage.
		 * 
		 * @see LexicalUnit#getFloatValue
		 * @see LexicalUnit#getDimensionUnitText
		 */
		PERCENTAGE,

		/**
		 * A dimensional quantity (number + unit).
		 * 
		 * @see LexicalUnit#getFloatValue
		 * @see LexicalUnit#getCssUnit
		 * @see LexicalUnit#getDimensionUnitText
		 */
		DIMENSION,

		/**
		 * RGB colors in functional (<code>rgb(0, 0, 0)</code>) and hex
		 * (<code>#000</code>) notations.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		RGBCOLOR,

		/**
		 * HSL(A) colors, for example: <code>hsl(0 0% 0% / 0)</code> or
		 * <code>hsla(0, 0%, 0%, 0)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		HSLCOLOR,

		/**
		 * HWB colors, for example: <code>hwb(0 0% 0% / 0)</code> or
		 * <code>hwb(120 0% 49.8%)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		HWBCOLOR,

		/**
		 * lab() colors, for example: <code>lab(53.2% 42.4 57.76 / 0.6)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		LABCOLOR,

		/**
		 * lch() colors, for example: <code>lch(58.9% 44.4 97.21 / 0.6)</code> or
		 * <code>lch(58.9% 44.4 97.21deg / 0.6)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		LCHCOLOR,

		/**
		 * oklab() colors, for example: <code>oklab(53.2% 42.4 57.76 / 0.6)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		OKLABCOLOR,

		/**
		 * oklch() colors, for example: <code>oklch(58.9% 44.4 97.21 / 0.6)</code> or
		 * <code>oklch(58.9% 44.4 97.21deg / 0.6)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		OKLCHCOLOR,

		/**
		 * color() function, for example: <code>color(display-p3, 0.328 0.962 0.551 / 0.6)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		COLOR_FUNCTION,

		/**
		 * color-mix() function, for example: <code>color-mix(in display-p3, #0200fa 10%, white)</code>
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		COLOR_MIX,

		/**
		 * Identifier, both predefined and custom.
		 * 
		 * @see LexicalUnit#getStringValue
		 */
		IDENT,

		/**
		 * A string.
		 * 
		 * @see LexicalUnit#getStringValue
		 */
		STRING,

		/**
		 * URI: <code>url(...)</code>.
		 * 
		 * Use {@link LexicalUnit#getStringValue() getStringValue()} to retrieve the
		 * URL, or {@link LexicalUnit#getParameters() getParameters()} if the argument
		 * is a {@code var()}.
		 */
		URI,

		/**
		 * A unicode range.
		 * 
		 * @see LexicalUnit#getSubValues
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
		 * <p>
		 * If the element Id is a string, use {@link LexicalUnit#getStringValue()
		 * getStringValue()}, otherwise use {@link LexicalUnit#getParameters()
		 * getParameters()} if it contains a {@code var()} or {@code attr()}.
		 * </p>
		 *
		 * @see LexicalUnit#getStringValue
		 * @see LexicalUnit#getParameters
		 */
		ELEMENT_REFERENCE,

		/**
		 * Custom property value: <code>var(...)</code>.
		 * <p>
		 * See {@link #getParameters()} for the custom property name and eventually the
		 * fallback.
		 * </p>
		 */
		VAR,

		/**
		 * Attribute: <code>attr(...)</code>.
		 * 
		 * @see LexicalUnit#getParameters
		 */
		ATTR,

		/**
		 * A <code>calc()</code> expression.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		CALC,

		/**
		 * function <code>counter</code>.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		COUNTER_FUNCTION,

		/**
		 * function <code>counters</code>.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		COUNTERS_FUNCTION,

		/**
		 * <code>cubic-bezier()</code> easing function.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		CUBIC_BEZIER_FUNCTION,

		/**
		 * <code>steps()</code> easing function.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		STEPS_FUNCTION,

		/**
		 * function <code>rect</code>.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		RECT_FUNCTION,

		/**
		 * A function.
		 * 
		 * @see LexicalUnit#getFunctionName
		 * @see LexicalUnit#getParameters
		 */
		FUNCTION,

		/**
		 * Sub-expressions <code>(a)</code> <code>(a + b)</code>
		 * 
		 * @see LexicalUnit#getSubValues
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
		 * @see LexicalUnit#getStringValue
		 */
		COMPAT_IDENT,

		/**
		 * Value with invalid priority accepted for IE compatibility, but it is
		 * interpreted as being of <code>!important</code> priority by the compatible
		 * browsers, which makes it different from <code>COMPAT_IDENT</code>.
		 *
		 * @see LexicalUnit#getStringValue
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
	 *                        If {@link LexicalType#EMPTY EMPTY}, this unit is
	 *                        replaced by the next non-empty unit in the parameter's
	 *                        lexical chain or, if there is none, by the next unit
	 *                        in this chain (same as if parameter was {@code null}).
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
	 * If this unit is a {@link LexicalType#DIMENSION DIMENSION}, returns the string
	 * representation of the CSS unit returned by {@link #getCssUnit()}.
	 * 
	 * @return the string representation of the CSS unit, or the empty string if
	 *         this lexical unit does not represent a {@link LexicalType#DIMENSION
	 *         DIMENSION}.
	 */
	String getDimensionUnitText();

	/**
	 * Returns the string value.
	 * <p>
	 * If the type is <code>URI</code>, the return value doesn't contain
	 * <code>uri(....)</code> or quotes.
	 * </p>
	 * <p>
	 * If the type is <code>UNICODE_WILDCARD</code>, the return value is the
	 * wildcard without the preceding "U+".
	 * </p>
	 * 
	 * @return the string value, or <code>null</code> if this unit does not have a
	 *         string to return.
	 * 
	 * @see LexicalType#IDENT
	 * @see LexicalType#STRING
	 * @see LexicalType#URI
	 * @see LexicalType#UNICODE_WILDCARD
	 * @see LexicalType#ELEMENT_REFERENCE
	 */
	String getStringValue();

	/**
	 * Returns the name of the function.
	 * 
	 * @return the function name, or <code>null</code> if this unit is not a
	 *         function.
	 * 
	 * @see LexicalType#CALC
	 * @see LexicalType#FUNCTION
	 * @see LexicalType#COUNTER_FUNCTION
	 * @see LexicalType#COUNTERS_FUNCTION
	 * @see LexicalType#CUBIC_BEZIER_FUNCTION
	 * @see LexicalType#STEPS_FUNCTION
	 * @see LexicalType#RECT_FUNCTION
	 * @see LexicalType#RGBCOLOR
	 * @see LexicalType#HSLCOLOR
	 * @see LexicalType#HWBCOLOR
	 * @see LexicalType#LABCOLOR
	 * @see LexicalType#LCHCOLOR
	 * @see LexicalType#OKLABCOLOR
	 * @see LexicalType#OKLCHCOLOR
	 * @see LexicalType#COLOR_FUNCTION
	 * @see LexicalType#COLOR_MIX
	 * @see LexicalType#ELEMENT_REFERENCE
	 * @see LexicalType#VAR
	 * @see LexicalType#ATTR
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
	 * <p>
	 * Since version 5, also returns the sub-values if this is an expression or a
	 * unicode range. Therefore, can be used as a more streamlined version of
	 * {@link #getSubValues()}.
	 * </p>
	 * 
	 * @return the parameters of this function, or <code>null</code> if this unit is
	 *         not a function, or an empty <code>FUNCTION</code>.
	 * 
	 * @see LexicalType#CALC
	 * @see LexicalType#FUNCTION
	 * @see LexicalType#COUNTER_FUNCTION
	 * @see LexicalType#COUNTERS_FUNCTION
	 * @see LexicalType#CUBIC_BEZIER_FUNCTION
	 * @see LexicalType#STEPS_FUNCTION
	 * @see LexicalType#RECT_FUNCTION
	 * @see LexicalType#RGBCOLOR
	 * @see LexicalType#HSLCOLOR
	 * @see LexicalType#HWBCOLOR
	 * @see LexicalType#LABCOLOR
	 * @see LexicalType#LCHCOLOR
	 * @see LexicalType#OKLABCOLOR
	 * @see LexicalType#OKLCHCOLOR
	 * @see LexicalType#COLOR_FUNCTION
	 * @see LexicalType#COLOR_MIX
	 * @see LexicalType#ELEMENT_REFERENCE
	 * @see LexicalType#VAR
	 * @see LexicalType#ATTR
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
	 * Verify whether this value matches the given grammar.
	 * <p>
	 * This method is intended to be used for generic sanity checks and,
	 * consequently, some corner cases may not be taken into account. For example,
	 * when matching {@code var()}, the implementations are allowed to assume that
	 * the property substitution shall not be empty, otherwise values like
	 * {@code var(--data) 1px} would give a non-{@code FALSE} match simultaneously
	 * on syntaxes with and without a multiplier ({@code +} or {@code #}), which
	 * would probably not be what that use case wants.
	 * </p>
	 * <p>
	 * In the case of {@code calc()}, if implementations do not perform a full
	 * dimensional analysis it is more acceptable to return a bogus {@code TRUE}
	 * than a {@code FALSE}.
	 * </p>
	 * <p>
	 * Here are some examples of matching:
	 * </p>
	 * <table style="border:1px solid;border-collapse:collapse;">
	 * <thead>
	 * <tr>
	 * <th style="border: 1px solid;padding:6pt">Value</th>
	 * <th style="border:1px solid">Syntax</th>
	 * <th style="border:1px solid">Match</th>
	 * </tr>
	 * </thead><tbody>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:6pt" rowspan=
	 * "7"><code>attr(data-width length, 8%)</code></td>
	 * <td style="border:1px
	 * solid;padding:4pt"><code>&lt;percentage&gt; | &lt;length&gt;</code></td>
	 * <td><code>TRUE</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;length&gt;</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>PENDING</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;percentage&gt;</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>PENDING</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;length&gt;#</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>PENDING</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px
	 * solid;padding:4pt"><code>&lt;length-percentage&gt;</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>TRUE</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;resolution&gt;</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>FALSE</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>*</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>TRUE</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:6pt" rowspan=
	 * "5"><code>6pt var(--custom) 12.3px</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>&lt;length&gt;+</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>PENDING</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;length&gt;#</code></td>
	 * <td style="border:1px solid;padding:2pt
	 * 4pt"><code>PENDING</code><sup>1</sup></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;length&gt;</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>FALSE</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>&lt;number&gt;+</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>FALSE</code></td>
	 * </tr>
	 * <tr style="text-align:center;">
	 * <td style="border:1px solid;padding:4pt"><code>*</code></td>
	 * <td style="border:1px solid;padding:4pt"><code>TRUE</code></td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * <ol style="font-size:smaller">
	 * <li>Despite the value containing no commas, the syntax with a {@code #}
	 * multiplier matches as {@code PENDING} because the custom property could have
	 * commas and start and end with one.</li>
	 * </ol>
	 * <p>
	 * See also: {@link io.sf.carte.doc.style.css.parser.SyntaxParser SyntaxParser}.
	 * </p>
	 * <br/>
	 * 
	 * @param syntax the syntax.
	 * @return the matching for the syntax.
	 */
	Match matches(CSSValueSyntax syntax);

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
