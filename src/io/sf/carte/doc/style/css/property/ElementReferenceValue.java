/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.util.SimpleWriter;

/**
 * Element reference (<code>element()</code>) CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
class ElementReferenceValue extends PrimitiveValue {

	private String refname = null;

	ElementReferenceValue() {
		super(CSSPrimitiveValue2.CSS_ELEMENT_REFERENCE);
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
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		InputSource source = new InputSource(new StringReader(cssText));
		CSSParser parser = new CSSParser();
		LexicalUnit lu;
		try {
			lu = parser.parsePropertyValue(source);
		} catch (IOException e) {
			lu = null;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad element reference: " + cssText);
			ex.initCause(e);
			throw ex;
		}
		if (lu.getLexicalUnitType() != LexicalUnit2.SAC_ELEMENT_REFERENCE || lu.getNextLexicalUnit() != null) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not an element reference: " + cssText);
		}
		LexicalSetter setter = newLexicalSetter();
		setter.setLexicalUnit(lu);
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
			if (other.refname != null) {
				return false;
			}
		} else if (!refname.equals(other.refname)) {
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
			refname = lunit.getStringValue();
			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getStringValue() {
		return refname;
	}

	@Override
	public ElementReferenceValue clone() {
		return new ElementReferenceValue(this);
	}

}
