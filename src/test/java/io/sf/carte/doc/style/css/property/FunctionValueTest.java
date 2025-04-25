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

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.util.BufferSimpleWriter;

public class FunctionValueTest {

	private static SyntaxParser syntaxParser;

	StyleRule styleRule;
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
		style.setCssText("function: foo(0.42, 0, 1, 1); ");
		FunctionValue value = (FunctionValue) style.getPropertyCSSValue("function");
		assertTrue(value.equals(value));
		style.setCssText("function: foo(0.42, 0, 1, 1); ");
		FunctionValue value2 = (FunctionValue) style.getPropertyCSSValue("function");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("function: foo(0.43, 0, 1, 1);");
		value2 = (FunctionValue) style.getPropertyCSSValue("function");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssTextNegativeArg() {
		style.setCssText("foo: bar(-.42, -.3, -1, -.01); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
		assertEquals("bar(-0.42, -0.3, -1, -0.01)", style.getPropertyValue("foo"));
		assertEquals("foo: bar(-0.42, -0.3, -1, -0.01); ", style.getCssText());
		assertEquals(4, val.getArguments().size());
		assertEquals("bar(-0.42, -0.3, -1, -0.01)", val.getCssText());
		assertEquals("bar(-.42,-.3,-1,-.01)", val.getMinifiedCssText("foo"));

		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.FALSE, val, "<number>#");
		assertMatch(Match.FALSE, val, "<percentage>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("foo: bar(0.3, 0, calc(3% -  1.4em)); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
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
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
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
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
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
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
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
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
		assertEquals("-webkit-calc(100% - 24px*2)", style.getPropertyValue("width"));
		assertEquals("width: -webkit-calc(100% - 24px*2); ", style.getCssText());
		assertEquals("width:-webkit-calc(100% - 24px*2)", style.getMinifiedCssText());
		assertEquals(1, val.getArguments().size());
		assertEquals("100% - 24px*2", val.getArguments().get(0).getCssText());
		assertEquals("-webkit-calc(100% - 24px*2)", val.getCssText());
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testWriteCssText() throws IOException {
		style.setCssText("foo: bar(0.3, 0, calc(3% -  1.4em)); ");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		BufferSimpleWriter wri = new BufferSimpleWriter(32);
		val.writeCssText(wri);
		assertEquals("bar(0.3, 0, calc(3% - 1.4em))", wri.toString());
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
		List<StyleValue> args = val.getArguments();
		assertEquals(1, args.size());
		CSSValue cssval = args.get(0);
		assertEquals(CssType.LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(5, list.getLength());
		assertEquals("iequirk", list.item(0).getCssText());
		assertEquals("\\=", list.item(1).getCssText());
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
		style.setCssText("foo: bar(calc(3% - 1.2 * 5px));");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.TYPED, arg.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, arg.getPrimitiveType());
		ExpressionValue calc = (ExpressionValue) arg;
		assertEquals("3% - 1.2*5px", calc.getExpression().getCssText());
		assertEquals("calc(3% - 1.2*5px)", calc.getCssText());
		assertEquals("bar(calc(3% - 1.2*5px))", val.getCssText());
		assertEquals("bar(calc(3% - 1.2*5px))", val.getMinifiedCssText(""));
		assertTrue(val.equals(val.clone()));
	}

	@Test
	public void testSRC() {
		style.setCssText("background-image:SRC('https://www.example.com/foo.svg' format(svg))");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.SRC, val.getPrimitiveType());
		assertEquals("src", val.getStringValue());
		assertEquals("src", val.getFunctionName());
		assertEquals("src('https://www.example.com/foo.svg' format(svg))", val.getCssText());
		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.LIST, arg.getCssValueType());

		StyleValue url = ((ValueList) arg).item(0);
		assertEquals(CssType.TYPED, url.getCssValueType());
		assertEquals(CSSValue.Type.STRING, url.getPrimitiveType());
		assertEquals("https://www.example.com/foo.svg", ((CSSTypedValue) url).getStringValue());

		StyleValue modifier = ((ValueList) arg).item(1);
		assertEquals(CssType.TYPED, modifier.getCssValueType());
		assertEquals(CSSValue.Type.FUNCTION, modifier.getPrimitiveType());

		assertMatch(Match.TRUE, val, "<url>");
		assertMatch(Match.TRUE, val, "<url>#");
		assertMatch(Match.TRUE, val, "<image>");
		assertMatch(Match.FALSE, val, "<transform-function>");
		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testPrefixed() {
		style.setCssText("color:-prefixed-rgb(from olive +6% g b)");
		FunctionValue val = (FunctionValue) style.getPropertyCSSValue("color");
		assertNotNull(val);
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
		assertEquals("-prefixed-rgb", val.getStringValue());
		assertEquals("-prefixed-rgb", val.getFunctionName());
		assertEquals("-prefixed-rgb(from olive 6% g b)", style.getPropertyValue("color"));

		assertEquals(1, val.getArguments().size());
		StyleValue arg = val.getArguments().get(0);
		assertEquals(CssType.LIST, arg.getCssValueType());
		ValueList list = (ValueList) arg;
		assertEquals(5, list.getLength());
		StyleValue item = list.item(0);
		assertEquals(CssType.TYPED, item.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, item.getPrimitiveType());
		assertEquals("from", ((CSSTypedValue) item).getStringValue());
		item = list.item(1);
		assertEquals(CssType.TYPED, item.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, item.getPrimitiveType());
		assertEquals("olive", ((CSSTypedValue) item).getStringValue());
		item = list.item(2);
		assertEquals(CssType.TYPED, item.getCssValueType());
		assertEquals(CSSValue.Type.NUMERIC, item.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, ((CSSPrimitiveValue) item).getUnitType());
		assertEquals(6, ((CSSTypedValue) item).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-7);
		assertEquals("6%", item.getCssText());
		item = list.item(3);
		assertEquals(CssType.TYPED, item.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, item.getPrimitiveType());
		assertEquals("g", ((CSSTypedValue) item).getStringValue());
		item = list.item(4);
		assertEquals(CssType.TYPED, item.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, item.getPrimitiveType());
		assertEquals("b", ((CSSTypedValue) item).getStringValue());
	}

	@Test
	public void testGetCssTextBracketList() {
		style.setCssText("grid-template-columns: repeat(3, [line1 line2 line3] 200px); ");
		StyleValue cssval = style.getPropertyCSSValue("grid-template-columns");
		assertNotNull(cssval);
		FunctionValue val = (FunctionValue) cssval;
		assertEquals(CSSValue.Type.FUNCTION, val.getPrimitiveType());
		assertEquals("repeat(3, [line1 line2 line3] 200px)", style.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template-columns: repeat(3, [line1 line2 line3] 200px); ", style.getCssText());
		assertEquals("grid-template-columns:repeat(3,[line1 line2 line3] 200px)", style.getMinifiedCssText());
		assertEquals(2, val.getArguments().size());
		assertEquals("[line1 line2 line3] 200px", val.getArguments().get(1).getCssText());
		assertTrue(val.equals(val.clone()));

		assertMatch(Match.TRUE, val, "*");
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("foo: bar(0.42, 0, 1, 1); ");
		FunctionValue value = (FunctionValue) style.getPropertyCSSValue("foo");
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
