/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleDefiner;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class StylableDocumentWrapperTest {

	static Document refXhtmlDoc;

	private StylableDocumentWrapper xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass()
			throws IOException, SAXException, ParserConfigurationException {
		InputStream is = SampleCSS.sampleHTMLStream();
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		docb.setEntityResolver(new DefaultEntityResolver());
		InputSource source = new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8));
		refXhtmlDoc = docb.parse(source);
		is.close();
		refXhtmlDoc.getDocumentElement().normalize();
	}

	@BeforeEach
	public void setUp() {
		Document doc = (Document) refXhtmlDoc.cloneNode(true);
		doc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		xhtmlDoc = cssFac.createCSSDocument(doc);
	}

	@Test
	public void testCreateDocument() throws IOException, DocumentException {
		assertEquals("CSS1Compat", xhtmlDoc.getCompatMode());
		assertEquals(CSSDocument.ComplianceMode.STRICT, xhtmlDoc.getComplianceMode());
		DocumentType docType = xhtmlDoc.getDoctype();
		assertNotNull(docType);
		assertEquals("html", docType.getName());
		assertNull(docType.getPublicId());
		assertNull(docType.getSystemId());
		assertFalse(xhtmlDoc.hasStyleIssues());
	}

	@Test
	public void testCreateDocument2() throws ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		DOMImplementation domImpl = docb.getDOMImplementation();
		Document doc = domImpl.createDocument(null, null, null);
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		StylableDocumentWrapper document = factory.createCSSDocument(doc);
		assertEquals("BackCompat", document.getCompatMode());
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, document.getComplianceMode());
		assertNull(document.getDoctype());
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		doc = domImpl.createDocument(null, "html", doctype);
		document = factory.createCSSDocument(doc);
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		assertEquals("html", docType.getName());
		assertNull(docType.getPublicId());
		assertNull(docType.getSystemId());
		assertEquals("CSS1Compat", document.getCompatMode());
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
	public void isSafeOrigin() throws MalformedURLException, URISyntaxException {
		URL url = new URI("http://www.example.COM/bar").toURL();
		assertTrue(xhtmlDoc.isSafeOrigin(url));

		assertTrue(xhtmlDoc.isSafeOrigin(new URI("http://www.example.com:80/other.html").toURL()));

		url = new URI("http://www.foo.com/bar").toURL();
		assertFalse(xhtmlDoc.isSafeOrigin(url));

		url = new URI("http://www1.example.com/other/").toURL();
		assertFalse(xhtmlDoc.isSafeOrigin(url));

		url = new URI("http://www.example.com:8000/xhtml/htmlsample.html").toURL();
		assertFalse(xhtmlDoc.isSafeOrigin(url));

		url = new URI("http://otherwww.example.com/other/").toURL();
		assertFalse(xhtmlDoc.isSafeOrigin(url));

		url = new URI("http://other.www.example.COM/other/").toURL();
		assertTrue(xhtmlDoc.isSafeOrigin(url));

		// Set a new document URI
		xhtmlDoc.setDocumentURI("http://www.example.org:80/html/other.html");
		// Remove <base> so the new document URI takes effect
		Node base = xhtmlDoc.getElementsByTagName("base").item(0);
		assertNotNull(base);
		Node parent = base.getParentNode();
		parent.removeChild(base);

		assertTrue(xhtmlDoc.isSafeOrigin(new URI("http://www.example.ORG/foo.html").toURL()));
	}

	@Test
	public void getDocumentElement() {
		CSSElement root = xhtmlDoc.getDocumentElement();
		assertNotNull(root);
		assertEquals("html", root.getTagName());

		CSSElement body = null;
		NodeList list = root.getChildNodes();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if ("body".equalsIgnoreCase(node.getNodeName())) {
				body = (CSSElement) node;
				break;
			}
		}
		assertNotNull(body);

		CSSElement p = xhtmlDoc.createElement("p");
		p.setAttribute("id", "p1");
		p.setIdAttribute("id", true);
		body.appendChild(p);

		assertSame(p, xhtmlDoc.getElementById("p1"));

		CSSElement div = xhtmlDoc.createElement("div");
		Attr divid = xhtmlDoc.createAttribute("id");
		divid.setValue("newdiv1");
		div.setAttributeNode(divid);
		div.setIdAttributeNode(divid, true);

		body.replaceChild(div, p);
		assertNull(xhtmlDoc.getElementById("p1"));
		assertSame(div, xhtmlDoc.getElementById("newdiv1"));

		body.insertBefore(p, div);
		assertSame(p, xhtmlDoc.getElementById("p1"));

		body.removeChild(p);
		assertNull(xhtmlDoc.getElementById("p1"));
		body.removeChild(div);
		assertNull(xhtmlDoc.getElementById("newdiv1"));
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
		assertEquals(7, countInternalSheets);
		assertEquals(7, xhtmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css",
				xhtmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());
		assertFalse(xhtmlDoc.hasStyleIssues());

		Iterator<LinkStyleDefiner> it = xhtmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals("background-color: red; ",
				((StyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		CSSStyleDeclaration fontface = ((CSSDeclarationRule) sheet.getCssRules().item(1))
				.getStyle();
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf')",
				fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CssType.TYPED, ffval.getCssValueType());
		assertEquals(CSSValue.Type.URI, ffval.getPrimitiveType());
		assertTrue(sheet.getCssRules().item(2).getMinifiedCssText()
				.startsWith("@font-feature-values Foo Sans,Bar"));

		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(defSz + 25, css.getCssRules().getLength());
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

		StyleSheetList sheets = xhtmlDoc.getStyleSheets();
		assertEquals(7, sheets.getLength());
		sheets.remove("Alter 2");
		assertEquals(6, sheets.getLength());
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void testAlternateStyle() {
		xhtmlDoc.setSelectedStyleSheetSet("Alter 1");
		CSSElement body = (CSSElement) xhtmlDoc.getElementsByTagName("body").item(0);
		CSSComputedProperties style = body.getComputedStyle(null);
		assertEquals("#000080", style.getPropertyValue("color"));
		assertEquals("#ff0", style.getPropertyValue("background-color"));
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
		assertEquals("'Does Not Exist', Neither", styledecl.getPropertyValue("font-family"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
		xhtmlDoc.getErrorHandler().reset();
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
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
		StyleDeclarationErrorHandler eh = xhtmlDoc.getErrorHandler()
				.getInlineStyleErrorHandler(elm);
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
	public void getOverrideStyle() {
		CSSElement elm = xhtmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("10px", style.getPropertyValue("margin-top"));
		assertEquals(
				"margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; ",
				style.getCssText());
		assertEquals(4, style.getLength());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals(
				"margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals("margin:16pt;color:red;", style.getMinifiedCssText());
		assertEquals(5, style.getLength());
	}

	@Test
	public void testGetDocumentElementGetColor() throws CSSMediaException {
		CSSElement elm = xhtmlDoc.getDocumentElement();
		assertNotNull(elm);
		CSSComputedProperties style = elm.getComputedStyle(null);
		StyleValue color = (StyleValue) style.getPropertyCSSValue("color");
		assertNotNull(color);
		assertEquals("initial", color.getCssText());
		assertTrue(color.isSystemDefault());
		assertEquals(0, style.getLength());
		// style database
		xhtmlDoc.setTargetMedium("print");
		style = elm.getComputedStyle(null);
		color = (StyleValue) style.getPropertyCSSValue("color");
		assertNotNull(color);
		assertEquals("initial", color.getCssText());
		assertTrue(color.isSystemDefault());
		assertEquals(0, style.getLength());
	}

	@Test
	public void testStyleElement() {
		CSSElement style = (CSSElement) xhtmlDoc.getElementsByTagName("style").item(0);
		CSSStyleSheet<?> sheet = ((LinkStyle<?>) style).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == style);
		assertEquals(1, style.getChildNodes().getLength());
		Node node = style.getFirstChild();
		node.setNodeValue(
				"body {font-size: /* pre */14pt/* after */; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = ((LinkStyle<?>) style).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == style);

		// Obtain a value to check comments
		StyleRule styleR = (StyleRule) sheet.getCssRules().item(0);
		StyleValue fontSize = styleR.getStyle().getPropertyCSSValue("font-size");
		assertNotNull(fontSize.getPrecedingComments());
		assertNotNull(fontSize.getTrailingComments());
		assertEquals(" pre ", fontSize.getPrecedingComments().item(0));
		assertEquals(" after ", fontSize.getTrailingComments().item(0));

		xhtmlDoc.getStyleSheetFactory().setFlag(Parser.Flag.VALUE_COMMENTS_IGNORE);

		// Set the stylesheet again
		style.setTextContent(
				"body {font-size: /* pre */14pt/* after */; margin-left: 7%;} h1 {font-size: 2.4em;}");
		AbstractCSSStyleSheet sheet2 = ((LinkStyleDefiner) style).getSheet();
		assertTrue(sheet2 == sheet);

		// Obtain the same value, no comments now
		styleR = (StyleRule) sheet2.getCssRules().item(0);
		fontSize = styleR.getStyle().getPropertyCSSValue("font-size");
		assertNull(fontSize.getPrecedingComments());
		assertNull(fontSize.getTrailingComments());

		sheet2.getStyleSheetFactory().unsetFlag(Parser.Flag.VALUE_COMMENTS_IGNORE);

		// Empty text content
		node.setNodeValue("");
		assertEquals(0, sheet.getCssRules().getLength());

		// Remove node
		style.removeChild(node);

		// Use setTextContent
		style.setTextContent("/* Comments only */");
		sheet2 = ((LinkStyleDefiner) style).getSheet();
		assertTrue(sheet2 == sheet);
		assertEquals(0, sheet2.getCssRules().getLength());

		Attr type = style.getAttributeNode("type");
		type.setNodeValue("foo");
		assertNull(((LinkStyle<?>) style).getSheet());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testStyleElement2() throws ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		Document doc = docb.getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element element = doc.createElement("style");
		element.setAttribute("id", "style1");
		element.setIdAttribute("id", true);
		element.setAttribute("type", "text/css");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(element);
		doc.setDocumentURI("http://www.example.com/");
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		CSSDocument cssDoc = cssFac.createCSSDocument(doc);

		CSSElement style = cssDoc.getElementById("style1");
		assertNotNull(style);
		CSSStyleSheet<?> sheet = ((LinkStyle<?>) style).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());

		Attr type = style.getAttributeNode("type");
		type.setValue("");
		sheet = ((LinkStyle<?>) style).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());

		type.setValue("text/xsl");
		sheet = ((LinkStyle<?>) style).getSheet();
		assertNull(sheet);

		doc = docb.getDOMImplementation().createDocument(null, "html", null);
		head = doc.createElement("head");
		element = doc.createElement("style");
		element.setAttribute("id", "style1");
		element.setIdAttribute("id", true);
		doc.getDocumentElement().appendChild(head);
		head.appendChild(element);
		doc.setDocumentURI("http://www.example.com/");
		cssDoc = cssFac.createCSSDocument(doc);
		// Lacks a specified 'type' attribute
		style = cssDoc.getElementById("style1");
		assertNotNull(style);
		sheet = ((LinkStyle<?>) style).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testLinkElement() {
		CSSElement link = (CSSElement) xhtmlDoc.getElementsByTagName("link").item(0);

		CSSStyleSheet<?> sheet = ((LinkStyle<?>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == link);

		Attr href = link.getAttributeNode("href");
		assertNotNull(href);
		href.setValue("http://www.example.com/css/example.css");
		assertNotNull(((LinkStyle<?>) link).getSheet());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		xhtmlDoc.getErrorHandler().reset();

		link = (CSSElement) xhtmlDoc.getElementsByTagName("link").item(4);
		sheet = ((LinkStyle<?>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(1, sheet.getMedia().getLength());
		assertEquals("print", sheet.getMedia().getMediaText());
		assertTrue(sheet.getCssRules().getLength() != 0);
		assertTrue(sheet.getOwnerNode() == link);
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());

		Attr media = link.getAttributeNode("media");
		assertNotNull(media);
		media.setValue("screen only and");
		assertNull(((LinkStyle<?>) link).getSheet());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testCascade() throws IOException {
		try (Reader re = SampleCSS.loadSampleUserCSSReader()) {
			xhtmlDoc.getStyleSheetFactory().setUserStyleSheet(re);
		}
		CSSElement elm = xhtmlDoc.getElementById("para1");
		CSSStyleDeclaration style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		elm.getOverrideStyle(null).setCssText("color: darkmagenta ! important;");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));

		// Clear the user sheet
		xhtmlDoc.getStyleSheetFactory().setUserStyleSheet(null);
		style = elm.getComputedStyle(null);
		assertEquals("#0000", style.getPropertyValue("background-color"));
		assertEquals("#8b008b", style.getPropertyValue("color"));
	}

	@Test
	public void testCascade2() throws IOException {
		BaseCSSStyleSheet sheet = (BaseCSSStyleSheet) xhtmlDoc.getStyleSheets().item(5);

		// Obtain the rule where a value is declared
		CSSParser parser = new CSSParser();
		SelectorList selist = parser.parseSelectors("p.boldmargin");
		StyleRule rule = sheet.getFirstStyleRule(selist);
		assertNotNull(rule);

		AbstractCSSStyleDeclaration declStyle = rule.getStyle();
		TypedValue declMarginLeft = (TypedValue) declStyle.getPropertyCSSValue("margin-left");
		assertEquals("2%", declMarginLeft.getCssText());

		/*
		 * Get an element that obtains the above value as computed style
		 */
		CSSElement elm = xhtmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getComputedStyle(null);
		assertEquals("2%", style.getPropertyValue("margin-left"));

		// Change the value itself
		declMarginLeft.setFloatValue(CSSUnit.CSS_PX, 6f);
		style = elm.getComputedStyle(null);
		assertEquals("6px", style.getPropertyValue("margin-left"));

		// Overwrite the property's value
		declStyle.setProperty("margin-left", "4px", null);
		style = elm.getComputedStyle(null);
		// The new value is not there yet
		assertEquals("6px", style.getPropertyValue("margin-left"));

		// Rebuild the cascade
		xhtmlDoc.rebuildCascade();
		style = elm.getComputedStyle(null);
		assertEquals("4px", style.getPropertyValue("margin-left"));
	}

	@Test
	public void testVisitors() throws IOException {
		StyleCountVisitor visitor = new StyleCountVisitor();
		xhtmlDoc.getStyleSheets().acceptStyleRuleVisitor(visitor);
		assertEquals(29, visitor.getCount());

		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		xhtmlDoc.getStyleSheets().acceptDeclarationRuleVisitor(visitorP);
		assertEquals(111, visitorP.getCount());

		visitorP.reset();
		xhtmlDoc.getStyleSheets().acceptDescriptorRuleVisitor(visitorP);
		assertEquals(2, visitorP.getCount());
	}

	@Test
	public void testBaseElement() {
		assertEquals("http://www.example.com/xhtml/htmlsample.html", xhtmlDoc.getDocumentURI());
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURL().toExternalForm());
		Node base = xhtmlDoc.getElementsByTagName("base").item(0);
		assertEquals("http://www.example.com/", base.getBaseURI());

		Node href = base.getAttributes().getNamedItem("href");
		href.setNodeValue("http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xhtmlDoc.getBaseURI());
		assertEquals("http://www.example.com/newbase/", base.getBaseURI());

		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		href.setNodeValue("foo://");
		assertEquals("http://www.example.com/xhtml/htmlsample.html", xhtmlDoc.getBaseURI());
		assertEquals("http://www.example.com/xhtml/htmlsample.html", base.getBaseURI());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasNodeErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	@Timeout(value = 8000, unit = TimeUnit.MILLISECONDS)
	public void testBaseElementEvil() {
		Node base = xhtmlDoc.getElementsByTagName("base").item(0);
		Node href = base.getAttributes().getNamedItem("href");
		href.setNodeValue("jar:http://www.example.com/evil.jar!/file");
		assertEquals("http://www.example.com/xhtml/htmlsample.html", base.getBaseURI());
		assertEquals("http://www.example.com/xhtml/htmlsample.html", xhtmlDoc.getBaseURI());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasPolicyErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());

		xhtmlDoc.getErrorHandler().reset();
		href.setNodeValue("file:/dev/zero");
		assertEquals("http://www.example.com/xhtml/htmlsample.html", base.getBaseURI());
		assertEquals("http://www.example.com/xhtml/htmlsample.html", xhtmlDoc.getBaseURI());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasPolicyErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());

		xhtmlDoc.getErrorHandler().reset();
		xhtmlDoc.setDocumentURI("jar:http://www.example.com/trusted.jar!/document.html");
		href.setNodeValue("jar:http://www.example.com/trusted.jar!/css/");
		assertEquals("jar:http://www.example.com/trusted.jar!/css/", xhtmlDoc.getBaseURI());
		assertEquals("jar:http://www.example.com/trusted.jar!/css/", base.getBaseURI());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testMetaElementReferrerPolicy() {
		assertEquals("same-origin", xhtmlDoc.getReferrerPolicy());
	}

}
