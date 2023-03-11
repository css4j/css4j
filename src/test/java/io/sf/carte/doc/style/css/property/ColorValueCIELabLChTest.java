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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

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
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorValueCIELabLChTest {

	static AbstractCSSStyleSheet sheet;
	CSSStyleDeclarationRule parentStyleRule;
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
	public void testLABColorModel() throws IOException {
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
		assertEquals(67f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
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
		assertEquals("lab(67 19.2 14.8)", lab.toString());
		assertEquals("lab(67 19.2 14.8)", lab.toMinifiedString());
		// Set wrong values
		DOMException e = assertThrows(DOMException.class, () -> labColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () -> labColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () -> labColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () -> labColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		// Serialization
		assertEquals("lab(67 19.2 14.8)", value.getCssText());
		assertEquals("lab(67 19.2 14.8)", labColor.toString());
		assertEquals("lab(67 19.2 14.8)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		value.writeCssText(wri);
		assertEquals("lab(67 19.2 14.8)", wri.toString());

		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(79.5237f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(58.806f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(53.9005f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
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
		assertEquals(67f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
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
		assertEquals(11.5f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(38.5f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(66.7f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0.01f, hslColor.deltaE2000(labColor), 0.01f);
		assertEquals(0.01f, labColor.deltaE2000(hslColor), 0.01f);

		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(calc(25*2),63,77)");
		e = assertThrows(DOMException.class, () -> labColor.deltaE2000(rgbColor2));
		assertEquals(DOMException.INVALID_STATE_ERR, e.code);
		e = assertThrows(DOMException.class, () -> rgbColor2.deltaE2000(labColor));
		assertEquals(DOMException.INVALID_STATE_ERR, e.code);

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
		assertEquals(87f, ((TypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);
		//
		labColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		labColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 27f));
		assertEquals(27f, ((TypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		assertThrows(NullPointerException.class, () -> labColor.setComponent(0, null));
		//
		DOMException ex = assertThrows(DOMException.class, () -> labColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);
		//
		assertThrows(NullPointerException.class, () -> labColor.setComponent(1, null));
		//
		ex = assertThrows(DOMException.class, () -> labColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);
		//
		assertThrows(NullPointerException.class, () -> labColor.setComponent(2, null));
		//
		ex = assertThrows(DOMException.class, () -> labColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);
		//
		assertThrows(NullPointerException.class, () -> labColor.setComponent(3, null));
		//
		ex = assertThrows(DOMException.class, () -> labColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);
	}

	@Test
	public void testLABColorModel2() {
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
		assertEquals(46.277f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-47.562f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(46.277 -47.562 48.583)", value.getCssText());
		assertEquals("lab(46.277 -47.562 48.583)", labColor.toString());
		assertEquals("lab(46.277 -47.562 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(50.195f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(46.277f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(67.989f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(134.3916f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);

		// Delta to OKLab
		style.setCssText("color: oklab(0.51974 -0.1403 0.10767)");
		CSSValue okValue = style.getPropertyCSSValue("color");
		assertNotNull(okValue);
		assertEquals(CssType.TYPED, okValue.getCssValueType());
		assertEquals(Type.COLOR, okValue.getPrimitiveType());
		ColorValue okColor = (ColorValue) okValue;
		assertEquals(CSSColorValue.ColorModel.LAB, okColor.getColorModel());
		assertEquals(ColorSpace.ok_lab, okColor.getColor().getColorSpace());
		assertEquals(0f, color.deltaE2000(okColor), 0.013f);
	}

	@Test
	public void testLABColorModel3() {
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
		assertEquals(54.2917f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(80.8125f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(69.8851f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(54.2917 80.8125 69.8851)", value.getCssText());
		assertEquals("lab(54.2917 80.8125 69.8851)", labColor.toString());
		assertEquals("lab(54.2917 80.8125 69.8851)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(54.2917f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(106.8390f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(40.8526f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testLABColorModel4() {
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
		assertEquals(54.237f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(80.7f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(70f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(54.237 80.7 70)", value.getCssText());
		assertEquals("lab(54.237 80.7 70)", labColor.toString());
		assertEquals("lab(54.237 80.7 70)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(99.884f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0.2073f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(54.237f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(106.82925f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(40.9387f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
	}

	@Test
	public void testLABColorModel5() {
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
		assertEquals(22.7233f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(20.0904f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-46.694f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(22.7233 20.0904 -46.694)", value.getCssText());
		assertEquals("lab(22.7233 20.0904 -46.694)", labColor.toString());
		assertEquals("lab(22.7233 20.0904 -46.694)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(16.8108f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(17.9048f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(49.2062f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
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
		assertEquals(22.7233f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
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
		assertEquals(23.0331f, ((CSSTypedValue) lightness2).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(14.973f, ((CSSTypedValue) a2).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-42.5619f, ((CSSTypedValue) b2).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab2.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(2.0373f, labColor.deltaE2000(labColor2), 0.0001f);
		assertEquals(2.0373f, labColor2.deltaE2000(labColor), 0.0001f);
	}

	@Test
	public void testLABColorModel6() {
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
		assertEquals(0.9311f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.9912f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.8882f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(0.9311 0.9912 -0.8882)", value.getCssText());
		assertEquals("lab(0.9311 0.9912 -0.8882)", labColor.toString());
		assertEquals("lab(.9311 .9912 -.8882)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(2.02766f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(1.04167f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(2.2085f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(0.9311f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(1.33093f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(318.1369f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testLABColorModelNumberLightness() {
		style.setCssText("color: lab(0.52 -0.14 0.11)");
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
		assertEquals(0.52f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-0.14f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0.11f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(0.52 -0.14 0.11)", value.getCssText());
		assertEquals("lab(0.52 -0.14 0.11)", labColor.toString());
		assertEquals("lab(.52 -.14 .11)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(0.64%, 0.79%, 0.63%)", rgb.toString());
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(0.52 0.178 141.843)", lch.toString());
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLABColorModelPcnt() {
		style.setCssText("color: lab(70.167% 68.65% -42.25%)");
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
		assertEquals(70.167f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(85.8125f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-52.8125f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(70.167 85.8125 -52.8125)", value.getCssText());
		assertEquals("lab(70.167 85.8125 -52.8125)", labColor.toString());
		assertEquals("lab(70.167 85.8125 -52.8125)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(100%, 33.52%, 100%)", rgb.toString());
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(33.52f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(100f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(70.167 100.762 328.39)", lch.toString());
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLABColorModelPcntClamp() {
		style.setCssText("color: lab(170.167% 168.65% -142.25%)");
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
		assertEquals(100f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(125f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-125f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(100 125 -125)", value.getCssText());
		assertEquals("lab(100 125 -125)", labColor.toString());
		assertEquals("lab(100 125 -125)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(100%, 99.62%, 100%)", rgb.toString());

		// To LCH
		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals("lch(100 176.777 315)", lch.toString());

		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLABColorModelAlpha() {
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
		assertEquals(81.74f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(-45.224f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(65.5257f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("lab(81.74 -45.224 65.5257 / 0.8)", value.getCssText());
		assertEquals("lab(81.74 -45.224 65.5257 / 0.8)", labColor.toString());
		assertEquals("lab(81.74 -45.224 65.5257/.8)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(55f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(88f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(25f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0.8f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
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
		assertEquals(81.74f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(79.61675f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(124.6124f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
	}

	@Test
	public void testLABColorModelCalc() {
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
		assertEquals(Type.NUMERIC, lightness.getPrimitiveType());
		assertEquals(73.8f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(18.438f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(73.8 18.438 48.583)", value.getCssText());
		assertEquals("lab(73.8 18.438 48.583)", labColor.toString());
		assertEquals("lab(73.8 18.438 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(90.69%, 65.74%, 35.68%)", rgb.toString());

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
		assertEquals(73.8f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(51.9641f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(69.21747f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0.006f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelCalc2() {
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
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(Type.NUMERIC, a.getPrimitiveType());
		assertEquals(6f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(81.7395 6 48.583)", value.getCssText());
		assertEquals("lab(81.7395 6 48.583)", labColor.toString());
		assertEquals("lab(81.7395 6 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(92.1941f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(77.551f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(92.19%, 77.55%, 43.19%)", rgb.toString());

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
		assertEquals(81.7395f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(48.9521f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(82.95962f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(0.002f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelCalc3() {
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
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(48.583f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.NUMERIC, b.getPrimitiveType());
		assertEquals(32.86f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(81.7395 48.583 32.86)", value.getCssText());
		assertEquals("lab(81.7395 48.583 32.86)", labColor.toString());
		assertEquals("lab(81.7395 48.583 32.86)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(100%, 70.72%, 64.94%)", rgb.toString());

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
		assertEquals(81.7395f, ((CSSTypedValue) lchLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(58.65226f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(34.073174, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0f, color.deltaE2000(lchColor), 0.001f);
		assertEquals(8.59f, color.deltaE2000(rgbValue), 0.01f);
	}

	@Test
	public void testLABColorModelCalcAlpha() {
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
		assertEquals(77.5325f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-29.3512f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(75.0654f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(Type.NUMERIC, alpha.getPrimitiveType());
		assertEquals(0.68f, ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(77.5325 -29.3512 75.0654 / 0.68)", value.getCssText());
		assertEquals("lab(77.5325 -29.3512 75.0654 / 0.68)", labColor.toString());
		assertEquals("lab(77.5325 -29.3512 75.0654/.68)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(65.204f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(2.3f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals("rgba(65.2%, 80.5%, 2.3%, 0.68)", rgb.toString());
	}

	@Test
	public void testLABColorModelClampedRGBConversion() {
		style.setCssText("color: lab(54.2324% 80.9288 70.8493);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		// To RGB (100%, -0.8%, -1.4%)
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

		// To RGB, clamped
		RGBAColor rgb = color.toRGBColor();
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#f00", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(0.334f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelClampedRGBConversion2() {
		style.setCssText("color: lab(72% 15.9649 130.0235);");
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
		assertEquals(85.74f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(65.89f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(85.74%, 65.89%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.948f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLABColorModelClampedRGBConversion3() {
		style.setCssText("color:lab(59% -100.8654 78.8047);");
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
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(65.42f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(16.79f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
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
		DOMException ex = assertThrows(DOMException.class,
				() -> value.setCssText("lch(67% 19.2 45.7deg)"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, ex.code);
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
		assertEquals(67f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
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
		assertEquals("lch(67 19.2 45.7)", lch.toString());
		assertEquals("lch(67 19.2 45.7)", lch.toMinifiedString());

		// Set wrong values
		DOMException e = assertThrows(DOMException.class, () -> lchColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Serialization
		assertEquals("lch(67 19.2 45.7)", value.getCssText());
		assertEquals("lch(67 19.2 45.7)", lchColor.toString());
		assertEquals("lch(67 19.2 45.7)", value.getMinifiedCssText("color"));
		//
		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		value.writeCssText(wri);
		assertEquals("lch(67 19.2 45.7)", wri.toString());

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(75.7543f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(60.4072f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(54.5142f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
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
		assertEquals(67f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
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
		assertEquals(16.7f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(30.5f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(65.1f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0.04f, hslColor.deltaE2000(lchColor), 0.01f);
		assertEquals(0.04f, lchColor.deltaE2000(hslColor), 0.01f);
		// DeltaE2000 failure
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText("rgb(calc(25*2),63,77)");
		assertThrows(DOMException.class, () -> lchColor.deltaE2000(rgbColor2));
		assertThrows(DOMException.class, () -> rgbColor2.deltaE2000(lchColor));

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
		assertEquals(87f, ((TypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);
		//
		lchColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 27f));
		assertEquals(27f, ((TypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		//
		lchColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 37f));
		assertEquals(37f, ((TypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG), 1e-6f);

		assertThrows(NullPointerException.class, () -> lchColor.setComponent(0, null));
		//
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		//
		assertThrows(NullPointerException.class, () -> lchColor.setComponent(1, null));
		//
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		//
		assertThrows(NullPointerException.class, () -> lchColor.setComponent(2, null));
		//
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		//
		assertThrows(NullPointerException.class, () -> lchColor.setComponent(3, null));
		//
		e = assertThrows(DOMException.class, () -> lchColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testLCHColorModel2() {
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
		assertEquals(0.0424f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(1.0058f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(285.2506f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.0424 1.0058 285.2506)", value.getCssText());
		assertEquals("lch(0.0424 1.0058 285.2506)", lchColor.toString());
		assertEquals("lch(.0424 1.0058 285.2506)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor(false);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
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
		assertEquals(0.0424f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(0.2645f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(-0.97038f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);

		// Delta to XYZ
		style.setCssText("color: color(xyz 0.0001397 0.0000559 0.0007359)");
		CSSValue xyzValue = style.getPropertyCSSValue("color");
		assertNotNull(xyzValue);
		assertEquals(CssType.TYPED, xyzValue.getCssValueType());
		assertEquals(Type.COLOR, xyzValue.getPrimitiveType());
		ColorValue xyzcolor = (ColorValue) xyzValue;
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzcolor.getColorModel());
		assertEquals(0.335f, color.deltaE2000(xyzcolor), 0.01f);

		// Delta to OKLCh
		style.setCssText("color: oklch(0.04151 0.02876 264.067)");
		CSSValue okValue = style.getPropertyCSSValue("color");
		assertNotNull(okValue);
		assertEquals(CssType.TYPED, okValue.getCssValueType());
		assertEquals(Type.COLOR, okValue.getPrimitiveType());
		ColorValue okColor = (ColorValue) okValue;
		assertEquals(CSSColorValue.ColorModel.LCH, okColor.getColorModel());
		assertEquals(ColorSpace.ok_lch, okColor.getColor().getColorSpace());
		assertEquals(0f, color.deltaE2000(okColor), 0.001f);
	}

	@Test
	public void testLCHColorModel3() {
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
		assertEquals(0.0424f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(1.0056f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(4.909f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.0424 1.0056 4.909)", value.getCssText());
		assertEquals("lch(0.0424 1.0056 4.909)", lchColor.toString());
		assertEquals("lch(.0424 1.0056 4.909)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(1.0942f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(9.13e-04f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
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
		assertEquals(0.0424f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(1.0019f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.0861f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testLCHColorModel4() {
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
		assertEquals(0.0424f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(1.0056f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(5.1234f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_GRAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.0424 1.0056 5.1234grad)", value.getCssText());
		assertEquals("lch(0.0424 1.0056 5.1234grad)", lchColor.toString());
		assertEquals("lch(.0424 1.0056 5.1234grad)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(1.093f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0.006, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(0.0424f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(1.0023f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.0808f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
	}

	@Test
	public void testLCHColorModel5() {
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
		assertEquals(22.7233f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(50.8326f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(293.2801f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Serialization
		assertEquals("lch(22.7233 50.8326 293.2801)", value.getCssText());
		assertEquals("lch(22.7233 50.8326 293.2801)", lchColor.toString());
		assertEquals("lch(22.7233 50.8326 293.2801)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(16.8108f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.0001f);
		assertEquals(17.9048f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
		assertEquals(49.2062f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.0001f);
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
		assertEquals(22.7233f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
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
		assertEquals(23.0331f, ((CSSTypedValue) lightness2).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(45.1188f, ((CSSTypedValue) chroma2).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(289.3815f, ((CSSTypedValue) hue2).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch2.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(2.0373f, lchColor.deltaE2000(lchColor2), 0.0001f);
	}

	@Test
	public void testLCHColorModel6() {
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
		assertEquals(0.9126f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0.956f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0.822f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_RAD), 1e-5f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(0.9126 0.956 0.822rad)", value.getCssText());
		assertEquals("lch(0.9126 0.956 0.822rad)", lchColor.toString());
		assertEquals("lch(.9126 .956 .822rad)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(2.193f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(1.0854f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.64646, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(0.9126f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(0.6508f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.70028f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, color.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(color), 0.001f);
	}

	@Test
	public void testLCHColorModelAlpha() {
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
		assertEquals(29.189f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(122.218f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(300.319f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("lch(29.189 122.218 300.319 / 0.8)", value.getCssText());
		assertEquals("lch(29.189 122.218 300.319 / 0.8)", lchColor.toString());
		assertEquals("lch(29.189 122.218 300.319/.8)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(8f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(95f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
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
		assertEquals(29.189f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(61.6973f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(-105.5020f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
	}

	@Test
	public void testLCHColorModelCalc() {
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
		assertEquals(Type.NUMERIC, lightness.getPrimitiveType());
		assertEquals(73.8f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(18.438f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(73.8 18.438 48.583)", value.getCssText());
		assertEquals("lch(73.8 18.438 48.583)", lchColor.toString());
		assertEquals("lch(73.8 18.438 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(82.48%, 67.86%, 61.42%)", rgb.toString());

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
		assertEquals(73.8f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(12.1974f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(13.82693f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText(rgb.toString());
		assertEquals(0f, color.deltaE2000(lchColor), 0.1f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.1f);
		assertEquals(0f, color.deltaE2000(labColor), 0.1f);
		assertEquals(0f, labColor.deltaE2000(color), 0.1f);
		assertEquals(0f, color.deltaE2000(rgbColor2), 0.1f);
		assertEquals(0f, rgbColor2.deltaE2000(color), 0.1f);
		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(0f, color.deltaE2000(hslColor), 0.1f);
		assertEquals(0f, hslColor.deltaE2000(lchColor), 0.1f);
	}

	@Test
	public void testLCHColorModelCalc2() {
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
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(Type.NUMERIC, chroma.getPrimitiveType());
		assertEquals(6f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(48.583f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(81.7395 6 48.583)", value.getCssText());
		assertEquals("lch(81.7395 6 48.583)", lchColor.toString());
		assertEquals("lch(81.7395 6 48.583)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(83.72%, 78.62%, 76.43%)", rgb.toString());

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
		assertEquals(81.7395f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(3.969207f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(4.499489f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText(rgb.toString());
		assertEquals(0f, color.deltaE2000(lchColor), 0.1f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.1f);
		assertEquals(0f, color.deltaE2000(labColor), 0.1f);
		assertEquals(0f, labColor.deltaE2000(color), 0.1f);
		assertEquals(0f, color.deltaE2000(rgbColor2), 0.1f);
		assertEquals(0f, rgbColor2.deltaE2000(color), 0.1f);
		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(0f, color.deltaE2000(hslColor), 0.1f);
		assertEquals(0.017f, hslColor.deltaE2000(lchColor), 0.1f);
	}

	@Test
	public void testLCHColorModelCalc3() {
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
		assertEquals(81.7395f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(48.583f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(Type.NUMERIC, hue.getPrimitiveType());
		assertEquals(32.86f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(81.7395 48.583 32.86)", value.getCssText());
		assertEquals("lch(81.7395 48.583 32.86)", lchColor.toString());
		assertEquals("lch(81.7395 48.583 32.86)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals("rgb(100%, 70.66%, 65.53%)", rgb.toString());

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
		assertEquals(81.7395f, ((CSSTypedValue) labLightness).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(40.8097f, ((CSSTypedValue) a).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(26.36056f, ((CSSTypedValue) b).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// DeltaE2000
		RGBColorValue rgbColor2 = new RGBColorValue();
		rgbColor2.setCssText(rgb.toString());
		assertEquals(0f, color.deltaE2000(lchColor), 0.1f);
		assertEquals(0f, lchColor.deltaE2000(color), 0.1f);
		assertEquals(0f, color.deltaE2000(labColor), 0.1f);
		assertEquals(0f, labColor.deltaE2000(color), 0.1f);
		assertEquals(5.86f, color.deltaE2000(rgbColor2), 0.1f);
		assertEquals(5.86f, rgbColor2.deltaE2000(color), 0.1f);
		HSLColorValue hslColor = rgbColor2.toHSLColorValue();
		assertEquals(5.89f, color.deltaE2000(hslColor), 0.1f);
		assertEquals(5.89f, hslColor.deltaE2000(lchColor), 0.1f);
	}

	@Test
	public void testLCHColorModelCalcAlpha() {
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
		assertEquals(77.5325f, ((CSSTypedValue) lightness).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(80.5996f, ((CSSTypedValue) chroma).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(111.356f, ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertEquals(Type.NUMERIC, alpha.getPrimitiveType());
		assertEquals(0.68f, ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals("lch(77.5325 80.5996 111.356 / 0.68)", value.getCssText());
		assertEquals("lch(77.5325 80.5996 111.356 / 0.68)", lchColor.toString());
		assertEquals("lch(77.5325 80.5996 111.356/.68)", value.getMinifiedCssText("color"));

		// To RGB
		RGBAColor rgb = color.toRGBColor();
		assertEquals(65.205f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(80.5f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(2.3f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0.68f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals("rgba(65.2%, 80.5%, 2.3%, 0.68)", rgb.toString());
	}

	@Test
	public void testLCHColorModelClampedRGBConversion() {
		style.setCssText("color: lch(54.2324% 107.5597 41.2006);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		ColorValue color = (ColorValue) value;

		// To RGB (100%, -0.8%, -1.4%)
		DOMException ex = assertThrows(DOMException.class, () -> color.toRGBColor(false));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);

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
		assertEquals(0.334f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversion2() {
		style.setCssText("color: lch(72% 131 83);");
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
		assertEquals(85.74f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(65.89f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(85.74%, 65.89%, 0%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.948f, color.deltaE2000(rgbValue), 0.001f);
		//
		LABColorValue labColor = rgbValue.toLABColorValue();
		LABColor lab = labColor.getColor();
		assertEquals(72.08f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.01f);
		assertEquals(10.612f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(74.444f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lab(72.0807 10.612 74.444)", labColor.getCssText());
		//
		LCHColorValue lchColor = labColor.toLCHColorValue();
		LCHColor lch = lchColor.getColor();
		assertEquals(72.08f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.01f);
		assertEquals(75.197f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(81.887f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("lch(72.0807 75.197 81.887)", lchColor.getCssText());
	}

	@Test
	public void testLCHColorModelClampedRGBConversion3() {
		style.setCssText("color:lch(59% 128 142);");
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
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(65.42f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(16.79f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 65.42%, 16.79%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(9.727f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversion4() {
		style.setCssText("color:lch(78.75% 81.3 50.4)");
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
		assertEquals(100f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(66.61f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(47.93f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(100%, 66.61%, 47.93%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(8.885f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversion5() {
		style.setCssText("color:lch(78% 54.5 195)");
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
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(85.725f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertEquals(85.01f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(0%, 85.73%, 85.01%)", rgb.toString());
		//
		RGBColorValue rgbValue = new RGBColorValue();
		rgbValue.setCssText(rgb.toString());
		assertEquals(2.32f, color.deltaE2000(rgbValue), 0.001f);
	}

	@Test
	public void testLCHColorModelClampedRGBConversionVeryLargeChroma() {
		style.setCssText("color:lch(59% 16000 142);");
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
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(65.42f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
		assertEquals(16.79f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.01f);
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
		DOMException ex = assertThrows(DOMException.class,
				() -> value.setCssText("lab(67% 19.2 14.8)"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, ex.code);
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
