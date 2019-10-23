/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.property.ColorValue.CSSRGBColor;

public class ColorValueTest {

	@Test
	public void testGetCssText() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color: rgb(8,63,255); ");
		assertEquals("#083fff", style.getPropertyValue("color"));
		assertEquals("color: #083fff; ", style.getCssText());
		//
		style.setCssText("color: rgba(8,63,255,0.5); ");
		assertEquals("rgba(8, 63, 255, 0.5)", style.getPropertyValue("color"));
		assertEquals("color: rgba(8, 63, 255, 0.5); ", style.getCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.RGBCOLOR, value.getPrimitiveType());
		ColorValue val = (ColorValue) value;
		RGBAColor rgb = val.getRGBColorValue();
		assertEquals(8, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(0.5f,
				(((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001);
		//
		style.setCssText("color: rgb(8 63 255); ");
		assertEquals("#083fff", style.getPropertyValue("color"));
		assertEquals("color: #083fff; ", style.getCssText());
		//
		style.setCssText("color: rgb(8 63 255/0.5); ");
		assertEquals("rgb(8 63 255 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 0.5); ", style.getCssText());
		value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.RGBCOLOR, value.getPrimitiveType());
		val = (ColorValue) value;
		rgb = val.getRGBColorValue();
		assertEquals(8f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(0.5f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		//
		val = new ColorValue();
		val.setCssText("rgb(8, 63, 255)");
		rgb = val.getRGBColorValue();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		//
		val.setCssText("magenta");
		rgb = val.getRGBColorValue();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(255f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		//
		val.setCssText("BLUE");
		rgb = val.getRGBColorValue();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		//
		style.setCssText("color: RGBA(8, 63, 255, 0);");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.getRGBColorValue();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(8f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(63f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		//
		style.setCssText("color: rgba(8, 63, 255, 0); ");
		assertEquals("rgba(8, 63, 255, 0)", style.getPropertyValue("color"));
		assertEquals("color: rgba(8, 63, 255, 0); ", style.getCssText());
		//
		style.setCssText("color: hsl(120, 100%, 50%); ");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		//
		val = new ColorValue();
		val.setCssText("rgb(8 63 255)");
		assertEquals(1f, ((CSSTypedValue) val.getRGBColorValue().getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		//
		style.setCssText("color: rgb(8 63 255/0); ");
		assertEquals("rgb(8 63 255 / 0)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 0); ", style.getCssText());
		//
		style.setCssText("color: rgb(8 63 255/1%); ");
		assertEquals("rgb(8 63 255 / 1%)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 1%); ", style.getCssText());
		assertEquals("color:rgb(8 63 255/1%)", style.getMinifiedCssText());
		//
		style.setCssText("color: hsl(120, 100%, 50%); ");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals("#0f0", rgb.toString());
		assertEquals("color: hsl(120, 100%, 50%); ", style.getCssText());
		//
		style.setCssText("color: hsl(120 100% 50%); ");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals("#0f0", rgb.toString());
		assertEquals("color: hsl(120 100% 50%); ", style.getCssText());
		//
		style.setCssText("color: transparent; ");
		assertEquals("transparent", style.getPropertyValue("color"));
		assertEquals("color: transparent; ", style.getCssText());
		//
		style.setCssText("color: rgb(8,63); ");
		assertEquals("", style.getCssText());
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac98213a");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.getRGBColorValue();
		assertEquals(0.22745f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001);
		assertEquals("rgb(172 152 33 / 0.227)", style.getPropertyValue("color"));
		assertEquals("rgb(172 152 33/.227)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac982100");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.getRGBColorValue();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001);
		assertEquals("rgb(172 152 33 / 0)", style.getPropertyValue("color"));
		assertEquals("rgb(172 152 33/0)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac9821");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.getRGBColorValue();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001);
		assertEquals("#ac9821", style.getPropertyValue("color"));
		assertEquals("#ac9821", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac9a");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.getRGBColorValue();
		assertEquals(0.66666f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001);
		assertEquals("rgb(170 204 153 / 0.667)", style.getPropertyValue("color"));
		assertEquals("rgb(170 204 153/.667)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac90");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.getRGBColorValue();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001);
		assertEquals("rgb(170 204 153 / 0)", style.getPropertyValue("color"));
		assertEquals("rgb(170 204 153/0)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac9");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.getRGBColorValue();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001);
		assertEquals("#ac9", style.getPropertyValue("color"));
		assertEquals("#ac9", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssText() {
		ColorValue val = new ColorValue();
		val.setCssText("#abc");
		RGBAColor rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(204f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		val.setCssText("#abc4");
		rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals(0.266667f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(204f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		val.setCssText("#aabbb840");
		rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals(0.25098f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
		assertEquals(184f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001);
	}

	@Test
	public void testGetMinifiedCssText() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(8,63,255); ");
		assertEquals("color:#083fff", style.getMinifiedCssText());
		style.setCssText("color: #002; ");
		assertEquals("color:#002", style.getMinifiedCssText());
		style.setCssText("color: #06e05b; ");
		assertEquals("color:#06e05b", style.getMinifiedCssText());
		style.setCssText("color: rgb(8 63 255/0.7); ");
		assertEquals("color:rgb(8 63 255/.7)", style.getMinifiedCssText());
		style.setCssText("color: rgba(8, 63, 255, 0.7); ");
		assertEquals("color:rgba(8,63,255,.7)", style.getMinifiedCssText());
		style.setCssText("color: hsl(0 0% 100% / 0.2); ");
		assertEquals("color:hsl(0 0% 100%/.2)", style.getMinifiedCssText());
		style.setCssText("color: hsl(0 0% 100% /.2); ");
		assertEquals("color:hsl(0 0% 100%/.2)", style.getMinifiedCssText());
		style.setCssText("color: hsl(0, 0%, 75%); ");
		assertEquals("color:#bfbfbf", style.getMinifiedCssText());
	}

	@Test
	public void testGetRGBColorValue() {
		ExtendedCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(8 63 255/0.5); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		RGBAColor rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(8 63 255 / 0.5)", rgb.toString());
		style.setCssText("color: #f00; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#f00", rgb.toString());
		assertEquals("#f00", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
		style.setCssText("color: red; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#f00", rgb.toString());
		assertEquals("#f00", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
		style.setCssText("color: #ea3; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#ea3", rgb.toString());
		assertEquals("#ea3", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
		style.setCssText("color: #fa07e9; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#fa07e9", rgb.toString());
		assertEquals("#fa07e9", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
	}

	@Test
	public void testHSLColorspace() {
		ExtendedCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(120, 100%, 50%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		RGBAColor rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals(0, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(100f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("#0f0", rgb.toString());
		assertEquals("hsl(120, 100%, 50%)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsl(120 100% 50%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals(0, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(100f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("#0f0", rgb.toString());
		assertEquals("hsl(120 100% 50%)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsl(240, 100%, 50%, 0.5); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgba(0%, 0%, 100%, 0.5)", rgb.toString());
		assertEquals("hsla(240, 100%, 50%, 0.5)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsl(240 100% 50% / 0.5); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(0% 0% 100% / 0.5)", rgb.toString());
		assertEquals("hsl(240 100% 50% / 0.5)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsla(40, 75%, 28%, 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgba(49%, 35%, 7%, 0.75)", rgb.toString());
		//
		style.setCssText("color: hsl(40 75% 28% / 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(49% 35% 7% / 0.75)", rgb.toString());
		//
		style.setCssText("color: hsl(0.75turn 75% 28% / 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(28% 7% 49% / 0.75)", rgb.toString());
		//
		style.setCssText("color: hsl(1.2rad 75% 28% / 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(42.9% 49% 7% / 0.75)", rgb.toString());
		//
		style.setCssText("color: hsl(760, 85%, 24%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#714f09", rgb.toString());
		assertEquals("hsl(40, 85%, 24%)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsl(760 85% 24%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#714f09", rgb.toString());
		assertEquals("hsl(40 85% 24%)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsl(-170, 95%, 35%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#0492ae", rgb.toString());
		assertEquals("hsl(190, 95%, 35%)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
		//
		style.setCssText("color: hsl(-170deg 95% 35%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#0492ae", rgb.toString());
		assertEquals("hsl(190 95% 35%)", ((ColorValue.CSSRGBColor) rgb).toHSLString());
	}

	@Test
	public void testHWBColorspace() {
		ExtendedCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hwb(205, 19%, 14%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		RGBAColor rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals(48f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.1f);
		assertEquals(148f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.1f);
		assertEquals(219f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.1f);
		assertEquals("#3094db", rgb.toString());
		//
		style.setCssText("color: hwb(0, 0%, 0%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#f00", rgb.toString());
		//
		style.setCssText("color: hwb(357, 25%, 12%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#e04048", rgb.toString());
		//
		style.setCssText("color: hwb(61, 37%, 8%, 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(232 235 94 / 0.75)", rgb.toString());
		//
		style.setCssText("color: hwb(61 37% 8% / 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("rgb(232 235 94 / 0.75)", rgb.toString());
		//
		style.setCssText("color: hwb(255, 33%, 13%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals("#7754de", rgb.toString());
		//
		style.setCssText("color: hwb(179, 65%, 19%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).getRGBColorValue();
		assertEquals(65.1f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER) / 2.55f, 0.1f);
		assertEquals(81.17f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER) / 2.55f, 0.1f);
		assertEquals(80.78f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER) / 2.55f, 0.1f);
		assertEquals("#a6cfce", rgb.toString());
	}

	@Test
	public void testEquals() {
		ColorValue value = new ColorValue();
		value.setCssText("rgb(8,63,255)");
		ColorValue other = new ColorValue();
		other.setCssText("rgb(8,63,255)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("rgba(8 63 255/1.0)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("rgba(7 63 255/1.0)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("rgba(8 63 255/0.8)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		value.setCssText("#000");
		other.setCssText("#000");
		assertTrue(value.equals(other));
	}

	@Test
	public void testEqualsHsl() {
		ColorValue value = new ColorValue();
		value.setCssText("hsl(180, 90%, 58%)");
		ColorValue other = new ColorValue();
		other.setCssText("hsl(180, 90%, 58%)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("hsl(180 90% 58% / 1.0)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("hsl(179 90% 58% / 1.0)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("hsl(180 90% 58% / 0.5)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testClone() {
		ExtendedCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgba(8,63,255,0.5); ");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.getRGBColorValue().equals(clon.getRGBColorValue()));
		assertTrue(value.getRGBColorValue().hashCode() == clon.getRGBColorValue().hashCode());
	}

	@Test
	public void testRGBAColor() throws CSSPropertyValueException {
		ExtendedCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgba(8,63,255,0.5); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.RGBCOLOR, ((CSSTypedValue) cssColor).getPrimitiveType());
		RGBAColor color4 = ((ColorValue) cssColor).getRGBColorValue();
		assertNotNull(color4);
		assertEquals(8, (int) ((CSSTypedValue) color4.getRed()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(63, (int) ((CSSTypedValue) color4.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(255, (int) ((CSSTypedValue) color4.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(128f, ((CSSTypedValue) color4.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER) * 256, 0.001f);
		//
		style.setCssText("color: #f00; ");
		cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.RGBCOLOR, ((CSSTypedValue) cssColor).getPrimitiveType());
		CSSRGBColor color = (CSSRGBColor) ((ColorValue) cssColor).getRGBColorValue();
		assertNotNull(color);
		assertEquals(255, (int) ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(0, (int) ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(0, (int) ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		//
		style.setCssText("color: transparent; ");
		cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, ((CSSTypedValue) cssColor).getPrimitiveType());
		color4 = ((CSSTypedValue) cssColor).getRGBColorValue();
		assertNotNull(color4);
		assertEquals(0, ((CSSTypedValue) color4.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
	}

	@Test
	public void testRGBATransparentColor() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color: rgba(0,0,0,0); ");
		assertEquals("rgba(0, 0, 0, 0)", style.getPropertyValue("color"));
		assertEquals("color: rgba(0, 0, 0, 0); ", style.getCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		RGBAColor rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals("rgba(0, 0, 0, 0)", rgb.toString());
		//
		style.setCssText("color: rgb(0 0 0/0); ");
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("color"));
		assertEquals("color: rgb(0 0 0 / 0); ", style.getCssText());
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.getRGBColorValue();
		assertNotNull(rgb);
		assertEquals("rgb(0 0 0 / 0)", rgb.toString());
	}

}
