/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.io.IOException;
import java.io.StringReader;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMCharacterException;
import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.PseudoCondition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * Methods that are useful for CSS parsing.
 */
public class ParseHelper {

	public static final byte ERR_UNEXPECTED_CHAR = 4;
	public static final byte ERR_WRONG_VALUE = 5;
	public static final byte ERR_UNMATCHED_PARENTHESIS = 6;
	public static final byte ERR_INVALID_IDENTIFIER = 7;
	public static final byte ERR_UNEXPECTED_TOKEN = 9;
	public static final byte ERR_UNKNOWN_NAMESPACE = 15;
	public static final byte ERR_EXPR_SYNTAX = 32;
	public static final byte ERR_RULE_SYNTAX = 33;
	public static final byte ERR_UNEXPECTED_EOF = 10;
	public static final byte ERR_UNSUPPORTED = 127;

	public static final byte WARN_DUPLICATE_SELECTOR = -1;
	public static final byte WARN_IDENT_COMPAT = -2;
	public static final byte WARN_PROGID_HACK = -3;
	public static final byte WARN_PROPERTY_NAME = -4;

	/**
	 * Generic warning about a property value
	 */
	public static final byte WARN_VALUE = -5;

	/**
	 * Non-fatal unexpected EOF
	 */
	public static final byte WARN_UNEXPECTED_EOF = -6;

	public static String unescapeStringValue(String value) {
		return unescapeStringValue(value, true, true);
	}

	/**
	 * Changes ISO 10646 escaped character values to the actual characters, when
	 * appropriate.
	 * 
	 * @param value           the string value to unescape.
	 * @param unescapeControl if <code>false</code>, control characters are not
	 *                        unescaped.
	 * @param replaceNull     if <code>false</code>, if NULL is found at the end of
	 *                        a string, it will trigger an exception instead of
	 *                        being replaced by the replacement character.
	 * @return the unescaped string.
	 * @throws DOMNullCharacterException if a null-character browser hack was found
	 *                                   and <code>replaceNull</code> is
	 *                                   <code>false</code>.
	 */
	public static String unescapeStringValue(String value, boolean unescapeControl, boolean replaceNull) throws DOMNullCharacterException {
		int iCP = value.indexOf('\\');
		if (iCP == -1) {
			return value;
		}
		int len = value.length();
		StringBuilder buf = new StringBuilder(len);
		if (iCP > 0) {
			appendString(buf, value, 0, iCP);
		}
		iCP++;
		int i = iCP;
		while (i < len) {
			if (i - iCP > 5) { // got 6 hexadecimal digits
				appendCodePoint(buf, value, iCP, i, unescapeControl, true);
				iCP = value.indexOf('\\', i);
				if (iCP == -1) {
					appendString(buf, value, i, len);
					break;
				}
				appendString(buf, value, i, iCP);
				iCP++;
				i = iCP;
				if (i == len) {
					break;
				}
			}
			char cp = value.charAt(i);
			int digit = Character.digit(cp, 16);
			if (digit == -1) {
				// no longer an hexadecimal digit
				int istart;
				if (i != iCP) {
					if (appendCodePoint(buf, value, iCP, i, unescapeControl, replaceNull)) {
						if (cp == 32) {
							// Skip first whitespace
							i++;
						}
					}
					istart = i;
				} else {
					istart = i + 1;
				}
				iCP = value.indexOf('\\', istart);
				if (iCP == -1) {
					appendString(buf, value, i, len);
					break;
				}
				appendString(buf, value, i, iCP);
				iCP++;
				i = iCP;
				if (i == len) {
					break;
				}
			} else {
				i++;
			}
		}
		if (iCP != -1 && len != iCP) {
			int cp = Character.digit(value.charAt(iCP), 16);
			if (cp == -1) {
				appendString(buf, value, iCP, len);
			} else {
				appendCodePoint(buf, value, iCP, len, unescapeControl, replaceNull);
			}
		}
		return buf.toString();
	}

	private static boolean appendCodePoint(StringBuilder buf, String value, int idx, int endIdx,
			boolean unescapeControl, boolean replaceNull) throws DOMNullCharacterException {
		if (idx + 1 == endIdx) {
			char cp = value.charAt(idx);
			int digit = Character.digit(cp, 16);
			if (digit == -1) {
				buf.append(cp);
				return false;
			} else if (digit != 0 || replaceNull) {
				if (appendCodePoint(buf, digit, unescapeControl)) {
					return true;
				}
			} else {
				throw new DOMNullCharacterException(idx);
			}
		} else {
			int cp;
			try {
				cp = Integer.parseInt(value.substring(idx, endIdx), 16);
				if (appendCodePoint(buf, cp, unescapeControl)) {
					return true;
				}
			} catch (NumberFormatException e) {
				// This probably cannot happen
			}
		}
		// Append the escaped sequence
		appendString(buf, value, idx - 1, endIdx);
		return false;
	}

	private static boolean appendCodePoint(StringBuilder buf, int cp, boolean unescapeControl) {
		int type = Character.getType(cp);
		switch (type) {
		case Character.PRIVATE_USE:
		case Character.UNASSIGNED:
			return false;
		case Character.CONTROL:
			if (cp == 0) { // no NUL
				buf.append('\uFFFD');
				break;
			}
			if (!unescapeControl) {
				return false;
			}
		default:
			buf.appendCodePoint(cp);
		}
		return true;
	}

	private static void appendString(StringBuilder buf, CharSequence value, int idx, int endIdx) {
		int prelen = buf.length();
		CharSequence seq = value.subSequence(idx, endIdx);
		buf.append(seq);
		for (int i = 0; i < seq.length(); i++) {
			if (seq.charAt(i) == '\n') {
				buf.setCharAt(prelen + i, '\uFFFD');
			}
		}
	}

	/**
	 * Escape a string according to CSS syntax, preserving escaped hex characters
	 * and surrogates.
	 * 
	 * @param text the text to escape.
	 * @return the escaped string.
	 */
	public static String escape(String text) {
		return escape(text, true, false);
	}

	/**
	 * Escape a string according to CSS syntax.
	 * 
	 * @param text               the text to escape.
	 * @param preserveHexEscapes if <code>true</code>, escaped hex characters shall
	 *                           be preserved (instead of escaping its backslash).
	 * @param escapeSurrogates   if <code>true</code>, surrogate characters shall be
	 *                           escaped, otherwise preserved.
	 * @return the escaped string.
	 */
	public static String escape(String text, boolean preserveHexEscapes, boolean escapeSurrogates) {
		final int len = text.length();
		if (len == 0) {
			return text;
		}
		int i = 0;
		StringBuilder buf = null;
		char c = text.charAt(0);
		if (c == '-') {
			if (len == 1) {
				return text;
			}
			i = 1;
			c = text.charAt(1);
		}
		//
		boolean noesc = c < 0x30 || c > 0x39; // First char is not digit ?
		if (!noesc) {
			// First char is digit
			buf = new StringBuilder(len + 24);
			if (i == 1) {
				buf.append(text.charAt(0));
			}
			buf.append("\\3").append(c).append(' ');
			i++;
		}
		//
		boolean preservingHex = false;
		while (i < len) {
			int cp = text.codePointAt(i);
			if ((cp >= 0x30 && cp <= 0x39) || (cp >= 0x41 && cp <= 0x5a) || (cp >= 0x61 && cp <= 0x7a) || cp == 0x2d
					|| cp == 0x5f) {
				if (preservingHex && !ParseHelper.isHexCodePoint(cp)) {
					preservingHex = false;
				}
				if (!noesc) {
					buf.append((char) cp);
				}
			} else if (cp > 0x7a) {
				preservingHex = false;
				if (cp < 0x7f) {
					if (noesc) {
						noesc = false;
						buf = new StringBuilder(len + 1);
						buf.append(text.subSequence(0, i));
					}
					buf.append('\\').append((char) cp);
				// Escape (high) controls and non-breaking spaces, soft hyphens and replacement char
				} else if (cp <= 0x9f || cp == 0xa0 || cp == 0xad|| cp == 0xfffd) {
					if (noesc) {
						noesc = false;
						buf = new StringBuilder(len + 24);
						buf.append(text.subSequence(0, i));
					}
					buf.append('\\').append(Integer.toHexString(cp));
					if (needsSpace(text, i + 1, len)) {
						buf.append(' ');
					}
				} else {
					int newIdx = text.offsetByCodePoints(i, 1);
					if (newIdx == i + 1) {
						if (!noesc) {
							buf.append((char) cp);
						}
					} else {
						if (!escapeSurrogates) {
							if (!noesc) {
								buf.appendCodePoint(cp);
							}
						} else {
							if (noesc) {
								noesc = false;
								buf = new StringBuilder(len + 24);
								buf.append(text.subSequence(0, i));
							}
							buf.append('\\').append(Integer.toHexString(cp));
							if (cp < 0xfffff && needsSpace(text, newIdx, len)) {
								buf.append(' ');
							}
						}
					}
					i = newIdx;
					continue;
				}
			} else if (cp <= 0x1f || cp == 0x20) {
				// Low control characters and whitespace
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 24);
					buf.append(text.subSequence(0, i));
				}
				if (cp == 0x20) {
					if (!preservingHex) {
						buf.append('\\');
					}
					buf.append(' ');
				} else {
					buf.append('\\').append(Integer.toHexString(cp));
					if (needsSpace(text, i + 1, len)) {
						buf.append(' ');
					}
				}
				preservingHex = false;
			} else if (preserveHexEscapes && cp == 0x5c && i != len - 1 && isHexCodePoint(text.codePointAt(i + 1))) {
				preservingHex = true;
				if (!noesc) {
					buf.append('\\');
				}
			} else {
				preservingHex = false;
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 1);
					buf.append(text.subSequence(0, i));
				}
				buf.append('\\').append((char) cp);
			}
			i++;
		}
		if (noesc) {
			return text;
		}
		return buf.toString();
	}

	private static boolean needsSpace(String text, int i, int len) {
		char c;
		return i < len && ((c = text.charAt(i)) == '\u0020' || Character.isLetter(c));
	}

	/**
	 * Escape the given text, but when escaping the backslashes, only do those that
	 * do not escape a private or unassigned character.
	 * 
	 * @param text             the text to escape.
	 * @param escapeSurrogates <code>true</code> if surrogates have to be escaped.
	 * @return the escaped text.
	 */
	public static String safeEscape(String text, boolean escapeSurrogates) {
		final int len = text.length();
		if (len == 0) {
			return text;
		}
		int i = 0;
		StringBuilder buf = null;
		char c = text.charAt(0);
		if (c == '-') {
			if (len == 1) {
				return text;
			}
			i = 1;
			c = text.charAt(1);
		}
		boolean noesc = c < 0x30 || c > 0x39; // First char is not digit ?
		if (!noesc) {
			// First char is digit
			buf = new StringBuilder(len + 24);
			if (i == 1) {
				buf.append(text.charAt(0));
			}
			buf.append("\\3").append(c).append(' ');
			i++;
		}
		while (i < len) {
			int cp = text.codePointAt(i);
			if ((cp >= 0x30 && cp <= 0x39) || (cp >= 0x41 && cp <= 0x5a) || (cp >= 0x61 && cp <= 0x7a) || cp == 0x2d
					|| cp == 0x5f) {
				if (!noesc) {
					buf.append((char) cp);
				}
			} else if (cp > 0x79) {
				// Escape (high) controls and non-breaking spaces, soft hyphens and replacement char
				if ((cp >= 0x7f && cp <= 0x9f) || cp == 0xa0 || cp == 0xad|| cp == 0xfffd) {
					if (noesc) {
						noesc = false;
						buf = new StringBuilder(len + 24);
						buf.append(text.subSequence(0, i));
					}
					buf.append('\\').append(Integer.toHexString(cp)).append(' ');
				} else {
					int newIdx = text.offsetByCodePoints(i, 1);
					i++;
					if (newIdx == i) {
						if (!noesc) {
							buf.append((char) cp);
						}
					} else {
						if (!escapeSurrogates) {
							if (!noesc) {
								buf.appendCodePoint(cp);
							}
						} else {
							if (noesc) {
								noesc = false;
								buf = new StringBuilder(len + 24);
								buf.append(text.subSequence(0, i));
							}
							buf.append('\\').append(Integer.toHexString(cp));
							if (cp <= 0xfffff) {
								buf.append(' ');
							}
						}
						i = newIdx;
					}
					continue;
				}
			} else if (cp <= 0x1f || cp == 0x20) {
				// Low control characters and whitespace
				if (cp != 0x20) {
					if (noesc) {
						noesc = false;
						buf = new StringBuilder(len + 24);
						buf.append(text.subSequence(0, i));
					}
					buf.append('\\').append(Integer.toHexString(cp)).append(' ');
				} else if (!noesc) {
					buf.append(' ');
				}
			} else if (cp == 0x5c && i != len - 1 && isPrivateOrUnassignedEscape(text, i + 1)) {
				if (!noesc) {
					buf.append('\\');
				}
			} else {
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 24);
					buf.append(text.subSequence(0, i));
				}
				buf.append('\\').append((char) cp);
			}
			i++;
		}
		if (noesc) {
			return text;
		}
		return buf.toString();
	}

	private static boolean isPrivateOrUnassignedEscape(String text, int idx) {
		char c = text.charAt(idx);
		if (!isHexCodePoint(c)) {
			return false;
		}
		int endIdx = text.length();
		endIdx = Math.min(endIdx, idx + 6);
		for (int i = idx + 1; i < endIdx; i++) {
			c = text.charAt(i);
			if (!isHexCodePoint(c)) {
				if (c == ' ') {
					int cp = Integer.parseInt(text.substring(idx, i), 16);
					return isPrivateOrUnassignedCodePoint(cp);
				}
				return false;
			}
		}
		int cp = Integer.parseInt(text.substring(idx, endIdx), 16);
		return isPrivateOrUnassignedCodePoint(cp);
	}

	private static boolean isPrivateOrUnassignedCodePoint(int cp) {
		int type = Character.getType(cp);
		return type == Character.UNASSIGNED || type == Character.PRIVATE_USE;
	}

	public static String escapeString(String text, char quoteChar) {
		return escapeString(text, quoteChar, false);
	}

	public static String escapeString(String text, char quoteChar, boolean endOfString) {
		final int len = text.length();
		if (len == 0) {
			return text;
		}
		int i = 0;
		StringBuilder buf = null;
		boolean noesc = true;
		while (i < len) {
			int cp = text.codePointAt(i);
			/*
			 * Escape controls, FORMAT and OTHER_PUNCTUATION & OTHER_SYMBOL greater than
			 * U+2800 (includes replacement char).
			 */
			int type = Character.getType(cp);
			if (type == Character.CONTROL || type == Character.FORMAT
					|| (cp >= 0x2800 && (type == Character.OTHER_PUNCTUATION || type == Character.OTHER_SYMBOL))) {
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 16);
					buf.append(text.subSequence(0, i));
				}
				buf.append('\\').append(Integer.toHexString(cp));
				i = text.offsetByCodePoints(i, 1);
				if (!endOfString || i != len) {
					buf.append(' ');
				}
				continue;
			} else if (cp == 0x5c) {
				if (i != len - 1 && isPrivateOrUnassignedEscape(text, i + 1)) {
					if (!noesc) {
						buf.append('\\');
					}
				} else {
					if (noesc) {
						noesc = false;
						buf = new StringBuilder(len + 16);
						buf.append(text.subSequence(0, i));
					}
					buf.append("\\\\");
				}
			} else if (cp == quoteChar) {
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 16);
					buf.append(text.subSequence(0, i));
				}
				buf.append('\\').append(quoteChar);
			} else {
				int newIdx = text.offsetByCodePoints(i, 1);
				i++;
				if (newIdx == i) {
					if (!noesc) {
						buf.append((char) cp);
					}
				} else {
					if (!noesc) {
						buf.appendCodePoint(cp);
					}
					i = newIdx;
				}
				continue;
			}
			i++;
		}
		if (noesc) {
			return text;
		}
		return buf.toString();
	}

	/**
	 * Quote a string according to the supplied <code>quote</code> character.
	 * 
	 * @param text  the string to quote.
	 * @param quote the character to be used as quote.
	 * @return the quoted string.
	 */
	public static String quote(String text, char quote) {
		int len = text.length();
		boolean useDQ = true;
		boolean hasDoubleQuotes = text.indexOf('"') != -1;
		boolean hasSingleQuotes = text.indexOf('\'') != -1;
		if (hasSingleQuotes) {
			if (hasDoubleQuotes) {
				// Escape quotes
				// count appearances of each quote
				// to determine which one to escape
				int sqc = 0, dqc = 0;
				for (int i = 0; i < len; i++) {
					char c = text.charAt(i);
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
				// Escape quote
				StringBuilder buf = new StringBuilder(len + 8);
				for (int i = 0; i < len; i++) {
					char c = text.charAt(i);
					if (c == quote) {
						buf.append('\\');
					}
					buf.append(c);
				}
				text = buf.toString();
			}
		} else if (hasDoubleQuotes) {
			useDQ = false;
		}
		StringBuilder buf = new StringBuilder(len + 2);
		if (hasSingleQuotes || hasDoubleQuotes) {
			if (useDQ) {
				buf.append('"');
				appendAndEscape(buf, text, len);
				buf.append('"');
			} else {
				buf.append('\'');
				appendAndEscape(buf, text, len);
				buf.append('\'');
			}
		} else {
			buf.append(quote);
			appendAndEscape(buf, text, len);
			buf.append(quote);
		}
		return buf.toString();
	}

	private static void appendAndEscape(StringBuilder buf, String css, int len) {
		// Check whether we need to escape last char
		boolean escapeLast = false;
		if (len != 0 && css.charAt(len - 1) == '\\') {
			int count = 1;
			for (int i = len - 2; i >= 0; i--) {
				if (css.charAt(i) == '\\') {
					count++;
				} else {
					break;
				}
			}
			escapeLast = (count & 1) == 0;
		}
		buf.append(css);
		if (escapeLast) {
			buf.append('\\');
		}
	}

	/**
	 * Escapes characters that have a special meaning for CSS, excluding backslash (x5c).
	 * 
	 * @param strval
	 *            the sequence to escape.
	 * @return the escaped string.
	 */
	public static CharSequence escapeCssChars(CharSequence strval) {
		int len = strval.length();
		if (len == 0) {
			return strval;
		}
		return escapeCssChars(strval, 0, null);
	}

	/**
	 * Escapes characters that have a special meaning for CSS, excluding backslash (x5c).
	 * <p>
	 * If the first character is a number, also escapes it.
	 * 
	 * @param strval
	 *            the sequence to escape.
	 * @return the escaped string.
	 */
	public static CharSequence escapeCssCharsAndFirstChar(CharSequence strval) {
		int len = strval.length();
		if (len == 0) {
			return strval;
		}
		int i = 0;
		StringBuilder buf;
		char cp = strval.charAt(0);
		if (cp == '-') {
			if (len == 1) {
				return strval;
			}
			i = 1;
			cp = strval.charAt(1);
		}
		boolean noesc = cp < 0x30 || cp > 0x39;
		if (noesc) {
			buf = null;
		} else {
			buf = new StringBuilder(len + 24);
			if (i == 1) {
				buf.append(strval.charAt(0));
			}
			buf.append("\\3").append(cp).append(' ');
			i++;
		}
		return escapeCssChars(strval, i, buf);
	}

	/**
	 * Escapes characters that have a special meaning for CSS, excluding backslash (x5c).
	 * <p>
	 * If the first character is a number, also escapes it.
	 * 
	 * @param strval
	 *            the sequence to escape.
	 * @param startIndex
	 *            the index where to start escaping.
	 * @param buf
	 *            if not <code>null</code>, the result has to be appended to its contents
	 *            before returning the result.
	 * @return the escaped string.
	 */
	private static CharSequence escapeCssChars(CharSequence strval, int startIndex, StringBuilder buf) {
		final int len = strval.length();
		boolean noesc = buf == null;
		for (int i = startIndex; i < len; i++) {
			char cp = strval.charAt(i);
			if ((cp >= 0x21 && cp <= 0x29) || cp == 0x2a || cp == 0x2b || cp == 0x2c || cp == 0x2e || cp == 0x2f
					|| (cp >= 0x3a && cp <= 0x3f) || cp == 0x40 || cp == 0x5b || cp == 0x5d || cp == 0x5e || cp == 0x60
					|| (cp >= 0x7b && cp <= 0x7e)) {
				// x21 !, x22 ", x23 #, x24 $, x25 %, x26 &, x27 ', x28 (, x29 ), x2a *
				// x2b +, x2c comma, x2e ., x2f /, x3a :, x3b ;, x3c <, x3d =, x3e >, x3f ?
				// x40 @, x5b [, x5d ], x5e ^, x60 `, x7b {, x7c |, x7d }, x7e ~
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 20);
					buf.append(strval.subSequence(0, i));
				}
				buf.append('\\').append(cp);
			} else if (!noesc) {
				buf.append(cp);
			}
		}
		if (noesc) {
			return strval;
		}
		return buf;
	}

	/**
	 * Escapes all the backslash characters found in the given string.
	 * 
	 * @param strval
	 *            the string to be escaped.
	 * @return the escaped string.
	 */
	public static CharSequence escapeAllBackslash(CharSequence strval) {
		boolean noesc = true;
		int lenm1 = strval.length() - 1;
		StringBuilder buf = null;
		for (int i = 0; i <= lenm1; i++) {
			char cp = strval.charAt(i);
			if (cp == 92) {
				// Backslash
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(lenm1 + 6);
					buf.append(strval.subSequence(0, i));
				}
				buf.append('\\').append('\\');
			} else if (!noesc) {
				buf.append(cp);
			}
		}
		if (noesc) {
			return strval;
		}
		return buf;
	}

	/**
	 * Escapes the backslash characters found in the given string, except those that escape
	 * hex-encoded codepoints.
	 * <p>
	 * If you have to call it together with {@link #escapeCssCharsAndFirstChar(CharSequence)}, make sure to
	 * execute this method first.
	 * 
	 * @param strval
	 *            the character sequence to be escaped.
	 * @return the escaped string.
	 */
	public static CharSequence escapeBackslash(CharSequence strval) {
		boolean noesc = true;
		int lenm1 = strval.length() - 1;
		StringBuilder buf = null;
		for (int i = 0; i <= lenm1; i++) {
			char cp = strval.charAt(i);
			if (cp == 92) {
				// Backslash
				if (noesc) {
					noesc = false;
					buf = createBuffer(i, strval, lenm1 + 12);
				}
				buf.append('\\');
				if (i < lenm1) {
					// Backslash is not the last char
					cp = strval.charAt(i + 1);
					if (isHexCodePoint(cp)) {
						// Hex-encoded char
						continue;
					} else if (cp == 92) {
						i++;
					}
				}
				buf.append('\\');
			} else if (!noesc) {
				buf.append(cp);
			}
		}
		if (noesc) {
			return strval;
		}
		return buf;
	}

	private static StringBuilder createBuffer(int i, CharSequence seq, int size) {
		StringBuilder buf = new StringBuilder(size);
		buf.append(seq.subSequence(0, i));
		return buf;
	}

	/**
	 * Escapes control characters.
	 * 
	 * @param strval the string to escape.
	 * @return the escaped string.
	 */
	public static String escapeControl(CharSequence strval) {
		boolean noctrl = true;
		int len = strval.length();
		StringBuilder buf = null;
		for (int i = 0; i < len; i++) {
			char cp = strval.charAt(i);
			if (Character.isISOControl(cp)) {
				if (noctrl) {
					noctrl = false;
					buf = new StringBuilder(len + 20);
					buf.append(strval.subSequence(0, i));
				}
				buf.append('\\').append(Integer.toHexString(cp)).append(' ');
			} else if (!noctrl) {
				buf.append(cp);
			}
		}
		if (noctrl) {
			return strval.toString();
		}
		return buf.toString();
	}

	/**
	 * Retrieves the CSS unit associated to the given unit string.
	 * 
	 * @param unit the unit string (must be interned).
	 * @return the associated CSS unit, or <code>CSS_OTHER</code> if the unit is
	 *         not known.
	 */
	public static short unitFromString(String unit) {
		if (unit == "%") {
			return CSSUnit.CSS_PERCENTAGE;
		} else if (unit == "em") {
			return CSSUnit.CSS_EM;
		} else if (unit == "ex") {
			return CSSUnit.CSS_EX;
		} else if (unit == "cap") {
			return CSSUnit.CSS_CAP;
		} else if (unit == "ch") {
			return CSSUnit.CSS_CH;
		} else if (unit == "ic") {
			return CSSUnit.CSS_IC;
		} else if (unit == "rem") {
			return CSSUnit.CSS_REM;
		} else if (unit == "lh") {
			return CSSUnit.CSS_LH;
		} else if (unit == "rlh") {
			return CSSUnit.CSS_RLH;
		} else if (unit == "vw") {
			return CSSUnit.CSS_VW;
		} else if (unit == "vh") {
			return CSSUnit.CSS_VH;
		} else if (unit == "vi") {
			return CSSUnit.CSS_VI;
		} else if (unit == "vb") {
			return CSSUnit.CSS_VB;
		} else if (unit == "vmin") {
			return CSSUnit.CSS_VMIN;
		} else if (unit == "vmax") {
			return CSSUnit.CSS_VMAX;
		} else if (unit == "cm") {
			return CSSUnit.CSS_CM;
		} else if (unit == "mm") {
			return CSSUnit.CSS_MM;
		} else if (unit == "q") {
			return CSSUnit.CSS_QUARTER_MM;
		} else if (unit == "in") {
			return CSSUnit.CSS_IN;
		} else if (unit == "pt") {
			return CSSUnit.CSS_PT;
		} else if (unit == "pc") {
			return CSSUnit.CSS_PC;
		} else if (unit == "px") {
			return CSSUnit.CSS_PX;
		} else if (unit == "deg") {
			return CSSUnit.CSS_DEG;
		} else if (unit == "grad") {
			return CSSUnit.CSS_GRAD;
		} else if (unit == "rad") {
			return CSSUnit.CSS_RAD;
		} else if (unit == "turn") {
			return CSSUnit.CSS_TURN;
		} else if (unit == "s") {
			return CSSUnit.CSS_S;
		} else if (unit == "ms") {
			return CSSUnit.CSS_MS;
		} else if (unit == "hz") {
			return CSSUnit.CSS_HZ;
		} else if (unit == "khz") {
			return CSSUnit.CSS_KHZ;
		} else if (unit == "dpi") {
			return CSSUnit.CSS_DPI;
		} else if (unit == "dpcm") {
			return CSSUnit.CSS_DPCM;
		} else if (unit == "dppx") {
			return CSSUnit.CSS_DPPX;
		} else if (unit == "fr") {
			return CSSUnit.CSS_FR;
		}
		return CSSUnit.CSS_OTHER;
	}

	/**
	 * Parse an escaped CSS identifier.
	 * 
	 * @param ident the identifier to parse.
	 * @return the parsed identifier.
	 * @throws DOMException DOMException.DOMSTRING_SIZE_ERR if the identifier is the
	 *                      empty string.<br>
	 *                      DOMException.INVALID_CHARACTER_ERR if the identifier
	 *                      contains invalid characters.<br>
	 *                      DOMException.SYNTAX_ERR if the identifier does not
	 *                      follow the syntax (e.g. only contains a hyphen-minus, or
	 *                      starts with a hyphen-minus and a digit).
	 */
	public static String parseIdent(String ident) throws DOMException {
		ident = ident.trim();
		int len = ident.length();
		if (len == 0) {
			throw new DOMException(DOMException.DOMSTRING_SIZE_ERR, "Empty string is no identifier");
		}
		char c = ident.charAt(0);
		if (!isNameStartCharOrEsc(c) && c != 0x2d) {
			throw new DOMCharacterException("Identifier cannot start with U+" + Integer.toHexString(c), 0);
		}
		if (c == 0x2d && (len == 1 || isDigitCodepoint(ident.charAt(1)))) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad identifier");
		}
		char prev = ' ';
		for (int i = 1; i < len; i++) {
			c = ident.charAt(i);
			if (!isNameCharOrEsc(c) && (c != ' ' || !isHexCodePoint(prev))) {
				String msg;
				if (Character.isLetterOrDigit(c)) {
					msg = "Identifier cannot contain '" + c + "'";
				} else {
					msg = "Identifier cannot contain U+" + Integer.toHexString(c);
				}
				throw new DOMCharacterException(msg, i);
			}
			prev = c;
		}
		return unescapeStringValue(ident);
	}

	private static boolean isNameStartCharOrEsc(char c) {
		return (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a) || c >= 0x80 || c == 0x5c || c == 0x5f;
	}

	private static boolean isNameCharOrEsc(char c) {
		return (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a) || (c >= 0x30 && c <= 0x39) || c >= 0x80
				|| c == 0x5f || c == 0x2d || c == 0x5c;
	}

	private static boolean isDigitCodepoint(char cp) {
		return cp >= 0x30 && cp <= 0x39;
	}

	static boolean isHexCodePoint(int codePoint) {
		return (codePoint >= 0x30 && codePoint <= 0x39) || (codePoint >= 0x41 && codePoint <= 0x46)
				|| (codePoint >= 0x61 && codePoint <= 0x66);
	}

	/**
	 * Check if the code point is a valid XML name character.
	 * 
	 * @param cp the code point.
	 * @return true if is a valid XML character, excluding a-z, A-Z, 0-9.
	 */
	static boolean isValidXMLCharacter(int cp) {
		return cp == 0x2d // -
				|| cp == 0x2e // .
				|| cp == 0xB7 // Middle dot
				|| isValidXMLStartCharacter(cp);
	}

	/**
	 * Check if the code point is a valid XML name start character.
	 * 
	 * @param cp the code point.
	 * @return true if is a valid XML start character, excluding a-z, A-Z.
	 */
	static boolean isValidXMLStartCharacter(int cp) {
		return cp == 0x5f // _
				|| (cp >= 0xC0 && cp <= 0xD6) // #xC0-#xD6
				|| (cp >= 0xD8 && cp <= 0xF6) // #xD8-#xF6
				|| (cp >= 0xF8 && cp <= 0x2FF) // #xF8-#x2FF
				|| (cp >= 0x370 && cp <= 0x37D) // #x370-#x37D
				|| (cp >= 0x37F && cp <= 0x1FFF) // #x37F-#x1FFF
				|| (cp >= 0x200C && cp <= 0x200D) // #x200C-#x200D
				|| (cp >= 0x2070 && cp <= 0x218F) // #x2070-#x218F
				|| (cp >= 0x2C00 && cp <= 0x2FEF) // #x2C00-#x2FEF
				|| (cp >= 0x3001 && cp <= 0xDFFF) // #x3001-#xD7FF + surrogates #xD800-#xDFFF
				|| (cp >= 0xF900 && cp <= 0xFDCF) // #xF900-#xFDCF
				|| (cp >= 0xFDF0 && cp <= 0xFFFD) // #xFDF0-#xFFFD
				|| (cp >= 0x10000 && cp <= 0xEFFFF); // #x10000-#xEFFFF (only String parse() variants)
	}

	public static boolean equalSelectorList(SelectorList list1, SelectorList list2) {
		if (list1 == null) {
			return list2 == null;
		}
		if (list2 != null) {
			int len = list1.getLength();
			if (len == list2.getLength()) {
				for (int i = 0; i < len; i++) {
					if (!list1.item(i).equals(list2.item(i))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests if the given {@code CharSequence} starts with the specified lower case
	 * string, but ignoring the case of the first argument.
	 * 
	 * @param seq      the non-{@code null} sequence to check.
	 * @param lcString the non-{@code null} lower case prefix that should be
	 *                 matched.
	 * @return {@code true} if the character sequence represented by the second
	 *         argument is a prefix of the character sequence in the first argument;
	 */
	public static boolean startsWithIgnoreCase(CharSequence seq, String lcString) {
		int seqLen = seq.length();
		int len = lcString.length();

		if (len > seqLen) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			char c = seq.charAt(i);
			char lc = lcString.charAt(i);
			if (c != lc) {
				if (Character.isLowerCase(c) || Character.toLowerCase(c) != lc) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Tests if the two given character sequences are the same, ignoring the case
	 * considerations except for the fact that the second argument <em>must</em> be
	 * lower case.
	 * 
	 * @param seq      the non-{@code null} sequence to compare to the second
	 *                 argument.
	 * @param lcString the non-{@code null} lower case sequence to compare to the
	 *                 first argument.
	 * @return {@code true} if the two arguments represent an equivalent
	 *         {@code CharSequence} ignoring case.
	 */
	public static boolean equalsIgnoreCase(CharSequence seq, CharSequence lcString) {
		int len = seq.length();
		if (lcString.length() != len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			char c = seq.charAt(i);
			char lc = lcString.charAt(i);
			if (c != lc) {
				if (Character.isLowerCase(c) || Character.toLowerCase(c) != lc) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Create a single pseudo-element condition, like {@code ::first-letter}.
	 * 
	 * @param pseudoElement the pseudo-element.
	 * @return the pseudo-element condition.
	 */
	public static PseudoCondition createPseudoElementCondition(String pseudoElement) {
		if (pseudoElement == null) {
			throw new NullPointerException("Null pseudo-element");
		}
		PseudoConditionImpl cond = new PseudoConditionImpl(Condition.ConditionType.PSEUDO_ELEMENT);
		cond.name = pseudoElement;
		return cond;
	}

	/**
	 * For internal use by the library, may be removed in the future.
	 * <p>
	 * Determine if the name is a transform function.
	 * </p>
	 * 
	 * @param functionName the function name.
	 * @return true if the name is a transform function.
	 */
	public static boolean isTransformFunction(String functionName) {
		return functionName.equals("matrix") || functionName.equals("translate") || functionName.equals("translateX")
				|| functionName.equals("translateY") || functionName.equals("scale") || functionName.equals("scaleX")
				|| functionName.equals("scaleY") || functionName.equals("rotate") || functionName.equals("skew")
				|| functionName.equals("skewX") || functionName.equals("skewY");
	}

	/**
	 * For internal use by the library, may be removed in the future.
	 * 
	 * @param dataType the attr data type.
	 * @param cat the grammar data type category to check.
	 * @return true if the data type matches the syntax category.
	 */
	public static boolean matchAttrType(String dataType, Category cat) {
		if ("length".equals(dataType)) {
			return cat == Category.length || cat == Category.lengthPercentage;
		} else if ("percentage".equals(dataType)) {
			return cat == Category.percentage || cat == Category.lengthPercentage;
		} else if ("color".equals(dataType)) {
			return cat == Category.color;
		} else if ("integer".equals(dataType)) {
			return cat == Category.integer || cat == Category.number;
		} else if ("number".equals(dataType)) {
			return cat == Category.number;
		} else if ("angle".equals(dataType)) {
			return cat == Category.angle;
		} else if ("time".equals(dataType)) {
			return cat == Category.time;
		} else if ("frequency".equals(dataType)) {
			return cat == Category.frequency;
		} else if ("string".equals(dataType)) {
			return cat == Category.string;
		} else if ("url".equals(dataType)) {
			return cat == Category.url || cat == Category.image;
		} else if ("flex".equals(dataType)) {
			return cat == Category.flex;
		} else {
			CSSParser parser = new CSSParser();
			try {
				LexicalUnit lu = parser.parsePropertyValue(new StringReader("1" + dataType));
				return matchesDimension(lu.getCssUnit(), cat);
			} catch (CSSParseException | IOException e) {
			}
		}
		return false;
	}

	/**
	 * Determine whether the given unit matches the category.
	 * 
	 * @param unit the dimension unit.
	 * @param cat the grammar data type category to check.
	 * @return true if the unit matches the syntax category.
	 */
	static boolean matchesDimension(short unit, Category cat) {
		switch (cat) {
		case length:
			return CSSUnit.isLengthUnitType(unit);
		case lengthPercentage:
			return CSSUnit.isLengthUnitType(unit) || unit == CSSUnit.CSS_PERCENTAGE;
		case percentage:
			return unit == CSSUnit.CSS_PERCENTAGE;
		case angle:
			return CSSUnit.isAngleUnitType(unit);
		case time:
			return CSSUnit.isTimeUnitType(unit);
		case resolution:
			return CSSUnit.isResolutionUnitType(unit);
		case flex:
			return unit == CSSUnit.CSS_FR;
		case frequency:
			return unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ;
		case integer:
		case number:
			// This probably never returns true (only actual dimensions reach this)
			return unit == CSSUnit.CSS_NUMBER;
		default:
			break;
		}
		return false;
	}

}
