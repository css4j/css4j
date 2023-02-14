/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.XYZColor;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorFunctionTest {

	private static CSSValueSyntax colorSyntax;
	private static CSSValueSyntax numberSyntax;

	@BeforeAll
	public static void setUpBeforeClass() {
		SyntaxParser syntaxParser = new SyntaxParser();
		colorSyntax = syntaxParser.parseSyntax("<color>");
		numberSyntax = syntaxParser.parseSyntax("<number>");
	}

	@Test
	public void testSRGB() throws IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(srgb 0.0314 0.24706 1); ");
		assertEquals("color(srgb 0.0314 0.24706 1)", style.getPropertyValue("color"));
		assertEquals("color: color(srgb 0.0314 0.24706 1); ", style.getCssText());
		//
		style.setCssText("color:color(srgb 0.0314 0.24706 1/ 0.5); ");
		assertEquals("color(srgb 0.0314 0.24706 1 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(srgb 0.0314 0.24706 1 / 0.5); ", style.getCssText());
		assertEquals("color:color(srgb .0314 .24706 1/.5)", style.getMinifiedCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(32);
		val.writeCssText(wri);
		assertEquals("color(srgb 0.0314 0.24706 1 / 0.5)", wri.toString());
		//
		assertEquals(Match.TRUE, val.matches(colorSyntax));
		assertEquals(Match.FALSE, val.matches(numberSyntax));
		//
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertSame(rgb, val.toRGBColor());
		assertEquals(ColorSpace.srgb, rgb.getColorSpace());
		assertEquals(ColorModel.RGB, rgb.getColorModel());
		assertEquals(0.0314, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		assertSame(rgb.getAlpha(), rgb.item(0));
		assertSame(rgb.getRed(), rgb.item(1));
		assertSame(rgb.getGreen(), rgb.item(2));
		assertSame(rgb.getBlue(), rgb.item(3));
		assertEquals(4, rgb.getLength());
		assertNull(rgb.item(4));
		//
		assertEquals("color(srgb 0.0314 0.24706 1 / 0.5)", rgb.toString());
		assertEquals("color(srgb .0314 .24706 1/.5)", rgb.toMinifiedString());
		//
		style.setCssText("color:color(srgb 0.442 0.7764 0.9976/0.5); ");
		assertEquals("color(srgb 0.442 0.7764 0.9976 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(srgb 0.442 0.7764 0.9976 / 0.5); ", style.getCssText());
		assertEquals("color:color(srgb .442 .7764 .9976/.5)", style.getMinifiedCssText());
		value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		val = (ColorValue) value;
		rgb = val.toRGBColor();
		assertEquals(0.442f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
	}

	@Test
	public void testSRGB2() {
		ValueFactory vf = new ValueFactory();
		ColorValue val = (ColorValue) vf.parseProperty("color(srgb 0 .333 .6542)");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val = (ColorValue) vf.parseProperty("COLOR(sRGB 0.4642 0.7764 0.9976/ 0)");
		assertNotNull(val);
		rgb = val.toRGBColor();
		assertEquals(0.4642f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7764f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.9976f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val = (ColorValue) vf.parseProperty("color(srgb 0.4642 0.7764 0.9976/1%)");
		assertEquals("color(srgb 0.4642 0.7764 0.9976 / 1%)", val.getCssText());
		assertEquals("color(srgb .4642 .7764 .9976/1%)", val.getMinifiedCssText("color"));
	}

	@Test
	public void testSRGB3() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(srgb 0.442 0.7764/0.22745)");
		assertEquals("color: color(srgb 0.442 0.7764 0 / 0.2274); ", style.getCssText());
		assertEquals("color:color(srgb .442 .7764 0/.2274)", style.getMinifiedCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(0.22745f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001);
		assertEquals("color(srgb 0.442 0.7764 0 / 0.2274)", style.getPropertyValue("color"));
		assertEquals("color(srgb .442 .7764 0/.2274)", val.getMinifiedCssText("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		//
		style.setCssText("color:color(srgb 0.4042 0.7764 -.1223)");
		assertEquals("color(srgb 0.4042 0.7764 -0.1223)", style.getPropertyValue("color"));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testSRGB4() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 0.442 0.7764 0.9976/0.5);");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color4 = rgbColor.toRGBColor();
		assertNotNull(color4);
		assertEquals(0.442f, ((CSSTypedValue) color4.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7764f, ((CSSTypedValue) color4.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.9976f, ((CSSTypedValue) color4.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, ((CSSTypedValue) color4.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		style.setCssText("color: color(srgb 0.78838 0.21333 0.0098); ");
		cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor2 = (ColorValue) cssColor;
		RGBColor color = (RGBColor) rgbColor2.toRGBColor();
		assertNotNull(color);
		assertEquals(0.78838f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.21333f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.0098f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		// DeltaE2000
		assertEquals(60.228f, rgbColor.deltaE2000(rgbColor2), 0.001f);
		assertEquals(60.228f, rgbColor2.deltaE2000(rgbColor), 0.001f);
		// Check component access
		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 0.6f);
		rgbColor2.setComponent(0, number);
		assertEquals(0.6f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.6f, ((CSSTypedValue) rgbColor2.getComponent(0)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 178f);
		rgbColor2.setComponent(3, number);
		rgbColor2.setComponent(4, number);
		assertEquals(178f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(178f, ((CSSTypedValue) rgbColor2.getComponent(3)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
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
		color.setRed(number);
		color.setGreen(number);
		color.setBlue(number);
		//
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
		color.setRed(number);
		color.setGreen(number);
		color.setBlue(number);
		//
		number.setFloatValue(CSSUnit.CSS_PERCENTAGE, 101f);
		try {
			color.setAlpha(number);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		color.setRed(number);
		color.setGreen(number);
		color.setBlue(number);
		//
		number.setFloatValue(CSSUnit.CSS_NUMBER, 1.1f);
		color.setRed(number); // allowed
		color.setGreen(number); // allowed
		color.setBlue(number); // allowed
	}

	@Test
	public void testVar() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(srgb var(--foo)/ 0.7); ");
		assertEquals("color(srgb var(--foo)/0.7)", style.getPropertyValue("color"));
		assertEquals("color: color(srgb var(--foo)/0.7); ", style.getCssText());
		assertEquals("color:color(srgb var(--foo)/0.7)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
	}

	@Test
	public void testCalc() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(srgb calc(30%) calc(15%) calc(99%)/ 0.7); ");
		assertEquals("color(srgb calc(30%) calc(15%) calc(99%) / 0.7)", style.getPropertyValue("color"));
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
	}

	@Test
	public void testEquals() {
		ValueFactory factory = new ValueFactory();
		ColorValue value = (ColorValue) factory.parseProperty("color(srgb 0.442 0.7764 0.9976)");
		ColorValue other = (ColorValue) factory.parseProperty("color(srgb 0.442 0.7764 0.9976)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		assertFalse(value.equals(null));
		//
		other.setCssText("color(srgb 0.442 0.7764 0.9976/1.0)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(rec2020 0.442 0.7764 0.9976/1.0)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(srgb 0.442 0.7764 0.9975)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(srgb 0.442 0.7763 0.9976)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(srgb 0.4426 0.7764 0.9976)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(srgb 0.442 0.7764 0.9976/0.8)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other.setCssText("color(srgb 0.442 0.7764 0.9976 0 0)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		//
		value = (ColorValue) factory.parseProperty("color(srgb 49.31% 75.22% 18.64%)");
		RGBColorValue rgbColor = new RGBColorValue();
		rgbColor.setCssText("rgb(49.31% 75.22% 18.64%)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		assertFalse(value.getColor().equals(other.getColor()));
		assertFalse(other.getColor().equals(value.getColor()));
		//
		other = (ColorValue) factory.parseProperty("color(srgb 0.4931 0.7522 0.1864)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testCloneSRGB() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 0.15142 0.77641 0.99768);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getColor(), clon.getColor());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
	}

	@Test
	public void testCloneSRGBA() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 0.41422 0.776043 0.82976/0.5);");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		ColorValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getColor(), clon.getColor());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertTrue(value.toRGBColor().hashCode() == clon.toRGBColor().hashCode());
		//
		CSSColor rgb = value.getColor();
		CSSColor rgbclon = rgb.clone();
		assertEquals(ColorSpace.srgb, rgbclon.getColorSpace());
		assertEquals(ColorModel.RGB, rgbclon.getColorModel());
		assertEquals("color(srgb 0.41422 0.776043 0.82976 / 0.5)", rgbclon.toString());
	}

	@Test
	public void testRGBColorCalc() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 0.1112 0.7743 calc(0.88632*0.5)); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		assertEquals(CSSColorValue.ColorModel.RGB, ((ColorValue) cssColor).getColorModel());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0.1112f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7743f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(Type.EXPRESSION, color.getBlue().getPrimitiveType());
		//
		try {
			rgbColor.toRGBColor();
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
		ColorValue rgbColor2 = (ColorValue) new ValueFactory()
				.parseProperty("color(srgb 0.88757 0.32344 .98833)");
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
		style.setCssText("color:color(srgb 0% 8% 95%/ 0.8); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(8f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(95f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(0.8f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(29.189f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(61.697f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(-105.502f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 1e-3f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 1e-3f);
		//
		LCHColorValue lchColor = rgbColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(29.189f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(122.218f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(300.319f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-3f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 1e-3f);
		//
		HSLColorValue hslVal = rgbColor.toHSLColorValue();
		assertEquals("hsla(234.9, 100%, 47.5%, 0.8)", hslVal.getCssText());
		assertEquals(0f, rgbColor.deltaE2000(hslVal), 0.03f);
		assertEquals(0f, hslVal.deltaE2000(rgbColor), 0.03f);
	}

	@Test
	public void testRGBAColorConversions2() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 55% 88% 25%); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(55f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(88f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(25f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(81.74f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(-45.224f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(65.5257f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 1e-3f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 1e-3f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(81.74f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(79.6168f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(124.6124f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 1e-3f);
	}

	@Test
	public void testRGBAColorConversions3() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 0% 0% 1%);");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(0f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
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
		assertEquals(0f, rgbColor.deltaE2000(labColor), 1e-3f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 1e-3f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(0.04239f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(1.00581f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(285.2506f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 1e-3f);
	}

	@Test
	public void testRGBAColorConversions4() throws CSSPropertyValueException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:color(srgb 50% 47% 89%); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(50f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(47f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(89f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(54.913f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(23.595f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(-54.491f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 1e-3f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 1e-3f);
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(54.913f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(59.380f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(293.413f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-3f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 1e-3f);
	}

	@Test
	public void testRGBATransparentColor() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(srgb 0 0 0 / 0); ");
		assertEquals("color(srgb 0 0 0 / 0)", style.getPropertyValue("color"));
		assertEquals("color: color(srgb 0 0 0 / 0); ", style.getCssText());
		assertEquals("color:color(srgb 0 0 0/0)", style.getMinifiedCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("color(srgb 0 0 0 / 0)", rgb.toString());
		//
		style.setCssText("color:color(srgb 0 0 0/0.0); ");
		assertEquals("color(srgb 0 0 0 / 0)", style.getPropertyValue("color"));
		assertEquals("color: color(srgb 0 0 0 / 0); ", style.getCssText());
		assertEquals("color:color(srgb 0 0 0/0)", style.getMinifiedCssText());
		val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("color(srgb 0 0 0 / 0)", rgb.toString());
	}

	@Test
	public void testP3() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(display-p3 0.0314 0.24706 1); ");
		assertEquals("color(display-p3 0.0314 0.24706 1)", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.0314 0.24706 1); ", style.getCssText());
		//
		style.setCssText("color:color(display-p3 0.0314 0.24706 1/ 0.5); ");
		assertEquals("color(display-p3 0.0314 0.24706 1 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.0314 0.24706 1 / 0.5); ", style.getCssText());
		assertEquals("color:color(display-p3 .0314 .24706 1/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorModel.RGB, rgb.getColorModel());
		assertEquals(ColorSpace.display_p3, rgb.getColorSpace());
		assertEquals(0.0314f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.24706f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(display-p3 0.0314 0.24706 1 / 0.5)", rgb.toString());
		assertEquals("color(display-p3 .0314 .24706 1/.5)", rgb.toMinifiedString());
		//
		LABColorValue labValue = val.toLABColorValue();
		assertEquals("lab(38.12% 48.669 -103.504 / 0.5)", labValue.getCssText());
		assertEquals(0f, val.deltaE2000(labValue), 1e-3f);
		assertEquals(0f, labValue.deltaE2000(val), 1e-3f);
		//
		LCHColorValue lchColor = val.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(38.1228f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(114.3753f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(295.1838f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(0.5f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, val.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(val), 1e-3f);
		//
		RGBAColor srgb = val.toRGBColor();
		String s = srgb.toString();
		assertEquals("rgba(0%, 25.2%, 100%, 0.5)", s);
		assertEquals("rgba(0%,25.2%,100%,.5)", srgb.toMinifiedString());
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0.981f, val.deltaE2000(srgbValue), 1e-3f);
		assertEquals(0.981f, srgbValue.deltaE2000(val), 1e-3f);
		assertFalse(rgb.equals(srgb));
		assertFalse(srgb.equals(rgb));
		//
		ColorValue srgbLab = (ColorValue) new ValueFactory().parseProperty(labValue.toRGBColor().toString());
		assertEquals(0f, srgbLab.deltaE2000(srgbValue), 1e-3f);
		assertEquals(0f, srgbValue.deltaE2000(srgbLab), 1e-3f);
		assertTrue(srgbLab.equals(srgbValue));
		assertTrue(srgbValue.equals(srgbLab));
		//
		style.setCssText("color:color(display-p3 0.442 0.7764 0.9976/0.8); ");
		assertEquals("color(display-p3 0.442 0.7764 0.9976 / 0.8)", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.442 0.7764 0.9976 / 0.8); ", style.getCssText());
		assertEquals("color:color(display-p3 .442 .7764 .9976/.8)", style.getMinifiedCssText());
		StyleValue value2 = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value2.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value2.getPrimitiveType());
		ColorValue val2 = (ColorValue) value2;
		rgb = (RGBAColor) val2.getColor();
		assertEquals(ColorSpace.display_p3, rgb.getColorSpace());
		assertEquals(0.442f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7764f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.9976f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(41.449f, val2.deltaE2000(val), 1e-3f);
		assertEquals(41.449f, val.deltaE2000(val2), 1e-3f);
		//
		assertSame(rgb.getAlpha(), rgb.item(0));
		assertSame(rgb.getRed(), rgb.item(1));
		assertSame(rgb.getGreen(), rgb.item(2));
		assertSame(rgb.getBlue(), rgb.item(3));
		//
		srgb = val2.toRGBColor();
		s = srgb.toString();
		assertEquals("rgba(30.19%, 78.66%, 100%, 0.8)", s);
		srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(1.146f, val2.deltaE2000(srgbValue), 1e-3f);
		assertEquals(1.146f, srgbValue.deltaE2000(val2), 1e-3f);
		// Set components
		val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		srgb = (RGBAColor) val.getColor();
		assertEquals(0.427f, ((TypedValue) srgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.827f));
		assertEquals(0.827f, ((TypedValue) srgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.927f));
		assertEquals(0.927f, ((TypedValue) srgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.987f));
		assertEquals(0.987f, ((TypedValue) srgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		try {
			val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.8f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.8f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.8f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.8f));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val2.setCssText("rgba(30.19%, 78.66%, 100%, 0.8)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testP3_Calc() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(display-p3 0.0314 0.24706 calc(1/2)); ");
		assertEquals("color(display-p3 0.0314 0.24706 calc(1/2))", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.0314 0.24706 calc(1/2)); ", style.getCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.display_p3, rgb.getColorSpace());
		//
		CSSColor clon = rgb.clone();
		assertEquals(ColorSpace.display_p3, clon.getColorSpace());
		assertEquals(ColorModel.RGB, clon.getColorModel());
		assertEquals("color(display-p3 0.0314 0.24706 calc(1/2))", clon.toString());
		//
		try {
			val.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testP3_OneArgument() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(display-p3 0.0314); ");
		assertEquals("color(display-p3 0.0314 0 0)", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.0314 0 0); ", style.getCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.display_p3, rgb.getColorSpace());
		assertEquals(0.0314f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(display-p3 0.0314 0 0)", rgb.toString());
		assertEquals("color(display-p3 .0314 0 0)", rgb.toMinifiedString());
	}

	@Test
	public void testP3_TwoArguments() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(display-p3 0.0314 0.7241); ");
		assertEquals("color(display-p3 0.0314 0.7241 0)", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.0314 0.7241 0); ", style.getCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.display_p3, rgb.getColorSpace());
		assertEquals(0.0314f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7241f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(display-p3 0.0314 0.7241 0)", rgb.toString());
		assertEquals("color(display-p3 .0314 .7241 0)", rgb.toMinifiedString());
		//
		rgb = val.toRGBColor();
		String s = rgb.toString();
		assertEquals("rgb(0%, 72.42%, 0%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(4.924f, val.deltaE2000(srgbValue), 1e-3f);
		assertEquals(4.924f, srgbValue.deltaE2000(val), 1e-3f);
	}

	@Test
	public void testP3_InSRGBGamut() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(display-p3 0.314 0.6241 0.2956); ");
		assertEquals("color(display-p3 0.314 0.6241 0.2956)", style.getPropertyValue("color"));
		assertEquals("color: color(display-p3 0.314 0.6241 0.2956); ", style.getCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.display_p3, rgb.getColorSpace());
		assertEquals(0.314f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.6241f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.2956f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(display-p3 0.314 0.6241 0.2956)", rgb.toString());
		assertEquals("color(display-p3 .314 .6241 .2956)", rgb.toMinifiedString());
		//
		rgb = val.toRGBColor(false);
		String s = rgb.toString();
		assertEquals("rgb(15.3%, 63.32%, 24.56%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, val.deltaE2000(srgbValue), 5e-3f);
		assertEquals(0f, srgbValue.deltaE2000(val), 5e-3f);
	}

	@Test
	public void testA98_RGB() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(a98-rgb 0.0314 0.24706 1); ");
		assertEquals("color(a98-rgb 0.0314 0.24706 1)", style.getPropertyValue("color"));
		assertEquals("color: color(a98-rgb 0.0314 0.24706 1); ", style.getCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertEquals(ColorModel.RGB, val.getColorModel());
		//
		style.setCssText("color:color(a98-rgb 0.0314 0.24706 1/ 0.5); ");
		assertEquals("color(a98-rgb 0.0314 0.24706 1 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(a98-rgb 0.0314 0.24706 1 / 0.5); ", style.getCssText());
		assertEquals("color:color(a98-rgb .0314 .24706 1/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.a98_rgb, rgb.getColorSpace());
		assertEquals(0.0314, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.24706, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(a98-rgb 0.0314 0.24706 1 / 0.5)", rgb.toString());
		assertEquals("color(a98-rgb .0314 .24706 1/.5)", rgb.toMinifiedString());
		//
		LABColorValue labValue = val.toLABColorValue();
		assertEquals("lab(36.42% 48.244 -103.15 / 0.5)", labValue.getCssText());
		assertEquals(0f, val.deltaE2000(labValue), 1e-3f);
		assertEquals(0f, labValue.deltaE2000(val), 1e-3f);
		//
		LCHColorValue lchColor = val.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(36.4194f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(113.8740f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(295.0658f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(0.5f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, val.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(val), 1e-3f);
		//
		RGBAColor srgb = val.toRGBColor();
		String s = srgb.toString();
		assertEquals("rgba(0%, 23.8%, 100%, 0.5)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(1.0526f, val.deltaE2000(srgbValue), 1e-3f);
		assertEquals(1.0526f, srgbValue.deltaE2000(val), 1e-3f);
		//
		style.setCssText("color:color(a98-rgb 0.442 0.7764 0.9976/0.8); ");
		assertEquals("color(a98-rgb 0.442 0.7764 0.9976 / 0.8)", style.getPropertyValue("color"));
		assertEquals("color: color(a98-rgb 0.442 0.7764 0.9976 / 0.8); ", style.getCssText());
		assertEquals("color:color(a98-rgb .442 .7764 .9976/.8)", style.getMinifiedCssText());
		StyleValue value2 = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value2.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value2.getPrimitiveType());
		ColorValue val2 = (ColorValue) value2;
		rgb = (RGBAColor) val2.getColor();
		assertEquals(ColorSpace.a98_rgb, rgb.getColorSpace());
		assertEquals(0.442f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7764f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.9976f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(44.228f, val2.deltaE2000(val), 1e-3f);
		assertEquals(44.228f, val.deltaE2000(val2), 1e-3f);
		srgb = val2.toRGBColor();
		s = srgb.toString();
		assertEquals("rgba(4.95%, 78.16%, 100%, 0.8)", s);
		srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0.334f, val2.deltaE2000(srgbValue), 1e-3f);
		assertEquals(0.334f, srgbValue.deltaE2000(val2), 1e-3f);
	}

	@Test
	public void testProPhoto_RGB() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(prophoto-rgb 0.4842 0.7107 0.2492); ");
		assertEquals("color(prophoto-rgb 0.4842 0.7107 0.2492)", style.getPropertyValue("color"));
		assertEquals("color: color(prophoto-rgb 0.4842 0.7107 0.2492); ", style.getCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertEquals(ColorModel.RGB, val.getColorModel());
		//
		style.setCssText("color:color(prophoto-rgb 0.4842 0.7107 0.2492/ 0.5); ");
		assertEquals("color(prophoto-rgb 0.4842 0.7107 0.2492 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(prophoto-rgb 0.4842 0.7107 0.2492 / 0.5); ", style.getCssText());
		assertEquals("color:color(prophoto-rgb .4842 .7107 .2492/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.prophoto_rgb, rgb.getColorSpace());
		assertEquals(0.4842f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7107f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.2492f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(prophoto-rgb 0.4842 0.7107 0.2492 / 0.5)", rgb.toString());
		assertEquals("color(prophoto-rgb .4842 .7107 .2492/.5)", rgb.toMinifiedString());
		//
		LABColorValue labValue = val.toLABColorValue();
		assertEquals("lab(73.74% -51.1 67.842 / 0.5)", labValue.getCssText());
		assertEquals(0f, val.deltaE2000(labValue), 1e-3f);
		assertEquals(0f, labValue.deltaE2000(val), 1e-3f);
		//
		LCHColorValue lchColor = val.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(73.74345f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(84.9341f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(126.9878f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(0.5f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, val.deltaE2000(lchColor), 1e-3f);
		assertEquals(0f, lchColor.deltaE2000(val), 1e-3f);
		//
		RGBAColor srgb = val.toRGBColor();
		String s = srgb.toString();
		assertEquals("rgba(40%, 80%, 10.01%, 0.5)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, val.deltaE2000(srgbValue), 1e-3f);
		assertEquals(0f, srgbValue.deltaE2000(val), 1e-3f);
		//
		style.setCssText("color:color(prophoto-rgb 0.442 0.7764 0.9976/0.8); ");
		assertEquals("color(prophoto-rgb 0.442 0.7764 0.9976 / 0.8)", style.getPropertyValue("color"));
		assertEquals("color: color(prophoto-rgb 0.442 0.7764 0.9976 / 0.8); ", style.getCssText());
		assertEquals("color:color(prophoto-rgb .442 .7764 .9976/.8)", style.getMinifiedCssText());
		StyleValue value2 = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value2.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value2.getPrimitiveType());
		ColorValue val2 = (ColorValue) value2;
		//
		LABColorValue labValue2 = val2.toLABColorValue();
		assertEquals("lab(77.15% -62.524 -39.117 / 0.8)", labValue2.getCssText());
		assertEquals(0f, val2.deltaE2000(labValue2), 1e-3f);
		assertEquals(0f, labValue2.deltaE2000(val2), 1e-3f);
		//
		rgb = (RGBAColor) val2.getColor();
		assertEquals(ColorSpace.prophoto_rgb, rgb.getColorSpace());
		assertEquals(0.442f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7764f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.9976f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(46.079f, val2.deltaE2000(val), 1e-3f);
		assertEquals(46.079f, val.deltaE2000(val2), 1e-3f);
		srgb = val2.toRGBColor(true);
		s = srgb.toString();
		assertEquals("rgba(0%, 83.74%, 93.99%, 0.8)", s);
		srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(7.613f, val2.deltaE2000(srgbValue), 1e-3f);
		assertEquals(7.613f, srgbValue.deltaE2000(val2), 1e-3f);
	}

	@Test
	public void testRec2020() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(rec2020 0.428 0.726 0.348); ");
		assertEquals("color(rec2020 0.428 0.726 0.348)", style.getPropertyValue("color"));
		assertEquals("color: color(rec2020 0.428 0.726 0.348); ", style.getCssText());
		//
		style.setCssText("color:color(rec2020 0.428 0.726 0.348/ 0.5); ");
		assertEquals("color(rec2020 0.428 0.726 0.348 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(rec2020 0.428 0.726 0.348 / 0.5); ", style.getCssText());
		assertEquals("color:color(rec2020 .428 .726 .348/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.rec2020, rgb.getColorSpace());
		assertEquals(0.428f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.726f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.348f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(rec2020 0.428 0.726 0.348 / 0.5)", rgb.toString());
		assertEquals("color(rec2020 .428 .726 .348/.5)", rgb.toMinifiedString());
		//
		LABColorValue labValue = val.toLABColorValue();
		assertEquals("lab(70.72% -60.968 43.322 / 0.5)", labValue.getCssText());
		assertEquals(0f, val.deltaE2000(labValue), 1e-3f);
		assertEquals(0f, labValue.deltaE2000(val), 1e-3f);
		//
		RGBAColor srgb = val.toRGBColor(false);
		String s = srgb.toString();
		assertEquals("rgba(4.95%, 78.19%, 34.05%, 0.5)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, val.deltaE2000(srgbValue), 3e-3f);
		assertEquals(0f, srgbValue.deltaE2000(val), 3e-3f);
		//
		style.setCssText("color:color(rec2020 0.4856 0.7085 0.4213 / 0.8); ");
		assertEquals("color(rec2020 0.4856 0.7085 0.4213 / 0.8)", style.getPropertyValue("color"));
		assertEquals("color: color(rec2020 0.4856 0.7085 0.4213 / 0.8); ", style.getCssText());
		assertEquals("color:color(rec2020 .4856 .7085 .4213/.8)", style.getMinifiedCssText());
		StyleValue value2 = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value2.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value2.getPrimitiveType());
		ColorValue val2 = (ColorValue) value2;
		rgb = (RGBAColor) val2.getColor();
		assertEquals(ColorSpace.rec2020, rgb.getColorSpace());
		assertEquals(0.4856f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.7085f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.4213f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(4.823f, val2.deltaE2000(val), 1e-3f);
		assertEquals(4.823f, val.deltaE2000(val2), 1e-3f);
		//
		srgb = val2.toRGBColor(false);
		s = srgb.toString();
		assertEquals("rgba(34.66%, 76.05%, 43.31%, 0.8)", s);
		srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, val2.deltaE2000(srgbValue), 2e-3f);
		assertEquals(0f, srgbValue.deltaE2000(val2), 2e-3f);
		//
		HSLColorValue hslVal = val2.toHSLColorValue();
		assertEquals("hsla(132.5, 46.4%, 55.4%, 0.8)", hslVal.getCssText());
		assertEquals(0f, val2.deltaE2000(hslVal), 0.05f);
		assertEquals(0f, hslVal.deltaE2000(val2), 0.05f);
	}

	@Test
	public void testRec2020_2() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(rec2020 0.928 0.986 0.048); ");
		assertEquals("color(rec2020 0.928 0.986 0.048)", style.getPropertyValue("color"));
		assertEquals("color: color(rec2020 0.928 0.986 0.048); ", style.getCssText());
		assertEquals("color:color(rec2020 .928 .986 .048)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.RGB, val.getColorModel());
		RGBAColor rgb = (RGBAColor) val.getColor();
		assertEquals(ColorSpace.rec2020, rgb.getColorSpace());
		assertEquals(0.928f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals("color(rec2020 0.928 0.986 0.048)", rgb.toString());
		assertEquals("color(rec2020 .928 .986 .048)", rgb.toMinifiedString());
		//
		LABColorValue labValue = val.toLABColorValue();
		assertEquals("lab(95.87% -24.065 122.484)", labValue.getCssText());
		assertEquals(0f, val.deltaE2000(labValue), 1e-3f);
		assertEquals(0f, labValue.deltaE2000(val), 1e-3f);
		//
		RGBAColor srgb = val.toRGBColor();
		String s = srgb.toString();
		assertEquals("rgb(95.37%, 98.94%, 0%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(5.292f, val.deltaE2000(srgbValue), 1e-3f);
		assertEquals(5.292f, srgbValue.deltaE2000(val), 1e-3f);
	}

	@Test
	public void testCustomProfile() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(--my-profile 0.428 0.726 0.348); ");
		assertEquals("color(--my-profile 0.428 0.726 0.348)", style.getPropertyValue("color"));
		assertEquals("color: color(--my-profile 0.428 0.726 0.348); ", style.getCssText());
		//
		style.setCssText("color:color(--my-profile 0.428 0.726 0.348/ 0.5); ");
		assertEquals("color(--my-profile 0.428 0.726 0.348 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(--my-profile 0.428 0.726 0.348 / 0.5); ", style.getCssText());
		assertEquals("color:color(--my-profile .428 .726 .348/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.PROFILE, val.getColorModel());
		CSSColor color = val.getColor();
		assertEquals(ColorModel.PROFILE, color.getColorModel());
		assertEquals("--my-profile", color.getColorSpace());
		assertEquals(0.428f, ((CSSTypedValue) color.item(1)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.726f, ((CSSTypedValue) color.item(2)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.348f, ((CSSTypedValue) color.item(3)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) color.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		assertSame(color.getAlpha(), color.item(0));
		assertEquals(4, color.getLength());
		//
		assertEquals("color(--my-profile 0.428 0.726 0.348 / 0.5)", color.toString());
		assertEquals("color(--my-profile .428 .726 .348/.5)", color.toMinifiedString());
		//
		CSSColor clon = color.clone();
		assertEquals("--my-profile", clon.getColorSpace());
		assertEquals(ColorModel.PROFILE, clon.getColorModel());
		assertEquals("color(--my-profile 0.428 0.726 0.348 / 0.5)", clon.toString());
		//
		try {
			val.setCssText("color(xyz 0.106 0.274 0.413)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		// Custom profiles are case sensitive
		try {
			val.setCssText("color(--My-Profile 0.106 0.274 0.413)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		// This is accepted
		val.setCssText("color(--my-profile 0.5271 0.0879 0.6543)");
		assertEquals("color(--my-profile 0.5271 0.0879 0.6543)", val.getCssText());
		assertEquals("color(--my-profile .5271 .0879 .6543)", val.getMinifiedCssText("color"));
		//
		try {
			val.toLABColorValue();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
		}
		//
		try {
			val.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
		}
		//
		ColorValue rgbVal = (ColorValue) new ValueFactory().parseProperty("color(srgb 0.106 0.074 0.413)");
		try {
			val.deltaE2000(rgbVal);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
		}
		try {
			rgbVal.deltaE2000(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
		}
	}

	@Test
	public void testCustomProfile4components() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(--my-profile 0.428 0.726 0.348 0.2241/ 0.5); ");
		assertEquals("color(--my-profile 0.428 0.726 0.348 0.2241 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(--my-profile 0.428 0.726 0.348 0.2241 / 0.5); ", style.getCssText());
		assertEquals("color:color(--my-profile .428 .726 .348 .2241/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.PROFILE, val.getColorModel());
		CSSColor color = val.getColor();
		assertEquals("--my-profile", color.getColorSpace());
		assertEquals(0.428f, ((CSSTypedValue) color.item(1)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.726f, ((CSSTypedValue) color.item(2)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.348f, ((CSSTypedValue) color.item(3)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.2241f, ((CSSTypedValue) color.item(4)).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) color.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		assertSame(color.getAlpha(), color.item(0));
		assertEquals(5, color.getLength());
	}

	@Test
	public void testEqualsCustomProfile() {
		ValueFactory factory = new ValueFactory();
		ColorValue value = (ColorValue) factory.parseProperty("color(--my-profile 0.442 0.7764 0.9976)");
		ColorValue other = (ColorValue) factory.parseProperty("color(--my-profile 0.442 0.7764 0.9976)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		//
		other.setCssText("color(--my-profile 0.442 0.7764 0.9976/1.0)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(--other-profile 0.442 0.7764 0.9976/1.0)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(--my-profile 0.442 0.7764 0.9976/0.8)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(prophoto-rgb 0.442 0.7764 0.9976)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(--my-profile 0.442 0.7764 0.99763)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(--my-profile 0.442 0.77644 0.9976)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) factory.parseProperty("color(--my-profile 0.4425 0.7764 0.9976)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testXYZ() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(xyz 0.173 0.102 0.786); ");
		assertEquals("color(xyz 0.173 0.102 0.786)", style.getPropertyValue("color"));
		assertEquals("color: color(xyz 0.173 0.102 0.786); ", style.getCssText());
		//
		style.setCssText("color:color(xyz 0.173 0.102 0.786/.5); ");
		assertEquals("color(xyz 0.173 0.102 0.786 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: color(xyz 0.173 0.102 0.786 / 0.5); ", style.getCssText());
		assertEquals("color:color(xyz .173 .102 .786/.5)", style.getMinifiedCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		//
		assertEquals(Match.TRUE, value.matches(colorSyntax));
		assertEquals(Match.FALSE, value.matches(numberSyntax));
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.XYZ, val.getColorModel());
		XYZColor xyz = (XYZColor) val.getColor();
		assertEquals(ColorModel.XYZ, xyz.getColorModel());
		assertEquals(ColorSpace.xyz, xyz.getColorSpace());
		assertEquals(0.173f, ((CSSTypedValue) xyz.getX()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.102f, ((CSSTypedValue) xyz.getY()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.786f, ((CSSTypedValue) xyz.getZ()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.5f, (((CSSTypedValue) xyz.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		assertSame(xyz.getAlpha(), xyz.item(0));
		assertSame(xyz.getX(), xyz.item(1));
		assertSame(xyz.getY(), xyz.item(2));
		assertSame(xyz.getZ(), xyz.item(3));
		assertEquals(4, xyz.getLength());
		assertNull(xyz.item(4));
		//
		assertEquals("color(xyz 0.173 0.102 0.786 / 0.5)", xyz.toString());
		assertEquals("color(xyz .173 .102 .786/.5)", xyz.toMinifiedString());
		//
		CSSColor clon = xyz.clone();
		assertEquals(ColorSpace.xyz, clon.getColorSpace());
		assertEquals(ColorModel.XYZ, clon.getColorModel());
		assertEquals("color(xyz 0.173 0.102 0.786 / 0.5)", clon.toString());
		//
		try {
			val.setCssText("transparent");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		//
		try {
			val.setCssText("color(srgb 0.106 0.074 0.413)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		//
		try {
			val.setCssText("lab(38.2% 48.391 -103.334 / 0.5)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		//
		LABColorValue labValue = val.toLABColorValue();
		assertEquals("lab(38.2% 48.391 -103.334 / 0.5)", labValue.getCssText());
		assertEquals(0f, val.deltaE2000(labValue), 1e-3f);
		assertEquals(0f, labValue.deltaE2000(val), 1e-3f);
		//
		assertFalse(val.equals(labValue));
		assertFalse(labValue.equals(val));
		assertFalse(val.getColor().equals(labValue.getColor()));
		assertFalse(labValue.getColor().equals(val.getColor()));
		//
		RGBAColor srgb = val.toRGBColor();
		String s = srgb.toString();
		assertEquals("rgba(0%, 25.38%, 100%, 0.5)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0.972f, val.deltaE2000(srgbValue), 1e-3f);
		assertEquals(0.972f, srgbValue.deltaE2000(val), 1e-3f);
		//
		style.setCssText("color:color(xyz 0.106 0.074 0.413/0.8); ");
		assertEquals("color(xyz 0.106 0.074 0.413 / 0.8)", style.getPropertyValue("color"));
		assertEquals("color: color(xyz 0.106 0.074 0.413 / 0.8); ", style.getCssText());
		assertEquals("color:color(xyz .106 .074 .413/.8)", style.getMinifiedCssText());
		StyleValue value2 = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value2.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value2.getPrimitiveType());
		ColorValue val2 = (ColorValue) value2;
		//
		assertTrue(val.equals(val));
		assertFalse(val.equals(val2));
		assertFalse(val2.equals(val));
		assertFalse(val2.getColor().equals(xyz));
		assertFalse(val.equals(null));
		//
		xyz = (XYZColor) val2.getColor();
		assertEquals(ColorSpace.xyz, xyz.getColorSpace());
		assertEquals(0.106f, ((CSSTypedValue) xyz.getX()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.074f, ((CSSTypedValue) xyz.getY()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.413f, ((CSSTypedValue) xyz.getZ()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.8f, ((CSSTypedValue) xyz.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(6.854f, val2.deltaE2000(val), 1e-3f);
		assertEquals(6.854f, val.deltaE2000(val2), 1e-3f);
		//
		srgb = val2.toRGBColor(false);
		s = srgb.toString();
		assertEquals("rgba(9.93%, 25.24%, 78.03%, 0.8)", s);
		srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, val2.deltaE2000(srgbValue), 5e-3f);
		assertEquals(0f, srgbValue.deltaE2000(val2), 5e-3f);
		//
		assertFalse(val2.getColor().equals(srgb));
		assertFalse(srgb.equals(val2.getColor()));
		//
		HSLColorValue hslVal = val2.toHSLColorValue();
		assertEquals("hsla(226.5, 77.4%, 44%, 0.8)", hslVal.getCssText());
		assertEquals(0f, val2.deltaE2000(hslVal), 0.04f);
		assertEquals(0f, hslVal.deltaE2000(val2), 0.04f);
		// Set components
		val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		xyz = (XYZColor) val.getColor();
		assertEquals(0.427f, ((TypedValue) xyz.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.827f));
		assertEquals(0.827f, ((TypedValue) xyz.getX()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.927f));
		assertEquals(0.927f, ((TypedValue) xyz.getY()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.987f));
		assertEquals(0.987f, ((TypedValue) xyz.getZ()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
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
			val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		//
		try {
			val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		// Valid setCssText()
		val.setCssText("color(xyz 0.106 0.074 0.413)");
		assertEquals("color(xyz 0.106 0.074 0.413)", val.getCssText());
		// Test equals()
		ColorValue other = (ColorValue) new ValueFactory().parseProperty("color(xyz 0.106 0.074 0.413)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertTrue(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) new ValueFactory().parseProperty("color(srgb 0.106 0.074 0.413)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) new ValueFactory().parseProperty("color(xyz 0.106 0.074 0.4131)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) new ValueFactory().parseProperty("color(xyz 0.106 0.0743 0.413)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) new ValueFactory().parseProperty("color(xyz 0.1067 0.074 0.413)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		//
		other = (ColorValue) new ValueFactory().parseProperty("color(xyz 0.106 0.074 0.413/0.2)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testXYZ_OneArgument() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(xyz 0.173); ");
		assertEquals("color(xyz 0.173 0 0)", style.getPropertyValue("color"));
		assertEquals("color: color(xyz 0.173 0 0); ", style.getCssText());
		assertEquals("color:color(xyz .173 0 0)", style.getMinifiedCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.XYZ, val.getColorModel());
		XYZColor xyz = (XYZColor) val.getColor();
		assertEquals(ColorSpace.xyz, xyz.getColorSpace());
		assertEquals(0.173f, ((CSSTypedValue) xyz.getX()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) xyz.getY()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) xyz.getZ()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, (((CSSTypedValue) xyz.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
	}

	@Test
	public void testXYZ_TwoArguments() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("color:color(xyz 0.173 0.102); ");
		assertEquals("color(xyz 0.173 0.102 0)", style.getPropertyValue("color"));
		assertEquals("color: color(xyz 0.173 0.102 0); ", style.getCssText());
		assertEquals("color:color(xyz .173 .102 0)", style.getMinifiedCssText());
		//
		CSSValue value = style.getPropertyCSSValue("color");
		//
		ColorValue val = (ColorValue) value;
		assertEquals(ColorModel.XYZ, val.getColorModel());
		XYZColor xyz = (XYZColor) val.getColor();
		assertEquals(ColorSpace.xyz, xyz.getColorSpace());
		assertEquals(0.173f, ((CSSTypedValue) xyz.getX()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0.102f, ((CSSTypedValue) xyz.getY()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(0f, ((CSSTypedValue) xyz.getZ()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertEquals(1f, (((CSSTypedValue) xyz.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
	}

	@Test
	public void testUnknownProfile() {
		try {
			new ValueFactory().parseProperty("color(unknown-profile 0.106 0.074 0.413/0.8)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
		}
	}

}
