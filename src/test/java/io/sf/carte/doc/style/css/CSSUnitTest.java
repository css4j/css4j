/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

public class CSSUnitTest {

	@Test
	public void testIsLengthUnitType() {
		assertTrue(CSSUnit.isLengthUnitType(CSSUnit.CSS_PX));
		assertTrue(CSSUnit.isLengthUnitType(CSSUnit.CSS_PT));
		assertTrue(CSSUnit.isLengthUnitType(CSSUnit.CSS_EM));
		assertFalse(CSSUnit.isLengthUnitType(CSSUnit.CSS_DPCM));
		assertFalse(CSSUnit.isLengthUnitType(CSSUnit.CSS_TURN));
		assertFalse(CSSUnit.isLengthUnitType(CSSUnit.CSS_NUMBER));
		assertFalse(CSSUnit.isLengthUnitType(CSSUnit.CSS_INVALID));
	}

	@Test
	public void testIsAngleUnitType() {
		assertTrue(CSSUnit.isAngleUnitType(CSSUnit.CSS_DEG));
		assertTrue(CSSUnit.isAngleUnitType(CSSUnit.CSS_RAD));
		assertTrue(CSSUnit.isAngleUnitType(CSSUnit.CSS_TURN));
		assertFalse(CSSUnit.isAngleUnitType(CSSUnit.CSS_EM));
		assertFalse(CSSUnit.isAngleUnitType(CSSUnit.CSS_NUMBER));
		assertFalse(CSSUnit.isAngleUnitType(CSSUnit.CSS_INVALID));
	}

	@Test
	public void testDimensionUnitString() {
		assertEquals("px", CSSUnit.dimensionUnitString(CSSUnit.CSS_PX));
		assertEquals("em", CSSUnit.dimensionUnitString(CSSUnit.CSS_EM));
		assertEquals(0, CSSUnit.dimensionUnitString(CSSUnit.CSS_NUMBER).length());
		assertEquals(0, CSSUnit.dimensionUnitString(CSSUnit.CSS_INVALID).length());
	}

	@Test
	public void testDimensionUnitStringError() {
		try {
			CSSUnit.dimensionUnitString((short) 12345);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
	}

}
