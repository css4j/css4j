/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

public class PseudoClassTest {

	private static DOMDocument htmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws SAXException, IOException {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(false));
		builder.setHTMLProcessing(true);
		builder.setXMLReader(parser);
		String str = "<!DOCTYPE html><html><head><style>table#corporate tr:first-child { background-color:#002a55; color:#faf; font-weight:bold } table#corporate tr:nth-child(odd):not(:first-child) { background-color:#f5f5f5; color:#001; } table#corporate tr:nth-child(even) { background-color:#fbf; color:#100 }</style></head><body><table id=\"corporate\"><tbody><tr id=\"tr1\"><td>Test</td><td>.</td></tr><tr id=\"tr2\"><td>.</td><td>.</td></tr><tr id=\"tr3\"><td>.</td><td>.</td></tr><tr id=\"tr4\"><td>.</td><td>.</td></tr><tr id=\"tr5\"><td>.</td><td>.</td></tr></tbody></table></body></html>";
		InputSource source = new InputSource(new StringReader(str));
		htmlDoc = (DOMDocument) builder.parse(source);
		htmlDoc.setDocumentURI("http://www.example.com/xhtml/pseudoclass.html");
	}

	public void setUp() {
		htmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getElementgetStyle1() {
		CSSElement elm = htmlDoc.getElementById("tr1");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(3, styledecl.getLength());
		assertEquals("#faf", styledecl.getPropertyValue("color"));
		assertEquals("#002a55", styledecl.getPropertyValue("background-color"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#002a55;color:#faf;font-weight:bold;",
			styledecl.getMinifiedCssText());

		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(htmlDoc.getErrorHandler().hasErrors());
		assertFalse(htmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle2() {
		CSSElement elm = htmlDoc.getElementById("tr2");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#100", styledecl.getPropertyValue("color"));
		assertEquals("#fbf", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#fbf;color:#100;", styledecl.getMinifiedCssText());

		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(htmlDoc.getErrorHandler().hasErrors());
		assertFalse(htmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle3() {
		CSSElement elm = htmlDoc.getElementById("tr3");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#001", styledecl.getPropertyValue("color"));
		assertEquals("#f5f5f5", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#f5f5f5;color:#001;", styledecl.getMinifiedCssText());

		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(htmlDoc.getErrorHandler().hasErrors());
		assertFalse(htmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle4() {
		CSSElement elm = htmlDoc.getElementById("tr4");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#100", styledecl.getPropertyValue("color"));
		assertEquals("#fbf", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#fbf;color:#100;", styledecl.getMinifiedCssText());

		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(htmlDoc.getErrorHandler().hasErrors());
		assertFalse(htmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getElementgetStyle5() {
		CSSElement elm = htmlDoc.getElementById("tr5");
		assertNotNull(elm);
		CSSComputedProperties styledecl = elm.getComputedStyle(null);
		assertEquals(2, styledecl.getLength());
		assertEquals("#001", styledecl.getPropertyValue("color"));
		assertEquals("#f5f5f5", styledecl.getPropertyValue("background-color"));
		assertEquals("normal", styledecl.getPropertyValue("font-weight"));

		assertEquals("background-color:#f5f5f5;color:#001;", styledecl.getMinifiedCssText());

		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(htmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(htmlDoc.getErrorHandler().hasErrors());
		assertFalse(htmlDoc.getErrorHandler().hasIOErrors());
	}

}
