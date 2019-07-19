/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class MediaRuleTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testInsertRuleStringInt() {
		MediaList mediaList = MediaList.createMediaList("screen,print");
		MediaRule rule = sheet.createCSSMediaRule(mediaList);
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(0, rule.insertRule("p {border-top: 1px dashed yellow; }", 0));
		assertEquals(1, rule.insertRule("span.reddish {color: red; }", 1));
		CSSRuleList rules = rule.getCssRules();
		assertEquals(2, rules.getLength());
		assertTrue(rule == rule.getCssRules().item(0).getParentRule());
	}

	@Test
	public void testGetCssText() {
		MediaList mediaList = MediaList.createMediaList("screen,print");
		MediaRule rule = sheet.createCSSMediaRule(mediaList);
		assertTrue(sheet == rule.getParentStyleSheet());
		rule.insertRule("p {border-top: 1px dashed yellow; }", 0);
		rule.insertRule("span.reddish {color: red; }", 1);
		assertEquals(
				"@media screen,print {\n    p {\n        border-top: 1px dashed yellow;\n    }\n    span.reddish {\n        color: red;\n    }\n}\n",
				rule.getCssText());
		assertEquals("@media screen,print{p{border-top:1px dashed yellow;}span.reddish{color:red}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testGetCssText2() {
		MediaList mediaList = MediaList.createMediaList("screen,print");
		MediaRule rule = new MediaRule(sheet, mediaList, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		assertEquals("@media screen,print{}", rule.getMinifiedCssText());
		assertEquals("@media screen,print {\n}\n", rule.getCssText());
	}

	@Test
	public void testParse() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media only screen and (min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px;}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("only screen and (min-width: 37.002em)", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(
				"@media only screen and (min-width: 37.002em) {\n    nav.foo {\n        display: none;\n    }\n    footer .footer .foo {\n        padding-left: 0;\n        padding-right: 0;\n    }\n    h4 {\n        font-size: 20px;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@media only screen and(min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParse2() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen and (-webkit-min-device-pixel-ratio:0){@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("screen and (-webkit-min-device-pixel-ratio: 0)", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(
				"@media screen and(-webkit-min-device-pixel-ratio:0){@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseIgnoreBad() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }@media {div.foo{margin:1em}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("all", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals("@media {\n    div.foo {\n        margin: 1em;\n    }\n}\n", rule.getCssText());
		assertEquals("@media{div.foo{margin:1em;}}", rule.getMinifiedCssText());
	}

	@Test
	public void testParseIgnoreBad2() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }}@media {div.foo{margin:1em}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("all", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals("@media {\n    div.foo {\n        margin: 1em;\n    }\n}\n", rule.getCssText());
		assertEquals("@media{div.foo{margin:1em;}}", rule.getMinifiedCssText());
	}

	@Test
	public void testParseNested() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("screen", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(2, rule.getCssRules().getLength());
		AbstractCSSRule rule0 = rule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule0.getType());
		assertTrue(rule == rule0.getParentRule());
		AbstractCSSRule rule1 = rule.getCssRules().item(1);
		assertEquals(CSSRule.MEDIA_RULE, rule1.getType());
		assertTrue(rule == rule1.getParentRule());
		MediaRule nestedmrule = (MediaRule) rule1;
		assertEquals("(max-width: 1600px)", nestedmrule.getMedia().getMedia());
		assertEquals("(max-width:1600px)", nestedmrule.getMedia().getMinifiedMedia());
		assertEquals(
				"@media screen {\n    .foo {\n        bottom: 20px ! important;\n    }\n    @media (max-width: 1600px) {\n        div.foo {\n            margin: 1em;\n        }\n    }\n}\n",
				rule.getCssText());
		assertEquals("@media screen{.foo{bottom:20px!important}@media (max-width:1600px){div.foo{margin:1em;}}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssText() throws DOMException {
		MediaList mediaList = MediaList.createMediaList("screen,print");
		MediaRule rule = new MediaRule(sheet, mediaList, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		String text = "@media only screen and (min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px;}}";
		rule.setCssText(text);
		assertEquals(3, rule.getCssRules().getLength());
		assertEquals("only screen and (min-width: 37.002em)", rule.getMedia().getMedia());
		assertEquals(
				"@media only screen and (min-width: 37.002em) {\n    nav.foo {\n        display: none;\n    }\n    footer .footer .foo {\n        padding-left: 0;\n        padding-right: 0;\n    }\n    h4 {\n        font-size: 20px;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@media only screen and(min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextBadRule() throws DOMException {
		MediaList mediaList = MediaList.createMediaList("screen,print");
		MediaRule rule = new MediaRule(sheet, mediaList, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@supports(display:table-cell)and(display:list-item){td{display:table-cell}@page{margin-top:20%}li{display:list-item}}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		assertEquals(0, rule.getCssRules().getLength());
		assertEquals("screen,print", rule.getMedia().getMedia());
		assertEquals("@media screen,print {\n}\n", rule.getCssText());
		assertEquals("@media screen,print{}", rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}"
						+ "@media print  {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(1).getType());
		MediaRule rule2 = (MediaRule) sheet.getCssRules().item(1);
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		MediaList mediaList = MediaList.createMediaList("screen,print");
		MediaRule rule = sheet.createCSSMediaRule(mediaList);
		rule.insertRule("p {border-top: 1px dashed yellow; }", 0);
		rule.insertRule("span.reddish {color: red; }", 1);
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null,
				null);
		MediaRule cloned = rule.clone(newSheet);
		assertFalse(rule == cloned);
		int nrules = rule.getCssRules().getLength();
		assertEquals(nrules, cloned.getCssRules().getLength());
		for (int i = 0; i < nrules; i++) {
			CSSRule r = rule.getCssRules().item(i);
			CSSRule c = cloned.getCssRules().item(i);
			assertTrue(r.getParentStyleSheet() != c.getParentStyleSheet());
			assertTrue(newSheet == c.getParentStyleSheet());
			assertTrue(rule == r.getParentRule());
			assertTrue(cloned == c.getParentRule());
			if (c instanceof CSSDeclarationRule) {
				assertTrue(c == ((CSSDeclarationRule) c).getStyle().getParentRule());
			}
			assertEquals(r.getCssText(), c.getCssText());
		}
		assertEquals(
				"@media screen,print {\n    p {\n        border-top: 1px dashed yellow;\n    }\n    span.reddish {\n        color: red;\n    }\n}\n",
				cloned.getCssText());
		assertTrue(rule.equals(cloned));
		assertEquals(rule.hashCode(), cloned.hashCode());
	}

}
