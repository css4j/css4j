/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

abstract class LexicalCallbackTH extends CallbackTokenHandler implements LexicalProvider {

	LexicalUnitImpl currentlu;

	LexicalCallbackTH(LexicalProvider caller) {
		super(caller);
		currentlu = caller.getCurrentLexicalUnit();
	}

	@Override
	protected LexicalProvider getCaller() {
		return (LexicalProvider) caller;
	}

	@Override
	public void endFunctionArgument(int index) {
		if (currentlu != null && currentlu.ownerLexicalUnit != null) {
			currentlu = currentlu.ownerLexicalUnit;
		} else {
			getCaller().endFunctionArgument(index);
		}
	}

	/**
	 * Add a non-function (nor expression) lexical unit as the current value.
	 * 
	 * @param lu the lexical unit to add.
	 * @return the lexical unit that should be processed as the current unit.
	 */
	public LexicalUnitImpl addPlainLexicalUnit(LexicalUnitImpl lu) {
		if (isCurrentUnitAFunction()) {
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
			}
			currentlu = lu;
		}
		return lu;
	}

	LexicalUnitImpl addFunctionOrExpressionUnit(LexicalUnitImpl lu) {
		if (isCurrentUnitAFunction()) {
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
			}
		}
		currentlu = lu;
		return lu;
	}

	@Override
	public LexicalUnitImpl getCurrentLexicalUnit() {
		return currentlu;
	}

	@Override
	public void setCurrentLexicalUnit(LexicalUnitImpl currentlu) {
		addPlainLexicalUnit(currentlu);
	}

	@Override
	public boolean isCurrentUnitAFunction() {
		return getCaller().isCurrentUnitAFunction();
	}

	@Override
	public void addEmptyLexicalUnit() {
		LexicalUnitImpl empty = new LexicalUnitImpl(LexicalType.EMPTY);
		empty.value = "";
		addPlainLexicalUnit(empty);
	}

	@Override
	void yieldBack() {
		// Set the result
		LexicalProvider c = getCaller();
		c.setCurrentLexicalUnit(getCurrentLexicalUnit());

		super.yieldBack();
	}

	@Override
	public boolean hasLegacySupport() {
		return false;
	}

}
