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

		if (name.contains("width") || name.contains("height") || name.contains("-x")
				|| name.contains("-y") || name.contains("top") || name.contains("right")
				|| name.contains("bottom") || name.contains("left")) {
			if (cat == Category.length || cat == Category.lengthPercentage) {
				return Match.TRUE;
			}
			match = Match.FALSE;
		} else {
			match = Match.PENDING;
		}

		return fallback == null ? match : fallback.matches(rootSyntax);
	}

}
