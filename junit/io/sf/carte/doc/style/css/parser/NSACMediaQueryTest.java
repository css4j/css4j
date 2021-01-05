/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class NSACMediaQueryTest {

	@Test
	public void testBasicQueries() {
		MediaQueryList mql;
		mql = createMediaQueryList("all");
		assertTrue(mql.isAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMedia());
		assertEquals("all", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("not all");
		assertFalse(mql.isAllMedia());
		assertTrue(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("not all", mql.getMedia());
		assertEquals("not all", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("not all, not all");
		assertFalse(mql.isAllMedia());
		assertTrue(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("not all", mql.getMedia());
		assertEquals("not all", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("not all, screen");
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("not all,screen", mql.getMedia());
		assertEquals("not all,screen", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("all");
		assertTrue(mql.isAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", mql.getMedia());
		assertEquals("all", mql.getMinifiedMedia());
		//
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
		assertEquals("only screen and (color: rgb(255, 4, 165))", mql.getMedia());
		assertEquals("only screen and (color:rgb(255, 4, 165))", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("only screen and (min-width:690px) and (max-width:780px)");
		assertFalse(mql.hasErrors());
		assertEquals("only screen and (min-width: 690px) and (max-width: 780px)", mql.getMedia());
		assertEquals("only screen and (min-width:690px) and (max-width:780px)", mql.getMinifiedMedia());
		//
		mql = createMediaQueryList("(not (max-width:500px))");
		assertFalse(mql.hasErrors());
		assertEquals("not (max-width: 500px)", mql.getMedia());
		assertEquals("not (max-width:500px)", mql.getMinifiedMedia());
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
	public void testGetMediaEscapedCompat() {
		MediaQueryList mql = createMediaQueryList("screen\\0 ");
		assertEquals("screen\\0", mql.getMedia());
		assertFalse(mql.hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser.Flag.IEVALUES));
		mql = factory.createMediaQueryList("screen\\0 ", null);
		assertEquals("screen\\0", mql.getMedia());
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
		mql = createMediaQueryList("all and (2 <= color <)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 > color < 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 >= color < 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 > color <= 5)");
		assertTrue(mql.isNotAllMedia() && mql.hasErrors());
		mql = createMediaQueryList("all and (2 >= color =< 5)");
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
	public void testGetCssMediaInvalidCompat() throws DOMException, ParserConfigurationException {
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
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser.Flag.IEVALUES));
		CSSDocument cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		NSACMediaQueryList mql = new NSACMediaQueryList();
		CSSParser parser = new CSSParser();
		parser.setFlag(Parser.Flag.IEVALUES);
		mql.parse(parser, "screen and (min-width:0\\0)", cssStyle);
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
		mql2 = createMediaQueryList("only screen and (color),tv");
		assertFalse(mql.equals(mql2));
		//
		mql2 = createMediaQueryList("screen,tv");
		assertFalse(mql.equals(mql2));
		//
		mql = createMediaQueryList("all and (2 <= color < 5)");
		mql2 = createMediaQueryList("all and (2 <= color < 5)");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		//
		mql2 = createMediaQueryList("all and (2 <= color < 4)");
		assertFalse(mql.equals(mql2));
		//
		mql2 = createMediaQueryList("screen");
		assertFalse(mql.equals(mql2));
		//
		mql2 = createMediaQueryList(mql.getMedia());
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
		//
		mql.setMediaText("screen");
		mql2.setMediaText("screen");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.hashCode() == mql2.hashCode());
	}

	@Test
	public void testMatch() {
		MediaQueryList mql = createMediaQueryList("all");
		MediaQueryList mql2 = createMediaQueryList("all");
		assertFalse(mql.isNotAllMedia());
		assertTrue(mql.isAllMedia());
		assertFalse(mql.hasErrors());
		assertTrue(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertTrue(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("all and (color) and (min-width:600px)");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("screen");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("screen and (color) and (min-width:600px)");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
	}

	@Test
	public void testMatchNotAll() {
		MediaQueryList mql = createMediaQueryList("all");
		MediaQueryList mql2 = createMediaQueryList("not all");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql = createMediaQueryList("not all");
		assertTrue(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("all");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("screen");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("not screen");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
	}

	@Test
	public void testMatchNotAll2() {
		MediaQueryList mql = createMediaQueryList("not screen");
		MediaQueryList mql2 = createMediaQueryList("not screen");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertTrue(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("screen");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("print");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
	}

	@Test
	public void testMatchOld() {
		MediaQueryList mql = createMediaQueryList("all");
		MediaQueryList mql2 = MediaList.createMediaList("screen, print");
		assertFalse(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
		assertFalse(mql2.matches(mql));
	}

	/*
	 * Do a match with a media query list that has 2 media queries.
	 */
	@Test
	public void testMatchList() {
		MediaQueryList mql = createMediaQueryList("tv");
		MediaQueryList mql2 = createMediaQueryList("tv,screen and (color)");
		assertFalse(mql.equals(mql2));
		assertFalse(mql.matches(mql2));
		assertTrue(mql2.matches(mql));
		//
		mql2 = createMediaQueryList("tv");
		assertTrue(mql.equals(mql2));
		assertTrue(mql.matches(mql2));
	}

	@Test
	public void testAppendMedium() {
		MediaQueryList mql = createMediaQueryList("screen");
		assertFalse(mql.hasErrors());
		try {
			mql.appendMedium("tv");
			fail("Must throw exception.");
		} catch (DOMException e) {
		}
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

	private MediaQueryList createMediaQueryList(String media) {
		NSACMediaQueryList list = new NSACMediaQueryList();
		list.parse(media, null);
		return list;
	}

}
