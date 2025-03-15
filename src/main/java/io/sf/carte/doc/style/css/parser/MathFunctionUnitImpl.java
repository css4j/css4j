/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

abstract class MathFunctionUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	private final MathFunction functionID;

	public MathFunctionUnitImpl(MathFunction functionID) {
		super(LexicalType.MATH_FUNCTION);
		this.functionID = functionID;
	}

	/**
	 * Gives the ID of this unit as a mathematical function, according to
	 * {@link MathFunction}.
	 * 
	 * @return the function enum.
	 */
	@Override
	public MathFunction getMathFunction() {
		return functionID;
	}

	/**
	 * Gives the index of this mathematical function, according to
	 * {@link MathFunction}.
	 * 
	 * @return the function index.
	 */
	@Override
	public int getMathFunctionIndex() {
		return functionID.ordinal();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		Category cat = syntax.getCategory();
		switch (cat) {
		case integer:
		case number:
		case percentage:
		case lengthPercentage:
		case length:
		case angle:
		case time:
		case frequency:
		case resolution:
		case flex:
			return dimensionalMatch(rootSyntax, syntax);
		case universal:
			return Match.TRUE;
		default:
		}
		return Match.FALSE;
	}

	private Match dimensionalMatch(CSSValueSyntax rootSyntax,
			CSSValueSyntax syntax) {
		DimensionalAnalyzer danal = new DimensionalAnalyzer();
		Dimension dim;
		try {
			dim = dimension(danal);
		} catch (DOMException e) {
			return Match.FALSE;
		}
		return dim != null ? dim.matches(syntax) : Match.PENDING;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + functionID.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		// super.equals implies MathFunctionUnitImpl instance
		MathFunctionUnitImpl other = (MathFunctionUnitImpl) obj;
		return functionID == other.functionID;
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
