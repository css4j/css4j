/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;

public class DOMTokenListImplTest {

	DOMTokenListImpl tokenSet;

	@Before
	public void setUp() {
		tokenSet = new DOMTokenListImpl();
	}

	@Test
	public void testAdd() {
		tokenSet.add("foo");
		assertEquals("foo", tokenSet.getValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
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
		tokenSet.add("bar");
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
	}

	@Test
	public void testSetValue() {
		assertEquals("", tokenSet.getValue());
		assertEquals("", tokenSet.getSortedValue());
		assertEquals(0, tokenSet.getLength());
		assertFalse(tokenSet.contains("foo"));
		tokenSet.setValue("");
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
		tokenSet.setValue("foo");
		assertEquals("foo", tokenSet.getValue());
		assertEquals("foo", tokenSet.getSortedValue());
		assertEquals(1, tokenSet.getLength());
		assertTrue(tokenSet.contains("foo"));
		assertFalse(tokenSet.contains("bar"));
		try {
			tokenSet.setValue(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		tokenSet.add("bar");
		assertEquals("foo bar", tokenSet.getValue());
		assertEquals(2, tokenSet.getLength());
		assertTrue(tokenSet.contains("bar"));
		assertFalse(tokenSet.contains("zzz"));
		tokenSet.add("zzz");
		assertEquals("foo bar zzz", tokenSet.getValue());
		assertEquals(3, tokenSet.getLength());
		assertTrue(tokenSet.contains("zzz"));
		assertFalse(tokenSet.contains("zzzzz"));
		assertEquals("bar foo zzz", tokenSet.getSortedValue());
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
		tokenSet.setValue("");
		assertEquals("", tokenSet.getValue());
		assertEquals(0, tokenSet.getLength());
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
		assertNull(tokenSet.item(2));
	}

	@Test
	public void testIterator() {
		tokenSet.setValue("one two three");
		Iterator<String> it = tokenSet.iterator();
		assertTrue(it.hasNext());
		assertEquals("one", it.next());
		assertTrue(it.hasNext());
		assertEquals("two", it.next());
		assertTrue(it.hasNext());
		assertEquals("three", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testContainsAllDOMTokenList() {
		tokenSet.setValue("one two three four five");
		DOMTokenListImpl tokenSet2 = new DOMTokenListImpl();
		tokenSet2.setValue("three four five");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.setValue("three four five six");
		assertFalse(tokenSet.containsAll(tokenSet2));
	}

	@Test
	public void testContainsAllCollection() {
		tokenSet.setValue("one two three four five");
		HashSet<String> tokenSet2 = new HashSet<String>();
		tokenSet2.add("three");
		tokenSet2.add("four");
		tokenSet2.add("five");
		assertTrue(tokenSet.containsAll(tokenSet2));
		tokenSet2.add("six");
		assertFalse(tokenSet.containsAll(tokenSet2));
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
		try {
			tokenSet.toggle(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.toggle("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.toggle("foo bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
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
		try {
			tokenSet.replace(null, null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.replace(null, "foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.replace("foo", null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.replace("", "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.replace("", "foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.replace("foo", "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.replace("foo bar", "foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			tokenSet.replace("foo", "foo bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testRemove() {
		try {
			tokenSet.remove(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.remove("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			tokenSet.remove("foo bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

}
