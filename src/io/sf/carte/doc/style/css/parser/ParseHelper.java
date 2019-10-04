/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMCharacterException;
import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
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
				cp = Integer.parseInt(value.substring(idx, endIdx).toString(), 16);
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
			buf.append(Character.toChars(cp));
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
					buf.append('\\').append(Integer.toHexString(cp));
					int ip1 = i + 1;
					if (ip1 < len) {
						
					}
					if (needsSpace(text, i + 1, len)) {
						buf.append(' ');
					}
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
								buf.append(Character.toChars(cp));
							}
						} else {
							if (noesc) {
								noesc = false;
								buf = new StringBuilder(len + 24);
								buf.append(text.subSequence(0, i));
							}
							buf.append('\\').append(Integer.toHexString(cp));
							if (cp < 0xfffff && needsSpace(text, i + 1, len)) {
								buf.append(' ');
							}
						}
						i = newIdx;
					}
					continue;
				}
			} else if (cp <= 0x1f || cp == 0x20) {
				// Low control characters and whitespace
				if (cp != 0x20 || !preserveHexEscapes) {
					if (noesc) {
						noesc = false;
						buf = new StringBuilder(len + 24);
						buf.append(text.subSequence(0, i));
					}
					buf.append('\\').append(Integer.toHexString(cp));
					if (needsSpace(text, i + 1, len)) {
						buf.append(' ');
					}
				} else if (!noesc) {
					buf.append(' ');
				}
			} else if (preserveHexEscapes && cp == 0x5c && i != len - 1 && isHexCodePoint(text.codePointAt(i + 1))) {
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
								buf.append(Character.toChars(cp));
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
		final int len = text.length();
		if (len == 0) {
			return text;
		}
		int i = 0;
		StringBuilder buf = null;
		boolean noesc = true;
		while (i < len) {
			int cp = text.codePointAt(i);
				// Escape controls and replacement char
			if (Character.isISOControl(cp) || cp == 0xfffd) {
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(len + 16);
					buf.append(text.subSequence(0, i));
				}
				buf.append('\\').append(Integer.toHexString(cp)).append(' ');
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
						buf.append(Character.toChars(cp));
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
				if (i < lenm1) {
					// Backslash is not the last char
					cp = strval.charAt(i + 1);
					if (isHexCodePoint(cp)) {
						// Hex-encoded char
						continue;
					}
				}
				if (noesc) {
					noesc = false;
					buf = new StringBuilder(lenm1 + 12);
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
	 * Retrieves the SAC unit associated to the given unit string.
	 * 
	 * @param unit the unit string (must be interned).
	 * @return the associated SAC unit, or <code>SAC_DIMENSION</code> if the unit is
	 *         not known.
	 */
	public static short unitFromString(String unit) {
		if (unit == "%") {
			return LexicalUnit.SAC_PERCENTAGE;
		} else if (unit == "em") {
			return LexicalUnit.SAC_EM;
		} else if (unit == "ex") {
			return LexicalUnit.SAC_EX;
		} else if (unit == "cap") {
			return LexicalUnit.SAC_CAP;
		} else if (unit == "ch") {
			return LexicalUnit.SAC_CH;
		} else if (unit == "ic") {
			return LexicalUnit.SAC_IC;
		} else if (unit == "rem") {
			return LexicalUnit.SAC_REM;
		} else if (unit == "lh") {
			return LexicalUnit.SAC_LH;
		} else if (unit == "rlh") {
			return LexicalUnit.SAC_RLH;
		} else if (unit == "vw") {
			return LexicalUnit.SAC_VW;
		} else if (unit == "vh") {
			return LexicalUnit.SAC_VH;
		} else if (unit == "vi") {
			return LexicalUnit.SAC_VI;
		} else if (unit == "vb") {
			return LexicalUnit.SAC_VB;
		} else if (unit == "vmin") {
			return LexicalUnit.SAC_VMIN;
		} else if (unit == "vmax") {
			return LexicalUnit.SAC_VMAX;
		} else if (unit == "cm") {
			return LexicalUnit.SAC_CENTIMETER;
		} else if (unit == "mm") {
			return LexicalUnit.SAC_MILLIMETER;
		} else if (unit == "q") {
			return LexicalUnit.SAC_QUARTER_MILLIMETER;
		} else if (unit == "in") {
			return LexicalUnit.SAC_INCH;
		} else if (unit == "pt") {
			return LexicalUnit.SAC_POINT;
		} else if (unit == "pc") {
			return LexicalUnit.SAC_PICA;
		} else if (unit == "px") {
			return LexicalUnit.SAC_PIXEL;
		} else if (unit == "deg") {
			return LexicalUnit.SAC_DEGREE;
		} else if (unit == "grad") {
			return LexicalUnit.SAC_GRADIAN;
		} else if (unit == "rad") {
			return LexicalUnit.SAC_RADIAN;
		} else if (unit == "turn") {
			return LexicalUnit.SAC_TURN;
		} else if (unit == "s") {
			return LexicalUnit.SAC_SECOND;
		} else if (unit == "ms") {
			return LexicalUnit.SAC_MILLISECOND;
		} else if (unit == "hz") {
			return LexicalUnit.SAC_HERTZ;
		} else if (unit == "khz") {
			return LexicalUnit.SAC_KILOHERTZ;
		} else if (unit == "dpi") {
			return LexicalUnit.SAC_DOTS_PER_INCH;
		} else if (unit == "dpcm") {
			return LexicalUnit.SAC_DOTS_PER_CENTIMETER;
		} else if (unit == "dppx") {
			return LexicalUnit.SAC_DOTS_PER_PIXEL;
		} else if (unit == "fr") {
			return LexicalUnit.SAC_FR;
		}
		return LexicalUnit.SAC_DIMENSION;
	}

	public static boolean isFunctionUnitType(short unitType) {
		switch (unitType) {
		case LexicalUnit.SAC_FUNCTION:
		case LexicalUnit.SAC_RGBCOLOR:
		case LexicalUnit.SAC_URI:
		case LexicalUnit.SAC_RECT_FUNCTION:
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
		case LexicalUnit.SAC_ATTR:
			return true;
		}
		return false;
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
		for (int i = 1; i < len; i++) {
			c = ident.charAt(i);
			if (!isNameCharOrEsc(c)) {
				String msg;
				if (Character.isLetterOrDigit(c)) {
					msg = "Identifier cannot contain '" + c + "'";
				} else {
					msg = "Identifier cannot contain U+" + Integer.toHexString(c);
				}
				throw new DOMCharacterException(msg, i);
			}
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

	public static boolean startsWithIgnoreCase(CharSequence seq, String lcString) {
		int len = lcString.length();
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

	public static boolean equalsIgnoreCase(CharSequence seq, String lcString) {
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

}
