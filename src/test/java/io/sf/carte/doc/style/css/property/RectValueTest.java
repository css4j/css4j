/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSRectValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class RectValueTest {

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
		style.setCssText("clip: rect(2px 12em 3em 2pt);");
		RectValue value = (RectValue) style.getPropertyCSSValue("clip");
		assertTrue(value.equals(value));
		style.setCssText("clip: rect(2px 12em 3em 2pt);");
		RectValue value2 = (RectValue) style.getPropertyCSSValue("clip");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("clip: rect(2px 12em 3em 1pt);");
		value2 = (RectValue) style.getPropertyCSSValue("clip");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testRect() {
		style.setCssText("clip: rect(2px 12em 3em 2pt); ");
		StyleValue cssval = style.getPropertyCSSValue("clip");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.RECT, cssval.getPrimitiveType());
		assertEquals("rect(2px, 12em, 3em, 2pt)", style.getPropertyValue("clip"));
		assertEquals("clip: rect(2px, 12em, 3em, 2pt); ", style.getCssText());
		assertEquals("clip:rect(2px,12em,3em,2pt)", style.getMinifiedCssText());
		CSSRectValue rect = (CSSRectValue) cssval;
		assertEquals("2px", rect.getTop().getCssText());
		assertEquals("12em", rect.getRight().getCssText());
		assertEquals("3em", rect.getBottom().getCssText());
		assertEquals("2pt", rect.getLeft().getCssText());
	}

	@Test
	public void testRectCommaSeparated() {
		style.setCssText("clip: rect(2px, 12em, 3em, 2pt); ");
		StyleValue cssval = style.getPropertyCSSValue("clip");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.RECT, cssval.getPrimitiveType());
		assertEquals("rect(2px, 12em, 3em, 2pt)", style.getPropertyValue("clip"));
		assertEquals("clip: rect(2px, 12em, 3em, 2pt); ", style.getCssText());
		assertEquals("clip:rect(2px,12em,3em,2pt)", style.getMinifiedCssText());
		CSSRectValue rect = (CSSRectValue) cssval;
		assertEquals("2px", rect.getTop().getCssText());
		assertEquals("12em", rect.getRight().getCssText());
		assertEquals("3em", rect.getBottom().getCssText());
		assertEquals("2pt", rect.getLeft().getCssText());
	}

	@Test
	public void testRectComponents() {
		style.setCssText("clip: rect(2px 12em 3em 2pt); ");
		RectValue value = (RectValue) style.getPropertyCSSValue("clip");

		assertEquals(4, value.getComponentCount());

		assertSame(value.getTop(), value.getComponent(0));
		assertSame(value.getRight(), value.getComponent(1));
		assertSame(value.getBottom(), value.getComponent(2));
		assertSame(value.getLeft(), value.getComponent(3));

		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_PT, 3f);
		value.setTop(number);
		assertEquals("rect(3pt, 12em, 3em, 2pt)", value.getCssText());
		number.setFloatValue(CSSUnit.CSS_PC, 2f);
		value.setComponent(0, number);
		assertEquals("rect(2pc, 12em, 3em, 2pt)", value.getCssText());
		assertSame(number, value.getComponent(0));
		value.setComponent(1, number);
		value.setComponent(2, number);
		value.setComponent(3, number);
		value.setComponent(4, number);
		assertEquals("rect(2pc, 2pc, 2pc, 2pc)", value.getCssText());

		assertMatch(Match.TRUE, value, "<basic-shape>");
		assertMatch(Match.TRUE, value, "<basic-shape>#");
		assertMatch(Match.FALSE, value, "<length>");
		assertMatch(Match.FALSE, value, "<color>");
		assertMatch(Match.TRUE, value, "*");
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("clip: rect(2px 12em 3em 2pt); ");
		RectValue value = (RectValue) style.getPropertyCSSValue("clip");
		RectValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getTop(), clon.getTop());
		assertTrue(value.equals(clon));
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
