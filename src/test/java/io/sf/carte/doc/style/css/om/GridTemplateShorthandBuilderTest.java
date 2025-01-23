/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GridTemplateShorthandBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@BeforeEach
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBuilderNoShorthand() {
		assertShorthandText("grid-row-start:2;", "grid-row-start: 2;");
		assertShorthandText("grid-template-areas:none;grid-template-columns:inherit;grid-template-rows:none;",
				"grid-template-areas:none;grid-template-rows:none;grid-template-columns:inherit;");
		assertShorthandText("grid-template-areas:none;grid-template-columns:revert;grid-template-rows:none;",
				"grid-template-areas:none;grid-template-rows:none;grid-template-columns:revert;");
		assertShorthandText("grid-column-gap:1.4rem;grid-template:repeat(7,auto)/repeat(2,1fr);grid-template-areas:\"section2 cheatSheet\" \"section3 section3\" \"section4 section4\" \"section5 section5\" \"section6 section6\" \"section7 section7\";",
				"grid-column-gap: 1.4rem;grid-template-areas: \"section2 cheatSheet\" \"section3 section3\" \"section4 section4\" \"section5 section5\" \"section6 section6\" \"section7 section7\";grid-template-columns: repeat(2,1fr);grid-template-rows: repeat(7,auto);");
	}

	@Test
	public void testBuilderMix() {
		assertShorthandText("grid-template:none;",
				"grid-template-areas:none;grid-template-rows:none;grid-template-columns:initial;");
		assertShorthandText("grid-template:none;",
				"grid-template-areas:none;grid-template-rows:none;grid-template-columns:unset;");
	}

	@Test
	public void testGridTemplateNone() {
		assertShorthandText("grid-template:none;", "grid-template: none");
		assertShorthandText("grid-template:none;grid-auto-rows:10%;",
				"grid-auto-rows:10%;grid-template: none");
		assertShorthandText("grid-template:none;grid-auto-rows:10%!important;",
				"grid-auto-rows:10%!important;grid-template: none");
	}

	@Test
	public void testGridTemplateNoneAuto() {
		assertShorthandText("grid-template:none/auto;", "grid-template: none / auto");
	}

	@Test
	public void testGridTemplateAutoNone() {
		assertShorthandText("grid-template:auto/none;", "grid-template: auto / none");
	}

	@Test
	public void testGridTemplate() {
		assertShorthandText("grid-template:\"a a a\" \"b b b\";", "grid-template: \"a a a\" \"b b b\"");
	}

	@Test
	public void testGridTemplate2() {
		assertShorthandText("grid-template:\"a a a\" \"b b b\" max-content;",
				"grid-template: \"a a a\" \"b b b\" max-content");
	}

	@Test
	public void testGridTemplate3() {
		assertShorthandText("grid-template:[header-top] 1fr/minmax(2%,1fr);",
				"grid-template: [header-top] 1fr / minmax(2%, 1fr)");
	}

	@Test
	public void testGridTemplate4() {
		assertShorthandText("grid-template:[header-top] repeat(2,1fr)/minmax(2%,1fr);",
				"grid-template: [header-top] repeat(2, 1fr) / minmax(2%, 1fr)");
	}

	@Test
	public void testGridTemplate5() {
		assertShorthandText("grid-template:auto 1fr/auto 1fr auto;", "grid-template: auto 1fr / auto 1fr auto; ");
	}

	@Test
	public void testGridTemplate6() {
		assertShorthandText("grid-template:[header-top] repeat(2,1fr)/minmax(2%,1fr);",
				"grid-template: [header-top] repeat(2, 1fr) / minmax(2%, 1fr); ");
	}

	@Test
	public void testGridTemplate7() {
		assertShorthandText("grid-template:auto-flow 1fr/100px;", "grid-template: auto-flow 1fr / 100px; ");
	}

	@Test
	public void testGridTemplate8() {
		assertShorthandText("grid-template:none/auto-flow 1fr;", "grid-template: none / auto-flow 1fr; ");
	}

	@Test
	public void testGridTemplate9() {
		assertShorthandText("grid-template:auto-flow 300px/repeat(3,[line1 line2 line3] 200px);",
				"grid-template: auto-flow 300px / repeat(3, [line1 line2 line3] 200px); ");
	}

	@Test
	public void testGridTemplate10() {
		assertShorthandText("grid-template:auto-flow dense 40%/[line1] minmax(20em,max-content);",
				"grid-template: auto-flow dense 40% / [line1] minmax(20em, max-content); ");
	}

	@Test
	public void testGridTemplate11() {
		assertShorthandText("grid-template:repeat(3,[line1 line2 line3] 200px)/auto-flow 300px;",
				"grid-template: repeat(3, [line1 line2 line3] 200px) / auto-flow 300px; ");
	}

	@Test
	public void testGridTemplate12() {
		assertShorthandText("grid-template:[line1] minmax(20em,max-content)/auto-flow dense 40%;",
				"grid-template: [line1] minmax(20em, max-content) / auto-flow dense 40%; ");
	}

	@Test
	public void testGridTemplate13() {
		assertShorthandText("grid-template:minmax(400px,min-content)/repeat(auto-fill,50px);",
				"grid-template: minmax(400px, min-content) / repeat(auto-fill, 50px); ");
	}

	@Test
	public void testGridTemplate14() {
		assertShorthandText("grid-template:100px 1fr/50px 1fr;", "grid-template: 100px 1fr / 50px 1fr; ");
	}

	@Test
	public void testGridTemplate15() {
		assertShorthandText("grid-template:auto 1fr/auto 1fr auto;", "grid-template: auto 1fr / auto 1fr auto; ");
	}

	@Test
	public void testGridTemplate16() {
		assertShorthandText("grid-template:[linename] 100px/[columnname1] 30% [columnname2] 70%;",
				"grid-template: [linename] 100px / [columnname1] 30% [columnname2] 70%; ");
	}

	@Test
	public void testGridTemplate17() {
		assertShorthandText("grid-template:fit-content(100px)/fit-content(40%);",
				"grid-template: fit-content(100px) / fit-content(40%); ");
	}

	@Test
	public void testGridTemplate18() {
		assertShorthandText("grid-template:1fr repeat(2,[foo] minmax(2%,1fr))/fit-content(40%);",
				"grid-template: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ");
	}

	@Test
	public void testGridTemplate19() {
		assertShorthandText("grid-template:repeat(1,10px)/auto;", "grid-template: repeat(1, [] 10px) / auto; ");
	}

	@Test
	public void testGridTemplate20() {
		assertShorthandText("grid-template:\"a a a\" max-content \"b b b\" max-content \"c c c\"/1fr 200px;",
				"grid-template:\"a a a\" max-content \"b b b\" max-content \"c c c\" auto /1fr 200px");
	}

	@Test
	public void testGridTemplate21() {
		assertShorthandText("grid-template:\". .\" \"a a a\" minmax(auto,max-content) \"b b b\" max-content \"c c c\" max-content \"d d d\"/auto 7rem;",
				"grid-template:\". .\" auto \"a a a\" minmax(auto, max-content) \"b b b\" max-content \"c c c\" max-content \"d d d\" auto / auto 7rem;");
	}

	@Test
	public void testGridTemplateFromLonghands() {
		assertShorthandText("grid-template:\"media-text-media media-text-content\"/50% auto;",
				"grid-template-rows: auto; grid-template-areas: \"media-text-media media-text-content\"; grid-template-columns: 50% auto;");
	}

	@Test
	public void testGridTemplateFromLonghandsWithLeftoverAutoRows() {
		assertShorthandText(
				"grid-template:\"a b c d\" min-content/min-content 1fr minmax(0,auto) min-content;grid-auto-rows:30px;",
				"grid-auto-rows: 30px;grid-template-areas: \"a b c d\";"
						+ "grid-template-columns: min-content 1fr minmax(0, auto) min-content;"
						+ "grid-template-rows: min-content;");
	}

	@Test
	public void testGridTemplateFromLonghandsWithLeftoverAutoColumns() {
		assertShorthandText(
				"grid-template:\"a b c d\" min-content/min-content 1fr minmax(0,auto) min-content;grid-auto-columns:15%;",
				"grid-auto-columns: 15%;grid-template-areas: \"a b c d\";"
						+ "grid-template-columns: min-content 1fr minmax(0, auto) min-content;"
						+ "grid-template-rows: min-content;");
	}

	@Test
	public void testGridTemplateFromLonghandsWithLeftoverAutoFlow() {
		assertShorthandText(
				"grid-template:\"a b c d\" min-content/min-content 1fr minmax(0,auto) min-content;grid-auto-flow:column;",
				"grid-auto-flow: column;grid-template-areas: \"a b c d\";"
						+ "grid-template-columns: min-content 1fr minmax(0, auto) min-content;"
						+ "grid-template-rows: min-content;");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("grid-template:var(--foo,repeat(1,10px));", "grid-template: var(--foo,repeat(1,10px));");
	}

	@Test
	public void testGridTemplateNoneImportant() {
		assertShorthandText("grid-template:none!important;", "grid-template: none !important");
	}

	@Test
	public void testGridTemplateNoneImportantMix() {
		assertShorthandText("grid-auto-rows:10%;grid-template:none!important;",
				"grid-auto-rows:10%;grid-template: none!important");
	}

	@Test
	public void testGridTemplateImportant() {
		assertShorthandText("grid-template:repeat(1,10px)/auto!important;",
				"grid-template: repeat(1,10px)/auto!important");
	}

	@Test
	public void testGridTemplateImportantMix() {
		assertShorthandText("grid-auto-rows:10%;grid-template:repeat(1,10px)/auto!important;",
				"grid-auto-rows:10%;grid-template: repeat(1,10px)/auto!important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("grid-template:inherit;", "grid-template: inherit;");
		assertShorthandText("grid-template:inherit;grid-auto-rows:10%;",
				"grid-template: inherit;grid-auto-rows:10%;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("grid-template:inherit!important;",
				"grid-template: inherit!important;");
		assertShorthandText("grid-auto-rows:10%;grid-template:inherit!important;",
				"grid-template: inherit!important;grid-auto-rows:10%;");
		assertShorthandText("grid-template:inherit!important;grid-auto-rows:10%!important;",
				"grid-template: inherit!important;grid-auto-rows:10%!important;");
	}

	@Test
	public void testBuilderInitial() {
		assertShorthandText("grid-template:none;", "grid-template: initial;");
	}

	@Test
	public void testBuilderInitialImportant() {
		assertShorthandText("grid-template:none!important;", "grid-template: initial!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("grid-template:none;", "grid-template: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("grid-template:none!important;", "grid-template: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("grid-template:revert;", "grid-template: revert;");
		assertShorthandText("grid-template:revert;grid-auto-rows:10%;",
				"grid-template: revert;grid-auto-rows:10%;");
		assertShorthandText("grid-template:revert;grid-auto-rows:10%!important;",
				"grid-template: revert;grid-auto-rows:10%!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
