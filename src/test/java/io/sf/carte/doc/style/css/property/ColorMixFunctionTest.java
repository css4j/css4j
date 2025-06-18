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
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.util.BufferSimpleWriter;

public class ColorMixFunctionTest {

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
	public void testColorMix() throws IOException {
		style.setCssText("color: color-mix(in display-p3, #0200fa 10%, white)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.display_p3, color.getCSSColorSpace());

		assertSame(color.getColorValue1(), color.getComponent(0));
		assertSame(color.getPercentage1(), color.getComponent(1));
		assertSame(color.getColorValue2(), color.getComponent(2));
		assertSame(color.getPercentage2(), color.getComponent(3));
		assertNull(color.getComponent(4));
		assertEquals(4, color.getComponentCount());

		ColorValue colorValue1 = (ColorValue) color.getColorValue1();
		assertNotNull(colorValue1);
		assertEquals(Type.COLOR, colorValue1.getPrimitiveType());
		BaseColor color1 = (BaseColor) colorValue1.getColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color1.getColorModel());
		assertEquals(ColorSpace.srgb, color1.getColorSpace());
		assertEquals("#0200fa", color1.toString());

		PrimitiveValue value2 = color.getColorValue2();
		assertNotNull(value2);
		assertEquals(Type.IDENT, value2.getPrimitiveType());
		CSSColor color2 = ((CSSTypedValue) value2).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color2.getColorModel());
		assertEquals(ColorSpace.srgb, color2.getColorSpace());
		assertEquals("#fff", color2.toString());
		CSSColorValue colorValue2 = color2.packInValue();

		CSSColor color2p3 = color2.toColorSpace(ColorSpace.display_p3);
		assertNotNull(color2p3);
		assertEquals("color(display-p3 0.999944 1 1)", color2p3.toString());

		PrimitiveValue pcntValue1 = color.getPercentage1();
		assertNotNull(pcntValue1);
		assertEquals(CSSUnit.CSS_PERCENTAGE, pcntValue1.getUnitType());
		assertEquals(10f, ((TypedValue) pcntValue1).getFloatValue(CSSUnit.CSS_PERCENTAGE));

		assertNull(color.getPercentage2());

		// Serialization
		assertEquals("color-mix(in display-p3, #0200fa 10%, white)", color.getCssText());
		assertEquals("color-mix(in display-p3,#0200fa 10%,white)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(45);
		color.writeCssText(wri);
		assertEquals("color-mix(in display-p3, #0200fa 10%, white)", wri.toString());

		// Set wrong values
		DOMException e = assertThrows(DOMException.class,
				() -> color.setComponent(0, NumberValue.createCSSNumberValue(CSSUnit.CSS_EM, 1f)));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.display_p3, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals("color(display-p3 0.900595 0.900026 0.994083)", mixed.toString());

		// DeltaE2000
		assertEquals(60.1f, color.deltaE2000(colorValue1), 0.1f);
		assertEquals(11.65f, color.deltaE2000(colorValue2), 0.1f);
		assertEquals(11.65f, colorValue2.deltaE2000(color), 0.1f);

		// To Lab
		LABColorValue cielabColor = color.toLABColorValue();
		assertNotNull(cielabColor);
		LABColor cielab = cielabColor.getColor();
		assertEquals("lab(91.7294 3.743 -12.697)", cielabColor.getCssText());
		assertEquals(1f, ((CSSTypedValue) cielab.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);

		// Small Delta to converted value
		assertEquals(0f, cielabColor.deltaE2000(color), 0.01f);
		assertEquals(0f, color.deltaE2000(cielabColor), 0.01f);

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

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixShorterHueNoPercents() throws IOException {
		style.setCssText("color: color-mix(in oklch shorter hue, #0200fa, hwb(135.6 40% 3%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, #0200fa, hwb(135.6 40% 3%))", color.getCssText());
		assertEquals("color-mix(in oklch,#0200fa,hwb(135.6 40% 3%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, #0200fa, hwb(135.6 40% 3%))", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.659293 0.2509 206.735)", mixed.toString());

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

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixHueNone() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch, lab(43.71 none none) 42%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, lab(43.71 none none) 42%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in oklch,lab(43.71 none none) 42%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, lab(43.71 none none) 42%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.676761 0.06737 230.136)", mixed.toString());

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

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixHueNoneOKLCh() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch, oklch(0.5147 none none) 35%, hwb(266.5 30% 40%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, oklch(0.5147 none none) 35%, hwb(266.5 30% 40%))",
				color.getCssText());
		assertEquals("color-mix(in oklch,oklch(.5147 none none) 35%,hwb(266.5 30% 40%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, oklch(0.5147 none none) 35%, hwb(266.5 30% 40%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.498551 0.12243 302.495)", mixed.toString());
	}

	@Test
	public void testColorMixHueNoneOKLCh2() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch, hwb(266.5 30% 40%) 55%, oklch(0.5147 none none))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, hwb(266.5 30% 40%) 55%, oklch(0.5147 none none))",
				color.getCssText());
		assertEquals("color-mix(in oklch,hwb(266.5 30% 40%) 55%,oklch(.5147 none none))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, hwb(266.5 30% 40%) 55%, oklch(0.5147 none none))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.501035 0.12243 302.495)", mixed.toString());
	}

	@Test
	public void testColorMixHueNoneOKLChNone() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch, oklch(0.4652 none none) 55%, oklch(0.5147 none none))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, oklch(0.4652 none none) 55%, oklch(0.5147 none none))",
				color.getCssText());
		assertEquals("color-mix(in oklch,oklch(.4652 none none) 55%,oklch(.5147 none none))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, oklch(0.4652 none none) 55%, oklch(0.5147 none none))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.487475 0 0)", mixed.toString());
	}

	@Test
	public void testColorMixShorterHue() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch shorter hue, #0200fa 40%, hwb(135.6 40% 3%) 60%)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, #0200fa 40%, hwb(135.6 40% 3%) 60%)", color.getCssText());
		assertEquals("color-mix(in oklch,#0200fa 40%,hwb(135.6 40% 3%) 60%)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, #0200fa 40%, hwb(135.6 40% 3%) 60%)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.702039 0.23939 195.243)", mixed.toString());

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

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());

		// Set percentage 1
		color.setPercentage1(NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 50f));
		assertEquals("color-mix(in oklch, #0200fa 50%, hwb(135.6 40% 3%) 60%)", color.getCssText());

		mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals("oklch(0.678723 0.24567 201.511)", mixed.toString());

		// Set percentage 2
		color.setPercentage2(NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 80f));
		assertEquals("color-mix(in oklch, #0200fa 50%, hwb(135.6 40% 3%) 80%)", color.getCssText());

		mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals("oklch(0.708615 0.23762 193.475)", mixed.toString());
	}

	@Test
	public void testColorMixLongerHue() throws IOException {
		style.setCssText("color: color-mix(in oklch longer hue, 40% #0200fa, hwb(135.6 40% 3%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch longer hue, #0200fa 40%, hwb(135.6 40% 3%))",
				color.getCssText());
		assertEquals("color-mix(in oklch longer hue,#0200fa 40%,hwb(135.6 40% 3%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch longer hue, #0200fa 40%, hwb(135.6 40% 3%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.702039 0.23939 51.243)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());

		// Set percentage 1 as calc()
		ValueFactory factory = new ValueFactory();
		PrimitiveValue calc1 = (PrimitiveValue) factory.parseProperty("calc(55%)");
		color.setPercentage1(calc1);
		assertEquals("color-mix(in oklch longer hue, #0200fa 55%, hwb(135.6 40% 3%))",
				color.getCssText());

		mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals("oklch(0.63792 0.25665 14.481)", mixed.toString());

		// Set percentage 2 as calc()
		PrimitiveValue calc2 = (PrimitiveValue) factory.parseProperty("calc(75%)");
		color.setPercentage2(calc2);
		assertEquals("color-mix(in oklch longer hue, #0200fa 55%, hwb(135.6 40% 3%) 75%)",
				color.getCssText());

		mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals("oklch(0.692174 0.24205 45.587)", mixed.toString());

		// Set color value 1
		PrimitiveValue newColor1 = (PrimitiveValue) factory.parseProperty("hsl(205.3 90% 69%)");
		color.setColorValue1(newColor1);
		assertEquals(
				"color-mix(in oklch longer hue, hsl(205.3 90% 69%) 55%, hwb(135.6 40% 3%) 75%)",
				color.getCssText());

		mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals("oklch(0.826951 0.16164 36.267)", mixed.toString());

		// Set color value 2
		PrimitiveValue newColor2 = (PrimitiveValue) factory
				.parseProperty("oklch(0.831 0.1602 167.34)");
		color.setColorValue2(newColor2);
		assertEquals(
				"color-mix(in oklch longer hue, hsl(205.3 90% 69%) 55%, oklch(0.831 0.1602 167.34) 75%)",
				color.getCssText());

		mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals("oklch(0.802708 0.1425 46.69)", mixed.toString());
	}

	@Test
	public void testColorMixIncreasingHue() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch increasing hue, #0200fa, 60% hwb(135.6 40% 3%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch increasing hue, #0200fa, hwb(135.6 40% 3%) 60%)",
				color.getCssText());
		assertEquals("color-mix(in oklch increasing hue,#0200fa,hwb(135.6 40% 3%) 60%)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch increasing hue, #0200fa, hwb(135.6 40% 3%) 60%)",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.702039 0.23939 51.243)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixDecreasingHue() throws IOException {
		style.setCssText(
				"color: color-mix(in oklch decreasing hue, #0200fa, hwb(325.6 40% 3%) 60%)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch decreasing hue, #0200fa, hwb(325.6 40% 3%) 60%)",
				color.getCssText());
		assertEquals("color-mix(in oklch decreasing hue,#0200fa,hwb(325.6 40% 3%) 60%)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch decreasing hue, #0200fa, hwb(325.6 40% 3%) 60%)",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.607824 0.24195 98.316)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixOKLabAttr() throws IOException {
		style.setCssText(
				"color: color-mix(in oklab, oklch(0.831 0.1602 167.34) attr(data-pcnt1 type(<percentage>)), hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		// Serialization
		assertEquals(
				"color-mix(in oklab, oklch(0.831 0.1602 167.34) attr(data-pcnt1 type(<percentage>)), hwb(186.2 29% 12%))",
				value.getCssText());
		assertEquals(
				"color-mix(in oklab,oklch(.831 .1602 167.34) attr(data-pcnt1 type(<percentage>)),hwb(186.2 29% 12%))",
				value.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		value.writeCssText(wri);
		assertEquals(
				"color-mix(in oklab, oklch(0.831 0.1602 167.34) attr(data-pcnt1 type(<percentage>)), hwb(186.2 29% 12%))",
				wri.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixHWB() throws IOException {
		style.setCssText(
				"color: color-mix(in HWB, hwb(60.8 26% 24%) 41%, hwb(90.3 40% 31%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.HWB, color.getColorModel());
		assertEquals("hwb", color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in hwb, hwb(60.8 26% 24%) 41%, hwb(90.3 40% 31%))",
				color.getCssText());
		assertEquals("color-mix(in hwb,hwb(60.8 26% 24%) 41%,hwb(90.3 40% 31%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in hwb, hwb(60.8 26% 24%) 41%, hwb(90.3 40% 31%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.HWB, mixed.getColorModel());
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals("hwb(78.205 34.26% 28.13%)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixCIELab() throws IOException {
		style.setCssText(
				"color: color-mix(in lab, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		assertEquals(ColorSpace.cie_lab, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in lab, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in lab,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in lab, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.LAB, mixed.getColorModel());
		assertEquals(ColorSpace.cie_lab, mixed.getColorSpace());
		assertEquals("lab(79.4828 -43.49156 -4.9166)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixOKLab() throws IOException {
		style.setCssText(
				"color: color-mix(in oklab, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LAB, color.getColorModel());
		assertEquals(ColorSpace.ok_lab, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklab, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in oklab,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklab, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.LAB, mixed.getColorModel());
		assertEquals(ColorSpace.ok_lab, mixed.getColorSpace());
		assertEquals("oklab(0.810328 -0.12717 -0.01317)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixXYZD50() throws IOException {
		style.setCssText(
				"color: color-mix(in xyz-d50, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.XYZ, color.getColorModel());
		assertEquals(ColorSpace.xyz_d50, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in xyz-d50, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in xyz-d50,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in xyz-d50, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.XYZ, mixed.getColorModel());
		assertEquals(ColorSpace.xyz_d50, mixed.getColorSpace());
		assertEquals("color(xyz-d50 0.38466 0.55903 0.50939)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixA98RGB() throws IOException {
		style.setCssText(
				"color: color-mix(in a98-rgb, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.a98_rgb, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in a98-rgb, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in a98-rgb,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in a98-rgb, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.a98_rgb, mixed.getColorSpace());
		assertEquals("color(a98-rgb 0.522163 0.855247 0.79893)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixProphotoRGB() throws IOException {
		style.setCssText(
				"color: color-mix(in prophoto-rgb, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.prophoto_rgb, color.getCSSColorSpace());

		// Serialization
		assertEquals(
				"color-mix(in prophoto-rgb, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in prophoto-rgb,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals(
				"color-mix(in prophoto-rgb, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.prophoto_rgb, mixed.getColorSpace());
		assertEquals("color(prophoto-rgb 0.557021 0.782109 0.761185)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixRec2020() throws IOException {
		style.setCssText(
				"color: color-mix(in rec2020, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.rec2020, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in rec2020, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in rec2020,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in rec2020, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.rec2020, mixed.getColorSpace());
		assertEquals("color(rec2020 0.533724 0.814218 0.778928)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixLinearRGB() throws IOException {
		style.setCssText(
				"color: color-mix(in srgb-linear, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb_linear, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb-linear, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				color.getCssText());
		assertEquals("color-mix(in srgb-linear,oklch(.831 .1602 167.34) 44%,hwb(186.2 29% 12%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb-linear, oklch(0.831 0.1602 167.34) 44%, hwb(186.2 29% 12%))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.srgb_linear, mixed.getColorSpace());
		assertEquals("color(srgb-linear 0.051673 0.711718 0.615333)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixPcntCalc() throws IOException {
		style.setCssText("color: color-mix(in srgb-linear, #0200fa, hwb(325.6 40% 3%) calc(2*30%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb_linear, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb-linear, #0200fa, hwb(325.6 40% 3%) 60%)",
				color.getCssText());
		assertEquals("color-mix(in srgb-linear,#0200fa,hwb(325.6 40% 3%) 60%)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb-linear, #0200fa, hwb(325.6 40% 3%) 60%)",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.srgb_linear, mixed.getColorSpace());
		assertEquals("color(srgb-linear 0.560107 0.079721 0.674653)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixPcntCalcError() throws IOException {
		style.setCssText(
				"color: color-mix(in srgb-linear, #0200fa, hwb(325.6 40% 3%) calc(2*3mm))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNull(value);

		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixPcntMax() throws IOException {
		style.setCssText(
				"color: color-mix(in srgb-linear, #0200fa, color(display-p3 0.0064507 0.0002603 0.9407362) max(10%,60%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb_linear, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb-linear, #0200fa, color(display-p3 0.0064507 2.603E-4 0.9407362) 60%)",
				color.getCssText());
		assertEquals("color-mix(in srgb-linear,#0200fa,color(display-p3 .0064507 2.603E-4 .9407362) 60%)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb-linear, #0200fa, color(display-p3 0.0064507 2.603E-4 0.9407362) 60%)",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.srgb_linear, mixed.getColorSpace());
		assertEquals("color(srgb-linear 0.000607 0 0.955849)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixPcntMaxError() throws IOException {
		style.setCssText(
				"color: color-mix(in srgb-linear, #0200fa, hwb(325.6 40% 3%) max(10%,60px))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNull(value);

		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixOutsideGamut() throws IOException {
		style.setCssText(
				"color:color-mix(in srgb-linear,#0200fa,color(display-p3 0.0064507 0.0002603 0.9407362) 60%)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb_linear, color.getCSSColorSpace());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals(ColorSpace.srgb_linear, mixed.getColorSpace());
		assertEquals("color(srgb-linear 0.000607 0 0.955849)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixSpecLchExample() throws IOException {
		style.setCssText("color: color-mix(in lch, peru 40%, palegoldenrod)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.cie_lch, color.getCSSColorSpace());

		assertSame(color.getColorValue1(), color.getComponent(0));
		assertSame(color.getPercentage1(), color.getComponent(1));
		assertSame(color.getColorValue2(), color.getComponent(2));
		assertSame(color.getPercentage2(), color.getComponent(3));
		assertNull(color.getComponent(4));
		assertEquals(4, color.getComponentCount());

		PrimitiveValue colorValue1 = color.getColorValue1();
		assertNotNull(colorValue1);
		assertEquals(Type.IDENT, colorValue1.getPrimitiveType());
		CSSColor color1 = ((CSSTypedValue) colorValue1).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color1.getColorModel());
		assertEquals(ColorSpace.srgb, color1.getColorSpace());
		assertEquals("lch(62.2517 54.0033 63.68)",
				color1.toColorSpace(ColorSpace.cie_lch).toString());

		PrimitiveValue colorValue2 = color.getColorValue2();
		assertNotNull(colorValue2);
		assertEquals(Type.IDENT, colorValue2.getPrimitiveType());
		CSSColor color2 = ((CSSTypedValue) colorValue2).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color2.getColorModel());
		assertEquals(ColorSpace.srgb, color2.getColorSpace());
		assertEquals("lch(91.3736 31.406 98.834)",
				color2.toColorSpace(ColorSpace.cie_lch).toString());

		PrimitiveValue pcntValue1 = color.getPercentage1();
		assertNotNull(pcntValue1);
		assertEquals(CSSUnit.CSS_PERCENTAGE, pcntValue1.getUnitType());
		assertEquals(40f, ((TypedValue) pcntValue1).getFloatValue(CSSUnit.CSS_PERCENTAGE));

		assertNull(color.getPercentage2());

		// Serialization
		assertEquals("color-mix(in lch, peru 40%, palegoldenrod)", color.getCssText());
		assertEquals("color-mix(in lch,peru 40%,palegoldenrod)", color.getMinifiedCssText("color"));
		assertEquals("#cd853f", color1.toString());
		assertEquals("#eee8aa", color2.toString());

		BufferSimpleWriter wri = new BufferSimpleWriter(45);
		color.writeCssText(wri);
		assertEquals("color-mix(in lch, peru 40%, palegoldenrod)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.cie_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("lch(79.7248 40.4449 84.773)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixSpecLchExample2() throws IOException {
		style.setCssText("color: color-mix(in lch, teal 65%, olive)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.cie_lch, color.getCSSColorSpace());

		assertSame(color.getColorValue1(), color.getComponent(0));
		assertSame(color.getPercentage1(), color.getComponent(1));
		assertSame(color.getColorValue2(), color.getComponent(2));
		assertSame(color.getPercentage2(), color.getComponent(3));
		assertNull(color.getComponent(4));
		assertEquals(4, color.getComponentCount());

		PrimitiveValue colorValue1 = color.getColorValue1();
		assertNotNull(colorValue1);
		assertEquals(Type.IDENT, colorValue1.getPrimitiveType());
		CSSColor color1 = ((CSSTypedValue) colorValue1).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color1.getColorModel());
		assertEquals(ColorSpace.srgb, color1.getColorSpace());
		assertEquals("#008080", color1.toString());
		assertEquals("lch(47.9864 31.6894 196.466)",
				color1.toColorSpace(ColorSpace.cie_lch).toString());

		PrimitiveValue colorValue2 = color.getColorValue2();
		assertNotNull(colorValue2);
		assertEquals(Type.IDENT, colorValue2.getPrimitiveType());
		CSSColor color2 = ((CSSTypedValue) colorValue2).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color2.getColorModel());
		assertEquals(ColorSpace.srgb, color2.getColorSpace());
		assertEquals("#808000", color2.toString());
		assertEquals("lch(52.1491 56.8125 99.58)",
				color2.toColorSpace(ColorSpace.cie_lch).toString());

		PrimitiveValue pcntValue1 = color.getPercentage1();
		assertNotNull(pcntValue1);
		assertEquals(CSSUnit.CSS_PERCENTAGE, pcntValue1.getUnitType());
		assertEquals(65f, ((TypedValue) pcntValue1).getFloatValue(CSSUnit.CSS_PERCENTAGE));

		assertNull(color.getPercentage2());

		// Serialization
		assertEquals("color-mix(in lch, teal 65%, olive)", color.getCssText());
		assertEquals("color-mix(in lch,teal 65%,olive)", color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(45);
		color.writeCssText(wri);
		assertEquals("color-mix(in lch, teal 65%, olive)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.cie_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("lch(49.4433 40.4824 162.556)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixSpecLchExample3() throws IOException {
		style.setCssText("color: color-mix(in lch, white, black)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.cie_lch, color.getCSSColorSpace());

		assertSame(color.getColorValue1(), color.getComponent(0));
		assertSame(color.getPercentage1(), color.getComponent(1));
		assertSame(color.getColorValue2(), color.getComponent(2));
		assertSame(color.getPercentage2(), color.getComponent(3));
		assertNull(color.getComponent(4));
		assertEquals(4, color.getComponentCount());

		PrimitiveValue colorValue1 = color.getColorValue1();
		assertNotNull(colorValue1);
		assertEquals(Type.IDENT, colorValue1.getPrimitiveType());
		CSSColor color1 = ((CSSTypedValue) colorValue1).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color1.getColorModel());
		assertEquals(ColorSpace.srgb, color1.getColorSpace());
		assertEquals("lch(99.9998 0.0146 254.931)",
				color1.toColorSpace(ColorSpace.cie_lch).toString());

		PrimitiveValue colorValue2 = color.getColorValue2();
		assertNotNull(colorValue2);
		assertEquals(Type.IDENT, colorValue2.getPrimitiveType());
		CSSColor color2 = ((CSSTypedValue) colorValue2).toRGBColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color2.getColorModel());
		assertEquals(ColorSpace.srgb, color2.getColorSpace());
		assertEquals("lch(0 0 0)", color2.toColorSpace(ColorSpace.cie_lch).toString());

		PrimitiveValue pcntValue1 = color.getPercentage1();
		assertNull(pcntValue1);

		assertNull(color.getPercentage2());

		// Serialization
		assertEquals("color-mix(in lch, white, black)", color.getCssText());
		assertEquals("color-mix(in lch,white,black)", color.getMinifiedCssText("color"));
		assertEquals("#fff", color1.toString());
		assertEquals("#000", color2.toString());

		BufferSimpleWriter wri = new BufferSimpleWriter(45);
		color.writeCssText(wri);
		assertEquals("color-mix(in lch, white, black)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.cie_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("lch(49.9999 0.0073 307.465)", mixed.toString());

		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testColorMixSpecXyzExample() throws IOException {
		style.setCssText("color: color-mix(in xyz, white, black)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.XYZ, color.getColorModel());
		assertEquals(ColorSpace.xyz, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in xyz, white, black)", color.getCssText());
		assertEquals("color-mix(in xyz,white,black)", color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(45);
		color.writeCssText(wri);
		assertEquals("color-mix(in xyz, white, black)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.xyz, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.XYZ, mixed.getColorModel());
		assertEquals("color(xyz 0.47523 0.5 0.54453)", mixed.toString());
		assertEquals("lch(76.0691 0.0116 254.918)",
				mixed.toColorSpace(ColorSpace.cie_lch).toString());
	}

	@Test
	public void testColorMixSpecSrgbExample() throws IOException {
		style.setCssText("color: color-mix(in srgb, white, black)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb, white, black)", color.getCssText());
		assertEquals("color-mix(in srgb,white,black)", color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(50);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb, white, black)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals("rgb(50%, 50%, 50%)", mixed.toString());
		assertEquals("lch(53.3889 0.0088 254.931)",
				mixed.toColorSpace(ColorSpace.cie_lch).toString());
	}

	@Test
	public void testColorMixSpecXyzExampleRgb() throws IOException {
		style.setCssText(
				"color:color-mix(in xyz-d65, rgb(82.02% 30.21% 35.02%) 75.23%, rgb(5.64% 55.94% 85.31%))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.XYZ, color.getColorModel());
		assertEquals(ColorSpace.xyz, color.getCSSColorSpace());

		// Serialization
		assertEquals(
				"color-mix(in xyz, rgb(82.02% 30.21% 35.02%) 75.23%, rgb(5.64% 55.94% 85.31%))",
				color.getCssText());
		assertEquals("color-mix(in xyz,rgb(82.02% 30.21% 35.02%) 75.23%,rgb(5.64% 55.94% 85.31%))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals(
				"color-mix(in xyz, rgb(82.02% 30.21% 35.02%) 75.23%, rgb(5.64% 55.94% 85.31%))",
				wri.toString());

		PrimitiveValue colorValue1 = color.getColorValue1();
		assertNotNull(colorValue1);
		assertEquals(Type.COLOR, colorValue1.getPrimitiveType());
		BaseColor color1 = (BaseColor) ((ColorValue) colorValue1).getColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color1.getColorModel());
		assertEquals(ColorSpace.srgb, color1.getColorSpace());
		assertEquals("rgb(82.02% 30.21% 35.02%)", color1.toString());

		CSSColor color1xyz = color1.toColorSpace(ColorSpace.xyz);
		assertNotNull(color1xyz);
		assertEquals("color(xyz 0.30809 0.19619 0.11682)", color1xyz.toString());

		PrimitiveValue colorValue2 = color.getColorValue2();
		assertNotNull(colorValue2);
		assertEquals(Type.COLOR, colorValue2.getPrimitiveType());
		BaseColor color2 = (BaseColor) ((ColorValue) colorValue2).getColor();
		assertEquals(CSSColorValue.ColorModel.RGB, color2.getColorModel());
		assertEquals(ColorSpace.srgb, color2.getColorSpace());
		assertEquals("rgb(5.64% 55.94% 85.31%)", color2.toString());

		CSSColor color2xyz = color2.toColorSpace(ColorSpace.xyz);
		assertNotNull(color2xyz);
		assertEquals("color(xyz 0.2255 0.24672 0.69591)", color2xyz.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.xyz, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.XYZ, mixed.getColorModel());
		assertEquals("color(xyz 0.28763 0.2087 0.26026)", mixed.toString());
		assertEquals("rgb(72.31%, 38.64%, 53.56%)", mixed.toColorSpace(ColorSpace.srgb).toString());
	}

	@Test
	public void testColorMixSpecSrgbExampleWhiteBlue() throws IOException {
		style.setCssText("color:color-mix(in srgb, white, blue)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb, white, blue)", color.getCssText());
		assertEquals("color-mix(in srgb,white,blue)", color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(60);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb, white, blue)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals("rgb(50%, 50%, 100%)", mixed.toString());
	}

	@Test
	public void testColorMixSpecOKExampleWhiteBlue() throws IOException {
		style.setCssText("color:color-mix(in oklch, white, blue)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.LCH, color.getColorModel());
		assertEquals(ColorSpace.ok_lch, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in oklch, white, blue)", color.getCssText());
		assertEquals("color-mix(in oklch,white,blue)", color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(60);
		color.writeCssText(wri);
		assertEquals("color-mix(in oklch, white, blue)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.ok_lch, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.LCH, mixed.getColorModel());
		assertEquals("oklch(0.726006 0.15672 262.02)", mixed.toString());
	}

	@Test
	public void testColorMixSpecHslExampleMixed() throws IOException {
		style.setCssText("color: color-mix(in hsl, color(display-p3 0 1 0) 80%, yellow)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.HSL, color.getColorModel());
		assertEquals("hsl", color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in hsl, color(display-p3 0 1 0) 80%, yellow)", color.getCssText());
		assertEquals("color-mix(in hsl,color(display-p3 0 1 0) 80%,yellow)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in hsl, color(display-p3 0 1 0) 80%, yellow)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.HSL, mixed.getColorModel());
		assertEquals("hsl(108, 100%, 49.95%)", mixed.toString());
		assertEquals("color(display-p3 0.489303 0.984755 0.29913)",
				mixed.toColorSpace(ColorSpace.display_p3).toString());
	}

	@Test
	public void testColorMixSpecHwbExampleMixed() throws IOException {
		style.setCssText("color: color-mix(in hwb, color(display-p3 0 1 0) 80%, yellow)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.HWB, color.getColorModel());
		assertEquals("hwb", color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in hwb, color(display-p3 0 1 0) 80%, yellow)", color.getCssText());
		assertEquals("color-mix(in hwb,color(display-p3 0 1 0) 80%,yellow)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in hwb, color(display-p3 0 1 0) 80%, yellow)", wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.HWB, mixed.getColorModel());
		assertEquals("hwb(108 0% 0.1%)", mixed.toString());
		assertEquals("color(display-p3 0.489306 0.98476 0.299132)",
				mixed.toColorSpace(ColorSpace.display_p3).toString());
	}

	@Test
	public void testColorMixSpecSrgbExampleAlpha() throws IOException {
		style.setCssText(
				"color: color-mix(in srgb, rgb(100% 0% 0% / 0.7) 25%, rgb(0% 100% 0% / 0.2))");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb, rgb(100% 0% 0% / 0.7) 25%, rgb(0% 100% 0% / 0.2))",
				color.getCssText());
		assertEquals("color-mix(in srgb,rgb(100% 0% 0%/.7) 25%,rgb(0% 100% 0%/.2))",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb, rgb(100% 0% 0% / 0.7) 25%, rgb(0% 100% 0% / 0.2))",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals("rgba(25%, 75%, 0%, 0.325)", mixed.toString());
	}

	@Test
	public void testColorMixSpecSrgbExampleAlphaTwoPercentages() throws IOException {
		style.setCssText(
				"color: color-mix(in srgb, rgb(100% 0% 0% / 0.7) 20%, rgb(0% 100% 0% / 0.2) 60%)");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.COLOR_MIX, value.getPrimitiveType());
		ColorMixFunction color = (ColorMixFunction) value;
		assertEquals(CSSColorValue.ColorModel.RGB, color.getColorModel());
		assertEquals(ColorSpace.srgb, color.getCSSColorSpace());

		// Serialization
		assertEquals("color-mix(in srgb, rgb(100% 0% 0% / 0.7) 20%, rgb(0% 100% 0% / 0.2) 60%)",
				color.getCssText());
		assertEquals("color-mix(in srgb,rgb(100% 0% 0%/.7) 20%,rgb(0% 100% 0%/.2) 60%)",
				color.getMinifiedCssText("color"));

		BufferSimpleWriter wri = new BufferSimpleWriter(100);
		color.writeCssText(wri);
		assertEquals("color-mix(in srgb, rgb(100% 0% 0% / 0.7) 20%, rgb(0% 100% 0% / 0.2) 60%)",
				wri.toString());

		// Mix the colors
		BaseColor mixed = color.getColor();
		assertNotNull(mixed);
		assertEquals(ColorSpace.srgb, mixed.getColorSpace());
		assertEquals(CSSColorValue.ColorModel.RGB, mixed.getColorModel());
		assertEquals("rgba(25%, 75%, 0%, 0.325)", mixed.toString());
	}

	@Test
	public void testEquals() {
		ColorMixFunction value = new ColorMixFunction();
		value.setCssText("color-mix(in display-p3, #0200fa 10%, white)");
		ColorMixFunction other = new ColorMixFunction();
		other.setCssText("color-mix(in display-p3, #0200fa 10%, white)");
		assertTrue(value.equals(other));
		assertTrue(other.equals(value));
		assertEquals(value.hashCode(), other.hashCode());
		assertFalse(value.equals(null));
		other.setCssText("color-mix(in sRGB, #0200fa 10%, white)");
		assertFalse(value.equals(other));
		assertFalse(other.equals(value));
		assertFalse(value.hashCode() == other.hashCode());
		other.setCssText("color-mix(in display-p3, #0200fa 20%, white)");
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
	public void testClone() {
		style.setCssText("color: color-mix(in display-p3, #0200fa 10%, white)");
		ColorMixFunction value = (ColorMixFunction) style.getPropertyCSSValue("color");
		ColorMixFunction clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.getCSSColorSpace().equals(clon.getCSSColorSpace()));
		assertTrue(value.getColorModel().equals(clon.getColorModel()));
		assertTrue(value.getColorValue1().equals(clon.getColorValue1()));
		assertTrue(value.getColorValue2().equals(clon.getColorValue2()));
		assertEquals(value.getPercentage1(), clon.getPercentage1());
		assertEquals(value.getPercentage2(), clon.getPercentage2());
		assertEquals(value.getColor(), clon.getColor());
		assertTrue(value.toRGBColor().equals(clon.toRGBColor()));
		assertEquals(value.toRGBColor().hashCode(), clon.toRGBColor().hashCode());
	}

}
