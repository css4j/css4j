/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.nsac.Condition;
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
public interface DocumentCSSStyleSheet extends CSSStyleSheet<AbstractCSSRule> {

	/**
	 * Gets the target medium for this sheet.
	 *
	 * @return the target medium, or null if has not been set.
	 */
	String getTargetMedium();

	/**
	 * Gets the computed style for the given Element and pseudo-element in the DOM Document
	 * associated to this style sheet.
	 *
	 * @param elm
	 *            the element.
	 * @param pseudoElt
	 *            the pseudo-element condition.
	 * @return the computed style declaration.
	 */
	CSSComputedProperties getComputedStyle(CSSElement elm, Condition pseudoElt);

	/**
	 * Registers the definition of a custom property.
	 * 
	 * @param definition the definition.
	 */
	void registerProperty(CSSPropertyDefinition definition);

	/**
	 * Clone this style sheet.
	 *
	 * @return the cloned style sheet.
	 */
	@Override DocumentCSSStyleSheet clone();

	/**
	 * Clone this style sheet, but only preserving rules targeting the given medium.
	 *
	 * @param targetMedium
	 *            the medium.
	 * @return a medium-specific pseudo-clone of this sheet.
	 */
	DocumentCSSStyleSheet clone(String targetMedium);

}
