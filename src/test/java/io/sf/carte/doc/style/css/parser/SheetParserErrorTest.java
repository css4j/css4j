/*

 Copyright (c) 2005-2025, Carlos Amengual.

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
import io.sf.carte.doc.style.css.nsac.InputSource;

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
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
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
	public void testSelectorStarHack() throws CSSException, IOException {
		String s = "p{*width:900px;}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(0, handler.propertyNames.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(4, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testDotError() throws CSSException, IOException {
		String s = "p{.width:900px;}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(0, handler.propertyNames.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(15, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testColonError() throws CSSException, IOException {
		String s = "p{:.width;}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(0, handler.propertyNames.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(4, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testNestedDotError() throws CSSException, IOException {
		String s = "p{&.cls{.width:900px;}}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(1, handler.nestedSelectors.size());
		assertEquals("&.cls", handler.nestedSelectors.get(0).toString());
		assertEquals(0, handler.propertyNames.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testNestedColonError() throws CSSException, IOException {
		String s = "p{&.cls{ :.width;}}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(1, handler.nestedSelectors.size());
		assertEquals("&.cls", handler.nestedSelectors.get(0).toString());
		assertEquals(0, handler.propertyNames.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
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

	@Test
	public void testBadNestedSelector() throws CSSException, IOException {
		String s = "A{Q	\\A(z\"#\"(z\"#\"\"'ÿ";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorNoRule() throws CSSException, IOException {
		String s = "p";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(2, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorCommaNoRule() throws CSSException, IOException {
		String s = "p,";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(3, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorErrorLT() throws CSSException, IOException {
		String s = "0<ÈÈ";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(1, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorCommaAt() throws CSSException, IOException {
		String s = "p,@";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(3, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorNestedEOF() throws CSSException, IOException {
		String s = "p{&{a";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorCDO() throws CSSException, IOException {
		String s = "|\u0211 :Z,<**\u0000\u0000\u0000 *";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(8, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorNoNS() throws CSSException, IOException {
		String s = "|* |* {r";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(5, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testSelectorRightCurlyBracket() throws CSSException, IOException {
		String s = "#x,::y,|}ð[##";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(12, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testPageUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = ";@page ˜l$$$$<$$$\n<}}<<<<<}}<<<<<<<<<<<<! ~";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(12, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testPageMarginUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@page :first{@top-left +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(24, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testCharsetUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = ";@charset +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testCounterStyleUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@counter-style foo +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(20, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testFontFaceUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@font-face +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(15, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testFontFeatureValuesUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@font-feature-values Otaru +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(28, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testFontFeatureUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@font-feature-values Otaru @styleset +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(28, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testKeyframesUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@keyframes anim-1 +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(19, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testKeyframeUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@keyframes anim-1{to +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(23, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testPropertyUnexpectedRightCurlyBracket() throws CSSException, IOException {
		String s = "@property --pty +$<}";
		parser.parseStyleSheet(new InputSource(new StringReader(s)));
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(17, errorHandler.getLastException().getColumnNumber());
	}

}
