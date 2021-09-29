/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class StyleValueTest {

	private Parser cssParser;

	@Before
	public void setUp() {
		this.cssParser = new CSSParser();
	}

	@Test
	public void testParseProperty() {
		ValueFactory factory = new ValueFactory();
		StyleValue value = factory.parseProperty("none");
		assertNotNull(value);
		assertEquals("none", value.getCssText());
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, value.getPrimitiveType());
		value = factory.parseProperty("url('a.png')", cssParser);
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("a.png", ((CSSTypedValue) value).getStringValue());
		assertEquals("url('a.png')", value.getCssText());
		value = factory.parseProperty("'aaa bbb ccc'", cssParser);
		assertNotNull(value);
		assertEquals("'aaa bbb ccc'", value.getCssText());
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.STRING, value.getPrimitiveType());
		assertEquals("aaa bbb ccc", ((CSSTypedValue) value).getStringValue());
		value = factory.parseProperty("url('a.png'),url(b.png)", cssParser);
		assertNotNull(value);
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals("a.png", ((CSSTypedValue) ((ValueList) value).item(0)).getStringValue());
		assertEquals("url('a.png'), url('b.png')", value.getCssText());
		value = factory.parseProperty("attr(href)", cssParser);
		assertNotNull(value);
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals("href", ((AttrValue) value).getAttributeName());
		assertEquals(CSSValue.Type.ATTR, value.getPrimitiveType());
		assertEquals("attr(href)", value.getCssText());
		value = factory.parseProperty("#f0be4f", cssParser);
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.COLOR, value.getPrimitiveType());
		assertEquals("#f0be4f", value.getCssText());
		assertEquals("#f0be4f", value.getMinifiedCssText("color"));
		value = factory.parseProperty("'Times New Roman'", cssParser);
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("'Times New Roman'", value.getCssText());
		value = factory.parseProperty("'Times New Roman', Helvetica, Arial", cssParser);
		assertNotNull(value);
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals("'Times New Roman', Helvetica, Arial", value.getCssText());
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals(3, ((ValueList) value).getLength());
		value = factory.parseProperty("50%", cssParser);
		assertNotNull(value);
		assertEquals(Type.NUMERIC, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, ((CSSTypedValue) value).getUnitType());
		assertEquals(50, ((CSSTypedValue) value).getFloatValue(CSSUnit.CSS_PERCENTAGE), 1e-5f);
		assertEquals("50%", value.getCssText());
		value = factory.parseProperty("15cm", cssParser);
		assertNotNull(value);
		assertEquals(15, ((CSSTypedValue) value).getFloatValue(CSSUnit.CSS_CM), 1e-5f);
		assertEquals("15cm", value.getCssText());
		value = factory.parseProperty("12", cssParser);
		assertNotNull(value);
		assertEquals(12, ((CSSTypedValue) value).getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertEquals(Type.NUMERIC, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_NUMBER, ((CSSTypedValue) value).getUnitType());
		assertEquals("12", value.getCssText());
		value = factory.parseProperty("inherit", cssParser);
		assertNotNull(value);
		assertEquals("inherit", value.getCssText());
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(Type.INHERIT, value.getPrimitiveType());
		assertTrue(value.equals(factory.parseProperty("inherit", cssParser)));
	}

}
