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

public class MaskBuilderTest {

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
	public void testMaskIndividual() {
		assertShorthandText("mask-image:linear-gradient(35deg,#fa3 50%,transparent 0);",
				"mask-image: linear-gradient(35deg,#fa3 50%,transparent 0);");
		assertShorthandText(
				"mask-image:linear-gradient(transparent,transparent),url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA');",
				"mask-image: linear-gradient(transparent, transparent), url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA');");
	}

	@Test
	public void testMaskLayeredMix() {
		assertShorthandText(
				"mask:linear-gradient(transparent,transparent) center top no-repeat luminance,url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') center top no-repeat luminance;",
				"mask: luminance no-repeat center top; mask-image: linear-gradient(transparent, transparent), url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA');");
	}

	@Test
	public void testMaskLayeredMixVar() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:linear-gradient(to right,currentColor var(--foo,0),no-repeat center var(--foo,0));mask-mode:match-source;mask-origin:border-box;mask-position:0 0;mask-repeat:repeat;mask-size:auto auto;",
				"mask: 0 0;mask-image: linear-gradient(to right, currentColor var(--foo, 0), no-repeat center var(--foo, 0))");
	}

	@Test
	public void testMaskLayers() {
		assertShorthandText(
				"mask:url('a.png') no-repeat,url('b.png') center/100% 100% no-repeat,url('c.png') alpha;",
				"mask: url(a.png) top left no-repeat,url(b.png) center / 100% 100% no-repeat,url(c.png) alpha;");
	}

	@Test
	public void testMaskLayersBad() {
		assertShorthandText("mask:url('bkg.png') 0 0/150px no-repeat;",
				"mask: url('bkg.png') no-repeat; mask-size: 150px, 150px; mask-position: 0 0;");
	}

	@Test
	public void testMaskNone() {
		assertShorthandText("mask:none;", "mask: none;");
		assertShorthandText("mask:none;", "mask: initial;");
	}

	@Test
	public void testMaskNoneImportant() {
		assertShorthandText("mask:none!important;", "mask: none ! important;");
		assertShorthandText("mask:none!important;", "mask: initial ! important;");
	}

	@Test
	public void testMaskNoneLayer() {
		assertShorthandText("mask:none,alpha;", "mask: none,alpha;");
	}

	@Test
	public void testMaskLayerNone() {
		assertShorthandText("mask:url('a.png'),none;", "mask: url(a.png),none;");
	}

	@Test
	public void testMaskLayerNoneImportant() {
		assertShorthandText("mask:url('a.png'),none!important;",
				"mask: url(a.png),none ! important;");
	}

	@Test
	public void testMaskInherit() {
		assertShorthandText("mask:inherit;", "mask: inherit;");
	}

	@Test
	public void testMaskInheritImportant() {
		assertShorthandText("mask:inherit!important;", "mask: inherit ! important;");
	}

	@Test
	public void testMaskInheritPlusBorderMode() {
		assertShorthandText("mask:inherit;mask-border-mode:inherit!important;",
				"mask: inherit;mask-border-mode:inherit ! important;");
	}

	@Test
	public void testMaskInheritPlusBorderModeImportant() {
		assertShorthandText("mask:inherit!important;mask-border-mode:alpha!important;",
				"mask: inherit!important;mask-border-mode:alpha ! important;");
	}

	@Test
	public void testMaskUnset() {
		assertShorthandText("mask:none;", "mask: unset;");
	}

	@Test
	public void testMaskUnsetImportant() {
		assertShorthandText("mask:none!important;", "mask: unset ! important;");
	}

	@Test
	public void testMaskRevert() {
		assertShorthandText("mask:revert;", "mask: revert;");
	}

	@Test
	public void testMaskRevertImportant() {
		assertShorthandText("mask:revert!important;", "mask: revert ! important;");
	}

	@Test
	public void testMask() {
		assertShorthandText("mask:url('bkg.png') 40%/10em round view-box intersect luminance;",
				"mask: url('bkg.png') 40% / 10em luminance round intersect view-box;");
	}

	@Test
	public void testMask2() {
		assertShorthandText("mask:url('bkg.png') right no-repeat view-box intersect luminance;",
				"mask: url('bkg.png') right center luminance no-repeat no-repeat intersect view-box;");
	}

	@Test
	public void testMask3() {
		assertShorthandText("mask:url('bkg.png') 0% 0%/10em round view-box intersect luminance;",
				"mask: url('bkg.png') 0% 0% / 10em luminance round round intersect view-box;");
	}

	@Test
	public void testMask4() {
		assertShorthandText("mask:center repeat-y alpha;",
				"mask:border-box none no-repeat repeat alpha center center");
	}

	@Test
	public void testMask5() {
		assertShorthandText("mask:center repeat-x alpha;",
				"mask:border-box none repeat no-repeat alpha center center");
	}

	@Test
	public void testMask6() {
		assertShorthandText("mask:center space alpha;",
				"mask:border-box none space space alpha center center");
	}

	/*
	 * IE hacks are not going to happen in mask properties, but the generic IE
	 * support is tested.
	 */
	@Test
	public void testMaskIEHack() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:intersect;mask-image:url('image.svg');mask-mode:match-source;mask-origin:border-box;mask-position:15px 10px;mask-repeat:no-repeat;mask-size:auto \\9;",
				"mask: intersect url('image.svg') 15px 10px no-repeat;mask-size: auto \\9;");
		assertShorthandText(
				"mask-clip:border-box;mask-composite:intersect;mask-image:url('image.svg');mask-mode:alpha;mask-origin:padding-box;mask-position:15px 10px;mask-repeat:no-repeat \\9;mask-size:auto;",
				"mask-mode:alpha;mask-clip:border-box;mask-composite:intersect;mask-image:url('image.svg');mask-origin:padding-box;mask-position:15px 10px;mask-repeat:no-repeat \\9 ;mask-size:auto;");
		assertShorthandText(
				"mask-clip:border-box;mask-composite:intersect;mask-image:url('image.svg');mask-mode:alpha;mask-origin:padding-box;mask-position:15px \\9;mask-repeat:no-repeat;mask-size:auto;",
				"mask-mode:alpha;mask-clip:border-box;mask-composite:intersect;mask-image:url('image.svg');mask-origin:padding-box;mask-position:15px \\9;mask-repeat:no-repeat;mask-size:auto;");
	}

	@Test
	public void testMaskData() {
		assertShorthandText(
				"mask:url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') 40%/10em round intersect luminance;",
				"mask: url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') 40% / 10em luminance round intersect border-box;");
	}

	/*
	 * Only mask-image.
	 */
	@Test
	public void testMaskNoShorthand() {
		assertShorthandText("mask-image:url('bkg.png');", "mask-image: url('bkg.png');");
	}

	/*
	 * All of the mask-border longhands but no mask properties.
	 */
	@Test
	public void testMaskNoShorthand2() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;",
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto");
	}

	/*
	 * All of the mask-border longhands, not enough mask properties.
	 */
	@Test
	public void testMaskNoShorthand3() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:none;mask-mode:alpha;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;",
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:none;mask-mode:alpha;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;");
	}

	/*
	 * All of the mask longhands, none of the mask-border ones.
	 */
	@Test
	public void testMaskNoShorthand4() {
		assertShorthandText(
				"mask-clip:border-box;mask-composite:add;mask-image:none;mask-mode:alpha;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask-clip:border-box;mask-composite:add;mask-image:none;mask-mode:alpha;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;");
	}

	@Test
	public void testMaskNoShorthandVar() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:var(--my-compositing);mask-image:none;mask-mode:alpha;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask: alpha;mask-composite: var(--my-compositing);");
	}

	@Test
	public void testMaskPendingSubstitution() {
		assertShorthandText("mask:var(--foo,#f6ac43);", "mask: var(--foo, #f6ac43);");
	}

	@Test
	public void testMaskPendingSubstitutionImportant() {
		assertShorthandText("mask:var(--foo,#f6ac43)!important;",
				"mask: var(--foo, #f6ac43)!important;");
	}

	@Test
	public void testMaskPendingSubstitutionUpperCase() {
		assertShorthandText("mask:var(--FOO,#f6ac43);mask-composite:var(--BAR);",
				"mask: var(--FOO, #f6ac43);mask-composite: var(--BAR)");
	}

	@Test
	public void testMaskPendingSubstitutionUpperCase2() {
		assertShorthandText("mask:var(--FOO,#f6ac43);", "mask: var(--FOO, #f6ac43);");
	}

	@Test
	public void testMaskPendingSubstitutionUpperCase2Important() {
		assertShorthandText("mask:var(--FOO,#f6ac43)!important;",
				"mask: var(--FOO, #f6ac43)!important;");
	}

	@Test
	public void testMaskBad() {
		emptyStyleDecl.setCssText(
				"mask: url('bkg.png') 40% / 10em gray round intersect border-box, url('foo.png');");
		assertEquals("", emptyStyleDecl.getOptimizedCssText());
	}

	@Test
	public void testMaskBadIndividual() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:url('bkg.png');mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:foo;",
				"mask: url('bkg.png'); mask-size: foo;");
	}

	@Test
	public void testMaskBadIndividual2() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:none foo;mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask: url('bkg.png'); mask-image: none foo ;");
	}

	@Test
	public void testMaskImage() {
		assertShorthandText("mask:url('bkg.png');", "mask: url('bkg.png');");
	}

	@Test
	public void testMaskImage2() {
		assertShorthandText("mask:0 0,url('../img/foo.png') no-repeat;",
				"mask:0 0,url(../img/foo.png) no-repeat;");
	}

	@Test
	public void testMaskImageGradient() {
		assertShorthandText("mask:linear-gradient(to left top,#5a66a7,#653287);",
				"mask:linear-gradient(to left top, rgb(90, 102, 167), rgb(101, 50, 135));");
	}

	@Test
	public void testMaskImageGradient2() {
		assertShorthandText(
				"mask:linear-gradient(to right,rgb(66 103 178/0),#577fbc,rgb(66 103 178/0)) 0% 0%/1016px auto;",
				"mask: linear-gradient(to right, rgb(66 103 178 / 0), #577fbc, rgb(66 103 178 / 0)) 0% 0% / 1016px auto;");
	}

	@Test
	public void testMaskImageGradient3() {
		assertShorthandText("mask:radial-gradient(40%,circle,#d4a9af 55%,#000 150%);",
				"mask:radial-gradient(40%,circle,#d4a9af 55%,#000 150%);");
	}

	@Test
	public void testMaskLayered() {
		assertShorthandText(
				"mask:luminance,url('../img/foo.png') bottom/cover no-repeat padding-box content-box intersect,bottom;",
				"mask:luminance, url(../img/foo.png) bottom / cover no-repeat intersect padding-box content-box, bottom;");
	}

	@Test
	public void testMaskLayered2() {
		assertShorthandText(
				"mask:alpha,url('../img/foo.png') bottom/cover no-repeat padding-box content-box intersect,repeat-y;",
				"mask:alpha, url(../img/foo.png) bottom / cover no-repeat intersect padding-box content-box, repeat-y;");
	}

	@Test
	public void testMaskImagePosition() {
		assertShorthandText("mask:url('bkg.png') 40%;", "mask: url('bkg.png') 40%;");
	}

	@Test
	public void testMaskCrossFadePosition() {
		assertShorthandText("mask:cross-fade(url('foo.png') 25%,url('bar.png') 75%) 40%;",
				"mask: cross-fade(url(foo.png) 25%, url(bar.png) 75%) 40%;");
	}

	@Test
	public void testMaskImagePositionPlusAttachment() {
		assertShorthandText("mask:url('bkg.png') 40%;mask-mode:match-source!important;",
				"mask: url('bkg.png') 40%; mask-mode: match-source ! important;");
	}

	@Test
	public void testMaskImagePositionPlusMaskBorderMode() {
		assertShorthandText("mask:url('bkg.png') 40%;mask-border-mode:luminance;",
				"mask: url('bkg.png') 40%;mask-border-mode:luminance;");
	}

	@Test
	public void testMaskImagePositionPlusMaskBorderModeImportant() {
		assertShorthandText("mask:url('bkg.png') 40%;mask-border-mode:luminance!important;",
				"mask: url('bkg.png') 40%;mask-border-mode:luminance ! important;");
	}

	@Test
	public void testMaskImagePositionPlusMaskBorderModeImportant2() {
		assertShorthandText("mask:url('bkg.png') 40%;mask-border-mode:alpha!important;",
				"mask-border-mode:alpha ! important;mask: url('bkg.png') 40%;");
	}

	@Test
	public void testMaskImagePositionPlusMaskBorderModeImportant3() {
		assertShorthandText(
				"mask:url('bkg.png') 40%!important;mask-border-mode:luminance!important;",
				"mask: url('bkg.png') 40%!important;mask-border-mode:luminance ! important;");
	}

	@Test
	public void testMaskImageRepeat() {
		assertShorthandText("mask:url('bkg.png') repeat-x;", "mask: url('bkg.png') repeat-x;");
	}

	@Test
	public void testMaskImageOriginClipFBBB() {
		assertShorthandText("mask:url('bkg.png') fill-box border-box;",
				"mask: url('bkg.png') fill-box border-box;");
	}

	@Test
	public void testMaskImageBorderBox() {
		assertShorthandText("mask:url('bkg.png');", "mask: url('bkg.png') border-box;");
	}

	@Test
	public void testMaskImageFillBox() {
		assertShorthandText("mask:url('bkg.png') fill-box;", "mask: url('bkg.png') fill-box;");
	}

	@Test
	public void testMaskImageStrokeBoxBorderBox() {
		assertShorthandText("mask:url('bkg.png') round space stroke-box border-box;",
				"mask: url('bkg.png') stroke-box border-box round space;");
	}

	@Test
	public void testMaskImageVar() {
		assertShorthandText(
				"mask:linear-gradient(to bottom,var(--white) 0%,var(--grey) 66%,var(--black) 100%);",
				"mask: linear-gradient(to bottom, var(--white) 0%, var(--grey) 66%, var(--black) 100%)");
	}

	@Test
	public void testMaskVarImage() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:var(--my-value);mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask:url('bkg.png');mask-image:var(--my-value);");
	}

	@Test
	public void testMaskVarOrigin() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:url('bkg.png');mask-mode:match-source;mask-origin:var(--my-value);mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask:url('bkg.png');mask-origin:var(--my-value);");
	}

	@Test
	public void testMaskVarClip() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:var(--my-value);mask-composite:add;mask-image:url('bkg.png');mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask:url('bkg.png');mask-clip:var(--my-value);");
	}

	@Test
	public void testMaskVarComposite() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:var(--my-value);mask-image:url('bkg.png');mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask:url('bkg.png');mask-composite:var(--my-value);");
	}

	@Test
	public void testMaskVarRepeat() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:url('bkg.png');mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:var(--my-value);mask-size:auto auto;",
				"mask:url('bkg.png');mask-repeat:var(--my-value);");
	}

	@Test
	public void testMaskVarMode() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:url('bkg.png');mask-mode:var(--my-value);mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:auto auto;",
				"mask:url('bkg.png');mask-mode:var(--my-value);");
	}

	@Test
	public void testMaskVarPosition() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:url('bkg.png');mask-mode:match-source;mask-origin:border-box;mask-position:var(--my-value);mask-repeat:repeat;mask-size:auto auto;",
				"mask:url('bkg.png');mask-position:var(--my-value);");
	}

	@Test
	public void testMaskVarSize() {
		assertShorthandText(
				"mask-border-mode:alpha;mask-border-outset:0;mask-border-repeat:stretch;mask-border-slice:0;mask-border-source:none;mask-border-width:auto;mask-clip:border-box;mask-composite:add;mask-image:url('bkg.png');mask-mode:match-source;mask-origin:border-box;mask-position:0% 0%;mask-repeat:repeat;mask-size:var(--my-value);",
				"mask:url('bkg.png');mask-size:var(--my-value);");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
