/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class EvaluatorTest {

	private BaseCSSStyleDeclaration style;
	private Evaluator evaluator;

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
		Unit unit = new Unit();
		assertEquals(0.04115f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSUnit.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcBadUnit() {
		style.setCssText("foo: calc(2pt*5px)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateExpression(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testCalcBadUnit2() {
		style.setCssText("foo: calc(1/5px)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateExpression(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testCalcPrecedence1() {
		style.setCssText("foo: calc(7 + 2*4)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
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
		try {
			evaluator.evaluateExpression(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
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
		Unit unit = new Unit();
		assertEquals(0.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_HZ), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcStoHz2() {
		style.setCssText("foo: calc(sqrt(1.2 / 3.6s / 2.1s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
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
		Unit unit = new Unit();
		assertEquals(333.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSUnit.CSS_HZ), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_KHZ, unit.getUnitType());
	}

	@Test
	public void testCalcHzToS() {
		style.setCssText("foo: calc(1.2 / 3.6Hz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
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
		try {
			evaluator.evaluateExpression(val);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
	}

	@Test
	public void testMin1() {
		style.setCssText("foo: min(1.2 * 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testMin2() {
		style.setCssText("foo: min(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testMin3() {
		style.setCssText("foo: min(1.2 * 3, 3, 4/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testMinUnits() {
		style.setCssText("foo: min(1.2px * 3, 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2.7f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testMinUnits3() {
		style.setCssText("foo: min(1.2px * 3, 3pt, 6px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2.25f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testMax1() {
		style.setCssText("foo: max(1.2 * 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testMax2() {
		style.setCssText("foo: max(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testMax3() {
		style.setCssText("foo: max(1.2 * 3, 3, 9/4)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testMaxUnits() {
		style.setCssText("foo: max(1.2px * 3, 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testMaxUnits3() {
		style.setCssText("foo: max(1.2px * 3, 3pt, 5mm/4)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.543308f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testClamp() {
		style.setCssText("foo: clamp(1.2 * 3, 8 * sin(45deg), 16/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.656854f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testClampUnits() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.656854f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testClamp3() {
		style.setCssText("foo: clamp(0.6mm * 4, 8pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(6.80315f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testClamp4() {
		style.setCssText("foo: clamp(0.6mm * 4, 12pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(7.5f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testSin() {
		style.setCssText("foo: sin(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.1045285f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testSinRad() {
		style.setCssText("foo: sin(.5235988)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.5f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-7f);
	}

	@Test
	public void testSinHalfPi() {
		style.setCssText("foo: sin(pi*1rad / 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testSinHalfPiUC() {
		style.setCssText("foo: sin(PI*1rad / 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testSinBadUnit() {
		style.setCssText("foo: sin(5deg * 1rad)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testSinBadUnit2() {
		style.setCssText("foo: sin(1/5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testCos() {
		style.setCssText("foo: cos(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.994522f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testCosRad() {
		style.setCssText("foo: cos(.52356)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.86604482f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-7f);
	}

	@Test
	public void testCosBadUnit() {
		style.setCssText("foo: cos(5deg * 1rad)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testCosBadUnit2() {
		style.setCssText("foo: cos(1/5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testTan() {
		style.setCssText("foo: tan(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.105104f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testTanRad() {
		style.setCssText("foo: tan(.866025)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1.17580979f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-7f);
	}

	@Test
	public void testTanBadUnit() {
		style.setCssText("foo: tan(5deg * 1rad)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testTanBadUnit2() {
		style.setCssText("foo: tan(1/5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.TYPE_MISMATCH_ERR, e.code);
		}
	}

	@Test
	public void testASin() {
		style.setCssText("foo: asin(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(23.578178f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
	}

	@Test
	public void testACos() {
		style.setCssText("foo: acos(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(66.4218216f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
	}

	@Test
	public void testATan() {
		style.setCssText("foo: atan(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(21.80141f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
	}

	@Test
	public void testATanUnits() {
		style.setCssText("foo: atan(2px/3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(26.565051f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
	}

	@Test
	public void testAtan2_1() {
		style.setCssText("foo: atan2(-1.5, 0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(-75.06858f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
	}

	@Test
	public void testAtan2_2() {
		style.setCssText("foo: atan2(0.2 * 2, -1.5)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(165.068588f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_DEG), 1e-5f);
	}

	@Test
	public void testPow() {
		style.setCssText("foo: pow(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(46.656f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testSqrt() {
		style.setCssText("foo: sqrt(1.2 * 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1.897367f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	@Test
	public void testSqrtUnit() {
		style.setCssText("foo: sqrt(1.2pt * 3px)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1.643168f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testSqrtUnitMM() {
		style.setCssText("foo: sqrt(1.2pt * 3px)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.579673f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_MM), 1e-5f);
	}

	@Test
	public void testHypot() {
		style.setCssText("foo: hypot(1.2pt + 3.3px, 1mm + 0.01cm)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(4.819568f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testHypot3() {
		style.setCssText("foo: hypot(1.2pt + 3.3px, 1mm + 0.01cm, 0.2pc)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.384073f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT), 1e-5f);
	}

	@Test
	public void testSignPos() {
		style.setCssText("foo: sign(1.2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(1f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignNeg() {
		style.setCssText("foo: sign(1.2 - 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(-1f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignZero() {
		style.setCssText("foo: sign(0)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(0f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignZeroNeg() {
		style.setCssText("foo: sign(0 / (-1/0))");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		float fval = evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0x80000000, Float.floatToIntBits(fval));
	}

	@Test
	public void testSignUnitPos() {
		style.setCssText("foo: sign(1.2pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(1f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignUnitNeg() {
		style.setCssText("foo: sign(1.2pt - 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(-1f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignUnitZero() {
		style.setCssText("foo: sign(0pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(0f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testSignPcnt() {
		style.setCssText("foo: sign(18%)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		try {
			evaluator.evaluateFunction(val);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_SUPPORTED_ERR, e.code);
		}
	}

	@Test
	public void testAbsPos() {
		style.setCssText("foo: abs(1.2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(1.2f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testAbsNeg() {
		style.setCssText("foo: abs(1 - 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(2f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void testAbsZero() {
		style.setCssText("foo: abs(0)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		float fval = evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0, Float.floatToIntBits(fval));
	}

	@Test
	public void testAbsZeroNeg() {
		style.setCssText("foo: abs(0 / (-1/0))");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		float fval = evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER);
		assertEquals(0, Float.floatToIntBits(fval));
	}

	@Test
	public void testAbsUnitPos() {
		style.setCssText("foo: abs(1.2pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(1.2f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT));
	}

	@Test
	public void testAbsUnitNeg() {
		style.setCssText("foo: abs(1pt - 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertTrue(2f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT));
	}

	@Test
	public void testAbsUnitZero() {
		style.setCssText("foo: abs(0pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		float fval = evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PT);
		assertEquals(0, Float.floatToIntBits(fval));
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

}
