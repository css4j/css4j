/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Locator;

public class NestingTest {

	private CSSParser parser;
	private TestCSSHandler handler;
	private TestErrorHandler errorHandler;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
		handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
	}

	@AfterEach
	public void tearDown() throws Exception {
		handler.checkRuleEndings();
	}

	@Test
	public void testNestedStyleSheet() throws CSSException, IOException {
		try (Reader re = loadTestCSSReader("nesting.css")) {
			parser.parseStyleSheet(re);
		}

		assertEquals(15, handler.selectors.size());
		assertEquals(22, handler.nestedSelectors.size());
		assertEquals(22, handler.endNestedSelectors.size());

		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals("h1", handler.selectors.get(1).toString());
		assertEquals("h2", handler.selectors.get(2).toString());
		assertEquals("h3", handler.selectors.get(3).toString());
		assertEquals("p", handler.selectors.get(4).toString());
		assertEquals("p.boldmargin", handler.selectors.get(5).toString());
		assertEquals("p.smallitalic", handler.selectors.get(6).toString());
		assertEquals("#tablepara", handler.selectors.get(7).toString());
		assertEquals("li", handler.selectors.get(11).toString());
		assertEquals("div", handler.selectors.get(12).toString());

		assertEquals("g", handler.selectors.getLast().toString());

		assertEquals("& #span1", handler.nestedSelectors.get(0).toString());
		assertEquals("& #span1", handler.endNestedSelectors.get(0).toString());
		assertEquals("&>div", handler.nestedSelectors.get(1).toString());
		assertEquals("&>div", handler.endNestedSelectors.get(1).toString());
		assertEquals("&.cls", handler.nestedSelectors.get(2).toString());
		assertEquals("&+ul", handler.nestedSelectors.get(3).toString());
		assertEquals("&+ul", handler.endNestedSelectors.get(2).toString());
		assertEquals("&.cls", handler.endNestedSelectors.get(3).toString());
		assertEquals("&#h1", handler.nestedSelectors.get(4).toString());
		assertEquals("#firstH3", handler.nestedSelectors.get(5).toString());
		assertEquals("&#listpara", handler.nestedSelectors.get(6).toString());
		assertEquals("&>span", handler.nestedSelectors.get(7).toString());
		assertEquals("&#span1", handler.nestedSelectors.get(8).toString());
		assertEquals(".small &", handler.nestedSelectors.get(9).toString());
		assertEquals("&+p", handler.nestedSelectors.get(10).toString());
		assertEquals("&~#span1", handler.nestedSelectors.get(11).toString());
		assertEquals(":not(&)~p", handler.nestedSelectors.get(12).toString());
		assertEquals("&.test1", handler.nestedSelectors.get(13).toString());
		assertEquals("& span", handler.nestedSelectors.get(14).toString());
		assertEquals("&.label", handler.nestedSelectors.get(15).toString());
		assertEquals("&:first-child", handler.nestedSelectors.get(16).toString());
		assertEquals("&:nth-last-child(2)", handler.nestedSelectors.get(17).toString());
		assertEquals("&>div", handler.nestedSelectors.get(18).toString());
		assertEquals("&>foreignObject", handler.nestedSelectors.get(19).toString());
		assertEquals("& div", handler.nestedSelectors.get(20).toString());
		assertEquals("&>rect", handler.nestedSelectors.get(21).toString());

		assertEquals(4, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals("screen", handler.mediaRuleLists.get(1).toString());

		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Font H3", handler.fontFeaturesNames.get(0)[0]);

		assertEquals(1, handler.comments.size());
		assertEquals(" Equivalent to p#listpara ", handler.comments.get(0));

		assertEquals(78, handler.propertyNames.size());
		assertEquals(78, handler.lexicalValues.size());
		assertEquals(78, handler.priorities.size());

		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("body", handler.propertySelectors.getFirst().toString());

		assertEquals("font-size", handler.propertyNames.get(1));
		assertEquals("12pt", handler.lexicalValues.get(1).toString());
		assertEquals("body", handler.propertySelectors.get(1).toString());

		assertEquals("margin-left", handler.propertyNames.get(2));
		assertEquals("5%", handler.lexicalValues.get(2).toString());
		assertEquals("body", handler.propertySelectors.get(2).toString());

		assertEquals("--my-text-align", handler.propertyNames.get(11));
		assertEquals("left", handler.lexicalValues.get(11).toString());
		assertEquals("body", handler.propertySelectors.get(11).toString());

		assertEquals("--my-bg-color", handler.propertyNames.get(15));
		assertEquals("#226", handler.lexicalValues.get(15).toString());
		assertEquals("&.cls", handler.propertySelectors.get(15).toString());

		assertEquals("font-size", handler.propertyNames.get(18));
		assertEquals("3em", handler.lexicalValues.get(18).toString());
		assertEquals("h1", handler.propertySelectors.get(18).toString());

		assertEquals("font-weight", handler.propertyNames.get(19));
		assertEquals("bold", handler.lexicalValues.get(19).toString());
		assertEquals("h1", handler.propertySelectors.get(19).toString());

		assertEquals("swishy", handler.propertyNames.get(30));
		assertEquals("1", handler.lexicalValues.get(30).toString());
		assertEquals("#firstH3", handler.propertySelectors.get(30).toString());

		assertEquals("font-size", handler.propertyNames.get(37));
		assertEquals("150%", handler.lexicalValues.get(37).toString());
		assertEquals("&>span", handler.propertySelectors.get(37).toString());

		assertEquals("color", handler.propertyNames.get(48));
		assertEquals("green", handler.lexicalValues.get(48).toString());
		assertEquals("#tablepara", handler.propertySelectors.get(48).toString());

		assertEquals("padding-left", handler.propertyNames.get(53));
		assertEquals("0", handler.lexicalValues.get(53).toString());
		assertEquals("ul,ol", handler.propertySelectors.get(53).toString());

		assertEquals("text-align", handler.propertyNames.get(54));
		assertEquals("match-parent", handler.lexicalValues.get(54).toString());
		assertEquals(":not(&)~p", handler.propertySelectors.get(54).toString());

		assertEquals("padding-right", handler.propertyNames.get(55));
		assertEquals("1px", handler.lexicalValues.get(55).toString());
		assertEquals("ul,ol", handler.propertySelectors.get(55).toString());

		assertEquals("margin-top", handler.propertyNames.get(56));
		assertEquals("1em", handler.lexicalValues.get(56).toString());
		assertEquals("ul,ol", handler.propertySelectors.get(56).toString());

		assertEquals("background-color", handler.propertyNames.get(70));
		assertEquals("#eec", handler.lexicalValues.get(70).toString());
		assertEquals("&:nth-last-child(2)", handler.propertySelectors.get(70).toString());

		assertEquals("stroke", handler.propertyNames.get(71));
		assertEquals("currentColor", handler.lexicalValues.get(71).toString());
		assertEquals("&.label", handler.propertySelectors.get(71).toString());

		assertEquals("display", handler.propertyNames.get(72));
		assertEquals("inline-block", handler.lexicalValues.get(72).toString());
		assertEquals("&>div", handler.propertySelectors.get(72).toString());

		assertEquals("fill", handler.propertyNames.getLast());
		assertEquals("#469", handler.lexicalValues.getLast().toString());
		assertEquals("&>rect", handler.propertySelectors.getLast().toString());

		Locator loc = handler.ptyLocators.get(2);
		assertEquals(4, loc.getLineNumber());
		assertEquals(18, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(5, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());
		assertEquals(10, handler.ptyLocators.get(7).getLineNumber());
		loc = handler.ptyLocators.get(10);
		assertEquals(14, loc.getLineNumber());
		assertEquals(41, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	private static Reader loadTestCSSReader(String filename) {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/parser/" + filename);
	}

	private static Reader loadCSSfromClasspath(final String filename) {
		InputStream is = SheetParserTest.class.getResourceAsStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

}
