/*
 * This software extends interfaces defined by CSS Animations Level 1
 *  (https://drafts.csswg.org/css-animations/).
 * Copyright © 2018 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.om.CSSRuleArrayList;

/**
 * <code>CSSKeyframesRule</code> represents a complete set of keyframes for a single
 * animation.
 *
 */
public interface CSSKeyframesRule extends ExtendedCSSRule {

	/**
	 * Gets the name of the keyframes.
	 * 
	 * @return the name of the keyframes.
	 */
	String getName();

	/**
	 * Gets the list of keyframe rules.
	 * 
	 * @return the list of keyframe rules.
	 */
	CSSRuleArrayList getCssRules();

	/**
	 * Appends a new rule into this keyframes rule collection.
	 * 
	 * @param rule
	 *            The parsable text representing the rule.
	 */
	void appendRule(String rule);

	/**
	 * Deletes the last declared <code>CSSKeyframeRule</code> matching the specified keyframe
	 * selector from this <code>keyframes</code> rule collection. If no matching rule exists,
	 * the method does nothing.
	 * <p>
	 * The number and order of the values in the specified keyframe selector must match those
	 * of the targeted keyframe rule(s). The match is not sensitive to white space around the
	 * values in the list.
	 * 
	 * @param select
	 *            The keyframe selector of the rule to be deleted: a comma-separated list of
	 *            keywords or percentage values between 0% and 100%.
	 */
	void deleteRule(String select);

	/**
	 * The findRule returns the last declared CSSKeyframeRule matching the specified keyframe
	 * selector. If no matching rule exists, the method does nothing.
	 * 
	 * @param select
	 *            The keyframe selector of the rule to be deleted: a comma-separated list of
	 *            keywords or percentage values between 0% and 100%.
	 * @return the found rule, or null if no rule was found.
	 */
	CSSKeyframeRule findRule(String select);

}