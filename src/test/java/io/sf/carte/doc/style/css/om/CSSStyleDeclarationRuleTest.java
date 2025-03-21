/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

public class CSSStyleDeclarationRuleTest {

	private TestCSSStyleSheetFactory factory;

	private AbstractCSSStyleSheet sheet;

	@BeforeEach
	public void setUp() {
		factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testSetCssTextString() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		String cssText = "p {border-top: 1px dashed yellow; }";
		rule.setCssText(cssText);
		assertEquals(cssText, rule.getCssText().replace("\n", ""));
		assertTrue(sheet == rule.getParentStyleSheet());
		CSSStyleDeclaration style = rule.getStyle();
		assertTrue(rule == style.getParentRule());
	}

	@Test
	public void testSetCssTextStringInvalidValue() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		String cssText = "p {color: #zzzz; border-top: 1px dashed yellow; }";
		rule.setCssText(cssText);
		assertEquals("p {border-top: 1px dashed yellow; }", rule.getCssText().replace("\n", ""));
		assertTrue(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextStringIEHack() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheetlenient = factory.createStyleSheet(null, null);
		factory.getParserFlags().add(Parser.Flag.STARHACK);
		CSSStyleDeclarationRule rule = sheetlenient.createStyleRule();
		String cssText = "p {*zoom: 1; }";
		rule.setCssText(cssText);
		assertEquals("p {*zoom: 1; }", rule.getCssText().replace("\n", ""));
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
		rule = sheet.createStyleRule();
		rule.setCssText(cssText);
		assertEquals("p {}", rule.getCssText());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testSetCssTextStringInvalidRule() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		String cssText = "p border-top: 1px dashed yellow; }.someclass, h1 > p, a:visited {color: blue; }";
		rule.setCssText(cssText);
		assertEquals(".someclass,h1>p,a:visited{color:blue}", rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextStringInvalidRule2() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		String cssText = ".someclass, h1 > p, span[foo~='bar'], a:visited color: rgb(127, 23, 142); }p {border-top: 1px dashed yellow; }";
		rule.setCssText(cssText);
		assertEquals("p {border-top: 1px dashed yellow; }", rule.getCssText().replace("\n", ""));
	}

	@Test
	public void testSetCssTextStringInvalidRule3() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		assertEquals("", rule.getCssText());
		String cssText = ".someclass, h1 > p, span[foo~='bar'], a:visited color: rgb(127, 23, 142); }";
		rule.setCssText(cssText);
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testSetCssTextStringMultipleRule() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		sheet.addRule(rule);
		String cssText = "p {border-top: 1px dashed yellow; }a:visited {color: orange; }";
		try {
			rule.setCssText(cssText);
			fail("Should throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertEquals("p {border-top: 1px dashed yellow; }", rule.getCssText());
	}

	@Test
	public void testCloneAbstractCSSStyleSheetInt() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		String cssText = "p {border-top: 1px dashed yellow; }";
		rule.setCssText(cssText);
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null, null);
		CSSStyleDeclarationRule cloned = rule.clone(newSheet);
		assertEquals(cssText, cloned.getCssText().replace("\n", ""));
		assertTrue(cloned == cloned.getStyle().getParentRule());
	}

	@Test
	public void testSelectorTextAttributeSelector2() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("table[align=\"center\"] > caption[align=\"left\"] {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[align='center']>caption[align='left']", rule.getSelectorText());

		rule.setCssText("table[foo=\\*bar] > caption[foo=\"'\"] {border-top-width: 1px; }");
		list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[foo='*bar']>caption[foo=\"'\"]", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextAttributeSelector2DQ() {
		CSSStyleDeclarationRule rule = createCSSStyleDeclarationRule(CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
		rule.setCssText("table[align=\"center\"] > caption[align=\"left\"] {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[align=\"center\"]>caption[align=\"left\"]", rule.getSelectorText());

		rule.setCssText("table[foo=\\*bar] > caption[foo=\"'\"] {border-top-width: 1px; }");
		list = rule.getSelectorList();
		assertEquals(1, list.getLength());
		assertEquals("table[foo=\"*bar\"]>caption[foo=\"'\"]", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorNth() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("p:first-child {border-top-width: 1px; }");
		assertEquals("p:first-child", rule.getSelectorText());
		rule.setCssText("p:first-of-type {border-top-width: 1px; }");
		assertEquals("p:first-of-type", rule.getSelectorText());
		rule.setCssText("p:last-child {border-top-width: 1px; }");
		assertEquals("p:last-child", rule.getSelectorText());
		rule.setCssText("p:last-of-type {border-top-width: 1px; }");
		assertEquals("p:last-of-type", rule.getSelectorText());
		rule.setCssText("p:nth-child(2) {border-top-width: 1px; }");
		assertEquals("p:nth-child(2)", rule.getSelectorText());
		rule.setCssText("p:nth-child(2n) {border-top-width: 1px; }");
		assertEquals("p:nth-child(2n)", rule.getSelectorText());
		rule.setCssText("p:nth-child(odd) {border-top-width: 1px; }");
		assertEquals("p:nth-child(odd)", rule.getSelectorText());
		rule.setCssText("p:nth-child(even) {border-top-width: 1px; }");
		assertEquals("p:nth-child(even)", rule.getSelectorText());
		rule.setCssText("p:nth-child(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-child(2n+1)", rule.getSelectorText());
		rule.setCssText("p:nth-last-child(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(2n+1)", rule.getSelectorText());
		rule.setCssText("p:nth-last-child(n + 2) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(n+2)", rule.getSelectorText());
		rule.setCssText("p:nth-last-child(-n + 2) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(-n+2)", rule.getSelectorText());
		rule.setCssText("p:nth-last-child(odd) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(odd)", rule.getSelectorText());
		rule.setCssText("p:nth-last-child(even) {border-top-width: 1px; }");
		assertEquals("p:nth-last-child(even)", rule.getSelectorText());
		rule.setCssText("p:nth-of-type(2n) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(2n)", rule.getSelectorText());
		rule.setCssText("p:nth-of-type(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(2n+1)", rule.getSelectorText());
		rule.setCssText("p:nth-of-type(2) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(2)", rule.getSelectorText());
		rule.setCssText("p:nth-of-type(-2n) {border-top-width: 1px; }");
		assertEquals("p:nth-of-type(-2n)", rule.getSelectorText());
		rule.setCssText("p:nth-last-of-type(2) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(2)", rule.getSelectorText());
		rule.setCssText("p:nth-last-of-type(2n) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(2n)", rule.getSelectorText());
		rule.setCssText("p:nth-last-of-type(2n + 1) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(2n+1)", rule.getSelectorText());
		rule.setCssText("p:nth-last-of-type(even) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(even)", rule.getSelectorText());
		rule.setCssText("p:nth-last-of-type(odd) {border-top-width: 1px; }");
		assertEquals("p:nth-last-of-type(odd)", rule.getSelectorText());
		rule.setCssText("*|p {border-top-width: 1px; }");
		assertEquals("p", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector9() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("h1~pre {border-top-width: 1px; }");
		assertEquals("h1~pre", rule.getSelectorText());

		rule.setCssText(":dir(rtl) {border-top-width: 1px; }");
		assertEquals(":dir(rtl)", rule.getSelectorText());

		rule.setCssText("::first-letter {border-top-width: 1px; }");
		assertEquals("::first-letter", rule.getSelectorText());

		rule.setCssText(".foo\\/1 {border-top-width: 1px; }");
		assertEquals(".foo\\/1", rule.getSelectorText());

		rule.setCssText("foo#bar\\/1 {border-top-width: 1px; }");
		assertEquals("foo#bar\\/1", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector10() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("#-\\31 23\\\\a {border-top-width: 1px; }");
		SelectorList selist = rule.getSelectorList();
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("-123\\a", ((AttributeCondition) cond).getValue());
		assertEquals("#\\-123\\\\a", rule.getSelectorText());

		rule.setCssText(".-\\31 23\\\\a {border-top-width: 1px; }");
		assertEquals(".\\-123\\\\a", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorEscapedType() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("\\.foo {border-top-width: 1px; }");
		assertEquals("\\.foo", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorLang() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("p:lang(zh, \"*-hant\") {border-top-width: 1px; }");
		assertEquals("p:lang(zh,\"*-hant\")", rule.getSelectorText());
		rule.setCssText("p:lang(zh, '*-hant') {border-top-width: 1px; }");
		assertEquals("p:lang(zh,'*-hant')", rule.getSelectorText());
		rule.setCssText("p:lang(es, fr, \\*-Latn) {border-top-width: 1px; }");
		assertEquals("p:lang(es,fr,\\*-Latn)", rule.getSelectorText());
		rule = createCSSStyleDeclarationRule(CSSStyleSheetFactory.FLAG_STRING_SINGLE_QUOTE);
		rule.setCssText("p:lang(zh, \"*-hant\") {border-top-width: 1px; }");
		assertEquals("p:lang(zh,'*-hant')", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelectorNot() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText(":not(:hover) {border-top-width: 1px; }");
		assertEquals(":not(:hover)", rule.getSelectorText());
		rule.setCssText("p:not(:hover) {border-top-width: 1px; }");
		assertEquals("p:not(:hover)", rule.getSelectorText());
		rule.setCssText(":not(:lang(en)) {border-top-width: 1px; }");
		assertEquals(":not(:lang(en))", rule.getSelectorText());
		rule.setCssText(":not([type]) {border-top-width: 1px; }");
		assertEquals(":not([type])", rule.getSelectorText());
		rule.setCssText(":not(#foo) {border-top-width: 1px; }");
		assertEquals(":not(#foo)", rule.getSelectorText());
		rule.setCssText("html:not(.foo) body:not(.bar) .myclass.otherclass {border-top-width: 1px; }");
		assertEquals("html:not(.foo) body:not(.bar) .myclass.otherclass", rule.getSelectorText());
	}

	@Test
	public void testEquals() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("p {border-top: 1px dashed yellow; }");
		CSSStyleDeclarationRule rule2 = sheet.createStyleRule();
		rule2.setCssText("p {border-top: 1px dashed yellow; }");
		assertTrue(rule.equals(rule2));
		rule2.setCssText("p {border-top: 1px dashed; }");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals2() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText("p {border-top: 1px dashed yellow; }");
		CSSStyleDeclarationRule rule2 = sheet.createStyleRule();
		rule2.setCssText("p {border-top: 1px dashed yellow; }");
		assertTrue(rule.equals(rule2));
		rule2.setCssText("p,div {border-top: 1px dashed yellow; }");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals3() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText(".foo+::after {border-top-width: 1px}");
		CSSStyleDeclarationRule rule2 = sheet.createStyleRule();
		rule2.setCssText(".foo+::after {border-top-width: 1px}");
		assertTrue(rule.equals(rule2));
		rule2.setCssText(".foo+::before {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals4() {
		CSSStyleDeclarationRule rule = sheet.createStyleRule();
		rule.setCssText(".foo .bar #fooid, .foo+::after {border-top-width: 1px}");
		CSSStyleDeclarationRule rule2 = sheet.createStyleRule();
		rule2.setCssText(".foo .bar #fooid,.foo+::after {border-top-width: 1px}");
		assertTrue(rule.equals(rule2));
		rule2.setCssText(".foo .bar #fooid, .foo+::before {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
		rule2.setCssText(".foo .bar #fooid {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
		rule2.setCssText(".foo+::after {border-top-width: 1px}");
		assertFalse(rule.equals(rule2));
	}

	private CSSStyleDeclarationRule createCSSStyleDeclarationRule(short flag) {
		factory.setFactoryFlag(flag);
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		return sheet.createStyleRule();
	}

}
