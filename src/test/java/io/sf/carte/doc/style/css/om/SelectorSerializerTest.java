/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class SelectorSerializerTest {

	private static TestCSSStyleSheetFactory factory;

	private AbstractCSSStyleSheet sheet;

	private SelectorSerializer serializer;

	@BeforeAll
	public static void setUpBeforeAll() {
		factory = new TestCSSStyleSheetFactory();
	}

	@BeforeEach
	public void setUp() {
		sheet = factory.createStyleSheet(null, null);
		serializer = new SelectorSerializer(sheet);
	}

	@Test
	public void testSelectorTextSelector() {
		StyleRule rule = parseStyleRule(
				"html:root + p:empty,span[foo~='bar'],span[foo='bar'],p:only-child,p:lang(en),p.someclass,a:link,span[class='example'] {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals(8, list.getLength());

		assertEquals("html:root+p:empty", serializer.selectorText(list.item(0), false));
		assertEquals("span[foo~='bar']", serializer.selectorText(list.item(1), false));
		assertEquals("span[foo='bar']", serializer.selectorText(list.item(2), false));
		assertEquals("p:only-child", serializer.selectorText(list.item(3), false));
		assertEquals("p:lang(en)", serializer.selectorText(list.item(4), false));
		assertEquals("p.someclass", serializer.selectorText(list.item(5), false));
		assertEquals("a:link", serializer.selectorText(list.item(6), false));
		assertEquals("span[class='example']", serializer.selectorText(list.item(7), false));
	}

	@Test
	public void testSelectorTextSelectorDQ() {
		StyleRule rule = parseStyleRule(
				"span[foo~='bar'],span[foo='bar'],span[class='example'] {border-top-width: 1px; }",
				CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
		SelectorList list = rule.getSelectorList();
		assertEquals(3, list.getLength());
		assertEquals("span[foo~=\"bar\"]", serializer.selectorText(list.item(0), false));
		assertEquals("span[foo=\"bar\"]", serializer.selectorText(list.item(1), false));
		assertEquals("span[class=\"example\"]", serializer.selectorText(list.item(2), false));
		rule = parseStyleRule("a[hreflang|='en'] {border-top-width: 1px; }");
		assertEquals("a[hreflang|=\"en\"]", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector2() {
		StyleRule rule = parseStyleRule(
				"ul li,h4[foo],a[hreflang|='en'] {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals("ul li,h4[foo],a[hreflang|='en']", rule.getSelectorText());
		assertEquals(3, list.getLength());
		assertEquals("ul li", serializer.selectorText(list.item(0), false));
		assertEquals("h4[foo]", serializer.selectorText(list.item(1), false));
		assertEquals("a[hreflang|='en']", serializer.selectorText(list.item(2), false));
	}

	@Test
	public void testSelectorTextSelector3() {
		StyleRule rule = parseStyleRule(
				"div ol>li p,p:first-line,p:hover {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals("div ol>li p,p::first-line,p:hover", rule.getSelectorText());
		assertEquals(3, list.getLength());
		assertEquals("div ol>li p", serializer.selectorText(list.item(0), false));
		assertEquals("p::first-line", serializer.selectorText(list.item(1), false));
		assertEquals("p:hover", serializer.selectorText(list.item(2), false));
	}

	@Test
	public void testSelectorTextSelector4() {
		StyleRule rule = parseStyleRule(".someclass, h1 > p, a:visited {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals(3, list.getLength());
		assertEquals(".someclass", serializer.selectorText(list.item(0), false));
		assertEquals("h1>p", serializer.selectorText(list.item(1), false));
		assertEquals("a:visited", serializer.selectorText(list.item(2), false));
		assertEquals(".someclass,h1>p,a:visited", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector5() {
		StyleRule rule = parseStyleRule(
				"*, p *, * p, p > *, * > p, * + p, * .foo, *:only-child, *[foo='bar'] {border-top-width: 1px; }");
		assertEquals("*,p *,* p,p>*,*>p,*+p,* .foo,:only-child,[foo='bar']",
				rule.getSelectorText());
		SelectorList list = rule.getSelectorList();
		assertEquals("*", serializer.selectorText(list.item(0), false));
		assertEquals("p *", serializer.selectorText(list.item(1), false));
		assertEquals("* p", serializer.selectorText(list.item(2), false));
		assertEquals("p>*", serializer.selectorText(list.item(3), false));
		assertEquals("*>p", serializer.selectorText(list.item(4), false));
		assertEquals("[foo='bar']", serializer.selectorText(list.item(8), true));
		assertEquals(9, list.getLength());
	}

	@Test
	public void testSelectorTextAttributeSelector() {
		StyleRule rule = parseStyleRule(
				"span[class=\"example\"][foo=\"'bar\"],:rtl * {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals("span[class='example'][foo=\"'bar\"],:rtl *", rule.getSelectorText());
		assertEquals(2, list.getLength());
		assertEquals("span[class='example'][foo=\"'bar\"]",
				serializer.selectorText(list.item(0), false));
		assertEquals(":rtl *", serializer.selectorText(list.item(1), false));
	}

	@Test
	public void testSelectorTextAttributeSelectorDQ() {
		StyleRule rule = parseStyleRule(
				"span[class=\"example\"][foo=\"bar\"],:rtl * {border-top-width: 1px; }",
				CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
		SelectorList list = rule.getSelectorList();
		assertEquals("span[class=\"example\"][foo=\"bar\"],:rtl *", rule.getSelectorText());
		assertEquals(2, list.getLength());
		assertEquals("span[class=\"example\"][foo=\"bar\"]",
				serializer.selectorText(list.item(0), false));
		assertEquals(":rtl *", serializer.selectorText(list.item(1), false));
	}

	private StyleRule parseStyleRule(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (StyleRule) sheet.getCssRules().item(0);
	}

	private StyleRule parseStyleRule(String cssText, short flag) {
		TestCSSStyleSheetFactory f = new TestCSSStyleSheetFactory();
		f.setFactoryFlag(flag);
		sheet = f.createStyleSheet(null, null);
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		serializer = new SelectorSerializer(sheet);
		return (StyleRule) sheet.getCssRules().item(0);
	}

}
