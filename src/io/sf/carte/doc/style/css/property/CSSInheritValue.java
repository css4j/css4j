/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.util.SimpleWriter;

/**
 * An inherited value.
 * 
 * @author Carlos Amengual
 *
 */
public class CSSInheritValue extends AbstractCSSValue implements ValueItem {
	private static CSSInheritValue singleton = new CSSInheritValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	protected CSSInheritValue() {
		super(CSSValue.CSS_INHERIT);
	}

	public static CSSInheritValue getValue() {
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
	public AbstractCSSValue getCSSValue() {
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
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Attempt to modify inherited value");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode() + "inherit".hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj || obj instanceof CSSInheritValue) {
			return true;
		}
		return false;
	}

	public CSSInheritValue asSubproperty() {
		return new SubpropertyInheritedValue();
	}

	@Override
	public CSSInheritValue clone() {
		return this;
	}

	private static class SubpropertyInheritedValue extends CSSInheritValue {
		SubpropertyInheritedValue() {
			super();
		}

		@Override
		public CSSInheritValue asSubproperty() {
			return this;
		}

		@Override
		public boolean isSubproperty() {
			return true;
		}

		@Override
		public AbstractCSSValue getCSSValue() {
			return this;
		}

		@Override
		public CSSInheritValue clone() {
			return this;
		}
	}
}
