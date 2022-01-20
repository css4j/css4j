/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Sub-interface to be implemented by individual operands.
 */
public interface CSSOperandExpression extends CSSExpression {
	/**
	 * Retrieve the operand.
	 *
	 * @return the primitive operand.
	 */
	CSSPrimitiveValue getOperand();

	/**
	 * Create and return a copy of this object.
	 *
	 * @return a copy of this object.
	 */
	@Override
	CSSOperandExpression clone();

	/**
	 * Set the operand value.
	 * 
	 * @param operand the operand value.
	 */
	void setOperand(CSSPrimitiveValue operand);

}
