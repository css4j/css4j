/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class CalcValueTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	@Test
	public void testEquals() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		style.setCssText("left: calc(20px + 2vw + 8.1% - 2.1vw); ");
		CalcValue value = (CalcValue) style.getPropertyCSSValue("left");
		assertTrue(value.equals(value));
		style.setCssText("left: calc(20px + 2vw + 8.1% - 2.1vw); ");
		CalcValue value2 = (CalcValue) style.getPropertyCSSValue("left");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("left: calc(20px + 2vw + 8% - 2.1vw);");
		value2 = (CalcValue) style.getPropertyCSSValue("left");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testSetCssText() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(80% - 3em); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("80% - 3em", calc.getExpression().toString());
		assertEquals("calc(80% - 3em)", val.getCssText());
		assertEquals("calc(80% - 3em)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssText2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(100%/3 - 2*1em - 2*1px); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("100%/3 - 2*1em - 2*1px", expr.toString());
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		assertFalse(expr.isInverseOperation());
		List<? extends CSSExpression> operands = ((CSSExpression.AlgebraicExpression) expr).getOperands();
		assertEquals(3, operands.size());
		CSSExpression op0 = operands.get(0);
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, op0.getPartType());
		assertEquals("100%/3", op0.toString());
		assertFalse(op0.isInverseOperation());
		List<? extends CSSExpression> op0erands = ((CSSExpression.AlgebraicExpression) op0).getOperands();
		assertEquals(2, op0erands.size());
		CSSExpression op01 = op0erands.get(1);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op01.getPartType());
		assertTrue(op01.isInverseOperation());
		CSSExpression op1 = operands.get(1);
		assertTrue(op1.isInverseOperation());
		assertEquals("2*1em", op1.toString());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", val.getCssText());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssText3() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("font-size: calc(1em + (0.4vw + 0.25vh)/2); ");
		AbstractCSSValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("1em + (0.4vw + 0.25vh)/2", expr.toString());
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		assertFalse(expr.isInverseOperation());
		List<? extends CSSExpression> operands = ((CSSExpression.AlgebraicExpression) expr).getOperands();
		assertEquals(2, operands.size());
		CSSExpression op0 = operands.get(0);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op0.getPartType());
		assertEquals("1em", op0.toString());
		assertFalse(op0.isInverseOperation());
		CSSExpression op1 = operands.get(1);
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, op1.getPartType());
		assertFalse(op1.isInverseOperation());
		List<? extends CSSExpression> op1erands = ((CSSExpression.AlgebraicExpression) op1).getOperands();
		assertEquals(2, op1erands.size());
		CSSExpression op10 = op1erands.get(0);
		assertEquals(CSSExpression.AlgebraicPart.SUM, op10.getPartType());
		assertEquals("0.4vw + 0.25vh", op10.toString());
		assertFalse(op10.isInverseOperation());
		CSSExpression op11 = op1erands.get(1);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op11.getPartType());
		assertTrue(op11.isInverseOperation());
		assertEquals("2", op11.toString());
		assertEquals("calc(1em + (0.4vw + 0.25vh)/2)", val.getCssText());
		assertEquals("calc(1em + (.4vw + .25vh)/2)", val.getMinifiedCssText("font-size"));
	}

	@Test
	public void testSetCssText4() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("font-size: calc(1em - (0.4vw + 0.25vh)/2); ");
		AbstractCSSValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("1em - (0.4vw + 0.25vh)/2", expr.toString());
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		assertFalse(expr.isInverseOperation());
		List<? extends CSSExpression> operands = ((CSSExpression.AlgebraicExpression) expr).getOperands();
		assertEquals(2, operands.size());
		CSSExpression op0 = operands.get(0);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op0.getPartType());
		assertEquals("1em", op0.toString());
		assertFalse(op0.isInverseOperation());
		CSSExpression op1 = operands.get(1);
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, op1.getPartType());
		assertTrue(op1.isInverseOperation());
		List<? extends CSSExpression> op1erands = ((CSSExpression.AlgebraicExpression) op1).getOperands();
		assertEquals(2, op1erands.size());
		CSSExpression op10 = op1erands.get(0);
		assertEquals(CSSExpression.AlgebraicPart.SUM, op10.getPartType());
		assertEquals("0.4vw + 0.25vh", op10.toString());
		assertFalse(op10.isInverseOperation());
		CSSExpression op11 = op1erands.get(1);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op11.getPartType());
		assertTrue(op11.isInverseOperation());
		assertEquals("2", op11.toString());
		assertEquals("calc(1em - (0.4vw + 0.25vh)/2)", val.getCssText());
		assertEquals("calc(1em - (.4vw + .25vh)/2)", val.getMinifiedCssText("font-size"));
	}

	@Test
	public void testSetCssText5() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("left: calc(20px + 2vw + 8.1% - 2.1vw); ");
		AbstractCSSValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("20px + 2vw + 8.1% - 2.1vw", expr.toString());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getCssText());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		SumExpression sum = (SumExpression) expr;
		assertFalse(sum.inverseOperation);
		assertEquals(4, sum.operands.size());
		AbstractCSSExpression op0 = sum.operands.get(0);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op0.getPartType());
		assertFalse(op0.inverseOperation);
		assertEquals("20px", op0.getCssText());
		AbstractCSSExpression op1 = sum.operands.get(1);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op1.getPartType());
		assertFalse(op1.inverseOperation);
		assertEquals("2vw", op1.getCssText());
		AbstractCSSExpression op2 = sum.operands.get(2);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op2.getPartType());
		assertFalse(op2.inverseOperation);
		assertEquals("8.1%", op2.getCssText());
		AbstractCSSExpression op3 = sum.operands.get(3);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op3.getPartType());
		assertTrue(op3.inverseOperation);
		assertEquals("2.1vw", op3.getCssText());
		assertTrue(expr.equals(expr.clone()));
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testSetCssText6() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("left: calc(20px + 2vw + (8.1% - 2.1vw)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("20px + 2vw + 8.1% - 2.1vw", expr.toString());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getCssText());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssText7() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("left: calc(20px + 2vw - (8.1% - 2.1vw)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("20px + 2vw - (8.1% - 2.1vw)", expr.toString());
		assertEquals("calc(20px + 2vw - (8.1% - 2.1vw))", val.getCssText());
		assertEquals("calc(20px + 2vw - (8.1% - 2.1vw))", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssText8() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("left: calc(20px + 2 * (8.1% - 2.1vw)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals("20px + 2*(8.1% - 2.1vw)", expr.toString());
		assertEquals("calc(20px + 2*(8.1% - 2.1vw))", val.getCssText());
		assertEquals("calc(20px + 2*(8.1% - 2.1vw))", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssTextNegative() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(-3em); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("-3em", calc.getExpression().toString());
		assertEquals("calc(-3em)", val.getCssText());
		assertEquals("calc(-3em)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubExpression() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((50% - 3em)*2); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(50% - 3em)*2", calc.getExpression().toString());
		assertEquals("calc((50% - 3em)*2)", val.getCssText());
		assertEquals("calc((50% - 3em)*2)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubExpression2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((100.0% - 60.0px) / 3); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(100% - 60px)/3", calc.getExpression().toString());
		assertEquals("calc((100% - 60px)/3)", val.getCssText());
		assertEquals("calc((100% - 60px)/3)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubCalcExpression() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(calc(50% - 3em)*2); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(50% - 3em)*2", calc.getExpression().toString());
		assertEquals("calc((50% - 3em)*2)", val.getCssText());
		assertEquals("calc((50% - 3em)*2)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubExpression3() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((50% - 3em) - (2% - 1px)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("50% - 3em - (2% - 1px)", calc.getExpression().toString());
		assertEquals("calc(50% - 3em - (2% - 1px))", val.getCssText());
		assertEquals("calc(50% - 3em - (2% - 1px))", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubExpressionFunction() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((50% - 3em) - max(6px, 1em)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("50% - 3em - max(6px, 1em)", calc.getExpression().toString());
		assertEquals("calc(50% - 3em - max(6px, 1em))", val.getCssText());
		assertEquals("calc(50% - 3em - max(6px,1em))", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubCalcExpression2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((50% - 3em) - calc(2% - 1px)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("50% - 3em - (2% - 1px)", calc.getExpression().toString());
		assertEquals("calc(50% - 3em - (2% - 1px))", val.getCssText());
		assertEquals("calc(50% - 3em - (2% - 1px))", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubCalcExpression3() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(calc(50% - 3em) - (2% - 1px)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("50% - 3em - (2% - 1px)", calc.getExpression().toString());
		assertEquals("calc(50% - 3em - (2% - 1px))", val.getCssText());
		assertEquals("calc(50% - 3em - (2% - 1px))", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubExpression4() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("line-height: calc((1.5 - 1.3)*(100vw - 21em)/(35 - 21)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(1.5 - 1.3)*(100vw - 21em)/(35 - 21)", calc.getExpression().toString());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em)/(35 - 21))", val.getCssText());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em)/(35 - 21))", val.getMinifiedCssText("line-height"));
	}

	@Test
	public void testSetCssTextSubExpression5() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("line-height: calc((1.5 - 1.3)*(100vw - 21em) - (35 - 21)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(1.5 - 1.3)*(100vw - 21em) - (35 - 21)", calc.getExpression().toString());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em) - (35 - 21))", val.getCssText());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em) - (35 - 21))", val.getMinifiedCssText("line-height"));
	}

	@Test
	public void testSetCssTextSubExpressionFunction4() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("line-height: calc((1.5 - 1.3)*(100vw - 21em) / max(6px, 1em)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(1.5 - 1.3)*(100vw - 21em)/max(6px, 1em)", calc.getExpression().toString());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em)/max(6px, 1em))", val.getCssText());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em)/max(6px,1em))", val.getMinifiedCssText("line-height"));
	}

	@Test
	public void testSetCssTextSubExpressionFunction5() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("line-height: calc((1.5 - 1.3)*(100vw - 21em) - max(6px, 1em)); ");
		AbstractCSSValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(1.5 - 1.3)*(100vw - 21em) - max(6px, 1em)", calc.getExpression().toString());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em) - max(6px, 1em))", val.getCssText());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em) - max(6px,1em))", val.getMinifiedCssText("line-height"));
	}

	@Test
	public void testSetCssTextSubExpressionNegative() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((-3em)*2); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("-3em*2", calc.getExpression().toString());
		assertEquals("calc(-3em*2)", val.getCssText());
		assertEquals("calc(-3em*2)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextSubExpressionNegative2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((-3em + 5%)*2); ");
		AbstractCSSValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, val.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) val).getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		AbstractCSSExpression expr = calc.getExpression();
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, expr.getPartType());
		List<AbstractCSSExpression> operands = ((ProductExpression) expr).getOperands();
		assertEquals(2, operands.size());
		assertEquals(CSSExpression.AlgebraicPart.SUM, operands.get(0).getPartType());
		assertEquals("(-3em + 5%)*2", expr.toString());
		assertEquals("calc((-3em + 5%)*2)", val.getCssText());
		assertEquals("calc((-3em + 5%)*2)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("width: calc(80% - 3em); ");
		CalcValue value = (CalcValue) style.getPropertyCSSValue("width");
		CalcValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		CSSExpression expr = value.getExpression();
		CSSExpression clonexpr = clon.getExpression();
		assertEquals(expr, clonexpr);
		assertEquals(expr.getPartType(), clonexpr.getPartType());
		SumExpression sum = (SumExpression) expr;
		SumExpression clonesum = (SumExpression) clonexpr;
		assertEquals(sum.getOperands().size(), clonesum.getOperands().size());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
