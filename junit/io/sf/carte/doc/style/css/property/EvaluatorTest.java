/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;

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
		CSSStyleDeclarationRule styleRule = sheet.createCSSStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		evaluator = new Evaluator();
	}

	@Test
	public void testCalc() {
		style.setCssText("foo: calc(min(1.2 * 3, 3) * 8)");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(24f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc2() {
		style.setCssText("foo: calc(2 - min(1.2 * 3, 3) * 8)");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(-22f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc3() {
		style.setCssText("foo: calc(64 / (max(1.2 * 3, 4) * 8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc4() {
		style.setCssText("foo: calc(30 - (max(1.2 * 3, 4) * 8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(-2f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc5() {
		style.setCssText("foo: calc(2 - (max(1.2 * 3, 4) - 8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(6f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc6() {
		style.setCssText("foo: calc(14 - (max(1.2 * 3, 4) + 8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc7() {
		style.setCssText("foo: calc(0 - (max(1.2 * 3, 4) + 8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals("calc(0 - (max(1.2*3, 4) + 8))", val.getCssText());
		assertEquals("calc(0 - (max(1.2*3,4) + 8))", val.getMinifiedCssText(""));
		assertEquals(-12f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc8() {
		style.setCssText("foo: calc(-(max(1.2 * 3, 4) + 8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals("calc( - (max(1.2*3, 4) + 8))", val.getCssText());
		assertEquals("calc( - (max(1.2*3,4) + 8))", val.getMinifiedCssText(""));
		assertEquals(-12f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCalc9() {
		style.setCssText("foo: calc(-(1.2*8))");
		ExpressionContainerValue val = (ExpressionContainerValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals("calc( - 1.2*8)", val.getCssText());
		assertEquals("calc( - 1.2*8)", val.getMinifiedCssText(""));
		assertEquals(-9.6f, evaluator.evaluateExpression(val.getExpression(), CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMin() {
		style.setCssText("foo: min(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMin2() {
		style.setCssText("foo: min(1.2px * 3, 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(2.7f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_PT).getFloatValue(CSSPrimitiveValue.CSS_PT),
				1e-5);
	}

	@Test
	public void testMax() {
		style.setCssText("foo: max(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3.6f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testMax2() {
		style.setCssText("foo: max(1.2px * 3, 3pt)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(3f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_PT).getFloatValue(CSSPrimitiveValue.CSS_PT),
				1e-5);
	}

	@Test
	public void testClamp() {
		style.setCssText("foo: clamp(1.2 * 3, 8 * sin(45deg), 16/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.656854f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testClamp2() {
		style.setCssText("foo: clamp(0.4mm * 4, 8pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(5.656854f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_PT).getFloatValue(CSSPrimitiveValue.CSS_PT),
				1e-5);
	}

	@Test
	public void testClamp3() {
		style.setCssText("foo: clamp(0.6mm * 4, 8pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(6.80315f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_PT).getFloatValue(CSSPrimitiveValue.CSS_PT),
				1e-5);
	}

	@Test
	public void testClamp4() {
		style.setCssText("foo: clamp(0.6mm * 4, 12pt * sin(45deg), 20px/2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(7.5f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_PT).getFloatValue(CSSPrimitiveValue.CSS_PT),
				1e-5);
	}

	@Test
	public void testSin() {
		style.setCssText("foo: sin(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.1045285f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testCos() {
		style.setCssText("foo: cos(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.994522f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testTan() {
		style.setCssText("foo: tan(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(0.105104f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testASin() {
		style.setCssText("foo: asin(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(23.578178f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_DEG).getFloatValue(CSSPrimitiveValue.CSS_DEG),
				1e-5);
	}

	@Test
	public void testACos() {
		style.setCssText("foo: acos(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(66.4218216f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_DEG).getFloatValue(CSSPrimitiveValue.CSS_DEG),
				1e-5);
	}

	@Test
	public void testATan() {
		style.setCssText("foo: atan(0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(21.80141f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_DEG).getFloatValue(CSSPrimitiveValue.CSS_DEG),
				1e-5);
	}

	@Test
	public void testAtan2_1() {
		style.setCssText("foo: atan2(-1.5, 0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(-75.06858f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_DEG).getFloatValue(CSSPrimitiveValue.CSS_DEG),
				1e-5);
	}

	@Test
	public void testAtan2_2() {
		style.setCssText("foo: atan2(0.2 * 2, -1.5)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(165.068588f,
				evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_DEG).getFloatValue(CSSPrimitiveValue.CSS_DEG),
				1e-5);
	}

	@Test
	public void testPow() {
		style.setCssText("foo: pow(1.2 * 3, 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(46.656f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

	@Test
	public void testSqrt() {
		style.setCssText("foo: sqrt(1.2 * 3)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(1.897367f, evaluator.evaluateFunction(val, CSSPrimitiveValue.CSS_NUMBER)
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
	}

}
