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

public class MarginInlineBuilderTest {

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
		assertShorthandText("padding-inline-end:0;", "padding-inline-end:0;");
	}

	@Test
	public void testMarginInline() {
		assertShorthandText("margin-inline:1em 2em;", "margin-inline: 1em 2em;");
		assertShorthandText("margin-inline:5% 3%;", "margin-inline: 5% 3%");
		assertShorthandText("margin-inline:auto;", "margin-inline: auto");
		assertShorthandText("margin-inline:1em;", "margin-inline: 1em;");
	}

	@Test
	public void testBuilderZero() {
		assertShorthandText("margin-inline:0;", "margin-inline: 0");
		assertShorthandText("margin-inline:1px 0;", "margin-inline: 1px 0");
		assertShorthandText("margin-inline:1em 0;", "margin-inline: 1em 0");
		assertShorthandText("margin-inline:0 1em;",
				"margin-inline-start: 0;margin-inline-end: 1em");
		assertShorthandText("margin-inline:1em 0;", "margin-inline-start:1em;margin-inline-end:0");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("margin-inline:var(--mi,1px 3px);", "margin-inline: var(--mi,1px 3px)");
	}

	@Test
	public void testBuilderAnchorSize() {
		assertShorthandText("margin-inline:anchor-size(block);",
				"margin-inline: anchor-size(block)");
	}

	@Test
	public void testBuilderAnchorSizeCalc() {
		assertShorthandText("margin-inline:calc(anchor-size(block) + 3em);",
				"margin-inline: calc(anchor-size(block) + 3em)");
	}

	@Test
	public void testBuilderMix() {
		assertShorthandText("margin-inline-end:inherit;margin-inline-start:auto;",
				"margin-inline: auto; margin-inline-end: inherit;");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("margin-inline:2% 5%!important;", "margin-inline: 2% 5% !important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("margin-inline:inherit;", "margin-inline: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("margin-inline:inherit!important;",
				"margin-inline: inherit!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("margin-inline:revert;", "margin-inline: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("margin-inline:revert!important;", "margin-inline: revert!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("margin-inline:0;", "margin-inline: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("margin-inline:0!important;", "margin-inline: unset!important;");
	}

	@Test
	public void testBuilderInitial() {
		assertShorthandText("margin-inline:0;", "margin-inline: initial");
	}

	@Test
	public void testBuilderInitialImportant() {
		assertShorthandText("margin-inline:0!important;", "margin-inline: initial!important");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
