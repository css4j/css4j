/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

abstract class MathFunctionUnitImpl extends LexicalUnitImpl {

	private static final long serialVersionUID = 1L;

	private final int functionIndex;

	public MathFunctionUnitImpl(int functionIndex) {
		super(LexicalType.MATH_FUNCTION);
		this.functionIndex = functionIndex;
	}

	/**
	 * Gives the index of this mathematical function, according to
	 * {@link io.sf.carte.doc.style.css.MathFunctions MathFunctions} .
	 * 
	 * @return the function index.
	 */
	@Override
	public int getMathFunctionIndex() {
		return functionIndex;
	}

	@Override
	CharSequence currentToString() {
		StringBuilder buf = new StringBuilder(32);
		buf.append(value).append('(');
		LexicalUnit lu = this.parameters;
		if (lu != null) {
			buf.append(lu.toString());
		}
		buf.append(')');
		return buf;
	}

	/**
	 * Compute the dimension of this mathematical function.
	 * 
	 * @param analyzer the dimensional analyzer.
	 * @return the dimension, or {@code null} if it could not be computed.
	 * @throws DOMException if the function is unknown or invalid.
	 */
	public abstract Dimension dimension(DimensionalAnalyzer analyzer) throws DOMException;

	/*
	 * Do an abstract override so implementations do not forget to override.
	 */
	@Override
	abstract MathFunctionUnitImpl instantiateLexicalUnit();

}
