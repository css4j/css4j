/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.PageSelector;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;

public class CSSParserTest {

	static CSSParser parser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
	}

	@Test
	public void testParsePageSelectorList() {
		PageSelectorList pagesel = parser.parsePageSelectorList("foo");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		PageSelector sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("foo", sel.getName());
		assertEquals("foo", sel.getCssText());
		assertNull(sel.getNext());
		assertEquals("foo", pagesel.toString());
		//
		pagesel = parser.parsePageSelectorList("\\31z");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("1z", sel.getName());
		assertEquals("\\31 z", sel.getCssText());
		assertNull(sel.getNext());
		assertEquals("\\31 z", pagesel.toString());
		//
		pagesel = parser.parsePageSelectorList("\\31 f");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("1f", sel.getName());
		assertEquals("\\31 f", sel.getCssText());
		assertNull(sel.getNext());
		assertEquals("\\31 f", pagesel.toString());
		//
		pagesel = parser.parsePageSelectorList(":first");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("first", sel.getName());
		assertEquals(":first", sel.getCssText());
		assertNull(sel.getNext());
		assertEquals(":first", pagesel.toString());
		//
		pagesel = parser.parsePageSelectorList(":first ,");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("first", sel.getName());
		assertEquals(":first", sel.getCssText());
		assertNull(sel.getNext());
		//
		pagesel = parser.parsePageSelectorList("foo:first");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("foo", sel.getName());
		assertEquals("foo", sel.getCssText());
		sel = sel.getNext();
		assertNotNull(sel);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("first", sel.getName());
		assertEquals(":first", sel.getCssText());
		assertNull(sel.getNext());
		//
		pagesel = parser.parsePageSelectorList("foo:first:left");
		assertNotNull(pagesel);
		assertEquals(1, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("foo", sel.getName());
		assertEquals("foo", sel.getCssText());
		sel = sel.getNext();
		assertNotNull(sel);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("first", sel.getName());
		assertEquals(":first", sel.getCssText());
		sel = sel.getNext();
		assertNotNull(sel);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("left", sel.getName());
		assertEquals(":left", sel.getCssText());
		assertNull(sel.getNext());
		//
		pagesel = parser.parsePageSelectorList("foo,bar:first");
		assertNotNull(pagesel);
		assertEquals(2, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("foo", sel.getName());
		assertEquals("foo", sel.getCssText());
		assertNull(sel.getNext());
		sel = pagesel.item(1);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("bar", sel.getName());
		assertEquals("bar", sel.getCssText());
		sel = sel.getNext();
		assertNotNull(sel);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("first", sel.getName());
		assertEquals(":first", sel.getCssText());
		assertNull(sel.getNext());
		assertEquals("foo,bar:first", pagesel.toString());
		//
		pagesel = parser.parsePageSelectorList("foo , bar:first");
		assertNotNull(pagesel);
		assertEquals(2, pagesel.getLength());
		sel = pagesel.item(0);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("foo", sel.getName());
		assertEquals("foo", sel.getCssText());
		assertNull(sel.getNext());
		sel = pagesel.item(1);
		assertEquals(PageSelector.Type.PAGE_TYPE, sel.getSelectorType());
		assertEquals("bar", sel.getName());
		assertEquals("bar", sel.getCssText());
		sel = sel.getNext();
		assertNotNull(sel);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, sel.getSelectorType());
		assertEquals("first", sel.getName());
		assertEquals(":first", sel.getCssText());
		assertNull(sel.getNext());
	}

	@Test
	public void testParsePageSelectorListError() {
		assertThrows(NullPointerException.class, () -> parser.parsePageSelectorList(null));

		DOMException ex = assertThrows(DOMException.class,
				() -> parser.parsePageSelectorList("::first"));
		assertEquals(DOMException.SYNTAX_ERR, ex.code);
	}

	@Test
	public void testParseMediaList() {
		NSACMediaQueryList list = new NSACMediaQueryList();
		list.parse(parser, "tv", null);
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("tv", list.item(0));
		list.setMediaText("tv, screen");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertEquals("tv", list.item(0));
		assertEquals("screen", list.item(1));
	}

	@Test
	public void testParseMediaQueryListAll() {
		MediaQueryList mql = parser.parseMediaQueryList("all", null);
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMediaText());
	}

	@Test
	public void testParseMediaQueryListEmpty() {
		MediaQueryList mql = parser.parseMediaQueryList("", null);
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMediaText());
	}

	@Test
	public void testParseMediaQueryListScreen() {
		MediaQueryList mql = parser.parseMediaQueryList("screen", null);
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("screen", mql.getMediaText());

		MediaQueryList mqlAll = parser.parseMediaQueryList("all", null);
		assertFalse(mql.matches(mqlAll));
		assertTrue(mqlAll.matches(mql));
	}

	@Test
	public void testParseMediaQueryListError() {
		MediaQueryList mql = parser.parseMediaQueryList("4/", null);
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertTrue(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("not all", mql.getMediaText());

		MediaQueryList mqlAll = parser.parseMediaQueryList("all", null);
		assertFalse(mql.matches(mqlAll));
		assertFalse(mqlAll.matches(mql));
	}

	@Test
	public void testParseSupportsCondition() {
		BooleanCondition cond = parser.parseSupportsCondition("(display: flex)", null);
		assertNotNull(cond);
		assertEquals(BooleanCondition.Type.PREDICATE, cond.getType());
		assertEquals("display", ((DeclarationCondition) cond).getName());
		CSSValue value = ((DeclarationCondition) cond).getValue();
		assertEquals(Type.IDENT, value.getPrimitiveType());
		assertEquals("flex", value.getCssText());
	}

	@Test
	public void testParseSupportsConditionUnrecognizedValue() {
		BooleanCondition cond = parser.parseSupportsCondition("(display:9pt0)", null);
		assertNotNull(cond);
		assertEquals(BooleanCondition.Type.OTHER, cond.getType());
		assertEquals("(display:9pt0)", cond.toString());
	}

	@Test
	public void testParseSupportsConditionErrorEmpty() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("", null));
		assertEquals(1, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionErrorWhitespace() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("   ", null));
		assertEquals(4, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionErrorEmptyParens() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("()", null));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionLeftMissingParenError() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("display: flex)", null));
		assertEquals(8, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionRightMissingParenError() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("(display: flex", null));
		assertEquals(15, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionNoParensError() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("display: flex", null));
		assertEquals(8, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionErrorNoValue() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("(display:)", null));
		assertEquals(10, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionErrorEmptyPredicate() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("not()", null));
		assertEquals(5, ex.getColumnNumber());

		ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("(display:block) and ()", null));
		assertEquals(22, ex.getColumnNumber());

		ex = assertThrows(CSSParseException.class,
				() -> parser.parseSupportsCondition("(display:block) or ()", null));
		assertEquals(21, ex.getColumnNumber());
	}

	@Test
	public void testBufferEndsWithEscapedCharOrWS() {
		StringBuilder buffer = new StringBuilder(50);
		buffer.append("1a");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("\\");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("foo");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("\\123456");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("\\12 foo");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("foo\\123456");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("\\12345");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("foo\\12345");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("\\1");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.setLength(0);
		buffer.append("foo\\1");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
	}

	@Test
	public void testBufferEndsWithEscapedChar() {
		StringBuilder buf = new StringBuilder("1a");
		assertFalse(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("\\qa");
		assertFalse(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("\\a");
		assertTrue(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("w\\1a");
		assertTrue(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("w\\1ab2e");
		assertTrue(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("w\\1ab2e7");
		assertFalse(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("abcdefghijk\\a");
		assertTrue(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("abcdefghijk\\ar");
		assertFalse(CSSParser.bufferEndsWithEscapedChar(buf));
		buf = new StringBuilder("\\");
		assertFalse(CSSParser.bufferEndsWithEscapedChar(buf));
	}

}
