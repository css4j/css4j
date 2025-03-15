/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserColorHSLTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueHSL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueHSLNumber() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAllNumber() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25 48)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(25, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25 48)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAllReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25.0 48.0)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25 48)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAllIntegerAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(240 80 50 / 0.5)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(240, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(80, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(50, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(240 80 50/0.5)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAllRealClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 125.0 -48.0)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 100 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAllIntegerClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 125 -148)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(100, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 100 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12, 25%, 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12, 25%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCommaNumber() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81, 25%, 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81, 25%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLA() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsla(12, 25%, 48%,.2)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsla", lu.getFunctionName());
		assertEquals("hsla(12, 25%, 48%, 0.2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLANumber() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsla(12.81, 25%, 48%,.2)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsla", lu.getFunctionName());
		assertEquals("hsla(12.81, 25%, 48%, 0.2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 0.1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 24%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(24f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/24%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashClampNegPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 -25% -48% / -24%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 0% 0%/0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLNumberSlashPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25% 48% / 24%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(24f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25% 48%/24%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLNumberSlashIntegerAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25% 48% / 1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLNumberSlashIntegerAlphaClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25% 48% / 1000)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 0)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha2Clamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 148% / -3)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 100%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLDeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12deg 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12deg 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLDegAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12deg 25% 48%/1.1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12deg 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% var(--foo))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% var(--foo))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(var(--foo) 25% 30%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(30f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(var(--foo) 25% 30%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVarSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% var(--foo)/0.6)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% var(--foo)/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVarSlash2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(var(--foo) 12% 25%/0.6)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(var(--foo) 12% 25%/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVarSlashInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% var(--foo)/1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% var(--foo)/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcHue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(calc(12) 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(calc(12) 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcSat() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 calc(25%) 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(25f, param.getParameters().getFloatValue(), 1e-7f);
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 calc(25%) 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcLig() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% calc(48%))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(48f, param.getParameters().getFloatValue(), 1e-7f);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% calc(48%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48%/calc(0.9))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.REAL, param.getParameters().getLexicalUnitType());
		assertEquals(0.9f, param.getParameters().getFloatValue(), 1e-7f);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/calc(0.9))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcAlphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48%/calc(90%))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(90f, param.getParameters().getFloatValue(), 1e-7f);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/calc(90%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAllCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(calc(12) calc(25%) calc(48%)/calc(90%))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit calcParam = param.getParameters();
		assertNotNull(calcParam);
		assertEquals(LexicalType.INTEGER, calcParam.getLexicalUnitType());
		assertEquals(12, calcParam.getIntegerValue());
		assertNull(calcParam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		calcParam = param.getParameters();
		assertNotNull(calcParam);
		assertEquals(25f, calcParam.getFloatValue(), 1e-5f);
		assertNull(calcParam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		calcParam = param.getParameters();
		assertNotNull(calcParam);
		assertEquals(LexicalType.PERCENTAGE, calcParam.getLexicalUnitType());
		assertEquals(48f, calcParam.getFloatValue(), 1e-7f);
		assertNull(calcParam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		calcParam = param.getParameters();
		assertNotNull(calcParam);
		assertEquals(LexicalType.PERCENTAGE, calcParam.getLexicalUnitType());
		assertEquals(90f, calcParam.getFloatValue(), 1e-7f);

		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(calc(12) calc(25%) calc(48%)/calc(90%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLNoneH() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(none 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(none 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLNoneS() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 none 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 none 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLNoneL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hsl(12.81 25% none)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12.81 25% none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"hsla(attr(data-hue turn),attr(data-s type(<percentage>)),attr(data-l %),attr(data-alpha type(<number>)))");
		assertNotNull(lu);
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-hue turn)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-s type(<percentage>))", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-l %)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-alpha type(<number>))", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsla", lu.getFunctionName());
		assertEquals(
				"hsla(attr(data-hue turn), attr(data-s type(<percentage>)), attr(data-l %), attr(data-alpha type(<number>)))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCommaBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12,, 48%)"));
	}

	@Test
	public void testParsePropertyValueHSLCommaBad2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12,13%,)"));
	}

	@Test
	public void testParsePropertyValueHSLCommaBad3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(,13,14%,15%)"));
	}

	@Test
	public void testParsePropertyValueHSLCommaNoCommaBadInt() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(3,14 15)"));
	}

	@Test
	public void testParsePropertyValueHSLCommaNoCommaBadPercent() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12,48% 94%,0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLCommaNoCommaBadReal() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12deg,48% 94.2,0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLCommasSyntaxSlash() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12,48%,91%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLBadCommasSyntaxCalc() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("hsl(calc(12),calc(48%) calc(91%))"));
	}

	@Test
	public void testParsePropertyValueHSLBadNoCommaThenComma() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12 48%,93%,0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLBadSlashNoAlpha() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12 48% 0.1/)"));
	}

	@Test
	public void testParsePropertyValueHSLBadNoLightness() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12 48%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLBadHueEm() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12em 48% 91%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLAngleSat() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(38.5,14deg, 15)"));
	}

	@Test
	public void testParsePropertyValueHSLBadHuePcnt() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12% 48% 91%)"));
	}

	@Test
	public void testParsePropertyValueHSLBadDoubleSlash() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12deg 48% 91%//0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLBadDoubleAlpha() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("hsl(12deg 48% 91%/2%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHSLBadDoubleSlash2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hsl(12deg 48% 91%/0.1/)"));
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException {
		try {
			return parser.parsePropertyValue(new StringReader(value));
		} catch (IOException e) {
			return null;
		}
	}

	private void assertMatch(Match match, LexicalUnit lu, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, lu.matches(syn));
	}

}
