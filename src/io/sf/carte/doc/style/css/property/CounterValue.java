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
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.Counter;

import io.sf.carte.util.SimpleWriter;

/**
 * CSS primitive value returning a Counter.
 * 
 * @author Carlos Amengual
 *
 */
class CounterValue extends AbstractCSSPrimitiveValue {

	private CSSCounter counter = null;

	public CounterValue() {
		super(CSSPrimitiveValue.CSS_COUNTER);
	}

	protected CounterValue(CounterValue copied) {
		super(copied);
		this.counter = new CSSCounter(copied.counter.identifier, copied.counter.listStyle, copied.counter.separator);
	}

	@Override
	public Counter getCounterValue() throws DOMException {
		return counter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((counter == null) ? 0 : counter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CounterValue other = (CounterValue) obj;
		if (counter == null) {
			if (other.counter != null) {
				return false;
			}
		} else if (!counter.equals(other.counter)) {
			return false;
		}
		return true;
	}

	@Override
	public CounterValue clone() {
		return new CounterValue(this);
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			super.setLexicalUnit(lunit);
			counter = new CSSCounter();
			LexicalUnit lu = lunit.getParameters();
			counter.identifier = lu.getStringValue();
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Bad counter syntax: " + lunit.toString());
					}
				}
				counter.listStyle = lu.getStringValue();
			}
			lu = lunit.getNextLexicalUnit();
			if (lu != null && lu.getLexicalUnitType() == LexicalUnit.SAC_STRING_VALUE) {
				counter.separator = lu.getStringValue();
				lu = lu.getNextLexicalUnit();
			}
			nextLexicalUnit = lu;
		}
	}

	@Override
	public String getCssText() {
		return counter != null ? counter.toString() : "";
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return counter != null ? counter.toMinifiedString() : "";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		if (counter != null) {
			counter.writeCssText(wri);
		}
	}

	/**
	 * Implementation of <code>Counter</code>.
	 * 
	 * @author Carlos Amengual
	 *
	 */
	class CSSCounter implements Counter {

		private String identifier = null;
		private String listStyle = "decimal";
		private String separator = "";

		CSSCounter() {
			super();
		}

		CSSCounter(String identifier, String listStyle, String separator) {
			super();
			this.identifier = identifier;
			this.listStyle = listStyle;
			this.separator = separator;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		@Override
		public String getListStyle() {
			return listStyle;
		}

		@Override
		public String getSeparator() {
			return separator;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			result = prime * result + ((listStyle == null) ? 0 : listStyle.hashCode());
			result = prime * result + ((separator == null) ? 0 : separator.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CSSCounter other = (CSSCounter) obj;
			if (identifier == null) {
				if (other.identifier != null) {
					return false;
				}
			} else if (!identifier.equals(other.identifier)) {
				return false;
			}
			if (listStyle == null) {
				if (other.listStyle != null) {
					return false;
				}
			} else if (!listStyle.equals(other.listStyle)) {
				return false;
			}
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
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("counter(").append(identifier);
			if (!listStyle.equalsIgnoreCase("decimal")) {
				buf.append(',').append(' ').append(listStyle);
			}
			buf.append(')');
			if (separator.length() != 0) {
				buf.append(' ');
				quoteSeparator(buf);
			}
			return buf.toString();
		}

		public String toMinifiedString() {
			StringBuilder buf = new StringBuilder();
			buf.append("counter(").append(identifier);
			if (!listStyle.equalsIgnoreCase("decimal")) {
				buf.append(',').append(listStyle);
			}
			buf.append(')');
			if (separator.length() != 0) {
				buf.append(' ');
				quoteSeparator(buf);
			}
			return buf.toString();
		}

		private void quoteSeparator(StringBuilder buf) {
			if (!separator.contains("'")) {
				buf.append('\'').append(separator).append('\'');
			} else {
				buf.append('"').append(separator).append('"');
			}
		}

		public void writeCssText(SimpleWriter wri) throws IOException {
			wri.write("counter(");
			wri.write(identifier);
			if (!listStyle.equalsIgnoreCase("decimal")) {
				wri.write(',');
				wri.write(' ');
				wri.write(listStyle);
			}
			wri.write(')');
			if (separator.length() != 0) {
				wri.write(' ');
				if (!separator.contains("'")) {
					wri.write('\'');
					wri.write(separator);
					wri.write('\'');
				} else {
					wri.write('"');
					wri.write(separator);
					wri.write('"');
				}
			}
		}
	}

}
