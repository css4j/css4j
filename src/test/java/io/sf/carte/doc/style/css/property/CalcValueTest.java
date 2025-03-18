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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class CalcValueTest {

	static AbstractCSSStyleSheet sheet;

	private static SyntaxParser syntaxParser;

	CSSStyleDeclarationRule parentStyleRule;
	AbstractCSSStyleDeclaration style;

	@BeforeAll
	public static void setUpBeforeAll() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	public void setUpBefore() {
		parentStyleRule = sheet.createStyleRule();
		style = parentStyleRule.getStyle();
	}

	private StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		return parentStyleRule.getStyleDeclarationErrorHandler();
	}

	@Test
	public void testEquals() {
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
		style.setCssText("width: calc(80% - 3em); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
		assertEquals("80% - 3em", calc.getExpression().toString());
		assertEquals("calc(80% - 3em)", val.getCssText());
		assertEquals("calc(80% - 3em)", val.getMinifiedCssText("width"));

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage> | <length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssText2() {
		style.setCssText("width: calc(100%/3 - 2*1em - 2*1px); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
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

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage> | <length>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<length-percentage>#");
		assertMatch(Match.TRUE, val, "<length-percentage>+");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssText3() {
		style.setCssText("font-size: calc(1em + (0.4vw + 0.25vh)/2); ");
		StyleValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
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

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<percentage> | <length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<length>#");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssText4() {
		style.setCssText("font-size: calc(1em - (0.4vw + 0.25vh)/2); ");
		StyleValue val = style.getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
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

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<percentage> | <length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssText5() {
		style.setCssText("left: calc(20px + 2vw + 8.1% - 2.1vw); ");
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
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
		style.setCssText("left: calc(20px + 2vw + (8.1% - 2.1vw)); ");
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
		CSSExpression expr = calc.getExpression();
		assertEquals("20px + 2vw + 8.1% - 2.1vw", expr.toString());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getCssText());
		assertEquals("calc(20px + 2vw + 8.1% - 2.1vw)", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssText7() {
		style.setCssText("left: calc(20px + 2vw - (8.1% - 2.1vw)); ");
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
		CSSExpression expr = calc.getExpression();
		assertEquals("20px + 2vw - (8.1% - 2.1vw)", expr.toString());
		assertEquals("calc(20px + 2vw - (8.1% - 2.1vw))", val.getCssText());
		assertEquals("calc(20px + 2vw - (8.1% - 2.1vw))", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssText8() {
		style.setCssText("left: calc(20px + 2 * (8.1% - 2.1vw)); ");
		StyleValue val = style.getPropertyCSSValue("left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
		CSSExpression expr = calc.getExpression();
		assertEquals("20px + 2*(8.1% - 2.1vw)", expr.toString());
		assertEquals("calc(20px + 2*(8.1% - 2.1vw))", val.getCssText());
		assertEquals("calc(20px + 2*(8.1% - 2.1vw))", val.getMinifiedCssText("left"));
		assertEquals(CSSExpression.AlgebraicPart.SUM, expr.getPartType());
	}

	@Test
	public void testSetCssText9() {
		style.setCssText("width: calc((75vw*9/16 - 100vh)/-2); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_PX, calc.computeUnitType());
		CSSExpression expr = calc.getExpression();
		assertEquals("(75vw*9/16 - 100vh)/-2", expr.toString());
		assertEquals("calc((75vw*9/16 - 100vh)/-2)", val.getCssText());
		assertEquals("calc((75vw*9/16 - 100vh)/-2)", val.getMinifiedCssText("width"));
		assertEquals(CSSExpression.AlgebraicPart.PRODUCT, expr.getPartType());
	}

	@Test
	public void testMatchNumber() {
		style.setCssText("z-index: calc((6*2 - 10)/2); ");
		StyleValue val = style.getPropertyCSSValue("z-index");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_NUMBER, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <integer>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <integer> | <length>");
		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<integer>+");
		assertMatch(Match.TRUE, val, "<number>+");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchNumberList() {
		style.setCssText("foo: calc((6*2 - 10)/2) calc(20 - 3)");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());

		assertMatch(Match.TRUE, val, "<integer>+");
		assertMatch(Match.TRUE, val, "<custom-ident> | <integer>+");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>+");
		assertMatch(Match.FALSE, val, "<number>#");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchNumberCommaList() {
		style.setCssText("foo: calc((6*2 - 10)/2),calc(20 - 3)");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());

		assertMatch(Match.TRUE, val, "<integer>#");
		assertMatch(Match.TRUE, val, "<custom-ident> | <integer>#");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>#");
		assertMatch(Match.FALSE, val, "<number>+");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchNumberLengthCancellation() {
		style.setCssText("z-index: calc((1vw - 1em)/(1cap - 1ex)); ");
		StyleValue val = style.getPropertyCSSValue("z-index");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_NUMBER, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.TRUE, val, "<integer>+");
		assertMatch(Match.TRUE, val, "<number>+");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchLength() {
		style.setCssText("margin-top: calc(1vh - 1em); ");
		StyleValue val = style.getPropertyCSSValue("margin-top");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PX, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <integer>");
		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<length>+");
		assertMatch(Match.TRUE, val, "<length>#");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchLengthList() {
		style.setCssText("foo: calc(1vh - 1em) calc(2.5vw - 1px); ");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());

		assertMatch(Match.TRUE, val, "<length>+");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>+");
		assertMatch(Match.FALSE, val, "<custom-ident> | <integer>+");
		assertMatch(Match.TRUE, val, "<length-percentage>+");
		assertMatch(Match.FALSE, val, "<length>#");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchLengthCommaList() {
		style.setCssText("foo: calc(1vh - 1em), calc(2.5vw - 1px); ");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());

		assertMatch(Match.TRUE, val, "<length>#");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>#");
		assertMatch(Match.FALSE, val, "<custom-ident> | <integer>#");
		assertMatch(Match.TRUE, val, "<length-percentage>#");
		assertMatch(Match.FALSE, val, "<length>+");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchLengthPercentage() {
		style.setCssText("margin-left: calc(90vw + 8% - 1em); ");
		StyleValue val = style.getPropertyCSSValue("margin-left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <integer>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<length-percentage>+");
		assertMatch(Match.TRUE, val, "<length-percentage>#");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchPercentage() {
		style.setCssText("margin-left: calc(2 * 8% - 3.2 * 1%); ");
		StyleValue val = style.getPropertyCSSValue("margin-left");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<custom-ident> | <integer>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage>+");
		assertMatch(Match.TRUE, val, "<percentage>#");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchPlainPercentage() {
		style.setCssText("foo: calc(4.2%)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchPercentageSumProduct() {
		style.setCssText("foo: calc(2% + (1% + 0.2%)*2)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchLengthMinusPercent() {
		style.setCssText("foo: calc(15.1em - 4.2%)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PX, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchAngle() {
		style.setCssText("azimuth: calc((75deg*2/1.6 - 100grad)/2); ");
		StyleValue val = style.getPropertyCSSValue("azimuth");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_DEG, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<angle> | <length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle> | <length>");
		assertMatch(Match.FALSE, val, "<length-percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchTime() {
		style.setCssText("pause-after: calc((6s*2 - 100ms*45)/2); ");
		StyleValue val = style.getPropertyCSSValue("pause-after");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_S, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.TRUE, val, "<time>");
		assertMatch(Match.TRUE, val, "<time> | <length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <time> | <length>");
		assertMatch(Match.FALSE, val, "<length-percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchResolution() {
		style.setCssText("resolution: calc((72dpi*2 - 100dpcm)/2); ");
		StyleValue val = style.getPropertyCSSValue("resolution");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_DPI, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.TRUE, val, "<resolution>");
		assertMatch(Match.TRUE, val, "<resolution> | <length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <resolution> | <length>");
		assertMatch(Match.FALSE, val, "<length-percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testMatchError() {
		style.setCssText("width: calc((75vw*9/16deg - 100vh)/-2); ");

		StyleValue val = style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((ExpressionValue) val).computeUnitType());

		assertMatch(Match.FALSE, val, "<length-percentage>");
		assertMatch(Match.FALSE, val, "<angle> | <length>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.FALSE, val, "*");
	}

	@Test
	public void testPiE() {
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
	public void testPi() {
		style.setCssText("border-top-width: calc(sin(2*pi/3)/2)");
		StyleValue val = style.getPropertyCSSValue("border-top-width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_NUMBER, calc.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testE() {
		style.setCssText("border-top-width: calc(pow(e, 0.345)/2)");
		StyleValue val = style.getPropertyCSSValue("border-top-width");
		assertNotNull(val);
		assertEquals(CssType.TYPED, val.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, val.getPrimitiveType());

		CalcValue calc = (CalcValue) val;
		assertEquals(CSSUnit.CSS_NUMBER, calc.computeUnitType());

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.FALSE, val, "<angle>");
		assertMatch(Match.TRUE, val, "*");
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
		style.setCssText("margin-left:calc(var(--bar,0.3rem))");
		StyleValue val = style.getPropertyCSSValue("margin-left");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(var(--bar, 0.3rem))", val.getCssText());
		assertEquals("calc(var(--bar,0.3rem))", val.getMinifiedCssText("margin-left"));

		assertMatch(Match.PENDING, val, "<length>");
		assertMatch(Match.PENDING, val, "<percentage> | <length>");
		assertMatch(Match.PENDING, val, "<percentage>");
		assertMatch(Match.PENDING, val, "<length-percentage>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextNegative() {
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
	public void testSetCssTextSubExpressionAttributeInt() {
		style.setCssText("counter-reset: calc(attr(start type(<integer>), 1) - 1);");
		StyleValue val = style.getPropertyCSSValue("counter-reset");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(start type(<integer>), 1) - 1)", val.getCssText());
		assertEquals("calc(attr(start type(<integer>),1) - 1)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeNumber() {
		style.setCssText("line-height: calc(attr(data-lh type(<number>), 1) + 1);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh type(<number>), 1) + 1)", val.getCssText());
		assertEquals("calc(attr(data-lh type(<number>),1) + 1)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<number>");
		assertMatch(Match.TRUE, val, "<integer>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeLength() {
		style.setCssText("line-height: calc(attr(data-lh type(<length>), 1em) + 1ex);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh type(<length>), 1em) + 1ex)", val.getCssText());
		assertEquals("calc(attr(data-lh type(<length>),1em) + 1ex)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length>");
		assertMatch(Match.FALSE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeLengthEx() {
		style.setCssText("line-height: calc(attr(data-lh ex, 1em) + 1ex);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh ex, 1em) + 1ex)", val.getCssText());
		assertEquals("calc(attr(data-lh ex,1em) + 1ex)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<length>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeLengthPercentage() {
		style.setCssText("line-height: calc(attr(data-lh %, 1em) + 1ex);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh %, 1em) + 1ex)", val.getCssText());
		assertEquals("calc(attr(data-lh %,1em) + 1ex)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.PENDING, val, "<length>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeLengthPercentageMixPcnt() {
		style.setCssText("line-height: calc(attr(data-lh %, 1em) + 1.1%);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh %, 1em) + 1.1%)", val.getCssText());
		assertEquals("calc(attr(data-lh %,1em) + 1.1%)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeLengthPercentagePcntUnit() {
		style.setCssText("line-height: calc(attr(data-lh %, 1%) + 2%);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh %, 1%) + 2%)", val.getCssText());
		assertEquals("calc(attr(data-lh %,1%) + 2%)", val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeLengthPercentagePcnt() {
		style.setCssText("line-height: calc(attr(data-lh type(<percentage>), 1%) + 2%);");
		StyleValue val = style.getPropertyCSSValue("line-height");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-lh type(<percentage>), 1%) + 2%)", val.getCssText());
		assertEquals("calc(attr(data-lh type(<percentage>),1%) + 2%)",
				val.getMinifiedCssText("line-height"));

		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <length-percentage>");
		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.FALSE, val, "<length>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeAngle() {
		style.setCssText("azimuth: calc(attr(data-az type(<angle>), 1rad) + 25deg);");
		StyleValue val = style.getPropertyCSSValue("azimuth");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-az type(<angle>), 1rad) + 25deg)", val.getCssText());
		assertEquals("calc(attr(data-az type(<angle>),1rad) + 25deg)",
				val.getMinifiedCssText("azimuth"));

		assertMatch(Match.TRUE, val, "<angle>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <angle>");
		assertMatch(Match.FALSE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeTime() {
		style.setCssText("pause-after: calc(attr(data-pause type(<time>), 1s) + 2/1Hz);");
		StyleValue val = style.getPropertyCSSValue("pause-after");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-pause type(<time>), 1s) + 2/1hz)", val.getCssText());
		assertEquals("calc(attr(data-pause type(<time>),1s) + 2/1hz)",
				val.getMinifiedCssText("pause-after"));

		assertMatch(Match.TRUE, val, "<time>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <time>");
		assertMatch(Match.FALSE, val, "<integer>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeFreq() {
		style.setCssText("pitch: calc(attr(data-pitch type(<frequency>), 1hz) + 1/25ms);");
		StyleValue val = style.getPropertyCSSValue("pitch");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(attr(data-pitch type(<frequency>), 1hz) + 1/25ms)", val.getCssText());
		assertEquals("calc(attr(data-pitch type(<frequency>),1hz) + 1/25ms)",
				val.getMinifiedCssText("pitch"));

		assertMatch(Match.TRUE, val, "<frequency>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <frequency>");
		assertMatch(Match.FALSE, val, "<time>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionAttributeTimeFreq() {
		style.setCssText("pitch: calc(1/attr(data-pitch type(<time>), 1ms) + 1/.0025s);");
		StyleValue val = style.getPropertyCSSValue("pitch");
		assertNotNull(val);
		assertEquals(CssType.PROXY, val.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, val.getPrimitiveType());
		assertEquals("calc(1/attr(data-pitch type(<time>), 1ms) + 1/0.0025s)", val.getCssText());
		assertEquals("calc(1/attr(data-pitch type(<time>),1ms) + 1/0.0025s)",
				val.getMinifiedCssText("pitch"));

		assertMatch(Match.TRUE, val, "<frequency>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <frequency>");
		assertMatch(Match.FALSE, val, "<time>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.FALSE, val, "<color>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSetCssTextSubExpressionNegative() {
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
		style.setCssText("width: calc(*(-3em + 5%)); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError2() {
		style.setCssText("width: calc(/(-3em + 5%)); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError3() {
		style.setCssText("width: calc(3em 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError5() {
		style.setCssText("width: calc(*5%);");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError6() {
		style.setCssText("width: calc(3*);");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextError7() {
		style.setCssText("width: calc((3em - 0.5%) 6);");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextErrorConsecutiveOps() {
		style.setCssText("width: calc(3em * + 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextErrorConsecutiveOps2() {
		style.setCssText("width: calc(3em + * 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextErrorConsecutiveOps3() {
		style.setCssText("width: calc(3em - / 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextErrorConsecutiveOps4() {
		style.setCssText("width: calc(3em / - 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testSetCssTextErrorConsecutiveOps5() {
		style.setCssText("width: calc(3em * - 5%); ");
		StyleValue val = style.getPropertyCSSValue("width");
		assertNull(val);
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
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

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
