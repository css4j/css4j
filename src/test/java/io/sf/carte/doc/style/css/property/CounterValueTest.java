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
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class CounterValueTest {

	private StyleRule styleRule;
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
		style.setCssText("content: counter(ListCounter, decimal);");
		CounterValue value = (CounterValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value));
		style.setCssText("content: counter(ListCounter, decimal);");
		CounterValue value2 = (CounterValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counter(ListCounter);");
		value2 = (CounterValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counter(ListCounter, upper-roman);");
		value2 = (CounterValue) style.getPropertyCSSValue("content");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testCounter() {
		style.setCssText("content: counter(ListCounter);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTER, cssval.getPrimitiveType());
		assertEquals("counter(ListCounter)", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter); ", style.getCssText());
		assertEquals("content:counter(ListCounter)", style.getMinifiedCssText());
		CounterValue counter = (CounterValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());

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
	public void testCounterStyleNone() {
		style.setCssText("content: counter(ListCounter, none);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTER, cssval.getPrimitiveType());
		assertEquals("counter(ListCounter, none)", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter, none); ", style.getCssText());
		assertEquals("content:counter(ListCounter,none)", style.getMinifiedCssText());
		CounterValue counter = (CounterValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue ctrstyle = counter.getCounterStyle();
		assertEquals(Type.IDENT, ctrstyle.getPrimitiveType());
		assertEquals("none", ((TypedValue) ctrstyle).getStringValue());

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testCounterStyleDecimal() {
		style.setCssText("content: counter(ListCounter, decimal);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTER, cssval.getPrimitiveType());
		assertEquals("counter(ListCounter)", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter); ", style.getCssText());
		assertEquals("content:counter(ListCounter)", style.getMinifiedCssText());
		CounterValue counter = (CounterValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.IDENT, counterstyle.getPrimitiveType());
		assertEquals("decimal", ((TypedValue) counterstyle).getStringValue());
	}

	@Test
	public void testCounterStyleSeparator() {
		style.setCssText("content: counter(ListCounter) '. ';");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CssType.LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(2, list.getLength());
		assertEquals("'. '", list.item(1).getCssText());
		cssval = list.item(0);
		assertEquals(CSSValue.Type.COUNTER, cssval.getPrimitiveType());
		assertEquals("counter(ListCounter) '. '", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter) '. '; ", style.getCssText());
		assertEquals("content:counter(ListCounter) '. '", style.getMinifiedCssText());
		CounterValue counter = (CounterValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
	}

	@Test
	public void testCounterStyleUpperLatin() {
		style.setCssText("content: counter(ListCounter, upper-latin);");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTER, cssval.getPrimitiveType());
		assertEquals("counter(ListCounter, upper-latin)", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter, upper-latin); ", style.getCssText());
		assertEquals("content:counter(ListCounter,upper-latin)", style.getMinifiedCssText());
		CounterValue counter = (CounterValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.IDENT, counterstyle.getPrimitiveType());
		assertEquals("upper-latin", ((TypedValue) counterstyle).getStringValue());
	}

	@Test
	public void testCounterStyleSymbols() {
		style.setCssText(
				"content: counter(ListCounter, symbols(cyclic '*' '\\2020' '\\2021' '\\A7')); ");
		StyleValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.COUNTER, cssval.getPrimitiveType());
		assertEquals("counter(ListCounter, symbols(cyclic '*' '\\2020' '\\2021' '\\A7'))",
				style.getPropertyValue("content"));
		assertEquals(
				"content: counter(ListCounter, symbols(cyclic '*' '\\2020' '\\2021' '\\A7')); ",
				style.getCssText());
		assertEquals("content:counter(ListCounter,symbols(cyclic '*' '\\2020' '\\2021' '\\A7'))",
				style.getMinifiedCssText());
		CounterValue counter = (CounterValue) cssval;
		assertEquals("ListCounter", counter.getName());
		PrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSValue.Type.FUNCTION, counterstyle.getPrimitiveType());
		assertEquals("symbols", ((TypedValue) counterstyle).getStringValue());
	}

	@Test
	public void testWrongCounter() {
		style.setCssText("content: counter(1px);");
		assertNull(style.getPropertyCSSValue("content"));
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testCounter_Var() {
		style.setCssText("content: counter(var(--myCounter), decimal);");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counter(var(--myCounter), decimal)", cssval.getCssText());
		assertEquals("counter(var(--myCounter),decimal)", cssval.getMinifiedCssText("content"));

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
	public void testCounter_Var2() {
		style.setCssText("content: counter(ListCounter, var(--myCounterStyle));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counter(ListCounter, var(--myCounterStyle))", cssval.getCssText());
		assertEquals("counter(ListCounter,var(--myCounterStyle))",
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
	public void testCounter_Var_Symbols() {
		style.setCssText("content: counter(ListCounter, symbols(var(--symbolType) '*' '†' '‡'));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counter(ListCounter, symbols(var(--symbolType) '*' '†' '‡'))",
				cssval.getCssText());
		assertEquals("counter(ListCounter,symbols(var(--symbolType) '*' '†' '‡'))",
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
	public void testCounter_Attr() {
		style.setCssText("content: counter(attr(data-counter type(<custom-ident>)), decimal);");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counter(attr(data-counter type(<custom-ident>)), decimal)", cssval.getCssText());
		assertEquals("counter(attr(data-counter type(<custom-ident>)),decimal)",
				cssval.getMinifiedCssText("content"));

		assertFalse(styleRule.getStyleDeclarationErrorHandler().hasErrors());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number> | <counter>");
		assertEquals(Match.TRUE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, cssval.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cssval.matches(syn));
	}

	@Test
	public void testCounter_Attr2() {
		style.setCssText("content: counter(ListCounter, attr(data-counter-style type(<custom-ident>)));");
		StyleValue cssval = style.getPropertyCSSValue("content");

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());

		assertEquals("counter(ListCounter, attr(data-counter-style type(<custom-ident>)))", cssval.getCssText());
		assertEquals("counter(ListCounter,attr(data-counter-style type(<custom-ident>)))",
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
		style.setCssText("content: counter(ListCounter, upper-latin);");
		CounterValue value = (CounterValue) style.getPropertyCSSValue("content");
		CounterValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testClone2() {
		style.setCssText(
				"content: counter(ListCounter, symbols(cyclic '*' '\\2020' '\\2021' '\\A7'));");
		CounterValue value = (CounterValue) style.getPropertyCSSValue("content");
		CounterValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertFalse(value.getCounterStyle() == clon.getCounterStyle());
		assertTrue(value.equals(clon));
	}

}
