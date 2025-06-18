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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
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
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.ColorValue.CSSRGBColor;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorValueTest {

	static AbstractCSSStyleSheet sheet;

	private static SyntaxParser syntaxParser;

	StyleRule parentStyleRule;
	AbstractCSSStyleDeclaration style;

	@BeforeAll
	public static void setUpBeforeAll() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
		syntaxParser = new SyntaxParser();
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
	public void testRGBColorComma() throws IOException {
		style.setCssText("color: rgb(8,63,255); ");
		assertEquals("#083fff", style.getPropertyValue("color"));
		assertEquals("color: #083fff; ", style.getCssText());

		style.setCssText("color: rgba(8,63,255,0.5); ");
		assertEquals("rgba(8, 63, 255, 0.5)", style.getPropertyValue("color"));
		assertEquals("color: rgba(8, 63, 255, 0.5); ", style.getCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		ColorValue val = (ColorValue) value;

		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		val.writeCssText(wri);
		assertEquals("rgba(8, 63, 255, 0.5)", wri.toString());

		RGBAColor rgb = val.toRGBColor();
		assertEquals(8, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.5f, (((CSSTypedValue) rgb.getAlpha())).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001);
	}

	@Test
	public void testRGBColor() throws IOException {
		style.setCssText("color: rgb(8.8 63.2 245.3)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("rgb(8.8 63.2 245.3)", val.getCssText());
		assertEquals("color: rgb(8.8 63.2 245.3); ", style.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());

		RGBAColor rgb = val.toRGBColor();

		// To sRGB
		CSSColor srgb = rgb.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("rgb(8.8 63.2 245.3)", srgb.toString());

		// HSL
		CSSColor hsl = rgb.toColorSpace("hsl");
		assertNotNull(hsl);
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("hsl(226.199, 93.07%, 49.82%)", hsl.toString());

		// HWB
		CSSColor hwb = rgb.toColorSpace("hwb");
		assertNotNull(hwb);
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("hwb(226.199 3.45% 3.8%)", hwb.toString());

		// To A98 RGB
		CSSColor a98rgb = rgb.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0.153224 0.256144 0.943633)", a98rgb.toString());

		// To Display P3
		CSSColor display_p3 = rgb.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0.106585 0.243843 0.924885)", display_p3.toString());

		// To Prophoto RGB
		CSSColor prophoto = rgb.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.344246 0.227768 0.882521)", prophoto.toString());

		// To REC 2020
		CSSColor rec2020 = rgb.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.20548 0.202572 0.908655)", rec2020.toString());

		// To XYZ
		CSSColor xyz = rgb.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.18424 0.10244 0.87636)", xyz.toString());

		// To XYZ D50
		CSSColor xyzD50 = rgb.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.15147 0.09197 0.65898)", xyzD50.toString());

		// To OK Lab
		CSSColor ok_lab = rgb.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.490395 -0.02705 -0.26836)", ok_lab.toString());

		// To OK LCh
		CSSColor ok_lch = rgb.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.490395 0.26972 264.245)", ok_lch.toString());

		// To CIE Lab
		CSSColor cie_lab = rgb.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(36.3604 44.09281 -95.27518)", cie_lab.toString());

		// To CIE LCh
		CSSColor cie_lch = rgb.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(36.3604 104.9835 294.834)", cie_lch.toString());
	}

	@Test
	public void testRGBColorIntegerComponents() throws IOException {
		style.setCssText("color: rgb(8 63 255); ");
		assertEquals("#083fff", style.getPropertyValue("color"));
		assertEquals("color: #083fff; ", style.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColorIntegerComponents2() throws IOException {
		style.setCssText("color: rgb(179 256 32)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");

		RGBAColor rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(179 256 32)", style.getPropertyValue("color"));
		assertEquals("rgb(179 256 32)", val.getMinifiedCssText("color"));

		assertTrue(rgb.isInGamut("rgb"));

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColorAlpha() throws IOException {
		style.setCssText("color: rgb(8 63 255/0.5); ");
		assertEquals("rgb(8 63 255 / 0.5)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 0.5); ", style.getCssText());
		StyleValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		ColorValue val = (ColorValue) value;
		RGBAColor rgb = val.toRGBColor();
		assertEquals(8f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.5f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
	}

	@Test
	public void testRGBAColorUpperCase() throws IOException {
		style.setCssText("color: RGBA(8, 63, 255, 0);");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		RGBAColor rgb = val.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(8f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(63f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
	}

	@Test
	public void testRGBAColor2() throws IOException {
		style.setCssText("color: rgba(8, 63, 255, 0); ");
		assertEquals("rgba(8, 63, 255, 0)", style.getPropertyValue("color"));
		assertEquals("color: rgba(8, 63, 255, 0); ", style.getCssText());

		style.setCssText("color: hsl(120, 100%, 50%); ");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);

		val = new RGBColorValue();
		val.setCssText("rgb(8 63 255)");
		assertEquals(1f,
				((CSSTypedValue) val.toRGBColor().getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
	}

	@Test
	public void testRGBColorTransparentAlpha() throws IOException {
		style.setCssText("color: rgb(8 63 255/0); ");
		assertEquals("rgb(8 63 255 / 0)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 0); ", style.getCssText());
	}

	@Test
	public void testRGBColorPcnt() throws IOException {
		style.setCssText("color: rgb(50%,50%,50%)");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals("rgb(50%, 50%, 50%)", value.getCssText());
		assertEquals("rgb(50%,50%,50%)", value.getMinifiedCssText("color"));

		LABColorValue labValue = value.toLABColorValue();
		assertNotNull(labValue);
		assertEquals("lab(53.3889 -0.002 -0.008)", labValue.getCssText());

		LCHColorValue lchValue = value.toLCHColorValue();
		assertNotNull(lchValue);
		assertEquals("lch(53.3889 0.009 254.931)", lchValue.getCssText());
	}

	@Test
	public void testRGBColorPcntAlpha() throws IOException {
		style.setCssText("color: rgb(8 63 255/1%); ");
		assertEquals("rgb(8 63 255 / 1%)", style.getPropertyValue("color"));
		assertEquals("color: rgb(8 63 255 / 1%); ", style.getCssText());
		assertEquals("color:rgb(8 63 255/1%)", style.getMinifiedCssText());
	}

	@Test
	public void testTransparentIdentifier() throws IOException {
		style.setCssText("color: transparent; ");
		assertEquals("transparent", style.getPropertyValue("color"));
		assertEquals("color: transparent; ", style.getCssText());

		style.setCssText("color: rgb(8,63); ");
		assertEquals("", style.getCssText());
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRGBColorHex8() throws IOException {
		style.setCssText("color:#ac98213a");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(0.22745f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001);
		assertEquals("rgb(172 152 33 / 0.2275)", val.getCssText());
		assertEquals("rgb(172 152 33/.2275)", val.getMinifiedCssText("color"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());

		// To sRGB
		CSSColor srgb = rgb.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("rgb(172 152 33 / 0.2275)", srgb.toString());

		// HSL
		CSSColor hsl = rgb.toColorSpace("hsl");
		assertNotNull(hsl);
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("hsla(51.367, 67.81%, 40.2%, 0.2275)", hsl.toString());

		// HWB
		CSSColor hwb = rgb.toColorSpace("hwb");
		assertNotNull(hwb);
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("hwb(51.367 12.94% 32.55% / 0.2275)", hwb.toString());

		// To A98 RGB
		CSSColor a98rgb = rgb.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0.647458 0.590538 0.195186 / 0.2275)", a98rgb.toString());

		// To Display P3
		CSSColor display_p3 = rgb.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0.661417 0.598901 0.231103 / 0.2275)", display_p3.toString());

		// To Prophoto RGB
		CSSColor prophoto = rgb.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.534775 0.526612 0.203774 / 0.2275)", prophoto.toString());

		// To REC 2020
		CSSColor rec2020 = rgb.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.597318 0.556611 0.18111 / 0.2275)", rec2020.toString());

		// To XYZ
		CSSColor xyz = rgb.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.28515 0.31338 0.05986 / 0.2275)", xyz.toString());

		// To XYZ D50
		CSSColor xyzD50 = rgb.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.30296 0.3178 0.0471 / 0.2275)", xyzD50.toString());

		// To OK Lab
		CSSColor ok_lab = rgb.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.677604 -0.02045 0.12934 / 0.2275)", ok_lab.toString());

		// To OK LCh
		CSSColor ok_lch = rgb.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.677604 0.13094 98.985 / 0.2275)", ok_lch.toString());

		// To CIE Lab
		CSSColor cie_lab = rgb.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(63.1604 -1.2929 59.47837 / 0.2275)", cie_lab.toString());

		// To CIE LCh
		CSSColor cie_lch = rgb.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(63.1604 59.4924 91.245 / 0.2275)", cie_lch.toString());
	}

	@Test
	public void testRGBColorHex8TransparentAlpha() throws IOException {
		style.setCssText("color:#ac982100");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(172 152 33 / 0)", style.getPropertyValue("color"));
		assertEquals("rgb(172 152 33/0)", val.getMinifiedCssText("color"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRGBColorHex6() throws IOException {
		style.setCssText("color:#ac9821");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#ac9821", style.getPropertyValue("color"));
		assertEquals("#ac9821", val.getMinifiedCssText("color"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRGBColorHex4() throws IOException {
		style.setCssText("color:#ac9a");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(0.66666f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("rgb(170 204 153 / 0.6667)", style.getPropertyValue("color"));
		assertEquals("rgb(170 204 153/.6667)", val.getMinifiedCssText("color"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRGBColorHex4TransparentAlpha() throws IOException {
		style.setCssText("color:#ac90");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("rgb(170 204 153 / 0)", style.getPropertyValue("color"));
		assertEquals("rgb(170 204 153/0)", val.getMinifiedCssText("color"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRGBColorHex3() throws IOException {
		style.setCssText("color:#ac9");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals("#ac9", style.getPropertyValue("color"));
		assertEquals("#ac9", val.getMinifiedCssText("color"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColorIdentifier() {
		final RGBColorValue val = new RGBColorValue();

		val.setCssText("magenta");
		RGBAColor rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);

		DOMException e = assertThrows(DOMException.class, () -> val.setCssText("notacolor"));
		assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		// Check that the values were kept unaltered
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);

		val.setCssText("BLUE");
		rgb = val.toRGBColor();
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(255f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColorClampWarning() {
		style.setCssText("color: rgb(179 -256 32)");
		assertEquals(1, style.getLength());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColorPcntClampWarning() {
		style.setCssText("color: rgb(179% -120% 320%)");
		assertEquals(1, style.getLength());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBColorValue() {
		ColorValue val = new RGBColorValue();
		val.setCssText("#abc");
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, rgb.getColorModel());
		assertEquals(ColorSpace.srgb, rgb.getColorSpace());
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(204f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);

		assertSame(rgb.getAlpha(), rgb.item(0));
		assertSame(rgb.getRed(), rgb.item(1));
		assertSame(rgb.getGreen(), rgb.item(2));
		assertSame(rgb.getBlue(), rgb.item(3));
		assertEquals(4, rgb.getLength());
		assertNull(rgb.item(4));

		assertMatch(Match.TRUE, val, "<color>");
		assertMatch(Match.TRUE, val, "<color>+");
		assertMatch(Match.TRUE, val, "<custom-ident> | <color>+");
		assertMatch(Match.FALSE, val, "<custom-ident>");
		assertMatch(Match.TRUE, val, "*");

		val.setCssText("#abc4");
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(0.266667f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.00001f);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(204f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);

		val.setCssText("#aabbb840");
		rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(0.25098f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.00001f);
		assertEquals(170f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(187f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(184f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
	}

	@Test
	public void testRGBColorValueComponentSetting() {
		ColorValue val = new RGBColorValue();
		val.setCssText("#0000");
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals(0f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.00001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(0f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);

		/*
		 * Component setting
		 */
		val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);

		val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 37f));
		assertEquals(37f, ((TypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-6f);

		val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 27f));
		assertEquals(27f, ((TypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-6f);

		val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-6f);

		/*
		 * Component setting: errors
		 */
		// Null Alpha
		assertThrows(NullPointerException.class, () -> val.setComponent(0, null));

		// Alpha Wrong type
		DOMException e = assertThrows(DOMException.class,
				() -> val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Null Red
		assertThrows(NullPointerException.class, () -> val.setComponent(1, null));

		// Red: Wrong type
		e = assertThrows(DOMException.class,
				() -> val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 12f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Red: % over 100%
		NumberValue num = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 120f);
		e = assertThrows(DOMException.class, () -> val.setComponent(1, num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);

		num.setCalculatedNumber(true);
		val.setComponent(1, num);
		assertEquals(100f, num.getFloatValue(CSSUnit.CSS_PERCENTAGE));

		// Red: % below 0%
		NumberValue negnum = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, -120f);
		e = assertThrows(DOMException.class, () -> val.setComponent(1, negnum));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);

		negnum.setCalculatedNumber(true);
		val.setComponent(1, negnum);
		assertEquals(0f, negnum.getFloatValue(CSSUnit.CSS_PERCENTAGE));

		// Null Green
		assertThrows(NullPointerException.class, () -> val.setComponent(2, null));

		// Green: Wrong type
		e = assertThrows(DOMException.class,
				() -> val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 12f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Null Blue
		assertThrows(NullPointerException.class, () -> val.setComponent(3, null));

		// Blue: Wrong type
		e = assertThrows(DOMException.class,
				() -> val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 12f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testGetMinifiedCssText() {
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
	public void testToRGBColor() {
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
	public void testRGBRelative() throws IOException {
		style.setCssText("color: rgb(from darkblue 16 32 b / 0.5)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("rgb(16 32 139 / 0.5)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBRelativeCalc() throws IOException {
		style.setCssText("color: rgb(from darkblue 16 32 calc(b - 100) / 0.5)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("rgb(16 32 39 / 0.5)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRGBRelativeHSL() throws IOException {
		// hsl(40 78% 28%) = rgb(49.84% 35.28% 6.16%)
		style.setCssText("color: rgb(from hsl(40 78% 28%) r 80 b)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("rgb(127.092 80 15.708)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testHSLColorModel() throws IOException {
		style.setCssText("color: hsl(120 100% 50%); ");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);

		CSSColor hsl = val.getColor();

		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("#0f0", rgb.toString());
		assertEquals("color: hsl(120 100% 50%); ", style.getCssText());

		// To sRGB
		CSSColor srgb = hsl.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("#0f0", srgb.toString());

		// HSL
		CSSColor hslback = rgb.toColorSpace("hsl");
		assertNotNull(hslback);
		assertEquals(CSSColorValue.ColorModel.HSL, hslback.getColorModel());
		assertEquals("hsl(120, 100%, 50%)", hslback.toString());

		// HWB
		CSSColor hwb = hsl.toColorSpace("hwb");
		assertNotNull(hwb);
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("hwb(120 0% 0%)", hwb.toString());

		// To A98 RGB
		CSSColor a98rgb = hsl.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0.564945 1 0.234448)", a98rgb.toString());

		// To Display P3
		CSSColor display_p3 = hsl.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0.458375 0.985272 0.298327)", display_p3.toString());

		// To Prophoto RGB
		CSSColor prophoto = hsl.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.540216 0.927609 0.304589)", prophoto.toString());

		// To REC 2020
		CSSColor rec2020 = hsl.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.567518 0.959285 0.269005)", rec2020.toString());

		// To XYZ
		CSSColor xyz = hsl.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.35758 0.71517 0.11919)", xyz.toString());

		// To XYZ D50
		CSSColor xyzD50 = hsl.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.38507 0.7169 0.09711)", xyzD50.toString());

		// To OK Lab
		CSSColor ok_lab = hsl.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.866439 -0.23392 0.17942)", ok_lab.toString());

		// To OK LCh
		CSSColor ok_lch = hsl.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.866439 0.2948 142.511)", ok_lch.toString());

		// To CIE Lab
		CSSColor cie_lab = hsl.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(87.8189 -79.28789 80.99087)", cie_lab.toString());

		// To CIE LCh
		CSSColor cie_lch = hsl.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(87.8189 113.3406 134.391)", cie_lch.toString());
	}

	@Test
	public void testHSLColorModelComma() throws IOException {
		style.setCssText("color: hsl(120, 100%, 50%); ");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("#0f0", rgb.toString());
		assertEquals("color: hsl(120, 100%, 50%); ", style.getCssText());
	}

	@Test
	public void testHSLColorModelDeg() throws IOException {
		style.setCssText("color: hsl(120deg, 100%, 50%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsl(120, 100%, 50%)", value.getCssText());
		assertEquals("#0f0", value.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		colorValue.writeCssText(wri);
		assertEquals("hsl(120, 100%, 50%)", wri.toString());

		HSLColor hsl = (HSLColor) colorValue.getColor();
		assertSame(hsl.getAlpha(), hsl.item(0));
		assertSame(hsl.getHue(), hsl.item(1));
		assertSame(hsl.getSaturation(), hsl.item(2));
		assertSame(hsl.getLightness(), hsl.item(3));
		assertEquals(4, hsl.getLength());
		assertNull(hsl.item(4));

		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("srgb", hsl.getColorSpace());
		assertEquals("hsl(120, 100%, 50%)", hsl.toString());
		assertEquals("hsl(120,100%,50%)", hsl.toMinifiedString());

		RGBAColor rgb = colorValue.toRGBColor();
		assertEquals(0, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(100f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(0, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);

		assertSame(rgb.getAlpha(), rgb.item(0));
		assertSame(rgb.getRed(), rgb.item(1));
		assertSame(rgb.getGreen(), rgb.item(2));
		assertSame(rgb.getBlue(), rgb.item(3));

		assertEquals("#0f0", rgb.toString());
		assertEquals("hsl(120, 100%, 50%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		CSSColorValue rgbColor = rgb.packInValue();
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(colorValue), 0.001f);

		LABColorValue labValue = colorValue.toLABColorValue();
		assertEquals("lab(87.8189 -79.288 80.991)", labValue.getCssText());
		assertEquals("lab(87.8189 -79.288 80.991)", labValue.getMinifiedCssText(""));
		assertEquals(0f, labValue.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(labValue), 0.001f);

		// Match
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	@Test
	public void testHSLColorModel2() throws IOException {
		style.setCssText("color: hsl(120 100% 90%); ");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(120 100% 90%)", value.getCssText());
		assertEquals("#cfc", value.getMinifiedCssText("color"));

		CSSColor hsl = value.getColor();
		assertNotNull(hsl);
		assertEquals("hsl(120 100% 90%)", hsl.toString());

		assertTrue(hsl.isInGamut(ColorSpace.srgb));
		assertTrue(hsl.isInGamut(ColorSpace.srgb_linear));
		assertTrue(hsl.isInGamut(ColorSpace.cie_lab));

		// Packing
		CSSColorValue hslValue = hsl.packInValue();
		assertNotNull(hslValue);
		assertEquals(value, hslValue);

		// To RGB
		RGBAColor rgb = value.toRGBColor();
		assertEquals(80f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(100f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(80, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals("#cfc", rgb.toString());
		assertEquals("hsl(120 100% 90%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

		double[] xyz = rgb.toXYZ(Illuminants.whiteD50);
		double[] hxyz = hsl.toXYZ(Illuminants.whiteD50);
		assertEquals(xyz[0], hxyz[0], 5e-8, "Different component x.");
		assertEquals(xyz[1], hxyz[1], 3e-8, "Different component y.");
		assertEquals(xyz[2], hxyz[2], 4e-8, "Different component z.");

		xyz = rgb.toXYZ(Illuminant.D65);
		hxyz = hsl.toXYZ(Illuminants.whiteD65);
		assertEquals(xyz[0], hxyz[0], 3e-8, "Different component x.");
		assertEquals(xyz[1], hxyz[1], 2e-8, "Different component y.");
		assertEquals(xyz[2], hxyz[2], 5e-8, "Different component z.");
	}

	@Test
	public void testHSLColorModelCalcHue() throws IOException {
		style.setCssText("color: hsl(calc(120) 100% 50%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsl(120 100% 50%)", value.getCssText());
		assertEquals("#0f0", value.getMinifiedCssText("color"));
		assertEquals(CSSColorValue.ColorModel.HSL, colorValue.getColorModel());
		HSLColor hsl = ((HSLColorValue) value).getColor();
		assertEquals(120, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(100f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(50, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals("hsl(120 100% 50%)", hsl.toString());
	}

	@Test
	public void testHSLColorModelCalcHueAlpha() throws IOException {
		style.setCssText("color: hsl(calc(120) 100% 50%/calc(0.9)); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSColorValue.ColorModel.HSL, ((CSSColorValue) value).getColorModel());
		ColorValue val = (ColorValue) value;
		HSLColor hsl = (HSLColor) val.getColor();
		assertEquals(120f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(100f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(50, ((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(0.9f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals("hsl(120 100% 50% / 0.9)", hsl.toString());
		// Set components
		val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);

		val.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);

		val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 27f));
		assertEquals(27f, ((TypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-6f);

		val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-6f);

		assertThrows(NullPointerException.class, () -> val.setComponent(0, null));

		DOMException e = assertThrows(DOMException.class,
				() -> val.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> val.setComponent(1, null));

		e = assertThrows(DOMException.class, () -> val.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> val.setComponent(2, null));

		e = assertThrows(DOMException.class,
				() -> val.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> val.setComponent(3, null));

		e = assertThrows(DOMException.class,
				() -> val.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testHSLColorModelCommaAlpha() {
		style.setCssText("color: hsl(240, 100%, 50%, 0.5); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsla(240, 100%, 50%, 0.5)", value.getCssText());
		assertEquals("hsla(240,100%,50%,.5)", value.getMinifiedCssText("color"));

		CSSColor hsl = colorValue.getColor();
		assertTrue(hsl.isInGamut(ColorSpace.srgb));
		assertTrue(hsl.isInGamut(ColorSpace.srgb_linear));
		assertTrue(hsl.isInGamut(ColorSpace.cie_lab));
		assertEquals("hsla(240, 100%, 50%, 0.5)", hsl.toColorSpace("hsl").toString());

		RGBAColor rgb = colorValue.toRGBColor();
		assertEquals("rgba(0%, 0%, 100%, 0.5)", rgb.toString());
		assertEquals("hsla(240, 100%, 50%, 0.5)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		CSSColorValue rgbColor = rgb.packInValue();
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);
	}

	@Test
	public void testHSLColorModelCommaAlpha2() {
		style.setCssText("color: hsla(40.56, 75%, 28%, 0.75); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsla(40.56, 75%, 28%, 0.75)", value.getCssText());
		assertEquals("hsla(40.56,75%,28%,.75)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("49%", rgb.getRed().getCssText());
		assertEquals("rgba(49%, 35.39%, 7%, 0.75)", rgb.toString());
	}

	@Test
	public void testHSLColorModelSlashAlpha() {
		style.setCssText("color: hsl(240 100% 50% / 0.5); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(240 100% 50% / 0.5)", value.getCssText());
		assertEquals("hsl(240 100% 50%/.5)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(0% 0% 100% / 0.5)", rgb.toString());
		assertEquals("hsl(240 100% 50% / 0.5)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelSlashAlpha2() {
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
		assertEquals("hsl(40.56 75% 28% / 0.75)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		CSSColorValue rgbColor = rgb.packInValue();
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.0001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.0001f);
	}

	@Test
	public void testHSLColorModelHueTurns() {
		style.setCssText("color: hsl(0.75turn 75% 28% / 0.75); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(0.75turn 75% 28% / 0.75)", value.getCssText());
		assertEquals("hsl(.75turn 75% 28%/.75)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(28% 7% 49% / 0.75)", rgb.toString());
		assertEquals("hsl(270 75% 28% / 0.75)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelHueRadians() {
		style.setCssText("color: hsl(1.217rad 75% 28% / 0.75); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsl(1.217rad 75% 28% / 0.75)", value.getCssText());
		assertEquals("hsl(1.217rad 75% 28%/.75)", value.getMinifiedCssText("color"));

		RGBAColor rgb = colorValue.toRGBColor();
		assertEquals("rgb(42.19% 49% 7% / 0.75)", rgb.toString());
		assertEquals("hsl(69.729 75% 28% / 0.75)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		CSSColorValue rgbColor = rgb.packInValue();
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);

		assertEquals(0f, colorValue.getColor().deltaEOK(rgb), 1e-6f);
	}

	@Test
	public void testHSLColorModelBigHueCommas() {
		style.setCssText("color: hsl(759.28, 85%, 24%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(759.28, 85%, 24%)", value.getCssText());
		assertEquals("hsl(759.28,85%,24%)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(44.4%, 30.31%, 3.6%)", rgb.toString());
		assertEquals("hsl(39.28, 85%, 24%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelBigHue() {
		style.setCssText("color: hsl(759.28 85% 24%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(759.28 85% 24%)", value.getCssText());
		assertEquals("hsl(759.28 85% 24%)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(44.4% 30.31% 3.6%)", rgb.toString());
		assertEquals("hsl(39.28 85% 24%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelNegativeHue() {
		style.setCssText("color: hsl(-169.88, 95%, 35%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(-169.88, 95%, 35%)", value.getCssText());
		assertEquals("hsl(-169.88,95%,35%)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(1.75%, 57.03%, 68.25%)", rgb.toString());
		assertEquals("hsl(190.12, 95%, 35%)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

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
	public void testHSLColorModelNearlyHue360() {
		style.setCssText("color: hsl(359.9999deg 95% 35%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(359.9999 95% 35%)", value.getCssText());
		assertEquals("hsl(359.9999 95% 35%)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(68.25% 1.75% 1.75%)", rgb.toString());
		assertEquals("hsl(0 95% 35%)", ((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelBadHueUnit() {
		style.setCssText("color: hsl(179px 65% 19%)");
		assertEquals(0, style.getLength());

		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testHSLColorModelNearlyHueOne() {
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
	public void testHSLColorModelAllCalc() {
		style.setCssText("color: hsl(calc(12.1) calc(25%) calc(48%)/calc(.7));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(12.1 25% 48% / 0.7)", value.getCssText());
		assertEquals("hsl(12.1 25% 48%/.7)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(60% 40.84% 36% / 0.7)", rgb.toString());
		assertEquals("hsl(12.1 25% 48% / 0.7)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelMath() {
		style.setCssText("color: hsl(atan(-2.0503) 11% 42%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(-1.117rad 11% 42%)", value.getCssText());
	}

	@Test
	public void testHSLColorModelIntegers() {
		style.setCssText("color: hsl(240, 100, 50, 0.5); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		CSSColorValue colorValue = (CSSColorValue) value;
		assertEquals("hsla(240, 100, 50, 0.5)", value.getCssText());
		assertEquals("hsla(240,100,50,.5)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgba(0%, 0%, 100%, 0.5)", rgb.toString());
		assertEquals("hsla(240, 100%, 50%, 0.5)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());

		// Delta 0 to converted value
		CSSColorValue rgbColor = rgb.packInValue();
		assertEquals(0f, rgbColor.deltaE2000(colorValue), 0.001f);
		assertEquals(0f, colorValue.deltaE2000(rgbColor), 0.001f);
	}

	@Test
	public void testHSLColorModelIntegers2() {
		style.setCssText("color: hsl(240 100 50 / 0.5); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsl(240 100 50 / 0.5)", value.getCssText());
		assertEquals("hsl(240 100 50/.5)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(0% 0% 100% / 0.5)", rgb.toString());
		assertEquals("hsl(240 100% 50% / 0.5)",
				((ColorValue.CSSRGBColor) rgb).toHSLColor().toString());
	}

	@Test
	public void testHSLColorModelIntegers3() {
		style.setCssText("color: hsla(40.56, 75, 28, 75%); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hsla(40.56, 75, 28, 75%)", value.getCssText());
		assertEquals("hsla(40.56,75,28,75%)", value.getMinifiedCssText("color"));

		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("49%", rgb.getRed().getCssText());
		assertEquals("rgba(49%, 35.39%, 7%, 75%)", rgb.toString());
	}

	@Test
	public void testHSLRelative() throws IOException {
		// peru is hsl(29.577 58.678% 52.549%)
		style.setCssText("color: hsl(from peru h 32% l / 0.5)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("hsl(29.577 32% 52.549% / 0.5)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testHSLRelativeCalc() throws IOException {
		// peru is hsl(29.577 58.678% 52.549%)
		style.setCssText("color: hsl(from peru calc(2*h) 32% calc(l - 25) / 0.5)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("hsl(59.154 32% 27.549% / 0.5)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testHWBColorModel() throws IOException {
		style.setCssText("color: hwb(205 19% 14%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		ColorValue color = (ColorValue) value;
		assertEquals(CSSColorValue.ColorModel.HWB, color.getColorModel());
		assertEquals("hwb(205 19% 14%)", value.getCssText());
		assertEquals("hwb(205 19% 14%)", value.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(24);
		color.writeCssText(wri);
		assertEquals("hwb(205 19% 14%)", wri.toString());

		HWBColor hwb = (HWBColor) color.getColor();
		assertSame(hwb.getAlpha(), hwb.item(0));
		assertSame(hwb.getHue(), hwb.item(1));
		assertSame(hwb.getWhiteness(), hwb.item(2));
		assertSame(hwb.getBlackness(), hwb.item(3));
		assertEquals(4, hwb.getLength());
		assertNull(hwb.item(4));

		assertTrue(hwb.isInGamut(ColorSpace.srgb));
		assertTrue(hwb.isInGamut(ColorSpace.srgb_linear));
		assertTrue(hwb.isInGamut(ColorSpace.cie_lab));

		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("srgb", hwb.getColorSpace());
		assertEquals("hwb(205 19% 14%)", hwb.toString());
		assertEquals("hwb(205 19% 14%)", hwb.toMinifiedString());

		RGBAColor rgb = color.toRGBColor();
		assertEquals(19f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(58.08334f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(86f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals("rgb(19%, 58.08%, 86%)", rgb.toString());

		assertEquals(0f, hwb.deltaEOK(rgb), 1e-6f);

		// Match
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));

		// To sRGB
		CSSColor srgb = hwb.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("rgb(19%, 58.08%, 86%)", srgb.toString());

		// HSL
		CSSColor hsl = hwb.toColorSpace("hsl");
		assertNotNull(hsl);
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("hsl(205, 70.53%, 52.5%)", hsl.toString());

		assertEquals(0f, hwb.deltaEOK(hsl), 1e-6f);

		// HWB
		CSSColor hwbConv = rgb.toColorSpace("hwb");
		assertNotNull(hwbConv);
		assertEquals(CSSColorValue.ColorModel.HWB, hwbConv.getColorModel());
		assertEquals("hwb(205 19% 14%)", hwbConv.toString());

		// To A98 RGB
		CSSColor a98rgb = hwb.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0.36041 0.575464 0.846791)", a98rgb.toString());

		assertEquals(0f, hwb.deltaEOK(a98rgb), 1e-6f);

		// To Display P3
		CSSColor display_p3 = hwb.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0.308246 0.572865 0.837371)", display_p3.toString());

		assertEquals(0f, hwb.deltaEOK(display_p3), 1e-6f);

		// To Prophoto RGB
		CSSColor prophoto = hwb.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.424349 0.495048 0.787508)", prophoto.toString());

		assertEquals(0f, hwb.deltaEOK(prophoto), 1e-6f);

		// To REC 2020
		CSSColor rec2020 = hwb.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.365024 0.523525 0.814463)", rec2020.toString());

		assertEquals(0f, hwb.deltaEOK(rec2020), 1e-6f);

		// To XYZ
		CSSColor xyz = hwb.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.24672 0.26983 0.71135)", xyz.toString());

		assertEquals(0f, hwb.deltaEOK(xyz), 1e-6f);

		// To XYZ D50
		CSSColor xyzD50 = hwb.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.22903 0.26243 0.53681)", xyzD50.toString());

		assertEquals(0f, hwb.deltaEOK(xyzD50), 1e-6f);

		// To OK Lab
		CSSColor ok_lab = hwb.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.643424 -0.05989 -0.12588)", ok_lab.toString());

		assertEquals(0f, hwb.deltaEOK(ok_lab), 1e-6f);

		// To OK LCh
		CSSColor ok_lch = hwb.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.643424 0.1394 244.557)", ok_lch.toString());

		assertEquals(0f, hwb.deltaEOK(ok_lch), 1e-6f);

		// To CIE Lab
		CSSColor cie_lab = hwb.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(58.2667 -10.46205 -45.24767)", cie_lab.toString());

		assertEquals(0f, hwb.deltaEOK(cie_lab), 1e-6f);

		// To CIE LCh
		CSSColor cie_lch = hwb.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(58.2667 46.4414 256.981)", cie_lch.toString());

		assertEquals(0f, hwb.deltaEOK(cie_lch), 1e-6f);
	}

	@Test
	public void testHWBColorModel000() throws IOException {
		style.setCssText("color: hwb(0 0% 0%); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR, value.getPrimitiveType());
		assertEquals("hwb(0 0% 0%)", value.getCssText());
		assertEquals("#f00", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("#f00", rgb.toString());
	}

	@Test
	public void testHWBColorModel2() throws IOException {
		style.setCssText("color: hwb(357 25% 12%); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(357 25% 12%)", value.getCssText());
		assertEquals("hwb(357 25% 12%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(88%, 25%, 28.15%)", rgb.toString());

		// Set components
		ColorValue hwbColor = (ColorValue) value;
		HWBColor hwb = (HWBColor) hwbColor.getColor();
		hwbColor.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0.427f));
		assertEquals(0.427f, ((TypedValue) hwb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);

		hwbColor.setComponent(1, NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 37f));
		assertEquals(37f, ((TypedValue) hwb.getHue()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);

		hwbColor.setComponent(2, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 27f));
		assertEquals(27f, ((TypedValue) hwb.getWhiteness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-6f);

		hwbColor.setComponent(3, NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 87f));
		assertEquals(87f, ((TypedValue) hwb.getBlackness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-6f);

		assertThrows(NullPointerException.class, () -> hwbColor.setComponent(0, null));

		DOMException e = assertThrows(DOMException.class, () -> hwbColor.setComponent(0,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 0.9f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> hwbColor.setComponent(1, null));

		e = assertThrows(DOMException.class, () -> hwbColor.setComponent(1,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> hwbColor.setComponent(2, null));

		e = assertThrows(DOMException.class, () -> hwbColor.setComponent(2,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertThrows(NullPointerException.class, () -> hwbColor.setComponent(3, null));

		e = assertThrows(DOMException.class, () -> hwbColor.setComponent(3,
				NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 12)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testHWBColorModelCalc() {
		style.setCssText("color: hwb(calc(61) 37% calc(1 * 8%) / calc(0.75)); ");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(61 37% 8% / 0.75)", value.getCssText());
		assertEquals("hwb(61 37% 8%/.75)", value.getMinifiedCssText("color"));

		HWBColor hwb = ((HWBColorValue) value).getColor();
		assertEquals("hwb(61 37% 8% / 0.75)", hwb.toString());
	}

	@Test
	public void testHWBColorModelSqrtMin() {
		style.setCssText("color: hwb(sqrt(3721) 37% min(8%,18%) / sqrt(0.5625));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(61 37% 8% / 0.75)", value.getCssText());
		assertEquals("hwb(61 37% 8%/.75)", value.getMinifiedCssText("color"));
		HWBColor hwb = ((HWBColorValue) value).getColor();
		assertEquals("hwb(61 37% 8% / 0.75)", hwb.toString());
	}

	@Test
	public void testHWBColorModelAlpha() {
		style.setCssText("color: hwb(61 37% 8% / 0.75); ");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(61 37% 8% / 0.75)", value.getCssText());
		assertEquals("hwb(61 37% 8%/.75)", value.getMinifiedCssText("color"));
		RGBAColor rgb = value.toRGBColor();
		assertEquals("rgba(91.08%, 92%, 37%, 0.75)", rgb.toString());

		CSSColor hwb = value.getColor();
		assertNotNull(hwb);
		assertEquals("hwb(61 37% 8% / 0.75)", hwb.toString());

		// Packing
		CSSColorValue hwbValue = hwb.packInValue();
		assertNotNull(hwbValue);
		assertEquals(value, hwbValue);
	}

	@Test
	public void testHWBColorModel3() {
		style.setCssText("color: hwb(73.29 22% 16%);");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(73.29 22% 16%)", value.getCssText());
		assertEquals("hwb(73.29 22% 16%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(70.27%, 84%, 22%)", rgb.toString());
	}

	@Test
	public void testHWBColorModel4() {
		style.setCssText("color: hwb(43.6 37% 8% / 0.75);");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(43.6 37% 8% / 0.75)", value.getCssText());
		assertEquals("hwb(43.6 37% 8%/.75)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgba(92%, 76.97%, 37%, 0.75)", rgb.toString());
	}

	@Test
	public void testHWBColorModel5() {
		style.setCssText("color: hwb(255 33% 13%);");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(255 33% 13%)", value.getCssText());
		assertEquals("hwb(255 33% 13%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals("rgb(46.5%, 33%, 87%)", rgb.toString());
	}

	@Test
	public void testHWBColorModel6() {
		style.setCssText("color: hwb(179 65% 19%);");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(179 65% 19%)", value.getCssText());
		assertEquals("hwb(179 65% 19%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(65f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(81f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(80.73333f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(65%, 81%, 80.73%)", rgb.toString());
	}

	@Test
	public void testHWBColorModelDeg() {
		style.setCssText("color: hwb(179deg 65% 19%); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(179 65% 19%)", value.getCssText());
		assertEquals("hwb(179 65% 19%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(65f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(81f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(80.73333f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(65%, 81%, 80.73%)", rgb.toString());
	}

	@Test
	public void testHWBColorModelRadians() {
		style.setCssText("color: hwb(3.124139rad 65% 19%)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(3.124139rad 65% 19%)", value.getCssText());
		assertEquals("rgb(65%,81%,80.73%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(65f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(81f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(80.73333f,
				((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("rgb(65%, 81%, 80.73%)", rgb.toString());
	}

	@Test
	public void testHWBColorModelRadians2() {
		style.setCssText("color: hwb(0.5rad 0.5% 0.19%); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(0.5rad 0.5% 0.19%)", value.getCssText());
		assertEquals("hwb(.5rad .5% .19%)", value.getMinifiedCssText("color"));
		RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
		assertEquals(99.809998f,
				((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(47.91703f,
				((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(0.5f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals("rgb(99.81%, 47.92%, 0.5%)", rgb.toString());
	}

	@Test
	public void testHWBColorModelMath() {
		style.setCssText("color: hwb(acos(-0.9998477) 65% 19%); ");
		StyleValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("hwb(3.124rad 65% 19%)", value.getCssText());
	}

	@Test
	public void testHWBColorModelBadHue() {
		style.setCssText("color: hwb(179px 65% 19%); ");
		assertEquals(0, style.getLength());

		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testHWBRelative() throws IOException {
		// peru is hwb(29.577 24.706% 19.608%)
		style.setCssText("color: hwb(from peru h 32% b / 0.5)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("hwb(29.577 32% 19.6078% / 0.5)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testHWBRelativeCalc() throws IOException {
		// peru is hwb(29.577 24.706% 19.608%)
		style.setCssText("color: hwb(from peru calc(2*h) 32% calc(b + 20.9922) / 0.5)");
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals("hwb(59.155 32% 40.6% / 0.5)", val.getCssText());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testVar() {
		style.setCssText("color: rgb(var(--foo), 0.7); ");
		assertEquals("rgb(var(--foo), 0.7)", style.getPropertyValue("color"));
		assertEquals("color: rgb(var(--foo), 0.7); ", style.getCssText());
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, value.getPrimitiveType());
	}

	@Test
	public void testCalc() {
		style.setCssText("color: rgb(calc(30%) calc(15%) calc(99%)/ calc(2*0.35)); ");
		assertEquals("rgb(30% 15% 99% / 0.7)", style.getPropertyValue("color"));
		CSSValue value = style.getPropertyCSSValue("color");
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
	}

	@Test
	public void testCalcHsl() {
		style.setCssText("color: hsl(calc(30deg) calc(15%) calc(99%) / calc(2*0.35)); ");
		assertEquals("hsl(30 15% 99% / 0.7)", style.getPropertyValue("color"));
		StyleValue value = style.getPropertyCSSValue("color");
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
		style.setCssText("color: rgba(0, 0, 0, 5%)");
		ColorValue value = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		RGBAColor rgb = value.toRGBColor();
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
		style.setCssText("color: rgba(8,63,255,0.5); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color4 = rgbColor.toRGBColor();
		assertNotNull(color4);
		assertEquals(8, (int) ((CSSTypedValue) color4.getRed()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(63,
				(int) ((CSSTypedValue) color4.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(255,
				(int) ((CSSTypedValue) color4.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(128f,
				((CSSTypedValue) color4.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER) * 256,
				0.001f);

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
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		// DeltaE2000
		assertEquals(54.11f, rgbColor.deltaE2000(rgbColor2), 0.01f);
		assertEquals(54.11f, rgbColor2.deltaE2000(rgbColor), 0.01f);
		// Check component access
		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 0.6f);
		rgbColor2.setComponent(0, number);
		assertEquals(0.6f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.6f,
				((CSSTypedValue) rgbColor2.getComponent(0)).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 178f);
		rgbColor2.setComponent(3, number);
		rgbColor2.setComponent(4, number);
		assertEquals(178f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(178f,
				((CSSTypedValue) rgbColor2.getComponent(3)).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertNull(rgbColor2.getComponent(4));

		// Clamps and sanity checks
		NumberValue num = new NumberValue();
		num.setFloatValue(CSSUnit.CSS_NUMBER, -1f);
		color.setAlpha(num);
		assertEquals(0f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);

		num.setFloatValue(CSSUnit.CSS_NUMBER, -1f);
		DOMException e = assertThrows(DOMException.class, () -> color.setRed(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		num.setFloatValue(CSSUnit.CSS_NUMBER, 2f);
		color.setAlpha(num);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		num.setFloatValue(CSSUnit.CSS_PERCENTAGE, -1f);
		color.setAlpha(num);
		assertEquals(0f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		num.setFloatValue(CSSUnit.CSS_PERCENTAGE, -1f);
		e = assertThrows(DOMException.class, () -> color.setRed(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		e = assertThrows(DOMException.class, () -> color.setGreen(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		e = assertThrows(DOMException.class, () -> color.setBlue(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		num.setFloatValue(CSSUnit.CSS_PERCENTAGE, 101f);
		color.setAlpha(num);
		assertEquals(100f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		num.setFloatValue(CSSUnit.CSS_PERCENTAGE, 101f);
		e = assertThrows(DOMException.class, () -> color.setRed(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		e = assertThrows(DOMException.class, () -> color.setGreen(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		e = assertThrows(DOMException.class, () -> color.setBlue(num));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		num.setFloatValue(CSSUnit.CSS_NUMBER, 256f);
		color.setRed(num); // allowed
		color.setGreen(num); // allowed
		color.setBlue(num); // allowed
	}

	@Test
	public void testTransparentColorIdentifier() throws CSSPropertyValueException {
		// Transparent identifier
		style.setCssText("color: transparent; ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.IDENT, cssColor.getPrimitiveType());
		RGBAColor color4 = ((CSSTypedValue) cssColor).toRGBColor();
		assertNotNull(color4);
		assertEquals(0, ((CSSTypedValue) color4.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0, ((CSSTypedValue) color4.getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
	}

	@Test
	public void testRGBColorCalc() throws CSSPropertyValueException {
		style.setCssText("color: rgb(25,63,calc(254*0.5)); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(Type.COLOR, cssColor.getPrimitiveType());
		assertEquals(CSSColorValue.ColorModel.RGB, ((ColorValue) cssColor).getColorModel());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor rgb = (RGBAColor) rgbColor.getColor();
		assertNotNull(rgb);
		assertEquals(25f, ((CSSTypedValue) rgb.getRed()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(63f, ((CSSTypedValue) rgb.getGreen()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(1f, ((CSSTypedValue) rgb.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER));
		assertEquals(Type.NUMERIC, rgb.getBlue().getPrimitiveType());
		assertEquals(127f, ((CSSTypedValue) rgb.getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertSame(rgb, rgbColor.toRGBColor());

		// To sRGB
		CSSColor srgb = rgb.toColorSpace(ColorSpace.srgb);
		assertNotNull(srgb);
		assertEquals(CSSColorValue.ColorModel.RGB, srgb.getColorModel());
		assertEquals("#193f7f", srgb.toString());

		// HSL
		CSSColor hsl = rgb.toColorSpace("hsl");
		assertNotNull(hsl);
		assertEquals(CSSColorValue.ColorModel.HSL, hsl.getColorModel());
		assertEquals("hsl(217.647, 67.11%, 29.8%)", hsl.toString());

		// HWB
		CSSColor hwb = rgb.toColorSpace("hwb");
		assertNotNull(hwb);
		assertEquals(CSSColorValue.ColorModel.HWB, hwb.getColorModel());
		assertEquals("hwb(217.647 9.8% 50.2%)", hwb.toString());

		// To A98 RGB
		CSSColor a98rgb = rgb.toColorSpace(ColorSpace.a98_rgb);
		assertNotNull(a98rgb);
		assertEquals(CSSColorValue.ColorModel.RGB, a98rgb.getColorModel());
		assertEquals("color(a98-rgb 0.173034 0.25542 0.487097)", a98rgb.toString());

		// To Display P3
		CSSColor display_p3 = rgb.toColorSpace(ColorSpace.display_p3);
		assertNotNull(display_p3);
		assertEquals(CSSColorValue.ColorModel.RGB, display_p3.getColorModel());
		assertEquals("color(display-p3 0.137306 0.243674 0.481197)", display_p3.toString());

		// To Prophoto RGB
		CSSColor prophoto = rgb.toColorSpace(ColorSpace.prophoto_rgb);
		assertNotNull(prophoto);
		assertEquals(CSSColorValue.ColorModel.RGB, prophoto.getColorModel());
		assertEquals("color(prophoto-rgb 0.192242 0.190063 0.397161)", prophoto.toString());

		// To REC 2020
		CSSColor rec2020 = rgb.toColorSpace(ColorSpace.rec2020);
		assertNotNull(rec2020);
		assertEquals(CSSColorValue.ColorModel.RGB, rec2020.getColorModel());
		assertEquals("color(rec2020 0.13315 0.183105 0.427058)", rec2020.toString());

		// To XYZ
		CSSColor xyz = rgb.toColorSpace(ColorSpace.xyz);
		assertNotNull(xyz);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyz.getColorModel());
		assertEquals("color(xyz 0.06009 0.05294 0.20784)", xyz.toString());

		// To XYZ D50
		CSSColor xyzD50 = rgb.toColorSpace(ColorSpace.xyz_d50);
		assertNotNull(xyzD50);
		assertEquals(CSSColorValue.ColorModel.XYZ, xyzD50.getColorModel());
		assertEquals("color(xyz-d50 0.05375 0.05066 0.15657)", xyzD50.toString());

		// To OK Lab
		CSSColor ok_lab = rgb.toColorSpace(ColorSpace.ok_lab);
		assertNotNull(ok_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, ok_lab.getColorModel());
		assertEquals("oklab(0.379166 -0.01984 -0.11519)", ok_lab.toString());

		// To OK LCh
		CSSColor ok_lch = rgb.toColorSpace(ColorSpace.ok_lch);
		assertNotNull(ok_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, ok_lch.getColorModel());
		assertEquals("oklch(0.379166 0.11689 260.225)", ok_lch.toString());

		// To CIE Lab
		CSSColor cie_lab = rgb.toColorSpace(ColorSpace.cie_lab);
		assertNotNull(cie_lab);
		assertEquals(CSSColorValue.ColorModel.LAB, cie_lab.getColorModel());
		assertEquals("lab(26.9234 5.99029 -40.91791)", cie_lab.toString());

		// To CIE LCh
		CSSColor cie_lch = rgb.toColorSpace(ColorSpace.cie_lch);
		assertNotNull(cie_lch);
		assertEquals(CSSColorValue.ColorModel.LCH, cie_lch.getColorModel());
		assertEquals("lch(26.9234 41.3541 278.329)", cie_lch.toString());
	}

	@Test
	public void testRGBAColorConversions() throws CSSPropertyValueException {
		style.setCssText("color: rgba(0%, 8%, 95%, 0.8); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(8f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(95f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(0.8f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(29.189f,
				((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(61.697f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-105.502f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);

		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(29.189f,
				((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(122.218f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(300.319f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);

		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(234.947f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(100f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(47.5f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(0.8f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(hslColor), 0.01f);
		assertEquals(0f, hslColor.deltaE2000(rgbColor), 0.01f);
	}

	@Test
	public void testRGBAColorConversions2() throws CSSPropertyValueException {
		style.setCssText("color: rgb(55%, 88%, 25%); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(55f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(88f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(25f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(81.74f, ((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-45.224f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(65.5257f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);

		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(81.74f, ((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(79.6168f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(124.6124f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);

		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(91.429f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(72.414f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(56.5f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(hslColor), 0.01f);
		assertEquals(0f, hslColor.deltaE2000(rgbColor), 0.01f);
	}

	@Test
	public void testRGBAColorConversions3() throws CSSPropertyValueException {
		style.setCssText("color: rgb(0%, 0%, 1%); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(0f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(0f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(1f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(1f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(0.0424f,
				((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(0.26457f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(-0.97039f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(1f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);

		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(0.04239f,
				((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER), 0.0001f);
		assertEquals(1.00581f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.0001f);
		assertEquals(285.2506f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(1f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);

		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(240f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(100f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(0.5f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(1f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(hslColor), 0.001f);
		assertEquals(0f, hslColor.deltaE2000(rgbColor), 0.001f);
	}

	@Test
	public void testRGBAColorConversions4() throws CSSPropertyValueException {
		style.setCssText("color: rgb(50%, 47%, 89%, calc(0.4*2)); ");
		CSSValue cssColor = style.getPropertyCSSValue("color");
		assertNotNull(cssColor);
		assertEquals(CssType.TYPED, cssColor.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, cssColor.getPrimitiveType());
		ColorValue rgbColor = (ColorValue) cssColor;
		RGBAColor color = (RGBAColor) rgbColor.getColor();
		assertNotNull(color);
		assertEquals(50f, ((CSSTypedValue) color.getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(47f, ((CSSTypedValue) color.getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(89f, ((CSSTypedValue) color.getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5f);
		assertEquals(Type.NUMERIC, color.getAlpha().getPrimitiveType());
		assertEquals(0.8f, ((CSSTypedValue) color.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		LABColorValue labColor = rgbColor.toLABColorValue();
		assertNotNull(labColor);
		LABColor lab = labColor.getColor();
		assertNotNull(lab);
		assertEquals(54.913f,
				((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(23.595f, ((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(-54.491f, ((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(Type.NUMERIC, lab.getAlpha().getPrimitiveType());
		assertEquals(0.8f, ((CSSTypedValue) lab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(labColor), 0.001f);
		assertEquals(0f, labColor.deltaE2000(rgbColor), 0.001f);

		LCHColorValue lchColor = labColor.toLCHColorValue();
		assertNotNull(lchColor);
		LCHColor lch = lchColor.getColor();
		assertNotNull(lch);
		assertEquals(54.913f,
				((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(59.380f, ((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(293.413f, ((CSSTypedValue) lch.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(Type.NUMERIC, lch.getAlpha().getPrimitiveType());
		assertEquals(0.8f, ((CSSTypedValue) lch.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(lchColor), 0.001f);
		assertEquals(0f, lchColor.deltaE2000(rgbColor), 0.001f);

		HSLColorValue hslColor = rgbColor.toHSLColorValue();
		assertNotNull(hslColor);
		HSLColor hsl = hslColor.getColor();
		assertNotNull(hsl);
		assertEquals(244.286f, ((CSSTypedValue) hsl.getHue()).getFloatValue(CSSUnit.CSS_DEG),
				0.001f);
		assertEquals(65.625f,
				((CSSTypedValue) hsl.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				0.001f);
		assertEquals(68f,
				((CSSTypedValue) hsl.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.001f);
		assertEquals(Type.NUMERIC, hsl.getAlpha().getPrimitiveType());
		assertEquals(0.8f, ((CSSTypedValue) hsl.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		// Delta 0 to converted value
		assertEquals(0f, rgbColor.deltaE2000(hslColor), 0.001f);
		assertEquals(0f, hslColor.deltaE2000(rgbColor), 0.001f);
	}

	@Test
	public void testRGBATransparentColor() {
		style.setCssText("color: rgba(0,0,0,0); ");
		assertEquals("rgba(0, 0, 0, 0)", style.getPropertyValue("color"));
		assertEquals("color: rgba(0, 0, 0, 0); ", style.getCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("rgba(0, 0, 0, 0)", rgb.toString());
	}

	@Test
	public void testRGBTransparentColor() {
		style.setCssText("color: rgb(0 0 0/0); ");
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("color"));
		assertEquals("color: rgb(0 0 0 / 0); ", style.getCssText());
		ColorValue val = (ColorValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		RGBAColor rgb = val.toRGBColor();
		assertNotNull(rgb);
		assertEquals("rgb(0 0 0 / 0)", rgb.toString());
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
