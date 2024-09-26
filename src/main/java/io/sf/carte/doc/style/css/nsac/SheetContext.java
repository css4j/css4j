/*

 Copyright (c) 2005-2024, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css.nsac;

/**
 * Provides a style sheet context, useful for selector serialization.
 */
public interface SheetContext {

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
	 * Check whether the given factory flag is set.
	 * 
	 * @param flag the flag.
	 * @return {@code true} if the flag is set.
	 */
	boolean hasFactoryFlag(short flag);

}
