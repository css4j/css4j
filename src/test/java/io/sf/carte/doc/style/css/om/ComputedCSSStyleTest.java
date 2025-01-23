/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

public class ComputedCSSStyleTest {

	static CSSStyleSheet sheet;

	static Document refXhtmlDoc;

	CSSDocument xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws IOException, DocumentException {
		sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		refXhtmlDoc = DOMCSSStyleSheetFactoryTest.plainDocumentFromStream(
				DOMCSSStyleSheetFactoryTest.sampleHTMLStream(),
				MockURLConnectionFactory.SAMPLE_URL);
		//refXhtmlDoc = TestDOMImplementation.sampleHTMLDocument();
	}

	@BeforeEach
	public void setUp() throws IOException, DocumentException {
		xhtmlDoc = DOMCSSStyleSheetFactoryTest.getFactoryWithUASheet()
				.createCSSDocument((Document) refXhtmlDoc.cloneNode(true));
		//xhtmlDoc = (CSSDocument) refXhtmlDoc.cloneNode(true);
	}

	@Test
	public void testGeneric() {
		Node node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSElement);
		CSSElement body = (CSSElement) node;
		CSSComputedProperties style = body.getComputedStyle(null);
		assertNotNull(style);
		assertNull(((BaseCSSStyleDeclaration) style).getParentRule());
		//
		assertEquals("", style.getPropertyPriority("display"));
		//
		body.getOverrideStyle(null).setCssText("font: 1.8ex Serif");
		style = body.getComputedStyle(null);
		assertEquals("", style.getPropertyPriority("font-size"));
		assertEquals("", style.getPropertyPriority("font"));
		assertEquals("1.8ex", style.getPropertyValue("font-size"));
		assertEquals("Serif", style.getPropertyValue("font-family"));
		assertEquals("1.8ex Serif", style.getPropertyValue("font"));
		assertEquals(10.8f, style.getComputedFontSize(), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		//
		body.getOverrideStyle(null).setCssText("font: 1.8ex Serif!important");
		assertEquals("font:1.8ex Serif!important;", body.getOverrideStyle(null).getMinifiedCssText());
		style = body.getComputedStyle(null);
		assertEquals("important", style.getPropertyPriority("font-size"));
		assertEquals("important", style.getPropertyPriority("font"));
		assertEquals("1.8ex", style.getPropertyValue("font-size"));
		assertEquals("Serif", style.getPropertyValue("font-family"));
		assertEquals("1.8ex Serif", style.getPropertyValue("font"));
		assertEquals(10.8f, style.getComputedFontSize(), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
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
	public void getComputedFontSize() {
		CSSElement elm = xhtmlDoc.getElementById("tablepara");
		elm = (CSSElement) elm.getElementsByTagName("span").item(0);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(1.5f * 12f, style.getComputedFontSize(), 0.001);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getFontSizeMedia() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("span1");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(15f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		CSSElement para = xhtmlDoc.getElementById("para2");
		CSSComputedProperties stylePara = xhtmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertNotNull(stylePara);
		assertEquals(12f, stylePara.getComputedFontSize(), 1e-5);

		xhtmlDoc.setTargetMedium("screen");
		assertEquals("screen", xhtmlDoc.getStyleSheet().getTargetMedium());
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(20f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		stylePara = xhtmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertEquals(16f, stylePara.getComputedFontSize(), 1e-5);
		xhtmlDoc.setTargetMedium("all");
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getColor() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("#000080", style.getCSSColor().getCssText());
		assertEquals(0,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(128f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
	}

	@Test
	public void getColorCalc() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		elm.getOverrideStyle(null).setCssText("color: rgb(calc(2*102) 37 120)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("#cc2578", style.getCSSColor().getCssText());
		assertEquals(204f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getRed()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(37f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getGreen()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(120f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
	}

	@Test
	public void getColorCalcPcnt() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		elm.getOverrideStyle(null).setCssText("color: rgb(calc(2*12%) 37% 67%)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("rgb(24% 37% 67%)", style.getCSSColor().getCssText());
		assertEquals(24f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getRed()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5);
		assertEquals(37f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getGreen()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5);
		assertEquals(67f,
				((CSSTypedValue) style.getCSSColor().toRGBColor().getBlue()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				1e-5);
	}

	@Test
	public void testGetContentFromStyleElement() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("  foo  bar  ", style.getPropertyValue("content"));
	}

	@Test
	public void getPropertyCSSValueForBorderWidth() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("0", style.getPropertyValue("border-top-width"));
		CSSValue cssval = style.getPropertyCSSValue("border-top-width");
		assertNotNull(cssval);
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(Type.NUMERIC, cssval.getPrimitiveType());
		CSSTypedValue primi = (CSSTypedValue) cssval;
		assertEquals(CSSUnit.CSS_NUMBER, primi.getUnitType());
		assertEquals(0, primi.getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
	}

	@Test
	public void testBoxModel() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		BoxValues box = style.getBoxValues(CSSUnit.CSS_PX);
		assertNotNull(box);

		assertEquals(CSSUnit.CSS_PX, box.getUnitType());

		assertEquals(900f, box.getWidth(), 0.01f);

		assertEquals(28.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(28.8f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);

		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);

		assertEquals(0f, box.getBorderTopWidth(), 0.01f);
		assertEquals(0f, box.getBorderRightWidth(), 0.01f);
		assertEquals(0f, box.getBorderBottomWidth(), 0.01f);
		assertEquals(0f, box.getBorderLeftWidth(), 0.01f);

		box.fillBoxValues(style);
		assertEquals(
			"display:block;font-family:'Does Not Exist',Neither;font-size:21.6pt;font-weight:bold;"
				+ "unicode-bidi:embed;border-style:none;border-width:0;content:\"  foo  bar  \";"
				+ "color:#000080;margin:28.8px 0;padding:0;width:900px;",
			style.getMinifiedCssText());
	}

	@Test
	public void nonexistentTag() {
		CSSElement elm = xhtmlDoc.getElementById("listpara");
		elm = (CSSElement) elm.getElementsByTagName("nonexistenttag").item(0);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(12f, style.getComputedFontSize(), 0.001);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
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
	public void getComputedStyle() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		HashMap<String, CSSPropertyValueException> warnings = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler())
				.getComputedStyleWarnings(elm);
		assertNotNull(warnings);
		assertEquals(1, warnings.size());
		assertEquals("padding-left", warnings.keySet().iterator().next());
		assertEquals("calc(10% - 3em - max(6px, 1em))", warnings.values().iterator().next().getValueText());
		// Box values
		BoxValues box = style.getBoxValues(CSSUnit.CSS_PT);
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
				"display: block; unicode-bidi: embed; margin-top: 28.8pt; margin-bottom: 43.2pt; background-position: 20% 0%; padding-left: calc(10% - 43.2pt - 14.4pt); font-size: 14.4pt; font-family: Arial; font-style: normal; font-weight: normal; font-stretch: normal; line-height: normal; font-variant-caps: normal; font-size-adjust: none; font-kerning: auto; font-optical-sizing: auto; font-feature-settings: normal; font-variation-settings: normal; font-variant-ligatures: normal; font-variant-position: normal; font-variant-numeric: normal; font-variant-alternates: normal; font-variant-east-asian: normal; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:43.2pt;margin-top:28.8pt;background-position:20% 0%;padding-left:calc(10% - 43.2pt - 14.4pt);font:14.4pt Arial;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		CSSElement docelm = xhtmlDoc.getDocumentElement();
		// Set explicit document width for box model
		docelm.getOverrideStyle(null).setCssText("width:675pt");
		CSSElement body = (CSSElement) docelm.getElementsByTagName("body").item(0);
		style = body.getComputedStyle(null);
		assertEquals(12f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(36f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(48f, box.getMarginBottom(), 0.01f);
		assertEquals(33.75f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * Relative % font, box model.
		 */
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
		assertEquals(14.4f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(9.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(28.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(43.2f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(665.1f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * Relative 'ex' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 1.8ex; margin-left:1ex");
		style = elm.getComputedStyle(null);
		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(24.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(21.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(32.4f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(645.3f, box.getWidth(), 0.01f);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Relative 'rem' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 2rem; margin-left:1rem");
		style = elm.getComputedStyle(null);
		assertEquals(24f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Relative 'rex' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 1.8rex; margin-left:1rex");
		style = elm.getComputedStyle(null);
		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(24.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(21.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(32.4f, box.getMarginBottom(), 0.01f);
		assertEquals(6f, box.getMarginLeft(), 0.01f);
		assertEquals(644.7f, box.getWidth(), 0.01f);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(6f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Relative 'lh' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 0.8lh; margin-left:0.6lh");
		style = elm.getComputedStyle(null);
		assertEquals(11.14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Relative 'rlh' unit, box model.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 1.08rlh; margin-left:0.6rlh");
		style = elm.getComputedStyle(null);
		assertEquals(15.03f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * 'vw' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vw");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSUnit.CSS_VW), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler())
				.getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		// Error due to inability to convert unit (no medium set).
		assertEquals("margin-left", errors.keySet().iterator().next());
		CSSPropertyValueException ex = errors.values().iterator().next();
		assertEquals("1vw", ex.getValueText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
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
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSUnit.CSS_CAP), 1e-5f);
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSUnit.CSS_CAP), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(9.6f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * Relative 'ch' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:3ch;margin-left:1.5ch");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(3f, fontSize.getFloatValue(CSSUnit.CSS_CH), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(3.375f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(3f, fontSize.getFloatValue(CSSUnit.CSS_CH), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(6.75f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * Relative 'rch' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:2.5rch;margin-left:1.5rch");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(2.5f, fontSize.getFloatValue(CSSUnit.CSS_RCH), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(4.5f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(2.5f, fontSize.getFloatValue(CSSUnit.CSS_RCH), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(7.5f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * Relative 'ic' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:0.8ic;margin-left:1.5ic");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSUnit.CSS_IC), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(14.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSUnit.CSS_IC), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(7.68f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		/*
		 * Relative 'ric' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:0.8ric;margin-left:1.2ric");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSUnit.CSS_RIC), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(14.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(0.8f, fontSize.getFloatValue(CSSUnit.CSS_RIC), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(9.6f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		/*
		 * 'vh' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vh");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSUnit.CSS_VH), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vh; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vh;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * 'vi' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vi");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSUnit.CSS_VI), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vi; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vi;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * 'vb' unit.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:1vb");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSUnit.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vb;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * 'vb' unit, custom property fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1f, marginLeft.getFloatValue(CSSUnit.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:1vb;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		// Error due to inability to convert unit (no medium set).
		assertEquals("margin-left", errors.keySet().iterator().next());
		ex = errors.values().iterator().next();
		assertEquals("1vb", ex.getValueText());
		/*
		 * 'vb' unit, shorthand custom property fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,1.1vb)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(1.1f, marginLeft.getFloatValue(CSSUnit.CSS_VB), 0.01f);
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(1.1f, marginTop.getFloatValue(CSSUnit.CSS_VB), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-top: 1.1vb; margin-right: 1.1vb; margin-bottom: 1.1vb; margin-left: 1.1vb; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);margin:1.1vb;",
				style.getMinifiedCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * property: uppercase color identifier
		 */
		elm.getOverrideStyle(null).setCssText("color:BLUE");
		style = elm.getComputedStyle(null);
		CSSTypedValue color = (CSSTypedValue) style.getPropertyCSSValue("color");
		assertEquals(CSSValue.Type.COLOR, color.getPrimitiveType());
		RGBAColor rgb = color.toRGBColor();
		assertEquals("#00f", rgb.toString());
	}

	@Test
	public void getComputedStyleRootElement() {
		CSSElement elm = xhtmlDoc.getDocumentElement();
		assertNotNull(elm);

		// Set explicit document width for box model
		elm.setAttribute("style", "width:675pt");

		elm.getOverrideStyle(null).setCssText("font-size: 1.8ex; margin-left:1ex");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);

		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		BoxValues box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);

		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		/*
		 * Font-relative rem units
		 */
		elm.getOverrideStyle(null).setCssText("font-size: 0.9rem; margin-left:0.5rem");
		style = elm.getComputedStyle(null);
		assertNotNull(style);

		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);

		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();

		/*
		 * Font-relative rex units
		 */
		elm.getOverrideStyle(null).setCssText("font-size: 1.8rex; margin-left:1rex");
		style = elm.getComputedStyle(null);
		assertNotNull(style);

		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);

		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();

		/*
		 * Font-relative rlh units
		 */
		elm.getOverrideStyle(null).setCssText("font-size: 0.9rlh; margin-left:1rlh");
		style = elm.getComputedStyle(null);
		assertNotNull(style);

		assertEquals(12.96f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(15.0336f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);

		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15.0336f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		/*
		 * Font-relative rch units
		 */
		elm.getOverrideStyle(null).setCssText("font-size: 3.6rch; margin-left:2rch");
		style = elm.getComputedStyle(null);
		assertNotNull(style);

		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);

		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();

		/*
		 * Font-relative ric units
		 */
		elm.getOverrideStyle(null).setCssText("font-size: 0.9ric; margin-left:.5ric");
		style = elm.getComputedStyle(null);
		assertNotNull(style);

		assertEquals(10.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(5.4f, box.getMarginLeft(), 0.01f);
		assertEquals(675f, box.getWidth(), 0.01f);

		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(5.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
	}

	@Test
	public void getComputedStyleEnv() {
		/*
		 * env() value, fallback.
		 */
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("padding-left:env(safe-area-inset-left,0.8em)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue paddingLeft = (CSSTypedValue) style.getPropertyCSSValue("padding-left");
		assertEquals(9.6f, paddingLeft.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value in calc(), fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("padding-left:calc(env(safe-area-inset-left,0.8em)*2)");
		style = elm.getComputedStyle(null);
		paddingLeft = (CSSTypedValue) style.getPropertyCSSValue("padding-left");
		assertEquals(CSSValue.Type.NUMERIC, paddingLeft.getPrimitiveType());
		assertEquals(19.2f, paddingLeft.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleEnvError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * env() value set in parent.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("text-indent:env(foo-indent)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertEquals(0f, textIndent.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler())
				.getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("text-indent", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertNotNull(textIndent);
		assertEquals(0f, textIndent.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler())
				.getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("text-indent", errptyIt.next());
	}

	@Test
	public void getComputedStyleMediumScreenNoDb()
			throws IOException, CSSMediaException, DocumentException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(true);
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
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(Type.NUMERIC, fontSize.getPrimitiveType());
		assertEquals(CSSUnit.CSS_VW, fontSize.getUnitType());
		assertEquals(2f, fontSize.getFloatValue(CSSUnit.CSS_VW), 0.01f);
		assertEquals(14.4f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 57.6pt; margin-bottom: 86.4pt; background-position: 20% 0%; padding-left: calc(10% - 86.4pt - 28.8pt); font-size: 2vw; margin-left: 14.4pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:86.4pt;margin-left:14.4pt;margin-top:57.6pt;background-position:20% 0%;padding-left:calc(10% - 86.4pt - 28.8pt);font-size:2vw;",
				style.getMinifiedCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vh;margin-left:1vh");
		style = elm.getComputedStyle(null);
		assertEquals(16.2f, style.getComputedFontSize(), 0.01f);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(Type.NUMERIC, fontSize.getPrimitiveType());
		assertEquals(CSSUnit.CSS_VH, fontSize.getUnitType());
		assertEquals(2f, fontSize.getFloatValue(CSSUnit.CSS_VH), 0.01f);
		assertEquals(8.1f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleMediumPrintNoDb() throws IOException, DocumentException, CSSMediaException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(true);
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
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(Type.NUMERIC, fontSize.getPrimitiveType());
		assertEquals(CSSUnit.CSS_VW, fontSize.getUnitType());
		assertEquals(2f, fontSize.getFloatValue(CSSUnit.CSS_VW), 0.01f);
		assertEquals(5.95f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 2vw; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:2vw;",
				style.getMinifiedCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vh;margin-left:1vh");
		style = elm.getComputedStyle(null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(Type.NUMERIC, fontSize.getPrimitiveType());
		assertEquals(CSSUnit.CSS_VH, fontSize.getUnitType());
		assertEquals(2f, fontSize.getFloatValue(CSSUnit.CSS_VH), 0.01f);
		assertEquals(8.42f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleMediumScreen() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		// medium 'screen'
		xhtmlDoc.setTargetMedium("screen");
		/*
		 * Now the library uses a style database.
		 */
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		BoxValues box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(663.5f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(674.7f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2ex; margin-left:1ex");
		style = elm.getComputedStyle(null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(7f, box.getMarginLeft(), 0.01f);
		assertEquals(656.5f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 28pt; margin-bottom: 42pt; background-position: 20% 0%; padding-left: calc(10% - 42pt - 14pt); font-size: 14pt; margin-left: 7pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:42pt;margin-left:7pt;margin-top:28pt;background-position:20% 0%;padding-left:calc(10% - 42pt - 14pt);font-size:14pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 2rem; margin-left:1rem");
		style = elm.getComputedStyle(null);
		assertEquals(24f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0f, box.getPaddingLeft(), 0.01f);
		assertEquals(48f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(72f, box.getMarginBottom(), 0.01f);
		assertEquals(12f, box.getMarginLeft(), 0.01f);
		assertEquals(663f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 48pt; margin-bottom: 72pt; background-position: 20% 0%; padding-left: calc(10% - 72pt - 24pt); font-size: 24pt; margin-left: 12pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:72pt;margin-left:12pt;margin-top:48pt;background-position:20% 0%;padding-left:calc(10% - 72pt - 24pt);font-size:24pt;",
				style.getMinifiedCssText());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size: 0.8lh; margin-left:0.6lh");
		style = elm.getComputedStyle(null);
		assertEquals(12.99f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 1.08rlh; margin-left:0.6rlh");
		style = elm.getComputedStyle(null);
		assertEquals(15.03f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vw; margin-left:1vw");
		style = elm.getComputedStyle(null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vh;margin-left:1vh");
		style = elm.getComputedStyle(null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vi;margin-left:1vi");
		style = elm.getComputedStyle(null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size:2vi;margin-left:1vi;writing-mode:vertical-lr");
		style = elm.getComputedStyle(null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; writing-mode: vertical-lr; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;writing-mode:vertical-lr;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vb;margin-left:1vb");
		style = elm.getComputedStyle(null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vb;margin-left:1vb;writing-mode:vertical-lr");
		style = elm.getComputedStyle(null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; writing-mode: vertical-lr; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;writing-mode:vertical-lr;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vmin;margin-left:1vmin");
		style = elm.getComputedStyle(null);
		assertEquals(11.9f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(19.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(23.8f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(35.7f, box.getMarginBottom(), 0.01f);
		assertEquals(5.95f, box.getMarginLeft(), 0.01f);
		assertEquals(649.15f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 23.8pt; margin-bottom: 35.7pt; background-position: 20% 0%; padding-left: calc(10% - 35.7pt - 11.9pt); font-size: 11.9pt; margin-left: 5.95pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:35.7pt;margin-left:5.95pt;margin-top:23.8pt;background-position:20% 0%;padding-left:calc(10% - 35.7pt - 11.9pt);font-size:11.9pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: 2vmax;margin-left:1vmax");
		style = elm.getComputedStyle(null);
		assertEquals(16.84f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.14f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.68f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.52f, box.getMarginBottom(), 0.01f);
		assertEquals(8.42f, box.getMarginLeft(), 0.01f);
		assertEquals(666.44f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 33.68pt; margin-bottom: 50.52pt; background-position: 20% 0%; padding-left: calc(10% - 50.52pt - 16.84pt); font-size: 16.84pt; margin-left: 8.42pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:50.52pt;margin-left:8.42pt;margin-top:33.68pt;background-position:20% 0%;padding-left:calc(10% - 50.52pt - 16.84pt);font-size:16.84pt;",
				style.getMinifiedCssText());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: calc(110% - 0.1vw);margin-left:max(1em,1rem)");
		style = elm.getComputedStyle(null);
		assertEquals(14.81f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(8.28f, box.getPaddingLeft(), 0.01f);
		assertEquals(29.61f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(44.415f, box.getMarginBottom(), 0.01f);
		assertEquals(14.81f, box.getMarginLeft(), 0.01f);
		assertEquals(651.915f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: max(110%,1.4rem);margin-left:calc(1em - 0.3rem)");
		style = elm.getComputedStyle(null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(13.2f, box.getMarginLeft(), 0.01f);
		assertEquals(661.5f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font-size: var(--foo,1.4rem);margin-left:var(--bar,0.9rem)");
		style = elm.getComputedStyle(null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(10.8f, box.getMarginLeft(), 0.01f);
		assertEquals(663.9f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null).setCssText("font: var(--foo,1.1rem) Verdana;margin:var(--bar,0.6rem 0.8rem)");
		style = elm.getComputedStyle(null);
		assertEquals(13.2f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(14.7f, box.getPaddingLeft(), 0.01f);
		assertEquals(7.2f, box.getMarginTop(), 0.01f);
		assertEquals(9.6f, box.getMarginRight(), 0.01f);
		assertEquals(7.2f, box.getMarginBottom(), 0.01f);
		assertEquals(9.6f, box.getMarginLeft(), 0.01f);
		assertEquals(641.1f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null)
				.setCssText("font-size: max(110%,var(--foo,1.4rem));margin-left:calc(1em - var(--bar,0.3rem))");
		style = elm.getComputedStyle(null);
		assertEquals(16.8f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(0.3f, box.getPaddingLeft(), 0.01f);
		assertEquals(33.6f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(50.4f, box.getMarginBottom(), 0.01f);
		assertEquals(13.2f, box.getMarginLeft(), 0.01f);
		assertEquals(661.5f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		elm.getOverrideStyle(null)
				.setCssText("font: max(110%,var(--foo,1.1rem)) Helvetica;margin:calc(1em - var(--bar,0.2rem))");
		style = elm.getComputedStyle(null);
		assertEquals(15.4f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(5.9f, box.getPaddingLeft(), 0.01f);
		assertEquals(13f, box.getMarginTop(), 0.01f);
		assertEquals(13f, box.getMarginRight(), 0.01f);
		assertEquals(13f, box.getMarginBottom(), 0.01f);
		assertEquals(13f, box.getMarginLeft(), 0.01f);
		assertEquals(643.1f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		// Longhand var replacement error
		elm.getOverrideStyle(null)
				.setCssText("margin-left:calc(1em - var(--bar,0.2rem 0.3rem))");
		style = elm.getComputedStyle(null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(663.5f, box.getWidth(), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		// Font-size longhand var replacement error
		elm.getOverrideStyle(null)
				.setCssText("font-size: calc(110% * var(--foo,1.1rem Helvetica))");
		style = elm.getComputedStyle(null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(663.5f, box.getWidth(), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		// Shorthand var replacement error
		elm.getOverrideStyle(null)
				.setCssText("margin:calc(1em - var(--bar,0.2rem 0.3rem))");
		style = elm.getComputedStyle(null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(0f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(0f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(663.5f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		// Font shorthand var replacement error
		elm.getOverrideStyle(null)
				.setCssText("font: calc(110% *var(--foo,1.1rem Helvetica))");
		style = elm.getComputedStyle(null);
		assertEquals(14f, style.getComputedFontSize(), 0.01f);
		box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(0f, box.getPaddingTop(), 0.01f);
		assertEquals(0f, box.getPaddingRight(), 0.01f);
		assertEquals(0f, box.getPaddingBottom(), 0.01f);
		assertEquals(11.5f, box.getPaddingLeft(), 0.01f);
		assertEquals(28f, box.getMarginTop(), 0.01f);
		assertEquals(0f, box.getMarginRight(), 0.01f);
		assertEquals(42f, box.getMarginBottom(), 0.01f);
		assertEquals(0f, box.getMarginLeft(), 0.01f);
		assertEquals(663.5f, box.getWidth(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("padding-top:env(safe-area-inset-top)");
		style = elm.getComputedStyle(null);
		CSSTypedValue paddingTop = (CSSTypedValue) style.getPropertyCSSValue("padding-top");
		assertEquals(20f, paddingTop.getFloatValue(CSSUnit.CSS_PX), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value substitution, unused fallback.
		 */
		elm.getOverrideStyle(null).setCssText("padding-top:env(safe-area-inset-top,1vb)");
		style = elm.getComputedStyle(null);
		paddingTop = (CSSTypedValue) style.getPropertyCSSValue("padding-top");
		assertEquals(20f, paddingTop.getFloatValue(CSSUnit.CSS_PX), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value substitution inside calc().
		 */
		elm.getOverrideStyle(null).setCssText("padding-top:calc(env(safe-area-inset-top)/2)");
		style = elm.getComputedStyle(null);
		paddingTop = (CSSTypedValue) style.getPropertyCSSValue("padding-top");
		assertEquals(10f, paddingTop.getFloatValue(CSSUnit.CSS_PX), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("padding-left:env(safe-area-inset-left,0.8em)");
		style = elm.getComputedStyle(null);
		CSSTypedValue paddingLeft = (CSSTypedValue) style.getPropertyCSSValue("padding-left");
		assertEquals(11.2f, paddingLeft.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value in calc(), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("padding-left:calc(env(safe-area-inset-left,0.8em)*2)");
		style = elm.getComputedStyle(null);
		paddingLeft = (CSSTypedValue) style.getPropertyCSSValue("padding-left");
		assertEquals(CSSValue.Type.NUMERIC, paddingLeft.getPrimitiveType());
		assertEquals(22.4f, paddingLeft.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value substitution, font-size.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:env(safe-area-inset-top)");
		style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(20f, fontSize.getFloatValue(CSSUnit.CSS_PX), 1e-6f);
		assertEquals(15f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value substitution inside calc(), font-size.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:calc(env(safe-area-inset-top)*2)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(40f, fontSize.getFloatValue(CSSUnit.CSS_PX), 1e-6f);
		assertEquals(30f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * env() value, fallback, font-size.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:env(safe-area-inset-left,0.8em)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(11.2f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		assertEquals(11.2f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleFontSize() {
		/*
		 * font-size: smaller
		 */
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("font-size:smaller");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(9.84f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(9.84f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font-size: larger
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:larger");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(14.4f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(14.4f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font-size: small, x-small, etc.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:small");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals("small", fontSize.getStringValue());
		assertEquals(10f, style.getComputedFontSize(), 1e-5f);
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:smaller");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals("x-small", fontSize.getStringValue());
		assertEquals(9f, style.getComputedFontSize(), 1e-5f);
		listpara.getOverrideStyle(null).setCssText("font-size:larger");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals("medium", fontSize.getStringValue());
		assertEquals(12f, style.getComputedFontSize(), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font-size: identifier inheritance.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:large");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(CSSValue.Type.IDENT, fontSize.getPrimitiveType());
		assertEquals("large", fontSize.getStringValue());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:42pt;margin-top:28pt;background-position:20% 0%;padding-left:calc(10% - 42pt - 14pt);font-size:large;",
				style.getMinifiedCssText());
		listpara.getOverrideStyle(null).setCssText("font-size:66%");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(Type.NUMERIC, fontSize.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PT, fontSize.getUnitType());
		assertEquals(9.24f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(9.24f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display:block;margin-bottom:9.24pt;margin-right:1%;margin-top:9.24pt;unicode-bidi:embed;font-family:'Does Not Exist',Neither,Helvetica;font-size:9.24pt;padding-left:calc(10% - 42pt - 14pt);",
				style.getMinifiedCssText());
		/*
		 * font-size: 3-level identifier inheritance. Requires state from previous test!
		 */
		CSSElement nonextag = (CSSElement) elm.getElementsByTagName("nonexistenttag").item(0);
		nonextag.getOverrideStyle(null).setCssText("font-size:0.85em");
		style = nonextag.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(Type.NUMERIC, fontSize.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PT, fontSize.getUnitType());
		assertEquals(7.85f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(7.85f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals("font-size:7.85pt;", style.getMinifiedCssText());
	}

	@Test
	public void getComputedStyleFontSizePcntInherit() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		assertNotNull(html);
		html.getOverrideStyle(null).setCssText("font-size: 120%;");
		CSSElement body = (CSSElement) html.getElementsByTagName("body").item(0);
		body.getOverrideStyle(null).setCssText("font-size: inherit");
		CSSElement div = xhtmlDoc.getElementById("div1");
		div.getOverrideStyle(null).setCssText("font-size: inherit;");
		CSSElement ul = xhtmlDoc.getElementById("ul1");
		ul.getOverrideStyle(null).setCssText("font-size: inherit;");
		CSSElement elm = xhtmlDoc.getElementById("inflink");
		CSSElement li = (CSSElement) elm.getParentNode();
		li.getOverrideStyle(null).setCssText("font-size: inherit;");
		elm.getOverrideStyle(null).setCssText("font-size: inherit;");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("14.4pt", style.getPropertyValue("font-size"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(14.4f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleFontSizeInheritInherit() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		assertNotNull(html);
		html.getOverrideStyle(null).setCssText("font-size:inherit");
		CSSElement elm = (CSSElement) html.getElementsByTagName("body").item(0);
		elm.getOverrideStyle(null).setCssText("font-size:120%;");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("14.4pt", style.getPropertyValue("font-size"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(14.4f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleFontSizePcntInheritPcnt() {
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(20.16f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
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
		BoxValues box = style.getBoxValues(CSSUnit.CSS_PT);
		assertEquals(19.5f, box.getPaddingLeft(), 0.01f);
		elm.getOverrideStyle(null).setCssText("font: 120% Arial");
		style = elm.getComputedStyle(null);
		assertEquals(
				"display: block; margin-top: 14.4pt; margin-bottom: 14.4pt; unicode-bidi: embed; margin-right: 1%; font-family: Arial; padding-left: calc(10% - 36pt - 12pt); font-size: 14.4pt; font-style: normal; font-weight: normal; font-stretch: normal; line-height: normal; font-variant-caps: normal; font-size-adjust: none; font-kerning: auto; font-optical-sizing: auto; font-feature-settings: normal; font-variation-settings: normal; font-variant-ligatures: normal; font-variant-position: normal; font-variant-numeric: normal; font-variant-alternates: normal; font-variant-east-asian: normal; ",
				style.getCssText());
		assertEquals(
				"display:block;margin-bottom:14.4pt;margin-right:1%;margin-top:14.4pt;unicode-bidi:embed;font:14.4pt Arial;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		assertEquals("calc(10% - 36pt - 12pt)", style.getPropertyValue("padding-left"));
		//
		elm.getOverrideStyle(null).setCssText("foo:sin(90deg/2);bar:sin(30deg)");
		style = elm.getComputedStyle(null);
		assertEquals("0.5", style.getPropertyValue("bar"));
		CSSValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(0.70710677f, ((CSSTypedValue) val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		/*
		 * Serialize (computed) centimeters
		 */
		elm.getOverrideStyle(null).setCssText("height: calc(2 * 0.0001102cm)");
		style = elm.getComputedStyle(null);
		val = style.getPropertyCSSValue("height");
		assertNotNull(val); // result is 0.00833px
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(0.0002204f, ((CSSTypedValue) val).getFloatValue(CSSUnit.CSS_CM), 1e-6f);
		assertEquals("0.0002cm", val.getCssText());
		assertEquals(".0002cm", val.getMinifiedCssText(""));
		//
		elm.getOverrideStyle(null).setCssText("height: calc(2 * 0.0000662cm)");
		style = elm.getComputedStyle(null);
		val = style.getPropertyCSSValue("height");
		assertNotNull(val);
		// result is 0.00500px, browsers often serialize rounding to 0.01px
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(1.324e-4, ((CSSTypedValue) val).getFloatValue(CSSUnit.CSS_CM), 1e-6f);
		assertEquals("0.0001cm", val.getCssText());
		assertEquals(".0001cm", val.getMinifiedCssText(""));
		//
		elm.getOverrideStyle(null).setCssText("height: calc(2 * 0.0110312cm)");
		style = elm.getComputedStyle(null);
		val = style.getPropertyCSSValue("height");
		assertNotNull(val); // result is 0.834px
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(0.0220624f, ((CSSTypedValue) val).getFloatValue(CSSUnit.CSS_CM), 1e-6f);
		assertEquals("0.0221cm", val.getCssText());
		assertEquals(".0221cm", val.getMinifiedCssText(""));
	}

	@Test
	public void getComputedStyleKeywords() {
		/*
		 * property: initial
		 */
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("font-size:initial;margin-left:initial");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSValue.Type.IDENT, fontSize.getPrimitiveType());
		assertEquals("medium", fontSize.getStringValue());
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:0;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);font-size:medium;",
				style.getMinifiedCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(12f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * property: unset
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:unset;margin-left:unset");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(12f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:0;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);font-size:12pt;",
				style.getMinifiedCssText());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:unset;margin-left:unset");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		elm.getOverrideStyle(null).setCssText("font-size:16pt;margin-left:8pt");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(16f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(16f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(
				"display:block;margin:16pt 1% 16pt 0;unicode-bidi:embed;font-family:'Does Not Exist',Neither,Helvetica;font-size:16pt;padding-left:calc(10% - 48pt - 16pt);",
				style.getMinifiedCssText());
	}

	@Test
	public void getComputedStyleUnset() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals("36pt", val.getCssText());
		assertEquals(36f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		// Check 'unset'
		elm.getOverrideStyle(null).setProperty("font-size", "unset", "");
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals("12pt", val.getCssText());
		assertEquals(12f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleForBackgroundImages() {
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals("url('http://www.example.com/css/background.png')", val.getCssText());
		assertEquals("http://www.example.com/css/background.png", ((CSSTypedValue) val).getStringValue());
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
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
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
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals("url('http://www.example.com/headerbg.png')", val.getCssText());
		elm.getOverrideStyle(null).setCssText("background: url('override.png')");
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals("url('http://www.example.com/override.png')", val.getCssText());
		// Check 'unset'
		elm.getOverrideStyle(null).setProperty("background-image", "unset", "");
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
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
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
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
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
		assertEquals(3, list.getLength());
		list = (ValueList) ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image",
				"background-repeat", (StyleValue) val);
		assertEquals(2, list.getLength());
		assertEquals("repeat no-repeat", list.item(0).getCssText());
		assertEquals("round round", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-position");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());
		list = (ValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("left center", list.item(0).getCssText());
		assertEquals("10% 5%", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-clip");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-clip",
				(StyleValue) val);
		assertEquals(CssType.LIST, val.getCssValueType());
		list = (ValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("padding-box", list.item(0).getCssText());
		assertEquals("padding-box", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-attachment");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-attachment",
				(StyleValue) val);
		assertEquals(CssType.LIST, val.getCssValueType());
		list = (ValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("fixed", list.item(0).getCssText());
		assertEquals("local", list.item(1).getCssText());
		val = style.getPropertyCSSValue("background-size");
		assertNotNull(val);
		val = ((BaseCSSStyleDeclaration) style).computeBoundProperty("background-image", "background-size",
				(StyleValue) val);
		assertEquals(CssType.LIST, val.getCssValueType());
		list = (ValueList) val;
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
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
		assertEquals(4, list.getLength());
		assertEquals("background-color", list.item(0).getCssText());
		assertEquals("opacity", list.item(1).getCssText());
		assertEquals("background-color, opacity, width, height", style.getPropertyValue("transition-property"));
		val = style.getPropertyCSSValue("transition-duration");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());
		list = (ValueList) val;
		assertEquals(3, list.getLength());
		list = (ValueList) ((BaseCSSStyleDeclaration) style).computeBoundProperty("transition-property",
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
	public void getComputedStyleShorthand() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("24.12pt 0", style.getPropertyValue("margin"));
		assertEquals("", style.getPropertyPriority("margin"));
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
		assertEquals("2 2 .1pt", style.getPropertyValue("flex"));
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

	@Test
	public void getComputedStyleColorMix() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		elm.getOverrideStyle(null)
				.setCssText("color: color-mix(in HWB, hwb(60.8 26% 24%) 41%, hwb(90.3 40% 31%))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("hwb(78.205 34.26% 28.13%)", style.getPropertyValue("color"));

		// Attr method, %
		elm.setAttribute("data-method", "longer");
		elm.setAttribute("data-pcnt1", "41%");
		elm.getOverrideStyle(null).setCssText(
				"color: color-mix(in HWB attr(data-method type(<custom-ident>)) hue, hwb(60.8 26% 24%) attr(data-pcnt1 type(<percentage>)), hwb(90.3 40% 31%))");
		style = elm.getComputedStyle(null);
		assertEquals("hwb(225.805 34.26% 28.13%)", style.getPropertyValue("color"));

		// Attr colorspace method, %
		elm.setAttribute("data-space", "hwb");
		elm.getOverrideStyle(null).setCssText(
				"color: color-mix(in attr(data-space type(<custom-ident>)) attr(data-method type(<custom-ident>)) hue, hwb(60.8 26% 24%) attr(data-pcnt1 type(<percentage>)), hwb(90.3 40% 31%))");
		style = elm.getComputedStyle(null);
		assertEquals("hwb(225.805 34.26% 28.13%)", style.getPropertyValue("color"));

		// Attr colors %
		elm.setAttribute("data-color1", "lab(76.473 -12.12 60.839)");
		elm.setAttribute("data-color2", "oklch(0.7118 0.1088 130.59)");
		elm.setAttribute("data-pcnt2", "55");
		elm.getOverrideStyle(null).setCssText(
				"color: color-mix(in HWB,attr(data-color1 type(<color>)) attr(data-pcnt1 type(<percentage>)),attr(data-color2 type(<color>)) attr(data-pcnt2 %))");
		style = elm.getComputedStyle(null);
		assertEquals("hwb(77.659 34% 28.01%)", style.getPropertyValue("color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Attr error
		elm.setAttribute("data-pcnt2", "55%");
		assertEquals("#808000", style.getPropertyValue("color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

}
