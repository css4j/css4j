/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

interface LexicalUnitFactory {

	LexicalUnitImpl createUnit();

	/**
	 * Get the name of the function with a canonical capitalization.
	 * 
	 * @param lcName the lower case name.
	 * @return the canonical name.
	 */
	default String canonicalName(String lcName) {
		return lcName;
	}

	/**
	 * Validate the given function lexical unit.
	 * <p>
	 * If validation fails, it may report the error and return {@code true}, or just
	 * return {@code false}.
	 * </p>
	 * <p>
	 * Validation may report warnings.
	 * </p>
	 * @param handler the handler that manages errors.
	 * @param index   the index at which the lexical unit ends.
	 * @param lu      the lexical unit. Parameters cannot be null.
	 * 
	 * @return {@code true} if validated successfully, or it didn't but the error
	 *         was already reported; {@code false} if a generic error should be
	 *         reported.
	 */
	default boolean validate(CSSContentHandler handler, final int index, LexicalUnitImpl lu) {
		/*
		 * Check that the function does not end with an algebraic operator.
		 */
		LexicalType type = CSSParser.findLastValue(lu.parameters).getLexicalUnitType();
		return !CSSParser.typeIsAlgebraicOperator(type);
	}

	default void handle(ValueTokenHandler parent) {
	}

}
