/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserAttrTest {

	private Parser parser;

	private static SyntaxParser syntaxParser;
	private static CSSValueSyntax universalSyntax;

	@BeforeAll
	public static void setUpBeforeClass() {
		syntaxParser = new SyntaxParser();
		universalSyntax = syntaxParser.parseSyntax("*");
	}

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParsePropertyValueAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-count)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-count", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-count)", lu.toString());

		assertMatch(Match.TRUE, lu, "<string>");
		assertMatch(Match.TRUE, lu, "<string>#");
		assertMatch(Match.TRUE, lu, "<string>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-count, 'default')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-count", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("default", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-count, 'default')", lu.toString());

		assertMatch(Match.TRUE, lu, "<string>");
		assertMatch(Match.TRUE, lu, "<string>#");
		assertMatch(Match.TRUE, lu, "<string>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueAttrListFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-radius %, / 10%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-radius", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MOD, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(10f, param.getFloatValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-radius %,/10%)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>#");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueAttrPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-count %)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-count", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MOD, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-count %)", lu.toString());

		assertMatch(Match.TRUE, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrPercentage() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-count type(<percentage>))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-count", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<percentage>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-count type(<percentage>))", lu.toString());

		assertMatch(Match.TRUE, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<length-percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <percentage>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <percentage>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueAttrInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<integer>)) attr(data-b type(<number>))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<number>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("attr(data-a type(<integer>)) attr(data-b type(<number>))", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrIntegerComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<integer>)),attr(data-b type(<number>))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.OPERATOR_COMMA, nlu.getLexicalUnitType());

		nlu = nlu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<number>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a type(<integer>)), attr(data-b type(<number>))", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <number>#");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<integer>), auto),attr(data-b type(<number>), none)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("auto", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.OPERATOR_COMMA, nlu.getLexicalUnitType());

		nlu = nlu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<number>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a type(<integer>), auto), attr(data-b type(<number>), none)",
				lu.toString());

		assertMatch(Match.PENDING, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>#");
		assertMatch(Match.PENDING, lu, "<custom-ident># | <number>");
		assertMatch(Match.PENDING, lu, "<custom-ident># | <number>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+ | <number>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackWSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<integer>), auto) attr(data-b type(<number>), none)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("auto", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<number>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a type(<integer>), auto) attr(data-b type(<number>), none)",
				lu.toString());

		assertMatch(Match.PENDING, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<custom-ident># | <number>#");
		assertMatch(Match.PENDING, lu, "<custom-ident>+ | <number>+");
		assertMatch(Match.PENDING, lu, "<custom-ident>+ | <number>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackWSList2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a string, 1) attr(data-b type(<integer>), 'foo')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("string", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("foo", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a string, 1) attr(data-b type(<integer>), 'foo')", lu.toString());

		assertMatch(Match.PENDING, lu, "<string>+");
		assertMatch(Match.PENDING, lu, "<integer>+");
		assertMatch(Match.FALSE, lu, "<integer>#");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<string>#");
		assertMatch(Match.FALSE, lu, "<string># | <integer>#");
		assertMatch(Match.PENDING, lu, "<string>+ | <integer>#");
		assertMatch(Match.PENDING, lu, "<string># | <integer>+");
		assertMatch(Match.PENDING, lu, "<string>+ | <integer>+");
		assertMatch(Match.PENDING, lu, "<string>+ | <integer>");
		assertMatch(Match.PENDING, lu, "<string> | <integer>+");
		assertMatch(Match.FALSE, lu, "<string> | <integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrLengthPercentageFallbackWSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<length>), 4%) attr(data-b type(<percentage>), 6px)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("length", param.getParameters().getSyntax().getName());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(4f, param.getFloatValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<percentage>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(6f, param.getFloatValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a type(<length>), 4%) attr(data-b type(<percentage>), 6px)",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<length-percentage>+");
		assertMatch(Match.FALSE, lu, "<length-percentage>#");
		assertMatch(Match.FALSE, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.PENDING, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackVarWSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<integer>), auto) attr(data-b type(<number>), var(--data-b-fb))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("auto", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<number>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit varparam = param.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--data-b-fb", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals(
				"attr(data-a type(<integer>), auto) attr(data-b type(<number>), var(--data-b-fb))",
				lu.toString());

		assertMatch(Match.PENDING, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<custom-ident># | <number>#");
		assertMatch(Match.PENDING, lu, "<custom-ident>+ | <number>+");
		assertMatch(Match.PENDING, lu, "<custom-ident>+ | <number>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, lu, "<color>+");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackVar2WSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a type(<integer>), var(--data-a-fb)) attr(data-b type(<number>), var(--data-b-fb))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<integer>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit varparam = param.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--data-a-fb", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.ATTR, nlu.getLexicalUnitType());
		assertEquals("attr", nlu.getFunctionName());
		param = nlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-b", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<number>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		varparam = param.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--data-b-fb", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals(
				"attr(data-a type(<integer>), var(--data-a-fb)) attr(data-b type(<number>), var(--data-b-fb))",
				lu.toString());

		assertMatch(Match.PENDING, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.PENDING, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<custom-ident># | <number>#");
		assertMatch(Match.PENDING, lu, "<custom-ident>+ | <number>+");
		assertMatch(Match.PENDING, lu, "<custom-ident>+ | <number>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <number>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertMatch(Match.PENDING, lu, "<color>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrUnit() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width type(<length>), 'default')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("attr(data-width type(<length>), 'default')", lu.toString());

		assertMatch(Match.TRUE, lu, "<string> | <length>");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<string> | <length-percentage>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrLengthPercentage() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width type(<length>), 8%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("attr(data-width type(<length>), 8%)", lu.toString());

		assertMatch(Match.TRUE, lu, "<percentage> | <length>");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <length-percentage>");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrURL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-img type(<url>))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-img", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<url>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-img type(<url>))", lu.toString());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <image>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrURLImage() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-img type(<url> | <image>))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-img", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<url> | <image>", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-img type(<url> | <image>))", lu.toString());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <image>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrURLFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-img type(<url>), 'foo.png')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-img", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<url>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("foo.png", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-img type(<url>), 'foo.png')", lu.toString());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <image>");
		assertMatch(Match.FALSE, lu, "<percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrFlex() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-flex type(/* pre1 *//* pre2 */<flex>/* tra1 *//* tra2 */), 2fr)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("attr(data-flex type(<flex>), 2fr)", lu.toString());

		LexicalUnit param = lu.getParameters().getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());

		param = param.getParameters();

		assertEquals(LexicalType.SYNTAX, param.getLexicalUnitType());

		StringList pre = param.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(2, pre.getLength());
		assertEquals(" pre1 ", pre.item(0));
		assertEquals(" pre2 ", pre.item(1));

		StringList after = param.getTrailingComments();
		assertNotNull(after);
		assertEquals(2, after.getLength());
		assertEquals(" tra1 ", after.item(0));
		assertEquals(" tra2 ", after.item(1));

		assertMatch(Match.TRUE, lu, "<flex>");
		assertMatch(Match.TRUE, lu, "<flex>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<string> | <flex>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width var(--data-type), 5%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-width", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit varparam = param.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--data-type", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(5f, param.getFloatValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("attr(data-width var(--data-type), 5%)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>#");
		assertMatch(Match.PENDING, lu, "<string> | <percentage>");
		assertMatch(Match.PENDING, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	/*
	 * This may not work in web browsers.
	 */
	@Test
	public void testParsePropertyValueAttrTypeVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width type(var(--data-syn)), 5%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-width", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		LexicalUnit typeparam = param.getParameters();
		assertEquals(LexicalType.VAR, typeparam.getLexicalUnitType());
		LexicalUnit varparam = typeparam.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--data-syn", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(typeparam.getNextLexicalUnit());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(5f, param.getFloatValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("attr(data-width type(var(--data-syn)), 5%)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>#");
		assertMatch(Match.PENDING, lu, "<string> | <percentage>");
		assertMatch(Match.PENDING, lu, "<color>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueAttrVarFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width type(<length>), var(--data-width))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-width", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.TYPE_FUNCTION, param.getLexicalUnitType());
		assertEquals("<length>", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit varparam = param.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--data-width", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());

		assertEquals("attr(data-width type(<length>), var(--data-width))", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "<string> | <length>");
		assertMatch(Match.PENDING, lu, "<custom-ident>");
		assertEquals(Match.TRUE, lu.matches(universalSyntax));
	}

	@Test
	public void testParsePropertyValueAttrEmptyError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("attr()"));
		assertEquals(6, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueAttrQuotedError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("attr(\"<percentage>\")"));
		assertEquals(20, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueAttrQuotedTypeError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("attr(data-pcnt type(\"<percentage>\"))"));
		assertEquals(21, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueAttrInvalidTypeError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("attr(data-pcnt type(&percentage>))"));
		assertEquals(21, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueAttrError2() throws CSSException {
		try {
			parsePropertyValue("attr(-)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
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
