/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GridShorthandBuilderTest {

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
	public void testGridNone() {
		assertShorthandText("grid:none;", "grid: none");
	}

	@Test
	public void testGridNoneAuto() {
		assertShorthandText("grid:none/auto;", "grid: none / auto");
		assertShorthandText("grid:auto/none;", "grid: auto / none");
	}

	@Test
	public void testGrid() {
		assertShorthandText("grid:\"a a a\" \"b b b\";", "grid: \"a a a\" \"b b b\"");
	}

	@Test
	public void testGrid2() {
		assertShorthandText("grid:\"a a a\" \"b b b\" max-content;", "grid: \"a a a\" \"b b b\" max-content");
	}

	@Test
	public void testGrid3() {
		assertShorthandText(
				"grid:[header-top] \"a   a   a\" [header-bottom] [main-top] \"b   b   b\" 1fr [main-bottom]/auto 1fr auto;",
				"grid: [header-top] \"a   a   a\" [header-bottom]  [main-top] \"b   b   b\" 1fr [main-bottom] / auto 1fr auto; ");
	}

	@Test
	public void testGrid4() {
		assertShorthandText("grid:[header-top] 1fr/minmax(2%,1fr);", "grid: [header-top] 1fr / minmax(2%, 1fr)");
	}

	@Test
	public void testGrid5() {
		assertShorthandText("grid:[header-top] repeat(2,1fr)/minmax(2%,1fr);",
				"grid: [header-top] repeat(2, 1fr) / minmax(2%, 1fr)");
	}

	@Test
	public void testGrid6() {
		assertShorthandText("grid:\"a   a   a\" [header-bottom] [main-top] \"b   b   b\" 1fr [main-bottom]/1fr 2fr;",
				"grid: [] \"a   a   a\"     [header-bottom] [main-top] \"b   b   b\" 1fr [main-bottom] / 1fr 2fr; ");
	}

	@Test
	public void testGrid6b() {
		assertShorthandText("grid:\"a   a   a\" [header-bottom] [main-top] \"b   b   b\" 1fr [main-bottom]/1fr 2fr;",
				"grid: \"a   a   a\"     [header-bottom] [main-top] \"b   b   b\" 1fr [main-bottom] / 1fr 2fr; ");
	}

	@Test
	public void testGrid7() {
		assertShorthandText("grid:auto 1fr/auto 1fr auto;", "grid: auto 1fr / auto 1fr auto; ");
	}

	@Test
	public void testGrid8() {
		assertShorthandText("grid:[header-top] repeat(2,1fr)/minmax(2%,1fr);",
				"grid: [header-top] repeat(2, 1fr) / minmax(2%, 1fr); ");
	}

	@Test
	public void testGrid9() {
		assertShorthandText("grid:auto-flow 1fr/100px;", "grid: auto-flow 1fr / 100px; ");
	}

	@Test
	public void testGrid10() {
		assertShorthandText("grid:none/auto-flow 1fr;", "grid: none / auto-flow 1fr; ");
	}

	@Test
	public void testGrid11() {
		assertShorthandText("grid:auto-flow 300px/repeat(3,[line1 line2 line3] 200px);",
				"grid: auto-flow 300px / repeat(3, [line1 line2 line3] 200px); ");
	}

	@Test
	public void testGrid12() {
		assertShorthandText("grid:auto-flow dense 40%/[line1] minmax(20em,max-content);",
				"grid: auto-flow dense 40% / [line1] minmax(20em, max-content); ");
	}

	@Test
	public void testGrid13() {
		assertShorthandText("grid:repeat(3,[line1 line2 line3] 200px)/auto-flow 300px;",
				"grid: repeat(3, [line1 line2 line3] 200px) / auto-flow 300px; ");
	}

	@Test
	public void testGrid14() {
		assertShorthandText("grid:[line1] minmax(20em,max-content)/auto-flow dense 40%;",
				"grid: [line1] minmax(20em, max-content) / auto-flow dense 40%; ");
	}

	@Test
	public void testGrid15() {
		assertShorthandText("grid:minmax(400px,min-content)/repeat(auto-fill,50px);",
				"grid: minmax(400px, min-content) / repeat(auto-fill, 50px); ");
	}

	@Test
	public void testGrid16() {
		assertShorthandText("grid:100px 1fr/50px 1fr;", "grid: 100px 1fr / 50px 1fr; ");
	}

	@Test
	public void testGrid17() {
		assertShorthandText("grid:auto 1fr/auto 1fr auto;", "grid: auto 1fr / auto 1fr auto; ");
	}

	@Test
	public void testGrid18() {
		assertShorthandText("grid:[linename] 100px/[columnname1] 30% [columnname2] 70%;",
				"grid: [linename] 100px / [columnname1] 30% [columnname2] 70%; ");
	}

	@Test
	public void testGrid19() {
		assertShorthandText("grid:fit-content(100px)/fit-content(40%);",
				"grid: fit-content(100px) / fit-content(40%); ");
	}

	@Test
	public void testGrid20() {
		assertShorthandText("grid:1fr repeat(2,[foo] minmax(2%,1fr))/fit-content(40%);",
				"grid: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ");
	}

	@Test
	public void testGridImportant() {
		assertShorthandText("grid:none!important;", "grid: none !important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("grid:inherit;", "grid: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("grid:inherit!important;", "grid: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("grid:unset;", "grid: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("grid:unset!important;", "grid: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
