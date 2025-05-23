/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

public class StyleRuleTestNS {

	private AbstractCSSStyleSheet sheet;

	@BeforeEach
	public void setUp() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"@namespace svg url('http://www.w3.org/2000/svg');svg|input[svg|foo=bar i]{color:red}svg|input[svg|foo=bar i]{color:red}");
		sheet.parseStyleSheet(re);
	}

	@Test
	public void testGetCssTextAndEquals() {
		StyleRule rule = (StyleRule) sheet.getCssRules().item(1);
		StyleRule rule2 = (StyleRule) sheet.getCssRules().item(2);
		assertEquals("svg|input[svg|foo='bar' i]", rule.getSelectorText());
		assertEquals("svg|input[svg|foo='bar' i]{color:red}", rule.getMinifiedCssText());
		assertTrue(rule.equals(rule2));
		assertTrue(rule2.equals(rule));
		assertEquals(rule.hashCode(), rule2.hashCode());
		rule2.setSelectorText("svg|input[foo=bar i]");
		assertFalse(rule.equals(rule2));
		assertFalse(rule2.equals(rule));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		StyleRule rule = (StyleRule) sheet.getCssRules().item(1);
		assertEquals(1, rule.getSelectorList().getLength());
		assertEquals(1, rule.getStyle().getLength());
		StyleRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getSelectorText(), clon.getSelectorText());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertTrue(clon.equals(rule));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

}
