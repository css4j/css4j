/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser;

public class CSSOMParserTest {

	Parser parser;

	@BeforeEach
	public void setUp() {
		parser = new CSSOMParser();
	}

	@Test
	public void testParseMediaQueryList() {
		MediaQueryList mql = parser.parseMediaQueryList("all", null);
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMedia());
	}

	@Test
	public void testParseMediaQueryListEmpty() {
		MediaQueryList mql = parser.parseMediaQueryList("", null);
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMedia());
	}

	@Test
	public void testParseMediaQueryListScreen() {
		MediaQueryList mql = parser.parseMediaQueryList("screen", null);
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("screen", mql.getMedia());

		MediaQueryList mqlAll = parser.parseMediaQueryList("all", null);
		assertFalse(mql.matches(mqlAll));
		assertTrue(mqlAll.matches(mql));
	}

	@Test
	public void testParseMediaQueryListError() {
		MediaQueryList mql = parser.parseMediaQueryList("16/", null);
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertTrue(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("not all", mql.getMedia());

		MediaQueryList mqlAll = parser.parseMediaQueryList("all", null);
		assertFalse(mql.matches(mqlAll));
		assertFalse(mqlAll.matches(mql));
	}

}
