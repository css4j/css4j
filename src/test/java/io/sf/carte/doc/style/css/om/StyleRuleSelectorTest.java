/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.Selector.SelectorType;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class StyleRuleSelectorTest {

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
	public void testStyleRule() {
		String cssText = "p {border-top: 1px dashed yellow; }";
		StyleRule rule = parseStyleSheet(cssText);
		assertEquals(cssText, rule.getCssText().replace("\n", ""));
		assertTrue(sheet == rule.getParentStyleSheet());
		CSSStyleDeclaration style = rule.getStyle();
		assertTrue(rule == style.getParentRule());
	}

	@Test
	public void testStyleRuleInvalidValue() {
		String cssText = "p {color: #zzzz; border-top: 1px dashed yellow; }";
		StyleRule rule = parseStyleSheet(cssText);
		assertEquals("p {border-top: 1px dashed yellow; }", rule.getCssText().replace("\n", ""));
		assertTrue(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testStyleRuleIEStarHack() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheetlenient = factory.createStyleSheet(null, null);
		factory.getParserFlags().add(Parser.Flag.STARHACK);
		String cssText = "p {*zoom: 1; }";
		sheetlenient.parseStyleSheet(new StringReader(cssText));
		StyleRule rule = (StyleRule) sheetlenient.getCssRules().item(0);
		assertEquals("p {*zoom: 1; }", rule.getCssText().replace("\n", ""));
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testStyleRuleIEHack() throws IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheetlenient = factory.createStyleSheet(null, null);
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		String cssText = "p {zoom: 1!ie; }";
		sheetlenient.parseStyleSheet(new StringReader(cssText));
		StyleRule rule = (StyleRule) sheetlenient.getCssRules().item(0);
		assertEquals("p {zoom: 1!ie; }", rule.getCssText().replace("\n", ""));
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());

		// Non-lenient parsing
		StyleRule nonLenientRule = parseStyleSheet(cssText);
		assertEquals("p {}", nonLenientRule.getCssText());

		assertTrue(nonLenientRule.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(nonLenientRule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testStyleRuleInvalidRule() {
		String cssText = "p border-top: 1px dashed yellow; }.someclass, h1 > p, a:visited {color: blue; }";
		StyleRule rule = parseStyleSheet(cssText);
		assertEquals(".someclass,h1>p,a:visited{color:blue}", rule.getMinifiedCssText());
	}

	@Test
	public void testStyleRuleInvalidRule2() {
		String cssText = ".someclass, h1 > p, span[foo~='bar'], a:visited color: rgb(127, 23, 142); }p {border-top: 1px dashed yellow; }";
		StyleRule rule = parseStyleSheet(cssText);
		assertEquals("p {border-top: 1px dashed yellow; }", rule.getCssText().replace("\n", ""));
	}

	@Test
	public void testStyleRuleInvalidRule3() {
		String cssText = ".someclass, h1 > p, span[foo~='bar'], a:visited color: rgb(127, 23, 142); }";
		assertNull(parseStyleSheet(cssText));
	}

	@Test
	public void testStyleRuleMultipleRule() {
		String cssText = "p {border-top: 1px dashed yellow; }a:visited {color: orange; }";
		StyleRule rule = parseStyleSheet(cssText);
		assertEquals("p {border-top: 1px dashed yellow; }", rule.getCssText());
	}

	@Test
	public void testCloneAbstractCSSStyleSheetInt() {
		String cssText = "p {border-top: 1px dashed yellow; }";
		StyleRule rule = parseStyleSheet(cssText);
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null, null);
		StyleRule cloned = rule.clone(newSheet);
		assertEquals(cssText, cloned.getCssText().replace("\n", ""));
		assertTrue(cloned == cloned.getStyle().getParentRule());
	}

	@Test
	public void testSelectorTextAttributeSelector2() {
		String cssText = "table[align=\"center\"] > caption[align=\"left\"] {border-top-width: 1px; }";
		StyleRule rule = parseStyleSheet(cssText);
		SelectorList list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[align='center']>caption[align='left']", rule.getSelectorText());

		rule = parseStyleSheet("table[foo=\\*bar] > caption[foo=\"'\"] {border-top-width: 1px; }");
		list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[foo='*bar']>caption[foo=\"'\"]", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextAttributeSelector2DQ() {
		String cssText = "table[align=\"center\"] > caption[align=\"left\"] {border-top-width: 1px; }";
		StyleRule rule = parseStyleSheet(cssText, CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
		SelectorList list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[align=\"center\"]>caption[align=\"left\"]", rule.getSelectorText());

		rule = parseStyleSheet("table[foo=\\*bar] > caption[foo=\"'\"] {border-top-width: 1px; }",
				CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
		list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[foo=\"*bar\"]>caption[foo=\"'\"]", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorNth() {
		StyleRule rule = parseStyleSheet("p:first-child {border-top-width: 1px; }");
		assertEquals("p:first-child", rule.getSelectorText());
		rule = parseStyleSheet("p:first-of-type {border-top-width: 1px; }");
		assertEquals("p:first-of-type", rule.getSelectorText());
		rule = parseStyleSheet("p:last-child {border-top-width: 1px; }");
		assertEquals("p:last-child", rule.getSelectorText());
		rule = parseStyleSheet("p:last-of-type {border-top-width: 1px; }");
		assertEquals("p:last-of-type", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-child(2) {border-top-width: 1px; }");
		assertEquals("p:nth-child(2)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-child(2n) {border-top-width: 1px; }");
		assertEquals("p:nth-child(2n)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-child(odd) {border-top-width: 1px; }");
		assertEquals("p:nth-child(odd)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-child(even) {border-top-width: 1px; }");
		assertEquals("p:nth-child(even)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-child(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-child(2n+1)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-child(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(2n+1)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-child(n + 2) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(n+2)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-child(-n + 2) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(-n+2)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-child(odd) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(odd)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-child(even) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(even)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-of-type(2n) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(2n)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-of-type(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(2n+1)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-of-type(2) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(2)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-of-type(-2n) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(-2n)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-of-type(2) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(2)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-of-type(2n) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(2n)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-of-type(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(2n+1)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-of-type(even) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(even)", rule.getSelectorText());
		rule = parseStyleSheet("p:nth-last-of-type(odd) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(odd)", rule.getSelectorText());
		rule = parseStyleSheet("*|p {border-top-width: 1px; }");
		assertEquals("p", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector9() {
		StyleRule rule = parseStyleSheet("h1~pre {border-top-width: 1px; }");
		assertEquals("h1~pre", rule.getSelectorText());

		rule = parseStyleSheet(":dir(rtl) {border-top-width: 1px; }");
		assertEquals(":dir(rtl)", rule.getSelectorText());

		rule = parseStyleSheet("::first-letter {border-top-width: 1px; }");
		assertEquals("::first-letter", rule.getSelectorText());

		rule = parseStyleSheet(".foo\\/1 {border-top-width: 1px; }");
		assertEquals(".foo\\/1", rule.getSelectorText());

		rule = parseStyleSheet("foo#bar\\/1 {border-top-width: 1px; }");
		assertEquals("foo#bar\\/1", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector10() {
		StyleRule rule = parseStyleSheet("#-\\31 23\\\\a {border-top-width: 1px; }");
		SelectorList selist = rule.getSelectorList();
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("-123\\a", ((AttributeCondition) cond).getValue());
		assertEquals("#\\-123\\\\a", rule.getSelectorText());

		rule = parseStyleSheet(".-\\31 23\\\\a {border-top-width: 1px; }");
		assertEquals(".\\-123\\\\a", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorEscapedType() {
		StyleRule rule = parseStyleSheet("\\.foo {border-top-width: 1px; }");
		assertEquals("\\.foo", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorLang() {
		StyleRule rule = parseStyleSheet("p:lang(zh, \"*-hant\") {border-top-width: 1px; }");
		assertEquals("p:lang(zh,\"*-hant\")", rule.getSelectorText());
		rule = parseStyleSheet("p:lang(zh, '*-hant') {border-top-width: 1px; }");
		assertEquals("p:lang(zh,'*-hant')", rule.getSelectorText());
		rule = parseStyleSheet("p:lang(es, fr, \\*-Latn) {border-top-width: 1px; }");
		assertEquals("p:lang(es,fr,\\*-Latn)", rule.getSelectorText());
		rule = parseStyleSheet("p:lang(zh, \"*-hant\") {border-top-width: 1px; }",
				CSSStyleSheetFactory.FLAG_STRING_SINGLE_QUOTE);
		assertEquals("p:lang(zh,'*-hant')", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorNot() {
		StyleRule rule = parseStyleSheet(":not(:hover) {border-top-width: 1px; }");
		assertEquals(":not(:hover)", rule.getSelectorText());
		rule = parseStyleSheet("p:not(:hover) {border-top-width: 1px; }");
		assertEquals("p:not(:hover)", rule.getSelectorText());
		rule = parseStyleSheet(":not(:lang(en)) {border-top-width: 1px; }");
		assertEquals(":not(:lang(en))", rule.getSelectorText());
		rule = parseStyleSheet(":not([type]) {border-top-width: 1px; }");
		assertEquals(":not([type])", rule.getSelectorText());
		rule = parseStyleSheet(":not(#foo) {border-top-width: 1px; }");
		assertEquals(":not(#foo)", rule.getSelectorText());
		rule = parseStyleSheet(
				"html:not(.foo) body:not(.bar) .myclass.otherclass {border-top-width: 1px; }");
		assertEquals("html:not(.foo) body:not(.bar) .myclass.otherclass", rule.getSelectorText());
	}

	@Test
	public void testEquals() {
		StyleRule rule = parseStyleSheet("p {border-top: 1px dashed yellow; }");
		StyleRule rule2 = parseStyleSheet("p {border-top: 1px dashed yellow; }");
		assertTrue(rule.equals(rule2));
		rule2 = parseStyleSheet("p {border-top: 1px dashed; }");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals2() {
		StyleRule rule = parseStyleSheet("p {border-top: 1px dashed yellow; }");
		StyleRule rule2 = parseStyleSheet("p {border-top: 1px dashed yellow; }");
		assertTrue(rule.equals(rule2));
		rule2 = parseStyleSheet("p,div {border-top: 1px dashed yellow; }");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals3() {
		StyleRule rule = parseStyleSheet(".foo+::after {border-top-width: 1px}");
		StyleRule rule2 = parseStyleSheet(".foo+::after {border-top-width: 1px}");
		assertTrue(rule.equals(rule2));
		rule2 = parseStyleSheet(".foo+::before {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals4() {
		StyleRule rule = parseStyleSheet(".foo .bar #fooid, .foo+::after {border-top-width: 1px}");
		StyleRule rule2 = parseStyleSheet(".foo .bar #fooid,.foo+::after {border-top-width: 1px}");
		assertTrue(rule.equals(rule2));
		rule2 = parseStyleSheet(".foo .bar #fooid, .foo+::before {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
		rule2 = parseStyleSheet(".foo .bar #fooid {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
		rule2 = parseStyleSheet(".foo+::after {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
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

	private StyleRule parseStyleSheet(String cssText, short flag) {
		TestCSSStyleSheetFactory ffactory = new TestCSSStyleSheetFactory();
		ffactory.setFactoryFlag(flag);
		AbstractCSSStyleSheet sheet = ffactory.createStyleSheet(null, null);
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (StyleRule) sheet.getCssRules().item(0);
	}

}
