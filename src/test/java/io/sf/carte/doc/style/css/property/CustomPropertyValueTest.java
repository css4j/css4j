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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class CustomPropertyValueTest {

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
		StyleRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("foo: var(--my-identifier); ");
		LexicalValue value = (LexicalValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value));
		style.setCssText("foo: var(--my-identifier); ");
		LexicalValue value2 = (LexicalValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("foo: var(--My-identifier); ");
		value2 = (LexicalValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		style.setCssText("foo: var(--other-identifier); ");
		value2 = (LexicalValue) style.getPropertyCSSValue("foo");
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
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals("var(--my-identifier)", val.getCssText());
		assertEquals("--my-identifier", val.getLexicalUnit().getParameters().getCssText());
		// Syntax matching
		assertMatch(Match.PENDING, val, "<custom-ident>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testGetCssTextUpperCase() {
		style.setCssText("foo: Var(--My-Identifier); ");
		assertEquals("var(--My-Identifier)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--My-Identifier); ", style.getCssText());
		assertEquals("foo:var(--My-Identifier)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals("var(--My-Identifier)", val.getCssText());
		assertEquals("--My-Identifier", val.getLexicalUnit().getParameters().getCssText());
	}

	@Test
	public void testGetCssTextFallback() {
		style.setCssText("foo: var(--my-identifier,#f0c); ");
		assertEquals("var(--my-identifier, #f0c)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-identifier, #f0c); ", style.getCssText());
		assertEquals("foo:var(--my-identifier,#f0c)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals("var(--my-identifier, #f0c)", val.getCssText());
		LexicalUnit fallback = val.getLexicalUnit().getParameters().getNextLexicalUnit()
				.getNextLexicalUnit();
		assertEquals(LexicalUnit.LexicalType.RGBCOLOR, fallback.getLexicalUnitType());
		assertEquals("#f0c", fallback.getCssText());
		assertEquals("--my-identifier", val.getLexicalUnit().getParameters().getCssText());
	}

	@Test
	public void testGetCssTextFallbackList() {
		style.setCssText("foo: var(--my-list, 1 2); ");
		assertEquals("var(--my-list, 1 2)", style.getPropertyValue("foo"));
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		LexicalValue val = (LexicalValue) cssval;
		LexicalUnit fallback = val.getLexicalUnit().getParameters().getNextLexicalUnit()
				.getNextLexicalUnit();
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
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		LexicalValue val = (LexicalValue) cssval;
		LexicalUnit fallback = val.getLexicalUnit().getParameters().getNextLexicalUnit()
				.getNextLexicalUnit();
		assertEquals(LexicalUnit.LexicalType.INTEGER, fallback.getLexicalUnitType());
		assertEquals("1", fallback.getCssText());
		assertEquals("1, 2", fallback.toString());
	}

	@Test
	public void testGetCssTextFallbackvar() {
		/*
		 * "If there are any var() references in the fallback, substitute them as well."
		 */
		style.setCssText("foo: var(--my-color,var(--my-fb-color,#f0c)); ");
		assertEquals("var(--my-color, var(--my-fb-color, #f0c))",
				style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-color, var(--my-fb-color, #f0c)); ",
				style.getCssText());
		assertEquals("foo:var(--my-color,var(--my-fb-color,#f0c))",
				style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals("--my-color", val.getLexicalUnit().getParameters().getCssText());
		assertEquals("var(--my-color, var(--my-fb-color, #f0c))", val.getCssText());
		LexicalUnit fallback = val.getLexicalUnit().getParameters().getNextLexicalUnit()
				.getNextLexicalUnit();
		assertEquals(LexicalUnit.LexicalType.VAR, fallback.getLexicalUnitType());
		assertEquals("var(--my-fb-color, #f0c)", fallback.getCssText());
	}

	@Test
	public void testSetCssText() {
		LexicalValue value = new LexicalValue();
		value.setCssText("var(--my-identifier)");
		assertEquals("var(--my-identifier)", value.getCssText());
		assertEquals("var(--my-identifier)", value.getMinifiedCssText(""));
		assertEquals("--my-identifier", value.getLexicalUnit().getParameters().getCssText());

		value.setCssText("var(--my-identifier, #f0c)");
		assertEquals("var(--my-identifier, #f0c)", value.getCssText());
		assertEquals("var(--my-identifier,#f0c)", value.getMinifiedCssText(""));
		assertEquals("--my-identifier", value.getLexicalUnit().getParameters().getCssText());
		assertEquals("#f0c", value.getLexicalUnit().getParameters().getNextLexicalUnit()
				.getNextLexicalUnit().getCssText());
	}

	@Test
	public void testSetCssTextError() {
		LexicalValue value = new LexicalValue();
		try {
			value.setCssText("var(");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError2() {
		LexicalValue value = new LexicalValue();
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
		 * "The var() function can not be used as property names, selectors, or
		 * anything else besides property values."
		 */
		LexicalValue value = new LexicalValue();
		try {
			value.setCssText("var(var(--color-icon-name),#879093)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		style.setCssText("foo: var(--my-identifier); ");
		LexicalValue value = (LexicalValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		LexicalValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getLexicalUnit(), clon.getLexicalUnit());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testClone2() {
		style.setCssText("foo: var(--my-identifier, auto); ");
		LexicalValue value = (LexicalValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		LexicalValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getLexicalUnit(), clon.getLexicalUnit());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
