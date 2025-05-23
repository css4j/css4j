/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class InnerTextTest {

	@Test
	public void testGetInnerText() throws IOException {
		DOMDocument xhtmlDoc = TestDOMImplementation.loadDocument("/io/sf/carte/doc/agent/contrib/innertext.html");
		String expected = DOMWriterTest.classPathFile("/io/sf/carte/doc/agent/contrib/innertext-html.bin");
		DOMElement body = xhtmlDoc.getElementById("bodyId");

		assertEquals(expected, body.getInnerText());
	}

	@Test
	public void testGetInnerTextXML() throws IOException {
		DOMDocument xhtmlDoc = TestDOMImplementation.loadXMLDocument("/io/sf/carte/doc/agent/contrib/innertext.xhtml");
		String expected = DOMWriterTest.classPathFile("/io/sf/carte/doc/agent/contrib/innertext.bin");
		DOMElement body = xhtmlDoc.getElementById("bodyId");

		assertEquals(expected, body.getInnerText());
	}

}
