/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CSSParserTest {

	@Test
	public void testBufferEndsWithEscapedChar() {
		StringBuilder buffer = new StringBuilder(50);
		buffer.append("foo");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\123456");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\12 foo");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("foo\\123456");
		assertFalse(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\12345");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("foo\\12345");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("\\1");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
		buffer.append("foo\\1");
		assertTrue(CSSParser.bufferEndsWithEscapedCharOrWS(buffer));
	}

}
