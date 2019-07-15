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

import org.w3c.css.sac.Condition;

/**
 * Updates SAC's {@link Condition} interface.
 */
public interface Condition2 extends Condition {

	/**
	 * This condition checks the beginning of an attribute value. Example:
	 * 
	 * <pre class="example">
	 *   [restart^="never"]
	 * </pre>
	 * 
	 * @see org.w3c.css.sac.AttributeCondition
	 */
	public static final short SAC_BEGINS_ATTRIBUTE_CONDITION = 14;

	/**
	 * This condition checks the end of an attribute value. Example:
	 * 
	 * <pre class="example">
	 *   [restart$="never"]
	 * </pre>
	 * 
	 * @see org.w3c.css.sac.AttributeCondition
	 */
	public static final short SAC_ENDS_ATTRIBUTE_CONDITION = 15;

	/**
	 * This condition checks a substring of an attribute value. Example:
	 * 
	 * <pre class="example">
	 *   [restart*="never"]
	 * </pre>
	 * 
	 * @see org.w3c.css.sac.AttributeCondition
	 */
	public static final short SAC_SUBSTRING_ATTRIBUTE_CONDITION = 16;

	/**
	 * This condition checks the selector list argument to which a pseudo-class applies.
	 * Example:
	 * 
	 * <pre class="example">
	 *   :not(:visited,:hover)
	 * </pre>
	 * 
	 * @see ArgumentCondition
	 */
	public static final short SAC_SELECTOR_ARGUMENT_CONDITION = 17;

	/**
	 * This condition checks for pseudo elements. Example:
	 * 
	 * <pre class="example">
	 *   ::first-line
	 *   ::first-letter
	 * </pre>
	 * 
	 * @see org.w3c.css.sac.AttributeCondition#getLocalName()
	 */
	public static final short SAC_PSEUDO_ELEMENT_CONDITION = 18;
}
