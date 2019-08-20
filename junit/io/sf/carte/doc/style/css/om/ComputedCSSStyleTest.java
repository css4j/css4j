/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.dom.TestDOMImplementation;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.NumberValue;

public class ComputedCSSStyleTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	static CSSStyleSheet sheet;

	static CSSDocument xhtmlDoc;

	@BeforeClass
	public static void setUpBeforeClass() {
		sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
	}

	@Before
	public void setUp() throws IOException, DocumentException {
		xhtmlDoc = DOMCSSStyleSheetFactoryTest.sampleXHTML();
	}

	@Test
	public void testGeneric() {
		Node node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSElement);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle((CSSElement) node, null);
		assertNotNull(style);
		assertNull(((BaseCSSStyleDeclaration) style).getParentRule());
	}

	@Test
	public void getUsedFontFamily1() throws CSSMediaException {
		xhtmlDoc.setTargetMedium("screen");
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("Helvetica", style.getUsedFontFamily());
	}

	@Test
	public void getUsedFontFamily2() throws CSSMediaException {
		xhtmlDoc.setTargetMedium("screen");
		CSSElement elm = xhtmlDoc.getElementById("listpara");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("Helvetica", style.getUsedFontFamily());
	}

	@Test
	public void getFontSize1() {
		TestDOMImplementation impl = new TestDOMImplementation();
		CSSDocument newdoc = impl.createDocument(null, null, null);
		CSSElement root = newdoc.createElement("html");
		newdoc.appendChild(root);
		CSSElement elm = newdoc.createElement("body");
		elm.setAttribute("style", "font-size: 12pt");
		root.appendChild(elm);
		CSSElement h3 = newdoc.createElement("h3");
		elm.appendChild(h3);
		CSSComputedProperties style = newdoc.getStyleSheet().getComputedStyle(h3, null);
		assertNotNull(style);
		CSSStyleRule rule = defaultStyleRule("h3", "font-size");
		assertNotNull(rule);
		NumberValue val = (NumberValue) rule.getStyle().getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(12f * val.getFloatValue(CSSPrimitiveValue.CSS_EMS), style.getComputedFontSize(), 0.05);
	}

	@Test
	public void getFontSize2() {
		CSSElement elm = xhtmlDoc.getElementById("tablepara");
		elm = (CSSElement) elm.getElementsByTagName("span").item(0);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals(1.5f * 12f, style.getComputedFontSize(), 0.001);
	}

	@Test
	public void getFontSizeMedia() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("span1");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals(12f, style.getComputedFontSize(), 0.001);
		CSSElement para = xhtmlDoc.getElementById("para2");
		CSSComputedProperties stylePara = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(stylePara);
		assertEquals(12f, stylePara.getComputedFontSize(), 0.001);
		xhtmlDoc.setTargetMedium("screen");
		assertEquals("screen", xhtmlDoc.getStyleSheet().getTargetMedium());
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals(20f, style.getComputedFontSize(), 0.001);
		stylePara = xhtmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertEquals(16f, stylePara.getComputedFontSize(), 0.001);
		xhtmlDoc.setTargetMedium("all");
	}

	@Test
	public void getColor() throws CSSPropertyValueException {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#000080", style.getCSSColor().getStringValue());
		assertEquals(128f, style.getCSSColor().getRGBColorValue().getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				0.001f);
		assertEquals(0, style.getCSSColor().getRGBColorValue().getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				0.001f);
		assertEquals(0, style.getCSSColor().getRGBColorValue().getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				0.001f);
	}

	@Test
	public void testGetContentFromStyleElement() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("  foo  bar  ", style.getPropertyValue("content"));
	}

	@Test
	public void getPropertyCSSValueForBorderWidth() throws CSSPropertyValueException {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("0", style.getPropertyValue("border-top-width"));
		CSSValue cssval = style.getPropertyCSSValue("border-top-width");
		assertNotNull(cssval);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals(0, ((CSSPrimitiveValue) cssval).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 0.001f);
	}

	@Test
	public void nonexistentTag() {
		CSSElement elm = xhtmlDoc.getElementById("listpara");
		elm = (CSSElement) elm.getElementsByTagName("nonexistenttag").item(0);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals(12f, style.getComputedFontSize(), 0.001);
	}

	@Test
	public void getBackgroundImages() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("http://www.example.com/css/background.png", style.getBackgroundImages()[0]);
	}

	@Test
	public void getBackgroundImagesForMedia() {
		try {
			xhtmlDoc.setTargetMedium("print");
		} catch (CSSMediaException e) {
			fail("Failed to change medium: " + e.getMessage());
		}
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals(2, style.getBackgroundImages().length);
		assertEquals("http://www.example.com/media/printbg.png", style.getBackgroundImages()[0]);
		assertEquals("http://www.example.com/media/printbg2.png", style.getBackgroundImages()[1]);
	}

	@Test
	public void getComputedStyle() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		BoxValues box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(19.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font: 120%");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 28.8pt; margin-bottom: 43.2pt; background-position: 20% 0%; padding-left: calc(10% - 43.2pt - 14.4pt); font-size: 14.4pt; font-style: normal; font-weight: normal; font-stretch: normal; font-family: initial; line-height: normal; font-variant-caps: normal; font-size-adjust: none; font-kerning: auto; font-variant-ligatures: normal; font-variant-position: normal; font-variant-numeric: normal; font-variant-alternates: normal; font-variant-east-asian: normal; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:43.2pt;margin-top:28.8pt;background-position:20% 0%;padding-left:calc(10% - 43.2pt - 14.4pt);font:14.4pt;",
				style.getMinifiedCssText());
		//
		CSSElement docelm = xhtmlDoc.getDocumentElement();
		// Set explicit document width
		xhtmlDoc.getOverrideStyle(docelm, null).setCssText("width:675pt");
		CSSElement body = (CSSElement) docelm.getElementsByTagName("body").item(0);
		style = body.getComputedStyle(null);
		assertEquals(12f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(36f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(48f, box.getMarginBottom(), 0.01f);
		assertEquals(33.75f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font: 120%");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(14.4f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(9.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(28.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(43.2f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(665.1f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 1.8ex; margin-left:1ex");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(24.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(21.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(32.4f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(645.3f, box.getWidth(), 0.01f);
		CSSPrimitiveValue marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2rem; margin-left:1rem");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(24f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(48f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(72f, box.getMarginBottom(), 0.01f);
		assertEquals(12f, box.getMarginLeft(), 0.01f);
		assertEquals(663f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 48pt; margin-bottom: 72pt; background-position: 20% 0%; padding-left: calc(10% - 72pt - 24pt); font-size: 24pt; margin-left: 12pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:72pt;margin-left:12pt;margin-top:48pt;background-position:20% 0%;padding-left:calc(10% - 72pt - 24pt);font-size:24pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 0.8lh; margin-left:0.6lh");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(11.14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(22.94f, box.getPaddingLeft(), 0.01f);
		assertEquals(22.28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(33.42f, box.getMarginBottom(), 0.01f);
		assertEquals(7.75f, box.getMarginLeft(), 0.01f);
		assertEquals(644.31f, box.getWidth(), 0.01f);
		assertEquals("auto", style.getWidth());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 1.08rlh; margin-left:0.6rlh");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(15.03f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(7.38f, box.getPaddingLeft(), 0.01f);
		assertEquals(30.06f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(45.09f, box.getMarginBottom(), 0.01f);
		assertEquals(8.35f, box.getMarginLeft(), 0.01f);
		assertEquals(659.27f, box.getWidth(), 0.01f);
		assertEquals("30.06pt", style.getMarginTop());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:1vw");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VW), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vw; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vw;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:0.8cap;margin-left:1vw");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		CSSPrimitiveValue fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.6f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.6f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:3ch;margin-left:1vw");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:0.8ic;margin-left:1vw");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.6f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.6f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:1vh");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VH), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vh; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vh;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:1vi");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VI), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vi; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vi;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:1vb");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vb;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:var(--foo,1vb)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vb;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:var(--foo,1vb);--foo:8pt");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 8pt; --foo: 8pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:8pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);--foo:8pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getOverrideStyle(listpara, null).setCssText("font-size:var(--foo,1vb)");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(8f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:var(--foo,9pt);--foo:var(--foo)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:calc(2*var(--foo,5pt));--foo:var(--foo)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(10f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:var(--foo);--foo:var(--foo)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-left:calc(2*var(--foo));--foo:var(--foo)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("text-indent:1pt");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		CSSPrimitiveValue textIndent = (CSSPrimitiveValue) style.getPropertyCSSValue("text-indent");
		assertEquals(1f, textIndent.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		xhtmlDoc.getOverrideStyle(listpara, null).setCssText("text-indent:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		textIndent = (CSSPrimitiveValue) style.getPropertyCSSValue("text-indent");
		assertNotNull(textIndent);
		assertEquals(1f, textIndent.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:21pt");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(21f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		xhtmlDoc.getOverrideStyle(listpara, null).setCssText("font-size:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(21f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:var(--foo,9pt);--foo:var(--foo)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:var(--foo);--foo:var(--foo)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(12f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin-right:calc(1.5*var(--foo));--foo:var(FONT-SIZE)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		CSSPrimitiveValue marginRight = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-right");
		assertEquals(18f, marginRight.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:smaller");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.84f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:larger");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(14.4f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:small");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals("small", fontSize.getStringValue());
		assertEquals(10f, style.getComputedFontSize(), 0.01f);
		xhtmlDoc.getOverrideStyle(listpara, null).setCssText("font-size:smaller");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals("x-small", fontSize.getStringValue());
		assertEquals(9f, style.getComputedFontSize(), 0.01f);
		xhtmlDoc.getOverrideStyle(listpara, null).setCssText("font-size:larger");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals("medium", fontSize.getStringValue());
		assertEquals(12f, style.getComputedFontSize(), 0.01f);
		//
		/*
		 * property: unset
		 */
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:unset;margin-left:unset");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		xhtmlDoc.getOverrideStyle(listpara, null).setCssText("font-size:unset;margin-left:unset");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:16pt;margin-left:8pt");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(16f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		/*
		 * property: uppercase color identifier
		 */
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("color:BLUE");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		CSSPrimitiveValue2 color = (CSSPrimitiveValue2) style.getPropertyCSSValue("color");
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, color.getPrimitiveType());
		RGBAColor rgb = color.getRGBColorValue();
		assertEquals("#00f", rgb.toString());
	}

	@Test
	public void getComputedStyleMediumScreen() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		// medium 'screen'
		xhtmlDoc.setTargetMedium("screen");
		/*
		 * Now the library uses a style database.
		 */
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		BoxValues box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(663.5f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font: 120%");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(674.7f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2ex; margin-left:1ex");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(7f, box.getMarginLeft(), 0.01f);
		assertEquals(656.5f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 28pt; margin-bottom: 42pt; background-position: 20% 0%; padding-left: calc(10% - 42pt - 14pt); font-size: 14pt; margin-left: 7pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:42pt;margin-left:7pt;margin-top:28pt;background-position:20% 0%;padding-left:calc(10% - 42pt - 14pt);font-size:14pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2rem; margin-left:1rem");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(24f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(48f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(72f, box.getMarginBottom(), 0.01f);
		assertEquals(12f, box.getMarginLeft(), 0.01f);
		assertEquals(663f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 48pt; margin-bottom: 72pt; background-position: 20% 0%; padding-left: calc(10% - 72pt - 24pt); font-size: 24pt; margin-left: 12pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:72pt;margin-left:12pt;margin-top:48pt;background-position:20% 0%;padding-left:calc(10% - 72pt - 24pt);font-size:24pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 0.8lh; margin-left:0.6lh");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(12.99f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(15.54f, box.getPaddingLeft(), 0.01f);
		assertEquals(25.98f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(38.97f, box.getMarginBottom(), 0.01f);
		assertEquals(9.04f, box.getMarginLeft(), 0.01f);
		assertEquals(650.42f, box.getWidth(), 0.01f);
		assertEquals("25.98pt", style.getMarginTop());
		assertEquals("auto", style.getWidth());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 1.08rlh; margin-left:0.6rlh");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(15.03f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(7.38f, box.getPaddingLeft(), 0.01f);
		assertEquals(30.06f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(45.09f, box.getMarginBottom(), 0.01f);
		assertEquals(8.35f, box.getMarginLeft(), 0.01f);
		assertEquals(659.27f, box.getWidth(), 0.01f);
		assertEquals("30.06pt", style.getMarginTop());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vw; margin-left:1vw");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vh;margin-left:1vh");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vi;margin-left:1vi");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size:2vi;margin-left:1vi;writing-mode:vertical-lr");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; writing-mode: vertical-lr; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;writing-mode:vertical-lr;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vb;margin-left:1vb");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vb;margin-left:1vb;writing-mode:vertical-lr");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; writing-mode: vertical-lr; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;writing-mode:vertical-lr;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vmin;margin-left:1vmin");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: 2vmax;margin-left:1vmax");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: calc(110% - 0.1vw);margin-left:max(1em,1rem)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(14.81f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(8.26f, box.getPaddingLeft(), 0.01f);
		assertEquals(29.62f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(44.43f, box.getMarginBottom(), 0.01f);
		assertEquals(14.81f, box.getMarginLeft(), 0.01f);
		assertEquals(651.93f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: max(110%,1.4rem);margin-left:calc(1em - 0.3rem)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(13.2f, box.getMarginLeft(), 0.01f);
		assertEquals(661.5f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font-size: var(--foo,1.4rem);margin-left:var(--bar,0.9rem)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(10.8f, box.getMarginLeft(), 0.01f);
		assertEquals(663.9f, box.getWidth(), 0.01f);
		//
		xhtmlDoc.getOverrideStyle(elm, null)
				.setCssText("font-size: max(110%,var(--foo,1.4rem));margin-left:calc(1em - var(--bar,0.3rem))");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(13.2f, box.getMarginLeft(), 0.01f);
		assertEquals(661.5f, box.getWidth(), 0.01f);
	}

	@Test
	public void getComputedStyleCalc() {
		CSSElement elm = xhtmlDoc.getElementById("listpara");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals(
				"display: block; margin-top: 12pt; margin-bottom: 12pt; unicode-bidi: embed; margin-right: 1%; font-family: 'Does Not Exist', Neither, Helvetica; padding-left: calc(10% - 36pt - 12pt); ",
				style.getCssText());
		assertEquals(
				"display:block;margin-bottom:12pt;margin-right:1%;margin-top:12pt;unicode-bidi:embed;font-family:'Does Not Exist',Neither,Helvetica;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		BoxValues box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(17.55f, box.getPaddingLeft(), 0.01f);
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("font: 120%");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals(
				"display: block; margin-top: 14.4pt; margin-bottom: 14.4pt; unicode-bidi: embed; margin-right: 1%; font-family: initial; padding-left: calc(10% - 36pt - 12pt); font-size: 14.4pt; font-style: normal; font-weight: normal; font-stretch: normal; line-height: normal; font-variant-caps: normal; font-size-adjust: none; font-kerning: auto; font-variant-ligatures: normal; font-variant-position: normal; font-variant-numeric: normal; font-variant-alternates: normal; font-variant-east-asian: normal; ",
				style.getCssText());
		assertEquals(
				"display:block;margin-bottom:14.4pt;margin-right:1%;margin-top:14.4pt;unicode-bidi:embed;font:14.4pt;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		//
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("foo:sin(90deg/2);bar:sin(30deg)");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("0.5", style.getPropertyValue("bar"));
		ExtendedCSSValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(0.70710677f, ((CSSPrimitiveValue) val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void getComputedStyleUnset() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		ExtendedCSSValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("36pt", val.getCssText());
		// Check 'unset'
		xhtmlDoc.getOverrideStyle(elm, null).setProperty("font-size", "unset", "");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("12pt", val.getCssText());
	}

	@Test
	public void getComputedStyleForBackgroundImages() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		ExtendedCSSValue val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("url('http://www.example.com/css/background.png')", val.getCssText());
		assertEquals("http://www.example.com/css/background.png", ((CSSPrimitiveValue) val).getStringValue());
		assertEquals("url('css/background.png')", val.getMinifiedCssText("background-image"));
		assertEquals(
				"display: block; margin-top: 36pt; margin-right: 5%; margin-bottom: 48pt; margin-left: 5%; unicode-bidi: embed; background-color: #fff; background-image: url('http://www.example.com/css/background.png'); background-position: 0% 0%; background-size: auto auto; background-origin: padding-box; background-clip: border-box; background-repeat: repeat repeat; background-attachment: scroll; color: #808000; font-family: Arial, Helvetica; font-size: 12pt; width: 900px; ",
				style.getCssText());
		assertEquals(
				"display:block;margin:36pt 5% 48pt;unicode-bidi:embed;background:url('css/background.png') #fff;color:#808000;font-family:Arial,Helvetica;font-size:12pt;width:900px;",
				style.getMinifiedCssText());
		try {
			xhtmlDoc.setTargetMedium("print");
		} catch (CSSMediaException e) {
			fail("Failed to change medium: " + e.getMessage());
		}
		elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		CSSValueList list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("url('http://www.example.com/media/printbg.png')", list.item(0).getCssText());
		assertEquals("url('http://www.example.com/media/printbg2.png')", list.item(1).getCssText());
		assertEquals(
				"url('http://www.example.com/media/printbg.png'), url('http://www.example.com/media/printbg2.png')",
				list.getCssText());
	}

	@Test
	public void getComputedStyleForBackgroundImagesInStyleAttribute() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("url('http://www.example.com/headerbg.png')", val.getCssText());
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("background: url('override.png')");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("url('http://www.example.com/override.png')", val.getCssText());
		// Check 'unset'
		xhtmlDoc.getOverrideStyle(elm, null).setProperty("background-image", "unset", "");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("none", val.getCssText());
	}

	@Test
	public void getComputedStyleForBackgroundRepeat() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-repeat");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		CSSValueList list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("repeat", list.item(0).getCssText());
		assertEquals("repeat", list.item(1).getCssText());
		assertEquals("repeat repeat", val.getCssText());
	}

	@Test
	public void getComputedStyleForBackgroundProperties() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		xhtmlDoc.getOverrideStyle(elm, null).setCssText(
				"background-image: url('img1.png'), url('img2.png'); background-repeat: repeat no-repeat, round, space; background-position: left center, 10% 5%; background-clip: padding-box; background-attachment: fixed, local, local, scroll");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-repeat");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		CSSValueList list = (CSSValueList) val;
		assertEquals(3, list.getLength());
		list = (CSSValueList) ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image",
				"background-repeat", (AbstractCSSValue) val);
		assertEquals(2, list.getLength());
		assertEquals("repeat no-repeat", list.item(0).getCssText());
		assertEquals("round round", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-position");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("left center", list.item(0).getCssText());
		assertEquals("10% 5%", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-clip");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-clip",
				(AbstractCSSValue) val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("padding-box", list.item(0).getCssText());
		assertEquals("padding-box", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-attachment");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-attachment",
				(AbstractCSSValue) val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("fixed", list.item(0).getCssText());
		assertEquals("local", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-size");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-size",
				(AbstractCSSValue) val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("auto auto", list.item(0).getCssText());
		assertEquals("auto auto", list.item(1).getCssText());
	}

	@Test
	public void getComputedStyleForTransitionProperties() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		xhtmlDoc.getOverrideStyle(elm, null).setCssText(
				"transition-property: background-color, opacity, width, height; transition-duration: 1s, 10s, 3s; transition-timing-function: ease, linear, cubic-bezier(0.33, 0.1, 0.5, 1); transition-delay: 2s, 1s");
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("transition-property");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		CSSValueList list = (CSSValueList) val;
		assertEquals(4, list.getLength());
		assertEquals("background-color", list.item(0).getCssText());
		assertEquals("opacity", list.item(1).getCssText());
		assertEquals("background-color, opacity, width, height", style.getPropertyValue("transition-property"));
		val = style.getPropertyCSSValue("transition-duration");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(3, list.getLength());
		list = (CSSValueList) ((BaseCSSStyleDeclaration) style).computeBoundProperty("transition-property",
				"transition-duration", (AbstractCSSValue) val);
		assertEquals(4, list.getLength());
		assertEquals("1s", list.item(0).getCssText());
		assertEquals("10s", list.item(1).getCssText());
		assertEquals("3s", list.item(2).getCssText());
		assertEquals("1s", list.item(3).getCssText());
		val = style.getPropertyCSSValue("transition-timing-function");
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("transition-property",
				"transition-timing-function", (AbstractCSSValue) val);
		assertEquals("ease, linear, cubic-bezier(0.33, 0.1, 0.5, 1), ease", val.getCssText());
		val = style.getPropertyCSSValue("transition-delay");
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("transition-property", "transition-delay",
				(AbstractCSSValue) val);
		assertEquals("2s, 1s, 2s, 1s", val.getCssText());
	}

	CSSStyleRule defaultStyleRule(String selectorText, String propertyName) {
		CSSRuleList rules = sheet.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			if (rule instanceof CSSStyleRule) {
				String selText = ((CSSStyleRule) rule).getSelectorText();
				// Small hack
				StringTokenizer st = new StringTokenizer(selText, ",");
				while (st.hasMoreElements()) {
					String selector = st.nextToken();
					if (selector.equals(selectorText)) {
						if (((CSSStyleRule) rule).getStyle().getPropertyCSSValue(propertyName) != null) {
							return ((CSSStyleRule) rule);
						}
						break;
					}
				}
			}
		}
		return null;
	}

}
