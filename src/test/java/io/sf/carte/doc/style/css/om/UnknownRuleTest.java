/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;

public class UnknownRuleTest {

	private static TestCSSStyleSheetFactory factory;

	private AbstractCSSStyleSheet sheet;

	@BeforeAll
	public static void setUpBeforeAll() {
		factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
	}

	@BeforeEach
	public void setUp() {
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParse() {
		UnknownRule rule = parseStyleSheet(
				"/* pre-rule */@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }");
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.UNKNOWN_RULE, rule.getType());
		assertEquals(
				"/* pre-rule */\n@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }\n",
				rule.getCssText());
		assertEquals(
				"@-webkit-keyframes progress-bar-stripes{from{background-position:40px 0}to{background-position:0 0}}",
				rule.getMinifiedCssText());
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, rule.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule.getPrecedingComments().get(0));
	}

	@Test
	public void testParse2() {
		UnknownRule rule = parseStyleSheet(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }");
		assertEquals(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }\n",
				rule.getCssText());
		assertEquals(
				"@-webkit-keyframes spin{from{-webkit-transform:rotate(0);transform:rotate(0)}to{-webkit-transform:rotate(360deg);transform:rotate(360deg)}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testComment() {
		UnknownRule rule = parseStyleSheet(
				"/* pre-comment */\n@-webkit-keyframes foo{from{background-position:0 0;}/* internal-comment */to{background-position:-200% 0;}} /* post-comment */");
		assertEquals(
				"/* pre-comment */\n@-webkit-keyframes foo{from{background-position:0 0;}/* internal-comment */to{background-position:-200% 0;}} /* post-comment */\n",
				rule.getCssText());
		assertEquals(" pre-comment ", rule.getPrecedingComments().item(0));
		assertEquals(" post-comment ", rule.getTrailingComments().item(0));
		assertEquals(
				"@-webkit-keyframes foo{from{background-position:0 0}to{background-position:-200% 0}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() throws DOMException, IOException {
		UnknownRule rule1 = parseStyleSheet(
				"/* pre-rule */@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }\n/* pre-rule 2 */\n@-webkit-keyframes progress-bar-stripes\n{\nfrom\n{\n    background-position:\n40px  0; \n } \nto \n  { \n background-position:\n 0 0;\n } \n}");
		assertEquals(2, sheet.getCssRules().getLength());
		AbstractCSSRule rule2 = sheet.getCssRules().item(1);
		assertEquals(CSSRule.UNKNOWN_RULE, rule1.getType());
		assertEquals(CSSRule.UNKNOWN_RULE, rule2.getType());
		assertNotNull(rule1.getPrecedingComments());
		assertEquals(1, rule1.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule1.getPrecedingComments().get(0));
		assertNotNull(rule2.getPrecedingComments());
		assertEquals(1, rule2.getPrecedingComments().size());
		assertEquals(" pre-rule 2 ", rule2.getPrecedingComments().get(0));
		assertTrue(rule1.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		UnknownRule rule = parseStyleSheet(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }");
		UnknownRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	private UnknownRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (UnknownRule) sheet.getCssRules().item(0);
	}

}
