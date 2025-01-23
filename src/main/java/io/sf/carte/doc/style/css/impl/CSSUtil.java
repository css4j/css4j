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

}
