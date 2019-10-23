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

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * A sum expression.
 *
 * @see CSSExpression
 */
class SumExpression extends StyleExpression implements AlgebraicExpression {
	private final LinkedList<StyleExpression> operands = new LinkedList<>();

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
	public StyleExpression item(int index) {
		try {
			return operands.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public int getLength() {
		return operands.size();
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
		StringBuilder buf = new StringBuilder(32);
		Iterator<StyleExpression> it = operands.iterator();
		CSSExpression expr = it.next();
		appendMinifiedFirstExpression(expr, buf);
		while (it.hasNext()) {
			expr = it.next();
			appendMinifiedExpression(expr, buf);
		}
		return buf.toString();
	}

	private void appendMinifiedFirstExpression(CSSExpression expr, StringBuilder buf) {
		boolean parens = false;
		if (expr.isInverseOperation()) {
			if (expr.getPartType() == AlgebraicPart.SUM) {
				buf.append(" - (");
				parens = true;
			} else {
				if (expr.getPartType() == AlgebraicPart.OPERAND) {
					CSSPrimitiveValue operand = ((CSSOperandExpression) expr).getOperand();
					NumberValue number;
					if (operand.getPrimitiveType() == Type.NUMERIC
							&& (number = (NumberValue) operand).isNegativeNumber()) {
						buf.append(" + ");
						buf.append(number.minifyAbsolute(""));
						return;
					}
				}
				buf.append(" - ");
			}
		}
		buf.append(expr.getMinifiedCssText());
		if (parens) {
			buf.append(')');
		}
	}

	private void appendMinifiedExpression(CSSExpression expr, StringBuilder buf) {
		boolean parens = false;
		// 8.1.1. "white space is required on both sides of the + and - operators."
		if (expr.isInverseOperation()) {
			if (expr.getPartType() == AlgebraicPart.SUM) {
				buf.append(" - (");
				parens = true;
			} else {
				if (expr.getPartType() == AlgebraicPart.OPERAND) {
					CSSPrimitiveValue operand = ((CSSOperandExpression) expr).getOperand();
					NumberValue number;
					if (operand.getPrimitiveType() == Type.NUMERIC
							&& (number = (NumberValue) operand).isNegativeNumber()) {
						buf.append(" + ");
						buf.append(number.minifyAbsolute(""));
						return;
					}
				}
				buf.append(" - ");
			}
		} else {
			if (expr.getPartType() == AlgebraicPart.OPERAND) {
				CSSPrimitiveValue operand = ((CSSOperandExpression) expr).getOperand();
				NumberValue number;
				if (operand.getPrimitiveType() == Type.NUMERIC
						&& (number = (NumberValue) operand).isNegativeNumber()) {
					buf.append(" - ");
					buf.append(number.minifyAbsolute(""));
					return;
				}
			}
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
			Iterator<StyleExpression> it = operands.iterator();
			StyleExpression expr = it.next();
			writeFirstExpression(expr, wri);
			while (it.hasNext()) {
				expr = it.next();
				writeExpression(expr, wri);
			}
		}
	}

	private void writeFirstExpression(StyleExpression expr, SimpleWriter wri) throws IOException {
		boolean parens = false;
		if (expr.isInverseOperation()) {
			if (expr.getPartType() == AlgebraicPart.SUM) {
				wri.write(" - (");
				parens = true;
			} else {
				if (expr.getPartType() == AlgebraicPart.OPERAND) {
					CSSPrimitiveValue operand = ((CSSOperandExpression) expr).getOperand();
					NumberValue number;
					if (operand.getPrimitiveType() == Type.NUMERIC
							&& (number = (NumberValue) operand).isNegativeNumber()) {
						wri.write(" + ");
						number.serializeAbsolute(wri);
						return;
					}
				}
				wri.write(" - ");
			}
		}
		expr.writeCssText(wri);
		if (parens) {
			wri.write(')');
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
				if (expr.getPartType() == AlgebraicPart.OPERAND) {
					CSSPrimitiveValue operand = ((CSSOperandExpression) expr).getOperand();
					NumberValue number;
					if (operand.getPrimitiveType() == Type.NUMERIC
							&& (number = (NumberValue) operand).isNegativeNumber()) {
						wri.write(" + ");
						number.serializeAbsolute(wri);
						return;
					}
				}
				wri.write(" - ");
			}
		} else {
			if (expr.getPartType() == AlgebraicPart.OPERAND) {
				CSSPrimitiveValue operand = ((CSSOperandExpression) expr).getOperand();
				NumberValue number;
				if (operand.getPrimitiveType() == Type.NUMERIC && (number = (NumberValue) operand).isNegativeNumber()) {
					wri.write(" - ");
					number.serializeAbsolute(wri);
					return;
				}
			}
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

	static AlgebraicExpression createSumExpression() {
		return new SumExpression();
	}

}
