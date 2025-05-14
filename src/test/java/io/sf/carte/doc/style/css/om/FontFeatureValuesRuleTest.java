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

import io.sf.carte.doc.style.css.CSSFontFeatureValuesMap;
import io.sf.carte.doc.style.css.CSSFontFeatureValuesRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.TypedValue;

public class FontFeatureValuesRuleTest {

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
	public void testParseRule() throws IOException {
		StringReader re = new StringReader(
				"/* pre-rule */@font-feature-values /* skip 1 */ Some Font, Other Font /* skip 2 */ {/* pre-swash */@swash /* skip 3 */{ swishy: 1; flowing: 2; } /* post-swash */\n/* pre-styleset */@styleset /* skip 4 */{ double-W: 14; sharp-terminals: 16 1; } /* post-styleset */} /* post-rule */");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		CSSFontFeatureValuesMap swash = rule.getSwash();
		assertEquals(1, ((TypedValue) swash.get("swishy")[0]).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, swash.getPrecedingComments().size());
		assertEquals(" pre-swash ", swash.getPrecedingComments().get(0));
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
		assertEquals(
				"/* pre-rule */\n@font-feature-values 'Some Font', 'Other Font' {\n    /* pre-swash */\n    @swash {\n        swishy: 1;\n        flowing: 2;\n    } /* post-swash */\n    /* pre-styleset */\n    @styleset {\n        double-W: 14;\n        sharp-terminals: 16 1;\n    } /* post-styleset */\n} /* post-rule */\n",
				rule.getCssText());
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, rule.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule.getPrecedingComments().get(0));

		CSSFontFeatureValuesMap swash2 = rule.getFeatureValuesMap("swash");
		assertTrue(swash == swash2);
		assertTrue(rule.getAnnotation() == rule.getFeatureValuesMap("annotation"));
		assertTrue(rule.getOrnaments() == rule.getFeatureValuesMap("ornaments"));
		assertTrue(rule.getStyleset() == rule.getFeatureValuesMap("styleset"));
		assertTrue(rule.getStylistic() == rule.getFeatureValuesMap("stylistic"));

		NumberValue number = new NumberValue();
		number.setIntegerValue(4);
		CSSFontFeatureValuesMap annot = rule.getAnnotation();
		annot.set("boxed", number);
		assertEquals(4f, ((TypedValue) annot.get("boxed")[0]).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);
		number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 4f);
		annot.set("boxed", number);
		assertEquals(4f, ((TypedValue) annot.get("boxed")[0]).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);
		number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 3.5f);
		try {
			annot.set("boxed", number);
			fail("Must throw eception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testParseRuleCalc() throws IOException {
		StringReader re = new StringReader(
				"@font-feature-values Some Font {@swash { swishy: 1; flowing: calc(1 + 1); } @styleset { double-W: var(--doubleW, 2); sharp-terminals: 16 1; }}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(1, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		CSSFontFeatureValuesMap swash = rule.getSwash();
		assertEquals(1, ((TypedValue) swash.get("swishy")[0]).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-6f);
		PrimitiveValue primi = swash.get("flowing")[0];
		assertEquals(CSSValue.Type.EXPRESSION, primi.getPrimitiveType());

		primi = rule.getStyleset().get("double-W")[0];
		assertEquals(CSSValue.Type.LEXICAL, primi.getPrimitiveType());

		assertEquals(
				"@font-feature-values Some Font{@swash{swishy:1;flowing:calc(1 + 1)}@styleset{double-W:var(--doubleW,2);sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleQuotedFF() throws IOException {
		StringReader re = new StringReader(
				"@font-feature-values 'Some Font' {@swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1; }}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
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
		StringReader re = new StringReader(
				"@font-feature-values 'Some Font', 'Other Font' {@swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1; }}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
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
	public void testParseRuleQuotedErrorRecovery() throws IOException {
		StringReader re = new StringReader(
				"@;@font-feature-values 'Some Font', 'Other Font' {@swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1");
		assertFalse(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
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
		StringReader re = new StringReader(
				"@font-feature-values Some Font, Other Font {@swash 'foo'}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals("@font-feature-values Some Font,Other Font{}", rule.getMinifiedCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleErrorRecovery() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values Bongo {\n" + "    @swash {ornate: 1}\n"
						+ "    annotation {boxed: 4} /* should be @annotation! */\n"
						+ "    @swash {double-loops: 1;flowing: 1}\n"
						+ "    @ornaments ; /* incomplete definition */\n"
						+ "    @styleset {double-W:14;sharp-terminals:16 1} /* missing ; */\n"
						+ "    redrum  /* random editing mistake */}");
		assertEquals("Bongo", rule.getFontFamily()[0]);
		assertEquals(
				"@font-feature-values Bongo{@swash{ornate:1;double-loops:1;flowing:1}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseRuleCR() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values\nSome Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleComment() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"/* pre-rule */ @font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testParseRuleError() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values $Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError2() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values +myfont{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError3() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values myfont+{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError4() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values myfont,+{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError5() {
		FontFeatureValuesRule rule = parseStyleSheet("@font-feature-values myfont");
		assertNull(rule);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleError6() {
		FontFeatureValuesRule rule = parseStyleSheet("@font-feature-values ");
		assertNull(rule);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleErrorKeyword1() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values None{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleErrorKeyword2() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values inherit{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testParseRuleErrorKeyword3() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values initial{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertNull(rule);

		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
	}

	@Test
	public void testStylesetSetStringPrimitiveValue() {
		String[] ff = { "Arial", "Helvetica" };
		CSSFontFeatureValuesRule rule = sheet.createFontFeatureValuesRule(ff);
		NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 14f);
		rule.getStyleset().set("sharp-terminals", number);
		assertEquals("@font-feature-values Arial,Helvetica{@styleset{sharp-terminals:14}}",
				rule.getMinifiedCssText());

		NumberValue number2 = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 1f);
		PrimitiveValue[] pvarray = { number, number2 };
		rule.getStyleset().set("sharp-terminals", pvarray);
		assertEquals("@font-feature-values Arial,Helvetica{@styleset{sharp-terminals:14 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testStylesetSetStringPrimitiveValueError() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values \"Font\"{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 14f);
		try {
			rule.getStyleset().set("sharp-terminals", number);
			fail("Must throw exception");
		} catch (DOMException e) {
		}

		PrimitiveValue[] pvarray = { null, null };
		try {
			rule.getStyleset().set("sharp-terminals", pvarray);
			fail("Must throw exception");
		} catch (DOMException e) {
		}

		try {
			rule.getStyleset().set("sharp-terminals", new IdentifierValue("foo"));
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testEquals() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		FontFeatureValuesRule rule2 = parseStyleSheet(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertTrue(rule.equals(rule2));
		assertTrue(rule.hashCode() == rule2.hashCode());
		rule2 = parseStyleSheet(
				"@font-feature-values Some Font,Other {@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertFalse(rule.equals(rule2));
		rule2 = parseStyleSheet(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16}}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		FontFeatureValuesRule rule = parseStyleSheet(
				"@font-feature-values Some Font {@swash { swishy: 1; flowing: 2; }}");
		FontFeatureValuesRule clon = rule.clone(factory.createStyleSheet(null, null));
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	private FontFeatureValuesRule parseStyleSheet(String cssText) {
		sheet.getCssRules().clear();
		try {
			sheet.parseStyleSheet(new StringReader(cssText));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return (FontFeatureValuesRule) sheet.getCssRules().item(0);
	}

}
