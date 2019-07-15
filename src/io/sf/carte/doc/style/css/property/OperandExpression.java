/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;

/**
 * An operand expression.
 * 
 * @see CSSExpression.CSSOperandExpression
 */
public class OperandExpression extends AbstractCSSExpression implements CSSExpression.CSSOperandExpression {
	ExtendedCSSPrimitiveValue operand = null;

	OperandExpression() {
		super();
	}

	OperandExpression(OperandExpression copyFrom) {
		super(copyFrom);
		this.operand = copyFrom.operand;
	}

	@Override
	void addExpression(AbstractCSSExpression expr) {
	}

	@Override
	public ExtendedCSSPrimitiveValue getOperand() {
		return operand;
	}

	public void setOperand(ExtendedCSSPrimitiveValue operand) {
		this.operand = operand;
	}

	@Override
	public AlgebraicPart getPartType() {
		return AlgebraicPart.OPERAND;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((operand == null) ? 0 : operand.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		// Superclass check guarantees that the class of obj is OperandExpression
		OperandExpression other = (OperandExpression) obj;
		if (operand == null) {
			if (other.operand != null)
				return false;
		} else if (!operand.equals(other.operand))
			return false;
		return true;
	}

	@Override
	public String getCssText() {
		return operand == null ? "" : operand.getCssText();
	}

	@Override
	public String getMinifiedCssText() {
		return operand == null ? "" : operand.getMinifiedCssText("");
	}

	@Override
	public OperandExpression clone() {
		return new OperandExpression(this);
	}

}
