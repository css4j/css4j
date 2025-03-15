/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

class UnicodeWildcardUnitImpl extends UnicodeUnitImpl {

	private static final long serialVersionUID = 1L;

	public UnicodeWildcardUnitImpl() {
		super(LexicalType.UNICODE_WILDCARD);
	}

	@Override
	CharSequence currentToString() {
		return getStringValue();
	}

	@Override
	UnicodeWildcardUnitImpl instantiateLexicalUnit() {
		return new UnicodeWildcardUnitImpl();
	}

}
