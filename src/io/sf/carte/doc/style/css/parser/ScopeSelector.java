/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Selector;

/**
 * Universal selector (for all namespaces).
 */
class ScopeSelector extends AbstractSelector {

	@Override
	public short getSelectorType() {
		return Selector.SAC_SCOPE_SELECTOR;
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
