/*

 Copyright (c) 1998-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.xml.sax.SAXException;

public class DocumentTypeDeclarationTest {

	@Test
	public void DocumentTypeDeclarationStringStringString() {
		DocumentTypeDeclaration dtdecl = new DocumentTypeDeclaration("html", 
				"-//W3C//DTD XHTML 1.0 Transitional//EN", 
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		assertEquals("html", dtdecl.getName());
		assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", dtdecl.getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", dtdecl.getSystemId());
	}

	@Test
	public void parse() throws SAXException {
		DocumentTypeDeclaration dtdecl = DocumentTypeDeclaration.parse(
				DocumentTypeDeclaration.XHTML1_TRA_DTDECL);
		assertEquals("html", dtdecl.getName());
		assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", dtdecl.getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", dtdecl.getSystemId());
		dtdecl = DocumentTypeDeclaration.parse(ContentModelTest.DTD_NONEXISTENT);
		assertEquals("hi", dtdecl.getName());
		assertEquals("-//HI//Does not exist//EN", dtdecl.getPublicId());
		assertNull(dtdecl.getSystemId());
	}

	@Test
	public void parse2() throws SAXException {
		DocumentTypeDeclaration dtdecl = DocumentTypeDeclaration.parse(
				"<!DOCTYPE  html  PUBLIC  \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"  >");
		assertEquals("html", dtdecl.getName());
		assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", dtdecl.getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", dtdecl.getSystemId());
	}

	@Test
	public void testToString() throws SAXException {
		DocumentTypeDeclaration dtdecl = DocumentTypeDeclaration.parse(
				DocumentTypeDeclaration.XHTML1_TRA_DTDECL);
		assertEquals(DocumentTypeDeclaration.XHTML1_TRA_DTDECL, dtdecl.toString());
	}

}
