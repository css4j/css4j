/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.TestDOMImplementation;

public class DirectionalityHelperTest {

	DOMDocument document;

	@Before
	public void setUp() throws IOException {
		document = TestDOMImplementation.loadDocument("/io/sf/carte/doc/dir.html");
	}

	@Test
	public void testGetDirectionality() {
		DOMElement elm = document.getElementById("head");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("h1");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("para1");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("para2");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("h2");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("tableid");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("cell12");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("tr1");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("form1");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("username");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("phonelabel");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("telephone");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("textareartl");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("textarealtr");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("textareaempty");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("bdirtl");
		assertEquals(DirectionalityHelper.Directionality.RTL, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("bdiltr");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("bdiauto");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("bdiempty");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("bdiautoempty");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
		elm = document.getElementById("bdibadempty");
		assertEquals(DirectionalityHelper.Directionality.LTR, DirectionalityHelper.getDirectionality(elm));
	}

}
