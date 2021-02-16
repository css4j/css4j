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
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class PropertyRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
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
		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:#047b42}",
				rule.getMinifiedCssText());
		assertEquals("@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: #047b42;\n}\n",
				rule.getCssText());
		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());
		//
		visitor.reset();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());
	}

	@Test
	public void testParseRuleMissingDescriptorError() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {inherits: false}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleMissingDescriptorError2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax: '<color>'}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleDescriptorValueError() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax: 12em; inherits: false}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleDescriptorValueError2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@property --my-color {syntax: '<color>'; inherits: 3px}");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my@color {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError2() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color; {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError3() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color {");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError4() throws DOMException, IOException {
		StringReader re = new StringReader("@property --my-color");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testParseRuleError5() throws DOMException, IOException {
		StringReader re = new StringReader("@property ");
		sheet.parseStyleSheet(re);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testSetCssTextString() {
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		assertEquals("--my-color", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:#047b42}",
				rule.getMinifiedCssText());
		assertEquals("@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: #047b42;\n}\n",
				rule.getCssText());
	}

	@Test
	public void testSetCssTextStringComment() {
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("/* pre-rule */ @property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		assertEquals("--my-color", rule.getName());
		assertEquals(3, rule.getStyle().getLength());
		assertEquals("@property --my-color {syntax:'<color>';inherits:false;initial-value:#047b42}",
				rule.getMinifiedCssText());
		assertEquals("@property --my-color {\n    syntax: '<color>';\n    inherits: false;\n    initial-value: #047b42;\n}\n",
				rule.getCssText());
	}

	@Test
	public void testSetCssTextStringBad() {
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@property --my-color {syntax: '<color>'; inherits: false; @initial-value: #047b42;}");
		assertEquals("@property --my-color {syntax:'<color>';inherits:false}", rule.getMinifiedCssText());
		assertEquals(2, rule.getStyle().getLength());
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
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
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
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
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		PropertyRule rule2 = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		assertTrue(rule.equals(rule2));
		rule2.setCssText("@property --my-color {syntax: '<color>'; inherits: false; initial-value: #090;}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		PropertyRule rule = new PropertyRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@property --my-color {syntax: '<color>'; inherits: false; initial-value: #047b42;}");
		PropertyRule clon = rule.clone(sheet);
		assertEquals(rule.getName(), clon.getName());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}
}
