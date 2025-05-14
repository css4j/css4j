/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Locator;

public class KeyframesRuleTest {

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
	public void testParseKeyframesRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes slide-right {\nfrom {margin-left: 0px;}\n50% {margin-left: 110px; opacity: 1;}\n"
				+ "70% {opacity: 0.9;}\nto\n{margin-left: 200px;}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals(4, handler.keyframeSelectors.size());
		assertEquals("from", handler.keyframeSelectors.get(0).toString());
		assertEquals("50%", handler.keyframeSelectors.get(1).toString());
		assertEquals("70%", handler.keyframeSelectors.get(2).toString());
		assertEquals("to", handler.keyframeSelectors.get(3).toString());
		assertEquals(5, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		assertEquals("opacity", handler.propertyNames.get(2));
		assertEquals("opacity", handler.propertyNames.get(3));
		assertEquals("margin-left", handler.propertyNames.get(4));
		assertEquals(5, handler.lexicalValues.size());
		assertEquals("0px", handler.lexicalValues.get(0).toString());
		assertEquals("110px", handler.lexicalValues.get(1).toString());
		assertEquals("1", handler.lexicalValues.get(2).toString());
		assertEquals("0.9", handler.lexicalValues.get(3).toString());
		assertEquals("200px", handler.lexicalValues.get(4).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(24, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(3, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(4, loc.getLineNumber());
		assertEquals(18, loc.getColumnNumber());
		loc = handler.ptyLocators.get(4);
		assertEquals(6, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRuleNoWS() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes slide-right{0{margin-left:0px}.9%,26%{margin-left:110px;opacity:1}"
				+ "+40.2%,{opacity:.6}7e1%{margin-left:150px}to{margin-left:200px}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals(5, handler.keyframeSelectors.size());
		LexicalUnit lu = handler.keyframeSelectors.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
		lu = handler.keyframeSelectors.get(1);
		assertEquals("0.9%", lu.getCssText());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals("26%", lu.getCssText());
		lu = handler.keyframeSelectors.get(2);
		assertEquals("40.2%", lu.getCssText());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		assertEquals("70%", handler.keyframeSelectors.get(3).toString());
		assertEquals("to", handler.keyframeSelectors.get(4).toString());

		assertEquals(6, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		assertEquals("opacity", handler.propertyNames.get(2));
		assertEquals("opacity", handler.propertyNames.get(3));
		assertEquals("margin-left", handler.propertyNames.get(4));
		assertEquals("margin-left", handler.propertyNames.get(5));
		assertEquals(6, handler.lexicalValues.size());
		assertEquals("0px", handler.lexicalValues.get(0).toString());
		assertEquals("110px", handler.lexicalValues.get(1).toString());
		assertEquals("1", handler.lexicalValues.get(2).toString());
		assertEquals("0.6", handler.lexicalValues.get(3).toString());
		assertEquals("150px", handler.lexicalValues.get(4).toString());
		assertEquals("200px", handler.lexicalValues.get(5).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(41, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(1, loc.getLineNumber());
		assertEquals(67, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(1, loc.getLineNumber());
		assertEquals(77, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(1, loc.getLineNumber());
		assertEquals(96, loc.getColumnNumber());
		loc = handler.ptyLocators.get(4);
		assertEquals(1, loc.getLineNumber());
		assertEquals(119, loc.getColumnNumber());
		loc = handler.ptyLocators.get(5);
		assertEquals(1, loc.getLineNumber());
		assertEquals(140, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRuleEOF() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes slide-right {\nfrom {margin-left: 0px;}\n50% {margin-left: 110px; opacity: 1;}\n"
				+ "70% {opacity: 0.9;}\nto\n{margin-left: 200px");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals(4, handler.keyframeSelectors.size());
		assertEquals("from", handler.keyframeSelectors.get(0).toString());
		assertEquals("50%", handler.keyframeSelectors.get(1).toString());
		assertEquals("70%", handler.keyframeSelectors.get(2).toString());
		assertEquals("to", handler.keyframeSelectors.get(3).toString());
		assertEquals(5, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		assertEquals("opacity", handler.propertyNames.get(2));
		assertEquals("opacity", handler.propertyNames.get(3));
		assertEquals("margin-left", handler.propertyNames.get(4));
		assertEquals(5, handler.lexicalValues.size());
		assertEquals("0px", handler.lexicalValues.get(0).toString());
		assertEquals("110px", handler.lexicalValues.get(1).toString());
		assertEquals("1", handler.lexicalValues.get(2).toString());
		assertEquals("0.9", handler.lexicalValues.get(3).toString());
		assertEquals("200px", handler.lexicalValues.get(4).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(24, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(3, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(4, loc.getLineNumber());
		assertEquals(18, loc.getColumnNumber());
		loc = handler.ptyLocators.get(4);
		assertEquals(6, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRuleErrorEOF() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes slide-right{from{margin-left:0px;}-50  {margin-left: 110px; opacity: 1;");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals(1, handler.keyframeSelectors.size());
		assertEquals("from", handler.keyframeSelectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("0px", handler.lexicalValues.get(0).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(44, loc.getColumnNumber());

		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(49, ex.getColumnNumber());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRuleComment() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes/* comment */slide-right {\nfrom {margin-left: 0px;}\n50% {margin-left: 110px; opacity: 1;}\n"
				+ "70% {opacity: 0.9;}\nto\n{margin-left: 200px;}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals(4, handler.keyframeSelectors.size());
		assertEquals("from", handler.keyframeSelectors.get(0).toString());
		assertEquals("50%", handler.keyframeSelectors.get(1).toString());
		assertEquals("70%", handler.keyframeSelectors.get(2).toString());
		assertEquals("to", handler.keyframeSelectors.get(3).toString());
		assertEquals(5, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		assertEquals("opacity", handler.propertyNames.get(2));
		assertEquals("opacity", handler.propertyNames.get(3));
		assertEquals("margin-left", handler.propertyNames.get(4));
		assertEquals(5, handler.lexicalValues.size());
		assertEquals("0px", handler.lexicalValues.get(0).toString());
		assertEquals("110px", handler.lexicalValues.get(1).toString());
		assertEquals("1", handler.lexicalValues.get(2).toString());
		assertEquals("0.9", handler.lexicalValues.get(3).toString());
		assertEquals("200px", handler.lexicalValues.get(4).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(24, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(3, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(4, loc.getLineNumber());
		assertEquals(18, loc.getColumnNumber());
		loc = handler.ptyLocators.get(4);
		assertEquals(6, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRuleErrorNameDot() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes slide.{from {margin-left: 0px;} 50% {margin-left: 110px; opacity: 1;}"
				+ " 70% {opacity: 0.9;} to {margin-left: 200px;}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.keyframesNames.size());
		assertEquals(0, handler.keyframeSelectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());

		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(17, ex.getColumnNumber());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRuleInvalidCustomIdent() throws CSSException, IOException {
		Reader re = new StringReader(
			"@keyframes inherit{from {margin-left: 0px;} 50% {margin-left: 110px; opacity: 1;}"
				+ " 70% {opacity: 0.9;} to {margin-left: 200px;}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.keyframesNames.size());
		assertEquals(0, handler.keyframeSelectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());

		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(19, ex.getColumnNumber());
		assertTrue(errorHandler.hasError());
	}

}
