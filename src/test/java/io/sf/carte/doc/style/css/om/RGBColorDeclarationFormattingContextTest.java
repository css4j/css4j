/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.StyleFormattingFactory;

public class RGBColorDeclarationFormattingContextTest {

	CSSDocument xhtmlDoc;

	@BeforeEach
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
		elm.getOverrideStyle(null).setCssText(
				"color:lch(80% 67 278);background:radial-gradient(lch(55% 94 40) 10%, lch(57% 67 67),color(display-p3 0.32 0.67 0.48),lch(58% 64 270));voice-family:announcer,male");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("lch(80 67 278)", style.getPropertyValue("color"));
		assertEquals(
				"margin-top: 24pt; margin-bottom: 36pt; background-position: 0% 0%; padding-left: calc(10% - 36pt - 12pt); color: rgb(68.74%, 77.3%, 100%); background-image: radial-gradient(rgb(96.56%, 19.73%, 11.21%) 10%, rgb(76.17%, 46.08%, 4.18%), rgb(10.56%, 68%, 46.33%), rgb(9.21%, 56.27%, 99.01%)); background-size: auto auto; background-origin: padding-box; background-clip: border-box; background-repeat: repeat repeat; background-attachment: scroll; background-color: rgb(0 0 0 / 0); voice-family: announcer, male; ",
				style.getCssText());
		assertEquals(
				"margin-bottom:36pt;margin-top:24pt;background:radial-gradient(rgb(96.56%,19.73%,11.21%) 10%,rgb(76.17%,46.08%,4.18%),rgb(10.56%,68%,46.33%),rgb(9.21%,56.27%,99.01%));padding-left:calc(10% - 36pt - 12pt);color:rgb(68.74%,77.3%,100%);voice-family:announcer,male;",
				style.getMinifiedCssText());
	}

}
