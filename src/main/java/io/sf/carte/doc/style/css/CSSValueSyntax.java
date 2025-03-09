/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a CSS value syntax component.
 * <p>
 * Example: {@code <length>}.
 * </p>
 * <p>
 * See also:
 * </p>
 * <ul>
 * <li>{@link CSSValue#matches(CSSValueSyntax)}</li>
 * <li>{@link io.sf.carte.doc.style.css.nsac.LexicalUnit#matches(CSSValueSyntax)
 * LexicalUnit.matches(CSSValueSyntax)}</li>
 * <li>{@link io.sf.carte.doc.style.css.parser.SyntaxParser SyntaxParser}</li>
 * </ul>
 */
public interface CSSValueSyntax {

	/**
	 * The syntax category.
	 */
	enum Category {
		/**
		 * The universal syntax.
		 * <p>
		 * <code>*</code>
		 * </p>
		 * <p>
		 * Matches as {@code TRUE} for all values except those of type {@code UNKNOWN}
		 * (in which case gives {@code PENDING}).
		 * </p>
		 */
		universal,

		/**
		 * Matches any length.
		 * <p>
		 * <code>&lt;length&gt;</code>
		 * </p>
		 */
		length,

		/**
		 * Matches any length or percentage.
		 * <p>
		 * <code>&lt;length-percentage&gt;</code>
		 * </p>
		 */
		lengthPercentage,

		/**
		 * Matches any percentage.
		 * <p>
		 * <code>&lt;percentage&gt;</code>
		 * </p>
		 */
		percentage,

		/**
		 * Matches any number.
		 * <p>
		 * <code>&lt;number&gt;</code>
		 * </p>
		 */
		number,

		/**
		 * Matches any color.
		 * <p>
		 * <code>&lt;color&gt;</code>
		 * </p>
		 */
		color,

		/**
		 * Matches any image.
		 * <p>
		 * <code>&lt;image&gt;</code>
		 * </p>
		 */
		image,

		/**
		 * Matches any url.
		 * <p>
		 * <code>&lt;url&gt;</code>
		 * </p>
		 */
		url,

		/**
		 * Matches any integer.
		 * <p>
		 * <code>&lt;integer&gt;</code>
		 * </p>
		 */
		integer,

		/**
		 * Matches any angle.
		 * <p>
		 * <code>&lt;angle&gt;</code>
		 * </p>
		 */
		angle,

		/**
		 * Matches any time.
		 * <p>
		 * <code>&lt;time&gt;</code>
		 * </p>
		 */
		time,

		/**
		 * Matches any frequency.
		 * <p>
		 * <code>&lt;frequency&gt;</code>
		 * </p>
		 */
		frequency,

		/**
		 * Matches a flexible length.
		 * <p>
		 * <code>&lt;flex&gt;</code>
		 * </p>
		 */
		flex,

		/**
		 * Matches any resolution.
		 * <p>
		 * <code>&lt;resolution&gt;</code>
		 * </p>
		 */
		resolution,

		/**
		 * Matches an unicode range.
		 * <p>
		 * <code>&lt;unicode-range&gt;</code>
		 * </p>
		 */
		unicodeRange,

		/**
		 * Matches any transform function, like {@code translate()} or {@code scale()}.
		 * <p>
		 * <code>&lt;transform-function&gt;</code>
		 * </p>
		 * <p>
		 * See <a href="https://www.w3.org/TR/css-transforms-1/#transform-functions">CSS
		 * Transforms Module Level 1</a>.
		 * </p>
		 */
		transformFunction,

		/**
		 * Matches a list of valid {@link #transformFunction} values.
		 * <p>
		 * {@code <transform-list>} is a pre-multiplied data type name equivalent to
		 * <code>&lt;transform-function&gt;+</code>.
		 * </p>
		 */
		transformList,

		/**
		 * Matches any string.
		 * <p>
		 * <code>&lt;string&gt;</code>
		 * </p>
		 */
		string,

		/**
		 * Matches any counter ({@code <counter()> | <counters()>} ).
		 * <p>
		 * <code>&lt;counter&gt;</code>
		 * </p>
		 */
		counter,

		/**
		 * Matches a basic shape ({@code rect()}, {@code path()}, etc.)
		 * <p>
		 * <code>&lt;basic-shape&gt;</code>
		 * </p>
		 */
		basicShape,

		/**
		 * Matches any valid custom identifier.
		 * <p>
		 * <code>&lt;custom-ident&gt;</code>
		 * </p>
		 */
		customIdent,

		/**
		 * The specific identifier returned by {@code getName()}.
		 */
		IDENT
	}

	/**
	 * The syntax multiplier.
	 */
	enum Multiplier {
		/**
		 * A space-separated list.
		 */
		PLUS,

		/**
		 * A comma-separated list.
		 */
		NUMBER,

		/**
		 * No multiplier.
		 */
		NONE
	}

	enum Match {
		/**
		 * Matches.
		 */
		TRUE,

		/**
		 * Does not match.
		 */
		FALSE,

		/**
		 * Value is either a {@link CSSValue.CssType#KEYWORD KEYWORD} or a
		 * {@link CSSValue.CssType#PROXY PROXY}, and matching cannot be determined until
		 * it is substituted at computed-style time.
		 * <p>
		 * Note that a {@code PROXY} value like:
		 * </p>
		 * <p>
		 * {@code attr(size type(<length>), auto)}
		 * </p>
		 * <p>
		 * would match as {@code PENDING} against {@code <lenght>}, but on
		 * {@code <lenght> | <ident>} would give {@code TRUE}, and {@code FALSE} on
		 * {@code <color>}.
		 * </p>
		 */
		PENDING,
	}

	/**
	 * The category corresponding to the name of the component.
	 * 
	 * @return the category.
	 */
	Category getCategory();

	/**
	 * The name of the component.
	 * 
	 * @return the name.
	 */
	String getName();

	/**
	 * The multiplier.
	 * 
	 * @return the multiplier.
	 */
	Multiplier getMultiplier();

	/**
	 * The next syntax component after the {@code |} character, if any.
	 * 
	 * @return the next component, or {@code null} if there is none.
	 */
	CSSValueSyntax getNext();

	/**
	 * Create a shallow clone of this syntax, <i>i.e.</i> one that does not have a
	 * {@code next} syntax component.
	 * <p>
	 * If this syntax has no next component, returns itself.
	 * </p>
	 * 
	 * @return a shallow clone of this syntax.
	 */
	CSSValueSyntax shallowClone();

}
