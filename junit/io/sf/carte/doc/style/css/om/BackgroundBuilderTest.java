/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BackgroundBuilderTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBackgroundIndividual() {
		emptyStyleDecl.setCssText(
				"background-image: linear-gradient(transparent, transparent), url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA');");
		assertEquals(
				"background-image:linear-gradient(transparent,transparent),url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA');",
				emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBackgroundLayeredMix() {
		assertShorthandText(
				"background:linear-gradient(transparent,transparent) center top no-repeat,url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') center top no-repeat;",
				"background: transparent no-repeat center top; background-image: linear-gradient(transparent, transparent), url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA');");
	}

	@Test
	public void testBackgroundLayers() {
		assertShorthandText(
				"background:url('a.png') no-repeat,url('b.png') center/100% 100% no-repeat,url('c.png') white;",
				"background: url(a.png) top left no-repeat,url(b.png) center / 100% 100% no-repeat,url(c.png) white;");
	}

	@Test
	public void testBackgroundLayersBad() {
		emptyStyleDecl.setCssText(
				"background: url('bkg.png') no-repeat; background-size: 150px, 150px; background-position: 0 0;");
		assertEquals("background:url('bkg.png') 0 0/150px no-repeat;", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBackgroundNone() {
		assertShorthandText("background:none;", "background: none;");
	}

	@Test
	public void testBackgroundNoneImportant() {
		assertShorthandText("background:none!important;", "background: none ! important;");
	}

	@Test
	public void testBackgroundNoneLayer() {
		assertShorthandText("background:none,yellow;", "background: none,yellow;");
	}

	@Test
	public void testBackgroundLayerNone() {
		assertShorthandText("background:url('a.png'),none;", "background: url(a.png),none;");
	}

	@Test
	public void testBackgroundLayerNoneImportant() {
		assertShorthandText("background:url('a.png'),none!important;", "background: url(a.png),none ! important;");
	}

	@Test
	public void testBackgroundInherit() {
		assertShorthandText("background:inherit;", "background: inherit;");
	}

	@Test
	public void testBackgroundInheritImportant() {
		assertShorthandText("background:inherit!important;", "background: inherit ! important;");
	}

	@Test
	public void testBackgroundInheritBad() {
		emptyStyleDecl.setCssText(
				"background: url(a.png) top left no-repeat,url(b.png) center / 100% 100% no-repeat,url(c.png) inherit;");
		assertEquals("background:url('a.png') no-repeat,url('b.png') center/100% 100% no-repeat,inherit;",
				emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBackgroundUnset() {
		assertShorthandText("background:unset;", "background: unset;");
	}

	@Test
	public void testBackgroundUnsetImportant() {
		assertShorthandText("background:unset!important;", "background: unset ! important;");
	}

	@Test
	public void testBackground() {
		assertShorthandText("background:url('bkg.png') 40%/10em round fixed border-box gray;",
				"background: url('bkg.png') 40% / 10em gray round fixed border-box;");
	}

	@Test
	public void testBackground2() {
		assertShorthandText("background:url('bkg.png') right round fixed border-box gray;",
				"background: url('bkg.png') right center gray round fixed border-box;");
	}

	@Test
	public void testBackground3() {
		assertShorthandText("background:url('bkg.png') 0% 0%/10em round fixed border-box gray;",
				"background: url('bkg.png') 0% 0% / 10em gray round fixed border-box;");
	}

	@Test
	public void testBackground4() {
		assertShorthandText("background:center no-repeat;",
				"background:transparent none no-repeat scroll center center");
	}

	@Test
	public void testBackground5() {
		assertShorthandText("background:center no-repeat content-box border-box;",
				"background:transparent none content-box no-repeat no-repeat scroll center center border-box");
	}

	@Test
	public void testBackgroundIEHack() {
		assertShorthandText("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box;background-position:15px 10px;background-repeat:no-repeat;background-size:auto \\9 ;",
				"background: transparent url('image.svg') 15px 10px no-repeat;background-size: auto \\9;");
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box;background-position:15px 10px;background-repeat:no-repeat \\9 ;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box;background-position:15px 10px;background-repeat:no-repeat \\9 ;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box;background-position:15px \\9;background-repeat:no-repeat;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box;background-position:15px \\9 ;background-repeat:no-repeat;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box\\9;background-position:15px;background-repeat:no-repeat;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box\\9 ;background-position:15px;background-repeat:no-repeat;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box \\9;background-position:15px;background-repeat:no-repeat;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg');background-origin:padding-box \\9 ;background-position:15px;background-repeat:no-repeat;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg') \\9 ;background-origin:padding-box;background-position:15px;background-repeat:no-repeat;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('image.svg') \\9 ;background-origin:padding-box;background-position:15px;background-repeat:no-repeat;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent\\9;background-image:url('image.svg');background-origin:padding-box;background-position:15px;background-repeat:no-repeat;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent\\9 ;background-image:url('image.svg');background-origin:padding-box;background-position:15px;background-repeat:no-repeat;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText("background-attachment:scroll;background-clip:border-box;background-color:transparent \\9;background-image:url('image.svg');background-origin:padding-box;background-position:15px;background-repeat:no-repeat;background-size:auto;");
		assertEquals("background-attachment:scroll;background-clip:border-box;background-color:transparent \\9 ;background-image:url('image.svg');background-origin:padding-box;background-position:15px;background-repeat:no-repeat;background-size:auto;", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBackgroundData() {
		assertShorthandText(
				"background:url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') 40%/10em round fixed border-box gray;",
				"background: url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') 40% / 10em gray round fixed border-box;");
	}

	@Test
	public void testBackgroundNoShorthand() {
		emptyStyleDecl.setCssText("background-image: url('bkg.png');");
		assertEquals("background-image:url('bkg.png');", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBackgroundNoShorthand2() {
		assertShorthandText(
				"background-attachment:scroll;background-clip:border-box;background-color:var(--my-color);background-image:none;background-origin:padding-box;background-position:0% 0%;background-repeat:repeat;background-size:auto auto;",
				"background: #dae;background-color: var(--my-color);");
	}

	@Test
	public void testBackgroundBad() {
		emptyStyleDecl.setCssText("background: url('bkg.png') 40% / 10em gray round fixed border-box, url('foo.png');");
		assertEquals("", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testBackgroundBadIndividual() {
		assertShorthandText(
				"background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:url('bkg.png');background-origin:padding-box;background-position:0% 0%;background-repeat:repeat;background-size:foo;",
				"background: url('bkg.png'); background-size: foo;");
	}

	@Test
	public void testBackgroundBadIndividual2() {
		assertShorthandText(
				"background-attachment:scroll;background-clip:border-box;background-color:transparent;background-image:none foo;background-origin:padding-box;background-position:0% 0%;background-repeat:repeat;background-size:auto auto;",
				"background: url('bkg.png'); background-image: none foo ;");
	}

	@Test
	public void testBackgroundImage() {
		assertShorthandText("background:url('bkg.png');", "background: url('bkg.png');");
	}

	@Test
	public void testBackgroundImage2() {
		assertShorthandText("background:0 0,url('../img/foo.png') no-repeat;",
				"background:0 0,url(../img/foo.png) no-repeat;");
	}

	@Test
	public void testBackgroundImageGradient() {
		assertShorthandText("background:linear-gradient(to left top,#5a66a7,#653287);",
				"background:linear-gradient(to left top, rgb(90, 102, 167), rgb(101, 50, 135));");
	}

	@Test
	public void testBackgroundImageGradient2() {
		assertShorthandText(
				"background:linear-gradient(to right,rgb(66 103 178/0),#577fbc,rgb(66 103 178/0)) 0% 0%/1016px auto;",
				"background: linear-gradient(to right, rgb(66 103 178 / 0), #577fbc, rgb(66 103 178 / 0)) 0% 0% / 1016px auto;");
	}

	@Test
	public void testBackgroundImageGradient3() {
		assertShorthandText("background:radial-gradient(40%,circle,#d4a9af 55%,#000 150%);",
				"background:radial-gradient(40%,circle,#d4a9af 55%,#000 150%);");
	}

	@Test
	public void testBackgroundMisleadingColor() {
		assertShorthandText(
				"background-attachment:scroll;background-clip:border-box;background-color:none;background-image:url('bkg.png');background-origin:padding-box;background-position:0% 0%;background-repeat:repeat;background-size:auto auto;",
				"background: url('bkg.png'); background-color: none");
	}

	@Test
	public void testBackgroundMisleadingColor2() {
		assertShorthandText(
				"background-attachment:scroll;background-clip:border-box;background-color:url('foo.png');background-image:none;background-origin:padding-box;background-position:0% 0%;background-repeat:no-repeat;background-size:auto auto;",
				"background: #ccee42 none no-repeat scroll; background-color: url('foo.png')");
	}

	@Test
	public void testBackgroundLayered() {
		assertShorthandText(
				"background:none,url('../img/foo.png') bottom/cover no-repeat fixed padding-box content-box,olive;",
				"background:none, url(../img/foo.png) bottom / cover no-repeat fixed padding-box content-box, olive;");
	}

	@Test
	public void testBackgroundLayered2() {
		assertShorthandText(
				"background:none,url('../img/foo.png') bottom/cover no-repeat fixed padding-box content-box,none;",
				"background:none, url(../img/foo.png) bottom / cover no-repeat fixed padding-box content-box, padding-box border-box;");
	}

	@Test
	public void testBackgroundImagePosition() {
		assertShorthandText("background:url('bkg.png') 40%;", "background: url('bkg.png') 40%;");
	}

	@Test
	public void testBackgroundImagePositionPlusAttachment() {
		assertShorthandText("background:url('bkg.png') 40%;background-attachment:local!important;",
				"background: url('bkg.png') 40%; background-attachment: local ! important;");
	}

	@Test
	public void testBackgroundColor() {
		assertShorthandText("background:gray;", "background: gray;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
