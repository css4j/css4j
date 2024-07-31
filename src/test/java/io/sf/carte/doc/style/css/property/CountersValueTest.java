/*

 Copyright (c) 2005-2024, Carlos Amengual.

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

public class CountersValueTest {

	private CSSStyleDeclarationRule styleRule;
	private BaseCSSStyleDeclaration style;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("content: counters(ListCounter,'. ');");
		CountersValue value = (CountersValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value));
		style.setCssText("content: counters(ListCounter,'. ');");
		CountersValue value2 = (CountersValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counters(ListCounter,'. ', decimal);");
		value2 = (CountersValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counters(ListCounter);");
		value2 = (CountersValue) style.getPropertyCSSValue("content");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("content: counters(ListCounter,'|');");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("counters(ListCounter, '|')", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '|'); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'|')", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
		assertEquals("|", counter.getSeparator());
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testGetCssTextNone() {
		style.setCssText("content: counters(ListCounter,'. ',none);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("counters(ListCounter, '. ', none)", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '. ', none); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'. ',none)", style.getMinifiedCssText());

		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());

		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.IDENT, counterstyle.getPrimitiveType());
		assertEquals("none", ((TypedValue) counterstyle).getStringValue());
		assertEquals(". ", counter.getSeparator());
	}

	@Test
	public void testGetCssTextDefaultStyle() {
		style.setCssText("content: counters(ListCounter,'|', decimal);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("counters(ListCounter, '|')", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '|'); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'|')", style.getMinifiedCssText());

		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.IDENT, counterstyle.getPrimitiveType());
		assertEquals("decimal", ((TypedValue) counterstyle).getStringValue());
		assertEquals("|", counter.getSeparator());
	}

	@Test
	public void testGetCssTextSeparators() {
		style.setCssText("content: '(' counters(ListCounter,'.') ') ';");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CssType.LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(3, list.getLength());
		assertEquals("'('", list.item(0).getCssText());
		cssval = list.item(1);
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("'(' counters(ListCounter, '.') ') '", style.getPropertyValue("content"));
		assertEquals("content: '(' counters(ListCounter, '.') ') '; ", style.getCssText());
		assertEquals("content:'(' counters(ListCounter,'.') ') '", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
		assertEquals(".", counter.getSeparator());
	}

	@Test
	public void testGetCssTextSeparators2() {
		style.setCssText("content: counters(ListCounter,'.') ':';");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CssType.LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(2, list.getLength());
		cssval = list.item(0);
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("counters(ListCounter, '.') ':'", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '.') ':'; ", style.getCssText());
		assertEquals("content:counters(ListCounter,'.') ':'", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
		assertEquals(".", counter.getSeparator());
		assertEquals("':'", list.item(1).getCssText());
	}

	@Test
	public void testGetCssTextStyle() {
		style.setCssText("content: counters(ListCounter, '.', upper-latin);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("counters(ListCounter, '.', upper-latin)", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '.', upper-latin); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'.',upper-latin)", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.IDENT, counterstyle.getPrimitiveType());
		assertEquals("upper-latin", ((TypedValue) counterstyle).getStringValue());
		assertEquals(".", counter.getSeparator());
	}

	@Test
	public void testGetCssTextStyleSymbols() {
		style.setCssText(
				"content: counters(ListCounter, '.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7')); ");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTERS, cssval.getPrimitiveType());
		assertEquals("counters(ListCounter, '.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7'))",
				style.getPropertyValue("content"));
		assertEquals(
				"content: counters(ListCounter, '.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7')); ",
				style.getCssText());
		assertEquals(
				"content:counters(ListCounter,'.',symbols(cyclic '*' '\\2020' '\\2021' '\\A7'))",
				style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.FUNCTION, counterstyle.getPrimitiveType());
		assertEquals("symbols", ((TypedValue) counterstyle).getStringValue());
		assertEquals(".", counter.getSeparator());
	}

	@Test
	public void testWrongCounters() {
		style.setCssText("content: counters(1px, '.', upper-latin);");
		assertNull(style.getPropertyCSSValue("content"));
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testCounters_Var() {
		style.setCssText("content: counters(var(--myCounter),'. ');");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(var(--myCounter), '. ')", cssval.getCssText());
		assertEquals("counters(var(--myCounter),'. ')", cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounters_Var_Separator() {
		style.setCssText("content: counters(ListCounter,var(--mySeparator));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(ListCounter, var(--mySeparator))", cssval.getCssText());
		assertEquals("counters(ListCounter,var(--mySeparator))",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounters_Var_Symbols() {
		style.setCssText(
				"content: counters(ListCounter,'. ',symbols(var(--symbolType) '*' '†' '‡'));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(ListCounter, '. ', symbols(var(--symbolType) '*' '†' '‡'))",
				cssval.getCssText());
		assertEquals("counters(ListCounter,'. ',symbols(var(--symbolType) '*' '†' '‡'))",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounters_Var_CounterStyle() {
		style.setCssText("content: counters(ListCounter,'. ',var(--counterStyle));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(ListCounter, '. ', var(--counterStyle))", cssval.getCssText());
		assertEquals("counters(ListCounter,'. ',var(--counterStyle))",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounters_Attr() {
		style.setCssText("content: counters(attr(data-counter ident),'. ');");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(attr(data-counter ident), '. ')", cssval.getCssText());
		assertEquals("counters(attr(data-counter ident),'. ')",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounters_Attr_Separator() {
		style.setCssText("content: counters(ListCounter,attr(data-separator));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(ListCounter, attr(data-separator))", cssval.getCssText());
		assertEquals("counters(ListCounter,attr(data-separator))",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounters_Attr_CounterStyle() {
		style.setCssText("content: counters(ListCounter,'. ',attr(data-counter-style));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counters(ListCounter, '. ', attr(data-counter-style))", cssval.getCssText());
		assertEquals("counters(ListCounter,'. ',attr(data-counter-style))",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testClone() {
		style.setCssText("content: counters(ListCounter,'.', upper-latin);");
		CountersValue value = (CountersValue) style.getPropertyCSSValue("content");
		CountersValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testClone2() {
		style.setCssText(
				"content: counters(ListCounter,'.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7'));");
		CountersValue value = (CountersValue) style.getPropertyCSSValue("content");
		CountersValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertFalse(value.getCounterStyle() == clon.getCounterStyle());
		assertTrue(value.equals(clon));
	}

}
