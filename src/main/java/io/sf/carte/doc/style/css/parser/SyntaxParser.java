/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;
import java.util.Locale;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Multiplier;
import io.sf.carte.doc.style.css.nsac.CSSException;

/**
 * Parses a syntax specification according to
 * <a href="https://www.w3.org/TR/css-properties-values-api-1/">CSS Properties
 * and Values API Level 1</a> (accepts additional types like {@code <string>}).
 */
public class SyntaxParser {

	private static final HashMap<String,Category> catMap;

	static {
		catMap = new HashMap<>(23);
		catMap.put("angle", Category.angle);
		catMap.put("color", Category.color);
		catMap.put("custom-ident", Category.customIdent);
		catMap.put("image", Category.image);
		catMap.put("integer", Category.integer);
		catMap.put("length", Category.length);
		catMap.put("length-percentage", Category.lengthPercentage);
		catMap.put("number", Category.number);
		catMap.put("percentage", Category.percentage);
		catMap.put("resolution", Category.resolution);
		catMap.put("time", Category.time);
		catMap.put("unicode-range", Category.unicodeRange);
		catMap.put("easing-function", Category.easingFunction);
		catMap.put("transform-function", Category.transformFunction);
		catMap.put("transform-list", Category.transformList);
		catMap.put("url", Category.url);
		catMap.put("string", Category.string);
		catMap.put("counter", Category.counter);
		catMap.put("frequency", Category.frequency);
		catMap.put("flex", Category.flex);
		catMap.put("basic-shape", Category.basicShape);
	}

	private final static CSSValueSyntax universal = new SyntaxComponent("*", Category.universal);

	public SyntaxParser() {
		super();
	}

	/**
	 * Create a single-component syntax without multipliers.
	 * 
	 * @param category the category name ({@code url}, {@code string},
	 *                 {@code image}, etc).
	 * @return the single-component syntax, or {@code null} if the category name was
	 *         not recognized.
	 */
	public static CSSValueSyntax createSimpleSyntax(String category) {
		category = category.toLowerCase(Locale.ROOT);
		Category cat = category(category);
		if (cat != null) {
			return new SyntaxComponent(category, cat);
		} else {
			return null;
		}
	}

	/**
	 * Parse a syntax definition.
	 * 
	 * @param def the syntax definition.
	 * @return the object representing the syntax.
	 * @throws CSSException if the definition was not valid.
	 */
	public CSSValueSyntax parseSyntax(String def) throws CSSException {
		def = def.trim();
		int len = def.length();
		if (len == 0 || len == 2) {
			errInvalidDefinition(def);
		}
		char c = def.charAt(0);
		if (len == 1) {
			if (c == '*') {
				return getUniversal();
			}
			errInvalidDefinition(def);
		}

		if (c == '|' || def.charAt(len - 1) == '|') {
			errInvalidDefinition(def);
		}

		SyntaxComponent comp = new SyntaxComponent();
		SyntaxComponent current = comp;
		int idx = 0;
		topLoop: while ((idx = parseComponent(def, idx, current)) < len) {
			// Skip whitespace
			while (Character.isWhitespace(c = def.charAt(idx))) {
				idx++;
				if (idx == len) {
					break topLoop;
				}
			}

			if (c != '|') {
				errInvalidDefinition(def);
			}
			idx++;
			// Skip whitespace
			while (Character.isWhitespace(c = def.charAt(idx))) {
				idx++;
				if (idx == len) {
					break topLoop;
				}
			}

			SyntaxComponent next = new SyntaxComponent();
			current.setNext(next);
			current = next;
		}

		return comp;
	}

	private static int parseComponent(String def, int idx, SyntaxComponent comp) throws CSSException {
		final int len = def.length();
		int cp;
		// Skip whitespace
		while (Character.isWhitespace(cp = def.codePointAt(idx))) {
			idx++;
			if (idx == len) {
				return len;
			}
		}
		if (cp == '<') {
			// Data type
			idx = parseDataType(def, idx + 1, comp);
		} else if (isNameStartCharOrEsc(cp) || cp == 0x2d) {
			// Custom ident
			idx = parseIdent(def, idx, comp);
		} else {
			throw new CSSException("Unexpected character at definition, index " + idx + ": '" + def + '\'');
		}
		return idx;
	}

	private static int parseDataType(String def, int idx, SyntaxComponent comp) throws CSSException {
		final int len = def.length();
		boolean escaped = false;
		int i = idx;
		int cp;
		while ((cp = def.codePointAt(i)) != '>') {
			i = def.offsetByCodePoints(i, 1);
			if (cp == 0x5c) {
				escaped = true;
				continue;
			}
			if (!isNameChar(cp) || i == len) {
				errInvalidDefinition(def);
			}
		}
		if (i == idx) {
			errInvalidDefinition(def);
		}

		String name = def.substring(idx, i);
		if (escaped) {
			name = ParseHelper.unescapeStringValue(name);
		}
		name = name.toLowerCase(Locale.ROOT);
		comp.setName(name);
		Category cat = category(name);
		if (cat == null) {
			errUnknownDataType(name, def);
		}
		comp.setCategory(cat);

		i++; // Increment after the '>'
		if (i < len) {
			char c = def.charAt(i);
			if (c == '#') {
				comp.setMultiplier(Multiplier.NUMBER);
				i++;
			} else if (c == '+') {
				comp.setMultiplier(Multiplier.PLUS);
				i++;
			}
		}

		return i;
	}

	private static Category category(String name) {
		return catMap.get(name);
	}

	private static int parseIdent(String def, int idx, SyntaxComponent comp) throws CSSException {
		final int len = def.length();
		int i = idx;
		int escapeIndex = -2; // a value of -2 means 'not processing a escape'
		while (i < len) {
			int cp = def.codePointAt(i);
			if (cp == 0x5c) {
				if (escapeIndex == i - 1) {
					escapeIndex = -2;
				} else {
					escapeIndex = i;
				}
				i++;
				continue;
			} else if (!isNameChar(cp)) {
				if (escapeIndex != -2 && (cp == 0x20 || i == escapeIndex + 1)) {
					// Whitespace at the end of hex escape, or simple escape
					escapeIndex = -2;
					i++;
					continue;
				}
				break;
			}
			if (escapeIndex != -2 && !ParseHelper.isHexCodePoint(cp)) {
				escapeIndex = -2;
			}
			i = def.offsetByCodePoints(i, 1);
		}
		if (i == idx + 1) {
			errInvalidDefinition(def);
		}

		String name = def.substring(idx, i);
		name = ParseHelper.unescapeStringValue(name);
		comp.setName(name);
		comp.setCategory(Category.IDENT);

		if (i < len) {
			char c = def.charAt(i);
			if (c == '#') {
				comp.setMultiplier(Multiplier.NUMBER);
				i++;
			} else if (c == '+') {
				comp.setMultiplier(Multiplier.PLUS);
				i++;
			}
		}

		return i;
	}

	private static void errInvalidDefinition(String def) throws CSSException {
		throw new CSSException("Invalid definition: '" + def + '\'');
	}

	private static void errUnknownDataType(String name, String def) {
		throw new CSSException("Unknown data type '" + name + "' in definition: " + def);
	}

	private static boolean isNameStartCharOrEsc(int c) {
		return (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a) || c >= 0x80 || c == 0x5c || c == 0x5f;
	}

	private static boolean isNameChar(int c) {
		return (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a) || (c >= 0x30 && c <= 0x39) || c >= 0x80
				|| c == 0x5f || c == 0x2d;
	}

	static CSSValueSyntax getUniversal() {
		return universal;
	}

}
