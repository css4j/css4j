/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

/**
 * Implementation of a mathematical function value.
 */
class MathFunctionValue extends FunctionValue implements CSSMathFunctionValue {

	private static final long serialVersionUID = 1L;

	private final MathFunction function;

	MathFunctionValue(MathFunction function) {
		super(Type.MATH_FUNCTION);
		this.function = function;
	}

	MathFunctionValue(MathFunctionValue copied) {
		super(copied);
		this.function = copied.function;
	}

	@Override
	public MathFunction getFunction() {
		return function;
	}

	/**
	 * Perform a dimensional analysis of this expression and compute the unit type
	 * of the result.
	 * 
	 * @return the unit type of the result, as in {@link CSSUnit}.
	 */
	@Override
	public short computeUnitType() {
		DimensionalEvaluator eval = new DimensionalEvaluator();
		short unit;
		try {
			unit = eval.computeUnitType(this);
		} catch (DOMException e) {
			unit = CSSUnit.CSS_INVALID;
		}
		return unit;
	}

	@Override
	public Match matches(CSSValueSyntax syntax) {
		if (syntax == null) {
			return Match.FALSE;
		}

		return dimensionalAnalysis(syntax, true);
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		return dimensionalAnalysis(syntax, false);
	}

	private Match dimensionalAnalysis(CSSValueSyntax syntax, boolean followComponents) {
		DimensionalEvaluator eval = new DimensionalEvaluator();
		Category result;
		try {
			result = eval.dimensionalAnalysis(this);
		} catch (DOMException e) {
			if (eval.hasUnknownFunction() && syntax.getCategory() == Category.universal) {
				return Match.TRUE;
			}
			return Match.FALSE;
		}
		// Universal match (after checking the expression correctness)
		if (syntax.getCategory() == Category.universal) {
			return Match.TRUE;
		}
		//
		boolean lengthPercentageL = false, lengthPercentageP = false;
		do {
			Category cat = syntax.getCategory();
			if (cat == result) {
				return Match.TRUE;
			}
			// Match length-percentage, also <number> clamps to <integer>.
			if ((cat == Category.lengthPercentage
					&& (result == Category.length || result == Category.percentage))
					|| (cat == Category.integer && result == Category.number)) {
				return Match.TRUE;
			}
			// Do we have a <length-percentage> and did we match length or percentage in
			// previous loops?
			if (result == Category.lengthPercentage) {
				if (cat == Category.length) {
					if (lengthPercentageP) {
						return Match.TRUE;
					}
					lengthPercentageL = true;
				} else if (cat == Category.percentage) {
					if (lengthPercentageL) {
						return Match.TRUE;
					}
					lengthPercentageP = true;
				}
			}
		} while (followComponents && (syntax = syntax.getNext()) != null);

		return Match.FALSE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + function.hashCode();
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		MathFunctionValue other = (MathFunctionValue) obj;
		return function == other.function;
	}

	@Override
	public MathFunctionValue clone() {
		return new MathFunctionValue(this);
	}

}
