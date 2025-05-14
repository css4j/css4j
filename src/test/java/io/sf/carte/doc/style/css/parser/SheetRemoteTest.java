/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.InputSource;

public class SheetRemoteTest {

	@Test
	public void testParseStyleSheetRemote() throws CSSException, IOException {
		assumeTrue(TestConfig.REMOTE_TESTS);
		CSSParser parser = new CSSParser();
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet("https://css4j.github.io/usage-e.css");
		assertEquals(0, handler.comments.size());
		assertTrue(handler.selectors.size() != 0);
		assertTrue(handler.propertyNames.size() != 0);
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetRemoteInvalid() throws CSSException, IOException {
		assumeTrue(TestConfig.REMOTE_TESTS);
		CSSParser parser = new CSSParser();
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		assertThrows(IOException.class,
				() -> parser.parseStyleSheet("https://css4j.github.io/benchmarks.html"));
	}

	@Test
	public void testParseStyleSheetRemoteInputSource() throws CSSException, IOException {
		assumeTrue(TestConfig.REMOTE_TESTS);
		CSSParser parser = new CSSParser();
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource is = new InputSource("https://css4j.github.io/usage-e.css");
		is.setEncoding("utf-8");
		parser.parseStyleSheet(is);
		assertEquals(0, handler.comments.size());
		assertTrue(handler.selectors.size() != 0);
		assertTrue(handler.propertyNames.size() != 0);
		assertFalse(errorHandler.hasError());
	}

}
