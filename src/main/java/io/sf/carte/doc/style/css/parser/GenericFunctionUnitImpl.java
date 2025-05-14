/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

class GenericFunctionUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public GenericFunctionUnitImpl() {
		super(LexicalType.FUNCTION);
	}

	public GenericFunctionUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	FunctionUnitImpl instantiateLexicalUnit() {
		return new GenericFunctionUnitImpl(getLexicalUnitType());
	}

}
