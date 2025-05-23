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

public class GridAreaShorthandBuilderTest {

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
		assertShorthandText("grid-column-start:2;grid-row-start:2;",
				"grid-row-start: 2; grid-column-start: 2;");
		assertShorthandText("grid-column-start:3;grid-row:2/foo;",
				"grid-row-start: 2; grid-row-end: foo; grid-column-start: 3;");
		assertShorthandText("grid-row-start:3;grid-column:2/foo;",
				"grid-row-start: 3; grid-column-start: 2; grid-column-end: foo; ");
		assertShorthandText("grid-row:2/foo;grid-column-start:3!important;",
				"grid-row-start: 2; grid-row-end: foo; grid-column-start: 3 ! important;");
		assertShorthandText("grid-column:2/foo;grid-row-start:3!important;",
				"grid-row-start: 3 ! important; grid-column-start: 2; grid-column-end: foo; ");
		assertShorthandText("grid-column-start:3;grid-row:2/foo!important;",
				"grid-row-start: 2 !important; grid-row-end: foo ! important; grid-column-start: 3;");
		assertShorthandText("grid-row-start:3;grid-column:2/foo!important;",
				"grid-row-start: 3; grid-column-start: 2 ! important; grid-column-end: foo ! important; ");
		assertShorthandText("grid-row:3/span 2;grid-column:2/foo!important;",
				"grid-row: 3 / span 2; grid-column-start: 2 ! important; grid-column-end: foo ! important; ");
		assertShorthandText("grid-column:2/foo;grid-row:3/span 2!important;",
				"grid-row: 3 / span 2 ! important; grid-column-start: 2; grid-column-end: foo; ");
		assertShorthandText(
				"grid-column-start:2!important;grid-row-start:2!important;grid-column-end:span 1;grid-row-end:foo;",
				"grid-row-start: 2 ! important; grid-column-start: 2 ! important; grid-row-end: foo; grid-column-end: span 1; ");
	}

	@Test
	public void testGridArea() {
		assertShorthandText("grid-area:auto;", "grid-area: initial");
		assertShorthandText("grid-area:auto;", "grid-area: auto");
		assertShorthandText("grid-area:foo;", "grid-area: foo");
		assertShorthandText("grid-area:2 foo/bar;", "grid-area: 2 foo / bar;");
		assertShorthandText("grid-area:1/3;", "grid-area: 1 / 3;");
		assertShorthandText("grid-area:auto;",
				"grid-row-start: auto; grid-row-end: auto; grid-column-start: auto; grid-column-end: auto;");
		assertShorthandText("grid-area:span 2/3 foo;", "grid-area: span 2 / 3 foo");
		assertShorthandText("grid-area:span 2/3 foo/bar;", "grid-area: span 2 / 3 foo / bar");
		assertShorthandText("grid-area:1/span 2/3 foo/bar;", "grid-area: 1 / span 2 / 3 foo / bar");
		assertShorthandText("grid-area:7/1/auto/3;", "grid-column:1/3;grid-row:7");
	}

	@Test
	public void testGridAreaImportant() {
		assertShorthandText("grid-area:auto!important;", "grid-area: initial!important");
		assertShorthandText("grid-area:auto!important;", "grid-area: auto !important");
		assertShorthandText("grid-area:foo!important;", "grid-area: foo !important");
		assertShorthandText("grid-area:2 foo/bar!important;", "grid-area: 2 foo / bar!important;");
		assertShorthandText("grid-area:1/3!important;", "grid-area: 1 / 3 !important;");
		assertShorthandText("grid-area:auto!important;",
				"grid-row-start: auto!important; grid-row-end: auto!important; grid-column-start: auto !important; grid-column-end: auto !important;");
		assertShorthandText("grid-area:span 2/3 foo!important;",
				"grid-area: span 2 / 3 foo!important");
		assertShorthandText("grid-area:span 2/3 foo/bar!important;",
				"grid-area: span 2 / 3 foo / bar!important");
		assertShorthandText("grid-area:1/span 2/3 foo/bar!important;",
				"grid-area: 1 / span 2 / 3 foo / bar !important");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("grid-area:var(--foo,span 2/3);", "grid-area: var(--foo,span 2/3);");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("grid-area:2 foo/bar!important;", "grid-area: 2 foo / bar ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("grid-area:inherit;", "grid-area: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("grid-area:inherit!important;", "grid-area: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("grid-area:unset;", "grid-area: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("grid-area:unset!important;", "grid-area: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("grid-area:revert;", "grid-area: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("grid-area:revert!important;", "grid-area: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
