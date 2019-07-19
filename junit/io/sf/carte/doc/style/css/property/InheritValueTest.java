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

public class InheritValueTest {

	@Test
	public void testEquals() {
		ValueFactory factory = new ValueFactory();
		AbstractCSSValue value = factory.parseProperty("scroll");
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
	}

	@Test
	public void testClone() {
		InheritValue value = InheritValue.getValue();
		InheritValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
