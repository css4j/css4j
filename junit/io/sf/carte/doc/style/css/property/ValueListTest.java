/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ValueListTest {

	static ValueFactory factory;

	@Before
	public void setUp() {
		factory = new ValueFactory();
	}

	@Test
	public void testItem() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		ws.add(factory.parseProperty("repeat"));
		ws.add(factory.parseProperty("repeat"));
		assertNull(cs.item(-1));
		assertNull(cs.item(2));
		assertNull(ws.item(-1));
		assertNull(ws.item(2));
		assertEquals("thin", cs.item(0).getCssText());
		assertEquals("repeat", ws.item(0).getCssText());
		assertEquals("thin, thick", cs.getCssText());
		assertEquals("thin,thick", cs.getMinifiedCssText(""));
		assertEquals("repeat repeat", ws.getCssText());
		assertEquals("repeat repeat", ws.getMinifiedCssText(""));
	}

	@Test
	public void testHashCode() {
		ValueList cs = ValueList.createCSValueList();
		ValueList cs2 = ValueList.createCSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		cs.add(factory.parseProperty("medium"));
		cs2.add(factory.parseProperty("thin"));
		cs2.add(factory.parseProperty("thick"));
		cs2.add(factory.parseProperty("medium"));
		assertEquals(cs.hashCode(), cs2.hashCode());
	}

	@Test
	public void testEqualsObject() {
		ValueList cs = ValueList.createCSValueList();
		ValueList cs2 = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		assertFalse(cs.equals(ws));
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		cs.add(factory.parseProperty("medium"));
		assertFalse(cs.equals(ws));
		ws.add(factory.parseProperty("thin"));
		ws.add(factory.parseProperty("thick"));
		ws.add(factory.parseProperty("medium"));
		assertFalse(cs.equals(ws));
		cs2.add(factory.parseProperty("thin"));
		cs2.add(factory.parseProperty("thick"));
		assertFalse(cs.equals(cs2));
		cs2.add(factory.parseProperty("medium"));
		assertTrue(cs.equals(cs2));
	}

	@Test
	public void testClone() {
		ValueList cs = ValueList.createCSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		cs.add(factory.parseProperty("medium"));
		ValueList clon = cs.clone();
		assertEquals(cs.getCssValueType(), clon.getCssValueType());
		assertEquals(cs.getLength(), clon.getLength());
		assertEquals(cs.item(0), clon.item(0));
		assertEquals(cs.getCssText(), clon.getCssText());
	}

	@Test
	public void testGetCssText() {
		ValueList ws = ValueList.createWSValueList();
		ws.add(factory.parseProperty("rgba(120, 47, 253, 0.9)"));
		ws.add(factory.parseProperty("rgb(10, 4, 2)"));
		assertEquals("rgba(120, 47, 253, 0.9) #0a0402", ws.getCssText());
		assertEquals("rgba(120,47,253,.9) #0a0402", ws.getMinifiedCssText(""));
	}

}
