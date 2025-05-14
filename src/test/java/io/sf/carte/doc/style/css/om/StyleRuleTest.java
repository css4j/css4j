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
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.Parser;

public class StyleRuleTest {

	private static TestCSSStyleSheetFactory factory;

	private AbstractCSSStyleSheet sheet;

	@BeforeAll
	public static void setUpBeforeAll() {
		factory = new TestCSSStyleSheetFactory();
	}

	@BeforeEach
	public void setUp() {
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testGetCssText() {
		StyleRule rule = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		assertEquals("", rule.getCssText());
		assertEquals("", rule.getMinifiedCssText());
	}

	@Test
	public void testCompat() throws IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(
				EnumSet.of(Parser.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
		sheet.parseStyleSheet(
				new StringReader("p {display: table-cell; filter:alpha(opacity=0);}"));
		StyleRule rule = (StyleRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getStyle().getLength());
		assertEquals("p {display: table-cell; filter: alpha(opacity=0); }", rule.getCssText());
		assertEquals("p{display:table-cell;filter:alpha(opacity=0)}", rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() {
		StyleRule rule = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setSelectorText("p, div");
		rule.getStyle().setCssText("margin-left: 1em; color: gray;");
		assertEquals("p,div {margin-left: 1em; color: gray; }", rule.getCssText());
		assertEquals("p,div{margin-left:1em;color:gray}", rule.getMinifiedCssText());
		StyleRule rule2 = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setSelectorText("p, div");
		rule2.getStyle().setCssText("margin-left: 1em; color: gray;");
		assertTrue(rule.equals(rule2));
		assertTrue(rule.hashCode() == rule2.hashCode());

		rule2.setSelectorText("p");
		assertFalse(rule.equals(rule2));

		rule2.setSelectorText("p, div");
		assertTrue(rule.equals(rule2));

		rule2.getStyle().setCssText("margin-left: 1em; color: blue;");
		assertFalse(rule.equals(rule2));

		rule2.getStyle().setCssText("margin-left: 1em");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		StyleRule rule = parseStyleSheet(
				"html:root + p:empty,span[foo~='bar'],span[foo='bar'],p:only-child,p:lang(en),p.someclass,a:link,span[class='example'] {border-top-width: 1px; }");
		assertEquals(8, rule.getSelectorList().getLength());
		assertEquals(1, rule.getStyle().getLength());
		StyleRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getSelectorText(), clon.getSelectorText());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	private StyleRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (StyleRule) sheet.getCssRules().item(0);
	}

}
