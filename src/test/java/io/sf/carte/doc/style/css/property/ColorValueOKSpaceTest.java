/*

 Copyright (c) 2005-2025, Carlos Amengual.

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorValueOKSpaceTest {

	static AbstractCSSStyleSheet sheet;
	StyleRule parentStyleRule;
	AbstractCSSStyleDeclaration style;

	@BeforeAll
	public static void setUpBeforeAll() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
	}

	@BeforeEach
	public void setUpBefore() {
		parentStyleRule = sheet.createStyleRule();
		style = parentStyleRule.getStyle();
	}

	private StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		return parentStyleRule.getStyleDeclarationErrorHandler();
	}

	@Test
	public void testOKLABColorModel() throws IOException {
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
		assertEquals(0.71834f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(0.0384f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0.0347f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertSame(lab.getAlpha(), lab.item(0));
		assertSame(lab.getLightness(), lab.item(1));
		assertSame(lab.getA(), lab.item(2));
		assertSame(lab.getB(), lab.item(3));
		assertEquals(4, lab.getLength());
		assertNull(lab.item(4));

		assertEquals(CSSColorValue.ColorModel.LAB, lab.getColorModel());
		assertEquals("oklab", lab.getColorSpace());
		assertEquals("oklab(0.71834 0.0384 0.0347)", lab.toString());
		assertEquals("oklab(.71834 .0384 .0347)", lab.toMinifiedString());
		// Set wrong values
		DOMException e = assertThrows(DOMException.class, () ->
			labColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () ->
			labColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () ->
			labColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () ->
			labColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Serialization
		assertEquals("oklab(0.71834 0.0384 0.0347)", value.getCssText());
		assertEquals("oklab(0.71834 0.0384 0.0347)", labColor.toString());
		assertEquals("oklab(.71834 .0384 .0347)", value.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(30);
		value.writeCssText(wri);
		assertEquals("oklab(0.71834 0.0384 0.0347)", wri.toString());

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(67.001 13.404 13.791)", cielabColor.getCssText());
		assertEquals("lab(67.001 13.404 13.791)", cielab.toString());
		assertEquals("lab(67.001 13.404 13.791)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(67.001f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(13.404f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(13.791f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		assertEquals(0f, lab.deltaEOK(cielab), 1e-6f);

		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals("rgb(75.76%, 60.41%, 54.48%)", rgb.toString());
		assertEquals(75.76f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(60.41f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(54.48f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		ColorValue srgbValue = (ColorValue) rgb.packInValue();
		assertEquals(0f, labColor.deltaE2000(srgbValue), 0.01f);
		assertEquals(0f, srgbValue.deltaE2000(labColor), 0.01f);

		assertEquals(0f, lab.deltaEOK(rgb), 1e-6f);

		assertFalse(labColor.equals(srgbValue));
		assertFalse(srgbValue.equals(labColor));

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(67.001 19.232 45.815)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(67.001f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(19.232f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(45.815f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);

		// Conversion lab -> lch -> rgb
		srgbValue = (ColorValue) lchColor.toRGBColor().packInValue();
		assertEquals(0f, color.deltaE2000(srgbValue), 0.001f);
		assertEquals(0f, srgbValue.deltaE2000(color), 0.001f);

		// HSL indirect conversion lab -> lch -> rgb -> hsl
		HSLColorValue hslColor = srgbValue.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals("hsl(16.717, 30.5%, 65.12%)", hsl.toString());
		assertEquals(16.72f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.01f);
		assertEquals(30.5f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(65.12f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Small Delta to converted value
		assertEquals(0f, hslColor.deltaE2000(labColor), 0.002f);
		assertEquals(0f, labColor.deltaE2000(hslColor), 0.002f);

		// Large DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(calc(25*2),63,77)");
		assertEquals(46.86f, labColor.deltaE2000(rgbColor2), 0.01f);
		assertEquals(46.86f, rgbColor2.deltaE2000(labColor), 0.01f);

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

		labColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(0.87f, ((TypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);

		labColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);

		labColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 27f));
		assertEquals(27f, ((TypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);

		assertThrows(NullPointerException.class, () -> labColor.setComponent(0, null));

		DOMException ex = assertThrows(DOMException.class, () -> labColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);

		assertThrows(NullPointerException.class, () -> labColor.setComponent(1, null));

		ex = assertThrows(DOMException.class, () -> labColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);

		assertThrows(NullPointerException.class, () -> labColor.setComponent(2, null));

		ex = assertThrows(DOMException.class, () -> labColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);

		assertThrows(NullPointerException.class, () -> labColor.setComponent(3, null));

		ex = assertThrows(DOMException.class, () -> labColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);
	}

	@Test
	public void testOKLABColorModel2() {
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
		assertEquals(0.52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.14f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.11f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.52 -0.14 0.11)", value.getCssText());
		assertEquals("oklab(0.52 -0.14 0.11)", labColor.toString());
		assertEquals("oklab(.52 -.14 .11)", value.getMinifiedCssText("color"));

		assertFalse(lab.isInGamut(ColorSpace.srgb));
		assertTrue(lab.isInGamut(ColorSpace.display_p3));
		assertTrue(lab.isInGamut(ColorSpace.ok_lab));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(46.301 -47.214 50.177)", cielabColor.getCssText());
		assertEquals("lab(46.301 -47.214 50.177)", cielab.toString());
		assertEquals("lab(46.301 -47.214 50.177)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(46.301f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(-47.214f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(50.177f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		assertEquals(0f, lab.deltaEOK(cielab), 1e-6f);

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

		assertEquals(4.5633e-3f, lab.deltaEOK(rgb), 1e-6f);

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(46.301 68.898 133.257)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(46.3f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.01f);
		assertEquals(68.898f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(133.257f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);

		assertEquals(0f, lab.deltaEOK(lch), 1e-6f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());

		// Delta to XYZ
		style.setCssText("color: color(xyz 0.0775579 0.1545064 0.0236256)");
		CSSValue xyzValue = style.getPropertyCSSValue("color");
		assertNotNull(xyzValue);
		assertEquals(CssType.TYPED, xyzValue.getCssValueType());
		assertEquals(Type.COLOR, xyzValue.getPrimitiveType());
		ColorValue xyzcolor = (ColorValue) xyzValue;
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzcolor.getColorModel());

		assertEquals(0.014f, color.deltaE2000(xyzcolor), 0.01f);

		assertEquals(5.07e-5f, lab.deltaEOK(xyzcolor.getColor()), 1e-6f);

		// Delta to OKLCh
		style.setCssText("color: oklch(0.52 0.17804 141.843)");
		CSSValue okValue = style.getPropertyCSSValue("color");
		assertNotNull(okValue);
		assertEquals(CssType.TYPED, okValue.getCssValueType());
		assertEquals(Type.COLOR, okValue.getPrimitiveType());
		ColorValue okColor = (ColorValue) okValue;
		assertEquals(CSSColorValue.ColorModel.LCH, okColor.getColorModel());
		assertEquals(ColorSpace.ok_lch, okColor.getColor().getColorSpace());

		assertEquals(0f, color.deltaE2000(okColor), 0.001f);

		assertEquals(5e-6f, lab.deltaEOK(okColor.getColor()), 1e-6f);

		// To sRGB
		CSSColor srgb = lab.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("rgb(0%, 50.77%, 0%)", srgb.toString());

		assertEquals(4.5633e-3f, lab.deltaEOK(srgb), 1e-6f);

		// HSL
		CSSColor hsl = lab.toColorSpace("hsl");
		assertNotNull(hsl);
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("hsl(120, 100%, 25.39%)", hsl.toString());

		assertEquals(4.56e-3f, lab.deltaEOK(hsl), 1e-5f);

		// HWB
		CSSColor hwb = lab.toColorSpace("hwb");
		assertNotNull(hwb);
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("hwb(120 0% 49.23%)", hwb.toString());

		assertEquals(4.56e-3f, lab.deltaEOK(hwb), 1e-5f);

		// To A98 RGB
		CSSColor a98rgb = lab.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0.2843 0.4978 0.1027)", a98rgb.toString());

		assertEquals(0f, lab.deltaEOK(a98rgb), 1e-7f);

		// To Display P3
		CSSColor display_p3 = lab.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0.2209 0.494 0.1209)", display_p3.toString());

		assertEquals(0f, lab.deltaEOK(display_p3), 1e-7f);

		// To Prophoto RGB
		CSSColor prophoto = lab.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.2318 0.3957 0.1243)", prophoto.toString());

		assertEquals(0f, lab.deltaEOK(prophoto), 1e-7f);

		// To REC 2020
		CSSColor rec2020 = lab.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.2376 0.4316 0.0764)", rec2020.toString());

		assertEquals(0f, lab.deltaEOK(rec2020), 1e-7f);

		// To XYZ
		CSSColor xyz = lab.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.07757 0.1545 0.02358)", xyz.toString());

		assertEquals(0f, lab.deltaEOK(xyz), 1e-7f);

		// To XYZ D50
		CSSColor xyzD50 = lab.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.08363 0.15492 0.01934)", xyzD50.toString());

		assertEquals(0f, lab.deltaEOK(xyzD50), 1e-7f);

		// To OK Lab
		CSSColor ok_lab = lab.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.52 -0.14 0.11)", ok_lab.toString());

		assertEquals(0f, lab.deltaEOK(ok_lab), 1e-7f);

		// To OK LCh
		CSSColor ok_lch = lab.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.52 0.17804 141.843)", ok_lch.toString());

		assertEquals(0f, lab.deltaEOK(ok_lch), 1e-7f);

		// To CIE Lab
		CSSColor cie_lab = lab.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(46.3012 -47.21364 50.17707)", cie_lab.toString());

		assertEquals(0f, lab.deltaEOK(cie_lab), 1e-7f);

		// To CIE LCh
		CSSColor cie_lch = lab.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(46.3012 68.8975 133.257)", cie_lch.toString());

		assertEquals(0f, lab.deltaEOK(cie_lch), 1e-7f);
	}

	@Test
	public void testOKLABColorModel3() {
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
		assertEquals(0.628f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(0.225f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(0.125f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.628 0.225 0.125)", value.getCssText());
		assertEquals("oklab(0.628 0.225 0.125)", labColor.toString());
		assertEquals("oklab(.628 .225 .125)", value.getMinifiedCssText("color"));

		assertTrue(lab.isInGamut("hsla"));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(54.292 80.783 69.007)", cielabColor.getCssText());
		assertEquals("lab(54.292 80.783 69.007)", cielab.toString());
		assertEquals("lab(54.292 80.783 69.007)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(54.292f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
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
		assertEquals("lch(54.292 106.244 40.505)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(54.2917f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(106.244f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(40.505f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);

		// Packing
		CSSColorValue labValue = lab.packInValue();
		assertNotNull(labValue);
		assertEquals(labColor, labValue);
	}

	@Test
	public void testOKLABColorModel4() {
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
		assertEquals(0.70167f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.2746f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.169f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.70167 0.2746 -0.169)", value.getCssText());
		assertEquals("oklab(0.70167 0.2746 -0.169)", labColor.toString());
		assertEquals("oklab(.70167 .2746 -.169)", value.getMinifiedCssText("color"));

		assertFalse(lab.isInGamut("hsl"));
		assertTrue(lab.isInGamut(ColorSpace.a98_rgb));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(60.171 93.535 -60.422)", cielabColor.getCssText());
		assertEquals("lab(60.171 93.535 -60.422)", cielab.toString());
		assertEquals("lab(60.171 93.535 -60.422)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(60.171f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(93.535f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-60.422f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#f0f", rgb.toString());
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(100f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(60.171 111.354 327.138)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(60.171f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(111.354f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(327.138f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);

		double[] xyz = lab.toXYZ(Illuminants.whiteD50);
		double[] sxyz = lch.toXYZ(Illuminants.whiteD50);
		assertEquals(xyz[0], sxyz[0], 2e-7, "Different component x.");
		assertEquals(xyz[1], sxyz[1], 3e-8, "Different component y.");
		assertEquals(xyz[2], sxyz[2], 4e-7, "Different component z.");

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLABColorModel5() {
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
		assertEquals("oklab(0.35032 0.013 -0.132)", value.getCssText());
		assertEquals("oklab(0.35032 0.013 -0.132)", lab.toString());
		assertEquals("oklab(.35032 .013 -.132)", value.getMinifiedCssText("color"));
		CSSPrimitiveValue lightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(lightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.35032f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.013f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.132f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(22.716 20.149 -46.788)", cielabColor.getCssText());
		assertEquals("lab(22.716 20.149 -46.788)", cielab.toString());
		assertEquals("lab(22.716 20.149 -46.788)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(22.716f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(20.149f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(-46.788f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
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
		assertEquals("lch(22.716 50.943 293.299)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(22.7156f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
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
		assertEquals(0.34982f, ((CSSTypedValue) lightness2).getFloatValue(CSSUnit.CSS_NUMBER),
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
		assertEquals(0.10f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.012f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.01f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.1 0.012 -0.01)", value.getCssText());
		assertEquals("oklab(0.1 0.012 -0.01)", labColor.toString());
		assertEquals("oklab(.1 .012 -.01)", value.getMinifiedCssText("color"));

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
		assertEquals(0.8523f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1.0208f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.9826f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("lab(0.852 1.021 -0.983)", cielabColor.getCssText());
		assertEquals("lab(0.852 1.021 -0.983)", cielab.toString());
		assertEquals("lab(.852 1.021 -.983)", cielabColor.getMinifiedCssText("color"));

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
		assertEquals("lch(0.852 1.417 316.092)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.8523f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(1.417f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(316.092f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testOKLABColorModel7() {
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
		assertEquals(0.45f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1.236f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.019f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.45 1.236 -0.019)", value.getCssText());
		assertEquals("oklab(0.45 1.236 -0.019)", labColor.toString());
		assertEquals("oklab(.45 1.236 -.019)", value.getMinifiedCssText("color"));

		assertFalse(lab.isInGamut(ColorSpace.prophoto_rgb));

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
		assertEquals(19.864f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(359.568f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(51.7415f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("lab(19.864 359.568 51.742)", cielabColor.getCssText());
		assertEquals("lab(19.864 359.568 51.742)", cielab.toString());
		assertEquals("lab(19.864 359.568 51.742)", cielabColor.getMinifiedCssText("color"));

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
		assertEquals("lch(19.864 363.272 8.189)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(19.864f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(363.272f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(8.189f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLABColorModelNumberLightness() {
		style.setCssText("color: oklab(0.52 -0.14 0.11)");
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
		assertEquals(0.52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.14f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.11f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.52 -0.14 0.11)", value.getCssText());
		assertEquals("oklab(0.52 -0.14 0.11)", labColor.toString());
		assertEquals("oklab(.52 -.14 .11)", value.getMinifiedCssText("color"));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(46.301 -47.214 50.177)", cielabColor.getCssText());
		assertEquals("lab(46.301 -47.214 50.177)", cielab.toString());
		assertEquals("lab(46.301 -47.214 50.177)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(46.301f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
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
		assertEquals("lch(46.301 68.898 133.257)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(46.3f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.01f);
		assertEquals(68.898f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(133.257f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLABColorModelPcnt() {
		style.setCssText("color: oklab(70.167% 68.65% -42.25%)");
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
		assertEquals(0.70167f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.2746f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.169f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.70167 0.2746 -0.169)", value.getCssText());
		assertEquals("oklab(0.70167 0.2746 -0.169)", labColor.toString());
		assertEquals("oklab(.70167 .2746 -.169)", value.getMinifiedCssText("color"));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(60.171 93.535 -60.422)", cielabColor.getCssText());
		assertEquals("lab(60.171 93.535 -60.422)", cielab.toString());
		assertEquals("lab(60.171 93.535 -60.422)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(60.171f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(93.535f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-60.422f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#f0f", rgb.toString());
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(100f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(60.171 111.354 327.138)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(60.171f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(111.354f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(327.138f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLABColorModelPcntClamp() {
		style.setCssText("color: oklab(170.167% 168.65% -142.25%)");
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
		assertEquals(1f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.4f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.4f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(1 0.4 -0.4)", value.getCssText());
		assertEquals("oklab(1 0.4 -0.4)", labColor.toString());
		assertEquals("oklab(1 .4 -.4)", value.getMinifiedCssText("color"));

		assertFalse(lab.isInGamut(ColorSpace.srgb));
		assertFalse(lab.isInGamut(ColorSpace.prophoto_rgb));
		assertTrue(lab.isInGamut(ColorSpace.ok_lab));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(89.111 159.191 -142.381)", cielabColor.getCssText());
		assertEquals("lab(89.111 159.191 -142.381)", cielab.toString());
		assertEquals("lab(89.111 159.191 -142.381)", cielabColor.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(99.15%, 82.28%, 100%)", rgb.toString());

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(89.111 213.575 318.19)", lch.toString());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLABColorModelAlpha() {
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
		assertEquals(0.82409f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-0.142f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.1498f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("oklab(0.82409 -0.142 0.1498 / 0.8)", value.getCssText());
		assertEquals("oklab(0.82409 -0.142 0.1498 / 0.8)", labColor.toString());
		assertEquals("oklab(.82409 -.142 .1498/.8)", value.getMinifiedCssText("color"));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(81.744 -45.343 65.538 / 0.8)", cielabColor.getCssText());
		assertEquals("lab(81.744 -45.343 65.538 / 0.8)", cielab.toString());
		assertEquals("lab(81.744 -45.343 65.538/.8)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(81.744f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3);
		assertEquals(-45.343f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3);
		assertEquals(65.538f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3);
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
		assertEquals("lch(81.744 79.694 124.678 / 0.8)", lch.toString());
		CSSPrimitiveValue lchLightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lchLightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(81.744f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(79.694f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(124.678f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
	}

	@Test
	public void testOKLABColorModelCalc() {
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
		assertEquals(Type.NUMERIC, lightness.getPrimitiveType());
		assertEquals(0.738f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(18.438f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.738 18.438 48.583)", value.getCssText());
		assertEquals("oklab(0.738 18.438 48.583)", labColor.toString());
		assertEquals("oklab(.738 18.438 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#fff", rgb.toString());

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(255,255,254)");
		assertEquals(131.9f, labColor.deltaE2000(rgbColor2), 0.1f);
		assertEquals(131.9f, rgbColor2.deltaE2000(labColor), 0.1f);

		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(131.9f, labColor.deltaE2000(hslColor), 0.1f);
		assertEquals(131.9f, hslColor.deltaE2000(labColor), 0.1f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLABColorModelCalc2() {
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
		assertEquals(0.817395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(Type.NUMERIC, a.getPrimitiveType());
		assertEquals(6f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.817395 6 48.583)", value.getCssText());
		assertEquals("oklab(0.817395 6 48.583)", labColor.toString());
		assertEquals("oklab(.817395 6 48.583)", value.getMinifiedCssText("color"));

		assertFalse(lab.isInGamut(ColorSpace.prophoto_rgb));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#fff", rgb.toString());

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(255,255,254)");
		assertEquals(131.8f, labColor.deltaE2000(rgbColor2), 0.1f);
		assertEquals(131.8f, rgbColor2.deltaE2000(labColor), 0.1f);

		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(131.8f, labColor.deltaE2000(hslColor), 0.1f);
		assertEquals(131.8f, hslColor.deltaE2000(labColor), 0.1f);
	}

	@Test
	public void testOKLABColorModelCalc3() {
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
		assertEquals(0.817395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(48.583f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.NUMERIC, b.getPrimitiveType());
		assertEquals(32.86f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.817395 48.583 32.86)", value.getCssText());
		assertEquals("oklab(0.817395 48.583 32.86)", labColor.toString());
		assertEquals("oklab(.817395 48.583 32.86)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#fff", rgb.toString());
	}

	@Test
	public void testOKLABColorModelCalcAlpha() {
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
		assertEquals(0.79217f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-0.103f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.1622f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(Type.NUMERIC, alpha.getPrimitiveType());
		assertEquals(0.68f, ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals("oklab(0.79217 -0.103 0.1622 / 0.68)", value.getCssText());
		assertEquals("oklab(0.79217 -0.103 0.1622 / 0.68)", labColor.toString());
		assertEquals("oklab(.79217 -.103 .1622/.68)", value.getMinifiedCssText("color"));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(77.532 -29.341 75.134 / 0.68)", cielabColor.getCssText());
		assertEquals("lab(77.532 -29.341 75.134 / 0.68)", cielab.toString());
		assertEquals("lab(77.532 -29.341 75.134/.68)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(77.53f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(-29.341f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(75.134f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(65.22%, 80.5%, 2.01%, 0.68)", rgb.toString());
		assertEquals(65.22f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(2.01f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(0.68f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.01f);
	}

	@Test
	public void testOKLABColorModelMath() {
		style.setCssText("color: oklab(64.221% sqrt(1.3225) min(-0.049,0.058))");
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
		assertEquals(0.64221f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.00001f);
		assertEquals(1.15f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.000001f);
		assertEquals(-0.049f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklab(0.64221 1.15 -0.049)", labColor.toString());

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(81.75%, 0%, 42.52%)", rgb.toString());
	}

	@Test
	public void testOKLABColorModelClampedRGBConversion() {
		style.setCssText("color: oklab(44% -0.04 -0.32);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		assertFalse(color.getColor().isInGamut(ColorSpace.srgb_linear));
		assertFalse(color.getColor().isInGamut(ColorSpace.display_p3));
		assertTrue(color.getColor().isInGamut(ColorSpace.prophoto_rgb));

		// To rgb(-5.076% -9.79% 100.15%)
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

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

		ColorValue rgbValue = (ColorValue) rgb.packInValue();
		assertEquals(2.04f, color.deltaE2000(rgbValue), 0.01f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(27.579 73.748 -115.575)", cielabColor.getCssText());

		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(27.579 137.1 302.542)", cielchColor.getCssText());
	}

	@Test
	public void testOKLABColorModelClampedRGBConversion2() {
		style.setCssText("color: oklab(75.74% 0.01 0.17);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		assertFalse(color.getColor().isInGamut(ColorSpace.srgb_linear));
		assertTrue(color.getColor().isInGamut(ColorSpace.display_p3));
		assertTrue(color.getColor().isInGamut(ColorSpace.xyz));

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(86.8%, 65.5%, 0%)", rgb.toString());

		CSSColorValue rgbValue = rgb.packInValue();
		assertEquals(3.024f, color.deltaE2000(rgbValue), 0.01f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(72.051 12.611 88.176)", cielabColor.getCssText());
	}

	@Test
	public void testOKLABColorModelClampedRGBConversion3() {
		style.setCssText("color:oklab(63.2% -0.16 0.1);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		assertFalse(color.getColor().isInGamut(ColorSpace.srgb_linear));
		assertTrue(color.getColor().isInGamut(ColorSpace.display_p3));
		assertTrue(color.getColor().isInGamut(ColorSpace.ok_lch));

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 66.07%, 19.86%)", rgb.toString());

		CSSColorValue rgbValue = rgb.packInValue();
		assertEquals(2.033f, color.deltaE2000(rgbValue), 0.1f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(59.597 -55.899 41.645)", cielabColor.getCssText());
	}

	@Test
	public void testOKLABColorModelBadMixedType() {
		ColorValue value = new OKLABColorValue();
		DOMException e = assertThrows(DOMException.class, () ->
			value.setCssText("oklch(67% 19.2 45.7deg)"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
	}

	@Test
	public void testOKLABColorModelBadMixedTypeLab() {
		ColorValue value = new OKLABColorValue();
		DOMException e = assertThrows(DOMException.class, () ->
			value.setCssText("lab(65.195% -85.22 -78.03)"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
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
		assertEquals(0.71834f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(0.0518f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(42.069f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertSame(lch.getAlpha(), lch.item(0));
		assertSame(lch.getLightness(), lch.item(1));
		assertSame(lch.getChroma(), lch.item(2));
		assertSame(lch.getHue(), lch.item(3));
		assertEquals(4, lch.getLength());
		assertNull(lch.item(4));

		assertEquals(CSSColorValue.ColorModel.LCH, lch.getColorModel());
		assertEquals("oklch", lch.getColorSpace());
		assertEquals("oklch(0.71834 0.0518 42.069)", lch.toString());
		assertEquals("oklch(.71834 .0518 42.069)", lch.toMinifiedString());

		// Set wrong values
		DOMException e = assertThrows(DOMException.class, () ->
			lchColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () ->
			lchColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () ->
			lchColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () ->
			lchColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Serialization
		assertEquals("oklch(0.71834 0.0518 42.069)", value.getCssText());
		assertEquals("oklch(0.71834 0.0518 42.069)", lchColor.toString());
		assertEquals("oklch(.71834 .0518 42.069)", value.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(30);
		value.writeCssText(wri);
		assertEquals("oklch(0.71834 0.0518 42.069)", wri.toString());

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(75.77f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(60.4072f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(54.477f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);

		assertEquals("rgb(75.77%, 60.4%, 54.48%)", rgb.toString());
		ColorValue srgbValue = (ColorValue) rgb.packInValue();
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 1.1e-2f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 1.1e-2f);
		assertFalse(lchColor.equals(srgbValue));
		assertFalse(srgbValue.equals(lchColor));

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(67 13.422 13.794)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(67f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(13.422f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(13.794f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);

		// HSL indirect conversion
		HSLColorValue hslColor = srgbValue.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(16.7f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.1f);
		assertEquals(30.528f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(65.125f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Small Delta to converted value
		assertEquals(0f, hslColor.deltaE2000(lchColor), 0.01f);
		assertEquals(0f, lchColor.deltaE2000(hslColor), 0.01f);

		// DeltaE2000 to rgb
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(calc(25*2),63,77)");
		assertEquals(46.87f, lchColor.deltaE2000(rgbColor2), 0.01f);
		assertEquals(46.87f, rgbColor2.deltaE2000(lchColor), 0.01f);

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

		lchColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(0.87f, ((TypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);

		lchColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 27f));
		assertEquals(27f, ((TypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);

		lchColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 37f));
		assertEquals(37f, ((TypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-6f);

		assertThrows(NullPointerException.class, () -> lchColor.setComponent(0, null));

		e = assertThrows(DOMException.class, () -> lchColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> lchColor.setComponent(1, null));

		e = assertThrows(DOMException.class, () -> lchColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> lchColor.setComponent(2, null));

		e = assertThrows(DOMException.class, () -> lchColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> lchColor.setComponent(3, null));

		e = assertThrows(DOMException.class, () -> lchColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testOKLCHColorModel2() {
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
		assertEquals(0.041502f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0.0288f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(264.05f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(0.041502 0.0288 264.05)", value.getCssText());
		assertEquals("oklch(0.041502 0.0288 264.05)", lchColor.toString());
		assertEquals("oklch(.041502 .0288 264.05)", value.getMinifiedCssText("color"));

		assertTrue(lch.isInGamut(ColorSpace.srgb));
		assertTrue(lch.isInGamut(ColorSpace.ok_lab));

		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(1.002f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);

		assertEquals("rgb(0%, 0%, 1%)", rgb.toString());
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.0021f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.0021f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(0.042 0.265 -0.972)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.04234f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-4f);
		assertEquals(0.2652f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.972f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);

		// Delta to XYZ
		style.setCssText("color: color(xyz 0.0001399 0.0000558 0.0007373)");
		CSSValue xyzValue = style.getPropertyCSSValue("color");
		assertNotNull(xyzValue);
		assertEquals(CssType.TYPED, xyzValue.getCssValueType());
		assertEquals(Type.COLOR, xyzValue.getPrimitiveType());
		ColorValue xyzcolor = (ColorValue) xyzValue;
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzcolor.getColorModel());
		assertEquals(0f, color.deltaE2000(xyzcolor), 0.001f);

		// Delta to OKLab
		style.setCssText("color: oklab(0.0415 -0.003 -0.0286)");
		CSSValue okValue = style.getPropertyCSSValue("color");
		assertNotNull(okValue);
		assertEquals(CssType.TYPED, okValue.getCssValueType());
		assertEquals(Type.COLOR, okValue.getPrimitiveType());
		ColorValue okColor = (ColorValue) okValue;
		assertEquals(CSSColorValue.ColorModel.LAB, okColor.getColorModel());
		assertEquals(ColorSpace.ok_lab, okColor.getColor().getColorSpace());
		assertEquals(0.003f, color.deltaE2000(okColor), 0.001f);

		// To sRGB
		CSSColor srgb = lch.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("rgb(0%, 0%, 1%)", srgb.toString());

		// HSL
		CSSColor hsl = lch.toColorSpace("hsl");
		assertNotNull(hsl);
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("hsl(240, 100%, 0.5%)", hsl.toString());

		// HWB
		CSSColor hwb = lch.toColorSpace("hwb");
		assertNotNull(hwb);
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("hwb(240 0% 99%)", hwb.toString());

		// To A98 RGB
		CSSColor a98rgb = lch.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0 0 0.0378)", a98rgb.toString());

		// To Display P3
		CSSColor display_p3 = lch.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0 0 0.0091)", display_p3.toString());

		// To Prophoto RGB
		CSSColor prophoto = lch.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.0017 0.0003 0.0107)", prophoto.toString());

		// To REC 2020
		CSSColor rec2020 = lch.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.0002 0 0.0031)", rec2020.toString());

		// To XYZ
		CSSColor xyz = lch.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.00014 0.00006 0.00074)", xyz.toString());

		// To XYZ D50
		CSSColor xyzD50 = lch.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.00011 0.00005 0.00055)", xyzD50.toString());

		// To OK Lab
		CSSColor ok_lab = lch.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.041502 -0.00299 -0.02864)", ok_lab.toString());

		// To OK LCh
		CSSColor ok_lch = lch.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.041502 0.0288 264.05)", ok_lch.toString());

		// To CIE Lab
		CSSColor cie_lab = lch.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(0.0423 0.26524 -0.97224)", cie_lab.toString());

		// To CIE LCh
		CSSColor cie_lch = lch.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(0.0423 1.0078 285.26)", cie_lch.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModel3() {
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
		assertEquals(0.07f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0.48f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(29f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(0.07 0.48 29)", value.getCssText());
		assertEquals("oklch(0.07 0.48 29)", lchColor.toString());
		assertEquals("oklch(.07 .48 29)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(9.7%, 0%, 0%)", rgb.toString());
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(25.3f, lchColor.deltaE2000(srgbValue), 0.1f);
		assertEquals(25.3f, srgbValue.deltaE2000(lchColor), 0.1f);

		// HSL
		assertEquals("hsl(0, 100%, 4.85%)", ((RGBColor) rgb).toHSLColor().toString());

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		assertEquals("lab(1.507 74.897 48.94)", labColor.getCssText());

		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(1.507 89.469 33.162)", cielchColor.getCssText());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, color.deltaE2000(cielchColor), 0.001f);

		// Packing
		CSSColorValue lchValue = lch.packInValue();
		assertNotNull(lchValue);
		assertEquals(lchColor, lchValue);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModel4() {
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
		assertEquals("oklch(0.52 0.178 157.6grad)", value.getCssText());
		assertEquals("oklch(0.52 0.178 157.6grad)", lchColor.toString());
		assertEquals("oklch(.52 .178 157.6grad)", value.getMinifiedCssText("color"));
		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(0.178f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(157.6f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_GRAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(46.301 -47.199 50.163)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(46.3f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
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
		assertEquals("hsl(120, 100%, 25.39%)", ((RGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0.75f, lchColor.deltaE2000(srgbValue), 0.01f);
		assertEquals(0.75f, srgbValue.deltaE2000(lchColor), 0.01f);

		assertEquals(0f, lab.deltaEOK(lch), 1e-6f);
		assertEquals(4.565e-3f, lab.deltaEOK(rgb), 1e-6f);

		// xyz
		double[] xyz = lab.toXYZ(Illuminants.whiteD65);
		double[] sxyz = lch.toXYZ(Illuminants.whiteD65);
		assertEquals(xyz[0], sxyz[0], 1e-7, "Different component x.");
		assertEquals(xyz[1], sxyz[1], 2e-8, "Different component y.");
		assertEquals(xyz[2], sxyz[2], 1e-7, "Different component z.");
	}

	@Test
	public void testOKLCHColorModel5() {
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
		assertEquals(0.84f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.14f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(201f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Serialization
		assertEquals("oklch(0.84 0.14 201)", value.getCssText());
		assertEquals("oklch(0.84 0.14 201)", lchColor.toString());
		assertEquals("oklch(.84 .14 201)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(10.71%, 89.83%, 94.01%)", rgb.toString());
		assertEquals(10.71f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(89.83f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(94.01f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.001f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.001f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(82.767 -43.37 -18.784)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(82.77f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.01f);
		assertEquals(-43.37f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(-18.784f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LCh
		LCHColorValue cielchColor = lchColor.toLCHColorValue();
		LCHColor cielch = cielchColor.getColor();
		assertNotNull(cielch);
		assertEquals("lch(82.767 47.263 203.418)", cielch.toString());
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
		assertEquals("oklch(0.82 0.16 193)", lch2.toString());
		assertEquals(5.24f, lchColor.deltaE2000(lchColor2), 0.01f);
	}

	@Test
	public void testOKLCHColorModel6() {
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
		assertEquals(0.81948f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-3f);
		assertEquals(0.16f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(3.369f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_RAD), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.81948 0.16 3.369rad)", value.getCssText());
		assertEquals("oklch(0.81948 0.16 3.369rad)", lchColor.toString());
		assertEquals("oklch(.81948 .16 3.369rad)", value.getMinifiedCssText("color"));

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(80.801 -53.201 -13.685)", lab.toString());

		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(80.801 54.933 194.425)", cielchColor.getCssText());

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 89.16%, 88.01%)", rgb.toString());

		// To HSL
		assertEquals("hsl(179.226, 100%, 44.58%)", ((RGBColor) rgb).toHSLColor().toString());

		// Delta to converted RGB value near 2 (clamp)
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0f, color.deltaE2000(srgbValue), 2f);
		assertEquals(0f, srgbValue.deltaE2000(color), 2f);

		// Delta 0 to converted Lab
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModel7() {
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
		assertEquals(0.68f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(0.444f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(930f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.68 0.444 930)", value.getCssText());
		assertEquals("oklch(0.68 0.444 930)", lchColor.toString());
		assertEquals("oklch(.68 .444 930)", value.getMinifiedCssText("color"));

		assertTrue(lch.isInGamut(ColorSpace.ok_lch));
		assertTrue(lch.isInGamut(ColorSpace.ok_lab));
		assertTrue(lch.isInGamut(ColorSpace.cie_lch));
		assertTrue(lch.isInGamut(ColorSpace.cie_lab));
		assertTrue(lch.isInGamut(ColorSpace.xyz_d50));
		assertFalse(lch.isInGamut(ColorSpace.prophoto_rgb));

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(65.201 -85.261 -77.993)", lab.toString());

		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(65.201 115.552 222.451)", cielchColor.getCssText());

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 69.18%, 83.53%)", rgb.toString());
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(16.4f, lchColor.deltaE2000(srgbValue), 0.1f);
		assertEquals(16.4f, srgbValue.deltaE2000(lchColor), 0.1f);

		// HSL
		assertEquals("hsl(190.309, 100%, 41.77%)", ((RGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted Lab
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelIntegerL() {
		style.setCssText("color: oklch(.52 0.178 157.6grad);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		OKLCHColorValue lchColor = (OKLCHColorValue) color;
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);

		assertEquals("oklch(0.52 0.178 157.6grad)", value.getCssText());
		assertEquals("oklch(0.52 0.178 157.6grad)", lchColor.toString());
		assertEquals("oklch(.52 .178 157.6grad)", value.getMinifiedCssText("color"));

		CSSPrimitiveValue lightness = lch.getLightness();
		CSSPrimitiveValue chroma = lch.getChroma();
		CSSPrimitiveValue hue = lch.getHue();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertEquals(0.52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertEquals(0.178f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(157.6f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_GRAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(46.301 -47.199 50.163)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(46.301f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-3f);
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
		assertEquals("hsl(120, 100%, 25.39%)", ((RGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0.75f, lchColor.deltaE2000(srgbValue), 0.01f);
		assertEquals(0.75f, srgbValue.deltaE2000(lchColor), 0.01f);
	}

	@Test
	public void testOKLCHColorModelPcntChroma() {
		style.setCssText("color: oklch(4.1502% 7.2% 264.05deg);");
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
		assertEquals(0.041502f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0.0288f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(264.05f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.041502 0.0288 264.05)", value.getCssText());
		assertEquals("oklch(0.041502 0.0288 264.05)", lchColor.toString());
		assertEquals("oklch(.041502 .0288 264.05)", value.getMinifiedCssText("color"));

		// To RGB
		assertTrue(lch.isInGamut(ColorSpace.srgb));
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(1.002f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals("rgb(0%, 0%, 1%)", rgb.toString());
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.001f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.001f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(0.042 0.265 -0.972)", lab.toString());
		CSSPrimitiveValue labLightness = lab.getLightness();
		CSSPrimitiveValue a = lab.getA();
		CSSPrimitiveValue b = lab.getB();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.04234f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-4f);
		assertEquals(0.2652f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.972f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelPcntChromaClamp() {
		style.setCssText("color: oklch(4.1502% 157.2% 264.05deg);");
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
		NumberValue alpha = (NumberValue) lch.getAlpha();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertNotNull(alpha);
		assertEquals(0.041502f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0.4f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(264.05f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.041502 0.4 264.05)", value.getCssText());
		assertEquals("oklch(0.041502 0.4 264.05)", lchColor.toString());
		assertEquals("oklch(.041502 .4 264.05)", value.getMinifiedCssText("color"));
		assertTrue(alpha.isSpecified());

		// To RGB
		RGBAColor rgb = color.toRGBColor(true);
		NumberValue red = (NumberValue) rgb.getRed();
		NumberValue green = (NumberValue) rgb.getGreen();
		NumberValue blue = (NumberValue) rgb.getBlue();
		alpha = (NumberValue) rgb.getAlpha();
		assertEquals(0f, red.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertFalse(red.isSpecified());
		assertEquals(0f, green.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertFalse(green.isSpecified());
		assertEquals(0f, blue.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertFalse(blue.isSpecified());
		assertTrue(alpha.isSpecified());
		assertEquals("#000", rgb.toString());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(37.14f, lchColor.deltaE2000(srgbValue), 0.001f);
		assertEquals(37.14f, srgbValue.deltaE2000(lchColor), 0.001f);

		assertEquals(0.402147f, lch.deltaEOK(rgb), 1e-6f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(-13.802 158.902 -122.482)", lab.toString());
		NumberValue labLightness = (NumberValue) lab.getLightness();
		NumberValue a = (NumberValue) lab.getA();
		NumberValue b = (NumberValue) lab.getB();
		alpha = (NumberValue) lab.getAlpha();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(-13.802f, labLightness.getFloatValue(CSSUnit.CSS_NUMBER),
				1e-3f);
		assertEquals(158.902f, a.getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-122.482f, b.getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, alpha.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(labLightness.isSpecified());
		assertFalse(a.isSpecified());
		assertFalse(b.isSpecified());
		assertTrue(alpha.isSpecified());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);

		assertEquals(0f, lch.deltaEOK(lab), 1e-6f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelPcntChromaClampNeg() {
		style.setCssText("color: oklch(4.1502% -157.2% 264.05deg);");
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
		NumberValue alpha = (NumberValue) lch.getAlpha();
		assertNotNull(lightness);
		assertNotNull(chroma);
		assertNotNull(hue);
		assertNotNull(alpha);
		assertEquals(0.041502f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(264.05f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.041502 0 264.05)", value.getCssText());
		assertEquals("oklch(0.041502 0 264.05)", lchColor.toString());
		assertEquals("oklch(.041502 0 264.05)", value.getMinifiedCssText("color"));
		assertTrue(alpha.isSpecified());

		assertTrue(lch.isInGamut(ColorSpace.srgb));

		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals("rgb(0.09%, 0.09%, 0.09%)", rgb.toString());
		alpha = (NumberValue) rgb.getAlpha();
		assertTrue(alpha.isSpecified());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.001f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.001f);

		assertEquals(0f, lch.deltaEOK(rgb), 1e-6f);

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals("lab(0.065 0 0)", lab.toString());
		NumberValue labLightness = (NumberValue) lab.getLightness();
		NumberValue a = (NumberValue) lab.getA();
		NumberValue b = (NumberValue) lab.getB();
		alpha = (NumberValue) lab.getAlpha();
		assertNotNull(labLightness);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(0.065f, labLightness.getFloatValue(CSSUnit.CSS_NUMBER),
				1e-3f);
		assertEquals(0f, a.getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0f, b.getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, alpha.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(labLightness.isSpecified());
		assertFalse(a.isSpecified());
		assertFalse(b.isSpecified());
		assertTrue(alpha.isSpecified());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);

		assertEquals(0f, lch.deltaEOK(lab), 1e-6f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelAlpha() {
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
		assertEquals(0.45f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.4f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(264f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		assertEquals("oklch(0.45 0.4 264 / 0.8)", value.getCssText());
		assertEquals("oklch(0.45 0.4 264 / 0.8)", lchColor.toString());
		assertEquals("oklch(.45 .4 264/.8)", value.getMinifiedCssText("color"));

		// To LAB
		LABColorValue labColor = lchColor.toLABColorValue();
		assertEquals("lab(23.437 119.995 -148.148 / 0.8)", labColor.getCssText());

		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(23.437 190.648 309.006 / 0.8)", cielchColor.getCssText());

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(32.33%, 0%, 71.14%, 0.8)", rgb.toString());
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(12.85f, lchColor.deltaE2000(srgbValue), 0.01f);
		assertEquals(12.85f, srgbValue.deltaE2000(lchColor), 0.01f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelCalc() {
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
		assertEquals(Type.NUMERIC, lightness.getPrimitiveType());
		assertEquals(0.738f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(18.438f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.738 18.438 48.583)", value.getCssText());
		assertEquals("oklch(0.738 18.438 48.583)", lchColor.toString());
		assertEquals("oklch(.738 18.438 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#fff", rgb.toString());

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(255,255,254)");
		assertEquals(113.1f, lchColor.deltaE2000(rgbColor2), 0.1f);
		assertEquals(113.1f, rgbColor2.deltaE2000(lchColor), 0.1f);

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(113.1f, lchColor.deltaE2000(srgbValue), 0.1f);
		assertEquals(113.1f, srgbValue.deltaE2000(lchColor), 0.1f);

		// HSL
		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(113.1f, lchColor.deltaE2000(hslColor), 0.1f);
		assertEquals(113.1f, hslColor.deltaE2000(lchColor), 0.1f);
	}

	@Test
	public void testOKLCHColorModelCalc2() {
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
		assertEquals(0.817395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(Type.NUMERIC, chroma.getPrimitiveType());
		assertEquals(6f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.817395 6 48.583)", value.getCssText());
		assertEquals("oklch(0.817395 6 48.583)", lchColor.toString());
		assertEquals("oklch(.817395 6 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#fff", rgb.toString());
		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(70.3f, lchColor.deltaE2000(srgbValue), 0.1f);
		assertEquals(70.3f, srgbValue.deltaE2000(lchColor), 0.1f);

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(255,255,254)");
		assertEquals(70.3f, lchColor.deltaE2000(rgbColor2), 0.1f);
		assertEquals(70.3f, rgbColor2.deltaE2000(lchColor), 0.1f);

		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(70.3f, lchColor.deltaE2000(hslColor), 0.1f);
		assertEquals(70.3f, hslColor.deltaE2000(lchColor), 0.1f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelCalc3() {
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
		assertEquals(0.817395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(48.583f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.NUMERIC, hue.getPrimitiveType());
		assertEquals(32.86f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("oklch(0.817395 48.583 32.86)", value.getCssText());
		assertEquals("oklch(0.817395 48.583 32.86)", lchColor.toString());
		assertEquals("oklch(.817395 48.583 32.86)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#fff", rgb.toString());

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(255,255,254)");
		assertEquals(125.9f, lchColor.deltaE2000(rgbColor2), 0.1f);
		assertEquals(125.9f, rgbColor2.deltaE2000(lchColor), 0.1f);

		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(125.9f, lchColor.deltaE2000(hslColor), 0.1f);
		assertEquals(125.9f, hslColor.deltaE2000(lchColor), 0.1f);
	}

	@Test
	public void testOKLCHColorModelCalcAlpha() {
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
		assertEquals(0.79217f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.1921f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(122.42f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(Type.NUMERIC, alpha.getPrimitiveType());
		assertEquals(0.68f, ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals("oklch(0.79217 0.1921 122.42 / 0.68)", value.getCssText());
		assertEquals("oklch(0.79217 0.1921 122.42 / 0.68)", lchColor.toString());
		assertEquals("oklch(.79217 .1921 122.42/.68)", value.getMinifiedCssText("color"));

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(77.531 -29.34 75.106 / 0.68)", cielabColor.getCssText());
		assertEquals("lab(77.531 -29.34 75.106 / 0.68)", cielab.toString());
		assertEquals("lab(77.531 -29.34 75.106/.68)", cielabColor.getMinifiedCssText("color"));
		CSSPrimitiveValue ciel = cielab.getLightness();
		CSSPrimitiveValue ciea = cielab.getA();
		CSSPrimitiveValue cieb = cielab.getB();
		assertNotNull(ciel);
		assertNotNull(ciea);
		assertNotNull(cieb);
		assertEquals(77.5315f, ((CSSTypedValue) ciel).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-29.34f, ((CSSTypedValue) ciea).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(75.106f, ((CSSTypedValue) cieb).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgba(65.22%, 80.5%, 2.13%, 0.68)", rgb.toString());
		assertEquals(65.22f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(2.13f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(0.68f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0f, lchColor.deltaE2000(srgbValue), 0.001f);
		assertEquals(0f, srgbValue.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testOKLCHColorModelMath() {
		style.setCssText("color: oklch(pow(0.9,2) 0.16 asin(0.44));");
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
		assertEquals(0.81f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-3f);
		assertEquals(0.16f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		assertEquals(26.104f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		assertEquals("oklch(0.81 0.16 0.456rad)", lchColor.toString());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion() {
		style.setCssText("color: oklch(62.9% 0.26 29.24);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;
		// To rgb(100.5% -2.811% -1.578%)
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		assertFalse(color.getColor().isInGamut(ColorSpace.srgb_linear));
		assertTrue(color.getColor().isInGamut(ColorSpace.display_p3));

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

		ColorValue rgbValue = (ColorValue) rgb.packInValue();
		assertEquals(0.373f, color.deltaE2000(rgbValue), 0.001f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(54.383 81.574 71.273)", cielabColor.getCssText());

		// To LCH
		LCHColorValue lchColor = color.toLCHColorValue();
		assertEquals("lch(54.383 108.324 41.144)", lchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion2() {
		style.setCssText("color: oklch(75.5% 0.16 86.8);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		LABColorValue labColor = color.toLABColorValue();
		assertEquals("lab(71.795 11.228 78.738)", labColor.getCssText());

		LCHColorValue lchColor = color.toLCHColorValue();
		assertEquals("lch(71.795 79.535 81.884)", lchColor.getCssText());

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(86.44%, 65.25%, 0%)", rgb.toString());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(1.33f, color.deltaE2000(srgbValue), 0.01f);
		assertEquals(1.33f, srgbValue.deltaE2000(color), 0.01f);
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion3() {
		style.setCssText("color:oklch(63% 0.2 145);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 66.03%, 6.67%)", rgb.toString());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(2.32f, color.deltaE2000(srgbValue), 0.01f);
		assertEquals(2.32f, srgbValue.deltaE2000(color), 0.01f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(59.416 -56.416 49.442)", cielabColor.getCssText());

		LCHColorValue lchColor = color.toLCHColorValue();
		assertEquals("lch(59.416 75.015 138.769)", lchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion4() {
		style.setCssText("color:oklch(82% 0.12 49)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(100%, 66.24%, 45.39%)", rgb.toString());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(1.88f, color.deltaE2000(srgbValue), 0.01f);
		assertEquals(1.88f, srgbValue.deltaE2000(color), 0.01f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(78.449 29.311 38.005)", cielabColor.getCssText());

		// To LCh
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertNotNull(cielchColor);
		assertEquals("lch(78.449 47.995 52.36)", cielchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversion5() {
		style.setCssText("color:oklch(80% 0.14 194)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 86.06%, 85.73%)", rgb.toString());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(0.7f, color.deltaE2000(srgbValue), 0.1f);
		assertEquals(0.7f, srgbValue.deltaE2000(color), 0.1f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(78.304 -46.169 -12.853)", cielabColor.getCssText());

		// To LCh
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertNotNull(cielchColor);
		assertEquals("lch(78.304 47.924 195.556)", cielchColor.getCssText());
	}

	@Test
	public void testOKLCHColorModelClampedRGBConversionVeryLargeChroma() {
		style.setCssText("color:oklch(55% 0.2 145);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		assertFalse(color.getColor().isInGamut(ColorSpace.srgb_linear));
		assertTrue(color.getColor().isInGamut(ColorSpace.display_p3));

		// To RGB
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0%, 55.29%, 0%)", rgb.toString());

		CSSColorValue srgbValue = rgb.packInValue();
		assertEquals(2f, color.deltaE2000(srgbValue), 0.01f);
		assertEquals(2f, srgbValue.deltaE2000(color), 0.01f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		assertEquals("lab(50.094 -56.628 51.674)", cielabColor.getCssText());

		// To LCH
		LCHColorValue cielchColor = color.toLCHColorValue();
		assertEquals("lch(50.094 76.662 137.619)", cielchColor.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testOKLCHColorModelBadMixedType() {
		ColorValue value = new OKLCHColorValue();
		DOMException e = assertThrows(DOMException.class,
				() -> value.setCssText("oklab(67% 19.2 14.8)"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
	}

	@Test
	public void testOKLCHColorModelBadMixedTypeLCh() {
		ColorValue value = new OKLCHColorValue();
		DOMException e = assertThrows(DOMException.class,
				() -> value.setCssText("lch(65.195% 115 222)"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
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
