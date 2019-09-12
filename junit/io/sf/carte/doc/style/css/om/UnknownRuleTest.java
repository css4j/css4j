/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class UnknownRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"/* pre-rule */@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }"));
		sheet.parseStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.UNKNOWN_RULE, sheet.getCssRules().item(0).getType());
		UnknownRule rule = (UnknownRule) sheet.getCssRules().item(0);
		assertEquals(
				"@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }",
				rule.getCssText());
		assertEquals(
				"@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }",
				rule.getMinifiedCssText());
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, rule.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule.getPrecedingComments().get(0));
	}

	@Test
	public void testSetCssTextString() {
		UnknownRule rule = new UnknownRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }");
		assertEquals(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }",
				rule.getMinifiedCssText());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		UnknownRule rule = new UnknownRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }");
		UnknownRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}
}
