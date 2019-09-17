/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.MediaQueryList;

public class BaseCSSStyleSheetFactoryTest {

	private TestCSSStyleSheetFactory factory;

	@Before
	public void setUp() {
		factory = new TestCSSStyleSheetFactory();
	}

	@Test
	public void testCreateUnmodifiable() {
		MediaQueryList mql = factory.createUnmodifiable("screen", null);
		assertNotNull(mql);
		assertEquals(1, mql.getLength());
		assertEquals("screen", mql.getMedia());
		//
		try {
			mql.appendMedium("print");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
		//
		try {
			mql.deleteMedium("print");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
		//
		try {
			mql.setMediaText("print");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
	}

}
