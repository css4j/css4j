/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

/**
 * Universal selector (for all namespaces).
 */
class ScopeSelector extends AbstractSelector {

	@Override
	public SelectorType getSelectorType() {
		return SelectorType.SCOPE_MARKER;
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode();
	}

	@Override
	public String toString() {
		return "";
	}

}
