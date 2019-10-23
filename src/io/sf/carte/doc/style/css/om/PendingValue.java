/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.util.SimpleWriter;

/**
 * A value that is pending a {@code var} substitution in a shorthand.
 */
class PendingValue extends PrimitiveValue {

	private final String shorthandName;
	private final LexicalUnit lexicalValue;

	PendingValue(String shorthandName, LexicalUnit lexicalValue) {
		super(Type.INTERNAL);
		this.shorthandName = shorthandName;
		this.lexicalValue = lexicalValue;
	}

	PendingValue(PendingValue copied) {
		super(copied);
		shorthandName = copied.shorthandName;
		lexicalValue = copied.lexicalValue;
	}

	String getShorthandName() {
		return shorthandName;
	}

	LexicalUnit getLexicalUnit() {
		return lexicalValue;
	}

	@Override
	public CssType getCssValueType() {
		return CssType.PROXY;
	}

	@Override
	public boolean isSubproperty() {
		return true;
	}

	@Override
	public String getCssText() {
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lexicalValue == null) ? 0 : lexicalValue.toString().hashCode());
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
		PendingValue other = (PendingValue) obj;
		if (lexicalValue == null) {
			if (other.lexicalValue != null) {
				return false;
			}
		} else if (!lexicalValue.toString().equals(other.lexicalValue.toString())) {
			return false;
		}
		return true;
	}

	@Override
	public PendingValue clone() {
		return new PendingValue(this);
	}

}
