/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AbstractCSSStyleSheetTest {

	@Test
	public void testParseRelAttribute() {
		assertEquals(0, AbstractCSSStyleSheet.parseRelAttribute(""));
		assertEquals(-1, AbstractCSSStyleSheet.parseRelAttribute("foo"));
		assertEquals(0, AbstractCSSStyleSheet.parseRelAttribute("stylesheet"));
		assertEquals(-1, AbstractCSSStyleSheet.parseRelAttribute("stylesheet foo"));
		assertEquals(0, AbstractCSSStyleSheet.parseRelAttribute("STYLESHEET"));
		assertEquals(0, AbstractCSSStyleSheet.parseRelAttribute(" stylesheet "));
		assertEquals(1, AbstractCSSStyleSheet.parseRelAttribute("alternate"));
		assertEquals(1, AbstractCSSStyleSheet.parseRelAttribute("ALTERNATE"));
		assertEquals(1, AbstractCSSStyleSheet.parseRelAttribute("alternate stylesheet"));
		assertEquals(1, AbstractCSSStyleSheet.parseRelAttribute(" alternate  stylesheet "));
		assertEquals(1, AbstractCSSStyleSheet.parseRelAttribute("stylesheet alternate"));
	}

}
