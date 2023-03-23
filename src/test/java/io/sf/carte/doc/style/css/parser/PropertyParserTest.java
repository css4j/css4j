/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

public class PropertyParserTest {

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
	public void testParsePropertyInherit() throws CSSException {
		LexicalUnit lu = parsePropertyValue("inherit");
		assertEquals(LexicalType.INHERIT, lu.getLexicalUnitType());
		assertEquals("inherit", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.PENDING, lu.matches(syn));
	}

	@Test
	public void testParsePropertyInitial() throws CSSException {
		LexicalUnit lu = parsePropertyValue("initial");
		assertEquals(LexicalType.INITIAL, lu.getLexicalUnitType());
		assertEquals("initial", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.PENDING, lu.matches(syn));
	}

	@Test
	public void testParsePropertyUnset() throws CSSException {
		LexicalUnit lu = parsePropertyValue("unset");
		assertEquals(LexicalType.UNSET, lu.getLexicalUnitType());
		assertEquals("unset", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.PENDING, lu.matches(syn));
	}

	@Test
	public void testParsePropertyReset() throws CSSException {
		LexicalUnit lu = parsePropertyValue("revert");
		assertEquals(LexicalType.REVERT, lu.getLexicalUnitType());
		assertEquals("revert", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.PENDING, lu.matches(syn));
	}

	@Test
	public void testParsePropertyBad() throws IOException {
		try {
			parsePropertyValue("@");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifier() throws IOException {
		try {
			parsePropertyValue("-9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifier2() throws IOException {
		try {
			parsePropertyValue("9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifierMinus() throws IOException {
		try {
			parsePropertyValue("-");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifierPlus() throws IOException {
		try {
			parsePropertyValue("+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedIdentifier() throws IOException {
		LexicalUnit lu = parsePropertyValue("\\35 px\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9 ", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyIdentifierHighChar() throws IOException {
		LexicalUnit lu = parsePropertyValue("foo\uff08");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo\uff08", lu.getStringValue());
		assertEquals("foo\uff08", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyIdentifierOtherChar() throws IOException {
		LexicalUnit lu = parsePropertyValue("⁑");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("⁑", lu.getStringValue());
		assertEquals("⁑", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyIdentifierSurrogate() throws IOException {
		LexicalUnit lu = parsePropertyValue("🚧");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("🚧", lu.getStringValue());
		assertEquals("🚧", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyIdentifierHighControl() throws IOException {
		LexicalUnit lu = parsePropertyValue("foo\u009e");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo\u009e", lu.getStringValue());
		assertEquals("foo\u009e", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseProperty2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(" Times New Roman ");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
	}

	@Test
	public void testParseProperty3() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("Times New Roman");
		assertEquals(LexicalType.IDENT, lunit.getLexicalUnitType());
		assertEquals("Times", lunit.getStringValue());
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyBadImportant() throws CSSException {
		try {
			parsePropertyValue("calc(100% - 3em !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyRange() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+416");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+416", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(1046, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyRange2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+0025-00FF");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+25-ff", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyRange3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+0025-00FF ");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+25-ff", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyRangeWildcard() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+4??");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+4??", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertEquals("4??", subv.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyRangeList() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("U+022, U+0025-00FF, U+4??, U+FF00");
		assertEquals(LexicalType.UNICODE_RANGE, lunit.getLexicalUnitType());
		assertEquals("U+22, U+25-ff, U+4??, U+ff00", lunit.toString());
		LexicalUnit subv = lunit.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(34, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(65280, subv.getIntegerValue());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyRangeWildcard2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+???");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("???", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<unicode-range>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<unicode-range>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyRangeWildcardBad() throws CSSException {
		try {
			parsePropertyValue("U+030-???");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyRangeWildcardBad2() throws CSSException {
		try {
			parsePropertyValue("U+030-?");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyRangeWildcardBad3() throws CSSException {
		try {
			parsePropertyValue("U+???-250");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyRangeBadWildcard() throws CSSException {
		try {
			parsePropertyValue("U+??????");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscaped() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\1F44D");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1f44d ", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\:foo");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(":foo", lu.getStringValue());
		assertEquals("\\:foo", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("block\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block\t", lu.getStringValue());
		assertEquals("block\\9 ", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("bl\\9 ock");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bl\tock", lu.getStringValue());
		assertEquals("bl\\9 ock", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped5() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-\\9 block");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-\tblock", lu.getStringValue());
		assertEquals("-\\9 block", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped6() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\FFFFFF");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\FFFFFF", lu.getStringValue());
		assertEquals("\\FFFFFF", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped7() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\f435");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\f435", lu.getStringValue()); // Private use character, must be escaped
		assertEquals("\\f435", lu.toString()); // Private use character, must be escaped
	}

	@Test
	public void testParsePropertyEscaped8() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-a\\14c");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-aŌ", lu.getStringValue());
		assertEquals("-a\\14c ", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped9() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-a\\14c  u");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-aŌ", lu.getStringValue());
		assertEquals("u", lu.getNextLexicalUnit().getStringValue());
		assertEquals("-a\\14c ", lu.getCssText());
		assertEquals("-a\\14c  u", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped10() throws CSSException {
		LexicalUnit lu = parsePropertyValue("a\\3d b");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("a=b", lu.getStringValue());
		assertEquals("a\\=b", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedNull() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\0");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\ufffd", lu.getStringValue());
		assertEquals("\\0 ", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedIdentNull() throws CSSException {
		LexicalUnit lu = parsePropertyValue("a\\0");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("a\ufffd", lu.getStringValue());
		assertEquals("a\\0 ", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedIdentNull2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("a\\0 b");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("a\ufffdb", lu.getStringValue());
		assertEquals("a\\0 b", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedNullIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\0 a");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\ufffda", lu.getStringValue());
		assertEquals("\\0 a", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedIdentNullIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("a\\0  b");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("a\ufffd", lu.getStringValue());
		assertEquals("b", lu.getNextLexicalUnit().getStringValue());
		assertEquals("a\\0  b", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedTwoIdents() throws CSSException {
		LexicalUnit lu = parsePropertyValue("a\\3d  b");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("a=", lu.getStringValue());
		assertEquals("a\\=", lu.getCssText());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.IDENT, nlu.getLexicalUnitType());
		assertEquals("b", nlu.getStringValue());
		assertEquals("b", nlu.getCssText());

		assertEquals("a\\= b", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedBackslash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\\\");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\", lu.getStringValue());
		assertEquals("\\\\", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedWS_FirstChar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\ 5");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(" 5", lu.getStringValue());
		assertEquals("\\ 5", lu.toString());
		assertEquals("\\ 5", lu.getCssText());
	}

	@Test
	public void testParsePropertyEscapedWS_Minus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\ -");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(" -", lu.getStringValue());
		assertEquals("\\ -", lu.getCssText());
	}

	@Test
	public void testParsePropertyEscapedWS_MinusDigit() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\ -2");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(" -2", lu.getStringValue());
		assertEquals("\\ -2", lu.toString());
		assertEquals("\\ -2", lu.getCssText());
	}

	@Test
	public void testParsePropertyEscapedMinus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\-");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-", lu.getStringValue());
		assertEquals("\\-", lu.getCssText());
	}

	@Test
	public void testParsePropertyEscapedSeveral() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\31\\ a\\;\\-\\\\");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("1 a;-\\", lu.getStringValue());
		assertEquals("\\31 \\ a\\;-\\\\", lu.getCssText());
	}

	@Test
	public void testParsePropertyEscapedSeveral_WithPlus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\+\\ a\\;\\-\\\\");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("+ a;-\\", lu.getStringValue());
		assertEquals("\\+\\ a\\;-\\\\", lu.getCssText());
	}

	@Test
	public void testParsePropertyEscapedWS_PlusError() throws IOException {
		try {
			parsePropertyValue("\\ +");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedBackslash_PlusError() throws IOException {
		try {
			parsePropertyValue("\\\\+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedPlus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\+");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("+", lu.getStringValue());
		assertEquals("\\+", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedPlusDigit() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\+2");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("+2", lu.getStringValue());
		assertEquals("\\+2", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedColon() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\:");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(":", lu.getStringValue());
		assertEquals("\\:", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedWS() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Awesome\\ 5");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Awesome 5", lu.getStringValue());
		assertEquals("Awesome\\ 5", lu.toString());
	}

	@Test
	public void testParsePropertyBackslahHackError() throws CSSException {
		try {
			parsePropertyValue("600px\\9");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyTab() throws CSSException {
		LexicalUnit lu = parsePropertyValue("larger\t");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedTab() throws CSSException {
		LexicalUnit lu = parsePropertyValue("\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\t", lu.getStringValue());
		assertEquals("\\9 ", lu.toString());
	}

	@Test
	public void testParsePropertyIntegerArg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyIntegerPlusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyMargin() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("0.5em auto");
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lunit.getCssUnit());
		assertEquals(0.5f, lunit.getFloatValue(), 1e-5f);
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals("auto", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <custom-ident>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+ | <custom-ident>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyBorderImage() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(25f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(30f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(12f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(20f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("fill", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, lu.getCssUnit());
		assertEquals(2f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("round", lu.getStringValue());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyQuotedString() throws CSSException {
		LexicalUnit lu = parsePropertyValue("'foo'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyQuotedBackslash() throws CSSException {
		LexicalUnit lu = parsePropertyValue("'\\\\'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\\", lu.getStringValue());
		assertEquals("'\\\\'", lu.toString());
	}

	@Test
	public void testParsePropertyQuotedBackslashBad() throws CSSException {
		try {
			parsePropertyValue("'\\'");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	/*
	 * Common constructs related to IE
	 */

	@Test
	public void testParsePropertyCustomFunctionError() throws CSSException {
		try {
			parsePropertyValue("--my-function(foo=bar)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgidError() throws CSSException {
		try {
			parsePropertyValue(
					"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgid2Error() throws CSSException {
		try {
			parsePropertyValue(
					"progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgid3Error() throws CSSException {
		try {
			parsePropertyValue("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgidEscaped() throws CSSException {
		LexicalUnit lu = parsePropertyValue("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertySquareBrackets() throws CSSException {
		LexicalUnit lu = parsePropertyValue("[header-top]");
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("header-top", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueEmptyError() throws CSSException {
		try {
			parsePropertyValue("");
			fail("Must throw an excption");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueEofError() throws CSSException {
		try {
			parsePropertyValue("yellow;");
			fail("Must throw an excption");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueLengthIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2.1px auto");
		assertEquals(2.1f, lu.getFloatValue(), 1e-5f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length># | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+ | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+ | <custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitEm() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3em");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitEmPlusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1.3em");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<resolution>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitEmMinusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-1.3em");
		assertEquals(-1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals("-1.3em", lu.getCssText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitsUC() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3EX");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("ex", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyUnitsList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2em .85em");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(2f, lu.getFloatValue(), 1e-5f);
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(0.85, nlu.getFloatValue(), 1e-5f);
		assertEquals("em", nlu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, nlu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, nlu.getCssUnit());
		assertSame(lu, nlu.getPreviousLexicalUnit());
		// Clone test
		LexicalUnit clone = nlu.clone();
		assertNull(clone.getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertEquals(nlu, clone);
		//
		clone = lu.clone();
		assertNull(clone.getPreviousLexicalUnit());
		assertEquals(lu, clone);
		assertEquals(nlu, clone.getNextLexicalUnit());
		assertSame(clone, clone.getNextLexicalUnit().getPreviousLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitsNegative() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-0.2em");
		assertEquals(-0.2f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitsNegShort() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-.2em");
		assertEquals(-0.2f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitHz() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3Hz");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_HZ, lu.getCssUnit());
		assertEquals("hz", lu.getDimensionUnitText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<frequency>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <frequency>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <frequency>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitKHz() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3kHz");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_KHZ, lu.getCssUnit());
		assertEquals("khz", lu.getDimensionUnitText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<frequency>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <frequency>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <frequency>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitSecond() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3s");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("s", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lu.getCssUnit());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<time>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<time>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<time>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitSecondMilliSecond() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("1.3s 20ms");
		assertEquals(1.3, lunit.getFloatValue(), 1e-5f);
		assertEquals("s", lunit.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lunit.getCssUnit());
		//
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(20f, lu.getFloatValue(), 1e-5f);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MS, lu.getCssUnit());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<time>+");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<time>#");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<time>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>#");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>+");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitSecondCommaMilliSecond() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("1.3s, 20ms");
		assertEquals(1.3, lunit.getFloatValue(), 1e-5f);
		assertEquals("s", lunit.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lunit.getCssUnit());
		//
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(20f, lu.getFloatValue(), 1e-5f);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MS, lu.getCssUnit());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<time>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<time>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<time>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitMillisecond() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3ms");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MS	, lu.getCssUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<time>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<time>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<time>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <time>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueUnitFlex() throws CSSException {
		LexicalUnit lu = parsePropertyValue("0.7fr");
		assertEquals(0.7f, lu.getFloatValue(), 1e-5f);
		assertEquals("fr", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_FR	, lu.getCssUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <flex>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <flex>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueSquareBrackets() throws CSSException {
		LexicalUnit lu = parsePropertyValue("[header-top] auto [header-bottom]");
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("header-top", lu.getStringValue());
		LexicalUnit next = lu.getNextLexicalUnit();
		assertNotNull(next);
		assertEquals(LexicalType.RIGHT_BRACKET, next.getLexicalUnitType());
		assertTrue(lu.getNextLexicalUnit().getPreviousLexicalUnit() == lu);
	}

	@Test
	public void testParsePropertyZero() throws CSSException {
		LexicalUnit lu = parsePropertyValue("0");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertEquals("0", lu.toString());
		//
		lu = parsePropertyValue(" 0 ");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<frequency>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <integer>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <integer>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	/*
	 * Integers larger than 2147483647 are parsed as floats.
	 */
	@Test
	public void testParsePropertyLongInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("10000000000");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1e10, lu.getFloatValue(), 1e-5f);
		assertEquals("10000000000", lu.toString());
	}

	@Test
	public void testParsePropertyLongReal() throws CSSException {
		LexicalUnit lu = parsePropertyValue("10000000000.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1e10, lu.getFloatValue(), 1e-5f);
		assertEquals("10000000000", lu.toString());
	}

	@Test
	public void testParsePropertyZeroFloat() throws CSSException {
		LexicalUnit lu = parsePropertyValue("0.0");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
	}

	@Test
	public void testParsePropertyZeroEm() throws CSSException {
		LexicalUnit lu = parsePropertyValue("0.0em");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(0f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyOneFloat() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyPlusOneFloat() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
	}

	@Test
	public void testParsePropertyPlusOneFloatError() throws CSSException {
		try {
			parsePropertyValue("++1.0");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyMinusOneFloat() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 1e-5f);
	}

	@Test
	public void testParsePropertyFloatExp_e() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2.345678e-05");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(2.345678e-5, lu.getFloatValue(), 1e-11);
		assertEquals("2.345678E-5", lu.getCssText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatExp_e_plus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2.345678e+8");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(2.345678e+8, lu.getFloatValue(), 10f);
		assertEquals("234567808", lu.getCssText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatExp_E() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-2.345678E-05");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-2.345678e-5, lu.getFloatValue(), 1e-11);
		assertEquals("-2.345678E-5", lu.getCssText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatExp_E_plus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+2.345678E+8");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(2.345678e+8, lu.getFloatValue(), 10f);
		assertEquals("234567808", lu.getCssText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatExpError() throws CSSException {
		try {
			parsePropertyValue("1.0e+ 02");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyFloatExpError2() throws CSSException {
		try {
			parsePropertyValue("+1.0e+ 02");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyFloatExpError3() throws CSSException {
		try {
			parsePropertyValue("-1.0e+ 02");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyFloatList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234 5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-6f);
		//
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.INTEGER, nlu.getLexicalUnitType());
		assertEquals(5, nlu.getIntegerValue());
		//
		assertEquals("0.1234 5", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatList2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234 +5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-5f);
		//
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.INTEGER, nlu.getLexicalUnitType());
		assertEquals(5, nlu.getIntegerValue());
		//
		assertEquals("0.1234 5", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatList3() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234 -5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-5f);
		//
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.INTEGER, nlu.getLexicalUnitType());
		assertEquals(-5, nlu.getIntegerValue());
		//
		assertEquals("0.1234 -5", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyFloatCommaList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234,5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-5f);
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyIntegerIdentBadMatch() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2 auto");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(2, lu.getIntegerValue());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>+ | <custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer> | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number> | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyPercent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyPercentPlusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyPercentNegativeSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());
		assertEquals("-1%", lu.getCssText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyLengthPercentage() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("2px,1%");
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(2f, lunit.getFloatValue(), 1e-5f);
		assertEquals("px", lunit.getDimensionUnitText());
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage># | <length>#");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyPercent2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("0.01%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(0.01f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueString() throws CSSException {
		LexicalUnit lu = parsePropertyValue("'a string'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("a string", lu.getStringValue());
		assertEquals("'a string'", lu.toString());
	}

	@Test
	public void testParsePropertyValueIdentifier() throws CSSException {
		LexicalUnit lu = parsePropertyValue("foo bar");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("foo bar", lu.toString());
		LexicalUnit next = lu.getNextLexicalUnit();
		assertNotNull(next);
		assertEquals(LexicalType.IDENT, next.getLexicalUnitType());
		assertEquals("bar", next.getStringValue());
		assertTrue(lu.getNextLexicalUnit().getPreviousLexicalUnit() == lu);
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueIdentifier2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("foo         bar     ");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("foo bar", lu.toString());
		LexicalUnit next = lu.getNextLexicalUnit();
		assertNotNull(next);
		assertEquals(LexicalType.IDENT, next.getLexicalUnitType());
		assertEquals("bar", next.getStringValue());
		assertTrue(lu.getNextLexicalUnit().getPreviousLexicalUnit() == lu);
	}

	@Test
	public void testParsePropertyValueIdentifierColor() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Yellow");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Yellow", lu.getStringValue());
		assertEquals("Yellow", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <color>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueSpecificIdentifier() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Foo");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Foo", lu.getStringValue());
		assertEquals("Foo", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("Foo");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("Foo+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("Foo#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("foo");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | Foo+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueSpecificIdentifierSequence() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Foo Foo");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Foo", lu.getStringValue());
		assertEquals("Foo Foo", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("Foo");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("Foo+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("Foo#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("foo");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | Foo+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueBadIdentifier() throws CSSException {
		try {
			parsePropertyValue("-9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueBadIdentifier2() throws CSSException {
		try {
			parsePropertyValue("9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueIdentifierTab() throws CSSException {
		LexicalUnit lu = parsePropertyValue("foo\tbar");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueIdentifierNL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("foo\nbar");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueCounters() throws CSSException {
		LexicalUnit lu = parsePropertyValue("counters(section, '.') ' '");
		assertEquals("counters", lu.getFunctionName());
		assertEquals(LexicalType.COUNTERS_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("counters(section, '.') ' '", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCountersError() throws CSSException {
		try {
			parsePropertyValue("counters()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueCounterError() throws CSSException {
		try {
			parsePropertyValue("counter()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100% - 3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM	, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcNegative() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(-3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertEquals("calc(-3em)", lu.toString());
		assertNull(param.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcNumber() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(-2*3.4)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(3.4f, param.getFloatValue(), 1e-5f);
		assertEquals("", param.getDimensionUnitText());
		assertEquals("calc(-2*3.4)", lu.toString());
		assertNull(param.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.TRUE, lu.matches(syn)); // calc() clamps to integer
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalc2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(10em - 2%)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(10em - 2%)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalc3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - 2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 2em)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalc4() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc((10em + 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, subvalues.getCssUnit());
		assertEquals(10f, subvalues.getFloatValue(), 1e-5f);
		assertEquals("em", subvalues.getDimensionUnitText());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.PERCENTAGE, subvalues.getLexicalUnitType());
		assertEquals(2f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((10em + 2%)*3)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalc5() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100%/3 - 2*1em - 2*1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("%", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("px", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc6() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(0ex + max(10em, 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("max", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();
		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalType.DIMENSION, subparams.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, subparams.getCssUnit());
		assertEquals(10f, subparams.getFloatValue(), 1e-5f);
		assertEquals("em", subparams.getDimensionUnitText());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(2f, subparams.getFloatValue(), 1e-5f);
		assertNull(subparams.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(0ex + max(10em, 2%)*3)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalc7() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, subvalues.getCssUnit());
		assertEquals(0.4f, subvalues.getFloatValue(), 1e-5f);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(1em + (0.4vw + 0.25vh)/2)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcVarSubexpression() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc((var(--subexp)) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.VAR, subvalues.getLexicalUnitType());
		assertNull(subvalues.getNextLexicalUnit());
		//
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((var(--subexp))*3)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcNegDenom() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/-2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, subvalues.getCssUnit());
		assertEquals(0.4f, subvalues.getFloatValue(), 1e-5f);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(1em + (0.4vw + 0.25vh)/-2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcNegDenom2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc((75vw*9/16 - 100vh)/-2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, subvalues.getCssUnit());
		assertEquals(75f, subvalues.getFloatValue(), 1e-5f);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.INTEGER, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, subvalues.getCssUnit());
		assertEquals(9, subvalues.getIntegerValue());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_SLASH, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.INTEGER, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, subvalues.getCssUnit());
		assertEquals(16, subvalues.getIntegerValue());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_MINUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(100f, subvalues.getFloatValue(), 1e-5f);
		assertNull(subvalues.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(-2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((75vw*9/16 - 100vh)/-2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcPlusNegValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh + -2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh + -2em)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcPlusZerolessNegValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh + -.2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-0.2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh + -0.2em)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcMinusPosValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - +2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 2em)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcMinusZerolessPosValue() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - +.2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 0.2em)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcInsideCalc() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(calc(2.1 * 3px) - 1pt)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		//
		LexicalUnit subparam = param.getParameters();
		assertNotNull(subparam);
		assertEquals(LexicalType.REAL, subparam.getLexicalUnitType());
		assertEquals(2.1f, subparam.getFloatValue(), 1e-6f);
		subparam = subparam.getNextLexicalUnit();
		assertNotNull(subparam);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertNotNull(subparam);
		assertEquals(LexicalType.DIMENSION, subparam.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, subparam.getCssUnit());
		assertEquals(3f, subparam.getFloatValue(), 1e-6f);
		assertNull(subparam.getNextLexicalUnit());
		//
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-6f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(calc(2.1*3px) - 1pt)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start integer, 1) - 1)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("start integer, 1", param.getParameters().toString());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start integer, 1) - 1)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcAttr2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start length, 8%) - 1.1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		LexicalUnit attrparam = param.getParameters();
		assertNotNull(attrparam);
		assertEquals(LexicalType.IDENT, attrparam.getLexicalUnitType());
		assertEquals("start", attrparam.getStringValue());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.IDENT, attrparam.getLexicalUnitType());
		assertEquals("length", attrparam.getStringValue());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.OPERATOR_COMMA, attrparam.getLexicalUnitType());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.PERCENTAGE, attrparam.getLexicalUnitType());
		assertEquals(8f, attrparam.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(1.1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start length, 8%) - 1.1px)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcAttr3() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start length, 8%) * 2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		LexicalUnit attrparam = param.getParameters();
		assertNotNull(attrparam);
		assertEquals(LexicalType.IDENT, attrparam.getLexicalUnitType());
		assertEquals("start", attrparam.getStringValue());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.IDENT, attrparam.getLexicalUnitType());
		assertEquals("length", attrparam.getStringValue());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.OPERATOR_COMMA, attrparam.getLexicalUnitType());
		attrparam = attrparam.getNextLexicalUnit();
		assertNotNull(attrparam);
		assertEquals(LexicalType.PERCENTAGE, attrparam.getLexicalUnitType());
		assertEquals(8f, attrparam.getFloatValue(), 1e-5f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start length, 8%)*2)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueCalcCustom() throws CSSException {
		LexicalUnit lu = parsePropertyValue("calc(var(--foo, 1%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();
		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalType.IDENT, subparams.getLexicalUnitType());
		assertEquals("--foo", subparams.getStringValue());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(1f, subparams.getFloatValue(), 1e-5f);
		assertNull(subparams.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(var(--foo, 1%)*3)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyBadCalc() throws CSSException {
		try {
			parsePropertyValue("calc(100% - 3em");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc3() throws CSSException {
		try {
			parsePropertyValue("calc(100% -");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc4() throws CSSException {
		try {
			parsePropertyValue("calc(100% -)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc5() throws CSSException {
		try {
			parsePropertyValue("calc(100%-)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc6() throws CSSException {
		try {
			parsePropertyValue("calc(100%+)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc7() throws CSSException {
		try {
			parsePropertyValue("calc(100%-2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc8() throws CSSException {
		try {
			parsePropertyValue("calc(100% -!important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadCalc9() throws CSSException {
		try {
			parsePropertyValue("calc(100% + - 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc10() throws CSSException {
		try {
			parsePropertyValue("calc(100% - + 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc11() throws CSSException {
		try {
			parsePropertyValue("calc(100% + + 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc12() throws CSSException {
		try {
			parsePropertyValue("calc(100% + * 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc13() throws CSSException {
		try {
			parsePropertyValue("calc(100% * + 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc14() throws CSSException {
		try {
			parsePropertyValue("calc(100% * - 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc15() throws CSSException {
		try {
			parsePropertyValue("calc(100% * * 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalcSignedSubexpression() throws CSSException {
		try {
			parsePropertyValue("calc(+(2em * 1))");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalcSignedSubexpression2() throws CSSException {
		try {
			parsePropertyValue("calc(-(2em * 1))");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadExpression() throws CSSException {
		try {
			parsePropertyValue("3em*2");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadExpression2() throws CSSException {
		try {
			parsePropertyValue("calc(1)*2");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadExpression3() throws CSSException {
		try {
			parsePropertyValue("calc(1)+2");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueMax() throws CSSException {
		LexicalUnit lu = parsePropertyValue("max(10em, 2%)");
		assertEquals("max", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("max(10em, 2%)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueClamp() throws CSSException {
		LexicalUnit lu = parsePropertyValue("clamp(10deg, 0.2rad, 25deg)");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("clamp", lu.getFunctionName());

		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_RAD, param.getCssUnit());
		assertEquals(0.2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("clamp(10deg, 0.2rad, 25deg)", lu.toString());

		CSSValueSyntax syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <angle>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueClampVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("clamp(10deg, var(--angle), 25deg)");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("clamp", lu.getFunctionName());

		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("clamp(10deg, var(--angle), 25deg)", lu.toString());

		CSSValueSyntax syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <angle>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueFunctionTrigonometric() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("cos(30deg), tan(45deg)");
		assertEquals(LexicalType.FUNCTION, lunit.getLexicalUnitType());
		assertEquals("cos", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(30f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		//
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("tan", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(45f, param.getFloatValue(), 1e-5f);
		assertEquals("deg", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		//
		assertEquals("cos(30deg), tan(45deg)", lunit.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyValueFunctionTrigonometricInverse() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("acos(.62), atan(0.965)");
		assertEquals(LexicalType.FUNCTION, lunit.getLexicalUnitType());
		assertEquals("acos", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.62f, param.getFloatValue(), 1e-5f);
		assertEquals("", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		//
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("atan", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.965f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		//
		assertEquals("acos(0.62), atan(0.965)", lunit.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<angle>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<angle>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>+");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <angle>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		assertMatch(Match.FALSE, lu, "<transform-list>");
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyValueMaxBad() throws CSSException {
		try {
			parsePropertyValue("max(10em, 2%");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueMaxBad2() throws CSSException {
		try {
			parsePropertyValue("max(10em, 2%!important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(13, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueFunction() throws CSSException {
		LexicalUnit pre = parsePropertyValue("bar foo(0.1, calc((0.5% - 2em)*2.2), 1.0)");
		assertEquals(LexicalType.IDENT, pre.getLexicalUnitType());
		assertEquals("bar", pre.getStringValue());
		LexicalUnit lu = pre.getNextLexicalUnit();
		assertNotNull(lu);
		assertTrue(lu.getPreviousLexicalUnit() == pre);
		assertEquals("foo", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		assertNotNull(param.getNextLexicalUnit());
		assertTrue(param.getNextLexicalUnit().getPreviousLexicalUnit() == param);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		LexicalUnit calcparam = param.getParameters();
		assertNotNull(calcparam);
		assertEquals(LexicalType.SUB_EXPRESSION, calcparam.getLexicalUnitType());
		LexicalUnit calcsub = calcparam.getSubValues();
		assertNotNull(calcsub);
		assertEquals(LexicalType.PERCENTAGE, calcsub.getLexicalUnitType());
		assertEquals(0.5f, calcsub.getFloatValue(), 0.001);
		calcsub = calcsub.getNextLexicalUnit();
		assertNotNull(calcsub);
		assertEquals(LexicalType.OPERATOR_MINUS, calcsub.getLexicalUnitType());
		calcsub = calcsub.getNextLexicalUnit();
		assertNotNull(calcsub);
		assertEquals(LexicalType.DIMENSION, calcsub.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, calcsub.getCssUnit());
		assertEquals(2f, calcsub.getFloatValue(), 0.001);
		assertNull(calcsub.getNextLexicalUnit());
		calcparam = calcparam.getNextLexicalUnit();
		assertNotNull(calcparam);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, calcparam.getLexicalUnitType());
		calcparam = calcparam.getNextLexicalUnit();
		assertNotNull(calcparam);
		assertEquals(LexicalType.REAL, calcparam.getLexicalUnitType());
		assertEquals(2.2f, calcparam.getFloatValue(), 0.001);
		assertNull(calcparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		assertNotNull(param.getNextLexicalUnit());
		assertTrue(param.getNextLexicalUnit().getPreviousLexicalUnit() == param);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.001);
		assertNull(param.getNextLexicalUnit());
		assertEquals("bar foo(0.1, calc((0.5% - 2em)*2.2), 1)", pre.toString());

		assertMatch(Match.FALSE, lu, "<transform-list>");
	}

	@Test
	public void testParsePropertyValueFunctionBezier() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5, 1)");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueFunctionBezierMini() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(.33, .1, .5, 1)");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueFunctionBezierBackslashError() throws CSSException {
		try {
			parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5, 1\\9)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(30, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueFunctionBezierNegativeArg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(-.33, -.1, -1, -1.02)");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-1.02f, param.getFloatValue(), 0.001);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(-0.33, -0.1, -1, -1.02)", lu.toString());

		assertMatch(Match.FALSE, lu, "<transform-list>");
	}

	@Test
	public void testParsePropertyValueFunctionImageSet() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"-webkit-image-set(url(//www.example.com/path/to/img.png) 1x, url(//www2.example.com/path2/to2/img2.png) 2x) foo(bar)");
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("-webkit-image-set", lu.getFunctionName());
		// parameters
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.URI, param.getLexicalUnitType());
		assertEquals("//www.example.com/path/to/img.png", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertEquals("x", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.URI, param.getLexicalUnitType());
		assertEquals("//www2.example.com/path2/to2/img2.png", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		assertEquals("x", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		// Next value
		LexicalUnit nextlu = lu.getNextLexicalUnit();
		assertNotNull(nextlu);
		assertEquals(LexicalType.FUNCTION, nextlu.getLexicalUnitType());
		assertEquals("foo", nextlu.getFunctionName());
		param = nextlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("bar", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nextlu.getNextLexicalUnit());
		assertEquals(
				"-webkit-image-set(url('//www.example.com/path/to/img.png') 1x, url('//www2.example.com/path2/to2/img2.png') 2x) foo(bar)",
				lu.toString());

		assertMatch(Match.FALSE, lu, "<transform-list>");
	}

	@Test
	public void testParsePropertyValueFunctionTransformList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("translate(-10px, -20px) scale(2) rotate(45deg)");
		assertEquals("translate", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<transform-function>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-function>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <transform-list>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <transform-function>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <transform-function>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueFunctionTransformListCS() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"translate(-10px, -20px) scale(2) rotate(45deg), rotate(15deg) scale(2) translate(20px)");
		assertEquals("translate", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<transform-list>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-function>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <transform-list>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <transform-function>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <transform-function>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueFunctionCustom() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-webkit-linear-gradient(transparent, #fff)");
		assertEquals("-webkit-linear-gradient", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", param.getFunctionName());
		param = param.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		assertEquals("-webkit-linear-gradient(transparent, #fff)", lu.toString());

		assertMatch(Match.FALSE, lu, "<transform-list>");
	}

	@Test
	public void testParsePropertyValueFunctionCustomNoWS() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-foo(transparent,green,#fff)");
		assertEquals("-foo", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("green", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", param.getFunctionName());
		param = param.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		assertEquals("-foo(transparent, green, #fff)", lu.toString());
	}

	@Test
	public void testParsePropertyValueFunctionSwitch() throws CSSException {
		LexicalUnit lu = parsePropertyValue("switch(var(--foo); transparent; #fff)");
		assertEquals("switch", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		LexicalUnit varparam = param.getParameters();
		assertNotNull(varparam);
		assertEquals(LexicalType.IDENT, varparam.getLexicalUnitType());
		assertEquals("--foo", varparam.getStringValue());
		assertNull(varparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SEMICOLON, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SEMICOLON, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", param.getFunctionName());
		param = param.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		assertEquals("switch(var(--foo); transparent; #fff)", lu.toString());
	}

	@Test
	public void testParsePropertyBadFunction() throws CSSException {
		try {
			parsePropertyValue("foo(,+)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadFunction2() throws CSSException {
		try {
			parsePropertyValue("foo(2,+)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadFunction3() throws CSSException {
		try {
			parsePropertyValue("foo(2,+3,bar*)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueURL1() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url(imag/image.png)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("imag/image.png", lu.getStringValue());
		assertEquals("url('imag/image.png')", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueURL2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"url(data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
		assertEquals("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')",
			lu.toString());

		lu = parsePropertyValue("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueURLSQ() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
		assertEquals("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueURLDQ() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"url(\"data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/\")");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
		assertEquals("url(\"data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/\")",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueURLSemicolon() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"url(data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
		assertEquals("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueURLSemicolon2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"url(https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,200;0,300;0,400;0,500;0,700;1,400;1,500;1,700&display=swap)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals(
			"https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,200;0,300;0,400;0,500;0,700;1,400;1,500;1,700&display=swap",
			lu.getStringValue());
		assertEquals(
			"url('https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,200;0,300;0,400;0,500;0,700;1,400;1,500;1,700&display=swap')",
			lu.toString());
	}

	@Test
	public void testParsePropertyValueURL_Var() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url(var(--image))");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertNull(lu.getStringValue());
		assertEquals("url(var(--image))", lu.toString());

		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getFunctionName());

		CSSValueSyntax syn = syntaxParser.parseSyntax("<url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueURLEmpty() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url()");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertNull(lu.getStringValue());
		assertEquals("url()", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyBadUrl() throws CSSException {
		try {
			parsePropertyValue(" url(http://www.example.com/");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(29, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadUrl3() throws CSSException {
		try {
			parsePropertyValue("url(");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadUrl4() throws CSSException {
		try {
			parsePropertyValue("url('a' 'b')");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadUrl5() throws CSSException {
		try {
			parsePropertyValue("url(a 'b')");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadUrl6() throws CSSException {
		try {
			parsePropertyValue("url('a' b)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadUrl7() throws CSSException {
		try {
			parsePropertyValue("url(a b)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
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
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
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
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <string>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrPcnt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-count percentage)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-count", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("percentage", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-count percentage)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <percentage>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrInteger() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer) attr(data-b number)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("number", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("attr(data-a integer) attr(data-b number)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrIntegerComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer),attr(data-b number)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("number", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a integer), attr(data-b number)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer, auto),attr(data-b number, none)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("number", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a integer, auto), attr(data-b number, none)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident># | <number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident># | <number>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackWSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer, auto) attr(data-b number, none)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("number", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("none", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a integer, auto) attr(data-b number, none)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident># | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackWSList2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-a string, 1) attr(data-b integer, 'foo')");
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("foo", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		assertEquals("attr(data-a string, 1) attr(data-b integer, 'foo')", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<integer>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string># | <integer>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+ | <integer>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string># | <integer>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+ | <integer>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string>+ | <integer>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <integer>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <integer>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrLengthPercentageFallbackWSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-a length, 4%) attr(data-b percentage, 6px)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("length", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("percentage", param.getStringValue());
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

		assertEquals("attr(data-a length, 4%) attr(data-b percentage, 6px)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackVarWSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a integer, auto) attr(data-b number, var(--data-b-fb))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("number", param.getStringValue());
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

		assertEquals("attr(data-a integer, auto) attr(data-b number, var(--data-b-fb))", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident># | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrIntegerFallbackVar2WSList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a integer, var(--data-a-fb)) attr(data-b number, var(--data-b-fb))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-a", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("integer", param.getStringValue());
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
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("number", param.getStringValue());
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

		assertEquals("attr(data-a integer, var(--data-a-fb)) attr(data-b number, var(--data-b-fb))", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident># | <number>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+ | <number>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <number>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrUnit() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width length, 'default')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("attr(data-width length, 'default')", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrLengthPercentage() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width length, 8%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("attr(data-width length, 8%)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<percentage> | <length>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length-percentage>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrURL() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-img url)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-img", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("url", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-img url)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrURLFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-img url, 'foo.png')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-img", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("url", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("foo.png", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("attr(data-img url, 'foo.png')", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<url>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <url>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrFlex() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-flex flex, 2fr)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("attr(data-flex flex, 2fr)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<flex>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<flex>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <flex>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
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
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <percentage>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrVarFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("attr(data-width length, var(--data-width))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("data-width", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("length", param.getStringValue());
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

		assertEquals("attr(data-width length, var(--data-width))", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueAttrError() throws CSSException {
		try {
			parsePropertyValue("attr()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
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

	@Test
	public void testParsePropertyValueEnv() throws CSSException {
		LexicalUnit lu = parsePropertyValue("env(safe-area-inset-top, 20px)");
		assertEquals("env", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("safe-area-inset-top", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(20f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("env(safe-area-inset-top, 20px)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueVarLengthList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--foo) 12.3px");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--foo", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var", lu.getFunctionName());
		assertEquals("var(--foo) 12.3px", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueLengthVarLengthList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("6pt var(--foo) 12.3px");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals("6pt var(--foo) 12.3px", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueLengthVarLengthListDoubleComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("6pt var(--foo), 12.3px");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals("6pt var(--foo), 12.3px", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueLengthVarLengthLengthList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("6pt var(--foo) 12.3px 2vw");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals("6pt var(--foo) 12.3px 2vw", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueVarElementRef() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--foo, element(#bar))");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--foo", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.ELEMENT_REFERENCE, param.getLexicalUnitType());
		assertEquals("bar", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("var", lu.getFunctionName());
		assertEquals("var(--foo, element(#bar))", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.PENDING, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueProgidError() throws CSSException {
		try {
			parsePropertyValue(
					"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueIEExpressionError() throws CSSException {
		try {
			parsePropertyValue("expression(iequirk = (document.body.scrollTop) + \"px\" )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(20, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueElementReference() throws CSSException {
		LexicalUnit lu = parsePropertyValue("element(#fooid)");
		assertNotNull(lu);
		assertEquals(LexicalType.ELEMENT_REFERENCE, lu.getLexicalUnitType());
		assertEquals("fooid", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("element(#fooid)", lu.toString());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>#");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>+");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, lu.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lu.matches(syn));
	}

	@Test
	public void testParsePropertyValueGradient() throws CSSException {
		LexicalUnit lunit = parsePropertyValue(
				"linear-gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))");
		assertEquals(LexicalType.FUNCTION, lunit.getLexicalUnitType());
		assertEquals("linear-gradient", lunit.getFunctionName());
		assertEquals("linear-gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))", lunit.toString());
		LexicalUnit lu = lunit.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("linear", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("left", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("top", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("left", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bottom", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("from", lu.getFunctionName());
		LexicalUnit params = lu.getParameters();
		assertNotNull(params);
		assertEquals(LexicalType.RGBCOLOR, params.getLexicalUnitType());
		assertNull(params.getNextLexicalUnit());
		params = params.getParameters();
		assertNotNull(params);
		assertEquals(LexicalType.INTEGER, params.getLexicalUnitType());
		assertEquals(189, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalType.INTEGER, params.getLexicalUnitType());
		assertEquals(10, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalType.INTEGER, params.getLexicalUnitType());
		assertEquals(250, params.getIntegerValue());
		assertNull(params.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("to", lu.getFunctionName());
		params = lu.getParameters();
		assertNotNull(params);
		assertEquals(LexicalType.RGBCOLOR, params.getLexicalUnitType());
		assertNull(params.getNextLexicalUnit());
		params = params.getParameters();
		assertNotNull(params);
		assertEquals(LexicalType.INTEGER, params.getLexicalUnitType());
		assertEquals(208, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalType.INTEGER, params.getLexicalUnitType());
		assertEquals(223, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalType.INTEGER, params.getLexicalUnitType());
		assertEquals(159, params.getIntegerValue());
		assertNull(params.getNextLexicalUnit());
		//
		CSSValueSyntax syn = syntaxParser.parseSyntax("<image>");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<image>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<image>+");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>#");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>+");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <image>");
		assertEquals(Match.TRUE, lunit.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyValueMask() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
			"url(https://www.example.com/foo.svg) no-repeat center/1.3128205128ex .8ex");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("https://www.example.com/foo.svg", lu.getStringValue());
		//
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("no-repeat", lu.getStringValue());
		//
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("center", lu.getStringValue());
		//
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		//
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(1.3128205128f, lu.getFloatValue(), 1e-8);
		//
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(0.8f, lu.getFloatValue(), 1e-8);
		//
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePriorityString() throws IOException {
		assertFalse(parser.parsePriority(new StringReader("")));
		assertFalse(parser.parsePriority(new StringReader("foo")));
		assertTrue(parser.parsePriority(new StringReader("important")));
		assertTrue(parser.parsePriority(new StringReader("IMPORTANT")));
		assertTrue(parser.parsePriority(new StringReader("\t important    \n")));
		assertFalse(parser.parsePriority(new StringReader("\t impo  rtant    \n")));
		assertFalse(parser.parsePriority(new StringReader("i mportant")));
		assertFalse(parser.parsePriority(new StringReader("importantt")));
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
