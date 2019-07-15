/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GenericShorthandBuilderTest {

	private BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
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
	public void testBuilderNoShorthandCustomProperty() {
		assertShorthandText(
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:var(--foo-style);",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:var(--foo-style);");
	}

	@Test
	public void testBuilderNoShorthandIEHack() {
		assertShorthandText(
				"text-decoration-color:navy\\9 ;text-decoration-line:line-through;text-decoration-style:double;",
				"text-decoration-color:navy\\9;text-decoration-line:line-through;text-decoration-style:double;");
		assertShorthandText(
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:double \\9 ;",
				"text-decoration-color:navy;text-decoration-line:line-through;text-decoration-style:double \\9;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("text-decoration:double;", "text-decoration: double");
		assertShorthandText("text-decoration:blink dashed #ffb;", "text-decoration: blink dashed #ffb");
		assertShorthandText("text-decoration:blink dashed;", "text-decoration: blink dashed");
		assertShorthandText("text-decoration:blink #ffb;", "text-decoration: blink #ffb");
		assertShorthandText("text-decoration:#ffb;", "text-decoration: #ffb");
		assertShorthandText("outline:solid 1rem;", "outline: 1rem solid invert; ");
		assertShorthandText("outline:solid 1rem;", "outline: 1rem solid; ");
		assertShorthandText("columns:10em;", "columns: 10em auto; ");
		assertShorthandText("columns:2;", "columns: 2 auto; ");
		assertShorthandText("columns:auto;", "columns: auto; ");
		assertShorthandText("columns:10em 2;", "columns: 10em 2; ");
		assertShorthandText("flex-flow:row;", "flex-flow: row; ");
		assertShorthandText("flex-flow:column wrap;", "flex-flow: column wrap; ");
		assertShorthandText("flex-flow:column;", "flex-flow: column; ");
		assertShorthandText("column-rule:1em solid black;", "column-rule: 1em solid black; ");
		assertShorthandText("column-rule:1em solid;", "column-rule: 1em solid; ");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("text-decoration:double!important;", "text-decoration: double ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("text-decoration:inherit;", "text-decoration: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("text-decoration:inherit!important;", "text-decoration: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("text-decoration:unset;", "text-decoration: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("text-decoration:unset!important;", "text-decoration: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
