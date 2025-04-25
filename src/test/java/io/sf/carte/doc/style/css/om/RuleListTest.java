/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

public class RuleListTest {

	private AbstractCSSStyleSheet sheet;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testRuleList() throws DOMException, IOException {
		CSSRuleArrayList list = new CSSRuleArrayList();
		list.append(sheet.createFontFaceRule());
		list.append(sheet.createFontFaceRule());
		assertEquals(2, list.getLength());
		CSSRuleArrayList list2 = new CSSRuleArrayList(list);
		assertEquals(2, list2.getLength());
		list2.append(sheet.createFontFaceRule());
		list2.append(sheet.createPageRule());
		assertEquals(4, list2.getLength());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		CSSRuleArrayList list = new CSSRuleArrayList();
		list.append(sheet.createFontFaceRule());
		list.append(sheet.createFontFaceRule());
		CSSRuleArrayList clon = (CSSRuleArrayList) list.clone();
		assertEquals(list.getLength(), clon.getLength());
		assertTrue(list.equals(clon));
		assertEquals(list.hashCode(), clon.hashCode());
	}

}
