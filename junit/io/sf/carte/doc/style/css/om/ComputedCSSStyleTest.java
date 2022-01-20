/*

 Copyright (c) 2005-2022, Carlos Amengual.

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
import java.util.HashMap;
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
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.StyleValue;

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
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("Does Not Exist", style.getUsedFontFamily());
		// Now we use a style database
		xhtmlDoc.setTargetMedium("screen");
		style = elm.getComputedStyle(null);
		assertEquals("Helvetica", style.getUsedFontFamily());
	}

	@Test
	public void getUsedFontFamily2() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("listpara");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("Does Not Exist", style.getUsedFontFamily());
		// Now we use a style database
		xhtmlDoc.setTargetMedium("screen");
		style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(1.5f * 12f, style.getComputedFontSize(), 0.001);
	}

	@Test
	public void getFontSizeMedia() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("span1");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(12f, style.getComputedFontSize(), 0.001);
		CSSElement para = xhtmlDoc.getElementById("para2");
		CSSComputedProperties stylePara = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(stylePara);
		assertEquals(12f, stylePara.getComputedFontSize(), 0.001);
		xhtmlDoc.setTargetMedium("screen");
		assertEquals("screen", xhtmlDoc.getStyleSheet().getTargetMedium());
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(20f, style.getComputedFontSize(), 0.001);
		stylePara = xhtmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertEquals(16f, stylePara.getComputedFontSize(), 0.001);
		xhtmlDoc.setTargetMedium("all");
	}

	@Test
	public void getColor() throws CSSPropertyValueException {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("  foo  bar  ", style.getPropertyValue("content"));
	}

	@Test
	public void getPropertyCSSValueForBorderWidth() throws CSSPropertyValueException {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(12f, style.getComputedFontSize(), 0.001);
	}

	@Test
	public void getBackgroundImages() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		CSSComputedProperties style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(2, style.getBackgroundImages().length);
		assertEquals("http://www.example.com/media/printbg.png", style.getBackgroundImages()[0]);
		assertEquals("http://www.example.com/media/printbg2.png", style.getBackgroundImages()[1]);
	}

	@Test
	public void getComputedStyle() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		// Box values
		BoxValues box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(19.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		//
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 28.8pt; margin-bottom: 43.2pt; background-position: 20% 0%; padding-left: calc(10% - 43.2pt - 14.4pt); font-size: 14.4pt; font-family: Arial; font-style: normal; font-weight: normal; font-stretch: normal; line-height: normal; font-variant-caps: normal; font-size-adjust: none; font-kerning: auto; font-variant-ligatures: normal; font-variant-position: normal; font-variant-numeric: normal; font-variant-alternates: normal; font-variant-east-asian: normal; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:43.2pt;margin-top:28.8pt;background-position:20% 0%;padding-left:calc(10% - 43.2pt - 14.4pt);font:14.4pt Arial;",
				style.getMinifiedCssText());
		//
		CSSElement docelm = xhtmlDoc.getDocumentElement();
		// Set explicit document width for box model
		docelm.getOverrideStyle(null).setCssText("width:675pt");
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
		/*
		 * Relative % font, box model.
		 */
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
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
		/*
		 * Relative 'ex' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 1.8ex; margin-left:1ex");
		style = elm.getComputedStyle(null);
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Relative 'rem' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 2rem; margin-left:1rem");
		style = elm.getComputedStyle(null);
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * Relative 'lh' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 0.8lh; margin-left:0.6lh");
		style = elm.getComputedStyle(null);
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * Relative 'rlh' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 1.08rlh; margin-left:0.6rlh");
		style = elm.getComputedStyle(null);
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * 'vw' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vw");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VW), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vw; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vw;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * Relative 'cap' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:0.8cap;margin-left:1vw");
		style = elm.getComputedStyle(null);
		CSSPrimitiveValue fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_CAP), 0.01f);
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_CAP), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		assertEquals(9.6f, style.getComputedFontSize(), 0.01f);
		/*
		 * Relative 'ch' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:3ch;margin-left:1vw");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(3f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_CH), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(3f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_CH), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		assertEquals(6.75f, style.getComputedFontSize(), 0.01f);
		/*
		 * Relative 'ic' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:0.8ic;margin-left:1vw");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_IC), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_IC), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		assertEquals(7.68f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		/*
		 * 'vh' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vh");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VH), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vh; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vh;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * 'vi' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vi");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VI), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vi; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vi;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * 'vb' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vb");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vb;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * 'vb' unit, custom property fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vb;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * 'vb' unit, shorthand custom property fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,1.1vb)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1.1f, marginLeft.getFloatValue(CSSPrimitiveValue2.CSS_VB), 0.01f);
		CSSPrimitiveValue marginTop = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-top");
		assertEquals(1.1f, marginTop.getFloatValue(CSSPrimitiveValue2.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 1.1vb; margin-bottom: 1.1vb; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-right: 1.1vb; margin-left: 1.1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin:1.1vb;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * custom property substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb);--foo:8pt");
		style = elm.getComputedStyle(null);
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
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo,1vb)");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(8f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property circular dependency, fallback used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,9pt);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * custom property circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * custom property circular dependency in ancestor, no fallback (I).
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		docelm.getOverrideStyle(null).setCssText("--foo:var(--foo)");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(docelm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(docelm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * custom property, check for bogus circular dependency with ancestor, no
		 * fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		docelm.getOverrideStyle(null).setCssText("--foo:var(--bar)");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--bar:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
		/*
		 * custom property circular dependency in ancestor, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		docelm.getOverrideStyle(null).setCssText("--foo:var(--foo,2pt)");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
		//
		docelm.getOverrideStyle(null).setCssText("");
		/*
		 * custom property inside calc(), circular dependency.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:calc(2*var(--foo,5pt));--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(10f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * custom property circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * custom property inside calc(), circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:calc(2*var(--foo));--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * custom property circular dependency, no fallback, inherited value used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("text-indent:1pt");
		style = elm.getComputedStyle(null);
		CSSPrimitiveValue textIndent = (CSSPrimitiveValue) style.getPropertyCSSValue("text-indent");
		assertEquals(1f, textIndent.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		listpara.getOverrideStyle(null).setCssText("text-indent:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		textIndent = (CSSPrimitiveValue) style.getPropertyCSSValue("text-indent");
		assertNotNull(textIndent);
		assertEquals(1f, textIndent.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * font-size custom property circular dependency, no fallback, inherited value
		 * used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:21pt");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(21f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(21f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		/*
		 * font-size custom property circular dependency, fallback used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo,9pt);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * font-size custom property circular dependency, no fallback, inherited value
		 * used (II).
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(12f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * custom property inside calc(), uppercase property name.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-right:calc(1.5*var(--foo));--foo:var(FONT-SIZE)");
		style = elm.getComputedStyle(null);
		CSSPrimitiveValue marginRight = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-right");
		assertEquals(18f, marginRight.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		/*
		 * font-size: smaller
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:smaller");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.84f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * font-size: larger
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:larger");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(14.4f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * font-size: small, x-small, etc.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:small");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals("small", fontSize.getStringValue());
		assertEquals(10f, style.getComputedFontSize(), 0.01f);
		listpara.getOverrideStyle(null).setCssText("font-size:smaller");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals("x-small", fontSize.getStringValue());
		assertEquals(9f, style.getComputedFontSize(), 0.01f);
		listpara.getOverrideStyle(null).setCssText("font-size:larger");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals("medium", fontSize.getStringValue());
		assertEquals(12f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		/*
		 * font-size: identifier inheritance.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:large");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertEquals(CSSPrimitiveValue.CSS_IDENT, fontSize.getPrimitiveType());
		assertEquals("large", fontSize.getStringValue());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:42pt;margin-top:28pt;background-position:20% 0%;padding-left:calc(10% - 42pt - 14pt);font-size:large;",
				style.getMinifiedCssText());
		listpara.getOverrideStyle(null).setCssText("font-size:66%");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(CSSPrimitiveValue.CSS_PT, fontSize.getPrimitiveType());
		assertEquals(9.24f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(9.24f, style.getComputedFontSize(), 0.01f);
		assertEquals(
				"display:block;margin-bottom:9.24pt;margin-right:1%;margin-top:9.24pt;unicode-bidi:embed;font-family:'Does Not Exist',Neither,Helvetica;font-size:9.24pt;padding-left:calc(10% - 42pt - 14pt);",
				style.getMinifiedCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		/*
		 * font-size: 3-level identifier inheritance. Requires state from previous test!
		 */
		CSSElement nonextag = (CSSElement) elm.getElementsByTagName("nonexistenttag").item(0);
		nonextag.getOverrideStyle(null).setCssText("font-size:0.85em");
		style = nonextag.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(CSSPrimitiveValue.CSS_PT, fontSize.getPrimitiveType());
		assertEquals(7.85f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(7.85f, style.getComputedFontSize(), 0.01f);
		assertEquals("font-size:7.85pt;", style.getMinifiedCssText());
		/*
		 * property: initial
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:initial;margin-left:initial");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSPrimitiveValue.CSS_IDENT, fontSize.getPrimitiveType());
		assertEquals("medium", fontSize.getStringValue());
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:0;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);font-size:medium;",
				style.getMinifiedCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		/*
		 * property: unset
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:unset;margin-left:unset");
		style = elm.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:0;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);font-size:12pt;",
				style.getMinifiedCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		listpara.getOverrideStyle(null).setCssText("font-size:unset;margin-left:unset");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		elm.getOverrideStyle(null).setCssText("font-size:16pt;margin-left:8pt");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(16f, fontSize.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(
				"display:block;margin:16pt 1% 16pt 0;unicode-bidi:embed;font-family:'Does Not Exist',Neither,Helvetica;font-size:16pt;padding-left:calc(10% - 48pt - 16pt);",
				style.getMinifiedCssText());
		/*
		 * property: uppercase color identifier
		 */
		elm.getOverrideStyle(null).setCssText("color:BLUE");
		style = elm.getComputedStyle(null);
		CSSPrimitiveValue2 color = (CSSPrimitiveValue2) style.getPropertyCSSValue("color");
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, color.getPrimitiveType());
		RGBAColor rgb = color.getRGBColorValue();
		assertEquals("#00f", rgb.toString());
		/*
		 * attr() value, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin,0.8em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9.6f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		/*
		 * attr() value in calc(), fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:calc(attr(leftmargin,0.8em)*2)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(19.2f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		CSSPrimitiveValue paddingLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("padding-left");
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, paddingLeft.getPrimitiveType());
		/*
		 * attr() wrong value, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-foo length,0.8em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9.6f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() value.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-foo length,0.8em)");
		elm.getAttributeNode("data-foo").setValue("11pt");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(11f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() unsafe value, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement usernameElm = xhtmlDoc.getElementById("username");
		usernameElm.getOverrideStyle(null).setCssText("--steal:attr(data-default-user,\"no luck\")");
		style = usernameElm.getComputedStyle(null);
		CSSPrimitiveValue customProp = (CSSPrimitiveValue) style.getPropertyCSSValue("--steal");
		assertEquals("no luck", customProp.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(usernameElm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(usernameElm));
		/*
		 * attr() unsafe 'value' attribute inside form, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		usernameElm.getOverrideStyle(null).setCssText("--steal:attr(value,\"no luck\")");
		style = usernameElm.getComputedStyle(null);
		customProp = (CSSPrimitiveValue) style.getPropertyCSSValue("--steal");
		assertEquals("no luck", customProp.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(usernameElm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(usernameElm));
		/*
		 * attr() circular reference, default/fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar integer)");
		elm.getAttributeNode("data-bar").setValue("attr(data-foo integer, 1)");
		elm.getOverrideStyle(null).setCssText("--foo:attr(data-foo integer)");
		style = elm.getComputedStyle(null);
		assertNull(style.getPropertyCSSValue("--foo"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("--foo:attr(data-bar integer)");
		style = elm.getComputedStyle(null);
		assertNull(style.getPropertyCSSValue("--foo"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() circular reference, wrong data type.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar integr)");
		elm.getAttributeNode("data-bar").setValue("attr(data-foo integr)");
		elm.getOverrideStyle(null).setCssText("--foo:attr(data-foo integr)");
		style = elm.getComputedStyle(null);
		customProp = (CSSPrimitiveValue) style.getPropertyCSSValue("--foo");
		assertNull(customProp);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() circular reference, wrong data type, default.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("--foo:attr(data-foo integer)");
		style = elm.getComputedStyle(null);
		customProp = (CSSPrimitiveValue) style.getPropertyCSSValue("--foo");
		assertNull(style.getPropertyCSSValue("--foo"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() circular reference, wrong data type, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar integr, 1)");
		elm.getOverrideStyle(null).setCssText("--foo:attr(data-foo integer)");
		style = elm.getComputedStyle(null);
		assertNull(style.getPropertyCSSValue("--foo"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleMediumScreenNoDb() throws CSSMediaException, IOException, DocumentException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(true, null);
		factory.setDeviceFactory(null);
		xhtmlDoc = DOMCSSStyleSheetFactoryTest.sampleXHTML(factory);
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		// medium 'screen'
		xhtmlDoc.setTargetMedium("screen");
		elm.getOverrideStyle(null).setCssText("font-size: 2vw; margin-left:1vw");
		style = elm.getComputedStyle(null);
		assertEquals(28.8f, style.getComputedFontSize(), 0.01f);
		CSSPrimitiveValue fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		CSSPrimitiveValue marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSPrimitiveValue2.CSS_VW, fontSize.getPrimitiveType());
		assertEquals(2f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_VW), 0.01f);
		assertEquals(14.4f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 57.6pt; margin-bottom: 86.4pt; background-position: 20% 0%; padding-left: calc(10% - 86.4pt - 28.8pt); font-size: 2vw; margin-left: 14.4pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:86.4pt;margin-left:14.4pt;margin-top:57.6pt;background-position:20% 0%;padding-left:calc(10% - 86.4pt - 28.8pt);font-size:2vw;",
				style.getMinifiedCssText());
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vh;margin-left:1vh");
		style = elm.getComputedStyle(null);
		assertEquals(16.2f, style.getComputedFontSize(), 0.01f);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSPrimitiveValue2.CSS_VH, fontSize.getPrimitiveType());
		assertEquals(2f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_VH), 0.01f);
		assertEquals(8.1f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
	}

	@Test
	public void getComputedStyleMediumPrintNoDb() throws CSSMediaException, IOException, DocumentException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(true, null);
		factory.setDeviceFactory(null);
		xhtmlDoc = DOMCSSStyleSheetFactoryTest.sampleXHTML(factory);
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		// medium 'print'
		xhtmlDoc.setTargetMedium("print");
		elm.getOverrideStyle(null).setCssText("font-size: 2vw; margin-left:1vw");
		style = elm.getComputedStyle(null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		CSSPrimitiveValue fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		CSSPrimitiveValue marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSPrimitiveValue2.CSS_VW, fontSize.getPrimitiveType());
		assertEquals(2f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_VW), 0.01f);
		assertEquals(5.95f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 2vw; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:2vw;",
				style.getMinifiedCssText());
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vh;margin-left:1vh");
		style = elm.getComputedStyle(null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		fontSize = (CSSPrimitiveValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSPrimitiveValue2.CSS_VH, fontSize.getPrimitiveType());
		assertEquals(2f, fontSize.getFloatValue(CSSPrimitiveValue2.CSS_VH), 0.01f);
		assertEquals(8.42f, marginLeft.getFloatValue(CSSPrimitiveValue.CSS_PT), 0.01f);
	}

	@Test
	public void getComputedStyleMediumScreen() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		// medium 'screen'
		xhtmlDoc.setTargetMedium("screen");
		/*
		 * Now the library uses a style database.
		 */
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2ex; margin-left:1ex");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2rem; margin-left:1rem");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 0.8lh; margin-left:0.6lh");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 1.08rlh; margin-left:0.6rlh");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vw; margin-left:1vw");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vh;margin-left:1vh");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vi;margin-left:1vi");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size:2vi;margin-left:1vi;writing-mode:vertical-lr");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vb;margin-left:1vb");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vb;margin-left:1vb;writing-mode:vertical-lr");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vmin;margin-left:1vmin");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: 2vmax;margin-left:1vmax");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: calc(110% - 0.1vw);margin-left:max(1em,1rem)");
		style = elm.getComputedStyle(null);
		assertEquals(14.81f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(8.28f, box.getPaddingLeft(), 0.01f);
		assertEquals(29.61f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(44.415f, box.getMarginBottom(), 0.01f);
		assertEquals(14.81f, box.getMarginLeft(), 0.01f);
		assertEquals(651.915f, box.getWidth(), 0.01f);
		//
		elm.getOverrideStyle(null).setCssText("font-size: max(110%,1.4rem);margin-left:calc(1em - 0.3rem)");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font-size: var(--foo,1.4rem);margin-left:var(--bar,0.9rem)");
		style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null)
				.setCssText("font-size: max(110%,var(--foo,1.4rem));margin-left:calc(1em - var(--bar,0.3rem))");
		style = elm.getComputedStyle(null);
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
	public void getComputedStyleFontSizeInheritInherit() throws CSSMediaException {
		CSSElement html = xhtmlDoc.getDocumentElement();
		assertNotNull(html);
		html.getOverrideStyle(null).setCssText("font-size:inherit");
		CSSElement elm = (CSSElement) html.getElementsByTagName("body").item(0);
		elm.getOverrideStyle(null).setCssText("font-size:120%;");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("14.4pt", style.getPropertyValue("font-size"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(html));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(html));
	}

	@Test
	public void getComputedStyleFontSizeVarInherit() throws CSSMediaException {
		CSSElement html = xhtmlDoc.getDocumentElement();
		assertNotNull(html);
		html.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:inherit");
		CSSElement elm = (CSSElement) html.getElementsByTagName("body").item(0);
		elm.getOverrideStyle(null).setCssText("font-size:120%;");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("14.4pt", style.getPropertyValue("font-size"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(html));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(html));
	}

	@Test
	public void getComputedStyleFontSizePcntInheritPcnt() throws CSSMediaException {
		CSSElement html = xhtmlDoc.getDocumentElement();
		assertNotNull(html);
		html.getOverrideStyle(null).setCssText("font-size:120%;");
		CSSElement body = (CSSElement) html.getElementsByTagName("body").item(0);
		body.getOverrideStyle(null).setCssText("font-size:inherit");
		CSSElement elm = xhtmlDoc.getElementById("h1");
		elm.getOverrideStyle(null).setCssText("font-size:140%;");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("20.16pt", style.getPropertyValue("font-size"));
	}

	@Test
	public void getComputedStyleCalc() {
		CSSElement elm = xhtmlDoc.getElementById("listpara");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
		assertEquals(
				"display: block; margin-top: 14.4pt; margin-bottom: 14.4pt; unicode-bidi: embed; margin-right: 1%; font-family: Arial; padding-left: calc(10% - 36pt - 12pt); font-size: 14.4pt; font-style: normal; font-weight: normal; font-stretch: normal; line-height: normal; font-variant-caps: normal; font-size-adjust: none; font-kerning: auto; font-variant-ligatures: normal; font-variant-position: normal; font-variant-numeric: normal; font-variant-alternates: normal; font-variant-east-asian: normal; ",
				style.getCssText());
		assertEquals(
				"display:block;margin-bottom:14.4pt;margin-right:1%;margin-top:14.4pt;unicode-bidi:embed;font:14.4pt Arial;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		//
		elm.getOverrideStyle(null).setCssText("foo:sin(90deg/2);bar:sin(30deg)");
		style = elm.getComputedStyle(null);
		assertEquals("0.5", style.getPropertyValue("bar"));
		ExtendedCSSValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(0.70710677f, ((CSSPrimitiveValue) val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-6);
		/*
		 * Serialize (computed) centimeters
		 */
		elm.getOverrideStyle(null).setCssText("height: calc(2 * 0.0001102cm)");
		style = elm.getComputedStyle(null);
		val = style.getPropertyCSSValue("height");
		assertNotNull(val); // result is 0.00833px
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(0.0002204f, ((CSSPrimitiveValue) val).getFloatValue(CSSPrimitiveValue.CSS_CM), 1e-6);
		assertEquals("0.0002cm", val.getCssText());
		assertEquals(".0002cm", val.getMinifiedCssText(""));
		//
		elm.getOverrideStyle(null).setCssText("height: calc(2 * 0.0000662cm)");
		style = elm.getComputedStyle(null);
		val = style.getPropertyCSSValue("height");
		assertNotNull(val);
		// result is 0.00500px, browsers often serialize rounding to 0.01px
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(1.324e-4, ((CSSPrimitiveValue) val).getFloatValue(CSSPrimitiveValue.CSS_CM), 1e-6);
		assertEquals("0.0001cm", val.getCssText());
		assertEquals(".0001cm", val.getMinifiedCssText(""));
		//
		elm.getOverrideStyle(null).setCssText("height: calc(2 * 0.0110312cm)");
		style = elm.getComputedStyle(null);
		val = style.getPropertyCSSValue("height");
		assertNotNull(val); // result is 0.834px
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(0.0220624f, ((CSSPrimitiveValue) val).getFloatValue(CSSPrimitiveValue.CSS_CM), 1e-6);
		assertEquals("0.0221cm", val.getCssText());
		assertEquals(".0221cm", val.getMinifiedCssText(""));
	}

	@Test
	public void getComputedStyleUnset() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		ExtendedCSSValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("36pt", val.getCssText());
		// Check 'unset'
		elm.getOverrideStyle(null).setProperty("font-size", "unset", "");
		style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
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
		style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("url('http://www.example.com/headerbg.png')", val.getCssText());
		elm.getOverrideStyle(null).setCssText("background: url('override.png')");
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals("url('http://www.example.com/override.png')", val.getCssText());
		// Check 'unset'
		elm.getOverrideStyle(null).setProperty("background-image", "unset", "");
		style = elm.getComputedStyle(null);
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
		CSSComputedProperties style = elm.getComputedStyle(null);
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
		elm.getOverrideStyle(null).setCssText(
				"background-image: url('img1.png'), url('img2.png'); background-repeat: repeat no-repeat, round, space; background-position: left center, 10% 5%; background-clip: padding-box; background-attachment: fixed, local, local, scroll");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-repeat");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		CSSValueList list = (CSSValueList) val;
		assertEquals(3, list.getLength());
		list = (CSSValueList) ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image",
				"background-repeat", (StyleValue) val);
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
				(StyleValue) val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("padding-box", list.item(0).getCssText());
		assertEquals("padding-box", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-attachment");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-attachment",
				(StyleValue) val);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		list = (CSSValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("fixed", list.item(0).getCssText());
		assertEquals("local", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-size");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-size",
				(StyleValue) val);
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
		elm.getOverrideStyle(null).setCssText(
				"transition-property: background-color, opacity, width, height; transition-duration: 1s, 10s, 3s; transition-timing-function: ease, linear, cubic-bezier(0.33, 0.1, 0.5, 1); transition-delay: 2s, 1s");
		CSSComputedProperties style = elm.getComputedStyle(null);
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
				"transition-duration", (StyleValue) val);
		assertEquals(4, list.getLength());
		assertEquals("1s", list.item(0).getCssText());
		assertEquals("10s", list.item(1).getCssText());
		assertEquals("3s", list.item(2).getCssText());
		assertEquals("1s", list.item(3).getCssText());
		val = style.getPropertyCSSValue("transition-timing-function");
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("transition-property",
				"transition-timing-function", (StyleValue) val);
		assertEquals("ease, linear, cubic-bezier(0.33, 0.1, 0.5, 1), ease", val.getCssText());
		val = style.getPropertyCSSValue("transition-delay");
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("transition-property", "transition-delay",
				(StyleValue) val);
		assertEquals("2s, 1s, 2s, 1s", val.getCssText());
	}

	/*
	 * Shorthand serialization
	 */
	@Test
	public void getComputedStyleShorthand() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("24.12pt 0", style.getPropertyValue("margin"));
		//
		elm.getOverrideStyle(null).setCssText("padding: 0.4em 1.8em");
		style = elm.getComputedStyle(null);
		assertEquals("14.4pt 64.8pt", style.getPropertyValue("padding"));
		//
		elm.getOverrideStyle(null).setCssText("font:bold 83%/116% Arial");
		style = elm.getComputedStyle(null);
		assertEquals("bold 9.96pt/116% Arial", style.getPropertyValue("font"));
		//
		elm.getOverrideStyle(null).setCssText(
				"background:url('a.png') no-repeat,url('b.png') center/100% 100% no-repeat,url('c.png') white;");
		style = elm.getComputedStyle(null);
		assertEquals("url('a.png') no-repeat,url('b.png') center/100% 100% no-repeat,url('c.png') #fff",
				style.getPropertyValue("background"));
		//
		elm.getOverrideStyle(null).setCssText("list-style:inside square url('foo.png')");
		style = elm.getComputedStyle(null);
		assertEquals("inside square url('foo.png')", style.getPropertyValue("list-style"));
		//
		elm.getOverrideStyle(null).setCssText("text-decoration:blink dashed magenta");
		style = elm.getComputedStyle(null);
		assertEquals("blink dashed #f0f", style.getPropertyValue("text-decoration"));
		//
		elm.getOverrideStyle(null).setCssText("flex:2 2 0.1pt");
		style = elm.getComputedStyle(null);
		assertEquals("2 2 0.1pt", style.getPropertyValue("flex"));
		//
		elm.getOverrideStyle(null).setCssText("grid:auto 1fr/auto 1fr auto;");
		style = elm.getComputedStyle(null);
		assertEquals("auto 1fr/auto 1fr auto", style.getPropertyValue("grid"));
		//
		elm.getOverrideStyle(null).setCssText("animation:3500ms 5s reverse 'my anim'");
		style = elm.getComputedStyle(null);
		assertEquals("3500ms 5s reverse 'my anim'", style.getPropertyValue("animation"));
		//
		elm.getOverrideStyle(null).setCssText("transition:margin-top 3500ms cubic-bezier(.05,.69,.95,.6) 5s");
		style = elm.getComputedStyle(null);
		assertEquals("margin-top 3500ms cubic-bezier(.05,.69,.95,.6) 5s", style.getPropertyValue("transition"));
		//
		elm.getOverrideStyle(null).setCssText("place-content:last baseline right");
		style = elm.getComputedStyle(null);
		assertEquals("last baseline right", style.getPropertyValue("place-content"));
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
							return (CSSStyleRule) rule;
						}
						break;
					}
				}
			}
		}
		return null;
	}

}
