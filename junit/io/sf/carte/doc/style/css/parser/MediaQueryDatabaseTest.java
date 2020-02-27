/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
