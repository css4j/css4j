/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.DOMException;

/**
 * ParseHelper tests. Tests for equalSelectorList are in
 * NSACSelectorFactoryTest.
 */
public class ParseHelperTest {

	@Test
	public void testUnescapeStringValue() {
		assertEquals("", ParseHelper.unescapeStringValue(""));
		assertEquals(" ", ParseHelper.unescapeStringValue(" "));
		assertEquals("\\", ParseHelper.unescapeStringValue("\\\\"));
		assertEquals("\n", ParseHelper.unescapeStringValue("\\A "));
		assertEquals("&", ParseHelper.unescapeStringValue("\\26 "));
		assertEquals("&", ParseHelper.unescapeStringValue("\\0026"));
		assertEquals("&", ParseHelper.unescapeStringValue("\\000026"));
		assertEquals("&B", ParseHelper.unescapeStringValue("\\000026B"));
		assertEquals("&&", ParseHelper.unescapeStringValue("\\0026\\0026"));
		assertEquals("&\"", ParseHelper.unescapeStringValue("\\0026\\\""));
		assertEquals("&+", ParseHelper.unescapeStringValue("\\0026\\+"));
		assertEquals("++&", ParseHelper.unescapeStringValue("\\+\\+\\0026"));
		assertEquals("++ ", ParseHelper.unescapeStringValue("\\+\\+ "));
		assertEquals("\"&", ParseHelper.unescapeStringValue("\\\"\\0026"));
		assertEquals("\r", ParseHelper.unescapeStringValue("\\D"));
		assertEquals("&\r", ParseHelper.unescapeStringValue("\\0026\\D"));
		assertEquals("\r&", ParseHelper.unescapeStringValue("\\D\\0026"));
		assertEquals("\u200B", ParseHelper.unescapeStringValue("\\200B"));
		assertEquals("&C", ParseHelper.unescapeStringValue("\\26 C"));
		assertEquals("ĂC", ParseHelper.unescapeStringValue("\\102 C"));
		assertEquals("\n\uFFFD \uFFFD", ParseHelper.unescapeStringValue("\\A \n \n"));
		assertEquals("1A", ParseHelper.unescapeStringValue("\\31 A"));
		assertEquals("1Z", ParseHelper.unescapeStringValue("\\31 Z"));
	}

	@Test
	public void testUnescapeStringValue2() {
		assertEquals("+0", ParseHelper.unescapeStringValue("\\+0"));
		assertEquals("+e", ParseHelper.unescapeStringValue("\\+e"));
	}

	@Test
	public void testUnescapeStringValue3() {
		assertEquals("++enabled", ParseHelper.unescapeStringValue("\\+\\+enabled"));
		assertEquals("((enabled", ParseHelper.unescapeStringValue("\\(\\(enabled"));
	}

	@Test
	public void testUnescapeStringValueProgid() {
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", ParseHelper
				.unescapeStringValue("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)"));
	}

	@Test
	public void testUnescapeStringValue4() {
		assertEquals("line\nanother line\nfinal line\n",
				ParseHelper.unescapeStringValue("line\\A another line\\A final line\\A"));
		assertEquals("line\n another line\n final line\n",
				ParseHelper.unescapeStringValue("line\\00000a another line\\00000a final line\\00000a"));
		assertEquals("line\n another line\n final line\n",
				ParseHelper.unescapeStringValue("line\\00000a another line\\00000A final line\\A"));
	}

	@Test
	public void testUnescapeStringValue5() {
		assertEquals("-1zzz:_", ParseHelper.unescapeStringValue("-\\31zzz\\:_"));
	}

	@Test
	public void testUnescapeStringValueSurrogate() {
		assertEquals("\nfoo🚧bar", ParseHelper.unescapeStringValue("\\a foo🚧bar"));
	}

	@Test
	public void testUnescapeStringValueEOF() {
		assertEquals("", ParseHelper.unescapeStringValue("\\"));
		assertEquals("+", ParseHelper.unescapeStringValue("\\+\\"));
		assertEquals("x", ParseHelper.unescapeStringValue("\\000078\\"));
	}

	@Test
	public void testUnescapeStringValue6() {
		assertEquals("line\nMore lines\nOther line\n",
				ParseHelper.unescapeStringValue("line\\AMore lines\\AOther line\\A"));
	}

	@Test
	public void testUnescapeStringValueNull() {
		assertEquals("line\ufffdZ another line\n final line\n",
				ParseHelper.unescapeStringValue("line\\00Z another line\\00000A final line\\A", true, false));
	}

	@Test
	public void testUnescapeStringValue7() {
		assertEquals("1jkl", ParseHelper.unescapeStringValue("\\31jkl"));
	}

	@Test
	public void testEscapeString() {
		assertEquals(" ", ParseHelper.escape(" "));
		assertEquals("-", ParseHelper.escape("-"));
		assertEquals("_a", ParseHelper.escape("_a"));
		assertEquals("a_", ParseHelper.escape("a_"));
		assertEquals("-a", ParseHelper.escape("-a"));
		assertEquals("-a-b", ParseHelper.escape("-a-b"));
		assertEquals("a\\a0 b", ParseHelper.escape("a\u00a0b"));
		assertEquals("a\\ad b", ParseHelper.escape("a\u00adb"));
		assertEquals("-\\39 a", ParseHelper.escape("-9a"));
		assertEquals("\\\\", ParseHelper.escape("\\"));
		assertEquals(" \\\\", ParseHelper.escape(" \\"));
		assertEquals(" \\9", ParseHelper.escape(" \u0009"));
		assertEquals("\\1b", ParseHelper.escape("\u001b"));
		assertEquals("\\1b\\\\", ParseHelper.escape("\u001b\\"));
		assertEquals("\\\\ foo🚧bar", ParseHelper.escape("\\ foo🚧bar"));
		assertEquals("\\e", ParseHelper.escape("\u000e"));
		assertEquals("a\\/", ParseHelper.escape("a/"));
		assertEquals("\\31 a\\/", ParseHelper.escape("1a/"));
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)",
				ParseHelper.escape("progid:DXImageTransform.Microsoft.gradient(enabled=false)"));
		assertEquals("\\+foo🚧bar", ParseHelper.escape("+foo🚧bar"));
		assertEquals("\\9  C", ParseHelper.escape("\u0009 C"));
		assertEquals("\\9 C\\&D", ParseHelper.escape("\u0009C&D"));
		assertEquals("\\9 C\\&D\\\\", ParseHelper.escape("\u0009C&D\\"));
		assertEquals(" \\9 C \\&", ParseHelper.escape(" \u0009C &"));
		assertEquals("\\fffd", ParseHelper.escape("\ufffd"));
		assertEquals("\\0", ParseHelper.escape("\u0000"));
		assertEquals("\\1F44D", ParseHelper.escape("\\1F44D"));
		assertEquals("foo\\1F44D bar", ParseHelper.escape("foo\\1F44D bar"));
	}

	@Test
	public void testEscapeCssCharsAndFirstCharString() {
		assertEquals("a b", ParseHelper.escapeCssCharsAndFirstChar("a b"));
		assertEquals("_a", ParseHelper.escapeCssCharsAndFirstChar("_a"));
		assertEquals("a_", ParseHelper.escapeCssCharsAndFirstChar("a_"));
		assertEquals("-a", ParseHelper.escapeCssCharsAndFirstChar("-a"));
		assertEquals("-a-b", ParseHelper.escapeCssCharsAndFirstChar("-a-b"));
		assertEquals("-", ParseHelper.escapeCssCharsAndFirstChar("-"));
		assertEquals("\\39 a", ParseHelper.escapeCssCharsAndFirstChar("9a").toString());
		assertEquals("-\\39 a", ParseHelper.escapeCssCharsAndFirstChar("-9a").toString());
		assertEquals("\\39 a\\+", ParseHelper.escapeCssCharsAndFirstChar("9a+").toString());
	}

	@Test
	public void testEscapeBackslashString() {
		assertEquals(" ", ParseHelper.escapeBackslash(" "));
		assertEquals("\\\\", ParseHelper.escapeBackslash("\\").toString());
		assertEquals(" \\\\", ParseHelper.escapeBackslash(" \\").toString());
		assertEquals(" \\A", ParseHelper.escapeBackslash(" \\A"));
		assertEquals(" \\038", ParseHelper.escapeBackslash(" \\038"));
		assertEquals(" \\038\\\\", ParseHelper.escapeBackslash(" \\038\\").toString());
	}

	@Test
	public void testEscapeBackslashStringSurrogate() {
		assertEquals("\\\\ foo🚧bar", ParseHelper.escapeBackslash("\\ foo🚧bar").toString());
	}

	@Test
	public void testEscapeCssCharsString() {
		assertEquals(" ", ParseHelper.escapeCssCharsAndFirstChar(" "));
		assertEquals("a\\/", ParseHelper.escapeCssCharsAndFirstChar("a/").toString());
		assertEquals("\\31 a\\/", ParseHelper.escapeCssCharsAndFirstChar("1a/").toString());
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)", ParseHelper
				.escapeCssCharsAndFirstChar("progid:DXImageTransform.Microsoft.gradient(enabled=false)").toString());
	}

	@Test
	public void testEscapeCssCharsStringSurrogate() {
		assertEquals("\\+foo🚧bar", ParseHelper.escapeCssCharsAndFirstChar("+foo🚧bar").toString());
	}

	@Test
	public void testEscapeControlString() {
		assertEquals("\\9 C", ParseHelper.escapeControl(ParseHelper.unescapeStringValue("\\9 C")));
		assertEquals("\\9 C &D", ParseHelper.escapeControl(ParseHelper.unescapeStringValue("\\9 C \\26 D")));
		assertEquals(" \\9 C &", ParseHelper.escapeControl(ParseHelper.unescapeStringValue(" \\9 C \\26")));
	}

	@Test
	public void testEscapeControlString2() {
		assertEquals("â†\\90 ", ParseHelper.escapeControl("â†\u0090"));
	}

	@Test
	public void testEscapeControlStringSurrogate() {
		assertEquals("\\a foo🚧bar", ParseHelper.escapeControl(ParseHelper.unescapeStringValue("\\a foo🚧bar")));
	}

	@Test
	public void testParseIdentString() {
		assertEquals("foo", ParseHelper.parseIdent("foo"));
		assertEquals("1f", ParseHelper.parseIdent("\\31 f"));
		assertEquals("o", ParseHelper.parseIdent("\\6f"));
		assertEquals("o", ParseHelper.parseIdent("\\6F"));
		assertEquals("1z", ParseHelper.parseIdent("\\31z"));
		assertEquals("-â†\u0090", ParseHelper.parseIdent("-â†\\000090"));
		assertEquals("-â†\u0090", ParseHelper.parseIdent("-â†\\90"));
	}

	@Test
	public void testParseIdentStringError() {
		try {
			ParseHelper.parseIdent("1ident");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testParseIdentStringError2() {
		try {
			ParseHelper.parseIdent("-1ident");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testParseIdentStringError3() {
		try {
			ParseHelper.parseIdent("\\31  f");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testParseIdentStringError4() {
		try {
			ParseHelper.parseIdent("a b");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

}
