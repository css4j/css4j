/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class PropertyParserColorTest {

	private Parser parser;

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
	public void testParsePropertyBadImportant() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(128, 0, 97 !important");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(16, e.getColumnNumber());
		}
	}

	@Test
	public void testParsePropertyBadImportant2() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBCalcReal() throws CSSException, IOException {
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
		assertEquals(127.8f, param.getFloatValue(), 1e-7);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
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
	public void testParsePropertyValueRGBPcntMix() throws CSSException, IOException {
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
	public void testParsePropertyValueRGBPcntMix2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("rgb(27% 0 48%)");
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(27f, param.getFloatValue(), 1e-6);
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(48f, param.getFloatValue(), 1e-6);
		assertNull(param.getNextLexicalUnit());
		assertEquals("rgb", lu.getFunctionName());
		assertEquals("rgb(27% 0 48%)", lu.toString());
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
	public void testParsePropertyValueRGBBadRValue() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(-6 0 2)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadGValue() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 -6 2)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadBValue() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 2 -6)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadChar() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 a 0/ 0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadChar2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 0 0/@ 0)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadPcnt() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(10% 2% 180%)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMix() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0 2% 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMix2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(2% 0 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMix3() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(2% 20 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMix4() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(2% 20.6 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMix5() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(20.6 2% 30%)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMixCommas() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(0, 2%, 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadMixCommas2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(2%, 0, 10)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadNegativeReal() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(-10.3 56.4 70.8)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadNegativeReal2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(10.3 56.4 -70.8)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadNegativePcnt() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(-10% 56% 70%)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadNegativePcnt2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(10% 56% -70%)");
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
	public void testParsePropertyValueRGBBadVar9() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(var(--foo)/.8/.4)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVar10() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(var(--foo)/1/.4)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadVarComma() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(10 20 var(--foo), 0)");
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
	public void testParsePropertyValueRGBBadAlphaReal() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12 48 127/2.1)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParsePropertyValueRGBBadAlphaReal2() throws CSSException, IOException {
		try {
			parsePropertyValue("rgb(12 48 127/-0.1)");
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
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
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
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
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
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
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
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
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
		assertEquals(LexicalType.CALC, param.getLexicalUnitType());
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

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(value));
	}

}
