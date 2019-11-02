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

public class BorderImageBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBorderImageNoShorthand() {
		assertShorthandText("border-image-source:url('/img/border.png');",
				"border-image-source:url('/img/border.png');");
	}

	@Test
	public void testBorderImageNone() {
		assertShorthandText("border-image-source:none;", "border-image-source:none;");
	}

	@Test
	public void testBorderImageVar() {
		assertShorthandText("border-image:var(--foo, url('/img/border.png'));",
				"border-image:var(--foo,url('/img/border.png'));");
	}

	@Test
	public void testBorderImageNoneImportant() {
		emptyStyleDecl.setCssText("border-image: none ! important");
		assertEquals("border-image:none!important;", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBorderImageInherit() {
		emptyStyleDecl.setCssText("border-image: inherit");
		assertEquals("border-image:inherit;", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBorderImageInheritImportant() {
		emptyStyleDecl.setCssText("border-image: inherit ! important");
		assertEquals("border-image:inherit!important;", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBorderImageUnset() {
		assertShorthandText("border-image:none;", "border-image: unset");
	}

	@Test
	public void testBorderImageUnsetImportant() {
		assertShorthandText("border-image:none!important;", "border-image: unset ! important");
	}

	@Test
	public void testBorderImageRevert() {
		assertShorthandText("border-image:revert;", "border-image: revert");
	}

	@Test
	public void testBorderImageRevertImportant() {
		assertShorthandText("border-image:revert!important;", "border-image: revert ! important");
	}

	@Test
	public void testBorderImage() {
		assertShorthandText("border-image:url('/img/border.png') 25% 30% 12% 20% fill/2pt/1 round;",
				"border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round;");
	}

	@Test
	public void testBorderImage2() {
		assertShorthandText("border-image:url('/img/border.png') 25% 30% 12% 20% fill/2pt 4pt/1 3 round;",
				"border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt 4pt / 1 3 round;");
	}

	@Test
	public void testBorderImage3() {
		assertShorthandText("border-image:url('/img/border.png') 25% 30%/2pt round;",
				"border-image: url('/img/border.png') 25% 30% / 2pt round;");
	}

	@Test
	public void testBorderImage4() {
		assertShorthandText("border-image:url('/img/border.png') 25% 30%/auto round;",
				"border-image: url('/img/border.png') 25% 30% / auto round;");
	}

	@Test
	public void testBorderImage5() {
		assertShorthandText("border-image:25% 30%/auto round;", "border-image: none 25% 30% / auto round;");
	}

	@Test
	public void testBorderImage6() {
		assertShorthandText("border-image:25% 30%/auto round;border-image-source:url('foo.png')!important;",
				"border-image-source: url(foo.png)!important; border-image: none 25% 30% / auto round;");
	}

	@Test
	public void testBorderImage7() {
		assertShorthandText(
				"border:8px;border-image:repeating-conic-gradient(gold,#f06 20deg) 25% 30% 12% 20% fill/2pt/1 round;",
				"border:8px; border-image: repeating-conic-gradient(gold, #f06 20deg) 25% 30% 12% 20% fill / 2pt / 1 round;");
	}

	@Test
	public void testBorderImage8() {
		assertShorthandText("border-image:url('foo.png') 9 repeat;border-width:8px;",
				"border-width: 8px; border-image: url('foo.png') 9 repeat;");
	}

	@Test
	public void testBorderImageImportant() {
		assertShorthandText("border-image:url('/img/border.png') 25% 30% 12% 20% fill/2pt/1 round!important;",
				"border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round ! important;");
		assertShorthandText("border-image:url('/img/border.png') 25% 30% 12% 20% fill/2pt 4pt/1 3 round!important;",
				"border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt 4pt / 1 3 round!important;");
		assertShorthandText("border-image:url('/img/border.png') 25% 30%/2pt round!important;",
				"border-image: url('/img/border.png') 25% 30% / 2pt round!important;");
	}

	@Test
	public void testBorderImageNoShorthandKeyword() {
		assertShorthandText(
				"border-image:url('foo.png');",
				"border-image-source: url('foo.png'); border-image-outset: 0; border-image-slice: 100%; border-image-repeat: unset; border-image-width: 1;");
		assertShorthandText(
				"border-image:none;",
				"border-image-source:unset; border-image-outset:0; border-image-slice:100%;border-image-repeat: unset;border-image-width:1;");
		assertShorthandText(
				"border-image-outset:0;border-image-repeat:revert;border-image-slice:100%;border-image-source:url('foo.png');border-image-width:1;",
				"border-image-source: url('foo.png'); border-image-outset: 0; border-image-slice: 100%; border-image-repeat: revert; border-image-width: 1;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
