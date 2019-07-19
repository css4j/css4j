/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

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
				0.02f);
		assertEquals(1, NumberValue.floatValueConversion(16f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_PC),
				0.02f);
		assertEquals(1, NumberValue.floatValueConversion(96f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_IN),
				0.02f);
		assertEquals(1, NumberValue.floatValueConversion(37.8f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_CM),
				0.2f);
		assertEquals(1, NumberValue.floatValueConversion(3.78f, CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_MM),
				0.02f);
		assertEquals(100, NumberValue.floatValueConversion(75f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX),
				0.02f);
		assertEquals(1, NumberValue.floatValueConversion(12f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PC),
				0.02f);
		assertEquals(1, NumberValue.floatValueConversion(72f, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_IN),
				0.02f);
	}
}
