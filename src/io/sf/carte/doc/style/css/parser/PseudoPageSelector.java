/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

class PseudoPageSelector extends AbstractPageSelector {

	PseudoPageSelector(String name) {
		super(name);
	}

	@Override
	public Type getSelectorType() {
		return Type.PSEUDO_PAGE;
	}

	@Override
	public String getCssText() {
		return ':' + getName();
	}

}
