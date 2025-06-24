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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class LexicalAttrTest {

	@Test
	public void testSetCssTextString() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(title)");
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

		value.setCssText("attr(title string)");
		assertEquals("attr(title string)", value.getCssText());
		assertEquals("attr(title string)", value.getMinifiedCssText(""));

		assertEquals(Match.TRUE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(title string, 'foo')");
		assertEquals("attr(title string, 'foo')", value.getCssText());
		assertEquals("attr(title string,'foo')", value.getMinifiedCssText(""));

		value.setCssText("attr(data-id type(<custom-ident>), none)");
		assertEquals("attr(data-id type(<custom-ident>), none)", value.getCssText());
		assertEquals("attr(data-id type(<custom-ident>),none)", value.getMinifiedCssText(""));

		value.setCssText("attr(width type(<length>), 20em)");
		assertEquals("attr(width type(<length>), 20em)", value.getCssText());
		assertEquals("attr(width type(<length>),20em)", value.getMinifiedCssText(""));

		CSSValueSyntax synLength = synParser.parseSyntax("<length>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.TRUE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(width type(<length>))");
		assertEquals("attr(width type(<length>))", value.getCssText());
		assertEquals("attr(width type(<length>))", value.getMinifiedCssText(""));

		value.setCssText("attr(width px, 20em)");
		assertEquals("attr(width px, 20em)", value.getCssText());
		assertEquals("attr(width px,20em)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.TRUE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(width px)");
		assertEquals("attr(width px)", value.getCssText());
		assertEquals("attr(width px)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.TRUE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(width type(<percentage>), 40%)");
		assertEquals("attr(width type(<percentage>), 40%)", value.getCssText());
		assertEquals("attr(width type(<percentage>),40%)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(width type(<percentage>))");
		assertEquals("attr(width type(<percentage>))", value.getCssText());
		assertEquals("attr(width type(<percentage>))", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(width %)");
		assertEquals("attr(width %)", value.getCssText());
		assertEquals("attr(width %)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synLengthP));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(elev type(<angle>), 20deg)");
		assertEquals("attr(elev type(<angle>), 20deg)", value.getCssText());
		assertEquals("attr(elev type(<angle>),20deg)", value.getMinifiedCssText(""));

		CSSValueSyntax synAngle = synParser.parseSyntax("<angle>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(elev type(<angle>))");
		assertEquals("attr(elev type(<angle>))", value.getCssText());
		assertEquals("attr(elev type(<angle>))", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(elev deg, 20deg)");
		assertEquals("attr(elev deg, 20deg)", value.getCssText());
		assertEquals("attr(elev deg,20deg)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(elev deg)");
		assertEquals("attr(elev deg)", value.getCssText());
		assertEquals("attr(elev deg)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synAngle));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(pause type(<time>), 2s)");
		assertEquals("attr(pause type(<time>), 2s)", value.getCssText());
		assertEquals("attr(pause type(<time>),2s)", value.getMinifiedCssText(""));

		CSSValueSyntax synTime = synParser.parseSyntax("<time>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(pause type(<time>))");
		assertEquals("attr(pause type(<time>))", value.getCssText());
		assertEquals("attr(pause type(<time>))", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(pause s, 2s)");
		assertEquals("attr(pause s, 2s)", value.getCssText());
		assertEquals("attr(pause s,2s)", value.getMinifiedCssText(""));

		value.setCssText("attr(pause s)");
		assertEquals("attr(pause s)", value.getCssText());
		assertEquals("attr(pause s)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(pitch type(<frequency>), 200Hz)");
		assertEquals("attr(pitch type(<frequency>), 200hz)", value.getCssText());
		assertEquals("attr(pitch type(<frequency>),200hz)", value.getMinifiedCssText(""));

		value.setCssText("attr(pitch type(<frequency>))");
		assertEquals("attr(pitch type(<frequency>))", value.getCssText());
		assertEquals("attr(pitch type(<frequency>))", value.getMinifiedCssText(""));

		CSSValueSyntax synFreq = synParser.parseSyntax("<frequency>");

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.TRUE, value.matches(synFreq));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(pitch Hz, 200Hz)");
		assertEquals("attr(pitch Hz, 200hz)", value.getCssText());
		assertEquals("attr(pitch Hz,200hz)", value.getMinifiedCssText(""));

		value.setCssText("attr(pitch Hz)");
		assertEquals("attr(pitch Hz)", value.getCssText());
		assertEquals("attr(pitch Hz)", value.getMinifiedCssText(""));

		assertEquals(Match.FALSE, value.matches(syn));
		assertEquals(Match.FALSE, value.matches(synCI));
		assertEquals(Match.FALSE, value.matches(synLength));
		assertEquals(Match.FALSE, value.matches(synTime));
		assertEquals(Match.TRUE, value.matches(synFreq));
		assertEquals(Match.TRUE, value.matches(synUniversal));

		value.setCssText("attr(data-grid type(<flex>))");
		assertEquals("attr(data-grid type(<flex>))", value.getCssText());
		assertEquals("attr(data-grid type(<flex>))", value.getMinifiedCssText(""));

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
		LexicalValue value = new LexicalValue();
		DOMException e = assertThrows(DOMException.class, () -> value.setCssText("attr()"));
		assertEquals(DOMException.SYNTAX_ERR, e.code);

		e = assertThrows(DOMException.class, () -> value.setCssText("attr( )"));
		assertEquals(DOMException.SYNTAX_ERR, e.code);

		e = assertThrows(DOMException.class, () -> value.setCssText("attr(-)"));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testSetCssTextString2() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(title)");
		assertEquals("attr(title)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeType() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(data-title string)");
		assertEquals("attr(data-title string)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeTypePercentage() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(data-pcnt type(<percentage>))");
		assertEquals("attr(data-pcnt type(<percentage>))", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallback() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(data-title string, \"My Title\")");
		assertEquals("attr(data-title string, \"My Title\")", value.getCssText());
		assertEquals("attr(data-title string,\"My Title\")", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackList() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(data-index type(<integer>), 1 2)");
		assertEquals("attr(data-index type(<integer>), 1 2)", value.getCssText());
		assertEquals("attr(data-index type(<integer>),1 2)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackListComma() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(data-index type(<integer>), 1, 2)");
		assertEquals("attr(data-index type(<integer>), 1, 2)", value.getCssText());
		assertEquals("attr(data-index type(<integer>),1,2)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURL() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(myuri type(<url>),'https://www.example.com/foo')");
		assertEquals("attr(myuri type(<url>), 'https://www.example.com/foo')", value.getCssText());
		assertEquals("attr(myuri type(<url>),'https://www.example.com/foo')", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURLBad() {
		LexicalValue value = new LexicalValue();
		try {
			value.setCssText("attr(myuri type(<url>),https://www.example.com/foo)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testParseAttr() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("margin-left:attr(leftmargin type(<percentage>))");
		StyleValue marginLeft = style.getPropertyCSSValue("margin-left");
		assertNotNull(marginLeft);
		assertEquals("attr(leftmargin type(<percentage>))", marginLeft.getCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testMatch() {
		SyntaxParser syntaxParser = new SyntaxParser();
		LexicalValue value = new LexicalValue();

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

		value.setCssText("attr(data-pcnt type(<percentage>))");
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

		value.setCssText("attr(data-width type(<length>), 'default')");
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

		value.setCssText("attr(data-width type(<length>), 8%)");
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

		value.setCssText("attr(data-width type(<percentage>), 11px)");
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
		StyleRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		return (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		LexicalValue value = new LexicalValue();
		value.setCssText("attr(title)");
		LexicalValue other = new LexicalValue();
		other.setCssText("attr(title)");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("attr(href)");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
	}

}
