/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.sf.carte.doc.style.css.CSSValue.Type;

public class LexicalValueTest {

	@Test
	public void testGetFinalType() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("1em");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1%");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1.1");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("foo");
		assertEquals(Type.IDENT, value.getFinalType());
		//
		value.setCssText("'foo'");
		assertEquals(Type.STRING, value.getFinalType());
		//
		value.setCssText("1em / 1");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("16 / 9");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("16 / calc(3*2)");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("calc(4*4) / 9");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("16 / foo");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("foo / 9");
		assertEquals(Type.UNKNOWN, value.getFinalType());
	}

	@Test
	public void testEquals() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertTrue(value.equals(value));
		LexicalValue value2 = new LexicalValue();
		value2.setCssText("1em 2em");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		value2.setCssText("1em 2px");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertEquals("1em 2em", value.getCssText());
	}

	@Test
	public void testClone() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 1px");
		LexicalValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
