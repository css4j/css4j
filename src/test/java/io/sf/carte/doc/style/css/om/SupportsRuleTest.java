/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSMediaRule;

import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class SupportsRuleTest {

	private static TestCSSStyleSheetFactory factory;

	private static StyleDatabase styleDb;

	private AbstractCSSStyleSheet sheet;

	@BeforeAll
	public static void setUpBeforeAll() {
		factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		DeviceFactory df = factory.getDeviceFactory();
		styleDb = df.getStyleDatabase("screen");
	}

	@BeforeEach
	public void setUp() {
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseSupportsConditionBad() {
		SupportsRule rule = createSupportsRule();
		CSSParser parser = new CSSOMParser();
		BooleanCondition cond = parser.parseSupportsCondition(" ", rule);
		assertNull(cond);
		LinkedList<RuleParseException> errors = ((DefaultSheetErrorHandler) sheet.getErrorHandler())
				.getRuleParseErrors();
		assertNotNull(errors);
		assertEquals(1, errors.size());
		RuleParseException rpe = errors.getFirst();
		CSSParseException ex = rpe.getCause();
		assertNotNull(ex);
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad2() {
		SupportsRule rule = createSupportsRule();
		CSSParser parser = new CSSOMParser();
		BooleanCondition cond = parser.parseSupportsCondition("(", rule);
		assertNull(cond);
		LinkedList<RuleParseException> errors = ((DefaultSheetErrorHandler) sheet.getErrorHandler())
				.getRuleParseErrors();
		assertNotNull(errors);
		assertEquals(1, errors.size());
		RuleParseException rpe = errors.getFirst();
		CSSParseException ex = rpe.getCause();
		assertNotNull(ex);
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsRule1() throws DOMException, IOException {
		StringReader re = new StringReader(
				"/* pre-rule */@supports /* skip 1 */ (display: table-cell) and (display: list-item) /* skip 2 */ {/* pre-td */td {display: table-cell; }/* post-td */ li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getCssRules().getLength());
		AbstractCSSRule nested0 = rule.getCssRules().get(0);
		assertEquals(CSSRule.STYLE_RULE, nested0.getType());
		assertTrue(rule == nested0.getParentRule());
		AbstractCSSRule nested1 = rule.getCssRules().get(1);
		assertEquals(CSSRule.STYLE_RULE, nested1.getType());
		assertTrue(rule == nested1.getParentRule());
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"/* pre-rule */\n@supports (display: table-cell) and (display: list-item) {\n    /* pre-td */\n    td {\n        display: table-cell;\n    } /* post-td */\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, rule.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule.getPrecedingComments().get(0));

		assertFalse(sheet.getErrorHandler().hasSacErrors());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule1Minified() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports(display:table-cell) and (display:list-item){td{display:table-cell}li{display:list-item}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports(display:table-cell) and (display:list-item){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: flexbox) and (not (display: inline-grid))",
				rule.getConditionText());
		assertEquals(
				"@supports (display: flexbox) and (not (display: inline-grid)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports (display: table-cell) and (display: list-item) and (display: run-in) {td {display: table-cell; } li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: table-cell) and (display: list-item) and (display: run-in)",
				rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) and (display: run-in) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule4() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports ((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid))) {td {display: table-cell; } li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid)))",
				rule.getConditionText());
		assertEquals(
				"@supports ((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid))) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule5() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports (display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell))) {td {display: table-cell; } li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))",
				rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell))) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule6() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports (display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell)) {td {display: table-cell; } li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell))",
				rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule7() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports (text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(text-decoration: underline dotted)", rule.getConditionText());
		assertEquals(
				"@supports (text-decoration: underline dotted) {\n    abbr[title],.explain[title] {\n        border-bottom: 0;\n        text-decoration: underline dotted;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted;}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule7InsideMedia() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media screen {@supports (text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted}}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		CSSMediaRule mediarule = (CSSMediaRule) sheet.getCssRules().item(0);
		assertEquals(1, mediarule.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, mediarule.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) mediarule.getCssRules().item(0);
		assertEquals("(text-decoration: underline dotted)", rule.getConditionText());
		assertEquals(
				"@supports (text-decoration: underline dotted) {\n    abbr[title],.explain[title] {\n        border-bottom: 0;\n        text-decoration: underline dotted;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted;}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule8() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports ((-webkit-backdrop-filter: initial) or (backdrop-filter: initial)){#fooid.fooclass .barclass{-webkit-backdrop-filter:saturate(180%) blur(20px);backdrop-filter:saturate(180%) blur(20px);background-color:rgb(255 255 255/0.7)}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(-webkit-backdrop-filter: initial) or (backdrop-filter: initial)",
				rule.getConditionText());
		assertEquals(
				"@supports (-webkit-backdrop-filter: initial) or (backdrop-filter: initial) {\n    #fooid.fooclass .barclass {\n        -webkit-backdrop-filter: saturate(180%) blur(20px);\n        backdrop-filter: saturate(180%) blur(20px);\n        background-color: rgb(255 255 255 / 0.7);\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(-webkit-backdrop-filter:initial) or (backdrop-filter:initial){#fooid.fooclass .barclass{-webkit-backdrop-filter:saturate(180%) blur(20px);backdrop-filter:saturate(180%) blur(20px);background-color:rgb(255 255 255/.7)}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule9() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports ((-webkit-backdrop-filter: initial) or (backdrop-filter: initial)){.fooclass #descid.barclass .someclass,.barclass#otherid.otherclass .someclass{background-color:rgb(11 11 11/0.7)}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(-webkit-backdrop-filter: initial) or (backdrop-filter: initial)",
				rule.getConditionText());
		assertEquals(
				"@supports (-webkit-backdrop-filter: initial) or (backdrop-filter: initial) {\n    .fooclass #descid.barclass .someclass,.barclass#otherid.otherclass .someclass {\n        background-color: rgb(11 11 11 / 0.7);\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(-webkit-backdrop-filter:initial) or (backdrop-filter:initial){.fooclass #descid.barclass .someclass,.barclass#otherid.otherclass .someclass{background-color:rgb(11 11 11/.7)}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule10() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports ((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px))) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px))",
				rule.getConditionText());
		assertEquals(
				"@supports (-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)) {\n    .foo {\n        backdrop-filter: saturate(180%) blur(20px);\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRule11() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports ((position: -webkit-sticky) or (position: sticky)){html:not(.foo) body:not(.bar) .myclass{position:sticky;bottom:-0.08em}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-0.06em}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(position: -webkit-sticky) or (position: sticky)", rule.getConditionText());
		assertEquals(
				"@supports(position:-webkit-sticky) or (position:sticky){html:not(.foo) body:not(.bar) .myclass{position:sticky;bottom:-.08em}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-.06em}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRuleInsideMediaAndNestedPageRule()
			throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media screen {@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } @page {margin-top: 20%;} li {display: list-item; }}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		CSSMediaRule mediarule = (CSSMediaRule) sheet.getCssRules().item(0);
		assertEquals(1, mediarule.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, mediarule.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) mediarule.getCssRules().item(0);
		assertEquals(3, rule.getCssRules().getLength());
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(display:table-cell) and (display:list-item){td{display:table-cell}@page{margin-top:20%}li{display:list-item}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRuleOr() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports(display:table-cell) or (display:list-item){td{display:table-cell}li{display:list-item}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: table-cell) or (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports(display:table-cell) or (display:list-item){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports (display: table-cell) or (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRuleNot() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports not((display:table-cell) or (display:list-item)){td{display:table-cell}li{display:list-item}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("not ((display: table-cell) or (display: list-item))",
				rule.getConditionText());
		assertEquals(
				"@supports not ((display:table-cell) or (display:list-item)){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports not ((display: table-cell) or (display: list-item)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(sheet.getErrorHandler().hasSacErrors());

		assertTrue(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRuleSelector() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports not((display:table-cell) or (display:list-item)) and selector(:has(*)) {td{display:table-cell}li{display:list-item}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(not ((display: table-cell) or (display: list-item))) and selector(:has(*))",
				rule.getConditionText());
		assertEquals(
				"@supports(not ((display:table-cell) or (display:list-item))) and selector(:has(*)){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports (not ((display: table-cell) or (display: list-item))) and selector(:has(*)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(sheet.getErrorHandler().hasSacErrors());

		assertTrue(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRuleSelectorUnknown() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@supports not((display:table-cell) or (display:list-item)) and not(selector([p++])) {td{display:table-cell}li{display:list-item}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(not ((display: table-cell) or (display: list-item))) and (not selector([p++]))",
				rule.getConditionText());
		assertEquals(
				"@supports(not ((display:table-cell) or (display:list-item))) and (not selector([p++])){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports (not ((display: table-cell) or (display: list-item))) and (not selector([p++])) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());

		assertFalse(sheet.getErrorHandler().hasSacErrors());

		assertTrue(rule.supports(styleDb));
	}

	@Test
	public void testParseSupportsRuleCompat() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(
				EnumSet.of(Parser.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell; filter:alpha(opacity=0); } li {display: list-item; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getCssRules().getLength());
		AbstractCSSRule nested0 = rule.getCssRules().get(0);
		assertEquals(CSSRule.STYLE_RULE, nested0.getType());
		assertTrue(rule == nested0.getParentRule());
		assertEquals(2, ((CSSDeclarationRule) nested0).getStyle().getLength());
		AbstractCSSRule nested1 = rule.getCssRules().get(1);
		assertEquals(CSSRule.STYLE_RULE, nested1.getType());
		assertTrue(rule == nested1.getParentRule());
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports(display:table-cell) and (display:list-item){td{display:table-cell;filter:alpha(opacity=0)}li{display:list-item}}",
				rule.getMinifiedCssText());

		assertFalse(rule.supports(styleDb));
	}

	@Test
	public void testSupports() {
		SupportsRule rule = parseStyleSheet(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports(display:table-cell) and (display:list-item){td{display:table-cell}@page{margin-top:20%}li{display:list-item}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSupports2() {
		SupportsRule rule = parseStyleSheet(
				"@supports ((background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))){.foo{padding:10px}}");
		assertEquals(
				"(background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))",
				rule.getConditionText());
		assertEquals(
				"@supports(background:-webkit-gradient(linear,left top,left bottom,from(transparent),to(#fff))) or (background:-webkit-linear-gradient(transparent,#fff)) or (background:-moz-linear-gradient(transparent,#fff)) or (background:-o-linear-gradient(transparent,#fff)) or (background:linear-gradient(transparent,#fff)){.foo{padding:10px;}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSupports3() {
		SupportsRule rule = parseStyleSheet(
				"@supports (background: radial-gradient(closest-side,#ff0000,#a2f3a1)){.foo .bar:first-child{background:radial-gradient(closest-side,rgb(32 45 46/0),#da212e),url(\"//example.com/img/image.jpg\");background-size:600px 600px}}");
		assertEquals(
				"@supports(background:radial-gradient(closest-side,red,#a2f3a1)){.foo .bar:first-child{background:radial-gradient(closest-side,#202d2e00,#da212e),url(//example.com/img/image.jpg);background-size:600px 600px}}",
				rule.getMinifiedCssText());
		SupportsRule rule2 = parseStyleSheet(
				"@supports (background: radial-gradient(closest-side, #f00, #a2f3a1)) {.foo .bar:first-child {background: radial-gradient(closest-side, rgb(32 45 46 / 0), #da212e), url('//example.com/img/image.jpg');background-size: 600px 600px;}}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
	}

	@Test
	public void testSupportsWrongCondition() {
		// Is missing a final parenthesis
		SupportsRule rule = parseStyleSheet(
				"@supports ((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
		assertNull(rule);
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testSupportsWrongCondition2() {
		// Extra final parenthesis
		SupportsRule rule = parseStyleSheet(
				"@supports ((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)))) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
		assertNull(rule);
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testSupportsWrongCondition3() {
		// Ambiguous, forbidden by specification
		SupportsRule rule = parseStyleSheet(
				"@supports ((transition-property: color) or (animation-name: foo) and (transform: rotate(10deg))) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
		assertNull(rule);
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testEquals() {
		SupportsRule rule = parseStyleSheet(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		SupportsRule rule2 = parseStyleSheet(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		assertTrue(rule.equals(rule2));
	}

	@Test
	public void testEquals2() {
		SupportsRule rule = parseStyleSheet(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		SupportsRule rule2 = parseStyleSheet(
				"@supports (display: table-cell) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals3() {
		SupportsRule rule = parseStyleSheet(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell;}@page {margin-top: 20%;}li {display: list-item;}}");
		SupportsRule rule2 = parseStyleSheet(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell;}li {display: list-item;}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals4() {
		SupportsRule rule = parseStyleSheet(
				"@supports ((position: -webkit-sticky) or (position: sticky)){html:not(.foo) body:not(.bar) .myclass{position:-webkit-sticky;position:sticky;bottom:-0.0625rem}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-0.06em}}");
		SupportsRule rule2 = parseStyleSheet(
				"@supports ((position: -webkit-sticky) or (position: sticky)){html:not(.foo) body:not(.bar) .myclass{position:-webkit-sticky;position:sticky;bottom:-0.0625rem}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-0.06em}}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		SupportsRule rule = parseStyleSheet(
				"@supports(display:table-cell) and (display:list-item){td{display:table-cell}li{display:list-item}}");
		SupportsRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getConditionText(), clon.getConditionText());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	private SupportsRule createSupportsRule() {
		return new SupportsRule(sheet, sheet.getOrigin());
	}

	private SupportsRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (SupportsRule) sheet.getCssRules().item(0);
	}

}
