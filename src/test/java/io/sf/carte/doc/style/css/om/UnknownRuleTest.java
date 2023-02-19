/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class UnknownRuleTest {

	private AbstractCSSStyleSheet sheet;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws DOMException, IOException {
		StringReader re = new StringReader(
				"/* pre-rule */@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.UNKNOWN_RULE, sheet.getCssRules().item(0).getType());
		UnknownRule rule = (UnknownRule) sheet.getCssRules().item(0);
		assertEquals(
				"/* pre-rule */\n@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }\n",
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
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }\n",
				rule.getCssText());
		assertEquals(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextStringComment() {
		UnknownRule rule = new UnknownRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"/* pre-comment */\n@-webkit-keyframes foo{from{background-position:0 0;}to{background-position:-200% 0;}}");
		assertEquals(
				"/* pre-comment */\n@-webkit-keyframes foo{from{background-position:0 0;}to{background-position:-200% 0;}}\n",
				rule.getCssText());
		assertEquals(
				"@-webkit-keyframes foo{from{background-position:0 0;}to{background-position:-200% 0;}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() throws DOMException, IOException {
		StringReader re = new StringReader(
				"/* pre-rule */@-webkit-keyframes progress-bar-stripes { from { background-position: 40px 0; } to { background-position: 0 0; } }\n/* pre-rule 2 */\n@-webkit-keyframes progress-bar-stripes\n{\nfrom\n{\n    background-position:\n40px  0; \n } \nto \n  { \n background-position:\n 0 0;\n } \n}");
		sheet.parseStyleSheet(re);
		assertEquals(2, sheet.getCssRules().getLength());
		AbstractCSSRule rule1 = sheet.getCssRules().item(0);
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
