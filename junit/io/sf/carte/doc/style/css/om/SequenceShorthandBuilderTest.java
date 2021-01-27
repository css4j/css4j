/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SequenceShorthandBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBuilderNoShorthand() {
		assertShorthandText("cue-before:url('foo.au');", "cue-before: url('foo.au')");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("cue:url('foo.au');", "cue: url('foo.au'); ");
		assertShorthandText("cue:url('foo.au') url('bar.au');",
				"cue: url('foo.au') url('bar.au'); ");
	}

	@Test
	public void testBuilderMix() {
		/*
		 * Not very realistic examples
		 */
		assertShorthandText("cue-after:inherit;cue-before:url('foo.au');",
				"cue-before: url('foo.au'); cue-after:inherit;");
		assertShorthandText("cue-after:unset;cue-before:url('foo.au');",
				"cue-before: url('foo.au'); cue-after:unset;");
		assertShorthandText("cue:url('foo.au') none;",
				"cue-before: url('foo.au'); cue-after:initial;");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("cue:url('foo.au')!important;", "cue: url('foo.au') !important; ");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("cue:inherit;", "cue: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("cue:inherit!important;", "cue: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("cue:unset;", "cue: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("cue:unset!important;", "cue: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
