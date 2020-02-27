/*

 Copyright (c) 2005-2020, Carlos Amengual.

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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

@RunWith(Parameterized.class)
public class ContribSheetTest {

	private Parser cssParser;

	public ContribSheetTest(Parser cssParser) {
		super();
		this.cssParser = cssParser;
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> parsers = new LinkedList<Object[]>();
		parsers.add(new Object[] { new io.sf.carte.doc.style.css.parser.CSSParser() });
		return parsers;
	}

	@Test
	public void testParseFontAwesomeCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadFontAwesomeReader();
		InputSource source = new InputSource(re);
		cssParser.setDocumentHandler(css.createDocumentHandler(css.getOrigin(), false));
		cssParser.parseStyleSheet(source);
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
		source = new InputSource(re);
		assertTrue(reparsed.parseStyleSheet(source, false));
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
		InputSource source = new InputSource(re);
		cssParser.setDocumentHandler(css.createDocumentHandler(css.getOrigin(), false));
		cssParser.parseStyleSheet(source);
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
		source = new InputSource(re);
		assertTrue(reparsed.parseStyleSheet(source, false));
		CSSRuleArrayList reparsedrules = reparsed.getCssRules();
		assertEquals(len, reparsedrules.getLength());
		List<String> reparsedcomments = reparsedrules.item(4).getPrecedingComments();
		assertNotNull(reparsedcomments);
		assertEquals(2, reparsedcomments.size());
		assertEquals(comments.get(0), reparsedcomments.get(0));
		for (int i = 0; i < len; i++) {
			assertTrue(rules.item(i).equals(reparsedrules.item(i)));
		}
	}

	@Test
	public void testParseAnimateCSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadAnimateReader();
		InputSource source = new InputSource(re);
		cssParser.setDocumentHandler(css.createDocumentHandler(css.getOrigin(), false));
		cssParser.parseStyleSheet(source);
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
		source = new InputSource(re);
		assertTrue(reparsed.parseStyleSheet(source, false));
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
	public void testParseMetroUICSS() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		factory.setLenientSystemValues(false);
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadMetroReader();
		InputSource source = new InputSource(re);
		cssParser.setDocumentHandler(css.createDocumentHandler(css.getOrigin(), false));
		cssParser.parseStyleSheet(source);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		int len = rules.getLength();
		assertEquals(6033, len);
		assertFalse(css.getErrorHandler().hasSacErrors());
		List<String> comments = rules.item(0).getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.size());
		BaseCSSStyleSheet reparsed = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		re = new StringReader(css.toString());
		source = new InputSource(re);
		assertTrue(reparsed.parseStyleSheet(source, false));
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
