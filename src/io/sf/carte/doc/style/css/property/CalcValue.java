/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.util.SimpleWriter;

/**
 * Calc CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class CalcValue extends ExpressionValue {

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
	protected boolean isInvalidOperand(AbstractCSSPrimitiveValue primi, short lutype, short lastlutype) {
		if (super.isInvalidOperand(primi, lutype, lastlutype)) {
			return true;
		}
		if (primi instanceof NumberValue) {
			return (lastlutype != -1 && lastlutype != LexicalUnit.SAC_SUB_EXPRESSION && primi.isNegativeNumber());
		}
		return lutype != LexicalUnit.SAC_FUNCTION;
	}

	@Override
	public float getFloatValue(short unitType) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Must retrieve individual operands and compute result");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + "calc".hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
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
