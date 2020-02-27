/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSCountersValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS counters function.
 * 
 * @author Carlos Amengual
 *
 */
class CountersValue extends AbstractCounterValue implements CSSCountersValue {

	private String separator = "";

	public CountersValue() {
		super(CSSPrimitiveValue2.CSS_COUNTERS);
	}

	protected CountersValue(CountersValue copied) {
		super(copied);
		this.separator = copied.separator;
	}

	@Override
	public CountersValue clone() {
		return new CountersValue(this);
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	@Override
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			setName(lu.getStringValue());
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
					if (lu == null || lu.getLexicalUnitType() != LexicalUnit.SAC_STRING_VALUE) {
						badSyntax(lunit);
					}
					setSeparator(lu.getStringValue());
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
							lu = lu.getNextLexicalUnit();
							if (lu == null) {
								badSyntax(lunit);
							}
							short lutype = lu.getLexicalUnitType();
							if (lutype == LexicalUnit.SAC_IDENT || (lutype == LexicalUnit.SAC_FUNCTION
									&& "symbols".equalsIgnoreCase(lu.getFunctionName()))) {
								ValueFactory vf = new ValueFactory();
								LexicalSetter item = vf.createCSSPrimitiveValueItem(lu, false);
								setCounterStyle(item.getCSSValue());
								if (item.getNextLexicalUnit() != null) {
									badSyntax(lunit);
								}
							} else {
								badSyntax(lunit);
							}
						} else {
							badSyntax(lunit);
						}
					}
				} else {
					badSyntax(lunit);
				}
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void badSyntax(LexicalUnit lunit) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad counters syntax: " + lunit.toString());
		}

	}

	@Override
	public String getCssText() {
		StringBuilder buf = new StringBuilder();
		buf.append("counters(").append(getName());
		String separator = getSeparator();
		buf.append(", ");
		quoteSeparator(separator, buf);
		PrimitiveValue style = getCounterStyle();
		String listStyle;
		if (style != null && !"decimal".equalsIgnoreCase(listStyle = style.getCssText())) {
			buf.append(", ").append(listStyle);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder();
		buf.append("counters(").append(getName());
		String separator = getSeparator();
		buf.append(',');
		quoteSeparator(separator, buf);
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
		wri.write("counters(");
		wri.write(getName());
		String separator = getSeparator();
		wri.write(", ");
		if (!separator.contains("'")) {
			wri.write('\'');
			wri.write(separator);
			wri.write('\'');
		} else {
			wri.write('"');
			wri.write(separator);
			wri.write('"');
		}
		PrimitiveValue listStyle = getCounterStyle();
		if (listStyle != null && !isCSSIdentifier(listStyle, "decimal")) {
			wri.write(", ");
			listStyle.writeCssText(wri);
		}
		wri.write(')');
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		CountersValue other = (CountersValue) obj;
		if (separator == null) {
			if (other.separator != null) {
				return false;
			}
		} else if (!separator.equals(other.separator)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((separator == null) ? 0 : separator.hashCode());
		return result;
	}

}
