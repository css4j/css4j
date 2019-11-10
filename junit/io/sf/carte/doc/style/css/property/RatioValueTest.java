/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.DOMException;

public class RatioValueTest {

	@Test
	public void testEquals() {
		ValueFactory vf = new ValueFactory();
		PrimitiveValue value = vf.parseMediaFeature("3/2");
		RatioValue ratio = (RatioValue) value;
		assertTrue(ratio.equals(ratio));
		RatioValue ratio2 = (RatioValue) vf.parseMediaFeature("3/2");
		assertTrue(ratio.equals(ratio2));
		assertEquals(ratio.hashCode(), ratio2.hashCode());
		//
		CalcValue calc = (CalcValue) vf.parseProperty("calc(2 * 3)");
		ratio2.setAntecedentValue(calc);
		assertFalse(ratio.equals(ratio2));
		ratio2 = (RatioValue) vf.parseMediaFeature("3/3");
		assertFalse(ratio.equals(ratio2));
		assertFalse(ratio.hashCode() == ratio2.hashCode());
	}

	@Test
	public void testGetCssText() {
		ValueFactory vf = new ValueFactory();
		PrimitiveValue value = vf.parseMediaFeature("3/2");
		RatioValue ratio = (RatioValue) value;
		assertEquals("3", ratio.getAntecedentValue().getCssText());
		assertEquals("2", ratio.getConsequentValue().getCssText());
		assertEquals("3", ratio.getComponent(0).getCssText());
		assertEquals("2", ratio.getComponent(1).getCssText());
		assertEquals("3/2", ratio.getCssText());
		//
		PrimitiveValue primi = (PrimitiveValue) vf.parseProperty("9");
		ratio.setAntecedentValue(primi);
		assertEquals("9/2", ratio.getCssText());
		//
		primi = (PrimitiveValue) vf.parseProperty("7");
		ratio.setComponent(0, primi);
		assertSame(primi, ratio.getComponent(0));
		ratio.setComponent(2, primi);
		assertEquals("7/2", ratio.getCssText());
		primi = (PrimitiveValue) vf.parseProperty("3");
		ratio.setComponent(1, primi);
		assertEquals("7/3", ratio.getCssText());
		//
		ratio.setConsequentValue((PrimitiveValue) vf.parseProperty("5"));
		assertEquals("7/5", ratio.getCssText());
		//
		ratio.setAntecedentValue((PrimitiveValue) vf.parseProperty("11.8"));
		assertEquals("11.8/5", ratio.getCssText());
		//
		ratio.setConsequentValue((PrimitiveValue) vf.parseProperty("3.7"));
		assertEquals("11.8/3.7", ratio.getCssText());
		//
		CalcValue calc = (CalcValue) vf.parseProperty("calc(2 * 3)");
		ratio.setAntecedentValue(calc);
		assertEquals("calc(2*3)/3.7", ratio.getCssText());
		calc = (CalcValue) vf.parseProperty("calc(5 / 3)");
		ratio.setConsequentValue(calc);
		assertEquals("calc(2*3)/calc(5/3)", ratio.getCssText());
		//
		try {
			ratio.setAntecedentValue(null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			ratio.setConsequentValue(null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		//
		try {
			ratio.setAntecedentValue((PrimitiveValue) vf.parseProperty("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			ratio.setConsequentValue((PrimitiveValue) vf.parseProperty("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testSetCssText() {
		ValueFactory vf = new ValueFactory();
		PrimitiveValue value = vf.parseMediaFeature("3/2");
		value.setCssText("16/9");
		assertEquals("16/9", value.getCssText());
		try {
			value.setCssText("foo");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		try {
			value.setCssText("16");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		try {
			value.setCssText("16/");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		ValueFactory vf = new ValueFactory();
		PrimitiveValue value = vf.parseMediaFeature("3/2");
		RatioValue ratio = (RatioValue) value;
		RatioValue clon = ratio.clone();
		assertEquals(ratio.getAntecedentValue().getCssText(), clon.getAntecedentValue().getCssText());
		assertEquals(ratio.getConsequentValue().getCssText(), clon.getConsequentValue().getCssText());
		assertEquals(ratio.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(ratio.getCssText(), clon.getCssText());
	}

	@Test
	public void testInvalid() {
		ValueFactory vf = new ValueFactory();
		try {
			vf.parseMediaFeature("3/foo");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			vf.parseMediaFeature("foo/5");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

}
