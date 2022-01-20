/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.SimpleWriter;

/**
 * String value.
 * 
 * @author Carlos Amengual
 *
 */
public class StringValue extends AbstractTextValue {

	private static final long serialVersionUID = 1L;

	private String stringValue = null;

	private final byte flags;

	char quote;

	public StringValue() {
		this((byte) 0);
	}

	/**
	 * A string value with a flag specifying the quote behaviour.
	 * <p>
	 * 
	 * @param flags See
	 *              {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#setFactoryFlag(byte)
	 *              CSSStyleSheetFactory.setFactoryFlag(byte)}
	 */
	public StringValue(byte flags) {
		this(Type.STRING, flags);
	}

	/**
	 * A string value with a flag specifying the quote behaviour.
	 * <p>
	 * 
	 * @param primitiveType the primitive type.
	 * @param flags         See
	 *                      {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#setFactoryFlag(byte)
	 *                      CSSStyleSheetFactory.setFactoryFlag(byte)}
	 */
	StringValue(Type primitiveType, byte flags) {
		super(primitiveType);
		this.flags = flags;
		quote = '\'';
		setQuote();
	}

	protected StringValue(StringValue copied) {
		super(copied);
		this.stringValue = copied.stringValue;
		this.flags = copied.flags;
		this.quote = copied.quote;
	}

	private void setQuote() {
		if (isDoubleQuoteSet()) {
			quote = '"';
		} else if ((flags & AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE) == AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE) {
			quote = '\'';
		}
	}

	private boolean isDoubleQuoteSet() {
		return (flags & AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE) == AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE;
	}

	private boolean isNoQuotePreferenceSet() {
		return (flags & (byte) 3) == 0;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		boolean doubleQuotes = false;
		boolean singleQuotes = false;
		cssText = cssText.trim();
		String text = cssText;
		int tlm1 = cssText.length() - 1;
		if (tlm1 > 1) {
			char c = cssText.charAt(0);
			char d = cssText.charAt(tlm1);
			if (c == '\'') {
				if (d == '\'') {
					text = cssText.substring(1, tlm1);
					singleQuotes = true;
				}
			} else if (c == '"' && d == '"') {
				text = cssText.substring(1, tlm1);
				doubleQuotes = true;
			}
		}
		this.stringValue = ParseHelper.unescapeStringValue(text);
		if (isNoQuotePreferenceSet() || (isDoubleQuoteSet() && doubleQuotes) || (!singleQuotes && !doubleQuotes)) {
			if (doubleQuotes) {
				quote = '"';
			}
			text = ParseHelper.escapeControl(text);
			setPlainCssText(ParseHelper.quote(text, quote));
		} else {
			setUnescapedCssText(stringValue);
		}
	}

	@Override
	public String getStringValue() {
		return stringValue;
	}

	@Override
	public void setStringValue(Type stringType, String stringValue) throws DOMException {
		checkModifiableProperty();
		if (getPrimitiveType() != stringType) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"This value is a different type. To have a new type, set it at the style-declaration level.");
		}
		if (stringValue == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null value.");
		}
		setStringValue(stringValue);
		setUnescapedCssText(stringValue);
	}

	protected void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case string:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			super.setLexicalUnit(lunit);
			String strval = lunit.getStringValue();
			setStringValue(strval);
			if (isNoQuotePreferenceSet() && lunit instanceof LexicalUnit) {
				String text = lunit.getCssText();
				if (text.length() != 0 && usesDoubleQuote(text)) {
					quote = '"';
				}
				setPlainCssText(text);
			} else {
				setUnescapedCssText(strval);
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	boolean usesDoubleQuote(String text) {
		return text.charAt(0) == '"';
	}

	protected void setUnescapedCssText(String css) {
		setPlainCssText(escapeControlBackslashAndQuote(css, quote));
	}

	private static String escapeControlBackslashAndQuote(String css, char quote) {
		boolean useDQ = true;
		boolean hasDoubleQuotes = css.indexOf('"') != -1;
		boolean hasSingleQuotes = css.indexOf('\'') != -1;
		if (hasSingleQuotes) {
			quote = '"';
			if (hasDoubleQuotes) {
				// Escape quotes
				int len = css.length();
				// count appearances of each quote
				// to determine which one to escape
				int sqc = 0, dqc = 0;
				for (int i = 0; i < len; i++) {
					char c = css.charAt(i);
					if (c == '\'') {
						sqc++;
					} else if (c == '"') {
						dqc++;
					}
				}
				if (sqc > dqc) {
					quote = '"';
				} else {
					quote = '\'';
					useDQ = false;
				}
			}
		} else if (hasDoubleQuotes) {
			useDQ = false;
			quote = '\'';
		}
		css = ParseHelper.escapeString(css, quote, true);
		if (hasSingleQuotes || hasDoubleQuotes) {
			if (useDQ) {
				css = '"' + css + '"';
			} else {
				css = '\'' + css + '\'';
			}
		} else {
			css = quote + css + quote;
		}
		return css;
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return escapeControlBackslashAndQuote(stringValue, quote);
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
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof StringValue)) {
			return false;
		}
		StringValue other = (StringValue) obj;
		if (stringValue == null) {
			return other.stringValue == null;
		} else {
			return stringValue.equals(other.stringValue);
		}
	}

	@Override
	public StringValue clone() {
		return new StringValue(this);
	}
}
