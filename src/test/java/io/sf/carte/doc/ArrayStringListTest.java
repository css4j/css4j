/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayStringListTest {

	StringList list;

	@BeforeEach
	public void setUp() {
		list = new ArrayStringList();
	}

	@Test
	public void testContains() {
		assertFalse(list.contains("foo"));
		list.add("foo");
		assertTrue(list.contains("foo"));
	}

	@Test
	public void testItem() {
		assertNull(list.item(-1));
		assertNull(list.item(0));
		list.add("foo");
		assertEquals("foo", list.item(0));
		assertNull(list.item(1));
		assertNull(list.item(140));
	}

	@Test
	public void testGetLength() {
		assertEquals(0, list.getLength());
		list.add("foo");
		assertEquals(1, list.getLength());
	}

	@Test
	public void testClone() {
		StringList clon = list.clone();
		assertEquals(0, clon.getLength());

		list.add("foo");
		clon = list.clone();
		assertEquals(1, clon.getLength());
		assertEquals("foo", clon.item(0));

		assertTrue(clon.containsAll(list));
		assertTrue(list.containsAll(clon));
	}

}
