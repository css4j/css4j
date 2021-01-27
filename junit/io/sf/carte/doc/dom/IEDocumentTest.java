/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.dom.DOMDocument.LinkStyleDefiner;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSDeclarationRule;
import io.sf.carte.doc.style.css.om.StyleSheetList;

public class IEDocumentTest {
	HTMLDocument xhtmlDoc;

	@Before
	public void setUp() throws IOException {
		xhtmlDoc = TestDOMImplementation.sampleIEDocument();
		xhtmlDoc.normalizeDocument();
	}

	@Test
	public void getTextContent() {
		CSSElement elm = xhtmlDoc.getElementsByTagName("style").item(0);
		assertNotNull(elm);
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1142, text.trim().length());
	}

	@Test
	public void testStyleSheetErrorState() {
		StyleSheetList list = xhtmlDoc.getStyleSheets();
		AbstractCSSStyleSheet sheet = list.item(list.getLength() - 1);
		assertTrue(sheet.hasRuleErrorsOrWarnings());
	}

	@Test
	public void getStyleSheet() {
		DocumentCSSStyleSheet defsheet = xhtmlDoc.getStyleSheetFactory()
				.getDefaultStyleSheet(xhtmlDoc.getComplianceMode());
		assertNotNull(defsheet);
		// Obtain the number of rules in the default style sheet, to use it
		// as a baseline.
		int defSz = defsheet.getCssRules().getLength();
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xhtmlDoc.embeddedStyle.size() + xhtmlDoc.linkedStyle.size();
		assertEquals(6, countInternalSheets);
		assertEquals(6, xhtmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css", xhtmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());
		Iterator<LinkStyleDefiner> it = xhtmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals("background-color: red; ", ((CSSStyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		AbstractCSSStyleDeclaration fontface = ((BaseCSSDeclarationRule) sheet.getCssRules().item(1)).getStyle();
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf')", fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ffval.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) ffval).getPrimitiveType());
		assertTrue(sheet.getCssRules().item(2).getMinifiedCssText().startsWith("@font-feature-values Foo Sans,Bar"));
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(defSz + 20, css.getCssRules().getLength());
		assertFalse(css.getErrorHandler().hasSacErrors());
		assertTrue(xhtmlDoc.hasStyleIssues());
	}

	@Test
	public void getElementgetStyle() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", style.getCssText());
		assertEquals(2, style.getLength());
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(19, styledecl.getLength());
		assertEquals("#000080", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
		//
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void getElementgetStyleHack() {
		CSSElement elm = xhtmlDoc.getElementById("cell62");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertEquals("padding: 2pt 3pt; padding: 5pt 6pt\\9; margin-left: 8pt; margin-left: 9pt\\9; ",
				style.getCssText());
		assertEquals(5, style.getLength());
		// Check for non-existing property
		assertNull(style.getPropertyCSSValue("does-not-exist"));
		assertEquals("", style.getPropertyValue("does-not-exist"));
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("8pt", styledecl.getPropertyValue("margin-left"));
		assertEquals("2pt", styledecl.getPropertyValue("padding-top"));
		assertEquals("3pt", styledecl.getPropertyValue("padding-left"));
	}

	@Test
	public void getElementgetComputedStylePresentationalAttribute() {
		CSSElement elm = xhtmlDoc.getElementById("fooimg");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(2, styledecl.getLength());
		assertEquals("200px", styledecl.getPropertyValue("width"));
		assertEquals("180px", styledecl.getPropertyValue("height"));
		elm.setAttribute("style", "width: 220px; height: 193px;");
		styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(2, styledecl.getLength());
		assertEquals("220px", styledecl.getPropertyValue("width"));
		assertEquals("193px", styledecl.getPropertyValue("height"));
		// Check error handling
		CSSElement parent = (CSSElement) elm.getParentNode();
		parent.setAttribute("bgcolor", "#90fz77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(11, styledecl.getLength());
		assertEquals("rgb(0 0 0 / 0)", styledecl.getPropertyValue("background-color"));
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(parent));
		xhtmlDoc.getErrorHandler().reset();
		parent.setAttribute("bgcolor", "#90ff77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("#90ff77", styledecl.getPropertyValue("background-color"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getOverrideStyle() {
		CSSElement elm = xhtmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("10px", style.getPropertyValue("margin-top"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; ",
				style.getCssText());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals(
				"display:table-row;vertical-align:middle;border-color:#808080;unicode-bidi:embed;margin:16pt;color:#f00;",
				style.getMinifiedCssText());
		// Inject backslash hack values
		elm.getOverrideStyle(null).setCssText("margin: 16pt 14pt; color: red; margin: 12pt 10pt\\9;");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt 14pt; margin: 12pt 10pt\\9; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("14pt", style.getPropertyValue("margin-left"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 16pt; margin-right: 14pt; margin-bottom: 16pt; margin-left: 14pt; color: #f00; ",
				style.getCssText());
	}

	@Test
	public void testCompatComputedStyle() {
		CSSElement elm = xhtmlDoc.getElementById("cell12");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertNull(style);
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("5pt", styledecl.getPropertyValue("margin-left"));
		assertEquals("4pt", styledecl.getPropertyValue("padding-top"));
		assertEquals("6pt", styledecl.getPropertyValue("padding-left"));
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
	}

}
