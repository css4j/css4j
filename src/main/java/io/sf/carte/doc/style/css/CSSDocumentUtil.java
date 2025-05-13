/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * Move these methods to CSSDocument after upgrading to Java 11 or later.
 */
class CSSDocumentUtil {

	private CSSDocumentUtil() {
		super();
	}

	static URI validURI(String uri) throws MalformedURLException {
		URI u;
		try {
			u = new URI(uri);
		} catch (URISyntaxException e) {
			int index = e.getIndex();
			// Replace invalid characters, avoiding those located at or before
			// first ':', if any, and next couple of chars.
			int colonIdx;
			if (index == -1 || ((colonIdx = uri.indexOf(':')) != -1 && colonIdx + 3 > index)) {
				throw new MalformedURLException(e.getMessage());
			}
			uri = replaceInvalidChar(uri, index);
			u = validURI(uri);
		}
		return u;
	}

	static private String replaceInvalidChar(String uri, int index) throws MalformedURLException {
		char invc = uri.charAt(index);
		if (invc == '\\' || invc == '"') {
			// For security and consistency reasons, do not replace '\' nor '"'
			StringBuilder buf = new StringBuilder(45);
			buf.append("Character '").append(invc).append("' invalid in URI at index: ");
			buf.append(index);
			throw new MalformedURLException(buf.toString());
		}
		String invalid = String.valueOf(invc);
		// TODO: uncomment next line when dropping Java 8
		//return uri.replace(invalid, URLEncoder.encode(invalid, StandardCharsets.UTF_8));
		try {
			return uri.replace(invalid, URLEncoder.encode(invalid, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			return uri;
		}
	}

}
