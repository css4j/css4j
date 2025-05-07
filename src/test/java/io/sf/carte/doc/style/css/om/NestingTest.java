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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSNestedDeclarations;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;

public class NestingTest {

	private static DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();

	private AbstractCSSStyleSheet sheet;

	@BeforeAll
	public static void setUpBeforeClass() {
		factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
	}

	@BeforeEach
	public void setUp() {
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParsing() throws DOMException, IOException {
		try (InputStream is = NestingTest.class
				.getResourceAsStream("/io/sf/carte/doc/style/css/parser/nesting.css");) {
			sheet.parseStyleSheet(new InputStreamReader(is, StandardCharsets.UTF_8),
					CSSStyleSheet.COMMENTS_IGNORE);
		}

		assertEquals(15, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		Iterator<AbstractCSSRule> topIter = sheet.getCssRules().iterator();
		StyleRule styleR = (StyleRule) topIter.next();
		assertEquals("body", styleR.getSelectorText());
		assertEquals("body", styleR.getAbsoluteSelectorList().toString());
		assertEquals(6, styleR.getStyle().getLength());

		CSSRuleArrayList rules = styleR.getCssRules();
		assertNotNull(rules);
		assertEquals(6, rules.getLength());

		Iterator<AbstractCSSRule> it = rules.iterator();
		AbstractCSSRule nestedR = it.next();
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		StyleRule nestedStyleR = (StyleRule) nestedR;
		assertEquals("& #span1", nestedStyleR.getSelectorText());
		assertEquals("body #span1", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(2, nestedStyleR.getStyle().getLength());
		assertEquals("#ffe4f5", nestedStyleR.getStyle().getPropertyValue("color"));

		nestedR = it.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, nestedR.getType());
		CSSNestedDeclarations nestedDecl = (CSSNestedDeclarations) nestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("#233f5a", nestedDecl.getStyle().getPropertyValue("--my-color"));

		nestedR = it.next();
		assertEquals(CSSRule.MEDIA_RULE, nestedR.getType());
		MediaRule nestedMediaR = (MediaRule) nestedR;
		assertEquals("all", nestedMediaR.getMedia().getMedia());
		CSSRuleArrayList mediaGroup = nestedMediaR.getCssRules();
		assertEquals(2, mediaGroup.getLength());

		// Media group all

		Iterator<AbstractCSSRule> mediaIt = mediaGroup.iterator();
		AbstractCSSRule mediaNestedR = mediaIt.next();
		assertEquals(CSSRule.STYLE_RULE, mediaNestedR.getType());
		StyleRule mediaNestedStyleR = (StyleRule) mediaNestedR;
		assertEquals("&>div", mediaNestedStyleR.getSelectorText());
		assertEquals("body>div", mediaNestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(2, mediaNestedStyleR.getStyle().getLength());
		assertEquals("#fd8eab", mediaNestedStyleR.getStyle().getPropertyValue("color"));
		assertNull(mediaNestedStyleR.getCssRules());

		mediaNestedR = mediaIt.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, mediaNestedR.getType());
		nestedDecl = (CSSNestedDeclarations) mediaNestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("left", nestedDecl.getStyle().getPropertyValue("--my-text-align"));

		assertFalse(mediaIt.hasNext());

		// Back to body properties

		nestedR = it.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, nestedR.getType());
		nestedDecl = (CSSNestedDeclarations) nestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("white", nestedDecl.getStyle().getPropertyValue("background-color"));

		// Media group screen

		nestedR = it.next();
		assertEquals(CSSRule.MEDIA_RULE, nestedR.getType());
		nestedMediaR = (MediaRule) nestedR;
		assertEquals("screen", nestedMediaR.getMedia().getMedia());
		mediaGroup = nestedMediaR.getCssRules();
		assertEquals(2, mediaGroup.getLength());

		mediaIt = mediaGroup.iterator();

		mediaNestedR = mediaIt.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, mediaNestedR.getType());
		nestedDecl = (CSSNestedDeclarations) mediaNestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("always", nestedDecl.getStyle().getPropertyValue("page-break-after"));
		assertEquals("important", nestedDecl.getStyle().getPropertyPriority("page-break-after"));

		mediaNestedR = mediaIt.next();
		assertEquals(CSSRule.STYLE_RULE, mediaNestedR.getType());
		mediaNestedStyleR = (StyleRule) mediaNestedR;
		assertEquals("&.cls", mediaNestedStyleR.getSelectorText());
		assertEquals("body.cls", mediaNestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(1, mediaNestedStyleR.getStyle().getLength());
		assertEquals("21pt", mediaNestedStyleR.getStyle().getPropertyValue("font-size"));
		assertEquals(2, mediaNestedStyleR.getCssRules().getLength());
		assertEquals("&.cls{font-size:21pt!important;&+ul{margin-left:18px}--my-bg-color:#226}",
				mediaNestedStyleR.getMinifiedCssText());
		assertEquals(
				"&.cls {\n    font-size: 21pt ! important;\n    &+ul {\n"
						+ "        margin-left: 18px;\n" + "    }\n    --my-bg-color: #226;\n}\n",
				mediaNestedStyleR.getCssText());

		mediaNestedR = mediaNestedStyleR.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, mediaNestedR.getType());
		StyleRule styleNestedStyleR = (StyleRule) mediaNestedR;
		assertEquals("&+ul", styleNestedStyleR.getSelectorText());
		assertEquals("body.cls+ul", styleNestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(1, styleNestedStyleR.getStyle().getLength());
		assertEquals("18px", styleNestedStyleR.getStyle().getPropertyValue("margin-left"));

		mediaNestedR = mediaNestedStyleR.getCssRules().item(1);
		assertEquals(CSSRule.NESTED_DECLARATIONS, mediaNestedR.getType());
		nestedDecl = (CSSNestedDeclarations) mediaNestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("#226", nestedDecl.getStyle().getPropertyValue("--my-bg-color"));

		// Serialization test
		assertEquals(
				"@media screen{page-break-after:always!important;&.cls{font-size:21pt!important;"
						+ "&+ul{margin-left:18px}--my-bg-color:#226}}",
				nestedMediaR.getMinifiedCssText());
		assertEquals(
				"@media screen {\n" + "    page-break-after: always ! important;\n"
						+ "    &.cls {\n" + "        font-size: 21pt ! important;\n"
						+ "        &+ul {\n" + "            margin-left: 18px;\n" + "        }\n"
						+ "        --my-bg-color: #226;\n" + "    }\n}\n",
				nestedMediaR.getCssText());

		assertFalse(mediaIt.hasNext());

		// Back to body nested

		nestedR = it.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, nestedR.getType());
		nestedDecl = (CSSNestedDeclarations) nestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("900px", nestedDecl.getStyle().getPropertyValue("width"));

		assertFalse(it.hasNext());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("h1", styleR.getSelectorText());
		assertEquals("h1", styleR.getAbsoluteSelectorList().toString());
		assertEquals(0, styleR.getStyle().getLength());

		rules = styleR.getCssRules();
		assertNotNull(rules);
		assertEquals(2, rules.getLength());

		it = rules.iterator();

		nestedR = it.next();
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		nestedStyleR = (StyleRule) nestedR;
		assertEquals("&#h1", nestedStyleR.getSelectorText());
		assertEquals("h1#h1", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(8, nestedStyleR.getStyle().getLength());
		assertEquals("url('headerbg.png')",
				nestedStyleR.getStyle().getPropertyValue("background-image"));

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("h2", styleR.getSelectorText());
		assertEquals("h2", styleR.getAbsoluteSelectorList().toString());
		assertEquals(6, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("h3", styleR.getSelectorText());
		assertEquals(14, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("p", styleR.getSelectorText());
		assertEquals(1, styleR.getStyle().getLength());

		rules = styleR.getCssRules();
		assertNotNull(rules);
		assertEquals(4, rules.getLength());

		it = rules.iterator();

		nestedR = it.next();
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		nestedStyleR = (StyleRule) nestedR;
		assertEquals("&#listpara", nestedStyleR.getSelectorText());
		assertEquals("p#listpara", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(2, nestedStyleR.getStyle().getLength());
		assertEquals("inherit", nestedStyleR.getStyle().getPropertyValue("padding-left"));

		nestedR = it.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, nestedR.getType());
		nestedDecl = (CSSNestedDeclarations) nestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("#8ad", nestedDecl.getStyle().getPropertyValue("background-color"));

		// Media group print

		nestedR = it.next();
		assertEquals(CSSRule.MEDIA_RULE, nestedR.getType());
		nestedMediaR = (MediaRule) nestedR;
		assertEquals("print", nestedMediaR.getMedia().getMedia());
		mediaGroup = nestedMediaR.getCssRules();
		assertEquals(2, mediaGroup.getLength());

		// Media group screen

		nestedR = it.next();
		assertEquals(CSSRule.MEDIA_RULE, nestedR.getType());
		nestedMediaR = (MediaRule) nestedR;
		assertEquals("screen", nestedMediaR.getMedia().getMedia());
		mediaGroup = nestedMediaR.getCssRules();
		assertEquals(3, mediaGroup.getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("p.boldmargin", styleR.getSelectorText());
		assertEquals(19, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("p.smallitalic", styleR.getSelectorText());
		assertEquals(3, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("#tablepara", styleR.getSelectorText());
		assertEquals(5, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("#tablerow1", styleR.getSelectorText());
		assertEquals(4, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("#cell12", styleR.getSelectorText());
		assertEquals(5, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("ul,ol", styleR.getSelectorText());
		assertEquals(0, styleR.getStyle().getLength());

		rules = styleR.getCssRules();
		assertNotNull(rules);
		assertEquals(2, rules.getLength());

		it = rules.iterator();

		nestedR = it.next();
		assertEquals(CSSRule.SUPPORTS_RULE, nestedR.getType());
		SupportsRule nestedSupportsR = (SupportsRule) nestedR;
		assertEquals("(text-align: match-parent)", nestedSupportsR.getConditionText());
		CSSRuleArrayList supportsRules = nestedSupportsR.getCssRules();
		assertEquals(3, supportsRules.getLength());

		Iterator<AbstractCSSRule> supportsIt = supportsRules.iterator();
		AbstractCSSRule supportsNestedR = supportsIt.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, supportsNestedR.getType());
		nestedDecl = (CSSNestedDeclarations) supportsNestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("0", nestedDecl.getStyle().getPropertyValue("padding-left"));

		supportsNestedR = supportsIt.next();
		assertEquals(CSSRule.STYLE_RULE, supportsNestedR.getType());
		nestedStyleR = (StyleRule) supportsNestedR;
		assertEquals(":not(&)~p", nestedStyleR.getSelectorText());
		assertEquals(":not(:is(ul,ol))~p", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(1, nestedStyleR.getStyle().getLength());
		assertEquals("match-parent", nestedStyleR.getStyle().getPropertyValue("text-align"));

		supportsNestedR = supportsIt.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, supportsNestedR.getType());
		nestedDecl = (CSSNestedDeclarations) supportsNestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("1px", nestedDecl.getStyle().getPropertyValue("padding-right"));

		assertFalse(supportsIt.hasNext());

		// Next nested from ul, ol

		nestedR = it.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, nestedR.getType());
		nestedDecl = (CSSNestedDeclarations) nestedR;
		assertEquals(2, nestedDecl.getStyle().getLength());
		assertEquals("1em", nestedDecl.getStyle().getPropertyValue("margin-top"));
		assertEquals("2em", nestedDecl.getStyle().getPropertyValue("margin-bottom"));

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("li", styleR.getSelectorText());
		assertEquals(2, styleR.getStyle().getLength());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("div", styleR.getSelectorText());
		assertEquals(2, styleR.getStyle().getLength());
		assertEquals(1, styleR.getCssRules().getLength());
		assertEquals(CSSRule.STYLE_RULE, styleR.getCssRules().item(0).getType());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("svg", styleR.getSelectorText());
		assertEquals(1, styleR.getStyle().getLength());
		assertEquals(2, styleR.getCssRules().getLength());
		assertEquals(CSSRule.STYLE_RULE, styleR.getCssRules().item(0).getType());
		assertEquals(CSSRule.NESTED_DECLARATIONS, styleR.getCssRules().item(1).getType());

		// Next top-level qualified rule

		styleR = (StyleRule) topIter.next();
		assertEquals("g", styleR.getSelectorText());
		assertEquals(0, styleR.getStyle().getLength());
		assertEquals(4, styleR.getCssRules().getLength());
		nestedR = styleR.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		nestedStyleR = (StyleRule) nestedR;
		assertEquals("&.label", nestedStyleR.getSelectorText());
		assertEquals("g.label", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(0, nestedStyleR.getStyle().getLength());

		rules = nestedStyleR.getCssRules();
		assertNotNull(rules);
		assertEquals(4, rules.getLength());

		it = rules.iterator();

		nestedR = it.next();
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		nestedStyleR = (StyleRule) nestedR;
		assertEquals("&:first-child", nestedStyleR.getSelectorText());
		assertEquals("g.label:first-child", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(2, nestedStyleR.getStyle().getLength());
		assertEquals("center", nestedStyleR.getStyle().getPropertyValue("text-align"));

		nestedR = it.next();
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		nestedStyleR = (StyleRule) nestedR;
		assertEquals("&:nth-last-child(2)", nestedStyleR.getSelectorText());
		assertEquals("g.label:nth-last-child(2)",
				nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(2, nestedStyleR.getStyle().getLength());
		assertEquals("#eec", nestedStyleR.getStyle().getPropertyValue("background-color"));

		nestedR = it.next();
		assertEquals(CSSRule.NESTED_DECLARATIONS, nestedR.getType());
		nestedDecl = (CSSNestedDeclarations) nestedR;
		assertEquals(1, nestedDecl.getStyle().getLength());
		assertEquals("currentColor", nestedDecl.getStyle().getPropertyValue("stroke"));

		nestedR = it.next();
		assertEquals(CSSRule.STYLE_RULE, nestedR.getType());
		nestedStyleR = (StyleRule) nestedR;
		assertEquals("&>div", nestedStyleR.getSelectorText());
		assertEquals("g.label>div", nestedStyleR.getAbsoluteSelectorList().toString());
		assertEquals(2, nestedStyleR.getStyle().getLength());
		assertEquals("nowrap", nestedStyleR.getStyle().getPropertyValue("white-space"));

		assertFalse(it.hasNext());

		assertFalse(topIter.hasNext());
	}

	@Test
	public void testNestedMediaRule() throws DOMException, IOException {
		parseStyleSheet("div{@media print{margin-left:0}}");
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals("div{@media print{margin-left:0}}", rule.getMinifiedCssText());
		assertEquals("div {\n    @media print {\n        margin-left: 0;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testNestedMediaRuleEOF() throws DOMException, IOException {
		parseStyleSheet("div{@media print{margin-left:0");
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals("div{@media print{margin-left:0}}", rule.getMinifiedCssText());
		assertEquals("div {\n    @media print {\n        margin-left: 0;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testNestedRuleMediaRule() throws DOMException, IOException {
		parseStyleSheet("div{&.cls{@media print{margin-left:0}}}");
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals("div{&.cls{@media print{margin-left:0}}}", rule.getMinifiedCssText());
		assertEquals(
				"div {\n    &.cls {\n        @media print {\n            margin-left: 0;\n        }\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testNestedMediaNested() throws DOMException, IOException {
		parseStyleSheet("div{&.cls{@media print{>p{margin-left:0}}}}");
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals("div{&.cls{@media print{&>p{margin-left:0}}}}", rule.getMinifiedCssText());
		assertEquals(
				"div {\n    &.cls {\n        @media print {\n            &>p {\n"
						+ "                margin-left: 0;\n            }\n        }\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testNestedSupportsRule() throws DOMException, IOException {
		parseStyleSheet("div{@supports(display: flex){margin-left:0}}");
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals("div{@supports(display:flex){margin-left:0}}", rule.getMinifiedCssText());
		assertEquals("div {\n    @supports (display: flex) {\n        margin-left: 0;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testNestedRuleSupportsRule() throws DOMException, IOException {
		parseStyleSheet("div{&.cls{@supports (display: flex){margin-left:0}}}");
		assertEquals(1, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals("div{&.cls{@supports(display:flex){margin-left:0}}}",
				rule.getMinifiedCssText());
		assertEquals(
				"div {\n    &.cls {\n        @supports (display: flex) {\n            margin-left: 0;\n        }\n    }\n}\n",
				rule.getCssText());
	}

	private void parseStyleSheet(String cssText) throws DOMException {
		try {
			sheet.parseStyleSheet(new StringReader(cssText), CSSStyleSheet.COMMENTS_IGNORE);
		} catch (IOException e) {
		}
	}

}
