/*

 Copyright (c) 1998-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;

public class ContentModelTest {

	public static final String DTD_NONEXISTENT = "<!DOCTYPE hi PUBLIC \"-//HI//Does not exist//EN\">";

	private static ContentModel model;

	@BeforeClass
	public static void classFixture() {
		model = ContentModel.getXHTML1TransitionalModel();
	}

	@Test
	public void nonexistantDeclaration() throws SAXException, IOException {
		assertNull(ContentModel.getModel(DTD_NONEXISTENT));
	}

	@Test
	public void getModelFromDocType() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		DocumentType docType = docbuilder.getDOMImplementation().createDocumentType("html",
				"-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		ContentModel model = ContentModel.getModel(docType);
		assertNotNull(model);
		assertFalse(model.isEmpty("html"));
		assertFalse(model.isEmpty("head"));
		assertTrue(model.isEmpty("base"));
		assertTrue(model.isEmpty("link"));
		assertTrue(model.isEmpty("hr"));
	}

	@Test
	public void unparsableDeclaration() {
		try {
			ContentModel.getModel("hi");
			fail("Must throw SAXException");
		} catch (SAXException e) {
		} catch (IOException e) {
			fail("Throws IOException");
		}
	}

	@Test
	public void getXHTML1TransitionalModelFromString() throws SAXException, IOException {
		assertNotNull(ContentModel.getModel(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"));
	}

	@Test
	public void getXHTML1TransitionalModel() {
		assertNotNull(model);
	}

	@Test
	public void isHTMLEmpty() {
		assertFalse(model.isEmpty("html"));
	}

	@Test
	public void isHEADEmpty() {
		assertFalse(model.isEmpty("head"));
	}

	@Test
	public void isBASEEmpty() {
		assertTrue(model.isEmpty("base"));
	}

	@Test
	public void isMETAEmpty() {
		assertTrue(model.isEmpty("meta"));
	}

	@Test
	public void isLINKEmpty() {
		assertTrue(model.isEmpty("link"));
	}

	@Test
	public void isHREmpty() {
		assertTrue(model.isEmpty("hr"));
	}

	@Test
	public void isCOLEmpty() {
		assertTrue(model.isEmpty("col"));
	}

	@Test
	public void isIMGEmpty() {
		assertTrue(model.isEmpty("img"));
	}

	@Test
	public void isSCRIPTEmpty() {
		assertFalse(model.isEmpty("script"));
	}

	@Test
	public void isDIVEmpty() {
		assertFalse(model.isEmpty("div"));
	}

}
