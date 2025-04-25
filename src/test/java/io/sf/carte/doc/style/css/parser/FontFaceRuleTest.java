/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.InputSource;
import io.sf.carte.doc.style.css.nsac.Locator;

public class FontFaceRuleTest {

	private CSSParser parser;
	private TestCSSHandler handler;
	private TestErrorHandler errorHandler;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
		handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
	}

	@AfterEach
	public void tearDown() throws Exception {
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetFontFaceRule() throws CSSException, IOException {
		try (InputStream is = FontFaceRuleTest.class
				.getResourceAsStream("/io/sf/carte/doc/agent/common.css")) {
			InputSource source = new InputSource();
			source.setByteStream(is);
			source.setEncoding("utf-8");
			parser.parseStyleSheet(source);
		}

		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());

		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Foo Sans", handler.fontFeaturesNames.get(0)[0]);
		assertEquals(1, handler.featureMapNames.size());
		assertEquals("styleset", handler.featureMapNames.get(0));

		assertEquals(4, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("font-family", handler.propertyNames.get(1));
		assertEquals("src", handler.propertyNames.get(2));
		assertEquals("my-style", handler.propertyNames.get(3));
		assertEquals(4, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("'OpenSans Regular'", handler.lexicalValues.get(1).toString());
		assertEquals("url('/fonts/OpenSans-Regular.ttf')", handler.lexicalValues.get(2).toString());
		assertEquals("2", handler.lexicalValues.get(3).toString());
		assertEquals(4, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertNull(handler.priorities.get(3));
		assertEquals(1, handler.fontFaceCount);
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		assertEquals(33, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(74, handler.ptyLocators.get(2).getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(9, loc.getLineNumber());
		assertEquals(14, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetFontFaceRuleWrongChar() throws CSSException, IOException {
		Reader re = new StringReader(
				"\ufeff@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}@import 'foo.css';");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(0, handler.fontFaceCount);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertTrue(errorHandler.hasError());
	}

}
