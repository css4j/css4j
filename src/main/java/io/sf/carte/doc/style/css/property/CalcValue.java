/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.util.SimpleWriter;

/**
 * Calc value.
 * 
 * @author Carlos Amengual
 *
 */
public class CalcValue extends ExpressionValue {

	private static final long serialVersionUID = 1L;

	public CalcValue() {
		super();
	}

	protected CalcValue(CalcValue copied) {
		super(copied);
	}

	@Override
	public String getStringValue() throws DOMException {
		return "calc";
	}

	@Override
	ExpressionFactory createExpressionFactory(LexicalUnit nextLexicalUnit) {
		return new ExpressionFactory(nextLexicalUnit) {

			@Override
			protected boolean isCalcValue() {
				return true;
			}

			@Override
			protected boolean isInvalidOperand(CSSPrimitiveValue primi, LexicalType lutype,
					LexicalType lastlutype) {
				if (super.isInvalidOperand(primi, lutype, lastlutype)) {
					return true;
				}
				if (primi.getPrimitiveType() == Type.NUMERIC) {
					return lastlutype == LexicalType.SUB_EXPRESSION;
				}
				return lutype != LexicalType.CALC && lutype != LexicalType.MATH_FUNCTION
						&& lutype != LexicalType.VAR && lutype != LexicalType.ATTR
						&& lutype != LexicalType.ENV && lutype != LexicalType.FUNCTION;
			}

		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + "calc".hashCode();
		return result;
	}

	@Override
	public String getCssText() {
		String s = getExpression().getCssText();
		StringBuilder buf = new StringBuilder(s.length() + 7);
		buf.append("calc(").append(s).append(')');
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText(String pname) {
		String s = getExpression().getMinifiedCssText();
		StringBuilder buf = new StringBuilder(s.length() + 6);
		buf.append("calc(").append(s).append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("calc(");
		wri.write(getExpression().getCssText());
		wri.write(')');
	}

	@Override
	public CalcValue clone() {
		return new CalcValue(this);
	}

}
