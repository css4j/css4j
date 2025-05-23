/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2005-2025, Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.util.SimpleWriter;

/**
 * A CSS style value.
 *
 */
public interface CSSValue extends Cloneable {

	/**
	 * The main categories of values.
	 */
	enum CssType {

		/**
		 * A CSS-wide keyword like {@code inherit}.
		 */
		KEYWORD,

		/**
		 * <p>
		 * A vehicle towards a final value, of a CSS type that cannot be anticipated.
		 * </p>
		 * <p>
		 * Example: {@code var()} or {@code attr()}.
		 * </p>
		 * <p>
		 * <i>(note that </i>{@code attr()}<i> has two components, a main one whose type
		 * could be anticipated, and a fallback that could be of a different type)</i>.
		 * </p>
		 * <p>
		 * {@code PROXY} values can be cast to a {@link CSSPrimitiveValue}.
		 * </p>
		 */
		PROXY,

		/**
		 * A typed primitive value, includes numbers and identifiers.
		 * <p>
		 * You can cast to {@link CSSTypedValue} (interface) or
		 * {@link io.sf.carte.doc.style.css.property.TypedValue TypedValue} (base
		 * implementation class).
		 * </p>
		 */
		TYPED,

		/**
		 * A list of values.
		 * <p>
		 * You can always cast to {@link CSSValueList} but, unless you are dealing with
		 * the argument list of a {@link Type#FUNCTION FUNCTION}, it is better to cast
		 * directly to a {@link io.sf.carte.doc.style.css.property.ValueList ValueList}.
		 * </p>
		 */
		LIST,

		/**
		 * A shorthand property.
		 * <p>
		 * Declared shorthands can be retrieved from style declarations.
		 * </p>
		 * <p>
		 * Cast it to {@link CSSShorthandValue} to have access to the set of longhand
		 * property names set by the shorthand.
		 * </p>
		 */
		SHORTHAND
	}

	/**
	 * The type of value. For keywords, it is the keyword.
	 */
	enum Type {
		/**
		 * Unknown type, probably a system default or a compat value.
		 */
		UNKNOWN,

		/**
		 * {@code inherit} keyword.
		 */
		INHERIT,

		/**
		 * {@code initial} keyword.
		 */
		INITIAL,

		/**
		 * {@code unset} keyword.
		 */
		UNSET,

		/**
		 * {@code revert} keyword.
		 */
		REVERT,

		/**
		 * Numeric type (excludes {@code calc()} which is an {@link #EXPRESSION}).
		 * <p>
		 * Cast to {@link CSSNumberValue}, although {@link CSSTypedValue} or
		 * {@link io.sf.carte.doc.style.css.property.TypedValue TypedValue} may give you
		 * what you need (like the numeric value via
		 * {@link CSSTypedValue#getFloatValue(short) getFloatValue}). If neither do,
		 * please look at {@link io.sf.carte.doc.style.css.property.NumberValue
		 * NumberValue}.
		 * </p>
		 */
		NUMERIC,

		/**
		 * String.
		 * <p>
		 * Casting to {@link CSSTypedValue} or
		 * {@link io.sf.carte.doc.style.css.property.TypedValue TypedValue} will give
		 * you access to the string value via {@link CSSTypedValue#getStringValue()
		 * getStringValue()}.
		 * </p>
		 */
		STRING,

		/**
		 * Identifier.
		 * <p>
		 * Casting to {@link CSSTypedValue} or
		 * {@link io.sf.carte.doc.style.css.property.TypedValue TypedValue} will give
		 * you access to the identifier value via {@link CSSTypedValue#getStringValue()
		 * getStringValue()}.
		 * </p>
		 */
		IDENT,

		/**
		 * Color.
		 * <p>
		 * Casting to {@link CSSColorValue} or
		 * {@link io.sf.carte.doc.style.css.property.ColorValue ColorValue} provides
		 * methods like {@link CSSColorValue#getColorModel() getColorModel()} or
		 * {@link CSSColorValue#getColor() getColor()} (the latter provides the actual
		 * color as a {@link CSSColor}).
		 * </p>
		 * <p>
		 * Once you retrieve the color, you can then use the indexed component access in
		 * {@link CSSColor#item(int)}, or cast it to a model sub-interface (like
		 * {@link RGBAColor}) according to the result of
		 * {@link CSSColorValue#getColorModel() getColorModel()}. See
		 * {@link CSSColorValue.ColorModel ColorModel} for details.
		 * </p>
		 */
		COLOR,

		/**
		 * Color-Mix function.
		 * <p>
		 * Cast to {@link CSSColorMixFunction}.
		 * </p>
		 * <p>
		 * {@link CSSColorValue#getColor() getColor()} provides the interpolated color
		 * as a {@link CSSColor}.
		 * </p>
		 * <p>
		 * Once you retrieve the color, you can then use the indexed component access in
		 * {@link CSSColor#item(int)}, or cast it to a model sub-interface (like
		 * {@link RGBAColor}) according to the result of {@link CSSColor#getColorModel()
		 * getColorModel()}. See {@link CSSColorValue.ColorModel ColorModel} for
		 * details.
		 * </p>
		 */
		COLOR_MIX,

		/**
		 * URI ({@code url()}).
		 * <p>
		 * Casting to {@link CSSTypedValue} or
		 * {@link io.sf.carte.doc.style.css.property.TypedValue TypedValue} will give
		 * you access to the URL value as a string, via
		 * {@link CSSTypedValue#getStringValue() getStringValue()} and is probably all
		 * that you need.
		 * </p>
		 * <p>
		 * You could also cast to {@link io.sf.carte.doc.style.css.property.URIValue
		 * URIValue}, which gives you a couple of convenience methods like
		 * {@link io.sf.carte.doc.style.css.property.URIValue#isEquivalent(io.sf.carte.doc.style.css.property.URIValue)
		 * isEquivalent} or
		 * {@link io.sf.carte.doc.style.css.property.URIValue#getURLValue()
		 * getURLValue()}.
		 * </p>
		 */
		URI,

		/**
		 * {@code src()} function.
		 * <p>
		 * Cast to {@link CSSFunctionValue} or
		 * {@link io.sf.carte.doc.style.css.property.FunctionValue FunctionValue}.
		 * </p>
		 */
		SRC,

		/**
		 * {@code rect()} function.
		 * <p>
		 * Cast to {@link CSSRectValue} or
		 * {@link io.sf.carte.doc.style.css.property.RectValue RectValue}.
		 * </p>
		 */
		RECT,

		/**
		 * An expression with algebraic syntax (i.e. <code>calc()</code>).
		 * <p>
		 * See {@link CSSExpressionValue}.
		 * </p>
		 */
		EXPRESSION,

		/**
		 * Gradient function.
		 * <p>
		 * Cast to {@link CSSGradientValue}.
		 * </p>
		 */
		GRADIENT,

		/**
		 * CSS <code>counter()</code> function.
		 * <p>
		 * See {@link CSSCounterValue}.
		 * </p>
		 */
		COUNTER,

		/**
		 * CSS <code>counters()</code> function.
		 * <p>
		 * See {@link CSSCountersValue}.
		 * </p>
		 */
		COUNTERS,

		/**
		 * <code>cubic-bezier()</code> easing function.
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		CUBIC_BEZIER,

		/**
		 * <code>linear()</code> easing function.
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		LINEAR,

		/**
		 * <code>steps()</code> easing function.
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		STEPS,

		/**
		 * Function.
		 * <p>
		 * Cast to {@link CSSFunctionValue} or
		 * {@link io.sf.carte.doc.style.css.property.FunctionValue FunctionValue}.
		 * </p>
		 * <p>
		 * Note: on functions, {@link CSSTypedValue#getStringValue()} must return the
		 * function name.
		 * </p>
		 */
		FUNCTION,

		/**
		 * Mathematical function (as defined by CSS Values and Units).
		 * <p>
		 * Cast to {@link CSSMathFunctionValue}.
		 * </p>
		 * <p>
		 * Note: on functions, {@link CSSTypedValue#getStringValue()} must return the
		 * function name.
		 * </p>
		 */
		MATH_FUNCTION,

		/**
		 * {@code circle()} function (see
		 * <a href="https://www.w3.org/TR/css-shapes-1/#funcdef-basic-shape-circle">CSS
		 * Shapes Level 1</a>).
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		CIRCLE,

		/**
		 * {@code ellipse()} function (see
		 * <a href="https://www.w3.org/TR/css-shapes-1/#funcdef-basic-shape-ellipse">CSS
		 * Shapes Level 1</a>).
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		ELLIPSE,

		/**
		 * {@code inset()} function (see
		 * <a href="https://www.w3.org/TR/css-shapes-1/#funcdef-basic-shape-inset">CSS
		 * Shapes Level 1</a>).
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		INSET,

		/**
		 * {@code path()} function (see
		 * <a href="https://www.w3.org/TR/css-shapes-1/#funcdef-basic-shape-path">CSS
		 * Shapes Level 1</a>).
		 * <p>
		 * Cast to {@link CSSPathValue}.
		 * </p>
		 */
		PATH,

		/**
		 * {@code polygon()} function (see
		 * <a href="https://www.w3.org/TR/css-shapes-1/#funcdef-basic-shape-polygon">CSS
		 * Shapes Level 1</a>).
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		POLYGON,

		/**
		 * {@code shape()} function (see
		 * <a href="https://drafts.csswg.org/css-shapes-2/#shape-function">CSS
		 * Shapes Level 2</a>).
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		SHAPE,

		/**
		 * {@code xywh()} function (see
		 * <a href="https://www.w3.org/TR/css-shapes-1/#funcdef-basic-shape-xywh">CSS
		 * Shapes Level 1</a>).
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		XYWH,

		/**
		 * Transform function (see <a href=
		 * "https://www.w3.org/TR/css-transforms-1/#typedef-transform-function">CSS
		 * Transforms Module Level 1</a>).
		 * <p>
		 * Cast to {@link CSSTransformFunction}.
		 * </p>
		 */
		TRANSFORM_FUNCTION,

		/**
		 * Unicode range. See {@link CSSUnicodeRangeValue}.
		 */
		UNICODE_RANGE,

		/**
		 * Unicode character. See {@link CSSUnicodeRangeValue.CSSUnicodeValue}.
		 */
		UNICODE_CHARACTER,

		/**
		 * Unicode wildcard. See {@link CSSUnicodeRangeValue}.
		 */
		UNICODE_WILDCARD,

		/**
		 * {@code image-set()}
		 * <p>
		 * Cast to {@link CSSFunctionValue}.
		 * </p>
		 */
		IMAGE_SET,

		/**
		 * Element reference.
		 * <p>
		 * Casting to {@link CSSTypedValue} or
		 * {@link io.sf.carte.doc.style.css.property.TypedValue TypedValue} will give
		 * you access to the reference value via {@link CSSTypedValue#getStringValue()}.
		 * </p>
		 */
		ELEMENT_REFERENCE,

		/**
		 * Ratio value.
		 * <p>
		 * Cast to {@link CSSRatioValue} or
		 * {@link io.sf.carte.doc.style.css.property.RatioValue RatioValue}.
		 * </p>
		 */
		RATIO,

		/**
		 * Environment variable. See {@link CSSEnvVariableValue}.
		 */
		ENV,

		/**
		 * Lexical value.
		 * <p>
		 * Cast to {@link CSSLexicalValue} or
		 * {@link io.sf.carte.doc.style.css.property.LexicalValue LexicalValue}.
		 * </p>
		 */
		LEXICAL,

		/**
		 * For this library's internal use.
		 */
		INTERNAL,

		/**
		 * Invalid (non-primitive and non-keyword) value.
		 * <p>
		 * The value is either a list or a shorthand.
		 * </p>
		 */
		INVALID

	}

	/**
	 * Get the general category to which this value belongs.
	 * 
	 * @return the general value type.
	 */
	CssType getCssValueType();

	/**
	 * Get the primitive type.
	 * 
	 * @return the primitive type.
	 */
	Type getPrimitiveType();

	/**
	 * Check whether this value is primitive, that is, either a
	 * {@link io.sf.carte.doc.style.css.CSSValue.CssType#TYPED TYPED} or
	 * {@link io.sf.carte.doc.style.css.CSSValue.CssType#PROXY PROXY} value.
	 * 
	 * @return {@code true} if the value is {@code TYPED} or {@code PROXY}.
	 */
	default boolean isPrimitiveValue() {
		return false;
	}

	/**
	 * Get a parsable representation of this value.
	 * 
	 * @return the CSS serialization of this value.
	 */
	String getCssText();

	/**
	 * Set this value according to the given parsable text.
	 * 
	 * @param cssText the text value.
	 * @throws DOMException INVALID_MODIFICATION_ERR if the text value represents a
	 *                      different type or the value cannot be modified. <br/>
	 *                      INVALID_CHARACTER_ERR if an invalid character was found.
	 *                      <br/>
	 *                      SYNTAX_ERR if the string is invalid CSS.
	 */
	void setCssText(String cssText) throws DOMException;

	/**
	 * Creates and returns a copy of this value.
	 *
	 * @return a clone of this value.
	 */
	CSSValue clone();

	/**
	 * Gives a minified version of the css text of the property.
	 *
	 * @return the minified css text.
	 */
	default String getMinifiedCssText() {
		return getMinifiedCssText(null);
	}

	/**
	 * Gives a minified version of the css text of the property.
	 * <p>
	 * It may be customized for the given property name.
	 * </p>
	 *
	 * @param propertyName the property name.
	 * @return the minified css text.
	 */
	default String getMinifiedCssText(String propertyName) {
		return getCssText();
	}

	/**
	 * Serialize this value to a {@link SimpleWriter}.
	 *
	 * @param wri the SimpleWriter.
	 * @throws IOException if an error happened while writing.
	 */
	void writeCssText(SimpleWriter wri) throws IOException;

	/**
	 * Get the list of preceding comments.
	 * 
	 * @return the list of preceding comments, or {@code null} if none.
	 */
	default StringList getPrecedingComments() {
		return null;
	}

	/**
	 * Get the list of trailing comments.
	 * 
	 * @return the list of trailing comments, or {@code null} if none.
	 */
	default StringList getTrailingComments() {
		return null;
	}

	/**
	 * Verify if this value matches the given grammar.
	 * <p>
	 * See also: {@link io.sf.carte.doc.style.css.parser.SyntaxParser SyntaxParser}.
	 * </p>
	 * 
	 * @param syntax the syntax.
	 * @return the matching for the syntax.
	 */
	Match matches(CSSValueSyntax syntax);

}
