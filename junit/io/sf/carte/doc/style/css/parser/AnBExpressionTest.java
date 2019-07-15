/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SelectorList;

import io.sf.carte.doc.style.css.SACParserFactory;

public class AnBExpressionTest {

	@Test
	public void testParse() {
		AnBExpression expr = new MyAnBExpression();
		expr.parse("1");
		assertEquals(1, expr.getOffset());
		assertEquals(0, expr.getStep());
		expr.parse("2n+1");
		assertEquals(1, expr.getOffset());
		assertEquals(2, expr.getStep());
		expr.parse("+2n+1");
		assertEquals(1, expr.getOffset());
		assertEquals(2, expr.getStep());
		expr.parse("-2n-1");
		assertEquals(-1, expr.getOffset());
		assertEquals(-2, expr.getStep());
		expr.parse("+2n-1");
		assertEquals(-1, expr.getOffset());
		assertEquals(2, expr.getStep());
		expr.parse("2n-1");
		assertEquals(-1, expr.getOffset());
		assertEquals(2, expr.getStep());
		expr.parse("0n+1");
		assertEquals(1, expr.getOffset());
		assertEquals(0, expr.getStep());
		expr.parse("-n+1");
		assertEquals(1, expr.getOffset());
		assertEquals(-1, expr.getStep());
		expr.parse("-n-1");
		assertEquals(-1, expr.getOffset());
		assertEquals(-1, expr.getStep());
		expr.parse("0n");
		assertEquals(0, expr.getOffset());
		assertEquals(0, expr.getStep());
		expr.parse("n");
		assertEquals(0, expr.getOffset());
		assertEquals(1, expr.getStep());
		expr.parse("-n");
		assertEquals(0, expr.getOffset());
		assertEquals(-1, expr.getStep());
		expr.parse("-n+0");
		assertEquals(0, expr.getOffset());
		assertEquals(-1, expr.getStep());
		expr.parse("-n + 0");
		assertEquals(0, expr.getOffset());
		assertEquals(-1, expr.getStep());
		expr.parse("-2n - 1");
		assertEquals(-1, expr.getOffset());
		assertEquals(-2, expr.getStep());
		expr.parse("odd");
		assertEquals(1, expr.getOffset());
		assertEquals(2, expr.getStep());
		expr.parse("even");
		assertEquals(0, expr.getOffset());
		assertEquals(2, expr.getStep());
		// Example 10 from https://drafts.csswg.org/css-syntax-3/#anb-syntax
		try {
			expr.parse("+ 2n");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("2 n");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("+ 2");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		// Other cases of bad syntax
		try {
			expr.parse("0n+-1");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("a0n+1");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("0n+1a");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
	}

	static class MyAnBExpression extends AnBExpression {

		@Override
		protected SelectorList parseSelector(String selText) {
			if (selText.length() == 0) {
				return null;
			}
			Parser parser = SACParserFactory.createSACParser();
			InputSource source = new InputSource(new StringReader(selText));
			SelectorList list;
			try {
				list = parser.parseSelectors(source);
			} catch (IOException e) {
				list = null;
			} catch (RuntimeException e) {
				list = null;
			}
			return list;
		}
		
	}
}
