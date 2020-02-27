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

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class StyleRuleTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testGetCssText() {
		CSSStyleDeclarationRule rule = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		assertEquals("", rule.getCssText());
		assertEquals("", rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextCompat() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser2.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("p {display: table-cell; filter:alpha(opacity=0);}");
		assertEquals(2, rule.getStyle().getLength());
		assertEquals("p {display: table-cell; filter: alpha(opacity=0); }", rule.getCssText());
		assertEquals("p{display:table-cell;filter:alpha(opacity=0)}", rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() {
		CSSStyleDeclarationRule rule = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setSelectorText("p, div");
		rule.getStyle().setCssText("margin-left: 1em; color: gray;");
		assertEquals("p,div {margin-left: 1em; color: gray; }", rule.getCssText());
		assertEquals("p,div{margin-left:1em;color:gray}", rule.getMinifiedCssText());
		CSSStyleDeclarationRule rule2 = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setSelectorText("p, div");
		rule2.getStyle().setCssText("margin-left: 1em; color: gray;");
		assertTrue(rule.equals(rule2));
		assertTrue(rule.hashCode() == rule2.hashCode());
		//
		rule2.setSelectorText("p");
		assertFalse(rule.equals(rule2));
		//
		rule2.setSelectorText("p, div");
		assertTrue(rule.equals(rule2));
		//
		rule2.getStyle().setCssText("margin-left: 1em; color: blue;");
		assertFalse(rule.equals(rule2));
		//
		rule2.getStyle().setCssText("margin-left: 1em");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		CSSStyleDeclarationRule rule = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"html:root + p:empty,span[foo~='bar'],span[foo='bar'],p:only-child,p:lang(en),p.someclass,a:link,span[class='example'] {border-top-width: 1px; }");
		assertEquals(8, rule.getSelectorList().getLength());
		assertEquals(1, rule.getStyle().getLength());
		CSSStyleDeclarationRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getSelectorText(), clon.getSelectorText());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

}
