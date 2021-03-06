/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

public class DeclarationRuleParserTest {

	static CSSParser parser;
	TestDeclarationRuleHandler handler;
	TestErrorHandler errorHandler;

	@BeforeClass
	public static void setUpBeforeClass() {
		parser = new CSSParser();
	}

	@Before
	public void setUp() {
		handler = new TestDeclarationRuleHandler();
		errorHandler = new TestErrorHandler();
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(errorHandler);
	}

	@Test
	public void testParseDeclarationRuleMargin() throws CSSException, IOException {
		parseDeclarationRule("@top-left{content:'foo';color:blue}");
		assertEquals(1, handler.ruleNames.size());
		assertEquals(1, handler.selectorNames.size());
		assertEquals("top-left", handler.ruleNames.getFirst());
		assertNull(handler.selectorNames.getFirst());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("content", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.getLast());
		assertEquals(2, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = handler.lexicalValues.getLast();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDeclarationRuleViewport() throws CSSException, IOException {
		parseDeclarationRule("@viewport{orientation:landscape}");
		assertEquals(1, handler.ruleNames.size());
		assertEquals(1, handler.selectorNames.size());
		assertEquals("viewport", handler.ruleNames.getFirst());
		assertNull(handler.selectorNames.getFirst());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("orientation", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("landscape", lu.getStringValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDeclarationRuleViewport2() throws CSSException, IOException {
		parseDeclarationRule("@viewport{orientation:landscape;min-width: 640px;}");
		assertEquals(1, handler.ruleNames.size());
		assertEquals(1, handler.selectorNames.size());
		assertEquals("viewport", handler.ruleNames.getFirst());
		assertNull(handler.selectorNames.getFirst());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("orientation", handler.propertyNames.getFirst());
		assertEquals("min-width", handler.propertyNames.getLast());
		assertEquals(2, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("landscape", lu.getStringValue());
		lu = handler.lexicalValues.getLast();
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(640, lu.getFloatValue(), 0.01f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("640px", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDeclarationRuleViewportEOF() throws CSSException, IOException {
		parseDeclarationRule("@viewport{orientation:landscape");
		assertEquals(1, handler.ruleNames.size());
		assertEquals(1, handler.selectorNames.size());
		assertEquals("viewport", handler.ruleNames.getFirst());
		assertNull(handler.selectorNames.getFirst());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("orientation", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("landscape", lu.getStringValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDeclarationRuleCounterStyle() throws CSSException, IOException {
		parseDeclarationRule("@counter-style thumbs {system:cyclic;symbols:\\1F44D;suffix:\" \"}");
		assertEquals(1, handler.ruleNames.size());
		assertEquals(1, handler.selectorNames.size());
		assertEquals("counter-style", handler.ruleNames.getFirst());
		assertEquals("thumbs", handler.selectorNames.getFirst());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("system", handler.propertyNames.getFirst());
		assertEquals("symbols", handler.propertyNames.get(1));
		assertEquals("suffix", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("cyclic", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		lu = handler.lexicalValues.get(2);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals(" ", lu.getStringValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDeclarationRuleBad() throws CSSException, IOException {
		parseDeclarationRule("@viewport{orientation;landscape}");
		assertEquals(1, handler.ruleNames.size());
		assertEquals(1, handler.selectorNames.size());
		assertNull(handler.selectorNames.get(0));
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	private void parseDeclarationRule(String string) throws CSSParseException, IOException {
		parser.parseDeclarationRule(new StringReader(string));
		assertEquals(1, handler.streamEndcount);
	}

	static class TestDeclarationRuleHandler extends TestDeclarationHandler implements CSSParser.DeclarationRuleHandler {

		private LinkedList<String> ruleNames = new LinkedList<String>();
		private LinkedList<String> selectorNames = new LinkedList<String>();

		@Override
		public void property(String name, LexicalUnit value, boolean important) {
			propertyNames.add(name);
			lexicalValues.add(value);
			if (important) {
				priorities.add("important");
			} else {
				priorities.add(null);
			}
		}

		@Override
		public void startAtRule(String ruleName, String selector) {
			ruleNames.add(ruleName);
			selectorNames.add(selector);
		}

		@Override
		public void endAtRule() {
		}

	}
}
