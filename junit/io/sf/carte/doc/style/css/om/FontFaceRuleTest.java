/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.SheetErrorHandler;

public class FontFaceRuleTest {

	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testParseFontFaceRule() throws IOException {
		InputSource source = new InputSource(
				new StringReader("@font-face{font-family:'Mechanical Bold';src:url(font/MechanicalBd.otf)}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("@font-face {\n    font-family: 'Mechanical Bold';\n    src: url('font/MechanicalBd.otf');\n}\n",
				ffrule.getCssText());
		assertEquals("url('font/MechanicalBd.otf')", ffrule.getStyle().getPropertyValue("src"));
	}

	@Test
	public void testParseFontFaceRule2() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-face{font-family:'Mechanical Bold';src:url(font/MechanicalBd.otf); font-feature-settings: normal; font-variation-settings: normal; font-stretch: extra-expanded;}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(5, ffrule.getStyle().getLength());
		assertEquals(
				"@font-face {\n    font-family: 'Mechanical Bold';\n    src: url('font/MechanicalBd.otf');\n    font-feature-settings: normal;\n    font-variation-settings: normal;\n    font-stretch: extra-expanded;\n}\n",
				ffrule.getCssText());
		assertEquals(
				"@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf');font-feature-settings:normal;font-variation-settings:normal;font-stretch:extra-expanded}",
				ffrule.getMinifiedCssText());
		assertEquals("url('font/MechanicalBd.otf')", ffrule.getStyle().getPropertyValue("src"));
	}

	@Test
	public void testParseFontFaceRule3() throws IOException {
		InputSource source = new InputSource(new StringReader("@font-face {font-family: 'Glyphicons Halflings';\n"
				+ " src: url('fonts/glyphicons-halflings-regular.eot') format('embedded-opentype'), url('fonts/glyphicons-halflings-regular.woff') format('woff'), url('fonts/glyphicons-halflings-regular.ttf') format('truetype'), url('fonts/glyphicons-halflings-regular.svg#glyphicons-halflingsregular') format('svg');}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals(
				"@font-face {\n    font-family: 'Glyphicons Halflings';\n    src: url('fonts/glyphicons-halflings-regular.eot') format('embedded-opentype'), url('fonts/glyphicons-halflings-regular.woff') format('woff'), url('fonts/glyphicons-halflings-regular.ttf') format('truetype'), url('fonts/glyphicons-halflings-regular.svg#glyphicons-halflingsregular') format('svg');\n}\n",
				ffrule.getCssText());
		assertEquals(
				"url('fonts/glyphicons-halflings-regular.eot') format('embedded-opentype'), url('fonts/glyphicons-halflings-regular.woff') format('woff'), url('fonts/glyphicons-halflings-regular.ttf') format('truetype'), url('fonts/glyphicons-halflings-regular.svg#glyphicons-halflingsregular') format('svg')",
				ffrule.getStyle().getPropertyValue("src"));
	}

	@Test
	public void testParseFontFaceRule4() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("url(\"fonts/foo-file.svg#bar-icons\") format('svg')", ffrule.getStyle().getPropertyValue("src"));
		assertEquals("@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}",
				ffrule.getMinifiedCssText());
	}

	@Test
	public void testParseFontFaceRuleFormat() throws IOException {
		InputSource source = new InputSource(
				new StringReader("@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("url('font/FooSans.woff2') format('woff2')", ffrule.getStyle().getPropertyValue("src"));
		CSSValue src = ffrule.getStyle().getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_VALUE_LIST, src.getCssValueType());
		assertEquals(2, ((CSSValueList) src).getLength());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) ((CSSValueList) src).item(0)).getPrimitiveType());
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ((CSSValueList) src).item(1).getCssValueType());
		CSSPrimitiveValue format = (CSSPrimitiveValue) ((CSSValueList) src).item(1);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, format.getPrimitiveType());
		assertEquals("format('woff2')", format.getCssText());
		assertEquals(
				"@font-face {\n    font-family: 'FooSans';\n    src: url('font/FooSans.woff2') format('woff2');\n}\n",
				ffrule.getCssText());
		assertEquals("@font-face{font-family:'FooSans';src:url('font/FooSans.woff2') format('woff2')}",
				ffrule.getMinifiedCssText());
	}

	@Test
	public void testParseFontFaceRuleWrongChar() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"\ufeff@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}"));
		assertFalse(sheet.parseCSSStyleSheet(source));
		assertEquals(0, sheet.getCssRules().getLength());
		SheetErrorHandler eh = sheet.getErrorHandler();
		assertTrue(eh.hasSacErrors());
	}

	@Test
	public void testParseFontFaceRuleErrorRecovery() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@;@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}"));
		assertFalse(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("url('font/FooSans.woff2') format('woff2')", ffrule.getStyle().getPropertyValue("src"));
		CSSValue src = ffrule.getStyle().getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_VALUE_LIST, src.getCssValueType());
		assertEquals(2, ((CSSValueList) src).getLength());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) ((CSSValueList) src).item(0)).getPrimitiveType());
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ((CSSValueList) src).item(1).getCssValueType());
		CSSPrimitiveValue format = (CSSPrimitiveValue) ((CSSValueList) src).item(1);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, format.getPrimitiveType());
		assertEquals("format('woff2')", format.getCssText());
		assertEquals(
				"@font-face {\n    font-family: 'FooSans';\n    src: url('font/FooSans.woff2') format('woff2');\n}\n",
				ffrule.getCssText());
		assertEquals("@font-face{font-family:'FooSans';src:url('font/FooSans.woff2') format('woff2')}",
				ffrule.getMinifiedCssText());
		SheetErrorHandler eh = sheet.getErrorHandler();
		assertTrue(eh.hasSacErrors());
	}

	@Test
	public void testParseFontFaceRuleUnicodeRange() throws IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-face{font-family:Montserrat;font-style:normal;font-weight:700;src:local('Montserrat-Bold'),url(//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2) format(\"woff2\");unicode-range:u+00??,u+0131,u+0152-0153,u+02c6,u+02da,u+02dc,u+2000-206f,u+2074,u+20ac,u+2212,u+2215,u+e0ff,u+effd,u+f000}"));
		assertTrue(sheet.parseCSSStyleSheet(source));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(5, ffrule.getStyle().getLength());
		assertEquals(
				"local('Montserrat-Bold'), url('//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2') format(\"woff2\")",
				ffrule.getStyle().getPropertyValue("src"));
		CSSValue src = ffrule.getStyle().getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_VALUE_LIST, src.getCssValueType());
		assertEquals(2, ((CSSValueList) src).getLength());
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ((CSSValueList) src).item(0).getCssValueType());
		CSSPrimitiveValue local = (CSSPrimitiveValue) ((CSSValueList) src).item(0);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, local.getPrimitiveType());
		assertEquals("local('Montserrat-Bold')", local.getCssText());
		CSSValue val = ((CSSValueList) src).item(1);
		assertEquals(CSSValue.CSS_VALUE_LIST, val.getCssValueType());
		CSSValueList list = (CSSValueList) val;
		CSSPrimitiveValue uri = (CSSPrimitiveValue) list.item(0);
		assertEquals(CSSPrimitiveValue.CSS_URI, uri.getPrimitiveType());
		assertEquals("url('//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2')", uri.getCssText());
		CSSPrimitiveValue format = (CSSPrimitiveValue) list.item(1);
		assertEquals("format(\"woff2\")", format.getCssText());
		CSSValue range = ffrule.getStyle().getPropertyCSSValue("unicode-range");
		assertNotNull(range);
		assertEquals(CSSValue.CSS_VALUE_LIST, range.getCssValueType());
		assertEquals(14, ((CSSValueList) range).getLength());
		assertEquals(
				"@font-face {\n    font-family: Montserrat;\n    font-style: normal;\n    font-weight: 700;\n    src: local('Montserrat-Bold'), url('//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2') format(\"woff2\");\n    unicode-range: U+00??, U+131, U+152-153, U+2c6, U+2da, U+2dc, U+2000-206f, U+2074, U+20ac, U+2212, U+2215, U+e0ff, U+effd, U+f000;\n}\n",
				ffrule.getCssText());
		assertEquals(
				"@font-face{font-family:Montserrat;font-style:normal;font-weight:700;src:local('Montserrat-Bold'),url('//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2') format(\"woff2\");unicode-range:U+00??,U+131,U+152-153,U+2c6,U+2da,U+2dc,U+2000-206f,U+2074,U+20ac,U+2212,U+2215,U+e0ff,U+effd,U+f000}",
				ffrule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextString() {
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		assertEquals("@font-face{font-family:'FooSans';src:url('font/FooSans.woff2') format('woff2')}", rule.getMinifiedCssText());
		assertEquals(2, rule.getStyle().getLength());
	}

	@Test
	public void testEquals() {
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		FontFaceRule rule2 = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
		rule2.setCssText("@font-face{font-family:'FooSans';font-style: normal;src:url(font/FooSans.woff2) format('woff2')}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testSetCssTextStringWrongRule() {
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
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
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-face{font-family:'Mechanical Bold';src:url(font/MechanicalBd.otf)}");
		BaseCSSDeclarationRule clon = rule.clone(sheet);
		assertTrue(rule.getParentStyleSheet() == clon.getParentStyleSheet());
		assertTrue(rule.getParentRule() == clon.getParentRule());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertEquals(rule.getMinifiedCssText(), clon.getMinifiedCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet2() {
		sheet.setHref("http://www.example.com/");
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-face{font-family:'Mechanical Bold';src:url(font/MechanicalBd.otf)}");
		BaseCSSDeclarationRule clon = rule.clone(sheet);
		assertTrue(rule.getParentStyleSheet() == clon.getParentStyleSheet());
		assertTrue(rule.getParentRule() == clon.getParentRule());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertEquals(rule.getMinifiedCssText(), clon.getMinifiedCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet3() {
		sheet.setHref("http://www.example.com/");
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-face{font-family:'Mechanical Bold';src:url(http://www.example.org/font/MechanicalBd.otf)}");
		BaseCSSDeclarationRule clon = rule.clone(sheet);
		assertTrue(rule.getParentStyleSheet() == clon.getParentStyleSheet());
		assertTrue(rule.getParentRule() == clon.getParentRule());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertEquals(rule.getMinifiedCssText(), clon.getMinifiedCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

	@Test
	public void testCloneAbstractCSSStyleSheet4() {
		sheet.setHref("http://www.example.com/foo/");
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("@font-face{font-family:'Mechanical Bold';src:url(../font/MechanicalBd.otf)}");
		BaseCSSDeclarationRule clon = rule.clone(sheet);
		assertTrue(rule.getParentStyleSheet() == clon.getParentStyleSheet());
		assertTrue(rule.getParentRule() == clon.getParentRule());
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
		assertEquals(rule.getMinifiedCssText(), clon.getMinifiedCssText());
		assertTrue(rule.equals(clon));
		assertEquals(rule.hashCode(), clon.hashCode());
	}

}
