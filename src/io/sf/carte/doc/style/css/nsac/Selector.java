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

	/**
	 * This is a conditional selector. Example:
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
	short SAC_CONDITIONAL_SELECTOR = 0;

	/**
	 * The universal selector.
	 * 
	 * @see ElementSelector
	 */
	short SAC_UNIVERSAL_SELECTOR = 1;

	/**
	 * This selector matches only element node. Example:
	 * 
	 * <pre class="example">
	 *   H1
	 *   animate
	 * </pre>
	 * 
	 * @see ElementSelector
	 */
	short SAC_ELEMENT_NODE_SELECTOR = 4;

	/**
	 * This selector matches an arbitrary descendant of some ancestor element.
	 * Example:
	 * 
	 * <pre class="example">
	 *   E F
	 * </pre>
	 * 
	 * @see CombinatorSelector
	 */
	short SAC_DESCENDANT_SELECTOR = 10;

	/**
	 * This selector matches a childhood relationship between two elements. Example:
	 * 
	 * <pre class="example">
	 * E &gt; F
	 * </pre>
	 * 
	 * @see CombinatorSelector
	 */
	short SAC_CHILD_SELECTOR = 11;
	/**
	 * This selector matches two selectors who shared the same parent in the
	 * document tree and the element represented by the first sequence immediately
	 * precedes the element represented by the second one. Example:
	 * 
	 * <pre class="example">
	 * E + F
	 * </pre>
	 * 
	 * @see CombinatorSelector
	 */
	short SAC_DIRECT_ADJACENT_SELECTOR = 12;

	/**
	 * <pre class="example">
	 *   E ~ F
	 * </pre>
	 *
	 * @see CombinatorSelector
	 */
	short SAC_SUBSEQUENT_SIBLING_SELECTOR = 13;

	/**
	 * <pre class="example">
	 * E || F
	 * </pre>
	 *
	 * @see CombinatorSelector
	 */
	short SAC_COLUMN_COMBINATOR_SELECTOR = 14;

	/**
	 * Scope pseudo-selector in selector arguments.
	 * <p>
	 * Scope should be applied where this pseudo-selector is found.
	 * <p>
	 * This selector has no serialization.
	 * <p>
	 * 
	 * @see Condition#SAC_SELECTOR_ARGUMENT_CONDITION
	 */
	short SAC_SCOPE_SELECTOR = 15;

	/**
	 * An integer indicating the type of <code>Selector</code>
	 * 
	 * @return the type of selector.
	 */
	short getSelectorType();

}
