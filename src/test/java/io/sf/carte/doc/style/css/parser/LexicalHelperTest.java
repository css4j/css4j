/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LexicalHelperTest {

	@Test
	void testGetBasicShapeIndexCount() {
		assertTrue(LexicalHelper.getBasicShapeIndexCount() > 0);
	}

	@Test
	void testGetColorIndexCount() {
		assertTrue(LexicalHelper.getColorIndexCount() > 0);
	}

	@Test
	void testGetCounterIndexCount() {
		assertTrue(LexicalHelper.getCounterIndexCount() > 0);
	}

	@Test
	void testGetEasingFunctionIndexCount() {
		assertTrue(LexicalHelper.getEasingFunctionIndexCount() > 0);
	}

	@Test
	void testGetImageFunctionIndexCount() {
		assertTrue(LexicalHelper.getImageFunctionIndexCount() > 0);
	}

	@Test
	void testGetMathFunctionIndexCount() {
		assertTrue(LexicalHelper.getMathFunctionIndexCount() > 0);
	}

	@Test
	void testGetTransformFunctionIndexCount() {
		assertTrue(LexicalHelper.getTransformFunctionIndexCount() > 0);
	}

}
