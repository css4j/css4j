/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.util.SimpleWriter;

/**
 * Element reference (<code>element()</code>) value.
 *
 */
class ElementReferenceValue extends TypedValue {

	private static final long serialVersionUID = 1L;

	private String refname = null;

	ElementReferenceValue() {
		super(Type.ELEMENT_REFERENCE);
	}

	protected ElementReferenceValue(ElementReferenceValue copied) {
		super(copied);
		this.refname = copied.refname;
	}

	@Override
	public String getCssText() {
		return "element(#" + refname + ")";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("element(#");
		wri.write(refname);
		wri.write(')');
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		CSSParser parser = new CSSParser();
		LexicalUnit lu;
		try {
			lu = parser.parsePropertyValue(new StringReader(cssText));
		} catch (IOException e) {
			lu = null;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_CHARACTER_ERR,
					"Invalid element reference: " + cssText);
			ex.initCause(e);
			throw ex;
		}
		if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.ELEMENT_REFERENCE
				|| lu.getNextLexicalUnit() != null) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"Not an element reference: " + cssText);
		}
		LexicalSetter setter = newLexicalSetter();
		setter.setLexicalUnit(lu);
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case image:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((refname == null) ? 0 : refname.hashCode());
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
		ElementReferenceValue other = (ElementReferenceValue) obj;
		if (refname == null) {
			return other.refname == null;
		} else {
			return refname.equals(other.refname);
		}
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			refname = lunit.getStringValue();
			if (refname == null) {
				LexicalUnit param = lunit.getParameters();
				if (param != null) {
					checkProxyValue(param);
				}
				throw new DOMSyntaxException(
						"Invalid element reference: " + lunit.toString());
			}
			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getStringValue() {
		return refname;
	}

	@Override
	public void setStringValue(Type stringType, String stringValue) throws DOMException {
		if (stringType != getPrimitiveType()) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Type not supported.");
		}
		if (stringValue == null || (stringValue = stringValue.trim()).length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty or null value.");
		}
		refname = stringValue;
	}

	@Override
	public ElementReferenceValue clone() {
		return new ElementReferenceValue(this);
	}

}
