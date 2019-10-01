/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;

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
		expr.parse("2N+1");
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
		expr.parse("+n+7");
		assertEquals(7, expr.getOffset());
		assertEquals(1, expr.getStep());
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
		expr.parse("-20n - 10");
		assertEquals(-10, expr.getOffset());
		assertEquals(-20, expr.getStep());
		expr.parse("odd");
		assertEquals(1, expr.getOffset());
		assertEquals(2, expr.getStep());
		expr.parse("even");
		assertEquals(0, expr.getOffset());
		assertEquals(2, expr.getStep());
	}

	@Test
	public void testParseError() {
		AnBExpression expr = new MyAnBExpression();
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
			expr.parse("n 2");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("++n+2");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("--n+2");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("++n");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("++ n");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("n++2");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("n--2");
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
			expr.parse("n +- 1");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("n + - 1");
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
		try {
			expr.parse("0-n");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("2 + 2");
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
		try {
			expr.parse("0 + 2");
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
			Parser parser = new CSSParser();
			SelectorList list;
			try {
				list = parser.parseSelectors(new StringReader(selText));
			} catch (IOException e) {
				list = null;
			} catch (RuntimeException e) {
				list = null;
			}
			return list;
		}

	}
}
