/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class CustomPropertyValueTest {

	private BaseCSSStyleDeclaration style;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("foo: var(--my-identifier); ");
		VarValue value = (VarValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value));
		style.setCssText("foo: var(--my-identifier); ");
		VarValue value2 = (VarValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("foo: var(--My-identifier); ");
		value2 = (VarValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		style.setCssText("foo: var(--other-identifier); ");
		value2 = (VarValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("foo: var(--my-identifier); ");
		assertEquals("var(--my-identifier)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-identifier); ", style.getCssText());
		assertEquals("foo:var(--my-identifier)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.VAR, cssval.getPrimitiveType());
		VarValue val = (VarValue) cssval;
		assertEquals("var(--my-identifier)", val.getCssText());
		assertEquals("--my-identifier", val.getName());
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.PENDING, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
	}

	@Test
	public void testGetCssTextUpperCase() {
		style.setCssText("foo: var(--My-Identifier); ");
		assertEquals("var(--My-Identifier)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--My-Identifier); ", style.getCssText());
		assertEquals("foo:var(--My-Identifier)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.VAR, cssval.getPrimitiveType());
		VarValue val = (VarValue) cssval;
		assertEquals("var(--My-Identifier)", val.getCssText());
		assertEquals("--My-Identifier", val.getName());
	}

	@Test
	public void testGetCssTextFallback() {
		style.setCssText("foo: var(--my-identifier,#f0c); ");
		assertEquals("var(--my-identifier, #f0c)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-identifier, #f0c); ", style.getCssText());
		assertEquals("foo:var(--my-identifier,#f0c)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.VAR, cssval.getPrimitiveType());
		VarValue val = (VarValue) cssval;
		assertEquals("var(--my-identifier, #f0c)", val.getCssText());
		LexicalUnit fallback = val.getFallback();
		assertEquals(LexicalUnit.LexicalType.RGBCOLOR, fallback.getLexicalUnitType());
		assertEquals("#f0c", fallback.getCssText());
		assertEquals("--my-identifier", val.getName());
	}

	@Test
	public void testGetCssTextFallbackList() {
		style.setCssText("foo: var(--my-list, 1 2); ");
		assertEquals("var(--my-list, 1 2)", style.getPropertyValue("foo"));
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.VAR, cssval.getPrimitiveType());
		VarValue val = (VarValue) cssval;
		LexicalUnit fallback = val.getFallback();
		assertEquals(LexicalUnit.LexicalType.INTEGER, fallback.getLexicalUnitType());
		assertEquals("1", fallback.getCssText());
		assertEquals("1 2", fallback.toString());
	}

	@Test
	public void testGetCssTextFallbackCommas() {
		style.setCssText("foo: var(--my-list, 1, 2); ");
		assertEquals("var(--my-list, 1, 2)", style.getPropertyValue("foo"));
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.VAR, cssval.getPrimitiveType());
		VarValue val = (VarValue) cssval;
		LexicalUnit fallback = val.getFallback();
		assertEquals(LexicalUnit.LexicalType.INTEGER, fallback.getLexicalUnitType());
		assertEquals("1", fallback.getCssText());
		assertEquals("1, 2", fallback.toString());
	}

	@Test
	public void testGetCssTextFallbackVar() {
		/*
		 * "If there are any var() references in the fallback, substitute them as well."
		 */
		style.setCssText("foo: var(--my-color,var(--my-fb-color,#f0c)); ");
		assertEquals("var(--my-color, var(--my-fb-color, #f0c))", style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-color, var(--my-fb-color, #f0c)); ", style.getCssText());
		assertEquals("foo:var(--my-color,var(--my-fb-color,#f0c))", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.VAR, cssval.getPrimitiveType());
		VarValue val = (VarValue) cssval;
		assertEquals("--my-color", val.getName());
		assertEquals("var(--my-color, var(--my-fb-color, #f0c))", val.getCssText());
		LexicalUnit fallback = val.getFallback();
		assertEquals(LexicalUnit.LexicalType.VAR, fallback.getLexicalUnitType());
		assertEquals("var(--my-fb-color, #f0c)", fallback.getCssText());
	}

	@Test
	public void testSetCssText() {
		VarValue value = new VarValue();
		value.setCssText("var(--my-identifier)");
		assertEquals("var(--my-identifier)", value.getCssText());
		assertEquals("var(--my-identifier)", value.getMinifiedCssText(""));
		assertEquals("--my-identifier", value.getName());
		//
		value.setCssText("var(--my-identifier, #f0c)");
		assertEquals("var(--my-identifier, #f0c)", value.getCssText());
		assertEquals("var(--my-identifier,#f0c)", value.getMinifiedCssText(""));
		assertEquals("--my-identifier", value.getName());
		assertEquals("#f0c", value.getFallback().getCssText());
	}

	@Test
	public void testSetCssTextError() {
		VarValue value = new VarValue();
		try {
			value.setCssText("foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError2() {
		VarValue value = new VarValue();
		try {
			value.setCssText("var(--my-identifier, ;)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			value.setCssText("var(--my-identifier, ()");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			value.setCssText("var(--my-identifier, {)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			value.setCssText("var(--my-identifier, @)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextErrorPropertyName() {
		/*
		 * "The var() function can not be used as property names, selectors,
		 * or anything else besides property values."
		 */
		VarValue value = new VarValue();
		try {
			value.setCssText("var(var(--color-icon-name),#879093)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		style.setCssText("foo: var(--my-identifier); ");
		VarValue value = (VarValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		VarValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getName(), clon.getName());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getFallback(), clon.getFallback());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testClone2() {
		style.setCssText("foo: var(--my-identifier, auto); ");
		VarValue value = (VarValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		VarValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getName(), clon.getName());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getFallback(), clon.getFallback());
		assertTrue(value.equals(clon));
	}

}
