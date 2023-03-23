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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class AttrValueTest {

	@Test
	public void testSetCssTextString() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		assertEquals("title", value.getAttributeName());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title)", value.getCssText());
		assertEquals("attr(title)", value.getMinifiedCssText(""));

		final CSSValueSyntax syn = SyntaxParser.createSimpleSyntax("string");
		SyntaxParser synParser = new SyntaxParser();
		assertEquals(Match.TRUE, value.matches(syn));
		CSSValueSyntax synCI = synParser.parseSyntax("<custom-ident>");
		CSSValueSyntax synLengthP = synParser.parseSyntax("<length-percentage>");
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLengthP));
		CSSValueSyntax synUniversal = synParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(title string)");
		assertEquals("title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title string)", value.getCssText());
		assertEquals("attr(title string)", value.getMinifiedCssText(""));

		assertEquals(Match.TRUE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(title string, 'foo')");
		assertEquals("title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		assertEquals("foo", ((CSSTypedValue) value.getFallback()).getStringValue());
		assertEquals("attr(title string, 'foo')", value.getCssText());
		assertEquals("attr(title string,'foo')", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(data-id ident, none)");
		assertEquals("data-id", value.getAttributeName());
		assertEquals("ident", value.getAttributeType());
		assertEquals("none", ((CSSTypedValue) value.getFallback()).getStringValue());
		assertEquals("attr(data-id ident, none)", value.getCssText());
		assertEquals("attr(data-id ident,none)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width length, 20em)");
		assertEquals("width", value.getAttributeName());
		assertEquals("length", value.getAttributeType());
		assertEquals("20em", value.getFallback().getCssText());
		assertEquals("attr(width length, 20em)", value.getCssText());
		assertEquals("attr(width length,20em)", value.getMinifiedCssText(""));

		CSSValueSyntax synLength = synParser.parseSyntax("<length>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.TRUE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(width length)");
		assertEquals("width", value.getAttributeName());
		assertEquals("length", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width length)", value.getCssText());
		assertEquals("attr(width length)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width px, 20em)");
		assertEquals("width", value.getAttributeName());
		assertEquals("px", value.getAttributeType());
		assertEquals("20em", value.getFallback().getCssText());
		assertEquals("attr(width px, 20em)", value.getCssText());
		assertEquals("attr(width px,20em)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.TRUE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(width px)");
		assertEquals("width", value.getAttributeName());
		assertEquals("px", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width px)", value.getCssText());
		assertEquals("attr(width px)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.TRUE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(width percentage, 40%)");
		assertEquals("width", value.getAttributeName());
		assertEquals("percentage", value.getAttributeType());
		assertEquals("40%", value.getFallback().getCssText());
		assertEquals("attr(width percentage, 40%)", value.getCssText());
		assertEquals("attr(width percentage,40%)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(width percentage)");
		assertEquals("width", value.getAttributeName());
		assertEquals("percentage", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0%", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width percentage)", value.getCssText());
		assertEquals("attr(width percentage)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(width %)");
		assertEquals("width", value.getAttributeName());
		assertEquals("%", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("attr(width %)", value.getCssText());
		assertEquals("attr(width %)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(elev angle, 20deg)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("angle", value.getAttributeType());
		assertEquals("20deg", value.getFallback().getCssText());
		assertEquals("attr(elev angle, 20deg)", value.getCssText());
		assertEquals("attr(elev angle,20deg)", value.getMinifiedCssText(""));

		CSSValueSyntax synAngle = synParser.parseSyntax("<angle>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(elev angle)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("angle", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0deg", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(elev angle)", value.getCssText());
		assertEquals("attr(elev angle)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(elev deg, 20deg)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("deg", value.getAttributeType());
		assertEquals("20deg", value.getFallback().getCssText());
		assertEquals("attr(elev deg, 20deg)", value.getCssText());
		assertEquals("attr(elev deg,20deg)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(elev deg)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("deg", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0deg", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(elev deg)", value.getCssText());
		assertEquals("attr(elev deg)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(pause time, 2s)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("time", value.getAttributeType());
		assertEquals("2s", value.getFallback().getCssText());
		assertEquals("attr(pause time, 2s)", value.getCssText());
		assertEquals("attr(pause time,2s)", value.getMinifiedCssText(""));

		CSSValueSyntax synTime = synParser.parseSyntax("<time>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(pause time)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("time", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0s", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pause time)", value.getCssText());
		assertEquals("attr(pause time)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(pause s, 2s)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("s", value.getAttributeType());
		assertEquals("2s", value.getFallback().getCssText());
		assertEquals("attr(pause s, 2s)", value.getCssText());
		assertEquals("attr(pause s,2s)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pause s)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("s", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0s", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pause s)", value.getCssText());
		assertEquals("attr(pause s)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(pitch frequency, 200Hz)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("frequency", value.getAttributeType());
		assertEquals("200hz", value.getFallback().getCssText());
		assertEquals("attr(pitch frequency, 200hz)", value.getCssText());
		assertEquals("attr(pitch frequency,200hz)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pitch frequency)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("frequency", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0Hz", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pitch frequency)", value.getCssText());
		assertEquals("attr(pitch frequency)", value.getMinifiedCssText(""));

		CSSValueSyntax synFreq = synParser.parseSyntax("<frequency>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synFreq));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(pitch Hz, 200Hz)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("Hz", value.getAttributeType());
		assertEquals("200hz", value.getFallback().getCssText());
		assertEquals("attr(pitch Hz, 200hz)", value.getCssText());
		assertEquals("attr(pitch Hz,200hz)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pitch Hz)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("Hz", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0Hz", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pitch Hz)", value.getCssText());
		assertEquals("attr(pitch Hz)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.FALSE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synFreq));
		assertEquals(Match.TRUE, value.matches(synUniversal));
		//
		value.setCssText("attr(data-grid flex)");
		assertEquals("data-grid", value.getAttributeName());
		assertEquals("flex", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("attr(data-grid flex)", value.getCssText());
		assertEquals("attr(data-grid flex)", value.getMinifiedCssText(""));

		CSSValueSyntax synFlex = synParser.parseSyntax("<flex>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.FALSE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synFlex));
		assertEquals(Match.TRUE, value.matches(synUniversal));
	}

	@Test
	public void testSetCssTextStringError() {
		AttrValue value = new AttrValue((byte) 0);
		DOMException e = assertThrows(DOMException.class, () -> value.setCssText("attr()"));
		assertEquals(DOMException.SYNTAX_ERR, e.code);

		e = assertThrows(DOMException.class, () -> value.setCssText("attr( )"));
		assertEquals(DOMException.SYNTAX_ERR, e.code);

		e = assertThrows(DOMException.class, () -> value.setCssText("attr(-)"));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testSetCssTextStringErrorContainsAttr() {
		AttrValue value = new AttrValue((byte) 0);
		DOMException e = assertThrows(DOMException.class,
				() -> value.setCssText("attr(att-content string, attr(foo))"));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testSetCssTextStringErrorAttrInsideCalc() {
		AttrValue value = new AttrValue((byte) 0);
		DOMException e = assertThrows(DOMException.class,
				() -> value.setCssText("attr(att-content string, calc(attr(foo)/3))"));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testSetCssTextStringErrorAttrInsideSqrt() {
		AttrValue value = new AttrValue((byte) 0);
		DOMException e = assertThrows(DOMException.class,
				() -> value.setCssText("attr(att-content string, calc(sqrt(attr(foo)/3)))"));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testSetCssTextString2() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		assertEquals("title", value.getAttributeName());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeType() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-title string)");
		assertEquals("data-title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(data-title string)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeTypePercentage() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-pcnt percentage)");
		assertEquals("data-pcnt", value.getAttributeName());
		assertEquals("percentage", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0f, AttrValue.defaultFallback(value.getAttributeType()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-7f);
		assertEquals("attr(data-pcnt percentage)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallback() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-title string, \"My Title\")");
		assertEquals("data-title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSValue.Type.STRING, ((CSSTypedValue) fallback).getPrimitiveType());
		assertEquals("\"My Title\"", fallback.getCssText());
		assertEquals("attr(data-title string, \"My Title\")", value.getCssText());
		assertEquals("attr(data-title string,\"My Title\")", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackList() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-index integer, 1 2)");
		assertEquals("data-index", value.getAttributeName());
		assertEquals("integer", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSValue.CssType.LIST, fallback.getCssValueType());
		assertEquals("1 2", fallback.getCssText());
		assertEquals("attr(data-index integer, 1 2)", value.getCssText());
		assertEquals("attr(data-index integer,1 2)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackListComma() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-index integer, 1, 2)");
		assertEquals("data-index", value.getAttributeName());
		assertEquals("integer", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSValue.CssType.LIST, fallback.getCssValueType());
		assertEquals("1, 2", fallback.getCssText());
		assertEquals("attr(data-index integer, 1, 2)", value.getCssText());
		assertEquals("attr(data-index integer,1,2)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURL() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(myuri url,'https://www.example.com/foo')");
		assertEquals("myuri", value.getAttributeName());
		assertEquals("url", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSValue.Type.STRING, ((CSSTypedValue) fallback).getPrimitiveType());
		assertEquals("'https://www.example.com/foo'", fallback.getCssText());
		assertEquals("attr(myuri url, 'https://www.example.com/foo')", value.getCssText());
		assertEquals("attr(myuri url,'https://www.example.com/foo')", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURLBad() {
		AttrValue value = new AttrValue((byte) 0);
		try {
			value.setCssText("attr(myuri url,https://www.example.com/foo)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testParseAttr() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("margin-left:attr(leftmargin percentage)");
		StyleValue marginLeft = style.getPropertyCSSValue("margin-left");
		assertNotNull(marginLeft);
		assertEquals("attr(leftmargin percentage)", marginLeft.getCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParseFallbackCustomPropertyRecursiveAttr() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("margin-left:attr(noattr length,var(--foo,attr(noattr)))");
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertNull(style.getPropertyCSSValue("margin-left"));
	}

	@Test
	public void testMatch() {
		SyntaxParser syntaxParser = new SyntaxParser();
		AttrValue value = new AttrValue((byte) 0);
		//
		value.setCssText("attr(title)");
		CSSValueSyntax syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setCssText("attr(data-pcnt percentage)");
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <percentage>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <percentage>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setCssText("attr(data-width length, 'default')");
		syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setCssText("attr(data-width length, 8%)");
		syn = syntaxParser.parseSyntax("<percentage> | <length>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setCssText("attr(data-width percentage, 11px)");
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <length>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	private static BaseCSSStyleDeclaration createStyleDeclaration() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		return (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		AttrValue other = new AttrValue((byte) 0);
		other.setCssText("attr(title)");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("attr(href)");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testClone() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		AttrValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getAttributeName(), clon.getAttributeName());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
