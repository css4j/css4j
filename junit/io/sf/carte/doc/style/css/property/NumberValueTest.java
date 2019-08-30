/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.StyleRule;

public class NumberValueTest {

	@Test
	public void testGetCssText() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSPrimitiveValue.CSS_PX, 5f);
		assertEquals("5px", val.getCssText());
		val.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 5f);
		assertEquals("5", val.getCssText());
		val.setFloatValue(CSSPrimitiveValue.CSS_PX, 0f);
		assertEquals("0px", val.getCssText());
	}

	@Test
	public void testGetMinifiedCssText() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSPrimitiveValue.CSS_PX, 5f);
		assertEquals("5px", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 5f);
		assertEquals("5", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_PX, 0f);
		assertEquals("0", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_PX, 0.1f);
		assertEquals(".1px", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_PX, -0.1f);
		assertEquals("-.1px", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 0.1f);
		assertEquals(".1", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, -0.1f);
		assertEquals("-.1", val.getMinifiedCssText(null));
	}

	@Test
	public void testGetCssTextPercentage() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 35f);
		assertEquals("35%", val.getCssText());
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 5f);
		assertEquals("5%", val.getCssText());
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 5.6f);
		assertEquals("5.6%", val.getCssText());
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 0.01f);
		assertEquals("0.01%", val.getCssText());
	}

	@Test
	public void testGetMinifiedCssTextPercentage() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 35f);
		assertEquals("35%", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 5f);
		assertEquals("5%", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 5.6f);
		assertEquals("5.6%", val.getMinifiedCssText(null));
		val.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 0.01f);
		assertEquals(".01%", val.getMinifiedCssText(null));
	}

	@Test
	public void testGetCssText2() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("height: 10vh");
		assertEquals("10vh", style.getPropertyCSSValue("height").getCssText());
		assertEquals("10vh", style.getPropertyValue("height"));
		//
		style.setCssText("height: 5px");
		assertEquals("5px", style.getPropertyValue("height"));
		assertEquals("5px", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		style.setCssText("height: 5.666667px");
		assertEquals("5.666667px", style.getPropertyValue("height"));
		assertEquals("5.666667px", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		style.setCssText("margin-left: -5px");
		assertEquals("-5px", style.getPropertyValue("margin-left"));
		assertEquals("-5px", style.getPropertyCSSValue("margin-left").getMinifiedCssText(""));
		style.setCssText("line-height: 5");
		assertEquals("5", style.getPropertyValue("line-height"));
		assertEquals("5", style.getPropertyCSSValue("line-height").getMinifiedCssText(""));
		style.setCssText("line-height: -5");
		assertEquals("-5", style.getPropertyValue("line-height"));
		assertEquals("-5", style.getPropertyCSSValue("line-height").getMinifiedCssText(""));
		style.setCssText("height: 0px");
		assertEquals("0px", style.getPropertyValue("height"));
		assertEquals("0", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		style.setCssText("height: 0%");
		assertEquals("0%", style.getPropertyValue("height"));
		assertEquals("0%", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		style.setCssText("height: 10%");
		assertEquals("10%", style.getPropertyValue("height"));
		assertEquals("10%", style.getPropertyCSSValue("height").getMinifiedCssText(""));
		style.setCssText("resolution: 300dpi");
		assertEquals("300dpi", style.getPropertyValue("resolution"));
		AbstractCSSValue value = style.getPropertyCSSValue("resolution");
		assertEquals(CSSPrimitiveValue2.CSS_DPI, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("300dpi", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetFloatValuePt() {
		NumberValue val = new NumberValue();
		val.setFloatValue(CSSPrimitiveValue.CSS_EMS, 2f);
		assertEquals("2em", val.getCssText());
		val.setFloatValuePt(5f);
		assertEquals("5pt", val.getCssText());
	}

	@Test
	public void testEquals() {
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_PX, 5f);
		NumberValue other = new NumberValue();
		other.setFloatValue(CSSPrimitiveValue.CSS_PX, 5.2f);
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		other.setFloatValue(CSSPrimitiveValue.CSS_PX, 5f);
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 5f);
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		value.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 0);
		other.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 0);
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		value.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 0);
		other.setFloatValue(CSSPrimitiveValue.CSS_PX, 0);
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
	}

	@Test
	public void testEquals2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("left: 5.33333333%; ");
		AbstractCSSValue value = style.getPropertyCSSValue("left");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_PERCENTAGE, ((CSSPrimitiveValue) value).getPrimitiveType());
		style.setCssText("left: 5.33333333%; ");
		AbstractCSSValue other = style.getPropertyCSSValue("left");
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
		AbstractCSSValue value = style.getPropertyCSSValue("left");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_PX, ((CSSPrimitiveValue) value).getPrimitiveType());
		style.setCssText("left: 2.6666666667px; ");
		AbstractCSSValue other = style.getPropertyCSSValue("left");
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
		AbstractCSSValue value = style.getPropertyCSSValue("left");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_PX, ((CSSPrimitiveValue) value).getPrimitiveType());
		style.setCssText("left: 0px; ");
		AbstractCSSValue other = style.getPropertyCSSValue("left");
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
		value.setFloatValue(CSSPrimitiveValue.CSS_PX, 5f);
		NumberValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getFloatValue(CSSPrimitiveValue.CSS_PX), clon.getFloatValue(CSSPrimitiveValue.CSS_PX), 1e-8);
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testFloatValueConversion() {
		assertEquals(75, NumberValue.floatValueConversion(100f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_PT),
				1e-6);
		assertEquals(1, NumberValue.floatValueConversion(16f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_PC),
				1e-6);
		assertEquals(1, NumberValue.floatValueConversion(96f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_IN),
				1e-6);
		assertEquals(1, NumberValue.floatValueConversion(37.7952756f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_CM),
				1e-6);
		assertEquals(1, NumberValue.floatValueConversion(3.77952756f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_MM),
				1e-6);
		assertEquals(4, NumberValue.floatValueConversion(3.77952756f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue2.CSS_QUARTER_MM),
				1e-6);
		//
		assertEquals(100, NumberValue.floatValueConversion(75f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX),
				1e-6);
		assertEquals(1, NumberValue.floatValueConversion(12f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PC),
				1e-6);
		assertEquals(1, NumberValue.floatValueConversion(72f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_IN),
				1e-6);
		assertEquals(2.54f, NumberValue.floatValueConversion(72f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_CM),
				1e-7);
		assertEquals(25.4f, NumberValue.floatValueConversion(72f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_MM),
				1e-7);
		assertEquals(101.6f, NumberValue.floatValueConversion(72f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue2.CSS_QUARTER_MM),
				1e-6);
		//
		assertEquals(72f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_PT),
				1e-6);
		assertEquals(6f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_PC),
				1e-6);
		assertEquals(96f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_PX),
				1e-6);
		assertEquals(2.54f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_CM),
				1e-6);
		assertEquals(25.4f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_MM),
				1e-6);
		assertEquals(101.6f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue2.CSS_QUARTER_MM),
				1e-5);
		//
		assertEquals(72f, NumberValue.floatValueConversion(6f, CSSPrimitiveValue.CSS_PC, CSSPrimitiveValue.CSS_PT),
				1e-5);
		assertEquals(1f, NumberValue.floatValueConversion(6f, CSSPrimitiveValue.CSS_PC, CSSPrimitiveValue.CSS_IN),
				1e-6);
		assertEquals(96f, NumberValue.floatValueConversion(6f, CSSPrimitiveValue.CSS_PC, CSSPrimitiveValue.CSS_PX),
				1e-5);
		assertEquals(2.54f, NumberValue.floatValueConversion(6f, CSSPrimitiveValue.CSS_PC, CSSPrimitiveValue.CSS_CM),
				1e-6);
		assertEquals(25.4f, NumberValue.floatValueConversion(6f, CSSPrimitiveValue.CSS_PC, CSSPrimitiveValue.CSS_MM),
				1e-5);
		assertEquals(101.6f, NumberValue.floatValueConversion(6f, CSSPrimitiveValue.CSS_PC, CSSPrimitiveValue2.CSS_QUARTER_MM),
				1e-5);
		//
		assertEquals(72f, NumberValue.floatValueConversion(2.54f, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_PT),
				1e-5);
		assertEquals(1f, NumberValue.floatValueConversion(2.54f, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_IN),
				1e-6);
		assertEquals(96f, NumberValue.floatValueConversion(2.54f, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_PX),
				1e-5);
		assertEquals(6f, NumberValue.floatValueConversion(2.54f, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_PC),
				1e-6);
		assertEquals(10f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_MM),
				1e-5);
		assertEquals(4f, NumberValue.floatValueConversion(0.1f, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue2.CSS_QUARTER_MM),
				1e-6);
		//
		assertEquals(72f, NumberValue.floatValueConversion(25.4f, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_PT),
				1e-5);
		assertEquals(1f, NumberValue.floatValueConversion(25.4f, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_IN),
				1e-6);
		assertEquals(96f, NumberValue.floatValueConversion(25.4f, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_PX),
				1e-5);
		assertEquals(6f, NumberValue.floatValueConversion(25.4f, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_PC),
				1e-6);
		assertEquals(1f, NumberValue.floatValueConversion(10f, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_CM),
				1e-6);
		assertEquals(4f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue2.CSS_QUARTER_MM),
				1e-6);
		//
		assertEquals(72f, NumberValue.floatValueConversion(101.6f, CSSPrimitiveValue2.CSS_QUARTER_MM, CSSPrimitiveValue.CSS_PT),
				1e-5);
		assertEquals(1f, NumberValue.floatValueConversion(101.6f, CSSPrimitiveValue2.CSS_QUARTER_MM, CSSPrimitiveValue.CSS_IN),
				1e-6);
		assertEquals(96f, NumberValue.floatValueConversion(101.6f, CSSPrimitiveValue2.CSS_QUARTER_MM, CSSPrimitiveValue.CSS_PX),
				1e-5);
		assertEquals(6f, NumberValue.floatValueConversion(101.6f, CSSPrimitiveValue2.CSS_QUARTER_MM, CSSPrimitiveValue.CSS_PC),
				1e-6);
		assertEquals(1f, NumberValue.floatValueConversion(40f, CSSPrimitiveValue2.CSS_QUARTER_MM, CSSPrimitiveValue.CSS_CM),
				1e-6);
		assertEquals(1f, NumberValue.floatValueConversion(4f, CSSPrimitiveValue2.CSS_QUARTER_MM, CSSPrimitiveValue.CSS_MM),
				1e-6);
		//
		assertEquals(1000f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_S, CSSPrimitiveValue.CSS_MS),
				1e-5);
		assertEquals(1f, NumberValue.floatValueConversion(1000f, CSSPrimitiveValue.CSS_MS, CSSPrimitiveValue.CSS_S),
				1e-6);
		//
		assertEquals(0.001f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_HZ, CSSPrimitiveValue.CSS_KHZ),
				1e-7);
		assertEquals(1000f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_KHZ, CSSPrimitiveValue.CSS_HZ),
				1e-5);
		//
		assertEquals(57.2957795f, NumberValue.floatValueConversion(1f, CSSPrimitiveValue.CSS_RAD, CSSPrimitiveValue.CSS_DEG),
				1e-5);
		assertEquals(200f, NumberValue.floatValueConversion(3.1415927f, CSSPrimitiveValue.CSS_RAD, CSSPrimitiveValue.CSS_GRAD),
				1e-5);
		assertEquals(0.5f, NumberValue.floatValueConversion(3.141593f, CSSPrimitiveValue.CSS_RAD, CSSPrimitiveValue2.CSS_TURN),
				1e-6);
		//
		assertEquals(1f, NumberValue.floatValueConversion(57.2957795f, CSSPrimitiveValue.CSS_DEG, CSSPrimitiveValue.CSS_RAD),
				1e-6);
		assertEquals(200f, NumberValue.floatValueConversion(180f, CSSPrimitiveValue.CSS_DEG, CSSPrimitiveValue.CSS_GRAD),
				1e-4);
		assertEquals(0.5f, NumberValue.floatValueConversion(180f, CSSPrimitiveValue.CSS_DEG, CSSPrimitiveValue2.CSS_TURN),
				1e-6);
		//
		assertEquals(3.14159265f, NumberValue.floatValueConversion(200f, CSSPrimitiveValue.CSS_GRAD, CSSPrimitiveValue.CSS_RAD),
				1e-6);
		assertEquals(180f, NumberValue.floatValueConversion(200f, CSSPrimitiveValue.CSS_GRAD, CSSPrimitiveValue.CSS_DEG),
				1e-5);
		assertEquals(0.5f, NumberValue.floatValueConversion(200f, CSSPrimitiveValue.CSS_GRAD, CSSPrimitiveValue2.CSS_TURN),
				1e-6);
		//
		assertEquals(3.1415927f, NumberValue.floatValueConversion(0.5f, CSSPrimitiveValue2.CSS_TURN, CSSPrimitiveValue.CSS_RAD),
				1e-6);
		assertEquals(180f, NumberValue.floatValueConversion(0.5f, CSSPrimitiveValue2.CSS_TURN, CSSPrimitiveValue.CSS_DEG),
				1e-5);
		assertEquals(200f, NumberValue.floatValueConversion(0.5f, CSSPrimitiveValue2.CSS_TURN, CSSPrimitiveValue.CSS_GRAD),
				1e-5);
	}

}
