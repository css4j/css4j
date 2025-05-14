/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;
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

class ShapeFunctionTest {

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
		style.setCssText("clip-path: polygon(0px 0px, 100px 50px, 0px 100px);");
		StyleValue value = style.getPropertyCSSValue("clip-path");
		assertTrue(value.equals(value));
		style.setCssText("clip-path: polygon(0px 0px, 100px 50px, 0px 100px);");
		StyleValue value2 = style.getPropertyCSSValue("clip-path");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());

		style.setCssText("clip-path: polygon(0px 0px, 100px 50px, 0px 100px, 50px 20px)");
		value2 = style.getPropertyCSSValue("clip-path");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testCircle() {
		style.setCssText("clip-path: circle(5vw at right center)");
		ShapeFunction val = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		assertNotNull(val);
		assertEquals(CSSValue.Type.CIRCLE, val.getPrimitiveType());
		assertEquals("circle(5vw at right center)", val.getCssText());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testEllipse() {
		style.setCssText("clip-path: ellipse(5em 50% at left center)");
		ShapeFunction val = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		assertNotNull(val);
		assertEquals(CSSValue.Type.ELLIPSE, val.getPrimitiveType());
		assertEquals("ellipse(5em 50% at left center)", val.getCssText());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testInset() {
		style.setCssText("clip-path: inset(25px 75px 15px 0 round 90px)");
		ShapeFunction val = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		assertNotNull(val);
		assertEquals(CSSValue.Type.INSET, val.getPrimitiveType());
		assertEquals("inset(25px 75px 15px 0 round 90px)", val.getCssText());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testPolygon() {
		style.setCssText("clip-path: polygon(0px 0px, 100px 50px, 0px 100px)");
		ShapeFunction val = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		assertNotNull(val);
		assertEquals(CSSValue.Type.POLYGON, val.getPrimitiveType());
		assertEquals("polygon(0px 0px, 100px 50px, 0px 100px)", val.getCssText());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testShape() {
		style.setCssText("clip-path: shape(nonzero from 0 0, line to 50em 80px)");
		ShapeFunction val = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		assertNotNull(val);
		assertEquals(CSSValue.Type.SHAPE, val.getPrimitiveType());
		assertEquals("shape(nonzero from 0 0, line to 50em 80px)", val.getCssText());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testXYWH() {
		style.setCssText("clip-path: xywh(0 2% 5px 6% round 0 3px 4% 7px)");
		ShapeFunction val = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		assertNotNull(val);
		assertEquals(CSSValue.Type.XYWH, val.getPrimitiveType());
		assertEquals("xywh(0 2% 5px 6% round 0 3px 4% 7px)", val.getCssText());

		assertMatch(Match.TRUE, val, "<basic-shape>");
		assertMatch(Match.TRUE, val, "<basic-shape>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("clip-path: polygon(0px 0px, 100px 50px, 0px 100px)");
		ShapeFunction value = (ShapeFunction) style.getPropertyCSSValue("clip-path");
		ShapeFunction clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
