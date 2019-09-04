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
		MediaQueryList mql;
		mql = MediaQueryFactory.createMediaQueryList("not screen", null);
		assertFalse(mql.hasErrors());
		assertEquals("not screen", mql.getMedia());
		assertEquals("not screen", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("only screen and (color)", null);
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color)", mql.getMedia());
		assertEquals("only screen and (color)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("only screen", null);
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
		assertEquals("only screen", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("screen, print", null);
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		assertEquals("screen,print", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaList("screen, print", null);
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		assertEquals("screen,print", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color: 4)", null);
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: 4)", mql.getMedia());
		assertEquals("all and (min-color:4)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (min-color: calc(2*2))", null);
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: calc(2*2))", mql.getMedia());
		assertEquals("all and (min-color:calc(2*2))", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (color-index)", null);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color-index)", mql.getMedia());
		assertEquals("all and (color-index)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (max-width:47.9375em)", null);
		assertFalse(mql.hasErrors());
		assertEquals("all and (max-width: 47.9375em)", mql.getMedia());
		assertEquals("all and (max-width:47.9375em)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("tv and (min-width: 700px) and (orientation: landscape)", null);
		assertFalse(mql.hasErrors());
		assertEquals("tv and (min-width: 700px) and (orientation: landscape)", mql.getMedia());
		assertEquals("tv and (min-width:700px) and (orientation:landscape)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 700px) and (orientation: landscape)", null);
		assertFalse(mql.hasErrors());
		assertEquals("(min-width: 700px) and (orientation: landscape)", mql.getMedia());
		assertEquals("(min-width:700px) and (orientation:landscape)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("only screen and (color: rgb(255, 4, 165))", null);
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color: #ff04a5)", mql.getMedia());
		assertEquals("only screen and (color:#ff04a5)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("only screen and (min-width:690px) and (max-width:780px)", null);
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (min-width: 690px) and (max-width: 780px)", mql.getMedia());
		assertEquals("only screen and (min-width:690px) and (max-width:780px)", mql.getMinifiedMedia());
	}

	@Test
	public void testGetMediaNL() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only\nscreen", null);
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
	}

	@Test
	public void testGetMediaEscaped() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only \\9 screen and (color)", null);
		assertFalse(mql.hasErrors());
		assertEquals("only \\9 screen and (color)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("only \\9 screen and (color)", null);
		assertFalse(mql.hasErrors());
		assertEquals("only \\9 screen and (color)", mql.getMedia());
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 700px) and (orientation: \\9 foo)", null);
		assertEquals("(min-width: 700px) and (orientation: \\9 foo)", mql.getMedia());
		assertFalse(mql.hasErrors());
	}

	@Test
	public void testGetMediaEscapedBad() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only \\9screen and (color)", null);
		assertEquals("only \\9screen and (color)", mql.getMedia());
		assertFalse(mql.hasErrors());
	}

	@Test
	public void testGetMediaRatio() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("(max-aspect-ratio:160/100) and (min-width:300px)",
				null);
		assertFalse(mql.hasErrors());
		assertEquals("(max-aspect-ratio: 160/100) and (min-width: 300px)", mql.getMedia());
		assertEquals("(max-aspect-ratio:160/100) and (min-width:300px)", mql.getMinifiedMedia());
		mql.item(0);
	}

	@Test
	public void testGetMediaLevel4() {
		MediaQueryList mql = MediaQueryFactory.createMediaList("all and (2 <= color < 5)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 <= color < 5)", mql.getMedia());
		assertEquals("all and (2<=color<5)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 < color <= 5)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 < color <= 5)", mql.getMedia());
		assertEquals("all and (2<color<=5)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 < color < 5)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 < color < 5)", mql.getMedia());
		assertEquals("all and (2<color<5)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= color <= 5)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 <= color <= 5)", mql.getMedia());
		assertEquals("all and (2<=color<=5)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 > color > 2)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 > color > 2)", mql.getMedia());
		assertEquals("all and (5>color>2)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 >= color > 2)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 >= color > 2)", mql.getMedia());
		assertEquals("all and (5>=color>2)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 > color >= 2)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 > color >= 2)", mql.getMedia());
		assertEquals("all and (5>color>=2)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (5 >= color >= 2)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 >= color >= 2)", mql.getMedia());
		assertEquals("all and (5>=color>=2)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (calc(4/2) < color < calc(4 + 1))", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (calc(4/2) < color < calc(4 + 1))", mql.getMedia());
		assertEquals("all and (calc(4/2)<color<calc(4 + 1))", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 < aspect-ratio < 16/9)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 < aspect-ratio < 16/9)", mql.getMedia());
		assertEquals("all and (4/3<aspect-ratio<16/9)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 <= aspect-ratio < 16/9)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 <= aspect-ratio < 16/9)", mql.getMedia());
		assertEquals("all and (4/3<=aspect-ratio<16/9)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 < aspect-ratio <= 16/9)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 < aspect-ratio <= 16/9)", mql.getMedia());
		assertEquals("all and (4/3<aspect-ratio<=16/9)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (4/3 <= aspect-ratio <= 16/9)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 <= aspect-ratio <= 16/9)", mql.getMedia());
		assertEquals("all and (4/3<=aspect-ratio<=16/9)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory
				.createMediaQueryList("all and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))", mql.getMedia());
		assertEquals("all and (calc(2*2)/calc(9/3)<aspect-ratio<calc(4*4)/calc(3*3))", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList(
				"all and (calc(6 - 2)/calc(5 - 2) < aspect-ratio < calc(20 - 4)/calc(10 - 1))", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (calc(6 - 2)/calc(5 - 2) < aspect-ratio < calc(20 - 4)/calc(10 - 1))", mql.getMedia());
		assertEquals("all and (calc(6 - 2)/calc(5 - 2)<aspect-ratio<calc(20 - 4)/calc(10 - 1))",
				mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (color >= 2)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color >= 2)", mql.getMedia());
		assertEquals("all and (color>=2)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= color)", null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color >= 2)", mql.getMedia());
		assertEquals("all and (color>=2)", mql.getMinifiedMedia());
	}

	@Test
	public void testGetMediaLevel4_2() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("all and (color >= 2) and (resolution >= 96dpi)",
				null);
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color >= 2) and (resolution >= 96dpi)", mql.getMedia());
		assertEquals("all and (color>=2) and (resolution>=96dpi)", mql.getMinifiedMedia());
		// Backwards-compatible serialization
		mql = MediaQueryFactory.createMediaQueryList("(resolution = 300dpi) and (orientation: landscape)", null);
		assertFalse(mql.hasErrors());
		assertEquals("(resolution: 300dpi) and (orientation: landscape)", mql.getMedia());
		assertEquals("(resolution:300dpi) and (orientation:landscape)", mql.getMinifiedMedia());
	}

	@Test
	public void testGetCssMediaInvalid() {
		MediaQueryList mql;
		mql = createMediaQueryList("only screen and (color");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (min-color 4)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and {min-color: 4}");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("not all and");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and ()");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("tv and (orientation:)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("tv and only (orientation: landscape)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("(min-width: 700px) and");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("not");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("screen only");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("foo bar");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("and only");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("and screen");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("or screen");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and only");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (min-color: 4) and only");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (min-color: 4) and not");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (min-color: 4) and (not)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 <= min-color <)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 > min-color < 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 >= min-color < 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 > min-color <= 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 >= min-color =< 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 <= 4)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("only screen and (color: rgb(255, 165))");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("only screen and (color: #xxxz)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
	}

	@Test
	public void testGetCssMediaInvalid2() {
		MediaQueryList mql;
		mql = createMediaQueryList("all and (min-color 4), tv");
		assertFalse(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("tv", mql.getMedia());
	}

	@Test
	public void testGetCssMediaInvalid3() {
		MediaQueryList mql;
		// Example 18 of spec
		mql = createMediaQueryList("(example, all,), speech");
		assertFalse(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("speech", mql.getMedia());
	}

	@Test
	public void testGetCssMediaInvalid4() {
		MediaQueryList mql;
		mql = createMediaQueryList("&test, speech");
		assertFalse(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("speech", mql.getMedia());
		mql = createMediaQueryList("(example, speech");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
	}

	@Test
	public void testGetCssMediaInvalid5() {
		MediaQueryList mql;
		// Example 20 of spec
		mql = createMediaQueryList("or and (color)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
	}

	@Test
	public void testEquals() {
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("only screen and (color)", null);
		MediaQueryList mql2 = MediaQueryFactory.createMediaQueryList("only screen and (color)", null);
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		mql = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 5)", null);
		mql2 = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 5)", null);
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		mql2 = MediaQueryFactory.createMediaQueryList("all and (2 <= min-color < 4)", null);
		assertFalse(mql.equals(mql2));
	}

	@Test
	public void testMatchesPlainMedia() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("not screen", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
	}

	@Test
	public void testMatches() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		/*
		 * Grid
		 */
		MediaQueryList mql = MediaQueryFactory.createMediaQueryList("screen and (grid: 1)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (grid)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (grid)", mql.getMedia());
		assertEquals("screen and (grid)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (grid:0)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Scan
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (scan: interlace)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (scan: interlace)", mql.getMedia());
		assertEquals("screen and (scan:interlace)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (scan)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (scan: progressive)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Update
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (update: slow)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (update)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (update: fast)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (not (update: fast))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (not (update: fast))", mql.getMedia());
		assertEquals("screen and (not (update:fast))", mql.getMinifiedMedia());
		/*
		 * Overflow-block
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-block: none)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-block)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-block: scroll)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Overflow-inline
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-inline: none)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-inline)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (overflow-inline: scroll)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Pointer
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (pointer: coarse)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (pointer)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (pointer: fine)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Color-gamut
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (color-gamut: rec2020)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color-gamut)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color-gamut: srgb)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Color
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (color: 0)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-color: 8)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (color >= 8)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (color >= 8)", mql.getMedia());
		assertEquals("screen and (color>=8)", mql.getMinifiedMedia());
		/*
		 * Monochrome
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (monochrome: 1)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (monochrome)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (monochrome: 0)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Orientation
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (orientation: portrait)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (orientation)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (orientation: landscape)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Resolution
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution: 200dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (min-resolution: 200dpi)", mql.getMedia());
		assertEquals("screen and (min-resolution:200dpi)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution: 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution: 300dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution: 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// Wrong feature names and <=, >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution <= 200dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (min-resolution <= 200dpi)", mql.getMedia());
		assertEquals("screen and (min-resolution<=200dpi)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-resolution >= 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (min-resolution >= 72dpi)", mql.getMedia());
		assertEquals("screen and (min-resolution>=72dpi)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution <= 300dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-resolution <= 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution > 200dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (resolution > 200dpi)", mql.getMedia());
		assertEquals("screen and (resolution>200dpi)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution > 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution >= 200dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution >= 96dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution >= 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution < 300dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (resolution < 300dpi)", mql.getMedia());
		assertEquals("screen and (resolution<300dpi)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution < 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution <= 300dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution <= 96dpi)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (resolution <= 72dpi)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * width
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-width: 2000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-width: 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-width: 2000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-width: 250px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-width: 2000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-width: 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-width: 2000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-width: 250px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (width >= 2000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width >= 1024px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width >= 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (width <= 2000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width <= 1024px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width <= 250px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 2000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = MediaQueryFactory.createMediaQueryList("screen and (width < 2000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width < 250px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// =
		mql = MediaQueryFactory.createMediaQueryList("screen and (width = 1024px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width = 250px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// > calc
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > calc(200 * 1em))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > calc(60 * 1em))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// OR
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 1500px) or (height < 1000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("(min-width: 1500px) or (height < 1000px)", mql.getMedia());
		assertEquals("(min-width:1500px) or (height<1000px)", mql.getMinifiedMedia());
		// OR NOT
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 1500px) or (not (height < 1000px))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("(min-width: 1500px) or (not (height < 1000px))", mql.getMedia());
		assertEquals("(min-width:1500px) or (not (height<1000px))", mql.getMinifiedMedia());
		// OR NOT (II)
		mql = MediaQueryFactory.createMediaQueryList("(min-width: 1500px) or (not (height > 1000px))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("(min-width: 1500px) or (not (height > 1000px))", mql.getMedia());
		assertEquals("(min-width:1500px) or (not (height>1000px))", mql.getMinifiedMedia());
		// > relative unit
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 60em)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 100em)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (width > 100ex)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * height
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-height: 1000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-height: 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-height: 1000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-height: 500px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-height: 1000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-device-height: 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-height: 1000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-device-height: 500px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (height >= 1000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height >= 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height >= 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (height <= 1000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height <= 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height <= 500px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = MediaQueryFactory.createMediaQueryList("screen and (height > 1000px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height > 600px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = MediaQueryFactory.createMediaQueryList("screen and (height < 1000px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height < 500px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// =
		mql = MediaQueryFactory.createMediaQueryList("screen and (height = 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (height = 500px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >, <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px < height <= 1080px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (720px < height <= 1080px)", mql.getMedia());
		assertEquals("screen and (720px<height<=1080px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px < height <= 1080px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (768px < height <= 1080px)", mql.getMedia());
		assertEquals("screen and (768px<height<=1080px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px < height <= 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px < height <= 720px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=, <=
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px <= height <= 1080px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px <= height <= 1080px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px <= height <= 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px <= height <= 720px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=, <
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px <= height < 1080px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px <= height < 1080px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px <= height < 768px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px <= height < 1480px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >, <
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px < height < 1080px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (720px < height < 1080px)", mql.getMedia());
		assertEquals("screen and (720px<height<1080px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px < height < 1080px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (360px < height < 768px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <, >
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height > 720px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (1080px > height > 720px)", mql.getMedia());
		assertEquals("screen and (1080px>height>720px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height > 768px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px > height > 360px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=, >
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height > 720px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (1080px >= height > 720px)", mql.getMedia());
		assertEquals("screen and (1080px>=height>720px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height > 768px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px >= height > 360px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px >= height > 360px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <, >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height >= 720px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (1080px > height >= 720px)", mql.getMedia());
		assertEquals("screen and (1080px>height>=720px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px > height >= 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px > height >= 360px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=, >=
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height >= 800px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (1080px >= height >= 800px)", mql.getMedia());
		assertEquals("screen and (1080px>=height>=800px)", mql.getMinifiedMedia());
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height >= 720px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (1080px >= height >= 768px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (768px >= height >= 360px)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (720px >= height >= 360px)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * aspect-ratio
		 */
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-aspect-ratio: 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (min-aspect-ratio: 4/3)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = MediaQueryFactory.createMediaQueryList("screen and (max-aspect-ratio: 1024/768)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: 4/3)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: calc(2*8)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (aspect-ratio: calc(2*8)/calc(3*3))", mql.getMedia());
		assertEquals("screen and (aspect-ratio:calc(2*8)/calc(3*3))", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio: calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = 4/3)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < 16/9)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (aspect-ratio < 16/9)", mql.getMedia());
		assertEquals("screen and (aspect-ratio<16/9)", mql.getMinifiedMedia());
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < 4/3)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > 4/3)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= 16/9)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= 4/3)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= 4/3)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = calc(2*8)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio = calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < calc(2*8)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio < calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > calc(2*8)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio > calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= calc(2*8)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio <= calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= calc(2*8)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (aspect-ratio >= calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 < aspect-ratio < 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 < aspect-ratio <= 16/9)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 <= aspect-ratio <= 16/9)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (7/6 <= aspect-ratio <= 4/3)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (7/6 < aspect-ratio < 4/3)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 >= aspect-ratio >= 7/6)", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList("screen and (4/3 > aspect-ratio >= 7/6)", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * aspect-ratio + Calc + level 4 syntax
		 */
		//
		mql = MediaQueryFactory
				.createMediaQueryList("screen and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory
				.createMediaQueryList("screen and (calc(2*2)/calc(9/3) < aspect-ratio <= calc(4*4)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory
				.createMediaQueryList("screen and (calc(2*2)/calc(9/3) <= aspect-ratio <= calc(4*4)/calc(3*3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList(
				"screen and (calc(8 - 1)/calc(2*3) <= aspect-ratio <= calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory
				.createMediaQueryList("screen and (calc(8 - 1)/calc(2*3) < aspect-ratio < calc(2*2)/calc(9/3))", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory.createMediaQueryList(
				"screen and (calc(2*2)/calc(9/3) >= aspect-ratio >= calc(8 - 1)/calc(2*3))", null);
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = MediaQueryFactory
				.createMediaQueryList("screen and (calc(2*2)/calc(9/3) > aspect-ratio >= calc(8 - 1)/calc(2*3))", null);
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

	private MediaQueryList createMediaQueryList(String media) {
		return MediaQueryFactory.createMediaQueryList(media, null);
	}

}
