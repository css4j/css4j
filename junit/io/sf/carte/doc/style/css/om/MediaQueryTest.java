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

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.MediaQueryList;

public class MediaQueryTest {

	@Test
	public void testGetMedia() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("only screen");
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("screen, print");
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaList("screen, print");
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color: 4)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: 4)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color: calc(2*2))");
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: calc(2*2))", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (color-index)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (color-index)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (max-width:47.9375em)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (max-width: 47.9375em)", mql.getMedia());
		assertEquals("all and(max-width:47.9375em)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("tv and (min-width: 700px) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("tv and (min-width: 700px) and (orientation: landscape)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 700px) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("(min-width: 700px) and (orientation: landscape)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("only screen and (color: rgb(255, 4, 165))");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color: #ff04a5)", mql.getMedia());
		assertEquals("only screen and(color:#ff04a5)", mql.getMinifiedMedia());
		//
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
		MediaQueryList mql = MediaQueryFactory.createMediaList("all and (2 <= color < 5)");
		assertNotNull(mql);
		assertEquals("all and (2 <= color < 5)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 < color <= 5)");
		assertNotNull(mql);
		assertEquals("all and (2 < color <= 5)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 < color < 5)");
		assertNotNull(mql);
		assertEquals("all and (2 < color < 5)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= color <= 5)");
		assertNotNull(mql);
		assertEquals("all and (2 <= color <= 5)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 > color > 2)");
		assertNotNull(mql);
		assertEquals("all and (5 > color > 2)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 >= color > 2)");
		assertNotNull(mql);
		assertEquals("all and (5 >= color > 2)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 > color >= 2)");
		assertNotNull(mql);
		assertEquals("all and (5 > color >= 2)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 >= color >= 2)");
		assertNotNull(mql);
		assertEquals("all and (5 >= color >= 2)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (calc(4/2) < color < calc(4 + 1))");
		assertNotNull(mql);
		assertEquals("all and (calc(4/2) < color < calc(4 + 1))", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 < aspect-ratio < 16/9)");
		assertNotNull(mql);
		assertEquals("all and (4/3 < aspect-ratio < 16/9)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 <= aspect-ratio < 16/9)");
		assertNotNull(mql);
		assertEquals("all and (4/3 <= aspect-ratio < 16/9)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 < aspect-ratio <= 16/9)");
		assertNotNull(mql);
		assertEquals("all and (4/3 < aspect-ratio <= 16/9)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 <= aspect-ratio <= 16/9)");
		assertNotNull(mql);
		assertEquals("all and (4/3 <= aspect-ratio <= 16/9)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))");
		assertNotNull(mql);
		assertEquals("all and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (calc(6 - 2)/calc(5 - 2) < aspect-ratio < calc(20 - 4)/calc(10 - 1))");
		assertNotNull(mql);
		assertEquals("all and (calc(6 - 2)/calc(5 - 2) < aspect-ratio < calc(20 - 4)/calc(10 - 1))", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (color >= 2)");
		assertNotNull(mql);
		assertEquals("all and (color >= 2)", mql.getMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= color)");
		assertNotNull(mql);
		assertEquals("all and (color >= 2)", mql.getMedia());
		// Backwards-compatible serialization
		mql = MediaQueryFactory.createMediaQueryList("(resolution = 300dpi) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("(resolution: 300dpi) and (orientation: landscape)", mql.getMedia());
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
		mql = MediaQueryFactory.createMediaQueryList("all and (2 > min-color < 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 >= min-color < 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 > min-color <= 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 >= min-color =< 5)");
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
	public void testMatches() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		/*
		 * Grid
		 */
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("screen and (grid: 1)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (grid)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (grid:0)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Scan
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (scan: interlace)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (scan)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (scan: progressive)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Update
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (update: slow)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (update)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (update: fast)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Overflow-block
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-block: none)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-block)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-block: scroll)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Overflow-inline
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-inline: none)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-inline)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-inline: scroll)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Pointer
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (pointer: coarse)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (pointer)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (pointer: fine)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Color-gamut
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (color-gamut: rec2020)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color-gamut)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color-gamut: srgb)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Color
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (color: 0)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-color: 8)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color >= 8)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Monochrome
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (monochrome: 1)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (monochrome)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (monochrome: 0)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Orientation
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (orientation: portrait)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (orientation)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Resolution
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution: 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution: 72dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution: 300dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution: 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// Wrong feature names and <=, >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution <= 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution >= 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution <= 300dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution <= 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution > 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution > 72dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution >= 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution >= 96dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution >= 72dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution < 300dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution < 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution <= 300dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution <= 96dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution <= 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * width
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-width: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-width: 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-width: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-width: 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (width >= 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width >= 1024px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width >= 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (width <= 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width <= 1024px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width <= 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = MediaQueryFactory.createMediaQueryList("screen and (width < 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width < 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// =
		mql = MediaQueryFactory.createMediaQueryList("screen and (width = 1024px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width = 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// > calc
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > calc(200 * 1em))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > calc(60 * 1em))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// > relative unit
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 60em)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 100em)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * height
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-height: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-height: 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-height: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-height: 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (height >= 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height >= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height >= 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (height <= 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height <= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height <= 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = MediaQueryFactory.createMediaQueryList("screen and (height > 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height > 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = MediaQueryFactory.createMediaQueryList("screen and (height < 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height < 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// =
		mql = MediaQueryFactory.createMediaQueryList("screen and (height = 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height = 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >, <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px < height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px < height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px < height <= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px < height <= 720px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=, <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px <= height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px <= height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px <= height <= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px <= height <= 720px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=, <
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px <= height < 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px <= height < 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px <= height < 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px <= height < 1480px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >, <
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px < height < 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px < height < 1080px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px < height < 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <, >
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height > 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height > 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px > height > 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=, >
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height > 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height > 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px >= height > 360px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px >= height > 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <, >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height >= 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height >= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px > height >= 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=, >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height >= 800px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height >= 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height >= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px >= height >= 360px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px >= height >= 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * aspect-ratio
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-aspect-ratio: 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-aspect-ratio: 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-aspect-ratio: 1024/768)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < 16/9)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < 4/3)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > 4/3)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= 16/9)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 < aspect-ratio < 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 < aspect-ratio <= 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 <= aspect-ratio <= 16/9)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (7/6 <= aspect-ratio <= 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (7/6 < aspect-ratio < 4/3)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 >= aspect-ratio >= 7/6)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 > aspect-ratio >= 7/6)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * aspect-ratio + Calc + level 4 syntax
		 */
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(2*2)/calc(9/3) < aspect-ratio <= calc(4*4)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(2*2)/calc(9/3) <= aspect-ratio <= calc(4*4)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(8 - 1)/calc(2*3) <= aspect-ratio <= calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(8 - 1)/calc(2*3) < aspect-ratio < calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(2*2)/calc(9/3) >= aspect-ratio >= calc(8 - 1)/calc(2*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (calc(2*2)/calc(9/3) > aspect-ratio >= calc(8 - 1)/calc(2*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
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
