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

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserColorHWBTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueHWB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%)", lu.toString());

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
	public void testParsePropertyValueHWB_UC() throws CSSException {
		LexicalUnit lu = parsePropertyValue("HWB(12 25% 48%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueHWBClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 125% 148%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 100% 100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 -125% -148%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 0% 0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / 0.1)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBDecHueAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12.76 25.7% 48.2% / 0.1)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.76, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25.7f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12.76 25.7% 48.2%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / 1.1)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampAlphaNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / -1.1)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampAlphaInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / 2)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampAlphaNegInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / -1)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampPcntAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / 111%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBClampNegPcntAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48% / -111%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% var(--foo))");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% var(--foo))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueHWBVar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(var(--foo) 25% 30%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(var(--foo) 25% 30%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBVarSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% var(--foo)/0.6)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% var(--foo)/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBVarSlash2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(var(--foo) 12% 25%/0.6)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(var(--foo) 12% 25%/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBVarSlashInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% var(--foo)/1)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% var(--foo)/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBCalcHue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(calc(12) 25% 48%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(calc(12) 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBCalcSat() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 calc(25%) 48%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 calc(25%) 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBCalcLig() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% calc(48%))");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% calc(48%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBCalcAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48%/calc(0.9))");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/calc(0.9))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBCalcAlphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% 48%/calc(90%))");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% 48%/calc(90%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHWBNoneH() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(none 25% 48%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
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
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(none 25% 48%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueHWBNoneW() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 none 48%)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 none 48%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueHWBNoneB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("hwb(12 25% none)");
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals("hwb(12 25% none)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueHWBAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"hwb(attr(data-hue type(<angle>)) attr(data-w type(<percentage>)) attr(data-b %)/attr(data-alpha type(<number>)))");
		assertNotNull(lu);
		assertEquals(LexicalType.HWBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-hue type(<angle>))", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-w type(<percentage>))", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-b %)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-alpha type(<number>))", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hwb", lu.getFunctionName());
		assertEquals(
				"hwb(attr(data-hue type(<angle>)) attr(data-w type(<percentage>)) attr(data-b %)/attr(data-alpha type(<number>)))",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueHWBBadLightness() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12 48% 0.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadNoLightness() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12 48%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadCommaSyntax() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12,48%,91%)"));
	}

	@Test
	public void testParsePropertyValueHWBBadHuePcnt() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12% 48% 91%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadHueEm() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12em 48% 91%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadIntSat() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48 91%)"));
	}

	@Test
	public void testParsePropertyValueHWBBadRealSat() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48.2 91%)"));
	}

	@Test
	public void testParsePropertyValueHWBBadIntLightness() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48% 91)"));
	}

	@Test
	public void testParsePropertyValueHWBBadRealLightness() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48% 91.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadAngleLightness() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48% 91.1deg)"));
	}

	@Test
	public void testParsePropertyValueHWBBadDoubleSlash() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48% 91%//0.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadDoubleSlashAlpha() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("hwb(12deg 48% 91%/2%/0.1)"));
	}

	@Test
	public void testParsePropertyValueHWBBadSlashAlphaSlash() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48% 91%/0.1/)"));
	}

	@Test
	public void testParsePropertyValueHWBBadSlashNoAlpha() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("hwb(12deg 48% 31%/)"));
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
