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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

public class DOMTokenSetImplTest {

	DOMTokenSetImpl tokenSet;

	@BeforeEach
	public void setUp() {
		tokenSet = new DOMTokenSetImpl();
	}

	@Test
	public void testSetValue() {
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("foo"));
		tokenSet.setValue("");
		assertEquals("", tokenSet.getValue());
		assertEquals("", tokenSet.getSortedValue());
		assertEquals("", tokenSet.toString());
		assertEquals(0, tokenSet.getLength());
		tokenSet.setValue("foo");
		assertEquals("foo", tokenSet.getValue());
		assertEquals("foo", tokenSet.getSortedValue());
		assertEquals("foo", tokenSet.toString());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		assertFalse(tokenSet.contains("bar"));
		tokenSet.add("bar");
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals("bar foo", tokenSet.getSortedValue());
		assertEquals(2, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("zzz"));
		tokenSet.add("zzz");
		assertEquals("foo bar zzz", tokenSet.getValue());
		assertEquals("bar foo zzz", tokenSet.getSortedValue());
		assertEquals(3, tokenSet.getLength());
		assertTrue(tokenSet.contains("zzz"));
		assertFalse(tokenSet.contains("zzzzz"));
		tokenSet.remove("zzz");
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("zzz"));
		tokenSet.remove("zzz");
		assertEquals(2, tokenSet.getLength());
		tokenSet.remove("bar");
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		assertFalse(tokenSet.contains("bar"));
		tokenSet.remove("zzz");
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		assertFalse(tokenSet.contains("bar"));
		tokenSet.remove("foo");
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("foo"));
		tokenSet.setValue("foo bar zzz");
		assertEquals("foo bar zzz", tokenSet.getValue());
		assertEquals(3, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		tokenSet.setValue("bar zzz");
		assertEquals("bar zzz", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("foo"));
		tokenSet.setValue("bar");
		assertEquals("bar", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("zzz"));
		tokenSet.setValue("bar zzz");
		tokenSet.setValue("");
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("zzz"));
		try {
			tokenSet.setValue(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testAdd() {
		try {
			tokenSet.add(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.add("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.add("foo bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		tokenSet.add("foo");
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		tokenSet.add("foo");
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		tokenSet.add("bar");
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		tokenSet.add("foo");
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		tokenSet.clear();
		assertEquals(0, tokenSet.getLength());
		tokenSet.add("foo");
		tokenSet.clear();
		assertEquals(0, tokenSet.getLength());
		tokenSet.setValue("foo bar");
		tokenSet.clear();
		assertEquals(0, tokenSet.getLength());
	}

	@Test
	public void testToggle() {
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("foo"));
		assertTrue(tokenSet.toggle("foo"));
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		assertTrue(tokenSet.toggle("bar"));
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertTrue(tokenSet.toggle("zzz"));
		assertEquals("foo bar zzz", tokenSet.getValue());
		assertEquals(3, tokenSet.getLength());
		assertTrue(tokenSet.contains("zzz"));
		assertFalse(tokenSet.toggle("zzz"));
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("zzz"));
		assertFalse(tokenSet.toggle("bar"));
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		assertFalse(tokenSet.contains("bar"));
		assertFalse(tokenSet.toggle("foo"));
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("foo"));
	}

	@Test
	public void testContainsAllDOMTokenList() {
		DOMTokenSetImpl tokenSet2 = new DOMTokenSetImpl();
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.setValue("three four five");
		assertFalse(tokenSet.containsAll(tokenSet2));
		tokenSet.setValue("one two three four five");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.setValue("three four five six");
		assertFalse(tokenSet.containsAll(tokenSet2));
		tokenSet2.setValue("four");
		assertTrue(tokenSet.containsAll(tokenSet2));
		//
		tokenSet.setValue("one");
		tokenSet2.setValue("five");
		assertFalse(tokenSet.containsAll(tokenSet2));
		tokenSet2.setValue("");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.setValue("one");
		assertTrue(tokenSet.containsAll(tokenSet2));
	}

	@Test
	public void testContainsAllCollection() {
		HashSet<String> tokenSet2 = new HashSet<String>();
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.add("three");
		assertFalse(tokenSet.containsAll(tokenSet2));
		tokenSet.setValue("one");
		assertFalse(tokenSet.containsAll(tokenSet2));
		tokenSet.setValue("three");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.add("four");
		assertFalse(tokenSet.containsAll(tokenSet2));
		tokenSet.setValue("one two three four five");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.add("five");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.add("six");
		assertFalse(tokenSet.containsAll(tokenSet2));
	}

	@Test
	public void testItem() {
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		tokenSet.setValue("foo");
		assertEquals("foo", tokenSet.item(0));
		assertNull(tokenSet.item(1));
		assertNull(tokenSet.item(-1));
		tokenSet.remove("foo");
		assertNull(tokenSet.item(0));
		tokenSet.setValue("foo bar");
		assertEquals("foo", tokenSet.item(0));
		assertEquals("bar", tokenSet.item(1));
		assertNull(tokenSet.item(-1));
		assertNull(tokenSet.item(2));
	}

	@Test
	public void testIterator() {
		Iterator<String> it = tokenSet.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		tokenSet.setValue("foo");
		it = tokenSet.iterator();
		assertTrue(it.hasNext());
		assertEquals("foo", it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		for (String s : tokenSet) {
			assertEquals("foo", s);
		}
		tokenSet.setValue("foo bar");
		it = tokenSet.iterator();
		assertTrue(it.hasNext());
		assertEquals("foo", it.next());
		assertTrue(it.hasNext());
		assertEquals("bar", it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testReplace() {
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("foo"));
		tokenSet.add("foo");
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		tokenSet.replace("foo", "bar");
		assertEquals("bar", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("foo"));
		tokenSet.add("zzz");
		tokenSet.add("000");
		assertEquals("bar zzz 000", tokenSet.getValue());
		assertEquals(3, tokenSet.getLength());
		tokenSet.replace("zzz", "111");
		assertEquals("bar 111 000", tokenSet.getValue());
		assertEquals(3, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertTrue(tokenSet.contains("111"));
		tokenSet.replace("foo", "fff");
		assertEquals(3, tokenSet.getLength());
		assertFalse(tokenSet.contains("fff"));
		tokenSet.replace("000", "bar");
		assertEquals("bar 111", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
	}

	@Test
	public void testCheckMultipleToken() {
		assertTrue(DOMTokenSetImpl.checkMultipleToken("foo bar"));
		assertTrue(DOMTokenSetImpl.checkMultipleToken(" foo bar "));
		assertTrue(DOMTokenSetImpl.checkMultipleToken("foo\tbar"));
		assertTrue(DOMTokenSetImpl.checkMultipleToken("\nfoo\tbar"));
		assertFalse(DOMTokenSetImpl.checkMultipleToken("foo"));
		assertFalse(DOMTokenSetImpl.checkMultipleToken("foo "));
		assertFalse(DOMTokenSetImpl.checkMultipleToken(" foo   "));
		assertFalse(DOMTokenSetImpl.checkMultipleToken("\tfoo\n\f"));
	}
}
