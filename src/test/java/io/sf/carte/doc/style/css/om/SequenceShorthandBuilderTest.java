/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SequenceShorthandBuilderTest {

	BaseCSSStyleDeclaration emptyStyleDecl;

	@BeforeEach
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
		assertShorthandText("cue:none;", "cue: initial;");
		assertShorthandText("cue:url('foo.au');", "cue: url('foo.au'); ");
		assertShorthandText("cue:url('foo.au') url('bar.au');",
				"cue: url('foo.au') url('bar.au'); ");
	}

	@Test
	public void testBuilderVar() {
		assertShorthandText("cue:var(--foo,url('foo.au'));", "cue: var(--foo,url('foo.au')); ");
	}

	@Test
	public void testBuilderMix() {
		/*
		 * Not very realistic examples
		 */
		assertShorthandText("cue-after:inherit;cue-before:url('foo.au');",
				"cue-before: url('foo.au'); cue-after:inherit;");
		assertShorthandText("cue-after:revert;cue-before:url('foo.au');",
				"cue-before: url('foo.au'); cue-after:revert;");
		assertShorthandText("cue:url('foo.au') none;",
				"cue-before: url('foo.au'); cue-after:unset;");
		assertShorthandText("cue:url('foo.au') none;",
				"cue-before: url('foo.au'); cue-after:initial;");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("cue:none!important;", "cue: initial!important;");
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
		assertShorthandText("cue:none;", "cue: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("cue:none!important;", "cue: unset!important;");
	}

	@Test
	public void testBuilderRevert() {
		assertShorthandText("cue:revert;", "cue: revert;");
	}

	@Test
	public void testBuilderRevertImportant() {
		assertShorthandText("cue:revert!important;", "cue: revert!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
