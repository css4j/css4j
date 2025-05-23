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

public class GridPlacementShorthandBuilderTest {

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
		assertShorthandText("grid-row-start:2;", "grid-row-start: 2;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("grid-row:auto;", "grid-row: auto");
		assertShorthandText("grid-row:foo;", "grid-row: foo");
		assertShorthandText("grid-row:2 foo/bar;", "grid-row: 2 foo / bar;");
		assertShorthandText("grid-row:1/3;", "grid-row: 1 / 3;");
		assertShorthandText("grid-row:auto;", "grid-row-start: auto; grid-row-end: auto;");
		assertShorthandText("grid-row:span 2/3 foo;", "grid-row: span 2 / 3 foo");
		assertShorthandText("grid-column:auto;", "grid-column: auto");
		assertShorthandText("grid-column:foo;", "grid-column: foo");
		assertShorthandText("grid-column:2 foo/bar;", "grid-column: 2 foo / bar;");
		assertShorthandText("grid-column:1/3;", "grid-column: 1 / 3;");
		assertShorthandText("grid-column:auto;", "grid-column-start: auto; grid-column-end: auto;");
		assertShorthandText("grid-column:span 2/3 foo;", "grid-column: span 2 / 3 foo");
	}

	@Test
	public void testBuilderMix() {
		assertShorthandText("grid-column:auto;",
				"grid-column-start: auto; grid-column-end: initial;");
		assertShorthandText("grid-column:auto;",
				"grid-column-start: auto; grid-column-end: unset;");
		assertShorthandText("grid-column:auto;",
				"grid-column-start: initial; grid-column-end: auto;");
		assertShorthandText("grid-column:auto;",
				"grid-column-start: unset; grid-column-end: auto;");
		assertShorthandText("grid-column:2;", "grid-column-start: 2; grid-column-end: initial;");
		assertShorthandText("grid-column:2;", "grid-column-start: 2; grid-column-end: unset;");
		assertShorthandText("grid-column:auto/2;",
				"grid-column-start: initial; grid-column-end: 2;");
		assertShorthandText("grid-column:auto/2;", "grid-column-start: unset; grid-column-end: 2;");
		assertShorthandText("grid-column:2 foo;",
				"grid-column-start: 2 foo; grid-column-end: initial;");
		assertShorthandText("grid-column:2 foo;",
				"grid-column-start: 2 foo; grid-column-end: unset;");
		assertShorthandText("grid-column:auto/2 foo;",
				"grid-column-start: initial; grid-column-end: 2 foo;");
		assertShorthandText("grid-column:auto/2 foo;",
				"grid-column-start: unset; grid-column-end: 2 foo;");
		assertShorthandText("grid-column:span 2;",
				"grid-column-start: span 2; grid-column-end: initial;");
		assertShorthandText("grid-column:span 2;",
				"grid-column-start: span 2; grid-column-end: unset;");
		assertShorthandText("grid-column:auto/span 2;",
				"grid-column-start: initial; grid-column-end: span 2;");
		assertShorthandText("grid-column:auto/span 2;",
				"grid-column-start: unset; grid-column-end: span 2;");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("grid-row:var(--foo,span 2);", "grid-row: var(--foo,span 2);");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("grid-row:2 foo/bar!important;", "grid-row: 2 foo / bar ! important");
	}

	@Test
	public void testBuilderImportantMix() {
		assertShorthandText("grid-column-start:3;grid-row:2 foo/bar!important;",
				"grid-row: 2 foo / bar ! important; grid-column-start: 3;");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("grid-row:inherit;", "grid-row: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("grid-row:inherit!important;", "grid-row: inherit!important;");
	}

	@Test
	public void testBuilderInitial() {
		assertShorthandText("grid-row:auto;", "grid-row: initial;");
	}

	@Test
	public void testBuilderInitialImportant() {
		assertShorthandText("grid-row:auto!important;", "grid-row: initial!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("grid-row:unset;", "grid-row: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("grid-row:unset!important;", "grid-row: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("grid-row:revert;", "grid-row: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("grid-row:revert!important;", "grid-row: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
