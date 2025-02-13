/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSVarValue;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.util.SimpleWriter;

/**
 * <code>var()</code> value.
 * 
 * @author Carlos Amengual
 * @deprecated see {@link LexicalValue}.
 */
@Deprecated(forRemoval = true)
public class VarValue extends ProxyValue implements CSSVarValue {

	private static final long serialVersionUID = 1L;

	private String name = null;

	private LexicalUnit fallback = null;

	VarValue() {
		super(Type.VAR);
		this.fallback = null;
	}

	protected VarValue(VarValue copied) {
		super(copied);
		this.name = copied.name;
		this.fallback = copied.fallback;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public LexicalUnit getFallback() {
		return fallback;
	}

	/**
	 * Sets the fallback for this <code>var()</code> value.
	 * 
	 * @param fallback the fallback.
	 */
	public void setFallback(LexicalUnit fallback) {
		this.fallback = fallback;
	}

	@Override
	public String getCssText() {
		String ftext;
		int sz = name.length();
		if (fallback == null) {
			sz += 2;
			ftext = null;
		} else {
			ftext = fallback.toString();
			sz += 4 + ftext.length();
		}
		StringBuilder buf = new StringBuilder(sz);
		buf.append("var(");
		buf.append(name);
		if (ftext != null) {
			buf.append(", ");
			buf.append(ftext);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("var(");
		wri.write(name);
		if (fallback != null) {
			wri.write(", ");
			writeFallback(wri);
		}
		wri.write(')');
	}

	private void writeFallback(SimpleWriter wri) throws IOException {
		ValueFactory vf = new ValueFactory();
		StyleValue cssval;
		try {
			cssval = vf.createCSSValue(fallback);
			cssval.writeCssText(wri);
		} catch (DOMException e) {
			wri.write(fallback.toString());
		}
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		int sz = name.length() + 5;
		if (fallback != null) {
			sz += 80;
		}
		StringBuilder buf = new StringBuilder(sz);
		buf.append("var(");
		buf.append(name);
		if (fallback != null) {
			buf.append(',');
			appendMinifiedFallback(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	private void appendMinifiedFallback(StringBuilder buf) {
		ValueFactory vf = new ValueFactory();
		String text;
		try {
			StyleValue cssval = vf.createCSSValue(fallback);
			text = cssval.getMinifiedCssText(name);
		} catch (DOMException e) {
			text = fallback.toString();
		}
		buf.append(text);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();

		CSSParser parser = new CSSParser();
		LexicalUnit lunit;
		try {
			lunit = parser.parsePropertyValue(new StringReader(cssText));
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// Cannot happen
			return;
		}
		if (lunit == null || lunit.getLexicalUnitType() != LexicalUnit.LexicalType.VAR) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a var() function.");
		}

		LexicalSetter setter = newLexicalSetter();
		setter.setLexicalUnit(lunit);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((fallback == null) ? 0 : fallback.hashCode());
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
		VarValue other = (VarValue) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (fallback == null) {
			return other.fallback == null;
		} else {
			return fallback.equals(other.fallback);
		}
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			if (lu == null || lu.getLexicalUnitType() != LexicalUnit.LexicalType.IDENT) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Variable name must be an identifier");
			}
			String name = lu.getStringValue();
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_COMMA) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Fallback must be separated by comma");
				}
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					fallback = lu.clone();
				} else {
					fallback = null;
				}
			}
			VarValue.this.name = name;
			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public VarValue clone() {
		return new VarValue(this);
	}

}
