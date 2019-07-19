/*

 Copyright (c) 2005-2019, Carlos Amengual.

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

public class FontFeatureValuesRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseRule() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-feature-values Some Font, Other Font {@swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1; }}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@font-feature-values 'Some Font', 'Other Font' {\n    @swash {\n        swishy: 1;\n        flowing: 2;\n    }\n    @styleset {\n        double-W: 14;\n        sharp-terminals: 16 1;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleQuotedFF() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-feature-values 'Some Font' {@swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1; }}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals(
				"@font-feature-values Some Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@font-feature-values 'Some Font' {\n    @swash {\n        swishy: 1;\n        flowing: 2;\n    }\n    @styleset {\n        double-W: 14;\n        sharp-terminals: 16 1;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleQuotedFF2() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-feature-values 'Some Font', 'Other Font' {@swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1; }}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(ExtendedCSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
		assertEquals(
				"@font-feature-values 'Some Font', 'Other Font' {\n    @swash {\n        swishy: 1;\n        flowing: 2;\n    }\n    @styleset {\n        double-W: 14;\n        sharp-terminals: 16 1;\n    }\n}\n",
				rule.getCssText());
	}

	@Test
	public void testParseRuleBad() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-feature-values Some Font, Other Font {@swash 'foo'}"));
		sheet.parseCSSStyleSheet(source);
		assertTrue(sheet.getErrorHandler().hasOMErrors());
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testSetCssTextString() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextStringError() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values $Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringError2() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values +myfont{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringError3() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values myfont+{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringError4() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values myfont,+{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testEquals() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		FontFeatureValuesRule rule2 = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@font-feature-values Some Font,Other {@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
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
	public void testCloneAbstractCSSStyleSheet() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-feature-values Some Font {@swash { swishy: 1; flowing: 2; }}");
		FontFeatureValuesRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

}
