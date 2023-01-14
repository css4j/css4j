/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class InnerTextTest {

	@Test
	public void testGetInnerText() throws IOException {
		DOMDocument xhtmlDoc = TestDOMImplementation.loadDocument("/io/sf/carte/doc/agent/contrib/innertext.html");
		String expected = DOMWriterTest.classPathFile("/io/sf/carte/doc/agent/contrib/innertext-html.bin");
		DOMElement body = xhtmlDoc.getElementById("bodyId");
		//
		assertEquals(expected, body.getInnerText());
	}

	@Test
	public void testGetInnerTextXML() throws IOException {
		DOMDocument xhtmlDoc = TestDOMImplementation.loadXMLDocument("/io/sf/carte/doc/agent/contrib/innertext.xhtml");
		String expected = DOMWriterTest.classPathFile("/io/sf/carte/doc/agent/contrib/innertext.bin");
		DOMElement body = xhtmlDoc.getElementById("bodyId");
		//
		assertEquals(expected, body.getInnerText());
	}

}
