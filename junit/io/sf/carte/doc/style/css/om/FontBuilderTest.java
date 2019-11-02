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

public class FontBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testFontNoShorthand() {
		assertShorthandText("font-size:18;", "font-size: 18;");
		assertShorthandText("font-kerning:none;font-size:18;font-size-adjust:.6;",
				"font-size: 18; font-kerning: none; font-size-adjust: 0.6;");
	}

	@Test
	public void testFont() {
		assertShorthandText("font:smaller;", "font: smaller");
		assertShorthandText("font:smaller;", "font: smaller; font-kerning: auto;");
		assertShorthandText("font:smaller;font-kerning:none;", "font: smaller; font-kerning: none;");
		assertShorthandText("font:smaller;font-kerning:none;font-size-adjust:.6;",
				"font: smaller; font-kerning: none; font-size-adjust: 0.6;");
		assertShorthandText("font:400 80%/120%;", "font: 400 80%/120%");
		assertShorthandText("font:400 80%/120% Arial;", "font: 400 80%/120% Arial");
		assertShorthandText("font:400 80%/120% \"Times New Roman\",Arial;",
				"font: 400 80%/120% \"Times New Roman\",Arial");
		assertShorthandText("font:italic small-caps 400 80%/120% \"Times New Roman\",Arial;",
				"font:italic small-caps 400 80%/120% \"Times New Roman\",Arial");
		assertShorthandText("font:serif;", "font: serif");
		assertShorthandText("font:16pt \"Times New Roman\",Arial;", "font: 16pt \"Times New Roman\", Arial");
		assertShorthandText("font:condensed 80% sans-serif;", "font: condensed 80% sans-serif");
		assertShorthandText("font:3em/1.25 \"Helvetica Neue\",Helvetica,sans-serif;",
				"font: 3em/1.25 \"Helvetica Neue\", Helvetica, sans-serif;");
		assertShorthandText("font:14px/1.4 'Helvetica Neue',HelveticaNeue,Helvetica,sans-serif;",
				"font: normal 14.0px / 1.4 Helvetica Neue , HelveticaNeue , Helvetica , sans-serif;");
		assertShorthandText("font:bold 14px/32px 'Roboto',Arial,sans-serif;",
				"font: bold 14px/32px 'Roboto', Arial, sans-serif");
		assertShorthandText("font:14px/1 FontAwesome;", "font: normal 14px / 1 FontAwesome");
	}

	@Test
	public void testFontPlusImportant() {
		assertShorthandText("font:bolder serif;font-size:120%!important;",
				"font: bolder serif; font-size: 120% ! important;");
	}

	@Test
	public void testFontImportant() {
		assertShorthandText("font:smaller!important;", "font: smaller ! important");
		assertShorthandText("font:smaller!important;font-kerning:none!important;",
				"font: smaller ! important; font-kerning: none ! important;");
		assertShorthandText("font:400 80%/120%!important;", "font: 400 80%/120%!important");
		assertShorthandText("font:serif!important;", "font: serif!important");
	}

	@Test
	public void testFontVar() {
		assertShorthandText("font:var(--foo, 12pt Arial);", "font: var(--foo,12pt Arial)");
	}

	@Test
	public void testFontInherit() {
		assertShorthandText("font:inherit;", "font: inherit;");
	}

	@Test
	public void testFontInheritImportant() {
		assertShorthandText("font:inherit!important;", "font: inherit!important;");
	}

	@Test
	public void testFontUnset() {
		assertShorthandText("font:unset;", "font: unset;");
	}

	@Test
	public void testFontUnsetImportant() {
		assertShorthandText("font:unset!important;", "font: unset!important;");
	}

	@Test
	public void testFontRevert() {
		assertShorthandText("font:revert;", "font: revert;");
	}

	@Test
	public void testFontRevertImportant() {
		assertShorthandText("font:revert!important;", "font: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
