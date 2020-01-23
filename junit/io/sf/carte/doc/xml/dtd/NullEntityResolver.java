/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * A {@link EntityResolver2} that always returns {@code null}, except for
 * malformed {@code system ID} URLs (where it throws a {@link IOException}).
 */
public class NullEntityResolver implements EntityResolver2 {

	/**
	 * Construct the resolver.
	 */
	public NullEntityResolver() {
		super();
	}

	@Override
	public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
		return null;
	}

	@Override
	public final InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
			throws SAXException, IOException {
		return resolveEntity(publicId, systemId);
	}

	@Override
	public final InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId != null) {
			new URL(systemId);
		}
		return null;
	}

}
