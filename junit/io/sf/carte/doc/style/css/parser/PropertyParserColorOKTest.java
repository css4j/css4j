/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.BeforeClass;
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserColorOKTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeClass
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueOKLAB() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42.4 57.76)");
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42.4 57.76)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>#");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(CSSValueSyntax.Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueOKLAB2() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLAB3() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLAB4() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLABvar() throws CSSException, IOException {
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
	}

	@Test
	public void testParsePropertyValueOKLABvar2() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLABalpha() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLAB2alpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42 57.76 / 0.6)");
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
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB3alpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42.4 57 / 0.6)");
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
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42.4 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLAB4alpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42 57 / 0.6)");
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABalphaPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklab(53.2% 42.4 57.76 / 60%)");
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
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(60f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.2% 42.4 57.76/60%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklab(var(--ligthness-a) 57.76/0.6)");
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(var(--ligthness-a) 57.76/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABvarAlphaVar() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLABCalc() throws CSSException, IOException {
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
		//
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
	public void testParsePropertyValueOKLABCalc2() throws CSSException, IOException {
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
		//
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.1% calc(2*21.6) 42.4)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc3() throws CSSException, IOException {
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
		//
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(53.1% 42.4 calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc4() throws CSSException, IOException {
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
		//
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(calc(2*24%) calc(-2*31.3) calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLABCalc4Alpha() throws CSSException, IOException {
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
		//
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
		//
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklab", lu.getFunctionName());
		assertEquals("oklab(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLabBad() throws CSSException, IOException {
		try {
			parsePropertyValue("oklab(12 48 0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(-12% 48.5 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(12% 48% 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(12% 48% 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(12% 48 89.1deg)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(74% 48 89.1/)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(74% 48 /89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(74%/48 21)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(74% 48 89.1//)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(74% a 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(var(--lightness) 48 89.1deg)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklab(var(--lightness-a) 89.1deg)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueOKLCH() throws CSSException, IOException {
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
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>#");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(CSSValueSyntax.Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueOKLCH2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42 57.76)");
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(57.76f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42 57.76)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH3() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCH4() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHangle() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHvar() throws CSSException, IOException {
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
	}

	@Test
	public void testParsePropertyValueOKLCHvar2() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHalpha() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCH2alpha() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCH3alpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57 / 0.6)");
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCH4alpha() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHangleAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("oklch(53.2% 42.4 57.76deg / 0.6)");
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
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.2% 42.4 57.76deg/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHalphaPcnt() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHvarAlpha() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHalphaTrailingVar() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHvarAlphaVar() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHvarAngleAlphaVar() throws CSSException, IOException {
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
	public void testParsePropertyValueOKLCHCalc() throws CSSException, IOException {
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
		//
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
	}

	@Test
	public void testParsePropertyValueOKLCHCalc2() throws CSSException, IOException {
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
		//
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(42.4f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.1% calc(2*21.6) 42.4)", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc3() throws CSSException, IOException {
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
		//
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(53.1% 42.4 calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc4() throws CSSException, IOException {
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
		//
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(calc(2*24%) calc(-2*31.3) calc(2*21.6))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLCHCalc4Alpha() throws CSSException, IOException {
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
		//
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
		//
		assertNull(param.getNextLexicalUnit());
		assertEquals("oklch", lu.getFunctionName());
		assertEquals("oklch(calc(2*24%) calc(-2*31.3) calc(2*21.6)/calc(2*0.18))", lu.toString());
	}

	@Test
	public void testParsePropertyValueOKLchBad() throws CSSException, IOException {
		try {
			parsePropertyValue("oklch(12 48 0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(-12% 48.6 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(12% 48% 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(12% 48% 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(12% 48deg 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(12% 48 89.1%)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(74% 48 89.1/)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(74% 48 /89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(74%/48 21)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(74% 48 89.1//)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
		//
		try {
			parsePropertyValue("oklch(74% a 89.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(value));
	}

}
