/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * URI primitive value.
 * 
 * @author Carlos Amengual
 *
 */
public class URIValue extends StringValue {

	public URIValue(byte flags) {
		super(flags);
		setCSSUnitType(CSS_URI);
	}

	protected URIValue(URIValue copied) {
		super(copied);
	}

	@Override
	public short getPrimitiveType() {
		return CSSPrimitiveValue.CSS_URI;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		if (cssText.indexOf('(') == -1 && cssText.indexOf(')') == -1) {
			cssText = "url(" + cssText + ")";
		}
		LexicalSetter setter = newLexicalSetter();
		CSSParser parser = new CSSParser();
		LexicalUnit2 lunit;
		try {
			lunit = parser.parsePropertyValue(new InputSource(new StringReader(cssText)));
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, "Error parsing text: " + e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			lunit = null;
		}
		if (lunit == null || lunit.getLexicalUnitType() != LexicalUnit.SAC_URI) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a URI value.");
		}
		setter.setLexicalUnit(lunit);
	}

	@Override
	boolean usesDoubleQuote(String text) {
		int i = text.lastIndexOf(')');
		while (i > 0) {
			i--;
			char c = text.charAt(i);
			if (c != ' ') {
				return c == '"';
			}
		}
		return false;
	}

	/*
	 * Escape backslashes and single quotes
	 */
	@Override
	protected void setUnescapedCssText(String css) {
		css = ParseHelper.escapeBackslash(css).toString();
		int idx = css.indexOf('\'');
		if (idx != -1) {
			int len = css.length();
			// Escape quote
			StringBuilder buf = new StringBuilder(len + 8);
			buf.append(css.subSequence(0, idx));
			for (int i = idx; i < len; i++) {
				char c = css.charAt(i);
				if (c == quote) {
					buf.append('\\');
				}
				buf.append(c);
			}
			css = buf.toString();
		}
		setPlainCssText("url(" + quote + css + quote + ')');
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return "url(" + ParseHelper.quote(getStringValue(), quote) + ')';
	}

	/**
	 * Get the URL contained in this URI value, if it is absolute.
	 * 
	 * @return the URL contained in this URI value if it is absolute, null otherwise.
	 */
	public URL getURLValue() {
		URL url;
		String sv = getStringValue();
		try {
			url = new URL(sv);
		} catch (MalformedURLException e) {
			url = null;
		}
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getCssValueType();
		result = prime * result + getPrimitiveType();
		String sv = getStringValue();
		return prime * result + ((sv == null) ? 0 : sv.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof URIValue)) {
			return false;
		}
		URIValue other = (URIValue) obj;
		String sv = getStringValue();
		if (sv == null) {
			if (other.getStringValue() != null) {
				return false;
			}
		} else if (!sv.equals(other.getStringValue())) {
			return false;
		}
		return true;
	}

	public boolean isEquivalent(URIValue other) {
		if (getMinifiedCssText(null).equals(other.getMinifiedCssText(null))) {
			return true;
		}
		String sv = getStringValue();
		if (sv == null) {
			if (other.getStringValue() != null) {
				return false;
			}
		} else if (!sv.equals(other.getStringValue())) {
			return false;
		}
		return true;
	}

	@Override
	public URIValue clone() {
		return new URIValue(this);
	}

}
