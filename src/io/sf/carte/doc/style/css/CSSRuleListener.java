/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSPageRule;

/**
 * A CSS-rule listener.
 * <p>
 * Some use cases require up-to-date information on available font-face and page
 * rules, and scanning the rule lists of the style sheet(s) is not efficient.
 * This mechanism allows a listener to be updated about rule events.
 * 
 * @author Carlos
 *
 */
public interface CSSRuleListener {

	/**
	 * Try to load the font family according to the given font face rule, and
	 * make it available to the canvas.
	 * 
	 * @param rule
	 *            the font face rule.
	 */
	public void onFontFaceRule(CSSFontFaceRule rule);

	/**
	 * Apply the configurations specified by the given {@literal @}page rule.
	 * 
	 * @param rule the {@literal @}page rule.
	 */
	public void onPageRule(CSSPageRule rule);
}
