/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LinkedStringListTest {

	StringList list;

	@Before
	public void setUp() {
		list = new LinkedStringList();
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

}
