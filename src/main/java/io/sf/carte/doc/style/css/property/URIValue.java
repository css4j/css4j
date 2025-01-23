/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * URI primitive value.
 * 
 * @author Carlos Amengual
 *
 */
public class URIValue extends StringValue {

	private static final long serialVersionUID = 1L;

	/**
	 * A URI value with a flag specifying the quote behaviour.
	 * <p>
	 * 
	 * @param flags See
	 *              {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#setFactoryFlag(short)
	 *              CSSStyleSheetFactory.setFactoryFlag(short)}
	 */
	public URIValue(short flags) {
		super(Type.URI, flags);
	}

	protected URIValue(URIValue copied) {
		super(copied);
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new URILexicalSetter();
	}

	class URILexicalSetter extends MyLexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			if (lunit.getParameters() == null) {
				super.setLexicalUnit(lunit);
			} else {
				throw new CSSLexicalProcessingException("var() found.");
			}
		}

	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		if (cssText.indexOf('(') == -1 && cssText.indexOf(')') == -1) {
			cssText = "url(" + cssText + ")";
		}
		LexicalSetter setter = newLexicalSetter();
		CSSParser parser = new CSSParser();
		LexicalUnit lunit;
		try {
			lunit = parser.parsePropertyValue(new StringReader(cssText));
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, "Error parsing text: " + e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			lunit = null;
		}
		if (lunit == null || lunit.getLexicalUnitType() != LexicalUnit.LexicalType.URI) {
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
		String sv = getStringValue();
		String text;
		if (sv != null) {
			String quoted = ParseHelper.quote(sv, quote);
			StringBuilder buf = new StringBuilder(quoted.length() + 5);
			buf.append("url(").append(quoted).append(')');
			text = buf.toString();
		} else {
			text = "url()";
		}
		return text;
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
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case url:
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
		int result = getCssValueType().hashCode();
		result = prime * result + getPrimitiveType().hashCode();
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
			return other.getStringValue() == null;
		} else {
			return sv.equals(other.getStringValue());
		}
	}

	public boolean isEquivalent(URIValue other) {
		if (getMinifiedCssText(null).equals(other.getMinifiedCssText(null))) {
			return true;
		}
		String sv = getStringValue();
		if (sv == null) {
			return other.getStringValue() == null;
		} else {
			return sv.equals(other.getStringValue());
		}
	}

	@Override
	public URIValue clone() {
		return new URIValue(this);
	}

}
