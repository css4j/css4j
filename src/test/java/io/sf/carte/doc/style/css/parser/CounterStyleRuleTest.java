/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Locator;

public class CounterStyleRuleTest {

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
	public void testParseCounterStyleRule() throws CSSException, IOException {
		Reader re = new StringReader("@counter-style foo {symbols: \\1F44D;\n suffix: \" \";\n}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		assertEquals("suffix", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\\1f44d ", handler.lexicalValues.get(0).toString());
		assertEquals("\" \"", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		assertEquals(13, handler.ptyLocators.get(1).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCounterStyleRuleEOF() throws CSSException, IOException {
		Reader re = new StringReader("@counter-style foo {symbols: \\1F44D;\n suffix: \" \"");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		assertEquals("suffix", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\\1f44d ", handler.lexicalValues.get(0).toString());
		assertEquals("\" \"", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		assertEquals(13, handler.ptyLocators.get(1).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCounterStyleRuleBadNameDot() throws CSSException, IOException {
		Reader re = new StringReader(
			"@counter-style foo. {symbols: \\1F44D; suffix: \" \";}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.counterStyleNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());
		assertEquals(0, handler.atRules.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(19, ex.getColumnNumber());
	}

	@Test
	public void testParseCounterStyleRuleNone() throws CSSException, IOException {
		Reader re = new StringReader(
			"@counter-style None {symbols: \\1F44D; suffix: \" \";}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.counterStyleNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());
		assertEquals(0, handler.atRules.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(20, ex.getColumnNumber());
	}

	@Test
	public void testParseCounterStyleRuleInherit() throws CSSException, IOException {
		Reader re = new StringReader(
			"@counter-style inherit{symbols: \\1F44D; suffix: \" \";}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.counterStyleNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());
		assertEquals(0, handler.atRules.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(23, ex.getColumnNumber());
	}

}
