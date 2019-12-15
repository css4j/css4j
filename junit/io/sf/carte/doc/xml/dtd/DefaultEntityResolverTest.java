/*

 Copyright (c) 1998-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.TestConfig;

public class DefaultEntityResolverTest {
	private static DefaultEntityResolver resolver;

	@BeforeClass
	public static void classFixture() {
		resolver = new DefaultEntityResolver();
	}

	@Test
	public void getExternalSubsetStringString() throws SAXException, IOException {
		InputSource isrc = resolver.getExternalSubset("html", null);
		assertNotNull(isrc);
		assertNull(isrc.getPublicId());
		assertNull(isrc.getSystemId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
		isrc = resolver.getExternalSubset("foo", null);
		assertNull(isrc);
	}

	@Test
	public void resolveEntityStringString() throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity(DocumentTypeDeclaration.XHTML1_TRA_PUBLICID,
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		assertNotNull(isrc);
		assertNotNull(isrc.getPublicId());
		assertEquals(DocumentTypeDeclaration.XHTML1_TRA_PUBLICID, isrc.getPublicId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
	}

	@Test
	public void resolveEntityStringString2() throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity(null, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		assertNotNull(isrc);
		assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", isrc.getPublicId());
		assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", isrc.getSystemId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
	}

	@Test
	public void resolveEntityStringStringXHTML11() throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity("-//W3C//DTD XHTML 1.1//EN", null);
		assertNotNull(isrc);
		assertEquals("-//W3C//DTD XHTML 1.1//EN", isrc.getPublicId());
		assertEquals("http://www.w3.org/MarkUp/DTD/xhtml11.dtd", isrc.getSystemId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
	}

	@Test
	public void resolveEntityStringStringXHTML11Meta() throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity(null, "http://www.w3.org/MarkUp/DTD/xhtml-meta-1.mod");
		assertNotNull(isrc);
		assertEquals("http://www.w3.org/MarkUp/DTD/xhtml-meta-1.mod", isrc.getSystemId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
	}

	@Test
	public void resolveEntityStringStringRemoteDisallow() throws SAXException, IOException {
		try {
			resolver.resolveEntity("-//OASIS//DTD DocBook XML V4.5//EN",
					"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd");
			fail("Must throw exception");
		} catch (SAXException e) {
		} catch (IOException e) {
			fail("Should throw SAXException, not IOException");
		}
	}

	@Test
	public void resolveEntityStringStringRemoteDisallowConstructor1Arg() throws SAXException, IOException {
		try {
			resolver.resolveEntity("-//W3C//DTD SVG 1.1//EN", "https://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd");
			fail("Must throw exception");
		} catch (SAXException e) {
		} catch (IOException e) {
			fail("Should throw SAXException, not IOException");
		}
	}

	@Test
	public void resolveEntityStringStringRemote() throws SAXException, IOException {
		if (TestConfig.REMOTE_TESTS) {
			resolver.addHostToWhiteList("www.oasis-open.org");
			InputSource isrc = resolver.resolveEntity("-//OASIS//DTD DocBook XML V4.5//EN",
					"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd");
			assertNotNull(isrc);
			assertEquals("-//OASIS//DTD DocBook XML V4.5//EN", isrc.getPublicId());
			assertEquals("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd", isrc.getSystemId());
			Reader re = isrc.getCharacterStream();
			assertNotNull(re);
			re.close();
			//
			resolver.addHostToWhiteList("css4j.github.io");
			assertNull(resolver.resolveEntity(null, "https://css4j.github.io/"));
			assertNull(resolver.resolveEntity(null, "https://css4j.github.io/faq.html"));
			assertNull(resolver.resolveEntity(null, "https://css4j.github.io/foo/badurl"));
			assertNull(resolver.resolveEntity(null, "https://css4j.github.io/foo/badurl.dtd"));
		}
	}

	@Test
	public void resolveEntityString() throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity(DocumentTypeDeclaration.XHTML1_TRA_DTDECL);
		assertNotNull(isrc);
		assertNotNull(isrc.getPublicId());
		assertEquals(DocumentTypeDeclaration.XHTML1_TRA_PUBLICID, isrc.getPublicId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
	}

	@Test
	public void resolveEntityDocumentTypeDeclaration() throws SAXException, IOException {
		InputSource isrc = resolver
				.resolveEntity(DocumentTypeDeclaration.parse(DocumentTypeDeclaration.XHTML1_TRA_DTDECL));
		assertNotNull(isrc);
		assertNotNull(isrc.getPublicId());
		assertEquals(DocumentTypeDeclaration.XHTML1_TRA_PUBLICID, isrc.getPublicId());
		Reader re = isrc.getCharacterStream();
		assertNotNull(re);
		re.close();
	}

	@Test
	public void resolveNonexistentDeclaration() {
		try {
			resolver.resolveEntity("hi");
			fail("Must throw exception");
		} catch (SAXException e) {
		} catch (IOException e) {
			fail("Should throw SAXException, not IOException");
		}
	}

	@Test
	public void resolveNonexistent() {
		InputSource isrc = null;
		try {
			isrc = resolver.resolveEntity("hi", null);
		} catch (Exception e) {
			fail("Should return null, not thow an Exception" + e.getLocalizedMessage());
		}
		assertNull(isrc);
	}

	@Test
	public void testIsInvalidPath() throws SAXException, IOException {
		assertTrue(resolver.isInvalidPath(new URL("http://dtd.example.com/etc/passwd").getPath()));
		assertTrue(resolver.isInvalidPath(new URL("http://dtd.example.com/etc/passwd#fake.dtd").getPath()));
		assertFalse(resolver.isInvalidPath(new URL("http://foo.example.com/bar.dtd").getPath()));
		assertTrue(resolver.isInvalidPath(new URL("http://dtd.example.com/etc/passwd").getPath()));
	}

	@Test
	public void testIsValidContentType() throws SAXException, IOException {
		assertTrue(resolver.isValidContentType("application/xml-dtd"));
		assertTrue(resolver.isValidContentType("application/xml-external-parsed-entity"));
		assertTrue(resolver.isValidContentType("text/xml-external-parsed-entity"));
		assertFalse(resolver.isValidContentType("text/html"));
		assertFalse(resolver.isValidContentType(null));
	}

}
