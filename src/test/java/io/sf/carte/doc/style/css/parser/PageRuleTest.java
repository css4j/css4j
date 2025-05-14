/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.PageSelector;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;

public class PageRuleTest {

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
	public void testParseStyleSheetPageRule() throws CSSException, IOException {
		Reader re = loadTestCSSReader("page.css");
		parser.parseStyleSheet(re);
		re.close();

		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(6, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("margin-top", handler.propertyNames.get(1));
		assertEquals("content", handler.propertyNames.get(2));
		assertEquals("margin-left", handler.propertyNames.get(3));
		assertEquals("content", handler.propertyNames.get(4));
		assertEquals("margin-right", handler.propertyNames.get(5));
		assertEquals(6, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("20%", handler.lexicalValues.get(1).toString());
		assertEquals("none", handler.lexicalValues.get(2).toString());
		assertEquals("10%", handler.lexicalValues.get(3).toString());
		assertEquals("counter(page)", handler.lexicalValues.get(4).toString());
		assertEquals(6, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.get(0).toString());
		assertEquals("foo:left", handler.pageRuleSelectors.get(1).toString());
		assertEquals("bar:right,:blank", handler.pageRuleSelectors.get(2).toString());
		assertEquals(2, handler.marginRuleNames.size());
		assertEquals("top-center", handler.marginRuleNames.get(0));
		assertEquals("bottom-center", handler.marginRuleNames.get(1));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(5, loc.getLineNumber());
		assertEquals(17, loc.getColumnNumber());
		assertEquals(30, handler.ptyLocators.get(2).getColumnNumber());
		assertEquals(18, handler.ptyLocators.get(3).getColumnNumber());
		assertEquals(42, handler.ptyLocators.get(4).getColumnNumber());
		assertEquals(19, handler.ptyLocators.get(5).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetPageRuleEOF() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page :first{margin-top:20%;@top-left{content:'foo';color:blue");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		assertEquals(52, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(63, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetPageRule2() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		assertEquals(52, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(63, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetPageRule3() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page LetterHead:first{margin-top:20%;@top-left{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		PageSelector ps = psl.item(0);
		assertEquals("LetterHead:first", ps.toString());
		assertEquals("LetterHead", ps.getCssText());
		assertEquals("LetterHead", ps.getName());
		assertEquals(PageSelector.Type.PAGE_TYPE, ps.getSelectorType());
		ps = ps.getNext();
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":first", ps.getCssText());
		assertEquals("first", ps.getName());
		assertNull(ps.getNext());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals("green", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(38, loc.getColumnNumber());
		assertEquals(62, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(74, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetPageRule4() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page Foo:blank:first,:right{margin-top:20%;@top-left{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(2, psl.getLength());
		assertEquals("Foo:blank:first,:right", psl.toString());
		PageSelector ps = psl.item(0);
		assertEquals("Foo:blank:first", ps.toString());
		assertEquals("Foo", ps.getCssText());
		assertEquals("Foo", ps.getName());
		assertEquals(PageSelector.Type.PAGE_TYPE, ps.getSelectorType());
		ps = ps.getNext();
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":blank", ps.getCssText());
		assertEquals("blank", ps.getName());
		ps = ps.getNext();
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":first", ps.getCssText());
		assertEquals("first", ps.getName());
		assertNull(ps.getNext());

		ps = psl.item(1);
		assertEquals(":right", ps.toString());
		assertEquals(":right", ps.getCssText());
		assertEquals("right", ps.getName());
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertNull(ps.getNext());

		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals("green", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(44, loc.getColumnNumber());
		assertEquals(68, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(80, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetPageRule5() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page:first{margin-top:20%;@top-left{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals(":first", psl.toString());
		PageSelector ps = psl.item(0);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":first", ps.toString());
		assertEquals(":first", ps.getCssText());
		assertEquals("first", ps.getName());
		assertNull(ps.getNext());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals("green", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(27, loc.getColumnNumber());
		assertEquals(51, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(63, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetPageRuleBad() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page :first{margin-top:20%;myname@top-left{content:'foo';color:blue}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(35, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleBad2() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page :first{margin-top:20%;myname @top-left{content:'foo';color:blue}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(36, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleBad3() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page :first{margin-top:20%; top-left{content:'foo';color:blue}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(38, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleInvalidSelectorName() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page LetterHead :first{margin-top:20%;@top-left{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.pageRuleSelectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(0, handler.eventSeq.size());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(18, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleBadNameDot() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page LetterHead. {margin-top:20%;@top-left{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.pageRuleSelectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(17, ex.getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleBadNestedMarginBox() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page LetterHead:first{margin-top:20%;@top-left @{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		assertEquals(0, handler.marginRuleNames.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(49, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleBadNestedMarginBoxEOF() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page LetterHead:first{margin-top:20%;@top-left .{content:'foo';color:green");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		assertEquals(0, handler.marginRuleNames.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(49, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleBadNestedMarginBox2() throws CSSException, IOException {
		Reader re = new StringReader(
			"@page LetterHead:first{margin-top:20%;@top+left{content:'foo';color:green}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		assertEquals(0, handler.marginRuleNames.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(43, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetPageRuleNestedOnMediaRule() throws CSSException, IOException {
		Reader re = new StringReader("@media print {@page {margin-top: 20%;}h3 {width: 80%}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.pageRuleSelectors.size());
		assertNull(handler.pageRuleSelectors.getFirst());
		assertEquals(1, handler.mediaRuleLists.size());
		MediaQueryList medialist = handler.mediaRuleLists.getFirst();
		assertEquals(1, medialist.getLength());
		assertEquals("print", medialist.item(0));
		assertEquals(1, handler.selectors.size());
		assertEquals("h3", handler.selectors.get(0).toString());
		assertEquals(1, handler.endSelectors.size());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("width", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("80%", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals("endPage", handler.eventSeq.get(3));
		assertEquals("startSelector", handler.eventSeq.get(4));
		assertEquals("endSelector", handler.eventSeq.get(6));
		assertEquals("endMedia", handler.eventSeq.get(7));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());
		assertEquals(53, handler.ptyLocators.get(1).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	private static Reader loadTestCSSReader(String filename) {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/parser/" + filename);
	}

	private static Reader loadCSSfromClasspath(final String filename) {
		InputStream is = PageRuleTest.class.getResourceAsStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

}
