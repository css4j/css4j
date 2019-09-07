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
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.SimpleWriter;

/**
 * Identifier CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class IdentifierValue extends PrimitiveValue {

	private String stringValue = null;

	public IdentifierValue() {
		super();
		setCSSUnitType(CSS_IDENT);
	}

	/**
	 * Fast path constructor.
	 * <p>
	 * The provided identifier cannot have characters that should be escaped.
	 * 
	 * @param plainIdentifier
	 *            the identifier.
	 */
	public IdentifierValue(String plainIdentifier) {
		super();
		setCSSUnitType(CSS_IDENT);
		this.stringValue = plainIdentifier;
		setPlainCssText(plainIdentifier);
	}

	protected IdentifierValue(IdentifierValue copied) {
		super(copied);
		this.stringValue = copied.stringValue;
		setPlainCssText(copied.getCssText());
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
			DOMException ex = new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad identifier: " + cssText);
			ex.initCause(e);
			throw ex;
		}
		if (lu.getLexicalUnitType() != LexicalUnit.SAC_IDENT || lu.getNextLexicalUnit() != null) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not an identifier: " + cssText);
		}
		LexicalSetter setter = newLexicalSetter();
		setter.setLexicalUnit(lu);
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return ParseHelper.safeEscape(stringValue, false);
	}

	@Override
	public String getStringValue() {
		return stringValue;
	}

	@Override
	public void setStringValue(short stringType, String stringValue) throws DOMException {
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		if (stringType != CSSPrimitiveValue.CSS_IDENT) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"This value is an identifier. To have a new type, set it at the style-declaration level.");
		}
		setCSSUnitType(stringType);
		setStringValue(stringValue);
		setPlainCssText(escape(this.stringValue));
	}

	private void setStringValue(String value) {
		this.stringValue = value.intern();
	}

	private String escape(String css) throws DOMException {
		return ParseHelper.escape(css);
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			String strval = lunit.getStringValue();
			setStringValue(strval);
			if (lunit instanceof LexicalUnit2) {
				setPlainCssText(((LexicalUnit2) lunit).getCssText());
			} else {
				setPlainCssText(escape(stringValue));
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getCssText());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		String sv = getStringValue();
		result = prime * result + ((sv == null) ? 0 : sv.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IdentifierValue other = (IdentifierValue) obj;
		String sv = getStringValue();
		String osv = other.getStringValue();
		if (sv == null) {
			if (osv != null) {
				return false;
			}
		} else if (!sv.equals(osv)) {
			return false;
		}
		return true;
	}

	@Override
	public IdentifierValue clone() {
		return new IdentifierValue(this);
	}

}
