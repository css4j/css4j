/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.HWBColor;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.ColorValue.CSSRGBColor;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorValueTest {

	@Test
	public void testGetCssText() throws IOException {
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
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		ColorValue val = (ColorValue) value;
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		val.writeCssText(wri);
		assertEquals("rgba(8, 63, 255, 0.5)", wri.toString());
		//
		RGBAColor rgb = val.toRGBColor();
		assertEquals(8, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.5f,
				(((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001);
		//
		style.setCssText("color: rgb(8 63 255); ");
		assertEquals("#083fff", style.getPropertyValue("color"));
		assertEquals("color: #083fff; ", style.getCssText());
		//
		style.setCssText("color: rgb(8.8 63.2 245.3); ");
		assertEquals("rgb(8.8 63.2 245.3)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8.8 63.2 245.3); ", style.getCssText());
		//
		style.setCssText("color: rgb(8 63 255/0.5); ");
		assertEquals("rgb(8 63 255 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 0.5); ", style.getCssText());
		value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		val = (ColorValue) value;
		rgb = val.toRGBColor();
		assertEquals(8f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.5f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		val = new RGBColorValue();
		val.setCssText("rgb(8, 63, 255)");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		val.setCssText("magenta");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		try {
			val.setCssText("notacolor");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		// Check that the values were kept unaltered
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		val.setCssText("BLUE");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		style.setCssText("color: RGBA(8, 63, 255, 0);");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(8f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(63f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		style.setCssText("color: rgba(8, 63, 255, 0); ");
		assertEquals("rgba(8, 63, 255, 0)", style.getPropertyValue("color"));
		assertEquals("color: rgba(8, 63, 255, 0); ", style.getCssText());
		//
		style.setCssText("color: hsl(120, 100%, 50%); ");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		//
		val = new RGBColorValue();
		val.setCssText("rgb(8 63 255)");
		assertEquals(1f, ((CSSTypedValue) val.toRGBColor().getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
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
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("#0f0", rgb.toString());
		assertEquals("color: hsl(120, 100%, 50%); ", style.getCssText());
		//
		style.setCssText("color: hsl(120 100% 50%); ");
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.toRGBColor();
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
		rgb = val.toRGBColor();
		assertEquals(0.22745f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001);
		assertEquals("rgb(172 152 33 / 0.2275)", style.getPropertyValue("color"));
		assertEquals("rgb(172 152 33/.2275)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac982100");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(172 152 33 / 0)", style.getPropertyValue("color"));
		assertEquals("rgb(172 152 33/0)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac9821");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#ac9821", style.getPropertyValue("color"));
		assertEquals("#ac9821", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac9a");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.toRGBColor();
		assertEquals(0.66666f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(170 204 153 / 0.6667)", style.getPropertyValue("color"));
		assertEquals("rgb(170 204 153/.6667)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac90");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(170 204 153 / 0)", style.getPropertyValue("color"));
		assertEquals("rgb(170 204 153/0)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:#ac9");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#ac9", style.getPropertyValue("color"));
		assertEquals("#ac9", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color: rgb(179 256 32)");
		val = (ColorValue) style.getPropertyCSSValue("color");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(179 256 32)", style.getPropertyValue("color"));
		assertEquals("rgb(179 256 32)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(style.getStyleDeclarationErrorHandler().hasWarnings());
		//
		style.setCssText("color: rgb(179 -256 32)");
		assertEquals(0, style.getLength());
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColor() {
		ColorValue val = new RGBColorValue();
		val.setCssText("#abc");
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, rgb.getColorModel());
		assertEquals(ColorSpace.srgb, rgb.getColorSpace());
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(204f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		assertSame(rgb.getAlpha(), rgb.item(0));
		assertSame(rgb.getRed(), rgb.item(1));
		assertSame(rgb.getGreen(), rgb.item(2));
		assertSame(rgb.getBlue(), rgb.item(3));
		assertEquals(4, rgb.getLength());
		assertNull(rgb.item(4));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <color>+");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
		//
		val.setCssText("#abc4");
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(0.266667f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.00001f);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(204f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		//
		val.setCssText("#aabbb840");
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(0.25098f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.00001f);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(184f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		// Set components
		val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 37f));
		assertEquals(37f, ((TypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 27f));
		assertEquals(27f, ((TypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		try {
			val.setComponent(0, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(1, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(2, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(3, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
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
		assertEquals("color:hsl(0,0%,75%)", style.getMinifiedCssText());
	}

	@Test
	public void testGetRGBColorValue() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(8 63 255/0.5); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(8 63 255 / 0.5)", rgb.toString());
		style.setCssText("color: #f00; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("#f00", rgb.toString());
		assertEquals("#f00", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
		style.setCssText("color: red; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("#f00", rgb.toString());
		assertEquals("#f00", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
		style.setCssText("color: #ea3; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("#ea3", rgb.toString());
		assertEquals("#ea3", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
		style.setCssText("color: #fa07e9; ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("#fa07e9", rgb.toString());
		assertEquals("#fa07e9", ((ColorValue.CSSRGBColor) rgb).toMinifiedString());
	}

	@Test
	public void testHSLColorModel() throws IOException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(120, 100%, 50%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsl(120, 100%, 50%)", value.getCssText());
		assertEquals("#0f0", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		colorValue.writeCssText(wri);
		assertEquals("hsl(120, 100%, 50%)", wri.toString());
		//
		HSLColor hsl = (HSLColor) colorValue.getColor();
		assertSame(hsl.getAlpha(), hsl.item(0));
		assertSame(hsl.getHue(), hsl.item(1));
		assertSame(hsl.getSaturation(), hsl.item(2));
		assertSame(hsl.getLightness(), hsl.item(3));
		assertEquals(4, hsl.getLength());
		assertNull(hsl.item(4));
		//
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("srgb", hsl.getColorSpace());
		assertEquals("hsl(120, 100%, 50%)", hsl.toString());
		assertEquals("hsl(120,100%,50%)", hsl.toMinifiedString());
		//
		RGBAColor rgb = colorValue.toRGBColor();
		assertEquals(0, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(100f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		//
		assertSame(rgb.getAlpha(), rgb.item(0));
		assertSame(rgb.getRed(), rgb.item(1));
		assertSame(rgb.getGreen(), rgb.item(2));
		assertSame(rgb.getBlue(), rgb.item(3));
		//
		assertEquals("#0f0", rgb.toString());
		assertEquals("hsl(120, 100%, 50%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted value
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText(rgb.toString());
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(colorValue), 0.001f);
		//
		LABColorValue labValue = colorValue.toLABColorValue();
		assertEquals("lab(87.82% -79.288 80.991)", labValue.getCssText());
		assertEquals("lab(87.82% -79.288 80.991)", labValue.getMinifiedCssText(""));
		assertEquals(0f, labValue.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(labValue), 0.001f);
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		style.setCssText("color: hsl(120 100% 50%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(120 100% 50%)", value.getCssText());
		assertEquals("#0f0", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(0, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(100f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("#0f0", rgb.toString());
		assertEquals("hsl(120 100% 50%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		style.setCssText("color: hsl(calc(120) 100% 50%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		colorValue = (CSSColorValue) value;
		assertEquals("hsl(calc(120) 100% 50%)", value.getCssText());
		assertEquals("hsl(calc(120) 100% 50%)", value.getMinifiedCssText("color"));
		assertEquals(CSSColorValue.ColorModel.HSL, colorValue.getColorModel());
		hsl = ((HSLColorValue) value).getColor();
		CalcValue calc = (CalcValue) hsl.getHue();
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, calc.getExpression().getPartType());
		CSSPrimitiveValue operand = ((CSSOperandExpression) calc.getExpression()).getOperand();
		assertEquals(120, ((CSSTypedValue) operand).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(100f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(50, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("hsl(calc(120) 100% 50%)", hsl.toString());
		// DeltaE2000 failure
		try {
			rgbColor.deltaE2000(colorValue);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			colorValue.deltaE2000(rgbColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		//
		style.setCssText("color: hsl(calc(120) 100% 50%/calc(0.9)); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSColorValue.ColorModel.HSL, ((CSSColorValue) value).getColorModel());
		ColorValue val = (ColorValue) value;
		hsl = (HSLColor) val.getColor();
		calc = (CalcValue) hsl.getHue();
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, calc.getExpression().getPartType());
		operand = ((CSSOperandExpression) calc.getExpression()).getOperand();
		assertEquals(120f, ((CSSTypedValue) operand).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(100f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(50, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		calc = (CalcValue) hsl.getAlpha();
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, calc.getExpression().getPartType());
		operand = ((CSSOperandExpression) calc.getExpression()).getOperand();
		assertEquals(0.9f, ((CSSTypedValue) operand).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("hsl(calc(120) 100% 50% / calc(0.9))", hsl.toString());
		// Set components
		val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 27f));
		assertEquals(27f, ((TypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		try {
			val.setComponent(0, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(1, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(2, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(3, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testHSLColorModel2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(240, 100%, 50%, 0.5); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsla(240, 100%, 50%, 0.5)", value.getCssText());
		assertEquals("hsla(240,100%,50%,.5)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgba(0%, 0%, 100%, 0.5)", rgb.toString());
		assertEquals("hsla(240, 100%, 50%, 0.5)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted value
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText(rgb.toString());
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);
		//
		style.setCssText("color: hsl(240 100% 50% / 0.5); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(240 100% 50% / 0.5)", value.getCssText());
		assertEquals("hsl(240 100% 50%/.5)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(0% 0% 100% / 0.5)", rgb.toString());
		assertEquals("hsl(240 100% 50% / 0.5)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		//
		style.setCssText("color: hsla(40.56, 75%, 28%, 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsla(40.56, 75%, 28%, 0.75)", value.getCssText());
		assertEquals("hsla(40.56,75%,28%,.75)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("49%", rgb.getRed().getCssText());
		assertEquals("rgba(49%, 35.39%, 7%, 0.75)", rgb.toString());
	}

	@Test
	public void testHSLColorModel3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(40.56 75% 28% / 0.75); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsl(40.56 75% 28% / 0.75)", value.getCssText());
		assertEquals("hsl(40.56 75% 28%/.75)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("49%", rgb.getRed().getCssText());
		assertEquals("rgb(49% 35.39% 7% / 0.75)", rgb.toString());
		assertEquals("hsl(40.6 75% 28% / 0.75)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted value
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText(rgb.toString());
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.01f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.01f);
	}

	@Test
	public void testHSLColorModel4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(0.75turn 75% 28% / 0.75); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(0.75turn 75% 28% / 0.75)", value.getCssText());
		assertEquals("hsl(.75turn 75% 28%/.75)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(28% 7% 49% / 0.75)", rgb.toString());
		assertEquals("hsl(270 75% 28% / 0.75)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModel5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(1.217rad 75% 28% / 0.75); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsl(1.217rad 75% 28% / 0.75)", value.getCssText());
		assertEquals("hsl(1.217rad 75% 28%/.75)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(42.19% 49% 7% / 0.75)", rgb.toString());
		assertEquals("hsl(69.7 75% 28% / 0.75)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted value
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText(rgb.toString());
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);
	}

	@Test
	public void testHSLColorModel6() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(759.28, 85%, 24%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(759.28, 85%, 24%)", value.getCssText());
		assertEquals("hsl(759.28,85%,24%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(44.4%, 30.31%, 3.6%)", rgb.toString());
		assertEquals("hsl(39.3, 85%, 24%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModel7() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(759.28 85% 24%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(759.28 85% 24%)", value.getCssText());
		assertEquals("hsl(759.28 85% 24%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(44.4% 30.31% 3.6%)", rgb.toString());
		assertEquals("hsl(39.3 85% 24%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		//
		style.setCssText("color: hsl(-169.88, 95%, 35%);");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(-169.88, 95%, 35%)", value.getCssText());
		assertEquals("hsl(-169.88,95%,35%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(1.75%, 57.03%, 68.25%)", rgb.toString());
		assertEquals("hsl(190.1, 95%, 35%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		//
		style.setCssText("color: hsl(-170deg 95% 35%);");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(-170 95% 35%)", value.getCssText());
		assertEquals("hsl(-170 95% 35%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(1.75% 57.17% 68.25%)", rgb.toString());
		assertEquals("hsl(190 95% 35%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModel8() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(359.9999deg 95% 35%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(359.9999 95% 35%)", value.getCssText());
		assertEquals("hsl(359.9999 95% 35%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(68.25% 1.75% 1.75%)", rgb.toString());
		assertEquals("hsl(0 95% 35%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
		//
		style.setCssText("color: hsl(179px 65% 19%)");
		assertEquals(0, style.getLength());
	}

	@Test
	public void testHSLColorModel9() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(0.9999deg 0.999% 0.999%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(0.9999 0.999% 0.999%)", value.getCssText());
		assertEquals("hsl(.9999 .999% .999%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(1.01% 0.99% 0.99%)", rgb.toString());
		assertEquals("hsl(1 1% 1%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHWBColorModel() throws IOException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hwb(205 19% 14%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.HWB, color.getColorModel());
		assertEquals("hwb(205 19% 14%)", value.getCssText());
		assertEquals("hwb(205 19% 14%)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		color.writeCssText(wri);
		assertEquals("hwb(205 19% 14%)", wri.toString());
		//
		HWBColor hwb = (HWBColor) color.getColor();
		assertSame(hwb.getAlpha(), hwb.item(0));
		assertSame(hwb.getHue(), hwb.item(1));
		assertSame(hwb.getWhiteness(), hwb.item(2));
		assertSame(hwb.getBlackness(), hwb.item(3));
		assertEquals(4, hwb.getLength());
		assertNull(hwb.item(4));
		//
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("srgb", hwb.getColorSpace());
		assertEquals("hwb(205 19% 14%)", hwb.toString());
		assertEquals("hwb(205 19% 14%)", hwb.toMinifiedString());
		//
		RGBAColor rgb = color.toRGBColor();
		assertEquals(19f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(58.08334f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(86f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(19%, 58.08%, 86%)", rgb.toString());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		//
		style.setCssText("color: hwb(0 0% 0%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(0 0% 0%)", value.getCssText());
		assertEquals("#f00", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("#f00", rgb.toString());
		//
		style.setCssText("color: hwb(357 25% 12%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(357 25% 12%)", value.getCssText());
		assertEquals("hwb(357 25% 12%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(88%, 25%, 28.15%)", rgb.toString());
		// Set components
		color = (ColorValue) value;
		hwb = (HWBColor) color.getColor();
		color.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) hwb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		color.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) hwb.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		color.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 27f));
		assertEquals(27f, ((TypedValue) hwb.getWhiteness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		color.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) hwb.getBlackness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		try {
			color.setComponent(0, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			color.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			color.setComponent(1, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			color.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			color.setComponent(2, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			color.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			color.setComponent(3, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			color.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testHWBColorModel2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hwb(calc(61) 37% calc(1 * 8%) / calc(0.75)); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(calc(61) 37% calc(1*8%) / calc(0.75))", value.getCssText());
		assertEquals("hwb(calc(61) 37% calc(1*8%)/calc(.75))", value.getMinifiedCssText("color"));
		HWBColor hwb = ((HWBColorValue) value).getColor();
		assertEquals("hwb(calc(61) 37% calc(1*8%) / calc(0.75))", hwb.toString());
		//
		style.setCssText("color: hwb(61 37% 8% / 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(61 37% 8% / 0.75)", value.getCssText());
		assertEquals("hwb(61 37% 8%/.75)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgba(91.08%, 92%, 37%, 0.75)", rgb.toString());
		//
		style.setCssText("color: hwb(73.29 22% 16%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(73.29 22% 16%)", value.getCssText());
		assertEquals("hwb(73.29 22% 16%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(70.27%, 84%, 22%)", rgb.toString());
		//
		style.setCssText("color: hwb(43.6 37% 8% / 0.75); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(43.6 37% 8% / 0.75)", value.getCssText());
		assertEquals("hwb(43.6 37% 8%/.75)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgba(92%, 76.97%, 37%, 0.75)", rgb.toString());
		//
		style.setCssText("color: hwb(255 33% 13%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(255 33% 13%)", value.getCssText());
		assertEquals("hwb(255 33% 13%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(46.5%, 33%, 87%)", rgb.toString());
		//
		style.setCssText("color: hwb(179 65% 19%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(179 65% 19%)", value.getCssText());
		assertEquals("hwb(179 65% 19%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(65f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(81f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(80.73333f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(65%, 81%, 80.73%)", rgb.toString());
		//
		style.setCssText("color: hwb(179deg 65% 19%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(179 65% 19%)", value.getCssText());
		assertEquals("hwb(179 65% 19%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(65f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(81f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(80.73333f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(65%, 81%, 80.73%)", rgb.toString());
		//
		style.setCssText("color: hwb(3.124139rad 65% 19%)");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(3.124139rad 65% 19%)", value.getCssText());
		assertEquals("rgb(65%,81%,80.73%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(65f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(81f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(80.73333f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(65%, 81%, 80.73%)", rgb.toString());
		//
		style.setCssText("color: hwb(0.5rad 0.5% 0.19%); ");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(0.5rad 0.5% 0.19%)", value.getCssText());
		assertEquals("hwb(.5rad .5% .19%)", value.getMinifiedCssText("color"));
		rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(99.809998f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(47.91703f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.5f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(99.81%, 47.92%, 0.5%)", rgb.toString());
		//
		style.setCssText("color: hwb(179px 65% 19%); ");
		assertEquals(0, style.getLength());
	}

	@Test
	public void testVar() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color: rgb(var(--foo), 0.7); ");
		assertEquals("rgb(var(--foo), 0.7)", style.getPropertyValue("color"));
		assertEquals("color: rgb(var(--foo), 0.7); ", style.getCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, value.getPrimitiveType());
	}

	@Test
	public void testCalc() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color: rgb(calc(30%) calc(15%) calc(99%)/ 0.7); ");
		assertEquals("rgb(calc(30%) calc(15%) calc(99%) / 0.7)", style.getPropertyValue("color"));
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		style.setCssText("color: hsl(calc(30deg) calc(15%) calc(99%) / 0.7); ");
		assertEquals("hsl(calc(30deg) calc(15%) calc(99%) / 0.7)", style.getPropertyValue("color"));
		value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
	}

	@Test
	public void testEquals() {
		ColorValue value = new RGBColorValue();
		value.setCssText("rgb(8,63,255)");
		ColorValue other = new RGBColorValue();
		other.setCssText("rgb(8,63,255)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		assertFalse(value.equals(null));
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
		ColorValue value = new HSLColorValue();
		value.setCssText("hsl(180, 90%, 58%)");
		ColorValue other = new HSLColorValue();
		other.setCssText("hsl(180, 90%, 58%)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		assertFalse(value.equals(null));
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
	public void testEqualsHWB() {
		ColorValue value = new HWBColorValue();
		value.setCssText("hwb(205 19% 14%)");
		ColorValue other = new HWBColorValue();
		other.setCssText("hwb(205 19% 14%)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		assertFalse(value.equals(null));
		other.setCssText("hwb(205 19% 14%/1)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("hwb(204 19% 14%)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("hwb(205 19% 14%/0.5)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testCloneRGB() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(8,63,255);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testCloneRGBA() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgba(8,63,255,0.5);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getMinifiedCssText("color"), clon.getMinifiedCssText("color"));
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testCloneRGBAPcnt() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgba(0, 0, 0, 5%)");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgba(0, 0, 0, 5%)", rgb.toString());
		assertEquals("rgba(0,0,0,5%)", rgb.toMinifiedString());

		rgb = (RGBAColor) rgb.clone();
		assertEquals("rgba(0, 0, 0, 5%)", rgb.toString());
		assertEquals("rgba(0,0,0,5%)", rgb.toMinifiedString());

		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getMinifiedCssText("color"), clon.getMinifiedCssText("color"));
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testCloneHSL() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsl(120 100% 50%);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testCloneHSLA() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hsla(120 100% 50%/0.5);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testCloneHWB() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: hwb(205 19% 14%);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testRGBAColor() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgba(8,63,255,0.5); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color4 = rgbColor.toRGBColor();
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
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor2 = (ColorValue) cssColor;
		CSSRGBColor color = (CSSRGBColor) rgbColor2.toRGBColor();
		assertNotNull(color);
		assertEquals(255, (int) ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(0, (int) ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(0, (int) ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		// DeltaE2000
		assertEquals(54.11f, rgbColor.deltaE2000(rgbColor2), 0.01f);
		assertEquals(54.11f, rgbColor2.deltaE2000(rgbColor), 0.01f);
		// Check component access
		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 0.6f);
		rgbColor2.setComponent(0, number);
		assertEquals(0.6f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.6f, ((CSSTypedValue) rgbColor2.getComponent(0)).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 178f);
		rgbColor2.setComponent(3, number);
		rgbColor2.setComponent(4, number);
		assertEquals(178f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(178f, ((CSSTypedValue) rgbColor2.getComponent(3)).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertNull(rgbColor2.getComponent(4));
		// Sanity checks
		number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, -1f);
		try {
			color.setAlpha(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setRed(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		number.setFloatValue(CSSUnit.CSS_NUMBER, 2f);
		try {
			color.setAlpha(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		number.setFloatValue(CSSUnit.CSS_PERCENTAGE, -1f);
		try {
			color.setAlpha(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setRed(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setGreen(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setBlue(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		number.setFloatValue(CSSUnit.CSS_PERCENTAGE, 101f);
		try {
			color.setAlpha(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setRed(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setGreen(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		try {
			color.setBlue(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		number.setFloatValue(CSSUnit.CSS_NUMBER, 256f);
		color.setRed(number); // allowed
		color.setGreen(number); // allowed
		color.setBlue(number); // allowed
		// Transparent identifier
		style.setCssText("color: transparent; ");
		cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.IDENT, cssColor.getPrimitiveType());
		color4 = ((CSSTypedValue) cssColor).toRGBColor();
		assertNotNull(color4);
		assertEquals(0, ((CSSTypedValue) color4.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
	}

	@Test
	public void testRGBColorCalc() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(25,63,calc(254*0.5)); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		assertEquals(CSSColorValue.ColorModel.RGB, ((ColorValue) cssColor).getColorModel());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(25, (int) ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(63, (int) ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.EXPRESSION, color.getBlue().getPrimitiveType());
		assertSame(color, rgbColor.toRGBColor());
		//
		try {
			rgbColor.toHSLColorValue();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		//
		try {
			rgbColor.toLABColorValue();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(25,63,77)");
		try {
			rgbColor.deltaE2000(rgbColor2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			rgbColor2.deltaE2000(rgbColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testRGBAColorConversions() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgba(0%, 8%, 95%, 0.8); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(8f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(95f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.8f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(29.189f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(61.697f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-105.502f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(29.189f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(122.218f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(300.319f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);
		//
		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(234.90f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(100f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(47.5f, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0.02f, rgbColor.deltaE2000(hslColor), 0.01f);
		assertEquals(0.02f, hslColor.deltaE2000(rgbColor), 0.01f);
	}

	@Test
	public void testRGBAColorConversions2() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(55%, 88%, 25%); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(55f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(88f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(25f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(81.74f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(-45.224f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(65.5257f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(81.74f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(79.6168f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(124.6124f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);
		//
		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(91.4f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(72.4f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(56.5f, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0.01f, rgbColor.deltaE2000(hslColor), 0.01f);
		assertEquals(0.01f, hslColor.deltaE2000(rgbColor), 0.01f);
	}

	@Test
	public void testRGBAColorConversions3() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(0%, 0%, 1%); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(0.0424f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0.26457f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(-0.97039f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(0.04239f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(1.00581f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(285.2506f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);
		//
		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(240f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(100f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.5f, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(hslColor), 0.001f);
		assertEquals(0f, hslColor.deltaE2000(rgbColor), 0.001f);
	}

	@Test
	public void testRGBAColorConversions4() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: rgb(50%, 47%, 89%, calc(0.4*2)); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(50f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(47f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(89f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(Type.EXPRESSION, color.getAlpha().getPrimitiveType());
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(54.913f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(23.595f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-54.491f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals("calc(0.4*2)", lab.getAlpha().getCssText());
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(54.913f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(59.380f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(293.413f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals("calc(0.4*2)", lch.getAlpha().getCssText());
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);
		//
		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(244.3f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(65.6f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(68f, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals("calc(0.4*2)", hsl.getAlpha().getCssText());
		// Delta 0 to converted value
		assertEquals(0.01f, rgbColor.deltaE2000(hslColor), 0.01f);
		assertEquals(0.01f, hslColor.deltaE2000(rgbColor), 0.01f);
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
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("rgba(0, 0, 0, 0)", rgb.toString());
		//
		style.setCssText("color: rgb(0 0 0/0); ");
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("color"));
		assertEquals("color: rgb(0 0 0 / 0); ", style.getCssText());
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("rgb(0 0 0 / 0)", rgb.toString());
	}

	@Test
	public void testLABColorModel() throws IOException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(67% 19.2 14.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(67f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(19.2f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(14.8f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertSame(lab.getAlpha(), lab.item(0));
		assertSame(lab.getLightness(), lab.item(1));
		assertSame(lab.getA(), lab.item(2));
		assertSame(lab.getB(), lab.item(3));
		assertEquals(4, lab.getLength());
		assertNull(lab.item(4));
		//
		assertEquals(CSSColorValue.ColorModel.LAB, lab.getColorModel());
		assertEquals("lab", lab.getColorSpace());
		assertEquals("lab(67% 19.2 14.8)", lab.toString());
		assertEquals("lab(67% 19.2 14.8)", lab.toMinifiedString());
		// Set wrong values
		try {
			labColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		try {
			labColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		try {
			labColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		try {
			labColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		// Serialization
		assertEquals("lab(67% 19.2 14.8)", value.getCssText());
		assertEquals("lab(67% 19.2 14.8)", labColor.toString());
		assertEquals("lab(67% 19.2 14.8)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		value.writeCssText(wri);
		assertEquals("lab(67% 19.2 14.8)", wri.toString());
		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(79.5237f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(58.806f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(53.9005f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		String s = rgb.toString();
		assertEquals("rgb(79.52%, 58.81%, 53.9%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, labColor.deltaE2000(srgbValue), 0.01f);
		assertEquals(0f, srgbValue.deltaE2000(labColor), 0.01f);
		assertFalse(labColor.equals(srgbValue));
		assertFalse(srgbValue.equals(labColor));
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(67f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(24.2421f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(37.6262f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
		// HSL indirect conversion
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText(rgb.toString());
		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(11.5f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(38.5f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(66.7f, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0.01f, hslColor.deltaE2000(labColor), 0.01f);
		assertEquals(0.01f, labColor.deltaE2000(hslColor), 0.01f);
		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(calc(25*2),63,77)");
		try {
			labColor.deltaE2000(rgbColor2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			rgbColor2.deltaE2000(labColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		// Component access
		assertSame(lab.getAlpha(), labColor.getComponent(0));
		assertSame(lab.getLightness(), labColor.getComponent(1));
		assertSame(lab.getA(), labColor.getComponent(2));
		assertSame(lab.getB(), labColor.getComponent(3));
		assertNull(labColor.getComponent(4));
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		// Set components
		labColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		labColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		labColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		labColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 27f));
		assertEquals(27f, ((TypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		try {
			labColor.setComponent(0, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			labColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			labColor.setComponent(1, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			labColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			labColor.setComponent(2, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			labColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			labColor.setComponent(3, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			labColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testLABColorModel2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(46.277% -47.562 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(46.277f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(-47.562f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(46.277% -47.562 48.583)", value.getCssText());
		assertEquals("lab(46.277% -47.562 48.583)", labColor.toString());
		assertEquals("lab(46.277% -47.562 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(50.195f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 50.2%, 0%)", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(46.277f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(67.989f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(134.3916f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testLABColorModel3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(54.2917% 80.8125 69.8851);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(54.2917f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(80.8125f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(69.8851f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(54.2917% 80.8125 69.8851)", value.getCssText());
		assertEquals("lab(54.2917% 80.8125 69.8851)", labColor.toString());
		assertEquals("lab(54.2917% 80.8125 69.8851)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#f00", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(54.2917f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(106.8390f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(40.8526f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testLABColorModel4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(54.237% 80.7 70);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(54.237f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(80.7f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(70f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(54.237% 80.7 70)", value.getCssText());
		assertEquals("lab(54.237% 80.7 70)", labColor.toString());
		assertEquals("lab(54.237% 80.7 70)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(99.884f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0.2073f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(99.88%, 0.21%, 0%)", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(54.237f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(106.82925f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(40.9387f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testLABColorModel5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(22.7233% 20.0904 -46.694);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(22.7233f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(20.0904f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-46.694f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(22.7233% 20.0904 -46.694)", value.getCssText());
		assertEquals("lab(22.7233% 20.0904 -46.694)", labColor.toString());
		assertEquals("lab(22.7233% 20.0904 -46.694)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(16.8108f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(17.9048f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(49.2062f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals("rgb(16.81%, 17.9%, 49.21%)", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(22.7233f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(50.8326f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(293.2801f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
		// Diff (example 29 from G. Sharma et al. 2004)
		style.setCssText("color: lab(23.0331% 14.973 -42.5619);");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor2 = (LABColorValue) color;
		LABColor lab2 = labColor2.getColor();
		assertNotNull(lab2);
		CSSPrimitiveValue lightness2 = lab2.getLightness();
		CSSPrimitiveValue a2 = lab2.getA();
		CSSPrimitiveValue b2 = lab2.getB();
		assertNotNull(lightness2);
		assertNotNull(a2);
		assertNotNull(b2);
		assertEquals(23.0331f, ((CSSTypedValue) lightness2).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(14.973f, ((CSSTypedValue) a2).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-42.5619f, ((CSSTypedValue) b2).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab2.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(2.0373f, labColor.deltaE2000(labColor2), 0.0001f);
		assertEquals(2.0373f, labColor2.deltaE2000(labColor), 0.0001f);
	}

	@Test
	public void testLABColorModel6() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(0.9311% 0.9912 -.8882);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.9311f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.9912f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.8882f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(0.9311% 0.9912 -0.8882)", value.getCssText());
		assertEquals("lab(0.9311% 0.9912 -0.8882)", labColor.toString());
		assertEquals("lab(.9311% .9912 -.8882)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(2.02766f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(1.04167f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(2.2085f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(2.03%, 1.04%, 2.21%)", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.9311f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1.33093f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(318.1369f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testLABColorModelAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(81.74% -45.224 65.5257/0.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(81.74f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(-45.224f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(65.5257f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(81.74% -45.224 65.5257 / 0.8)", value.getCssText());
		assertEquals("lab(81.74% -45.224 65.5257 / 0.8)", labColor.toString());
		assertEquals("lab(81.74% -45.224 65.5257/.8)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(55f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(88f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(25f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgba(55%, 88%, 25%, 0.8)", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.74f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(79.61675f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(124.6124f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testLABColorModelCalc() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(calc(2*36.9%) 18.438 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(Type.EXPRESSION, lightness.getPrimitiveType());
		assertEquals("calc(2*36.9%)", lightness.getCssText());
		assertEquals(18.438f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(calc(2*36.9%) 18.438 48.583)", value.getCssText());
		assertEquals("lab(calc(2*36.9%) 18.438 48.583)", labColor.toString());
		assertEquals("lab(calc(2*36.9%) 18.438 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(25,63,77)");
		try {
			labColor.deltaE2000(rgbColor2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			rgbColor2.deltaE2000(labColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		try {
			labColor.deltaE2000(hslColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			hslColor.deltaE2000(labColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testLABColorModelCalc2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(81.7395% calc(2*3) 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(Type.EXPRESSION, a.getPrimitiveType());
		assertEquals("calc(2*3)", a.getCssText());
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(81.7395% calc(2*3) 48.583)", value.getCssText());
		assertEquals("lab(81.7395% calc(2*3) 48.583)", labColor.toString());
		assertEquals("lab(81.7395% calc(2*3) 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testLABColorModelCalc3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(81.7395% 48.583 calc(2*16.43));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.EXPRESSION, b.getPrimitiveType());
		assertEquals("calc(2*16.43)", b.getCssText());
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(81.7395% 48.583 calc(2*16.43))", value.getCssText());
		assertEquals("lab(81.7395% 48.583 calc(2*16.43))", labColor.toString());
		assertEquals("lab(81.7395% 48.583 calc(2*16.43))", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testLABColorModelCalcAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(77.5325% -29.3512 75.0654/calc(2*0.34));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		LABColorValue labColor = (LABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		CSSPrimitiveValue alpha = lab.getAlpha();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertNotNull(alpha);
		assertEquals(77.5325f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(-29.3512f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(75.0654f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(Type.EXPRESSION, alpha.getPrimitiveType());
		assertEquals("calc(2*0.34)", alpha.getCssText());
		assertEquals("lab(77.5325% -29.3512 75.0654 / calc(2*0.34))", value.getCssText());
		assertEquals("lab(77.5325% -29.3512 75.0654 / calc(2*0.34))", labColor.toString());
		assertEquals("lab(77.5325% -29.3512 75.0654/calc(2*.34))", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(65.204f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(2.3f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals("calc(2*0.34)", rgb.getAlpha().getCssText());
		assertEquals("calc(2*.34)", rgb.getAlpha().getMinifiedCssText(""));
		assertEquals("rgba(65.2%, 80.5%, 2.3%, calc(2*0.34))", rgb.toString());
	}

	@Test
	public void testLABColorModelClampedRGBConversion() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(54.2324% 80.9288 70.8493);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB (100%, -0.8%, -1.4%)
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#f00", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0.334f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelClampedRGBConversion2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(72% 15.9649 130.0235);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(85.74f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.89f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(85.74%, 65.89%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.948f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelClampedRGBConversion3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:lab(59% -100.8654 78.8047);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.42f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(16.79f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 65.42%, 16.79%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.727f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelBadMixedType() {
		ColorValue value = new LABColorValue();
		try {
			value.setCssText("lch(67% 19.2 45.7deg)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testEqualsLAB() {
		ColorValue value = new LABColorValue();
		value.setCssText("lab(81.7395% -45.2202 65.5283)");
		ColorValue other = new LABColorValue();
		other.setCssText("lab(81.7395% -45.2202 65.5283)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertEquals(value.hashCode(), other.hashCode());
		assertFalse(value.equals(null));
		other.setCssText("lab(81.7395% -45.2202 65.5283/1)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		other.setCssText("lab(81.7395% -45.2202 65.5)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("lab(81.7395% -45.2202 65.5283/70%)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());

		ValueFactory factory = new ValueFactory();
		StyleValue val = factory.parseProperty("oklab(81.7395% -45.2202 65.5283)");
		assertFalse(value.equals(val));
		assertFalse(val.equals(value));
		assertFalse(value.hashCode() == val.hashCode());
	}

	@Test
	public void testCloneLAB() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lab(81.7395% -45.2202 65.5283);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.getColor().equals(clon.getColor()));
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testLCHColorModel() throws IOException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(67% 19.2 45.7deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(67f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(19.2f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(45.7f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertSame(lch.getAlpha(), lch.item(0));
		assertSame(lch.getLightness(), lch.item(1));
		assertSame(lch.getChroma(), lch.item(2));
		assertSame(lch.getHue(), lch.item(3));
		assertEquals(4, lch.getLength());
		assertNull(lch.item(4));
		//
		assertEquals(CSSColorValue.ColorModel.LCH, lch.getColorModel());
		assertEquals("lch", lch.getColorSpace());
		assertEquals("lch(67% 19.2 45.7)", lch.toString());
		assertEquals("lch(67% 19.2 45.7)", lch.toMinifiedString());
		// Set wrong values
		try {
			lchColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		try {
			lchColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		try {
			lchColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		try {
			lchColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		// Serialization
		assertEquals("lch(67% 19.2 45.7)", value.getCssText());
		assertEquals("lch(67% 19.2 45.7)", lchColor.toString());
		assertEquals("lch(67% 19.2 45.7)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		value.writeCssText(wri);
		assertEquals("lch(67% 19.2 45.7)", wri.toString());
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(75.7543f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(60.4072f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(54.5142f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		String s = rgb.toString();
		assertEquals("rgb(75.75%, 60.41%, 54.51%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 1.1e-2f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 1.1e-2f);
		assertFalse(lchColor.equals(srgbValue));
		assertFalse(srgbValue.equals(lchColor));
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(67f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(13.4096f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(13.7413f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		// HSL indirect conversion
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText(rgb.toString());
		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(16.7f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(30.5f, ((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(65.1f, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0.04f, hslColor.deltaE2000(lchColor), 0.01f);
		assertEquals(0.04f, lchColor.deltaE2000(hslColor), 0.01f);
		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(calc(25*2),63,77)");
		try {
			lchColor.deltaE2000(rgbColor2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			rgbColor2.deltaE2000(lchColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		// Component access
		assertSame(lch.getAlpha(), lchColor.getComponent(0));
		assertSame(lch.getLightness(), lchColor.getComponent(1));
		assertSame(lch.getChroma(), lchColor.getComponent(2));
		assertSame(lch.getHue(), lchColor.getComponent(3));
		assertNull(lchColor.getComponent(4));
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		// Set components
		lchColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		lchColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);
		//
		lchColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 27f));
		assertEquals(27f, ((TypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		lchColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 37f));
		assertEquals(37f, ((TypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-6f);
		//
		try {
			lchColor.setComponent(0, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			lchColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			lchColor.setComponent(1, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			lchColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			lchColor.setComponent(2, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			lchColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			lchColor.setComponent(3, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			lchColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testLCHColorModel2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(0.0424% 1.0058 285.2506deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.0424f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(1.0058f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(285.2506f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.0424% 1.0058 285.2506)", value.getCssText());
		assertEquals("lch(0.0424% 1.0058 285.2506)", lchColor.toString());
		assertEquals("lch(.0424% 1.0058 285.2506)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		String s = rgb.toString();
		assertEquals("rgb(0%, 0%, 1%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.001f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.001f);
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.0424f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.2645f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(-0.97038f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testLCHColorModel3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(0.0424% 1.0056 4.909deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.0424f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1.0056f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(4.909f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.0424% 1.0056 4.909)", value.getCssText());
		assertEquals("lch(0.0424% 1.0056 4.909)", lchColor.toString());
		assertEquals("lch(.0424% 1.0056 4.909)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(1.0942f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(9.13e-04f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals("rgb(1.09%, 0%, 0%)", rgb.toString());
		assertEquals("hsl(359.9, 100%, 0.5%)", ((RGBColor) rgb).toHSLColor().toString());
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.0424f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1.0019f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.0861f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testLCHColorModel4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(0.0424% 1.0056 5.1234grad);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.0424f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1.0056f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(5.1234f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_GRAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.0424% 1.0056 5.1234grad)", value.getCssText());
		assertEquals("lch(0.0424% 1.0056 5.1234grad)", lchColor.toString());
		assertEquals("lch(.0424% 1.0056 5.1234grad)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(1.093f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.006, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals("rgb(1.09%, 0%, 0.01%)", rgb.toString());
		assertEquals("hsl(359.7, 100%, 0.5%)", ((RGBColor) rgb).toHSLColor().toString());
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.0424f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1.0023f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.0808f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testLCHColorModel5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(22.7233% 50.8326 293.2801deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(22.7233f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(50.8326f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(293.2801f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Serialization
		assertEquals("lch(22.7233% 50.8326 293.2801)", value.getCssText());
		assertEquals("lch(22.7233% 50.8326 293.2801)", lchColor.toString());
		assertEquals("lch(22.7233% 50.8326 293.2801)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(16.8108f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(17.9048f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(49.2062f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals("rgb(16.81%, 17.9%, 49.21%)", rgb.toString());
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(22.7233f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(20.0904f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(-46.694f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		// Diff (example 29 from G. Sharma et al. 2004)
		//    L       a        b     aprime  Cprime   hprime hprime_av   G      T     SL     SC     SH     RT
		// 22.7233 20.0904 -46.6940 20.1424 50.8532 5.119642 5.085556 0.0026 0.3636 1.4014 3.1597 1.2617 -1.2537
		// 23.0331 14.9730 -42.5619 15.0118 45.1317 5.05147
		style.setCssText("color: lch(23.0331% 45.1188 289.3815deg);");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor2 = (LCHColorValue) color;
		LCHColor lch2 = lchColor2.getColor();
		assertNotNull(lch2);
		CSSPrimitiveValue lightness2 = lch2.getLightness();
		CSSPrimitiveValue chroma2 = lch2.getChroma();
		CSSPrimitiveValue hue2 = lch2.getHue();
		assertNotNull(lightness2);
		assertNotNull(chroma2);
		assertNotNull(hue2);
		assertEquals(23.0331f, ((CSSTypedValue) lightness2).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(45.1188f, ((CSSTypedValue) chroma2).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(289.3815f, ((CSSTypedValue) hue2).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch2.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(2.0373f, lchColor.deltaE2000(lchColor2), 0.0001f);
	}

	@Test
	public void testLCHColorModel6() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(0.9126% 0.956 0.822rad);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.9126f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.956f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0.822f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_RAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.9126% 0.956 0.822rad)", value.getCssText());
		assertEquals("lch(0.9126% 0.956 0.822rad)", lchColor.toString());
		assertEquals("lch(.9126% .956 .822rad)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(2.193f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1.0854f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.64646, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		String s = rgb.toString();
		assertEquals("rgb(2.19%, 1.09%, 0.65%)", s);
		assertEquals("hsl(17, 54.5%, 1.4%)", ((RGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted value
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, color.deltaE2000(srgbValue), 0.01f);
		assertEquals(0f, srgbValue.deltaE2000(color), 0.01f);
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.9126f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.6508f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.70028f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testLCHColorModelAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(29.189% 122.218 300.3190/.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(29.189f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(122.218f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(300.319f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(29.189% 122.218 300.319 / 0.8)", value.getCssText());
		assertEquals("lch(29.189% 122.218 300.319 / 0.8)", lchColor.toString());
		assertEquals("lch(29.189% 122.218 300.319/.8)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(8f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(95f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals("rgba(0%, 8%, 95%, 0.8)", rgb.toString());
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(29.189f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(61.6973f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(-105.5020f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testLCHColorModelCalc() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(calc(2*36.9%) 18.438 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(Type.EXPRESSION, lightness.getPrimitiveType());
		assertEquals("calc(2*36.9%)", lightness.getCssText());
		assertEquals(18.438f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(calc(2*36.9%) 18.438 48.583)", value.getCssText());
		assertEquals("lch(calc(2*36.9%) 18.438 48.583)", lchColor.toString());
		assertEquals("lch(calc(2*36.9%) 18.438 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(25,63,77)");
		try {
			lchColor.deltaE2000(rgbColor2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			rgbColor2.deltaE2000(lchColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		try {
			lchColor.deltaE2000(hslColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
		try {
			hslColor.deltaE2000(lchColor);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testLCHColorModelCalc2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(81.7395% calc(2*3) 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(Type.EXPRESSION, chroma.getPrimitiveType());
		assertEquals("calc(2*3)", chroma.getCssText());
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(81.7395% calc(2*3) 48.583)", value.getCssText());
		assertEquals("lch(81.7395% calc(2*3) 48.583)", lchColor.toString());
		assertEquals("lch(81.7395% calc(2*3) 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testLCHColorModelCalc3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(81.7395% 48.583 calc(2*16.43deg));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.EXPRESSION, hue.getPrimitiveType());
		assertEquals("calc(2*16.43deg)", hue.getCssText());
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(81.7395% 48.583 calc(2*16.43deg))", value.getCssText());
		assertEquals("lch(81.7395% 48.583 calc(2*16.43deg))", lchColor.toString());
		assertEquals("lch(81.7395% 48.583 calc(2*16.43deg))", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testLCHColorModelCalcAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(77.5325% 80.5996 111.356/calc(2*0.34));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		LCHColorValue lchColor = (LCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		CSSPrimitiveValue alpha = lch.getAlpha();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertNotNull(alpha);
		assertEquals(77.5325f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(80.5996f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(111.356f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(Type.EXPRESSION, alpha.getPrimitiveType());
		assertEquals("calc(2*0.34)", alpha.getCssText());
		assertEquals("lch(77.5325% 80.5996 111.356 / calc(2*0.34))", value.getCssText());
		assertEquals("lch(77.5325% 80.5996 111.356 / calc(2*0.34))", lchColor.toString());
		assertEquals("lch(77.5325% 80.5996 111.356/calc(2*.34))", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(65.205f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(2.3f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals("calc(2*0.34)", rgb.getAlpha().getCssText());
		assertEquals("calc(2*.34)", rgb.getAlpha().getMinifiedCssText(""));
		assertEquals("rgba(65.2%, 80.5%, 2.3%, calc(2*0.34))", rgb.toString());
	}

	@Test
	public void testLCHColorModelClampedRGBConversion() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(54.2324% 107.5597 41.2006);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB (100%, -0.8%, -1.4%)
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#f00", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0.334f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversion2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(72% 131 83);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(85.74f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.89f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(85.74%, 65.89%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.948f, color.deltaE2000(rgbValue), 0.001f);
		//
		LABColorValue labColor = rgbValue.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertEquals(72.08f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(10.612f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(74.444f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(72.08% 10.612 74.444)", labColor.getCssText());
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		LCHColor lch = lchColor.getColor();
		assertEquals(72.08f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(75.197f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(81.887f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(72.08% 75.197 81.887)", lchColor.getCssText());
	}

	@Test
	public void testLCHColorModelClampedRGBConversion3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:lch(59% 128 142);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.42f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(16.79f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 65.42%, 16.79%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.727f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversion4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:lch(78.75% 81.3 50.4)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(66.61f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(47.93f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(100%, 66.61%, 47.93%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(8.885f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversion5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:lch(78% 54.5 195)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(85.725f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(85.01f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 85.73%, 85.01%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(2.32f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversionVeryLargeChroma() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:lch(59% 16000 142);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.42f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(16.79f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 65.42%, 16.79%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(43.914f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelBadMixedType() {
		ColorValue value = new LCHColorValue();
		try {
			value.setCssText("lab(67% 19.2 14.8)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testEqualsLCH() {
		ColorValue value = new LCHColorValue();
		value.setCssText("lch(29.186% 122.2075 300.3188)");
		ColorValue other = new LCHColorValue();
		other.setCssText("lch(29.186% 122.2075 300.3188)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertEquals(value.hashCode(), other.hashCode());
		assertFalse(value.equals(null));
		other.setCssText("lch(29.186% 122.2075 300.3188/1)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		other.setCssText("lch(29.186% 122.2075 300.3)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("lch(29.186% 122.2075 300.3188/70%)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());

		ValueFactory factory = new ValueFactory();
		StyleValue val = factory.parseProperty("oklch(29.186% 122.2075 300.3188)");
		assertFalse(value.equals(val));
		assertFalse(val.equals(value));
		assertFalse(value.hashCode() == val.hashCode());
	}

	@Test
	public void testCloneLCH() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: lch(29.186% 122.2075 300.3);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.getColor().equals(clon.getColor()));
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

}
