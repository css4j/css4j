/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

public class SupportsConditionTest {

	private CSSParser parser;

	@BeforeEach
	public void setUp() {
		this.parser = new CSSParser();
	}

	@Test
	public void testParseSupportsCondition() {
		BooleanCondition cond = parseSupportsCondition(
				"((-webkit-backdrop-filter: saturate(180%) blur(20px)) or (backdrop-filter: saturate(180%) blur(20px)))");
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
		BooleanCondition cond = parseSupportsCondition(
				"(display:table-cell) and (display:list-item)");
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(display: table-cell) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsCondition3() {
		BooleanCondition cond = parseSupportsCondition(
				"((display: table-cell) and (display: list-item) and (display: run-in)) or ((display: table-cell) and (not (display: inline-grid)))");
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
		BooleanCondition cond = parseSupportsCondition(
				"(background: -webkit-gradient(linear, left top, left bottom, from(transparent), to(#fff))) or (background: -webkit-linear-gradient(transparent, #fff)) or (background: -moz-linear-gradient(transparent, #fff)) or (background: -o-linear-gradient(transparent, #fff)) or (background: linear-gradient(transparent, #fff))");
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
		BooleanCondition cond = parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell))");
		assertNotNull(cond);
		assertEquals(
				"(display:table-cell) and (display:list-item) and ((not (display:run-in)) or (display:table-cell))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and (display: list-item) and ((not (display: run-in)) or (display: table-cell))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionUnknownValue() {
		BooleanCondition cond = parseSupportsCondition("(foo:bar(a&b)) and (display:list-item)");
		assertNotNull(cond);
		assertEquals("(foo:bar(a&b)) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(foo:bar(a&b)) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsConditionAndSelector() {
		BooleanCondition cond = parseSupportsCondition(
				"((selector(:has(*))) or (selector(:not(*)))) and (display: table-cell)");
		assertNotNull(cond);
		assertEquals("(selector(:has(*)) or selector(:not(*))) and (display:table-cell)",
				toMinifiedText(cond));
		assertEquals("(selector(:has(*)) or selector(:not(*))) and (display: table-cell)",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNotSelector() {
		BooleanCondition cond = parseSupportsCondition("not selector(:has(*))");
		assertNotNull(cond);
		assertEquals("not selector(:has(*))", toMinifiedText(cond));
		assertEquals("not selector(:has(*))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionSelector() {
		BooleanCondition cond = parseSupportsCondition("selector(:has(*))");
		assertNotNull(cond);
		assertEquals("selector(:has(*))", toMinifiedText(cond));
		assertEquals("selector(:has(*))", cond.toString());
	}

	@Test
	public void testParseSupportsConditionUnknownSelector() {
		BooleanCondition cond = parseSupportsCondition("selector([foo&^bar]) and (display:flex)");
		assertNotNull(cond);
		assertEquals("selector([foo&^bar]) and (display:flex)", toMinifiedText(cond));
		assertEquals("selector([foo&^bar]) and (display: flex)", cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr() {
		BooleanCondition cond = parseSupportsCondition(
				"(display:table-cell) and ((display:list-item) or (display:flex))");
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (display:flex))",
				toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or (display: flex))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr2() {
		BooleanCondition cond = parseSupportsCondition(
				"(display:table-cell) and (((display:list-item) or (display:flex)))");
		assertNotNull(cond);
		assertEquals("(display:table-cell) and ((display:list-item) or (display:flex))",
				toMinifiedText(cond));
		assertEquals("(display: table-cell) and ((display: list-item) or (display: flex))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr3() {
		BooleanCondition cond = parseSupportsCondition(
				"(display:table-cell) and ((((display:list-item)) or (((display:flex)) and ((display:foo)))))");
		assertNotNull(cond);
		assertEquals(
				"(display:table-cell) and ((display:list-item) or ((display:flex) and (display:foo)))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and ((display: list-item) or ((display: flex) and (display: foo)))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr4() {
		BooleanCondition cond = parseSupportsCondition(
				"((display:table-cell)) and ((((display:list-item)) or ((((display:flex)) and ((display:foo))))))");
		assertNotNull(cond);
		assertEquals(
				"(display:table-cell) and ((display:list-item) or ((display:flex) and (display:foo)))",
				toMinifiedText(cond));
		assertEquals(
				"(display: table-cell) and ((display: list-item) or ((display: flex) and (display: foo)))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedOr5() {
		BooleanCondition cond = parseSupportsCondition(
				"(display: table-cell) and ((display: list-item) or (not ((display: run-in) or (display: table-cell))))");
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
		BooleanCondition cond = parseSupportsCondition(
				"(display:table-cell) or ((display:list-item) and (display:flex))");
		assertNotNull(cond);
		assertEquals("(display:table-cell) or ((display:list-item) and (display:flex))",
				toMinifiedText(cond));
		assertEquals("(display: table-cell) or ((display: list-item) and (display: flex))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionNestedAnd2() {
		BooleanCondition cond = parseSupportsCondition(
				"(display:table-cell) or (((display:list-item) and (display:flex)))");
		assertNotNull(cond);
		assertEquals("(display:table-cell) or ((display:list-item) and (display:flex))",
				toMinifiedText(cond));
		assertEquals("(display: table-cell) or ((display: list-item) and (display: flex))",
				cond.toString());
	}

	@Test
	public void testParseSupportsConditionComments() {
		BooleanCondition cond = parseSupportsCondition(
				"/*comment 1*/(display:table-cell)/*comment 2*/and(display:list-item)/*comment 3*/");
		assertNotNull(cond);
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		assertEquals("(display: table-cell) and (display: list-item)", cond.toString());
	}

	@Test
	public void testParseSupportsConditionEmpty() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition(""));
		assertEquals(1, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionEmpty2() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition(" "));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionEmpty3() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition("()"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition("(display:table-cell) and (display:list-item"));
		assertEquals(44, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad2() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition("(display:table-cell) and (display:list-item))"));
		assertEquals(45, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad3() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition("(display foo:table-cell) and (display:list-item)"));
		assertEquals(10, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad4() {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSupportsCondition(
				"((transition-property: color) or (animation-name: foo) and (transform: rotate(10deg)))"));
		assertEquals(56, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad5() {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSupportsCondition(
				"((transition-property: color) and (animation-name: foo) or (transform: rotate(10deg)))"));
		assertEquals(57, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad6() {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSupportsCondition(
				"(transition-property: color) and ((animation-name: foo) or (animation-name: bar) and (transform: rotate(10deg)))"));
		assertEquals(82, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad7() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition("(((display):table-cell) and (display:list-item))"));
		assertEquals(11, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad8() {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSupportsCondition("'foo' (display:table-cell) and (display:list-item)"));
		assertEquals(1, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad9() {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSupportsCondition(
				"(transition-property: color) or (animation-name: foo) and (transform: rotate(10deg))"));
		assertEquals(55, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsConditionBad10() {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSupportsCondition(
				"(transition-property: color) and (animation-name: foo) or (transform: rotate(10deg))"));
		assertEquals(56, ex.getColumnNumber());
	}

	@Test
	public void testParseSupportsCondition_Or_And_Many_Parens() {
		BooleanCondition cond = parseSupportsCondition(
				"((((transition-property: color) or (animation-name: foo)))) and (transform: rotate(10deg))");
		assertNotNull(cond);
		assertEquals(
				"((transition-property:color) or (animation-name:foo)) and (transform:rotate(10deg))",
				toMinifiedText(cond));
		assertEquals(
				"((transition-property: color) or (animation-name: foo)) and (transform: rotate(10deg))",
				cond.toString());
	}

	@Test
	public void testParseSupportsCondition_And_Or_Many_Parens() {
		BooleanCondition cond = parseSupportsCondition(
				"((((transition-property: color) and (animation-name: foo)))) or (transform: rotate(10deg))");
		assertNotNull(cond);
		assertEquals(
				"((transition-property:color) and (animation-name:foo)) or (transform:rotate(10deg))",
				toMinifiedText(cond));
		assertEquals(
				"((transition-property: color) and (animation-name: foo)) or (transform: rotate(10deg))",
				cond.toString());
	}

	@Test
	public void testEquals() {
		BooleanCondition cond = parseSupportsCondition(
				"(display: table-cell) and (display: list-item)");
		BooleanCondition other = parseSupportsCondition(
				"(display: table-cell) and (display: list-item)");
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals("(display:table-cell) and (display:list-item)", toMinifiedText(cond));
		other = parseSupportsCondition("(display: table-cell) and (display: foo)");
		assertFalse(cond.equals(other));
		other = parseSupportsCondition("(display: table-cell)");
		assertFalse(cond.equals(other));
	}

	@Test
	public void testEquals2() {
		BooleanCondition cond = parseSupportsCondition(
				"(display: flexbox) and (not (display: inline-grid))");
		BooleanCondition other = parseSupportsCondition(
				"(display: flexbox) and (not (display: inline-grid))");
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals("(display:flexbox) and (not (display:inline-grid))", toMinifiedText(cond));
		other = parseSupportsCondition("(display: flexbox) and (display: inline-grid)");
		assertFalse(cond.equals(other));
		other = parseSupportsCondition("(display: flexbox) or (not (display: inline-grid))");
		assertFalse(cond.equals(other));
	}

	@Test
	public void testEquals3() {
		BooleanCondition cond = parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))");
		BooleanCondition other = parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not ((display: run-in) or (display: table-cell)))");
		assertTrue(cond.equals(other));
		assertEquals(cond.hashCode(), other.hashCode());
		assertEquals(
				"(display:table-cell) and (display:list-item) and (not ((display:run-in) or (display:table-cell)))",
				toMinifiedText(cond));
		other = parseSupportsCondition(
				"(display: table-cell) and (display: list-item) and (not (display: run-in) or (display: table-cell))");
		assertFalse(cond.equals(other));
	}

	private BooleanCondition parseSupportsCondition(String condition) {
		return parser.parseSupportsCondition(condition, null);
	}

	private static String toMinifiedText(BooleanCondition cond) {
		StringBuilder buf = new StringBuilder(32);
		cond.appendMinifiedText(buf);
		return buf.toString();
	}

}
