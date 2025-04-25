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

public class FontFeatureValuesRuleTest {

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
	public void testParseFontFeatureValuesRule() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values Otaru Kisa{@annotation{circled:1;black-boxed:3;}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.fontFeaturesNames.size());
		String[] ff = handler.fontFeaturesNames.get(0);
		assertEquals(1, ff.length);
		assertEquals("Otaru Kisa", ff[0]);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("circled", handler.propertyNames.get(0));
		assertEquals("black-boxed", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("1", handler.lexicalValues.get(0).toString());
		assertEquals("3", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(54, loc.getColumnNumber());
		assertEquals(68, handler.ptyLocators.get(1).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseFontFeatureValuesRuleQuoted() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values \"Otaru Kisa\" , \"Taisho Gothic\" {@annotation{circled:1;black-boxed:3}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.fontFeaturesNames.size());
		String[] ff = handler.fontFeaturesNames.get(0);
		assertEquals(2, ff.length);
		assertEquals("Otaru Kisa", ff[0]);
		assertEquals("Taisho Gothic", ff[1]);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("circled", handler.propertyNames.get(0));
		assertEquals("black-boxed", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("1", handler.lexicalValues.get(0).toString());
		assertEquals("3", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(75, loc.getColumnNumber());
		assertEquals(89, handler.ptyLocators.get(1).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseFontFeatureValuesRuleQuotedNoWS() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values \"Otaru Kisa\",\"Taisho Gothic\"{@annotation{circled:1;black-boxed:3}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.fontFeaturesNames.size());
		String[] ff = handler.fontFeaturesNames.get(0);
		assertEquals(2, ff.length);
		assertEquals("Otaru Kisa", ff[0]);
		assertEquals("Taisho Gothic", ff[1]);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("circled", handler.propertyNames.get(0));
		assertEquals("black-boxed", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("1", handler.lexicalValues.get(0).toString());
		assertEquals("3", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(72, loc.getColumnNumber());
		assertEquals(86, handler.ptyLocators.get(1).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseFontFeatureValuesRuleEOF() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values Otaru Kisa , Taisho Gothic{@annotation{circled:1;black-boxed:3");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.fontFeaturesNames.size());
		String[] ff = handler.fontFeaturesNames.get(0);
		assertEquals(2, ff.length);
		assertEquals("Otaru Kisa", ff[0]);
		assertEquals("Taisho Gothic", ff[1]);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("circled", handler.propertyNames.get(0));
		assertEquals("black-boxed", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("1", handler.lexicalValues.get(0).toString());
		assertEquals("3", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(70, loc.getColumnNumber());
		assertEquals(84, handler.ptyLocators.get(1).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseFontFeatureValuesRuleBadNameDot() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values Otaru .{@annotation{circled:1;black-boxed:3");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.counterStyleNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());
		assertEquals(0, handler.atRules.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(28, ex.getColumnNumber());
	}

	@Test
	public void testParseFontFeatureValuesRuleNone() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values None {@annotation{circled:1;black-boxed:3");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.counterStyleNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());
		assertEquals(0, handler.atRules.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(27, ex.getColumnNumber());
	}

	@Test
	public void testParseFontFeatureValuesRuleInherit() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values inherit{@annotation{circled:1;black-boxed:3");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.counterStyleNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.eventSeq.size());
		assertEquals(0, handler.atRules.size());

		assertTrue(errorHandler.hasError());
		CSSParseException ex = errorHandler.getLastException();
		assertEquals(1, ex.getLineNumber());
		assertEquals(29, ex.getColumnNumber());
	}

}
