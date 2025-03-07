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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserCalcTest {

	private Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParsePropertyValueCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100% - 3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcNegative() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(-3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertEquals("calc(-3em)", lu.toString());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcNumber() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(-2*3.4)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(3.4f, param.getFloatValue(), 1e-5f);
		assertEquals("", param.getDimensionUnitText());
		assertEquals("calc(-2*3.4)", lu.toString());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<integer>"); // calc() clamps to integer
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.TRUE, lu, "<integer>#");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalc2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(10em - 2%)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(10em - 2%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalc3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - 2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 2em)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalc4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc((10em + 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, subvalues.getCssUnit());
		assertEquals(10f, subvalues.getFloatValue(), 1e-5f);
		assertEquals("em", subvalues.getDimensionUnitText());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.PERCENTAGE, subvalues.getLexicalUnitType());
		assertEquals(2f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());

		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((10em + 2%)*3)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalc5() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100%/3 - 2*1em - 2*1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("%", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("px", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc6() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(0ex + max(10em, 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.MATH_FUNCTION, param.getLexicalUnitType());
		assertEquals("max", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();

		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalType.DIMENSION, subparams.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, subparams.getCssUnit());
		assertEquals(10f, subparams.getFloatValue(), 1e-5f);
		assertEquals("em", subparams.getDimensionUnitText());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(2f, subparams.getFloatValue(), 1e-5f);
		assertNull(subparams.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(0ex + max(10em, 2%)*3)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalc7() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();

		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, subvalues.getCssUnit());
		assertEquals(0.4f, subvalues.getFloatValue(), 1e-5f);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());

		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(1em + (0.4vw + 0.25vh)/2)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcTime() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"calc(2s*1 + 100*2ms + 1/2Hz + 2s*3s/2s - 5ms/2ms*0.8s)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<time>");
		assertMatch(Match.TRUE, lu, "<time>#");
		assertMatch(Match.TRUE, lu, "<time>+");
		assertMatch(Match.FALSE, lu, "<frequency>");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <time>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcFrequency() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"calc(2Hz*11 + 1.01*2kHz + 1/2s + 100Hz/1khz*2hz + 1Hz*2khz/50hz)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<frequency>");
		assertMatch(Match.TRUE, lu, "<frequency>#");
		assertMatch(Match.TRUE, lu, "<frequency>+");
		assertMatch(Match.FALSE, lu, "<time>");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <frequency>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcSin() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(2em*sin(45deg))");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueWebkitCalcAtan() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-webkit-calc(2*atan(0.7))");
		assertEquals("-webkit-calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<angle>");
		assertMatch(Match.TRUE, lu, "<angle>#");
		assertMatch(Match.TRUE, lu, "<angle>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <angle>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcPow() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(2*sqrt(pow(2em,attr(data-exp type(<number>)))))");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueSqrtPow() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(2em,attr(data-exp type(<number>))))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueSqrtPowWebkitCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(2em,-webkit-calc(1 + 1)))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcInvalidUnit() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(1em*1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc(1em*1px)", lu.toString());

		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length-percentage>");
	}

	@Test
	public void testParsePropertyValueCalcVarSubexpression() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc((var(--subexp)) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();

		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.VAR, subvalues.getLexicalUnitType());
		assertNull(subvalues.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((var(--subexp))*3)", lu.toString());

		assertMatch(Match.PENDING, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<number>#");
		assertMatch(Match.PENDING, lu, "<number>+");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>#");
		assertMatch(Match.PENDING, lu, "<length-percentage>+");
		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcNegDenom() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/-2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();

		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, subvalues.getCssUnit());
		assertEquals(0.4f, subvalues.getFloatValue(), 1e-5f);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());

		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(1em + (0.4vw + 0.25vh)/-2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcNegDenom2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc((75vw*9/16 - 100vh)/-2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();

		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, subvalues.getCssUnit());
		assertEquals(75f, subvalues.getFloatValue(), 1e-5f);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.INTEGER, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, subvalues.getCssUnit());
		assertEquals(9, subvalues.getIntegerValue());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_SLASH, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.INTEGER, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, subvalues.getCssUnit());
		assertEquals(16, subvalues.getIntegerValue());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_MINUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(100f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());

		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(-2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((75vw*9/16 - 100vh)/-2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcPlusNegValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh + -2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh + -2em)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcPlusZerolessNegValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh + -.2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-0.2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh + -0.2em)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcMinusPosValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - +2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 2em)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcMinusZerolessPosValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - +.2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 0.2em)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcInsideCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(calc(2.1 * 3px) - 1pt)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());

		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(2.1f, subparam.getFloatValue(), 1e-6f);
		subparam = subparam.getNextLexicalUnit();
		assertNotNull(subparam);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertNotNull(subparam);
		assertEquals(LexicalType.DIMENSION, subparam.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, subparam.getCssUnit());
		assertEquals(3f, subparam.getFloatValue(), 1e-6f);
		assertNull(subparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-6f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(calc(2.1*3px) - 1pt)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start type(<integer>), 1) - 1)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("start type(<integer>), 1", param.getParameters().toString());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start type(<integer>), 1) - 1)", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.TRUE, lu, "<integer>");
		assertMatch(Match.TRUE, lu, "<integer>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<angle>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcAttr2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start type(<length>), 8%) - 1.1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		LexicalUnit attrparam = param.getParameters();
		assertNotNull(attrparam);
		assertEquals(LexicalType.IDENT, attrparam.getLexicalUnitType());
		assertEquals("start", attrparam.getStringValue());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.TYPE_FUNCTION, attrparam.getLexicalUnitType());
		assertEquals("length", attrparam.getParameters().getSyntax().getName());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.OPERATOR_COMMA, attrparam.getLexicalUnitType());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.PERCENTAGE, attrparam.getLexicalUnitType());
		assertEquals(8f, attrparam.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(1.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start type(<length>), 8%) - 1.1px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<angle>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<length> | <percentage>");
		assertMatch(Match.TRUE, lu, "<length> | <percentage>#");
		assertMatch(Match.TRUE, lu, "<length> | <percentage>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcAttr3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start type(<length>), 8%) * 2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		LexicalUnit attrparam = param.getParameters();
		assertNotNull(attrparam);
		assertEquals(LexicalType.IDENT, attrparam.getLexicalUnitType());
		assertEquals("start", attrparam.getStringValue());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.TYPE_FUNCTION, attrparam.getLexicalUnitType());
		assertEquals("length", attrparam.getParameters().getSyntax().getName());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.OPERATOR_COMMA, attrparam.getLexicalUnitType());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.PERCENTAGE, attrparam.getLexicalUnitType());
		assertEquals(8f, attrparam.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start type(<length>), 8%)*2)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<angle>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<length> | <percentage>#");
		assertMatch(Match.TRUE, lu, "<length> | <percentage>+");
		assertMatch(Match.TRUE, lu, "<length> | <percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCalcCustom() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(var(--foo, 1%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();
		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalType.IDENT, subparams.getLexicalUnitType());
		assertEquals("--foo", subparams.getStringValue());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(1f, subparams.getFloatValue(), 1e-5f);
		assertNull(subparams.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(var(--foo, 1%)*3)", lu.toString());

		assertMatch(Match.PENDING, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<number>#");
		assertMatch(Match.PENDING, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>#");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyBadCalc() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% - 3em"));
	}

	@Test
	public void testParsePropertyBadCalc3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% -"));
	}

	@Test
	public void testParsePropertyBadCalc4() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% -)"));
	}

	@Test
	public void testParsePropertyBadCalc5() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100%-)"));
	}

	@Test
	public void testParsePropertyBadCalc6() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100%+)"));
	}

	@Test
	public void testParsePropertyBadCalc7() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100%-2em)"));
	}

	@Test
	public void testParsePropertyBadCalc8() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("calc(100% -!important"));
		assertEquals(12, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadCalc9() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% + - 2em)"));
	}

	@Test
	public void testParsePropertyBadCalc10() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% - + 2em)"));
	}

	@Test
	public void testParsePropertyBadCalc11() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% + + 2em)"));
	}

	@Test
	public void testParsePropertyBadCalc12() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% + * 2em)"));
	}

	@Test
	public void testParsePropertyBadCalc13() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% * + 2em)"));
	}

	@Test
	public void testParsePropertyBadCalc14() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% * - 2em)"));
	}

	@Test
	public void testParsePropertyBadCalc15() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(100% * * 2em)"));
	}

	@Test
	public void testParsePropertyBadCalcSignedSubexpression() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(+(2em * 1))"));
	}

	@Test
	public void testParsePropertyBadCalcSignedSubexpression2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(-(2em * 1))"));
	}

	@Test
	public void testParsePropertyBadExpression() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("3em*2"));
	}

	@Test
	public void testParsePropertyBadExpression2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(1)*2"));
	}

	@Test
	public void testParsePropertyBadExpression3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("calc(1)+2"));
	}

	@Test
	public void testParsePropertyValueAbs() throws CSSException {
		LexicalUnit lu = parsePropertyValue("abs(10em + 2% *1.2 - 1px/2)");
		assertEquals("abs", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("abs(10em + 2%*1.2 - 1px/2)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueMax() throws CSSException {
		LexicalUnit lu = parsePropertyValue("max(10em, 2%)");
		assertEquals("max", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("max(10em, 2%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueMaxBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("max(10em, 2%"));
	}

	@Test
	public void testParsePropertyValueMaxBad2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("max(10em, 2%!important"));
	}

	@Test
	public void testParsePropertyValueMin() throws CSSException {
		LexicalUnit lu = parsePropertyValue("min(10em, 2%)");
		assertEquals("min", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("min(10em, 2%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length-percentage>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("clamp(10deg, 0.2rad, 25deg)");
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertEquals("clamp", lu.getFunctionName());

		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_RAD, param.getCssUnit());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("clamp(10deg, 0.2rad, 25deg)", lu.toString());

		assertMatch(Match.TRUE, lu, "<angle>");
		assertMatch(Match.TRUE, lu, "<angle>#");
		assertMatch(Match.TRUE, lu, "<angle>+");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <angle>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <angle>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <angle>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueClampVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("clamp(10deg, var(--angle), 25deg)");
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertEquals("clamp", lu.getFunctionName());

		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("clamp(10deg, var(--angle), 25deg)", lu.toString());

		assertMatch(Match.TRUE, lu, "<angle>");
		assertMatch(Match.TRUE, lu, "<angle>#");
		assertMatch(Match.TRUE, lu, "<angle>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <angle>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <angle>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <angle>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueRound() throws CSSException {
		LexicalUnit lu = parsePropertyValue("round(18em, 10px)");
		assertEquals("round", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(18f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("round(18em, 10px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueRoundUp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("round(UP, 18em, 10px)");
		assertEquals("round", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("UP", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(18f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("round(UP, 18em, 10px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueMod() throws CSSException {
		LexicalUnit lu = parsePropertyValue("mod(18em, 10px)");
		assertEquals("mod", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(18f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("mod(18em, 10px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueRem() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rem(18em, 10px)");
		assertEquals("rem", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(18f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rem(18em, 10px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLog() throws CSSException {
		LexicalUnit lu = parsePropertyValue("log(18.3, 10)");
		assertEquals("log", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(18.3f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(10, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("log(18.3, 10)", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.TRUE, lu, "<integer>"); // Computations can round to integer
		assertMatch(Match.FALSE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueExp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Exp(18)");
		assertEquals("Exp", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(18, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("Exp(18)", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.TRUE, lu, "<integer>"); // Computations can round to integer
		assertMatch(Match.FALSE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Sign(-18)");
		assertEquals("Sign", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-18, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("Sign(-18)", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.TRUE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePowMix() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(pow(4.2em, 4), 0.5))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.MATH_FUNCTION, param.getLexicalUnitType());
		assertEquals("pow", param.getFunctionName());
		assertEquals("sqrt(pow(pow(4.2em, 4), 0.5))", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePowAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(3ex, attr(data-exp type(<integer>))))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(3ex, attr(data-exp type(<integer>))))", lu.toString());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePowAttrInvalidExponent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(3ex, attr(data-exp type(<length>))))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(3ex, attr(data-exp type(<length>))))", lu.toString());

		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length>#");
	}

	@Test
	public void testParsePropertyValuePowVarBase() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(var(--my-exp),2))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(var(--my-exp), 2))", lu.toString());

		assertMatch(Match.PENDING, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePowNumberVarExp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(3, var(--my-exp)))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(3, var(--my-exp)))", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePowLengthVarExp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(3ex, var(--my-exp)))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(3ex, var(--my-exp)))", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
	}

	@Test
	public void testParsePropertyValuePowLengthFuncExpVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(3ex, abs(var(--my-exp))))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(3ex, abs(var(--my-exp))))", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<color>");
	}

	@Test
	public void testParsePropertyValuePowLengthFuncExpVar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("sqrt(pow(3ex, pow(2em,var(--my-exp))/2px))");
		assertEquals("sqrt", lu.getFunctionName());
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("sqrt(pow(3ex, pow(2em, var(--my-exp))/2px))", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<color>");
	}

	@Test
	public void testParsePropertyValueFunctionTrigonometric() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("cos(30deg), tan(45deg)");
		assertEquals(LexicalType.MATH_FUNCTION, lunit.getLexicalUnitType());
		assertEquals("cos", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(30f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertEquals("tan", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(45f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());

		assertEquals("cos(30deg), tan(45deg)", lunit.toString());

		assertMatch(Match.TRUE, lunit, "<number>#");
		assertMatch(Match.FALSE, lunit, "<number>");
		assertMatch(Match.FALSE, lunit, "<number>+");
		assertMatch(Match.FALSE, lunit, "<color>");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <number>#");
		assertMatch(Match.FALSE, lunit, "<custom-ident> | <number>+");
		assertMatch(Match.FALSE, lunit, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyValueFunctionTrigonometricInverse() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("acos(.62), atan(0.965)");
		assertEquals(LexicalType.MATH_FUNCTION, lunit.getLexicalUnitType());
		assertEquals("acos", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.62f, param.getFloatValue(), 1e-5f);
		assertEquals("", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.MATH_FUNCTION, lu.getLexicalUnitType());
		assertEquals("atan", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.965f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("acos(0.62), atan(0.965)", lunit.toString());

		assertMatch(Match.TRUE, lunit, "<angle>#");
		assertMatch(Match.FALSE, lunit, "<angle>");
		assertMatch(Match.FALSE, lunit, "<angle>+");
		assertMatch(Match.FALSE, lunit, "<number>");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <angle>#");
		assertMatch(Match.FALSE, lunit, "<custom-ident> | <angle>+");
		assertMatch(Match.FALSE, lunit, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, lunit, "<transform-list>");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyValueFunctionAtan2() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("atan2(-0.62,0.965)");
		assertEquals(LexicalType.MATH_FUNCTION, lunit.getLexicalUnitType());
		assertEquals("atan2", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(-0.62f, param.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.965f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("atan2(-0.62, 0.965)", lunit.toString());

		assertMatch(Match.TRUE, lunit, "<angle>");
		assertMatch(Match.TRUE, lunit, "<angle>#");
		assertMatch(Match.TRUE, lunit, "<angle>+");
		assertMatch(Match.FALSE, lunit, "<number>");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <angle>#");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <angle>+");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, lunit, "<transform-list>");
		assertMatch(Match.TRUE, lunit, "*");
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
