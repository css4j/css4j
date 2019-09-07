/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.util.SimpleWriter;

/**
 * An operand expression.
 *
 * @see CSSExpression
 */
public class OperandExpression extends StyleExpression implements CSSOperandExpression {
	ExtendedCSSPrimitiveValue operand = null;

	OperandExpression() {
		super();
	}

	OperandExpression(OperandExpression copyFrom) {
		super(copyFrom);
		this.operand = copyFrom.operand;
	}

	@Override
	void addExpression(StyleExpression expr) {
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
	public void writeCssText(SimpleWriter wri) throws IOException {
		if (operand != null) {
			operand.writeCssText(wri);
		}
	}

	@Override
	public OperandExpression clone() {
		return new OperandExpression(this);
	}

	public static OperandExpression createOperand(ExtendedCSSPrimitiveValue operand) {
		OperandExpression opexpr = new OperandExpression();
		opexpr.setOperand(operand);
		return opexpr;
	}

}
