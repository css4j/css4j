/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * Conditional selector, by Philippe Le Hegaret
 */
public interface ConditionalSelector extends SimpleSelector {
	/**
	 * Returns the simple selector.
	 * <p>
	 * The simple selector can't be a <code>ConditionalSelector</code>.
	 * </p>
	 * @return the simple selector.
	 */
	SimpleSelector getSimpleSelector();

	/**
	 * Returns the condition to be applied on the simple selector.
	 */
	Condition getCondition();

}
