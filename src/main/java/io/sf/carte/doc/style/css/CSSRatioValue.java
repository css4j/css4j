/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2017 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * The ratio value type is a number followed by optional whitespace, followed by
 * a solidus ('/'), followed by optional whitespace, followed by a number. (as
 * defined by Media Queries level 4)
 */
public interface CSSRatioValue extends CSSTypedValue {

	/**
	 * Get the antecedent value (i.e. the 'a' in 'a/b').
	 * 
	 * @return the antecedent value.
	 */
	CSSPrimitiveValue getAntecedentValue();

	/**
	 * Get the consequent value (i.e. the 'b' in 'a/b').
	 * 
	 * @return the consequent value.
	 */
	CSSPrimitiveValue getConsequentValue();

	@Override
	CSSRatioValue clone();

}
