/*

 Copyright (c) 2005-2024, Carlos Amengual.

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
import java.net.URL;

import org.junit.jupiter.api.Test;

class CSSUtilTest {

	@Test
	public void testIsInvalidContentType() throws IOException {
		assertFalse(CSSUtil
				.isInvalidCSSContentType(new URL("http://www.example.com/foo.css"), "text/css"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("https://www.example.com/foo.css"), "text/css; charset=utf-8"));
		assertFalse(CSSUtil
				.isInvalidCSSContentType(new URL("HTTP://www.example.com/foo.css"), "text/css"));
		assertFalse(CSSUtil
				.isInvalidCSSContentType(new URL("HTTPS://www.example.com/foo.css"), "text/css"));
		assertTrue(CSSUtil
				.isInvalidCSSContentType(new URL("https://www.example.com/foo.css"), null));
		assertTrue(CSSUtil
				.isInvalidCSSContentType(new URL("https://www.example.com/foo.css"), "text/html"));
		assertTrue(CSSUtil
				.isInvalidCSSContentType(new URL("HTTPS://www.example.com/foo.css"), "text/html"));
		assertTrue(CSSUtil.isInvalidCSSContentType(new URL("HTTP://www.example.com/foo.css"),
				"content/unknown"));
		assertTrue(CSSUtil.isInvalidCSSContentType(
				new URL("FTP://ftp.example.com/foo.css"), "application/json"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("ftp://FTP.example.com/foo.css"), "content/unknown"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("ftp://ftp.example.com/foo.css"), "unknown/unknown"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("ftp://FTP.example.com/foo.css"), "application/x-unknown-content-type"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("jar:https://www.example.com/foo.jar!/foo.css"),
				"application/java-archive"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("jar:file:/path/to/foo.jar!/bar"), "content/unknown"));
		assertFalse(CSSUtil.isInvalidCSSContentType(
				new URL("jar:file:/path/to/foo.jar!/bar"), "unknown/unknown"));
		assertFalse(CSSUtil
				.isInvalidCSSContentType(new URL("jar:file:/path/to/foo.jar!/bar"), null));
		assertFalse(CSSUtil.isInvalidCSSContentType(new URL("file:///foo.css"),
				"content/unknown"));
		assertFalse(CSSUtil.isInvalidCSSContentType(new URL("FILE:///foo.css"),
				"content/unknown"));
		assertFalse(CSSUtil.isInvalidCSSContentType(new URL("ftp://ftp.example.org/foo.css"),
				"content/unknown"));
		assertTrue(CSSUtil.isInvalidCSSContentType(new URL("ftp://ftp.example.org/foo.css"),
				"text/plain"));
	}

}
