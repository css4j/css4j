/*
 * This software includes interfaces defined by CSS Counter Styles Level 3
 *  (https://www.w3.org/TR/css-counter-styles-3/).
 * Copyright © 2017 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * Counter-style rule. @see <a href="https://www.w3.org/TR/css-counter-styles-3/">CSS Counter Styles Level 3</a>.
 */
public interface CSSCounterStyleRule extends CSSRule {

	/**
	 * Gets the counter-style name.
	 * 
	 * @return the counter-style name.
	 */
	String getName();

}
