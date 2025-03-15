/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserColorMixTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueColorMixInRec2020_LCh() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"Color-Mix(in rec2020, color(display-p3 0.42053 0.97978 0.00579),lch(88.423 142.708 128.211))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.COLOR_FUNCTION, param.getLexicalUnitType());
		LexicalUnit p3param = param.getParameters();
		assertEquals(LexicalType.IDENT, p3param.getLexicalUnitType());
		assertEquals("display-p3", p3param.getStringValue());

		p3param = p3param.getNextLexicalUnit();
		assertNotNull(p3param);
		assertEquals(LexicalType.REAL, p3param.getLexicalUnitType());
		assertEquals(0.42053f, p3param.getFloatValue(), 1e-5f);
		p3param = p3param.getNextLexicalUnit();
		assertNotNull(p3param);
		assertEquals(LexicalType.REAL, p3param.getLexicalUnitType());
		assertEquals(0.97978f, p3param.getFloatValue(), 1e-5f);
		p3param = p3param.getNextLexicalUnit();
		assertNotNull(p3param);
		assertEquals(LexicalType.REAL, p3param.getLexicalUnitType());
		assertEquals(0.00579f, p3param.getFloatValue(), 1e-5f);
		assertNull(p3param.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.LCHCOLOR, param.getLexicalUnitType());
		LexicalUnit lchparam = param.getParameters();
		assertEquals(LexicalType.REAL, lchparam.getLexicalUnitType());
		assertEquals(88.423f, lchparam.getFloatValue(), 1e-5f);
		lchparam = lchparam.getNextLexicalUnit();
		assertNotNull(lchparam);
		assertEquals(LexicalType.REAL, lchparam.getLexicalUnitType());
		assertEquals(142.708f, lchparam.getFloatValue(), 1e-5f);
		lchparam = lchparam.getNextLexicalUnit();
		assertNotNull(lchparam);
		assertEquals(LexicalType.REAL, lchparam.getLexicalUnitType());
		assertEquals(128.211f, lchparam.getFloatValue(), 1e-5f);
		assertNull(lchparam.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals(
				"color-mix(in rec2020, color(display-p3 0.42053 0.97978 0.00579), lch(88.423 142.708 128.211))",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	/**
	 * This implementation allows omitting 'hue' after the interpolation method.
	 * 
	 * @throws CSSException
	 */
	@Test
	public void testParsePropertyValueColorMixInLChLonger_OmitHue() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"Color-Mix(in lch longer, hwb(144.48 0.8% 15%),lch(78.745 123.946 106.33))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("lch", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("longer", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.HWBCOLOR, param.getLexicalUnitType());
		assertEquals("hwb(144.48 0.8% 15%)", param.getCssText());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.LCHCOLOR, param.getLexicalUnitType());
		assertEquals("lch(78.745 123.946 106.33)", param.toString());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in lch longer, hwb(144.48 0.8% 15%), lch(78.745 123.946 106.33))",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixInLChDecreasingHue() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"Color-Mix(in lch decreasing hue, hwb(144.48 0.8% 15%),lch(78.745 123.946 106.33))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("lch", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("decreasing", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("hue", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.HWBCOLOR, param.getLexicalUnitType());
		assertEquals("hwb(144.48 0.8% 15%)", param.getCssText());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.LCHCOLOR, param.getLexicalUnitType());
		assertEquals("lch(78.745 123.946 106.33)", param.toString());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals(
				"color-mix(in lch decreasing hue, hwb(144.48 0.8% 15%), lch(78.745 123.946 106.33))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueColorMixPcntLCh() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"Color-Mix(in rec2020, color(display-p3 0.42053 0.97978 0.00579) 40%,lch(88.423 142.708 128.211))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.COLOR_FUNCTION, param.getLexicalUnitType());
		LexicalUnit p3param = param.getParameters();
		assertEquals(LexicalType.IDENT, p3param.getLexicalUnitType());
		assertEquals("display-p3", p3param.getStringValue());

		p3param = p3param.getNextLexicalUnit();
		assertNotNull(p3param);
		assertEquals(LexicalType.REAL, p3param.getLexicalUnitType());
		assertEquals(0.42053f, p3param.getFloatValue(), 1e-5f);
		p3param = p3param.getNextLexicalUnit();
		assertNotNull(p3param);
		assertEquals(LexicalType.REAL, p3param.getLexicalUnitType());
		assertEquals(0.97978f, p3param.getFloatValue(), 1e-5f);
		p3param = p3param.getNextLexicalUnit();
		assertNotNull(p3param);
		assertEquals(LexicalType.REAL, p3param.getLexicalUnitType());
		assertEquals(0.00579f, p3param.getFloatValue(), 1e-5f);
		assertNull(p3param.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(40f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.LCHCOLOR, param.getLexicalUnitType());
		LexicalUnit lchparam = param.getParameters();
		assertEquals(LexicalType.REAL, lchparam.getLexicalUnitType());
		assertEquals(88.423f, lchparam.getFloatValue(), 1e-5f);
		lchparam = lchparam.getNextLexicalUnit();
		assertNotNull(lchparam);
		assertEquals(LexicalType.REAL, lchparam.getLexicalUnitType());
		assertEquals(142.708f, lchparam.getFloatValue(), 1e-5f);
		lchparam = lchparam.getNextLexicalUnit();
		assertNotNull(lchparam);
		assertEquals(LexicalType.REAL, lchparam.getLexicalUnitType());
		assertEquals(128.211f, lchparam.getFloatValue(), 1e-5f);
		assertNull(lchparam.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals(
				"color-mix(in rec2020, color(display-p3 0.42053 0.97978 0.00579) 40%, lch(88.423 142.708 128.211))",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixHexPcntIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in display-p3, #0200fa 10%, white)");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("display-p3", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#0200fa", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("white", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in display-p3, #0200fa 10%, white)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixPcntHexIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in display-p3, 20% #0200fa, white)");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("display-p3", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(20f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#0200fa", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("white", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in display-p3, 20% #0200fa, white)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixAllVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(var(--color-args))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(var(--color-args))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixInVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in var(--color-args))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in var(--color-args))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixVarColors() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Color-Mix(In rec2020, var(--color-args))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("In", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(In rec2020, var(--color-args))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixInVarCommaColor() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in var(--color-args), #10aa40)");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#10aa40", param.getCssText());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in var(--color-args), #10aa40)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixInVarColor() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in var(--color-args) #10aa40)");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#10aa40", param.getCssText());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in var(--color-args) #10aa40)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixVarPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in display-p3, #0200fa var(--pcnt), white)");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("display-p3", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#0200fa", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--pcnt", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("white", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in display-p3, #0200fa var(--pcnt), white)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueColorMixVarBeforePcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in display-p3, var(--color) 10%, white)");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("display-p3", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("white", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in display-p3, var(--color) 10%, white)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixVarPcnt2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color-mix(in display-p3, #0200fa, white var(--pcnt))");
		assertEquals(LexicalType.COLOR_MIX, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("in", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("display-p3", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#0200fa", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("white", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--pcnt", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		assertNull(param.getNextLexicalUnit());

		assertEquals("color-mix", lu.getFunctionName());
		assertEquals("color-mix(in display-p3, #0200fa, white var(--pcnt))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorMixBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("color-mix(in rec2020 )"));
	}

	@Test
	public void testParsePropertyValueColorMixBad2() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color-mix(in rec2020, 0 0 )"));
	}

	@Test
	public void testParsePropertyValueColorMixBadOnlyPercent() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color-mix(in rec2020,40%)"));
	}

	@Test
	public void testParsePropertyValueColorMixBad3Args() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color-mix(in rec2020, red 1%, green 40%, blue)"));
	}

	@Test
	public void testParsePropertyValueColorMixBadDoublePcnt() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color-mix(in rec2020, #001 10% 20%, #ff0)"));
	}

	@Test
	public void testParsePropertyValueColorMixBadDoublePcnt2() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color-mix(in rec2020, #001 10%, #ff0 80% 90%)"));
	}

	@Test
	public void testParsePropertyValueColorMixBadSlash() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue(
				"color-mix(in hsl, hsl(200 50 80), coral 80%/)"));
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException {
		try {
			return parser.parsePropertyValue(new StringReader(value));
		} catch (IOException e) {
			return null;
		}
	}

	private void assertMatch(Match match, LexicalUnit lu, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, lu.matches(syn));
	}

}
