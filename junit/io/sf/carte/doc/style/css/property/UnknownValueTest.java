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

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class UnknownValueTest {

	@Test
	public void testEquals() {
		UnknownValue value = new UnknownValue();
		value.setPlainCssText("*");
		assertTrue(value.equals(value));
		UnknownValue value2 = new UnknownValue();
		value2.setPlainCssText("*");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		value2.setPlainCssText("^");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		UnknownValue value = new UnknownValue();
		value.setPlainCssText("*");
		assertEquals("*", value.getCssText());
	}

	@Test
	public void testSetLexicalUnitIEHackFlag() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		parser.setFlag(Parser.Flag.IEVALUES);
		StringReader re = new StringReader("screen\\0");
		LexicalUnit lu = parser.parsePropertyValue(re);
		assertEquals("screen\\0", lu.getStringValue());
		UnknownValue value = new UnknownValue();
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("screen\\0", value.getCssText());
		assertEquals("screen\\0", value.getMinifiedCssText(""));
	}

	@Test
	public void testClone() {
		UnknownValue value = new UnknownValue();
		value.setPlainCssText("*");
		UnknownValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
