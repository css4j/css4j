/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class EvaluatorTest {

	private BaseCSSStyleDeclaration style;
	private Evaluator evaluator;

	@Before
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
		ExtendedCSSPrimitiveValue number = evaluator.evaluateExpression(val.getExpression(), unit);
		assertEquals(24f, number.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
		assertTrue(number.isCalculatedNumber());
	}

	@Test
	public void testCalc2() {
		style.setCssText("foo: calc(2 - min(1.2 * 3, 3) * 8)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(-22f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc3() {
		style.setCssText("foo: calc(64 / (max(1.2 * 3, 4) * 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc4() {
		style.setCssText("foo: calc(30 - (max(1.2 * 3, 4) * 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(-2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc5() {
		style.setCssText("foo: calc(2 - (max(1.2 * 3, 4) - 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(6f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalc6() {
		style.setCssText("foo: calc(14 - (max(1.2 * 3, 4) + 8))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
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
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
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
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_PX),
				1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_PX, unit.getUnitType());
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
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_PX),
				1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_PX, unit.getUnitType());
	}

	@Test
	public void testCalc10() {
		style.setCssText("foo: calc(sqrt(1.2pt * 3.6pt * 8.1))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(7.8872f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_PX), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_PT, unit.getUnitType());
	}

	@Test
	public void testCalc11() {
		style.setCssText("foo: calc(1.2pt / 3.6pt / 8.1)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(0.04115f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence1() {
		style.setCssText("foo: calc(7 + 2*4)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(15f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence2() {
		style.setCssText("foo: calc(7 + 4/2)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(9f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence3() {
		style.setCssText("foo: calc(9 - 2*4)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(1f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence4() {
		style.setCssText("foo: calc(9 - 4/2)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(7f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence5() {
		style.setCssText("foo: calc(2*4 + 7)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(15f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence6() {
		style.setCssText("foo: calc(4/2 + 7)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(9f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence7() {
		style.setCssText("foo: calc(2*4 - 7)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(1f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcPrecedence8() {
		style.setCssText("foo: calc(9/3 - 1)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(2f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
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
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER)));
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcInfinityPt() {
		style.setCssText("foo: calc(1.2pt / 0)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertTrue(Float.isInfinite(
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_PT)));
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_PT, unit.getUnitType());
	}

	@Test
	public void testCalcStoHz() {
		style.setCssText("foo: calc(1.2 / 3.6s)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(0.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_HZ), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcStoHz2() {
		style.setCssText("foo: calc(sqrt(1.2 / 3.6s / 2.1s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(0.39840954f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_HZ), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcMStoKHz() {
		style.setCssText("foo: calc(1.2 / 3.6ms)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(333.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_HZ), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_KHZ, unit.getUnitType());
	}

	@Test
	public void testCalcHzToS() {
		style.setCssText("foo: calc(1.2 / 3.6Hz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(0.33333f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_S), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_S, unit.getUnitType());
	}

	@Test
	public void testCalcHzToS2() {
		style.setCssText("foo: calc(sqrt(1.2 / 3.6Hz / 2.1Hz))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(0.39840954f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_S), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_S, unit.getUnitType());
	}

	@Test
	public void testCalcKHzToMS() {
		style.setCssText("foo: calc(1.2 / 3.6kHz)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(3.33333e-4,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_S), 1e-9);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_MS, unit.getUnitType());
	}

	@Test
	public void testCalcHzS() {
		style.setCssText("foo: calc(sqrt(1.2Hz * 3.6s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(2.078461f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
	}

	@Test
	public void testCalcHzS2() {
		style.setCssText("foo: calc(sqrt(1.2Hz * 3Hz * 1.1Hz * 3.6s))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(3.775712f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_HZ), 1e-5);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_HZ, unit.getUnitType());
	}

	@Test
	public void testCalcKHzMs() {
		style.setCssText("foo: calc(sqrt(1.2kHz * 3.6ms))");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		Unit unit = new Unit();
		assertEquals(2.078461f,
				evaluator.evaluateExpression(val.getExpression(), unit).getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				1e-5);
		assertEquals(0, unit.getExponent());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, unit.getUnitType());
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
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMin2() {
		style.setCssText("foo: min(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMin3() {
		style.setCssText("foo: min(1.2 * 3, 3, 4/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMinUnits() {
		style.setCssText("foo: min(1.2px * 3, 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2.7f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testMinUnits3() {
		style.setCssText("foo: min(1.2px * 3, 3pt, 6px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2.25f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testMax1() {
		style.setCssText("foo: max(1.2 * 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMax2() {
		style.setCssText("foo: max(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMax3() {
		style.setCssText("foo: max(1.2 * 3, 3, 9/4)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMaxUnits() {
		style.setCssText("foo: max(1.2px * 3, 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testMaxUnits3() {
		style.setCssText("foo: max(1.2px * 3, 3pt, 5mm/4)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.543308f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testClamp() {
		style.setCssText("foo: clamp(1.2 * 3, 8 * sin(45deg), 16/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.656854f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testClampUnits() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.656854f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testClamp3() {
		style.setCssText("foo: clamp(0.6mm * 4, 8pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(6.80315f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testClamp4() {
		style.setCssText("foo: clamp(0.6mm * 4, 12pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(7.5f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testSin() {
		style.setCssText("foo: sin(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.1045285f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCos() {
		style.setCssText("foo: cos(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.994522f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testTan() {
		style.setCssText("foo: tan(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.105104f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testASin() {
		style.setCssText("foo: asin(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(23.578178f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_DEG), 1e-5);
	}

	@Test
	public void testACos() {
		style.setCssText("foo: acos(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(66.4218216f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_DEG), 1e-5);
	}

	@Test
	public void testATan() {
		style.setCssText("foo: atan(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(21.80141f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_DEG), 1e-5);
	}

	@Test
	public void testATanUnits() {
		style.setCssText("foo: atan(2px/3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(26.565051f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_DEG), 1e-5);
	}

	@Test
	public void testAtan2_1() {
		style.setCssText("foo: atan2(-1.5, 0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(-75.06858f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_DEG), 1e-5);
	}

	@Test
	public void testAtan2_2() {
		style.setCssText("foo: atan2(0.2 * 2, -1.5)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(165.068588f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_DEG), 1e-5);
	}

	@Test
	public void testPow() {
		style.setCssText("foo: pow(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(46.656f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testSqrt() {
		style.setCssText("foo: sqrt(1.2 * 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1.897367f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testSqrtUnit() {
		style.setCssText("foo: sqrt(1.2pt * 3px)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1.643168f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testSqrtUnitMM() {
		style.setCssText("foo: sqrt(1.2pt * 3px)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.579673f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_MM), 1e-5);
	}

	@Test
	public void testHypot() {
		style.setCssText("foo: hypot(1.2pt + 3.3px, 1mm + 0.01cm)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(4.819568f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testHypot3() {
		style.setCssText("foo: hypot(1.2pt + 3.3px, 1mm + 0.01cm, 0.2pc)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.384073f, evaluator.evaluateFunction(val).getFloatValue(CSSPrimitiveValue.CSS_PT), 1e-5);
	}

	@Test
	public void testUnitConversion() {
		Unit unit = new Unit(CSSPrimitiveValue.CSS_PT);
		assertEquals(1f, unit.convert(1f, CSSPrimitiveValue.CSS_PT), 1e-5);
		assertEquals(1.333333f, unit.convert(1f, CSSPrimitiveValue.CSS_PX), 1e-5);
		unit.setExponent(2);
		assertEquals(1f, unit.convert(1f, CSSPrimitiveValue.CSS_PT), 1e-5);
		assertEquals(1.777777f, unit.convert(1f, CSSPrimitiveValue.CSS_PX), 1e-5);
	}

}
