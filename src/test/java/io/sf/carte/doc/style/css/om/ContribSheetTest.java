/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.nsac.CSSException;

public class ContribSheetTest {

	private CSSOMParser cssParser;

	@BeforeEach
	public void setUp() {
		cssParser = new CSSOMParser();
	}

	@Test
	public void testParseFontAwesomeCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = SampleCSS.loadFontAwesomeReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_PRECEDING));
		cssParser.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(1499, len);
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
		Reader re = SampleCSS.loadNormalizeReader();
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
			assertTrue(Objects.equals(rule.getPrecedingComments(),
					reparsedrule.getPrecedingComments()));
			assertTrue(
					Objects.equals(rule.getTrailingComments(), reparsedrule.getTrailingComments()));
		}
	}

	@Test
	public void testParseAnimateCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = SampleCSS.loadAnimateReader();
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
		Reader re = SampleCSS.loadMetroReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_AUTO));
		cssParser.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(".mif-medium::before{content:\"\\f23a\"}",
				rules.item(len - 1).getMinifiedCssText());
		assertEquals(7334, len);
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

	@Test
	public void testParseMetroUICSSResourceLimit() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		cssParser.setStreamSizeLimit(0x50000);
		Reader re = SampleCSS.loadMetroReader();
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_IGNORE));
		try {
			cssParser.parseStyleSheet(re);
			fail("Must throw exception.");
		} catch (SecurityException e) {
		} finally {
			re.close();
		}
	}

}
