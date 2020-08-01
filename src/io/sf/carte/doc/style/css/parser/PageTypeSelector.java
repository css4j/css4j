/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

class PageTypeSelector extends AbstractPageSelector {

	PageTypeSelector(String name) {
		super(name);
	}

	@Override
	public Type getSelectorType() {
		return Type.PAGE_TYPE;
	}

	@Override
	public String getCssText() {
		return ParseHelper.escape(getName());
	}

}