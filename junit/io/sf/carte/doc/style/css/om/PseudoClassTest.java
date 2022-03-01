/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class PseudoClassTest {

	private static StylableDocumentWrapper xhtmlDoc;

	@BeforeClass
	public static void setUpBeforeClass()
		throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		docb.setEntityResolver(new DefaultEntityResolver());
		String str = "<!DOCTYPE html><html><head><style>table#corporate tr:first-child { background-color:#002a55; color:#faf; font-weight:bold } table#corporate tr:nth-child(odd):not(:first-child) { background-color:#f5f5f5; color:#001; } table#corporate tr:nth-child(even) { background-color:#fbf; color:#100 }</style></head><body><table id=\"corporate\"><tbody><tr id=\"tr1\"><td>Test</td><td>.</td></tr><tr id=\"tr2\"><td>.</td><td>.</td></tr><tr id=\"tr3\"><td>.</td><td>.</td></tr><tr id=\"tr4\"><td>.</td><td>.</td></tr><tr id=\"tr5\"><td>.</td><td>.</td></tr></tbody></table></body></html>";
		InputSource source = new InputSource(new StringReader(str));
		Document doc = docb.parse(source);
		doc.getDocumentElement().normalize();
		doc.setDocumentURI("http://www.example.com/xhtml/pseudoclass.html");
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		xhtmlDoc = cssFac.createCSSDocument(doc);
	}

	public void setUp() {
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getElementgetStyle1() {
		CSSElement elm = xhtmlDoc.getElementById("tr1");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(3, styledecl.getLength());
		assertEquals("#faf", styledecl.getPropertyValue("color"));
		assertEquals("#002a55", styledecl.getPropertyValue("background-color"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#002a55;color:#faf;font-weight:bold;",
			styledecl.getMinifiedCssText());

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle2() {
		CSSElement elm = xhtmlDoc.getElementById("tr2");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#100", styledecl.getPropertyValue("color"));
		assertEquals("#fbf", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#fbf;color:#100;", styledecl.getMinifiedCssText());

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle3() {
		CSSElement elm = xhtmlDoc.getElementById("tr3");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#001", styledecl.getPropertyValue("color"));
		assertEquals("#f5f5f5", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#f5f5f5;color:#001;", styledecl.getMinifiedCssText());

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle4() {
		CSSElement elm = xhtmlDoc.getElementById("tr4");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#100", styledecl.getPropertyValue("color"));
		assertEquals("#fbf", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#fbf;color:#100;", styledecl.getMinifiedCssText());

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle5() {
		CSSElement elm = xhtmlDoc.getElementById("tr5");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#001", styledecl.getPropertyValue("color"));
		assertEquals("#f5f5f5", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#f5f5f5;color:#001;", styledecl.getMinifiedCssText());

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

}
