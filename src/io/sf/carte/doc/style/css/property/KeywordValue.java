/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * A CSS-wide keyword value, like <code>inherit</code>.
 * 
 */
abstract public class KeywordValue extends StyleValue implements ValueItem {

	/**
	 * Must access instance through static method.
	 *
	 */
	protected KeywordValue() {
		super();
	}

	@Override
	public CssType getCssValueType() {
		return CssType.KEYWORD;
	}

	@Override
	public LexicalUnit getNextLexicalUnit() {
		return null;
	}

	@Override
	public boolean hasWarnings() {
		return false;
	}

	@Override
	public void handleSyntaxWarnings(StyleDeclarationErrorHandler handler) {
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Attempt to modify inherit value");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + getCssText().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
        return this == obj || (obj instanceof KeywordValue && getCssText().equals(((KeywordValue) obj).getCssText()));
    }

	public KeywordValue asSubproperty() {
		return new SubpropertyKeywordValue();
	}

	@Override
	public KeywordValue clone() {
		return this;
	}

	private class SubpropertyKeywordValue extends KeywordValue {
		SubpropertyKeywordValue() {
			super();
		}

		@Override
		public void writeCssText(SimpleWriter wri) throws IOException {
			KeywordValue.this.writeCssText(wri);
		}

		@Override
		public String getCssText() {
			return KeywordValue.this.getCssText();
		}

		@Override
		public Type getPrimitiveType() {
			return KeywordValue.this.getPrimitiveType();
		}

		@Override
		public KeywordValue asSubproperty() {
			return this;
		}

		@Override
		public boolean isSubproperty() {
			return true;
		}

		@Override
		public StyleValue getCSSValue() {
			return this;
		}

		@Override
		public KeywordValue clone() {
			return this;
		}
	}
}
