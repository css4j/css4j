/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FontBuilderTest {

	private static AbstractCSSStyleSheet sheet;

	BaseCSSStyleDeclaration emptyStyleDecl;

	@BeforeAll
	public static void setUpBeforeAll() {
		sheet = new DOMCSSStyleSheetFactory().createStyleSheet(null, null);
	}

	@BeforeEach
	public void setUp() {
		StyleRule styleRule = sheet.createStyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testFontNoShorthand() {
		assertShorthandText("font-size:18;", "font-size: 18;");
		assertShorthandText("font-kerning:none;font-size:18;font-size-adjust:.6;",
				"font-size: 18; font-kerning: none; font-size-adjust: 0.6;");
	}

	@Test
	public void testFont() {
		assertShorthandText("font:normal;font-size:smaller;", "font:normal;font-size:smaller");
		assertShorthandText("font:normal;", "font: normal; font-kerning: auto;");
		assertShorthandText("font:normal;font-kerning:none;", "font: normal; font-kerning: none;");
		assertShorthandText("font:normal;font-kerning:none;font-size-adjust:.6;",
				"font: normal; font-kerning: none; font-size-adjust: 0.6;");
		assertShorthandText("font:bold;font-size:80%;line-height:120%;",
				"font:bold;font-size:80%;line-height:120%;");
		assertShorthandText("font:bold;font-stretch:condensed;font-size:80%;line-height:120%;",
				"font:bold;font-size:80%;line-height:120%;font-stretch:condensed");
		assertShorthandText("font:bold;font-family:Arial;", "font:bold;font-family:Arial;");
		assertShorthandText("font:400 80%/120% Verdana;", "font: 400 80%/120% Verdana");
		assertShorthandText("font:400 80%/120% Arial;", "font: 400 80%/120% Arial");
		assertShorthandText("font:400 80%/120% \"Times New Roman\",Arial;",
				"font: 400 80%/120% \"Times New Roman\",Arial");
		assertShorthandText("font:italic small-caps 400 80%/120% \"Times New Roman\",Arial;",
				"font:italic small-caps 400 80%/120% \"Times New Roman\",Arial");
		assertShorthandText("font:24pt serif;", "font:24pt serif");
		assertShorthandText("font:16pt \"Times New Roman\",Arial;",
				"font: 16pt \"Times New Roman\", Arial");
		assertShorthandText("font:condensed 80% sans-serif;", "font: condensed 80% sans-serif");
		assertShorthandText("font:3em/1.25 \"Helvetica Neue\",Helvetica,sans-serif;",
				"font: 3em/1.25 \"Helvetica Neue\", Helvetica, sans-serif;");
		assertShorthandText("font:14px/1.4 'Helvetica Neue',HelveticaNeue,Helvetica,sans-serif;",
				"font: normal 14.0px / 1.4 Helvetica Neue , HelveticaNeue , Helvetica , sans-serif;");
		assertShorthandText("font:bold 14px/32px 'Roboto',Arial,sans-serif;",
				"font: bold 14px/32px 'Roboto', Arial, sans-serif");
		assertShorthandText("font:14px/1 FontAwesome;", "font: normal 14px / 1 FontAwesome");
	}

	@Test
	public void testFontInitialAndLineHeight() {
		assertShorthandText("font:normal;line-height:1;", "font: initial; line-height: 1;");
	}

	@Test
	public void testFontInitialAndLineHeightImportant() {
		assertShorthandText("font:normal;line-height:1!important;",
				"font: initial; line-height: 1!important;");
	}

	@Test
	public void testFontInitialAndFontStretchCss3() {
		assertShorthandText("font:normal;font-stretch:82%;", "font: initial; font-stretch:82%;");
	}

	@Test
	public void testFontAndFontStretchCss3Important() {
		assertShorthandText("font:normal;font-stretch:82%!important;",
				"font:normal;font-stretch:82%!important");
	}

	@Test
	public void testFontImportantAndFontStretchCss3Important() {
		assertShorthandText("font:normal!important;font-stretch:82%!important;",
				"font:normal!important;font-stretch:82%!important");
	}

	@Test
	public void testFontInitialAndFontKerning() {
		assertShorthandText("font:normal;font-kerning:normal;",
				"font: initial; font-kerning:normal;");
	}

	@Test
	public void testFontInitialAndOthers() {
		assertShorthandText(
				"font:normal;line-height:1;font-optical-sizing:none;font-feature-settings:\"zero\";font-variation-settings:\"WDTH\" 120;",
				"font: initial;line-height: 1;font-optical-sizing: none;font-feature-settings:\"zero\";font-variation-settings:\"WDTH\" 120;");
	}

	@Test
	public void testFontVariantCss3() {
		assertShorthandText("font:normal;font-variant:titling-caps;",
				"font:normal;font-variant-caps:titling-caps");
	}

	@Test
	public void testFontVariantCss3Important() {
		assertShorthandText("font:normal;font-variant-caps:unicase!important;",
				"font:normal;font-variant-caps:unicase!important");
	}

	@Test
	public void testFontPlusImportant() {
		assertShorthandText("font:bolder 120% serif;font-size:120%!important;",
				"font: bolder 16pt serif; font-size: 120% ! important;");
	}

	@Test
	public void testFontImportant() {
		assertShorthandText("font:normal!important;", "font: normal ! important");
		assertShorthandText("font:normal!important;font-kerning:none!important;",
				"font: normal ! important; font-kerning: none ! important;");
		assertShorthandText("font:400 80%/120% Serif!important;",
				"font: 400 80%/120% Serif!important");
		assertShorthandText("font:23pt serif!important;", "font: 23pt serif!important");
	}

	@Test
	public void testFontImportantMix() {
		assertShorthandText("font:normal;font-stretch:82%!important;",
				"font: normal;font-stretch:82%!important;");
	}

	@Test
	public void testFontVariantImportantMix() {
		assertShorthandText("font:normal;font-variant-numeric:ordinal!important;",
				"font: normal; font-variant-numeric: ordinal!important;");
	}

	@Test
	public void testFontVariantImportantMix2() {
		assertShorthandText("font:normal;font-variant:ordinal;font-size:16pt!important;",
				"font: normal; font-size: 16pt!important; font-variant-numeric: ordinal;");
	}

	@Test
	public void testFontVar() {
		assertShorthandText("font:var(--foo,12pt Arial);", "font: var(--foo,12pt Arial)");
	}

	@Test
	public void testFontInherit() {
		assertShorthandText("font:inherit;", "font: inherit;");
	}

	@Test
	public void testFontInheritImportant() {
		assertShorthandText("font:inherit!important;", "font: inherit!important;");
	}

	@Test
	public void testFontInheritAndFontKerning() {
		assertShorthandText("font:inherit;font-kerning:normal;",
				"font: inherit; font-kerning:normal;");
	}

	@Test
	public void testFontInheritAndFontKerningImportant() {
		assertShorthandText("font:inherit;font-kerning:normal!important;",
				"font: inherit; font-kerning:normal!important;");
	}

	@Test
	public void testFontInheritAndFontSize() {
		assertShorthandText(
				"font-family:inherit;font-feature-settings:inherit;font-kerning:inherit;font-optical-sizing:inherit;font-size:80%;font-size-adjust:inherit;font-stretch:inherit;font-style:inherit;font-variant-alternates:inherit;font-variant-caps:inherit;font-variant-east-asian:inherit;font-variant-ligatures:inherit;font-variant-numeric:inherit;font-variant-position:inherit;font-variation-settings:inherit;font-weight:inherit;line-height:inherit;",
				"font: inherit; font-size:80%;");
	}

	@Test
	public void testFontInheritAndFontSizeImportant() {
		assertShorthandText(
				"font-size:80%!important;font-family:inherit;font-feature-settings:inherit;font-kerning:inherit;font-optical-sizing:inherit;font-size-adjust:inherit;font-stretch:inherit;font-style:inherit;font-variant-alternates:inherit;font-variant-caps:inherit;font-variant-east-asian:inherit;font-variant-ligatures:inherit;font-variant-numeric:inherit;font-variant-position:inherit;font-variation-settings:inherit;font-weight:inherit;line-height:inherit;",
				"font: inherit; font-size:80%!important;");
	}

	@Test
	public void testFontAndFontSizeInherit() {
		assertShorthandText(
				"font-family:initial;font-feature-settings:normal;font-kerning:auto;font-optical-sizing:auto;font-size:inherit;font-size-adjust:none;font-stretch:normal;font-style:normal;font-variant-alternates:normal;font-variant-caps:normal;font-variant-east-asian:normal;font-variant-ligatures:normal;font-variant-numeric:normal;font-variant-position:normal;font-variation-settings:normal;font-weight:bold;line-height:normal;",
				"font: bold; font-size:inherit;");
	}

	@Test
	public void testFontAndFontSizeInheritImportant() {
		assertShorthandText(
				"font-size:inherit!important;font-family:initial;font-feature-settings:normal;font-kerning:auto;font-optical-sizing:auto;font-size-adjust:none;font-stretch:normal;font-style:normal;font-variant-alternates:normal;font-variant-caps:normal;font-variant-east-asian:normal;font-variant-ligatures:normal;font-variant-numeric:normal;font-variant-position:normal;font-variation-settings:normal;font-weight:bold;line-height:normal;",
				"font: bold; font-size:inherit!important;");
	}

	@Test
	public void testFontUnset() {
		assertShorthandText("font:unset;", "font: unset;");
	}

	@Test
	public void testFontUnsetImportant() {
		assertShorthandText("font:unset!important;", "font: unset!important;");
	}

	@Test
	public void testFontRevert() {
		assertShorthandText("font:revert;", "font: revert;");
	}

	@Test
	public void testFontRevertImportant() {
		assertShorthandText("font:revert!important;", "font: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
