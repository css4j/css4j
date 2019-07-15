/*

 Copyright (c) 1998-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class TestEntityResolver extends DefaultEntityResolver {

	private final MockURLFactory urlFactory;

	public TestEntityResolver() {
		urlFactory = new MockURLFactory();
		addHostToWhiteList("www.example.com");
	}

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return urlFactory.createConnection(url);
	}

}
