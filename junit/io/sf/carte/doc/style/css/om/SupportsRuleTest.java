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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSMediaRule;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.SupportsCondition;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.om.DefaultSheetErrorHandler.RuleParseError;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class SupportsRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseSupportsConditionBad() throws DOMException {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		CSSParser parser = new CSSParser();
		SupportsCondition cond = parser.parseSupportsCondition(" ", rule);
		assertNull(cond);
		LinkedList<RuleParseError> errors = ((DefaultSheetErrorHandler) sheet.getErrorHandler()).getRuleParseErrors();
		assertNotNull(errors);
		assertEquals(1, errors.size());
		RuleParseError rpe = errors.getFirst();
		CSSParseException ex = rpe.getException();
		assertNotNull(ex);
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad2() throws DOMException {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		CSSParser parser = new CSSParser();
		SupportsCondition cond = parser.parseSupportsCondition("(", rule);
		assertNull(cond);
		LinkedList<RuleParseError> errors = ((DefaultSheetErrorHandler) sheet.getErrorHandler()).getRuleParseErrors();
		assertNotNull(errors);
		assertEquals(1, errors.size());
		RuleParseError rpe = errors.getFirst();
		CSSParseException ex = rpe.getException();
		assertNotNull(ex);
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsRule1() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
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
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule1Minified() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports(display:table-cell)and(display:list-item){td{display:table-cell}li{display:list-item}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals("@supports(display:table-cell)and(display:list-item){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule2() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: flexbox) and (not (display: inline-grid))", rule.getConditionText());
		assertEquals(
				"@supports (display: flexbox) and (not (display: inline-grid)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule3() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports (display: table-cell) and (display: list-item) and (display: run-in) {td {display: table-cell; } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: table-cell) and (display: list-item) and (display: run-in)", rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) and (display: run-in) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule4() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports ((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid))) {td {display: table-cell; } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid)))",
				rule.getConditionText());
		assertEquals(
				"@supports ((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid))) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule5() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports (display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell))) {td {display: table-cell; } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))",
				rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell))) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule6() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports (display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell)) {td {display: table-cell; } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell))",
				rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule7() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports (text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(text-decoration: underline dotted)", rule.getConditionText());
		assertEquals(
				"@supports (text-decoration: underline dotted) {\n    abbr[title],.explain[title] {\n        border-bottom: 0;\n        text-decoration: underline dotted;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted;}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseSupportsRule7InsideMedia() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {@supports (text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted}}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		CSSMediaRule mediarule = (CSSMediaRule) sheet.getCssRules().item(0);
		assertEquals(1, mediarule.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, mediarule.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) mediarule.getCssRules().item(0);
		assertEquals("(text-decoration: underline dotted)", rule.getConditionText());
		assertEquals(
				"@supports (text-decoration: underline dotted) {\n    abbr[title],.explain[title] {\n        border-bottom: 0;\n        text-decoration: underline dotted;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(text-decoration:underline dotted){abbr[title],.explain[title]{border-bottom:0;text-decoration:underline dotted;}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseSupportsRule8() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports ((-webkit-backdrop-filter: initial) or (backdrop-filter: initial)){#fooid.fooclass .barclass{-webkit-backdrop-filter:saturate(180%) blur(20px);backdrop-filter:saturate(180%) blur(20px);background-color:rgb(255 255 255/0.7)}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(-webkit-backdrop-filter: initial) or (backdrop-filter: initial)", rule.getConditionText());
		assertEquals(
				"@supports (-webkit-backdrop-filter: initial) or (backdrop-filter: initial) {\n    #fooid.fooclass .barclass {\n        -webkit-backdrop-filter: saturate(180%) blur(20px);\n        backdrop-filter: saturate(180%) blur(20px);\n        background-color: rgb(255 255 255 / 0.7);\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(-webkit-backdrop-filter:initial)or(backdrop-filter:initial){#fooid.fooclass .barclass{-webkit-backdrop-filter:saturate(180%) blur(20px);backdrop-filter:saturate(180%) blur(20px);background-color:rgb(255 255 255/.7)}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseSupportsRule9() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports ((-webkit-backdrop-filter: initial) or (backdrop-filter: initial)){.fooclass #descid.barclass .someclass,.barclass#otherid.otherclass .someclass{background-color:rgb(11 11 11/0.7)}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(-webkit-backdrop-filter: initial) or (backdrop-filter: initial)", rule.getConditionText());
		assertEquals(
				"@supports (-webkit-backdrop-filter: initial) or (backdrop-filter: initial) {\n    .fooclass #descid.barclass .someclass,.barclass#otherid.otherclass .someclass {\n        background-color: rgb(11 11 11 / 0.7);\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(-webkit-backdrop-filter:initial)or(backdrop-filter:initial){.fooclass #descid.barclass .someclass,.barclass#otherid.otherclass .someclass{background-color:rgb(11 11 11/.7)}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseSupportsRule10() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports ((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px))) {.foo {backdrop-filter:saturate(180%) blur(20px);}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals(
				"(-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px))",
				rule.getConditionText());
		assertEquals(
				"@supports (-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)) {\n    .foo {\n        backdrop-filter: saturate(180%) blur(20px);\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRule11() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports ((position: -webkit-sticky) or (position: sticky)){html:not(.foo) body:not(.bar) .myclass{position:sticky;bottom:-0.08em}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-0.06em}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(position: -webkit-sticky) or (position: sticky)", rule.getConditionText());
		assertEquals(
				"@supports(position:-webkit-sticky)or(position:sticky){html:not(.foo) body:not(.bar) .myclass{position:sticky;bottom:-.08em}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-.06em}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseSupportsRuleInsideMediaAndNestedPageRule() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } @page {margin-top: 20%;} li {display: list-item; }}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		CSSMediaRule mediarule = (CSSMediaRule) sheet.getCssRules().item(0);
		assertEquals(1, mediarule.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, mediarule.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) mediarule.getCssRules().item(0);
		assertEquals(3, rule.getCssRules().getLength());
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@supports(display:table-cell)and(display:list-item){td{display:table-cell}@page{margin-top:20%}li{display:list-item}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseSupportsRuleOr() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports(display:table-cell)or(display:list-item){td{display:table-cell}li{display:list-item}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("(display: table-cell) or (display: list-item)", rule.getConditionText());
		assertEquals("@supports(display:table-cell)or(display:list-item){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports (display: table-cell) or (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRuleNot() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@supports not((display:table-cell)or(display:list-item)){td{display:table-cell}li{display:list-item}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
		SupportsRule rule = (SupportsRule) sheet.getCssRules().item(0);
		assertEquals("not ((display: table-cell) or (display: list-item))", rule.getConditionText());
		assertEquals(
				"@supports not((display:table-cell)or(display:list-item)){td{display:table-cell}li{display:list-item}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@supports not ((display: table-cell) or (display: list-item)) {\n    td {\n        display: table-cell;\n    }\n    li {\n        display: list-item;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseSupportsRuleCompat() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser2.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
		InputSource source = new InputSource(new StringReader(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell; filter:alpha(opacity=0); } li {display: list-item; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, sheet.getCssRules().item(0).getType());
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
				"@supports(display:table-cell)and(display:list-item){td{display:table-cell;filter:alpha(opacity=0)}li{display:list-item}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString() throws DOMException {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		assertEquals("(display: table-cell) and (display: list-item)", rule.getConditionText());
		assertEquals(
				"@supports(display:table-cell)and(display:list-item){td{display:table-cell}@page{margin-top:20%}li{display:list-item}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString2() throws DOMException {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports ((background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))){.foo{padding:10px}}");
		assertEquals(
				"(background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))",
				rule.getConditionText());
		assertEquals(
				"@supports(background:-webkit-gradient(linear,left top,left bottom,from(transparent),to(#fff)))or(background:-webkit-linear-gradient(transparent,#fff))or(background:-moz-linear-gradient(transparent,#fff))or(background:-o-linear-gradient(transparent,#fff))or(background:linear-gradient(transparent,#fff)){.foo{padding:10px;}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString3() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports (background: radial-gradient(closest-side,#ff0000,#a2f3a1)){.foo .bar:first-child{background:radial-gradient(closest-side,rgb(32 45 46/0),#da212e),url(\"//example.com/img/image.jpg\");background-size:600px 600px}}");
		assertEquals(
				"@supports(background:radial-gradient(closest-side,#f00,#a2f3a1)){.foo .bar:first-child{background:radial-gradient(closest-side,rgb(32 45 46/0),#da212e),url(\"//example.com/img/image.jpg\");background-size:600px 600px}}",
				rule.getMinifiedCssText());
		SupportsRule rule2 = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@supports (background: radial-gradient(closest-side, #f00, #a2f3a1)) {.foo .bar:first-child {background: radial-gradient(closest-side, rgb(32 45 46 / 0), #da212e), url('//example.com/img/image.jpg');background-size: 600px 600px;}}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText("@page {margin-top: 20%;}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testSetCssTextStringWrongCondition() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@supports ((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testSetCssTextStringWrongCondition2() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@supports ((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)))) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testSetCssTextStringWrongCondition3() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@supports ((transition-property: color) or (animation-name: foo) and (transform: rotate(10deg))) {.foo {backdrop-filter:saturate(180%) blur(20px);}}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testEquals() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		SupportsRule rule2 = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		assertTrue(rule.equals(rule2));
	}

	@Test
	public void testEquals2() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports (display: table-cell) and (display: list-item) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		SupportsRule rule2 = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@supports (display: table-cell) {\n    td {\n        display: table-cell;\n    }\n    @page {\n        margin-top: 20%;\n    }\n    li {\n        display: list-item;\n    }\n}\n");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals3() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell;}@page {margin-top: 20%;}li {display: list-item;}}");
		SupportsRule rule2 = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell;}li {display: list-item;}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals4() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports ((position: -webkit-sticky) or (position: sticky)){html:not(.foo) body:not(.bar) .myclass{position:-webkit-sticky;position:sticky;bottom:-0.0625rem}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-0.06em}}");
		SupportsRule rule2 = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@supports ((position: -webkit-sticky) or (position: sticky)){html:not(.foo) body:not(.bar) .myclass{position:-webkit-sticky;position:sticky;bottom:-0.0625rem}html:not(.foo) body:not(.bar) .myclass.otherclass{top:-0.06em}}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		SupportsRule rule = new SupportsRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@supports(display:table-cell)and(display:list-item){td{display:table-cell}li{display:list-item}}");
		SupportsRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

}
