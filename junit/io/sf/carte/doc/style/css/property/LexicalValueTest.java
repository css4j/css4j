/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class LexicalValueTest {

	@Test
	public void testGetFinalType() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("1em");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1%");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("1.1");
		assertEquals(Type.NUMERIC, value.getFinalType());
		//
		value.setCssText("foo");
		assertEquals(Type.IDENT, value.getFinalType());
		//
		value.setCssText("'foo'");
		assertEquals(Type.STRING, value.getFinalType());
		//
		value.setCssText("1em / 1");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("16 / 9");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("16 / calc(3*2)");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("calc(4*4) / 9");
		assertEquals(Type.RATIO, value.getFinalType());
		//
		value.setCssText("16 / foo");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("foo / 9");
		assertEquals(Type.UNKNOWN, value.getFinalType());
		//
		value.setCssText("rgb(var(--foo) 0 0.3)");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("hsl(var(--foo) 0% 0.3%)");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("hwb(var(--foo) 0% 0.3%)");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("color(display-p3 0.584 var(--foo))");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("color(display-p3 var(--foo))");
		assertEquals(Type.COLOR, value.getFinalType());
		//
		value.setCssText("url(var(--myURI))");
		assertEquals(Type.URI, value.getFinalType());
	}

	@Test
	public void testEquals() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertTrue(value.equals(value));
		LexicalValue value2 = new LexicalValue();
		value2.setCssText("1em 2em");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		value2.setCssText("1em 2px");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 2em");
		assertEquals("1em 2em", value.getCssText());
		assertEquals("1em 2em", value.getMinifiedCssText(null));
		//
		value.setCssText("1.0em,2.1em");
		assertEquals("1em, 2.1em", value.getCssText());
		assertEquals("1em,2.1em", value.getMinifiedCssText(null));
		//
		value.setCssText("foo(1em,2em),bar(1 2)");
		assertEquals("foo(1em, 2em), bar(1 2)", value.getCssText());
		assertEquals("foo(1em,2em),bar(1 2)", value.getMinifiedCssText(null));
		//
		value.setCssText("calc(1 + 2) calc(1 * 2)");
		assertEquals("calc(1 + 2) calc(1*2)", value.getCssText());
		assertEquals("calc(1 + 2) calc(1*2)", value.getMinifiedCssText(null));
		//
		value.setCssText("url('http://example.com/')");
		assertEquals("url('http://example.com/')", value.getCssText());
		assertEquals("url('http://example.com/')", value.getMinifiedCssText(null));

		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	@Test
	public void testBracketList() {
		LexicalValue value = new LexicalValue();

		value.setCssText("[var(--foo) var(--bar)]");
		assertEquals("[var(--foo) var(--bar)]", value.getCssText());
		assertEquals("[var(--foo) var(--bar)]", value.getMinifiedCssText(null));

		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, value.matches(syn));
	}

	@Test
	public void testGetCssTextEmpty() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("--foo:");
		StyleValue cssval = style.getPropertyCSSValue("--foo");
		assertEquals("", cssval.getCssText());
		assertEquals("", style.getPropertyValue("height"));
		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("--foo: ;\n", style.getCssText());
		assertEquals("--foo:", style.getMinifiedCssText());
	}

	@Test
	public void testGetCssTextEmpty2() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("--foo:;");
		StyleValue cssval = style.getPropertyCSSValue("--foo");
		assertEquals("", cssval.getCssText());
		assertEquals("", style.getPropertyValue("height"));
		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("--foo: ;\n", style.getCssText());
		assertEquals("--foo:", style.getMinifiedCssText());
	}

	@Test
	public void testURI_Var() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("background-image:url(var(--myURI));");
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertEquals("url(var(--myURI))", cssval.getCssText());

		assertEquals(CssType.PROXY, cssval.getCssValueType());
		assertEquals(Type.LEXICAL, cssval.getPrimitiveType());
		assertEquals("url(var(--myURI))", cssval.getMinifiedCssText("background-image"));
	}

	@Test
	public void testRGBColorVarCalc() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: rgb(calc(var(--r)*2) calc(var(--g) + 15) calc(var(--b) - var(--b)/2));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals("rgb(calc(var(--r)*2) calc(var(--g) + 15) calc(var(--b) - var(--b)/2))",
			value.getCssText());
		assertEquals("rgb(calc(var(--r)*2) calc(var(--g) + 15) calc(var(--b) - var(--b)/2))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testRGBColorVarClamp() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: rgb(var(--color-r) clamp(10%, calc(var(--color-g) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%) / 100%));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"rgb(var(--color-r) clamp(10%, calc(var(--color-g) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%)/100%))",
			value.getCssText());
		assertEquals(
			"rgb(var(--color-r) clamp(10%,calc(var(--color-g) - 82%),90%) clamp(20%,calc(var(--color-b) - 17%)/100%))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testRGBColorVarClampMax() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: rgb(max(var(--color-r),50%) clamp(10%, calc(var(--color-g) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%) / 100%));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"rgb(max(var(--color-r), 50%) clamp(10%, calc(var(--color-g) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%)/100%))",
			value.getCssText());
		assertEquals(
			"rgb(max(var(--color-r),50%) clamp(10%,calc(var(--color-g) - 82%),90%) clamp(20%,calc(var(--color-b) - 17%)/100%))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testHSLColorVarCalc() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: hsl(calc(var(--h)*2) calc(var(--sat) + 15%) calc(var(--l) - var(--l)/2));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals("hsl(calc(var(--h)*2) calc(var(--sat) + 15%) calc(var(--l) - var(--l)/2))",
			value.getCssText());
		assertEquals("hsl(calc(var(--h)*2) calc(var(--sat) + 15%) calc(var(--l) - var(--l)/2))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testHSLColorVarClamp() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: hsl(clamp(100deg, var(--color-h), 120deg), clamp(10%, calc(var(--color-s) - 82%), 90%), clamp(20%, calc(var(--color-l) - 17%), 100%));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"hsl(clamp(100deg, var(--color-h), 120deg), clamp(10%, calc(var(--color-s) - 82%), 90%), clamp(20%, calc(var(--color-l) - 17%), 100%))",
			value.getCssText());
		assertEquals(
			"hsl(clamp(100deg,var(--color-h),120deg),clamp(10%,calc(var(--color-s) - 82%),90%),clamp(20%,calc(var(--color-l) - 17%),100%))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testHWBColorVarCalc() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: hwb(calc(var(--h)*2) calc(var(--w) + 15%) calc(var(--b) - var(--b)/2));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals("hwb(calc(var(--h)*2) calc(var(--w) + 15%) calc(var(--b) - var(--b)/2))",
			value.getCssText());
		assertEquals("hwb(calc(var(--h)*2) calc(var(--w) + 15%) calc(var(--b) - var(--b)/2))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testHWBColorVarClamp() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: hwb(clamp(100deg, var(--color-h), 120deg) clamp(10%, calc(var(--color-w) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%), 100%));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"hwb(clamp(100deg, var(--color-h), 120deg) clamp(10%, calc(var(--color-w) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%), 100%))",
			value.getCssText());
		assertEquals(
			"hwb(clamp(100deg,var(--color-h),120deg) clamp(10%,calc(var(--color-w) - 82%),90%) clamp(20%,calc(var(--color-b) - 17%),100%))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testLabColorVarCalc() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: lab(calc(var(--l)*2) calc(var(--a) + 0.05) calc(var(--b) - var(--b)/2));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals("lab(calc(var(--l)*2) calc(var(--a) + 0.05) calc(var(--b) - var(--b)/2))",
			value.getCssText());
		assertEquals("lab(calc(var(--l)*2) calc(var(--a) + 0.05) calc(var(--b) - var(--b)/2))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testLabColorVarClamp() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: lab(clamp(2%, calc(var(--color-l) - 17%), 30%) clamp(0.1, calc(var(--color-a) - 0.3), 1.1) clamp(-1, var(--color-b), 1));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"lab(clamp(2%, calc(var(--color-l) - 17%), 30%) clamp(0.1, calc(var(--color-a) - 0.3), 1.1) clamp(-1, var(--color-b), 1))",
			value.getCssText());
		assertEquals(
			"lab(clamp(2%,calc(var(--color-l) - 17%),30%) clamp(0.1,calc(var(--color-a) - 0.3),1.1) clamp(-1,var(--color-b),1))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testLChColorVarCalc() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: lch(calc(var(--l) + 15%) calc(var(--c)*2) calc(var(--h) - var(--h)/2));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals("lch(calc(var(--l) + 15%) calc(var(--c)*2) calc(var(--h) - var(--h)/2))",
			value.getCssText());
		assertEquals("lch(calc(var(--l) + 15%) calc(var(--c)*2) calc(var(--h) - var(--h)/2))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testLChColorVarClamp() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: lch(clamp(2%, calc(var(--color-l) - 17%), 30%) clamp(0.1, calc(var(--color-c) - 0.3), 1.1) clamp(100deg, var(--color-h), 120deg));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"lch(clamp(2%, calc(var(--color-l) - 17%), 30%) clamp(0.1, calc(var(--color-c) - 0.3), 1.1) clamp(100deg, var(--color-h), 120deg))",
			value.getCssText());
		assertEquals(
			"lch(clamp(2%,calc(var(--color-l) - 17%),30%) clamp(0.1,calc(var(--color-c) - 0.3),1.1) clamp(100deg,var(--color-h),120deg))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testColorVarCalc() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: color(a98rgb calc(var(--r)*2) calc(var(--g) + 15) calc(var(--b) - var(--b)/2));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"color(a98rgb calc(var(--r)*2) calc(var(--g) + 15) calc(var(--b) - var(--b)/2))",
			value.getCssText());
		assertEquals(
			"color(a98rgb calc(var(--r)*2) calc(var(--g) + 15) calc(var(--b) - var(--b)/2))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testColorVarClamp() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText(
			"color: color(a98rgb max(var(--color-r), 70%) clamp(10%, calc(var(--color-g) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%) / 100%));");
		CSSValue value = style.getPropertyCSSValue("color");
		assertNotNull(value);

		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());

		assertEquals(
			"color(a98rgb max(var(--color-r), 70%) clamp(10%, calc(var(--color-g) - 82%), 90%) clamp(20%, calc(var(--color-b) - 17%)/100%))",
			value.getCssText());
		assertEquals(
			"color(a98rgb max(var(--color-r),70%) clamp(10%,calc(var(--color-g) - 82%),90%) clamp(20%,calc(var(--color-b) - 17%)/100%))",
			value.getMinifiedCssText("color"));

		SyntaxParser synParser = new SyntaxParser();
		CSSValueSyntax synColor = synParser.parseSyntax("<color>");
		CSSValueSyntax synColorP = synParser.parseSyntax("<color>+");
		CSSValueSyntax synUniv = synParser.parseSyntax("*");
		CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

		assertEquals(Match.TRUE, value.matches(synColor));
		assertEquals(Match.TRUE, value.matches(synColorP));
		assertEquals(Match.TRUE, value.matches(synUniv));
		assertEquals(Match.FALSE, value.matches(synPcnt));

		LexicalValue lexval = (LexicalValue) value;
		assertEquals(Type.COLOR, lexval.getFinalType());

		LexicalUnit lunit = lexval.getLexicalUnit();
		assertNotNull(lunit);

		assertEquals(Match.TRUE, lunit.matches(synColor));
		assertEquals(Match.TRUE, lunit.matches(synColorP));
		assertEquals(Match.TRUE, lunit.matches(synUniv));
		assertEquals(Match.FALSE, lunit.matches(synPcnt));
	}

	@Test
	public void testClone() {
		LexicalValue value = new LexicalValue();
		value.setCssText("1em 1px");
		LexicalValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
