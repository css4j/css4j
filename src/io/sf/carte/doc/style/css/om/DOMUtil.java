/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.LinkedHashMap;
import java.util.LinkedList;

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
	public static void parsePseudoAttributes(String data, LinkedHashMap<String, String> pseudoAttrs)
			throws DOMException {
		pseudoAttrs.clear();
		LinkedList<String> tokenlist = new LinkedList<String>();
		TokenParser tp = new TokenParser(data, " ", "\"'");
		while (tp.hasNext()) {
			tokenlist.add(tp.next());
		}
		String token = null, name = null;
		byte stage = 0;
		for (int i = 0, sz = tokenlist.size(); i < sz; i++) {
			token = tokenlist.get(i);
			if (stage == 0) {
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
						pseudoAttrs.put(name, value);
						name = null;
						stage = 0;
					} else {
						// name=
						stage = 2;
					}
				}
			} else if (stage == 1) {
				int idx = token.indexOf('=');
				if (idx == -1) {
					pseudoAttrs.put(name, null);
					if (invalidPseudoAttrName(token)) {
						pseudoAttrs.clear();
						parseError("Invalid pseudo-attribute name in PI: " + token);
						return;
					}
					name = token;
				} else if (idx == 0) {
					// =[value]
					idx++;
					if (idx != token.length()) {
						String value = token.substring(idx);
						pseudoAttrs.put(name, value);
						name = null;
						stage = 0;
					} else {
						stage = 2;
					}
				} else {
					pseudoAttrs.put(name, null);
					name = token.substring(0, idx);
					if (invalidPseudoAttrName(name)) {
						pseudoAttrs.clear();
						parseError("Invalid pseudo-attribute name in PI: " + name);
						return;
					}
					idx++;
					if (idx != token.length()) {
						String value = token.substring(idx);
						pseudoAttrs.put(name, value);
						name = null;
						stage = 0;
					} else {
						// name=
						stage = 2;
					}
				}
			} else if (stage == 2) {
				pseudoAttrs.put(name, token);
				stage = 0;
			}
		}
	}

	private static void parseError(String message) throws DOMException {
		throw new DOMException(DOMException.INVALID_CHARACTER_ERR, message);
	}

	private static boolean invalidPseudoAttrName(String name) {
		if (name.length() != 0) {
			char[] na = name.toCharArray();
			for (int i = 0; i < na.length; i++) {
				if (!Character.isLetterOrDigit(na[i])) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
