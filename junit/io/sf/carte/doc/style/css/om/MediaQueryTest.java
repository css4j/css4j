/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.sf.carte.doc.style.css.MediaQueryList;

public class MediaQueryTest {

	@Test
	public void testGetMedia() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("only screen");
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen, print");
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		mql = MediaQueryFactory.createMediaList("screen, print");
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color: 4)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: 4)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("all and (color-index)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (color-index)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("all and (max-width:47.9375em)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (max-width: 47.9375em)", mql.getMedia());
		assertEquals("all and(max-width:47.9375em)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("tv and (min-width: 700px) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("tv and (min-width: 700px) and (orientation: landscape)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 700px) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("(min-width: 700px) and (orientation: landscape)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("only screen and (color: rgb(255, 4, 165))");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color: #ff04a5)", mql.getMedia());
		assertEquals("only screen and(color:#ff04a5)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList(
				"only screen and (min-width:690px) and (max-width:780px)");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (min-width: 690px) and (max-width: 780px)", mql.getMedia());
		assertEquals("only screen and(min-width:690px) and(max-width:780px)", mql.getMinifiedMedia());
	}

	@Test
	public void testGetMediaNL() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only\nscreen");
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
	}

	@Test
	public void testGetMediaEscaped() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only \\9 screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only \\9 screen and (color)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("only \\9 screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only \\9 screen and (color)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 700px) and (orientation: \\9 foo)");
		assertEquals("(min-width: 700px) and (orientation: \\9 foo)", mql.getMedia());
		assertFalse(mql.hasErrors());
	}

	@Test
	public void testGetMediaEscapedBad() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only \\9screen and (color)");
		assertEquals("only \\9screen and (color)", mql.getMedia());
		assertFalse(mql.hasErrors());
	}

	@Test
	public void testGetMediaRatio() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("(max-aspect-ratio:160/100) and (min-width:300px)");
		assertFalse(mql.hasErrors());
		assertEquals("(max-aspect-ratio: 160/100) and (min-width: 300px)", mql.getMedia());
		mql.item(0);
	}

	@Test
	public void testGetMediaLevel4() {
		MediaQueryList mql = MediaQueryFactory.createMediaList("all and (2 <= min-color < 5)");
		assertNotNull(mql);
		assertEquals("all and (2 <= min-color < 5)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 5)");
		assertNotNull(mql);
		assertEquals("all and (2 <= min-color < 5)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color >= 2)");
		assertNotNull(mql);
		assertEquals("all and (min-color >= 2)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color)");
		assertNotNull(mql);
		assertEquals("all and (min-color >= 2)", mql.getMedia());
	}

	@Test
	public void testGetCssMediaInvalid() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only screen and (color");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color 4)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and {min-color: 4}");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("not all and");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and ()");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("tv and (orientation:)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("tv and only (orientation: landscape)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 700px) and");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("foo bar");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color <)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= 4)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("only screen and (color: rgb(255, 165))");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("only screen and (color: #xxxz)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color 4), tv");
		assertEquals("tv", mql.getMedia());
		// Example 18 of spec
		mql = MediaQueryFactory.createMediaQueryList("(example, all,), speech");
		assertEquals("speech", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("&test, speech");
		assertEquals("speech", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("(example, speech");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		// Example 20 of spec
		mql = MediaQueryFactory.createMediaQueryList("or and (color)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
	}

	@Test
	public void testEquals() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only screen and (color)");
		MediaQueryList mql2 = MediaQueryFactory.createMediaQueryList("only screen and (color)");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 5)");
		mql2 = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 5)");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		mql2 = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 4)");
		assertFalse(mql.equals(mql2));
	}

	@Test
	public void testIsMediaFeature() {
		assertTrue(MediaQueryFactory.isMediaFeature("color"));
		assertTrue(MediaQueryFactory.isMediaFeature("width"));
		assertTrue(MediaQueryFactory.isMediaFeature("pointer"));
		assertTrue(MediaQueryFactory.isMediaFeature("orientation"));
		assertTrue(MediaQueryFactory.isMediaFeature("aspect-ratio"));
		assertFalse(MediaQueryFactory.isMediaFeature("foo"));
	}

	@Test
	public void testIsPlainMediaList() {
		assertTrue(MediaQueryFactory.isPlainMediaList("screen, tv"));
		assertTrue(MediaQueryFactory.isPlainMediaList("print"));
		assertFalse(MediaQueryFactory.isPlainMediaList("not screen"));
		assertFalse(MediaQueryFactory.isPlainMediaList("screen and (color)"));
	}
}
