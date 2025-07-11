/*

 Copyright (c) 1998-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.agent;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class TestEntityResolver extends DefaultEntityResolver {

	private final MockURLConnectionFactory urlFactory;

	public TestEntityResolver() {
		urlFactory = new MockURLConnectionFactory();
		addHostToWhiteList("www.example.com");
	}

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return urlFactory.createConnection(url);
	}

	@Override
	protected boolean registerSystemIdFilename(String systemId, String filename) {
		return super.registerSystemIdFilename(systemId, filename);
	}

}
