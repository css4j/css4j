/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import io.sf.carte.util.SimpleWriter;

/**
 * Implemented by CSS expressions. See
 * <a href="https://www.w3.org/TR/css3-values/#calc-notation">CSS Values and Units Module
 * Level 3: Mathematical Expressions</a>
 */
public interface CSSExpression {

	/**
	 * Enumeration of expression parts (operations, operands).
	 */
	enum AlgebraicPart {
		/**
		 * A sum.
		 * <p>
		 * See {@link AlgebraicExpression}.
		 * </p>
		 */
		SUM,

		/**
		 * A product.
		 * <p>
		 * See {@link AlgebraicExpression}.
		 * </p>
		 */
		PRODUCT,

		/**
		 * An operand.
		 * <p>
		 * See {@link CSSOperandExpression}.
		 * </p>
		 */
		OPERAND
	}

	/**
	 * Get a string representation of the expression.
	 * 
	 * @return a string representation of the expression.
	 */
	String getCssText();

	/**
	 * Get a minified string representation of the expression.
	 * 
	 * @return a minified string representation of the expression.
	 */
	String getMinifiedCssText();

	/**
	 * Serialize this expression to a {@link SimpleWriter}.
	 * 
	 * @param wri
	 *            the SimpleWriter.
	 * @throws IOException
	 *            if an error happened while writing.
	 */
	void writeCssText(SimpleWriter wri) throws IOException;

	/**
	 * <p>
	 * Get the parent expression. In a nested expression, the outer expression is the parent.
	 * </p>
	 * <p>
	 * For example, in <code>a * (b + c)</code>, being the sum <code>(b + c)</code> this
	 * expression, its parent would be the product by <code>a</code>.
	 * </p>
	 * 
	 * @return the parent expression, <code>null</code> if none (this is root expression).
	 */
	CSSExpression getParentExpression();

	/**
	 * Is this an inverse operation ?
	 * <p>
	 * If there is a containing operation, the inverse is from the point of view of the
	 * parent operation. In a sum (or a product contained inside a sum), the inverse
	 * means that the minus sign applies to the whole operation. In a product it is
	 * a division.
	 * </p>
	 * 
	 * @return <code>true</code> if the operation is inverse.
	 */
	boolean isInverseOperation();

	/**
	 * Get the type of {@link AlgebraicPart}.
	 * 
	 * @return the type of part (operation, operand).
	 */
	AlgebraicPart getPartType();

	/**
	 * Create and return a copy of this object.
	 *
	 * @return a copy of this object.
	 */
	CSSExpression clone();

}
