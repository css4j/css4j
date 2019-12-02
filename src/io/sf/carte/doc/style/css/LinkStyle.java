/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

public interface LinkStyle<R extends CSSRule> {

	/**
	 * Get the style sheet that this node links to.
	 * 
	 * @return the style sheet.
	 */
	CSSStyleSheet<R> getSheet();

}
