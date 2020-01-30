/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserTest {

	private static Parser parser;

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
	}

	@Test
	public void testParsePropertyInitial() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("initial");
		assertEquals(LexicalType.INITIAL, lu.getLexicalUnitType());
		assertEquals("initial", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyUnset() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("unset");
		assertEquals(LexicalType.UNSET, lu.getLexicalUnitType());
		assertEquals("unset", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyReset() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("revert");
		assertEquals(LexicalType.REVERT, lu.getLexicalUnitType());
		assertEquals("revert", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyBadHexColor2() throws CSSException, IOException {
		try {
			parsePropertyValue("#");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadHexColor3() throws CSSException, IOException {
		try {
			parsePropertyValue("#x");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor4() throws CSSException, IOException {
		try {
			parsePropertyValue("#,");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor5() throws CSSException, IOException {
		try {
			parsePropertyValue("#:");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor6() throws CSSException, IOException {
		try {
			parsePropertyValue("#@charset");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor7() throws CSSException, IOException {
		try {
			parsePropertyValue(" #-");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor8() throws CSSException, IOException {
		try {
			parsePropertyValue("#_");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor9() throws CSSException, IOException {
		try {
			parsePropertyValue("#.");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor10() throws CSSException, IOException {
		try {
			parsePropertyValue("##");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor11() throws CSSException, IOException {
		try {
			parsePropertyValue("#fff(e)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor12() throws CSSException, IOException {
		try {
			parsePropertyValue("#(e)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor13() throws CSSException, IOException {
		try {
			parsePropertyValue("#:fff");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor14() throws CSSException, IOException {
		try {
			parsePropertyValue("#fff(e)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadHexColor15() throws CSSException, IOException {
		try {
			parsePropertyValue("#foo ");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyBadHexColor16() throws CSSException, IOException {
		try {
			parsePropertyValue("#\\#aaa");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
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
		LexicalUnit lu = parsePropertyValue("Times New Roman");
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
	public void testParsePropertyBadImportant() throws CSSException, IOException {
		try {
			parsePropertyValue("calc(100% - 3em !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadImportant2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(128, 0, 97 !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(16, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadImportant3() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(128, 0, 97 !important)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(16, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadImportant4() throws CSSException, IOException {
		try {
			parsePropertyValue("# !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadImportant5() throws CSSException, IOException {
		try {
			parsePropertyValue("#!important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
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
	}

	@Test
	public void testParsePropertyRangeList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("U+022, U+0025-00FF, U+4??, U+FF00");
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+22, U+25-ff, U+4??, U+ff00", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(34, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
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
	}

	@Test
	public void testParsePropertyMargin() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.5em auto");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(0.5, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals("auto", lu.toString());
	}

	@Test
	public void testParsePropertyBorderColor() throws CSSException, IOException {
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
	public void testParsePropertyBorderImage() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(25, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(30, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(12, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(20, lu.getFloatValue(), 0.01);
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
		assertEquals(2, lu.getFloatValue(), 0.01);
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
	}

	@Test
	public void testParsePropertyQuotedString() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("'foo'");
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
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
	public void testParsePropertyValue() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("yellow");
		assertEquals("yellow", lu.getStringValue());
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		lu = parsePropertyValue("inherit");
		assertEquals(LexicalType.INHERIT, lu.getLexicalUnitType());
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
	public void testParsePropertyValue2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times New Roman");
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueUnits1() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3em");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitsUC() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3EX");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("ex", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyUnitsList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("2em .85em");
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(2f, lu.getFloatValue(), 0.01);
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(0.85, nlu.getFloatValue(), 0.01);
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
	}

	@Test
	public void testParsePropertyValueUnitsNegative() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-0.2em");
		assertEquals(-0.2, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitsNegShort() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-.2em");
		assertEquals(-0.2, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitHz() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3Hz");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_HZ, lu.getCssUnit());
		assertEquals("hz", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueUnitKHz() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3kHz");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_KHZ, lu.getCssUnit());
		assertEquals("khz", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueUnitSecond() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3s");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("s", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_S, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitMillisecond() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3ms");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MS	, lu.getCssUnit());
	}

	@Test
	public void testParsePropertyValueUnitFlex() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.7fr");
		assertEquals(0.7, lu.getFloatValue(), 0.01);
		assertEquals("fr", lu.getDimensionUnitText());
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_FR	, lu.getCssUnit());
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
		lu = parsePropertyValue(" 0 ");
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
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
		assertEquals(0, lu.getFloatValue(), 0.01f);
		assertEquals("em", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParsePropertyMinusOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-1.0");
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParsePropertyPercent() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1%");
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("%", lu.getDimensionUnitText());
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
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM	, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcNegative() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(-3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertEquals("calc(-3em)", lu.toString());
		assertNull(param.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueCalc2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(10em - 2%)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(10f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 0.01);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(10em - 2%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - 2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 0.01);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 0.01);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 2em)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc4() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc((10em + 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, subvalues.getCssUnit());
		assertEquals(10f, subvalues.getFloatValue(), 0.01);
		assertEquals("em", subvalues.getDimensionUnitText());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.PERCENTAGE, subvalues.getLexicalUnitType());
		assertEquals(2f, subvalues.getFloatValue(), 0.01);
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
	}

	@Test
	public void testParsePropertyValueCalc5() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100%/3 - 2*1em - 2*1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01f);
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
		assertEquals(1f, param.getFloatValue(), 0.01);
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
		assertEquals(1f, param.getFloatValue(), 0.01f);
		assertEquals("px", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc6() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(max(10em, 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
		assertEquals(10f, subparams.getFloatValue(), 0.01);
		assertEquals("em", subparams.getDimensionUnitText());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalType.PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(2f, subparams.getFloatValue(), 0.01);
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
	}

	@Test
	public void testParsePropertyValueCalc7() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 0.01);
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
		assertEquals(0.4f, subvalues.getFloatValue(), 0.01);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 0.01);
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
	}

	@Test
	public void testParsePropertyValueCalcNegDenom() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/-2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(1f, param.getFloatValue(), 0.01);
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
		assertEquals(0.4f, subvalues.getFloatValue(), 0.01);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalType.DIMENSION, subvalues.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_VH, subvalues.getCssUnit());
		assertEquals(0.25f, subvalues.getFloatValue(), 0.01);
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
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
	public void testParsePropertyValueCalcAttr() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start integer, 1) - 1)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
	}

	@Test
	public void testParsePropertyValueCalcCustom() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(var(--foo, 1%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
		assertEquals(1f, subparams.getFloatValue(), 0.01);
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
		assertEquals(10f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(2f, param.getFloatValue(), 0.01);
		assertNull(param.getNextLexicalUnit());
		assertEquals("max(10em, 2%)", lu.toString());
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
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
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
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
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
		assertEquals(1f, param.getFloatValue(), 0.01f);
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
		assertEquals(2f, param.getFloatValue(), 0.01f);
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
	public void testParsePropertyValueRGBZero() throws CSSException, IOException {
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
	}

	@Test
	public void testParsePropertyValueRGBZeroSlash() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBA() throws CSSException, IOException {
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
		assertEquals(0.45f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1, 2, 3, 0.45)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAZeroAlpha() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBAPcntAlpha() throws CSSException, IOException {
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
		assertEquals(45f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1, 2, 3, 45%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(1%,2%,3%,0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 1e-4);
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
	public void testParsePropertyValueRGBAVar() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBAVar2() throws CSSException, IOException {
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
		assertEquals(0.9f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba(1, var(--foo), 0.9)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAVar3() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBVarSlash() throws CSSException, IOException {
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
		assertEquals(0.6f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(1 var(--foo)/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBVarSlashPcnt() throws CSSException, IOException {
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
		assertEquals(52f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(1 var(--foo)/52%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashVar() throws CSSException, IOException {
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
	public void testParsePropertyValueRGB() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBReal() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12.6 127.4 48.8)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.6f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(127.4f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.8f, param.getFloatValue(), 1e-7);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12.6 127.4 48.8)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(calc(12) 127 calc(48))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
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
	public void testParsePropertyValueRGBCalcReal() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(calc(12) 127.8 calc(48.3))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(127.8f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.REAL, param.getParameters().getLexicalUnitType());
		assertEquals(48.3f, param.getParameters().getFloatValue(), 1e-7);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(calc(12) 127.8 calc(48.3))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCalcAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(calc(12) 127 calc(48)/calc(80%))");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(48, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(80f, param.getParameters().getFloatValue(), 1e-5);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(calc(12) 127 calc(48)/calc(80%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcnt2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(0 27% 48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 27% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlash() throws CSSException, IOException {
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
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBRealSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12.9 127 48.1 / 0.1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.9f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.1f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-7);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12.9 127 48.1/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBRealSlashZero() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12.9 127 48.1 / 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.9f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.1f, param.getFloatValue(), 1e-7);
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
	public void testParsePropertyValueRGBRealSlashZeroReal() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12.9 127 48.1 / 0.0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(12.9f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(48.1f, param.getFloatValue(), 1e-7);
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
	public void testParsePropertyValueRGBSlashMini() throws CSSException, IOException {
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
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashMini2() throws CSSException, IOException {
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
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashPcnt() throws CSSException, IOException {
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
		assertEquals(82f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/82%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlpha() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBSlashIntAlpha2() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBPcntSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%/0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
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
	public void testParsePropertyValueRGBPcntSlashPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%/8%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(8f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%/8%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBComma() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBCommaPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12%,27%,48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12%, 27%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaPcnt2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(0,27%,48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0, 27%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 8%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(8f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 8%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 0)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
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
	public void testParsePropertyValueRGBAPcntAlphaInt2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
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
	public void testParsePropertyValueRGBAPcntAlphaInt3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 0, 48%, 1)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
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
	public void testParsePropertyValueRGBCommaBad() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12,, 48)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBCommaBad2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12,13,)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBCommaBad3() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(,13,14,15)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12/ 48 0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12 48/0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad3() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12 48,127,0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad4() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12,48 127,0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad5() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12,48,127/0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad6() throws CSSException, IOException {
		try {
			parsePropertyValue("rgba(0, 0, 0 / 0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad7() throws CSSException, IOException {
		try {
			parsePropertyValue("rgba(0, 0, 0, 0, 0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad8() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 0 0 0/0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad9() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 0 0/0 0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad10() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 0 0/0/0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad11() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 0 0//0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad12() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 0 0/0/)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBad13() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 2% 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(1,var(--foo)/0.2)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(1 var(--foo) var(--bar),0.2)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar3() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(1 /var(--foo))");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar4() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(1 3 5 /var(--foo)/)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar5() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(var(--foo)/.8,4)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar6() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(var(--foo)/.8,.4)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar7() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(var(--foo),var(--foo),var(--foo),var(--foo),var(--foo))");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar8() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(var(--foo) var(--foo) var(--foo) var(--foo)/var(--foo))");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBCommaBadAlpha() throws CSSException, IOException {
		try {
			parsePropertyValue("rgba(12,48,127,2)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBCommaBadAlpha2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgba(12,48,127,-1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadAlpha() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12 48 127/2)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadAlpha2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12 48 127/-1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSL() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLComma() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12, 25%, 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12, 25%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLA() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsla(12, 25%, 48%,.2)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsla", lu.getFunctionName());
		assertEquals("hsla(12, 25%, 48%, 0.2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 0.1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 24%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(24f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/24%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 0)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLDeg() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12deg 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12deg 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLDegAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12deg 25% 48%/0.1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12deg 25% 48%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVar() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% var(--foo))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% var(--foo))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVar2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(var(--foo) 25% 30%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(30f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(var(--foo) 25% 30%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVarSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% var(--foo)/0.6)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% var(--foo)/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVarSlash2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(var(--foo) 12% 25%/0.6)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.6, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(var(--foo) 12% 25%/0.6)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLVarSlashInt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% var(--foo)/1)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertEquals("--foo", param.getParameters().getStringValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% var(--foo)/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcHue() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(calc(12) 25% 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.INTEGER, param.getParameters().getLexicalUnitType());
		assertEquals(12, param.getParameters().getIntegerValue());
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(calc(12) 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcSat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 calc(25%) 48%)");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(25f, param.getParameters().getFloatValue(), 1e-7);
		assertNull(param.getParameters().getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 calc(25%) 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcLig() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% calc(48%))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(48f, param.getParameters().getFloatValue(), 1e-7);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% calc(48%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48%/calc(0.9))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.REAL, param.getParameters().getLexicalUnitType());
		assertEquals(0.9f, param.getParameters().getFloatValue(), 1e-7);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/calc(0.9))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCalcAlphaPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48%/calc(90%))");
		assertEquals(LexicalType.HSLCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		assertNotNull(param.getParameters());
		assertEquals(LexicalType.PERCENTAGE, param.getParameters().getLexicalUnitType());
		assertEquals(90f, param.getParameters().getFloatValue(), 1e-7);
		assertNull(param.getParameters().getNextLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/calc(90%))", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLCommaBad() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12,, 48%)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLCommaBad2() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12,13%,)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLCommaBad3() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(,13,14%,15%)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12 48% 0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad2() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12 48%/0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad3() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12 48%,93%,0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad4() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12,48% 94%,0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad5() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12deg,48% 94%,0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad6() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12,48%,91%/0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad7() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12em 48% 91%/0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad8() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12deg 48% 91%//0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad9() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12deg 48% 91%/2%/0.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueHSLBad10() throws CSSException, IOException {
		try {
			parsePropertyValue("hsl(12deg 48% 91%/0.1/)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueColorHex3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("#fd3");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("#fd3", lu.toString());
		assertNull(lu.getNextLexicalUnit());
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
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueColorHex4() throws CSSException, IOException {
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
	public void testParsePropertyValueColorHex6() throws CSSException, IOException {
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
	public void testParsePropertyValueColorHex8() throws CSSException, IOException {
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

	@Test
	public void testParsePropertyValueURL1() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url(imag/image.png)");
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("imag/image.png", lu.getStringValue());
		assertEquals("url('imag/image.png')", lu.toString());
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
	}

	@Test
	public void testParsePropertyValueAttrPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-count %)");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-count %", lu.getStringValue());
		assertEquals("attr(data-count %)", lu.toString());
	}

	@Test
	public void testParsePropertyValueAttrUnit() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-width px, 'default')");
		assertEquals(LexicalType.ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-width px, 'default'", lu.getStringValue());
		assertEquals("attr(data-width px, 'default')", lu.toString());
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
	}

	@Test
	public void testParsePropertyValueGradient() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))");
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("gradient", lu.getFunctionName());
		assertEquals("gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))", lu.toString());
		lu = lu.getParameters();
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
