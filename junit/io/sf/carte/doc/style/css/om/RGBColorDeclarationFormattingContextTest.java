/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.StyleFormattingFactory;

public class RGBColorDeclarationFormattingContextTest {

	CSSDocument xhtmlDoc;

	@Before
	public void setUp() throws IOException, DocumentException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(false);
		StyleFormattingFactory sff = new RGBStyleFormattingFactory();
		factory.setStyleFormattingFactory(sff);
		xhtmlDoc = DOMCSSStyleSheetFactoryTest.sampleXHTML(factory);
	}

	@Test
	public void testWriteValue() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		elm.getOverrideStyle(null).setCssText("color:lch(80% 67 278)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("lch(80% 67 278)", style.getPropertyValue("color"));
		assertEquals(
				"margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); color: rgb(68.74%, 77.3%, 100%); ",
				style.getCssText());
		assertEquals(
				"margin-bottom:36pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);color:lch(80% 67 278);",
				style.getMinifiedCssText());
	}

}
