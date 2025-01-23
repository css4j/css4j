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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class UnicodeRangeValueTest {

	BaseCSSStyleDeclaration style;

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
		style.setCssText("unicode-range: U+402;");
		UnicodeRangeValue value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value));
		style.setCssText("unicode-range: U+402;");
		UnicodeRangeValue value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("unicode-range: U+403;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		// Wildcard
		style.setCssText("unicode-range: U+???;");
		value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value));
		style.setCssText("unicode-range: U+???;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("unicode-range: U+2??;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		// Range
		style.setCssText("unicode-range: U+22-28;");
		value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value));
		style.setCssText("unicode-range: U+22-28;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("unicode-range: U+22-26;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testWildcardRange() {
		style.setCssText("unicode-range: U+???;");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.UNICODE_RANGE, cssval.getPrimitiveType());
		assertEquals("unicode-range: U+???; ", style.getCssText());
		assertEquals("unicode-range:U+???", style.getMinifiedCssText());
		assertEquals("U+???", style.getPropertyValue("unicode-range"));
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testSetStringValueWildcard() {
		style.setCssText("unicode-range: U+???;");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		UnicodeRangeValue val = (UnicodeRangeValue) cssval;
		assertEquals("U+???", val.getCssText());
		TypedValue unicode = val.getValue();
		assertNotNull(unicode);
		assertEquals(CSSValue.Type.UNICODE_WILDCARD, unicode.getPrimitiveType());
		assertEquals("U+???", unicode.getCssText());
		assertEquals("???", unicode.getStringValue());
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
		//
		try {
			unicode.setStringValue(CSSValue.Type.UNICODE_WILDCARD, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			unicode.setStringValue(CSSValue.Type.UNICODE_WILDCARD, "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			unicode.setStringValue(CSSValue.Type.UNICODE_WILDCARD, "foo");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			unicode.setStringValue(CSSValue.Type.IDENT, "foo");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		unicode.setStringValue(CSSValue.Type.UNICODE_WILDCARD, "2??");
		assertEquals("U+2??", unicode.getCssText());
		assertEquals("2??", unicode.getStringValue());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("unicode-range: U+0027; ");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.UNICODE_RANGE, cssval.getPrimitiveType());
		assertEquals("U+27", style.getPropertyValue("unicode-range"));
		assertEquals("unicode-range: U+27; ", style.getCssText());
		assertEquals("unicode-range:U+27", style.getMinifiedCssText());
		UnicodeRangeValue val = (UnicodeRangeValue) cssval;
		assertEquals("U+27", val.getCssText());
		try {
			val.setStringValue(CSSValue.Type.UNICODE_WILDCARD, "??");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		try {
			val.setStringValue(CSSValue.Type.UNICODE_RANGE, "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("unicode-range: U+0025-00FF; ");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.UNICODE_RANGE, cssval.getPrimitiveType());
		assertEquals("U+25-ff", style.getPropertyValue("unicode-range"));
		assertEquals("unicode-range: U+25-ff; ", style.getCssText());
		assertEquals("unicode-range:U+25-ff", style.getMinifiedCssText());
		UnicodeRangeValue val = (UnicodeRangeValue) cssval;
		assertEquals("U+25-ff", val.getCssText());
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("unicode-range: U+0025-00FF; ");
		UnicodeRangeValue value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertNotNull(value);
		UnicodeRangeValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
