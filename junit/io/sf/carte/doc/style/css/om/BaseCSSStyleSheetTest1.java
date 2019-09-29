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

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;

import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class BaseCSSStyleSheetTest1 {

	private Parser2 cssParser;

	@Before
	public void setUp() {
		this.cssParser = new CSSParser();
	}

	@Test
	public void testParseCSSStyleSheet() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleCSSReader();
		InputSource source = new InputSource(re);
		cssParser.setDocumentHandler(css.createDocumentHandler(css.getOrigin(), false));
		cssParser.parseStyleSheet(source);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(DOMCSSStyleSheetFactoryTest.RULES_IN_SAMPLE_CSS, rules.getLength());
		assertFalse(css.getErrorHandler().hasSacErrors());
		List<String> comments = rules.item(3).getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.size()); assertEquals("Comment before li",
		comments.get(0).trim());
	}

}
