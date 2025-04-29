/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import io.sf.carte.doc.style.css.TransformFunctions;
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

		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "*");
	}

	@Test
	public void testParsePropertyInitial() throws CSSException {
		LexicalUnit lu = parsePropertyValue("initial");
		assertEquals(LexicalType.INITIAL, lu.getLexicalUnitType());
		assertEquals("initial", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "*");
	}

	@Test
	public void testParsePropertyInitialComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue("/* pre *//* pre2 */initial/* after *//* after2 */");
		assertEquals(LexicalType.INITIAL, lu.getLexicalUnitType());
		assertEquals("initial", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(2, pre.getLength());
		assertEquals(" pre ", pre.item(0));
		assertEquals(" pre2 ", pre.item(1));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(2, after.getLength());
		assertEquals(" after ", after.item(0));
		assertEquals(" after2 ", after.item(1));
	}

	@Test
	public void testParsePropertyUnset() throws CSSException {
		LexicalUnit lu = parsePropertyValue("unset");
		assertEquals(LexicalType.UNSET, lu.getLexicalUnitType());
		assertEquals("unset", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "*");
	}

	@Test
	public void testParsePropertyReset() throws CSSException {
		LexicalUnit lu = parsePropertyValue("revert");
		assertEquals(LexicalType.REVERT, lu.getLexicalUnitType());
		assertEquals("revert", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.PENDING, lu, "*");
	}

	@Test
	public void testParsePropertyBad() {
		try {
			parsePropertyValue("@");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifier() {
		try {
			parsePropertyValue("-9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifier2() {
		try {
			parsePropertyValue("9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifierMinus() {
		try {
			parsePropertyValue("-");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadIdentifierPlus() {
		try {
			parsePropertyValue("+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedIdentifier() {
		LexicalUnit lu = parsePropertyValue("\\35 px\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9 ", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyIdentifierHighChar() {
		LexicalUnit lu = parsePropertyValue("foo\uff08");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo\uff08", lu.getStringValue());
		assertEquals("foo\uff08", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<easing-function>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyIdentifierOtherChar() {
		LexicalUnit lu = parsePropertyValue("⁑");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("⁑", lu.getStringValue());
		assertEquals("⁑", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<color>");
	}

	@Test
	public void testParsePropertyIdentifierSurrogate() {
		LexicalUnit lu = parsePropertyValue("🚧");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("🚧", lu.getStringValue());
		assertEquals("🚧", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	/*
	 * See https://github.com/w3c/csswg-drafts/issues/7129
	 */
	@Test
	public void testParsePropertyIdentifierHighControl() {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("foo\u009e"));
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

		assertMatch(Match.TRUE, lu, "<custom-ident>+");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lunit, "<custom-ident>+");
		assertMatch(Match.FALSE, lunit, "<custom-ident>#");
		assertMatch(Match.FALSE, lunit, "<custom-ident>");
		assertMatch(Match.FALSE, lunit, "<resolution>");
		assertMatch(Match.TRUE, lunit, "*");
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

		assertMatch(Match.TRUE, lu, "<unicode-range>");
		assertMatch(Match.TRUE, lu, "<unicode-range>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
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

		assertMatch(Match.TRUE, lu, "<unicode-range>");
		assertMatch(Match.TRUE, lu, "<unicode-range>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lu, "<unicode-range>");
		assertMatch(Match.TRUE, lu, "<unicode-range>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lu, "<unicode-range>");
		assertMatch(Match.TRUE, lu, "<unicode-range>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyRangeList() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("U+022, U+0025-00FF, U+4?? , U+FF00");
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

		assertMatch(Match.TRUE, lunit, "<unicode-range>#");
		assertMatch(Match.FALSE, lunit, "<unicode-range>");
		assertMatch(Match.FALSE, lunit, "<custom-ident>+");
		assertMatch(Match.FALSE, lunit, "<resolution>");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyRangeWildcard2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+??? ");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("???", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<unicode-range>");
		assertMatch(Match.TRUE, lu, "<unicode-range>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyRangeWildcard2EOF() throws CSSException {
		LexicalUnit lu = parsePropertyValue("U+???");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("???", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<unicode-range>");
		assertMatch(Match.TRUE, lu, "<unicode-range>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFunctionRange() throws CSSException {
		LexicalUnit lu = parsePropertyValue("function(U+0025-00FF) ident");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.UNICODE_RANGE, param.getLexicalUnitType());
		assertEquals("U+25-ff", param.toString());
		LexicalUnit subv = param.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());

		assertMatch(Match.TRUE, param, "<unicode-range>");
		assertMatch(Match.TRUE, param, "<unicode-range>#");
		assertMatch(Match.FALSE, param, "<custom-ident>+");
		assertMatch(Match.FALSE, param, "<resolution>");
		assertMatch(Match.TRUE, param, "*");

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("ident", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
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
	public void testParsePropertyEscapedWS_PlusError() {
		try {
			parsePropertyValue("\\ +");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedBackslash_PlusError() {
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

		assertMatch(Match.TRUE, lu, "<integer>");
		assertMatch(Match.TRUE, lu, "<integer>#");
		assertMatch(Match.TRUE, lu, "<integer>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyIntegerPlusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());

		assertMatch(Match.TRUE, lu, "<integer>");
		assertMatch(Match.TRUE, lu, "<integer>#");
		assertMatch(Match.TRUE, lu, "<integer>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.FALSE, lunit, "<length>");
		assertMatch(Match.FALSE, lunit, "<length>#");
		assertMatch(Match.FALSE, lunit, "<length>+");
		assertMatch(Match.FALSE, lunit, "<custom-ident>");
		assertMatch(Match.FALSE, lunit, "<length> | <custom-ident>");
		assertMatch(Match.FALSE, lunit, "<length>+ | <custom-ident>+");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyBorderImage() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round");
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

		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyQuotedString() throws CSSException {
		LexicalUnit lu = parsePropertyValue("'foo'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());

		assertMatch(Match.TRUE, lu, "<string>");
		assertMatch(Match.TRUE, lu, "<string>#");
		assertMatch(Match.TRUE, lu, "<string>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <string>");
		assertMatch(Match.TRUE, lu, "*");
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
		LexicalUnit lu = parsePropertyValue(
				"progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)",
				lu.getStringValue());
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
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueEofError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("yellow;"));
		assertEquals(7, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueLengthIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2.1px auto");
		assertEquals(2.1f, lu.getFloatValue(), 1e-5f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());

		assertMatch(Match.TRUE, lu, "*");
		assertMatch(Match.FALSE, lu, "<length># | <custom-ident>");
		assertMatch(Match.FALSE, lu, "<length>+ | <custom-ident>");
		assertMatch(Match.FALSE, lu, "<length>+ | <custom-ident>+");
		assertMatch(Match.FALSE, lu, "<length> | <custom-ident>");
		assertMatch(Match.FALSE, lu, "<color>");
	}

	@Test
	public void testParsePropertyValueUnitEm() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3em");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<flex>");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.FALSE, lu, "<angle>");
		assertMatch(Match.FALSE, lu, "<frequency>");
		assertMatch(Match.TRUE, lu, "<string> | <length>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueUnitEmPlusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1.3em");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.TRUE, lu, "<length>#");
		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<flex>");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<resolution>");
		assertMatch(Match.FALSE, lu, "<angle>");
		assertMatch(Match.FALSE, lu, "<frequency>");
		assertMatch(Match.TRUE, lu, "<string> | <length>");
		assertMatch(Match.TRUE, lu, "*");
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

		clone = lu.clone();
		assertNull(clone.getPreviousLexicalUnit());
		assertEquals(lu, clone);
		assertEquals(nlu, clone.getNextLexicalUnit());
		assertSame(clone, clone.getNextLexicalUnit().getPreviousLexicalUnit());

		assertMatch(Match.TRUE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<string> | <length>");
		assertMatch(Match.TRUE, lu, "<string> | <length>+");
		assertMatch(Match.TRUE, lu, "*");

		assertShallowMatch(Match.TRUE, lu, "<length>");
		assertShallowMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyUnitsListComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue("/* pre1 */2em/* after1 */ .85em/* after2 */");
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

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre1 ", pre.item(0));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" after1 ", after.item(0));

		StringList after2 = nlu.getTrailingComments();
		assertNotNull(after2);
		assertEquals(1, after2.getLength());
		assertEquals(" after2 ", after2.item(0));
	}

	@Test
	public void testParsePropertyUnitsListComments2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"/* pre1 */2em/* after1 */ /* pre2 */ .85em /* after2 */");
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

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre1 ", pre.item(0));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" after1 ", after.item(0));

		StringList pre2 = nlu.getPrecedingComments();
		assertNotNull(pre2);
		assertEquals(1, pre2.getLength());
		assertEquals(" pre2 ", pre2.item(0));

		StringList after2 = nlu.getTrailingComments();
		assertNotNull(after2);
		assertEquals(1, after2.getLength());
		assertEquals(" after2 ", after2.item(0));
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

		assertMatch(Match.TRUE, lu, "<frequency>+");
		assertMatch(Match.TRUE, lu, "<frequency>#");
		assertMatch(Match.TRUE, lu, "<frequency>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <frequency>");
		assertMatch(Match.TRUE, lu, "<string> | <frequency>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueUnitKHz() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3kHz");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_KHZ, lu.getCssUnit());
		assertEquals("khz", lu.getDimensionUnitText());

		assertMatch(Match.TRUE, lu, "<frequency>+");
		assertMatch(Match.TRUE, lu, "<frequency>#");
		assertMatch(Match.TRUE, lu, "<frequency>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <frequency>");
		assertMatch(Match.TRUE, lu, "<string> | <frequency>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueUnitSecond() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3s");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("s", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lu.getCssUnit());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<time>+");
		assertMatch(Match.TRUE, lu, "<time>#");
		assertMatch(Match.TRUE, lu, "<time>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <time>");
		assertMatch(Match.TRUE, lu, "<string> | <time>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueUnitSecondMilliSecond() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("1.3s 20ms");
		assertEquals(1.3, lunit.getFloatValue(), 1e-5f);
		assertEquals("s", lunit.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lunit.getCssUnit());

		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(20f, lu.getFloatValue(), 1e-5f);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MS, lu.getCssUnit());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lunit, "<time>+");
		assertMatch(Match.FALSE, lunit, "<time>#");
		assertMatch(Match.FALSE, lunit, "<time>");
		assertMatch(Match.FALSE, lunit, "<color>");
		assertMatch(Match.FALSE, lunit, "<string> | <time>");
		assertMatch(Match.FALSE, lunit, "<string> | <time>#");
		assertMatch(Match.TRUE, lunit, "<string> | <time>+");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyValueUnitSecondCommaMilliSecond() throws CSSException {
		LexicalUnit lunit = parsePropertyValue("1.3s, 20ms");
		assertEquals(1.3, lunit.getFloatValue(), 1e-5f);
		assertEquals("s", lunit.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lunit.getCssUnit());

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

		assertMatch(Match.TRUE, lunit, "<time>#");
		assertMatch(Match.FALSE, lunit, "<time>+");
		assertMatch(Match.FALSE, lunit, "<time>");
		assertMatch(Match.FALSE, lunit, "<color>");
		assertMatch(Match.FALSE, lunit, "<string> | <time>");
		assertMatch(Match.FALSE, lunit, "<string> | <time>+");
		assertMatch(Match.TRUE, lunit, "<string> | <time>#");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyValueUnitMillisecond() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1.3ms");
		assertEquals(1.3f, lu.getFloatValue(), 1e-5f);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MS, lu.getCssUnit());

		assertMatch(Match.TRUE, lu, "<time>+");
		assertMatch(Match.TRUE, lu, "<time>#");
		assertMatch(Match.TRUE, lu, "<time>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <time>");
		assertMatch(Match.TRUE, lu, "<string> | <time>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueUnitFlex() throws CSSException {
		LexicalUnit lu = parsePropertyValue("0.7fr");
		assertEquals(0.7f, lu.getFloatValue(), 1e-5f);
		assertEquals("fr", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_FR, lu.getCssUnit());

		assertMatch(Match.TRUE, lu, "<flex>");
		assertMatch(Match.TRUE, lu, "<flex>#");
		assertMatch(Match.TRUE, lu, "<flex>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <flex>");
		assertMatch(Match.TRUE, lu, "<string> | <flex>+");
		assertMatch(Match.TRUE, lu, "*");
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

		lu = parsePropertyValue(" 0 ");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());

		assertMatch(Match.TRUE, lu, "<integer>");
		assertMatch(Match.TRUE, lu, "<integer>#");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<integer>+");
		assertMatch(Match.FALSE, lu, "<frequency>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <integer>");
		assertMatch(Match.TRUE, lu, "<string> | <integer>+");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFloatExp_e_plus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2.345678e+8");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(2.345678e+8, lu.getFloatValue(), 10f);
		assertEquals("234567808", lu.getCssText());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFloatExp_E() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-2.345678E-05");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-2.345678e-5, lu.getFloatValue(), 1e-11);
		assertEquals("-2.345678E-5", lu.getCssText());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFloatExp_E_plus() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+2.345678E+8");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(2.345678e+8, lu.getFloatValue(), 10f);
		assertEquals("234567808", lu.getCssText());

		assertMatch(Match.TRUE, lu, "<number>");
		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.TRUE, lu, "*");
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

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.INTEGER, nlu.getLexicalUnitType());
		assertEquals(5, nlu.getIntegerValue());

		assertEquals("0.1234 5", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.FALSE, lu, "<string> | <number>#");
		assertMatch(Match.FALSE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFloatList2() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234 +5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-5f);

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.INTEGER, nlu.getLexicalUnitType());
		assertEquals(5, nlu.getIntegerValue());

		assertEquals("0.1234 5", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.FALSE, lu, "<string> | <number>#");
		assertMatch(Match.FALSE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFloatList3() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234 -5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-5f);

		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.INTEGER, nlu.getLexicalUnitType());
		assertEquals(-5, nlu.getIntegerValue());

		assertEquals("0.1234 -5", lu.toString());

		assertMatch(Match.TRUE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>+");
		assertMatch(Match.FALSE, lu, "<string> | <number>#");
		assertMatch(Match.FALSE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyFloatCommaList() throws CSSException {
		LexicalUnit lu = parsePropertyValue(".1234,5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-5f);

		assertMatch(Match.TRUE, lu, "<number>#");
		assertMatch(Match.FALSE, lu, "<number>+");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <number>#");
		assertMatch(Match.FALSE, lu, "<string> | <number>+");
		assertMatch(Match.FALSE, lu, "<string> | <number>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyIntegerIdentBadMatch() throws CSSException {
		LexicalUnit lu = parsePropertyValue("2 auto");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(2, lu.getIntegerValue());

		assertMatch(Match.FALSE, lu, "<custom-ident> | <number>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<integer>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<string> | <number>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <integer>");
		assertMatch(Match.FALSE, lu, "<integer>+ | <custom-ident>+");
		assertMatch(Match.FALSE, lu, "<integer> | <custom-ident>");
		assertMatch(Match.FALSE, lu, "<number> | <custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		assertShallowMatch(Match.TRUE, lu, "<integer>");
	}

	@Test
	public void testParsePropertyPercent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());

		assertMatch(Match.TRUE, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <percentage>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyPercentComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue("/* pre */1%/* after */");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre ", pre.item(0));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" after ", after.item(0));
	}

	@Test
	public void testParsePropertyPercentPlusSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("+1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());

		assertMatch(Match.TRUE, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <percentage>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyPercentNegativeSign() throws CSSException {
		LexicalUnit lu = parsePropertyValue("-1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 1e-5f);
		assertEquals("%", lu.getDimensionUnitText());
		assertEquals("-1%", lu.getCssText());

		assertMatch(Match.TRUE, lu, "<percentage>");
		assertMatch(Match.TRUE, lu, "<percentage>#");
		assertMatch(Match.TRUE, lu, "<percentage>+");
		assertMatch(Match.TRUE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <length-percentage>");
		assertMatch(Match.TRUE, lu, "<string> | <percentage>+");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lunit, "<length-percentage>#");
		assertMatch(Match.FALSE, lunit, "<percentage># | <length>#");
		assertMatch(Match.FALSE, lunit, "<length-percentage>+");
		assertMatch(Match.FALSE, lunit, "<length-percentage>");
		assertMatch(Match.FALSE, lunit, "<color>");
		assertMatch(Match.TRUE, lunit, "*");

		assertShallowMatch(Match.TRUE, lunit, "<length>");
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

		assertMatch(Match.TRUE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<string> | <custom-ident>#");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>+");
		assertMatch(Match.FALSE, lu, "<string> | <custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.TRUE, lu, "<custom-ident>+");
		assertMatch(Match.TRUE, lu, "<custom-ident>#");
		assertMatch(Match.TRUE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.TRUE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<color>+");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>#");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>+");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>");
		assertMatch(Match.TRUE, lu, "<string> | <color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueSpecificIdentifier() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Foo");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Foo", lu.getStringValue());
		assertEquals("Foo", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<custom-ident>+");
		assertMatch(Match.TRUE, lu, "<custom-ident>#");
		assertMatch(Match.TRUE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "Foo");
		assertMatch(Match.TRUE, lu, "Foo+");
		assertMatch(Match.TRUE, lu, "Foo#");
		assertMatch(Match.FALSE, lu, "foo");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>#");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>+");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>");
		assertMatch(Match.TRUE, lu, "<string> | Foo+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueSpecificIdentifierSequence() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Foo Foo");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Foo", lu.getStringValue());
		assertEquals("Foo Foo", lu.toString());

		assertMatch(Match.TRUE, lu, "<custom-ident>+");
		assertMatch(Match.FALSE, lu, "<custom-ident>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.FALSE, lu, "Foo");
		assertMatch(Match.TRUE, lu, "Foo+");
		assertMatch(Match.FALSE, lu, "Foo#");
		assertMatch(Match.FALSE, lu, "foo");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<string> | <custom-ident>#");
		assertMatch(Match.TRUE, lu, "<string> | <custom-ident>+");
		assertMatch(Match.FALSE, lu, "<string> | <custom-ident>");
		assertMatch(Match.TRUE, lu, "<string> | Foo+");
		assertMatch(Match.TRUE, lu, "*");
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

		assertTrue(lu.getContextIndex() >= 0);

		assertMatch(Match.FALSE, lu, "<length-percentage>");
		assertMatch(Match.FALSE, lu, "<string>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length-percentage>#");
		assertMatch(Match.TRUE, lu, "*");
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
	public void testParsePropertyValueCircleFunction() throws CSSException {
		LexicalUnit lu = parsePropertyValue("circle(50px)");
		assertEquals("circle", lu.getFunctionName());
		assertEquals(LexicalType.CIRCLE_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(50f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertTrue(lu.getContextIndex() >= 0);

		assertEquals("circle(50px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueCircleFunctionAt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("circle(5vw at right center)");
		assertEquals("circle", lu.getFunctionName());
		assertEquals(LexicalType.CIRCLE_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, param.getCssUnit());
		assertEquals(5f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("at", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("right", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("center", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("circle(5vw at right center)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueCircleFunctionAtComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"/* pre-func */circle(/* pre1 */ 5vw/* after1 */ /* pre2 */ at/* after2 */ /* pre3 */right/* after3 */ center /* after params */)/* after-func */");
		assertEquals("circle", lu.getFunctionName());
		assertEquals(LexicalType.CIRCLE_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VW, param.getCssUnit());
		assertEquals(5f, param.getFloatValue(), 1e-5f);

		StringList ppre = param.getPrecedingComments();
		assertNotNull(ppre);
		assertEquals(1, ppre.getLength());
		assertEquals(" pre1 ", ppre.item(0));

		StringList pafter = param.getTrailingComments();
		assertNotNull(pafter);
		assertEquals(1, pafter.getLength());
		assertEquals(" after1 ", pafter.item(0));

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("at", param.getStringValue());

		StringList ppre2 = param.getPrecedingComments();
		assertNotNull(ppre2);
		assertEquals(1, ppre2.getLength());
		assertEquals(" pre2 ", ppre2.item(0));

		StringList pafter2 = param.getTrailingComments();
		assertNotNull(pafter2);
		assertEquals(1, pafter2.getLength());
		assertEquals(" after2 ", pafter2.item(0));

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("right", param.getStringValue());

		StringList ppre3 = param.getPrecedingComments();
		assertNotNull(ppre3);
		assertEquals(1, ppre3.getLength());
		assertEquals(" pre3 ", ppre3.item(0));

		StringList pafter3 = param.getTrailingComments();
		assertNotNull(pafter3);
		assertEquals(1, pafter3.getLength());
		assertEquals(" after3 ", pafter3.item(0));

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("center", param.getStringValue());

		assertNull(param.getPrecedingComments());

		StringList afterParams = param.getTrailingComments();
		assertNotNull(afterParams);
		assertEquals(1, afterParams.getLength());
		assertEquals(" after params ", afterParams.item(0));

		assertNull(param.getNextLexicalUnit());

		assertEquals("circle(5vw at right center)", lu.toString());

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre-func ", pre.item(0));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" after-func ", after.item(0));
	}

	@Test
	public void testParsePropertyValueEllipseFunctionAt() throws CSSException {
		LexicalUnit lu = parsePropertyValue("ellipse(5em 50% at right center)");
		assertEquals("ellipse", lu.getFunctionName());
		assertEquals(LexicalType.ELLIPSE_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(5f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(50f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("at", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("right", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("center", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertEquals("ellipse(5em 50% at right center)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueInsetFunction() throws CSSException {
		LexicalUnit lu = parsePropertyValue("inset(25px 75px 15px 0 round 90px)");
		assertEquals("inset", lu.getFunctionName());
		assertEquals(LexicalType.INSET_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(25f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(75f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(15f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("round", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(90f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("inset(25px 75px 15px 0 round 90px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePolygonFunction() throws CSSException {
		LexicalUnit lu = parsePropertyValue("polygon(0px 0px, 100px 50px, 0px 100px)");
		assertEquals("polygon", lu.getFunctionName());
		assertEquals(LexicalType.POLYGON_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(50f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(0f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("polygon(0px 0px, 100px 50px, 0px 100px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValuePathFunction() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"path('M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')");
		assertEquals("path", lu.getFunctionName());
		assertEquals(LexicalType.PATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("path('M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')", lu.toString());

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValuePathFunctionFillRule() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"path(evenodd, 'M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')");
		assertEquals("path", lu.getFunctionName());
		assertEquals(LexicalType.PATH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("evenodd", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("path(evenodd, 'M 10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80')",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueShapeFunctionLineTo() throws CSSException {
		LexicalUnit lu = parsePropertyValue("shape(nonzero from 0 0, line to 50em 80px)");
		assertEquals("shape", lu.getFunctionName());
		assertEquals(LexicalType.SHAPE_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("nonzero", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("from", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("line", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("to", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(50f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(80f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("shape(nonzero from 0 0, line to 50em 80px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueXYWHFunction() throws CSSException {
		LexicalUnit lu = parsePropertyValue("xywh(0 2% 5px 6% round 0 3px 4% 7px)");
		assertEquals("xywh", lu.getFunctionName());
		assertEquals(LexicalType.XYWH_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(5f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(6f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("round", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(4f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(7f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());

		assertEquals("xywh(0 2% 5px 6% round 0 3px 4% 7px)", lu.toString());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
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
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
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
		assertEquals(0.5f, calcsub.getFloatValue(), 0.001f);
		calcsub = calcsub.getNextLexicalUnit();
		assertNotNull(calcsub);
		assertEquals(LexicalType.OPERATOR_MINUS, calcsub.getLexicalUnitType());
		calcsub = calcsub.getNextLexicalUnit();
		assertNotNull(calcsub);
		assertEquals(LexicalType.DIMENSION, calcsub.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, calcsub.getCssUnit());
		assertEquals(2f, calcsub.getFloatValue(), 0.001f);
		assertNull(calcsub.getNextLexicalUnit());
		calcparam = calcparam.getNextLexicalUnit();
		assertNotNull(calcparam);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, calcparam.getLexicalUnitType());
		calcparam = calcparam.getNextLexicalUnit();
		assertNotNull(calcparam);
		assertEquals(LexicalType.REAL, calcparam.getLexicalUnitType());
		assertEquals(2.2f, calcparam.getFloatValue(), 0.001f);
		assertNull(calcparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		assertNotNull(param.getNextLexicalUnit());
		assertTrue(param.getNextLexicalUnit().getPreviousLexicalUnit() == param);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.001f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("bar foo(0.1, calc((0.5% - 2em)*2.2), 1)", pre.toString());

		assertMatch(Match.FALSE, lu, "<transform-list>");
	}

	@Test
	public void testParsePropertyValueFunctionNegativeArg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("foo(-.33,-.1,-1,-1.02)");
		assertEquals("foo", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001f);
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
		assertEquals(-1.02f, param.getFloatValue(), 0.001f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("foo(-0.33, -0.1, -1, -1.02)", lu.toString());

		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<number>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
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
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());

		assertTrue(lu.getContextIndex() >= 0);

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.TRUE, lu, "<easing-function>#");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
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
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierBackslashError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5, 1\\9)"));
		assertEquals(30, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueFunctionBezierNegativeArg() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(.33, -.1, 1, -1.02)");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-1.02f, param.getFloatValue(), 0.001f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, -0.1, 1, -1.02)", lu.toString());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierAttr() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"cubic-bezier(attr(data-x1 type(<number>)), attr(data-y1 type(<number>)), attr(data-x2 type(<number>)), attr(data-y2 type(<number>)))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("data-x1", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("data-y1", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("data-x2", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("data-y2", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals(
				"cubic-bezier(attr(data-x1 type(<number>)), attr(data-y1 type(<number>)), attr(data-x2 type(<number>)), attr(data-y2 type(<number>)))",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierVar() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(var(--x1y1-x2y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueFunctionBezierVarCommaY1X2Y2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33 var(--comma-y1-x2y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierVarY1X2Y2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, var(--y1-x2y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierVarCommaX2Y2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, 0.1 var(--comma-x2y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierVarX2Y2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, 0.1, var(--x2y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierVarCommaY2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5 var(--y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionBezierVarY2() throws CSSException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5, var(--y2))");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		assertShallowMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLinear() throws CSSException {
		LexicalUnit lu = parsePropertyValue("linear");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("linear", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.TRUE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		assertShallowMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionLinear() throws CSSException {
		LexicalUnit lu = parsePropertyValue("linear(0, 0.5 25% 75%, 1)");
		assertEquals("linear", lu.getFunctionName());
		assertEquals(LexicalType.LINEAR_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(75f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());

		assertNull(param.getNextLexicalUnit());

		assertTrue(lu.getContextIndex() >= 0);

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.TRUE, lu, "<easing-function>#");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());

		// Test equals
		LexicalUnit otherlu = parsePropertyValue("linear(0, 0.5 25%, 1)");
		assertNotEquals(lu, otherlu);
	}

	@Test
	public void testParsePropertyValueFunctionSteps() throws CSSException {
		LexicalUnit lu = parsePropertyValue("steps(2, end)");
		assertEquals("steps", lu.getFunctionName());
		assertEquals(LexicalType.STEPS_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("end", param.getStringValue());
		assertNull(param.getNextLexicalUnit());

		assertTrue(lu.getContextIndex() >= 0);

		assertMatch(Match.TRUE, lu, "<easing-function>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<custom-ident>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueFunctionImageSet() throws CSSException {
		final LexicalUnit lu = parsePropertyValue(
				"image-set(url(//www.example.com/path/to/img.png) 1x, url(//www2.example.com/path2/to2/img2.png) 2x)");
		assertNotNull(lu);
		assertEquals(LexicalType.IMAGE_SET, lu.getLexicalUnitType());
		assertEquals("image-set", lu.getFunctionName());

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

		assertTrue(lu.getContextIndex() >= 0);

		assertEquals(
				"image-set(url('//www.example.com/path/to/img.png') 1x, url('//www2.example.com/path2/to2/img2.png') 2x)",
				lu.toString());

		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionPrefixedImageSet() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"-webkit-image-set(url(//www.example.com/path/to/img.png) 1x, url(//www2.example.com/path2/to2/img2.png) 2x) foo(bar)");
		assertNotNull(lu);
		assertEquals(LexicalType.PREFIXED_FUNCTION, lu.getLexicalUnitType());
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

		assertMatch(Match.FALSE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "*");

		assertShallowMatch(Match.FALSE, lu, "<image>");
	}

	@Test
	public void testParsePropertyValueFunctionTransformList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("translate(-10px, -20px) scale(2) rotate(45deg)");
		assertEquals("translate", lu.getFunctionName());
		assertEquals(LexicalType.TRANSFORM_FUNCTION, lu.getLexicalUnitType());
		assertEquals(TransformFunctions.TRANSLATE, lu.getTransformFunction());

		assertMatch(Match.TRUE, lu, "<transform-function>+");
		assertMatch(Match.TRUE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<transform-function>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <transform-list>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <transform-function>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <transform-function>");
		assertMatch(Match.TRUE, lu, "*");

		assertShallowMatch(Match.TRUE, lu, "<transform-function>");

		LexicalUnit clone = lu.shallowClone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueFunctionTransformListCS() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"translate(-10px, -20px) scale(2) rotate(45deg), rotate(15deg) scale(2) translate(20px)");
		assertEquals("translate", lu.getFunctionName());
		assertEquals(LexicalType.TRANSFORM_FUNCTION, lu.getLexicalUnitType());
		assertEquals(TransformFunctions.TRANSLATE, lu.getTransformFunction());

		assertTrue(lu.getContextIndex() >= 0);

		assertMatch(Match.TRUE, lu, "<transform-list>#");
		assertMatch(Match.FALSE, lu, "<transform-list>");
		assertMatch(Match.FALSE, lu, "<transform-function>");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <transform-list>#");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <transform-function>#");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <transform-function>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueFunctionCustom() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"-webkit-linear-gradient(transparent /* zero alpha */,/* white */ #fff)");
		assertEquals("-webkit-linear-gradient", lu.getFunctionName());
		assertEquals(LexicalType.PREFIXED_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());
		StringList after = param.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" zero alpha ", after.item(0));

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", param.getFunctionName());

		StringList pre = param.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" white ", pre.item(0));

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
		assertEquals(LexicalType.PREFIXED_FUNCTION, lu.getLexicalUnitType());
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
		assertThrows(CSSParseException.class, () -> parsePropertyValue("foo(,+)"));
	}

	@Test
	public void testParsePropertyBadFunction2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("foo(2,+)"));
	}

	@Test
	public void testParsePropertyBadFunction3() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("foo(2,+3,bar*)"));
	}

	@Test
	public void testParsePropertyValueURL1() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url(imag/image.png)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("imag/image.png", lu.getStringValue());
		assertEquals("url('imag/image.png')", lu.toString());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>");
		assertMatch(Match.TRUE, lu, "*");
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
	public void testParsePropertyValueURL_Quoteless() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url(https://www.example.com/foo.png)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("https://www.example.com/foo.png", lu.getStringValue());
		assertEquals("url('https://www.example.com/foo.png')", lu.toString());

		assertNull(lu.getParameters());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyUrlModifierIdent() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url('a' b)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("a", lu.getStringValue());
		assertEquals("url('a' b)", lu.toString());
	}

	@Test
	public void testParsePropertyUrlModifierFunction() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url('a' format('b'))");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("a", lu.getStringValue());
		assertEquals("url('a' format('b'))", lu.toString());
	}

	@Test
	public void testParsePropertyUrlModifierFunctionComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"/* pre-url */url(/* pre-str */ 'a' /* ignored */ format('b') /* ignored 2 */)/* after-url *//* after-url2 */ ident /* after-ident */");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("a", lu.getStringValue());
		assertEquals("url('a' format('b')) ident", lu.toString());

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre-url ", pre.item(0));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(2, after.getLength());
		assertEquals(" after-url ", after.item(0));
		assertEquals(" after-url2 ", after.item(1));

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());

		LexicalUnit nextlu = lu.getNextLexicalUnit();
		assertNotNull(nextlu);
		assertEquals(LexicalType.IDENT, nextlu.getLexicalUnitType());
		assertEquals("ident", nextlu.getStringValue());

		StringList afterIdent = nextlu.getTrailingComments();
		assertNotNull(afterIdent);
		assertEquals(1, afterIdent.getLength());
		assertEquals(" after-ident ", afterIdent.item(0));
	}

	@Test
	public void testParsePropertyValueURLComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue("/* pre */ url(imag/image.png) /* after */");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("imag/image.png", lu.getStringValue());
		assertEquals("url('imag/image.png')", lu.toString());

		StringList pre = lu.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre ", pre.item(0));

		StringList after = lu.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" after ", after.item(0));
	}

	@Test
	public void testParsePropertyValueURL_Var() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("url(var(--image))"));
		assertEquals(8, ex.getColumnNumber());
		assertEquals(1, ex.getLineNumber());
	}

	@Test
	public void testParsePropertyValueSRC_String() throws CSSException {
		LexicalUnit lu = parsePropertyValue("Src('https://www.example.com/')");
		assertEquals(LexicalType.SRC, lu.getLexicalUnitType());
		assertEquals("src", lu.getStringValue());
		assertEquals("src('https://www.example.com/')", lu.toString());

		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.STRING, param.getLexicalUnitType());
		assertEquals("https://www.example.com/", param.getStringValue());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueSRC_Var() throws CSSException {
		LexicalUnit lu = parsePropertyValue("src(var(--image))");
		assertEquals(LexicalType.SRC, lu.getLexicalUnitType());
		assertEquals("src", lu.getStringValue());
		assertEquals("src(var(--image))", lu.toString());

		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getFunctionName());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueURLEmpty() throws CSSException {
		LexicalUnit lu = parsePropertyValue("url()");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertNull(lu.getStringValue());
		assertEquals("url()", lu.toString());

		assertMatch(Match.TRUE, lu, "<url>");
		assertMatch(Match.TRUE, lu, "<url>#");
		assertMatch(Match.TRUE, lu, "<url>+");
		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <url>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyBadUrl() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue(" url(http://www.example.com/"));
		assertEquals(29, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadUrl3() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("url("));
		assertEquals(5, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadUrl4() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("url('a' 'b')"));
		assertEquals(9, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadUrl5() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("url(a 'b')"));
		assertEquals(7, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadUrl6() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("url(http'https://www.example.com/')"));
		assertEquals(9, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyBadUrlLegacySyntaxModifier() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("url(a b)"));
	}

	@Test
	public void testParsePropertyValuePath_Var() throws CSSException {
		LexicalUnit lu = parsePropertyValue("path(var(--path))");
		assertEquals(LexicalType.PATH_FUNCTION, lu.getLexicalUnitType());
		assertEquals("path", lu.getStringValue());
		assertEquals("path(var(--path))", lu.toString());

		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("var", param.getFunctionName());

		assertMatch(Match.TRUE, lu, "<basic-shape>");
		assertMatch(Match.TRUE, lu, "<basic-shape>#");
		assertMatch(Match.TRUE, lu, "<basic-shape>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <basic-shape>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueEnv() throws CSSException {
		LexicalUnit lu = parsePropertyValue("env(safe-area-inset-top, 20px)");
		assertEquals("env", lu.getFunctionName());
		assertEquals(LexicalType.ENV, lu.getLexicalUnitType());
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

		assertMatch(Match.TRUE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<image>#");
		assertMatch(Match.FALSE, lu, "<image>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <image>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueVarSlashFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--data-radius, / 10%)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-radius", param.getStringValue());
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
		assertEquals("var(--data-radius,/10%)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>#");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.PENDING, lu, "<color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueVarAsteriskFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--data-factor, * 10)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-factor", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(10, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var(--data-factor,*10)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>#");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.PENDING, lu, "<color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueVarPlusFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--data-add, + 10)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-add", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(10, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var(--data-add, + 10)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>#");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.PENDING, lu, "<color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueVarMinusFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--data-subtract, - 10)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-subtract", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(10, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var(--data-subtract, - 10)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>#");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.PENDING, lu, "<color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueVarEmptyFallback() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(--data-radius,)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-radius", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.EMPTY, param.getLexicalUnitType());
		assertEquals("", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var(--data-radius,)", lu.toString());

		assertMatch(Match.PENDING, lu, "<percentage>");
		assertMatch(Match.PENDING, lu, "<length-percentage>");
		assertMatch(Match.PENDING, lu, "<percentage>#");
		assertMatch(Match.PENDING, lu, "<percentage>+");
		assertMatch(Match.PENDING, lu, "<color>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueVarEmptyFallbackComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(/* pre */--data-radius/* tra */,/* empty */)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-radius", param.getStringValue());

		StringList pre = param.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre ", pre.item(0));

		StringList after = param.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" tra ", after.item(0));

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.EMPTY, param.getLexicalUnitType());
		assertEquals("/* empty */", param.getCssText());

		StringList ecomments = param.getPrecedingComments();
		assertNotNull(ecomments);
		assertEquals(1, ecomments.getLength());
		assertEquals(" empty ", ecomments.item(0));

		assertNull(param.getNextLexicalUnit());

		assertEquals("var(--data-radius,/* empty */)", lu.toString());

	}

	@Test
	public void testParsePropertyValueVarEmptyFallbackWSComments() throws CSSException {
		LexicalUnit lu = parsePropertyValue("var(/* pre */ --data-radius /* tra */, /* empty */)");
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--data-radius", param.getStringValue());

		StringList pre = param.getPrecedingComments();
		assertNotNull(pre);
		assertEquals(1, pre.getLength());
		assertEquals(" pre ", pre.item(0));

		StringList after = param.getTrailingComments();
		assertNotNull(after);
		assertEquals(1, after.getLength());
		assertEquals(" tra ", after.item(0));

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);

		assertEquals(LexicalType.EMPTY, param.getLexicalUnitType());
		assertEquals("/* empty */", param.getCssText());

		StringList ecomments = param.getPrecedingComments();
		assertNotNull(ecomments);
		assertEquals(1, ecomments.getLength());
		assertEquals(" empty ", ecomments.item(0));

		assertNull(param.getNextLexicalUnit());

		assertEquals("var(--data-radius,/* empty */)", lu.toString());

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

		assertMatch(Match.PENDING, lu, "<length>+");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<image>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLengthVarLengthList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("6pt var(--foo) 12.3px");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals("6pt var(--foo) 12.3px", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>+");
		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<image>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLengthVarLengthListDoubleComma() throws CSSException {
		LexicalUnit lu = parsePropertyValue("6pt var(--foo), 12.3px");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals("6pt var(--foo), 12.3px", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<image>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>#");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length>+");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueLengthVarLengthLengthList() throws CSSException {
		LexicalUnit lu = parsePropertyValue("6pt var(--foo) 12.3px 2vw");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals("6pt var(--foo) 12.3px 2vw", lu.toString());

		assertMatch(Match.PENDING, lu, "<length>+");
		assertMatch(Match.FALSE, lu, "<length>#");
		assertMatch(Match.FALSE, lu, "<length>");
		assertMatch(Match.FALSE, lu, "<image>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <length>+");
		assertMatch(Match.FALSE, lu, "<custom-ident> | <length>#");
		assertMatch(Match.TRUE, lu, "*");
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

		assertMatch(Match.PENDING, lu, "<image>");
		assertMatch(Match.PENDING, lu, "<image>#");
		assertMatch(Match.PENDING, lu, "<image>+");
		assertMatch(Match.PENDING, lu, "<custom-ident> | <image>");
		assertMatch(Match.TRUE, lu, "*");
	}

	@Test
	public void testParsePropertyValueVarEmptyError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("var()"));
		assertEquals(5, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueVarNoCustomError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("var(width)"));
		assertEquals(5, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueVarNoPropertyError() throws CSSException {
		assertThrows(CSSParseException.class, () -> parsePropertyValue("var(1px)"));
	}

	@Test
	public void testParsePropertyValueProgidError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parsePropertyValue(
				"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')"));
		assertEquals(7, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueIEExpressionError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parsePropertyValue(
				"expression(iequirk = (document.body.scrollTop) + \"px\" )"));
		assertEquals(20, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueElementReference() throws CSSException {
		LexicalUnit lu = parsePropertyValue("element(#fooid)");
		assertNotNull(lu);
		assertEquals(LexicalType.ELEMENT_REFERENCE, lu.getLexicalUnitType());
		assertEquals("fooid", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("element(#fooid)", lu.toString());

		assertTrue(lu.getContextIndex() >= 0);

		assertMatch(Match.TRUE, lu, "<image>");
		assertMatch(Match.TRUE, lu, "<image>#");
		assertMatch(Match.TRUE, lu, "<image>+");
		assertMatch(Match.FALSE, lu, "<color>");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <image>#");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <image>+");
		assertMatch(Match.TRUE, lu, "<custom-ident> | <image>");
		assertMatch(Match.TRUE, lu, "*");

		LexicalUnit clone = lu.clone();
		assertEquals(lu, clone);
		assertEquals(lu.hashCode(), clone.hashCode());
	}

	@Test
	public void testParsePropertyValueElementReferenceEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("element()"));
		assertEquals(9, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueElementReferenceNoID() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parsePropertyValue("element(#)"));
		assertEquals(10, ex.getColumnNumber());
	}

	@Test
	public void testParsePropertyValueGradient() throws CSSException {
		LexicalUnit lunit = parsePropertyValue(
				"linear-gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))");
		assertEquals(LexicalType.GRADIENT, lunit.getLexicalUnitType());
		assertEquals("linear-gradient", lunit.getFunctionName());
		assertEquals("linear-gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))",
				lunit.toString());
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

		assertTrue(lunit.getContextIndex() >= 0);

		assertMatch(Match.TRUE, lunit, "<image>");
		assertMatch(Match.TRUE, lunit, "<image>#");
		assertMatch(Match.TRUE, lunit, "<image>+");
		assertMatch(Match.FALSE, lunit, "<color>");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <image>#");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <image>+");
		assertMatch(Match.TRUE, lunit, "<custom-ident> | <image>");
		assertMatch(Match.TRUE, lunit, "*");
	}

	@Test
	public void testParsePropertyValueMask() throws CSSException {
		LexicalUnit lu = parsePropertyValue(
				"url(https://www.example.com/foo.svg) no-repeat center/1.3128205128ex .8ex");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("https://www.example.com/foo.svg", lu.getStringValue());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("no-repeat", lu.getStringValue());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("center", lu.getStringValue());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(1.3128205128f, lu.getFloatValue(), 1e-8);

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(0.8f, lu.getFloatValue(), 1e-8);

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

	private void assertShallowMatch(Match match, LexicalUnit lu, String syntax) {
		CSSValueSyntax syn = syntaxParser.parseSyntax(syntax);
		assertEquals(match, lu.shallowMatch(syn));
	}

}
