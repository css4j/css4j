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

public class PaddingBuilderTest {

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
	public void testPaddingNoShorthand() {
		assertShorthandText("padding-top:2px;", "padding-top: 2px;");
	}

	@Test
	public void testPaddingNoShorthand2() {
		assertShorthandText("padding-bottom:2px;padding-left:2;padding-right:2px;padding-top:2px;",
				"padding-top: 2px; padding-right: 2px; padding-bottom: 2px; padding-left: 2");
	}

	@Test
	public void testPaddingNoShorthandIEHack() {
		assertShorthandText(
				"padding-bottom:\\35 px\\9;padding-left:2px;padding-right:2px;padding-top:2px;",
				"padding: 2px; padding-bottom: \\35 px\\9;");
		assertShorthandText(
				"padding-bottom:20px iehack;padding-left:2px;padding-right:2px;padding-top:2px;",
				"padding: 2px; padding-bottom: 20px iehack;");
	}

	@Test
	public void testPadding() {
		assertShorthandText("padding:0;", "padding:0;");
		assertShorthandText("padding:1px;", "padding:1px;");
		assertShorthandText("padding:1px 2px;", "padding:1px 2px;");
		assertShorthandText("padding:1px 2px 3px;", "padding:1px 2px 3px;");
		assertShorthandText("padding:1px 2px 3px 4px;", "padding:1px 2px 3px 4px;");
	}

	@Test
	public void testPaddingVar() {
		assertShorthandText("padding:var(--foo,1px);", "padding:var(--foo,1px);");
	}

	@Test
	public void testPaddingVarNoShorthand() {
		assertShorthandText(
				"padding-bottom:0;padding-left:var(--foo);padding-right:.75rem;padding-top:0;",
				"padding:0 0.75rem;padding-left:var(--foo)");
	}

	@Test
	public void testPaddingImportant() {
		assertShorthandText("padding:0!important;", "padding:0!important;");
		assertShorthandText("padding:1px!important;", "padding:1px!important;");
		assertShorthandText("padding:1px 2px!important;", "padding:1px 2px!important;");
		assertShorthandText("padding:1px 2px 3px!important;", "padding:1px 2px 3px!important;");
		assertShorthandText("padding:1px 2px 3px 4px!important;",
				"padding:1px 2px 3px 4px!important;");
	}

	@Test
	public void testPaddingImportantMix() {
		assertShorthandText("padding:1px;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important");
		assertShorthandText("padding:1px 2px;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important");
		assertShorthandText("padding:3px 2px;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important");
		assertShorthandText("padding:0 2px 3px 4px;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important");
		assertShorthandText("padding:1px;padding-right:3px!important;",
				"padding:1px;padding-right:3px!important");
		assertShorthandText("padding:1px 2px;padding-right:3px!important;",
				"padding:1px 2px;padding-right:3px!important");
		assertShorthandText("padding:1px 2px 3px;padding-right:5px!important;",
				"padding:1px 2px 3px;padding-right:5px!important");
		assertShorthandText("padding:1px 4px 3px;padding-right:5px!important;",
				"padding:1px 2px 3px 4px;padding-right:5px!important");
		assertShorthandText("padding:1px;padding-bottom:3px!important;",
				"padding:1px;padding-bottom:3px!important");
		assertShorthandText("padding:1px 2px;padding-bottom:3px!important;",
				"padding:1px 2px;padding-bottom:3px!important");
		assertShorthandText("padding:1px 2px;padding-bottom:5px!important;",
				"padding:1px 2px 3px;padding-bottom:5px!important");
		assertShorthandText("padding:1px 2px 0 4px;padding-bottom:5px!important;",
				"padding:1px 2px 3px 4px;padding-bottom:5px!important");
		assertShorthandText("padding:1px;padding-left:3px!important;",
				"padding:1px;padding-left:3px!important");
		assertShorthandText("padding:1px 2px;padding-left:3px!important;",
				"padding:1px 2px;padding-left:3px!important");
		assertShorthandText("padding:1px 2px 3px;padding-left:5px!important;",
				"padding:1px 2px 3px;padding-left:5px!important");
		assertShorthandText("padding:1px 2px 3px;padding-left:5px!important;",
				"padding:1px 2px 3px 4px;padding-left:5px!important");
	}

	@Test
	public void testPaddingImportantMix2() {
		assertShorthandText("padding:1px;padding-right:5px!important;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important; padding-right:5px!important;");
		assertShorthandText(
				"padding:1px 2px;padding-right:5px!important;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important;padding-right:5px!important;");
		assertShorthandText(
				"padding:3px 2px;padding-right:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important;padding-right:6px!important;");
		assertShorthandText(
				"padding:3px 4px;padding-right:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important;padding-right:6px!important;");
		assertShorthandText("padding:1px;padding-bottom:5px!important;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important; padding-bottom:5px!important;");
		assertShorthandText("padding:2px;padding-bottom:5px!important;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important;padding-bottom:5px!important;");
		assertShorthandText("padding:2px;padding-bottom:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important;padding-bottom:6px!important;");
		assertShorthandText(
				"padding:0 2px 0 4px;padding-bottom:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important;padding-bottom:6px!important;");
		assertShorthandText("padding:1px;padding-left:3px!important;padding-right:5px!important;",
				"padding:1px;padding-left:3px!important; padding-right:5px!important;");
		assertShorthandText("padding:1px;padding-left:3px!important;padding-right:5px!important;",
				"padding:1px 2px;padding-left:3px!important;padding-right:5px!important;");
		assertShorthandText(
				"padding:1px 0 3px;padding-left:5px!important;padding-right:6px!important;",
				"padding:1px 2px 3px;padding-left:5px!important;padding-right:6px!important;");
		assertShorthandText(
				"padding:1px 0 3px;padding-left:5px!important;padding-right:6px!important;",
				"padding:1px 2px 3px 4px;padding-left:5px!important;padding-right:6px!important;");
		assertShorthandText("padding:1px;padding-left:5px!important;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important; padding-left:5px!important;");
		assertShorthandText("padding:1px 2px;padding-left:5px!important;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important;padding-left:5px!important;");
		assertShorthandText("padding:3px 2px;padding-left:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important;padding-left:6px!important;");
		assertShorthandText("padding:3px 2px;padding-left:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important;padding-left:6px!important;");
		assertShorthandText("padding:1px;padding-bottom:5px!important;padding-right:3px!important;",
				"padding:1px;padding-right:3px!important; padding-bottom:5px!important;");
		assertShorthandText(
				"padding:1px 2px;padding-bottom:5px!important;padding-right:3px!important;",
				"padding:1px 2px;padding-right:3px!important;padding-bottom:5px!important;");
		assertShorthandText(
				"padding:1px 2px;padding-bottom:6px!important;padding-right:5px!important;",
				"padding:1px 2px 3px;padding-right:5px!important;padding-bottom:6px!important;");
		assertShorthandText(
				"padding:1px 4px;padding-bottom:6px!important;padding-right:5px!important;",
				"padding:1px 2px 3px 4px;padding-right:5px!important;padding-bottom:6px!important;");
	}

	@Test
	public void testPaddingImportantMix3() {
		assertShorthandText(
				"padding:1px;padding-left:7px!important;padding-right:5px!important;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important; padding-right:5px!important;padding-left:7px!important;");
		assertShorthandText(
				"padding:1px;padding-left:7px!important;padding-right:5px!important;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important;padding-right:5px!important;padding-left:7px!important;");
		assertShorthandText(
				"padding:3px;padding-left:7px!important;padding-right:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important;padding-right:6px!important;padding-left:7px!important;");
		assertShorthandText(
				"padding:3px;padding-left:7px!important;padding-right:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important;padding-right:6px!important;padding-left:7px!important;");
	}

	@Test
	public void testPaddingImportantMix4() {
		assertShorthandText(
				"padding:1px;padding-bottom:7px!important;padding-right:5px!important;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important; padding-right:5px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:2px;padding-bottom:7px!important;padding-right:5px!important;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important;padding-right:5px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:2px;padding-bottom:7px!important;padding-right:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important;padding-right:6px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:4px;padding-bottom:7px!important;padding-right:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important;padding-right:6px!important;padding-bottom:7px!important;");
	}

	@Test
	public void testPaddingImportantMix5() {
		assertShorthandText(
				"padding:1px;padding-bottom:7px!important;padding-left:5px!important;padding-top:3px!important;",
				"padding:1px;padding-top:3px!important; padding-left:5px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:2px;padding-bottom:7px!important;padding-left:5px!important;padding-top:3px!important;",
				"padding:1px 2px;padding-top:3px!important;padding-left:5px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:2px;padding-bottom:7px!important;padding-left:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px;padding-top:5px!important;padding-left:6px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:2px;padding-bottom:7px!important;padding-left:6px!important;padding-top:5px!important;",
				"padding:1px 2px 3px 4px;padding-top:5px!important;padding-left:6px!important;padding-bottom:7px!important;");
	}

	@Test
	public void testPaddingImportantMix6() {
		assertShorthandText(
				"padding:1px;padding-bottom:7px!important;padding-left:3px!important;padding-right:5px!important;",
				"padding:1px;padding-left:3px!important; padding-right:5px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:1px;padding-bottom:7px!important;padding-left:3px!important;padding-right:5px!important;",
				"padding:1px 2px;padding-left:3px!important;padding-right:5px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:1px;padding-bottom:7px!important;padding-left:5px!important;padding-right:6px!important;",
				"padding:1px 2px 3px;padding-left:5px!important;padding-right:6px!important;padding-bottom:7px!important;");
		assertShorthandText(
				"padding:1px;padding-bottom:7px!important;padding-left:5px!important;padding-right:6px!important;",
				"padding:1px 2px 3px 4px;padding-left:5px!important;padding-right:6px!important;padding-bottom:7px!important;");
	}

	@Test
	public void testPaddingInherit() {
		assertShorthandText("padding:inherit;", "padding: inherit;");
		assertShorthandText("padding:inherit!important;", "padding: inherit ! important;");
	}

	@Test
	public void testPaddingInitial() {
		assertShorthandText("padding:0;", "padding: initial;");
		assertShorthandText("padding:0!important;", "padding: initial !important;");
	}

	@Test
	public void testPaddingUnset() {
		assertShorthandText("padding:0;", "padding: unset;");
		assertShorthandText("padding:0!important;", "padding: unset !important;");
	}

	@Test
	public void testPaddingInheritMix() {
		assertShorthandText("padding:inherit;padding-left:0;padding-right:0;",
				"padding-top: inherit; padding-right: 0; padding-bottom: inherit; padding-left: 0;");
		assertShorthandText("padding:inherit;padding-left:0!important;padding-right:0!important;",
				"padding-top: inherit; padding-right: 0!important; padding-bottom: inherit; padding-left: 0!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
