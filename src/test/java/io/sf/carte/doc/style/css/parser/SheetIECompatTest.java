/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

public class SheetIECompatTest {

	private CSSParser parser;
	private TestErrorHandler errorHandler;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
		errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
	}

	@Test
	public void testParseStyleSheetMediaRuleIEHack() throws CSSException, IOException {
		Reader re = new StringReader("@media screen\\0 {.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen\\0", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).item(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(0, handler.atRules.size());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetAsteriskHack() throws CSSException, IOException {
		TestDeclarationHandler handler = new TestDeclarationHandler();
		parser.setDocumentHandler(handler);
		Reader re = new StringReader(".foo{*width: 80%}");
		parser.parseStyleSheet(re);
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
		errorHandler.reset();
		parser.setFlag(CSSParser.Flag.STARHACK);
		re = new StringReader(".foo{*width: 80%}");
		parser.parseStyleSheet(re);
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(80, lu.getFloatValue(), 0.01);
	}

}
