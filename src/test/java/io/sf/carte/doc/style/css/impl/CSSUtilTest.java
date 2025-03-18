/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class CSSUtilTest {

	@Test
	public void testIsInvalidContentType() throws IOException, URISyntaxException {
		assertFalse(isInvalidCSSContentType("http://www.example.com/foo.css", "text/css"));
		assertFalse(isInvalidCSSContentType("https://www.example.com/foo.css",
				"text/css; charset=utf-8"));
		assertFalse(isInvalidCSSContentType("HTTP://www.example.com/foo.css", "text/css"));
		assertFalse(isInvalidCSSContentType("HTTPS://www.example.com/foo.css", "text/css"));
		assertTrue(isInvalidCSSContentType("https://www.example.com/foo.css", null));
		assertTrue(isInvalidCSSContentType("https://www.example.com/foo.css", "text/html"));
		assertTrue(isInvalidCSSContentType("HTTPS://www.example.com/foo.css", "text/html"));
		assertTrue(isInvalidCSSContentType("HTTP://www.example.com/foo.css", "content/unknown"));
		assertTrue(isInvalidCSSContentType("FTP://ftp.example.com/foo.css", "application/json"));
		assertFalse(isInvalidCSSContentType("ftp://FTP.example.com/foo.css", "content/unknown"));
		assertFalse(isInvalidCSSContentType("ftp://ftp.example.com/foo.css", "unknown/unknown"));
		assertFalse(isInvalidCSSContentType("ftp://FTP.example.com/foo.css",
				"application/x-unknown-content-type"));
		assertFalse(isInvalidCSSContentType("jar:https://www.example.com/foo.jar!/foo.css",
				"application/java-archive"));
		assertFalse(isInvalidCSSContentType("jar:file:/path/to/foo.jar!/bar", "content/unknown"));
		assertFalse(isInvalidCSSContentType("jar:file:/path/to/foo.jar!/bar", "unknown/unknown"));
		assertFalse(isInvalidCSSContentType("jar:file:/path/to/foo.jar!/bar", null));
		assertFalse(isInvalidCSSContentType("file:///foo.css", "content/unknown"));
		assertFalse(isInvalidCSSContentType("FILE:///foo.css", "content/unknown"));
		assertFalse(isInvalidCSSContentType("ftp://ftp.example.org/foo.css", "content/unknown"));
		assertTrue(isInvalidCSSContentType("ftp://ftp.example.org/foo.css", "text/plain"));
	}

	private boolean isInvalidCSSContentType(String url, String contentType)
			throws MalformedURLException, URISyntaxException {
		URI uri = new URI(url);
		return CSSUtil.isInvalidCSSContentType(uri.toURL(), contentType);
	}

}
