/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class PageRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
	}

	@Test
	public void testParsePageRule() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader("@page {margin-top: 20%;}"));
		sheet.parseStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals("@page {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page{margin-top:20%}", pagerule.getMinifiedCssText());
	}

	@Test
	public void testParsePageRuleNested() throws DOMException, IOException {
		InputSource source = new InputSource(
				new StringReader("@media print {@page {margin-top: 20%;}h3 {width: 80%}}"));
		sheet.parseStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mediarule = (MediaRule) rule;
		assertEquals("print", mediarule.getMedia().getMediaText());
		assertEquals(2, mediarule.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, mediarule.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) mediarule.getCssRules().item(0);
		assertEquals("@page {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page{margin-top:20%}", pagerule.getMinifiedCssText());
		assertEquals(
				"@media print {\n    @page {\n        margin-top: 20%;\n    }\n    h3 {\n        width: 80%;\n    }\n}\n",
				rule.getCssText());
		assertEquals("@media print{@page{margin-top:20%}h3{width:80%}}", rule.getMinifiedCssText());
	}

	@Test
	public void testParsePageRulePseudoPage() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader("@page :first {margin-top: 20%;}"));
		sheet.parseStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals("@page :first {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page :first{margin-top:20%}", pagerule.getMinifiedCssText());
	}

	@Test
	public void testParsePageRuleWithMargin() throws DOMException, IOException {
		InputSource source = new InputSource(
				new StringReader("@page :first {margin-top: 20%;@top-left {content: 'foo'; color: blue;}}"));
		sheet.parseStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		MarginRuleList marginlist = pagerule.getMarginRules();
		assertEquals(1, marginlist.getLength());
		assertEquals("@top-left{content:'foo';color:blue}", marginlist.get(0).getMinifiedCssText());
		assertEquals(
				"@page :first {\n    margin-top: 20%;\n    @top-left {\n        content: 'foo';\n        color: blue;\n    }\n}\n",
				pagerule.getCssText());
		assertEquals("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}", pagerule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString() throws DOMException {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@page {margin-top: 20%;}");
		assertEquals("", rule.getSelectorText());
		assertEquals("@page{margin-top:20%}", rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString2() throws DOMException {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}");
		assertEquals("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}", rule.getMinifiedCssText());
		assertEquals(":first", rule.getSelectorText());
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
	}

	@Test
	public void testSetCssTextStringWrongRule2() {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText("@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } li {display: list-item; }}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testEquals() {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@page {margin-top: 20%;}");
		PageRule rule2 = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@page :first {margin-top: 20%;}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals2() {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@page {margin-top: 20%;}");
		PageRule rule2 = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@page {margin-top: 20%;@top-left{content:'foo';color:blue}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@page :first {margin-top: 20%;}");
		PageRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertNotNull(rule.getSelectorText());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet2() {
		PageRule rule = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@page :first {margin-top: 20%;@top-left{content:'foo';color:blue}}");
		PageRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertNotNull(rule.getSelectorText());
		assertTrue(rule.getMarginRules().equals(clon.getMarginRules()));
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
		assertEquals(rule.getCssText(), clon.getCssText());
	}

}
