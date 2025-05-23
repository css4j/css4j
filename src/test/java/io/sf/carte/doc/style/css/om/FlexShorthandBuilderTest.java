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

public class FlexShorthandBuilderTest {

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
		assertShorthandText("flex-basis:2%;", "flex-basis: 2%;");
	}

	@Test
	public void testBuilderMixNoShorthand() {
		assertShorthandText("flex-basis:inherit;flex-grow:0;flex-shrink:0;",
				"flex-grow:0;flex-shrink:0;flex-basis:inherit;");
		assertShorthandText("flex-basis:revert;flex-grow:0;flex-shrink:0;",
				"flex-grow:0;flex-shrink:0;flex-basis:revert;");
	}

	@Test
	public void testBuilderFlexFlow() {
		assertShorthandText("flex-flow:wrap-reverse;",
				"flex-direction: row; flex-wrap: wrap-reverse;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("flex:auto;", "flex: auto; ");
		assertShorthandText("flex:0;", "flex: 0; ");
		assertShorthandText("flex:2;", "flex: 2; ");
		assertShorthandText("flex:2 2;", "flex: 2 2; ");
		assertShorthandText("flex:2 2 3%;", "flex: 2 2 3%; ");
		assertShorthandText("flex:2 2;", "flex: 2 2 auto; ");
		assertShorthandText("flex:3%;", "flex: 3%; ");
		assertShorthandText("flex:content;", "flex: content; ");
		assertShorthandText("flex:0 0 50%;", "flex: 0 0 50%; ");
		assertShorthandText("flex:0 100 max-content;", "flex:0 100 max-content;");
		assertShorthandText("flex:0 100 fit-content(50%);", "flex:0 100 fit-content(50%);");
	}

	@Test
	public void testBuilderImportantMix() {
		assertShorthandText("flex:50%;flex-shrink:0!important;",
				"flex: 0 0 50%; flex-shrink:0!important; ");
		assertShorthandText("flex:1 50%;flex-shrink:0!important;",
				"flex: 1 0 50%; flex-shrink:0!important; ");
		assertShorthandText("flex:0 0 50%;flex-grow:0!important;",
				"flex: 0 0 50%; flex-grow:0!important; ");
		assertShorthandText("flex:2 2;flex-basis:3%!important;",
				"flex: 2 2 3%; flex-basis:3%!important;");
	}

	@Test
	public void testBuilderMulti() {
		assertShorthandText("flex:1 0px;", "flex-basis:0;flex-grow:1;flex-shrink:1");
		assertShorthandText("flex:1 0px;", "flex-basis:0px;flex-grow:1;flex-shrink:1");
	}

	@Test
	public void testFlexCalc() {
		assertShorthandText("flex:0 0 calc(100% - 60px/3);",
				"flex: 0 0 calc(100.0% - 60.0px / 3); ");
		assertShorthandText("flex:0 0 calc(100%/3);", "flex: 0 0 calc(100.0%/ 3); ");
	}

	@Test
	public void testBuilderZeroBasis() {
		assertShorthandText("flex:2 2 0px;", "flex: 2 2 0pt; ");
		assertShorthandText("flex:2 2 0px;", "flex: 2 2 0; ");
		assertShorthandText("flex:0px;", "flex:0pt; ");
	}

	@Test
	public void testBuilderBadBasis() {
		assertShorthandText("flex-basis:5;flex-grow:2;flex-shrink:2;",
				"flex: 2 2; flex-basis: 5; ");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("flex:auto!important;", "flex: auto!important; ");
		assertShorthandText("flex:2!important;", "flex: 2!important; ");
		assertShorthandText("flex:2 2!important;", "flex: 2 2!important; ");
		assertShorthandText("flex:2 2 3%!important;", "flex: 2 2 3%!important; ");
		assertShorthandText("flex:2 2!important;", "flex: 2 2 auto!important; ");
		assertShorthandText("flex:3%!important;", "flex: 3%!important; ");
		assertShorthandText("flex:content!important;", "flex: content!important; ");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("flex:var(--foo,1%);", "flex: var(--foo,1%); ");
	}

	@Test
	public void testBuilderInitial() {
		assertShorthandText("flex:0;", "flex: initial; ");
	}

	@Test
	public void testBuilderInitialImportant() {
		assertShorthandText("flex:0!important;", "flex: initial!important; ");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("flex:inherit;", "flex: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("flex:inherit!important;", "flex: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("flex:0;", "flex: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("flex:0!important;", "flex: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("flex:revert;", "flex: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("flex:revert!important;", "flex: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
