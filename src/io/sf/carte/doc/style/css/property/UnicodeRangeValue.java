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

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.CSSUnicodeRangeValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.util.SimpleWriter;

/**
 * Unicode range CSSPrimitiveValue.
 * 
 */
public class UnicodeRangeValue extends PrimitiveValue implements CSSUnicodeRangeValue {

	private PrimitiveValue value = null;
	private PrimitiveValue endValue = null;

	UnicodeRangeValue() {
		super(CSSPrimitiveValue2.CSS_UNICODE_RANGE);
	}

	protected UnicodeRangeValue(UnicodeRangeValue copied) {
		super(copied);
		this.value = copied.value;
		this.endValue = copied.endValue;
	}

	@Override
	public String getCssText() {
		if (endValue == null) {
			return value.getCssText();
		}
		String s1 = value.getCssText();
		StringBuilder buf = new StringBuilder(s1.length() * 2 + 16);
		buf.append(s1).append('-');
		if (endValue.getPrimitiveType() == CSSPrimitiveValue2.CSS_UNICODE_CHARACTER) {
			buf.append(Integer.toHexString(((CSSUnicodeRangeValue.CSSUnicodeValue) endValue).getCodePoint()));
		} else {
			buf.append(endValue.getStringValue());
		}
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		value.writeCssText(wri);
		if (endValue != null) {
			wri.write('-');
			if (endValue.getPrimitiveType() == CSSPrimitiveValue2.CSS_UNICODE_CHARACTER) {
				wri.write(Integer.toHexString(((CSSUnicodeRangeValue.CSSUnicodeValue) endValue).getCodePoint()));
			} else {
				wri.write(endValue.getStringValue());
			}
		}
	}

	@Override
	public PrimitiveValue getValue() {
		return value;
	}

	@Override
	public PrimitiveValue getEndValue() {
		return endValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		UnicodeRangeValue other = (UnicodeRangeValue) obj;
		if (endValue == null) {
			if (other.endValue != null) {
				return false;
			}
		} else if (!endValue.equals(other.endValue)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getSubValues();
			if (lu == null) {
				throw new DOMException(DOMException.INVALID_STATE_ERR, "Invalid (empty) range value");
			}
			value = readValue(lu);
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				endValue = readValue(lu);
			} else {
				endValue = null;
			}
			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private PrimitiveValue readValue(LexicalUnit lu) {
			short type = lu.getLexicalUnitType();
			if (type == LexicalUnit.SAC_INTEGER) {
				UnicodeValue val = new UnicodeValue();
				val.setCodePoint(lu.getIntegerValue());
				return val;
			} else if (type == LexicalUnit2.SAC_UNICODE_WILDCARD) {
				UnicodeWildcardValue val = new UnicodeWildcardValue();
				val.setWildcard(lu.getStringValue());
				return val;
			}
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Wrong type for unicode range");
		}
	}

	@Override
	public UnicodeRangeValue clone() {
		return new UnicodeRangeValue(this);
	}

}
