/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BorderRadiusBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBorderRadiusNoShorthand() {
		assertShorthandText("border-top-right-radius:2px;", "border-top-right-radius: 2px;");
	}

	@Test
	public void testBorderRadiusZero() {
		assertShorthandText("border-radius:0;", "border-radius: 0;");
		assertShorthandText("border-radius:0!important;", "border-radius: 0!important;");
		assertShorthandText("border-radius:1px 0;", "border-radius:1px 0;");
		assertShorthandText("border-radius:0 1px;", "border-radius: 0 1px;");
		assertShorthandText("border-radius:1px 0 3px;", "border-radius:1px 0 3px;");
		assertShorthandText("border-radius:1px 2px 3px 0;", "border-radius:1px 2px 3px 0;");
		assertShorthandText("border-radius:0 2px 3px 4px;", "border-radius:0 2px 3px 4px;");
	}

	@Test
	public void testBorderRadius() {
		assertShorthandText("border-radius:1px;", "border-radius:1px;");
		assertShorthandText("border-radius:1px 2px;", "border-radius:1px 2px;");
		assertShorthandText("border-radius:1px 2px 3px;", "border-radius:1px 2px 3px;");
		assertShorthandText("border-radius:1px 2px 3px 4px;", "border-radius:1px 2px 3px 4px;");
	}

	@Test
	public void testBorderRadiusSlash() {
		assertShorthandText("border-radius:1px/2px;", "border-radius:1px/2px;");
		assertShorthandText("border-radius:1px 2px/3px;", "border-radius:1px 2px/3px;");
		assertShorthandText("border-radius:1px 2px 3px/5px 6px;", "border-radius:1px 2px 3px/5px 6px;");
		assertShorthandText("border-radius:1px 2px 3px 4px/5px 6px 7px 8px;",
				"border-radius:1px 2px 3px 4px / 5px 6px 7px 8px;");
	}

	@Test
	public void testBorderRadiusVar() {
		assertShorthandText("border-radius:var(--foo,1px);", "border-radius:var(--foo,1px);");
	}

	@Test
	public void testBorderRadiusMix() {
		assertShorthandText(
				"border-radius:1px 5px 7px 3px;",
				"border-top-left-radius:1px;border-bottom-left-radius:3px; border-top-right-radius:5px;border-bottom-right-radius:7px;");
		assertShorthandText(
				"border-radius:0 5px 7px 3px;",
				"border-top-left-radius:unset;border-bottom-left-radius:3px; border-top-right-radius:5px;border-bottom-right-radius:7px;");
		assertShorthandText(
				"border-radius:0 0 7px 3px;",
				"border-top-left-radius:unset;border-bottom-left-radius:3px; border-top-right-radius:initial;border-bottom-right-radius:7px;");
	}

	@Test
	public void testBorderRadiusImportant() {
		assertShorthandText("border-radius:1px!important;", "border-radius:1px!important;");
		assertShorthandText("border-radius:1px 2px!important;", "border-radius:1px 2px!important;");
		assertShorthandText("border-radius:1px 2px 3px!important;", "border-radius:1px 2px 3px!important;");
		assertShorthandText("border-radius:1px 2px 3px 4px!important;", "border-radius:1px 2px 3px 4px!important;");
	}

	@Test
	public void testBorderRadiusImportantMix() {
		assertShorthandText("border-radius:1px;border-bottom-right-radius:3px!important;",
				"border-radius:1px;border-bottom-right-radius:3px!important");
		assertShorthandText("border-radius:1px 2px;border-bottom-right-radius:3px!important;",
				"border-radius:1px 2px;border-bottom-right-radius:3px!important");
		assertShorthandText("border-radius:1px 2px;border-bottom-right-radius:5px!important;",
				"border-radius:1px 2px 3px;border-bottom-right-radius:5px!important");
		assertShorthandText("border-radius:1px 2px 0 4px;border-bottom-right-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-bottom-right-radius:5px!important");
		assertShorthandText("border-radius:0 2px 3px 4px;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important");
		assertShorthandText("border-radius:1px;border-top-left-radius:3px!important;",
				"border-radius:1px;border-top-left-radius:3px!important");
		assertShorthandText("border-radius:1px 2px;border-top-left-radius:3px!important;",
				"border-radius:1px 2px;border-top-left-radius:3px!important");
		assertShorthandText("border-radius:1px 2px 3px;border-bottom-left-radius:5px!important;",
				"border-radius:1px 2px 3px;border-bottom-left-radius:5px!important");
		assertShorthandText("border-radius:1px 4px 3px;border-top-right-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-top-right-radius:5px!important");
		assertShorthandText("border-radius:1px;border-bottom-right-radius:3px!important;",
				"border-radius:1px;border-bottom-right-radius:3px!important");
		assertShorthandText("border-radius:1px 2px;border-bottom-right-radius:3px!important;",
				"border-radius:1px 2px;border-bottom-right-radius:3px!important");
		assertShorthandText("border-radius:1px 2px;border-bottom-right-radius:5px!important;",
				"border-radius:1px 2px 3px;border-bottom-right-radius:5px!important");
		assertShorthandText("border-radius:1px 2px 0 4px;border-bottom-right-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-bottom-right-radius:5px!important");
		assertShorthandText("border-radius:1px;border-bottom-left-radius:3px!important;",
				"border-radius:1px;border-bottom-left-radius:3px!important");
		assertShorthandText("border-radius:1px 2px;border-bottom-left-radius:3px!important;",
				"border-radius:1px 2px;border-bottom-left-radius:3px!important");
		assertShorthandText("border-radius:1px 2px 3px;border-bottom-left-radius:5px!important;",
				"border-radius:1px 2px 3px;border-bottom-left-radius:5px!important");
		assertShorthandText("border-radius:1px 2px 3px;border-bottom-left-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-bottom-left-radius:5px!important");
	}

	@Test
	public void testBorderRadiusImportantMix2() {
		assertShorthandText(
				"border-radius:1px;border-top-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px;border-top-left-radius:3px!important; border-top-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:1px 2px;border-top-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px;border-top-left-radius:3px!important;border-top-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:3px 2px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:3px 4px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-right-radius:5px!important;border-top-left-radius:3px!important;",
				"border-radius:1px;border-top-left-radius:3px!important; border-bottom-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-right-radius:5px!important;border-top-left-radius:3px!important;",
				"border-radius:1px 2px;border-top-left-radius:3px!important;border-bottom-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-right-radius:6px!important;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px;border-top-left-radius:5px!important;border-bottom-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:0 2px 0 4px;border-bottom-right-radius:6px!important;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important;border-bottom-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px;border-bottom-left-radius:3px!important; border-top-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px;border-bottom-left-radius:3px!important;border-top-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:1px 0 3px;border-bottom-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px;border-bottom-left-radius:5px!important;border-top-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:1px 0 3px;border-bottom-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px 4px;border-bottom-left-radius:5px!important;border-top-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:5px!important;border-top-left-radius:3px!important;",
				"border-radius:1px;border-top-left-radius:3px!important; border-bottom-left-radius:5px!important;");
		assertShorthandText(
				"border-radius:1px 2px;border-bottom-left-radius:5px!important;border-top-left-radius:3px!important;",
				"border-radius:1px 2px;border-top-left-radius:3px!important;border-bottom-left-radius:5px!important;");
		assertShorthandText(
				"border-radius:3px 2px;border-bottom-left-radius:6px!important;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px;border-top-left-radius:5px!important;border-bottom-left-radius:6px!important;");
		assertShorthandText(
				"border-radius:3px 2px;border-bottom-left-radius:6px!important;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important;border-bottom-left-radius:6px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-right-radius:5px!important;border-top-right-radius:3px!important;",
				"border-radius:1px;border-top-right-radius:3px!important; border-bottom-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:1px 2px;border-bottom-right-radius:5px!important;border-top-right-radius:3px!important;",
				"border-radius:1px 2px;border-top-right-radius:3px!important;border-bottom-right-radius:5px!important;");
		assertShorthandText(
				"border-radius:1px 2px;border-bottom-right-radius:6px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px 3px;border-top-right-radius:5px!important;border-bottom-right-radius:6px!important;");
		assertShorthandText(
				"border-radius:1px 4px;border-bottom-right-radius:6px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-top-right-radius:5px!important;border-bottom-right-radius:6px!important;");
	}

	@Test
	public void testBorderRadiusImportantMix3() {
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:7px!important;border-top-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px;border-top-left-radius:3px!important; border-top-right-radius:5px!important;border-bottom-left-radius:7px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:7px!important;border-top-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px;border-top-left-radius:3px!important;border-top-right-radius:5px!important;border-bottom-left-radius:7px!important;");
		assertShorthandText(
				"border-radius:3px;border-bottom-left-radius:7px!important;border-top-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;border-bottom-left-radius:7px!important;");
		assertShorthandText(
				"border-radius:3px;border-bottom-left-radius:7px!important;border-top-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;border-bottom-left-radius:7px!important;");
	}

	@Test
	public void testBorderRadiusImportantMix4() {
		assertShorthandText(
				"border-radius:1px;border-bottom-right-radius:7px!important;border-top-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px;border-top-left-radius:3px!important; border-top-right-radius:5px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-right-radius:7px!important;border-top-left-radius:3px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px;border-top-left-radius:3px!important;border-top-right-radius:5px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-right-radius:7px!important;border-top-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:4px;border-bottom-right-radius:7px!important;border-top-left-radius:5px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important;border-top-right-radius:6px!important;border-bottom-right-radius:7px!important;");
	}

	@Test
	public void testBorderRadiusImportantMix5() {
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:5px!important;border-bottom-right-radius:7px!important;border-top-left-radius:3px!important;",
				"border-radius:1px;border-top-left-radius:3px!important; border-bottom-left-radius:5px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-left-radius:5px!important;border-bottom-right-radius:7px!important;border-top-left-radius:3px!important;",
				"border-radius:1px 2px;border-bottom-left-radius:5px!important;border-bottom-right-radius:7px!important;border-top-left-radius:3px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-left-radius:6px!important;border-bottom-right-radius:7px!important;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px;border-top-left-radius:5px!important;border-bottom-left-radius:6px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:2px;border-bottom-left-radius:6px!important;border-bottom-right-radius:7px!important;border-top-left-radius:5px!important;",
				"border-radius:1px 2px 3px 4px;border-top-left-radius:5px!important;border-bottom-left-radius:6px!important;border-bottom-right-radius:7px!important;");
	}

	@Test
	public void testBorderRadiusImportantMix6() {
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:3px!important;border-bottom-right-radius:7px!important;border-top-right-radius:5px!important;",
				"border-radius:1px;border-bottom-left-radius:3px!important; border-top-right-radius:5px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:3px!important;border-bottom-right-radius:7px!important;border-top-right-radius:5px!important;",
				"border-radius:1px 2px;border-bottom-left-radius:3px!important;border-top-right-radius:5px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:5px!important;border-bottom-right-radius:7px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px;border-bottom-left-radius:5px!important;border-top-right-radius:6px!important;border-bottom-right-radius:7px!important;");
		assertShorthandText(
				"border-radius:1px;border-bottom-left-radius:5px!important;border-bottom-right-radius:7px!important;border-top-right-radius:6px!important;",
				"border-radius:1px 2px 3px 4px;border-bottom-left-radius:5px!important;border-top-right-radius:6px!important;border-bottom-right-radius:7px!important;");
	}

	@Test
	public void testBorderRadiusInherit() {
		assertShorthandText("border-radius:inherit;", "border-radius: inherit;");
		assertShorthandText("border-radius:inherit!important;", "border-radius: inherit ! important;");
	}

	@Test
	public void testBorderRadiusInheritBad() {
		assertShorthandText(
				"border-bottom-left-radius:inherit;border-bottom-right-radius:inherit;border-top-left-radius:0;border-top-right-radius:0;",
				"border-radius: 0; border-bottom-left-radius: inherit; border-bottom-right-radius: inherit; ");
	}

	@Test
	public void testBorderRadiusInitial() {
		assertShorthandText("border-radius:0;", "border-radius: initial;");
		assertShorthandText("border-radius:0!important;", "border-radius: initial ! important;");
	}

	@Test
	public void testBorderRadiusUnset() {
		assertShorthandText("border-radius:0;", "border-radius: unset;");
		assertShorthandText("border-radius:0!important;", "border-radius: unset !important;");
	}

	@Test
	public void testBorderRadiusRevert() {
		assertShorthandText("border-radius:revert;", "border-radius: revert;");
		assertShorthandText("border-radius:revert!important;", "border-radius: revert !important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
