/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

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

}
