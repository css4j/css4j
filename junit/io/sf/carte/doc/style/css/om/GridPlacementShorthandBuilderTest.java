/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GridPlacementShorthandBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
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
	public void testBuilderImportant() {
		assertShorthandText("grid-row:2 foo/bar!important;", "grid-row: 2 foo / bar ! important");
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
	public void testBuilderUnset() {
		assertShorthandText("grid-row:unset;", "grid-row: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("grid-row:unset!important;", "grid-row: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
