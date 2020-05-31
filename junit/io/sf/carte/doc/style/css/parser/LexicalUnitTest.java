/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class LexicalUnitTest {

	private Parser parser;

	@Before
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
		//
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Very", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		assertSame(lu, lu2);
		assertFalse(lu.isParameter());
		//
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		assertSame(lu, lu.getNextLexicalUnit().getPreviousLexicalUnit());
		//
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
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
		//
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
		LexicalUnit lu = parsePropertyValue("inset 10px 5px 5px blue");
		LexicalUnit nlu = lu.getNextLexicalUnit();
		LexicalUnit replacement = lu.replaceBy(null);
		assertNotNull(replacement);
		assertNull(lu.getPreviousLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("10px 5px 5px blue", replacement.toString());
		assertSame(nlu, replacement);
	}

	@Test
	public void testReplaceByNull2() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Monospace Regular");
		LexicalUnit nlu = lu.getNextLexicalUnit();
		LexicalUnit replacement = nlu.replaceBy(null);
		assertNull(replacement);
		assertNull(nlu.getPreviousLexicalUnit());
		assertNull(nlu.getNextLexicalUnit());
		assertEquals("Monospace", lu.toString());
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
	public void testEquals() throws CSSException, IOException {
		LexicalUnit lu = parsePropertyValue("Sans Serif");
		LexicalUnit lu2 = parsePropertyValue("Lucida Sans");
		assertNotEquals(lu, lu2);
		assertEquals(lu, lu2.getNextLexicalUnit());
		//
		lu = parsePropertyValue("calc(2 * (3 + 2))");
		lu2 = parsePropertyValue("calc(2 * (3 + 2)) 7");
		assertEquals(lu, lu2);
		assertNotEquals(lu, lu2.getNextLexicalUnit());
		//
		lu2 = parsePropertyValue("calc(2 * (3 + 1))");
		assertNotEquals(lu, lu2);
		//
		lu2 = parsePropertyValue("calc(2 * (3 + 2) * 5)");
		assertNotEquals(lu, lu2);
		//
		lu2 = parsePropertyValue("calc(2)");
		assertNotEquals(lu, lu2);
		//
		lu = parsePropertyValue("calc(2)");
		lu2 = parsePropertyValue("calc(2 * (3 + 2))");
		assertNotEquals(lu, lu2);
	}

	private LexicalUnit parsePropertyValue(String value) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(value));
	}

}
