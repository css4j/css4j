/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css.impl;

import java.net.URL;
import java.util.Locale;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * Utility methods related to CSS.
 */
public class CSSUtil {

	/**
	 * Is the given string a valid CSS identifier?
	 * 
	 * @param s the non-empty identifier to test; cannot contain hex escapes.
	 * @return true if is a valid identifier.
	 */
	public static boolean isValidIdentifier(CharSequence s) {
		int len = s.length();
		int idx;
		char c = s.charAt(0);
		if (c != '-') {
			if (!isNameStartChar(c) && c != '\\') {
				return false;
			}
			idx = 1;
		} else if (len > 1) {
			c = s.charAt(1);
			if (!isNameStartChar(c) && c != '-' && c != '\\') {
				return false;
			}
			idx = 2;
		} else {
			return false;
		}
		while (idx < len) {
			c = s.charAt(idx);
			if (!isNameChar(c)) {
				return false;
			}
			idx++;
		}
		return true;
	}

	/**
	 * Is the given string a valid CSS pseudo-element/class name?
	 * 
	 * @param s the non-empty name to test; cannot contain escapes.
	 * @return true if is a valid name.
	 */
	public static boolean isValidPseudoName(CharSequence s) {
		int len = s.length();
		int idx;
		char c = s.charAt(0);
		if (c != '-') {
			if (!isNameStartChar(c)) {
				return false;
			}
			idx = 1;
		} else if (len > 1) {
			c = s.charAt(1);
			if (!isNameStartChar(c)) {
				return false;
			}
			idx = 2;
		} else {
			return false;
		}
		while (idx < len) {
			c = s.charAt(idx);
			if (!isNameChar(c)) {
				return false;
			}
			idx++;
		}
		return true;
	}

	private static boolean isNameChar(char cp) {
		return (cp >= 0x61 && cp <= 0x7A) // a-z
				|| (cp >= 0x41 && cp <= 0x5A) // A-Z
				|| (cp >= 0x30 && cp <= 0x39) // 0-9
				|| cp == 0x2d // -
				|| cp == 0x5f // _
				|| cp > 0x80 // non-ASCII code point
				|| cp == 0x5c; // '\'
	}

	private static boolean isNameStartChar(char cp) {
		return (cp >= 0x61 && cp <= 0x7A) // a-z
				|| (cp >= 0x41 && cp <= 0x5A) // A-Z
				|| cp == 0x5f // _
				|| cp > 0x80; // non-ASCII code point
	}

	/**
	 * Check whether the given content-type is invalid for the provided URL pointing
	 * to a style sheet.
	 * 
	 * @param url     the URL.
	 * @param conType the content-type.
	 * @return {@code true} if the content-type is invalid for a CSS style sheet.
	 */
	public static boolean isInvalidCSSContentType(URL url, String conType) {
		if (conType == null) {
			// Disallow http/https
			String proto = url.getProtocol();
			return "https".equals(proto) || "http".equals(proto);
		}

		int sepidx = conType.indexOf(';');
		if (sepidx != -1) {
			conType = conType.substring(0, sepidx);
		}

		conType = conType.toLowerCase(Locale.ROOT);

		if ("text/css".equals(conType)) {
			return false;
		}

		String proto = url.getProtocol();
		/*
		 * If the content-type could not be determined, it may be content/unknown,
		 * unknown/unknown or even others. Be tolerant for non-http(s) URLs.
		 * 
		 * Ignore jar: and file: URLs because the content-type could be unpredictable.
		 */
		return !"jar".equals(proto) && !"file".equals(proto)
				&& ("https".equals(proto) || "http".equals(proto)
						|| (!"content/unknown".equals(conType) && !"unknown/unknown".equals(conType)
								&& !"application/x-unknown-content-type".equals(conType)));
	}

	/**
	 * Check whether the given function name is a proposed image function that was
	 * not yet implemented in browsers at the time of last revision.
	 * <p>
	 * This method helps the library in processing image functions that had not been
	 * implemented by browsers when it was released.
	 * </p>
	 * 
	 * @param lcfName the lower-case name of the function.
	 * @return {@code true} if the name is an unimplemented proposed image function.
	 */
	public static boolean isUnimplementedImageFunction(String lcfName) {
		return lcfName.equals("paint") || lcfName.equals("image") || lcfName.equals("cross-fade");
	}

	/**
	 * Match an environment variable.
	 */
	public static Match matchEnv(CSSValueSyntax rootSyntax, CSSValueSyntax syntax, String name,
			LexicalUnit fallback) {
		Category cat = syntax.getCategory();

		if (cat == Category.universal) {
			return Match.TRUE;
		}

		Match match;

		if (isEnvLengthName(name)) {
			if (cat == Category.length || cat == Category.lengthPercentage) {
				return Match.TRUE;
			}
			match = Match.FALSE;
		} else {
			match = Match.PENDING;
		}

		return fallback == null ? match : fallback.matches(rootSyntax);
	}

	public static boolean isEnvLengthName(String name) {
		return name.contains("width") || name.contains("height") || name.contains("-x")
				|| name.contains("-y") || name.contains("top") || name.contains("right")
				|| name.contains("bottom") || name.contains("left");
	}

}
