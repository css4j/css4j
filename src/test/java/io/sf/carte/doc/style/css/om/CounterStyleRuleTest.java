/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;

public class CounterStyleRuleTest {

	private static TestCSSStyleSheetFactory factory;

	private AbstractCSSStyleSheet sheet;

	@BeforeAll
	public static void setUpBeforeAll() {
		factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
	}

	@BeforeEach
	public void setUp() {
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws DOMException, IOException {
		// Rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		StringReader re = new StringReader(
				"@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		CounterStyleRule rule = (CounterStyleRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.COUNTER_STYLE_RULE, rule.getType());
		assertEquals("@counter-style thumbs{system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals(
				"@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
				rule.getCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());

		visitor.reset();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());
	}

	@Test
	public void testParseRuleInvalidNone() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style none {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
	}

	@Test
	public void testParseRuleInvalidCustomIdent() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style inherit {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
	}

	/*
	 * Name is a valid list-style-position
	 */
	@Test
	public void testParseRuleInvalidCounterStyleName() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style outside {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasOMErrors());
	}

	/*
	 * Name is a valid list-style-type
	 */
	@Test
	public void testParseRuleInvalidCounterStyleName2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style disc {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasOMErrors());
	}

	@Test
	public void testParseRuleErrorNoDescriptorsNoClosing() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style outside {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasOMErrors());
	}

	@Test
	public void testParseRuleErrorNoBody() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style outside");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style ");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleSyntaxError() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style outsi@de {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorBadIdent() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style foo. {system: cyclic; symbols: \\1F44D; suffix: \" \";}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorRecovery() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@;@counter-style thumbs{system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \"");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		CounterStyleRule rule = (CounterStyleRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.COUNTER_STYLE_RULE, rule.getType());
		assertEquals("@counter-style thumbs{system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals(
				"@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
				rule.getCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());
	}

	@Test
	public void testParseRuleErrorRecoveryStringEOF() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@;@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" ");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		CounterStyleRule rule = (CounterStyleRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.COUNTER_STYLE_RULE, rule.getType());
		assertEquals("@counter-style thumbs{system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals(
				"@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
				rule.getCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParse() {
		CounterStyleRule rule = parseStyleSheet(
				"@counter-style go{system:alphabetic;symbols:url(white.svg)\nurl(black.svg);suffix:\" \";}");

		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());

		assertEquals("go", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals(
				"@counter-style go{system:alphabetic;symbols:url('white.svg') url('black.svg');suffix:\" \"}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseComment() {
		// Based on rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		CounterStyleRule rule = parseStyleSheet(
				"/* pre-rule */ @counter-style/* pre-name */thumbs/*pre-left-b*/{/*post-left-b*/"
				+ "system/* post-desc-name */:/* pre-value */cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}/* post-rule */");
		assertEquals("thumbs", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@counter-style thumbs{system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals(
				"/* pre-rule */\n@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n} /* post-rule */\n",
				rule.getCssText());
	}

	@Test
	public void testParseBad() {
		CounterStyleRule rule = parseStyleSheet(
				"@counter-style thumbs {@system:cyclic;symbols:@12;suffix:\" \"}");
		assertEquals(1, rule.getStyle().getLength());
		assertEquals("suffix: \" \";", rule.getStyle().getCssText().trim());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testEquals() {
		CounterStyleRule rule = parseStyleSheet(
				"@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		CounterStyleRule rule2 = parseStyleSheet(
				"@counter-style footnote {\nsystem: symbolic;\nsymbols: '*' ⁑;\nsuffix: \" \";}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		CounterStyleRule rule = parseStyleSheet(
				"@counter-style thumbs {system:cyclic;symbols:\\1F44D;suffix: \" \"}");
		CounterStyleRule clon = rule.clone(sheet);
		assertEquals(rule.getName(), clon.getName());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	private CounterStyleRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (CounterStyleRule) sheet.getCssRules().item(0);
	}

}
