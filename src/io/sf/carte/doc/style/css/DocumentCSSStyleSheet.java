/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.om.AbstractCSSRule;

/**
 * A style sheet that is able to take the responsibility to compute the styles associated
 * to document elements.
 * <p>
 * Accordingly, it has a target medium (which can be <code>null</code>) and has abilities
 * that 'normal' style sheets do not have, like dealing with rules that have different
 * origins (author, user, etc).
 *
 */
public interface DocumentCSSStyleSheet extends ExtendedCSSStyleSheet<AbstractCSSRule> {

	/**
	 * Gets the target medium for this sheet.
	 * 
	 * @return the target medium, or null if has not been set.
	 */
	public String getTargetMedium();

	/**
	 * Gets the computed style for the given Element and pseudo-element in the DOM Document
	 * associated to this style sheet.
	 * 
	 * @param elm
	 *            the element.
	 * @param pseudoElt
	 *            the pseudo-element.
	 * @return the computed style declaration.
	 */
	public CSSComputedProperties getComputedStyle(CSSElement elm, String pseudoElt);

	/**
	 * Clone this style sheet.
	 * 
	 * @return the cloned style sheet.
	 */
	@Override
	public DocumentCSSStyleSheet clone();

	/**
	 * Clone this style sheet, but only preserving rules targeting the given medium.
	 * 
	 * @param targetMedium
	 *            the medium.
	 * @return a medium-specific pseudo-clone of this sheet.
	 */
	public DocumentCSSStyleSheet clone(String targetMedium);

}
