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

public class PaddingInlineBuilderTest {

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
	public void testBuilderNoShorthand() {
		assertShorthandText("padding-inline-start:0;", "padding-inline-start:0;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("padding-inline:0;", "padding-inline: 0");
		assertShorthandText("padding-inline:1em;", "padding-inline: 1em;");
		assertShorthandText("padding-inline:1em 2em;", "padding-inline: 1em 2em;");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("padding-inline:var(--pi,1px 3px);", "padding-inline: var(--pi,1px 3px)");
	}

	@Test
	public void testBuilderMix() {
		assertShorthandText("padding-inline-end:inherit;padding-inline-start:1ex;",
				"padding-inline: 1ex; padding-inline-end: inherit;");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("padding-inline:2% 5%!important;", "padding-inline: 2% 5% !important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("padding-inline:inherit;", "padding-inline: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("padding-inline:inherit!important;",
				"padding-inline: inherit!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("padding-inline:revert;", "padding-inline: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("padding-inline:revert!important;", "padding-inline: revert!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("padding-inline:0;", "padding-inline: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("padding-inline:0!important;", "padding-inline: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
