/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.css.sac.CSSParseException;

public class SupportsConditionTest {

	@Test
	public void testParseSupportsCondition() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)))",
				null);
		assertNotNull(cond);
		assertEquals("(-webkit-backdrop-filter:saturate(180%) blur(20px)) or (backdrop-filter:saturate(180%) blur(20px))",
				toMinifiedText(cond));
		assertEquals(
				"(-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px))",
				cond.toString());
	}

	@Test
	public void testParseSupportsCondition2() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition("(display:table-cell) and (display:list-item)", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(display: table-cell) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsCondition3() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid)))",
				null);
		assertNotNull(cond);
		assertEquals(
				"((display:table-cell) and (display:list-item) and (display:run-in)) or ((display:table-cell) and (not (display:inline-grid)))",
				toMinifiedText(cond));
		assertEquals(
				"((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid)))",
				cond.toString());
	}

	@Test
	public void testParseSupportsCondition4() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"(background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))",
				null);
		assertNotNull(cond);
		assertEquals(
				"(background:-webkit-gradient(linear,left top,left bottom,from(transparent),to(#fff))) or (background:-webkit-linear-gradient(transparent,#fff)) or (background:-moz-linear-gradient(transparent,#fff)) or (background:-o-linear-gradient(transparent,#fff)) or (background:linear-gradient(transparent,#fff))",
				toMinifiedText(cond));
		assertEquals(
				"(background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))",
				cond.toString());
	}

	@Test
	public void testParseSupportsCondition5() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell))",
				null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item) and ((not (display:run-in)) or (display:table-cell))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) and ((display:list-item) or (display:flex))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr2() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) and (((display:list-item) or (display:flex)))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr3() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display:table-cell) and ((((display:list-item)) or (((display:flex)) and ((display:foo)))))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or ((display:flex) and (display:foo)))",
				toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or ((display: flex) and (display: foo)))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr4() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"((display:table-cell)) and ((((display:list-item)) or ((((display:flex)) and ((display:foo))))))",
				null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or ((display:flex) and (display:foo)))",
				toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or ((display: flex) and (display: foo)))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr5() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display: table-cell) and ((display: list-item) or (not ((display: run-in) or (display: table-cell))))",
				null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (not ((display:run-in) or (display:table-cell))))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and ((display: list-item) or (not ((display: run-in) or (display: table-cell))))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedAnd() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) or ((display:list-item) and (display:flex))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) or ((display:list-item) and (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) or ((display: list-item) and (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedAnd2() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) or (((display:list-item) and (display:flex)))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) or ((display:list-item) and (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) or ((display: list-item) and (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionComments() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"/*comment 1*/(display:table-cell)/*comment 2*/and(display:list-item)/*comment 3*/", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(display: table-cell) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsConditionEmpty() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionEmpty2() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition(" ", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionEmpty3() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("()", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("(display:table-cell) and (display:list-item", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad2() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("(display:table-cell) and (display:list-item))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad3() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("(display foo:table-cell) and (display:list-item)", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad4() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition(
					"((transition-property: color) or (animation-name: foo) and (transform: rotate(10deg)))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad5() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition(
					"(transition-property: color) or (animation-name: foo) and (transform: rotate(10deg))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad6() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition(
					"((transition-property: color) and (animation-name: foo) or (transform: rotate(10deg)))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad7() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition(
					"(transition-property: color) and (animation-name: foo) or (transform: rotate(10deg))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad8() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition(
					"(transition-property: color) and ((animation-name: foo) or (animation-name: bar) and (transform: rotate(10deg)))",
					null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad9() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("(((display):table-cell) and (display:list-item))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad10() {
		CSSParser parser = new CSSParser();
		try {
			parser.parseSupportsCondition("'foo' (display:table-cell) and (display:list-item)", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testEquals() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition("(display: table-cell) and (display: list-item)", null);
		BooleanCondition other = parser.parseSupportsCondition("(display: table-cell) and (display: list-item)", null);
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		other = parser.parseSupportsCondition("(display: table-cell) and (display: foo)", null);
		assertFalse(cond.equals(other));
		other = parser.parseSupportsCondition("(display: table-cell)", null);
		assertFalse(cond.equals(other));
	}

	@Test
	public void testEquals2() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition("(display: flexbox) and (not (display: inline-grid))",
				null);
		BooleanCondition other = parser.parseSupportsCondition("(display: flexbox) and (not (display: inline-grid))",
				null);
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals("(display:flexbox) and (not (display:inline-grid))", toMinifiedText(cond));
		other = parser.parseSupportsCondition("(display: flexbox) and (display: inline-grid)", null);
		assertFalse(cond.equals(other));
		other = parser.parseSupportsCondition("(display: flexbox) or (not (display: inline-grid))", null);
		assertFalse(cond.equals(other));
	}

	@Test
	public void testEquals3() {
		CSSParser parser = new CSSParser();
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))",
				null);
		BooleanCondition other = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))",
				null);
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals("(display:table-cell) and (display:list-item) and (not ((display:run-in) or (display:table-cell)))",
				toMinifiedText(cond));
		other = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell))",
				null);
		assertFalse(cond.equals(other));
	}

	private static String toMinifiedText(BooleanCondition cond) {
		StringBuilder buf = new StringBuilder(32);
		cond.appendMinifiedText(buf);
		return buf.toString();
	}

}
