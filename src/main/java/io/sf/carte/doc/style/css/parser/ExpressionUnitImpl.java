/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class ExpressionUnitImpl extends LexicalUnitImpl {

	private static final long serialVersionUID = 1L;

	public ExpressionUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	public LexicalUnit getSubValues() {
		return parameters;
	}

	@Override
	CharSequence currentToString() {
		return functionalSerialization(value);
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
			return matchExpression(rootSyntax, syntax);
		case universal:
			return Match.TRUE;
		default:
		}
		return Match.FALSE;
	}

	/**
	 * If the supplied value represents an expression, determine if its result that
	 * could be consistent with the requested syntax.
	 * 
	 * @param lunit      the lexical value containing the first operand.
	 * @param rootSyntax the first syntax in the syntax chain.
	 * @param syntax     the current syntax to be evaluated in the syntax chain.
	 * @return the match that would be expected from the expression.
	 */
	private Match matchExpression(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		DimensionalAnalyzer danal = new DimensionalAnalyzer();
		Dimension dim;
		try {
			dim = danal.expressionDimension(parameters);
		} catch (DOMException e) {
			return Match.FALSE;
		}
		if (dim == null) { // var()
			return Match.PENDING;
		}

		Match expected = Match.FALSE;
		// Look for both lengths and percentages being matched
		boolean lenghtMatched = false, pcntMatched = false;
		CSSValueSyntax comp = rootSyntax;
		do {
			Category cat = comp.getCategory();

			Match match;

			boolean lenientLP = danal.isAttrPending();

			if (lenientLP) {
				/*
				 * The idea of attr() lenient length-percentage processing is that the attr()
				 * type and fallback may be a length and a percentage, or vice-versa. In which
				 * case one cannot clearly match either with TRUE, FALSE nor PENDING. However,
				 * when we find lengths or percentages in subsequent computations, this can be
				 * used to narrow the match.
				 */
				match = categoryMatch(true, true, dim.category, cat);
				if (match == Match.PENDING) {
					// Special case: length-percentage
					if (cat == Category.length) {
						lenghtMatched = true;
						match = dim.isPercentageProcessed() ? Match.FALSE : Match.PENDING;
					} else if (cat == Category.percentage) {
						pcntMatched = true;
						match = dim.isLengthProcessed() ? Match.FALSE : Match.PENDING;
					}
				}
			} else {
				// Special case: length-percentage
				if (cat == Category.length) {
					lenghtMatched = true;
				} else if (cat == Category.percentage) {
					pcntMatched = true;
				}
				match = categoryMatch(true, false, dim.category, cat);
			}

			if (match == Match.TRUE) {
				if (dim.isCSS()) {
					return Match.TRUE;
				}
			} else if (expected != Match.PENDING) {
				expected = match;
			}
		} while ((comp = comp.getNext()) != null);

		// Special case: length-percentage
		if (dim.category == Category.lengthPercentage && lenghtMatched && pcntMatched) {
			expected = Match.TRUE;
		}

		return expected;
	}

	@Override
	ExpressionUnitImpl instantiateLexicalUnit() {
		return new ExpressionUnitImpl(getLexicalUnitType());
	}

}
