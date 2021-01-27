/*

 Copyright (c) 2005-2021, Carlos Amengual.

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

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.ExtendedCSSRule;

public class CounterStyleRuleTest {

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
		InputSource source = new InputSource(
				new StringReader("@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}"));
		sheet.parseStyleSheet(source);
		assertEquals(1, sheet.getCssRules().getLength());
		CounterStyleRule rule = (CounterStyleRule) sheet.getCssRules().item(0);
		assertEquals(ExtendedCSSRule.COUNTER_STYLE_RULE, rule.getType());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}", rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1F44D;\n    suffix: \" \";\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleError1() throws DOMException, IOException {
		InputSource source = new InputSource(
				new StringReader("@counter-style inherit {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}"));
		sheet.parseStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		InputSource source = new InputSource(
				new StringReader("@counter-style outside {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}"));
		sheet.parseStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError3() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader("@counter-style outside {"));
		sheet.parseStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError4() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader("@counter-style outside"));
		sheet.parseStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError5() throws DOMException, IOException {
		InputSource source = new InputSource(new StringReader("@counter-style "));
		sheet.parseStyleSheet(source);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testSetCssTextString() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		// Rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		rule.setCssText("@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		assertEquals("thumbs", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}", rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1F44D;\n    suffix: \" \";\n}\n",
				rule.getCssText());
	}

	@Test
	public void testSetCssTextStringCR() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@counter-style\nthumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		assertEquals("thumbs", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}", rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1F44D;\n    suffix: \" \";\n}\n",
				rule.getCssText());
	}

	@Test
	public void testSetCssTextStringBad() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@counter-style thumbs {@system:cyclic;symbols:@12;suffix:\" \"}");
		assertEquals("@counter-style thumbs {suffix:\" \"}", rule.getMinifiedCssText());
		assertEquals(1, rule.getStyle().getLength());
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		assertEquals("", rule.getMinifiedCssText());
		assertEquals("", rule.getCssText());
	}

	@Test
	public void testEquals() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		CounterStyleRule rule2 = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@counter-style footnote {\nsystem: symbolic;\nsymbols: '*' ⁑;\nsuffix: \" \";}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@counter-style thumbs {system:cyclic;symbols:\\1F44D;suffix: \" \"}");
		CounterStyleRule clon = rule.clone(sheet);
		assertEquals(rule.getName(), clon.getName());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}
}
