/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

/**
 * Scope selector.
 */
class ScopeSelector extends AbstractSelector {

	private static final long serialVersionUID = 1L;

	@Override
	public SelectorType getSelectorType() {
		return SelectorType.SCOPE_MARKER;
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	NSACSelectorFactory getSelectorFactory() throws IllegalStateException {
		throw new IllegalStateException();
	}

}
