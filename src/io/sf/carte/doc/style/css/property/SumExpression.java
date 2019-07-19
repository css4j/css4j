/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.sf.carte.doc.style.css.CSSExpression;

/**
 * A sum expression.
 * 
 * @see CSSExpression.AlgebraicExpression
 */
public class SumExpression extends AbstractCSSExpression implements AbstractCSSExpression.AlgebraicExpression {
	LinkedList<AbstractCSSExpression> operands = new LinkedList<AbstractCSSExpression>();

	SumExpression() {
		super();
	}

	SumExpression(SumExpression copyFrom) {
		super();
		Iterator<AbstractCSSExpression> it = copyFrom.operands.iterator();
		while (it.hasNext()) {
			this.operands.add(it.next().clone());
		}
	}

	@Override
	void addExpression(AbstractCSSExpression expr) {
		operands.add(expr);
		expr.setParentExpression(this);
		if (nextOperandInverse) {
			expr.setInverseOperation(true);
			nextOperandInverse = false;
		}
	}

	@Override
	void replaceLastExpression(AbstractCSSExpression operation) {
		AbstractCSSExpression lastexpr = operands.removeLast();
		if (lastexpr.isInverseOperation()) {
			lastexpr.setInverseOperation(false);
			operation.setInverseOperation(true);
		}
		lastexpr.setParentExpression(null);
		operation.addExpression(lastexpr);
		operation.setParentExpression(this);
		operands.addLast(operation);
	}

	@Override
	public List<AbstractCSSExpression> getOperands() {
		return operands;
	}

	@Override
	public AlgebraicPart getPartType() {
		return AlgebraicPart.SUM;
	}

	@Override
	public int hashCode() {
		final int prime = 1021;
		int result = super.hashCode();
		result = prime * result + ((operands == null) ? 0 : operands.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		// Superclass check guarantees that the class of obj is SumExpression
		SumExpression other = (SumExpression) obj;
		if (operands == null) {
			if (other.operands != null)
				return false;
		} else if (!operands.equals(other.operands))
			return false;
		return true;
	}

	@Override
	public String getCssText() {
		StringBuilder buf = new StringBuilder();
		Iterator<AbstractCSSExpression> it = operands.iterator();
		if (!it.hasNext()) {
			return "";
		}
		CSSExpression expr = it.next();
		// 8.1.1. "white space is required on both sides of the + and - operators."
		if (expr.getPartType() != AlgebraicPart.SUM && expr.isInverseOperation()) {
			buf.append(' ').append('-').append(' ');
		}
		buf.append(expr.getCssText());
		while (it.hasNext()) {
			boolean parens = false;
			expr = it.next();
			if (expr.getPartType() == AlgebraicPart.SUM && expr.isInverseOperation()) {
				buf.append(' ').append('-').append(' ').append('(');
				parens = true;
			} else if (expr.getPartType() != AlgebraicPart.SUM && expr.isInverseOperation()) {
				buf.append(' ').append('-').append(' ');
			} else {
				buf.append(' ').append('+').append(' ');
			}
			buf.append(expr.getCssText());
			if (parens) {
				buf.append(')');
			}
		}
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText() {
		StringBuilder buf = new StringBuilder();
		Iterator<AbstractCSSExpression> it = operands.iterator();
		if (!it.hasNext()) {
			return "";
		}
		CSSExpression expr = it.next();
		// 8.1.1. "white space is required on both sides of the + and - operators."
		if (expr.getPartType() != AlgebraicPart.SUM && expr.isInverseOperation()) {
			buf.append(' ').append('-').append(' ');
		}
		buf.append(expr.getMinifiedCssText());
		while (it.hasNext()) {
			boolean parens = false;
			expr = it.next();
			if (expr.getPartType() == AlgebraicPart.SUM && expr.isInverseOperation()) {
				buf.append(' ').append('-').append(' ').append('(');
				parens = true;
			} else if (expr.getPartType() != AlgebraicPart.SUM && expr.isInverseOperation()) {
				buf.append(' ').append('-').append(' ');
			} else {
				buf.append(' ').append('+').append(' ');
			}
			buf.append(expr.getMinifiedCssText());
			if (parens) {
				buf.append(')');
			}
		}
		return buf.toString();
	}

	@Override
	public SumExpression clone() {
		return new SumExpression(this);
	}

}

