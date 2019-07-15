/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSGradientValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class GradientValueTest {

	BaseCSSStyleDeclaration style;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createCSSStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testGetCssTextLinear() {
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("linear-gradient(to top right, red, white, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to top right, red, white, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(to top right,red,white,blue)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(yellow, blue 20%, #0f0); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("linear-gradient(yellow, blue 20%, #0f0)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(yellow, blue 20%, #0f0); ", style.getCssText());
		assertEquals("background-image:linear-gradient(yellow,blue 20%,#0f0)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(135deg, yellow, blue); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("linear-gradient(135deg, yellow, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(135deg, yellow, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(135deg,yellow,blue)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(to bottom, yellow 0%, blue 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("linear-gradient(to bottom, yellow 0%, blue 100%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to bottom, yellow 0%, blue 100%); ", style.getCssText());
		assertEquals("background-image:linear-gradient(to bottom,yellow 0%,blue 100%)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(transparent, #fff); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("linear-gradient(transparent, #fff)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(transparent, #fff); ", style.getCssText());
		assertEquals("background-image:linear-gradient(transparent,#fff)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
	}

	@Test
	public void testGetCssTextLinearBad() {
		style.setCssText("background-image: linear-gradient(45deg to left top, yellow 0%, blue 100%); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		//
		style.setCssText("background-image: linear-gradient(to bottom, yellow 0% blue 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
	}

	@Test
	public void testGetCssTextLinearNonStandard() {
		style.setCssText(
				"background-image: -moz-linear-gradient(center top, rgb(51, 153, 51) 0%, rgb(51, 119, 51) 100%); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("-moz-linear-gradient(center top, #393 0%, #373 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: -moz-linear-gradient(center top, #393 0%, #373 100%); ", style.getCssText());
		assertEquals("background-image:-moz-linear-gradient(center top,#393 0%,#373 100%)", style.getMinifiedCssText());
		CSSFunctionValue val = (CSSFunctionValue) cssval;
		assertEquals(3, val.getArguments().getLength());
	}

	@Test
	public void testGetCssTextLinearNonStandard2() {
		style.setCssText("background-image: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff));");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("-webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))", style.getPropertyValue("background-image"));
		assertEquals("background-image: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff)); ", style.getCssText());
		assertEquals("background-image:-webkit-gradient(linear,left top,left bottom,from(transparent),to(#fff))", style.getMinifiedCssText());
		FunctionValue val = (FunctionValue) cssval;
		assertEquals(5, val.getArguments().getLength());
		assertEquals("to(#fff)", val.getArguments().item(4).getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testGetCssTextLinearNonStandard3() {
		style.setCssText("background-image: -webkit-linear-gradient(transparent, #fff);");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, val.getPrimitiveType());
		assertEquals("-webkit-linear-gradient(transparent, #fff)", style.getPropertyValue("background-image"));
		assertEquals("background-image: -webkit-linear-gradient(transparent, #fff); ", style.getCssText());
		assertEquals("background-image:-webkit-linear-gradient(transparent,#fff)", style.getMinifiedCssText());
		assertEquals(2, val.getArguments().getLength());
		assertEquals("#fff", val.getArguments().item(1).getCssText());
		assertEquals(CSSGradientValue.GradientType.OTHER_GRADIENT, val.getGradientType());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testGetCssTextRadial() {
		style.setCssText("background-image: radial-gradient(5em circle at top left, yellow, blue); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("radial-gradient(5em circle at top left, yellow, blue)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(5em circle at top left, yellow, blue); ", style.getCssText());
		assertEquals("background-image:radial-gradient(5em circle at top left,yellow,blue)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(yellow, green); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("radial-gradient(yellow, green)", style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(yellow, green); ", style.getCssText());
		assertEquals("background-image:radial-gradient(yellow,green)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(ellipse at center, yellow 0%, green 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("radial-gradient(ellipse at center, yellow 0%, green 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(ellipse at center, yellow 0%, green 100%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(ellipse at center,yellow 0%,green 100%)",
				style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(farthest-corner at 50% 50%, yellow, green); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("radial-gradient(farthest-corner at 50% 50%, yellow, green)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(farthest-corner at 50% 50%, yellow, green); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(farthest-corner at 50% 50%,yellow,green)",
				style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(20px 30px at 20px 30px, red, yellow, green); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("radial-gradient(20px 30px at 20px 30px, red, yellow, green)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(20px 30px at 20px 30px, red, yellow, green); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(20px 30px at 20px 30px,red,yellow,green)",
				style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
	}

	@Test
	public void testGetCssTextRadial2() {
		style.setCssText("background-image: radial-gradient(center, ellipse cover, rgba(0, 0, 0, 0.4) 0, rgba(0, 0, 0, 0.9) 100%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(center, ellipse cover, rgb(0, 0, 0 / 0.4) 0, rgb(0, 0, 0 / 0.9) 100%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(center, ellipse cover, rgb(0, 0, 0 / 0.4) 0, rgb(0, 0, 0 / 0.9) 100%); ", style.getCssText());
		assertEquals("background-image:radial-gradient(center,ellipse cover,rgb(0,0,0/0.4) 0,rgb(0,0,0/0.9) 100%)", style.getMinifiedCssText());
	}

	@Test
	public void testGetCssTextRadial3() {
		style.setCssText("background-image: radial-gradient(circle at 40% 40%,rgba(255,255,255,.8),rgba(255,200,200,.6),#111 60%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(circle at 40% 40%, rgb(255, 255, 255 / 0.8), rgb(255, 200, 200 / 0.6), #111 60%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(circle at 40% 40%, rgb(255, 255, 255 / 0.8), rgb(255, 200, 200 / 0.6), #111 60%); ", style.getCssText());
		assertEquals("background-image:radial-gradient(circle at 40% 40%,rgb(255,255,255/0.8),rgb(255,200,200/0.6),#111 60%)", style.getMinifiedCssText());
	}

	@Test
	public void testGetCssTextRadial4() {
		style.setCssText("background-image: radial-gradient(circle,#e6e7e0 40%,rgba(43,42,161,.6) 110%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertEquals("radial-gradient(circle, #e6e7e0 40%, rgb(43, 42, 161 / 0.6) 110%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(circle, #e6e7e0 40%, rgb(43, 42, 161 / 0.6) 110%); ", style.getCssText());
		assertEquals("background-image:radial-gradient(circle,#e6e7e0 40%,rgb(43,42,161/0.6) 110%)", style.getMinifiedCssText());
	}

	@Test
	public void testGetCssTextRadial5() {
		style.setCssText("background-image: radial-gradient(40%,circle,#d4a9af 55%,#000 150%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(40%, circle, #d4a9af 55%, #000 150%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(40%, circle, #d4a9af 55%, #000 150%); ", style.getCssText());
		assertEquals("background-image:radial-gradient(40%,circle,#d4a9af 55%,#000 150%)", style.getMinifiedCssText());
	}

	@Test
	public void testGetCssTextRadialBad() {
		//
		style.setCssText("background-image: radial-gradient(20px 30px at 20px 30px, red, yellow 10% 100% 1%, green); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		//
		style.setCssText("background-image: radial-gradient(ellipse at center, yellow 0% green 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
	}

	@Test
	public void testGetCssTextConic() {
		style.setCssText("background-image: conic-gradient(#f06, gold); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(#f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(#f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#f06,gold)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(at 50% 50%, #f06, gold); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(at 50% 50%, #f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(at 50% 50%, #f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(at 50% 50%,#f06,gold)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(from 0deg, #f06, gold); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(from 0deg, #f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(from 0deg, #f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(from 0deg,#f06,gold)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(from 0deg at center, #f06, gold); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(from 0deg at center, #f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(from 0deg at center, #f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(from 0deg at center,#f06,gold)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(#f06 0%, gold 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(#f06 0%, gold 100%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(#f06 0%, gold 100%); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#f06 0%,gold 100%)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(#f06 0deg, gold 1turn); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(#f06 0deg, gold 1turn)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(#f06 0deg, gold 1turn); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#f06 0deg,gold 1turn)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(white -50%, black 150%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(white -50%, black 150%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(white -50%, black 150%); ", style.getCssText());
		assertEquals("background-image:conic-gradient(white -50%,black 150%)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(hsl(0,0%,75%), hsl(0,0%,25%)); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(hsl(0, 0%, 75%), hsl(0, 0%, 25%))", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(hsl(0, 0%, 75%), hsl(0, 0%, 25%)); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#bfbfbf,#404040)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(white 45deg, black 225deg, white 405deg); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(white 45deg, black 225deg, white 405deg)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(white 45deg, black 225deg, white 405deg); ", style.getCssText());
		assertEquals("background-image:conic-gradient(white 45deg,black 225deg,white 405deg)",
				style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(red, magenta, blue, aqua, lime, yellow, red); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("conic-gradient(red, magenta, blue, aqua, lime, yellow, red)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(red, magenta, blue, aqua, lime, yellow, red); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(red,magenta,blue,aqua,lime,yellow,red)",
				style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(7, val.getArguments().size());
	}

	@Test
	public void testGetCssTextConicBad() {
		style.setCssText("background-image: conic-gradient(red, magenta blue, aqua, lime, yellow, red); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		//
		style.setCssText("background-image: conic-gradient(white 45deg, black 225deg 200deg 1deg, white 405deg); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
	}

	@Test
	public void testGetCssTextRepeatingConic() {
		style.setCssText("background-image: repeating-conic-gradient(hsla(0,0%,100%,.2) 0deg 15deg); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("repeating-conic-gradient(hsl(0, 0%, 100% / 0.2) 0deg 15deg)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: repeating-conic-gradient(hsl(0, 0%, 100% / 0.2) 0deg 15deg); ",
				style.getCssText());
		assertEquals("background-image:repeating-conic-gradient(hsl(0,0%,100%/.2) 0deg 15deg)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.REPEATING_CONIC_GRADIENT, val.getGradientType());
		assertEquals(1, val.getArguments().size());
		//
		style.setCssText("background-image: repeating-conic-gradient(gold, #f06 20deg); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSPrimitiveValue2.CSS_GRADIENT, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("repeating-conic-gradient(gold, #f06 20deg)", style.getPropertyValue("background-image"));
		assertEquals("background-image: repeating-conic-gradient(gold, #f06 20deg); ", style.getCssText());
		assertEquals("background-image:repeating-conic-gradient(gold,#f06 20deg)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.REPEATING_CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
	}

	@Test
	public void testEquals() {
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		BaseCSSStyleDeclaration style2 = new BaseCSSStyleDeclaration();
		style2.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("background-image");
		AbstractCSSValue cssval2 = style2.getPropertyCSSValue("background-image");
		assertTrue(cssval.equals(cssval2));
		assertEquals(cssval.hashCode(), cssval2.hashCode());
		style2.setCssText("background-image: linear-gradient(to top right, red, white); ");
		cssval2 = style2.getPropertyCSSValue("background-image");
		assertFalse(cssval.equals(cssval2));
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		GradientValue value = (GradientValue) style.getPropertyCSSValue("background-image");
		GradientValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getArguments(), clon.getArguments());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
