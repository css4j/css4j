/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Locator;

/**
 * {@link Locator} implementation.
 */
class LocatorImpl implements Locator, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final int line;
	private final int column;

	LocatorImpl(int line, int column) {
		super();
		this.line = line;
		this.column = column;
	}

	@Override
	public int getLineNumber() {
		return line;
	}

	@Override
	public int getColumnNumber() {
		return column;
	}

}
