/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

public class MediaListTest {

	@Test
	public void testGetMediaText() {
		MediaList list = MediaList.createMediaList();
		assertEquals("all", list.getMediaText());
		list = MediaList.createMediaList("screen, handheld");
		assertEquals("screen,handheld", list.getMediaText());
	}

	@Test
	public void testSetMediaText() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("print, screen");
		assertEquals("print,screen", list.getMediaText());
	}

	@Test
	public void testSetMediaTextEscaped() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("\\:print, \\9 screen");
		assertEquals("\\:print,\\9 screen", list.getMediaText());
		assertEquals(2, list.getLength());
	}

	@Test
	public void testGetLength() {
		MediaList list = MediaList.createMediaList();
		assertEquals(0, list.getLength());
		list.setMediaText("print, screen");
		assertEquals(2, list.getLength());
	}

	@Test
	public void testItem() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("print, screen");
		assertEquals("screen", list.item(1));
	}

	@Test
	public void testDeleteMedium() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("print, screen");
		list.deleteMedium("print");
		assertEquals(1, list.getLength());
		assertEquals("screen", list.getMediaText());
	}

	@Test
	public void testAppendMedium() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("print, screen");
		list.appendMedium("handheld");
		assertEquals(3, list.getLength());
		assertEquals("print,screen,handheld", list.getMediaText());
		assertEquals("screen", list.item(1));
		assertEquals("handheld", list.item(2));
	}

	@Test
	public void testEquals() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("print, screen");
		MediaList other = MediaList.createMediaList();
		other.setMediaText("print, screen");
		assertTrue(list.equals(other));
		assertEquals(list.hashCode(), other.hashCode());
		other.setMediaText("print, audio");
		assertFalse(list.equals(other));
		other.setMediaText("screen, print");
		assertTrue(list.equals(other));
		assertEquals(list.hashCode(), other.hashCode());
	}

	@Test
	public void testToString() {
		MediaList list = MediaList.createMediaList();
		list.setMediaText("print, screen");
		assertEquals("print,screen", list.toString());
	}

	@Test
	public void testMatchesString() {
		MediaList list = MediaList.createMediaList();
		assertTrue(list.matches("all", null));
		assertTrue(list.matches("screen", null));
		list.setMediaText("print, screen");
		assertTrue(list.matches("screen", null));
		assertFalse(list.matches("handheld", null));
	}

	@Test
	public void testMatchesMediaQuerList() {
		MediaList list = MediaList.createMediaList();
		MediaList otherList = MediaList.createMediaList();
		assertTrue(list.matches(otherList));
		otherList.setMediaText("all");
		assertTrue(list.matches(otherList));
		otherList.setMediaText("print, screen");
		assertTrue(list.matches(otherList));
		assertFalse(otherList.matches(list));
		list.setMediaText("print, screen");
		assertTrue(list.matches(otherList));
		otherList.setMediaText("screen");
		assertTrue(list.matches(otherList));
	}

	@Test
	public void testCreateUnmodifiable() {
		MediaList list = MediaList.createUnmodifiable();
		assertEquals("all", list.getMediaText());
		boolean pass = false;
		try {
			list.appendMedium("tv");
		} catch (DOMException e) {
			pass = e.code == DOMException.NO_MODIFICATION_ALLOWED_ERR;
		}
		assertTrue(pass);
	}

	@Test
	public void testCreateUnmodifiableString() {
		MediaList list = MediaList.createUnmodifiable("print, screen");
		assertEquals("print,screen", list.getMediaText());
		boolean pass = false;
		try {
			list.appendMedium("tv");
		} catch (DOMException e) {
			pass = e.code == DOMException.NO_MODIFICATION_ALLOWED_ERR;
		}
		assertTrue(pass);
	}

	@Test
	public void testUnmodifiable() {
		MediaList list = MediaList.createMediaList("print, screen");
		MediaList unm = list.unmodifiable();
		assertEquals(list.getMediaText(), unm.getMediaText());
		list.appendMedium("handheld");
		assertEquals(list.getMediaText(), unm.getMediaText());
		list.deleteMedium("print");
		assertEquals(list.getMediaText(), unm.getMediaText());
		boolean pass = false;
		try {
			unm.appendMedium("tv");
		} catch (DOMException e) {
			pass = e.code == DOMException.NO_MODIFICATION_ALLOWED_ERR;
		}
		assertTrue(pass);
	}

}
