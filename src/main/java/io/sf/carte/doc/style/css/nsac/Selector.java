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

/**
 * Based on SAC's {@code Selector} interface by Philippe Le Hegaret.
 */
public interface Selector {

	enum SelectorType {

		/**
		 * The universal selector.
		 * <pre class="example">
		 *   *
		 * </pre>
		 * 
		 * @see ElementSelector
		 */
		UNIVERSAL,

		/**
		 * This is a conditional selector.
		 * <p>
		 * Example:
		 * </p>
		 * 
		 * <pre class="example">
		 *   simple[role="private"]
		 *   .part1
		 *   H1#myId
		 *   P:lang(fr).p1
		 * </pre>
		 *
		 * @see ConditionalSelector
		 */
		CONDITIONAL,

		/**
		 * This selector matches only element node.
		 * <p>
		 * Example:
		 * </p>
		 * 
		 * <pre class="example">
		 *   H1
		 *   animate
		 * </pre>
		 * 
		 * @see ElementSelector
		 */
		ELEMENT,

		/**
		 * This selector matches an arbitrary descendant of some ancestor element.
		 * <p>
		 * Example:
		 * </p>
		 * 
		 * <pre class="example">
		 *   E F
		 * </pre>
		 * 
		 * @see CombinatorSelector
		 */
		DESCENDANT,

		/**
		 * This selector matches a childhood relationship between two elements.
		 * <p>
		 * Example:
		 * </p>
		 * 
		 * <pre class="example">
		 * E &gt; F
		 * </pre>
		 * 
		 * @see CombinatorSelector
		 */
		CHILD,

		/**
		 * This selector matches two selectors who shared the same parent in the
		 * document tree and the element represented by the first sequence immediately
		 * precedes the element represented by the second one.
		 * <p>
		 * Example:
		 * </p>
		 * 
		 * <pre class="example">
		 * E + F
		 * </pre>
		 * 
		 * @see CombinatorSelector
		 */
		DIRECT_ADJACENT,

		/**
		 * <pre class="example">
		 *   E ~ F
		 * </pre>
		 *
		 * @see CombinatorSelector
		 */
		SUBSEQUENT_SIBLING,

		/**
		 * Column combinator.
		 * 
		 * <pre class="example">
		 * E || F
		 * </pre>
		 *
		 * @see CombinatorSelector
		 */
		COLUMN_COMBINATOR,

		/**
		 * Scope pseudo-selector in selector arguments.
		 * <p>
		 * Scope should be applied where this pseudo-selector is found.
		 * </p>
		 * <p>
		 * This selector has no serialization.
		 * </p>
		 * 
		 * @see Condition.ConditionType#SELECTOR_ARGUMENT
		 */
		SCOPE_MARKER

	}

	/**
	 * Gives the type of <code>Selector</code>
	 * 
	 * @return the type of selector.
	 */
	SelectorType getSelectorType();

}
