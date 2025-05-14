/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Locator;

public class CharsetRuleTest {

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
	public void testParseCharsetRule() throws CSSException, IOException {
		Reader re = new StringReader("@charset \"utf-8\";");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCharsetRuleEOF() throws CSSException, IOException {
		Reader re = new StringReader("@charset \"utf-8\"");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseCharsetRule2() throws CSSException, IOException {
		Reader re = new StringReader("/* My sheet */\n@charset \"utf-8\";");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.comments.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCharsetRuleBad() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css');@charset \"utf-8\";");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(24, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad2() throws CSSException, IOException {
		Reader re = new StringReader("p{display:block;}@charset \"utf-8\";");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).item(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(18, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad3() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media print {@page {margin-top: 20%;}h3 {width: 80%}}@charset \"utf-8\";");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.pageRuleSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(55, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad4() throws CSSException, IOException {
		Reader re = new StringReader(
			"@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf')}@charset \"utf-8\";");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.atRules.size());

		assertEquals(2, handler.ptyLocators.size());
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(41, loc.getColumnNumber());
		assertEquals(74, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(75, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad5() throws CSSException, IOException {
		Reader re = new StringReader(
			"@font-feature-values Foo Sans, Bar {@styleset {my-style: 2;}}@charset \"utf-8\";");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Foo Sans", handler.fontFeaturesNames.get(0)[0]);
		assertEquals("Bar", handler.fontFeaturesNames.get(0)[1]);
		assertEquals(1, handler.featureMapNames.size());
		assertEquals("styleset", handler.featureMapNames.get(0));
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(59, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(62, errorHandler.getLastException().getColumnNumber());
	}

	/*
	 * Make sure that parsing recovers from bad charset rule error.
	 * 
	 */

	@Test
	public void testParseCharsetRuleBadRecovery() throws CSSException, IOException {
		Reader re = new StringReader("p{display:block;}@charset \"utf-8\";span {color: blue;}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.atRules.size());
		assertEquals(2, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).item(0).toString());
		assertEquals("span", handler.selectors.get(1).item(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(16, loc.getColumnNumber());
		assertEquals(52, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(18, errorHandler.getLastException().getColumnNumber());
	}

}
