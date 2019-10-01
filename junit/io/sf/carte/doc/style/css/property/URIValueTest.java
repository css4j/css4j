/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;

public class URIValueTest {

	@Test
	public void testSetStringValueShortString() {
		URIValue value = new URIValue((byte) 0);
		value.setStringValue(CSSPrimitiveValue.CSS_URI, "http://www.example.com");
		assertEquals("http://www.example.com", value.getStringValue());
		assertEquals("url('http://www.example.com')", value.getCssText());
		value.setStringValue(CSSPrimitiveValue.CSS_URI, "http://www.example.com/app?param='foo'");
		assertEquals("http://www.example.com/app?param='foo'", value.getStringValue());
		assertEquals("url('http://www.example.com/app?param=\\\'foo\\\'')", value.getCssText());
	}

	@Test
	public void testSetCssTextString() {
		URIValue value = new URIValue((byte) 0);
		value.setCssText("http://www.example.com");
		assertEquals("http://www.example.com", value.getStringValue());
		assertEquals("url('http://www.example.com')", value.getCssText());
		value.setCssText("url('http://www.example.com')");
		assertEquals("http://www.example.com", value.getStringValue());
		assertEquals("url('http://www.example.com')", value.getCssText());
	}

	@Test
	public void testSetCssTextStringDQ() {
		URIValue value = new URIValue((byte) 0);
		value.setCssText("url(\"http://www.example.com\")");
		assertEquals("http://www.example.com", value.getStringValue());
		assertEquals("url(\"http://www.example.com\")", value.getCssText());
		assertEquals("url(\"http://www.example.com\")", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscapeQuotes() {
		URIValue value = new URIValue((byte) 0);
		value.setCssText("url('http://www.example.com/app?param=\\\'foo\\\'')");
		assertEquals("http://www.example.com/app?param='foo'", value.getStringValue());
		assertEquals("url('http://www.example.com/app?param=\\\'foo\\\'')", value.getCssText());
		assertEquals("url(\"http://www.example.com/app?param=\'foo\'\")", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscapeQuotes2() {
		URIValue value = new URIValue((byte) 0);
		value.setCssText("url('http://www.example.com/app?param=\\\'foo\\\'&param2=\"bar\"')");
		assertEquals("http://www.example.com/app?param='foo'&param2=\"bar\"", value.getStringValue());
		assertEquals("url('http://www.example.com/app?param=\\\'foo\\\'&param2=\"bar\"')", value.getCssText());
		assertEquals("url('http://www.example.com/app?param=\\\'foo\\\'&param2=\"bar\"')",
				value.getMinifiedCssText(""));
		URIValue wrapped = new URIValueWrapper(value, null, "http://www.example.com/");
		assertEquals(value.getCssText(), wrapped.getCssText());
		assertEquals(value.getMinifiedCssText(""), wrapped.getMinifiedCssText(""));
	}

	@Test
	public void testEquals() {
		URIValue value = new URIValue((byte) 0);
		value.setCssText("url('http://www.example.com/foo')");
		URIValue other = new URIValue((byte) 0);
		other.setCssText("url('http://www.example.com/foo')");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("url('http://www.example.com/bar')");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
		// Wrapper (I)
		value.setCssText("url('http://www.example.com/dir/file.txt')");
		other.setCssText("url('../dir/file.txt')");
		URIValue wrapped = new URIValueWrapper(other, null, "http://www.example.com/foo/");
		assertEquals(value.getCssText(), wrapped.getCssText());
		assertTrue(value.equals(wrapped));
		assertEquals(value.hashCode(), wrapped.hashCode());
		// Wrapper (II)
		value.setCssText("url('http://www.example.com/dir/file.txt')");
		other.setCssText("url('/dir/file.txt')");
		wrapped = new URIValueWrapper(other, null, "http://www.example.com/foo/");
		assertEquals(value.getCssText(), wrapped.getCssText());
		assertTrue(value.equals(wrapped));
		assertEquals(value.hashCode(), wrapped.hashCode());
		// Wrapper (III)
		value.setCssText("url('http://www.example.com/dir/file.txt')");
		other.setCssText("url('file.txt')");
		wrapped = new URIValueWrapper(other, null, "http://www.example.com/dir/");
		assertEquals(value.getCssText(), wrapped.getCssText());
		assertTrue(value.equals(wrapped));
		assertEquals(value.hashCode(), wrapped.hashCode());
		// Wrapper (IV)
		value.setCssText("url('http://www.example.com/dir/file.txt')");
		other.setCssText("url('dir/file.txt')");
		wrapped = new URIValueWrapper(other, null, "http://www.example.com/");
		assertEquals(value.getCssText(), wrapped.getCssText());
		assertTrue(value.equals(wrapped));
		assertEquals(value.hashCode(), wrapped.hashCode());
		// Wrapper (V)
		value.setCssText("url('http://www.example.com/dir/file.txt')");
		other.setCssText("url('foo/file.txt')");
		wrapped = new URIValueWrapper(other, null, "http://www.example.com/");
		assertFalse(value.equals(wrapped));
		// Wrapper (VI)
		value.setCssText("url('http://www.example.com/dir/file.txt')");
		other.setCssText("url('../dir/file.txt')");
		wrapped = new URIValueWrapper(other, null, "http://www.example.com/foo/");
		assertEquals(value.getCssText(), wrapped.getCssText());
		assertTrue(value.equals(wrapped));
		assertEquals(value.hashCode(), wrapped.hashCode());
	}

	@Test
	public void testIsEquivalent() {
		URIValue value = new URIValue((byte) 0);
		URIValue other = new URIValue((byte) 0);
		value.setCssText("url('http://www.example.com/foo')");
		other.setCssText("url('http://www.example.com/foo')");
		assertTrue(value.isEquivalent(other));
		other.setCssText("url('http://www.example.com/bar')");
		assertFalse(value.isEquivalent(other));
		// Wrapper
		value.setCssText("url('dir/file.txt')");
		other.setCssText("url('dir/file.txt')");
		URIValue wrapped = new URIValueWrapper(other, null, "http://www.example.com/");
		assertTrue(value.isEquivalent(wrapped));
	}

	@Test
	public void testClone() {
		URIValue value = new URIValue((byte) 0);
		value.setStringValue(CSSPrimitiveValue.CSS_URI, "http://www.example.com");
		URIValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getMinifiedCssText(""), clon.getMinifiedCssText(""));
	}

}
