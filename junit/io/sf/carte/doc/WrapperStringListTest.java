/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import org.junit.Before;
import org.junit.Test;

public class WrapperStringListTest {

	LinkedList<String> baselist;
	WrapperStringList list;

	@Before
	public void setUp() {
		baselist = new LinkedList<>();
		list = new WrapperStringList(baselist);
	}

	@Test
	public void testContains() {
		assertFalse(list.contains("foo"));
		baselist.add("foo");
		assertTrue(list.contains("foo"));
	}

	@Test
	public void testContainsAll() {
		HashSet<String> hs = new HashSet<>(3);
		assertTrue(list.containsAll(hs));
		baselist.add("foo");
		assertTrue(list.containsAll(hs));
		hs.add("foo");
		assertTrue(list.containsAll(hs));
		hs.add("bar");
		assertFalse(list.containsAll(hs));
		baselist.add("bar");
		assertTrue(list.containsAll(hs));
		baselist.add("foobar");
		assertTrue(list.containsAll(hs));
		hs.add("hi");
		assertFalse(list.containsAll(hs));
	}

	@Test
	public void testGet() {
		try {
			list.get(-1);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			list.get(0);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		baselist.add("foo");
		assertEquals("foo", list.get(0));
		try {
			list.get(1);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			list.get(140);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		baselist.add("bar");
		assertEquals("bar", list.get(1));
		try {
			list.get(2);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testIndexOf() {
		assertEquals(-1, list.indexOf("foo"));
		baselist.add("foo");
		assertEquals(0, list.indexOf("foo"));
		baselist.add("bar");
		assertEquals(0, list.indexOf("foo"));
		assertEquals(1, list.indexOf("bar"));
		baselist.add("foo");
		assertEquals(0, list.indexOf("foo"));
	}

	@Test
	public void testLastIndexOf() {
		assertEquals(-1, list.lastIndexOf("foo"));
		baselist.add("foo");
		assertEquals(0, list.lastIndexOf("foo"));
		baselist.add("bar");
		assertEquals(0, list.lastIndexOf("foo"));
		assertEquals(1, list.lastIndexOf("bar"));
		baselist.add("foo");
		assertEquals(2, list.lastIndexOf("foo"));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(list.isEmpty());
		baselist.add("foo");
		assertFalse(list.isEmpty());
	}

	@Test
	public void testItem() {
		assertNull(list.item(-1));
		assertNull(list.item(0));
		baselist.add("foo");
		assertEquals("foo", list.item(0));
		assertNull(list.item(1));
		assertNull(list.item(140));
	}

	@Test
	public void testGetLength() {
		assertEquals(0, list.getLength());
		assertEquals(0, list.size());
		baselist.add("foo");
		assertEquals(1, list.getLength());
		assertEquals(1, list.size());
	}

	@Test
	public void testRemove() {
		baselist.add("foo");
		try {
			list.remove(0);
			fail("Must throw exception.");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testSublist() {
		StringList sub = list.subList(0, 0);
		assertTrue(sub.isEmpty());
		//
		try {
			list.subList(-1, 1);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			list.subList(1, -1);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			list.subList(0, 2);
			fail("Must throw exception.");
		} catch (IndexOutOfBoundsException e) {
		}
		baselist.add("foo");
		baselist.add("bar");
		baselist.add("foobar");
		sub = list.subList(0, 2);
		assertNotNull(sub);
		assertEquals("foo", sub.get(0));
		assertEquals("bar", sub.get(1));
		assertEquals(2, sub.size());
		try {
			sub.add("hi");
			fail("Must throw exception.");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testIterator() {
		Iterator<String> it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		baselist.add("foo");
		it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("foo", it.next());
		try {
			it.remove();
			fail("Must throw exception.");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testSpliterator() {
		baselist.add("foo");
		baselist.add("bar");
		baselist.add("hi");
		Spliterator<String> split = list.spliterator();
		assertNotNull(split);
		assertTrue(split.hasCharacteristics(Spliterator.SIZED));
		assertEquals(3, split.estimateSize());
		assertTrue(split.tryAdvance((a) -> {if (!a.equals("foo")) throw new IllegalStateException(a);}));
		//
		Spliterator<String> split2 = split.trySplit();
		assertNotNull(split2);
		assertTrue(split2.hasCharacteristics(Spliterator.SIZED));
		assertTrue(split2.hasCharacteristics(Spliterator.SUBSIZED));
		assertEquals(2, split2.estimateSize());
		assertTrue(split2.tryAdvance((a) -> {if (!a.equals("bar")) throw new IllegalStateException(a);}));
		assertTrue(split2.tryAdvance((a) -> {if (!a.equals("hi")) throw new IllegalStateException(a);}));
		assertFalse(split2.tryAdvance((a) -> {if (!a.equals("foo")) throw new IllegalStateException(a);}));
		assertFalse(split.tryAdvance((a) -> {if (!a.equals("foo")) throw new IllegalStateException(a);}));
	}

	@Test
	public void testToArray() {
		String[] a = list.toArray(new String[0]);
		assertEquals(0, a.length);
		baselist.add("foo");
		baselist.add("bar");
		a = list.toArray(new String[0]);
		assertEquals(2, a.length);
		assertEquals("foo", a[0]);
		assertEquals("bar", a[1]);
	}

}
