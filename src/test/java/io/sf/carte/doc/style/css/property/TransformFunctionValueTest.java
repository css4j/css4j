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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSTransformFunction;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.TransformFunctions;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class TransformFunctionValueTest {

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
		style.setCssText("transform: translateX(0%);");
		FunctionValue value = (FunctionValue) style.getPropertyCSSValue("transform");
		assertTrue(value.equals(value));
		style.setCssText("transform: translateX(0%)");
		FunctionValue value2 = (FunctionValue) style.getPropertyCSSValue("transform");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("transform: translateX(0.1%)");
		value2 = (FunctionValue) style.getPropertyCSSValue("transform");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());

		assertMatch(Match.TRUE, value, "<transform-function>");
		assertMatch(Match.TRUE, value, "<transform-function>#");
		assertMatch(Match.TRUE, value, "<transform-list>");
		assertMatch(Match.FALSE, value, "<easing-function>");
		assertMatch(Match.FALSE, value, "<percentage>");
		assertMatch(Match.TRUE, value, "*");
	}

	@Test
	public void testMatrix() {
		style.setCssText("transform: matrix(1, 2, -1, 1, 50, 50);");
		CSSTransformFunction val = (CSSTransformFunction) style.getPropertyCSSValue("transform");
		assertNotNull(val);
		assertEquals(CSSValue.Type.TRANSFORM_FUNCTION, val.getPrimitiveType());
		assertEquals(TransformFunctions.MATRIX, val.getFunction());
		assertEquals(6, val.getArguments().getLength());
		assertEquals("matrix(1, 2, -1, 1, 50, 50)", val.getCssText());
		assertEquals("matrix(1,2,-1,1,50,50)", val.getMinifiedCssText(""));
		assertTrue(val.equals(val.clone()));

		assertMatch(Match.TRUE, val, "<transform-function>");
		assertMatch(Match.TRUE, val, "<transform-function>#");
		assertMatch(Match.TRUE, val, "<transform-list>");
		assertMatch(Match.FALSE, val, "<easing-function>");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testCalcArgument() {
		style.setCssText("transform: translateY(calc(3% - 1.2 * 5px));");
		CSSTransformFunction val = (CSSTransformFunction) style.getPropertyCSSValue("transform");
		assertNotNull(val);
		assertEquals(CSSValue.Type.TRANSFORM_FUNCTION, val.getPrimitiveType());
		assertEquals(TransformFunctions.TRANSLATE_Y, val.getFunction());
		assertEquals("translateY(calc(3% - 1.2*5px))", style.getPropertyValue("transform"));
		assertEquals("transform: translateY(calc(3% - 1.2*5px)); ", style.getCssText());
		assertEquals(1, val.getArguments().getLength());
		CSSValue arg = val.getArguments().item(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("3% - 1.2*5px", calc.getExpression().getCssText());
		assertEquals("calc(3% - 1.2*5px)", calc.getCssText());
		assertEquals("translateY(calc(3% - 1.2*5px))", val.getCssText());
		assertEquals("translateY(calc(3% - 1.2*5px))", val.getMinifiedCssText(""));
		assertTrue(val.equals(val.clone()));

		assertMatch(Match.TRUE, val, "<transform-function>");
		assertMatch(Match.TRUE, val, "<transform-function>#");
		assertMatch(Match.TRUE, val, "<transform-list>");
		assertMatch(Match.FALSE, val, "<easing-function>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("transform: translateZ(42px);");
		FunctionValue value = (FunctionValue) style.getPropertyCSSValue("transform");
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
