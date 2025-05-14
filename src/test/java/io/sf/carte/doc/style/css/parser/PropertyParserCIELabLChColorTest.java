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
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class PropertyParserCIELabLChColorTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueLAB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42.4 57.76)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42.4 57.76)", lu.toString());

		assertTrue(lu.getContextIndex() >= 0);

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
	public void testParsePropertyValueLAB2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2 42 57.76)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLAB3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42.4 57)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLAB4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42 57)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLabIntegerL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53 42 57.76)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(53, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABAllPercent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42% -57%)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42% -57%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABvar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(var(--ligthness-a-b))");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-a-b", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(var(--ligthness-a-b))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABvar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(var(--ligthness-a) 57.76)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(var(--ligthness-a) 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABalpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42.4 57.76 / 0.6)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42.4 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLAB2alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42 57.76 / 0.6)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLAB3alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42.4 57 / 0.6)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42.4 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLAB4alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42 57 / 0.6)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABalphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 42.4 57.76 / 60%)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(60f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 42.4 57.76/60%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABvarAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(var(--ligthness-a) 57.76/0.6)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(var(--ligthness-a) 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABvarAlphaVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"lab(var(--ligthness-a) 57.76/var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(var(--ligthness-a) 57.76/var(--alpha) var(--these) var(--could-be-empty))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueLABCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(calc(2*24%) 42.4 57)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(calc(2*24%) 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABCalc2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.1% calc(2*21.6) 42.4)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.1% calc(2*21.6) 42.4)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABCalc3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.1% 42.4 calc(2*21.6))");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.1% 42.4 calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABCalc4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(calc(2*24%) calc(-2*31.3) calc(2*21.6))");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(calc(2*24%) calc(-2*31.3) calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABCalc4Alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"lab(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLABNoneL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(none 0.424 0.5776)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(none 0.424 0.5776)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLABNoneA() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% none 0.5776)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% none 0.5776)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLABNoneB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 0.424 none)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 0.424 none)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLABNoneAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lab(53.2% 0.424 0.5776/none)");
		assertEquals(LexicalType.LABCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lab", lu.getFunctionName());
		assertEquals("lab(53.2% 0.424 0.5776/none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLabBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(-12deg 48 0.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(-12% 48.5deg 89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(12% 48% 89.1deg)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(12% 48%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(12% 48)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(12%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(74% 48 89.1/)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(74% 48 /89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(74%/48 21)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(74% 48 89.1//)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lab(74% a 89.1)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("lab(var(--lightness) 48 89.1deg)"));

		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("lab(var(--lightness-a) 89.1deg)"));
	}

	@Test
	public void testParsePropertyValueLCH() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76)", lu.toString());

		assertTrue(lu.getContextIndex() >= 0);

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
	public void testParsePropertyValueLCH2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2 42 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCH3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCH4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42 57)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHNegChroma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% -42.4 57)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 0 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLchIntegerL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53 42 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(53, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(42, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHChromaPercent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4% 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4% 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHClampPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(153.2% 142.4% 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(100% 100% 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHClampNegPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(-53.2% -142.4% 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(0% 0% 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHClampReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(153.2 542.4 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(542.4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(100 542.4 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHClampNegReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(-53.2 -142.4 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(0 0 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHClampInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(153 542 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(100, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(542, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(100 542 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHClampNegInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(-53 -142 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(0 0 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHangle() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57.76deg)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76deg)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHvar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(var(--ligthness-chroma-hue))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--ligthness-chroma-hue", subparam.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(var(--ligthness-chroma-hue))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLCHvar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(var(--ligthness-chroma) 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(var(--ligthness-chroma) 57.76)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLCHalpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57.76 / 0.6)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCH2alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42 57.76 / 0.6)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCH3alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57 / 0.6)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCH4alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42 57 / 0.6)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHangleAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57.76deg / 0.6)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76deg/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHalphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57.76 / 60%)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76/60%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHvarAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(var(--ligthness-chroma) 57.76/0.6)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(var(--ligthness-chroma) 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHalphaTrailingVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"lch(53.2% 42.4 57.76 / var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76/var(--alpha) var(--these) var(--could-be-empty))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHvarAlphaVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"lch(var(--ligthness-chroma) 57.76/var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(var(--ligthness-chroma) 57.76/var(--alpha) var(--these) var(--could-be-empty))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHvarAngleAlphaVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"lch(var(--ligthness-chroma) 0.8rad/var(--alpha) var(--these) var(--could-be-empty))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(var(--ligthness-chroma) 0.8rad/var(--alpha) var(--these) var(--could-be-empty))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(calc(2*24%) 42.4 57)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(calc(2*24%) 42.4 57)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHCalc2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.1% calc(2*21.6) 42.4)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.1% calc(2*21.6) 42.4)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHCalc3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.1% 42.4 calc(2*21.6))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.1% 42.4 calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHCalc4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(calc(2*24%) calc(-2*31.3) calc(2*21.6))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(calc(2*24%) calc(-2*31.3) calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHCalc4Alpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"lch(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHNoneLightness() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(none 42.4 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(none 42.4 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHNoneChroma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% none 57.76)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% none 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHNoneHue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 none)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLCHNoneAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("lch(53.2% 42.4 57.76/none)");
		assertEquals(LexicalType.LCHCOLOR, lu.getLexicalUnitType());
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
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("lch", lu.getFunctionName());
		assertEquals("lch(53.2% 42.4 57.76/none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueLchBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(-12deg 48 0.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(-12% 48.6)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(12%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(calc(12%))"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(12% 48deg 89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(12% 48 89.1%)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(74% 48 89.1/)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(74% 48 /89.1)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(74%/48 21)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(74% 48 89.1//)"));

		assertThrows(CSSParseException.class, () -> parsePropertyValue("lch(74% a 89.1)"));
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
