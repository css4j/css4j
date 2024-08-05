/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;

class SheetParserErrorTest {

	private CSSParser parser;
	private TestCSSHandler handler;
	private TestErrorHandler errorHandler;

	@BeforeEach
	void setUp() throws Exception {
		parser = new CSSParser();
		handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
	}

	@AfterEach
	void tearDown() throws Exception {
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testSelectorForbiddenEscapedControlError() throws CSSException, IOException {
		String s = "VV\014\\\001++";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(2, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testNSSelectorError() throws CSSException, IOException {
		String s = ".foo|bar";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testNSSelectorErrorLF() throws CSSException, IOException {
		String s = ".foo|bar\n";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testNSSelectorErrorNoTypeEOF() throws CSSException, IOException {
		String s = ".foo|";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testNSSelectorErrorNoTypeLF() throws CSSException, IOException {
		String s = ".foo|\n";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testNSSelectorErrorNoClass() throws CSSException, IOException {
		String s = "p. a ~";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(3, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testRuleErrorMgmt() throws CSSException, IOException {
		String s = "@{@\n+}\n.\n.{};@;*;@;*-color";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(4, errorHandler.getLastException().getLineNumber());
		assertEquals(12, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testBadRuleClosing() throws CSSException, IOException {
		String s = "*{(&+}(";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(7, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testBadPageRule() throws CSSException, IOException {
		String s = "@page{;;@";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(10, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testBadPageRule2() throws CSSException, IOException {
		String s = "@page{;;@m";
		parser.parseStyleSheet(new StringReader(s));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

}
