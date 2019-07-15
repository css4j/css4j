/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 * 
 * Copyright © 2017 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */
package io.sf.carte.doc.style.css.nsac;

import org.w3c.css.sac.Selector;

/**
 * Updates SAC's {@link Selector} interface.
 */
public interface Selector2 extends Selector {

	/**
	 * <pre class="example">
	 *   E ~ F
	 * </pre>
	 * 
	 * @see org.w3c.css.sac.SiblingSelector
	 */
	public static final short SAC_SUBSEQUENT_SIBLING_SELECTOR = 13;

	/**
	 * <pre class="example">
	 * E || F
	 * </pre>
	 * 
	 * @see org.w3c.css.sac.DescendantSelector
	 */
	public static final short SAC_COLUMN_COMBINATOR_SELECTOR = 14;

	/**
	 * Scope pseudo-selector in selector arguments.
	 * <p>
	 * Scope should be applied where this pseudo-selector is found.
	 * <p>
	 * This selector has no serialization.
	 * <p>
	 * @see Condition2#SAC_SELECTOR_ARGUMENT_CONDITION
	 */
	public static final short SAC_SCOPE_SELECTOR = 15;

}
