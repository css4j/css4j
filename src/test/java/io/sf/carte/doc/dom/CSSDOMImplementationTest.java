/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.StringTokenizer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.property.NumberValue;

public class CSSDOMImplementationTest {
	private static CSSDOMImplementation domImpl;

	@BeforeAll
	public static void setUpBeforeClass() {
		domImpl = new CSSDOMImplementation();
	}

	@Test
	public void testNewDocument() {
		DOMDocument document = domImpl.newDocument();
		assertFalse(document instanceof HTMLDocument);
	}

	@Test
	public void testNewHTMLDocument() {
		DOMDocument document = domImpl.newHTMLDocument();
		assertTrue(document instanceof HTMLDocument);
		assertNotNull(document.getDocumentElement());
		assertNotNull(document.getDoctype());
	}

	@Test
	public void testCreateDocument() {
		DOMDocument document = domImpl.createDocument(null, null, null);
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, document.getComplianceMode());
	}

	@Test
	public void testCreateDocument2() {
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		assertNull(doctype.getNextSibling());
		assertNull(doctype.getPreviousSibling());
		assertNull(doctype.getParentNode());
		assertNull(doctype.getOwnerDocument());
		doctype.setNodeValue("foo"); // No effect
		assertFalse(doctype.hasAttributes());
		DOMDocument document = domImpl.createDocument(null, null, doctype);
		assertEquals(CSSDocument.ComplianceMode.STRICT, document.getComplianceMode());
		assertNull(doctype.getNextSibling());
		assertNull(doctype.getPreviousSibling());
		assertTrue(document == doctype.getOwnerDocument());
		try {
			domImpl.createDocument(null, null, doctype);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
	}

	@Test
	public void testCreateDocument3() {
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		assertNull(doctype.getNextSibling());
		assertNull(doctype.getPreviousSibling());
		DOMDocument document = domImpl.createDocument(null, "html", doctype);
		assertEquals("CSS1Compat", document.getCompatMode());
		assertEquals(CSSDocument.ComplianceMode.STRICT, document.getComplianceMode());
		DOMElement docelm = document.getDocumentElement();
		assertNotNull(docelm);
		assertTrue(docelm == doctype.getNextSibling());
		assertNull(doctype.getPreviousSibling());
		assertTrue(doctype == docelm.getPreviousSibling());
		assertNull(docelm.getNextSibling());
		assertTrue(document == docelm.getParentNode());
		assertTrue(document == docelm.getOwnerDocument());
		assertTrue(document == doctype.getOwnerDocument());
	}

	@Test
	public void testCreateDocumentType() {
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		assertEquals("<!DOCTYPE html>", doctype.toString());
		//
		doctype = domImpl.createDocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", null);
		assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">", doctype.toString());
		//
		doctype = domImpl.createDocumentType("html", null, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
		assertEquals("<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
				doctype.toString());
		//
		doctype = domImpl.createDocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
		assertEquals(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
				doctype.toString());
		//
		try {
			domImpl.createDocumentType("html><html><injection/", null, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		//
		doctype = domImpl.createDocumentType("html", "\"><injection foo=\"", null);
		assertEquals("<!DOCTYPE html PUBLIC \"&quot;&gt;&lt;injection foo=&quot;\">", doctype.toString());
		//
		doctype = domImpl.createDocumentType("html", null, "\"><injection foo=\"");
		assertEquals("<!DOCTYPE html SYSTEM \"&quot;&gt;&lt;injection foo=&quot;\">", doctype.toString());
	}

	@Test
	public void getFontSize() {
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
		StyleRule rule = defaultStyleRule("h3", "font-size");
		assertNotNull(rule);
		NumberValue val = (NumberValue) rule.getStyle().getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(12f * val.getFloatValue(CSSUnit.CSS_EM), style.getComputedFontSize(), 0.05);
		assertFalse(newdoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(newdoc.getErrorHandler().hasComputedStyleWarnings());
	}

	private StyleRule defaultStyleRule(String selectorText, String propertyName) {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		CSSRuleList rules = sheet.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			if (rule instanceof StyleRule) {
				String selText = ((StyleRule) rule).getSelectorText();
				// Small hack
				StringTokenizer st = new StringTokenizer(selText, ",");
				while (st.hasMoreElements()) {
					String selector = st.nextToken();
					if (selector.equals(selectorText)) {
						if (((StyleRule) rule).getStyle().getPropertyCSSValue(propertyName) != null) {
							return (StyleRule) rule;
						}
						break;
					}
				}
			}
		}
		return null;
	}

}
