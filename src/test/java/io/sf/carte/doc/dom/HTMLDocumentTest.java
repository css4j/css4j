/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.StyleSheetList;

public class HTMLDocumentTest {

	private static TestDOMImplementation domImplementation;

	private HTMLDocument xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeAll() throws IOException {
		domImplementation = new TestDOMImplementation(false);
	}

	@BeforeEach
	public void setUp() throws IOException {
		xhtmlDoc = domImplementation.createHTMLDocument(null);
	}

	@Test
	public void getDoctype() {
		assertNull(xhtmlDoc.getDoctype());
	}

	@Test
	public void getDocumentElement() {
		assertNull(xhtmlDoc.getDocumentElement());
	}

	@Test
	public void getNamespaceURI() {
		assertNull(xhtmlDoc.getNamespaceURI());
	}

	@Test
	public void testAppendChildElementHierarchyError() throws DOMException {
		DOMElement html = xhtmlDoc.createElement("html");
		xhtmlDoc.appendChild(html);

		// Document already has this node
		DOMException ex = assertThrows(DOMException.class, () -> xhtmlDoc.appendChild(html));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
		assertEquals(1, xhtmlDoc.getChildNodes().getLength());

		// DOCTYPE after DOCUMENT element
		DocumentType docType = domImplementation.createDocumentType("html", null, null);
		ex = assertThrows(DOMException.class, () -> xhtmlDoc.appendChild(docType));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
		assertEquals(1, xhtmlDoc.getChildNodes().getLength());

		xhtmlDoc.insertBefore(docType, html);
		assertEquals(2, xhtmlDoc.getChildNodes().getLength());

		// Document already has this node AND is after DOCUMENT element
		ex = assertThrows(DOMException.class, () -> xhtmlDoc.appendChild(docType));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		// Now before DOCUMENT element
		ex = assertThrows(DOMException.class, () -> xhtmlDoc.insertBefore(docType, html));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
		assertEquals(2, xhtmlDoc.getChildNodes().getLength());

		// Document already has a DOCUMENT element
		DOMElement html2 = xhtmlDoc.createElement("html");
		ex = assertThrows(DOMException.class, () -> xhtmlDoc.appendChild(html2));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		// Document already has a DOCTYPE
		DocumentType docType2 = domImplementation.createDocumentType("foo", null, null);
		ex = assertThrows(DOMException.class, () -> xhtmlDoc.insertBefore(docType2, html));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		// DOCTYPE after DOCUMENT element
		xhtmlDoc.removeChild(docType);
		ex = assertThrows(DOMException.class, () -> xhtmlDoc.appendChild(docType2));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
	}

	@Test
	public void testAppendChildTwoDoctypesError() throws DOMException {
		DOMDocument document = domImplementation.createDocument(null, null, null);
		document.appendChild(domImplementation.createDocumentType("foo", null, null));
		try {
			document.appendChild(
					document.getImplementation().createDocumentType("bar", null, null));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testCloneNode() {
		assertTrue(xhtmlDoc.isEqualNode(xhtmlDoc.cloneNode(true)));
	}

	@Test
	public void testProcessingInstruction() {
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"style.xsl\"");

		DOMElement html = xhtmlDoc.createElement("html");
		DOMException ex = assertThrows(DOMException.class, () -> pi.appendChild(html));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		DocumentType docType = domImplementation.createDocumentType("foo", null, null);

		ex = assertThrows(DOMException.class, () -> docType.appendChild(pi));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		ex = assertThrows(DOMException.class, () -> pi.appendChild(docType));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		Text text = xhtmlDoc.createTextNode("bar");

		ex = assertThrows(DOMException.class, () -> text.appendChild(pi));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);

		ex = assertThrows(DOMException.class, () -> pi.appendChild(text));
		assertEquals(DOMException.HIERARCHY_REQUEST_ERR, ex.code);
	}

	@Test
	public void testCloneDocumentNode() {
		HTMLDocument doc = xhtmlDoc.cloneNode(false);
		assertNull(doc.getDoctype());
		assertNull(doc.getDocumentElement());
		assertTrue(xhtmlDoc.getImplementation() == doc.getImplementation());
	}

	@Test
	public void getChildNodes() {
		NodeList list = xhtmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(0, list.getLength());
	}

	@Test
	public void testGetElementsByTagName() {
		assertTrue(xhtmlDoc.getElementsByTagName("style").isEmpty());
	}

	@Test
	public void getElementsByClassName() {
		assertTrue(xhtmlDoc.getElementsByClassName("tableclass").isEmpty());
	}

	@Test
	public void getElementsByTagNameNS() {
		assertTrue(xhtmlDoc.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "*").isEmpty());
	}

	@Test
	public void testQuerySelectorAll() {
		assertTrue(xhtmlDoc.querySelectorAll("#ul1").isEmpty());
	}

	@Test
	public void getStyleSheet() {
		DocumentCSSStyleSheet defsheet = xhtmlDoc.getStyleSheetFactory()
				.getDefaultStyleSheet(xhtmlDoc.getComplianceMode());
		assertEquals(0, defsheet.getCssRules().getLength());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(0, list.getLength());

		assertNull(xhtmlDoc.getLastStyleSheetSet());
		assertEquals("", xhtmlDoc.getSelectedStyleSheetSet());

		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("", xhtmlDoc.getSelectedStyleSheetSet());

		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertTrue(xhtmlDoc.getSelectedStyleSheetSet().isEmpty());

		StyleSheetList sheets = xhtmlDoc.getStyleSheets();
		assertEquals(0, sheets.getLength());
	}

	@Test
	public void testSetSelectedStyleSheetSet() {
		xhtmlDoc.setSelectedStyleSheetSet("Alter 1");
	}

	@Test
	public void getElementgetStyle() {
		DOMElement html = xhtmlDoc.createElement("html");
		xhtmlDoc.appendChild(html);
		assertTrue(html.getAttribute("style").isEmpty());

		assertNull(html.getStyle());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testReferrerPolicy() throws IOException {
		assertEquals("", xhtmlDoc.getReferrerPolicy());
	}

}
