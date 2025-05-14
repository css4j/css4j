/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

/**
 * Checks IE compatible functions
 */
public class IEFunctionValueTest {

	StyleRule styleRule;
	BaseCSSStyleDeclaration style;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(
				EnumSet.of(Parser.Flag.IEVALUES));
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testSubExpression() {
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
		List<StyleValue> args = val.getArguments();
		assertEquals(1, args.size());
		CSSValue cssval = args.get(0);
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(CSSValue.Type.FUNCTION, cssval.getPrimitiveType());
		FunctionValue eval = (FunctionValue) cssval;
		args = eval.getArguments();
		assertEquals(1, args.size());
		cssval = args.get(0);
		assertEquals(CssType.LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(3, list.getLength());
		cssval = list.item(0);
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals("document\\.documentElement\\.scrollTop", cssval.getCssText());
		cssval = list.item(2);
		assertEquals(CssType.TYPED, cssval.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, cssval.getPrimitiveType());
		assertEquals("(document\\.documentElement\\.clientHeight-this\\.offsetHeight)",
				cssval.getCssText());
		assertTrue(val.equals(val.clone()));
	}

}
