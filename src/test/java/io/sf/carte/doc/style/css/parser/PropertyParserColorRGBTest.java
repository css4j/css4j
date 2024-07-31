/*

 Copyright (c) 2005-2024, Carlos Amengual.

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
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserColorRGBTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyBadHexColor2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("#"));
	}

	@Test
	public void testParsePropertyBadHexColor3() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class, () -> parsePropertyValue("#x"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor4() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class, () -> parsePropertyValue("#,"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor5() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class, () -> parsePropertyValue("#:"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor6() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#@charset"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor7() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue(" #-"));
		assertEquals(3, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor8() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class, () -> parsePropertyValue("#_"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor9() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class, () -> parsePropertyValue("#."));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor10() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class, () -> parsePropertyValue("##"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor11() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#fff(e)"));
		assertEquals(5, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor12() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#(e)"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor13() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#:fff"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor14() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#fff(e)"));
		assertEquals(5, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor15() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#foo "));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadHexColor16() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("#\\#aaa"));
	}

	@Test
	public void testParsePropertyBadImportant() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("rgb(128, 0, 97 !important"));
		assertEquals(16, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadImportant2() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("rgb(128, 0, 97 !important)"));
		assertEquals(16, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadImportant4() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("# !important"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadImportant5() throws CSSException {
		CSSParseException e = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("#!important"));
		assertEquals(2, e.getColumnNumber());
	}

	@Test
	public void testParsePropertyBorderColor() throws CSSException {
		LexicalUnit lu = parsePropertyValue("blue #a7f31a green");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(167, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(243, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(26, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("green", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueRGBZero() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0 0 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 0 0)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>#");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(CSSValueSyntax.Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueRGBZeroSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0 0 0 / 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 0 0/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBA() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1,2,3,0.45)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.45f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1, 2, 3, 0.45)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAZeroAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(0,0,0,0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(0, 0, 0, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1,2,3,45%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(45f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1, 2, 3, 45%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1%,2%,3%,0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1%, 2%, 3%, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBACommasPcntMixed() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1%,0,3%,120%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1%, 0, 3%, 100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntMixed() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1% 0 3%/120%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1% 0 3%/100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1,var(--foo))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		param = param.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--foo", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1, var(--foo))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAVar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1,var(--foo),0.9)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getStringValue());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--foo", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.9f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba(1, var(--foo), 0.9)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAVar3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(1,2,3,var(--foo))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getStringValue());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--foo", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba(1, 2, 3, var(--foo))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBVarSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(1 var(--foo)/0.6)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getStringValue());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--foo", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(1 var(--foo)/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBVarSlashPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(1 var(--foo)/52%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getStringValue());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--foo", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(52f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(1 var(--foo)/52%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(1 2 3/var(--foo))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getStringValue());
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.IDENT, subparam.getLexicalUnitType());
		assertEquals("--foo", subparam.getStringValue());
		assertNull(subparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(1 2 3/var(--foo))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12.6 127.4 48.8)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.6f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(127.4f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.8f, param.getFloatValue(), 1e-7f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12.6 127.4 48.8)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(calc(12) 127 calc(48))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(48, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(calc(12) 127 calc(48))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCalcReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(calc(12) 127.8 calc(48.3))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(127.8f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.REAL, param.getParameters().getLexicalUnitType());
		assertEquals(48.3f, param.getParameters().getFloatValue(), 1e-7f);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(calc(12) 127.8 calc(48.3))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCalcAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(calc(12) 127 calc(48)/calc(80%))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(48, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(80f, param.getParameters().getFloatValue(), 1e-5f);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(calc(12) 127 calc(48)/calc(80%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcntMix() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0 27% 48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 27% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcntMix2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(27% 0 48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-6f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-6f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(27% 0 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBRealSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12.9 127 48.1 / 0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.9f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.1f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-7f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12.9 127 48.1/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBRealSlashZero() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12.9 127 48.1 / 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.9f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.1f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12.9 127 48.1/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBRealSlashZeroReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12.9 127 48.1 / 0.0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.9f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.1f, param.getFloatValue(), 1e-7f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12.9 127 48.1/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashMini() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48/.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashMini2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48/ .1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 82%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(82f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/82%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlphaClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 2)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlphaZero() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlphaZeroClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / -2)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcntSlash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%/0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcntSlashPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%/8%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(8f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%/8%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"rgb(attr(data-red percentage) attr(data-green percentage) attr(data-blue %)/attr(data-alpha %))");
		assertNotNull(lu);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-red percentage)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-green percentage)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-blue %)", param.getCssText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("attr(data-alpha %)", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(attr(data-red percentage) attr(data-green percentage) attr(data-blue %)/attr(data-alpha %))",
				lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12, 127, 48)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12, 127, 48)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12%,27%,48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12%, 27%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaPcnt2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0,27%,48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0, 27%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 8%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(8f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 8%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaPcntClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 108%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 100%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 0, 48%, 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 0, 48%, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12,, 48)"));
	}

	@Test
	public void testParsePropertyValueRGBCommaBad2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12,13,)"));
	}

	@Test
	public void testParsePropertyValueRGBCommaBad3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(,13,14,15)"));
	}

	@Test
	public void testParsePropertyValueRGBBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12/ 48 0.1)"));
	}

	@Test
	public void testParsePropertyValueRGBBad2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12 48/0.1)"));
	}

	@Test
	public void testParsePropertyValueRGBBad3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12 48,127,0.1)"));
	}

	@Test
	public void testParsePropertyValueRGBBad4() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12,48 127,0.1)"));
	}

	@Test
	public void testParsePropertyValueRGBBad5() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(12,48,127/0.1)"));
	}

	@Test
	public void testParsePropertyValueRGBBad6() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgba(0, 0, 0 / 0)"));
	}

	@Test
	public void testParsePropertyValueRGBBad7() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgba(0, 0, 0, 0, 0)"));
	}

	@Test
	public void testParsePropertyValueRGBBad8() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 0 0 0/0)"));
	}

	@Test
	public void testParsePropertyValueRGBBad9() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 0 0/0 0)"));
	}

	@Test
	public void testParsePropertyValueRGBBad10() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 0 0/0/0)"));
	}

	@Test
	public void testParsePropertyValueRGBBad11() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 0 0//0)"));
	}

	@Test
	public void testParsePropertyValueRGBBad12() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 0 0/0/)"));
	}

	@Test
	public void testParsePropertyValueRGBClampRValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(-6 0.2 2.8)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0f, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(2.8f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 0.2 2.8)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBClampGValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0.2 -6 2.8)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0f, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(2.8f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0.2 0 2.8)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBClampBValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0.2 2.8 -6)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(2.8f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0f, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0.2 2.8 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMix() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0 2% 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(0 2% 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMix2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(2% 0 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(2% 0 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMix3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(2% 20 10)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(2% 20 10)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMix4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(2% 20.6 10)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(2% 20.6 10)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMix5() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(20.6 2% 30%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(20.6 2% 30%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMixCommas() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(0, 2%, 10)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(0, 2%, 10)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBMixCommas2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(2%, 0, 10)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb(2%, 0, 10)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBBadChar() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 a 0/ 0)"));
	}

	@Test
	public void testParsePropertyValueRGBBadChar2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(0 0 0/@ 0)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(1,var(--foo)/0.2)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar2() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("rgb(1 var(--foo) var(--bar),0.2)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(1 /var(--foo))"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar4() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(1 3 5 /var(--foo)/)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar5() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(var(--foo)/.8,4)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar6() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(var(--foo)/.8,.4)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar7() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue(
				"rgb(var(--foo),var(--foo),var(--foo),var(--foo),var(--foo))"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar8() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue(
				"rgb(var(--foo) var(--foo) var(--foo) var(--foo)/var(--foo))"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar9() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(var(--foo)/.8/.4)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVar10() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(var(--foo)/1/.4)"));
	}

	@Test
	public void testParsePropertyValueRGBBadVarComma() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("rgb(10 20 var(--foo), 0)"));
	}

	@Test
	public void testParsePropertyValueRGBCommaClampAlphaInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(12,48,-127,2)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12, 48, 0, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaClampAlphaNegInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgba(-12,-48,-127,-1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(0, 0, 0, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBClampAlphaInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 48% 127%/2)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 48% 100%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBClampAlpha2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 48% 127%/-1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 48% 100%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBClampAlphaReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 48% 127%/2.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 48% 100%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBClampAlphaRealNeg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 48% 127%/-0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 48% 100%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBNoneR() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(none 48% 27%/0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(none 48% 27%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBNoneG() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% none 27%/0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% none 27%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBNoneB() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 48% none/0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 48% none/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBNoneAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 48% 27.3%/none)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27.3f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 48% 27.3%/none)", lu.toString());
	}

	@Test
	public void testParsePropertyValueColorHex3() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("#fd3");
		assertEquals(LexicalType.RGBCOLOR, lunit.getLexicalUnitType());
		assertEquals("rgb", lunit.getFunctionName());
		assertEquals("#fd3", lunit.toString());
		assertNull(lunit.getNextLexicalUnit());
		LexicalUnit lu = lunit.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(255, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(221, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(51, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<color>");
		assertEquals(CSSValueSyntax.Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(CSSValueSyntax.Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(CSSValueSyntax.Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueColorHex4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("#fd3b");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#fd3b", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(255, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(221, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(51, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.7333f, lu.getFloatValue(), 0.0001f);
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueColorHex6() throws CSSException {
		LexicalUnit lu = parsePropertyValue("#a7f31a");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#a7f31a", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(167, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(243, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(26, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueColorHex8() throws CSSException {
		LexicalUnit lu = parsePropertyValue("#a7f31af0");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#a7f31af0", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(167, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(243, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(26, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.9412f, lu.getFloatValue(), 0.0001f);
		assertNull(lu.getNextLexicalUnit());
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException {
		try {
			return parser.parsePropertyValue(new StringReader(value));
		} catch (IOException e) {
			return null;
		}
	}

}
