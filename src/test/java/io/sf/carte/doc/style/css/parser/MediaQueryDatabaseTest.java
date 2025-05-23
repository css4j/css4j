/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MediaQueryDatabaseTest {

	@Test
	public void testIsMediaFeature() {
		assertTrue(MediaQueryDatabase.isMediaFeature("color"));
		assertTrue(MediaQueryDatabase.isMediaFeature("width"));
		assertTrue(MediaQueryDatabase.isMediaFeature("pointer"));
		assertTrue(MediaQueryDatabase.isMediaFeature("orientation"));
		assertTrue(MediaQueryDatabase.isMediaFeature("aspect-ratio"));
		assertFalse(MediaQueryDatabase.isMediaFeature("foo"));
	}

}
