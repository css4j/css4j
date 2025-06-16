/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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

public class PropertyParserColorOKTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueOKLAB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% .424 .5776)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.424f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5776f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 0.424 0.5776)", lu.toString());

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
	public void testParsePropertyValueOKLAB2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42 57.76)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42.4 57)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42 57)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABClampPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(153.2% 142% 157%/200%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(100% 100% 100%/100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABClampPcntNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(-1.2% -142% -157%/-200%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(-100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(-100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0% -100% -100%/0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(1.532 1000.9 -3300.7/200%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1000.9f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-3300.7f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(1 1000.9 -3300.7/100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABClampNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(-0.2 1000.9 -3300.7/-200%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1000.9f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-3300.7f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0 1000.9 -3300.7/0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLabIntegerL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(2 42 57.76)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(1 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLabIntegerLNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(-2 42 57.76)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABAllPercent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42% -57%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(42f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(-57f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42% -57%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(var(--ligthness-a-b))");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-a-b", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(var(--ligthness-a-b))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLABvar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(var(--ligthness-a) 57.76)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-a", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(var(--ligthness-a) 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(var(--ligthness-a-b) /.3)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-a-b", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.3f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(var(--ligthness-a-b)/0.3)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABalpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42.4 57.76 / 0.6)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42.4 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB2alphaClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42 57.76 / 1.6)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42 57.76/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB3alphaClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42.4 57 / -0.6)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42.4 57/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB4alphaIntClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42 57 / -6)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42 57/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABalphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.532 42.4 57.76 / 60%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.532f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(60f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.532 42.4 57.76/60%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(var(--ligthness-a) 57.76/160%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-a", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(var(--ligthness-a) 57.76/100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlashInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.237 0.115 -0.044 var(--slash) 2)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlashNegInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.237 0.115 -0.044 var(--slash) -2)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.237 0.115 -0.044 var(--slash) 1.6)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlashNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.237 0.115 -0.044 var(--slash) -0.7)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlashPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"oklab(0.237 0.115 -0.044 var(--slash) var(--ignoreme) 160%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ignoreme", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) var(--ignoreme) 100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlashNegPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.237 0.115 -0.044 var(--slash) -160%)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) 0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarContainsSlashAndAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(0.237 0.115 -0.044 var(--slash) var(--alpha))");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.237f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.115f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.044f, param.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--alpha", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(0.237 0.115 -0.044 var(--slash) var(--alpha))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarAlphaVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"oklab(var(--ligthness-a) 57.76/var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-a", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--alpha", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--these", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--could-be-empty", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals(
			"oklab(var(--ligthness-a) 57.76/var(--alpha) var(--these) var(--could-be-empty))",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(calc(2*24%) 42.4 57)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, subparam.getLexicalUnitType());
		assertEquals(24f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(calc(2*24%) 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.1% calc(2*21.6) 42.4)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.1% calc(2*21.6) 42.4)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.1% 42.4 calc(2*21.6))");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.1% 42.4 calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(calc(2*24%) calc(-2*31.3) calc(2*21.6))");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, subparam.getLexicalUnitType());
		assertEquals(24f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(-2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(31.3f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(calc(2*24%) calc(-2*31.3) calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc4Alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"oklab(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, subparam.getLexicalUnitType());
		assertEquals(24f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(-2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(31.3f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(0.18f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABNoneL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(none .424 .5776)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.424f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5776f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(none 0.424 0.5776)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLABNoneA() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% none .5776)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5776f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% none 0.5776)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLABNoneB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% .424 none)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.424f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 0.424 none)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLABNoneAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% .424 .5776/none)");
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.424f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5776f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 0.424 0.5776/none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLabAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"oklab(attr(data-l %) attr(data-a type(<number>)) attr(data-b type(<number>))/attr(data-alpha type(<number>)))");
		assertNotNull(lu);
		assertEquals(LexicalType.OKLABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-l %)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-a type(<number>))", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-b type(<number>))", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-alpha type(<number>))", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals(
				"oklab(attr(data-l %) attr(data-a type(<number>)) attr(data-b type(<number>))/attr(data-alpha type(<number>)))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLabBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(-12deg 48 0.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(-12% 48.5)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(12%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(calc(12%) 48%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(12% 48 89.1deg)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(var(--foo)/)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% 48 89.1/)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% 48 89.1,100%)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("oklab(var(--foo) 74% 48 89.1 0.22 1%)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("oklab(var(--foo) 74% 48 89.1 0.22/ 1%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% 48 /89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74%/48 21)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% 48 89.1//)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% from 89.1)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("oklab(var(--lightness) 48 89.1deg)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("oklab(var(--lightness-a) 89.1deg)"));
	}

	@Test
	public void testParsePropertyValueOKLabBadNoSlash() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% 48 89.1 9)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklab(74% 48 89.1 1%)"));
	}

	@Test
	public void testParsePropertyValueOKLCH() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLCH2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.532 42 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.532f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.532 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42 57)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHNegChroma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% -42.4 57)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 0 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLChIntegerL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(3 42 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(1 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHChromaPercent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4% 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4% 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHClampPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(153.2% 142.4% 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(100% 100% 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHClampNegPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(-53.2% -142.4% 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0% 0% 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHClampReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(153.2 542.4 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(542.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(1 542.4 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHClampNegReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(-53.2 -142.4 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0 0 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHClampInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(153 542 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(542, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(1 542 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHClampNegInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(-53 -142 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0 0 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHangle() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57.76deg)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76deg)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(var(--ligthness-chroma-hue))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-chroma-hue", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(var(--ligthness-chroma-hue))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLCHvar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(var(--ligthness-chroma) 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-chroma", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(var(--ligthness-chroma) 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(var(--ligthness-c-h) /.3)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-c-h", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.3f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(var(--ligthness-c-h)/0.3)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHalpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57.76 / 0.6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH2alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42 57.76 / 0.6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH3alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57 / 6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH4alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42 57 / 0.6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHangleAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57.76deg / -1%)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76deg/0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHalphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57.76 / 60%)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(60f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76/60%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(var(--ligthness-chroma) 57.76/0.6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-chroma", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(var(--ligthness-chroma) 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashAlphaInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.589 42.81 57.76 var(--slash) 6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashAlphaNegInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.589 42.81 57.76 var(--slash) -6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.589 42.81 57.76 var(--slash) 1.6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashNegAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.589 42.81 57.76 var(--slash) -.6)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashAlphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"oklch(0.589 42.81 57.76 var(--slash) var(--ignoreme) 330%)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ignoreme", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) var(--ignoreme) 100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashAlphaNegPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.589 42.81 57.76 var(--slash) -330%)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) 0%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarContainsSlashAndAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(0.589 42.81 57.76 var(--slash) var(--alpha))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.589f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.81f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--slash", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--alpha", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.589 42.81 57.76 var(--slash) var(--alpha))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHalphaTrailingVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"oklch(53.2% 42.4 57.76 / var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--alpha", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--these", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--could-be-empty", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76/var(--alpha) var(--these) var(--could-be-empty))",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarAlphaVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"oklch(var(--ligthness-chroma) 57.76/var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-chroma", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--alpha", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--these", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--could-be-empty", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals(
			"oklch(var(--ligthness-chroma) 57.76/var(--alpha) var(--these) var(--could-be-empty))",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHvarAngleAlphaVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"oklch(var(--ligthness-chroma) 0.8rad/var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-chroma", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_RAD, param.getCssUnit());
		assertEquals(0.8f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--alpha", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--these", subparam.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--could-be-empty", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals(
			"oklch(var(--ligthness-chroma) 0.8rad/var(--alpha) var(--these) var(--could-be-empty))",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(calc(2*24%) 42.4 57)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, subparam.getLexicalUnitType());
		assertEquals(24f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(57, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(calc(2*24%) 42.4 57)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueOKLCHCalc2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.1% calc(2*21.6) 42.4)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.1% calc(2*21.6) 42.4)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(53.1% 42.4 calc(2*21.6))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(53.1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.1% 42.4 calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(calc(2*24%) calc(-2*31.3) calc(2*21.6))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, subparam.getLexicalUnitType());
		assertEquals(24f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(-2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(31.3f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(calc(2*24%) calc(-2*31.3) calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc4Alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"oklch(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, subparam.getLexicalUnitType());
		assertEquals(24f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(-2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(31.3f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(21.6f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.INTEGER, subparam.getLexicalUnitType());
		assertEquals(2, subparam.getIntegerValue());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(0.18f, subparam.getFloatValue(), 1e-5f);
		assertNull(subparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHNoneLightness() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(none 42.4 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(none 42.4 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHNoneChroma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(.2% none 57.76)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.2% none 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHNoneHue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(.2% 42.4 none)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(0.2% 42.4 none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHNoneAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("oklch(3.2% 42.4 57.76/none)");
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(3.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(3.2% 42.4 57.76/none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"oklch(attr(data-l %) attr(data-c %) attr(data-hue type(<angle>))/attr(data-alpha type(<number>)))");
		assertNotNull(lu);
		assertEquals(LexicalType.OKLCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-l %)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-c %)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-hue type(<angle>))", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-alpha type(<number>))", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals(
				"oklch(attr(data-l %) attr(data-c %) attr(data-hue type(<angle>))/attr(data-alpha type(<number>)))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLchBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(-12deg 48 0.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(-12% 48.6)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(12%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(calc(12%))"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(12% 48deg 89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(12% 48 89.1%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(var(--foo)/)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(74% 48 89.1/)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("oklch(var(--ligthness-chroma) 57.76/)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(74% 48 89.1,100%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(12% 48 89.1 9)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(74% 48 /89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(74%/48 21)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(74% 48 89.1//)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("oklch(74% from 89.1)"));
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
