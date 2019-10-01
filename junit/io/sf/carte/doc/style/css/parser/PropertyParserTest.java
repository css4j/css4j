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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;

public class PropertyParserTest {

	private static Parser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
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
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyIdentifierHighChar() throws IOException {
		LexicalUnit lu = parsePropertyValue("foo\uff08");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("foo\uff08", lu.getStringValue());
		assertEquals("foo\uff08", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyIdentifierOtherChar() throws IOException {
		LexicalUnit lu = parsePropertyValue("⁑");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("⁑", lu.getStringValue());
		assertEquals("⁑", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyIdentifierSurrogate() throws IOException {
		LexicalUnit lu = parsePropertyValue("🚧");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("🚧", lu.getStringValue());
		assertEquals("🚧", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseProperty2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(" Times New Roman ");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
	}

	@Test
	public void testParseProperty3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times New Roman");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
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
			parsePropertyValue("# !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadImportant4() throws CSSException, IOException {
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
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+416", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(1046, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyRange2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("U+0025-00FF");
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+25-ff", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyRange3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("U+0025-00FF ");
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+25-ff", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyRangeWildcard() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("U+4??");
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+4??", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyRangeList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("U+022, U+0025-00FF, U+4??, U+FF00");
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+22, U+25-ff, U+4??, U+ff00", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(34, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(65280, subv.getIntegerValue());
	}

	@Test
	public void testParsePropertyRangeWildcard2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("U+???");
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_UNICODE_WILDCARD, subv.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1F44D", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\:foo");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals(":foo", lu.getStringValue());
		assertEquals("\\:foo", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("block\\9");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("block\t", lu.getStringValue());
		assertEquals("block\\9", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped4() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("bl\\9 ock");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("bl\tock", lu.getStringValue());
		assertEquals("bl\\9 ock", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped5() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-\\9 block");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("-\tblock", lu.getStringValue());
		assertEquals("-\\9 block", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped6() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\FFFFFF");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\\FFFFFF", lu.getStringValue());
		assertEquals("\\FFFFFF", lu.toString());
	}

	@Test
	public void testParsePropertyEscaped7() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\f435");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\\f435", lu.getStringValue()); // Private use character, must be escaped
		assertEquals("\\f435", lu.toString()); // Private use character, must be escaped
	}

	@Test
	public void testParsePropertyEscaped8() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("a\\3d b");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("600px\\9", lu.getStringValue());
	}

	@Test
	public void testParsePropertyEscapedBackslahHack2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lu = parsePropertyValue("2px 3px\\9");
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("2px 3px\\9", lu.getStringValue());
		assertEquals("2px 3px\\9", lu.toString());
	}

	@Test
	public void testParsePropertyTab() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("larger\t");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
	}

	@Test
	public void testParsePropertyEscapedTab() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("\\9");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\t", lu.getStringValue());
		assertEquals("\\9", lu.toString());
	}

	@Test
	public void testParsePropertyIntegerArg() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1");
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
	}

	@Test
	public void testParsePropertyMargin() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.5em auto");
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
		assertEquals(0.5, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals("auto", lu.toString());
	}

	@Test
	public void testParsePropertyBorderColor() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("blue #a7f31a green");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(167, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(243, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(26, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("green", lu.getStringValue());
	}

	@Test
	public void testParsePropertyBorderImage() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round");
		assertEquals(LexicalUnit.SAC_URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(25, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(30, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(12, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(20, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("fill", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_POINT, lu.getLexicalUnitType());
		assertEquals(2, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("round", lu.getStringValue());
	}

	@Test
	public void testParsePropertyQuotedString() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("'foo'");
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
	}

	@Test
	public void testParsePropertyQuotedBackslash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("'\\\\'");
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("--my-function", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("startColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("endColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("GradientType=0", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("StartColorStr=#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("pixelradius=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyProgidEscaped() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertySquareBrackets() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("[header-top]");
		assertEquals(LexicalUnit.SAC_LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("header-top", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_RIGHT_BRACKET, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValue() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("yellow");
		assertEquals("yellow", lu.getStringValue());
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		lu = parsePropertyValue("inherit");
		assertEquals(LexicalUnit.SAC_INHERIT, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueUnits1() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3em");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueUnitsUC() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3EX");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("ex", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_EX, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyUnitsList() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("2em .85em");
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
		assertEquals(2f, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(0.85, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueUnitsNegative() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-0.2em");
		assertEquals(-0.2, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueUnitsNegShort() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-.2em");
		assertEquals(-0.2, lu.getFloatValue(), 0.01);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueUnitHz() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3Hz");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals(LexicalUnit.SAC_HERTZ, lu.getLexicalUnitType());
		assertEquals("hz", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueUnitKHz() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3kHz");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals(LexicalUnit.SAC_KILOHERTZ, lu.getLexicalUnitType());
		assertEquals("khz", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueUnitSecond() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3s");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("s", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_SECOND, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueUnitMillisecond() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.3ms");
		assertEquals(1.3, lu.getFloatValue(), 0.01);
		assertEquals("ms", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_MILLISECOND, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueUnitFlex() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.7fr");
		assertEquals(0.7, lu.getFloatValue(), 0.01);
		assertEquals("fr", lu.getDimensionUnitText());
		assertEquals(LexicalUnit.SAC_FR, lu.getLexicalUnitType());
	}

	@Test
	public void testParsePropertyValueSquareBrackets() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("[header-top] auto [header-bottom]");
		assertEquals(LexicalUnit.SAC_LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("header-top", lu.getStringValue());
		LexicalUnit next = lu.getNextLexicalUnit();
		assertNotNull(next);
		assertEquals(LexicalUnit.SAC_RIGHT_BRACKET, next.getLexicalUnitType());
		assertTrue(lu.getNextLexicalUnit().getPreviousLexicalUnit() == lu);
	}

	@Test
	public void testParsePropertyZero() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0");
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertEquals("0", lu.toString());
		lu = parsePropertyValue(" 0 ");
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
	}

	@Test
	public void testParsePropertyZeroFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.0");
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
	}

	@Test
	public void testParsePropertyZeroEm() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.0em");
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
		assertEquals(0, lu.getFloatValue(), 0.01f);
		assertEquals("em", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1.0");
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParsePropertyMinusOneFloat() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("-1.0");
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParsePropertyPercent() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1%");
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("%", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyPercent2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("0.01%");
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(0.01f, lu.getFloatValue(), 1e-5);
		assertEquals("%", lu.getDimensionUnitText());
	}

	@Test
	public void testParsePropertyValueString() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("'a string'");
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("a string", lu.getStringValue());
		assertEquals("'a string'", lu.toString());
	}

	@Test
	public void testParsePropertyValueIdentifier() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo bar");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("foo bar", lu.toString());
		LexicalUnit next = lu.getNextLexicalUnit();
		assertNotNull(next);
		assertEquals(LexicalUnit.SAC_IDENT, next.getLexicalUnitType());
		assertEquals("bar", next.getStringValue());
		assertTrue(lu.getNextLexicalUnit().getPreviousLexicalUnit() == lu);
	}

	@Test
	public void testParsePropertyValueIdentifier2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo         bar     ");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("foo bar", lu.toString());
		LexicalUnit next = lu.getNextLexicalUnit();
		assertNotNull(next);
		assertEquals(LexicalUnit.SAC_IDENT, next.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueIdentifierNL() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo\nbar");
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueCounters() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("counters(section, '.') ' '");
		assertEquals("counters", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_COUNTERS_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_IDENT, param.getLexicalUnitType());
		assertEquals("counters(section, '.') ' '", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100% - 3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcNegative() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(-3em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(-3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertEquals("calc(-3em)", lu.toString());
		assertNull(param.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueCalc2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(10em - 2%)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(10f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 0.01);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(10em - 2%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100vh - 2em)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_VH, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01);
		assertEquals("vh", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 0.01);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100vh - 2em)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc4() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc((10em + 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalUnit.SAC_EM, subvalues.getLexicalUnitType());
		assertEquals(10f, subvalues.getFloatValue(), 0.01);
		assertEquals("em", subvalues.getDimensionUnitText());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalUnit.SAC_OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, subvalues.getLexicalUnitType());
		assertEquals(2f, subvalues.getFloatValue(), 0.01);
		assertNull(subvalues.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc((10em + 2%)*3)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc5() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(100%/3 - 2*1em - 2*1px)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01f);
		assertEquals("%", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PIXEL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.01f);
		assertEquals("px", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100%/3 - 2*1em - 2*1px)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc6() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(max(10em, 2%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_FUNCTION, param.getLexicalUnitType());
		assertEquals("max", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();
		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalUnit.SAC_EM, subparams.getLexicalUnitType());
		assertEquals(10f, subparams.getFloatValue(), 0.01);
		assertEquals("em", subparams.getDimensionUnitText());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(2f, subparams.getFloatValue(), 0.01);
		assertNull(subparams.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(max(10em, 2%)*3)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalc7() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(1em + (0.4vw + 0.25vh)/2)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_SUB_EXPRESSION, param.getLexicalUnitType());
		LexicalUnit subvalues = param.getSubValues();
		// Subexpression
		assertNotNull(subvalues);
		assertEquals(LexicalUnit.SAC_VW, subvalues.getLexicalUnitType());
		assertEquals(0.4f, subvalues.getFloatValue(), 0.01);
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalUnit.SAC_OPERATOR_PLUS, subvalues.getLexicalUnitType());
		subvalues = subvalues.getNextLexicalUnit();
		assertNotNull(subvalues);
		assertEquals(LexicalUnit.SAC_VH, subvalues.getLexicalUnitType());
		assertEquals(0.25f, subvalues.getFloatValue(), 0.01);
		assertNull(subvalues.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(1em + (0.4vw + 0.25vh)/2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcAttr() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(attr(start integer, 1) - 1)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_ATTR, param.getLexicalUnitType());
		assertEquals("start integer, 1", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(attr(start integer, 1) - 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueCalcCustom() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(var(--foo, 1%) * 3)");
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_FUNCTION, param.getLexicalUnitType());
		assertEquals("var", param.getFunctionName());
		LexicalUnit subparams = param.getParameters();
		// Subexpression
		assertNotNull(subparams);
		assertEquals(LexicalUnit.SAC_IDENT, subparams.getLexicalUnitType());
		assertEquals("--foo", subparams.getStringValue());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, subparams.getLexicalUnitType());
		subparams = subparams.getNextLexicalUnit();
		assertNotNull(subparams);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, subparams.getLexicalUnitType());
		assertEquals(1f, subparams.getFloatValue(), 0.01);
		assertNull(subparams.getNextLexicalUnit());
		// End of subvalue checking
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(10f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_IDENT, pre.getLexicalUnitType());
		assertEquals("bar", pre.getStringValue());
		LexicalUnit lu = pre.getNextLexicalUnit();
		assertNotNull(lu);
		assertTrue(lu.getPreviousLexicalUnit() == pre);
		assertEquals("foo", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		assertNotNull(param.getNextLexicalUnit());
		assertTrue(param.getNextLexicalUnit().getPreviousLexicalUnit() == param);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_FUNCTION, param.getLexicalUnitType());
		assertEquals("calc", param.getFunctionName());
		LexicalUnit calcparam = param.getParameters();
		assertNotNull(calcparam);
		assertEquals(LexicalUnit.SAC_SUB_EXPRESSION, calcparam.getLexicalUnitType());
		LexicalUnit calcsub = calcparam.getSubValues();
		assertNotNull(calcsub);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, calcsub.getLexicalUnitType());
		assertEquals(0.5f, calcsub.getFloatValue(), 0.001);
		calcsub = calcsub.getNextLexicalUnit();
		assertNotNull(calcsub);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, calcsub.getLexicalUnitType());
		calcsub = calcsub.getNextLexicalUnit();
		assertNotNull(calcsub);
		assertEquals(LexicalUnit.SAC_EM, calcsub.getLexicalUnitType());
		assertEquals(2f, calcsub.getFloatValue(), 0.001);
		assertNull(calcsub.getNextLexicalUnit());
		calcparam = calcparam.getNextLexicalUnit();
		assertNotNull(calcparam);
		assertEquals(LexicalUnit.SAC_OPERATOR_MULTIPLY, calcparam.getLexicalUnitType());
		calcparam = calcparam.getNextLexicalUnit();
		assertNotNull(calcparam);
		assertEquals(LexicalUnit.SAC_REAL, calcparam.getLexicalUnitType());
		assertEquals(2.2f, calcparam.getFloatValue(), 0.001);
		assertNull(calcparam.getNextLexicalUnit());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		assertNotNull(param.getNextLexicalUnit());
		assertTrue(param.getNextLexicalUnit().getPreviousLexicalUnit() == param);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.001);
		assertNull(param.getNextLexicalUnit());
		assertEquals("bar foo(0.1, calc((0.5% - 2em)*2.2), 1)", pre.toString());
	}

	@Test
	public void testParsePropertyValueFunctionBezier() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(0.33, 0.1, 0.5, 1)");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueFunctionBezierMini() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("cubic-bezier(.33, .1, .5, 1)");
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(-0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(-1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(-1.02f, param.getFloatValue(), 0.001);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(-0.33, -0.1, -1, -1.02)", lu.toString());
	}

	@Test
	public void testParsePropertyValueFunctionImageSet() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue(
				"-webkit-image-set(url(//www.example.com/path/to/img.png) 1x, url(//www2.example.com/path2/to2/img2.png) 2x) foo(bar)");
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("-webkit-image-set", lu.getFunctionName());
		// parameters
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_URI, param.getLexicalUnitType());
		assertEquals("//www.example.com/path/to/img.png", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_DIMENSION, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 0.01f);
		assertEquals("x", param.getDimensionUnitText());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_URI, param.getLexicalUnitType());
		assertEquals("//www2.example.com/path2/to2/img2.png", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_DIMENSION, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 0.01f);
		assertEquals("x", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		// Next value
		LexicalUnit nextlu = lu.getNextLexicalUnit();
		assertNotNull(nextlu);
		assertEquals(LexicalUnit.SAC_FUNCTION, nextlu.getLexicalUnitType());
		assertEquals("foo", nextlu.getFunctionName());
		param = nextlu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_IDENT, param.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_RGBCOLOR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", param.getFunctionName());
		param = param.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 0 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBZeroSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(0 0 0 / 0)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 0 0/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAZeroAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(0,0,0,0)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(0, 0, 0, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(1,2,3,45%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(3, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(45f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1, 2, 3, 45%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(1%,2%,3%,0)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(2f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(1%, 2%, 3%, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGB() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcnt2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(0 27% 48%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0 27% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 0.1)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashMini() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48/.1)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashMini2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48/ .1)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 82%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(82f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/82%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 1)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBSlashIntAlpha2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12 127 48 / 0)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12 127 48/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcntSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%/0)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBPcntSlashPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12% 27% 48%/8%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(8f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12% 27% 48%/8%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBComma() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12, 127, 48)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(127, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(48, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12, 127, 48)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(12%,27%,48%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(12%, 27%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBCommaPcnt2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(0,27%,48%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(0, 27%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 8%)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(8f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 8%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 0)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 27%, 48%, 1)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgba", lu.getFunctionName());
		assertEquals("rgba(12%, 27%, 48%, 1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueRGBAPcntAlphaInt3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgba(12%, 0, 48%, 1)");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
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
			parsePropertyValue("rgb(12 48 0.1)");
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLComma() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12, 25%, 48%)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12, 25%, 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLA() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsla(12, 25%, 48%,.2)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.2f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsla", lu.getFunctionName());
		assertEquals("hsla(12, 25%, 48%, 0.2)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlash() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 0.1)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/0.1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 24%)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(24f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/24%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 1)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/1)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLSlashIntegerAlpha2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12 25% 48% / 0)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(12, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12 25% 48%/0)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLDeg() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12deg 25% 48%)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_DEGREE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12deg 25% 48%)", lu.toString());
	}

	@Test
	public void testParsePropertyValueHSLDegAlpha() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("hsl(12deg 25% 48%/0.1)");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalUnit.SAC_DEGREE, param.getLexicalUnitType());
		assertEquals(12f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(25f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-4);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1, param.getFloatValue(), 1e-4);
		assertNull(param.getNextLexicalUnit());
		assertEquals("hsl", lu.getFunctionName());
		assertEquals("hsl(12deg 25% 48%/0.1)", lu.toString());
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
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("#fd3", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(255, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(221, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(51, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueColorHex4() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("#fd3b");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#fd3b", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(255, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(221, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(51, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(0.7333f, lu.getFloatValue(), 0.0001f);
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueColorHex6() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("#a7f31a");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#a7f31a", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(167, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(243, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(26, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueColorHex8() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("#a7f31af0");
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#a7f31af0", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(167, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(243, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(26, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(0.9412f, lu.getFloatValue(), 0.0001f);
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParsePropertyValueURL1() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url(imag/image.png)");
		assertEquals(LexicalUnit.SAC_URI, lu.getLexicalUnitType());
		assertEquals("imag/image.png", lu.getStringValue());
		assertEquals("url('imag/image.png')", lu.toString());
	}

	@Test
	public void testParsePropertyValueURL2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("url(data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/)");
		assertEquals(LexicalUnit.SAC_URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
		assertEquals("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')", lu.toString());
		lu = parsePropertyValue("url('data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/')");
		assertEquals(LexicalUnit.SAC_URI, lu.getLexicalUnitType());
		assertEquals("data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/", lu.getStringValue());
	}

	@Test
	public void testParsePropertyValueAttr() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-count)");
		assertEquals(LexicalUnit.SAC_ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-count", lu.getStringValue());
		assertEquals("attr(data-count)", lu.toString());
	}

	@Test
	public void testParsePropertyValueAttrPcnt() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-count %)");
		assertEquals(LexicalUnit.SAC_ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-count %", lu.getStringValue());
		assertEquals("attr(data-count %)", lu.toString());
	}

	@Test
	public void testParsePropertyValueAttrUnit() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("attr(data-width px, 'default')");
		assertEquals(LexicalUnit.SAC_ATTR, lu.getLexicalUnitType());
		assertEquals("attr", lu.getFunctionName());
		assertEquals("data-width px, 'default'", lu.getStringValue());
		assertEquals("attr(data-width px, 'default')", lu.toString());
	}

	@Test
	public void testParsePropertyValueAttrError() throws CSSException, IOException {
		try {
			parsePropertyValue("attr(-)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("startColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("endColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("iequirk=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_SUB_EXPRESSION, lu.getLexicalUnitType());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_IDENT, subv.getLexicalUnitType());
		assertEquals("document.body.scrollTop", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_PLUS, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
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
		assertEquals(LexicalUnit.SAC_ELEMENT_REFERENCE, lu.getLexicalUnitType());
		assertEquals("fooid", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("element(#fooid)", lu.toString());
	}

	@Test
	public void testParsePropertyValueGradient() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))");
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("gradient", lu.getFunctionName());
		assertEquals("gradient(linear, left top, left bottom, from(#bd0afa), to(#d0df9f))", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("linear", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("left", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("top", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("left", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("bottom", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("from", lu.getFunctionName());
		LexicalUnit params = lu.getParameters();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_RGBCOLOR, params.getLexicalUnitType());
		assertNull(params.getNextLexicalUnit());
		params = params.getParameters();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_INTEGER, params.getLexicalUnitType());
		assertEquals(189, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_INTEGER, params.getLexicalUnitType());
		assertEquals(10, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_INTEGER, params.getLexicalUnitType());
		assertEquals(250, params.getIntegerValue());
		assertNull(params.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("to", lu.getFunctionName());
		params = lu.getParameters();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_RGBCOLOR, params.getLexicalUnitType());
		assertNull(params.getNextLexicalUnit());
		params = params.getParameters();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_INTEGER, params.getLexicalUnitType());
		assertEquals(208, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_INTEGER, params.getLexicalUnitType());
		assertEquals(223, params.getIntegerValue());
		params = params.getNextLexicalUnit();
		assertNotNull(params);
		assertEquals(LexicalUnit.SAC_INTEGER, params.getLexicalUnitType());
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
