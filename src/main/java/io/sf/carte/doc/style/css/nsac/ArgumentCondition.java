/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2025 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * A condition is applied to a list of selectors that is supplied as an argument to the
 * selector name.
 * <p>
 * Represents selectors like <code>:has(...)</code> and <code>:not(...)</code>.
 */
public interface ArgumentCondition extends Condition {

	/**
	 * Get the list of selectors that were supplied as argument.
	 *
	 * @return the list of argument selectors.
	 */
	SelectorList getSelectors();

	/**
	 * The name of the pseudo-class or pseudo-element.
	 *
	 * @return the name of the pseudo-class/element.
	 */
	String getName();
}
