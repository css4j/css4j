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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import org.junit.BeforeClass;
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Parser;

public class ContribSheetTest {

	private static Parser cssParser;

	@BeforeClass
	public static void setUpBeforeClass() {
		cssParser = new CSSOMParser();
	}

	@Test
	public void testParseFontAwesomeCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadFontAwesomeReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_PRECEDING));
		cssParser.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(1404, len);
		assertFalse(css.getErrorHandler().hasSacErrors());
		List<String> comments = rules.item(0).getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.size());
		BaseCSSStyleSheet reparsed = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		re = new StringReader(css.toString());
		assertTrue(reparsed.parseStyleSheet(re, CSSStyleSheet.COMMENTS_PRECEDING));
		CSSRuleArrayList reparsedrules = reparsed.getCssRules();
		assertEquals(len, reparsedrules.getLength());
		List<String> reparsedcomments = reparsedrules.item(0).getPrecedingComments();
		assertNotNull(reparsedcomments);
		assertEquals(1, reparsedcomments.size());
		assertEquals(comments.get(0), reparsedcomments.get(0));
		for (int i = 0; i < len; i++) {
			assertTrue(rules.item(i).equals(reparsedrules.item(i)));
		}
	}

	@Test
	public void testParseNormalizeCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadNormalizeReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_PRECEDING));
		cssParser.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(34, len);
		assertFalse(css.getErrorHandler().hasSacErrors());
		List<String> comments = rules.item(4).getPrecedingComments();
		assertNotNull(comments);
		assertEquals(2, comments.size());
		assertEquals(" Grouping content", comments.get(0).substring(0, 17));
		BaseCSSStyleSheet reparsed = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		re = new StringReader(css.toString());
		assertTrue(reparsed.parseStyleSheet(re, CSSStyleSheet.COMMENTS_PRECEDING));
		CSSRuleArrayList reparsedrules = reparsed.getCssRules();
		assertEquals(len, reparsedrules.getLength());
		List<String> reparsedcomments = reparsedrules.item(4).getPrecedingComments();
		assertNotNull(reparsedcomments);
		assertEquals(2, reparsedcomments.size());
		assertEquals(comments.get(0), reparsedcomments.get(0));
		for (int i = 0; i < len; i++) {
			AbstractCSSRule rule = rules.item(i);
			AbstractCSSRule reparsedrule = reparsedrules.item(i);
			assertTrue(rule.equals(reparsedrule));
			assertTrue(Objects.equals(rule.getPrecedingComments(), reparsedrule.getPrecedingComments()));
			assertTrue(Objects.equals(rule.getTrailingComments(), reparsedrule.getTrailingComments()));
		}
	}

	@Test
	public void testParseAnimateCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadAnimateReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_AUTO));
		cssParser.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(246, len);
		assertFalse(css.getErrorHandler().hasSacErrors());
		List<String> comments = rules.item(0).getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.size());
		BaseCSSStyleSheet reparsed = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		re = new StringReader(css.toString());
		assertTrue(reparsed.parseStyleSheet(re));
		CSSRuleArrayList reparsedrules = reparsed.getCssRules();
		assertEquals(len, reparsedrules.getLength());
		List<String> reparsedcomments = reparsedrules.item(0).getPrecedingComments();
		assertNotNull(reparsedcomments);
		assertEquals(1, reparsedcomments.size());
		assertEquals(comments.get(0), reparsedcomments.get(0));
		for (int i = 0; i < len; i++) {
			AbstractCSSRule rule = rules.item(i);
			AbstractCSSRule reparsedrule = reparsedrules.item(i);
			assertTrue(rule.equals(reparsedrule));
		}
	}

	@Test
	public void testParseMetroUICSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadMetroReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_AUTO));
		cssParser.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(6237, len);
		assertFalse(css.getErrorHandler().hasSacErrors());
		List<String> comments = rules.item(0).getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.size());
		BaseCSSStyleSheet reparsed = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		re = new StringReader(css.toString());
		assertTrue(reparsed.parseStyleSheet(re, CSSStyleSheet.COMMENTS_AUTO));
		CSSRuleArrayList reparsedrules = reparsed.getCssRules();
		assertEquals(len, reparsedrules.getLength());
		List<String> reparsedcomments = reparsedrules.item(0).getPrecedingComments();
		assertNotNull(reparsedcomments);
		assertEquals(1, reparsedcomments.size());
		assertEquals(comments.get(0), reparsedcomments.get(0));
		for (int i = 0; i < len; i++) {
			AbstractCSSRule rule = rules.item(i);
			AbstractCSSRule reparsedrule = reparsedrules.item(i);
			assertTrue(rule.equals(reparsedrule));
		}
	}

}
