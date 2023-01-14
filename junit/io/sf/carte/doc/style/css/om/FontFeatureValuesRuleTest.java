/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSFontFeatureValuesMap;
import io.sf.carte.doc.style.css.CSSFontFeatureValuesRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.TypedValue;

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
		StringReader re = new StringReader(
				"/* pre-rule */@font-feature-values /* skip 1 */ Some Font, Other Font /* skip 2 */ {/* pre-swash */@swash /* skip 3 */{ swishy: 1; flowing: 2; } /* post-swash */\n/* pre-styleset */@styleset /* skip 4 */{ double-W: 14; sharp-terminals: 16 1; }}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, sheet.getCssRules().item(0).getType());
		FontFeatureValuesRule rule = (FontFeatureValuesRule) sheet.getCssRules().item(0);
		assertEquals(2, rule.getFontFamily().length);
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		CSSFontFeatureValuesMap swash = rule.getSwash();
		assertEquals(1, ((TypedValue) swash.get("swishy")[0]).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, swash.getPrecedingComments().size());
		assertEquals(" pre-swash ", swash.getPrecedingComments().get(0));
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
		assertEquals(
				"/* pre-rule */\n@font-feature-values 'Some Font', 'Other Font' {\n    /* pre-swash */\n    @swash {\n        swishy: 1;\n        flowing: 2;\n    }\n    /* pre-styleset */\n    @styleset {\n        double-W: 14;\n        sharp-terminals: 16 1;\n    }\n}\n",
				rule.getCssText());
		assertNotNull(rule.getPrecedingComments());
		assertEquals(1, rule.getPrecedingComments().size());
		assertEquals(" pre-rule ", rule.getPrecedingComments().get(0));
		//
		CSSFontFeatureValuesMap swash2 = rule.getFeatureValuesMap("swash");
		assertTrue(swash == swash2);
		assertTrue(rule.getAnnotation() == rule.getFeatureValuesMap("annotation"));
		assertTrue(rule.getOrnaments() == rule.getFeatureValuesMap("ornaments"));
		assertTrue(rule.getStyleset() == rule.getFeatureValuesMap("styleset"));
		assertTrue(rule.getStylistic() == rule.getFeatureValuesMap("stylistic"));
		//
		NumberValue number;
		number = new NumberValue();
		number.setIntegerValue(4);
		CSSFontFeatureValuesMap annot = rule.getAnnotation();
		annot.set("boxed", number);
		assertEquals(4f, ((TypedValue) annot.get("boxed")[0]).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 4f);
		annot.set("boxed", number);
		assertEquals(4f, ((TypedValue) annot.get("boxed")[0]).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
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
		assertEquals(1, ((TypedValue) swash.get("swishy")[0]).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		PrimitiveValue primi = swash.get("flowing")[0];
		assertEquals(CSSValue.Type.EXPRESSION, primi.getPrimitiveType());
		//
		primi = rule.getStyleset().get("double-W")[0];
		assertEquals(CSSValue.Type.VAR, primi.getPrimitiveType());
		//
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
	public void testParseRuleBad() throws IOException {
		StringReader re = new StringReader("@font-feature-values Some Font, Other Font {@swash 'foo'}");
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
	public void testSetCssTextStringCR() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@font-feature-values\nSome Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertEquals("Some Font", rule.getFontFamily()[0]);
		assertEquals("Other Font", rule.getFontFamily()[1]);
		assertEquals(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextStringComment() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"/* pre-rule */ @font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
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
	public void testSetCssTextStringError5() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText("@font-feature-values myfont");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringError6() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText("@font-feature-values ");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringErrorKeyword1() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values None{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringErrorKeyword2() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values inherit{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testSetCssTextStringErrorKeyword3() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@font-feature-values initial{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testStylesetSetStringPrimitiveValue() {
		String[] ff = { "Arial", "Helvetica" };
		CSSFontFeatureValuesRule rule = sheet.createFontFeatureValuesRule(ff);
		NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 14f);
		rule.getStyleset().set("sharp-terminals", number);
		assertEquals("@font-feature-values Arial,Helvetica{@styleset{sharp-terminals:14}}", rule.getMinifiedCssText());
		//
		NumberValue number2 = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 1f);
		PrimitiveValue[] pvarray = { number, number2 };
		rule.getStyleset().set("sharp-terminals", pvarray);
		assertEquals("@font-feature-values Arial,Helvetica{@styleset{sharp-terminals:14 1}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testStylesetSetStringPrimitiveValueError() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_PX, 14f);
		try {
			rule.getStyleset().set("sharp-terminals", number);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		//
		PrimitiveValue[] pvarray = { null, null };
		try {
			rule.getStyleset().set("sharp-terminals", pvarray);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		//
		try {
			rule.getStyleset().set("sharp-terminals", new IdentifierValue("foo"));
			fail("Must throw exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testEquals() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		FontFeatureValuesRule rule2 = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertTrue(rule.equals(rule2));
		assertTrue(rule.hashCode() == rule2.hashCode());
		rule2.setCssText(
				"@font-feature-values Some Font,Other {@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
		assertFalse(rule.equals(rule2));
		rule2.setCssText(
				"@font-feature-values Some Font,Other Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16}}");
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
	public void testSetCssTextStringWrongRule2() {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@keyframes Font{@swash{swishy:1;flowing:2}@styleset{double-W:14;sharp-terminals:16 1}}");
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
