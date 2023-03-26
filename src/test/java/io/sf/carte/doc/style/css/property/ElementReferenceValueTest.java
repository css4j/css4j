/*

 Copyright (c) 2005-2023, Carlos Amengual.

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

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class ElementReferenceValueTest {

	@Test
	public void testEquals() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(#someId);");
		CSSTypedValue value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertTrue(value.equals(value));
		style.setCssText("background-image: element(#someId);");
		CSSTypedValue value2 = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("background-image: element(#otherId);");
		value2 = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testParse() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(#someId);");
		assertEquals("element(#someId)", style.getPropertyValue("background-image"));
		assertEquals("background-image: element(#someId); ", style.getCssText());
		assertEquals("background-image:element(#someId)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ELEMENT_REFERENCE, cssval.getPrimitiveType());
		assertEquals("element(#someId)", cssval.getCssText());
		assertEquals("someId", ((CSSTypedValue) cssval).getStringValue());
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testParseLexicalVarId() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(var(--myId));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("element(var(--myId))", cssval.getCssText());
		assertEquals("background-image:element(var(--myId))", style.getMinifiedCssText());

		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testParseLexicalAttrId() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(attr(data-id ident));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("element(attr(data-id ident))", cssval.getCssText());
		assertEquals("background-image:element(attr(data-id ident))", style.getMinifiedCssText());

		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testParseError() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(someId); ");
		assertNull(style.getPropertyCSSValue("background-image"));
	}

	@Test
	public void testSetCssText() {
		ElementReferenceValue value = new ElementReferenceValue();
		value.setCssText("element(#someId)");
		assertEquals("someId", value.getStringValue());
		assertEquals("element(#someId)", value.getCssText());
		assertEquals("element(#someId)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextError() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setCssText("foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError2() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setCssText("element(foo)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError3() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setCssText("element(#foo bar)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testSetStringValue() {
		ElementReferenceValue value = new ElementReferenceValue();
		value.setStringValue(Type.ELEMENT_REFERENCE, "Id");
		assertEquals("Id", value.getStringValue());
		assertEquals("element(#Id)", value.getCssText());
		assertEquals("element(#Id)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetStringValueError() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setStringValue(Type.STRING, "foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		//
		try {
			value.setStringValue(Type.ELEMENT_REFERENCE, "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("background-image: element(#someId); ");
		TypedValue value = (TypedValue) style.getPropertyCSSValue("background-image");
		assertNotNull(value);
		TypedValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	private static BaseCSSStyleDeclaration createStyleDeclaration() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		return (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

}
