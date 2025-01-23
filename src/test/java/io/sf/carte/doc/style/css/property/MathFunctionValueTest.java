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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class MathFunctionValueTest {

	CSSStyleDeclarationRule styleRule;
	BaseCSSStyleDeclaration style;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("function: atan2(0.2 * 2, -1.5)");
		MathFunctionValue value = (MathFunctionValue) style.getPropertyCSSValue("function");
		assertTrue(value.equals(value));
		style.setCssText("function: atan2(0.2 * 2, -1.5)");
		MathFunctionValue value2 = (MathFunctionValue) style.getPropertyCSSValue("function");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());

		style.setCssText("function: atan2(0.2 * 2, 1.5)");
		value2 = (MathFunctionValue) style.getPropertyCSSValue("function");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testMatchFunctionList() {
		style.setCssText("foo: sin(25deg) cos(0.322)");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<integer>+");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <integer>+");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
	}

	@Test
	public void testMatchFunctionCommaList() {
		style.setCssText("foo: sin(25deg),cos(0.322)");
		StyleValue val = style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());

		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <integer>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
	}

	@Test
	public void testAbs() {
		style.setCssText("foo: abs(1.2 * -.5)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("abs", val.getStringValue());
		assertEquals("abs", val.getFunctionName());
		assertEquals(MathFunction.ABS, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals("abs(1.2*-0.5)", val.getCssText());
		assertEquals("abs(1.2*-.5)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*-0.5", calc.getExpression().getCssText());
		assertEquals("1.2*-.5", calc.getExpression().getMinifiedCssText());
	}

	@Test
	public void testAbsAngle() {
		style.setCssText("foo: abs(1.2 * -.5rad)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("abs", val.getStringValue());
		assertEquals("abs", val.getFunctionName());
		assertEquals(MathFunction.ABS, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertEquals("abs(1.2*-0.5rad)", val.getCssText());
		assertEquals("abs(1.2*-.5rad)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*-0.5rad", calc.getExpression().getCssText());
		assertEquals("1.2*-.5rad", calc.getExpression().getMinifiedCssText());
	}

	@Test
	public void testMax() {
		style.setCssText("foo: max(1.2 * -.5, .94)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("max", val.getStringValue());
		assertEquals("max", val.getFunctionName());
		assertEquals(MathFunction.MAX, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals("max(1.2*-0.5, 0.94)", val.getCssText());
		assertEquals("max(1.2*-.5,.94)", val.getMinifiedCssText(""));

		assertEquals(2, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*-0.5", calc.getExpression().getCssText());
		assertEquals("1.2*-.5", calc.getExpression().getMinifiedCssText());
	}

	@Test
	public void testSin() {
		style.setCssText("foo: sin(1.2 * 5deg)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("sin", val.getStringValue());
		assertEquals("sin", val.getFunctionName());
		assertEquals(MathFunction.SIN, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals("sin(1.2*5deg)", val.getCssText());
		assertEquals("sin(1.2*5deg)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*5deg", calc.getExpression().getCssText());
	}

	@Test
	public void testCos() {
		style.setCssText("foo: cos(1.2 * 5deg)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("cos", val.getStringValue());
		assertEquals("cos", val.getFunctionName());
		assertEquals(MathFunction.COS, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals("cos(1.2*5deg)", val.getCssText());
		assertEquals("cos(1.2*5deg)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*5deg", calc.getExpression().getCssText());
	}

	@Test
	public void testTan() {
		style.setCssText("foo: tan(1.2 * 5deg)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("tan", val.getStringValue());
		assertEquals("tan", val.getFunctionName());
		assertEquals(MathFunction.TAN, val.getFunction());
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals("tan(1.2*5deg)", val.getCssText());
		assertEquals("tan(1.2*5deg)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*5deg", calc.getExpression().getCssText());
	}

	@Test
	public void testASin() {
		style.setCssText("foo: asin(1.2 * .5)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("asin", val.getStringValue());
		assertEquals("asin", val.getFunctionName());
		assertEquals(MathFunction.ASIN, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertEquals("asin(1.2*0.5)", val.getCssText());
		assertEquals("asin(1.2*.5)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*0.5", calc.getExpression().getCssText());
		assertEquals("1.2*.5", calc.getExpression().getMinifiedCssText());
	}

	@Test
	public void testACos() {
		style.setCssText("foo: acos(1.2 * .5)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("acos", val.getStringValue());
		assertEquals("acos", val.getFunctionName());
		assertEquals(MathFunction.ACOS, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertEquals("acos(1.2*0.5)", val.getCssText());
		assertEquals("acos(1.2*.5)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*0.5", calc.getExpression().getCssText());
		assertEquals("1.2*.5", calc.getExpression().getMinifiedCssText());
	}

	@Test
	public void testATan() {
		style.setCssText("foo: atan(1.2 * .5)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("atan", val.getStringValue());
		assertEquals("atan", val.getFunctionName());
		assertEquals(MathFunction.ATAN, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertEquals("atan(1.2*0.5)", val.getCssText());
		assertEquals("atan(1.2*.5)", val.getMinifiedCssText(""));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*0.5", calc.getExpression().getCssText());
		assertEquals("1.2*.5", calc.getExpression().getMinifiedCssText());
	}

	@Test
	public void testAtan2_1() {
		style.setCssText("foo: atan2(-1.5, 0.2 * 2)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals(MathFunction.ATAN2, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertEquals("atan2(-1.5, 0.2*2)", val.getCssText());
		assertEquals("atan2(-1.5,.2*2)", val.getMinifiedCssText(""));

		assertEquals(2, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.NUMERIC, arg.getPrimitiveType());
		CSSTypedValue primi = (CSSTypedValue) arg;
		assertEquals(CSSUnit.CSS_NUMBER, primi.getUnitType());
		assertEquals(-1.5f, primi.getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
		arg = val.getArguments().get(1);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("0.2*2", calc.getExpression().getCssText());
	}

	@Test
	public void testAtan2_2() {
		style.setCssText("foo: atan2(0.2 * 2, -1.5)");
		MathFunctionValue val = (MathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.MATH_FUNCTION, val.getPrimitiveType());
		assertEquals("atan2", val.getStringValue());
		assertEquals("atan2", val.getFunctionName());
		assertEquals(MathFunction.ATAN2, val.getFunction());
		assertEquals(CSSUnit.CSS_RAD, val.computeUnitType());

		assertEquals("atan2(0.2*2, -1.5)", val.getCssText());
		assertEquals("atan2(.2*2,-1.5)", val.getMinifiedCssText(""));

		assertEquals(2, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("0.2*2", calc.getExpression().getCssText());
		arg = val.getArguments().get(1);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.NUMERIC, arg.getPrimitiveType());
		CSSTypedValue primi = (CSSTypedValue) arg;
		assertEquals(CSSUnit.CSS_NUMBER, primi.getUnitType());
		assertEquals(-1.5f, ((CSSTypedValue) arg).getFloatValue(CSSUnit.CSS_NUMBER), 0.01f);
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("foo: sin(1.2 * 5deg); ");
		MathFunctionValue value = (MathFunctionValue) style.getPropertyCSSValue("foo");
		MathFunctionValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getFunction(), clon.getFunction());

		LinkedCSSValueList args = value.getArguments();
		LinkedCSSValueList clonargs = clon.getArguments();
		assertEquals(args.size(), clonargs.size());
		assertEquals(args, clonargs);
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
