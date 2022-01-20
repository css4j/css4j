/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Unicode range wildcard value.
 * 
 */
public class UnicodeWildcardValue extends TypedValue {

	private static final long serialVersionUID = 1L;

	private String wildcard = null;

	public UnicodeWildcardValue() {
		super(Type.UNICODE_WILDCARD);
	}

	protected UnicodeWildcardValue(UnicodeWildcardValue copied) {
		super(copied);
		this.wildcard = copied.wildcard;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This property is read-only.");
	}

	@Override
	public String getStringValue() {
		return wildcard;
	}

	@Override
	public void setStringValue(Type stringType, String stringValue) throws DOMException {
		checkModifiableProperty();
		if (stringType != Type.UNICODE_WILDCARD) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Only setting wildcards is supported.");
		}
		if (stringValue == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null value.");
		}
		stringValue = stringValue.trim();
		checkWildcard(stringValue);
		this.wildcard = stringValue;
	}

	private void checkWildcard(String wildcard) {
		int len = wildcard.length();
		if (len == 0 || len > 6) {
			invalidWildcardError(wildcard);
		}
		for (int i = 0; i < len; i++) {
			char c = wildcard.charAt(i);
			if (c != '?' && !isHexChar(c)) {
				invalidWildcardError(wildcard);
			}
		}
	}

	static boolean isHexChar(char codePoint) {
		return (codePoint >= 0x30 && codePoint <= 0x39) || (codePoint >= 0x41 && codePoint <= 0x46)
				|| (codePoint >= 0x61 && codePoint <= 0x66);
	}

	private void invalidWildcardError(String wildcard2) {
		throw new DOMException(DOMException.SYNTAX_ERR, "Not a valid wildcard: " + wildcard);
	}

	public void setWildcard(String wildcard) {
		this.wildcard = wildcard;
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("U+");
		wri.write(wildcard);
	}

	@Override
	public String getCssText() {
		return "U+" + wildcard;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((wildcard == null) ? 0 : wildcard.hashCode());
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
		UnicodeWildcardValue other = (UnicodeWildcardValue) obj;
		if (wildcard == null) {
			return other.wildcard == null;
		} else {
			return wildcard.equals(other.wildcard);
		}
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			wildcard = lunit.getStringValue();
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public UnicodeWildcardValue clone() {
		return new UnicodeWildcardValue(this);
	}

}
