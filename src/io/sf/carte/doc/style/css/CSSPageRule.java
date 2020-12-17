/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * A CSS {@literal @}page rule.
 *
 */
public interface CSSPageRule extends CSSDeclarationRule {

	/**
	 * Get the list of page-margin rules.
	 * 
	 * @return the list of page-margin rules, or {@code null} if there are no margin
	 *         rules.
	 */
	CSSRuleList<? extends CSSMarginRule> getMarginRules();

	String getSelectorText();

	void setSelectorText(String selectorText) throws DOMException;

}
