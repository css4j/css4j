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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.stylesheets.LinkStyle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSFontFeatureValuesRule;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleDefiner;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class StylableDocumentWrapperTest {
	StylableDocumentWrapper xhtmlDoc;

	@Before
	public void setUp() throws IOException, SAXException, ParserConfigurationException {
		InputStream is = DOMCSSStyleSheetFactoryTest.sampleHTMLStream();
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		docb.setEntityResolver(new DefaultEntityResolver());
        InputSource source = new InputSource(is);
		Document doc = docb.parse(source);
		is.close();
		doc.getDocumentElement().normalize();
		doc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		xhtmlDoc = cssFac.createCSSDocument(doc);
	}

	@Test
	public void testCreateDocument() throws IOException, DocumentException {
		assertEquals(CSSDocument.ComplianceMode.STRICT, xhtmlDoc.getComplianceMode());
	}

	@Test
	public void testCreateDocument2() throws ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		DOMImplementation domImpl = docb.getDOMImplementation();
		Document doc = domImpl.createDocument(null, null, null);
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		CSSDocument document = factory.createCSSDocument(doc);
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, document.getComplianceMode());
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		doc = domImpl.createDocument(null, "html", doctype);
		document = factory.createCSSDocument(doc);
		assertEquals(CSSDocument.ComplianceMode.STRICT, document.getComplianceMode());
	}

	@Test
	public void getElementById() {
		assertNull(xhtmlDoc.getElementById("foo1234"));
	}

	@Test
	public void getElementsByTagName() {
		NodeList tags = xhtmlDoc.getElementsByTagName("li");
		assertNotNull(tags);
		assertEquals(6, tags.getLength());
	}

	@Test
	public void getDocumentURI() {
		assertEquals("http://www.example.com/xhtml/htmlsample.html", xhtmlDoc.getDocumentURI());
	}

	@Test
	public void getBaseURI() {
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
	}

	@Test
	public void getBaseURL() {
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURL().toExternalForm());
	}

	@Test
	public void getDocumentElement() {
		CSSElement root = xhtmlDoc.getDocumentElement();
		assertNotNull(root);
		assertEquals("html", root.getTagName());
	}

	@Test
	public void getChildNodes() {
		NodeList list = xhtmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(2, list.getLength());
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
		assertEquals("background-color: red; ",
				((BaseCSSDeclarationRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		AbstractCSSStyleDeclaration fontface = ((BaseCSSDeclarationRule) sheet.getCssRules().item(1)).getStyle();
		assertEquals("url('http://www.example.com/css/font/MechanicalBd.otf')", fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ffval.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) ffval).getPrimitiveType());
		assertTrue(((CSSFontFeatureValuesRule) sheet.getCssRules().item(2)).getMinifiedCssText()
				.startsWith("@font-feature-values Foo Sans,Bar"));
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(defSz + 20, css.getCssRules().getLength());
		assertFalse(xhtmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertNull(xhtmlDoc.getLastStyleSheetSet());
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertNull(xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Default");
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		assertEquals("Default", xhtmlDoc.getLastStyleSheetSet());
	}

	@Test
	public void getElementgetStyle() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", style.getCssText());
		assertEquals(2, style.getLength());
		assertEquals("'Does Not Exist', Neither", style.getPropertyValue("font-family"));
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(17, styledecl.getLength());
		assertEquals("#000080", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		assertEquals("  foo  bar  ", styledecl.getPropertyValue("content"));
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
		// Change the style
		style.setCssText("font-family: Arial; color: #45f4a2");
		assertEquals(2, style.getLength());
		assertEquals("font-family: Arial; color: #45f4a2; ", style.getCssText());
		assertEquals("font-family: Arial; color: #45f4a2; ", elm.getAttribute("style"));
		// Error in inline style
		style.setCssText("width:calc(80%-)");
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		StyleDeclarationErrorHandler eh = xhtmlDoc.getErrorHandler().getInlineStyleErrorHandler(elm);
		assertNotNull(eh);
		assertTrue(eh.hasErrors());
	}

	@Test
	public void getElementgetStyleNull() {
		CSSElement elm = xhtmlDoc.getElementById("h2");
		assertNotNull(elm);
		assertNull(elm.getStyle());
	}

	@Test
	public void getOverrideStyle() {
		CSSElement elm = xhtmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("10px", style.getPropertyValue("margin-top"));
		assertEquals("margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; ",
				style.getCssText());
		assertEquals(4, style.getLength());
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin: 16pt; color: red");
		assertEquals("red", xhtmlDoc.getOverrideStyle(elm, null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", xhtmlDoc.getOverrideStyle(elm, null).getCssText());
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals("margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals("margin:16pt;color:#f00;", style.getMinifiedCssText());
		assertEquals(5, style.getLength());
	}

	@Test
	public void testGetDocumentElementGetColor() {
		CSSElement elm = xhtmlDoc.getDocumentElement();
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		AbstractCSSValue color = (AbstractCSSValue) style.getPropertyCSSValue("color");
		assertNotNull(color);
		assertEquals("initial", color.getCssText());
		assertTrue(color.isSystemDefault());
		assertEquals(0, style.getLength());
	}

	@Test
	public void testStyleElement() {
		CSSElement style = (CSSElement) xhtmlDoc.getElementsByTagName("style").item(0);
		CSSStyleSheet sheet = (CSSStyleSheet) ((LinkStyle) style).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == style);
		assertEquals(1, style.getChildNodes().getLength());
		Node node = style.getFirstChild();
		node.setNodeValue("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = (CSSStyleSheet) ((LinkStyle) style).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == style);
		// Empty text content
		node.setNodeValue("");
		assertEquals(0, sheet.getCssRules().getLength());
		//
		Attr type = style.getAttributeNode("type");
		type.setNodeValue("foo");
		assertNull(((LinkStyle) style).getSheet());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testLinkElement() {
		CSSElement link = (CSSElement) xhtmlDoc.getElementsByTagName("link").item(0);
		CSSStyleSheet sheet = (CSSStyleSheet) ((LinkStyle) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == link);
		//
		Attr href = link.getAttributeNode("href");
		assertNotNull(href);
		href.setValue("http://www.example.com/css/example.css");
		assertNotNull(((LinkStyle) link).getSheet());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		xhtmlDoc.getErrorHandler().reset();
		//
		link = (CSSElement) xhtmlDoc.getElementsByTagName("link").item(4);
		sheet = (CSSStyleSheet) ((LinkStyle) link).getSheet();
		assertNotNull(sheet);
		assertEquals(1, sheet.getMedia().getLength());
		assertEquals("print", sheet.getMedia().getMediaText());
		assertTrue(sheet.getCssRules().getLength() != 0);
		assertTrue(sheet.getOwnerNode() == link);
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		Attr media = link.getAttributeNode("media");
		assertNotNull(media);
		media.setValue("screen only and");
		assertNull(((LinkStyle) link).getSheet());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testCascade() throws IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleUserCSSReader();
		xhtmlDoc.getStyleSheetFactory().setUserStyleSheet(re);
		re.close();
		CSSElement elm = xhtmlDoc.getElementById("para1");
		CSSStyleDeclaration style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("color: darkmagenta ! important;");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));
	}

	@Test
	public void testMetaElementReferrerPolicy() {
		assertEquals("same-origin", xhtmlDoc.getReferrerPolicy());
	}

}
