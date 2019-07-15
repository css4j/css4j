/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Extended {@link NamedNodeMap}.
 */
public interface ExtendedNamedNodeMap<T extends Node> extends NamedNodeMap, Iterable<T> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	T getNamedItem(String name);

	/**
	 * {@inheritDoc}
	 */
	@Override
	T item(int index);

	/**
	 * {@inheritDoc}
	 */
	@Override
	T getNamedItemNS(String namespaceURI, String localName) throws DOMException;

}
