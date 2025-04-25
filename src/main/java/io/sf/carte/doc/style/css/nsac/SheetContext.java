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
 * Provides a style sheet context, useful for selector serialization.
 */
public interface SheetContext extends NamespacePrefixMap {

	/**
	 * Check whether the given factory flag is set.
	 * 
	 * @param flag the flag.
	 * @return {@code true} if the flag is set.
	 */
	boolean hasFactoryFlag(short flag);

}
