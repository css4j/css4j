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

public class TransitionShorthandBuilderTest {

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
		assertShorthandText("transition-property:foo;", "transition-property: foo;");
		// Nobody uses IE hacks for transitions, but the detection code is shared
		assertShorthandText(
				"transition-delay:1s;transition-duration:3200ms;transition-property:foo\\9;",
				"transition-duration: 3200ms;transition-delay:1s;transition-property:foo\\9;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("transition:0s;", "transition: initial;");
		assertShorthandText("transition:none;", "transition: none;");
		assertShorthandText("transition:none 3500ms 5s;", "transition: 3500ms 5s none");
		assertShorthandText("transition:none 0s steps(4,end) 5s;",
				"transition-duration: 0s; transition-timing-function: steps(4,end); transition-delay: 5s; transition-property: none;");
		assertShorthandText("transition:margin-top 3500ms cubic-bezier(.05,.69,.95,.6) 5s;",
				"transition: 3500ms 5s cubic-bezier(.05,.69,.95,.6) margin-top");
		assertShorthandText("transition:ease-in-out;", "transition: ease-in-out;");
		assertShorthandText("transition:linear;", "transition: linear all;");
		assertShorthandText("transition:none 3s linear;", "transition: 3s none linear;");
		assertShorthandText("transition:3s ease-in 1s;", "transition: 3s ease-in 1s;");
	}

	@Test
	public void testBuilderMix() {
		assertShorthandText(
				"transition-delay:inherit;transition-duration:3200ms;transition-property:foo;transition-timing-function:ease-in;",
				"transition-duration: 3200ms;transition-delay:inherit;transition-timing-function:ease-in;transition-property:foo;");
		assertShorthandText(
				"transition-delay:revert;transition-duration:3200ms;transition-property:foo;transition-timing-function:ease-in;",
				"transition-duration: 3200ms;transition-delay:revert;transition-timing-function:ease-in;transition-property:foo;");
		assertShorthandText("transition:foo 3200ms ease-in;",
				"transition-duration: 3200ms;transition-delay:initial;transition-timing-function:ease-in;transition-property:foo;");
		assertShorthandText("transition:foo 3200ms ease-in;",
				"transition-duration: 3200ms;transition-delay:unset;transition-timing-function:ease-in;transition-property:foo;");
	}

	@Test
	public void testBuilderMulti() {
		assertShorthandText("transition:right .15s ease-out,bottom .15s ease-out;",
				"transition:.15s ease-out;transition-property:right,bottom");
		assertShorthandText("transition:width 1s ease-in-out,border 1s ease-in-out;",
				"transition: width 1s ease-in-out,border 1s ease-in-out;");
	}

	@Test
	public void testBuilderList() {
		assertShorthandText("transition:height .6s,opacity .3s .2s,visibility;",
				"transition: 0.6s height ease, opacity 0.3s 0.2s, visibility 0s");
		assertShorthandText("transition:margin-left 3500ms 5s,margin-top 0s steps(2,start) 3s;",
				"transition: 3500ms 5s margin-left, 0s 3s steps(2, start) margin-top");
	}

	@Test
	public void testBuilderList2() {
		assertShorthandText("transition:height .6s ease-in .2s,width .6s ease-in .2s;",
				"transition:height,width;transition-duration:0.6s;transition-delay:0.2s;transition-timing-function:ease-in");
	}

	@Test
	public void testBuilderListBad() {
		assertShorthandText(
				"transition-delay:5s,inherit;transition-duration:0s;transition-property:margin-left;transition-timing-function:linear;",
				"transition-duration: 0s; transition-timing-function: linear; transition-delay: 5s,inherit; transition-property: margin-left;");
	}

	@Test
	public void testBuilderZero() {
		assertShorthandText("transition:visibility linear,opacity 50ms;",
				"transition: visibility 0 linear 0,opacity 50ms");
	}

	@Test
	public void testBuilderListImportant() {
		assertShorthandText(
				"transition:margin-left 3500ms 5s,margin-top 0s steps(2,start) 3s!important;",
				"transition: 3500ms 5s margin-left, 0s 3s steps(2, start) margin-top ! important");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("transition:0s!important;", "transition: initial!important;");
		assertShorthandText("transition:none!important;", "transition: none!important;");
		assertShorthandText("transition:margin-top 3500ms ease-out 5s!important;",
				"transition: 3500ms 5s ease-out margin-top ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("transition:inherit;", "transition: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("transition:inherit!important;", "transition: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("transition:0s;", "transition: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("transition:0s!important;", "transition: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
