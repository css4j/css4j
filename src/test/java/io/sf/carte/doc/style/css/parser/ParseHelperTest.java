/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.PseudoCondition;

/**
 * ParseHelper tests. Tests for equalSelectorList are in
 * NSACSelectorFactoryTest.
 */
public class ParseHelperTest {

	@Test
	public void testStartsWithIgnoreCase() {
		assertTrue(ParseHelper.startsWithIgnoreCase("", ""));
		assertTrue(ParseHelper.startsWithIgnoreCase("foo", ""));
		assertFalse(ParseHelper.startsWithIgnoreCase("", "bar"));
		assertFalse(ParseHelper.startsWithIgnoreCase("foo", "bar"));
		assertFalse(ParseHelper.startsWithIgnoreCase("foo", "foobar"));
		assertTrue(ParseHelper.startsWithIgnoreCase("foo", "fo"));
		assertTrue(ParseHelper.startsWithIgnoreCase("foo", "foo"));
	}

	@Test
	public void testEndsWithIgnoreCase() {
		assertTrue(ParseHelper.endsWithIgnoreCase("", ""));
		assertTrue(ParseHelper.endsWithIgnoreCase("foo", ""));
		assertFalse(ParseHelper.endsWithIgnoreCase("", "bar"));
		assertFalse(ParseHelper.endsWithIgnoreCase("foo", "bar"));
		assertFalse(ParseHelper.endsWithIgnoreCase("bar", "fbar"));
		assertTrue(ParseHelper.endsWithIgnoreCase("abc", "bc"));
		assertTrue(ParseHelper.endsWithIgnoreCase("abc", "abc"));
	}

	@Test
	public void testEqualsIgnoreCase() {
		assertTrue(ParseHelper.equalsIgnoreCase("", ""));
		assertTrue(ParseHelper.equalsIgnoreCase("a", "a"));
		assertTrue(ParseHelper.equalsIgnoreCase("A", "a"));
		assertTrue(ParseHelper.equalsIgnoreCase("AA", "aa"));
		assertTrue(ParseHelper.equalsIgnoreCase("aA", "aa"));
		assertFalse(ParseHelper.equalsIgnoreCase("", "a"));
		assertFalse(ParseHelper.equalsIgnoreCase("aa", "a"));
		assertFalse(ParseHelper.equalsIgnoreCase("a", "aaa"));
		assertFalse(ParseHelper.equalsIgnoreCase("AAAA", "aaa"));
	}

	@Test
	public void testUnescapeStringValue() {
		assertEquals("", ParseHelper.unescapeStringValue(""));
		assertEquals(" ", ParseHelper.unescapeStringValue(" "));
		assertEquals(" ", ParseHelper.unescapeStringValue("\\ "));
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
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)",
			ParseHelper.unescapeStringValue(
				"progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)"));
	}

	@Test
	public void testUnescapeStringValue4() {
		assertEquals("line\nanother line\nfinal line\n",
			ParseHelper.unescapeStringValue("line\\A another line\\A final line\\A"));
		assertEquals("line\n another line\n final line\n", ParseHelper
			.unescapeStringValue("line\\00000a another line\\00000a final line\\00000a"));
		assertEquals("line\n another line\n final line\n",
			ParseHelper.unescapeStringValue("line\\00000a another line\\00000A final line\\A"));
	}

	@Test
	public void testUnescapeStringValue5() {
		assertEquals("-1zzz:_", ParseHelper.unescapeStringValue("-\\31zzz\\:_"));
	}

	@Test
	public void testUnescapeStringValueSurrogate() {
		assertEquals("\nfoo\uD83D\uDEA7bar",
			ParseHelper.unescapeStringValue("\\a foo\uD83D\uDEA7bar")); // 🚧
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
		assertEquals("line\ufffdZ another line\n final line\n", ParseHelper
			.unescapeStringValue("line\\00Z another line\\00000A final line\\A", true, false));
	}

	@Test
	public void testUnescapeStringValue7() {
		assertEquals("1jkl", ParseHelper.unescapeStringValue("\\31jkl"));
	}

	@Test
	public void testUnescapeStringValueBackslashEnd() {
		assertEquals("jkl\\", ParseHelper.unescapeStringValue("jkl\\\\"));
	}

	@Test
	public void testEscapeString() {
		assertEquals("\\ ", ParseHelper.escape(" "));
		assertEquals("a\\ ", ParseHelper.escape("a "));
		assertEquals("\\-", ParseHelper.escape("-"));
		assertEquals("_a", ParseHelper.escape("_a"));
		assertEquals("a_", ParseHelper.escape("a_"));
		assertEquals("-a", ParseHelper.escape("-a"));
		assertEquals("-a-b", ParseHelper.escape("-a-b"));
		assertEquals("--a", ParseHelper.escape("--a"));
		assertEquals("--a-b", ParseHelper.escape("--a-b"));
		assertEquals("𝜀", ParseHelper.escape("\ud835\udf00"));
		assertEquals("a\\a0 b", ParseHelper.escape("a\u00a0b"));
		assertEquals("a\\ad b", ParseHelper.escape("a\u00adb"));
		assertEquals("\\-9a", ParseHelper.escape("-9a"));
		assertEquals("\\[\\]\\(\\)\\{\\}", ParseHelper.escape("[](){}"));
		assertEquals("\\\\", ParseHelper.escape("\\"));
		assertEquals("\\ \\\\", ParseHelper.escape(" \\"));
		assertEquals("\\ \\9 ", ParseHelper.escape(" \u0009"));
		assertEquals("-\\9 ", ParseHelper.escape("-\u0009"));
		assertEquals("\\1b ", ParseHelper.escape("\u001b"));
		assertEquals("\\1b\\\\", ParseHelper.escape("\u001b\\"));
		assertEquals("\\\\\\ foo\uD83D\uDEA7bar", ParseHelper.escape("\\ foo\uD83D\uDEA7bar"));
		assertEquals("\\5b8b\u4f53", ParseHelper.escape("\\5b8b\u4f53"));
		assertEquals("\\e ", ParseHelper.escape("\u000e"));
		assertEquals("a\\/", ParseHelper.escape("a/"));
		assertEquals("\\31 a\\/", ParseHelper.escape("1a/"));
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)",
			ParseHelper.escape("progid:DXImageTransform.Microsoft.gradient(enabled=false)"));
		assertEquals("\\+foo🚧bar", ParseHelper.escape("+foo\uD83D\uDEA7bar"));
		assertEquals("\\9 \\ C", ParseHelper.escape("\u0009 C"));
		assertEquals("\\9 C\\&D", ParseHelper.escape("\u0009C&D"));
		assertEquals("\\9 C\\&D\\\\", ParseHelper.escape("\u0009C&D\\"));
		assertEquals("\\ \\9 C\\ \\&", ParseHelper.escape(" \u0009C &"));
		assertEquals("-\\9", ParseHelper.escape("-\\9"));
		assertEquals("\\fffd ", ParseHelper.escape("\ufffd"));
		assertEquals("\uD83D\uDC4D", ParseHelper.escape("\uD83D\uDC4D"));
		assertEquals("\\0 ", ParseHelper.escape("\u0000"));
	}

	@Test
	public void testEscapeString_Surrogate() {
		assertEquals("\\1F44D", ParseHelper.escape("\\1F44D"));
		assertEquals("foo\\1F44D bar", ParseHelper.escape("foo\\1F44D bar"));
		assertEquals("-👍", ParseHelper.escape("-\uD83D\uDC4D"));
	}

	@Test
	public void testEscapeStringBooleanBoolean() {
		assertEquals("\\ ", ParseHelper.escape(" ", true, true));
		assertEquals("a\\ ", ParseHelper.escape("a ", true, true));
		assertEquals("\\1d700 ", ParseHelper.escape("\ud835\udf00", true, true));
		assertEquals("\\9 C\\&D", ParseHelper.escape("\u0009C&D", true, true));
		assertEquals("\\\\5b8b\u4f53", ParseHelper.escape("\\5b8b\u4f53", false, true));
		assertEquals("\\1f44d ", ParseHelper.escape("\uD83D\uDC4D", false, true));
	}

	@Test
	public void testSafeEscapeString() {
		assertEquals("\\ ", ParseHelper.safeEscape(" "));
		assertEquals("a\\ ", ParseHelper.safeEscape("a "));
		assertEquals("\\-", ParseHelper.safeEscape("-"));
		assertEquals("_a", ParseHelper.safeEscape("_a"));
		assertEquals("a_", ParseHelper.safeEscape("a_"));
		assertEquals("-a", ParseHelper.safeEscape("-a"));
		assertEquals("-a-b", ParseHelper.safeEscape("-a-b"));
		assertEquals("--a", ParseHelper.safeEscape("--a"));
		assertEquals("--a-b", ParseHelper.safeEscape("--a-b"));
		assertEquals("𝜀", ParseHelper.safeEscape("\ud835\udf00"));
		assertEquals("a\\a0 b", ParseHelper.safeEscape("a\u00a0b"));
		assertEquals("a\\ad b", ParseHelper.safeEscape("a\u00adb"));
		assertEquals("a\\ad ", ParseHelper.safeEscape("a\u00ad"));
		assertEquals("\\-9a", ParseHelper.safeEscape("-9a"));
		assertEquals("\\[\\]\\(\\)\\{\\}", ParseHelper.safeEscape("[](){}"));
		assertEquals("\\\\", ParseHelper.safeEscape("\\"));
		assertEquals("\\ \\\\", ParseHelper.safeEscape(" \\"));
		assertEquals("\\ \\9 ", ParseHelper.safeEscape(" \u0009"));
		assertEquals("-\\9 ", ParseHelper.safeEscape("-\u0009"));
		assertEquals("\\1b ", ParseHelper.safeEscape("\u001b"));
		assertEquals("\\1b \\\\", ParseHelper.safeEscape("\u001b\\"));
		assertEquals("\\\\\\ foo🚧bar", ParseHelper.safeEscape("\\ foo\uD83D\uDEA7bar"));
		assertEquals("\\e ", ParseHelper.safeEscape("\u000e"));
		assertEquals("a\\/", ParseHelper.safeEscape("a/"));
		assertEquals("\\31 a\\/", ParseHelper.safeEscape("1a/"));
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)",
			ParseHelper.safeEscape("progid:DXImageTransform.Microsoft.gradient(enabled=false)"));
		assertEquals("\\+foo🚧bar", ParseHelper.safeEscape("+foo\uD83D\uDEA7bar"));
		assertEquals("\\9 \\ C", ParseHelper.safeEscape("\u0009 C"));
		assertEquals("\\9 C\\&D", ParseHelper.safeEscape("\u0009C&D"));
		assertEquals("\\9 C\\&D\\\\", ParseHelper.safeEscape("\u0009C&D\\"));
		assertEquals("\\ \\9 C\\ \\&", ParseHelper.safeEscape(" \u0009C &"));
		assertEquals("-\\\\9", ParseHelper.safeEscape("-\\9"));
		assertEquals("\\fffd ", ParseHelper.safeEscape("\ufffd"));
		assertEquals("\uD83D\uDC4D", ParseHelper.safeEscape("\uD83D\uDC4D"));
		assertEquals("-\uD83D\uDC4D", ParseHelper.safeEscape("-\uD83D\uDC4D"));
		assertEquals("\\0 ", ParseHelper.safeEscape("\u0000"));
	}

	@Test
	public void testSafeEscapeStringBoolean() {
		assertEquals("\\\\5b8b\u4f53", ParseHelper.safeEscape("\\5b8b\u4f53", true));
		assertEquals("\\1f44d ", ParseHelper.safeEscape("\uD83D\uDC4D", true));
		assertEquals("-\\1f44d ", ParseHelper.safeEscape("-\uD83D\uDC4D", true));
		assertEquals("--\\1f44d ", ParseHelper.safeEscape("--\uD83D\uDC4D", true));
	}

	@Test
	public void testSafeEscapeStringBooleanBoolean() {
		assertEquals("\\\\5FAE\\8f6f\\96c5\\9ed1 ",
			ParseHelper.safeEscape("\\5FAE\u8F6F\u96C5\u9ED1", true, true));
	}

	@Test
	public void testEscapeCssCharsAndFirstCharString() {
		assertEquals("a b", ParseHelper.escapeCssCharsAndFirstChar("a b").toString());
		assertEquals("_a", ParseHelper.escapeCssCharsAndFirstChar("_a").toString());
		assertEquals("a_", ParseHelper.escapeCssCharsAndFirstChar("a_").toString());
		assertEquals("-a", ParseHelper.escapeCssCharsAndFirstChar("-a").toString());
		assertEquals("\\.a", ParseHelper.escapeCssCharsAndFirstChar(".a").toString());
		assertEquals("-a-b\\.", ParseHelper.escapeCssCharsAndFirstChar("-a-b.").toString());
		assertEquals("-", ParseHelper.escapeCssCharsAndFirstChar("-").toString());
		assertEquals("\\39 a", ParseHelper.escapeCssCharsAndFirstChar("9a").toString());
		assertEquals("\\-9a", ParseHelper.escapeCssCharsAndFirstChar("-9a").toString());
		assertEquals("\\39 a\\+", ParseHelper.escapeCssCharsAndFirstChar("9a+").toString());
	}

	@Test
	public void testEscapeBackslashString() {
		assertEquals(" ", ParseHelper.escapeBackslash(" "));
		assertEquals("\\\\", ParseHelper.escapeBackslash("\\").toString());
		assertEquals(" \\\\", ParseHelper.escapeBackslash(" \\").toString());
		assertEquals(" \\A", ParseHelper.escapeBackslash(" \\A").toString());
		assertEquals(" \\038", ParseHelper.escapeBackslash(" \\038").toString());
		assertEquals(" \\038\\\\", ParseHelper.escapeBackslash(" \\038\\").toString());
		assertEquals("\\\\5FAE\\8F6F\\96C5\\9ED1",
			ParseHelper.escapeBackslash("\\\\5FAE\\8F6F\\96C5\\9ED1").toString());
		assertEquals("\\\\x\\8F6F\\96C5\\9ED1",
			ParseHelper.escapeBackslash("\\x\\8F6F\\96C5\\9ED1").toString());
	}

	@Test
	public void testEscapeBackslashStringSurrogate() {
		assertEquals("\\\\ foo\uD83D\uDEA7bar",
			ParseHelper.escapeBackslash("\\ foo\uD83D\uDEA7bar").toString());
	}

	@Test
	public void testEscapeCssCharsString() {
		assertEquals(" ", ParseHelper.escapeCssChars(" ").toString());
		assertEquals("\\-9a", ParseHelper.escapeCssChars("-9a").toString());
		assertEquals("\\.9a", ParseHelper.escapeCssChars(".9a").toString());
		assertEquals("\\-9a-b\\.", ParseHelper.escapeCssChars("-9a-b.").toString());
		assertEquals("a ", ParseHelper.escapeCssChars("a ").toString());
		assertEquals("a\\/", ParseHelper.escapeCssChars("a/").toString());
		assertEquals("\\:a\\/", ParseHelper.escapeCssChars(":a/").toString());
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)",
			ParseHelper.escapeCssChars("progid:DXImageTransform.Microsoft.gradient(enabled=false)")
				.toString());
	}

	@Test
	public void testEscapeCssCharsStringSurrogate() {
		assertEquals("\\+foo🚧bar", ParseHelper.escapeCssChars("+foo\uD83D\uDEA7bar").toString());
	}

	@Test
	public void testEscapeCssCharsAndFirstCharString2() {
		assertEquals(" ", ParseHelper.escapeCssCharsAndFirstChar(" ").toString());
		assertEquals("-", ParseHelper.escapeCssCharsAndFirstChar("-").toString());
		assertEquals("a ", ParseHelper.escapeCssCharsAndFirstChar("a ").toString());
		assertEquals("a\\/", ParseHelper.escapeCssCharsAndFirstChar("a/").toString());
		assertEquals("\\31 a\\/", ParseHelper.escapeCssCharsAndFirstChar("1a/").toString());
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)",
			ParseHelper.escapeCssCharsAndFirstChar(
				"progid:DXImageTransform.Microsoft.gradient(enabled=false)").toString());
	}

	@Test
	public void testEscapeCssCharsAndFirstCharStringSurrogate() {
		assertEquals("\\+foo🚧bar",
			ParseHelper.escapeCssCharsAndFirstChar("+foo\uD83D\uDEA7bar").toString());
	}

	@Test
	public void testEscapeControlString() {
		assertEquals("\\9 C", ParseHelper.escapeControl(ParseHelper.unescapeStringValue("\\9 C")));
		assertEquals("\\9 C &D",
			ParseHelper.escapeControl(ParseHelper.unescapeStringValue("\\9 C \\26 D")));
		assertEquals(" \\9 C &",
			ParseHelper.escapeControl(ParseHelper.unescapeStringValue(" \\9 C \\26")));
	}

	@Test
	public void testEscapeControlString2() {
		assertEquals("â†\\90 ", ParseHelper.escapeControl("â\u2020\u0090"));
	}

	@Test
	public void testEscapeControlStringSurrogate() {
		assertEquals("\\a foo\uD83D\uDEA7bar",
			ParseHelper.escapeControl(ParseHelper.unescapeStringValue("\\a foo\uD83D\uDEA7bar")));
	}

	@Test
	public void testParseIdentString() {
		assertEquals("foo", ParseHelper.parseIdent("foo"));
		assertEquals("1f", ParseHelper.parseIdent("\\31 f"));
		assertEquals("o", ParseHelper.parseIdent("\\6f"));
		assertEquals("o", ParseHelper.parseIdent("\\6F"));
		assertEquals("1z", ParseHelper.parseIdent("\\31z"));
		assertEquals("-â†\u0090", ParseHelper.parseIdent("-â\u2020\\000090"));
		assertEquals("-â†\u0090", ParseHelper.parseIdent("-â\u2020\\90"));
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

	@Test
	public void testCreatePseudoElementCondition() {
		PseudoCondition pe = ParseHelper.createPseudoElementCondition("first-letter");
		assertNotNull(pe);
		assertEquals(Condition.ConditionType.PSEUDO_ELEMENT, pe.getConditionType());
		assertEquals("first-letter", pe.getName());
		assertEquals("::first-letter", pe.toString());
		assertNull(pe.getArgument());
	}

}
