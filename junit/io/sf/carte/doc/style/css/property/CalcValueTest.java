/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;

public class CalcValueTest {

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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("100%/3 - 2*1em - 2*1px", expr.toString());
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		assertFalse(expr.isInverseOperation());
		AlgebraicExpression ae = (AlgebraicExpression) expr;
		assertEquals(3, ae.getLength());
		CSSExpression op0 = ae.item(0);
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, op0.getPartType());
		assertEquals("100%/3", op0.toString());
		assertFalse(op0.isInverseOperation());
		AlgebraicExpression op0erands = (AlgebraicExpression) op0;
		assertEquals(2, op0erands.getLength());
		CSSExpression op01 = op0erands.item(1);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op01.getPartType());
		assertTrue(op01.isInverseOperation());
		CSSExpression op1 = ae.item(1);
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
		StyleValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("1em + (0.4vw + 0.25vh)/2", expr.toString());
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		assertFalse(expr.isInverseOperation());
		AlgebraicExpression operands = (AlgebraicExpression) expr;
		assertEquals(2, operands.getLength());
		CSSExpression op0 = operands.item(0);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op0.getPartType());
		assertEquals("1em", op0.toString());
		assertFalse(op0.isInverseOperation());
		CSSExpression op1 = operands.item(1);
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, op1.getPartType());
		assertFalse(op1.isInverseOperation());
		AlgebraicExpression op1erands = (AlgebraicExpression) op1;
		assertEquals(2, op1erands.getLength());
		CSSExpression op10 = op1erands.item(0);
		assertEquals(CSSExpression.AlgebraicPart.SUM, op10.getPartType());
		assertEquals("0.4vw + 0.25vh", op10.toString());
		assertFalse(op10.isInverseOperation());
		CSSExpression op11 = op1erands.item(1);
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
		StyleValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("1em - (0.4vw + 0.25vh)/2", expr.toString());
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		assertFalse(expr.isInverseOperation());
		AlgebraicExpression operands = (AlgebraicExpression) expr;
		assertEquals(2, operands.getLength());
		CSSExpression op0 = operands.item(0);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op0.getPartType());
		assertEquals("1em", op0.toString());
		assertFalse(op0.isInverseOperation());
		CSSExpression op1 = operands.item(1);
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, op1.getPartType());
		assertTrue(op1.isInverseOperation());
		AlgebraicExpression op1erands = (AlgebraicExpression) op1;
		assertEquals(2, op1erands.getLength());
		CSSExpression op10 = op1erands.item(0);
		assertEquals(CSSExpression.AlgebraicPart.SUM, op10.getPartType());
		assertEquals("0.4vw + 0.25vh", op10.toString());
		assertFalse(op10.isInverseOperation());
		CSSExpression op11 = op1erands.item(1);
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
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("20px + 2vw + 8.1% - 2.1vw", expr.toString());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getCssText());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
		SumExpression sum = (SumExpression) expr;
		assertFalse(sum.inverseOperation);
		assertEquals(4, sum.getLength());
		CSSExpression op0 = sum.item(0);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op0.getPartType());
		assertFalse(op0.isInverseOperation());
		assertEquals("20px", op0.getCssText());
		CSSExpression op1 = sum.item(1);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op1.getPartType());
		assertFalse(op1.isInverseOperation());
		assertEquals("2vw", op1.getCssText());
		CSSExpression op2 = sum.item(2);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op2.getPartType());
		assertFalse(op2.isInverseOperation());
		assertEquals("8.1%", op2.getCssText());
		CSSExpression op3 = sum.item(3);
		assertEquals(CSSExpression.AlgebraicPart.OPERAND, op3.getPartType());
		assertTrue(op3.isInverseOperation());
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
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
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
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
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
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("20px + 2*(8.1% - 2.1vw)", expr.toString());
		assertEquals("calc(20px + 2*(8.1% - 2.1vw))", val.getCssText());
		assertEquals("calc(20px + 2*(8.1% - 2.1vw))", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssText9() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((75vw*9/16 - 100vh)/-2); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("(75vw*9/16 - 100vh)/-2", expr.toString());
		assertEquals("calc((75vw*9/16 - 100vh)/-2)", val.getCssText());
		assertEquals("calc((75vw*9/16 - 100vh)/-2)", val.getMinifiedCssText("width"));
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, expr.getPartType());
	}

	@Test
	public void testPiE() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("border-top-width: calc(sin(pi*1rad/3)/e*1px); ");
		StyleValue val = style.getPropertyCSSValue("border-top-width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals("sin(3.1415927*1rad/3)/2.7182817*1px", expr.toString());
		assertEquals("calc(sin(3.1415927*1rad/3)/2.7182817*1px)", val.getCssText());
		assertEquals("calc(sin(3.1415927*1rad/3)/2.7182817*1px)", val.getMinifiedCssText(""));
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, expr.getPartType());
		// Uppercase
		style.setCssText("border-top-width: calc(sin(PI*1rad/3)/E*1px); ");
		val = style.getPropertyCSSValue("border-top-width");
		calc = (CalcValue) val;
		expr = calc.getExpression();
		assertEquals("sin(3.1415927*1rad/3)/2.7182817*1px", expr.toString());
	}

	@Test
	public void testCreateCSSValueVar() throws CSSException, IOException {
		ValueFactory vf = new ValueFactory();
		StyleValue value = vf.parseProperty("calc(3*calc(2*var(--foo, 3px)))");
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, value.getPrimitiveType());
		assertEquals("calc(3*calc(2*var(--foo, 3px)))", value.getCssText());
		assertEquals("calc(3*calc(2*var(--foo,3px)))", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextVar2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("margin-left:calc(var(--bar,0.3rem))");
		StyleValue val = style.getPropertyCSSValue("margin-left");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(var(--bar, 0.3rem))", val.getCssText());
		assertEquals("calc(var(--bar,0.3rem))", val.getMinifiedCssText("margin-left"));
	}

	@Test
	public void testSetCssTextNegative() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(-3em); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("-3em", calc.getExpression().toString());
		assertEquals("calc(-3em)", val.getCssText());
		assertEquals("calc(-3em)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextNegative2() {
		NumberValue number1 = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 4);
		OperandExpression op1 = OperandExpression.createOperand(number1);
		NumberValue number2 = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, -1);
		OperandExpression op2 = OperandExpression.createOperand(number2);
		AlgebraicExpression sum = SumExpression.createSumExpression();
		((StyleExpression) sum).addExpression(op1);
		((StyleExpression) sum).addExpression(op2);
		assertEquals("4 - 1", sum.getCssText());
		assertEquals("4 - 1", sum.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextSubExpression() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((50% - 3em)*2); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("(1.5 - 1.3)*(100vw - 21em) - max(6px, 1em)", calc.getExpression().toString());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em) - max(6px, 1em))", val.getCssText());
		assertEquals("calc((1.5 - 1.3)*(100vw - 21em) - max(6px,1em))", val.getMinifiedCssText("line-height"));
	}

	@Test
	public void testSetCssTextSubExpressionAttribute() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("counter-reset: calc(attr(start integer, 1) - 1);");
		StyleValue val = style.getPropertyCSSValue("counter-reset");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		assertEquals("attr(start integer, 1) - 1", calc.getExpression().toString());
		assertEquals("calc(attr(start integer, 1) - 1)", val.getCssText());
		assertEquals("calc(attr(start integer,1) - 1)", val.getMinifiedCssText("line-height"));
	}

	@Test
	public void testSetCssTextSubExpressionNegative() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((-3em)*2); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
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
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		CalcValue calc = (CalcValue) val;
		CSSExpression expr = calc.getExpression();
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, expr.getPartType());
		ProductExpression operands = (ProductExpression) expr;
		assertEquals(2, operands.getLength());
		assertEquals(CSSExpression.AlgebraicPart.SUM, operands.item(0).getPartType());
		assertEquals("(-3em + 5%)*2", expr.toString());
		assertEquals("calc((-3em + 5%)*2)", val.getCssText());
		assertEquals("calc((-3em + 5%)*2)", val.getMinifiedCssText("width"));
	}

	@Test
	public void testSetCssTextError() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(*(-3em + 5%)); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError2() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(/(-3em + 5%)); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError3() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(3em 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError4() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(3em * + 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError5() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc(*5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError6() {
		StyleRule styleRule = new StyleRule();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style.setCssText("width: calc((3em - 0.5%) 6); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(styleRule.getStyleDeclarationErrorHandler().hasErrors());
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
		assertEquals(sum.getLength(), clonesum.getLength());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
