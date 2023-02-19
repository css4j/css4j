/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.util.SimpleWriter;

/**
 * An operand expression.
 *
 * @see CSSExpression
 */
class OperandExpression extends StyleExpression implements CSSOperandExpression {

	private static final long serialVersionUID = 1L;

	CSSPrimitiveValue operand = null;

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
	public CSSPrimitiveValue getOperand() {
		return operand;
	}

	@Override
	public void setOperand(CSSPrimitiveValue operand) {
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
			return other.operand == null;
		} else {
			return operand.equals(other.operand);
		}
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

	static OperandExpression createOperand(CSSTypedValue operand) {
		OperandExpression opexpr = new OperandExpression();
		opexpr.setOperand(operand);
		return opexpr;
	}

}
