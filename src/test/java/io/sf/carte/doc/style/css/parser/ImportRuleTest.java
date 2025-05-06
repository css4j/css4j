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
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class ImportRuleTest {

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
	public void testParseImportRule() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css');");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertEquals(1, handler.importLayers.size());
		assertNull(handler.importLayers.get(0));
		assertEquals(1, handler.importSupportsConditions.size());
		assertNull(handler.importSupportsConditions.get(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRule2() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertEquals(1, handler.importLayers.size());
		assertNull(handler.importLayers.get(0));
		assertEquals(1, handler.importSupportsConditions.size());
		assertNull(handler.importSupportsConditions.get(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleComment() throws CSSException, IOException {
		Reader re = new StringReader("@import/* comment */url(foo.css);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertEquals(1, handler.importLayers.size());
		assertNull(handler.importLayers.get(0));
		assertEquals(1, handler.importSupportsConditions.size());
		assertNull(handler.importSupportsConditions.get(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMedia() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css) print;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));

		assertEquals(1, handler.importLayers.size());
		assertNull(handler.importLayers.get(0));
		assertEquals(1, handler.importSupportsConditions.size());
		assertNull(handler.importSupportsConditions.get(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaComment() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css)/* comment */print;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));

		assertEquals(1, handler.importLayers.size());
		assertNull(handler.importLayers.get(0));
		assertEquals(1, handler.importSupportsConditions.size());
		assertNull(handler.importSupportsConditions.get(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMedia2() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css) screen, tv;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQuery() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') (orientation:landscape);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("(orientation: landscape)", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryComment() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('foo.css') (orientation:/* comment 1 */landscape/* comment 2 */);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("(orientation: landscape)", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQuery2() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and (orientation:landscape);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (orientation: landscape)", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and (800px<width<=1200px);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (800px < width <= 1200px)", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4b() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and (resolution>=72dpi);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (resolution >= 72dpi)", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4Comment() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('foo.css') screen/* comment1 */and (800px/* comment2 */<width<=1200px);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (800px < width <= 1200px)", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4calc() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('foo.css') screen and (800px<width<=calc(2400px/2 + 2*100px - 50px));");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (800px < width <= calc(2400px/2 + 2*100px - 50px))", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryBad() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and ((orientation:landscape);");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(59, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleMediaQueryMissingRightParenEOF()
			throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and ((orientation:landscape)");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals(1, handler.importMedias.size());
		assertEquals("screen and (orientation: landscape)", handler.importMedias.get(0).toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrl() throws CSSException, IOException {
		Reader re = new StringReader("@import 'foo.css';");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlMedia() throws CSSException, IOException {
		Reader re = new StringReader("@import 'foo.css' print;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlDQ() throws CSSException, IOException {
		Reader re = new StringReader("@import \"foo.css\";");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlDQMedia() throws CSSException, IOException {
		Reader re = new StringReader("@import \"foo.css\" screen, tv;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoString() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url(https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,200;0,300;0,400;0,500;0,700;1,400;1,500;1,700&display=swap);");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals(
				"https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,200;0,300;0,400;0,500;0,700;1,400;1,500;1,700&display=swap",
				handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleAnonLayerSupports() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('foo.css') layer supports(display: grid) screen and (max-width: 900px),print;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importLayers.size());
		assertEquals(0, handler.importLayers.get(0).length());
		assertEquals(1, handler.importSupportsConditions.size());
		assertEquals("(display: grid)", handler.importSupportsConditions.get(0).toString());
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen and (max-width: 900px)", list.item(0));
		assertEquals("print", list.item(1));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleLayerSupports() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('foo.css') layer( top.default ) supports(display: grid ) "
						+ "screen and (max-width: 900px ),print;");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importLayers.size());
		assertEquals("top.default", handler.importLayers.get(0));
		assertEquals(1, handler.importSupportsConditions.size());
		assertEquals("(display: grid)", handler.importSupportsConditions.get(0).toString());
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen and (max-width: 900px)", list.item(0));
		assertEquals("print", list.item(1));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleBad() throws CSSException, IOException {
		Reader re = new StringReader("foo@import url('bar.css');");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(4, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleBadClosingParen() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('bar.css'));a:not([href]):not([tabindex]),a:not([href]):not([tabindex]):focus,code,pre,div{display:block}");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());

		assertEquals(1, handler.selectors.size());
		SelectorList selist = handler.selectors.get(0);
		assertEquals(
				"a:not([href]):not([tabindex]),a:not([href]):not([tabindex]):focus,code,pre,div",
				selist.toString());
		assertEquals(5, selist.getLength());
		assertEquals("a:not([href]):not([tabindex])", selist.item(0).toString());
		assertEquals("a:not([href]):not([tabindex]):focus", selist.item(1).toString());
		assertEquals("code", selist.item(2).toString());
		assertEquals("pre", selist.item(3).toString());
		assertEquals("div", selist.item(4).toString());

		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());

		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(117, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(23, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleBadUnclosedParen() throws CSSException, IOException {
		Reader re = new StringReader("@import url((bar.css);");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(13, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleBadURLTwoString() throws CSSException, IOException {
		Reader re = new StringReader("@import url(' ' ' ');");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(17, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleMalformedURL() throws CSSException, IOException {
		Reader re = new StringReader("@import url(a 'b');");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(15, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleUnexpectedURLModifier() throws CSSException, IOException {
		Reader re = new StringReader("@import url('a' b);");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(17, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleURLLegacyInvalidModifier() throws CSSException, IOException {
		Reader re = new StringReader("@import url(a b);");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(15, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleInvalidLayer() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('sheet.css') layer(inherit) supports(display:grid) (max-width: 900px)");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(39, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleInvalidNestedLayer() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('sheet.css') layer(default.inherit) supports(display:grid) (max-width: 900px)");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(47, errorHandler.getLastException().getColumnNumber());
	}

}
