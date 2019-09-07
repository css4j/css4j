/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Expression sub-interface to be implemented by operations like sum or product.
 */
public interface AlgebraicExpression extends CSSExpression {
	/**
	 * Retrieve the operand at index <code>index</code>.
	 *
	 * @return the operand at index <code>index</code>, or <code>null</code> if the
	 *         index is invalid.
	 */
	CSSExpression item(int index);

	/**
	 * Get the number of operands in this expression.
	 *
	 * @return the number of operands in this expression.
	 */
	int getLength();

	/**
	 * Create and return a copy of this object.
	 *
	 * @return a copy of this object.
	 */
	@Override
	AlgebraicExpression clone();

}
