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
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class KeyframesRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws DOMException, IOException {
		// Rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		InputSource source = new InputSource(new StringReader(
				"@keyframes foo {  from { margin-left: 100%;  width: 300%;} 50% {margin-left: 50%;    width: 50%; }  to {margin-left: 0%;    width: 100%; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals(
				"@keyframes foo{from{margin-left:100%;width:300%}50%{margin-left:50%;width:50%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@keyframes foo {\n    from {\n        margin-left: 100%;\n        width: 300%;\n    }\n    50% {\n        margin-left: 50%;\n        width: 50%;\n    }\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRule2() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes foo {  0,50% { margin-left: 100%;  width: 300%;} to {margin-left: 0%;    width: 100%; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("foo", rule.getName());
		assertEquals("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@keyframes foo {\n    0,50% {\n        margin-left: 100%;\n        width: 300%;\n    }\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRule3() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes \"My Animation\" {  0,50% { margin-left: 100%;  width: 300%;} to {margin-left: 0%;    width: 100%; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("My Animation", rule.getName());
		assertEquals("@keyframes 'My Animation'{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@keyframes 'My Animation' {\n    0,50% {\n        margin-left: 100%;\n        width: 300%;\n    }\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleEscapedFF() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes \\66 00 {  0,50% { margin-left: 100%;  width: 300%;} to {margin-left: 0%;    width: 100%; }}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("f00", rule.getName());
		assertEquals("@keyframes f00{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@keyframes f00 {\n    0,50% {\n        margin-left: 100%;\n        width: 300%;\n    }\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleCompat() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser2.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
		InputSource source = new InputSource(new StringReader(
				"@keyframes animate-opacity{0%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=100)\";filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("animate-opacity", rule.getName());
		assertEquals(
				"@keyframes animate-opacity{0%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=100)\";filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleCompatSQ() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser2.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		factory.setFactoryFlag(AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE);
		sheet = factory.createStyleSheet(null, null);
		InputSource source = new InputSource(new StringReader(
				"@keyframes animate-opacity{0%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=100)\";filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("animate-opacity", rule.getName());
		assertEquals(
				"@keyframes animate-opacity{0%{-ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=100)';filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=0)';filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleError() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes $varname{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes Name, Other {0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError3() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes Name, Other Name {0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError4() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes 'Name', Other {0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError5() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes My name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError6() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes \\61'My name'{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError7() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes +name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError8() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes name+{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError9() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@keyframes \\61  name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}"));
		sheet.parseCSSStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testSetCssTextString() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		assertEquals("foo", rule.getName());
		assertEquals("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString2() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@keyframes foo{from{-webkit-transform:translate(-70%,-62.5%) scale(1);/*!rtl:ignore*/-moz-transform:translate(-70%,-62.5%) scale(1);/*!rtl:ignore*/-o-transform:translate(-70%,-62.5%) scale(1);/*!rtl:ignore*/transform:translate(-70%,-62.5%) scale(1)/*!rtl:ignore*/}to{-webkit-transform:translate(-70%,-62.5%) scale(1.05);/*!rtl:ignore*/-moz-transform:translate(-70%,-62.5%) scale(1.05);/*!rtl:ignore*/-o-transform:translate(-70%,-62.5%) scale(1.05);/*!rtl:ignore*/transform:translate(-70%,-62.5%) scale(1.05)/*!rtl:ignore*/}}");
		assertEquals("foo", rule.getName());
		assertEquals(
				"@keyframes foo{from{-webkit-transform:translate(-70%,-62.5%) scale(1);-moz-transform:translate(-70%,-62.5%) scale(1);-o-transform:translate(-70%,-62.5%) scale(1);transform:translate(-70%,-62.5%) scale(1)}to{-webkit-transform:translate(-70%,-62.5%) scale(1.05);-moz-transform:translate(-70%,-62.5%) scale(1.05);-o-transform:translate(-70%,-62.5%) scale(1.05);transform:translate(-70%,-62.5%) scale(1.05)}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextStringError() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@keyframes $varname{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringError2() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@keyframes \\61  name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
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
	public void testEquals() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		KeyframesRule rule2 = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@keyframes bar{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@keyframes foo{from{margin-left:100%;width:300%}50%{margin-left:50%;width:50%}to{margin-left:0%;width:100%}}");
		KeyframesRule clon = rule.clone(sheet);
		assertEquals(rule.getName(), clon.getName());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		CSSRuleArrayList list = rule.getCssRules();
		assertEquals(list.getLength(), clon.getCssRules().getLength());
		assertTrue(list.equals(clon.getCssRules()));
		KeyframeRule kf0 = (KeyframeRule) list.get(0);
		assertEquals(kf0.getKeyText(), ((CSSKeyframeRule) clon.getCssRules().get(0)).getKeyText());
		KeyframeRule kf0clon = kf0.clone(sheet);
		assertEquals(kf0.getKeyText(), kf0clon.getKeyText());
		assertEquals(kf0.getCssText(), kf0clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}
}
