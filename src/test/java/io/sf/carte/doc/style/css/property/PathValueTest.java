/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class PathValueTest {

	BaseCSSStyleDeclaration style;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		StyleRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("d: path('M2,5 S2,-2 4,5 S7,8 8,4');");
		StyleValue value = style.getPropertyCSSValue("d");
		assertTrue(value.equals(value));
		style.setCssText("d: path('M2,5 S2,-2 4,5 S7,8 8,4');");
		StyleValue value2 = style.getPropertyCSSValue("d");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());

		style.setCssText("d: path('M2,5 S2,-2 4,5')");
		value2 = style.getPropertyCSSValue("d");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testPath() {
		style.setCssText("d:PATH('M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')");
		PathValue val = (PathValue) style.getPropertyCSSValue("d");
		assertNotNull(val);
		assertEquals(CSSValue.Type.PATH, val.getPrimitiveType());
		assertEquals("M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80", val.getStringValue());
		assertEquals("path('M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')", val.getCssText());

		assertNull(val.getFillRule());

		assertEquals("M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80", val.getPath());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testPathFillRule() {
		style.setCssText("d:path(nonzero, 'M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')");
		PathValue val = (PathValue) style.getPropertyCSSValue("d");
		assertNotNull(val);
		assertEquals(CSSValue.Type.PATH, val.getPrimitiveType());
		assertEquals("M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80", val.getStringValue());
		assertEquals("path(nonzero, 'M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')",
				val.getCssText());
		assertEquals("path(nonzero,'M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')",
				val.getMinifiedCssText());

		assertEquals("nonzero", val.getFillRule().getStringValue());

		assertEquals("M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80", val.getPath());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<integer>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("d: path(nonzero,'M2,5 S2,-2 4,5 S7,8 8,4')");
		PathValue value = (PathValue) style.getPropertyCSSValue("d");
		PathValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getPath(), clon.getPath());
		assertEquals(value.getFillRule(), clon.getFillRule());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testCloneNoFill() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("d: path('M2,5 S2,-2 4,5 S7,8 8,4')");
		PathValue value = (PathValue) style.getPropertyCSSValue("d");
		PathValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getPath(), clon.getPath());
		assertEquals(value.getFillRule(), clon.getFillRule());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
