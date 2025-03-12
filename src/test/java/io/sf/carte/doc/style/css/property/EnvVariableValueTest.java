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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSEnvVariableValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class EnvVariableValueTest {

	private static SyntaxParser syntaxParser;

	private BaseCSSStyleDeclaration style;

	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("foo: env(safe-area-inset-left); ");
		CSSEnvVariableValue value = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value));

		style.setCssText("foo: env(safe-area-inset-left); ");
		CSSEnvVariableValue value2 = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());

		style.setCssText("foo: env(safe-area-inset-left 1); ");
		value2 = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());

		style.setCssText("foo: env(safe-area-inset-left, 8px); ");
		value2 = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());

		style.setCssText("foo: env(safe-area-inset-right); ");
		value2 = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testInsetLeft() {
		style.setCssText("foo: env(safe-area-inset-left); ");
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(safe-area-inset-left)", val.getCssText());
		assertEquals("env(safe-area-inset-left)", val.getMinifiedCssText());
		assertEquals("safe-area-inset-left", val.getName());
		assertNull(val.getFallback());

		// Syntax matching
		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testIndicesFallback() {
		style.setCssText("foo: env(safe-area-inset-left 1 5, 8px); ");
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(safe-area-inset-left 1 5, 8px)", val.getCssText());
		assertEquals("env(safe-area-inset-left 1 5,8px)", val.getMinifiedCssText());
		assertEquals("safe-area-inset-left", val.getName());

		int[] indices = val.getIndices();
		assertNotNull(indices);
		assertEquals(2, indices.length);
		assertEquals(1, indices[0]);
		assertEquals(5, indices[1]);

		LexicalUnit fb = val.getFallback();
		assertNotNull(fb);
		assertEquals(LexicalType.DIMENSION, fb.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, fb.getCssUnit());
		assertEquals("8px", fb.getCssText());

		// Syntax matching
		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("foo: env(safe-area-inset-left, 1px); ");
		assertEquals("env(safe-area-inset-left, 1px)", style.getPropertyValue("foo"));
		assertEquals("foo: env(safe-area-inset-left, 1px); ", style.getCssText());
		assertEquals("foo:env(safe-area-inset-left,1px)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(safe-area-inset-left, 1px)", val.getCssText());
		assertEquals("safe-area-inset-left", val.getName());

		LexicalUnit fb = val.getFallback();
		assertNotNull(fb);
		assertEquals(LexicalType.DIMENSION, fb.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, fb.getCssUnit());
		assertEquals("1px", fb.getCssText());

		// Syntax matching
		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testUnknown() {
		style.setCssText("foo: env(foo-bar); ");
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(foo-bar)", val.getCssText());
		assertEquals("env(foo-bar)", val.getMinifiedCssText());
		assertEquals("foo-bar", val.getName());
		assertNull(val.getFallback());

		// Syntax matching
		assertMatch(Match.PENDING, val, "<length>");
		assertMatch(Match.PENDING, val, "<number>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testUnknownFallback() {
		style.setCssText("foo: env(foo-bar, #000); ");
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(foo-bar, #000)", val.getCssText());
		assertEquals("env(foo-bar,#000)", val.getMinifiedCssText());
		assertEquals("foo-bar", val.getName());
		assertEquals("#000", val.getFallback().getCssText());

		// Syntax matching
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.TRUE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssText() {
		EnvVariableValue value = new EnvVariableValue();
		value.setCssText("env(safe-area-inset-left, 1px)");
		assertEquals("env(safe-area-inset-left, 1px)", value.getCssText());
		assertEquals("env(safe-area-inset-left,1px)", value.getMinifiedCssText(""));
		assertEquals("safe-area-inset-left", value.getName());
		assertNotNull(value.getFallback());
		assertEquals("1px", value.getFallback().getCssText());

		value.setCssText("env(safe-area-inset-left)");
		assertEquals("env(safe-area-inset-left)", value.getCssText());
		assertEquals("env(safe-area-inset-left)", value.getMinifiedCssText(""));
		assertEquals("safe-area-inset-left", value.getName());
		assertNull(value.getFallback());
	}

	@Test
	public void testSetCssTextError() {
		EnvVariableValue value = new EnvVariableValue();
		try {
			value.setCssText("foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("foo: env(safe-area-inset-left 2 3, 1px); ");
		EnvVariableValue value = (EnvVariableValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		EnvVariableValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getName(), clon.getName());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.getFallback().equals(clon.getFallback()));
		assertTrue(Arrays.equals(value.getIndices(), clon.getIndices()));
		assertTrue(value.equals(clon));
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
