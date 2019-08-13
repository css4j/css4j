/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class FunctionValueTest {

	CSSStyleDeclarationRule styleRule;
	BaseCSSStyleDeclaration style;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		styleRule = sheet.createCSSStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("transition-timing-function: cubic-bezier(0.42, 0, 1, 1); ");
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
	public void testEquals2() {
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
	}

	@Test
	public void testGetCssText() {
		style.setCssText("transition-timing-function: cubic-bezier(0.42, 0, 1, 1); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("cubic-bezier(0.42, 0, 1, 1)", style.getPropertyValue("transition-timing-function"));
		assertEquals("transition-timing-function: cubic-bezier(0.42, 0, 1, 1); ", style.getCssText());
		assertEquals(4, val.getArguments().size());
		assertEquals("cubic-bezier(0.42, 0, 1, 1)", val.getCssText());
		assertEquals("cubic-bezier(.42,0,1,1)", val.getMinifiedCssText("transition-timing-function"));
	}

	@Test
	public void testGetCssTextNegativeArg() {
		style.setCssText("transition-timing-function: cubic-bezier(-.42, -.3, -1, -.01); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transition-timing-function");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("cubic-bezier(-0.42, -0.3, -1, -0.01)", style.getPropertyValue("transition-timing-function"));
		assertEquals("transition-timing-function: cubic-bezier(-0.42, -0.3, -1, -0.01); ", style.getCssText());
		assertEquals(4, val.getArguments().size());
		assertEquals("cubic-bezier(-0.42, -0.3, -1, -0.01)", val.getCssText());
		assertEquals("cubic-bezier(-.42,-.3,-1,-.01)", val.getMinifiedCssText("transition-timing-function"));
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("foo: bar(0.3, 0, calc(3% -  1.4em)); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("bar(0.3, 0, calc(3% - 1.4em))", style.getPropertyValue("foo"));
		assertEquals("foo: bar(0.3, 0, calc(3% - 1.4em)); ", style.getCssText());
		assertEquals(3, val.getArguments().size());
		assertEquals("calc(3% - 1.4em)", val.getArguments().get(2).getCssText());
		assertEquals("bar(0.3, 0, calc(3% - 1.4em))", val.getCssText());
		assertEquals("bar(.3,0,calc(3% - 1.4em))", val.getMinifiedCssText(""));
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testGetCssText3() {
		style.setCssText("property: foo(one two, three, four five six); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("property");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("foo(one two, three, four five six)", style.getPropertyValue("property"));
		assertEquals("property: foo(one two, three, four five six); ", style.getCssText());
		assertEquals("property:foo(one two,three,four five six)", style.getMinifiedCssText());
		assertEquals(3, val.getArguments().size());
		assertEquals("four five six", val.getArguments().get(2).getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testGetCssText4() {
		style.setCssText("property: foo(one, two, three, four five six); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("property");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("foo(one, two, three, four five six)", style.getPropertyValue("property"));
		assertEquals("property: foo(one, two, three, four five six); ", style.getCssText());
		assertEquals("property:foo(one,two,three,four five six)", style.getMinifiedCssText());
		assertEquals(4, val.getArguments().size());
		assertEquals("four five six", val.getArguments().get(3).getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testGetCssTextWS() {
		style.setCssText("property: foo(one two three); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("property");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("foo(one two three)", style.getPropertyValue("property"));
		assertEquals("property: foo(one two three); ", style.getCssText());
		assertEquals("property:foo(one two three)", style.getMinifiedCssText());
		assertEquals(1, val.getArguments().size());
		assertEquals("one two three", val.getArguments().get(0).getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testGetCssTextCustomCalc() {
		style.setCssText("width: -webkit-calc(100% - 24px*2); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("width");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("-webkit-calc(100% - 24px*2)", style.getPropertyValue("width"));
		assertEquals("width: -webkit-calc(100% - 24px*2); ", style.getCssText());
		assertEquals("width:-webkit-calc(100% - 24px*2)", style.getMinifiedCssText());
		assertEquals(1, val.getArguments().size());
		assertEquals("100% - 24px*2", val.getArguments().get(0).getCssText());
		assertEquals("-webkit-calc(100% - 24px*2)", val.getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testSubExpression() {
		style.setCssText("top: expression(iequirk \\= (document.body.scrollTop) + \"px\"); ");
		assertEquals("expression(iequirk \\= (document\\.body\\.scrollTop) + \"px\")", style.getPropertyValue("top"));
		assertEquals("top: expression(iequirk \\= (document\\.body\\.scrollTop) + \"px\"); ", style.getCssText());
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("top");
		assertNotNull(val);
		assertEquals("expression(iequirk \\= (document\\.body\\.scrollTop) + \"px\")", val.getCssText());
		assertEquals("expression(iequirk \\= (document\\.body\\.scrollTop) + \"px\")", val.getMinifiedCssText("top"));
		List<AbstractCSSValue> args = val.getArguments();
		assertEquals(1, args.size());
		CSSValue cssval = args.get(0);
		assertEquals(CSSValue.CSS_VALUE_LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(5, list.getLength());
		assertEquals("iequirk", list.item(0).getCssText());
		assertEquals("\\=", list.item(1).getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testSubExpression2() {
		style.setCssText(
				"top:expression(eval(document.documentElement.scrollTop+(document.documentElement.clientHeight-this.offsetHeight)))");
		assertEquals(
				"expression(eval(document\\.documentElement\\.scrollTop + (document\\.documentElement\\.clientHeight-this\\.offsetHeight)))",
				style.getPropertyValue("top"));
		assertEquals(
				"top: expression(eval(document\\.documentElement\\.scrollTop + (document\\.documentElement\\.clientHeight-this\\.offsetHeight))); ",
				style.getCssText());
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("top");
		assertNotNull(val);
		assertEquals(
				"expression(eval(document\\.documentElement\\.scrollTop + (document\\.documentElement\\.clientHeight-this\\.offsetHeight)))",
				val.getCssText());
		assertEquals(
				"expression(eval(document\\.documentElement\\.scrollTop + (document\\.documentElement\\.clientHeight-this\\.offsetHeight)))",
				val.getMinifiedCssText("top"));
		List<AbstractCSSValue> args = val.getArguments();
		assertEquals(1, args.size());
		CSSValue cssval = args.get(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, ((CSSPrimitiveValue2) cssval).getPrimitiveType());
		FunctionValue eval = (FunctionValue) cssval;
		args = eval.getArguments();
		assertEquals(1, args.size());
		cssval = args.get(0);
		assertEquals(CSSValue.CSS_VALUE_LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(3, list.getLength());
		cssval = list.item(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals("document\\.documentElement\\.scrollTop", cssval.getCssText());
		cssval = list.item(2);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue2) cssval).getPrimitiveType());
		assertEquals("(document\\.documentElement\\.clientHeight-this\\.offsetHeight)", cssval.getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testSubExpressionCustomCalc() {
		style.setCssText("top: -o-calc((55% - 25px)/2); ");
		assertEquals("-o-calc((55% - 25px)/2)", style.getPropertyValue("top"));
	}

	@Test
	public void testSubExpressionCustomCalc2() {
		style.setCssText("top: -o-calc(55%/2); ");
		assertEquals("-o-calc(55%/2)", style.getPropertyValue("top"));
	}

	@Test
	public void testGetCssTextCalcArgument() {
		style.setCssText("transform: translateY(calc(3% - 1.2 * 5px));");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transform");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("translateY(calc(3% - 1.2*5px))", style.getPropertyValue("transform"));
		assertEquals("transform: translateY(calc(3% - 1.2*5px)); ", style.getCssText());
		assertEquals(1, val.getArguments().size());
		AbstractCSSValue arg = val.getArguments().get(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, arg.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) arg).getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("3% - 1.2*5px", calc.getExpression().getCssText());
		assertEquals("calc(3% - 1.2*5px)", calc.getCssText());
		assertEquals("translateY(calc(3% - 1.2*5px))", val.getCssText());
		assertEquals("translateY(calc(3% - 1.2*5px))", val.getMinifiedCssText(""));
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testSin() {
		style.setCssText("transform: sin(1.2 * 5deg)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("transform");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("sin(1.2*5deg)", style.getPropertyValue("transform"));
		assertEquals(1, val.getArguments().size());
		AbstractCSSValue arg = val.getArguments().get(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, arg.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) arg).getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("1.2*5deg", calc.getExpression().getCssText());
	}

	@Test
	public void testAtan2_1() {
		style.setCssText("foo: atan2(-1.5, 0.2 * 2)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("atan2(-1.5, 0.2*2)", style.getPropertyValue("foo"));
		assertEquals(2, val.getArguments().size());
		AbstractCSSValue arg = val.getArguments().get(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, arg.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, ((CSSPrimitiveValue) arg).getPrimitiveType());
		assertEquals(-1.5f, ((CSSPrimitiveValue) arg).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 0.01f);
		arg = val.getArguments().get(1);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, arg.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) arg).getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("0.2*2", calc.getExpression().getCssText());
	}

	@Test
	public void testAtan2_2() {
		style.setCssText("foo: atan2(0.2 * 2, -1.5)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("atan2(0.2*2, -1.5)", style.getPropertyValue("foo"));
		assertEquals(2, val.getArguments().size());
		AbstractCSSValue arg = val.getArguments().get(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, arg.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_EXPRESSION, ((CSSPrimitiveValue) arg).getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("0.2*2", calc.getExpression().getCssText());
		arg = val.getArguments().get(1);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, arg.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, ((CSSPrimitiveValue) arg).getPrimitiveType());
		assertEquals(-1.5f, ((CSSPrimitiveValue) arg).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 0.01f);
	}

	@Test
	public void testGetCssTextBracketList() {
		style.setCssText("grid-template-columns: repeat(3, [line1 line2 line3] 200px); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("grid-template-columns");
		assertNotNull(cssval);
		FunctionValue val = (FunctionValue) cssval;
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, val.getPrimitiveType());
		assertEquals("repeat(3, [line1 line2 line3] 200px)", style.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template-columns: repeat(3, [line1 line2 line3] 200px); ", style.getCssText());
		assertEquals("grid-template-columns:repeat(3,[line1 line2 line3] 200px)", style.getMinifiedCssText());
		assertEquals(2, val.getArguments().size());
		assertEquals("[line1 line2 line3] 200px", val.getArguments().get(1).getCssText());
		assertTrue(val.equals(val.clone()));
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

}
