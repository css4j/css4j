/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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

import io.sf.carte.doc.style.css.nsac.Locator;

public class PropertyRuleTest {

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
	public void testParsePropertyRule() throws IOException {
		Reader re = new StringReader(
			"@property --my-length {syntax: '<length>'; inherits: false;\ninitial-value: 24px; ignore-me:0}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(4, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals("ignore-me", handler.propertyNames.get(3));
		assertEquals(4, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		assertEquals("24px", handler.lexicalValues.get(2).toString());
		assertEquals("0", handler.lexicalValues.get(3).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(42, loc.getColumnNumber());
		assertEquals(59, handler.ptyLocators.get(1).getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(2, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(2, loc.getLineNumber());
		assertEquals(33, loc.getColumnNumber());

		assertEquals("endProperty", handler.eventSeq.get(5));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePropertyRuleEOF() throws IOException {
		Reader re = new StringReader(
			"@property --my-length {syntax:'<length>';inherits:true;\ninitial-value:24px;ignore-me:0");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(4, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals("ignore-me", handler.propertyNames.get(3));
		assertEquals(4, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("true", handler.lexicalValues.get(1).toString());
		assertEquals("24px", handler.lexicalValues.get(2).toString());
		assertEquals("0", handler.lexicalValues.get(3).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(41, loc.getColumnNumber());
		assertEquals(55, handler.ptyLocators.get(1).getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(2, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(2, loc.getLineNumber());
		assertEquals(31, loc.getColumnNumber());

		assertEquals("endProperty", handler.eventSeq.get(5));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePropertyRuleNoNameError() throws IOException {
		Reader re = new StringReader(
			"@property {syntax: '<length>'; inherits: false;\ninitial-value: 24px}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.customPropertyNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.atRules.size());

		assertEquals(0, handler.eventSeq.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleBadNameError() throws IOException {
		Reader re = new StringReader(
			"@property 111 {syntax: '<length>'; inherits: false;\ninitial-value: 24px}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.customPropertyNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.atRules.size());

		assertEquals(0, handler.eventSeq.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleInvalidPropertyError() throws IOException {
		Reader re = new StringReader(
			"@property Width {syntax: '<length>'; inherits: false;\ninitial-value: 24px}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.customPropertyNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.atRules.size());

		assertEquals(0, handler.eventSeq.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleSyntaxDescriptorError() throws IOException {
		Reader re = new StringReader(
			"@property --my-length {syntax: '<foo>'; inherits: false;\ninitial-value: 24px}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("inherits", handler.propertyNames.get(0));
		assertEquals("initial-value", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("false", handler.lexicalValues.get(0).toString());
		assertEquals("24px", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(56, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(2, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());

		assertEquals("endProperty-Discard", handler.eventSeq.get(3));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(39, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleInitialValueDescriptorError() throws IOException {
		Reader re = new StringReader(
			"@property --my-length {syntax: '<length>'; inherits: false;\ninitial-value: 72dpi}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		assertEquals("72dpi", handler.lexicalValues.get(2).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(42, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(1, loc.getLineNumber());
		assertEquals(59, loc.getColumnNumber());

		assertEquals("endProperty-Discard", handler.eventSeq.get(4));
		assertTrue(errorHandler.hasError());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(21, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleInitialValueDescriptorErrorRelativeLength()
		throws IOException {
		Reader re = new StringReader(
			"@property --my-length {syntax: '<length>'; inherits: false;\ninitial-value: 2.1em}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		assertEquals("2.1em", handler.lexicalValues.get(2).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(42, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(1, loc.getLineNumber());
		assertEquals(59, loc.getColumnNumber());

		assertEquals("endProperty-Discard", handler.eventSeq.get(4));
		assertTrue(errorHandler.hasError());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(21, errorHandler.getLastException().getColumnNumber());
	}

}
