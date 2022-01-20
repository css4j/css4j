/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;

import io.sf.carte.doc.style.css.nsac.Parser.NamespaceMap;

public class TestNamespaceMap extends HashMap<String, String> implements NamespaceMap {

	private static final long serialVersionUID = 1L;

	@Override
	public String getNamespaceURI(String nsPrefix) {
		return get(nsPrefix);
	}

}
