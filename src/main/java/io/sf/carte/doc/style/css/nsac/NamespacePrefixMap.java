/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css.nsac;

/**
 * Maps a namespace URI to a prefix, useful for selector serialization.
 */
public interface NamespacePrefixMap extends Parser.NamespaceMap {

	/**
	 * Has the style sheet a default namespace?
	 * 
	 * @return {@code true} if it has a default namespace.
	 */
	boolean hasDefaultNamespace();

	/**
	 * Get the namespace prefix corresponding to the given URI.
	 * 
	 * @param namespaceURI the namespace URI.
	 * @return the prefix.
	 */
	String getNamespacePrefix(String namespaceURI);

	/**
	 * Register a namespace prefix - URI pair.
	 * 
	 * @param prefix       the namespace prefix.
	 * @param namespaceURI the namespace URI.
	 */
	void registerNamespacePrefix(String prefix, String namespaceURI);

}
