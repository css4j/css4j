/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class LexicalValueTest {

	@Test
	public void testGetFinalType() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("1em");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1%");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1.1");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("foo");
		assertEquals(Type.IDENT, value.getFinalType());
		//
		value.setCssText("'foo'");
		assertEquals(Type.STRING, value.getFinalType());
		//
		value.setCssText("1em / 1");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("16 / 9");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("16 / calc(3*2)");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("calc(4*4) / 9");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("16 / foo");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("foo / 9");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("rgb(var(--foo) 0 0.3)");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("hsl(var(--foo) 0% 0.3%)");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("hwb(var(--foo) 0% 0.3%)");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("color(display-p3 0.584 var(--foo))");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("color(display-p3 var(--foo))");
		assertEquals(Type.COLOR, value.getFinalType());
	}

	@Test
	public void testEquals() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertTrue(value.equals(value));
		LexicalValue value2 = new LexicalValue();
		value2.setCssText("1em 2em");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		value2.setCssText("1em 2px");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertEquals("1em 2em", value.getCssText());
		assertEquals("1em 2em", value.getMinifiedCssText(null));
		//
		value.setCssText("1.0em,2.1em");
		assertEquals("1em, 2.1em", value.getCssText());
		assertEquals("1em,2.1em", value.getMinifiedCssText(null));
		//
		value.setCssText("foo(1em,2em),bar(1 2)");
		assertEquals("foo(1em, 2em), bar(1 2)", value.getCssText());
		assertEquals("foo(1em,2em),bar(1 2)", value.getMinifiedCssText(null));
		//
		value.setCssText("calc(1 + 2) calc(1 * 2)");
		assertEquals("calc(1 + 2) calc(1*2)", value.getCssText());
		assertEquals("calc(1 + 2) calc(1*2)", value.getMinifiedCssText(null));
		//
		value.setCssText("url('http://example.com/')");
		assertEquals("url('http://example.com/')", value.getCssText());
		assertEquals("url('http://example.com/')", value.getMinifiedCssText(null));
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	@Test
	public void testGetCssTextEmpty() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("--foo:");
		StyleValue cssval = style.getPropertyCSSValue("--foo");
		assertEquals("", cssval.getCssText());
		assertEquals("", style.getPropertyValue("height"));
		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("--foo: ;\n", style.getCssText());
		assertEquals("--foo:", style.getMinifiedCssText());
	}

	@Test
	public void testGetCssTextEmpty2() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("--foo:;");
		StyleValue cssval = style.getPropertyCSSValue("--foo");
		assertEquals("", cssval.getCssText());
		assertEquals("", style.getPropertyValue("height"));
		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("--foo: ;\n", style.getCssText());
		assertEquals("--foo:", style.getMinifiedCssText());
	}

	@Test
	public void testClone() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 1px");
		LexicalValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
