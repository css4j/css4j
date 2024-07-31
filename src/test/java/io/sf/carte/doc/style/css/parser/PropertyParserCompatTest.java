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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserCompatTest {

	private Parser parser;

	private static SyntaxParser syntaxParser;

	@BeforeAll
	public static void setUpBeforeClass() {
		syntaxParser = new SyntaxParser();
	}

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParsePropertyEscapedBackslahHack() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("600px\\9");
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("600px\\9", lu.getStringValue());

		assertMatch(Match.TRUE, lu, "*");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
	}

	@Test
	public void testParsePropertyEscapedBackslahHack2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("2px 3px\\9");
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("2px 3px\\9", lu.getStringValue());
		assertEquals("2px 3px\\9", lu.toString());

		assertMatch(Match.TRUE, lu, "*");
		assertMatch(Match.FALSE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
	}

	@Test
	public void testParsePropertyCustomFunction() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("--my-function(foo=bar)");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("--my-function", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("foo=bar", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyProgid() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue(
				"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("startColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("endColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("#d0df9f", lu.getStringValue());
	}

	@Test
	public void testParsePropertyProgid2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue(
				"progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f)");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("GradientType=0", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("StartColorStr=#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("EndColorStr=#d0df9f", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyProgid3() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.COMPAT_IDENT, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, param.getCssUnit());
		assertEquals("pixelradius=5", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "*");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
	}

	@Test
	public void testParsePropertyValueProgid() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue(
				"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("startColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("endColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("#d0df9f", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueIEExpression() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("expression(iequirk = (document.body.scrollTop) + \"px\" )");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("iequirk=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.SUB_EXPRESSION, lu.getLexicalUnitType());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.IDENT, subv.getLexicalUnitType());
		assertEquals("document.body.scrollTop", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_PLUS, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("px", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueIEExpressionBackslashError() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		try {
			parsePropertyValue("expression(iequirk = (document.body.scrollTop) + 5px\\9 )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(50, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueIEExpressionCompatError() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		try {
			parsePropertyValue("expression(= (document.body.scrollTop) + \"px\" )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
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
