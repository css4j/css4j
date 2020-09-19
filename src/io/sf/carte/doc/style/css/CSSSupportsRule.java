/*
 * This software extends interfaces defined by CSS Conditional Rules Module Level 3
 *  (https://www.w3.org/TR/css3-conditional/).
 * Copyright © 2013 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2017,2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * Represents a ‘{@literal @}supports’ rule.
 */
public interface CSSSupportsRule extends CSSConditionRule {

	/**
	 * Get the object model representation of the condition associated to this rule.
	 * 
	 * @return the object model representation of the condition associated to this rule.
	 */
	BooleanCondition getCondition();

	/**
	 * Does the given style database support the condition associated to this rule ?
	 * 
	 * @param styleDatabase
	 *            the style database to test.
	 * @return <code>true</code> if the style database supports the condition, <code>false</code>
	 *         otherwise.
	 */
	boolean supports(StyleDatabase styleDatabase);

}
