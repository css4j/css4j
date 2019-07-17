/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.EnumSet;

import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.SACParserFactory;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class BaseCSSStyleSheetTest2 {

	@Test
	public void testGetSelectorsForPropertyValue() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		Selector[] selectors = sheet.getSelectorsForPropertyValue("display", "table-caption");
		assertEquals(1, selectors.length);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, selectors[0].getSelectorType());
		assertEquals("caption", ((ElementSelector) selectors[0]).getLocalName());
	}

	@Test
	public void testGetSelectorsForPropertyValue2() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		Selector[] selectors = sheet.getSelectorsForPropertyValue("outline", "auto");
		assertEquals(1, selectors.length);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, selectors[0].getSelectorType());
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION,
				((ConditionalSelector) selectors[0]).getCondition().getConditionType());
	}

	@Test
	public void testGetSelectorsForProperty() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		Selector[] selectors = sheet.getSelectorsForProperty("outline-style");
		assertEquals(2, selectors.length);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, selectors[0].getSelectorType());
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION,
				((ConditionalSelector) selectors[0]).getCondition().getConditionType());
	}

	@Test
	public void testParseCSSStyleSheetInvalidValue() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "li.foo{color:rgb(12, 255); display: block;}";
		Reader re = new StringReader(csstext);
		InputSource source = new InputSource(re);
		css.parseCSSStyleSheet(source);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(1, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings()); // Accounts for SAC errors
		assertEquals("li.foo {\n    display: block;\n}\n", css.toString());
	}

	@Test
	public void testParseCSSStyleSheetInvalidRule() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = ".foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}";
		Reader re = new StringReader(csstext);
		InputSource source = new InputSource(re);
		css.parseCSSStyleSheet(source);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(2, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings());
		assertEquals(".foo{margin-left:0;margin-right:auto}", css.toMinifiedString());
	}

	@Test
	public void testParseCSSStyleSheetInvalidRule2() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@media screen and (min-width: 768px){.foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}}";
		Reader re = new StringReader(csstext);
		InputSource source = new InputSource(re);
		css.parseCSSStyleSheet(source);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.MEDIA_RULE, rules.item(0).getType());
		MediaRule mediarule = (MediaRule) rules.item(0);
		rules = mediarule.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(2, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings());
		assertEquals("@media screen and(min-width:768px){.foo{margin-left:0;margin-right:auto}}",
				css.toMinifiedString());
	}

	@Test
	public void testParseCSSStyleSheetSupportsRule() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@supports(display:list-item)and(width:max-content){li.foo{width:max-content}}";
		Reader re = new StringReader(csstext);
		InputSource source = new InputSource(re);
		assertTrue(css.parseCSSStyleSheet(source));
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(ExtendedCSSRule.SUPPORTS_RULE, rules.item(0).getType());
		assertFalse(css.getErrorHandler().hasSacErrors());
		assertFalse(css.getErrorHandler().hasOMErrors());
		assertEquals(csstext, css.toString().replace('\n', ' ').replace(" ", "").replace(";}", "}"));
	}

	@Test
	public void testParseCSSStyleSheetIEMediaRule() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser2.Flag.IEVALUES));
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@media screen\\0 {li.foo {width: max-content}}";
		Reader re = new StringReader(csstext);
		InputSource source = new InputSource(re);
		assertTrue(css.parseCSSStyleSheet(source));
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		AbstractCSSRule rule = rules.item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		assertFalse(css.getErrorHandler().hasSacErrors());
		assertEquals(csstext, rule.getCssText().replace('\n', ' ').replace("; }", "}"));
	}

	@Test
	public void testToString() throws IOException {
		AbstractCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadSampleSheet(SACParserFactory.DEFAULT_PARSER);
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleCSSReader();
		CharBuffer target = CharBuffer.allocate(600);
		assertTrue(re.read(target) != -1);
		re.close();
		target.flip();
		String expected = target.toString().replace('\r', ' ').replace('\n', ' ').replace(" ", "").replace(";}", "}");
		assertEquals(expected, sheet.toString().replace('\n', ' ').replace(" ", "").replace(";}", "}"));
	}

	@Test
	public void testToString2() throws IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		InputSource source = new InputSource(new StringReader(
				"/** Comment 1 **/\n@media print {/** Comment 2 **/\n@page {margin-top: 20%;}h3 {width: 80%}/** Comment 3 **/}\n"));
		assertTrue(css.parseCSSStyleSheet(source));
		assertEquals(
				"/** Comment 1 **/\n@media print {\n    /** Comment 2 **/\n    @page {\n        margin-top: 20%;\n    }\n    h3 {\n        width: 80%;\n    }\n}\n",
				css.toString());
	}

}
