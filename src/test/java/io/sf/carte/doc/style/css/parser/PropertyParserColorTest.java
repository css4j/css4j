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

public class PropertyParserColorTest {

	private static Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		parser = new CSSParser();
		syntaxParser = new SyntaxParser();
	}

	@Test
	public void testParsePropertyValueColor() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Color(rec2020 0.42053 0.97978 0.00579)");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.42053f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.97978f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.00579f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("color", lu.getFunctionName());
		assertEquals("color(rec2020 0.42053 0.97978 0.00579)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color(rec2020 0.42053 0.97978 0.00579 / 62%)");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.42053f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.97978f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.00579f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(62f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("color", lu.getFunctionName());
		assertEquals("color(rec2020 0.42053 0.97978 0.00579/62%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color(var(--color-args))");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color", lu.getFunctionName());
		assertEquals("color(var(--color-args))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorVar2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Color(rec2020 var(--color-args))");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		assertEquals("color", lu.getFunctionName());
		assertEquals("color(rec2020 var(--color-args))", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorVarAlpha() throws CSSException {
		LexicalUnit lu = parsePropertyValue("color(var(--color-args)/0.344)");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());

		LexicalUnit varparam = param.getParameters();
		assertEquals("--color-args", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.344f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("color", lu.getFunctionName());
		assertEquals("color(var(--color-args)/0.344)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorNone() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Color(rec2020 none 0.97978 0.00579)");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("rec2020", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.97978f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.00579f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("color", lu.getFunctionName());
		assertEquals("color(rec2020 none 0.97978 0.00579)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCustomProfileNone() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Color(--my-profile none 0.97978 0.00579)");
		assertEquals(LexicalType.COLOR_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--my-profile", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.97978f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.00579f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("color", lu.getFunctionName());
		assertEquals("color(--my-profile none 0.97978 0.00579)", lu.toString());

		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<color>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueColorBad() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("color(rec2020 )"));
	}

	@Test
	public void testParsePropertyValueColorBad2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("color(rec2020, 0 0 0 )"));
	}

	@Test
	public void testParsePropertyValueColorBadNoComponents() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("color(rec2020/40%)"));
	}

	@Test
	public void testParsePropertyValueColorBadCommasBetweenComponents()
			throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color(rec2020 0.1, 0.2, 0.3)"));
	}

	@Test
	public void testParsePropertyValueColorBadSlashNoAlpha() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color(rec2020 0.1 0.2 0.3 /)"));
	}

	@Test
	public void testParsePropertyValueColorBadNumberAfterAlpha() throws CSSException {
		assertThrows(CSSParseException.class,
				() -> parsePropertyValue("color(rec2020 0.1 0.2 0.3 / 0.8 1)"));
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
