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

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.util.SimpleWriter;

/**
 * Unicode range wildcard CSSPrimitiveValue.
 * 
 */
public class UnicodeWildcardValue extends AbstractCSSPrimitiveValue {

	private String wildcard = null;

	public UnicodeWildcardValue() {
		super(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD);
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
	public void setStringValue(short stringType, String stringValue) throws DOMException {
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		if (stringType != CSSPrimitiveValue2.CSS_UNICODE_WILDCARD) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Only setting wildcards is supported.");
		}
		this.wildcard = stringValue;
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
			if (other.wildcard != null) {
				return false;
			}
		} else if (!wildcard.equals(other.wildcard)) {
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
			super.setLexicalUnit(lunit);
			wildcard = lunit.getStringValue();
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public UnicodeWildcardValue clone() {
		return new UnicodeWildcardValue(this);
	}

}
