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
import static org.junit.Assert.fail;

import java.util.EnumSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class MediaQueryTest {

	private static TestCSSStyleSheetFactory factory;

	@BeforeClass
	public static void setUpBeforeClass() {
		factory = new TestCSSStyleSheetFactory();
	}

	@Test
	public void testBasicQueries() {
		MediaQueryList mql;
		mql = createMediaQueryList("not screen");
		assertFalse(mql.hasErrors());
		assertEquals("not screen", mql.getMedia());
		assertEquals("not screen", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("only screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color)", mql.getMedia());
		assertEquals("only screen and (color)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("only screen");
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
		assertEquals("only screen", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("screen, print");
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		assertEquals("screen,print", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("screen, print");
		assertFalse(mql.hasErrors());
		assertEquals("screen,print", mql.getMedia());
		assertEquals("screen,print", mql.getMinifiedMedia());
	}

	@Test
	public void testMediaQueries() {
		MediaQueryList mql;
		//
		mql = createMediaQueryList("all and (min-color: 4)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: 4)", mql.getMedia());
		assertEquals("all and (min-color:4)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (min-color: calc(2*2))");
		assertFalse(mql.hasErrors());
		assertEquals("all and (min-color: calc(2*2))", mql.getMedia());
		assertEquals("all and (min-color:calc(2*2))", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (color-index)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (color-index)", mql.getMedia());
		assertEquals("all and (color-index)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (max-width:47.9375em)");
		assertFalse(mql.hasErrors());
		assertEquals("all and (max-width: 47.9375em)", mql.getMedia());
		assertEquals("all and (max-width:47.9375em)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("tv and (min-width: 700px) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("tv and (min-width: 700px) and (orientation: landscape)", mql.getMedia());
		assertEquals("tv and (min-width:700px) and (orientation:landscape)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("(min-width: 700px) and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertEquals("(min-width: 700px) and (orientation: landscape)", mql.getMedia());
		assertEquals("(min-width:700px) and (orientation:landscape)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("only screen and (color: rgb(255, 4, 165))");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (color: #ff04a5)", mql.getMedia());
		assertEquals("only screen and (color:#ff04a5)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("only screen and (min-width:690px) and (max-width:780px)");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (min-width: 690px) and (max-width: 780px)", mql.getMedia());
		assertEquals("only screen and (min-width:690px) and (max-width:780px)", mql.getMinifiedMedia());
	}

	@Test
	public void testGetMediaNL() {
		MediaQueryList mql = createMediaQueryList("only\nscreen");
		assertFalse(mql.hasErrors());
		assertEquals("only screen", mql.getMedia());
	}

	@Test
	public void testGetMediaEscaped() {
		MediaQueryList mql = createMediaQueryList("only \\9 screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only \\9 screen and (color)", mql.getMedia());
		mql = createMediaQueryList("only \\9 screen and (color)");
		assertFalse(mql.hasErrors());
		assertEquals("only \\9 screen and (color)", mql.getMedia());
		mql = createMediaQueryList("(min-width: 700px) and (orientation: \\9 foo)");
		assertEquals("(min-width: 700px) and (orientation: \\9 foo)", mql.getMedia());
		assertFalse(mql.hasErrors());
	}

	@Test
	public void testGetMediaEscapedBad() {
		MediaQueryList mql = createMediaQueryList("only \\9screen and (color)");
		assertEquals("only \\9screen and (color)", mql.getMedia());
		assertFalse(mql.hasErrors());
	}

	@Test
	public void testGetMediaRatio() {
		MediaQueryList mql = createMediaQueryList("(max-aspect-ratio:160/100) and (min-width:300px)");
		assertFalse(mql.hasErrors());
		assertEquals("(max-aspect-ratio: 160/100) and (min-width: 300px)", mql.getMedia());
		assertEquals("(max-aspect-ratio:160/100) and (min-width:300px)", mql.getMinifiedMedia());
		mql.item(0);
	}

	@Test
	public void testGetMediaLevel4() {
		MediaQueryList mql = createMediaQueryList("all and (2 <= color < 5)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 <= color < 5)", mql.getMedia());
		assertEquals("all and (2<=color<5)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (2 < color <= 5)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 < color <= 5)", mql.getMedia());
		assertEquals("all and (2<color<=5)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (2 < color < 5)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 < color < 5)", mql.getMedia());
		assertEquals("all and (2<color<5)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (2 <= color <= 5)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (2 <= color <= 5)", mql.getMedia());
		assertEquals("all and (2<=color<=5)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (5 > color > 2)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 > color > 2)", mql.getMedia());
		assertEquals("all and (5>color>2)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (5 >= color > 2)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 >= color > 2)", mql.getMedia());
		assertEquals("all and (5>=color>2)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (5 > color >= 2)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 > color >= 2)", mql.getMedia());
		assertEquals("all and (5>color>=2)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (5 >= color >= 2)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (5 >= color >= 2)", mql.getMedia());
		assertEquals("all and (5>=color>=2)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (calc(4/2) < color < calc(4 + 1))");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (calc(4/2) < color < calc(4 + 1))", mql.getMedia());
		assertEquals("all and (calc(4/2)<color<calc(4 + 1))", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (4/3 < aspect-ratio < 16/9)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 < aspect-ratio < 16/9)", mql.getMedia());
		assertEquals("all and (4/3<aspect-ratio<16/9)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (4/3 <= aspect-ratio < 16/9)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 <= aspect-ratio < 16/9)", mql.getMedia());
		assertEquals("all and (4/3<=aspect-ratio<16/9)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (4/3 < aspect-ratio <= 16/9)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 < aspect-ratio <= 16/9)", mql.getMedia());
		assertEquals("all and (4/3<aspect-ratio<=16/9)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (4/3 <= aspect-ratio <= 16/9)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (4/3 <= aspect-ratio <= 16/9)", mql.getMedia());
		assertEquals("all and (4/3<=aspect-ratio<=16/9)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))", mql.getMedia());
		assertEquals("all and (calc(2*2)/calc(9/3)<aspect-ratio<calc(4*4)/calc(3*3))", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (calc(6 - 2)/calc(5 - 2) < aspect-ratio < calc(20 - 4)/calc(10 - 1))");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (calc(6 - 2)/calc(5 - 2) < aspect-ratio < calc(20 - 4)/calc(10 - 1))", mql.getMedia());
		assertEquals("all and (calc(6 - 2)/calc(5 - 2)<aspect-ratio<calc(20 - 4)/calc(10 - 1))",
				mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (color >= 2)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color >= 2)", mql.getMedia());
		assertEquals("all and (color>=2)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all and (2 <= color)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color >= 2)", mql.getMedia());
		assertEquals("all and (color>=2)", mql.getMinifiedMedia());
	}

	@Test
	public void testGetMediaLevel4_2() {
		MediaQueryList mql = createMediaQueryList("all and (color >= 2) and (resolution >= 96dpi)");
		assertNotNull(mql);
		assertFalse(mql.hasErrors());
		assertEquals("all and (color >= 2) and (resolution >= 96dpi)", mql.getMedia());
		assertEquals("all and (color>=2) and (resolution>=96dpi)", mql.getMinifiedMedia());
		// Backwards-compatible serialization
		mql = createMediaQueryList("(resolution = 300dpi) and (orientation: landscape)");
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
	}

	@Test
	public void testGetCssMediaInvalid5() {
		MediaQueryList mql;
		mql = createMediaQueryList("(example, speech");
		assertTrue(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("not all", mql.getMedia());
		assertEquals("not all", mql.getMinifiedMedia());
	}

	@Test
	public void testGetCssMediaInvalid6() {
		MediaQueryList mql;
		// Example 20 of spec
		mql = createMediaQueryList("or and (color)");
		assertTrue(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("not all", mql.getMedia());
		assertEquals("not all", mql.getMinifiedMedia());
	}

	@Test
	public void testGetCssMediaInvalidCompat() throws DOMException, ParserConfigurationException, CSSMediaException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setAttribute("media", "screen");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser2.Flag.IEVALUES));
		CSSDocument cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		MediaQueryList mql = factory.parseMediaQueryList("screen and (min-width:0\\0)", cssStyle);
		assertTrue(mql.isNotAllMedia());
		assertTrue(mql.hasErrors());
		assertEquals("screen and (min-width: 0\\0)", mql.getMedia());
		assertEquals("screen and (min-width:0\\0)", mql.getMinifiedMedia());
	}

	@Test
	public void testEquals() {
		MediaQueryList mql = createMediaQueryList("only screen and (color)");
		assertFalse(mql.equals(null));
		MediaQueryList mql2 = createMediaQueryList("only screen and (color)");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		//
		mql2.setMediaText("only screen and (color),tv");
		assertFalse(mql.equals(mql2));
		//
		mql2.setMediaText("screen,tv");
		assertFalse(mql.equals(mql2));
		//
		mql = createMediaQueryList("all and (2 <= min-color < 5)");
		mql2 = createMediaQueryList("all and (2 <= min-color < 5)");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		//
		mql2 = createMediaQueryList("all and (2 <= min-color < 4)");
		assertFalse(mql.equals(mql2));
		//
		mql2.setMediaText("screen");
		assertFalse(mql.equals(mql2));
		//
		mql2.setMediaText(mql.getMedia());
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
	}

	@Test
	public void testMatch() {
		MediaQueryList mql = createMediaQueryList("tv,screen and (color)");
		MediaQueryList mql2 = createMediaQueryList("screen and (color)");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		//
		mql2 = createMediaQueryList("tv,all and (2 <= min-color < 5)");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertEquals(mql.item(0), mql2.item(0));
		//
		mql2 = createMediaQueryList("all and (2 <= min-color < 4)");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
	}

	@Test
	public void testAppendMedium() {
		MediaQueryList mql = createMediaQueryList("screen");
		assertFalse(mql.hasErrors());
		mql.appendMedium("tv");
		assertEquals(2, mql.getLength());
		assertEquals("screen", mql.item(0));
		assertEquals("tv", mql.item(1));
	}

	@Test
	public void testUnmodifiable() {
		MediaQueryList mql = factory.createUnmodifiableMediaQueryList("screen", null);
		assertFalse(mql.hasErrors());
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		//
		try {
			mql.appendMedium("tv");
			fail("Must throw exception.");
		} catch (DOMException e) {
			
		}
		assertEquals(1, mql.getLength());
		assertEquals("screen", mql.item(0));
		//
		try {
			mql.setMediaText("tv");
			fail("Must throw exception.");
		} catch (DOMException e) {
			
		}
		assertEquals(1, mql.getLength());
		assertEquals("screen", mql.item(0));
	}

	@Test
	public void testUnmodifiable2() {
		MediaQueryList modifiable = createMediaQueryList("screen");
		MediaQueryList unmodif = ((MediaListAccess) modifiable).unmodifiable();
		assertFalse(unmodif.hasErrors());
		assertFalse(unmodif.isAllMedia());
		assertFalse(unmodif.isNotAllMedia());
		//
		try {
			unmodif.appendMedium("tv");
			fail("Must throw exception.");
		} catch (DOMException e) {
			
		}
		assertEquals(1, unmodif.getLength());
		assertEquals("screen", unmodif.item(0));
		//
		try {
			unmodif.setMediaText("tv");
			fail("Must throw exception.");
		} catch (DOMException e) {
			
		}
		assertEquals(1, unmodif.getLength());
		assertEquals("screen", unmodif.item(0));
		//
		assertTrue(unmodif == ((MediaListAccess) unmodif).unmodifiable());
		assertTrue(modifiable.equals(unmodif));
		assertEquals(modifiable.hashCode(), unmodif.hashCode());
		assertTrue(modifiable.matches(unmodif));
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		assertTrue(unmodif.matches("screen", canvas));
	}

	@Test
	public void testMatchesPlainMedia() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		MediaQueryList mql = createMediaQueryList("not screen");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
	}

	@Test
	public void testMatchesPlainMedia2() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		MediaQueryList mql = createMediaQueryList("screen");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
	}

	@Test
	public void testMatches() {
		CSSCanvas canvas = factory.getDeviceFactory().createCanvas("screen", null);
		/*
		 * Grid
		 */
		MediaQueryList mql = createMediaQueryList("screen and (grid: 1)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (grid)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (grid)", mql.getMedia());
		assertEquals("screen and (grid)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (grid:0)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Scan
		 */
		mql = createMediaQueryList("screen and (scan: interlace)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (scan: interlace)", mql.getMedia());
		assertEquals("screen and (scan:interlace)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (scan)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (scan: progressive)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Update
		 */
		mql = createMediaQueryList("screen and (update: slow)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (update)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (update: fast)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (not (update: fast))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (not (update: fast))", mql.getMedia());
		assertEquals("screen and (not (update:fast))", mql.getMinifiedMedia());
		/*
		 * Overflow-block
		 */
		mql = createMediaQueryList("screen and (overflow-block: none)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (overflow-block)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (overflow-block: scroll)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Overflow-inline
		 */
		mql = createMediaQueryList("screen and (overflow-inline: none)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (overflow-inline)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (overflow-inline: scroll)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Pointer
		 */
		mql = createMediaQueryList("screen and (pointer: coarse)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (pointer)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (pointer: fine)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Color-gamut
		 */
		mql = createMediaQueryList("screen and (color-gamut: rec2020)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (color-gamut)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (color-gamut: srgb)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Color
		 */
		mql = createMediaQueryList("screen and (color: 0)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (color)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (min-color: 8)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (color >= 8)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (color >= 8)", mql.getMedia());
		assertEquals("screen and (color>=8)", mql.getMinifiedMedia());
		/*
		 * Monochrome
		 */
		mql = createMediaQueryList("screen and (monochrome: 1)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (monochrome)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (monochrome: 0)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Orientation
		 */
		mql = createMediaQueryList("screen and (orientation: portrait)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (orientation)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (orientation: landscape)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * Resolution
		 */
		mql = createMediaQueryList("screen and (min-resolution: 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (min-resolution: 200dpi)", mql.getMedia());
		assertEquals("screen and (min-resolution:200dpi)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (min-resolution: 72dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-resolution: 300dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-resolution: 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// Wrong feature names and <=, >=
		mql = createMediaQueryList("screen and (min-resolution <= 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (min-resolution <= 200dpi)", mql.getMedia());
		assertEquals("screen and (min-resolution<=200dpi)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (min-resolution >= 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (min-resolution >= 72dpi)", mql.getMedia());
		assertEquals("screen and (min-resolution>=72dpi)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (max-resolution <= 300dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-resolution <= 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = createMediaQueryList("screen and (resolution > 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (resolution > 200dpi)", mql.getMedia());
		assertEquals("screen and (resolution>200dpi)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (resolution > 72dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// >=
		mql = createMediaQueryList("screen and (resolution >= 200dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (resolution >= 96dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (resolution >= 72dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = createMediaQueryList("screen and (resolution < 300dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (resolution < 300dpi)", mql.getMedia());
		assertEquals("screen and (resolution<300dpi)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (resolution < 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=
		mql = createMediaQueryList("screen and (resolution <= 300dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (resolution <= 96dpi)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (resolution <= 72dpi)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * width
		 */
		mql = createMediaQueryList("screen and (min-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (min-width: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-width: 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (min-device-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (min-device-width: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-device-width: 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-device-width: 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=
		mql = createMediaQueryList("screen and (width >= 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width >= 1024px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width >= 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <=
		mql = createMediaQueryList("screen and (width <= 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width <= 1024px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width <= 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = createMediaQueryList("screen and (width > 2000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width > 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = createMediaQueryList("screen and (width < 2000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width < 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// =
		mql = createMediaQueryList("screen and (width = 1024px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width = 250px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// > calc
		mql = createMediaQueryList("screen and (width > calc(200 * 1em))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width > calc(60 * 1em))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// OR
		mql = createMediaQueryList("(min-width: 1500px) or (height < 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("(min-width: 1500px) or (height < 1000px)", mql.getMedia());
		assertEquals("(min-width:1500px) or (height<1000px)", mql.getMinifiedMedia());
		// OR NOT
		mql = createMediaQueryList("(min-width: 1500px) or (not (height < 1000px))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("(min-width: 1500px) or (not (height < 1000px))", mql.getMedia());
		assertEquals("(min-width:1500px) or (not (height<1000px))", mql.getMinifiedMedia());
		// OR NOT (II)
		mql = createMediaQueryList("(min-width: 1500px) or (not (height > 1000px))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("(min-width: 1500px) or (not (height > 1000px))", mql.getMedia());
		assertEquals("(min-width:1500px) or (not (height>1000px))", mql.getMinifiedMedia());
		// > relative unit
		mql = createMediaQueryList("screen and (width > 60em)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width > 100em)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (width > 100ex)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		/*
		 * height
		 */
		mql = createMediaQueryList("screen and (min-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (min-height: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-height: 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (min-device-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (min-device-height: 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-device-height: 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-device-height: 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=
		mql = createMediaQueryList("screen and (height >= 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height >= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height >= 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <=
		mql = createMediaQueryList("screen and (height <= 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height <= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height <= 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >
		mql = createMediaQueryList("screen and (height > 1000px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height > 600px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		// <
		mql = createMediaQueryList("screen and (height < 1000px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height < 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// =
		mql = createMediaQueryList("screen and (height = 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (height = 500px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >, <=
		mql = createMediaQueryList("screen and (720px < height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (720px < height <= 1080px)", mql.getMedia());
		assertEquals("screen and (720px<height<=1080px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (768px < height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (768px < height <= 1080px)", mql.getMedia());
		assertEquals("screen and (768px<height<=1080px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (720px < height <= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (360px < height <= 720px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=, <=
		mql = createMediaQueryList("screen and (720px <= height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (768px <= height <= 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (720px <= height <= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (360px <= height <= 720px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >=, <
		mql = createMediaQueryList("screen and (720px <= height < 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (768px <= height < 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (360px <= height < 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (1080px <= height < 1480px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// >, <
		mql = createMediaQueryList("screen and (720px < height < 1080px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (720px < height < 1080px)", mql.getMedia());
		assertEquals("screen and (720px<height<1080px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (768px < height < 1080px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (360px < height < 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <, >
		mql = createMediaQueryList("screen and (1080px > height > 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (1080px > height > 720px)", mql.getMedia());
		assertEquals("screen and (1080px>height>720px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (1080px > height > 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (768px > height > 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=, >
		mql = createMediaQueryList("screen and (1080px >= height > 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (1080px >= height > 720px)", mql.getMedia());
		assertEquals("screen and (1080px>=height>720px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (1080px >= height > 768px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (768px >= height > 360px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (720px >= height > 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <, >=
		mql = createMediaQueryList("screen and (1080px > height >= 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (1080px > height >= 720px)", mql.getMedia());
		assertEquals("screen and (1080px>height>=720px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (1080px > height >= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (768px > height >= 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		// <=, >=
		mql = createMediaQueryList("screen and (1080px >= height >= 800px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (1080px >= height >= 800px)", mql.getMedia());
		assertEquals("screen and (1080px>=height>=800px)", mql.getMinifiedMedia());
		mql = createMediaQueryList("screen and (1080px >= height >= 720px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (1080px >= height >= 768px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (768px >= height >= 360px)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (720px >= height >= 360px)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * aspect-ratio
		 */
		mql = createMediaQueryList("screen and (min-aspect-ratio: 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (min-aspect-ratio: 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		mql = createMediaQueryList("screen and (max-aspect-ratio: 1024/768)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio: 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio: 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio: calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		assertEquals("screen and (aspect-ratio: calc(2*8)/calc(3*3))", mql.getMedia());
		assertEquals("screen and (aspect-ratio:calc(2*8)/calc(3*3))", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("screen and (aspect-ratio: calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio = 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio = 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio < 16/9)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		assertEquals("screen and (aspect-ratio < 16/9)", mql.getMedia());
		assertEquals("screen and (aspect-ratio<16/9)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("screen and (aspect-ratio > 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio < 4/3)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio > 4/3)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio <= 16/9)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio >= 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio <= 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio >= 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio = calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio = calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio < calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio < calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio > calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio > calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio <= calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio <= calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio >= calc(2*8)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (aspect-ratio >= calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (4/3 < aspect-ratio < 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (4/3 < aspect-ratio <= 16/9)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (4/3 <= aspect-ratio <= 16/9)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (7/6 <= aspect-ratio <= 4/3)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (7/6 < aspect-ratio < 4/3)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (4/3 >= aspect-ratio >= 7/6)");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (4/3 > aspect-ratio >= 7/6)");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		/*
		 * aspect-ratio + Calc + level 4 syntax
		 */
		//
		mql = createMediaQueryList("screen and (calc(2*2)/calc(9/3) < aspect-ratio < calc(4*4)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (calc(2*2)/calc(9/3) < aspect-ratio <= calc(4*4)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (calc(2*2)/calc(9/3) <= aspect-ratio <= calc(4*4)/calc(3*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (calc(8 - 1)/calc(2*3) <= aspect-ratio <= calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (calc(8 - 1)/calc(2*3) < aspect-ratio < calc(2*2)/calc(9/3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (calc(2*2)/calc(9/3) >= aspect-ratio >= calc(8 - 1)/calc(2*3))");
		assertFalse(mql.hasErrors());
		assertTrue(mql.matches("screen", canvas));
		//
		mql = createMediaQueryList("screen and (calc(2*2)/calc(9/3) > aspect-ratio >= calc(8 - 1)/calc(2*3))");
		assertFalse(mql.hasErrors());
		assertFalse(mql.matches("screen", canvas));
	}

	@Test
	public void testIsPlainMediaList() {
		assertTrue(BaseCSSStyleSheetFactory.isPlainMediaList("screen, tv"));
		assertTrue(BaseCSSStyleSheetFactory.isPlainMediaList("print"));
		assertFalse(BaseCSSStyleSheetFactory.isPlainMediaList("not screen"));
		assertFalse(BaseCSSStyleSheetFactory.isPlainMediaList("screen and (color)"));
	}

	private MediaQueryList createMediaQueryList(String media) {
		return factory.parseMediaQueryList(media, null);
	}

}
