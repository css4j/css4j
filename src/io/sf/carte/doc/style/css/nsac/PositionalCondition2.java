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

import org.w3c.css.sac.PositionalCondition;
import org.w3c.css.sac.SelectorList;

/**
 * Updates SAC's {@link PositionalCondition} interface.
 */
public interface PositionalCondition2 extends PositionalCondition {

	/**
	 * Is this a forward condition?
	 * 
	 * @return <code>true</code> if this is a forward condition (like nth-child),
	 *         <code>false</code> if not (like nth-last-child).
	 */
	public boolean isForwardCondition();

	/**
	 * Is this an of-type selector?
	 * <p>
	 * This method only returns <code>true</code> if the selector has been explicitly declared to apply
	 * to the same type, like in 'first-of-type' or 'nth-of-type'. It should return false
	 * otherwise, for example for selectors like the next one despite being equivalent to
	 * 'first-of-type': <code>div:nth-child(1 of div)</code>.
	 * <p>
	 * This method is essentially the same as the old
	 * <code>PositionalCondition.getType()</code>, but with a more detailed (and potentially
	 * different) specification.
	 * 
	 * @return <code>true</code> if this condition has been declared as to be applied to the collection
	 *         of elements that match the same type as the element to which it is applied
	 *         (like nth-of-type), <code>false</code> if not.
	 */
	public boolean isOfType();

	/**
	 * Get the An+B expression factor (i.e. &#39;A&#39;).
	 * 
	 * @return the An+B expression factor, zero if not specified.
	 */
	public int getFactor();

	/**
	 * Get the An+B expression offset (i.e. &#39;B&#39;).
	 * 
	 * @return the An+B expression offset, or the offset determined from the pseudo-class
	 *         name (e.g. 'first-child', 'first-of-type' and 'last-child' all mean 1).
	 */
	public int getOffset();

	/**
	 * Get the list of selectors that the children have to match.
	 * 
	 * @return the list of selectors that the children have to match, or null if not
	 *         specified.
	 */
	public SelectorList getOfList();

	/**
	 * The selector was specified with an argument ?
	 * <p>
	 * This is useful to tell apart <code>:first-child</code> from
	 * <code>:nth-child(1)</code>, for example.
	 * 
	 * @return <code>true</code> if the selector was specified with an argument.
	 */
	boolean hasArgument();

	/**
	 * The AnB expression is a keyword ?
	 * 
	 * @return <code>true</code> if the AnB expression is a keyword like
	 *         <code>odd</code>.
	 */
	boolean hasKeyword();

}
