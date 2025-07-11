/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.SimpleWriter;

/**
 * Identifier value.
 *
 */
public class IdentifierValue extends AbstractTextValue {

	private static final long serialVersionUID = 1L;

	private String stringValue = null;

	public IdentifierValue() {
		super(Type.IDENT);
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
		super(Type.IDENT);
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
		checkModifiableProperty();
		if (cssText == null || cssText.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null or empty value.");
		}
		CSSParser parser = new CSSParser();
		LexicalUnit lu;
		try {
			lu = parser.parsePropertyValue(new StringReader(cssText));
		} catch (IOException e) {
			lu = null;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_CHARACTER_ERR,
					"Invalid identifier: " + cssText);
			ex.initCause(e);
			throw ex;
		}
		if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.IDENT
				|| lu.getNextLexicalUnit() != null) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"Not an identifier: " + cssText);
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
	public void setStringValue(Type stringType, String stringValue) throws DOMException {
		checkModifiableProperty();
		if (stringType != Type.IDENT) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"This value is an identifier. To have a new type, set it at the style-declaration level.");
		}
		if (stringValue == null || stringValue.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null or empty value.");
		}
		setStringValue(stringValue);
		setPlainCssText(escape(this.stringValue));
	}

	private void setStringValue(String value) {
		this.stringValue = value.intern();
	}

	private String escape(String css) throws DOMException {
		return ParseHelper.safeEscape(css);
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case customIdent:
			return Match.TRUE;
		case IDENT:
			return syntax.getName().equals(getStringValue()) ? Match.TRUE : Match.FALSE;
		case color:
			String lc = getStringValue().toLowerCase(Locale.ROOT);
			return ColorIdentifiers.getInstance().isColorIdentifier(lc) ? Match.TRUE : Match.FALSE;
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		String ident = getStringValue().toLowerCase(Locale.ROOT);
		String spec;
		if ("transparent".equals(ident)) {
			spec = "#0000";
		} else {
			spec = ColorIdentifiers.getInstance().getColor(ident);
		}
		if (spec != null) {
			ValueFactory factory = new ValueFactory();
			try {
				StyleValue val = factory.parseProperty(spec);
				if (val.getCssValueType() == CssType.TYPED && val.getPrimitiveType() == Type.COLOR) {
					return ((TypedValue) val).toRGBColor();
				}
			} catch (DOMException e) {
			}
		}
		throw new DOMInvalidAccessException("Not an RGB Color");
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
			if (lunit instanceof LexicalUnit) {
				setPlainCssText(lunit.getCssText());
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
			return osv == null;
		} else {
			return sv.equals(osv);
		}
	}

	@Override
	public IdentifierValue clone() {
		return new IdentifierValue(this);
	}

}
