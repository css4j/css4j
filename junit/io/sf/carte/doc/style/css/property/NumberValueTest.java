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

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class NumberValueTest {

	@Test
	public void testVarious() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSUnit.CSS_PX, 5f);
		assertEquals("5px", val.getCssText());
		assertEquals("5px", val.getMinifiedCssText(null));
		assertEquals(CSSUnit.CSS_PX, val.getUnitType());
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertFalse(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_NUMBER, 5f);
		assertEquals("5", val.getCssText());
		assertEquals("5", val.getMinifiedCssText(null));
		assertEquals(CSSUnit.CSS_NUMBER, val.getUnitType());
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertFalse(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_PX, 0f);
		assertEquals("0px", val.getCssText());
		assertEquals("0", val.getMinifiedCssText(null));
		assertEquals(CSSUnit.CSS_PX, val.getUnitType());
		assertFalse(val.isCalculatedNumber());
		assertTrue(val.isNumberZero());
		assertFalse(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_PX, -5f);
		assertEquals("-5px", val.getCssText());
		assertEquals("-5px", val.getMinifiedCssText(null));
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertTrue(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_PX, 0.1f);
		assertEquals("0.1px", val.getCssText());
		assertEquals(".1px", val.getMinifiedCssText(null));
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertFalse(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_PX, -0.1f);
		assertEquals("-0.1px", val.getCssText());
		assertEquals("-.1px", val.getMinifiedCssText(null));
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertTrue(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_NUMBER, 0.1f);
		assertEquals("0.1", val.getCssText());
		assertEquals(".1", val.getMinifiedCssText(null));
		assertEquals(CSSUnit.CSS_NUMBER, val.getUnitType());
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertFalse(val.isNegativeNumber());
		//
		val.setFloatValue(CSSUnit.CSS_NUMBER, -0.1f);
		assertEquals("-0.1", val.getCssText());
		assertEquals("-.1", val.getMinifiedCssText(null));
		assertEquals(CSSUnit.CSS_NUMBER, val.getUnitType());
		assertFalse(val.isCalculatedNumber());
		assertFalse(val.isNumberZero());
		assertTrue(val.isNegativeNumber());
	}

	@Test
	public void testGetCssTextPercentage() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 35f);
		assertEquals("35%", val.getCssText());
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 5f);
		assertEquals("5%", val.getCssText());
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 5.6f);
		assertEquals("5.6%", val.getCssText());
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 0.01f);
		assertEquals("0.01%", val.getCssText());
	}

	@Test
	public void testGetMinifiedCssTextPercentage() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 35f);
		assertEquals("35%", val.getMinifiedCssText(null));
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 5f);
		assertEquals("5%", val.getMinifiedCssText(null));
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 5.6f);
		assertEquals("5.6%", val.getMinifiedCssText(null));
		val.setFloatValue(CSSUnit.CSS_PERCENTAGE, 0.01f);
		assertEquals(".01%", val.getMinifiedCssText(null));
	}

	@Test
	public void testGetCssText() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("height: 10vh");
		StyleValue cssval = style.getPropertyCSSValue("height");
		assertEquals("10vh", cssval.getCssText());
		assertEquals("10vh", style.getPropertyValue("height"));
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(Type.NUMERIC, cssval.getPrimitiveType());
		assertEquals(CSSUnit.CSS_VH, ((TypedValue) cssval).getUnitType());
		//
		style.setCssText("height: 5px");
		assertEquals("5px", style.getPropertyValue("height"));
		assertEquals("5px", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		//
		style.setCssText("height: 5.666667px");
		assertEquals("5.666667px", style.getPropertyValue("height"));
		assertEquals("5.666667px", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		//
		style.setCssText("margin-left: -5px");
		assertEquals("-5px", style.getPropertyValue("margin-left"));
		assertEquals("-5px", style.getPropertyCSSValue("margin-left").getMinifiedCssText(""));
		//
		style.setCssText("line-height: 5");
		assertEquals("5", style.getPropertyValue("line-height"));
		cssval = style.getPropertyCSSValue("line-height");
		assertEquals("5", cssval.getMinifiedCssText(""));
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(Type.NUMERIC, cssval.getPrimitiveType());
		assertEquals(CSSUnit.CSS_NUMBER, ((TypedValue) cssval).getUnitType());
		//
		style.setCssText("line-height: -5");
		assertEquals("-5", style.getPropertyValue("line-height"));
		assertEquals("-5", style.getPropertyCSSValue("line-height").getMinifiedCssText(""));
		//
		style.setCssText("height: 0px");
		assertEquals("0px", style.getPropertyValue("height"));
		assertEquals("0", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		//
		style.setCssText("height: 0%");
		assertEquals("0%", style.getPropertyValue("height"));
		cssval = style.getPropertyCSSValue("height");
		assertEquals("0%", cssval.getMinifiedCssText(""));
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(Type.NUMERIC, cssval.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, ((TypedValue) cssval).getUnitType());
		//
		style.setCssText("height: 10%");
		assertEquals("10%", style.getPropertyValue("height"));
		cssval = style.getPropertyCSSValue("height");
		assertEquals("10%", cssval.getMinifiedCssText(""));
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(Type.NUMERIC, cssval.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, ((TypedValue) cssval).getUnitType());
		//
		style.setCssText("resolution: 300dpi");
		assertEquals("300dpi", style.getPropertyValue("resolution"));
		StyleValue value = style.getPropertyCSSValue("resolution");
		assertEquals(Type.NUMERIC, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_DPI, ((CSSTypedValue) value).getUnitType());
		assertEquals("300dpi", value.getMinifiedCssText(""));
	}

	@Test
	public void testGetCssTextInfinite() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSUnit.CSS_NUMBER, Float.POSITIVE_INFINITY);
		assertEquals("calc(1/0)", val.getCssText());
		assertEquals("calc(1/0)", val.getMinifiedCssText(""));
		val.setFloatValue(CSSUnit.CSS_NUMBER, Float.NEGATIVE_INFINITY);
		assertEquals("calc(-1/0)", val.getCssText());
		assertEquals("calc(-1/0)", val.getMinifiedCssText(""));
	}

	@Test
	public void testSetFloatValuePt() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSUnit.CSS_EM, 2f);
		assertEquals("2em", val.getCssText());
		val.setFloatValuePt(5f);
		assertEquals("5pt", val.getCssText());
	}

	@Test
	public void testMatch() {
		SyntaxParser syntaxParser = new SyntaxParser();
		NumberValue value = NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 15f);
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setFloatValue(CSSUnit.CSS_DEG, 15f);
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <angle>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setFloatValue(CSSUnit.CSS_HZ, 50f);
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <frequency>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setFloatValue(CSSUnit.CSS_S, 2.1f);
		syn = syntaxParser.parseSyntax("<time>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<time>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<time>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setFloatValue(CSSUnit.CSS_FR, 2.1f);
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<time>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <flex>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		value.setIntegerValue(1);
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <integer>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <integer>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	@Test
	public void testEquals() {
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_PX, 5f);
		NumberValue other = new NumberValue();
		other.setFloatValue(CSSUnit.CSS_PX, 5.2f);
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		other.setFloatValue(CSSUnit.CSS_PX, 5f);
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setFloatValue(CSSUnit.CSS_PERCENTAGE, 5f);
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		value.setFloatValue(CSSUnit.CSS_NUMBER, 0);
		other.setFloatValue(CSSUnit.CSS_NUMBER, 0);
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		value.setFloatValue(CSSUnit.CSS_NUMBER, 0);
		other.setFloatValue(CSSUnit.CSS_PX, 0);
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
	}

	@Test
	public void testEquals2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("left: 5.33333333%; ");
		StyleValue value = style.getPropertyCSSValue("left");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.NUMERIC, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, ((CSSTypedValue) value).getUnitType());
		style.setCssText("left: 5.33333333%; ");
		StyleValue other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		style.setCssText(style.getCssText());
		other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		style.setCssText("left: 5.333%; ");
		other = style.getPropertyCSSValue("left");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testEquals3() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("left: 2.6666666667px; ");
		StyleValue value = style.getPropertyCSSValue("left");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.NUMERIC, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PX, ((CSSTypedValue) value).getUnitType());
		style.setCssText("left: 2.6666666667px; ");
		StyleValue other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		style.setCssText(style.getCssText());
		other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		style.setCssText("left: 2.667px; ");
		other = style.getPropertyCSSValue("left");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		style.setCssText("left: 2.6666666667pt; ");
		other = style.getPropertyCSSValue("left");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testEquals4() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("left: 0px; ");
		StyleValue value = style.getPropertyCSSValue("left");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.NUMERIC, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PX, ((CSSTypedValue) value).getUnitType());
		style.setCssText("left: 0px; ");
		StyleValue other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		style.setCssText(style.getCssText());
		other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		style.setCssText("left: 0.00001px; ");
		other = style.getPropertyCSSValue("left");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		style.setCssText("left: 0; ");
		other = style.getPropertyCSSValue("left");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
	}

	@Test
	public void testClone() {
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_PX, 5f);
		NumberValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getFloatValue(CSSUnit.CSS_PX), clon.getFloatValue(CSSUnit.CSS_PX), 1e-8f);
		assertEquals(value.getCssText(), clon.getCssText());
		assertFalse(clon.isCalculatedNumber());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testCloneCalculated() {
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_PX, 5f);
		value.setCalculatedNumber(true);
		NumberValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getFloatValue(CSSUnit.CSS_PX), clon.getFloatValue(CSSUnit.CSS_PX), 1e-8f);
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(clon.isCalculatedNumber());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testFloatValueConversion() {
		assertEquals(0f, NumberValue.floatValueConversion(0f, CSSUnit.CSS_NUMBER, CSSUnit.CSS_PX),
				1e-6f);
		assertEquals(0f, NumberValue.floatValueConversion(0f, CSSUnit.CSS_NUMBER, CSSUnit.CSS_DEG),
				1e-6f);
		assertEquals(75, NumberValue.floatValueConversion(100f, CSSUnit.CSS_PX, CSSUnit.CSS_PT),
				1e-6f);
		assertEquals(1, NumberValue.floatValueConversion(16f, CSSUnit.CSS_PX, CSSUnit.CSS_PC),
				1e-6f);
		assertEquals(1, NumberValue.floatValueConversion(96f, CSSUnit.CSS_PX, CSSUnit.CSS_IN),
				1e-6f);
		assertEquals(1,
				NumberValue.floatValueConversion(37.7952756f, CSSUnit.CSS_PX, CSSUnit.CSS_CM),
				1e-6f);
		assertEquals(1,
				NumberValue.floatValueConversion(3.77952756f, CSSUnit.CSS_PX, CSSUnit.CSS_MM),
				1e-6f);
		assertEquals(4, NumberValue.floatValueConversion(3.77952756f, CSSUnit.CSS_PX,
				CSSUnit.CSS_QUARTER_MM), 1e-6f);
		//
		assertEquals(100, NumberValue.floatValueConversion(75f, CSSUnit.CSS_PT, CSSUnit.CSS_PX),
				1e-6f);
		assertEquals(1, NumberValue.floatValueConversion(12f, CSSUnit.CSS_PT, CSSUnit.CSS_PC),
				1e-6f);
		assertEquals(1, NumberValue.floatValueConversion(72f, CSSUnit.CSS_PT, CSSUnit.CSS_IN),
				1e-6f);
		assertEquals(2.54f, NumberValue.floatValueConversion(72f, CSSUnit.CSS_PT, CSSUnit.CSS_CM),
				1e-7f);
		assertEquals(25.4f, NumberValue.floatValueConversion(72f, CSSUnit.CSS_PT, CSSUnit.CSS_MM),
				1e-7f);
		assertEquals(101.6f,
				NumberValue.floatValueConversion(72f, CSSUnit.CSS_PT, CSSUnit.CSS_QUARTER_MM),
				1e-6f);
		//
		assertEquals(72f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_IN, CSSUnit.CSS_PT),
				1e-6f);
		assertEquals(6f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_IN, CSSUnit.CSS_PC),
				1e-6f);
		assertEquals(96f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_IN, CSSUnit.CSS_PX),
				1e-6f);
		assertEquals(2.54f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_IN, CSSUnit.CSS_CM),
				1e-6f);
		assertEquals(25.4f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_IN, CSSUnit.CSS_MM),
				1e-6f);
		assertEquals(101.6f,
				NumberValue.floatValueConversion(1f, CSSUnit.CSS_IN, CSSUnit.CSS_QUARTER_MM),
				1e-5f);
		//
		assertEquals(72f, NumberValue.floatValueConversion(6f, CSSUnit.CSS_PC, CSSUnit.CSS_PT),
				1e-5f);
		assertEquals(1f, NumberValue.floatValueConversion(6f, CSSUnit.CSS_PC, CSSUnit.CSS_IN),
				1e-6f);
		assertEquals(96f, NumberValue.floatValueConversion(6f, CSSUnit.CSS_PC, CSSUnit.CSS_PX),
				1e-5f);
		assertEquals(2.54f, NumberValue.floatValueConversion(6f, CSSUnit.CSS_PC, CSSUnit.CSS_CM),
				1e-6f);
		assertEquals(25.4f, NumberValue.floatValueConversion(6f, CSSUnit.CSS_PC, CSSUnit.CSS_MM),
				1e-5f);
		assertEquals(101.6f,
				NumberValue.floatValueConversion(6f, CSSUnit.CSS_PC, CSSUnit.CSS_QUARTER_MM),
				1e-5f);
		//
		assertEquals(72f, NumberValue.floatValueConversion(2.54f, CSSUnit.CSS_CM, CSSUnit.CSS_PT),
				1e-5f);
		assertEquals(1f, NumberValue.floatValueConversion(2.54f, CSSUnit.CSS_CM, CSSUnit.CSS_IN),
				1e-6f);
		assertEquals(96f, NumberValue.floatValueConversion(2.54f, CSSUnit.CSS_CM, CSSUnit.CSS_PX),
				1e-5f);
		assertEquals(6f, NumberValue.floatValueConversion(2.54f, CSSUnit.CSS_CM, CSSUnit.CSS_PC),
				1e-6f);
		assertEquals(10f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_CM, CSSUnit.CSS_MM),
				1e-5f);
		assertEquals(4f,
				NumberValue.floatValueConversion(0.1f, CSSUnit.CSS_CM, CSSUnit.CSS_QUARTER_MM),
				1e-6f);
		//
		assertEquals(72f, NumberValue.floatValueConversion(25.4f, CSSUnit.CSS_MM, CSSUnit.CSS_PT),
				1e-5f);
		assertEquals(1f, NumberValue.floatValueConversion(25.4f, CSSUnit.CSS_MM, CSSUnit.CSS_IN),
				1e-6f);
		assertEquals(96f, NumberValue.floatValueConversion(25.4f, CSSUnit.CSS_MM, CSSUnit.CSS_PX),
				1e-5f);
		assertEquals(6f, NumberValue.floatValueConversion(25.4f, CSSUnit.CSS_MM, CSSUnit.CSS_PC),
				1e-6f);
		assertEquals(1f, NumberValue.floatValueConversion(10f, CSSUnit.CSS_MM, CSSUnit.CSS_CM),
				1e-6f);
		assertEquals(4f,
				NumberValue.floatValueConversion(1f, CSSUnit.CSS_MM, CSSUnit.CSS_QUARTER_MM),
				1e-6f);
		//
		assertEquals(72f,
				NumberValue.floatValueConversion(101.6f, CSSUnit.CSS_QUARTER_MM, CSSUnit.CSS_PT),
				1e-5f);
		assertEquals(1f,
				NumberValue.floatValueConversion(101.6f, CSSUnit.CSS_QUARTER_MM, CSSUnit.CSS_IN),
				1e-6f);
		assertEquals(96f,
				NumberValue.floatValueConversion(101.6f, CSSUnit.CSS_QUARTER_MM, CSSUnit.CSS_PX),
				1e-5f);
		assertEquals(6f,
				NumberValue.floatValueConversion(101.6f, CSSUnit.CSS_QUARTER_MM, CSSUnit.CSS_PC),
				1e-6f);
		assertEquals(1f,
				NumberValue.floatValueConversion(40f, CSSUnit.CSS_QUARTER_MM, CSSUnit.CSS_CM),
				1e-6f);
		assertEquals(1f,
				NumberValue.floatValueConversion(4f, CSSUnit.CSS_QUARTER_MM, CSSUnit.CSS_MM),
				1e-6f);
		//
		assertEquals(1000f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_S, CSSUnit.CSS_MS),
				1e-5f);
		assertEquals(1f, NumberValue.floatValueConversion(1000f, CSSUnit.CSS_MS, CSSUnit.CSS_S),
				1e-6f);
		//
		assertEquals(0.001f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_HZ, CSSUnit.CSS_KHZ),
				1e-7f);
		assertEquals(1000f, NumberValue.floatValueConversion(1f, CSSUnit.CSS_KHZ, CSSUnit.CSS_HZ),
				1e-5f);
		//
		assertEquals(57.2957795f,
				NumberValue.floatValueConversion(1f, CSSUnit.CSS_RAD, CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(200f,
				NumberValue.floatValueConversion(3.1415927f, CSSUnit.CSS_RAD, CSSUnit.CSS_GRAD),
				1e-5f);
		assertEquals(0.5f,
				NumberValue.floatValueConversion(3.141593f, CSSUnit.CSS_RAD, CSSUnit.CSS_TURN),
				1e-6f);
		//
		assertEquals(1f,
				NumberValue.floatValueConversion(57.2957795f, CSSUnit.CSS_DEG, CSSUnit.CSS_RAD),
				1e-6f);
		assertEquals(200f,
				NumberValue.floatValueConversion(180f, CSSUnit.CSS_DEG, CSSUnit.CSS_GRAD), 1e-4f);
		assertEquals(0.5f,
				NumberValue.floatValueConversion(180f, CSSUnit.CSS_DEG, CSSUnit.CSS_TURN), 1e-6f);
		//
		assertEquals(3.14159265f,
				NumberValue.floatValueConversion(200f, CSSUnit.CSS_GRAD, CSSUnit.CSS_RAD), 1e-6f);
		assertEquals(180f,
				NumberValue.floatValueConversion(200f, CSSUnit.CSS_GRAD, CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(0.5f,
				NumberValue.floatValueConversion(200f, CSSUnit.CSS_GRAD, CSSUnit.CSS_TURN), 1e-6f);
		//
		assertEquals(3.1415927f,
				NumberValue.floatValueConversion(0.5f, CSSUnit.CSS_TURN, CSSUnit.CSS_RAD), 1e-6f);
		assertEquals(180f,
				NumberValue.floatValueConversion(0.5f, CSSUnit.CSS_TURN, CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(200f,
				NumberValue.floatValueConversion(0.5f, CSSUnit.CSS_TURN, CSSUnit.CSS_GRAD), 1e-5f);
		//
		assertEquals(37.7952766f,
				NumberValue.floatValueConversion(96f, CSSUnit.CSS_DPI, CSSUnit.CSS_DPCM),
				1e-6f);
		assertEquals(1f,
				NumberValue.floatValueConversion(96f, CSSUnit.CSS_DPI, CSSUnit.CSS_DPPX),
				1e-6f);
		//
		assertEquals(76.2f,
				NumberValue.floatValueConversion(30f, CSSUnit.CSS_DPCM, CSSUnit.CSS_DPI),
				1e-6f);
		assertEquals(1.00542f,
				NumberValue.floatValueConversion(38f, CSSUnit.CSS_DPCM, CSSUnit.CSS_DPPX),
				1e-5f);
		//
		assertEquals(96f,
				NumberValue.floatValueConversion(1f, CSSUnit.CSS_DPPX, CSSUnit.CSS_DPI),
				1e-6f);
		assertEquals(37.795277f,
				NumberValue.floatValueConversion(1f, CSSUnit.CSS_DPPX, CSSUnit.CSS_DPCM),
				1e-6f);
	}

}
