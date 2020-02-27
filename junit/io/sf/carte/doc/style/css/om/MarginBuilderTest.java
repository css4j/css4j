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

public class MarginBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testMarginNoShorthand() {
		assertShorthandText("margin-top:2px;", "margin-top: 2px;");
	}

	@Test
	public void testMarginNoShorthand2() {
		assertShorthandText("margin-bottom:2px;margin-left:2;margin-right:2px;margin-top:2px;",
				"margin-top: 2px; margin-right: 2px; margin-bottom: 2px; margin-left: 2");
	}

	@Test
	public void testMarginZero() {
		assertShorthandText("margin:0;", "margin: 0;");
		assertShorthandText("margin:1px 0;", "margin:1px 0;");
		assertShorthandText("margin:0 1px;", "margin: 0 1px;");
		assertShorthandText("margin:1px 0 3px;", "margin:1px 0 3px;");
		assertShorthandText("margin:1px 2px 3px 0;", "margin:1px 2px 3px 0;");
		assertShorthandText("margin:0 2px 3px 4px;", "margin:0 2px 3px 4px;");
	}

	@Test
	public void testMargin() {
		assertShorthandText("margin:1px;", "margin:1px;");
		assertShorthandText("margin:1px 2px;", "margin:1px 2px;");
		assertShorthandText("margin:1px 2px 3px;", "margin:1px 2px 3px;");
		assertShorthandText("margin:1px 2px 3px 4px;", "margin:1px 2px 3px 4px;");
	}

	@Test
	public void testMargin2() {
		assertShorthandText("margin:1px 0;", "margin:1px 0;");
		assertShorthandText("margin:1px auto;", "margin:1px auto;");
		assertShorthandText("margin:1px auto auto;", "margin:1px auto auto;");
		assertShorthandText("margin:1px 0 auto;", "margin:1px 0 auto;");
		assertShorthandText("margin:1px 0 0;", "margin:1px 0 0;");
		assertShorthandText("margin:1px auto auto 0;", "margin:1px auto auto 0;");
	}

	@Test
	public void testMarginVar() {
		assertShorthandText("margin:var(--foo, 1px 2px);", "margin: var(--foo,1px 2px);");
		assertShorthandText("margin:var(--foo, 1px 2px)!important;", "margin: var(--foo,1px 2px) ! important;");
	}

	@Test
	public void testMarginVarNoShorthand() {
		assertShorthandText("margin-bottom:var(--foo,1px 2px);margin-left:auto;margin-right:auto;margin-top:0;",
				"margin:0 auto;margin-bottom:var(--foo,1px 2px);");
	}

	@Test
	public void testMarginImportant() {
		assertShorthandText("margin:1px!important;", "margin:1px!important;");
		assertShorthandText("margin:1px 2px!important;", "margin:1px 2px!important;");
		assertShorthandText("margin:1px 2px 3px!important;", "margin:1px 2px 3px!important;");
		assertShorthandText("margin:1px 2px 3px 4px!important;", "margin:1px 2px 3px 4px!important;");
	}

	@Test
	public void testMarginImportantMix() {
		assertShorthandText("margin:1px;margin-top:3px!important;", "margin:1px;margin-top:3px!important");
		assertShorthandText("margin:1px 2px;margin-top:3px!important;", "margin:1px 2px;margin-top:3px!important");
		assertShorthandText("margin:3px 2px;margin-top:5px!important;", "margin:1px 2px 3px;margin-top:5px!important");
		assertShorthandText("margin:0 2px 3px 4px;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important");
		assertShorthandText("margin:1px;margin-right:3px!important;", "margin:1px;margin-right:3px!important");
		assertShorthandText("margin:1px 2px;margin-right:3px!important;", "margin:1px 2px;margin-right:3px!important");
		assertShorthandText("margin:1px 2px 3px;margin-right:5px!important;",
				"margin:1px 2px 3px;margin-right:5px!important");
		assertShorthandText("margin:1px 4px 3px;margin-right:5px!important;",
				"margin:1px 2px 3px 4px;margin-right:5px!important");
		assertShorthandText("margin:1px;margin-bottom:3px!important;", "margin:1px;margin-bottom:3px!important");
		assertShorthandText("margin:1px 2px;margin-bottom:3px!important;",
				"margin:1px 2px;margin-bottom:3px!important");
		assertShorthandText("margin:1px 2px;margin-bottom:5px!important;",
				"margin:1px 2px 3px;margin-bottom:5px!important");
		assertShorthandText("margin:1px 2px 0 4px;margin-bottom:5px!important;",
				"margin:1px 2px 3px 4px;margin-bottom:5px!important");
		assertShorthandText("margin:1px;margin-left:3px!important;", "margin:1px;margin-left:3px!important");
		assertShorthandText("margin:1px 2px;margin-left:3px!important;", "margin:1px 2px;margin-left:3px!important");
		assertShorthandText("margin:1px 2px 3px;margin-left:5px!important;",
				"margin:1px 2px 3px;margin-left:5px!important");
		assertShorthandText("margin:1px 2px 3px;margin-left:5px!important;",
				"margin:1px 2px 3px 4px;margin-left:5px!important");
	}

	@Test
	public void testMarginImportantMix2() {
		assertShorthandText("margin:1px;margin-right:5px!important;margin-top:3px!important;",
				"margin:1px;margin-top:3px!important; margin-right:5px!important;");
		assertShorthandText("margin:1px 2px;margin-right:5px!important;margin-top:3px!important;",
				"margin:1px 2px;margin-top:3px!important;margin-right:5px!important;");
		assertShorthandText("margin:3px 2px;margin-right:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px;margin-top:5px!important;margin-right:6px!important;");
		assertShorthandText("margin:3px 4px;margin-right:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important;margin-right:6px!important;");
		assertShorthandText("margin:1px;margin-bottom:5px!important;margin-top:3px!important;",
				"margin:1px;margin-top:3px!important; margin-bottom:5px!important;");
		assertShorthandText("margin:2px;margin-bottom:5px!important;margin-top:3px!important;",
				"margin:1px 2px;margin-top:3px!important;margin-bottom:5px!important;");
		assertShorthandText("margin:2px;margin-bottom:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px;margin-top:5px!important;margin-bottom:6px!important;");
		assertShorthandText("margin:0 2px 0 4px;margin-bottom:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important;margin-bottom:6px!important;");
		assertShorthandText("margin:1px;margin-left:3px!important;margin-right:5px!important;",
				"margin:1px;margin-left:3px!important; margin-right:5px!important;");
		assertShorthandText("margin:1px;margin-left:3px!important;margin-right:5px!important;",
				"margin:1px 2px;margin-left:3px!important;margin-right:5px!important;");
		assertShorthandText("margin:1px 0 3px;margin-left:5px!important;margin-right:6px!important;",
				"margin:1px 2px 3px;margin-left:5px!important;margin-right:6px!important;");
		assertShorthandText("margin:1px 0 3px;margin-left:5px!important;margin-right:6px!important;",
				"margin:1px 2px 3px 4px;margin-left:5px!important;margin-right:6px!important;");
		assertShorthandText("margin:1px;margin-left:5px!important;margin-top:3px!important;",
				"margin:1px;margin-top:3px!important; margin-left:5px!important;");
		assertShorthandText("margin:1px 2px;margin-left:5px!important;margin-top:3px!important;",
				"margin:1px 2px;margin-top:3px!important;margin-left:5px!important;");
		assertShorthandText("margin:3px 2px;margin-left:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px;margin-top:5px!important;margin-left:6px!important;");
		assertShorthandText("margin:3px 2px;margin-left:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important;margin-left:6px!important;");
		assertShorthandText("margin:1px;margin-bottom:5px!important;margin-right:3px!important;",
				"margin:1px;margin-right:3px!important; margin-bottom:5px!important;");
		assertShorthandText("margin:1px 2px;margin-bottom:5px!important;margin-right:3px!important;",
				"margin:1px 2px;margin-right:3px!important;margin-bottom:5px!important;");
		assertShorthandText("margin:1px 2px;margin-bottom:6px!important;margin-right:5px!important;",
				"margin:1px 2px 3px;margin-right:5px!important;margin-bottom:6px!important;");
		assertShorthandText("margin:1px 4px;margin-bottom:6px!important;margin-right:5px!important;",
				"margin:1px 2px 3px 4px;margin-right:5px!important;margin-bottom:6px!important;");
	}

	@Test
	public void testMarginImportantMix3() {
		assertShorthandText("margin:1px;margin-left:7px!important;margin-right:5px!important;margin-top:3px!important;",
				"margin:1px;margin-top:3px!important; margin-right:5px!important;margin-left:7px!important;");
		assertShorthandText("margin:1px;margin-left:7px!important;margin-right:5px!important;margin-top:3px!important;",
				"margin:1px 2px;margin-top:3px!important;margin-right:5px!important;margin-left:7px!important;");
		assertShorthandText("margin:3px;margin-left:7px!important;margin-right:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px;margin-top:5px!important;margin-right:6px!important;margin-left:7px!important;");
		assertShorthandText("margin:3px;margin-left:7px!important;margin-right:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important;margin-right:6px!important;margin-left:7px!important;");
	}

	@Test
	public void testMarginImportantMix4() {
		assertShorthandText(
				"margin:1px;margin-bottom:7px!important;margin-right:5px!important;margin-top:3px!important;",
				"margin:1px;margin-top:3px!important; margin-right:5px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:2px;margin-bottom:7px!important;margin-right:5px!important;margin-top:3px!important;",
				"margin:1px 2px;margin-top:3px!important;margin-right:5px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:2px;margin-bottom:7px!important;margin-right:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px;margin-top:5px!important;margin-right:6px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:4px;margin-bottom:7px!important;margin-right:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important;margin-right:6px!important;margin-bottom:7px!important;");
	}

	@Test
	public void testMarginImportantMix5() {
		assertShorthandText(
				"margin:1px;margin-bottom:7px!important;margin-left:5px!important;margin-top:3px!important;",
				"margin:1px;margin-top:3px!important; margin-left:5px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:2px;margin-bottom:7px!important;margin-left:5px!important;margin-top:3px!important;",
				"margin:1px 2px;margin-top:3px!important;margin-left:5px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:2px;margin-bottom:7px!important;margin-left:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px;margin-top:5px!important;margin-left:6px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:2px;margin-bottom:7px!important;margin-left:6px!important;margin-top:5px!important;",
				"margin:1px 2px 3px 4px;margin-top:5px!important;margin-left:6px!important;margin-bottom:7px!important;");
	}

	@Test
	public void testMarginImportantMix6() {
		assertShorthandText(
				"margin:1px;margin-bottom:7px!important;margin-left:3px!important;margin-right:5px!important;",
				"margin:1px;margin-left:3px!important; margin-right:5px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:1px;margin-bottom:7px!important;margin-left:3px!important;margin-right:5px!important;",
				"margin:1px 2px;margin-left:3px!important;margin-right:5px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:1px;margin-bottom:7px!important;margin-left:5px!important;margin-right:6px!important;",
				"margin:1px 2px 3px;margin-left:5px!important;margin-right:6px!important;margin-bottom:7px!important;");
		assertShorthandText(
				"margin:1px;margin-bottom:7px!important;margin-left:5px!important;margin-right:6px!important;",
				"margin:1px 2px 3px 4px;margin-left:5px!important;margin-right:6px!important;margin-bottom:7px!important;");
	}

	@Test
	public void testMarginInherit() {
		assertShorthandText("margin:inherit;", "margin: inherit;");
		assertShorthandText("margin:inherit!important;", "margin: inherit ! important;");
	}

	@Test
	public void testMarginInitial() {
		assertShorthandText("margin:0;", "margin: initial;");
		assertShorthandText("margin:0!important;", "margin: 0 ! important;");
	}

	@Test
	public void testMarginUnset() {
		assertShorthandText("margin:0;", "margin: unset;");
		assertShorthandText("margin:0!important;", "margin: unset !important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
