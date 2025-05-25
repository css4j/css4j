/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.Selector;
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

		assertEquals("html:root+p:empty", selectorText(list.item(0)));
		assertEquals("span[foo~='bar']", selectorText(list.item(1)));
		assertEquals("span[foo='bar']", selectorText(list.item(2)));
		assertEquals("p:only-child", selectorText(list.item(3)));
		assertEquals("p:lang(en)", selectorText(list.item(4)));
		assertEquals("p.someclass", selectorText(list.item(5)));
		assertEquals("a:link", selectorText(list.item(6)));
		assertEquals("span[class='example']", selectorText(list.item(7)));
	}

	@Test
	public void testSelectorTextSelectorDQ() {
		StyleRule rule = parseStyleRule(
				"span[foo~='bar'],span[foo='bar'],span[class='example'] {border-top-width: 1px; }",
				CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
		SelectorList list = rule.getSelectorList();
		assertEquals(3, list.getLength());
		assertEquals("span[foo~=\"bar\"]", selectorText(list.item(0)));
		assertEquals("span[foo=\"bar\"]", selectorText(list.item(1)));
		assertEquals("span[class=\"example\"]", selectorText(list.item(2)));
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
		assertEquals("ul li", selectorText(list.item(0)));
		assertEquals("h4[foo]", selectorText(list.item(1)));
		assertEquals("a[hreflang|='en']", selectorText(list.item(2)));
	}

	@Test
	public void testSelectorTextSelector3() {
		StyleRule rule = parseStyleRule(
				"div ol>li p,p:first-line,p:hover {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals("div ol>li p,p::first-line,p:hover", rule.getSelectorText());
		assertEquals(3, list.getLength());
		assertEquals("div ol>li p", selectorText(list.item(0)));
		assertEquals("p::first-line", selectorText(list.item(1)));
		assertEquals("p:hover", selectorText(list.item(2)));
	}

	@Test
	public void testSelectorTextSelector4() {
		StyleRule rule = parseStyleRule(".someclass, h1 > p, a:visited {border-top-width: 1px; }");
		SelectorList list = rule.getSelectorList();
		assertEquals(3, list.getLength());
		assertEquals(".someclass", selectorText(list.item(0)));
		assertEquals("h1>p", selectorText(list.item(1)));
		assertEquals("a:visited", selectorText(list.item(2)));
		assertEquals(".someclass,h1>p,a:visited", rule.getSelectorText());
	}

	@Test
	public void testSelectorTextSelector5() {
		StyleRule rule = parseStyleRule(
				"*, p *, * p, p > *, * > p, * + p, * .foo, *:only-child, *[foo='bar'] {border-top-width: 1px; }");
		assertEquals("*,p *,* p,p>*,*>p,*+p,* .foo,:only-child,[foo='bar']",
				rule.getSelectorText());
		SelectorList list = rule.getSelectorList();
		assertEquals("*", selectorText(list.item(0)));
		assertEquals("p *", selectorText(list.item(1)));
		assertEquals("* p", selectorText(list.item(2)));
		assertEquals("p>*", selectorText(list.item(3)));
		assertEquals("*>p", selectorText(list.item(4)));
		assertEquals("[foo='bar']", selectorText(list.item(8), true));
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
				selectorText(list.item(0)));
		assertEquals(":rtl *", selectorText(list.item(1)));
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
				selectorText(list.item(0)));
		assertEquals(":rtl *", selectorText(list.item(1)));
	}

	private String selectorText(Selector sel) {
		return selectorText(sel, false);
	}

	private String selectorText(Selector sel, boolean omitUniversal) {
		StringBuilder buf = new StringBuilder();
		serializer.selectorText(buf, sel, omitUniversal);
		return buf.toString();
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
