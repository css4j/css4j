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
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.util.SimpleWriter;

/**
 * An <code>inherit</code> value.
 * 
 * @author Carlos Amengual
 *
 */
public class InheritValue extends StyleValue implements ValueItem {
	private static InheritValue singleton = new InheritValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	protected InheritValue() {
		super(CSSValue.CSS_INHERIT);
	}

	public static InheritValue getValue() {
		return singleton;
	}

	@Override
	public String getCssText() {
		return "inherit";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("inherit");
	}

	@Override
	public StyleValue getCSSValue() {
		return singleton;
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
		return prime * super.hashCode() + "inherit".hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj || obj instanceof InheritValue) {
			return true;
		}
		return false;
	}

	public InheritValue asSubproperty() {
		return new SubpropertyInheritedValue();
	}

	@Override
	public InheritValue clone() {
		return this;
	}

	private static class SubpropertyInheritedValue extends InheritValue {
		SubpropertyInheritedValue() {
			super();
		}

		@Override
		public InheritValue asSubproperty() {
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
		public InheritValue clone() {
			return this;
		}
	}
}
