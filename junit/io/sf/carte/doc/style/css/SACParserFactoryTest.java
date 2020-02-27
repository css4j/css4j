/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SACParserFactoryTest {

	@Test
	public void testCreateSACParser() {
		assertNotNull(SACParserFactory.createSACParser());
	}

}
