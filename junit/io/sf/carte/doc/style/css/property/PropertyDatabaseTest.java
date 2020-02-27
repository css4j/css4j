/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.css.CSSValue;

public class PropertyDatabaseTest {
	private static PropertyDatabase pdb;

	@BeforeClass
	public static void setUpBeforeClass() {
		pdb = PropertyDatabase.getInstance();
	}

	@AfterClass
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
		Iterator<String> it = pdb.getKnownPropertySet().iterator();
		while (it.hasNext()) {
			String property = it.next();
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
