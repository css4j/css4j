/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.ExtendedCSSRule;

public class ViewportRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws DOMException, IOException {
		StringReader re = new StringReader("@viewport {\norientation: landscape;\n}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.VIEWPORT_RULE, sheet.getCssRules().item(0).getType());
		ViewportRule rule = (ViewportRule) sheet.getCssRules().item(0);
		assertEquals("@viewport {\n    orientation: landscape;\n}\n", rule.getCssText());
		assertEquals("@viewport{orientation:landscape}", rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleMinified() throws DOMException, IOException {
		StringReader re = new StringReader("@viewport{orientation:landscape}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.VIEWPORT_RULE, sheet.getCssRules().item(0).getType());
		ViewportRule rule = (ViewportRule) sheet.getCssRules().item(0);
		assertEquals("@viewport {\n    orientation: landscape;\n}\n", rule.getCssText());
		assertEquals("@viewport{orientation:landscape}", rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleBad() throws DOMException, IOException {
		StringReader re = new StringReader("@viewport foo{orientation: landscape; min-width: 640px;}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testSetCssTextString() {
		ViewportRule rule = new ViewportRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@viewport{orientation:landscape}");
		assertEquals("@viewport{orientation:landscape}", rule.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextStringBad() {
		ViewportRule rule = new ViewportRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@viewport{@orientation: landscape; min-width: 640px;}");
		assertEquals(1, rule.getStyle().getLength());
		assertEquals("@viewport{min-width:640px}", rule.getMinifiedCssText());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText("@namespace svg url('http://www.w3.org/2000/svg');");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		ViewportRule rule = new ViewportRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@viewport {\norientation: landscape;\n}");
		ViewportRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}
}
