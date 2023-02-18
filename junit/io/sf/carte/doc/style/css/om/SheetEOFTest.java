/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

class SheetEOFTest {

	AbstractCSSStyleSheet sheet;

	@BeforeEach
	void setUp() throws Exception {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
	}

	@AfterEach
	void tearDown() throws Exception {
		sheet.getCssRules().clear();
	}

	@Test
	void testStyleRuleEOF() throws DOMException, IOException {
		String s = "p:before { margin-left: 2pt";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		StyleRule rule = (StyleRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getStyle().getLength());
	}

	@Test
	void testStyleRuleEOFClosedString() throws DOMException, IOException {
		String s = "p:before { content: 'Hello'";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		StyleRule rule = (StyleRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getStyle().getLength());
	}

	@Test
	void testStyleRuleEOFString() throws DOMException, IOException {
		String s = "p:before { content: 'Hello";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		StyleRule rule = (StyleRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getStyle().getLength());
	}

	@Test
	void testStyleRuleEOFStringEOL() throws DOMException, IOException {
		// The last property must be ignored
		String s = "p:before { margin-left: 2pt; content: 'Hello\n";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		StyleRule rule = (StyleRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getStyle().getLength());
	}

	@Test
	void testMediaRuleEOF() throws DOMException, IOException {
		String s = "@media screen {p { margin-left: 2pt";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		GroupingRule rule = (GroupingRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getCssRules().getLength());

		StyleRule styleRule = (StyleRule) rule.getCssRules().item(0);
		assertEquals(1, styleRule.getStyle().getLength());
	}

	@Test
	void testMediaRuleEOFString() throws DOMException, IOException {
		String s = "@media screen {p { content: 'Hello";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		GroupingRule rule = (GroupingRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getCssRules().getLength());

		StyleRule styleRule = (StyleRule) rule.getCssRules().item(0);
		assertEquals(1, styleRule.getStyle().getLength());
	}

	@Test
	void testSupportsRuleEOF() throws DOMException, IOException {
		String s = "@supports (width: 10rem) {p { margin-left: 2pt";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		GroupingRule rule = (GroupingRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getCssRules().getLength());

		StyleRule styleRule = (StyleRule) rule.getCssRules().item(0);
		assertEquals(1, styleRule.getStyle().getLength());
	}

	@Test
	void testSupportsRuleEOFString() throws DOMException, IOException {
		String s = "@supports (width: 10rem) {p { margin-left: 2pt";
		sheet.parseStyleSheet(new StringReader(s));
		assertEquals(1, sheet.getCssRules().getLength());

		GroupingRule rule = (GroupingRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getCssRules().getLength());

		StyleRule styleRule = (StyleRule) rule.getCssRules().item(0);
		assertEquals(1, styleRule.getStyle().getLength());
	}

}
