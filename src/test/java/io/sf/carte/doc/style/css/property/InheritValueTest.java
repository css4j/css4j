/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class InheritValueTest {

	@Test
	public void testEquals() {
		ValueFactory factory = new ValueFactory();
		StyleValue value = factory.parseProperty("scroll");
		InheritValue inherit = InheritValue.getValue();
		assertFalse(inherit.equals(value));
		assertTrue(inherit.equals(inherit));
		assertTrue(inherit.hashCode() != value.hashCode());
		value = factory.parseProperty("INHERIT");
		assertTrue(inherit.hashCode() == value.hashCode());
	}

	@Test
	public void testGetCssText() {
		InheritValue inherit = InheritValue.getValue();
		assertEquals("inherit", inherit.getCssText());
		assertEquals(CSSValue.Type.INHERIT, inherit.getPrimitiveType());
	}

	@Test
	public void testMatch() {
		SyntaxParser syntaxParser = new SyntaxParser();
		InheritValue value = InheritValue.getValue();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("<color> | <custom-ident>");
		assertEquals(Match.PENDING, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	@Test
	public void testClone() {
		InheritValue value = InheritValue.getValue();
		KeywordValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
