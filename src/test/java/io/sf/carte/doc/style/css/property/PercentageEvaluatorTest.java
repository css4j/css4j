/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class PercentageEvaluatorTest {

	private static SyntaxParser syntaxParser;

	private static AbstractCSSStyleSheet sheet;
	private BaseCSSStyleDeclaration style;
	private PercentageEvaluator evaluator;

	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	void setUp() throws Exception {
		StyleRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
		evaluator = new PercentageEvaluator();
	}

	@Test
	public void testCalcPcnt() {
		style.setCssText("foo: calc(18.1% * 2)");
		ExpressionValue val = (ExpressionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		Unit unit = new Unit();
		assertEquals(36.2f, evaluator.evaluateExpression(val.getExpression(), unit)
				.getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals(1, unit.getExponent());
		assertEquals(CSSUnit.CSS_PERCENTAGE, unit.getUnitType());
	}

	@Test
	public void testAbsPcnt() {
		style.setCssText("foo: abs(1.2%)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <percentage>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "*");

		assertTrue(1.2f == evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PERCENTAGE));
	}

	@Test
	public void testMaxPcnt() {
		style.setCssText("foo: max(1.2 * 3%, 2%)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <percentage>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "*");

		assertEquals(3.6f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
	}

	@Test
	public void testMaxPlainPcnt() {
		style.setCssText("foo: max(1.2%, 3%)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_PERCENTAGE, val.computeUnitType());

		assertMatch(Match.TRUE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "<custom-ident> | <percentage>");
		assertMatch(Match.FALSE, val, "<number>");
		assertMatch(Match.TRUE, val, "<length-percentage>");
		assertMatch(Match.TRUE, val, "*");

		assertEquals(3f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
	}

	@Test
	public void testSignPcnt() {
		style.setCssText("foo: sign(18%)");
		CSSMathFunctionValue val = (CSSMathFunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSUnit.CSS_NUMBER, val.computeUnitType());

		assertEquals(1f, evaluator.evaluateFunction(val).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
	}

	private void assertMatch(Match match, CSSValue value, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, value.matches(syn));
	}

}
