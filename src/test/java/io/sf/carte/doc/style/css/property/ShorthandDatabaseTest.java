/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ShorthandDatabaseTest {
	private static ShorthandDatabase pdb;

	@BeforeAll
	public static void setUpBeforeClass() {
		pdb = ShorthandDatabase.getInstance();
	}

	@AfterAll
	public static void tearDownAfterClass() {
		pdb = null;
	}

	@Test
	public void getInstance() {
		assertNotNull(pdb);
	}

	@Test
	public void isIdentifierValue() {
		assertTrue(pdb.isIdentifierValue("border-color", "gray"));
	}

	@Test
	public void isShorthandSubpropertyString() {
		assertTrue(pdb.isShorthandSubproperty("border-right-style"));
	}

	@Test
	public void isShorthandSubpropertyOfStringString() {
		assertTrue(pdb.isShorthandSubpropertyOf("border-style", "border-right-style"));
		assertTrue(pdb.isShorthandSubpropertyOf("border-right", "border-right-style"));
		assertTrue(pdb.isShorthandSubpropertyOf("border", "border-right-style"));
		assertFalse(pdb.isShorthandSubpropertyOf("border", "font-family"));
	}

}
