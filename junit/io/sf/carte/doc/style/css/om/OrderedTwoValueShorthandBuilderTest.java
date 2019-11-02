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

public class OrderedTwoValueShorthandBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBuilderNoShorthand() {
		assertShorthandText("justify-content:center;", "justify-content: center;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("place-content:normal;", "place-content: normal");
		assertShorthandText("place-content:center start;", "place-content: center start");
		assertShorthandText("place-content:center;", "place-content: center");
		assertShorthandText("place-content:center;", "place-content: center center");
		assertShorthandText("place-content:baseline center;", "place-content: baseline center");
		assertShorthandText("place-content:first baseline space-evenly;", "place-content: first baseline space-evenly");
		assertShorthandText("place-content:space-between;", "place-content: space-between");
		assertShorthandText("place-content:last baseline right;", "place-content: last baseline right");
		assertShorthandText("place-items:normal;", "place-items: normal");
		assertShorthandText("place-items:center;", "place-items: center center");
		assertShorthandText("place-items:center start;", "place-items: center start");
		assertShorthandText("place-items:stretch unsafe end;", "place-items: stretch unsafe end");
		assertShorthandText("place-items:first baseline legacy right;", "place-items: first baseline legacy right");
		assertShorthandText("place-self:normal;", "place-self: normal");
		assertShorthandText("place-self:center;", "place-self: center center");
		assertShorthandText("place-self:center start;", "place-self: center start");
		assertShorthandText("place-self:stretch unsafe end;", "place-self: stretch unsafe end");
		assertShorthandText("place-self:first baseline safe start;", "place-self: first baseline safe start");
		assertShorthandText("gap:normal;", "gap: normal");
		assertShorthandText("gap:50px normal;", "gap:50px normal");
		assertShorthandText("gap:50px;", "gap: 50px");
		assertShorthandText("gap:50px 20px;", "gap: 50px 20px");
		assertShorthandText("gap:50px 4%;", "gap: 50px 4%");
		assertShorthandText("gap:4%;", "gap: 4%");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("place-content:var(--foo, center start);", "place-content: var(--foo,center start)");
	}

	@Test
	public void testBuilderMix() {
		assertShorthandText("align-content:normal;justify-content:inherit;",
				"align-content: normal; justify-content: inherit;");
		assertShorthandText("align-content:normal;justify-content:revert;",
				"align-content: normal; justify-content: revert;");
		assertShorthandText("place-content:normal;",
				"align-content: normal; justify-content: initial;");
		assertShorthandText("place-content:normal;",
				"align-content: normal; justify-content: unset;");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("place-content:first baseline space-evenly!important;",
				"place-content: first baseline space-evenly !important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("place-content:inherit;", "place-content: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("place-content:inherit!important;", "place-content: inherit!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("place-content:revert;", "place-content: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("place-content:revert!important;", "place-content: revert!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("place-content:unset;", "place-content: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("place-content:unset!important;", "place-content: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
