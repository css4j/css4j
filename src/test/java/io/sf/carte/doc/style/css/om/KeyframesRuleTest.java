/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.CSSKeyframesRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.Parser;

public class KeyframesRuleTest {

	private AbstractCSSStyleSheet sheet;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws DOMException, IOException {
		// Rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		StringReader re = new StringReader(
				"/* pre-rule */@keyframes /* skip 1 */ foo /* skip 2 */ {  /* pre-from */ from /* skip 3 */ { margin-left: 100%;  width: 300%;} /* pre-50% */ 50% {margin-left: 50%;    width: 50%; }  to {margin-left: 0%;    width: 100%; }/* skip 4 */}");
		sheet.parseStyleSheet(re, CSSStyleSheet.COMMENTS_PRECEDING);
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		CSSRuleArrayList kfrules = rule.getCssRules();
		assertNotNull(kfrules);
		assertEquals(3, kfrules.getLength());
		KeyframeRule kfrule = (KeyframeRule) kfrules.item(0);
		assertEquals(CSSRule.KEYFRAME_RULE, kfrule.getType());
		assertEquals("from", kfrule.getKeyText());
		AbstractCSSStyleDeclaration style = kfrule.getStyle();
		assertNotNull(style);
		assertEquals(2, style.getLength());
		assertNotNull(kfrule.getPrecedingComments());
		assertEquals(1, kfrule.getPrecedingComments().size());
		assertEquals(" pre-from ", kfrule.getPrecedingComments().get(0));
		//
		kfrule = (KeyframeRule) kfrules.item(1);
		assertEquals(CSSRule.KEYFRAME_RULE, kfrule.getType());
		assertEquals("50%", kfrule.getKeyText());
		style = kfrule.getStyle();
		assertNotNull(style);
		assertEquals(2, style.getLength());
		assertNotNull(kfrule.getPrecedingComments());
		assertEquals(1, kfrule.getPrecedingComments().size());
		assertEquals(" pre-50% ", kfrule.getPrecedingComments().get(0));
		//
		kfrule = (KeyframeRule) kfrules.item(2);
		assertEquals(CSSRule.KEYFRAME_RULE, kfrule.getType());
		assertEquals("to", kfrule.getKeyText());
		style = kfrule.getStyle();
		assertNotNull(style);
		assertEquals(2, style.getLength());
		assertEquals(
				"@keyframes foo{from{margin-left:100%;width:300%}50%{margin-left:50%;width:50%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"/* pre-rule */\n@keyframes foo {\n    /* pre-from */\n    from {\n        margin-left: 100%;\n        width: 300%;\n    }\n    /* pre-50% */\n    50% {\n        margin-left: 50%;\n        width: 50%;\n    }\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, rule.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule.getPrecedingComments().get(0));
		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitor);
		assertEquals(6, visitor.getCount());
		//
		visitor.reset();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(6, visitor.getCount());
	}

	@Test
	public void testParseRule2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes foo {  /* pre-0,50% */0,50% { margin-left: 100%;  width: 300%;}/* post-0,50% */\n to {margin-left: 0%;    width: 100%; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("foo", rule.getName());
		assertEquals("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@keyframes foo {\n    /* pre-0,50% */\n    0,50% {\n        margin-left: 100%;\n        width: 300%;\n    } /* post-0,50% */\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
		// findRule
		CSSKeyframeRule kfrule = rule.findRule("0, 50%");
		assertNotNull(kfrule);
		assertEquals("0,50%", kfrule.getKeyText());
		assertEquals("margin-left:100%;width:300%", kfrule.getStyle().getMinifiedCssText());
		assertEquals("0,50%{margin-left:100%;width:300%}", kfrule.getMinifiedCssText());
		assertEquals(" pre-0,50% ", kfrule.getPrecedingComments().item(0));
		assertEquals(" post-0,50% ", kfrule.getTrailingComments().item(0));
		// appendRule
		rule.appendRule("75%{width:75%}");
		assertEquals(3, rule.getCssRules().getLength());
		kfrule = rule.findRule("75%");
		assertNotNull(kfrule);
		assertEquals("75%", kfrule.getKeyText());
		assertEquals("width:75%", kfrule.getStyle().getMinifiedCssText());
		assertEquals("75%{width:75%}", kfrule.getMinifiedCssText());
		// deleteRule
		rule.deleteRule("75%");
		assertNull(rule.findRule("75%"));
		assertEquals(2, rule.getCssRules().getLength());
		// appendRule
		rule.appendRule("50%,80%{width:80%}");
		assertEquals(3, rule.getCssRules().getLength());
		kfrule = rule.findRule("50%,80%");
		assertNotNull(kfrule);
		assertEquals("50%,80%", kfrule.getKeyText());
		assertEquals("width:80%", kfrule.getStyle().getMinifiedCssText());
		assertEquals("50%,80%{width:80%}", kfrule.getMinifiedCssText());

		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitor);
		assertEquals(5, visitor.getCount());
		//
		visitor.reset();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(5, visitor.getCount());
	}

	@Test
	public void testParseRule3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes \"My Animation\" {  0,50% { margin-left: 100%;  width: 300%;} to {margin-left: 0%;    width: 100%; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("My Animation", rule.getName());
		assertEquals("@keyframes 'My Animation'{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@keyframes 'My Animation' {\n    0,50% {\n        margin-left: 100%;\n        width: 300%;\n    }\n    to {\n        margin-left: 0%;\n        width: 100%;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleErrorRecovery() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@;@keyframes \"My Animation\" {  0,50% { margin-left: 100%;  width: 300%;} to {margin-left: 0%;    width: 100%");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
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
		StringReader re = new StringReader(
				"@keyframes \\66 00 {  0,50% { margin-left: 100%;  width: 300%;} to {margin-left: 0%;    width: 100%; }}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
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
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"@keyframes animate-opacity{0%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=100)\";filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("animate-opacity", rule.getName());
		assertEquals(
				"@keyframes animate-opacity{0%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=100)\";filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleCompatSQ() throws DOMException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser.Flag.IEVALUES));
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		factory.setFactoryFlag(AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE);
		sheet = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"@keyframes animate-opacity{0%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=100)\";filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:\"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)\";filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals(CSSRule.KEYFRAMES_RULE, sheet.getCssRules().item(0).getType());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		assertEquals("animate-opacity", rule.getName());
		assertEquals(
				"@keyframes animate-opacity{0%{-ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=100)';filter:alpha(opacity=100);-moz-opacity:1;-khtml-opacity:1;opacity:1}100%{-ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=0)';filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity:0;opacity:0}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleError() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes $varname{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes Name, Other {0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes Name, Other Name {0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError4() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes 'Name', Other {0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError5() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes My name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError6() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes \\61'My name'{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError7() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes +name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError8() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes name+{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError9() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes \\61  name{0%{opacity:1;position:absolute;top:auto}99.99%{opacity:0;position:absolute;top:auto}100%{opacity:0;position:absolute;top:-99999px}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	/*
	 * This one should be accepting part of the rule.
	 */
	@Test
	public void testParseRuleError10() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes \"My Animation\" {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; {} width: 100%;}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		CSSRuleArrayList kfrules = rule.getCssRules();
		assertNotNull(kfrules);
		assertEquals(2, kfrules.getLength());
		KeyframeRule kf1 = (KeyframeRule) kfrules.item(1);
		assertEquals(1, kf1.getStyle().getLength());
		assertEquals("margin-left", kf1.getStyle().item(0));
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	/*
	 * This one should be accepting part of the rule.
	 */
	@Test
	public void testParseRuleError11() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes \"My Animation\" {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; width: 100%;{}}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		CSSRuleArrayList kfrules = rule.getCssRules();
		assertNotNull(kfrules);
		assertEquals(2, kfrules.getLength());
		KeyframeRule kf1 = (KeyframeRule) kfrules.item(1);
		assertEquals(2, kf1.getStyle().getLength());
		assertEquals("width", kf1.getStyle().item(1));
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError12() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes \"My Animation\" {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; width: 100%;}{}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		KeyframesRule rule = (KeyframesRule) sheet.getCssRules().item(0);
		CSSRuleArrayList kfrules = rule.getCssRules();
		assertNotNull(kfrules);
		assertEquals(2, kfrules.getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError13() throws DOMException, IOException {
		StringReader re = new StringReader("@keyframes \"My Animation\"");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError14() throws DOMException, IOException {
		StringReader re = new StringReader("@keyframes ");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorKeyword1() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes initial {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; width: 100%;}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorKeyword2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes None {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; width: 100%;}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorKeyword3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes Inherit {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; width: 100%;}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorKeyword4() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@keyframes unset {0,50%{margin-left: 100%;width: 300%;}to{margin-left: 0%; width: 100%;}}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
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
	public void testSetCssTextString3() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@keyframes opacity-closing {0% {opacity: 1;} 100% {opacity: 0;}} /*!rtl:end:ignore*/");
		assertEquals("opacity-closing", rule.getName());
		assertEquals("@keyframes opacity-closing{0%{opacity:1}100%{opacity:0}}", rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextStringCR() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@keyframes\nopacity-closing {0% {opacity: 1;} 100% {opacity: 0;}} /*!rtl:end:ignore*/");
		assertEquals("opacity-closing", rule.getName());
		assertEquals("@keyframes opacity-closing{0%{opacity:1}100%{opacity:0}}", rule.getMinifiedCssText());
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
	public void testCreateAndSetCssText() {
		CSSKeyframesRule rule = sheet.createKeyframesRule("bar");
		rule.setCssText("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		assertEquals("foo", rule.getName());
		assertEquals("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				rule.getMinifiedCssText());
		CSSKeyframeRule kfrule = rule.findRule("to");
		kfrule.setCssText("100%{margin-left:10%;width:90%}");
		assertEquals("100%{margin-left:10%;width:90%}", kfrule.getMinifiedCssText());
		assertEquals("@keyframes foo{0,50%{margin-left:100%;width:300%}100%{margin-left:10%;width:90%}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() {
		KeyframesRule rule = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		KeyframesRule rule2 = new KeyframesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		assertTrue(rule.equals(rule2));
		assertTrue(rule.hashCode() == rule2.hashCode());
		rule2.setCssText("@keyframes bar{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		assertFalse(rule.equals(rule2));
		rule2.setCssText("@keyframes foo{0,50%{margin-left:100%}to{margin-left:0%;width:100%}}");
		assertFalse(rule.equals(rule2));
		rule2.setCssText("@keyframes foo{50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}");
		assertFalse(rule.equals(rule2));
		rule2.setCssText("@keyframes foo{0,50%{margin-left:100%;width:300%}}");
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
