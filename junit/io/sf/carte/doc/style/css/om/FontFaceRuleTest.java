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
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.property.ValueList;

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
		StringReader re = new StringReader("@font-face{font-family:'Mechanical Bold';src:url(font/MechanicalBd.otf)}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("@font-face {\n    font-family: 'Mechanical Bold';\n    src: url('font/MechanicalBd.otf');\n}\n",
				ffrule.getCssText());
		assertEquals("url('font/MechanicalBd.otf')", ffrule.getStyle().getPropertyValue("src"));
		// Visitor
		PropertyCountVisitor visitor = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitor);
		assertEquals(2, visitor.getCount());
		//
		visitor.reset();
		sheet.acceptDescriptorRuleVisitor(visitor);
		assertEquals(2, visitor.getCount());
	}

	@Test
	public void testParseFontFaceRule2() throws IOException {
		StringReader re = new StringReader(
				"@font-face{font-family:'Mechanical Bold';src:url(font/MechanicalBd.otf); font-feature-settings: normal; font-variation-settings: normal; font-stretch: extra-expanded;}");
		assertTrue(sheet.parseStyleSheet(re));
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
		StringReader re = new StringReader("@font-face {font-family: 'Glyphicons Halflings';\n"
				+ " src: url('fonts/glyphicons-halflings-regular.eot') format('embedded-opentype'), url('fonts/glyphicons-halflings-regular.woff') format('woff'), url('fonts/glyphicons-halflings-regular.ttf') format('truetype'), url('fonts/glyphicons-halflings-regular.svg#glyphicons-halflingsregular') format('svg');}");
		assertTrue(sheet.parseStyleSheet(re));
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
		StringReader re = new StringReader(
				"@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}");
		assertTrue(sheet.parseStyleSheet(re));
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
		StringReader re = new StringReader(
				"@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("url('font/FooSans.woff2') format('woff2')", ffrule.getStyle().getPropertyValue("src"));
		CSSValue src = ffrule.getStyle().getPropertyCSSValue("src");
		assertEquals(CssType.LIST, src.getCssValueType());
		assertEquals(2, ((ValueList) src).getLength());
		assertEquals(CSSValue.Type.URI, ((CSSTypedValue) ((ValueList) src).item(0)).getPrimitiveType());
		assertEquals(CssType.TYPED, ((ValueList) src).item(1).getCssValueType());
		CSSTypedValue format = (CSSTypedValue) ((ValueList) src).item(1);
		assertEquals(CSSValue.Type.FUNCTION, format.getPrimitiveType());
		assertEquals("format('woff2')", format.getCssText());
		assertEquals(
				"@font-face {\n    font-family: 'FooSans';\n    src: url('font/FooSans.woff2') format('woff2');\n}\n",
				ffrule.getCssText());
		assertEquals("@font-face{font-family:'FooSans';src:url('font/FooSans.woff2') format('woff2')}",
				ffrule.getMinifiedCssText());
	}

	@Test
	public void testParseFontFaceRuleWrongChar() throws IOException {
		StringReader re = new StringReader(
				"\ufeff@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		assertFalse(sheet.parseStyleSheet(re));
		assertEquals(0, sheet.getCssRules().getLength());
		SheetErrorHandler eh = sheet.getErrorHandler();
		assertTrue(eh.hasSacErrors());
	}

	@Test
	public void testParseFontFaceRuleErrorRecovery() throws IOException {
		StringReader re = new StringReader(
				"@;@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		assertFalse(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals("url('font/FooSans.woff2') format('woff2')", ffrule.getStyle().getPropertyValue("src"));
		CSSValue src = ffrule.getStyle().getPropertyCSSValue("src");
		assertEquals(CssType.LIST, src.getCssValueType());
		assertEquals(2, ((ValueList) src).getLength());
		assertEquals(CSSValue.Type.URI, ((CSSTypedValue) ((ValueList) src).item(0)).getPrimitiveType());
		assertEquals(CssType.TYPED, ((ValueList) src).item(1).getCssValueType());
		CSSTypedValue format = (CSSTypedValue) ((ValueList) src).item(1);
		assertEquals(CSSValue.Type.FUNCTION, format.getPrimitiveType());
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
		StringReader re = new StringReader(
				"@font-face{font-family:Montserrat;font-style:normal;font-weight:700;src:local('Montserrat-Bold'),url(//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2) format(\"woff2\");unicode-range:u+00??,u+0131,u+0152-0153,u+02c6,u+02da,u+02dc,u+2000-206f,u+2074,u+20ac,u+2212,u+2215,u+e0ff,u+effd,u+f000}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(5, ffrule.getStyle().getLength());
		assertEquals(
				"local('Montserrat-Bold'), url('//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2') format(\"woff2\")",
				ffrule.getStyle().getPropertyValue("src"));
		CSSValue src = ffrule.getStyle().getPropertyCSSValue("src");
		assertEquals(CssType.LIST, src.getCssValueType());
		assertEquals(2, ((ValueList) src).getLength());
		assertEquals(CssType.TYPED, ((ValueList) src).item(0).getCssValueType());
		CSSTypedValue local = (CSSTypedValue) ((ValueList) src).item(0);
		assertEquals(CSSValue.Type.FUNCTION, local.getPrimitiveType());
		assertEquals("local('Montserrat-Bold')", local.getCssText());
		CSSValue val = ((ValueList) src).item(1);
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
		CSSTypedValue uri = (CSSTypedValue) list.item(0);
		assertEquals(CSSValue.Type.URI, uri.getPrimitiveType());
		assertEquals("url('//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2')", uri.getCssText());
		CSSTypedValue format = (CSSTypedValue) list.item(1);
		assertEquals("format(\"woff2\")", format.getCssText());
		CSSValue range = ffrule.getStyle().getPropertyCSSValue("unicode-range");
		assertNotNull(range);
		assertEquals(CssType.LIST, range.getCssValueType());
		assertEquals(14, ((ValueList) range).getLength());
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
		assertEquals("@font-face{font-family:'FooSans';src:url('font/FooSans.woff2') format('woff2')}",
				rule.getMinifiedCssText());
		assertEquals(2, rule.getStyle().getLength());
	}

	@Test
	public void testSetCssTextStringComment() {
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("/* pre-rule */ @font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}");
		assertEquals("@font-face{font-family:'FooSans';src:url('font/FooSans.woff2') format('woff2')}",
				rule.getMinifiedCssText());
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
		rule2.setCssText(
				"@font-face{font-family:'FooSans';font-style: normal;src:url(font/FooSans.woff2) format('woff2')}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@font-face{font-family:Montserrat;font-style:normal;font-weight:700;src:local('Montserrat-Bold'),url(//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2) format(\"woff2\");unicode-range:u+00??,u+0131,u+0152-0153,u+02c6,u+02da,u+02dc,u+2000-206f,u+2074,u+20ac,u+2212,u+2215,u+e0ff,u+effd,u+f000}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule rule = (FontFaceRule) sheet.getCssRules().item(0);
		FontFaceRule rule2 = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText("@font-face{font-family:Montserrat;font-style:normal;font-weight:700;src:local('Montserrat-Bold'),url(//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2) format(\"woff2\");unicode-range:u+00??,u+0131,u+0152-0153,u+02c6,u+02da,u+02dc,u+2000-206f,u+2074,u+20ac,u+2212,u+2215,u+e0ff,u+effd,u+f000}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
		rule2.setCssText(
				"@font-face{font-family:Montserrat;font-style:normal;font-weight:700;src:local('Montserrat-Bold'),url(//fonts.gstatic.com/s/montserrat/v6/IQHow_FEY_Y.woff2) format(\"woff2\");unicode-range:u+00??,u+0131,u+0152-0153,u+02c6,u+02da,u+02dc,u+2000-206f,u+2074,u+20ac,u+2212,u+2215,u+e0ff,u+effd}");
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testEquals3() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@font-face{font-family:icons-ibm-v12;src:url(https://example.com/common/fonts/icons-ibm-v12.eot);src:url(https://example.com/common/fonts/icons-ibm-v12.woff) format(\"woff\"),url(https://example.com/common/fonts/icons-ibm-v12.ttf) format(\"truetype\"),url(https://example.com/common/fonts/icons-ibm-v12.svg#icons-ibm-v12) format(\"svg\");font-weight:400;font-style:normal}");
		assertTrue(sheet.parseStyleSheet(re));
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.FONT_FACE_RULE, sheet.getCssRules().item(0).getType());
		FontFaceRule rule = (FontFaceRule) sheet.getCssRules().item(0);
		FontFaceRule rule2 = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule2.setCssText(
				"@font-face{font-family:icons-ibm-v12;src:url(https://example.com/common/fonts/icons-ibm-v12.eot);src:url(https://example.com/common/fonts/icons-ibm-v12.woff) format(\"woff\"),url(https://example.com/common/fonts/icons-ibm-v12.ttf) format(\"truetype\"),url(https://example.com/common/fonts/icons-ibm-v12.svg#icons-ibm-v12) format(\"svg\");font-weight:400;font-style:normal}");
		assertTrue(rule.equals(rule2));
		assertEquals(rule.hashCode(), rule2.hashCode());
		rule2.setCssText(
				"@font-face{font-family:icons-ibm-v12;src:url(https://example.com/common/fonts/icons-ibm-v12.eot);src:url(https://example.com/common/fonts/icons-ibm-v12.woff) format(\"woff\"),url(https://example.com/common/fonts/icons-ibm-v12.ttf) format(\"truetype\");font-weight:400;font-style:normal}");
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
	public void testSetCssTextStringWrongRule2() {
		FontFaceRule rule = new FontFaceRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText("/* pre-rule */ @page {margin-top: 20%;}");
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
		rule.setCssText(
				"@font-face{font-family:'Mechanical Bold';src:url(http://www.example.org/font/MechanicalBd.otf)}");
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
