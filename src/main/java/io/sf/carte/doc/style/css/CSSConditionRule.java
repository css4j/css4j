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

import org.w3c.dom.DOMException;

/**
 * Represents all the "conditional" at-rules, which consist of a condition and a statement
 * block.
 */
public interface CSSConditionRule extends CSSGroupingRule {

	/**
	 * Gets the serialization of the condition of this rule.
	 * 
	 * @return the serialization of the condition of this rule.
	 */
	String getConditionText();

	/**
	 * Sets the condition associated to this rule.
	 * 
	 * @param conditionText
	 *            the condition text.
	 * @throws DOMException
	 */
	void setConditionText(String conditionText) throws DOMException;

}