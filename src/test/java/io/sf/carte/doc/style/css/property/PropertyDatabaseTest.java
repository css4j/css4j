/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValue;

public class PropertyDatabaseTest {
	private static PropertyDatabase pdb;

	@BeforeAll
	public static void setUpBeforeClass() {
		pdb = PropertyDatabase.getInstance();
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
	public void getInitialValue() {
		CSSValue value = pdb.getInitialValue("width");
		assertNotNull(value);
		assertEquals("auto", value.getCssText());
		CSSValue value2 = pdb.getInitialValue("width");
		assertTrue(value == value2);
		assertNull(pdb.getInitialValue("foo"));
	}

	@Test
	public void testInitialValues() {
		for (String property : pdb.getKnownPropertySet()) {
			assertNotNull(pdb.getInitialValue(property));
		}
	}

	@Test
	public void testIsInherited() {
		assertFalse(pdb.isInherited("foo"));
		assertFalse(pdb.isInherited("width"));
		assertTrue(pdb.isInherited("font-size"));
	}

	@Test
	public void testIsKnownProperty() {
		assertFalse(pdb.isKnownProperty("foo"));
		assertTrue(pdb.isKnownProperty("width"));
		assertTrue(pdb.isKnownProperty("font-size"));
	}

}
