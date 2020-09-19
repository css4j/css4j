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

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

public class SupportsConditionTest {

	private CSSParser parser;

	@Before
	public void setUp() {
		this.parser = new CSSParser();
	}

	@Test
	public void testParseSupportsCondition() {
		BooleanCondition cond = parser.parseSupportsCondition(
				"((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)))",
				null);
		assertNotNull(cond);
		assertEquals(
				"(-webkit-backdrop-filter:saturate(180%) blur(20px)) or (backdrop-filter:saturate(180%) blur(20px))",
				toMinifiedText(cond));
		assertEquals(
				"(-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px))",
				cond.toString());
	}

	@Test
	public void testParseSupportsCondition2() {
		BooleanCondition cond = parser.parseSupportsCondition("(display:table-cell) and (display:list-item)", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(display: table-cell) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsCondition3() {
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
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell))",
				null);
		assertNotNull(cond);
		assertEquals(
				"(display:table-cell) and (display:list-item) and ((not (display:run-in)) or (display:table-cell))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr() {
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) and ((display:list-item) or (display:flex))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr2() {
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) and (((display:list-item) or (display:flex)))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr3() {
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
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display: table-cell) and ((display: list-item) or (not ((display: run-in) or (display: table-cell))))",
				null);
		assertNotNull(cond);
		assertEquals(
				"(display:table-cell) and ((display:list-item) or (not ((display:run-in) or (display:table-cell))))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and ((display: list-item) or (not ((display: run-in) or (display: table-cell))))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedAnd() {
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) or ((display:list-item) and (display:flex))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) or ((display:list-item) and (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) or ((display: list-item) and (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedAnd2() {
		BooleanCondition cond = parser
				.parseSupportsCondition("(display:table-cell) or (((display:list-item) and (display:flex)))", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) or ((display:list-item) and (display:flex))", toMinifiedText(cond));
		assertEquals("(display: table-cell) or ((display: list-item) and (display: flex))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionComments() {
		BooleanCondition cond = parser.parseSupportsCondition(
				"/*comment 1*/(display:table-cell)/*comment 2*/and(display:list-item)/*comment 3*/", null);
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(display: table-cell) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsConditionEmpty() {
		try {
			parser.parseSupportsCondition("", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionEmpty2() {
		try {
			parser.parseSupportsCondition(" ", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionEmpty3() {
		try {
			parser.parseSupportsCondition("()", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad() {
		try {
			parser.parseSupportsCondition("(display:table-cell) and (display:list-item", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad2() {
		try {
			parser.parseSupportsCondition("(display:table-cell) and (display:list-item))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad3() {
		try {
			parser.parseSupportsCondition("(display foo:table-cell) and (display:list-item)", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad4() {
		try {
			parser.parseSupportsCondition(
					"((transition-property: color) or (animation-name: foo) and (transform: rotate(10deg)))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad5() {
		try {
			parser.parseSupportsCondition(
					"(transition-property: color) or (animation-name: foo) and (transform: rotate(10deg))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad6() {
		try {
			parser.parseSupportsCondition(
					"((transition-property: color) and (animation-name: foo) or (transform: rotate(10deg)))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad7() {
		try {
			parser.parseSupportsCondition(
					"(transition-property: color) and (animation-name: foo) or (transform: rotate(10deg))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad8() {
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
		try {
			parser.parseSupportsCondition("(((display):table-cell) and (display:list-item))", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSupportsConditionBad10() {
		try {
			parser.parseSupportsCondition("'foo' (display:table-cell) and (display:list-item)", null);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testEquals() {
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
		BooleanCondition cond = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))",
				null);
		BooleanCondition other = parser.parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))",
				null);
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals(
				"(display:table-cell) and (display:list-item) and (not ((display:run-in) or (display:table-cell)))",
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
