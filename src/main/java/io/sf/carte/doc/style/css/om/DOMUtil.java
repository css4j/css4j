/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.DOMException;

import io.sf.jclf.text.TokenParser;

/**
 * DOM utility method.
 */
public class DOMUtil {

	/**
	 * Parses the pseudo-attributes in <code>data</code>.
	 * 
	 * @param data
	 *            the pseudo-attribute string.
	 * @param pseudoAttrs
	 *            the pseudo-attribute map.
	 * @throws DOMException
	 *             DOMException.INVALID_CHARACTER_ERR if a syntax error was found while
	 *             parsing the pseudo-attributes.
	 */
	public static void parsePseudoAttributes(String data, Map<String, String> pseudoAttrs)
			throws DOMException {
		pseudoAttrs.clear();
		// There are 6 defined style pseudo-attributes
		List<String> tokenlist = new ArrayList<>(6);
		TokenParser tp = new TokenParser(data, " ", "\"'");
		while (tp.hasNext()) {
			tokenlist.add(tp.next().trim());
		}

		String token = null, name = null;
		byte stage = 0;
		for (String element : tokenlist) {
			token = element;
			if (stage == 0) { // Expecting pseudo-attribute name
				int idx = token.indexOf('=');
				if (idx == -1) {
					if (invalidPseudoAttrName(token)) {
						pseudoAttrs.clear();
						parseError("Invalid pseudo-attribute name in PI: " + token);
						return;
					}
					name = token;
					stage = 1;
				} else {
					name = token.substring(0, idx);
					if (invalidPseudoAttrName(name)) {
						pseudoAttrs.clear();
						parseError("Invalid pseudo-attribute name in PI: " + name);
						return;
					}
					idx++;
					if (idx != token.length()) {
						String value = token.substring(idx);
						setPseudoAttribute(pseudoAttrs, name, value);
						name = null;
						stage = 0;
					} else {
						// name=
						stage = 2;
					}
				}
			} else if (stage == 1) { // Waiting for either '=' or '=value'
				int idx = token.indexOf('=');
				if (idx == 0) {
					// =[value]
					idx++;
					if (idx != token.length()) {
						String value = token.substring(idx);
						setPseudoAttribute(pseudoAttrs, name, value);
						name = null;
						stage = 0;
					} else {
						stage = 2;
					}
				} else {
					parseError("Expected '=' in pseudo-attribute, instead found: " + token);
					return;
				}
			} else if (stage == 2) { // Waiting for value, having found '='
				pseudoAttrs.put(name, token);
				stage = 0;
			}
		}
	}

	private static void parseError(String message) throws DOMException {
		throw new DOMException(DOMException.INVALID_CHARACTER_ERR, message);
	}

	private static boolean invalidPseudoAttrName(String name) {
		if (!name.isEmpty()) {
			char[] na = name.toCharArray();
			for (char c : na) {
				if (!Character.isLetterOrDigit(c)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private static void setPseudoAttribute(Map<String, String> pseudoAttrs, String name,
			String value) {
		value = parseValue(value);
		pseudoAttrs.put(name, value);
	}

	private static String parseValue(String value) {
		int fromIdx = value.indexOf("&");
		if (fromIdx == -1) {
			return value;
		}

		int len = value.length();
		int lenm3 = len - 3;
		StringBuilder buf = new StringBuilder(lenm3);
		buf.append(value.subSequence(0, fromIdx));
		fromIdx++;

		int idx;
		while (fromIdx <= lenm3) {
			idx = value.indexOf(";", fromIdx);
			if (idx != -1) {
				String ent = value.substring(fromIdx, idx).toLowerCase(Locale.ROOT);
				buf.append(replaceEntity(ent));
				idx++;
				fromIdx = value.indexOf("&", idx);
				if (fromIdx != -1) {
					if (idx < fromIdx) {
						buf.append(value.subSequence(idx, fromIdx));
					}
					fromIdx++;
					continue;
				} else {
					fromIdx = idx;
				}
			} else {
				parseError("Malformed entity in PI: " + value);
				buf.append('&');
			}
			break;
		}

		buf.append(value.subSequence(fromIdx, len));

		return buf.toString();
	}

	private static char replaceEntity(String ent) {
		char c;
		switch(ent) {
		case "amp":
			c = '&';
			break;
		case "lt":
			c = '<';
			break;
		case "gt":
			c = '>';
			break;
		case "quot":
			c = '"';
			break;
		case "apos":
			c = '\'';
			break;
		default:
			parseError("Only predefined entities are valid in PI, not &" + ent + ';');
			c = '?';
		}
		return c;
	}

}
