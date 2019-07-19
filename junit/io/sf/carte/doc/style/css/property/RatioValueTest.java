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

public class RatioValueTest {

	@Test
	public void testEquals() {
		RatioValue value = new RatioValue();
		value.setAntecedentValue(1);
		value.setConsequentValue(2);
		assertTrue(value.equals(value));
		RatioValue value2 = new RatioValue();
		value2.setAntecedentValue(1);
		value2.setConsequentValue(2);
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		value2.setConsequentValue(3);
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		RatioValue value = new RatioValue();
		value.setAntecedentValue(1);
		value.setConsequentValue(2);
		assertEquals("1/2", value.getCssText());
	}

	@Test
	public void testClone() {
		RatioValue value = new RatioValue();
		value.setAntecedentValue(1);
		value.setConsequentValue(2);
		RatioValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
