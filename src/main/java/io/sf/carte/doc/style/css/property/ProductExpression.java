/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * A product expression.
 *
 * @see CSSExpression
 */
class ProductExpression extends StyleExpression implements AlgebraicExpression {

	private static final long serialVersionUID = 1L;

	private final LinkedList<CSSExpression> operands = new LinkedList<>();

	ProductExpression() {
		super();
	}

	ProductExpression(ProductExpression copyFrom) {
		super(copyFrom);
		Iterator<CSSExpression> it = copyFrom.operands.iterator();
		while (it.hasNext()) {
			this.operands.add(it.next().clone());
		}
	}

	@Override
	void addExpression(StyleExpression expr) {
		StyleExpression oparent = expr.getParentExpression();
		if (oparent != null && oparent.getPartType() == AlgebraicPart.SUM) {
			oparent.replaceLastExpression(this);
		} else {
			operands.add(expr);
			expr.setParentExpression(this);
			if (nextOperandInverse) {
				expr.setInverseOperation(true);
				nextOperandInverse = false;
			}
		}
	}

	@Override
	public CSSExpression item(int index) {
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
		return AlgebraicPart.PRODUCT;
	}

	@Override
	public int hashCode() {
		final int prime = 211;
		int result = super.hashCode();
		result = prime * result + ((operands == null) ? 0 : operands.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		// Superclass check guarantees that the class of obj is ProductExpression
		ProductExpression other = (ProductExpression) obj;
		if (operands == null) {
			return other.operands == null;
		} else {
			return operands.equals(other.operands);
		}
	}

	@Override
	public ProductExpression clone() {
		return new ProductExpression(this);
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
		Iterator<CSSExpression> it = operands.iterator();
		CSSExpression expr = it.next();
		if (expr.getPartType() == AlgebraicPart.SUM) {
			buf.append('(').append(expr.getMinifiedCssText()).append(')');
		} else {
			buf.append(expr.getMinifiedCssText());
		}
		while (it.hasNext()) {
			expr = it.next();
			if (expr.isInverseOperation()) {
				buf.append('/');
			} else {
				buf.append('*');
			}
			if (expr.getPartType() == AlgebraicPart.SUM) {
				buf.append('(').append(expr.getMinifiedCssText()).append(')');
			} else {
				buf.append(expr.getMinifiedCssText());
			}
		}
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		Iterator<CSSExpression> it = operands.iterator();
		if (!it.hasNext()) {
			return;
		}
		CSSExpression expr = it.next();
		if (expr.getPartType() == AlgebraicPart.SUM) {
			wri.write('(');
			expr.writeCssText(wri);
			wri.write(')');
		} else {
			expr.writeCssText(wri);
		}
		while (it.hasNext()) {
			expr = it.next();
			if (expr.isInverseOperation()) {
				wri.write('/');
			} else {
				wri.write('*');
			}
			if (expr.getPartType() == AlgebraicPart.SUM) {
				wri.write('(');
				expr.writeCssText(wri);
				wri.write(')');
			} else {
				expr.writeCssText(wri);
			}
		}
	}

	static AlgebraicExpression createProductExpression() {
		return new ProductExpression();
	}

}
