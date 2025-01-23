/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class LinkedCSSValueListTest {

	private static ValueFactory factory;

	@BeforeAll
	public static void setUpBeforeClass() {
		factory = new ValueFactory();
	}

	@Test
	public void testIdentList() {
		LinkedCSSValueList list = new LinkedCSSValueList();
		assertEquals("", list.getCssText());
		assertEquals("", list.getMinifiedCssText(""));
		list.add(factory.parseProperty("thin"));
		list.add(factory.parseProperty("thick"));
		assertEquals("thin, thick", list.getCssText());
		assertEquals("thin,thick", list.getMinifiedCssText(""));
		assertEquals(CssType.LIST, list.getCssValueType());
		assertEquals(Type.INVALID, list.getPrimitiveType());
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.TRUE, list.matches(syn));
		syn = syntaxParser.parseSyntax("<color> | <custom-ident>#");
		assertEquals(Match.TRUE, list.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, list.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, list.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, list.matches(syn));
	}

	@Test
	public void testVarList() {
		LinkedCSSValueList list = new LinkedCSSValueList();
		list.add(factory.parseProperty("thin"));
		list.add(factory.parseProperty("thick"));
		list.add(factory.parseProperty("var(--foo, bar)"));
		assertEquals("thin, thick, var(--foo, bar)", list.getCssText());
		assertEquals("thin,thick,var(--foo,bar)", list.getMinifiedCssText(""));
		assertEquals(CssType.LIST, list.getCssValueType());
		assertEquals(Type.INVALID, list.getPrimitiveType());
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.PENDING, list.matches(syn));
		syn = syntaxParser.parseSyntax("<color> | <custom-ident>#");
		assertEquals(Match.PENDING, list.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, list.matches(syn));
		syn = syntaxParser.parseSyntax("<color> | <custom-ident>+");
		assertEquals(Match.FALSE, list.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, list.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, list.matches(syn));
	}

	@Test
	public void testClone() {
		LinkedCSSValueList list = new LinkedCSSValueList();
		list.add(factory.parseProperty("thin"));
		list.add(factory.parseProperty("thick"));
		LinkedCSSValueList clon = list.clone();
		assertEquals(list.getCssValueType(), clon.getCssValueType());
		assertEquals(list.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(list.getCssText(), clon.getCssText());
	}

}
