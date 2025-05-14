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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

public class PropertyRuleTest {

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
		StringReader re = new StringReader(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		PropertyRule rule = (PropertyRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.PROPERTY_RULE, rule.getType());
		assertEquals("--my-color", rule.getName());

		CSSValueSyntax syntax = rule.getSyntax();
		assertNotNull(syntax);
		assertEquals(CSSValueSyntax.Category.color, syntax.getCategory());
		assertEquals(CSSValueSyntax.Multiplier.NONE, syntax.getMultiplier());

		assertFalse(rule.inherits());

		LexicalUnit initial = rule.getInitialValue();
		assertNotNull(initial);
		assertEquals(LexicalType.RGBCOLOR, initial.getLexicalUnitType());

		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:#047b42}",
				rule.getMinifiedCssText());
		assertEquals(
				"@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: #047b42;\n}\n",
				rule.getCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());

		visitor.reset();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());
		// Attempt to remove descriptors fails
		assertEquals(0, rule.getStyle().removeProperty("syntax").length());
		assertEquals(0, rule.getStyle().removeProperty("inherits").length());
		assertEquals(0, rule.getStyle().removeProperty("initial-value").length());
		// Setting invalid syntax fails
		try {
			rule.getStyle().setProperty("syntax", "<foo>", "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
		// Setting invalid inherit fails
		try {
			rule.getStyle().setProperty("inherits", "maybe", "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testParseRuleColorIdentifier() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: green;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		PropertyRule rule = (PropertyRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.PROPERTY_RULE, rule.getType());
		assertEquals("--my-color", rule.getName());

		CSSValueSyntax syntax = rule.getSyntax();
		assertNotNull(syntax);
		assertEquals(CSSValueSyntax.Category.color, syntax.getCategory());
		assertEquals(CSSValueSyntax.Multiplier.NONE, syntax.getMultiplier());

		assertFalse(rule.inherits());

		LexicalUnit initial = rule.getInitialValue();
		assertNotNull(initial);
		assertEquals(LexicalType.IDENT, initial.getLexicalUnitType());

		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:green}",
				rule.getMinifiedCssText());
		assertEquals(
				"@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: green;\n}\n",
				rule.getCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleUniversalSyntax() throws DOMException, IOException {
		StringReader re = new StringReader("@property --foo {syntax:'*'; inherits: false}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		PropertyRule rule = (PropertyRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.PROPERTY_RULE, rule.getType());
		assertEquals("--foo", rule.getName());
		assertFalse(rule.inherits());
		assertEquals("@property --foo {syntax:'*';inherits:false}", rule.getMinifiedCssText());
		assertEquals("@property --foo {\n    syntax: '*';\n    inherits: false;\n}\n",
				rule.getCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());

		assertEquals(0, rule.getStyle().removeProperty("syntax").length());
		assertEquals(0, rule.getStyle().removeProperty("inherits").length());
		assertEquals(0, rule.getStyle().removeProperty("not-a-descriptor").length());
	}

	@Test
	public void testParseRuleDescriptorBadDescriptorName() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax: '*'; inherits: false; @initial-value: #047b42;}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		PropertyRule rule = (PropertyRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.PROPERTY_RULE, rule.getType());
		assertEquals("--my-color", rule.getName());
		assertEquals("@property --my-color {syntax:'*';inherits:false}", rule.getMinifiedCssText());
		assertEquals(2, rule.getStyle().getLength());
	}

	@Test
	public void testParseRuleMissingDescriptorError() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color {inherits: false}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleMissingDescriptorError2() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color {syntax: '<color>'}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleMissingDescriptorError3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax:'<color>';inherits:false}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleDescriptorValueError() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color {syntax: 12em; inherits: false}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleDescriptorValueError2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax:'<color>'; inherits:3px;initial-value:#047b42}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleDescriptorValueError3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax:'<color>'; inherits:false;initial-value:12px}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my@color {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color; {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleError3() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError4() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError5() throws DOMException, IOException {
		StringReader re = new StringReader("@property ");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError6() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property 111 {syntax: '<color>'; inherits: false; initial-value: #111;}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void test() {
		PropertyRule rule = parseStyleSheet(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		assertEquals("--my-color", rule.getName());
		assertEquals(3, rule.getStyle().getLength());

		LexicalUnit initial = rule.getInitialValue();
		assertNotNull(initial);
		assertEquals(LexicalType.RGBCOLOR, initial.getLexicalUnitType());

		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:#047b42}",
				rule.getMinifiedCssText());
		assertEquals(
				"@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: #047b42;\n}\n",
				rule.getCssText());
	}

	@Test
	public void testComment() {
		PropertyRule rule = parseStyleSheet(
				"/* pre-rule */ @property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}/* post-rule */\n/* not-this-rule */");
		assertEquals("--my-color", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:#047b42}",
				rule.getMinifiedCssText());
		assertEquals(
				"/* pre-rule */\n@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: #047b42;\n} /* post-rule */\n",
				rule.getCssText());
	}

	@Test
	public void testMissingDescriptor() {
		assertNull(parseStyleSheet("@property --my-color {syntax: '<color>'; inherits: false}"));
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testBadDescriptorValue() {
		assertNull(parseStyleSheet("@property --my-color {syntax: '*'; inherits: 1px}"));
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testBadSyntaxDescriptor() {
		assertNull(parseStyleSheet("@property --my-color {syntax: ''; inherits: true}"));
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testBadSyntaxDescriptor2() {
		assertNull(parseStyleSheet("@property --my-color {syntax: '<color'; inherits: true}"));
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testBadSyntaxDescriptor3() {
		assertNull(parseStyleSheet("@property --my-color {syntax: '<foo>'; inherits: true}"));
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testInvalidDescriptorName() {
		PropertyRule rule = parseStyleSheet(
				"@property --my-color {syntax: '*'; inherits: false; @initial-value: #047b42;}");
		assertNotNull(rule);
		assertEquals(2, rule.getStyle().getLength());
		assertEquals("syntax", rule.getStyle().item(0));
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testInvalidDescriptorNameInvalidRule() {
		assertNull(parseStyleSheet(
				"@property --my-color {syntax: '<color>'; inherits: false; @initial-value: #047b42;}"));
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testEquals() {
		PropertyRule rule = parseStyleSheet(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		PropertyRule rule2 = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2 = parseStyleSheet(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());

		rule2 = parseStyleSheet(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: #090;}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		PropertyRule rule = parseStyleSheet(
				"@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		PropertyRule clon = rule.clone(sheet);
		assertEquals(rule.getName(), clon.getName());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	private PropertyRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (PropertyRule) sheet.getCssRules().item(0);
	}

}
