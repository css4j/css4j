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

public class GenericShorthandBuilderTest {

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
	public void testBuilderNoShorthand() {
		assertShorthandText("text-decoration-style:double;", "text-decoration-style: double;");
		// 'wavy' is not a valid color
		assertShorthandText(
				"text-decoration-color:wavy;text-decoration-line:line-through;text-decoration-style:double;",
				"text-decoration: double line-through; text-decoration-color: wavy;");
		assertShorthandText("outline-color:foo;outline-style:solid;outline-width:1rem;",
				"outline-width: 1rem; outline-style: solid; outline-color: foo;");
	}

	@Test
	public void testBuilderNoShorthandKeywordMix() {
		assertShorthandText(
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:inherit;",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:inherit;");
		assertShorthandText(
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:revert;",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:revert;");
		assertShorthandText("text-emphasis-color:unset;text-emphasis-style:open;",
				"text-emphasis-style:open;text-emphasis-color:unset");
	}

	@Test
	public void testBuilderNoShorthandCustomProperty() {
		assertShorthandText(
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:var(--foo-style);",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:var(--foo-style);");
	}

	@Test
	public void testBuilderNoShorthandIEHack() {
		assertShorthandText(
				"text-decoration-color:navy\\9;text-decoration-line:line-through;text-decoration-style:double;",
				"text-decoration-color:navy\\9;text-decoration-line:line-through;text-decoration-style:double;");
		assertShorthandText(
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:double \\9;",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:double \\9;");
	}

	@Test
	public void testBuilderKeywordMix() {
		assertShorthandText("text-decoration:line-through navy;",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:unset;");
		assertShorthandText("text-emphasis:open;",
				"text-emphasis-style:open;text-emphasis-color:initial");
	}

	@Test
	public void testTextDecoration() {
		assertShorthandText("text-decoration:none;", "text-decoration: initial");
		assertShorthandText("text-decoration:double;", "text-decoration: double");
		assertShorthandText("text-decoration:blink dashed #ffb;",
				"text-decoration: blink dashed #ffb");
		assertShorthandText("text-decoration:blink dashed;", "text-decoration: blink dashed");
		assertShorthandText("text-decoration:blink #ffb;", "text-decoration: blink #ffb");
		assertShorthandText("text-decoration:#ffb;", "text-decoration: #ffb");
	}

	@Test
	public void testColumns() {
		assertShorthandText("columns:10em;", "columns: 10em auto; ");
		assertShorthandText("columns:2;", "columns: 2 auto; ");
		assertShorthandText("columns:auto;", "columns: auto; ");
		assertShorthandText("columns:10em 2;", "columns: 10em 2; ");
	}

	@Test
	public void testColumnRule() {
		assertShorthandText("column-rule:1em solid black;", "column-rule: 1em solid black; ");
		assertShorthandText("column-rule:1em solid;", "column-rule: 1em solid; ");
	}

	@Test
	public void testFlexFlow() {
		assertShorthandText("flex-flow:row;", "flex-flow: row;");
		assertShorthandText("flex-flow:column wrap;", "flex-flow: column wrap;");
		assertShorthandText("flex-flow:column;", "flex-flow: column;");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("text-decoration:var(--foo,double);",
				"text-decoration: var(--foo,double);");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("text-decoration:none!important;",
				"text-decoration: initial!important");
		assertShorthandText("text-decoration:double!important;",
				"text-decoration: double ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("text-decoration:inherit;", "text-decoration: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("text-decoration:inherit!important;",
				"text-decoration: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("text-decoration:none;", "text-decoration: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("text-decoration:none!important;", "text-decoration: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("text-decoration:revert;", "text-decoration: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("text-decoration:revert!important;",
				"text-decoration: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
