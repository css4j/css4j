/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

	@BeforeClass
	public static void setUpBeforeClass() {
		syntaxParser = new SyntaxParser();
	}

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParsePropertyInherit() throws CSSException, IOException {
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
	public void testParsePropertyInitial() throws CSSException, IOException {
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
	public void testParsePropertyUnset() throws CSSException, IOException {
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
	public void testParsePropertyReset() throws CSSException, IOException {
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
	public void testParsePropertyBadUrl() throws CSSException, IOException {
		try {
			parsePropertyValue(" url(http://www.example.com/");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(29, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadUrl3() throws CSSException, IOException {
		try {
			parsePropertyValue("url(");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
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
	public void testParsePropertyBadIdentifier3() throws IOException {
		try {
			parsePropertyValue("-");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedIdentifier() throws IOException {
		LexicalUnit lu = parsePropertyValue("\\35 px\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9", lu.toString());
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
	public void testParseProperty2() throws CSSException, IOException {
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
	public void testParseProperty3() throws CSSException, IOException {
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
	public void testParsePropertyBadImportant() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% - 3em !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyRange() throws CSSException, IOException {
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
	public void testParsePropertyRange2() throws CSSException, IOException {
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
	public void testParsePropertyRange3() throws CSSException, IOException {
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
	public void testParsePropertyRangeWildcard() throws CSSException, IOException {
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
	public void testParsePropertyRangeList() throws CSSException, IOException {
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
	public void testParsePropertyRangeWildcard2() throws CSSException, IOException {
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
	public void testParsePropertyRangeWildcardBad() throws CSSException, IOException {
		try {
			parsePropertyValue("U+030-???");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyRangeWildcardBad2() throws CSSException, IOException {
		try {
			parsePropertyValue("U+030-?");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyRangeWildcardBad3() throws CSSException, IOException {
		try {
			parsePropertyValue("U+???-250");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyRangeBadWildcard() throws CSSException, IOException {
		try {
			parsePropertyValue("U+??????");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscaped() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\1F44D");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1F44D", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\:foo");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(":foo", lu.getStringValue());
		assertEquals("\\:foo", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("block\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block\t", lu.getStringValue());
		assertEquals("block\\9", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped4() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("bl\\9 ock");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bl\tock", lu.getStringValue());
		assertEquals("bl\\9 ock", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped5() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-\\9 block");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-\tblock", lu.getStringValue());
		assertEquals("-\\9 block", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped6() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\FFFFFF");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\FFFFFF", lu.getStringValue());
		assertEquals("\\FFFFFF", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped7() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\f435");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\f435", lu.getStringValue()); // Private use character, must be escaped
		assertEquals("\\f435", lu.toString()); // Private use character, must be escaped
	}

	@Test
	public void testParsePropertyEscaped8() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("a\\3d b");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("a=b", lu.getStringValue());
		assertEquals("a\\3d b", lu.toString());
	}

	@Test
	public void testParsePropertyBackslahHackError() throws CSSException, IOException {
		try {
			parsePropertyValue("600px\\9");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyEscapedBackslahHack() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("600px\\9");
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("600px\\9", lu.getStringValue());
	}

	@Test
	public void testParsePropertyEscapedBackslahHack2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("2px 3px\\9");
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("2px 3px\\9", lu.getStringValue());
		assertEquals("2px 3px\\9", lu.toString());
	}

	@Test
	public void testParsePropertyTab() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("larger\t");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedTab() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\9");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\t", lu.getStringValue());
		assertEquals("\\9", lu.toString());
	}

	@Test
	public void testParsePropertyIntegerArg() throws CSSException, IOException {
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
	public void testParsePropertyIntegerPlusSign() throws CSSException, IOException {
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
	public void testParsePropertyMargin() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("0.5em auto");
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lunit.getCssUnit());
		assertEquals(0.5, lunit.getFloatValue(), 1e-5);
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
	public void testParsePropertyBorderImage() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(25, lu.getFloatValue(), 1e-5);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(30, lu.getFloatValue(), 1e-5);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(12, lu.getFloatValue(), 1e-5);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(20, lu.getFloatValue(), 1e-5);
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
		assertEquals(2, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyQuotedString() throws CSSException, IOException {
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
	public void testParsePropertyQuotedBackslash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("'\\\\'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\\", lu.getStringValue());
		assertEquals("'\\\\'", lu.toString());
	}

	@Test
	public void testParsePropertyQuotedBackslashBad() throws CSSException, IOException {
		try {
			parsePropertyValue("'\\'");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyCustomFunctionError() throws CSSException, IOException {
		try {
			parsePropertyValue("--my-function(foo=bar)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyCustomFunction() throws CSSException, IOException {
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
	public void testParsePropertyProgidError() throws CSSException, IOException {
		try {
			parsePropertyValue(
					"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgid() throws CSSException, IOException {
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
	public void testParsePropertyProgid2Error() throws CSSException, IOException {
		try {
			parsePropertyValue(
					"progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgid2() throws CSSException, IOException {
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
	public void testParsePropertyProgid3Error() throws CSSException, IOException {
		try {
			parsePropertyValue("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyProgid3() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("pixelradius=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyProgidEscaped() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertySquareBrackets() throws CSSException, IOException {
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
	public void testParsePropertyValueEmptyError() throws CSSException, IOException {
		try {
			parsePropertyValue("");
			fail("Must throw an excption");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueEofError() throws CSSException, IOException {
		try {
			parsePropertyValue("yellow;");
			fail("Must throw an excption");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueLengthIdent() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("2.1px auto");
		assertEquals(2.1f, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitEm() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3em");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitEmPlusSign() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("+1.3em");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitEmMinusSign() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-1.3em");
		assertEquals(-1.3, lu.getFloatValue(), 1e-5);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals("-1.3em", lu.getCssText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitsUC() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3EX");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
		assertEquals("ex", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyUnitsList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("2em .85em");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(2f, lu.getFloatValue(), 1e-5);
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(0.85, nlu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitsNegative() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-0.2em");
		assertEquals(-0.2, lu.getFloatValue(), 1e-5);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitsNegShort() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-.2em");
		assertEquals(-0.2, lu.getFloatValue(), 1e-5);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitHz() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3Hz");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitKHz() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3kHz");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitSecond() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3s");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitSecondMilliSecond() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1.3s 20ms");
		assertEquals(1.3, lunit.getFloatValue(), 1e-5);
		assertEquals("s", lunit.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lunit.getCssUnit());
		//
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(20, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitSecondCommaMilliSecond() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1.3s, 20ms");
		assertEquals(1.3, lunit.getFloatValue(), 1e-5);
		assertEquals("s", lunit.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lunit.getCssUnit());
		//
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(20, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitMillisecond() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3ms");
		assertEquals(1.3, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueUnitFlex() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.7fr");
		assertEquals(0.7, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueSquareBrackets() throws CSSException, IOException {
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
	public void testParsePropertyZero() throws CSSException, IOException {
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

	@Test
	public void testParsePropertyZeroFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.0");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
	}

	@Test
	public void testParsePropertyZeroEm() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.0em");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(0, lu.getFloatValue(), 1e-5);
		assertEquals("em", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyPlusOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("+1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5);
	}

	@Test
	public void testParsePropertyPlusOneFloatError() throws CSSException, IOException {
		try {
			parsePropertyValue("++1.0");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyMinusOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 1e-5);
	}

	@Test
	public void testParsePropertyFloatExp_e() throws CSSException, IOException {
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
	public void testParsePropertyFloatExp_e_plus() throws CSSException, IOException {
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
	public void testParsePropertyFloatExp_E() throws CSSException, IOException {
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
	public void testParsePropertyFloatExp_E_plus() throws CSSException, IOException {
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
	public void testParsePropertyFloatExpError() throws CSSException, IOException {
		try {
			parsePropertyValue("1.0e+ 02");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyFloatExpError2() throws CSSException, IOException {
		try {
			parsePropertyValue("+1.0e+ 02");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyFloatExpError3() throws CSSException, IOException {
		try {
			parsePropertyValue("-1.0e+ 02");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyFloatList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(".1234 5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-6);
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
	public void testParsePropertyFloatList2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(".1234 +5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-4);
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
	public void testParsePropertyFloatList3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(".1234 -5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-4);
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
	public void testParsePropertyFloatCommaList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(".1234,5");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1234f, lu.getFloatValue(), 1e-4);
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
	public void testParsePropertyIntegerIdentBadMatch() throws CSSException, IOException {
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
	public void testParsePropertyPercent() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyPercentPlusSign() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("+1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyPercentNegativeSign() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyLengthPercentage() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("2px,1%");
		assertEquals(LexicalType.DIMENSION, lunit.getLexicalUnitType());
		assertEquals(2f, lunit.getFloatValue(), 1e-5);
		assertEquals("px", lunit.getDimensionUnitText());
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 1e-5);
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
	public void testParsePropertyPercent2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.01%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(0.01f, lu.getFloatValue(), 1e-5);
		assertEquals("%", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueString() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("'a string'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("a string", lu.getStringValue());
		assertEquals("'a string'", lu.toString());
	}

	@Test
	public void testParsePropertyValueIdentifier() throws CSSException, IOException {
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
	public void testParsePropertyValueIdentifier2() throws CSSException, IOException {
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
	public void testParsePropertyValueIdentifierColor() throws CSSException, IOException {
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
	public void testParsePropertyValueSpecificIdentifier() throws CSSException, IOException {
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
	public void testParsePropertyValueSpecificIdentifierSequence() throws CSSException, IOException {
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
	public void testParsePropertyValueBadIdentifier() throws CSSException, IOException {
		try {
			parsePropertyValue("-9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueBadIdentifier2() throws CSSException, IOException {
		try {
			parsePropertyValue("9foo_bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueIdentifierTab() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo\tbar");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueIdentifierNL() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo\nbar");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueCounters() throws CSSException, IOException {
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
	public void testParsePropertyValueCountersError() throws CSSException, IOException {
		try {
			parsePropertyValue("counters()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueCounterError() throws CSSException, IOException {
		try {
			parsePropertyValue("counter()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100% - 3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM	, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalcNegative() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(-3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-3f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalcNumber() throws CSSException, IOException {
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
		assertEquals(3.4f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalc2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(10em - 2%)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalc3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - 2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalc4() throws CSSException, IOException {
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
		assertEquals(10f, subvalues.getFloatValue(), 1e-5);
		assertEquals("em", subvalues.getDimensionUnitText());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.PERCENTAGE, subvalues.getLexicalUnitType());
		assertEquals(2f, subvalues.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalc5() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100%/3 - 2*1em - 2*1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 1e-5);
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
		assertEquals(1f, param.getFloatValue(), 1e-5);
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
		assertEquals(1f, param.getFloatValue(), 1e-5);
		assertEquals("px", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc6() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(max(10em, 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("max", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();
		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalType.DIMENSION, subparams.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, subparams.getCssUnit());
		assertEquals(10f, subparams.getFloatValue(), 1e-5);
		assertEquals("em", subparams.getDimensionUnitText());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(2f, subparams.getFloatValue(), 1e-5);
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
		assertEquals("calc(max(10em, 2%)*3)", lu.toString());
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
	public void testParsePropertyValueCalc7() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5);
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
		assertEquals(0.4f, subvalues.getFloatValue(), 1e-5);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalcNegDenom() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/-2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 1e-5);
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
		assertEquals(0.4f, subvalues.getFloatValue(), 1e-5);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalcNegDenom2() throws CSSException, IOException {
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
		assertEquals(75f, subvalues.getFloatValue(), 1e-5);
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
		assertEquals(100f, subvalues.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalcInsideCalc() throws CSSException, IOException {
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
		assertEquals(2.1f, subparam.getFloatValue(), 1e-6);
		subparam = subparam.getNextLexicalUnit();
		assertNotNull(subparam);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, subparam.getLexicalUnitType());
		subparam = subparam.getNextLexicalUnit();
		assertNotNull(subparam);
		assertEquals(LexicalType.DIMENSION, subparam.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, subparam.getCssUnit());
		assertEquals(3, subparam.getFloatValue(), 1e-6);
		assertNull(subparam.getNextLexicalUnit());
		//
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, param.getCssUnit());
		assertEquals(1, param.getFloatValue(), 1e-6);
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
	public void testParsePropertyValueCalcAttr() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start integer, 1) - 1)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("start integer, 1", param.getStringValue());
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
	public void testParsePropertyValueCalcAttr2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start length, 8%) - 1.1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("start length, 8%", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(1.1f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueCalcAttr3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start length, 8%) * 2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.ATTR, param.getLexicalUnitType());
		assertEquals("start length, 8%", param.getStringValue());
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
	public void testParsePropertyValueCalcCustom() throws CSSException, IOException {
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
		assertEquals(1f, subparams.getFloatValue(), 1e-5);
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
	public void testParsePropertyBadCalc() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% - 3em");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc3() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% -");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc4() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% -)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc5() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100%-)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc6() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100%+)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc7() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100%-2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc8() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% -!important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadCalc9() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% + - 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadCalc10() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% + * 2em)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueMax() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("max(10em, 2%)");
		assertEquals("max", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 1e-5);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueFunctionTrigonometric() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("cos(30deg), tan(45deg)");
		assertEquals(LexicalType.FUNCTION, lunit.getLexicalUnitType());
		assertEquals("cos", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(30f, param.getFloatValue(), 1e-5);
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
		assertEquals(45f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueFunctionTrigonometricInverse() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("acos(.62), atan(0.965)");
		assertEquals(LexicalType.FUNCTION, lunit.getLexicalUnitType());
		assertEquals("acos", lunit.getFunctionName());
		LexicalUnit param = lunit.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.62f, param.getFloatValue(), 1e-5);
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
		assertEquals(0.965f, param.getFloatValue(), 1e-5);
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
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, lunit.matches(syn));
	}

	@Test
	public void testParsePropertyValueMaxBad() throws CSSException, IOException {
		try {
			parsePropertyValue("max(10em, 2%");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueMaxBad2() throws CSSException, IOException {
		try {
			parsePropertyValue("max(10em, 2%!important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(13, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueFunction() throws CSSException, IOException {
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
	}

	@Test
	public void testParsePropertyValueFunctionBezier() throws CSSException, IOException {
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
	public void testParsePropertyValueFunctionBezierMini() throws CSSException, IOException {
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
	public void testParsePropertyValueFunctionBezierBackslashError() throws CSSException, IOException {
		try {
			parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5, 1\\9)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(30, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueFunctionBezierNegativeArg() throws CSSException, IOException {
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
	}

	@Test
	public void testParsePropertyValueFunctionImageSet() throws CSSException, IOException {
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
		assertEquals(1f, param.getFloatValue(), 1e-5);
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
		assertEquals(2f, param.getFloatValue(), 1e-5);
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
	}

	@Test
	public void testParsePropertyValueFunctionTransformList() throws CSSException, IOException {
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
	public void testParsePropertyValueFunctionTransformListCS() throws CSSException, IOException {
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
	public void testParsePropertyValueFunctionCustom() throws CSSException, IOException {
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
	}

	@Test
	public void testParsePropertyValueFunctionCustomNoWS() throws CSSException, IOException {
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
	public void testParsePropertyValueFunctionSwitch() throws CSSException, IOException {
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
	public void testParsePropertyBadFunction() throws CSSException, IOException {
		try {
			parsePropertyValue("foo(,+)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadFunction2() throws CSSException, IOException {
		try {
			parsePropertyValue("foo(2,+)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadFunction3() throws CSSException, IOException {
		try {
			parsePropertyValue("foo(2,+3,bar*)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueURL1() throws CSSException, IOException {
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
	public void testParsePropertyValueURL2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url(data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
		assertEquals("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')", lu.toString());
		lu = parsePropertyValue("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueAttr() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-count)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-count", lu.getStringValue());
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
	public void testParsePropertyValueAttrFallback() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-count, 'default')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-count, 'default'", lu.getStringValue());
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
	public void testParsePropertyValueAttrPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-count percentage)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-count percentage", lu.getStringValue());
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
	public void testParsePropertyValueAttrInteger() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer) attr(data-b number)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a integer", lu.getStringValue());
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
	public void testParsePropertyValueAttrIntegerComma() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer),attr(data-b number)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a integer", lu.getStringValue());
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
	public void testParsePropertyValueAttrIntegerFallbackComma() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer, auto),attr(data-b number, none)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a integer, auto", lu.getStringValue());
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
	public void testParsePropertyValueAttrIntegerFallbackWSList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-a integer, auto) attr(data-b number, none)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a integer, auto", lu.getStringValue());
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
	public void testParsePropertyValueAttrIntegerFallbackWSList2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-a string, 1) attr(data-b integer, 'foo')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a string, 1", lu.getStringValue());
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
	public void testParsePropertyValueAttrLengthPercentageFallbackWSList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-a length, 4%) attr(data-b percentage, 6px)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a length, 4%", lu.getStringValue());
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
	public void testParsePropertyValueAttrIntegerFallbackVarWSList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a integer, auto) attr(data-b number, var(--data-b-fb))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a integer, auto", lu.getStringValue());
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
	public void testParsePropertyValueAttrIntegerFallbackVar2WSList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(
				"attr(data-a integer, var(--data-a-fb)) attr(data-b number, var(--data-b-fb))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-a integer, var(--data-a-fb)", lu.getStringValue());
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
	public void testParsePropertyValueAttrUnit() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-width length, 'default')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-width length, 'default'", lu.getStringValue());
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
	public void testParsePropertyValueAttrLengthPercentage() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-width length, 8%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-width length, 8%", lu.getStringValue());
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
	public void testParsePropertyValueAttrFlex() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-flex flex, 2fr)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-flex flex, 2fr", lu.getStringValue());
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
	public void testParsePropertyValueAttrVar() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-width var(--data-type), 5%)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-width var(--data-type), 5%", lu.getStringValue());
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
	public void testParsePropertyValueAttrVarFallback() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-width length, var(--data-width))");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-width length, var(--data-width)", lu.getStringValue());
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
	public void testParsePropertyValueAttrError() throws CSSException, IOException {
		try {
			parsePropertyValue("attr()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueAttrError2() throws CSSException, IOException {
		try {
			parsePropertyValue("attr(-)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueEnv() throws CSSException, IOException {
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
		assertEquals(20f, param.getFloatValue(), 1e-5);
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
	public void testParsePropertyValueVarLengthList() throws CSSException, IOException {
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
	public void testParsePropertyValueLengthVarLengthList() throws CSSException, IOException {
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
	public void testParsePropertyValueLengthVarLengthListDoubleComma() throws CSSException, IOException {
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
	public void testParsePropertyValueLengthVarLengthLengthList() throws CSSException, IOException {
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
	public void testParsePropertyValueVarElementRef() throws CSSException, IOException {
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
	public void testParsePropertyValueProgidError() throws CSSException, IOException {
		try {
			parsePropertyValue(
					"progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueProgid() throws CSSException, IOException {
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
	public void testParsePropertyValueIEExpressionError() throws CSSException, IOException {
		try {
			parsePropertyValue("expression(iequirk = (document.body.scrollTop) + \"px\" )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(20, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueIEExpression() throws CSSException, IOException {
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
	public void testParsePropertyValueIEExpressionBackslashError() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		try {
			parsePropertyValue("expression(iequirk = (document.body.scrollTop) + 5px\\9 )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(50, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueIEExpressionCompatError() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		try {
			parsePropertyValue("expression(= (document.body.scrollTop) + \"px\" )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyValueElementReference() throws CSSException, IOException {
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
	public void testParsePropertyValueGradient() throws CSSException, IOException {
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
	public void testParsePriorityString() throws CSSException, IOException {
		assertFalse(parser.parsePriority(new StringReader("")));
		assertFalse(parser.parsePriority(new StringReader("foo")));
		assertTrue(parser.parsePriority(new StringReader("important")));
		assertTrue(parser.parsePriority(new StringReader("IMPORTANT")));
		assertTrue(parser.parsePriority(new StringReader("\t important    \n")));
		assertFalse(parser.parsePriority(new StringReader("\t impo  rtant    \n")));
		assertFalse(parser.parsePriority(new StringReader("i mportant")));
		assertFalse(parser.parsePriority(new StringReader("importantt")));
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(value));
	}

}
