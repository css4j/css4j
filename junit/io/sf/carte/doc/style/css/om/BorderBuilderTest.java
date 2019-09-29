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

public class BorderBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBorderNoShorthand() {
		assertShorthandText("border-top-width:0;border-right-width:1px;border-bottom-width:0;",
				"border-top-width: 0; border-right-width: 1px; border-bottom-width: 0; ");
	}

	@Test
	public void testBorderNoShorthandCustomIdent() {
		assertShorthandText(
				"border-bottom-width:0;border-left-width:var(--edge-border-width);border-right-width:var(--edge-border-width);border-top-width:0;",
				"border-top-width: 0; border-right-width: var(--edge-border-width); border-bottom-width: 0; border-left-width: var(--edge-border-width); ");
	}

	@Test
	public void testBorderNoImportantShorthand() {
		assertShorthandText("border:3px;border-bottom-style:solid!important;border-top-style:solid!important;",
				"border: 3px; border-top-style: solid !important; border-bottom-style: solid !important;");
	}

	@Test
	public void testBorderNoImportantShorthand2() {
		assertShorthandText("border-width:3px;border-bottom-style:solid!important;border-top-style:solid!important;",
				"border-width: 3px; border-top-style: solid !important; border-bottom-style: solid !important;");
		assertShorthandText(
				"border-width:3px;border-left-color:yellow!important;border-bottom-style:solid!important;border-top-style:solid!important;",
				"border-width: 3px; border-top-style: solid !important; border-bottom-style: solid !important; border-left-color:yellow!important;");
	}

	@Test
	public void testBorder() {
		assertShorthandText("border:1px dashed blue;", "border: 1px dashed blue; ");
		assertShorthandText("border:1px solid white;color:white;", "border: white solid 1px; color: white; ");
		assertShorthandText("color:white;border:1px solid;", "color: white; border: solid 1px; ");
		assertShorthandText("border-top-color:blue;", "border-top-color: blue; ");
		assertShorthandText("border-top-color:blue;border-image-source:url('foo.png');",
				"border-top-color: blue; border-image-source: url('foo.png')");
		assertShorthandText("border:solid rgb(0 0 0/0);", "border: solid rgb(0 0 0 / 0); ");
	}

	@Test
	public void testBorder2() {
		assertShorthandText("border:solid rgb(0 0 0/0);border-image:url('foo.png');",
				"border: solid rgb(0 0 0 / 0); border-image-source:url('foo.png');");
	}

	@Test
	public void testBorderWTrailingSubproperty() {
		assertShorthandText("border:none;border-top-width:2px;", "border: none; border-top-width: 2px; ");
		assertShorthandText("border:1px dashed blue;border-top-width:2px;",
				"border: 1px dashed blue; border-top-width: 2px; ");
		assertShorthandText("border:1px dashed blue;border-top-style:outset;",
				"border: 1px dashed blue; border-top-style: outset; ");
		assertShorthandText("border:1px dashed blue;border-top-color:yellow;",
				"border: 1px dashed blue; border-top-color: yellow; ");
		assertShorthandText("border:1px dashed blue;border-right-width:2px;",
				"border: 1px dashed blue; border-right-width: 2px; ");
		assertShorthandText("border:1px dashed blue;border-right-style:outset;",
				"border: 1px dashed blue; border-right-style: outset; ");
		assertShorthandText("border:1px dashed blue;border-right-color:yellow;",
				"border: 1px dashed blue; border-right-color: yellow; ");
		assertShorthandText("border:1px dashed blue;border-bottom-width:2px;",
				"border: 1px dashed blue; border-bottom-width: 2px; ");
		assertShorthandText("border:1px dashed blue;border-bottom-style:outset;",
				"border: 1px dashed blue; border-bottom-style: outset; ");
		assertShorthandText("border:1px dashed blue;border-bottom-color:yellow;",
				"border: 1px dashed blue; border-bottom-color: yellow; ");
		assertShorthandText("border:1px dashed blue;border-left-width:2px;",
				"border: 1px dashed blue; border-left-width: 2px; ");
		assertShorthandText("border:1px dashed blue;border-left-style:outset;",
				"border: 1px dashed blue; border-left-style: outset; ");
		assertShorthandText("border:1px dashed blue;border-left-color:yellow;",
				"border: 1px dashed blue; border-left-color: yellow; ");
	}

	@Test
	public void testBorderImportant() {
		assertShorthandText("border:1px dashed blue!important;", "border: 1px dashed blue ! important; ");
	}

	@Test
	public void testBorderPlusBorderSide() {
		assertShorthandText("border:2px solid yellow;",
				"border: 1px dashed blue; border-top: 4px dotted green; border: 2px solid yellow;");
		assertShorthandText("border:1px dashed blue;border-top:4px dotted green;",
				"border: 1px dashed blue; border-top: 4px dotted green;");
		assertShorthandText("border:1px dashed blue;border-right:4px dotted green;",
				"border: 1px dashed blue; border-right: 4px dotted green;");
		assertShorthandText("border:1px dashed blue;border-bottom:4px dotted green;",
				"border: 1px dashed blue; border-bottom: 4px dotted green;");
		assertShorthandText("border:1px dashed blue;border-left:4px dotted green;",
				"border: 1px dashed blue; border-left: 4px dotted green;");
		assertShorthandText("border:2px solid yellow!important;border-top:4px dotted green!important;",
				"border: 2px solid yellow ! important; border-top: 4px dotted green ! important; ");
		assertShorthandText("border:2px solid yellow!important;border-right:4px dotted green!important;",
				"border: 2px solid yellow ! important; border-right: 4px dotted green ! important; ");
		assertShorthandText("border:2px solid yellow!important;border-bottom:4px dotted green!important;",
				"border: 2px solid yellow ! important; border-bottom: 4px dotted green ! important; ");
		assertShorthandText("border:2px solid yellow!important;border-left:4px dotted green!important;",
				"border: 2px solid yellow ! important; border-left: 4px dotted green ! important; ");
	}

	@Test
	public void testBorderPlusBorderSide2() {
		assertShorthandText("border:1px dashed blue;border-top:4px dotted green;border-right:3px solid yellow;",
				"border: 1px dashed blue; border-right: 3px solid yellow; border-top: 4px dotted green;");
		assertShorthandText(
				"border:1px dashed blue;border-top:4px dotted green;border-right:3px solid yellow;border-image:url('foo.png');",
				"border: 1px dashed blue; border-right: 3px solid yellow; border-top: 4px dotted green;border-image-source:url('foo.png');");
		assertShorthandText("border:1px dashed blue;border-top:4px dotted green;border-bottom:3px solid yellow;",
				"border: 1px dashed blue; border-bottom: 3px solid yellow; border-top: 4px dotted green;");
		assertShorthandText(
				"border:1px dashed blue;border-top:4px dotted green;border-bottom:3px solid yellow;border-image:url('foo.png');",
				"border: 1px dashed blue; border-bottom: 3px solid yellow; border-top: 4px dotted green;border-image:url('foo.png');");
		assertShorthandText("border:1px dashed blue;border-right:4px dotted green;border-left:3px solid yellow;",
				"border: 1px dashed blue; border-left: 3px solid yellow; border-right: 4px dotted green;");
		assertShorthandText(
				"border:1px dashed blue;border-right:4px dotted green;border-left:3px solid yellow;border-image:url('foo.png');",
				"border: 1px dashed blue; border-left: 3px solid yellow; border-right: 4px dotted green;border-image:url('foo.png');");
		assertShorthandText("border:1px dashed blue;border-right:4px dotted green;border-bottom:3px solid yellow;",
				"border: 1px dashed blue; border-bottom: 3px solid yellow; border-right: 4px dotted green;");
		assertShorthandText(
				"border:1px dashed blue;border-right:4px dotted green;border-bottom:3px solid yellow;border-image:url('foo.png');",
				"border: 1px dashed blue; border-bottom: 3px solid yellow; border-right: 4px dotted green;border-image:url('foo.png');");
		assertShorthandText("border:1px dashed blue;border-bottom:3px solid yellow;border-left:4px dotted green;",
				"border: 1px dashed blue; border-bottom: 3px solid yellow; border-left: 4px dotted green;");
		assertShorthandText(
				"border:1px dashed blue;border-bottom:3px solid yellow;border-left:4px dotted green;border-image:url('foo.png');",
				"border: 1px dashed blue; border-bottom: 3px solid yellow; border-left: 4px dotted green;border-image:url('foo.png');");
	}

	@Test
	public void testBorderPlusBorderSideImportant() {
		assertShorthandText("border:2px solid yellow;border-top:4px dotted green!important;",
				"border: 2px solid yellow; border-top: 4px dotted green ! important; ");
		assertShorthandText("border:2px solid yellow;border-right:4px dotted green!important;",
				"border: 2px solid yellow; border-right: 4px dotted green ! important; ");
		assertShorthandText(
				"border:2px solid yellow;border-image:url('foo.png');border-right:4px dotted green!important;",
				"border: 2px solid yellow; border-right: 4px dotted green ! important; border-image:url('foo.png');");
		assertShorthandText("border:2px solid yellow;border-bottom:4px dotted green!important;",
				"border: 2px solid yellow; border-bottom: 4px dotted green ! important; ");
		assertShorthandText(
				"border:2px solid yellow;border-image:url('foo.png');border-bottom:4px dotted green!important;",
				"border: 2px solid yellow; border-bottom: 4px dotted green ! important; border-image-source:url('foo.png');");
		assertShorthandText("border:2px solid yellow;border-left:4px dotted green!important;",
				"border: 2px solid yellow; border-left: 4px dotted green ! important; ");
		assertShorthandText("border:2px solid yellow;border-top:4px green!important;",
				"border: 2px solid yellow; border-top: 4px green ! important; ");
		assertShorthandText("border:2px yellow;border-top:4px dotted green!important;",
				"border: 2px yellow; border-top: 4px dotted green ! important; ");
	}

	@Test
	public void testBorder0() {
		assertShorthandText("border:0;", "border: 0");
	}

	@Test
	public void testBorder0Important() {
		assertShorthandText("border:0!important;", "border: 0 !important");
	}

	@Test
	public void testBorder3() {
		assertShorthandText("border:solid;", "border: solid");
	}

	@Test
	public void testBorder4() {
		assertShorthandText("border:solid;border-width:1px 2px;border-color:blue navy;",
				"border: solid; border-color: blue navy; border-width: 1px 2px;");
	}

	@Test
	public void testBorder5() {
		assertShorthandText("border:solid;border-width:1px 2px 3px 4px;border-color:blue navy yellow;",
				"border: solid; border-color: blue navy yellow; border-width: 1px 2px 3px 4px;");
	}

	@Test
	public void testBorder6() {
		assertShorthandText("border:solid;border-width:1px 2px 3px 4px;border-color:blue navy yellow red;",
				"border: solid; border-color: blue navy yellow red; border-width: 1px 2px 3px 4px;");
	}

	@Test
	public void testBorder7() {
		assertShorthandText("border:0;border-bottom-width:4px;border-top-style:solid;border-left-color:blue;",
				"border: 0;border-top-style: solid; border-left-color:blue;border-bottom-width:4px;");
	}

	@Test
	public void testBorderMixed() {
		assertShorthandText("border:1px;border-style:unset;border-color:inherit;",
				"border: 1px; border-color: inherit; border-style: unset;");
		assertShorthandText("border:1px;border-style:unset;border-color:inherit!important;",
				"border: 1px; border-color: inherit !important; border-style: unset;");
	}

	@Test
	public void testBorderMixed2() {
		assertShorthandText("border:solid;border-width:unset;border-color:inherit;",
				"border: solid; border-color: inherit; border-width: unset;");
		assertShorthandText("border:solid;border-color:inherit;border-width:unset!important;",
				"border: solid; border-color: inherit; border-width: unset !important;");
	}

	@Test
	public void testBorderMixed3() {
		assertShorthandText("border:solid;border-width:unset;border-color:blue navy;",
				"border: solid; border-color: blue navy; border-width: unset;");
		assertShorthandText("border:solid;border-color:blue navy;border-width:unset!important;",
				"border: solid; border-color: blue navy; border-width: unset!important;");
	}

	@Test
	public void testBorderMixedWithImportant() {
		assertShorthandText("border:solid;border-color:blue navy;border-width:inherit!important;",
				"border: solid; border-color: blue navy; border-width: inherit ! important;");
	}

	@Test
	public void testBorderInherit() {
		assertShorthandText("border:inherit;", "border: inherit");
	}

	@Test
	public void testBorderInheritImportant() {
		assertShorthandText("border:inherit!important;", "border: inherit !important");
	}

	@Test
	public void testBorderInherit2() {
		assertShorthandText("border:inherit;border-color:yellow;", "border: inherit; border-color:yellow;");
	}

	@Test
	public void testBorderInheritPlusBorderColor() {
		assertShorthandText("border:inherit;border-color:yellow;",
				"border-width: inherit; border-style: inherit; border-color:yellow; border-image: initial");
	}

	@Test
	public void testBorderInheritPlusBorderColor2() {
		assertShorthandText("border:inherit;border-color:#fff transparent;",
				"border: inherit; border-color: #fff transparent;");
	}

	@Test
	public void testBorderInheritPlusBorderColor3() {
		assertShorthandText("border:inherit;border-color:#fff transparent transparent;",
				"border: inherit; border-color: #fff transparent transparent;");
	}

	@Test
	public void testBorderInheritMix2() {
		assertShorthandText("border-right:inherit;border-left:1px solid #c8c8f0;",
				"border-left: 1px solid #c8c8f0; border-right: inherit;");
	}

	@Test
	public void testBorderUnset() {
		assertShorthandText("border:unset;", "border: unset");
	}

	@Test
	public void testBorderUnsetImportant() {
		assertShorthandText("border:unset!important;", "border: unset !important");
	}

	@Test
	public void testBorderTop() {
		assertShorthandText("border-top:1px;", "border-top-style: inset; border-top: 1px; ");
		assertShorthandText("border-top:1px dashed;", "border-top: 1px dashed;");
		assertShorthandText("border-top:1px dashed yellow;", "border-top: 1px dashed yellow; ");
		assertShorthandText("border-top:0;border-left-width:2px;", "border-left-width: 2px; border-top: 0");
		assertShorthandText("border-top:0;border-left-color:blue;border-left-width:2px;",
				"border-left-width: 2px; border-top: 0;border-left-color:blue;");
		assertShorthandText("border-top:0;border-bottom-width:4px;border-left-color:blue;border-left-width:2px;",
				"border-left-width: 2px; border-top: 0;border-left-color:blue;border-bottom-width:4px;");
	}

	@Test
	public void testBorderWidth() {
		assertShorthandText("border-width:thick;", "border-top-width: 5px; border-width: thick; ");
		assertShorthandText("border-width:0;", "border-width: 0; ");
		assertShorthandText("border-width:medium;", "border-width: medium; ");
		assertShorthandText("border-width:2px;", "border-width: 2px; ");
		assertShorthandText("border-width:2px 8em;", "border-width: 2px 8em; ");
		assertShorthandText("border-width:2px thick;", "border-width: 2px thick; ");
		assertShorthandText("border-width:2px 8em 4pt;", "border-width: 2px 8em 4pt; ");
		assertShorthandText("border-width:2px 8em thick;", "border-width: 2px 8em thick; ");
		assertShorthandText("border-width:2px 8em 4pt 5px;", "border-width: 2px 8em 4pt 5px; ");
		assertShorthandText("border-width:1px 0 0;", "border-width: 1px 0 0 0; ");
		assertShorthandText("border-width:0 1px 0 0;", "border-width: 0 1px 0 0; ");
		assertShorthandText("border-width:0 0 1px;", "border-width: 0 0 1px 0; ");
		assertShorthandText("border-width:0 0 0 1px;", "border-width: 0 0 0 1px; ");
		assertShorthandText("border-image-source:url('foo.png');border-width:2px thick;",
				"border-width: 2px thick; border-image-source:url('foo.png');");
	}

	@Test
	public void testBorderStyle() {
		assertShorthandText("border-style:none;", "border-style: none; ");
		assertShorthandText("border-style:inset;", "border-top-style: dotted; border-style: inset; ");
		assertShorthandText("border-style:inset solid;", "border-style: inset solid; ");
		assertShorthandText("border-style:inset solid dotted;", "border-style: inset solid dotted; ");
		assertShorthandText("border-style:none solid dotted;", "border-style: none solid dotted; ");
		assertShorthandText("border-style:inset none dotted;", "border-style: inset none dotted; ");
		assertShorthandText("border-style:inset solid none;", "border-style: inset solid none; ");
		assertShorthandText("border-style:inset solid dotted outset;", "border-style: inset solid dotted outset; ");
		assertShorthandText("border-style:none solid dotted outset;", "border-style: none solid dotted outset; ");
		assertShorthandText("border-style:inset none dotted outset;", "border-style: inset none dotted outset; ");
		assertShorthandText("border-style:inset solid none outset;", "border-style: inset solid none outset; ");
		assertShorthandText("border-style:inset solid dotted none;", "border-style: inset solid dotted none; ");
	}

	@Test
	public void testBorderColor() {
		assertShorthandText("border-color:blue;", "border-top-color: yellow; border-color: blue; ");
		assertShorthandText("border-color:blue navy;", "border-color: blue navy; ");
		assertShorthandText("border-color:blue navy green;", "border-color: blue navy green; ");
		assertShorthandText("border-color:navy navy green;", "border-color: navy navy green; ");
		assertShorthandText("border-color:blue navy green yellow;", "border-color: blue navy green yellow; ");
		assertShorthandText("border-color:currentcolor;", "border-color: currentcolor; ");
	}

	@Test
	public void testBorderColor2() {
		assertShorthandText("border-color:blue navy green transparent;", "border-color: blue navy green transparent; ");
		assertShorthandText("border-color:blue #11bbfc green;", "border-color: blue #11bbfc green; ");
		assertShorthandText("border-color:blue navy green transparent;", "border-color: blue navy green transparent; ");
	}

	@Test
	public void testBorderStyleColor() {
		assertShorthandText("border-color:blue;border-style:solid;border-left-color:inherit;",
				"border-color: blue; border-style: solid; border-left-color: inherit;");
	}

	@Test
	public void testBorderWidthStyleColor() {
		assertShorthandText("border-width:inherit;border-style:solid;border-color:blue;",
				"border-width:inherit; border-color: blue; border-style: solid;");
	}

	@Test
	public void testBorderWidthStyleColorImageSource() {
		assertShorthandText(
				"border-width:inherit;border-style:solid;border-color:blue;border-image-source:url('foo.png');",
				"border-width:inherit; border-color: blue; border-style: solid;border-image-source:url('foo.png');");
		assertShorthandText("border:2px solid blue;border-image:url('foo.png');",
				"border-width:2px; border-color: blue; border-style: solid;border-image:url('foo.png');");
		assertShorthandText("border:solid blue;border-width:inherit;border-image:url('foo.png');",
				"border-width:inherit; border-color: blue; border-style: solid;border-image:url('foo.png');");
	}

	@Test
	public void testBorderColorInherit() {
		assertShorthandText("border-color:inherit;border-top-width:1px;border-top-style:solid;",
				"border-top: 1px solid; border-color: inherit;");
	}

	@Test
	public void testBorderColorInherit2() {
		assertShorthandText("border-color:inherit;", "border-top-color: blue; border-color: inherit; ");
		assertShorthandText("border-color:red blue;border-style:inherit;",
				"border-color: red blue; border-style: inherit; ");
	}

	@Test
	public void testBorderColorPlusSide() {
		// 1:t
		assertShorthandText("border-color:navy;border-top:0;", "border-color: navy; border-top: 0; ");
		// 1:r
		assertShorthandText("border-color:navy;border-right:0;", "border-color: navy; border-right: 0; ");
		// 1:b
		assertShorthandText("border-color:navy;border-bottom:0;", "border-color: navy; border-bottom: 0; ");
		// 1:l
		assertShorthandText("border-color:navy;border-left:0;", "border-color: navy; border-left: 0; ");
		// 1:l
		assertShorthandText("border-color:navy;border-left:blue;", "border-color: navy; border-left: blue; ");
		// 1:l
		assertShorthandText("border-color:navy;border-left:0 yellow;", "border-color: navy; border-left: 0 yellow;");
		// 1:tb
		assertShorthandText("border-color:navy;border-top:0;border-bottom:0;",
				"border-color: navy; border-top: 0; border-bottom: 0;");
		// 1:tr
		assertShorthandText("border-color:navy;border-top:0;border-right:0;",
				"border-color: navy; border-top: 0; border-right: 0;");
		// 1:tl
		assertShorthandText("border-color:navy;border-top:0;border-left:0;",
				"border-color: navy; border-top: 0; border-left: 0;");
		// 1:br
		assertShorthandText("border-color:navy;border-right:0;border-bottom:0;",
				"border-color: navy; border-bottom: 0; border-right: 0;");
		// 1:bl
		assertShorthandText("border-color:navy;border-bottom:0;border-left:0;",
				"border-color: navy; border-bottom: 0; border-left: 0;");
		// 1:lr
		assertShorthandText("border-color:navy;border-right:0;border-left:0;",
				"border-color: navy; border-left: 0; border-right: 0;");
		// 2:tr
		assertShorthandText("border-color:navy green;border-top:0;border-right:0;",
				"border-color: navy green; border-top: 0; border-right: 0;");
		// 2:tb
		assertShorthandText("border-color:green;border-top:0;border-bottom:0;",
				"border-color: navy green; border-bottom: 0; border-top: 0;");
		// 2:r
		assertShorthandText("border-color:navy yellow;border-right:0;", "border-color: navy yellow; border-right: 0;");
		// 2:l
		assertShorthandText("border-color:navy yellow;border-left:0;", "border-color: navy yellow; border-left: 0;");
		// 2:l
		assertShorthandText("border-color:yellow navy;border-left:0 yellow;",
				"border-color: yellow navy; border-left: 0 yellow;");
		// 2:rl
		assertShorthandText("border-color:navy;border-right:0;border-left:0;",
				"border-color: navy yellow; border-right: 0; border-left: 0;");
		// 3:tb
		assertShorthandText("border-color:green;border-top:0;border-bottom:0;",
				"border-color: navy green yellow; border-bottom: 0; border-top: 0;");
		// 3:t b
		assertShorthandText("border-color:green;border-top:0;border-bottom:0 black;",
				"border-color: navy green yellow; border-bottom: 0 black; border-top: 0;");
		// 3:t
		assertShorthandText("border-color:yellow green;border-top:0;",
				"border-color: navy green yellow; border-top: 0;");
		// 3:r
		assertShorthandText("border-color:navy green yellow;border-right:0;",
				"border-color: navy green yellow; border-right: 0;");
		// 3:b
		assertShorthandText("border-color:navy green;border-bottom:0;",
				"border-color: navy green yellow; border-bottom: 0;");
		// 3:l
		assertShorthandText("border-color:navy green yellow;border-left:0;",
				"border-color: navy green yellow; border-left: 0;");
		// 3(t=l):l
		assertShorthandText("border-color:navy green yellow;border-left:0 navy;",
				"border-color: navy green yellow; border-left: 0 navy;");
		// 3(t=l,b=r):l
		assertShorthandText("border-color:navy green green;border-left:0 navy;",
				"border-color: navy green green; border-left: 0 navy;");
		// 3(t=l,b=r):r
		assertShorthandText("border-color:green green navy;border-right:0 navy;",
				"border-color: green green navy; border-right: 0 navy;");
		// 3(b=l):r
		assertShorthandText("border-color:blue green green;border-right:0 navy;",
				"border-color: blue green green; border-right: 0 navy;");
		// 3(t=r=l):b
		assertShorthandText("border-color:navy;border-bottom:0;", "border-color: navy navy yellow; border-bottom: 0;");
		// 3(r=b=l):t
		assertShorthandText("border-color:green;border-top:0;", "border-color: navy green green; border-top: 0;");
		// 4:bl
		assertShorthandText("border-color:navy green;border-bottom:0;border-left:0;",
				"border-color: navy green yellow black; border-bottom: 0; border-left: 0;");
		// 4(t=r):bl
		assertShorthandText("border-color:navy;border-bottom:0;border-left:0;",
				"border-color: navy navy yellow black; border-bottom: 0; border-left: 0;");
		// 4(t=r):l
		assertShorthandText("border-color:navy navy yellow;border-left:0;",
				"border-color: navy navy yellow black; border-left: 0;");
		// 4:r
		assertShorthandText("border-color:navy blue black;border-right:0;",
				"border-color: navy yellow black blue; border-right: 0;");
		// 4(t=b=l):r
		assertShorthandText("border-color:navy;border-right:0;",
				"border-color: navy yellow navy navy; border-right: 0;");
		// 4(t=b):rl
		assertShorthandText("border-color:navy;border-right:0;border-left:0;",
				"border-color: navy yellow navy black; border-right: 0; border-left: 0;");
		// 4(t=b):l r
		assertShorthandText("border-color:navy;border-right:0 blue;border-left:0;",
				"border-color: navy yellow navy black; border-right: 0 blue; border-left: 0;");
		// 4(t=b):l
		assertShorthandText("border-color:navy yellow;border-left:0;",
				"border-color: navy yellow navy black; border-left: 0;");
		// 4(t=b):r
		assertShorthandText("border-color:navy black;border-right:0;",
				"border-color: navy yellow navy black; border-right: 0;");
		// 4(t=l):rb
		assertShorthandText("border-color:navy;border-right:0;border-bottom:0;",
				"border-color: navy yellow black navy; border-right: 0; border-bottom: 0;");
		// 4(t=l):r
		assertShorthandText("border-color:navy navy black;border-right:0;",
				"border-color: navy yellow black navy; border-right: 0;");
		// 4(t=l):rl
		assertShorthandText("border-color:navy currentcolor black;border-right:0;border-left:0;",
				"border-color: navy yellow black navy; border-right: 0; border-left: 0;");
		// 4:l
		assertShorthandText("border-color:navy yellow black;border-left:0;",
				"border-color: navy yellow black blue; border-left: 0;");
		// 4:tr
		assertShorthandText("border-color:black blue;border-top:0;border-right:0;",
				"border-color: navy yellow black blue; border-top: 0; border-right: 0;");
		// 4:r t
		assertShorthandText("border-color:black blue;border-top:0 green;border-right:0;",
				"border-color: navy yellow black blue; border-top: 0 green; border-right: 0;");
		// 4:b l
		assertShorthandText("border-color:navy yellow;border-bottom:0 green;border-left:0;",
				"border-color: navy yellow black blue; border-bottom: 0 green; border-left: 0;");
		// 1(d):tr
		assertShorthandText("border-color:currentcolor;border-top:0;border-right:0;",
				"border-color: currentcolor; border-top: 0; border-right: 0;");
		// 2( d):t r
		assertShorthandText("border-color:navy currentcolor;border-top:0;border-right:0 black;",
				"border-color: navy currentcolor; border-top: 0; border-right: 0 black;");
		// 2(d ):r t
		assertShorthandText("border-color:currentcolor navy;border-top:0 black;border-right:0;",
				"border-color: currentcolor navy; border-top: 0 black; border-right: 0;");
		// 2( d): r
		assertShorthandText("border-color:navy currentcolor;border-right:0 black;",
				"border-color: navy currentcolor; border-right: 0 black;");
	}

	@Test
	public void testBorderColorPlusSideImportant() {
		// 1:t
		assertShorthandText("border-color:navy;border-top:0!important;",
				"border-color: navy; border-top: 0!important; ");
		// 1:r
		assertShorthandText("border-color:navy;border-right:0!important;",
				"border-color: navy; border-right: 0!important; ");
		// 1:b
		assertShorthandText("border-color:navy;border-bottom:0!important;",
				"border-color: navy; border-bottom: 0!important; ");
		// 1:l
		assertShorthandText("border-color:navy;border-left:0!important;",
				"border-color: navy; border-left: 0!important; ");
		// 1:tb
		assertShorthandText("border-color:navy;border-top:0!important;border-bottom:0!important;",
				"border-color: navy; border-top: 0!important; border-bottom: 0!important;");
		// 1:tr
		assertShorthandText("border-color:navy;border-top:0!important;border-right:0!important;",
				"border-color: navy; border-top: 0!important; border-right: 0!important;");
		// 1:tl
		assertShorthandText("border-color:navy;border-top:0!important;border-left:0!important;",
				"border-color: navy; border-top: 0!important; border-left: 0!important;");
		// 1:br
		assertShorthandText("border-color:navy;border-right:0!important;border-bottom:0!important;",
				"border-color: navy; border-bottom: 0!important; border-right: 0!important;");
		// 1:bl
		assertShorthandText("border-color:navy;border-bottom:0!important;border-left:0!important;",
				"border-color: navy; border-bottom: 0!important; border-left: 0!important;");
		// 1:lr
		assertShorthandText("border-color:navy;border-right:0!important;border-left:0!important;",
				"border-color: navy; border-left: 0!important; border-right: 0!important;");
		// 2:tr
		assertShorthandText("border-color:navy green;border-top:0!important;border-right:0!important;",
				"border-color: navy green; border-top: 0!important; border-right: 0!important;");
		// 2:tb
		assertShorthandText("border-color:green;border-top:0!important;border-bottom:0!important;",
				"border-color: navy green; border-bottom: 0!important; border-top: 0!important;");
		// 2:r
		assertShorthandText("border-color:navy yellow;border-right:0!important;",
				"border-color: navy yellow; border-right: 0!important;");
		// 2:l
		assertShorthandText("border-color:navy yellow;border-left:0!important;",
				"border-color: navy yellow; border-left: 0!important;");
		// 2:rl
		assertShorthandText("border-color:navy;border-right:0!important;border-left:0!important;",
				"border-color: navy yellow; border-right: 0!important; border-left: 0!important;");
		// 3:tb
		assertShorthandText("border-color:green;border-top:0!important;border-bottom:0!important;",
				"border-color: navy green yellow; border-bottom: 0!important; border-top: 0!important;");
		// 3:t b
		assertShorthandText("border-color:green;border-top:0!important;border-bottom:0 black!important;",
				"border-color: navy green yellow; border-bottom: 0 black!important; border-top: 0!important;");
		// 3:t
		assertShorthandText("border-color:yellow green;border-top:0!important;",
				"border-color: navy green yellow; border-top: 0!important;");
		// 3:b
		assertShorthandText("border-color:navy green;border-bottom:0!important;",
				"border-color: navy green yellow; border-bottom: 0!important;");
		// 3(t=r=l):b
		assertShorthandText("border-color:navy;border-bottom:0!important;",
				"border-color: navy navy yellow; border-bottom: 0!important;");
		// 3(r=b=l):t
		assertShorthandText("border-color:green;border-top:0!important;",
				"border-color: navy green green; border-top: 0!important;");
		// 4:bl
		assertShorthandText("border-color:navy green;border-bottom:0!important;border-left:0!important;",
				"border-color: navy green yellow black; border-bottom: 0!important; border-left: 0!important;");
		// 4(t=r):bl
		assertShorthandText("border-color:navy;border-bottom:0!important;border-left:0!important;",
				"border-color: navy navy yellow black; border-bottom: 0!important; border-left: 0!important;");
		// 4(t=r):l
		assertShorthandText("border-color:navy navy yellow;border-left:0!important;",
				"border-color: navy navy yellow black; border-left: 0!important;");
		// 4:r
		assertShorthandText("border-color:navy blue black;border-right:0!important;",
				"border-color: navy yellow black blue; border-right: 0!important;");
		// 4(t=b=l):r
		assertShorthandText("border-color:navy;border-right:0!important;",
				"border-color: navy yellow navy navy; border-right: 0!important;");
		// 4(t=b):rl
		assertShorthandText("border-color:navy;border-right:0!important;border-left:0!important;",
				"border-color: navy yellow navy black; border-right: 0!important; border-left: 0!important;");
		// 4(t=b):l r
		assertShorthandText("border-color:navy;border-right:0 blue!important;border-left:0!important;",
				"border-color: navy yellow navy black; border-right: 0 blue!important; border-left: 0!important;");
		// 4(t=b):l
		assertShorthandText("border-color:navy yellow;border-left:0!important;",
				"border-color: navy yellow navy black; border-left: 0!important;");
		// 4(t=b):r
		assertShorthandText("border-color:navy black;border-right:0!important;",
				"border-color: navy yellow navy black; border-right: 0!important;");
		// 4(t=l):rb
		assertShorthandText("border-color:navy;border-right:0!important;border-bottom:0!important;",
				"border-color: navy yellow black navy; border-right: 0!important; border-bottom: 0!important;");
		// 4(t=l):r
		assertShorthandText("border-color:navy navy black;border-right:0!important;",
				"border-color: navy yellow black navy; border-right: 0!important;");
		// 4(t=l):rl
		assertShorthandText("border-color:navy currentcolor black;border-right:0!important;border-left:0!important;",
				"border-color: navy yellow black navy; border-right: 0!important; border-left: 0!important;");
		// 4:l
		assertShorthandText("border-color:navy yellow black;border-left:0!important;",
				"border-color: navy yellow black blue; border-left: 0!important;");
		// 4:tr
		assertShorthandText("border-color:black blue;border-top:0!important;border-right:0!important;",
				"border-color: navy yellow black blue; border-top: 0!important; border-right: 0!important;");
		// 4:r t
		assertShorthandText("border-color:black blue;border-top:0 green!important;border-right:0!important;",
				"border-color: navy yellow black blue; border-top: 0 green!important; border-right: 0!important;");
		// 1(d):tr
		assertShorthandText("border-color:currentcolor;border-top:0!important;border-right:0!important;",
				"border-color: currentcolor; border-top: 0!important; border-right: 0!important;");
		// 2( d):t r
		assertShorthandText("border-color:navy currentcolor;border-top:0!important;border-right:0 black!important;",
				"border-color: navy currentcolor; border-top: 0!important; border-right: 0 black!important;");
		// 2(d ):r t
		assertShorthandText("border-color:currentcolor navy;border-top:0 black!important;border-right:0!important;",
				"border-color: currentcolor navy; border-top: 0 black!important; border-right: 0!important;");
		// 2( d): r
		assertShorthandText("border-color:navy currentcolor;border-right:0 black!important;",
				"border-color: navy currentcolor; border-right: 0 black!important;");
	}

	@Test
	public void testBorderWidthCombined() {
		assertShorthandText("border:inset;border-width:2px 3px;", "border: inset; border-width: 2px 3px; ");
		assertShorthandText("border:inset;border-width:2px 3px!important;",
				"border: inset; border-width: 2px 3px ! important; ");
	}

	@Test
	public void testBorderStyleCombined() {
		assertShorthandText("border:1px;border-style:inset solid;", "border: 1px; border-style: inset solid; ");
		assertShorthandText("border:1px;border-style:inset solid!important;",
				"border: 1px; border-style: inset solid ! important; ");
	}

	@Test
	public void testBorderColorCombined() {
		assertShorthandText("border:1px;border-color:yellow blue;", "border: 1px; border-color: yellow blue; ");
		assertShorthandText("border:1px;border-color:yellow blue!important;",
				"border: 1px; border-color: yellow blue ! important; ");
	}

	@Test
	public void testBorderColorWidthCombined() {
		assertShorthandText("border:3px solid;border-bottom-width:0;border-color:black transparent;",
				"border: solid; border-color: black transparent; border-width: 3px 3px 0 3px; ");
	}

	@Test
	public void testBorderColorWidthCombinedImportantMix() {
		assertShorthandText("border:solid;border-color:black transparent;border-width:3px 3px 0!important;",
				"border: solid; border-color: black transparent; border-width: 3px 3px 0 3px!important; ");
	}

	@Test
	public void testBorderColorWidthCombinedImportantMixImageSource() {
		assertShorthandText(
				"border:solid;border-color:black transparent;border-image:url('foo.png');border-width:3px 3px 0!important;",
				"border: solid; border-color: black transparent; border-width: 3px 3px 0 3px!important; border-image-source:url('foo.png');");
	}

	@Test
	public void testBorderColorCombinedWithSide() {
		assertShorthandText("border-color:yellow;border-top:1px solid yellow;",
				"border-top: 1px solid; border-color: yellow; ");
	}

	@Test
	public void testBorderColorImportantCombinedWithSide() {
		assertShorthandText("border-top:1px solid;border-color:yellow!important;",
				"border-top: 1px solid; border-color: yellow ! important; ");
	}

	@Test
	public void testBorderCombinedWithColorImportant() {
		assertShorthandText("border:1px solid;border-color:yellow!important;",
				"border: 1px solid; border-color: yellow ! important; ");
	}

	@Test
	public void testBorderColorCombinedWithSideMixed() {
		assertShorthandText("border-color:inherit;border-top-width:1px;border-top-style:solid;",
				"border-top: 1px solid; border-color: inherit; ");
	}

	@Test
	public void testBorderColorCombinedWithSideMixed2() {
		assertShorthandText("border-color:yellow;border-top-width:inherit;border-top-style:inherit;",
				"border-top: inherit; border-color: yellow; ");
	}

	@Test
	public void testBorderWidthCombined2() {
		assertShorthandText("border:inset;border-width:1px 2px 3px;", "border: inset; border-width: 1px 2px 3px; ");
		assertShorthandText("border:inset;border-width:1px 2px 3px!important;",
				"border: inset; border-width: 1px 2px 3px ! important; ");
	}

	@Test
	public void testBorderStyleCombined2() {
		assertShorthandText("border:1px;border-style:inset solid outset;",
				"border: 1px; border-style: inset solid outset; ");
		assertShorthandText("border:1px;border-style:inset solid outset!important;",
				"border: 1px; border-style: inset solid outset ! important; ");
	}

	@Test
	public void testBorderColorCombined2() {
		assertShorthandText("border:1px;border-color:yellow blue red;", "border: 1px; border-color: yellow blue red; ");
		assertShorthandText("border:1px;border-color:yellow blue red!important;",
				"border: 1px; border-color: yellow blue red ! important; ");
	}

	@Test
	public void testBorderWidthCombined3() {
		assertShorthandText("border:inset;border-width:1px 2px 3px 4px;",
				"border: inset; border-width: 1px 2px 3px 4px; ");
		assertShorthandText("border:inset;border-width:1px 2px 3px 4px!important;",
				"border: inset; border-width: 1px 2px 3px 4px ! important; ");
	}

	@Test
	public void testBorderStyleCombined3() {
		assertShorthandText("border:1px;border-style:inset solid outset none;",
				"border: 1px; border-style: inset solid outset none; ");
		assertShorthandText("border:1px;border-style:inset solid outset none!important;",
				"border: 1px; border-style: inset solid outset none ! important; ");
	}

	@Test
	public void testBorderColorCombined3() {
		assertShorthandText("border:1px;border-color:yellow blue red navy;",
				"border: 1px; border-color: yellow blue red navy; ");
		assertShorthandText("border:1px;border-color:yellow blue red navy!important;",
				"border: 1px; border-color: yellow blue red navy ! important; ");
	}

	@Test
	public void testBorderWidthCombined4() {
		assertShorthandText("border:inset;border-width:1px 2px 1px 4px;",
				"border: inset; border-width: 1px 2px 1px 4px; ");
		assertShorthandText("border:inset;border-width:1px 2px 1px 4px!important;",
				"border: inset; border-width: 1px 2px 1px 4px ! important; ");
	}

	@Test
	public void testBorderStyleCombined4() {
		assertShorthandText("border:1px;border-style:inset solid inset none;",
				"border: 1px; border-style: inset solid inset none; ");
		assertShorthandText("border:1px;border-style:inset solid inset none!important;",
				"border: 1px; border-style: inset solid inset none ! important; ");
	}

	@Test
	public void testBorderColorCombined4() {
		assertShorthandText("border:1px;border-color:red blue red navy;",
				"border: 1px; border-color: red blue red navy; ");
		assertShorthandText("border:1px;border-color:red blue red navy!important;",
				"border: 1px; border-color: red blue red navy ! important; ");
	}

	@Test
	public void testBorderPartialShorthands() {
		assertShorthandText(
				"border-width:4px;border-style:solid;border-color:inherit;border-bottom-color:transparent!important;",
				"border-width: 4px; border-style: solid; border-color: inherit; border-bottom-color: transparent ! important; ");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
