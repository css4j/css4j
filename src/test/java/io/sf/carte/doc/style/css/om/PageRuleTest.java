/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class PageRuleTest {

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
	public void testParsePageRule() throws DOMException, IOException {
		StringReader re = new StringReader("@page {margin-top: 20%;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals("@page {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page{margin-top:20%}", pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRuleNested() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media print {@page {margin-top: 20%;}h3 {width: 80%}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mediarule = (MediaRule) rule;
		assertEquals("print", mediarule.getMedia().getMedia());
		assertEquals(2, mediarule.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, mediarule.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) mediarule.getCssRules().item(0);
		assertEquals("@page {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page{margin-top:20%}", pagerule.getMinifiedCssText());
		assertEquals(
				"@media print {\n    @page {\n        margin-top: 20%;\n    }\n    h3 {\n        width: 80%;\n    }\n}\n",
				rule.getCssText());
		assertEquals("@media print{@page{margin-top:20%}h3{width:80%}}", rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRulePageSelector() throws DOMException, IOException {
		StringReader re = new StringReader("@page foo{margin-top: 20%;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals("@page foo {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page foo{margin-top:20%}", pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRulePseudoPage() throws DOMException, IOException {
		StringReader re = new StringReader("@page :first {margin-top: 20%;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals("@page :first {\n    margin-top: 20%;\n}\n", pagerule.getCssText());
		assertEquals("@page :first{margin-top:20%}", pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRulePseudoPage2() throws DOMException, IOException {
		StringReader re = new StringReader("@page foo:first,bar:right {margin-top: 20%;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals("@page foo:first,bar:right {\n    margin-top: 20%;\n}\n",
				pagerule.getCssText());
		assertEquals("@page foo:first,bar:right{margin-top:20%}", pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRulePseudoPageMargin() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@page foo:first,bar:right {margin-top: 20%;@top-left {margin-top: 0.7em; margin-left:1ex}@bottom-center {content: counter(page); }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals(
				"@page foo:first,bar:right {\n    margin-top: 20%;\n    @top-left {\n        margin-top: 0.7em;\n        margin-left: 1ex;\n    }\n    @bottom-center {\n        content: counter(page);\n    }\n}\n",
				pagerule.getCssText());
		assertEquals(
				"@page foo:first,bar:right{margin-top:20%;@top-left{margin-top:.7em;margin-left:1ex}@bottom-center{content:counter(page)}}",
				pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRulePseudoPageMarginEOF() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@page foo:first,bar:right {margin-top: 20%;@top-left {margin-top: 0.7em; margin-left:1ex}@bottom-center {content: 'foo'");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals(
				"@page foo:first,bar:right {\n    margin-top: 20%;\n    @top-left {\n        margin-top: 0.7em;\n        margin-left: 1ex;\n    }\n    @bottom-center {\n        content: 'foo';\n    }\n}\n",
				pagerule.getCssText());
		assertEquals(
				"@page foo:first,bar:right{margin-top:20%;@top-left{margin-top:.7em;margin-left:1ex}@bottom-center{content:'foo'}}",
				pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRulePseudoPageMarginStringEOF() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@page foo:first,bar:right {margin-top: 20%;@top-left {margin-top: 0.7em; margin-left:1ex}@bottom-center {content: 'foo");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals(
				"@page foo:first,bar:right {\n    margin-top: 20%;\n    @top-left {\n        margin-top: 0.7em;\n        margin-left: 1ex;\n    }\n    @bottom-center {\n        content: 'foo';\n    }\n}\n",
				pagerule.getCssText());
		assertEquals(
				"@page foo:first,bar:right{margin-top:20%;@top-left{margin-top:.7em;margin-left:1ex}@bottom-center{content:'foo'}}",
				pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParsePageRuleWithMargin() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@page :first {margin-top: 20%;@top-left {content: 'foo'; color: blue;}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		MarginRuleList marginlist = pagerule.getMarginRules();
		assertEquals(1, marginlist.getLength());
		assertEquals("@top-left{content:'foo';color:blue}", marginlist.get(0).getMinifiedCssText());
		assertEquals(
				"@page :first {\n    margin-top: 20%;\n    @top-left {\n        content: 'foo';\n        color: blue;\n    }\n}\n",
				pagerule.getCssText());
		assertEquals("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}",
				pagerule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParse() {
		PageRule rule = parseStyleSheet("@page {margin-top: 20%;}");
		assertEquals("", rule.getSelectorText());
		assertEquals("@page{margin-top:20%}", rule.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParse2() {
		PageRule rule = parseStyleSheet("@page{margin-top: 20%;}");
		assertEquals("", rule.getSelectorText());
		assertEquals("@page{margin-top:20%}", rule.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParseCR() {
		PageRule rule = parseStyleSheet("@page\n{margin-top: 20%;}");
		assertEquals("", rule.getSelectorText());
		assertEquals("@page{margin-top:20%}", rule.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParseMargin() {
		PageRule rule = parseStyleSheet(
				"@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}");
		assertEquals("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}",
				rule.getMinifiedCssText());
		assertEquals(":first", rule.getSelectorText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParsePreComment() {
		PageRule rule = parseStyleSheet("/* pre-rule */ @page/* pre-sel */:first/* pre-lcb */{"
				+ "margin-top:20%/*post-value*/;/* pre-margin */@top-left/* pre-margin-body */{"
				+ "content:'foo';color:blue}/* post-margin-body */}");
		assertEquals("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}",
				rule.getMinifiedCssText());
		assertEquals(":first", rule.getSelectorText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParsePageSelector() {
		PageRule rule = parseStyleSheet("@page foo:first{margin-top: 20%;}");
		assertEquals("foo:first", rule.getSelectorText());
		assertEquals("@page foo:first{margin-top:20%}", rule.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParsePageSelectorList() {
		PageRule rule = parseStyleSheet("@page foo:first,bar:right {margin-top: 20%;}");
		assertEquals("foo:first,bar:right", rule.getSelectorText());
		assertEquals("@page foo:first,bar:right {\n    margin-top: 20%;\n}\n", rule.getCssText());
		assertEquals("@page foo:first,bar:right{margin-top:20%}", rule.getMinifiedCssText());

		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParseWrongPageSelectorList() {
		assertNull(parseStyleSheet("@page foo :first,bar :right {margin-top: 20%;}"));
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParsePageRuleErrorRecovery() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@page foo:first,bar:right {@top-right; @bottom-left ; margin-top:20%;margin-bottom:;1px;"
						+ "@top-left {margin-top; : 0.7em; margin-left:1ex}@bottom-center {content: counter(page); }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(0).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(0);
		assertEquals(
				"@page foo:first,bar:right {\n    margin-top: 20%;\n    @top-left {\n        margin-left: 1ex;\n    }\n    @bottom-center {\n        content: counter(page);\n    }\n}\n",
				pagerule.getCssText());
		assertEquals(
				"@page foo:first,bar:right{margin-top:20%;@top-left{margin-left:1ex}@bottom-center{content:counter(page)}}",
				pagerule.getMinifiedCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testEquals() {
		PageRule rule = parseStyleSheet("@page {margin-top: 20%;}");
		PageRule rule2 = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2 = parseStyleSheet("@page :first {margin-top: 20%;}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals2() {
		PageRule rule = parseStyleSheet("@page {margin-top: 20%;}");
		PageRule rule2 = new PageRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2 = parseStyleSheet("@page {margin-top: 20%;@top-left{content:'foo';color:blue}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		PageRule rule = parseStyleSheet("@page :first {margin-top: 20%;}");
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
		PageRule rule = parseStyleSheet(
				"@page :first {margin-top: 20%;@top-left{content:'foo';color:blue}}");
		PageRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertNotNull(rule.getSelectorText());
		assertTrue(rule.getMarginRules().equals(clon.getMarginRules()));
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
		assertEquals(rule.getCssText(), clon.getCssText());
	}

	private PageRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (PageRule) sheet.getCssRules().item(0);
	}

}
