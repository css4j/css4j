/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRuleList;

import io.sf.carte.doc.style.css.CSSNamespaceRule;

public class NamespaceRuleTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testInsertDeleteRuleStringInt() {
		NamespaceRule nsrule = (NamespaceRule) sheet.createNamespaceRule("svg", "http://www.w3.org/2000/svg");
		sheet.addRule(nsrule);
		CSSRuleList rules = sheet.getCssRules();
		assertEquals(1, rules.getLength());
		assertTrue(sheet == nsrule.getParentStyleSheet());
		assertEquals(1, sheet.insertRule("svg|div {color: blue;}", 1));
		assertEquals(2, sheet.insertRule("p {border-top: 1px dashed yellow; }", 2));
		assertEquals(3, rules.getLength());
		try {
			sheet.insertRule("foo|bar {width: 80%}", 3);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			sheet.deleteRule(0);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		sheet.deleteRule(1);
		sheet.deleteRule(0);
		assertEquals(1, rules.getLength());
	}

	@Test
	public void testGetCssText() {
		CSSNamespaceRule nsrule = sheet.createNamespaceRule("svg", "http://www.w3.org/2000/svg");
		assertEquals("@namespace svg url('http://www.w3.org/2000/svg');", nsrule.getCssText());
		assertEquals("@namespace svg url('http://www.w3.org/2000/svg');", nsrule.getMinifiedCssText());
	}

	@Test
	public void testSetPrefix() {
		NamespaceRule nsrule = (NamespaceRule) sheet.createNamespaceRule("svg", "http://www.w3.org/2000/svg");
		sheet.addRule(nsrule);
		CSSRuleList rules = sheet.getCssRules();
		assertEquals(1, rules.getLength());
		assertTrue(sheet == nsrule.getParentStyleSheet());
		assertEquals(1, sheet.insertRule("svg|div {color: blue;}", 1));
		assertEquals(2, sheet.insertRule("p {border-top: 1px dashed yellow; }", 2));
		assertEquals(3, rules.getLength());
		assertEquals("svg|div {\n    color: blue;\n}\n", rules.item(1).getCssText());
		nsrule.setPrefix("foo");
		assertEquals("@namespace foo url('http://www.w3.org/2000/svg');", nsrule.getCssText());
		assertEquals("foo|div {\n    color: blue;\n}\n", rules.item(1).getCssText());
	}

	@Test
	public void testEquals() {
		CSSNamespaceRule nsrule = sheet.createNamespaceRule("svg", "http://www.w3.org/2000/svg");
		CSSNamespaceRule nsrule2 = sheet.createNamespaceRule("foo", "http://www.example.com/examplens");
		assertFalse(nsrule.equals(nsrule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		NamespaceRule rule = (NamespaceRule) sheet.createNamespaceRule("svg",
				"http://www.w3.org/2000/svg");
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null,
				null);
		CSSNamespaceRule cloned = rule.clone(newSheet);
		assertFalse(rule == cloned);
		assertEquals(rule.getPrefix(), cloned.getPrefix());
		assertEquals(rule.getNamespaceURI(), cloned.getNamespaceURI());
		assertTrue(rule.equals(cloned));
		assertEquals(rule.hashCode(), cloned.hashCode());
	}

}
