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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

public class EasingFunctionValueTest {

	private static SyntaxParser syntaxParser;

	CSSStyleDeclarationRule styleRule;
	BaseCSSStyleDeclaration style;

	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		syntaxParser = new SyntaxParser();
	}

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
		style.setCssText("transition-timing-function: cubic-bezier(0.42, 0, 1, 1);");
		FunctionValue value = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertTrue(value.equals(value));
		style.setCssText("transition-timing-function: cubic-bezier(0.42, 0, 1, 1); ");
		FunctionValue value2 = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("transition-timing-function: cubic-bezier(0.43, 0, 1, 1);");
		value2 = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testCubicBezier() {
		style.setCssText("transition-timing-function: cubic-bezier(0.42, 0, 1, 1);");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertNotNull(val);
		assertEquals(CSSValue.Type.CUBIC_BEZIER, val.getPrimitiveType());
		assertEquals("cubic-bezier(0.42, 0, 1, 1)", style.getPropertyValue("transition-timing-function"));
		assertEquals("transition-timing-function: cubic-bezier(0.42, 0, 1, 1); ", style.getCssText());
		assertEquals(4, val.getArguments().size());
		assertEquals("cubic-bezier(0.42, 0, 1, 1)", val.getCssText());
		assertEquals("cubic-bezier(.42,0,1,1)", val.getMinifiedCssText("transition-timing-function"));

		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, 0.27f);
		val.setComponent(0, number);
		assertSame(val.getArguments().get(0), val.getComponent(0));
		val.setComponent(100, number);
		assertNull(val.getComponent(100));
		try {
			val.setComponent(0, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}

		assertMatch(Match.TRUE, val, "<easing-function>");
		assertMatch(Match.TRUE, val, "<easing-function>#");
		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.FALSE, val, "<number>#");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testBezierNegativeArg() {
		style.setCssText("foo: cubic-bezier(-.42, -.3, -1, -.01);");
		assertNull(style.getPropertyCSSValue("foo"));
	}

	@Test
	public void testBezier3Arg() {
		style.setCssText("foo: cubic-bezier(.1, .4, 1);");
		assertNull(style.getPropertyCSSValue("foo"));
	}

	@Test
	public void testBezierLengthXArg() {
		style.setCssText("foo: cubic-bezier(0, 0.5, 2px, 1);");
		assertNull(style.getPropertyCSSValue("foo"));
	}

	@Test
	public void testBezierCalcLengthXArg() {
		style.setCssText("foo: cubic-bezier(0, 0.5, calc(2px*2), 1);");
		assertNull(style.getPropertyCSSValue("foo"));
	}

	@Test
	public void testBezierLengthYArg() {
		style.setCssText("foo: cubic-bezier(0, 0.5, 1, 1px);");
		assertNull(style.getPropertyCSSValue("foo"));
	}

	@Test
	public void testBezierCalcXArgument() {
		style.setCssText("transition-timing-function: cubic-bezier(calc(.1*2), .1*2, 2/1, 1);");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertNotNull(val);
		assertEquals(CSSValue.Type.CUBIC_BEZIER, val.getPrimitiveType());
		assertEquals(4, val.getArguments().size());
		StyleValue arg = val.getArguments().get(1);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("0.1*2", calc.getExpression().getCssText());
		assertEquals("0.1*2", calc.getCssText());
		assertEquals("cubic-bezier(calc(0.1*2), 0.1*2, 2/1, 1)", val.getCssText());
		assertEquals("cubic-bezier(calc(.1*2),.1*2,2/1,1)", val.getMinifiedCssText());
		assertTrue(val.equals(val.clone()));

		assertMatch(Match.TRUE, val, "<easing-function>");
		assertMatch(Match.TRUE, val, "<easing-function>#");
		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testBezierCalcYArgument() {
		style.setCssText("transition-timing-function: cubic-bezier(0.3, .1*2, 1, calc(.5 + .5));");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertNotNull(val);
		assertEquals(CSSValue.Type.CUBIC_BEZIER, val.getPrimitiveType());
		assertEquals(4, val.getArguments().size());
		StyleValue arg = val.getArguments().get(1);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("0.1*2", calc.getExpression().getCssText());
		assertEquals("0.1*2", calc.getCssText());
		assertEquals("cubic-bezier(0.3, 0.1*2, 1, calc(0.5 + 0.5))", val.getCssText());
		assertEquals("cubic-bezier(.3,.1*2,1,calc(.5 + .5))", val.getMinifiedCssText());
		assertTrue(val.equals(val.clone()));

		assertMatch(Match.TRUE, val, "<easing-function>");
		assertMatch(Match.TRUE, val, "<easing-function>#");
		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testSteps() {
		style.setCssText("animation-timing-function:steps(6, start)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("animation-timing-function");
		assertNotNull(val);
		assertEquals(CSSValue.Type.STEPS, val.getPrimitiveType());
		assertEquals("steps", val.getStringValue());
		assertEquals("steps", val.getFunctionName());
		assertEquals("steps(6, start)", style.getPropertyValue("animation-timing-function"));
		assertEquals(2, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.NUMERIC, arg.getPrimitiveType());
		assertEquals(6f, ((CSSTypedValue) arg).getFloatValue(CSSUnit.CSS_NUMBER), 1e-6f);

		assertMatch(Match.TRUE, val, "<easing-function>");
		assertMatch(Match.TRUE, val, "<easing-function>#");
		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testStepsCalc() {
		style.setCssText("animation-timing-function:steps(calc(2*3), start)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("animation-timing-function");
		assertNotNull(val);
		assertEquals(CSSValue.Type.STEPS, val.getPrimitiveType());
		assertEquals("steps", val.getStringValue());
		assertEquals("steps", val.getFunctionName());
		assertEquals("steps(calc(2*3), start)", style.getPropertyValue("animation-timing-function"));
		assertEquals(2, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("calc(2*3)", calc.getCssText());
		assertEquals("2*3", calc.getExpression().getCssText());

		assertMatch(Match.TRUE, val, "<easing-function>");
		assertMatch(Match.TRUE, val, "<easing-function>#");
		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("transition-timing-function: cubic-bezier(0.42, 0, 1, 1); ");
		FunctionValue value = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		FunctionValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		LinkedCSSValueList args = value.getArguments();
		LinkedCSSValueList clonargs = clon.getArguments();
		assertEquals(args.size(), clonargs.size());
		assertEquals(args, clonargs);
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
