/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * A {@link EntityResolver2} that uses a main resolver and a fallback.
 * <p>
 * First, it attempts to resolve with the main resolver, if an exception is
 * thrown or {@code null} is returned, then the fallback is used.
 * </p>
 * <p>
 * If the first resolver threw a {@link SAXException} and the fallback one
 * returned {@code null}, that first exception is thrown, as it may contain
 * relevant information.
 * </p>
 */
public class StackedEntityResolver implements EntityResolver2 {

	private final EntityResolver2 firstResolver;

	private final EntityResolver2 fallbackResolver;

	/**
	 * Construct a resolver with the two stacked resolvers.
	 */
	public StackedEntityResolver(EntityResolver2 firstResolver, EntityResolver2 fallbackResolver) {
		super();
		this.firstResolver = firstResolver;
		this.fallbackResolver = fallbackResolver;
	}

	@Override
	public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
		InputSource source;
		SAXException ex = null;
		try {
			source = firstResolver.getExternalSubset(name, baseURI);
		} catch (SAXException e) {
			ex = e;
			source = null;
		} catch (IOException e) {
			source = null;
		}
		if (source == null) {
			source = fallbackResolver.getExternalSubset(name, baseURI);
			if (source == null && ex != null) {
				throw ex;
			}
		}
		return source;
	}

	@Override
	public final InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
			throws SAXException, IOException {
		InputSource source;
		SAXException ex = null;
		try {
			source = firstResolver.resolveEntity(name, publicId, baseURI, systemId);
		} catch (SAXException e) {
			ex = e;
			source = null;
		} catch (IOException e) {
			source = null;
		}
		if (source == null) {
			source = fallbackResolver.resolveEntity(name, publicId, baseURI, systemId);
			if (source == null && ex != null) {
				throw ex;
			}
		}
		return source;
	}

	@Override
	public final InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		InputSource source;
		SAXException ex = null;
		try {
			source = firstResolver.resolveEntity(publicId, systemId);
		} catch (SAXException e) {
			ex = e;
			source = null;
		} catch (IOException e) {
			source = null;
		}
		if (source == null) {
			source = fallbackResolver.resolveEntity(publicId, systemId);
			if (source == null && ex != null) {
				throw ex;
			}
		}
		return source;
	}

}
