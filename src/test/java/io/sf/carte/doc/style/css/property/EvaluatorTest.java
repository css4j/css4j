/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class EvaluatorTest {

	private static SyntaxParser syntaxParser;

	private BaseCSSStyleDeclaration style;
	private Evaluator evaluator;

	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		evaluator = new Evaluator();
	}

	@Test
	public void testCalc() {
		style.setCssText("foo: calc(min(1.2 * 3, 3) * 8)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		CSSTypedValue number = evaluator.evaluateExpression(val.getExpression(), unit);
		assertEquals(24f, number.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
		assertTrue(number.isCalculatedNumber());
	}

	@Test
	public void testCalc2() {
		style.setCssText("foo: calc(2 - min(1.2 * 3, 3) * 8)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(-22f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc3() {
		style.setCssText("foo: calc(64 / (max(1.2 * 3, 4) * 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc4() {
		style.setCssText("foo: calc(30 - (max(1.2 * 3, 4) * 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(-2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc5() {
		style.setCssText("foo: calc(2 - (max(1.2 * 3, 4) - 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(6f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc6() {
		style.setCssText("foo: calc(14 - (max(1.2 * 3, 4) + 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc7() {
		style.setCssText("foo: calc(0 - (max(1.2 * 3, 4) + 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals("calc(0 - (max(1.2*3, 4) + 8))", val.getCssText());
		assertEquals("calc(0 - (max(1.2*3,4) + 8))", val.getMinifiedCssText(""));
		Unit unit = new Unit();
		assertEquals(-12f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc8() {
		style.setCssText("foo: calc(0px - (max(1.2 * 3px, 4px) + 8px))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals("calc(0px - (max(1.2*3px, 4px) + 8px))", val.getCssText());
		assertEquals("calc(0px - (max(1.2*3px,4px) + 8px))", val.getMinifiedCssText(""));
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(-12f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_PX),
				1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_PX, unit.getUnitType());
	}

	@Test
	public void testCalc9() {
		style.setCssText("foo: calc(0px - (1.2*8px))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals("calc(0px - 1.2*8px)", val.getCssText());
		assertEquals("calc(0px - 1.2*8px)", val.getMinifiedCssText(""));
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(-9.6f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_PX),
				1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_PX, unit.getUnitType());
	}

	@Test
	public void testCalc10() {
		style.setCssText("foo: calc(sqrt(1.2pt * 3.6pt * 8.1))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");


		Unit unit = new Unit();
		assertEquals(7.8872f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_PT, unit.getUnitType());
	}

	@Test
	public void testCalc11() {
		style.setCssText("foo: calc(1.2pt / 3.6pt / 8.1)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(0.04115f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPlainNumber() {
		style.setCssText("foo: calc(8.1)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(8.1f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5f);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPlainPercentage() {
		style.setCssText("foo: calc(48.1%)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(48.1f, evaluator.evaluateExpression(val.getExpression(), unit)
				.getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_PERCENTAGE, unit.getUnitType());
	}

	@Test
	public void testCalcExpectIntegerNoComputation() {
		style.setCssText("foo: calc(3.21)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		val.setExpectInteger();
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		CSSTypedValue result = evaluator.evaluateExpression(val);

		assertEquals(CSSUnit.CSS_NUMBER, result.getUnitType());
		assertEquals(3f, result.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testCalcExpectInteger() {
		style.setCssText("foo: calc(3.1 - 0.8)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		val.setExpectInteger();
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		CSSTypedValue result = evaluator.evaluateExpression(val);

		assertEquals(CSSUnit.CSS_NUMBER, result.getUnitType());
		assertEquals(2f, result.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testCalcBadUnit() {
		style.setCssText("foo: calc(2pt*5px)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateExpression(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testCalcBadUnit2() {
		style.setCssText("foo: calc(1/5px)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateExpression(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testCalcPrecedence1() {
		style.setCssText("foo: calc(7 + 2*4)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(15f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence2() {
		style.setCssText("foo: calc(7 + 4/2)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(9f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence3() {
		style.setCssText("foo: calc(9 - 2*4)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(1f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence4() {
		style.setCssText("foo: calc(9 - 4/2)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(7f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence5() {
		style.setCssText("foo: calc(2*4 + 7)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(15f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence6() {
		style.setCssText("foo: calc(4/2 + 7)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(9f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence7() {
		style.setCssText("foo: calc(2*4 - 7)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(1f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence8() {
		style.setCssText("foo: calc(9/3 - 1)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcZeroNeg() {
		style.setCssText("foo: calc(0 / (-1/0))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		float fval = evaluator.evaluateExpression(val).getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0x80000000, Float.floatToIntBits(fval));
	}

	@Test
	public void testCalcZeroNeg2() {
		style.setCssText("foo: calc(-1*0)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		float fval = evaluator.evaluateExpression(val).getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0x80000000, Float.floatToIntBits(fval));
	}

	@Test
	public void testCalcNaN() {
		style.setCssText("foo: calc(0 / 0)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateExpression(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
	}

	@Test
	public void testSqrtNaNinFunction() {
		style.setCssText("foo: calc(2*sqrt(-1.8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateExpression(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
	}

	@Test
	public void testCalcInfinity() {
		style.setCssText("foo: calc(1.2 / 0)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertTrue(Float.isInfinite(
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER)));
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcInfinityPt() {
		style.setCssText("foo: calc(1.2pt / 0)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		Unit unit = new Unit();
		assertTrue(Float.isInfinite(
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_PT)));
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_PT, unit.getUnitType());
	}

	@Test
	public void testCalcStoHz() {
		style.setCssText("foo: calc(1.2 / 3.6s)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_HZ, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(0.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_HZ), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcSCancelHz() {
		style.setCssText("foo: calc(1.2s * 3.6hz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(4.32f, evaluator.evaluateExpression(val.getExpression(), unit)
				.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcSCancelKHz() {
		style.setCssText("foo: calc(1.2s * 0.0036khz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(4.32f, evaluator.evaluateExpression(val.getExpression(), unit)
				.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcStoHz2() {
		style.setCssText("foo: calc(sqrt(1.2 / 3.6s / 2.1s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_HZ, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<frequency>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <frequency>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(0.39840954f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_HZ), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcMStoKHz() {
		style.setCssText("foo: calc(1.2 / 3.6ms)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_KHZ, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(333.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_HZ), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_KHZ, unit.getUnitType());
	}

	@Test
	public void testCalcMSCancelHz() {
		style.setCssText("foo: calc(1215ms * 3.6hz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(4.374f, evaluator.evaluateExpression(val.getExpression(), unit)
				.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcMSCancelKHz() {
		style.setCssText("foo: calc(1.2ms * 3.6khz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(4.32f, evaluator.evaluateExpression(val.getExpression(), unit)
				.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcHzToS() {
		style.setCssText("foo: calc(1.2 / 3.6Hz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_S, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<time>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <time>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(0.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_S), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_S, unit.getUnitType());
	}

	@Test
	public void testCalcHzToS2() {
		style.setCssText("foo: calc(sqrt(1.2 / 3.6Hz / 2.1Hz))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_S, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<time>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <time>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(0.39840954f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_S), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_S, unit.getUnitType());
	}

	@Test
	public void testCalcKHzToMS() {
		style.setCssText("foo: calc(1.2 / 3.6kHz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_MS, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<time>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <time>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(3.33333e-4,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_S), 1e-9);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_MS, unit.getUnitType());
	}

	@Test
	public void testCalcHzS() {
		style.setCssText("foo: calc(sqrt(1.2Hz * 3.6s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(2.078461f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcHzS2() {
		style.setCssText("foo: calc(sqrt(1.2Hz * 3Hz * 1.1Hz * 3.6s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_HZ, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<frequency>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <frequency>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		Unit unit = new Unit();
		assertEquals(3.775712f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_HZ), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcKHzMs() {
		style.setCssText("foo: calc(sqrt(1.2kHz * 3.6ms))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(2.078461f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcHzError() {
		style.setCssText("foo: calc(sqrt(1.2 / 4Hz / 3.6Hz / 2.1Hz))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateExpression(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
	}

	@Test
	public void testCalcUnimplementedFunction() {
		style.setCssText("foo: calc(unimplemented(6.1))");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMin1() {
		style.setCssText("foo: min(1.2 * 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3.6f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMin2() {
		style.setCssText("foo: min(1.2 * 3, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testMinNotCalculated1() {
		style.setCssText("foo: min(1.2, 3*2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1.2f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testMinNotCalculated2() {
		style.setCssText("foo: min(2.4*2, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testMin3() {
		style.setCssText("foo: min(1.2 * 3, 3, 4/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(2f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMinUnits() {
		style.setCssText("foo: min(1.2px * 3, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(2.7f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMinUnits3() {
		style.setCssText("foo: min(1.2px * 3, 3pt, 6px/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(2.25f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMinLengthPcnt() {
		style.setCssText("foo: min(1.2px * 3, 2%, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MIN, val.getFunction());
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.TRUE, val, "*");

		assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
	}

	@Test
	public void testMinBadUnits() {
		style.setCssText("foo: min(1.2px * 3em, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testMax1() {
		style.setCssText("foo: max(1.2 * 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MAX, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3.6f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMax2() {
		style.setCssText("foo: max(1.2 * 3, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MAX, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3.6f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMax3() {
		style.setCssText("foo: max(1.2 * 3, 3, 9/4)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MAX, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3.6f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMaxNotCalculated1() {
		style.setCssText("foo: max(4, 1.5*2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MAX, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(4f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testMaxNotCalculated2() {
		style.setCssText("foo: max(2*2, 5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.MAX, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(5f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testMaxUnits() {
		style.setCssText("foo: max(1.2px * 3, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testMaxUnits3() {
		style.setCssText("foo: max(1.2px * 3, 3pt, 5mm/4)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_MM, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(3.543308f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMaxLengthPcnt() {
		style.setCssText("foo: max(1.2px * 3, 2%, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.TRUE, val, "*");

		assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
	}

	/**
	 * The two arguments have different dimensions.
	 */
	@Test
	public void testMaxBadUnits() {
		style.setCssText("foo: max(1.2px * 3em, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	/**
	 * The result is a square length which is not a CSS unit.
	 */
	@Test
	public void testMaxNonCssUnit() {
		style.setCssText("foo: max(1.2px * 3em, 3pt * 2vw)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testClamp() {
		style.setCssText("foo: clamp(1.2 * 3, 8 * sin(45deg), 16/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.CLAMP, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(5.656854f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testClampNotCalculated1() {
		style.setCssText("foo: clamp(12, 4*2, 4*4)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.CLAMP, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(12f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testClampNotCalculated2() {
		style.setCssText("foo: clamp(1.6*2, 8, 4*4)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.CLAMP, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(8f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testClampNotCalculated3() {
		style.setCssText("foo: clamp(2*2, 2*9, 16)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.CLAMP, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(16f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testClampUnits() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt * sin(45deg), 20px/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.CLAMP, val.getFunction());
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(5.656854f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testClamp3() {
		style.setCssText("foo: clamp(0.6mm * 4, 8pt * sin(45deg), 20px/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(6.80315f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testClamp4() {
		style.setCssText("foo: clamp(0.6mm * 4, 12pt * sin(45deg), 20px/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(7.5f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testClampLengthPcnt() {
		style.setCssText("foo: clamp(0.4mm * 4, 8% * sin(45deg), 20px/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "*");

		assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
	}

	@Test
	public void testClampBadUnits() {
		style.setCssText("foo: clamp(0.4mm * 4pt, 8pt * sin(45deg), 20px/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testClampBadUnits2() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt * sin(45deg), 20px/2pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testClampBadUnits3() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt * sin(45deg), 20deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testClamp2argError() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testRound() {
		style.setCssText("foo: round(7.65)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(8f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_length() {
		style.setCssText("foo: round(4 * 2.2pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(9f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_2numbers() {
		style.setCssText("foo: round(20 - 12, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(9f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_Nearest_2numbers() {
		style.setCssText("foo: round(Nearest, 120px * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(200f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRoundUp() {
		style.setCssText("foo: round(Up,7.15)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(8f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_Up_2numbers() {
		// 90pt = 120px
		style.setCssText("foo: round(up, 90pt * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(300f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_Up_2numbers_neg() {
		style.setCssText("foo: round(up, -140px * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-200f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRoundDown() {
		style.setCssText("foo: round(Down, 7.95)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(7f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_Down_2numbers() {
		// 105pt = 140px
		style.setCssText("foo: round(down, 105pt * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(200f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_Down_2numbers_neg() {
		style.setCssText("foo: round(down, -120px * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-300f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRoundToZero() {
		style.setCssText("foo: round(to-zero,7.65)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(7f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRoundToZeroNeg() {
		style.setCssText("foo: round(To-Zero,-7.65)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-7f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_ToZero_2numbers() {
		style.setCssText("foo: round(to-zero, 140px * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(200f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_ToZero_2numbers_neg() {
		style.setCssText("foo: round(to-zero, -140px * 2, 100px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-200f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_ToZero_1number() {
		style.setCssText("foo: round(to-zero, 1.987px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRound_ToZero_1number_neg() {
		style.setCssText("foo: round(to-zero, -1.987px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-1f, typed.getFloatValue(CSSUnit.CSS_PX), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRoundInvalid() {
		style.setCssText("foo: round(7.65,2pc)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testMod() {
		style.setCssText("foo: mod(4 * 2, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(2f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testModNeg() {
		style.setCssText("foo: mod(-4 * 2, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMod_deg() {
		style.setCssText("foo: mod(135deg, -90deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_DEG, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-45f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testMod_deg2() {
		style.setCssText("foo: mod(-70deg, 20deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_DEG, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(10f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testModInvalid() {
		style.setCssText("foo: mod(4, 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testRem() {
		style.setCssText("foo: rem(10 * 2, 1.7)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1.3f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRemNeg() {
		style.setCssText("foo: rem(-4 * 2, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-2f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRemVwNeg() {
		style.setCssText("foo: rem(-18vw, 5vw)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-3f, typed.getFloatValue(CSSUnit.CSS_VW), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRem_deg() {
		style.setCssText("foo: rem(140deg, -90deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_DEG, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(50f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRem_deg2() {
		style.setCssText("foo: rem(-90deg, 20deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_DEG, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-10f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testRemInvalid() {
		style.setCssText("foo: rem(10, 1.7px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testSin() {
		style.setCssText("foo: sin(1.2 * 5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.SIN, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.1045285f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSinRad() {
		style.setCssText("foo: sin(.5235988)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.5f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-7f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSinHalfPi() {
		style.setCssText("foo: sin(pi*1rad / 2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSinHalfPiUC() {
		style.setCssText("foo: sin(PI*1rad / 2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSinBadUnit() {
		style.setCssText("foo: sin(5deg * 1rad)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testSinBadUnit2() {
		style.setCssText("foo: sin(1/5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testSin2argError() {
		style.setCssText("foo: sin(1rad, 5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testCos() {
		style.setCssText("foo: cos(1.2 * 5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.COS, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.994522f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testCosPi() {
		style.setCssText("foo: cos(PI)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.COS, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-1f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testCosRad() {
		style.setCssText("foo: cos(.52356)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.86604482f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testCosBadUnit() {
		style.setCssText("foo: cos(5deg * 1rad)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testCosBadUnit2() {
		style.setCssText("foo: cos(1/5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
	}

	@Test
	public void testCos2argError() {
		style.setCssText("foo: cos(1rad, 5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testTan() {
		style.setCssText("foo: tan(1.2 * 5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.TAN, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.105104f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testTanRad() {
		style.setCssText("foo: tan(.866025)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1.17580979f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-7f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testTanBadUnit() {
		style.setCssText("foo: tan(5deg * 1rad)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testTanBadUnit2() {
		style.setCssText("foo: tan(1/5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testTan2argError() {
		style.setCssText("foo: tan(1rad, 5deg)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testASin() {
		style.setCssText("foo: asin(0.2 * 2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.ASIN, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(23.578178f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testASinBadUnits() {
		style.setCssText("foo: asin(0.5pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testASin2argError() {
		style.setCssText("foo: asin(.1, .5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testACos() {
		style.setCssText("foo: acos(0.2 * 2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.ACOS, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(66.421822f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testAcosCos2Pi() {
		style.setCssText("foo: acos(cos(pi)/2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.ACOS, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(2.0944f, typed.getFloatValue(CSSUnit.CSS_RAD), 1e-4f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testACosNaN() {
		style.setCssText("foo: acos(1.8)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
	}

	@Test
	public void testACosBadUnits() {
		style.setCssText("foo: acos(.5pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testACos2argError() {
		style.setCssText("foo: acos(.1, .5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testATan() {
		style.setCssText("foo: atan(0.2 * 2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.ATAN, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(21.80141f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testATanUnits() {
		style.setCssText("foo: atan(2px/3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(26.565051f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testATanBadUnits() {
		style.setCssText("foo: atan(.5pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testATan2argError() {
		style.setCssText("foo: atan(.1, .5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testAtan2_1() {
		style.setCssText("foo: atan2(-1.5, 0.2 * 2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.ATAN2, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(-75.06858f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testAtan2_2() {
		style.setCssText("foo: atan2(0.2 * 2, -1.5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(165.068588f, typed.getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testATan2_UnitError() {
		style.setCssText("foo: atan2(.1pt, .5pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testATan2_3argError() {
		style.setCssText("foo: atan2(.1, .5, .9)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testPow() {
		style.setCssText("foo: pow(1.2 * 3, 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.POW, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(46.656f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testPowDimensionExponentError() {
		style.setCssText("foo: pow(3,2px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.POW, val.getFunction());
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testPow1argError() {
		style.setCssText("foo: pow(3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.POW, val.getFunction());
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testLog() {
		style.setCssText("foo: log(4.2 * 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(2.533696814f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testLogBase10() {
		style.setCssText("foo: log(24.2 * 3, 2*5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1.8609366207f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-7f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testLogArgInvalid() {
		style.setCssText("foo: log(23pc, 5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testLogBaseInvalid() {
		style.setCssText("foo: log(23, 5px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testExp() {
		style.setCssText("foo: exp(3.2 * 2.1)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(828.8175f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-3f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testExpInvalid() {
		style.setCssText("foo: exp(3.2ex)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testSqrt() {
		style.setCssText("foo: sqrt(1.2 * 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.SQRT, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(1.897367f, typed.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSqrtUnitMM() {
		style.setCssText("foo: sqrt(1.2pt * 3px)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.579673f, typed.getFloatValue(CSSUnit.CSS_MM), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSqrtAngle() {
		style.setCssText("foo: sqrt(asin(0.3) * acos(.02))");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.SQRT, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(0.687398f, typed.getFloatValue(CSSUnit.CSS_RAD), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testSqrtNaN() {
		style.setCssText("foo: sqrt(-1.8)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
	}

	@Test
	public void testSqrtBadUnits() {
		style.setCssText("foo: sqrt(1.2pt * 3px * 8pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testSqrt2argError() {
		style.setCssText("foo: sqrt(.1, .5)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testHypot() {
		style.setCssText("foo: hypot(1.2pt + 3.3px, 1mm + 0.01cm)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.HYPOT, val.getFunction());
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertEquals(4.819568f, typed.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testHypot3() {
		style.setCssText("foo: hypot(1.2pt + 3.3px, 1mm + 0.01cm, 0.2pc)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PC, val.computeUnitType());

		assertEquals(5.384073f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testHypotInconsistentUnits() {
		style.setCssText("foo: hypot(.1,.2px,.3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
	}

	@Test
	public void testSignPositive() {
		style.setCssText("foo: sign(1.2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.SIGN, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertTrue(1f == typed.getFloatValue(CSSUnit.CSS_NUMBER));
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testSignNeg() {
		style.setCssText("foo: sign(1.2 - 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertTrue(-1f == typed.getFloatValue(CSSUnit.CSS_NUMBER));
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testSignZero() {
		style.setCssText("foo: sign(0)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		assertTrue(0f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignZeroNeg() {
		style.setCssText("foo: sign(0 / (-1/0))");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		float fval = evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0x80000000, Float.floatToIntBits(fval));
	}

	@Test
	public void testSignUnitPositive() {
		style.setCssText("foo: sign(1.2pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		assertTrue(1f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignUnitNeg() {
		style.setCssText("foo: sign(1.2pt - 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		assertTrue(-1f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignUnitZero() {
		style.setCssText("foo: sign(0pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		assertTrue(0f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignPcnt() {
		style.setCssText("foo: sign(18%)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
	}

	@Test
	public void testSign2argError() {
		style.setCssText("foo: sign(.1,-1.1)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testAbsPositive() {
		style.setCssText("foo: abs(1.2)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.ABS, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertTrue(1.2f == typed.getFloatValue(CSSUnit.CSS_NUMBER));
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testAbsNeg() {
		style.setCssText("foo: abs(1 - 3)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertTrue(2f == typed.getFloatValue(CSSUnit.CSS_NUMBER));
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testAbsZero() {
		style.setCssText("foo: abs(0)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		float fval = typed.getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0, Float.floatToIntBits(fval));
		assertFalse(typed.isCalculatedNumber());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testAbsZeroNeg() {
		style.setCssText("foo: abs(0 / (-1/0))");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		float fval = typed.getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0, Float.floatToIntBits(fval));
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testAbsUnitPositive() {
		style.setCssText("foo: abs(1.2pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		float fval = typed.getFloatValue(CSSUnit.CSS_PT);
		assertTrue(1.2f == fval);
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testAbsUnitNeg() {
		style.setCssText("foo: abs(1pt - 3pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		assertTrue(2f == typed.getFloatValue(CSSUnit.CSS_PT));
		assertTrue(typed.isCalculatedNumber());
	}

	@Test
	public void testAbsUnitZero() {
		style.setCssText("foo: abs(0pt)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PT, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		CSSTypedValue typed = evaluator.evaluateFunction(val);
		float fval = typed.getFloatValue(CSSUnit.CSS_PT);
		assertEquals(0, Float.floatToIntBits(fval));
		assertFalse(typed.isCalculatedNumber());
	}

	@Test
	public void testAbs2argError() {
		style.setCssText("foo: abs(.1,-1.1)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "*");

		DOMException e = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.SYNTAX_ERR, e.code);
	}

	@Test
	public void testUnimplementedFunction() {
		style.setCssText("foo: unimplemented(6.1)");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testUnimplementedFunctionArgument() {
		style.setCssText("foo: sqrt(1.2pt * 3px * unimplemented(6.1))");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(MathFunction.SQRT, val.getFunction());
		assertEquals(CSSUnit.CSS_INVALID, val.computeUnitType());

		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");

		DOMException ex = assertThrows(DOMException.class, () -> evaluator.evaluateFunction(val));
		assertEquals(DOMException.INVALID_ACCESS_ERR, ex.code);
	}

	@Test
	public void testUnitConversion() {
		Unit unit = new Unit(CSSUnit.CSS_PT);
		assertEquals(1f, unit.convert(1f, CSSUnit.CSS_PT), 1e-5f);
		assertEquals(1.333333f, unit.convert(1f, CSSUnit.CSS_PX), 1e-5f);
		unit.setExponent(2);
		assertEquals(1f, unit.convert(1f, CSSUnit.CSS_PT), 1e-5f);
		assertEquals(1.777777f, unit.convert(1f, CSSUnit.CSS_PX), 1e-5f);
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
