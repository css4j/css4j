/*
 * This software includes material that extends the Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2017-2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * A primitive value containing an expression (like <code>calc()</code>).
 */
public interface CSSExpressionValue extends CSSMathValue {

	/**
	 * Get the root expression corresponding to this <code>calc()</code> value.
	 * 
	 * @return the expression.
	 */
	CSSExpression getExpression();

	@Override
	CSSExpressionValue clone();

}
