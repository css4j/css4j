/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleDefiner;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleProcessingInstruction;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

/**
 * Similar to StylableDocumentWrapperTest, but processing the XML sample instead
 * of XHTML.
 */
public class XMLDocumentWrapperTest {

	private static DocumentBuilder docb;
	private StylableDocumentWrapper xmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		dbFac.setNamespaceAware(true);
		docb = dbFac.newDocumentBuilder();
		docb.setEntityResolver(new DefaultEntityResolver());
	}

	@BeforeEach
	public void setUp() throws SAXException, IOException {
		Reader re = SampleCSS.sampleXMLReader();
		InputSource is = new InputSource(re);
		Document doc = docb.parse(is);
		re.close();
		doc.setDocumentURI("http://www.example.com/xml/xmlsample.xml");
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		xmlDoc = cssFac.createCSSDocument(doc);
	}

	@Test
	public void getDoctype() {
		DocumentType docType = xmlDoc.getDoctype();
		assertNotNull(docType);
		assertEquals("html", docType.getName());
		assertNull(docType.getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", docType.getSystemId());
	}

	@Test
	public void getDocumentElement() {
		CSSElement elm = xmlDoc.getDocumentElement();
		assertNotNull(elm);
		assertEquals("html", elm.getTagName());
		assertEquals("http://www.w3.org/1999/xhtml", elm.getNamespaceURI());
	}

	@Test
	public void testEntities1() {
		CSSElement elm = xmlDoc.getElementById("entity");
		assertNotNull(elm);
		assertEquals("span", elm.getTagName());
		assertEquals("<>", elm.getTextContent());
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(1, nl.getLength());
		Node node0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, node0.getNodeType());
		assertEquals("<>", node0.getNodeValue());
		Attr classattr = elm.getAttributeNode("class");
		assertNotNull(classattr);
		assertEquals("ent\"ity", classattr.getValue());
	}

	@Test
	public void testEntities2() {
		CSSElement elm = xmlDoc.getElementById("entiacute");
		assertNotNull(elm);
		assertEquals("span", elm.getTagName());
		assertEquals("ítem", elm.getTextContent());
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(1, nl.getLength());
		Node ent0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, ent0.getNodeType());
		assertEquals("ítem", ent0.getNodeValue());
	}

	@Test
	public void getChildNodes() {
		NodeList list = xmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(8, list.getLength());
	}

	@Test
	public void getElementById() {
		CSSElement elm = xmlDoc.getElementById("ul1");
		assertNotNull(elm);
		assertEquals("ul", elm.getTagName());
		assertNull(xmlDoc.getElementById("xxxxxx"));

		assertNull(elm.getAttributeNode("foo"));
		assertNull(elm.getAttributeNodeNS("http://example.com/foo", "bar"));
		assertEquals(0, elm.getAttribute("foo").length());
		assertEquals(0, elm.getAttributeNS("http://example.com/foo", "bar").length());
	}

	@Test
	public void getElementsByTagName() {
		NodeList stylelist = xmlDoc.getElementsByTagName("style");
		assertNotNull(stylelist);
		assertEquals(2, stylelist.getLength());
		assertEquals("style", stylelist.item(0).getNodeName());
		NodeList list = xmlDoc.getElementsByTagName("li");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		list = xmlDoc.getElementsByTagName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByTagName("div");
		assertEquals(3, list.getLength());
	}

	@Test
	public void getTextContent() {
		Element elm = (Element) xmlDoc.getElementsByTagName("style").item(0);
		assertNotNull(elm);
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1204, text.trim().length());
	}

	@Test
	public void getStyleSheet() {
		DocumentCSSStyleSheet defsheet = xmlDoc.getStyleSheetFactory()
				.getDefaultStyleSheet(xmlDoc.getComplianceMode());
		assertNotNull(defsheet);
		// Obtain the number of rules in the default style sheet, to use it
		// as a baseline.
		int defSz = defsheet.getCssRules().getLength();
		DocumentCSSStyleSheet css = xmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xmlDoc.embeddedStyle.size() + xmlDoc.linkedStyle.size();
		assertEquals(7, countInternalSheets);
		assertEquals(7, xmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css",
				xmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xmlDoc.getStyleSheetSets().getLength());
		Iterator<LinkStyleDefiner> it = xmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(xmlDoc.hasStyleIssues());

		assertEquals("background-color: red; ",
				((StyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		CSSStyleDeclaration fontface = ((CSSDeclarationRule) sheet.getCssRules().item(1))
				.getStyle();
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf')",
				fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CssType.TYPED, ffval.getCssValueType());
		assertEquals(CSSValue.Type.URI, ffval.getPrimitiveType());
		assertTrue(((FontFeatureValuesRule) sheet.getCssRules().item(2)).getMinifiedCssText()
				.startsWith("@font-feature-values Foo Sans,Bar"));
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(defSz + 25, css.getCssRules().getLength());
		assertFalse(xmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.enableStyleSheetsForSet("Alter 1");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.enableStyleSheetsForSet("Alter 1");
		assertNull(xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Default");
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void getFontSizeMedia() throws CSSMediaException {
		CSSElement elm = xmlDoc.getElementById("span1");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(15f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		CSSElement para = xmlDoc.getElementById("para2");
		CSSComputedProperties stylePara = xmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertNotNull(stylePara);
		assertEquals(12f, stylePara.getComputedFontSize(), 1e-5);

		xmlDoc.setTargetMedium("screen");
		assertEquals("screen", xmlDoc.getStyleSheet().getTargetMedium());
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(20f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		stylePara = xmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertEquals(16f, stylePara.getComputedFontSize(), 1e-5);
		xmlDoc.setTargetMedium("all");
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyle() {
		CSSElement elm = xmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(15, styledecl.getLength());
		assertEquals("#808000", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
	}

	@Test
	public void getOverrideStyle() {
		CSSElement elm = xmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("5pt", style.getPropertyValue("margin-top"));
		assertEquals("margin-top: 5pt; margin-right: 5pt; margin-bottom: 5pt; margin-left: 5pt; ",
				style.getCssText());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals(
				"margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals("margin:16pt;color:red;", style.getMinifiedCssText());
	}

	@Test
	public void testGetDocumentElementGetColor() throws CSSMediaException {
		CSSElement elm = xmlDoc.getDocumentElement();
		assertNotNull(elm);
		CSSComputedProperties style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		StyleValue color = (StyleValue) style.getPropertyCSSValue("color");
		assertNotNull(color);
		assertEquals("initial", color.getCssText());
		assertTrue(color.isSystemDefault());
		assertEquals(0, style.getLength());
		// style database
		xmlDoc.setTargetMedium("print");
		style = elm.getComputedStyle(null);
		color = (StyleValue) style.getPropertyCSSValue("color");
		assertNotNull(color);
		assertEquals("initial", color.getCssText());
		assertTrue(color.isSystemDefault());
		assertEquals(0, style.getLength());
	}

	@Test
	public void testGetDocumentElementGetColorLenient() throws SAXException, IOException {
		Reader re = SampleCSS.sampleXMLReader();
		InputSource is = new InputSource(re);
		Document doc = docb.parse(is);
		re.close();
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(true);
		xmlDoc = cssFac.createCSSDocument(doc);

		CSSElement elm = xmlDoc.getDocumentElement();
		assertNotNull(elm);
		CSSComputedProperties style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		StyleValue color = (StyleValue) style.getPropertyCSSValue("color");
		assertNotNull(color);
		assertEquals("#000", color.getCssText());
		assertTrue(color.isSystemDefault());
		assertEquals(0, style.getLength());
	}

	@Test
	public void testEmbeddedStyle() {
		xmlDoc.getStyleSheets();
		LinkStyleDefiner link = xmlDoc.embeddedStyle.iterator().next();
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		link.setNodeValue("href=\"#style1\" media=\"screen\"");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertTrue(sheet2.getCssRules().getLength() > 0);
		// Change content of the container element
		LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) link;
		String href = pi.getPseudoAttribute("href");
		assertTrue(href.length() > 1);
		assertEquals('#', href.charAt(0));
		Element container = xmlDoc.getElementById(href.substring(1));
		assertNotNull(container);
		Node text = firstNonEmptyText(container.getChildNodes());
		assertNotNull(text);
		text.setNodeValue("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(2, sheet.getCssRules().getLength());
	}

	@Test
	public void testEmbeddedStyle2() {
		xmlDoc.getStyleSheets();
		LinkStyleDefiner link = xmlDoc.embeddedStyle.iterator().next();
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		// Change text child of the container element
		LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) link;
		String href = pi.getPseudoAttribute("href");
		assertTrue(href.length() > 1);
		assertEquals('#', href.charAt(0));
		Element container = xmlDoc.getElementById(href.substring(1));
		assertNotNull(container);
		Node text = firstNonEmptyText(container.getChildNodes());
		assertNotNull(text);
		text.setNodeValue("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertEquals(2, sheet2.getCssRules().getLength());
	}

	private static Node firstNonEmptyText(NodeList list) {
		Node text = null;
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.TEXT_NODE && node.getNodeValue().trim().length() > 2) {
				text = node;
				break;
			}
		}
		return text;
	}

	@Test
	public void testStyleElement() {
		xmlDoc.getStyleSheets();
		LinkStyleDefiner link = xmlDoc.embeddedStyle.iterator().next();
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() != 0);
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(sheet.getOwnerNode() == link);

		CSSElement style = (CSSElement) xmlDoc.getElementsByTagName("style").item(0);
		assertEquals(1, style.getChildNodes().getLength());
		Node node = style.getFirstChild();
		node.setNodeValue("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");

		link.setNodeValue("href=\"#doesnotexist\" type=\"text/css\"");
		assertNull(link.getSheet());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		xmlDoc.getErrorHandler().reset();

		link.setNodeValue("href=\"#style1\" type=\"foo\"");
		assertNull(link.getSheet());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testLinkElement() {
		xmlDoc.getStyleSheets();
		LinkStyleDefiner link = xmlDoc.linkedStyle.iterator().next();
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() != 0);
		assertFalse(xmlDoc.getErrorHandler().hasErrors());

		link.setNodeValue("href=\"/css/common.css\" media=\"screen\"");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertEquals(sheet.getCssRules().getLength(), sheet2.getCssRules().getLength());

		link.setNodeValue("href=\"http://www.example.com/css/alter1.css\" media=\"screen\"");
		sheet = link.getSheet();
		assertFalse(xmlDoc.getErrorHandler().hasErrors());

		link.setNodeValue(
				"href=\"http://www.example.com/css/alter1.css\" media=\"screen and only\"");
		assertNull(link.getSheet());
		xmlDoc.getErrorHandler().reset();

		link.setNodeValue("href=\"http://www.example.com/css/example.css\" media=\"screen\"");
		sheet = link.getSheet();
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testBaseAttribute() {
		assertEquals("http://www.example.com/xml/xmlsample.xml", xmlDoc.getDocumentURI());
		assertEquals("http://www.example.com/", xmlDoc.getBaseURI());
		Attr base = xmlDoc.getDocumentElement().getAttributeNode("xml:base");
		assertEquals("http://www.example.com/", base.getNodeValue());
	}

	@Test
	public void testCascade() throws IOException {
		try (Reader re = SampleCSS.loadSampleUserCSSReader()) {
			xmlDoc.getStyleSheetFactory().setUserStyleSheet(re);
		}
		CSSElement elm = xmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		elm.getOverrideStyle(null).setCssText("color: darkmagenta ! important;");
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));

		// Clear the user sheet
		xmlDoc.getStyleSheetFactory().setUserStyleSheet(null);
		style = elm.getComputedStyle(null);
		assertEquals("#0000", style.getPropertyValue("background-color"));
		assertEquals("#8b008b", style.getPropertyValue("color"));
	}

	@Test
	public void testMetaElementReferrerPolicy() {
		assertEquals("same-origin", xmlDoc.getReferrerPolicy());
	}

}
