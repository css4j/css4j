/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class LexicalUnitTest {

	private Parser parser;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testClone() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times Roman");
		LexicalUnit clone = lu.clone();
		assertNotNull(clone.getNextLexicalUnit());
		assertNull(clone.getNextLexicalUnit().getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertFalse(clone.isParameter());
		assertEquals(lu, clone);
		assertEquals(lu.toString(), clone.toString());
	}

	@Test
	public void testClone2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times New Roman");
		LexicalUnit clone = lu.getNextLexicalUnit().clone();
		assertNotNull(clone.getNextLexicalUnit());
		assertNull(clone.getNextLexicalUnit().getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertFalse(clone.isParameter());
		assertEquals(lu.getNextLexicalUnit(), clone);
		assertEquals("New Roman", clone.toString());
	}

	@Test
	public void testCloneCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(2 * (3 + 2))");
		LexicalUnit clone = lu.clone();
		assertTrue(lu.getParameters().isParameter());
		assertFalse(lu.isParameter());
		assertTrue(clone.getParameters().isParameter());
		assertFalse(clone.isParameter());
		assertNotNull(clone.getParameters());
		assertTrue(clone.getParameters().isParameter());
		assertTrue(clone.getParameters().getNextLexicalUnit().isParameter());
		assertNull(clone.getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertEquals(lu, clone);
		assertEquals(lu.toString(), clone.toString());
	}

	@Test
	public void testCloneFunction() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo(arg1, arg2) bar(arg3)");
		LexicalUnit clone = lu.clone();
		assertNotNull(clone.getNextLexicalUnit());
		assertNull(clone.getNextLexicalUnit().getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertFalse(clone.isParameter());
		assertEquals(lu, clone);
		LexicalUnit param = clone.getParameters();
		assertNotNull(param);
		assertTrue(param.isParameter());
		assertEquals(lu.getParameters(), param);
		assertEquals(lu.toString(), clone.toString());
	}

	@Test
	public void testShallowClone() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times Roman");
		LexicalUnit clone = lu.shallowClone();
		assertNull(clone.getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertFalse(clone.isParameter());
		assertEquals(lu, clone);
		assertEquals(lu.getCssText(), clone.toString());
	}

	@Test
	public void testShallowCloneFunction() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo(arg1, arg2) bar(arg3)");
		LexicalUnit clone = lu.shallowClone();
		assertNull(clone.getNextLexicalUnit());
		assertNull(clone.getPreviousLexicalUnit());
		assertFalse(clone.isParameter());
		assertEquals(lu, clone);
		LexicalUnit param = clone.getParameters();
		assertNotNull(param);
		assertTrue(param.isParameter());
		assertEquals(lu.getParameters(), param);
		assertEquals(lu.getCssText(), clone.toString());
		LexicalUnit pclone = param.shallowClone();
		assertNotNull(pclone);
		assertNull(pclone.getNextLexicalUnit());
		assertFalse(pclone.isParameter());
	}

	@Test
	public void testIsParameter() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times Roman");
		assertFalse(lu.getNextLexicalUnit().isParameter());
	}

	@Test
	public void testInsertNextLexicalUnit() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times Roman");
		LexicalUnit lu2 = parsePropertyValue("New");
		lu.insertNextLexicalUnit(lu2);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
	}

	@Test
	public void testInsertNextLexicalUnit2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times Roman");
		LexicalUnit lu2 = parsePropertyValue("Very New");
		lu.insertNextLexicalUnit(lu2);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		assertFalse(lu2.isParameter());
		assertFalse(lu.isParameter());

		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Very", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		assertSame(lu, lu2);
		assertFalse(lu.isParameter());

		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());

		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
	}

	@Test
	public void testInsertNextLexicalUnitEmpty() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Courier New");

		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		parser.parseStyleDeclaration(new StringReader("--foo:;"));
		LexicalUnit empty = handler.lexicalValues.get(0);
		assertEquals(LexicalUnit.LexicalType.EMPTY, empty.getLexicalUnitType());

		lu.insertNextLexicalUnit(empty);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Courier", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, nlu.getLexicalUnitType());
		assertEquals("New", nlu.getStringValue());
		assertNull(nlu.getNextLexicalUnit());

		lu.getNextLexicalUnit().insertNextLexicalUnit(empty);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Courier New", lu.toString());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testInsertNextLexicalUnitSubvalue() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("function(one three)");
		LexicalUnit lu2 = parsePropertyValue("two");
		LexicalUnit param = lu.getParameters();
		param.insertNextLexicalUnit(lu2);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("one", param.getStringValue());
		assertSame(param, param.getNextLexicalUnit().getPreviousLexicalUnit());
		assertTrue(param.isParameter());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("two", param.getStringValue());
		assertSame(lu2, param.getNextLexicalUnit().getPreviousLexicalUnit());
		assertTrue(param.isParameter());
		param = param.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("three", param.getStringValue());
		assertTrue(param.isParameter());

		lu2 = parsePropertyValue("four");
		param.insertNextLexicalUnit(lu2);
		assertEquals("three", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("four", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertSame(param, param.getPreviousLexicalUnit().getNextLexicalUnit());
		assertTrue(param.isParameter());
	}

	@Test
	public void testInsertNextLexicalUnitFailPrev() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo(1 3 4)");
		LexicalUnit lu2 = parsePropertyValue("1 2");
		LexicalUnit param = lu.getParameters();
		LexicalUnit nlu2 = lu2.getNextLexicalUnit();
		try {
			param.insertNextLexicalUnit(nlu2);
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testInsertNextLexicalUnitFailParam() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1 4 5");
		LexicalUnit lu2 = parsePropertyValue("foo(2 3)");
		LexicalUnit param = lu2.getParameters();
		try {
			lu.insertNextLexicalUnit(param);
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testReplaceBy() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times Roman");
		LexicalUnit lu2 = parsePropertyValue("New");
		LexicalUnit replacement = lu.replaceBy(lu2);
		assertSame(lu2, replacement);
		assertEquals("New Roman", lu2.toString());
		assertEquals("Times", lu.toString());
	}

	@Test
	public void testReplaceBy2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Monospace Regular");
		LexicalUnit lu2 = parsePropertyValue("Bold");
		LexicalUnit replacement = lu.getNextLexicalUnit().replaceBy(lu2);
		assertSame(lu2, replacement);
		assertEquals("Bold", lu2.toString());
		assertEquals("Monospace Bold", lu.toString());
	}

	@Test
	public void testReplaceBy3() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times New Roman");
		LexicalUnit lu2 = parsePropertyValue("Very Old");
		LexicalUnit replacement = lu.getNextLexicalUnit().replaceBy(lu2);
		assertSame(lu2, replacement);
		assertEquals("Very Old Roman", lu2.toString());
		assertEquals("Times Very Old Roman", lu.toString());
	}

	@Test
	public void testReplaceByNull() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("inset 10px 5px 6px blue");
		LexicalUnit nlu = lu.getNextLexicalUnit();
		LexicalUnit replacement = lu.replaceBy(null);
		assertNotNull(replacement);
		assertNull(lu.getPreviousLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("10px 5px 6px blue", replacement.toString());
		assertSame(nlu, replacement);
	}

	@Test
	public void testReplaceByEmpty() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("inset 10px 5px 6px blue");
		LexicalUnit nlu = lu.getNextLexicalUnit();

		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		parser.parseStyleDeclaration(new StringReader("--foo:;"));
		LexicalUnit empty = handler.lexicalValues.get(0);
		assertEquals(LexicalUnit.LexicalType.EMPTY, empty.getLexicalUnitType());

		LexicalUnit replacement = lu.replaceBy(empty);
		assertNotNull(replacement);
		assertNull(lu.getPreviousLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertNull(replacement.getPreviousLexicalUnit());
		assertEquals("inset", lu.toString());
		assertEquals("10px 5px 6px blue", replacement.toString());
		assertSame(nlu, replacement);

		replacement = nlu.getNextLexicalUnit().replaceBy(empty);
		assertEquals("6px blue", replacement.toString());
		assertEquals("10px 6px blue", nlu.toString());

		replacement = replacement.getNextLexicalUnit().replaceBy(empty);
		assertNull(replacement);
		assertEquals("10px 6px", nlu.toString());
	}

	@Test
	public void testRemove() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("inset 10px 5px 5px blue");
		LexicalUnit nlu = lu.getNextLexicalUnit();
		LexicalUnit replacement = lu.remove();
		assertNotNull(replacement);
		assertNull(lu.getPreviousLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("10px 5px 5px blue", replacement.toString());
		assertSame(nlu, replacement);
	}

	@Test
	public void testRemove2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Monospace Regular");
		LexicalUnit nlu = lu.getNextLexicalUnit();
		LexicalUnit replacement = nlu.remove();
		assertNull(replacement);
		assertNull(nlu.getPreviousLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());
		assertEquals("Monospace", lu.toString());
	}

	@Test
	public void testRemoveCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(3*2)");
		LexicalUnit param = lu.getParameters();
		LexicalUnit nextparam = param.getNextLexicalUnit();
		LexicalUnit replacement = param.remove();
		assertNotNull(replacement);
		assertNull(param.getPreviousLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("*2", replacement.toString());
		assertSame(nextparam, replacement);
		assertSame(nextparam, lu.getParameters());
	}

	@Test
	public void testReplaceByNullParam() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo(1 2 3 4)");
		LexicalUnit param = lu.getParameters();
		LexicalUnit nlu = param.getNextLexicalUnit();
		LexicalUnit replacement = param.replaceBy(null);
		assertNotNull(replacement);
		assertNull(param.getPreviousLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("2 3 4", replacement.toString());
		assertEquals("foo(2 3 4)", lu.toString());
		assertSame(nlu, replacement);
	}

	@Test
	public void testRemoveParam() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo(1 2 3 4)");
		LexicalUnit param = lu.getParameters();
		LexicalUnit nlu = param.getNextLexicalUnit();
		LexicalUnit replacement = param.remove();
		assertNotNull(replacement);
		assertNull(param.getPreviousLexicalUnit());
		assertNull(param.getNextLexicalUnit());
		assertEquals("2 3 4", replacement.toString());
		assertEquals("foo(2 3 4)", lu.toString());
		assertSame(nlu, replacement);
	}

	@Test
	public void testReplaceByArgFail() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("foo(1 2 3 4)");
		LexicalUnit lu2 = parsePropertyValue("0 1");
		LexicalUnit param = lu.getParameters();
		LexicalUnit nlu2 = lu2.getNextLexicalUnit();
		try {
			param.replaceBy(nlu2);
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testReplaceByArgFail2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("1 2 3");
		LexicalUnit lu2 = parsePropertyValue("foo(0 1)");
		LexicalUnit param = lu2.getParameters();
		try {
			lu.replaceBy(param);
			fail("Must throw exception.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testReplaceByCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(2 * (3 + 2))");
		LexicalUnit lu2 = parsePropertyValue("5");
		LexicalUnit replacement = lu.getParameters().replaceBy(lu2);
		assertSame(lu2, replacement);
		assertEquals("5*(3 + 2)", lu2.toString());
		assertEquals("calc(5*(3 + 2))", lu.toString());
	}

	@Test
	public void testReplaceByCalc2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(2 * (3 + 2))");
		LexicalUnit lu2 = parsePropertyValue("5");
		LexicalUnit secondOp = lu.getParameters().getNextLexicalUnit().getNextLexicalUnit();
		LexicalUnit replacement = secondOp.replaceBy(lu2);
		assertSame(lu2, replacement);
		assertEquals("5", lu2.toString());
		assertEquals("calc(2*5)", lu.toString());
	}

	@Test
	public void testCountReplaceBy() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times New Roman");
		LexicalUnit lu2 = parsePropertyValue("Very Old");
		int count = lu.getNextLexicalUnit().countReplaceBy(lu2);
		assertEquals(2, count);
		assertEquals("Very Old Roman", lu2.toString());
		assertEquals("Times Very Old Roman", lu.toString());
	}

	@Test
	public void testCountReplaceByCalc() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("calc(2 * (3 + 2))");
		LexicalUnit lu2 = parsePropertyValue("5");
		int count = lu.getParameters().countReplaceBy(lu2);
		assertEquals(1, count);
		assertEquals("5*(3 + 2)", lu2.toString());
		assertEquals("calc(5*(3 + 2))", lu.toString());
	}

	@Test
	public void testCountReplaceByEmpty() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Times New Roman");

		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		parser.parseStyleDeclaration(new StringReader("--foo:;"));
		LexicalUnit empty = handler.lexicalValues.get(0);
		assertEquals(LexicalUnit.LexicalType.EMPTY, empty.getLexicalUnitType());

		LexicalUnit nlu = lu.getNextLexicalUnit();
		int count = nlu.countReplaceBy(empty);
		assertEquals(1, count);
		assertNull(nlu.getPreviousLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());
		assertEquals("New", nlu.toString());
		assertEquals("Times Roman", lu.toString());
		nlu = lu.getNextLexicalUnit();
		assertNull(nlu.getNextLexicalUnit());

		count = lu.countReplaceBy(empty);
		assertEquals(1, count);
		assertEquals("Times", lu.toString());
		assertEquals("Roman", nlu.toString());
		assertNull(lu.getPreviousLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertNull(nlu.getPreviousLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());

		lu = parsePropertyValue("Courier New");
		count = lu.getNextLexicalUnit().countReplaceBy(empty);
		assertEquals(1, count);
		assertEquals("Courier", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testEquals() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Sans Serif");
		LexicalUnit lu2 = parsePropertyValue("Lucida Sans");
		assertNotEquals(lu, lu2);
		assertEquals(lu, lu2.getNextLexicalUnit());

		lu = parsePropertyValue("calc(2 * (3 + 2))");
		lu2 = parsePropertyValue("calc(2 * (3 + 2)) 7");
		assertEquals(lu, lu2);
		assertNotEquals(lu, lu2.getNextLexicalUnit());

		lu2 = parsePropertyValue("calc(2 * (3 + 1))");
		assertNotEquals(lu, lu2);

		lu2 = parsePropertyValue("calc(2 * (3 + 2) * 5)");
		assertNotEquals(lu, lu2);

		lu2 = parsePropertyValue("calc(2)");
		assertNotEquals(lu, lu2);

		lu = parsePropertyValue("calc(2)");
		lu2 = parsePropertyValue("calc(2 * (3 + 2))");
		assertNotEquals(lu, lu2);

		lu2 = parsePropertyValue("c\\41 lc(2)");
		assertEquals(lu, lu2);
		assertEquals(lu.hashCode(), lu2.hashCode());
	}

	@Test
	public void testEquals2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("#444");
		LexicalUnit lu2 = parsePropertyValue("#444");
		assertEquals(lu, lu2);
		assertEquals(lu.hashCode(), lu2.hashCode());

		lu2 = parsePropertyValue("rgb(68,68,68)");
		assertNotEquals(lu, lu2);
		assertNotEquals(lu.hashCode(), lu2.hashCode());
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(value));
	}

}
