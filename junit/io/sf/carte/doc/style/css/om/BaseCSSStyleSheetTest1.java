/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.Reader;
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
public class BaseCSSStyleSheetTest1 {

	Parser cssParser;

	public BaseCSSStyleSheetTest1(Parser cssParser) {
		super();
		this.cssParser = cssParser;
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> parsers = new LinkedList<Object[]>();
		parsers.add(new Object[] { new io.sf.carte.doc.style.css.parser.CSSParser() });
		parsers.add(new Object[] { new org.apache.batik.css.parser.Parser() });
		try {
			Parser p = (Parser) Class.forName("org.w3c.flute.parser.Parser").getConstructor().newInstance();
			parsers.add(new Object[] { p });
		} catch (Exception e) {
		}
		/* steadystate parser */
		try {
			Parser p = (Parser) Class.forName("com.steadystate.css.parser.SACParserCSS3").getConstructor()
					.newInstance();
			parsers.add(new Object[] { p });
		} catch (Exception e) {
		}
		return parsers;
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
		/*
		 * DOMStringList comments = rules.item(3).getPrecedingComments(); assertNotNull(comments);
		 * assertEquals(1, comments.getLength()); assertEquals("Comment before li",
		 * comments.item(0).trim());
		 */
	}

}
