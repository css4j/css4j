/*

 Copyright (c) 2005-2024, Carlos Amengual.

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSGradientValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class GradientValueTest {

	BaseCSSStyleDeclaration style;

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testLinear() {
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to top right, red, white, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to top right, red, white, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(to top right,red,white,blue)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
		//
		style.setCssText("background-image: linear-gradient(yellow, blue 20%, #0f0); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(yellow, blue 20%, #0f0)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(yellow, blue 20%, #0f0); ", style.getCssText());
		assertEquals("background-image:linear-gradient(yellow,blue 20%,#0f0)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(135deg, yellow, blue); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(135deg, yellow, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(135deg, yellow, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(135deg,yellow,blue)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(to bottom, yellow 0%, blue 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to bottom, yellow 0%, blue 100%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to bottom, yellow 0%, blue 100%); ", style.getCssText());
		assertEquals("background-image:linear-gradient(to bottom,yellow 0%,blue 100%)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: linear-gradient(transparent, #fff); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(transparent, #fff)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(transparent, #fff); ", style.getCssText());
		assertEquals("background-image:linear-gradient(transparent,#fff)", style.getMinifiedCssText());
		val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearAngleFirst() {
		style.setCssText(
			"background-image: linear-gradient(180deg,transparent,99%,rgba(0,0,0,.5));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(180deg, transparent, 99%, rgba(0, 0, 0, 0.5))",
			style.getPropertyValue("background-image"));
		assertEquals(
			"background-image: linear-gradient(180deg, transparent, 99%, rgba(0, 0, 0, 0.5)); ",
			style.getCssText());
		assertEquals("background-image:linear-gradient(180deg,transparent,99%,rgba(0,0,0,.5))",
			style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearPcntFirst() {
		style.setCssText("background-image: linear-gradient(to top right, 0% red, 33% white, 66% blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to top right, red 0%, white 33%, blue 66%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to top right, red 0%, white 33%, blue 66%); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(to top right,red 0%,white 33%,blue 66%)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearHsla() {
		style.setCssText("background-image: linear-gradient(180deg,hsla(0,0%,100%,0) 0,#000 100%)");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(180deg, hsla(0, 0%, 100%, 0) 0, #000 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(180deg, hsla(0, 0%, 100%, 0) 0, #000 100%); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(180deg,hsla(0,0%,100%,0) 0,#000 100%)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
	}

	@Test
	public void testLinearHsla2() {
		style.setCssText("background-image: linear-gradient(180deg,hsla(0,0%,100%,0) 0,#000,100%)");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(180deg, hsla(0, 0%, 100%, 0) 0, #000, 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(180deg, hsla(0, 0%, 100%, 0) 0, #000, 100%); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(180deg,hsla(0,0%,100%,0) 0,#000,100%)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
	}

	@Test
	public void testLinearVar() {
		style.setCssText("background-image: linear-gradient(135deg,var(--foo,#d32c1e),var(--bar,#e13b4a));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(135deg, var(--foo, #d32c1e), var(--bar, #e13b4a))",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(135deg, var(--foo, #d32c1e), var(--bar, #e13b4a)); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(135deg,var(--foo,#d32c1e),var(--bar,#e13b4a))",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearVar2() {
		style.setCssText("background-image: linear-gradient(to right,var(--foo,#d32c1e),var(--bar,#e13b4a));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to right, var(--foo, #d32c1e), var(--bar, #e13b4a))",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to right, var(--foo, #d32c1e), var(--bar, #e13b4a)); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(to right,var(--foo,#d32c1e),var(--bar,#e13b4a))",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearVar3() {
		style.setCssText("background-image: linear-gradient(var(--foo,#d32c1e),var(--bar,#e13b4a));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(var(--foo, #d32c1e), var(--bar, #e13b4a))",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(var(--foo, #d32c1e), var(--bar, #e13b4a)); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(var(--foo,#d32c1e),var(--bar,#e13b4a))",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearFirstArgVarLexical() {
		style.setCssText("background-image: linear-gradient(var(--to-corner), red, white, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("linear-gradient(var(--to-corner), red, white, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(var(--to-corner), red, white, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(var(--to-corner),red,white,blue)", style.getMinifiedCssText());

		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearToVarLexical() {
		style.setCssText("background-image: linear-gradient(to var(--corner), red, white, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to var(--corner), red, white, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to var(--corner), red, white, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(to var(--corner),red,white,blue)", style.getMinifiedCssText());

		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearVarLexical() {
		style.setCssText(
				"background-image: linear-gradient(to bottom, var(--white) 0%, var(--grey) 66%, var(--black) 100%);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to bottom, var(--white) 0%, var(--grey) 66%, var(--black) 100%)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: linear-gradient(to bottom, var(--white) 0%, var(--grey) 66%, var(--black) 100%); ",
				style.getCssText());
		assertEquals("background-image:linear-gradient(to bottom,var(--white) 0%,var(--grey) 66%,var(--black) 100%)",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearVarLexical2() {
		style.setCssText(
				"--my-background:linear-gradient(90deg,transparent var(--start1),var(--gray) 0,var(--black) var(--stop1),transparent 0) no-repeat 0 100%/100% 100%");
		StyleValue cssval = style.getPropertyCSSValue("--my-background");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"linear-gradient(90deg, transparent var(--start1), var(--gray) 0, var(--black) var(--stop1), transparent 0) no-repeat 0 100%/100% 100%",
				cssval.getCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testLinearVarLexical3() {
		style.setCssText("background-image: linear-gradient(to right,#ccc 50%,var(--foo) 60%)");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("linear-gradient(to right, #ccc 50%, var(--foo) 60%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(to right, #ccc 50%, var(--foo) 60%); ", style.getCssText());
		assertEquals("background-image:linear-gradient(to right,#ccc 50%,var(--foo) 60%)", style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearVarPcntFirst() {
		style.setCssText(
				"background-image: linear-gradient(to top right, 0% var(--foo1,red), 33% var(--foo2,white), 66% var(--foo3,blue)); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals(
				"linear-gradient(to top right, var(--foo1, red) 0%, var(--foo2, white) 33%, var(--foo3, blue) 66%)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: linear-gradient(to top right, var(--foo1, red) 0%, var(--foo2, white) 33%, var(--foo3, blue) 66%); ",
				style.getCssText());
		assertEquals(
				"background-image:linear-gradient(to top right,var(--foo1,red) 0%,var(--foo2,white) 33%,var(--foo3,blue) 66%)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearAttr() {
		style.setCssText(
				"background-image: linear-gradient(attr(data-angle type(<angle>)),attr(data-foo type(<color>)),attr(data-bar type(<color>)));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"linear-gradient(attr(data-angle type(<angle>)), attr(data-foo type(<color>)), attr(data-bar type(<color>)))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: linear-gradient(attr(data-angle type(<angle>)), attr(data-foo type(<color>)), attr(data-bar type(<color>))); ",
				style.getCssText());
		assertEquals(
				"background-image:linear-gradient(attr(data-angle type(<angle>)),attr(data-foo type(<color>)),attr(data-bar type(<color>)))",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearAttrToLexical() {
		style.setCssText(
				"background-image: linear-gradient(to attr(data-corner1 type(<custom-ident>), top) attr(data-corner2 type(<custom-ident>),right),attr(data-foo type(<color>)),attr(data-bar type(<color>)));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"linear-gradient(to attr(data-corner1 type(<custom-ident>), top) attr(data-corner2 type(<custom-ident>), right), attr(data-foo type(<color>)), attr(data-bar type(<color>)))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image:linear-gradient(to attr(data-corner1 type(<custom-ident>),top) attr(data-corner2 type(<custom-ident>),right),attr(data-foo type(<color>)),attr(data-bar type(<color>)))",
				style.getMinifiedCssText());

		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearWarning() {
		style.setCssText("background-image: linear-gradient(top right, red, white, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("linear-gradient(top right, red, white, blue)", style.getPropertyValue("background-image"));
		assertEquals("background-image: linear-gradient(top right, red, white, blue); ", style.getCssText());
		assertEquals("background-image:linear-gradient(top right,red,white,blue)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBad() {
		style.setCssText("background-image: linear-gradient(45deg to left top, yellow 0%, blue 100%); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBad2() {
		style.setCssText("background-image: linear-gradient(to bottom, yellow 0% blue 100%); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBad3() {
		style.setCssText("background-image:linear-gradient(35deg,get-vertical-color(foo) 50%,transparent 0)");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadEmpty() {
		style.setCssText("background-image: linear-gradient();");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadTrailingComma() {
		style.setCssText("background-image: linear-gradient(to top right,);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadTrailingComma2() {
		style.setCssText("background-image: linear-gradient(to top right,red,);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadTrailingComma3() {
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue,);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadInitialComma() {
		style.setCssText("background-image: linear-gradient(,to top right, red, white, blue);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadTwoCommas() {
		style.setCssText("background-image: linear-gradient(to top right,, red, white, blue);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadTwoCommas2() {
		style.setCssText("background-image: linear-gradient(to top right, red, white,, blue);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadSlash() {
		style.setCssText("background-image: linear-gradient(to top right, red, white/, blue);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadSlash2() {
		style.setCssText("background-image: linear-gradient(to top right, red, white,/ blue);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadOneStop() {
		style.setCssText("background-image: linear-gradient(to top right, red);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadOneStop2() {
		style.setCssText("background-image: linear-gradient(red);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadVar() {
		style.setCssText("background-image: linear-gradient(to bottom, yellow var(--foo,20deg), blue 100%);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearBadVar2() {
		style.setCssText("background-image: linear-gradient(to bottom, 2% var(--foo,2px), blue 100%);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearNonStandard() {
		style.setCssText(
				"background-image: -moz-linear-gradient(center top, rgb(51, 153, 51) 0%, rgb(51, 119, 51) 100%);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.FUNCTION, cssval.getPrimitiveType());
		assertEquals("-moz-linear-gradient(center top, #393 0%, #373 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: -moz-linear-gradient(center top, #393 0%, #373 100%); ", style.getCssText());
		assertEquals("background-image:-moz-linear-gradient(center top,#393 0%,#373 100%)", style.getMinifiedCssText());
		CSSFunctionValue val = (CSSFunctionValue) cssval;
		assertEquals(3, val.getArguments().getLength());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
	}

	@Test
	public void testLinearNonStandard2() {
		style.setCssText(
				"background-image: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.FUNCTION, cssval.getPrimitiveType());
		assertEquals("-webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff)); ",
				style.getCssText());
		assertEquals("background-image:-webkit-gradient(linear,left top,left bottom,from(transparent),to(#fff))",
				style.getMinifiedCssText());
		FunctionValue val = (FunctionValue) cssval;
		assertEquals(5, val.getArguments().getLength());
		assertEquals("to(#fff)", val.getArguments().item(4).getCssText());
		assertTrue(val.equals(val.clone()));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearNonStandard3() {
		style.setCssText("background-image: -webkit-linear-gradient(transparent, #fff);");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals("-webkit-linear-gradient(transparent, #fff)", style.getPropertyValue("background-image"));
		assertEquals("background-image: -webkit-linear-gradient(transparent, #fff); ", style.getCssText());
		assertEquals("background-image:-webkit-linear-gradient(transparent,#fff)", style.getMinifiedCssText());
		assertEquals(2, val.getArguments().getLength());
		assertEquals("#fff", val.getArguments().item(1).getCssText());
		assertEquals(CSSGradientValue.GradientType.OTHER_GRADIENT, val.getGradientType());
		assertTrue(val.equals(val.clone()));
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testLinearNonStandardVar() {
		style.setCssText(
				"background-image: -webkit-gradient(linear,left top,right top,from(var(--foo,#d32c1e)),to(var(--bar,#e13b4a)));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"-webkit-gradient(linear, left top, right top, from(var(--foo, #d32c1e)), to(var(--bar, #e13b4a)))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: -webkit-gradient(linear, left top, right top, from(var(--foo, #d32c1e)), to(var(--bar, #e13b4a))); ",
				style.getCssText());
		assertEquals(
				"background-image:-webkit-gradient(linear,left top,right top,from(var(--foo,#d32c1e)),to(var(--bar,#e13b4a)))",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingLinear() {
		style.setCssText("background-image: repeating-linear-gradient(45deg, yellow, blue 20%); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("repeating-linear-gradient(45deg, yellow, blue 20%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: repeating-linear-gradient(45deg, yellow, blue 20%); ", style.getCssText());
		assertEquals("background-image:repeating-linear-gradient(45deg,yellow,blue 20%)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.REPEATING_LINEAR_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingLinear2() {
		style.setCssText("background-image: repeating-linear-gradient(to right, #a02 0%, #2f1 10%, #a02 25%); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("repeating-linear-gradient(to right, #a02 0%, #2f1 10%, #a02 25%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: repeating-linear-gradient(to right, #a02 0%, #2f1 10%, #a02 25%); ",
				style.getCssText());
		assertEquals("background-image:repeating-linear-gradient(to right,#a02 0%,#2f1 10%,#a02 25%)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.REPEATING_LINEAR_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadial() {
		style.setCssText("background-image: radial-gradient(5em circle at top left, yellow, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("radial-gradient(5em circle at top left, yellow, blue)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(5em circle at top left, yellow, blue); ", style.getCssText());
		assertEquals("background-image:radial-gradient(5em circle at top left,yellow,blue)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
		//
		style.setCssText("background-image: radial-gradient(yellow, green); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("radial-gradient(yellow, green)", style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(yellow, green); ", style.getCssText());
		assertEquals("background-image:radial-gradient(yellow,green)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(ellipse at center, yellow 0%, green 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("radial-gradient(ellipse at center, yellow 0%, green 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(ellipse at center, yellow 0%, green 100%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(ellipse at center,yellow 0%,green 100%)",
				style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(farthest-corner at 50% 50%, yellow, green); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("radial-gradient(farthest-corner at 50% 50%, yellow, green)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(farthest-corner at 50% 50%, yellow, green); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(farthest-corner at 50% 50%,yellow,green)",
				style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: radial-gradient(20px 30px at 20px 30px, red, yellow, green); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("radial-gradient(20px 30px at 20px 30px, red, yellow, green)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(20px 30px at 20px 30px, red, yellow, green); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(20px 30px at 20px 30px,red,yellow,green)",
				style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadial2() {
		style.setCssText(
				"background-image: radial-gradient(center, ellipse cover, rgb(0 0 0 / 0.4) 0, rgb(0 0 0 / 0.9) 100%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(center, ellipse cover, rgb(0 0 0 / 0.4) 0, rgb(0 0 0 / 0.9) 100%)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: radial-gradient(center, ellipse cover, rgb(0 0 0 / 0.4) 0, rgb(0 0 0 / 0.9) 100%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(center,ellipse cover,rgb(0 0 0/.4) 0,rgb(0 0 0/.9) 100%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadial3() {
		style.setCssText(
				"background-image: radial-gradient(circle at 40% 40%,rgb(255 255 255/.8),rgb(255 200 200/.6),#111 60%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(circle at 40% 40%, rgb(255 255 255 / 0.8), rgb(255 200 200 / 0.6), #111 60%)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: radial-gradient(circle at 40% 40%, rgb(255 255 255 / 0.8), rgb(255 200 200 / 0.6), #111 60%); ",
				style.getCssText());
		assertEquals(
				"background-image:radial-gradient(circle at 40% 40%,rgb(255 255 255/.8),rgb(255 200 200/.6),#111 60%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadial4() {
		style.setCssText("background-image: radial-gradient(circle,#e6e7e0 40%,rgb(43 42 161 /.6) 110%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertEquals("radial-gradient(circle, #e6e7e0 40%, rgb(43 42 161 / 0.6) 110%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(circle, #e6e7e0 40%, rgb(43 42 161 / 0.6) 110%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(circle,#e6e7e0 40%,rgb(43 42 161/.6) 110%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadial5() {
		style.setCssText("background-image: radial-gradient(40%,circle,#d4a9af 55%,#000 150%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(40%, circle, #d4a9af 55%, #000 150%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(40%, circle, #d4a9af 55%, #000 150%); ", style.getCssText());
		assertEquals("background-image:radial-gradient(40%,circle,#d4a9af 55%,#000 150%)", style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialVar() {
		style.setCssText("background-image: radial-gradient(40%,circle,var(--foo,#d4a9af) 55%,var(--bar,#000) 150%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(40%, circle, var(--foo, #d4a9af) 55%, var(--bar, #000) 150%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(40%, circle, var(--foo, #d4a9af) 55%, var(--bar, #000) 150%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(40%,circle,var(--foo,#d4a9af) 55%,var(--bar,#000) 150%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialVarLexical() {
		style.setCssText(
				"background-image: radial-gradient(circle, var(--white) 0%, var(--grey) 66%, var(--black) 100%);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("radial-gradient(circle, var(--white) 0%, var(--grey) 66%, var(--black) 100%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(circle, var(--white) 0%, var(--grey) 66%, var(--black) 100%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(circle,var(--white) 0%,var(--grey) 66%,var(--black) 100%)",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialAttr() {
		style.setCssText(
				"background-image: radial-gradient(attr(data-shape type(<custom-ident>)),attr(data-c1 type(<color>)) attr(data-pcnt1 %),attr(data-c2 type(<color>)));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"radial-gradient(attr(data-shape type(<custom-ident>)), attr(data-c1 type(<color>)) attr(data-pcnt1 %), attr(data-c2 type(<color>)))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: radial-gradient(attr(data-shape type(<custom-ident>)), attr(data-c1 type(<color>)) attr(data-pcnt1 %), attr(data-c2 type(<color>))); ",
				style.getCssText());
		assertEquals(
				"background-image:radial-gradient(attr(data-shape type(<custom-ident>)),attr(data-c1 type(<color>)) attr(data-pcnt1 %),attr(data-c2 type(<color>)))",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialAttr2() {
		style.setCssText(
				"background-image: radial-gradient(attr(data-size type(<length>),5em) attr(data-shape type(<custom-ident>),circle) at top left, attr(data-color1 type(<color>),yellow), attr(data-color2 type(<color>),blue))");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("radial-gradient(attr(data-size type(<length>), 5em) attr(data-shape type(<custom-ident>), circle) at top left, attr(data-color1 type(<color>), yellow), attr(data-color2 type(<color>), blue))",
				style.getPropertyValue("background-image"));
		assertEquals("background-image:radial-gradient(attr(data-size type(<length>),5em) attr(data-shape type(<custom-ident>),circle) at top left,attr(data-color1 type(<color>),yellow),attr(data-color2 type(<color>),blue))",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialPcntFirst() {
		style.setCssText("background-image: radial-gradient(circle,40% #e6e7e0,110% rgb(43 42 161 /.6)); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertEquals("radial-gradient(circle, #e6e7e0 40%, rgb(43 42 161 / 0.6) 110%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(circle, #e6e7e0 40%, rgb(43 42 161 / 0.6) 110%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(circle,#e6e7e0 40%,rgb(43 42 161/.6) 110%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialPcntFirst2() {
		style.setCssText("background-image: radial-gradient(40% #e6e7e0,110% rgb(43 42 161 /.6)); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		assertEquals("radial-gradient(#e6e7e0 40%, rgb(43 42 161 / 0.6) 110%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(#e6e7e0 40%, rgb(43 42 161 / 0.6) 110%); ", style.getCssText());
		assertEquals("background-image:radial-gradient(#e6e7e0 40%,rgb(43 42 161/.6) 110%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialVarPcntFirst() {
		style.setCssText("background-image: radial-gradient(40%,circle,55% var(--foo,#d4a9af),150% var(--bar,#000)); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("radial-gradient(40%, circle, var(--foo, #d4a9af) 55%, var(--bar, #000) 150%)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: radial-gradient(40%, circle, var(--foo, #d4a9af) 55%, var(--bar, #000) 150%); ",
				style.getCssText());
		assertEquals("background-image:radial-gradient(40%,circle,var(--foo,#d4a9af) 55%,var(--bar,#000) 150%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialBad() {
		//
		style.setCssText("background-image: radial-gradient(20px 30px at 20px 30px, red, yellow 10% 100% 1%, green); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		//
		style.setCssText("background-image: radial-gradient(ellipse at center, yellow 0% green 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRadialBadZeroStops() {
		style.setCssText("background-image: radial-gradient(5em circle at top left); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialBadOneStop() {
		style.setCssText("background-image: radial-gradient(5em circle at top left, yellow); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialBadInitialComma() {
		style.setCssText("background-image: radial-gradient(,circle,blue 40%,navy 100%); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialBadTwoCommas() {
		style.setCssText("background-image: radial-gradient(circle,,blue 40%,navy 100%); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRadialBadTwoCommas2() {
		style.setCssText("background-image: radial-gradient(circle,blue 40%,,navy 100%); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingRadial() {
		style.setCssText(
				"background-image: repeating-radial-gradient(center, ellipse cover, rgb(0 0 0 / 0.4) 0, rgb(0 0 0 / 0.9) 100%); ");
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.Type.GRADIENT, val.getPrimitiveType());
		assertEquals(CSSGradientValue.GradientType.REPEATING_RADIAL_GRADIENT, val.getGradientType());
		assertEquals(4, val.getArguments().size());
		assertEquals("repeating-radial-gradient(center, ellipse cover, rgb(0 0 0 / 0.4) 0, rgb(0 0 0 / 0.9) 100%)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: repeating-radial-gradient(center, ellipse cover, rgb(0 0 0 / 0.4) 0, rgb(0 0 0 / 0.9) 100%); ",
				style.getCssText());
		assertEquals(
				"background-image:repeating-radial-gradient(center,ellipse cover,rgb(0 0 0/.4) 0,rgb(0 0 0/.9) 100%)",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConic() {
		style.setCssText("background-image: conic-gradient(#f06, gold); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(#f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(#f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#f06,gold)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
		//
		style.setCssText("background-image: conic-gradient(at 50% 50%, #f06, gold); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(at 50% 50%, #f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(at 50% 50%, #f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(at 50% 50%,#f06,gold)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(from 0deg, #f06, gold); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(from 0deg, #f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(from 0deg, #f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(from 0deg,#f06,gold)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(from 0deg at center, #f06, gold); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(from 0deg at center, #f06, gold)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(from 0deg at center, #f06, gold); ", style.getCssText());
		assertEquals("background-image:conic-gradient(from 0deg at center,#f06,gold)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(#f06 0%, gold 100%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(#f06 0%, gold 100%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(#f06 0%, gold 100%); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#f06 0%,gold 100%)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(#f06 0deg, gold 1turn); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(#f06 0deg, gold 1turn)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(#f06 0deg, gold 1turn); ", style.getCssText());
		assertEquals("background-image:conic-gradient(#f06 0deg,gold 1turn)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(white -50%, black 150%); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(white -50%, black 150%)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(white -50%, black 150%); ", style.getCssText());
		assertEquals("background-image:conic-gradient(white -50%,black 150%)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(hsl(0 0% 75%), hsl(0 0% 25%)); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(hsl(0 0% 75%), hsl(0 0% 25%))", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(hsl(0 0% 75%), hsl(0 0% 25%)); ", style.getCssText());
		assertEquals("background-image:conic-gradient(hsl(0 0% 75%),hsl(0 0% 25%))", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(white 45deg, black 225deg, white 405deg); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(white 45deg, black 225deg, white 405deg)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(white 45deg, black 225deg, white 405deg); ", style.getCssText());
		assertEquals("background-image:conic-gradient(white 45deg,black 225deg,white 405deg)",
				style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		//
		style.setCssText("background-image: conic-gradient(red, magenta, blue, aqua, lime, yellow, red); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(red, magenta, blue, aqua, lime, yellow, red)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(red, magenta, blue, aqua, lime, yellow, red); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(red,magenta,blue,aqua,lime,yellow,red)",
				style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(7, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicNoColorAtEnd() {
		style.setCssText("background-image: conic-gradient(white 45deg, black 225deg, 405deg); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(white 45deg, black 225deg, 405deg)", style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(white 45deg, black 225deg, 405deg); ", style.getCssText());
		assertEquals("background-image:conic-gradient(white 45deg,black 225deg,405deg)", style.getMinifiedCssText());
		GradientValue val = (GradientValue) style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicAngleFirst() {
		style.setCssText("background-image: conic-gradient(45deg white,225deg black,405deg white); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(white 45deg, black 225deg, white 405deg)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(white 45deg, black 225deg, white 405deg); ", style.getCssText());
		assertEquals("background-image:conic-gradient(white 45deg,black 225deg,white 405deg)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVar() {
		style.setCssText(
				"background-image: conic-gradient(white 45deg, var(--foo,black) 225deg, var(--bar,white) 405deg); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(white 45deg, var(--foo, black) 225deg, var(--bar, white) 405deg)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: conic-gradient(white 45deg, var(--foo, black) 225deg, var(--bar, white) 405deg); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(white 45deg,var(--foo,black) 225deg,var(--bar,white) 405deg)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertNotNull(val);
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarLexical() {
		style.setCssText(
				"background-image: conic-gradient(var(--white) 0deg, var(--grey) 120deg, var(--black) 300deg);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("conic-gradient(var(--white) 0deg, var(--grey) 120deg, var(--black) 300deg)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(var(--white) 0deg, var(--grey) 120deg, var(--black) 300deg); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(var(--white) 0deg,var(--grey) 120deg,var(--black) 300deg)",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarLexical2() {
		style.setCssText(
				"background-image: conic-gradient(var(--angle1, 0deg) #fff, var(--angle2) #555, var(--angle3) #111);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("conic-gradient(var(--angle1, 0deg) #fff, var(--angle2) #555, var(--angle3) #111)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: conic-gradient(var(--angle1, 0deg) #fff, var(--angle2) #555, var(--angle3) #111); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(var(--angle1,0deg) #fff,var(--angle2) #555,var(--angle3) #111)",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarLexical3() {
		style.setCssText(
				"background-image: conic-gradient(var(--angle1, 10%) #fff, var(--angle2, 33%) #555, var(--angle3) #111);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("conic-gradient(var(--angle1, 10%) #fff, var(--angle2, 33%) #555, var(--angle3) #111)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: conic-gradient(var(--angle1, 10%) #fff, var(--angle2, 33%) #555, var(--angle3) #111); ",
				style.getCssText());
		assertEquals(
				"background-image:conic-gradient(var(--angle1,10%) #fff,var(--angle2,33%) #555,var(--angle3) #111)",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarLexical4() {
		style.setCssText(
				"background-image: conic-gradient(#fff var(--angle1, 10%), #555 var(--angle2, 33%), #111 var(--angle3));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("conic-gradient(#fff var(--angle1, 10%), #555 var(--angle2, 33%), #111 var(--angle3))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: conic-gradient(#fff var(--angle1, 10%), #555 var(--angle2, 33%), #111 var(--angle3)); ",
				style.getCssText());
		assertEquals(
				"background-image:conic-gradient(#fff var(--angle1,10%),#555 var(--angle2,33%),#111 var(--angle3))",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarLexical5() {
		style.setCssText(
				"background-image: conic-gradient(#fff var(--angle1), #555 var(--angle2, 33%), #111 var(--angle3));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("conic-gradient(#fff var(--angle1), #555 var(--angle2, 33%), #111 var(--angle3))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: conic-gradient(#fff var(--angle1), #555 var(--angle2, 33%), #111 var(--angle3)); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(#fff var(--angle1),#555 var(--angle2,33%),#111 var(--angle3))",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarAngleFirst() {
		style.setCssText(
				"background-image: conic-gradient(45deg var(--color45,white),225deg var(--foo,black),405deg var(--bar,white)); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(var(--color45, white) 45deg, var(--foo, black) 225deg, var(--bar, white) 405deg)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: conic-gradient(var(--color45, white) 45deg, var(--foo, black) 225deg, var(--bar, white) 405deg); ",
				style.getCssText());
		assertEquals(
				"background-image:conic-gradient(var(--color45,white) 45deg,var(--foo,black) 225deg,var(--bar,white) 405deg)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicVarAngleFirstOmitFirstColor() {
		style.setCssText("background-image: conic-gradient(45deg,225deg var(--foo,black),405deg var(--bar,white)); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("conic-gradient(45deg, var(--foo, black) 225deg, var(--bar, white) 405deg)",
				style.getPropertyValue("background-image"));
		assertEquals("background-image: conic-gradient(45deg, var(--foo, black) 225deg, var(--bar, white) 405deg); ",
				style.getCssText());
		assertEquals("background-image:conic-gradient(45deg,var(--foo,black) 225deg,var(--bar,white) 405deg)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.CONIC_GRADIENT, val.getGradientType());
		assertEquals(3, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBad() {
		style.setCssText("background-image: conic-gradient(red, magenta blue, aqua, lime, yellow, red); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		//
		style.setCssText("background-image: conic-gradient(white 45deg, black 225deg 200deg 1deg, white 405deg); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNull(cssval);
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testConicBadZeroStops() {
		style.setCssText("background-image: conic-gradient(from 30deg); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadOneStop() {
		style.setCssText("background-image: conic-gradient(#f06); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadOneStop2() {
		style.setCssText("background-image: conic-gradient(at 50% 50%, #f06); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadInitialComma() {
		style.setCssText("background-image: conic-gradient(,from 0deg, #f06, gold); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadTwoCommas() {
		style.setCssText("background-image: conic-gradient(from 0deg,, #f06, gold); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadTwoCommas2() {
		style.setCssText("background-image: conic-gradient(from 0deg, #f06,, gold); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadSlash() {
		style.setCssText("background-image: conic-gradient(from 0deg, #f06,/ gold); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadSlash2() {
		style.setCssText("background-image: conic-gradient(from 0deg, #f06/, gold); ");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicBadVar() {
		style.setCssText(
				"background-image: conic-gradient(#fff var(--angle1, 2px), #555 var(--angle2, 33%), #111 var(--angle3));");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testConicAttr() {
		style.setCssText(
				"background-image: conic-gradient(from attr(data-from type(<angle>)) at attr(data-pos1 type(<length>)) attr(data-pos2 type(<length>)),attr(data-stopc1 type(<color>),black) attr(data-stop1 type(<percentage>)),attr(data-stop2 type(<angle>)) attr(data-stopc2 type(<color>),white))");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"conic-gradient(from attr(data-from type(<angle>)) at attr(data-pos1 type(<length>)) attr(data-pos2 type(<length>)), attr(data-stopc1 type(<color>), black) attr(data-stop1 type(<percentage>)), attr(data-stop2 type(<angle>)) attr(data-stopc2 type(<color>), white))",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image:conic-gradient(from attr(data-from type(<angle>)) at attr(data-pos1 type(<length>)) attr(data-pos2 type(<length>)),attr(data-stopc1 type(<color>),black) attr(data-stop1 type(<percentage>)),attr(data-stop2 type(<angle>)) attr(data-stopc2 type(<color>),white))",
				style.getMinifiedCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingConic() {
		style.setCssText(
				"background-image: repeating-conic-gradient(hsl(0 0% 100%/.2) 0deg 15deg, hsl(0 33% 100%/.7) 15deg 30deg);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("repeating-conic-gradient(hsl(0 0% 100% / 0.2) 0deg 15deg, hsl(0 33% 100% / 0.7) 15deg 30deg)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: repeating-conic-gradient(hsl(0 0% 100% / 0.2) 0deg 15deg, hsl(0 33% 100% / 0.7) 15deg 30deg); ",
				style.getCssText());
		assertEquals(
				"background-image:repeating-conic-gradient(hsl(0 0% 100%/.2) 0deg 15deg,hsl(0 33% 100%/.7) 15deg 30deg)",
				style.getMinifiedCssText());
		GradientValue val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.REPEATING_CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		// Match
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<url> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, val.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, val.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, val.matches(syn));
		//
		style.setCssText("background-image: repeating-conic-gradient(gold, #f06 20deg); ");
		cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.GRADIENT, cssval.getPrimitiveType());
		assertEquals("repeating-conic-gradient(gold, #f06 20deg)", style.getPropertyValue("background-image"));
		assertEquals("background-image: repeating-conic-gradient(gold, #f06 20deg); ", style.getCssText());
		assertEquals("background-image:repeating-conic-gradient(gold,#f06 20deg)", style.getMinifiedCssText());
		val = (GradientValue) cssval;
		assertEquals(CSSGradientValue.GradientType.REPEATING_CONIC_GRADIENT, val.getGradientType());
		assertEquals(2, val.getArguments().size());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingConicVarLexical() {
		style.setCssText(
				"background-image: repeating-conic-gradient(var(--white) 0deg 30deg, var(--grey) 30deg 120deg, var(--black) 120deg 300deg);");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals(
				"repeating-conic-gradient(var(--white) 0deg 30deg, var(--grey) 30deg 120deg, var(--black) 120deg 300deg)",
				style.getPropertyValue("background-image"));
		assertEquals(
				"background-image: repeating-conic-gradient(var(--white) 0deg 30deg, var(--grey) 30deg 120deg, var(--black) 120deg 300deg); ",
				style.getCssText());
		assertEquals(
				"background-image:repeating-conic-gradient(var(--white) 0deg 30deg,var(--grey) 30deg 120deg,var(--black) 120deg 300deg)",
				style.getMinifiedCssText());
		LexicalValue val = (LexicalValue) cssval;
		assertEquals(CSSValue.Type.GRADIENT, val.getFinalType());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingConicBadOneStop() {
		style.setCssText("background-image: repeating-conic-gradient(hsl(0 0% 100%/.2) 0deg 15deg);");
		assertNull(style.getPropertyCSSValue("background-image"));
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testRepeatingConicBadOneStop2() {
		style.setCssText("background-image: repeating-conic-gradient(gold 20deg); ");
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(style.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testEquals() {
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		BaseCSSStyleDeclaration style2 = new BaseCSSStyleDeclaration();
		style2.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		StyleValue cssval2 = style2.getPropertyCSSValue("background-image");
		assertTrue(cssval.equals(cssval2));
		assertEquals(cssval.hashCode(), cssval2.hashCode());
		style2.setCssText("background-image: linear-gradient(to top right, red, white); ");
		cssval2 = style2.getPropertyCSSValue("background-image");
		assertFalse(cssval.equals(cssval2));
	}

	@Test
	public void testEqualsRadial() {
		style.setCssText("background-image: radial-gradient(5em circle at top left, yellow, blue); ");
		BaseCSSStyleDeclaration style2 = new BaseCSSStyleDeclaration();
		style2.setCssText("background-image: radial-gradient(5em circle at top left, yellow, blue); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		StyleValue cssval2 = style2.getPropertyCSSValue("background-image");
		assertTrue(cssval.equals(cssval2));
		assertEquals(cssval.hashCode(), cssval2.hashCode());
		style2.setCssText("background-image: radial-gradient(5em circle at top right, yellow, blue); ");
		cssval2 = style2.getPropertyCSSValue("background-image");
		assertFalse(cssval.equals(cssval2));
	}

	@Test
	public void testEqualsConic() {
		style.setCssText("background-image: conic-gradient(at 50% 50%, #f06, gold); ");
		BaseCSSStyleDeclaration style2 = new BaseCSSStyleDeclaration();
		style2.setCssText("background-image: conic-gradient(at 50% 50%, #f06, gold); ");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		StyleValue cssval2 = style2.getPropertyCSSValue("background-image");
		assertTrue(cssval.equals(cssval2));
		assertEquals(cssval.hashCode(), cssval2.hashCode());
		style2.setCssText("background-image: conic-gradient(at 50% 52%, #f06, gold); ");
		cssval2 = style2.getPropertyCSSValue("background-image");
		assertFalse(cssval.equals(cssval2));
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("background-image: linear-gradient(to top right, red, white, blue); ");
		GradientValue value = (GradientValue) style.getPropertyCSSValue("background-image");
		GradientValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getGradientType(), clon.getGradientType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getArguments(), clon.getArguments());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
