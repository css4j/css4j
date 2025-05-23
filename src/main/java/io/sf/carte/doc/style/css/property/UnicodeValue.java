/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSUnicodeRangeValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Unicode character value.
 * 
 */
public class UnicodeValue extends TypedValue implements CSSUnicodeRangeValue.CSSUnicodeValue {

	private static final long serialVersionUID = 1L;

	private int codePoint = 0;

	public UnicodeValue() {
		super(Type.UNICODE_CHARACTER);
	}

	protected UnicodeValue(UnicodeValue copied) {
		super(copied);
		this.codePoint = copied.codePoint;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This property is read-only.");
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("U+");
		wri.write(Integer.toHexString(codePoint));
	}

	@Override
	public String getCssText() {
		return "U+" + Integer.toHexString(codePoint);
	}

	@Override
	public String getStringValue() throws DOMException {
		return new String(toChars());
	}

	@Override
	public int getCodePoint() {
		return codePoint;
	}

	@Override
	public void setCodePoint(int codePoint) {
		this.codePoint = codePoint;
	}

	public char[] toChars() {
		return Character.toChars(codePoint);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return super.hashCode() * prime + codePoint;
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
		UnicodeValue other = (UnicodeValue) obj;
		return codePoint == other.codePoint;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			codePoint = lunit.getIntegerValue();
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public UnicodeValue clone() {
		return new UnicodeValue(this);
	}

}
