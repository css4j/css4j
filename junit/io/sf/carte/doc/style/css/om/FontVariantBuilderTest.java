/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FontVariantBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testFontVariantNoShorthand() {
		assertShorthandText("font-variant-caps:small-caps;", "font-variant-caps: small-caps;");
	}

	@Test
	public void testFontVariantNone() {
		assertShorthandText("font-variant:none;", "font-variant: none;");
	}

	@Test
	public void testFontVariantNormal() {
		assertShorthandText("font-variant:normal;", "font-variant: normal;");
	}

	@Test
	public void testFontVariant() {
		assertShorthandText("font-variant:small-caps;", "font-variant: small-caps");
		assertShorthandText("font-variant:common-ligatures small-caps proportional-nums ordinal stylistic(foo) ruby;",
				"font-variant: common-ligatures stylistic(foo) small-caps proportional-nums ordinal ruby");
		assertShorthandText(
				"font-variant:common-ligatures discretionary-ligatures small-caps proportional-nums diagonal-fractions ordinal stylistic(foo) jis83;",
				"font-variant: common-ligatures discretionary-ligatures stylistic(foo) small-caps proportional-nums diagonal-fractions ordinal jis83");
		assertShorthandText("font-variant:ruby;", "font-variant: ruby");
	}

	@Test
	public void testFontVariantPlusImportant() {
		assertShorthandText("font-variant:small-caps;font-variant-east-asian:ruby!important;",
				"font-variant: small-caps ; font-variant-east-asian: ruby !important;");
	}

	@Test
	public void testFontVariantImportant() {
		assertShorthandText("font-variant:small-caps!important;", "font-variant: small-caps ! important");
	}

	@Test
	public void testFontVariantInherit() {
		assertShorthandText("font-variant:inherit;", "font-variant: inherit;");
	}

	@Test
	public void testFontVariantInheritImportant() {
		assertShorthandText("font-variant:inherit!important;", "font-variant: inherit!important;");
	}

	@Test
	public void testFontVariantUnset() {
		assertShorthandText("font-variant:unset;", "font-variant: unset;");
	}

	@Test
	public void testFontVariantUnsetImportant() {
		assertShorthandText("font-variant:unset!important;", "font-variant: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
