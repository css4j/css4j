/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class CSSUnknownValueTest {

	@Test
	public void testEquals() {
		CSSUnknownValue value = new CSSUnknownValue();
		value.setPlainCssText("*");
		assertTrue(value.equals(value));
		CSSUnknownValue value2 = new CSSUnknownValue();
		value2.setPlainCssText("*");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		value2.setPlainCssText("^");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		CSSUnknownValue value = new CSSUnknownValue();
		value.setPlainCssText("*");
		assertEquals("*", value.getCssText());
	}

	@Test
	public void testSetLexicalUnitIEHackFlag() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		parser.setFlag(Parser2.Flag.IEVALUES);
		InputSource source = new InputSource(new StringReader("screen\\0"));
		LexicalUnit lu = parser.parsePropertyValue(source);
		assertEquals("screen\\0", lu.getStringValue());
		CSSUnknownValue value = new CSSUnknownValue();
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("screen\\0", value.getCssText());
		assertEquals("screen\\0", value.getMinifiedCssText(""));
	}

	@Test
	public void testClone() {
		CSSUnknownValue value = new CSSUnknownValue();
		value.setPlainCssText("*");
		CSSUnknownValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
