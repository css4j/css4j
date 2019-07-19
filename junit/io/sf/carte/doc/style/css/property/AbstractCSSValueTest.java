/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.css.sac.Parser;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

@RunWith(Parameterized.class)
public class AbstractCSSValueTest {

	private Parser cssParser;

	public AbstractCSSValueTest(Parser cssParser) {
		super();
		this.cssParser = cssParser;
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> parsers = new LinkedList<Object[]>();
		parsers.add(new Object[] { new io.sf.carte.doc.style.css.parser.CSSParser() });
		parsers.add(new Object[] { new org.apache.batik.css.parser.Parser() });
		try {
			Parser p = (Parser) Class.forName("com.steadystate.css.parser.SACParserCSS3").getConstructor()
					.newInstance();
			parsers.add(new Object[] { p });
		} catch (Exception e) {
		}
		try {
			Parser p = (Parser) Class.forName("com.steadystate.css.parser.SACParserCSS3").getConstructor()
					.newInstance();
			parsers.add(new Object[] { p });
		} catch (Exception e) {
		}
		return parsers;
	}

	@Test
	public void testParseProperty() {
		ValueFactory factory = new ValueFactory();
		AbstractCSSValue value = factory.parseProperty("none");
		assertNotNull(value);
		assertEquals("none", value.getCssText());
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_IDENT, ((CSSPrimitiveValue) value).getPrimitiveType());
		value = factory.parseProperty("url('a.png')", cssParser);
		assertNotNull(value);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("a.png", ((CSSPrimitiveValue) value).getStringValue());
		assertEquals("url('a.png')", value.getCssText());
		value = factory.parseProperty("'aaa bbb ccc'", cssParser);
		assertNotNull(value);
		assertEquals("'aaa bbb ccc'", value.getCssText());
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_STRING, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("aaa bbb ccc", ((CSSPrimitiveValue) value).getStringValue());
		value = factory.parseProperty("url('a.png'),url(b.png)", cssParser);
		assertNotNull(value);
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		assertEquals("a.png", ((CSSPrimitiveValue) ((CSSValueList) value).item(0)).getStringValue());
		assertEquals("url('a.png'), url('b.png')", value.getCssText());
		value = factory.parseProperty("attr(href)", cssParser);
		assertNotNull(value);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals("href", ((CSSPrimitiveValue) value).getStringValue());
		assertEquals(CSSPrimitiveValue.CSS_ATTR, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("attr(href)", value.getCssText());
		value = factory.parseProperty("#f0be4f", cssParser);
		assertNotNull(value);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("#f0be4f", value.getCssText());
		assertEquals("#f0be4f", value.getMinifiedCssText("color"));
		value = factory.parseProperty("'Times New Roman'", cssParser);
		assertNotNull(value);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals("'Times New Roman'", value.getCssText());
		value = factory.parseProperty("'Times New Roman', Helvetica, Arial", cssParser);
		assertNotNull(value);
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		assertEquals("'Times New Roman', Helvetica, Arial", value.getCssText());
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		assertEquals(3, ((ValueList) value).getLength());
		value = factory.parseProperty("50%", cssParser);
		assertNotNull(value);
		assertEquals(CSSPrimitiveValue.CSS_PERCENTAGE, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals(50, ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE), 1e-5);
		assertEquals("50%", value.getCssText());
		value = factory.parseProperty("15cm", cssParser);
		assertNotNull(value);
		assertEquals(15, ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_CM), 1e-5);
		assertEquals("15cm", value.getCssText());
		value = factory.parseProperty("12", cssParser);
		assertNotNull(value);
		assertEquals(12, ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_NUMBER), 1e-5);
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("12", value.getCssText());
		value = factory.parseProperty("inherit", cssParser);
		assertNotNull(value);
		assertEquals("inherit", value.getCssText());
		assertEquals(CSSValue.CSS_INHERIT, value.getCssValueType());
		assertTrue(value.equals(factory.parseProperty("inherit", cssParser)));
	}

}
