/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorValueOKSpaceTest {

	@Test
	public void testOKLABColorModel() throws IOException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(71.834% .0384 0.0347);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(71.834f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-5f);
		assertEquals(0.0384f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0.0347f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
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
		assertEquals("oklab", lab.getColorSpace());
		assertEquals("oklab(71.834% 0.0384 0.0347)", lab.toString());
		assertEquals("oklab(71.834% .0384 .0347)", lab.toMinifiedString());
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
		assertEquals("oklab(71.834% 0.0384 0.0347)", value.getCssText());
		assertEquals("oklab(71.834% 0.0384 0.0347)", labColor.toString());
		assertEquals("oklab(71.834% .0384 .0347)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(30);
		value.writeCssText(wri);
		assertEquals("oklab(71.834% 0.0384 0.0347)", wri.toString());
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(67% 13.404 13.791)", cielabColor.getCssText());
		assertEquals("lab(67% 13.404 13.791)", cielab.toString());
		assertEquals("lab(67% 13.404 13.791)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(67f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(13.404f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(13.791f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		String s = rgb.toString();
		assertEquals("rgb(75.76%, 60.41%, 54.48%)", s);
		assertEquals(75.76f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(60.41f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(54.48f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
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
		assertEquals("lch(67% 19.232 45.815)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(67f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(19.232f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(45.815f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
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
		assertEquals("hsl(16.7, 30.5%, 65.1%)", hsl.toString());
		assertEquals(16.7f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER),
			0.01f);
		assertEquals(30.5f,
			((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.1f,
			((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Small Delta to converted value
		assertEquals(0f, hslColor.deltaE2000(labColor), 0.025f);
		assertEquals(0f, labColor.deltaE2000(hslColor), 0.025f);
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
		assertEquals(0.427f, ((TypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-6f);
		//
		labColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-6f);
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
	public void testOKLABColorModel2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(52% -0.14 0.11)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(-0.14f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.11f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(52% -0.14 0.11)", value.getCssText());
		assertEquals("oklab(52% -0.14 0.11)", labColor.toString());
		assertEquals("oklab(52% -.14 .11)", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(46.3% -47.214 50.177)", cielabColor.getCssText());
		assertEquals("lab(46.3% -47.214 50.177)", cielab.toString());
		assertEquals("lab(46.3% -47.214 50.177)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(46.301f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(-47.214f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(50.177f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 50.77%, 0%)", rgb.toString());
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(50.77f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(46.3% 68.898 133.257)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(46.3f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(68.898f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(133.257f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testOKLABColorModel3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(62.8% 0.225 0.125)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(62.8f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(0.225f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(0.125f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(62.8% 0.225 0.125)", value.getCssText());
		assertEquals("oklab(62.8% 0.225 0.125)", labColor.toString());
		assertEquals("oklab(62.8% .225 .125)", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(54.29% 80.783 69.007)", cielabColor.getCssText());
		assertEquals("lab(54.29% 80.783 69.007)", cielab.toString());
		assertEquals("lab(54.29% 80.783 69.007)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(54.292f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(80.783f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(69.007f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(99.977f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(0.16f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1.31f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(99.98%, 0.16%, 1.31%)", rgb.toString());
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(54.29% 106.244 40.505)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(54.2917f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(106.244f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(40.505f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testOKLABColorModel4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(70.167% 0.2746 -0.169)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(70.167f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0.2746f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.169f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(70.167% 0.2746 -0.169)", value.getCssText());
		assertEquals("oklab(70.167% 0.2746 -0.169)", labColor.toString());
		assertEquals("oklab(70.167% .2746 -.169)", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(60.17% 93.535 -60.422)", cielabColor.getCssText());
		assertEquals("lab(60.17% 93.535 -60.422)", cielab.toString());
		assertEquals("lab(60.17% 93.535 -60.422)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(60.17f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(93.535f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-60.422f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(100%, 0.05%, 99.94%)", rgb.toString());
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0.05f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(99.94f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(60.17% 111.354 327.138)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(60.17f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(111.354f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(327.138f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testOKLABColorModel5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(35.032% 0.013 -0.132)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("oklab(35.032% 0.013 -0.132)", value.getCssText());
		assertEquals("oklab(35.032% 0.013 -0.132)", lab.toString());
		assertEquals("oklab(35.032% .013 -.132)", value.getMinifiedCssText("color"));
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(35.032f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0.013f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.132f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(22.72% 20.149 -46.789)", cielabColor.getCssText());
		assertEquals("lab(22.72% 20.149 -46.789)", cielab.toString());
		assertEquals("lab(22.72% 20.149 -46.789)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(22.72f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(20.149f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(-46.789f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(16.79%, 17.89%, 49.26%)", rgb.toString());
		assertEquals(16.79f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(17.89f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(49.26f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(22.72% 50.943 293.299)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(22.7156f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(50.943f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(293.299f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
		// Diff
		style.setCssText("color: oklab(34.982% 0.0045 -0.12);");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor2 = (OKLABColorValue) color;
		LABColor lab2 = labColor2.getColor();
		assertNotNull(lab2);
		CSSPrimitiveValue lightness2 = lab2.getLightness();
		CSSPrimitiveValue a2 = lab2.getA();
		CSSPrimitiveValue b2 = lab2.getB();
		assertNotNull(lightness2);
		assertNotNull(a2);
		assertNotNull(b2);
		assertEquals(34.982f, ((CSSTypedValue) lightness2).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-5f);
		assertEquals(0.0045f, ((CSSTypedValue) a2).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.12f, ((CSSTypedValue) b2).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab2.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		assertEquals(2.05f, labColor.deltaE2000(labColor2), 0.01f);
		assertEquals(2.046f, labColor2.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testOKLABColorModel6() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(10% .012 -.01);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(10f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0.012f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.01f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(10% 0.012 -0.01)", value.getCssText());
		assertEquals("oklab(10% 0.012 -0.01)", labColor.toString());
		assertEquals("oklab(10% .012 -.01)", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(0.8523f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1.0208f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.9826f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		assertEquals("lab(0.85% 1.021 -0.983)", cielabColor.getCssText());
		assertEquals("lab(0.85% 1.021 -0.983)", cielab.toString());
		assertEquals("lab(.85% 1.021 -.983)", cielabColor.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(1.91%, 0.92%, 2.19%)", rgb.toString());
		assertEquals(1.9129f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(0.9218f,
			((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(2.1873f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(0.85% 1.417 316.092)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.8523f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1.417f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(316.092f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testOKLABColorModel7() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(45% 1.236 -0.019)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(45f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(1.236f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.019f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(45% 1.236 -0.019)", value.getCssText());
		assertEquals("oklab(45% 1.236 -0.019)", labColor.toString());
		assertEquals("oklab(45% 1.236 -.019)", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(19.864f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(359.568f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(51.7415f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		assertEquals("lab(19.86% 359.568 51.742)", cielabColor.getCssText());
		assertEquals("lab(19.86% 359.568 51.742)", cielab.toString());
		assertEquals("lab(19.86% 359.568 51.742)", cielabColor.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(41.61%, 0%, 15.96%)", rgb.toString());
		assertEquals(41.61f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(15.96f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(19.86% 363.272 8.189)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(19.86f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(363.272f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(8.189f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testOKLABColorModelAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(82.409% -0.142 0.1498/0.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(82.409f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(-0.142f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.1498f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		assertEquals("oklab(82.409% -0.142 0.1498 / 0.8)", value.getCssText());
		assertEquals("oklab(82.409% -0.142 0.1498 / 0.8)", labColor.toString());
		assertEquals("oklab(82.409% -.142 .1498/.8)", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(81.74% -45.343 65.538 / 0.8)", cielabColor.getCssText());
		assertEquals("lab(81.74% -45.343 65.538 / 0.8)", cielab.toString());
		assertEquals("lab(81.74% -45.343 65.538/.8)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(81.74f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(-45.343f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(65.538f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(0.8f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(54.89%, 88.02%, 24.98%, 0.8)", rgb.toString());
		assertEquals(54.89f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(88.02f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(24.98f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(81.74% 79.694 124.678 / 0.8)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.74f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(79.694f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(124.678f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
	}

	@Test
	public void testOKLABColorModelCalc() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(calc(2*36.9%) 18.438 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
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
		assertEquals("oklab(calc(2*36.9%) 18.438 48.583)", value.getCssText());
		assertEquals("oklab(calc(2*36.9%) 18.438 48.583)", labColor.toString());
		assertEquals("oklab(calc(2*36.9%) 18.438 48.583)", value.getMinifiedCssText("color"));
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
	public void testOKLABColorModelCalc2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(81.7395% calc(2*3) 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(Type.EXPRESSION, a.getPrimitiveType());
		assertEquals("calc(2*3)", a.getCssText());
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(81.7395% calc(2*3) 48.583)", value.getCssText());
		assertEquals("oklab(81.7395% calc(2*3) 48.583)", labColor.toString());
		assertEquals("oklab(81.7395% calc(2*3) 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testOKLABColorModelCalc3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(81.7395% 48.583 calc(2*16.43));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(48.583f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.EXPRESSION, b.getPrimitiveType());
		assertEquals("calc(2*16.43)", b.getCssText());
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(81.7395% 48.583 calc(2*16.43))", value.getCssText());
		assertEquals("oklab(81.7395% 48.583 calc(2*16.43))", labColor.toString());
		assertEquals("oklab(81.7395% 48.583 calc(2*16.43))", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testOKLABColorModelCalcAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(79.217% -0.103 0.1622/calc(2*0.34));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		OKLABColorValue labColor = (OKLABColorValue) color;
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
		assertEquals(79.217f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(-0.103f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.1622f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(Type.EXPRESSION, alpha.getPrimitiveType());
		assertEquals("calc(2*0.34)", alpha.getCssText());
		assertEquals("oklab(79.217% -0.103 0.1622 / calc(2*0.34))", value.getCssText());
		assertEquals("oklab(79.217% -0.103 0.1622 / calc(2*0.34))", labColor.toString());
		assertEquals("oklab(79.217% -.103 .1622/calc(2*.34))", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(77.53% -29.341 75.134 / calc(2*0.34))", cielabColor.getCssText());
		assertEquals("lab(77.53% -29.341 75.134 / calc(2*0.34))", cielab.toString());
		assertEquals("lab(77.53% -29.341 75.134/calc(2*.34))",
			cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(77.53f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(-29.341f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(75.134f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(65.22%, 80.5%, 2.01%, calc(2*0.34))", rgb.toString());
		assertEquals(65.22f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(2.01f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals("calc(2*0.34)", rgb.getAlpha().getCssText());
		assertEquals("calc(2*.34)", rgb.getAlpha().getMinifiedCssText(""));
	}

	@Test
	public void testOKLABColorModelClampedRGBConversion() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(44% -0.04 -0.32);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To rgb(-5.076% -9.79% 100.15%)
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(100f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#00f", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(2.04f, color.deltaE2000(rgbValue), 0.01f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(27.58% 73.748 -115.575)", cielabColor.getCssText());
		//
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(27.58% 137.1 302.542)", cielchColor.getCssText());
	}

	@Test
	public void testOKLABColorModelClampedRGBConversion2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(75.74% 0.01 0.17);");
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
		assertEquals("rgb(86.8%, 65.5%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(3.024f, color.deltaE2000(rgbValue), 0.01f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(72.05% 12.611 88.176)", cielabColor.getCssText());
	}

	@Test
	public void testOKLABColorModelClampedRGBConversion3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:oklab(63.2% -0.16 0.1);");
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
		assertEquals("rgb(0%, 66.07%, 19.86%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(2.033f, color.deltaE2000(rgbValue), 0.1f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(59.6% -55.898 41.645)", cielabColor.getCssText());
	}

	@Test
	public void testOKLABColorModelBadMixedType() {
		ColorValue value = new OKLABColorValue();
		try {
			value.setCssText("oklch(67% 19.2 45.7deg)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testOKLABColorModelBadMixedTypeLab() {
		ColorValue value = new OKLABColorValue();
		try {
			value.setCssText("lab(65.195% -85.22 -78.03)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testEqualsOKLAB() {
		ColorValue value = new OKLABColorValue();
		value.setCssText("oklab(81.7395% -45.2202 65.5283)");
		ColorValue other = new OKLABColorValue();
		other.setCssText("oklab(81.7395% -45.2202 65.5283)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertEquals(value.hashCode(), other.hashCode());
		assertFalse(value.equals(null));
		other.setCssText("oklab(81.7395% -45.2202 65.5283/1)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		other.setCssText("oklab(81.7395% -45.2202 65.5)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("oklab(81.7395% -45.2202 65.5283/70%)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());

		ValueFactory factory = new ValueFactory();
		StyleValue val = factory.parseProperty("lab(81.7395% -45.2202 65.5283)");
		assertFalse(value.equals(val));
		assertFalse(val.equals(value));
		assertFalse(value.hashCode() == val.hashCode());
	}

	@Test
	public void testCloneOKLAB() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklab(81.7395% -45.2202 65.5283);");
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
	public void testOKLCHColorModel() throws IOException {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(71.834% 0.0518 42.069deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(71.834f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-5f);
		assertEquals(0.0518f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(42.069f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
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
		assertEquals("oklch", lch.getColorSpace());
		assertEquals("oklch(71.834% 0.0518 42.069)", lch.toString());
		assertEquals("oklch(71.834% .0518 42.069)", lch.toMinifiedString());
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
		assertEquals("oklch(71.834% 0.0518 42.069)", value.getCssText());
		assertEquals("oklch(71.834% 0.0518 42.069)", lchColor.toString());
		assertEquals("oklch(71.834% .0518 42.069)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(30);
		value.writeCssText(wri);
		assertEquals("oklch(71.834% 0.0518 42.069)", wri.toString());
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(75.77f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(60.4072f,
			((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(54.477f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		String s = rgb.toString();
		assertEquals("rgb(75.77%, 60.4%, 54.48%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 1.1e-2f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 1.1e-2f);
		assertFalse(lchColor.equals(srgbValue));
		assertFalse(srgbValue.equals(lchColor));
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(67% 13.422 13.794)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(67f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(13.422f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(13.794f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
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
		assertEquals(16.7f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER),
			0.001f);
		assertEquals(30.5f,
			((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(65.1f,
			((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Small Delta to converted value
		assertEquals(0f, hslColor.deltaE2000(lchColor), 0.02f);
		assertEquals(0f, lchColor.deltaE2000(hslColor), 0.02f);
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
		assertEquals(0.427f, ((TypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-6f);
		//
		lchColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-6f);
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
	public void testOKLCHColorModel2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(4.1502% 0.0288 264.05deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(4.1502f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(0.0288f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(264.05f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(4.1502% 0.0288 264.05)", value.getCssText());
		assertEquals("oklch(4.1502% 0.0288 264.05)", lchColor.toString());
		assertEquals("oklch(4.1502% .0288 264.05)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(1.002f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		String s = rgb.toString();
		assertEquals("rgb(0%, 0%, 1%)", s);
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.0021f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.0021f);
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(0.04% 0.265 -0.972)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.04234f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-4f);
		assertEquals(0.2652f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.972f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testOKLCHColorModel3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(7% 0.48 29deg);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(7f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.48f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(29f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(7% 0.48 29)", value.getCssText());
		assertEquals("oklch(7% 0.48 29)", lchColor.toString());
		assertEquals("oklch(7% .48 29)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(9.7%, 0%, 0%)", rgb.toString());
		assertEquals("hsl(0, 100%, 4.9%)", ((RGBColor) rgb).toHSLColor().toString());
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		assertEquals("lab(1.51% 74.897 48.94)", labColor.getCssText());
		//
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(1.51% 89.469 33.162)", cielchColor.getCssText());
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, color.deltaE2000(cielchColor), 0.001f);
	}

	@Test
	public void testOKLCHColorModel4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(52% 0.178 157.6grad);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("oklch(52% 0.178 157.6grad)", value.getCssText());
		assertEquals("oklch(52% 0.178 157.6grad)", lchColor.toString());
		assertEquals("oklch(52% .178 157.6grad)", value.getMinifiedCssText("color"));
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-3f);
		assertEquals(0.178f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(157.6f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_GRAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(46.3% -47.199 50.163)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(46.3f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-2f);
		assertEquals(-47.199f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(50.1635f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 50.77%, 0%)", rgb.toString());
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(50.772f,
			((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals("hsl(120, 100%, 25.4%)", ((RGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testOKLCHColorModel5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(84% 0.14 201);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(84f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(0.14f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(201f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Serialization
		assertEquals("oklch(84% 0.14 201)", value.getCssText());
		assertEquals("oklch(84% 0.14 201)", lchColor.toString());
		assertEquals("oklch(84% .14 201)", value.getMinifiedCssText("color"));
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(10.71%, 89.83%, 94.01%)", rgb.toString());
		assertEquals(10.71f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(89.83f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(94.01f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(82.77% -43.37 -18.784)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(82.77f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(-43.37f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(-18.784f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// To LCh
		LCHColorValue cielchColor = lchColor.toLCHColorValue();
		LCHColor cielch = cielchColor.getColor();
		assertNotNull(cielch);
		assertEquals("lch(82.77% 47.263 203.418)", cielch.toString());
		CSSPrimitiveValue cieLightness = cielch.getLightness();
		CSSPrimitiveValue ciechroma = cielch.getChroma();
		CSSPrimitiveValue ciehue = cielch.getHue();
		assertNotNull(cieLightness);
		assertNotNull(ciechroma);
		assertNotNull(ciehue);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, color.deltaE2000(cielchColor), 0.001f);
		style.setCssText("color: oklch(82% 0.16 193);");
		value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor2 = (OKLCHColorValue) color;
		LCHColor lch2 = lchColor2.getColor();
		assertNotNull(lch2);
		assertEquals("oklch(82% 0.16 193)", lch2.toString());
		assertEquals(5.24f, lchColor.deltaE2000(lchColor2), 0.01f);
	}

	@Test
	public void testOKLCHColorModel6() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(81.948% 0.16 3.369rad);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.948f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-3f);
		assertEquals(0.16f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(3.369f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_RAD), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(81.948% 0.16 3.369rad)", value.getCssText());
		assertEquals("oklch(81.948% 0.16 3.369rad)", lchColor.toString());
		assertEquals("oklch(81.948% .16 3.369rad)", value.getMinifiedCssText("color"));
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(80.8% -53.201 -13.685)", lab.toString());
		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(80.8% 54.933 194.425)", cielchColor.getCssText());
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		String s = rgb.toString();
		assertEquals("rgb(0%, 89.16%, 88.01%)", s);
		assertEquals("hsl(179.2, 100%, 44.6%)", ((RGBColor) rgb).toHSLColor().toString());
		// Delta to converted RGB value near 2 (clamp)
		ColorValue srgbValue = (ColorValue) new ValueFactory().parseProperty(s);
		assertEquals(0f, color.deltaE2000(srgbValue), 2f);
		assertEquals(0f, srgbValue.deltaE2000(color), 2f);
		// Delta 0 to converted Lab
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testOKLCHColorModel7() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(68% 0.444 930);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(68f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			1e-3f);
		assertEquals(0.444f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(930f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(68% 0.444 930)", value.getCssText());
		assertEquals("oklch(68% 0.444 930)", lchColor.toString());
		assertEquals("oklch(68% .444 930)", value.getMinifiedCssText("color"));
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(65.2% -85.261 -77.993)", lab.toString());
		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(65.2% 115.552 222.451)", cielchColor.getCssText());
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		String s = rgb.toString();
		assertEquals("rgb(0%, 69.18%, 83.53%)", s);
		assertEquals("hsl(190.3, 100%, 41.8%)", ((RGBColor) rgb).toHSLColor().toString());
		// Delta 0 to converted Lab
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testOKLCHColorModelAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(45% 0.4 264/.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(45f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0.4f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(264f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
			1e-5f);
		assertEquals("oklch(45% 0.4 264 / 0.8)", value.getCssText());
		assertEquals("oklch(45% 0.4 264 / 0.8)", lchColor.toString());
		assertEquals("oklch(45% .4 264/.8)", value.getMinifiedCssText("color"));
		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		assertEquals("lab(23.44% 119.995 -148.148 / 0.8)", labColor.getCssText());
		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(23.44% 190.648 309.006 / 0.8)", cielchColor.getCssText());
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(32.33%, 0%, 71.14%, 0.8)", rgb.toString());
	}

	@Test
	public void testOKLCHColorModelCalc() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(calc(2*36.9%) 18.438 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
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
		assertEquals("oklch(calc(2*36.9%) 18.438 48.583)", value.getCssText());
		assertEquals("oklch(calc(2*36.9%) 18.438 48.583)", lchColor.toString());
		assertEquals("oklch(calc(2*36.9%) 18.438 48.583)", value.getMinifiedCssText("color"));
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
	public void testOKLCHColorModelCalc2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(81.7395% calc(2*3) 48.583);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(Type.EXPRESSION, chroma.getPrimitiveType());
		assertEquals("calc(2*3)", chroma.getCssText());
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(81.7395% calc(2*3) 48.583)", value.getCssText());
		assertEquals("oklch(81.7395% calc(2*3) 48.583)", lchColor.toString());
		assertEquals("oklch(81.7395% calc(2*3) 48.583)", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testOKLCHColorModelCalc3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(81.7395% 48.583 calc(2*16.43deg));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(48.583f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.EXPRESSION, hue.getPrimitiveType());
		assertEquals("calc(2*16.43deg)", hue.getCssText());
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(81.7395% 48.583 calc(2*16.43deg))", value.getCssText());
		assertEquals("oklch(81.7395% 48.583 calc(2*16.43deg))", lchColor.toString());
		assertEquals("oklch(81.7395% 48.583 calc(2*16.43deg))", value.getMinifiedCssText("color"));
		// To RGB
		try {
			color.toRGBColor();
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		}
	}

	@Test
	public void testOKLCHColorModelCalcAlpha() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(79.217% 0.1921 122.42/calc(2*0.34));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
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
		assertEquals(79.217f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0.1921f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(122.42f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(Type.EXPRESSION, alpha.getPrimitiveType());
		assertEquals("calc(2*0.34)", alpha.getCssText());
		assertEquals("oklch(79.217% 0.1921 122.42 / calc(2*0.34))", value.getCssText());
		assertEquals("oklch(79.217% 0.1921 122.42 / calc(2*0.34))", lchColor.toString());
		assertEquals("oklch(79.217% .1921 122.42/calc(2*.34))", value.getMinifiedCssText("color"));
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(77.53% -29.34 75.106 / calc(2*0.34))", cielabColor.getCssText());
		assertEquals("lab(77.53% -29.34 75.106 / calc(2*0.34))", cielab.toString());
		assertEquals("lab(77.53% -29.34 75.106/calc(2*.34))",
			cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(77.53f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(-29.34f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(75.106f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(65.22%, 80.5%, 2.13%, calc(2*0.34))", rgb.toString());
		assertEquals(65.22f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals(2.13f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.01f);
		assertEquals("calc(2*0.34)", rgb.getAlpha().getCssText());
		assertEquals("calc(2*.34)", rgb.getAlpha().getMinifiedCssText(""));
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(62.9% 0.26 29.24);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To rgb(100.5% -2.811% -1.578%)
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
			0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#f00", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0.373f, color.deltaE2000(rgbValue), 0.001f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(54.38% 81.574 71.273)", cielabColor.getCssText());
		//
		LCHColorValue lchColor = color.toLCHColorValue();
		assertEquals("lch(54.38% 108.325 41.144)", lchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion2() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(75.5% 0.16 86.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		//
		LABColorValue labColor = color.toLABColorValue();
		assertEquals("lab(71.79% 11.228 78.738)", labColor.getCssText());
		//
		LCHColorValue lchColor = color.toLCHColorValue();
		assertEquals("lch(71.79% 79.535 81.884)", lchColor.getCssText());
		// To RGB
		try {
			color.toRGBColor(false);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(86.44%, 65.25%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(1.33f, color.deltaE2000(rgbValue), 0.01f);
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion3() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:oklch(63% 0.2 145);");
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
		assertEquals("rgb(0%, 66.03%, 6.67%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(2.32f, color.deltaE2000(rgbValue), 0.01f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(59.42% -56.416 49.442)", cielabColor.getCssText());
		//
		LCHColorValue lchColor = color.toLCHColorValue();
		assertEquals("lch(59.42% 75.015 138.769)", lchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion4() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:oklch(82% 0.12 49)");
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
		assertEquals("rgb(100%, 66.24%, 45.39%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(1.88f, color.deltaE2000(rgbValue), 0.01f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(78.45% 29.311 38.005)", cielabColor.getCssText());
		// To LCh
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertNotNull(cielchColor);
		assertEquals("lch(78.45% 47.995 52.36)", cielchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion5() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:oklch(80% 0.14 194)");
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
		assertEquals("rgb(0%, 86.06%, 85.73%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0.7f, color.deltaE2000(rgbValue), 0.1f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(78.3% -46.169 -12.853)", cielabColor.getCssText());
		// To LCh
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertNotNull(cielchColor);
		assertEquals("lch(78.3% 47.924 195.556)", cielchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversionVeryLargeChroma() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color:oklch(55% 0.2 145);");
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
		assertEquals("rgb(0%, 55.29%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(2f, color.deltaE2000(rgbValue), 0.01f);
		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(50.09% -56.628 51.674)", cielabColor.getCssText());
		//
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(50.09% 76.662 137.619)", cielchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelBadMixedType() {
		ColorValue value = new OKLCHColorValue();
		try {
			value.setCssText("oklab(67% 19.2 14.8)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testOKLCHColorModelBadMixedTypeLCh() {
		ColorValue value = new OKLCHColorValue();
		try {
			value.setCssText("lch(65.195% 115 222)");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testEqualsOKLCH() {
		ColorValue value = new OKLCHColorValue();
		value.setCssText("oklch(29.186% 122.2075 300.3188)");
		ColorValue other = new OKLCHColorValue();
		other.setCssText("oklch(29.186% 122.2075 300.3188)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertEquals(value.hashCode(), other.hashCode());
		assertFalse(value.equals(null));
		other.setCssText("oklch(29.186% 122.2075 300.3188/1)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		other.setCssText("oklch(29.186% 122.2075 300.3)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("oklch(29.186% 122.2075 300.3188/70%)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());

		ValueFactory factory = new ValueFactory();
		StyleValue val = factory.parseProperty("lch(29.186% 122.2075 300.3188)");
		assertFalse(value.equals(val));
		assertFalse(val.equals(value));
		assertFalse(value.hashCode() == val.hashCode());
	}

	@Test
	public void testCloneOKLCH() {
		CSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("color: oklch(29.186% 122.2075 300.3);");
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
