/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
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
		CSSStyleDeclarationRule rule = sheet.createCSSStyleRule();
		rule.setCssText("p {display: table-cell; filter:alpha(opacity=0);}");
		assertEquals(2, rule.getStyle().getLength());
		assertEquals("p {display: table-cell; filter: alpha(opacity=0); }", rule.getCssText());
		assertEquals("p{display:table-cell;filter:alpha(opacity=0)}", rule.getMinifiedCssText());
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
