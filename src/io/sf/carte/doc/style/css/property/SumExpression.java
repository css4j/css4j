/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * A sum expression.
 * 
 * @see CSSExpression.AlgebraicExpression
 */
public class SumExpression extends StyleExpression implements StyleExpression.AlgebraicExpression {
	LinkedList<StyleExpression> operands = new LinkedList<StyleExpression>();

	SumExpression() {
		super();
	}

	SumExpression(SumExpression copyFrom) {
		super();
		Iterator<StyleExpression> it = copyFrom.operands.iterator();
		while (it.hasNext()) {
			this.operands.add(it.next().clone());
		}
	}

	@Override
	void addExpression(StyleExpression expr) {
		operands.add(expr);
		expr.setParentExpression(this);
		if (nextOperandInverse) {
			expr.setInverseOperation(true);
			nextOperandInverse = false;
		}
	}

	@Override
	void replaceLastExpression(StyleExpression operation) {
		StyleExpression lastexpr = operands.removeLast();
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
	public List<StyleExpression> getOperands() {
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
		if (operands.isEmpty()) {
			return "";
		}
		BufferSimpleWriter wri = new BufferSimpleWriter(32 + operands.size() * 16);
		try {
			writeCssText(wri);
		} catch (IOException e) {
		}
		return wri.toString();
	}

	@Override
	public String getMinifiedCssText() {
		if (operands.isEmpty()) {
			return "";
		}
		boolean parens = false;
		StringBuilder buf = new StringBuilder(32);
		Iterator<StyleExpression> it = operands.iterator();
		CSSExpression expr = it.next();
		if (expr.isInverseOperation()) {
			if (expr.getPartType() == AlgebraicPart.SUM) {
				buf.append(" - (");
				parens = true;
			} else {
				buf.append(" - ");
			}
		}
		buf.append(expr.getMinifiedCssText());
		if (parens) {
			buf.append(')');
		}
		while (it.hasNext()) {
			expr = it.next();
			appendMinifiedExpression(expr, buf);
		}
		return buf.toString();
	}

	private void appendMinifiedExpression(CSSExpression expr, StringBuilder buf) {
		boolean parens = false;
		// 8.1.1. "white space is required on both sides of the + and - operators."
		if (expr.isInverseOperation()) {
			if (expr.getPartType() == AlgebraicPart.SUM) {
				buf.append(" - (");
				parens = true;
			} else {
				buf.append(" - ");
			}
		} else {
			buf.append(" + ");
		}
		buf.append(expr.getMinifiedCssText());
		if (parens) {
			buf.append(')');
		}
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		if (!operands.isEmpty()) {
			boolean parens = false;
			Iterator<StyleExpression> it = operands.iterator();
			StyleExpression expr = it.next();
			if (expr.isInverseOperation()) {
				if (expr.getPartType() == AlgebraicPart.SUM) {
					wri.write(" - (");
					parens = true;
				} else {
					wri.write(" - ");
				}
			}
			expr.writeCssText(wri);
			if (parens) {
				wri.write(')');
			}
			while (it.hasNext()) {
				expr = it.next();
				writeExpression(expr, wri);
			}
		}
	}

	private void writeExpression(StyleExpression expr, SimpleWriter wri) throws IOException {
		boolean parens = false;
		// 8.1.1. "white space is required on both sides of the + and - operators."
		if (expr.isInverseOperation()) {
			if (expr.getPartType() == AlgebraicPart.SUM) {
				wri.write(" - (");
				parens = true;
			} else {
				wri.write(" - ");
			}
		} else {
			wri.write(" + ");
		}
		expr.writeCssText(wri);
		if (parens) {
			wri.write(')');
		}
	}

	@Override
	public SumExpression clone() {
		return new SumExpression(this);
	}

}

