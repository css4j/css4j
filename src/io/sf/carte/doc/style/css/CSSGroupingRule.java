/*
 * This software extends interfaces defined by CSS Conditional Rules Module Level 3
 *  (https://www.w3.org/TR/css3-conditional/).
 * Copyright © 2013 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * Represents an at-rule that contains other rules nested inside itself.
 */
public interface CSSGroupingRule extends ExtendedCSSRule {

	/**
	 * Get the list of CSS rules nested inside the grouping rule.
	 * 
	 * @return a CSSRuleList object for the list of CSS rules nested inside the grouping rule.
	 */
	ExtendedCSSRuleList<? extends ExtendedCSSRule> getCssRules();

	/**
	 * Inserts a new rule into this grouping rule collection.
	 * 
	 * @param rule
	 *            The parsable text representing the rule.
	 * @param index
	 *            The index within the collection of the rule before which to insert the
	 *            specified rule. If the specified index is equal to the length of the rule
	 *            collection, the rule will be added to its end.
	 * @return the index at which the rule was inserted.
	 * @throws DOMException
	 *             if the index is out of bounds or there was a problem parsing the rule.
	 */
	int insertRule(String rule, int index) throws DOMException;

	/**
	 * Removes a CSS rule from the CSS rule list returned by {@link #getCssRules()} at
	 * <code>index</code>.
	 * 
	 * @param index the rule list index at which the rule must be removed.
	 * @throws DOMException
	 *             INDEX_SIZE_ERR if <code>index</code> is greater than or equal to
	 *             {@link #getCssRules()}.getLength().
	 */
	void deleteRule(int index) throws DOMException;

}