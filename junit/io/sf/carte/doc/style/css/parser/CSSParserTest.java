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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

	@Test
	public void testParsePageSelectorList() {
		CSSParser parser = new CSSParser();
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
		CSSParser parser = new CSSParser();
		try {
			parser.parsePageSelectorList(null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			parser.parsePageSelectorList("::first");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testParseMediaList() {
		CSSParser parser = new CSSParser();
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
		CSSParser parser = new CSSParser();
		MediaQueryList mql = parser.parseMediaQueryList("all", null);
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMediaText());
	}

	@Test
	public void testParseMediaQueryListEmpty() {
		CSSParser parser = new CSSParser();
		MediaQueryList mql = parser.parseMediaQueryList("", null);
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMediaText());
	}

	@Test
	public void testParseMediaQueryListScreen() {
		CSSParser parser = new CSSParser();
		MediaQueryList mql = parser.parseMediaQueryList("screen", null);
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("screen", mql.getMediaText());
		//
		MediaQueryList mqlAll = parser.parseMediaQueryList("all", null);
		assertFalse(mql.matches(mqlAll));
		assertTrue(mqlAll.matches(mql));
	}

	@Test
	public void testParseMediaQueryListError() {
		CSSParser parser = new CSSParser();
		MediaQueryList mql = parser.parseMediaQueryList("4/", null);
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertTrue(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("not all", mql.getMediaText());
		//
		MediaQueryList mqlAll = parser.parseMediaQueryList("all", null);
		assertFalse(mql.matches(mqlAll));
		assertFalse(mqlAll.matches(mql));
	}

	@Test
	public void testParseSupportsCondition() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition("(display: flex)", null);
		assertNotNull(cond);
		assertEquals(BooleanCondition.Type.PREDICATE, cond.getType());
		assertEquals("display", ((DeclarationCondition) cond).getName());
		CSSValue value = ((DeclarationCondition) cond).getValue();
		assertEquals(Type.IDENT, value.getPrimitiveType());
		assertEquals("flex", value.getCssText());
	}

	@Test
	public void testParseSupportsConditionError() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
		//
		try {
			parser.parseSupportsCondition("   ", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
		try {
			parser.parseSupportsCondition("()", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
		//
		//
		try {
			parser.parseSupportsCondition("(display: flex", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(15, e.getColumnNumber());
		}
		//
		try {
			parser.parseSupportsCondition("display: flex", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
		//
		try {
			parser.parseSupportsCondition("(display:)", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
		//
		try {
			parser.parseSupportsCondition("(display:9pt0)", null);
			fail("Must throw exception.");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testBufferEndsWithEscapedChar() {
		StringBuilder buffer = new StringBuilder(50);
		buffer.append("foo");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\123456");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\12 foo");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("foo\\123456");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\12345");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("foo\\12345");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\1");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("foo\\1");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
	}

}
