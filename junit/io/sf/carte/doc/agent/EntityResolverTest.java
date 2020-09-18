/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test {@code DefaultEntityResolver} in the {@code xml-dtd} module from this module.
 * 
 */
public class EntityResolverTest {

	TestEntityResolver resolver;

	@Before
	public void setUp() {
		resolver = new TestEntityResolver();
	}

	@Test
	public void testResolveEntity() throws SAXException, IOException {
		InputSource is = resolver.resolveEntity(null, "http://www.w3.org/MarkUp/DTD/xhtml-meta-1.mod");
		assertNotNull(is);
		is.getCharacterStream().close();
	}

	@Test
	public void testRegisterSystemIdFilename() throws SAXException, IOException {
		assertTrue(resolver.registerSystemIdFilename("http://example.com/dtd/sample.dtd",
				"/io/sf/carte/doc/agent/sample.dtd"));
		InputSource is = resolver.resolveEntity(null, "http://example.com/dtd/sample.dtd");
		assertNotNull(is);
		is.getCharacterStream().close();
	}

}
