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

public class OutlineShorthandBuilderTest {

	private BaseCSSStyleDeclaration emptyStyleDecl;

	private static AbstractCSSStyleSheet sheet;

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
	public void testOutline() {
		assertShorthandText("outline:currentcolor solid 1rem;",
				"outline: 1rem solid currentColor; ");
		assertShorthandText("outline:solid 1rem;", "outline: 1rem solid; ");
		assertShorthandText("outline:invert dotted thin;", "outline: thin dotted invert; ");
		assertShorthandText("outline:#0ff solid 1rem;", "outline: 1rem solid #0ff; ");
	}

	@Test
	public void testOutlineNone() {
		assertShorthandText("outline:none;", "outline: none;");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("outline:var(--foo,double);", "outline: var(--foo,double);");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("outline:none!important;", "outline: none!important");
		assertShorthandText("outline:none!important;", "outline: initial!important");
		assertShorthandText("outline:auto!important;", "outline: auto ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("outline:inherit;", "outline: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("outline:inherit!important;", "outline: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("outline:none;", "outline: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("outline:none!important;", "outline: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("outline:revert;", "outline: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("outline:revert!important;", "outline: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
