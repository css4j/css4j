/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class CounterStyleRuleTest {

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
				"@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		CounterStyleRule rule = (CounterStyleRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.COUNTER_STYLE_RULE, rule.getType());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
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
	public void testParseRuleError1() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style inherit {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@counter-style outside {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError3() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style outside {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError4() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style outside");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError5() throws DOMException, IOException {
		StringReader re = new StringReader("@counter-style ");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError6() throws DOMException, IOException {
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
				"@;@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \"");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		CounterStyleRule rule = (CounterStyleRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.COUNTER_STYLE_RULE, rule.getType());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
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
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
				rule.getCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testSetCssTextString() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		// Rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		rule.setCssText("@counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}");
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals("thumbs", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals("@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n}\n",
				rule.getCssText());
	}

	@Test
	public void testSetCssTextStringComment() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		// Rule taken from mozilla website
		// https://developer.mozilla.org/en-US/docs/Web/CSS/@counter-style
		rule.setCssText(
				"/* pre-rule */ @counter-style thumbs {system: cyclic;\nsymbols: \\1F44D;\n suffix: \" \";\n}/* post-rule */");
		assertEquals("thumbs", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@counter-style thumbs {system:cyclic;symbols:\uD83D\uDC4D;suffix:\" \"}",
				rule.getMinifiedCssText());
		assertEquals(
				"/* pre-rule */\n@counter-style thumbs {\n    system: cyclic;\n    symbols: \\1f44d ;\n    suffix: \" \";\n} /* post-rule */\n",
				rule.getCssText());
	}

	@Test
	public void testSetCssTextStringBad() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		assertThrows(DOMException.class, () -> rule
			.setCssText("@counter-style thumbs {@system:cyclic;symbols:@12;suffix:\" \"}"));
		assertEquals(0, rule.getStyle().getLength());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
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
	public void testSetCssTextStringWrongRule2() {
		CounterStyleRule rule = new CounterStyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"/* pre-rule */ @font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
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
