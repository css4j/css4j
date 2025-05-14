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

public class ListStyleShorthandBuilderTest {

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
		assertShorthandText("list-style-type:disc;", "list-style-type: disc;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("list-style:disc;", "list-style: initial");
		assertShorthandText("list-style:square;", "list-style: square");
		assertShorthandText("list-style:disc;", "list-style: disc;");
		assertShorthandText("list-style:none;", "list-style: none;");
		assertShorthandText("list-style:none;",
				"list-style-image: none; list-style-position: outside; list-style-type: none;");
		assertShorthandText("list-style:inside square;",
				"list-style-image: none; list-style-position: inside; list-style-type: square;");
		assertShorthandText("list-style:url('foo.png');", "list-style: url('foo.png');");
		assertShorthandText("list-style:url('foo.png');", "list-style: url('foo.png') disc;");
		assertShorthandText("list-style:none url('foo.png');", "list-style: url('foo.png') none");
		assertShorthandText("list-style:inside none url('foo.png');",
				"list-style: url('foo.png') none inside");
		assertShorthandText("list-style:inside url('foo.png');",
				"list-style: url('foo.png') inside");
		assertShorthandText("list-style:inside square url('foo.png');",
				"list-style: url('foo.png') inside square");
		assertShorthandText("list-style:inside foo;", "list-style: foo inside;");
		assertShorthandText("list-style:inside \"foo\";", "list-style: \"foo\" inside;");
		assertShorthandText("list-style:inside thai;", "list-style: thai inside;");
		assertShorthandText("list-style:inside MyStyle;", "list-style: inside MyStyle;");
		assertShorthandText("list-style:inside symbols('*' '\u2020' '\u2021' '\u00a7');",
				"list-style: inside symbols('*' '\\2020' '\\2021' '\\A7');");
		assertShorthandText("list-style:radial-gradient(yellow,green);",
				"list-style: radial-gradient(yellow, green);");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("list-style:var(--foo,square);", "list-style: var(--foo,square)");
	}

	@Test
	public void testBuilderNoShorthandIEHack() {
		assertShorthandText(
				"list-style-image:none;list-style-position:inside;list-style-type:square \\9;",
				"list-style-image: none; list-style-position: inside; list-style-type: square \\9;");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("list-style:disc!important;", "list-style: initial ! important");
		assertShorthandText("list-style:disc!important;", "list-style: disc ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("list-style:inherit;", "list-style: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("list-style:inherit!important;", "list-style: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("list-style:unset;", "list-style: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("list-style:unset!important;", "list-style: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("list-style:revert;", "list-style: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("list-style:revert!important;", "list-style: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
