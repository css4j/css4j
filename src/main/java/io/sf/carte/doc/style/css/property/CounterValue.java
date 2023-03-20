/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS counter function.
 * 
 * @author Carlos Amengual
 *
 */
class CounterValue extends AbstractCounterValue {

	private static final long serialVersionUID = 1L;

	public CounterValue() {
		super(Type.COUNTER);
	}

	protected CounterValue(CounterValue copied) {
		super(copied);
	}

	@Override
	public CounterValue clone() {
		return new CounterValue(this);
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		Category cat = syntax.getCategory();
		if (cat == Category.universal || cat == Category.counter) {
			return Match.TRUE;
		}
		return Match.FALSE;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			if (lu.getLexicalUnitType() == LexicalType.IDENT) {
				setName(lu.getStringValue());
			} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
				throw new CSSLexicalProcessingException("var() inside counter() found.");
			}
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						badSyntax(lunit);
					}
					LexicalType lutype = lu.getLexicalUnitType();
					if (lutype == LexicalType.IDENT || (lutype == LexicalType.FUNCTION
							&& "symbols".equalsIgnoreCase(lu.getFunctionName()))) {
						ValueFactory vf = new ValueFactory();
						LexicalSetter item = vf.createCSSPrimitiveValueItem(lu, false, true);
						setCounterStyle(item.getCSSValue());
						if (item.getNextLexicalUnit() != null) {
							badSyntax(lunit);
						}
					} else if (lutype == LexicalType.VAR) {
						throw new CSSLexicalProcessingException("var() inside counter() found.");
					} else {
						badSyntax(lunit);
					}
				} else {
					badSyntax(lunit);
				}
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void badSyntax(LexicalUnit lunit) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad counter syntax: " + lunit.toString());
		}

	}

	@Override
	public String getCssText() {
		StringBuilder buf = new StringBuilder();
		buf.append("counter(").append(getName());
		PrimitiveValue style = getCounterStyle();
		String listStyle;
		if (style != null && !"decimal".equalsIgnoreCase(listStyle = style.getCssText())) {
			buf.append(',').append(' ').append(listStyle);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder();
		buf.append("counter(").append(getName());
		PrimitiveValue style = getCounterStyle();
		String listStyle;
		if (style != null && !"decimal".equalsIgnoreCase(listStyle = style.getCssText())) {
			buf.append(',').append(listStyle);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("counter(");
		wri.write(getName());
		PrimitiveValue listStyle = getCounterStyle();
		if (listStyle != null && !isCSSIdentifier(listStyle, "decimal")) {
			wri.write(", ");
			listStyle.writeCssText(wri);
		}
		wri.write(')');
	}

}
