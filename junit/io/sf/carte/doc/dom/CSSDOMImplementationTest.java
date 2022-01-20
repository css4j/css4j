/*

 Copyright (c) 2005-2022, Carlos Amengual.

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
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;

import io.sf.carte.doc.style.css.CSSDocument;

public class CSSDOMImplementationTest {
	private static CSSDOMImplementation domImpl;

	@BeforeClass
	public static void setUpBeforeClass() {
		domImpl = new CSSDOMImplementation();
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

}
