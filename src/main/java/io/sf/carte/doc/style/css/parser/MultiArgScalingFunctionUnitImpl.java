/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

class MultiArgScalingFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public MultiArgScalingFunctionUnitImpl(int functionIndex) {
		super(functionIndex);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) throws DOMException {
		LexicalUnitImpl first = parameters.shallowClone();
		LexicalUnitImpl second = parameters.nextLexicalUnit;
		Dimension dim = analyzer.expressionDimension(first);
		if (dim != null) {
			if (dim.category == Category.length || dim.category == Category.percentage) {
				// Check for a length-percentage situation with the second argument
				if (second != null && (second = second.nextLexicalUnit) != null) {
					Dimension dim2 = analyzer.expressionDimension(second);
					if (dim2 != null && dim.category != dim2.category) {
						if (!dim.sum(dim2)) {
							throw new DOMException(DOMException.INVALID_ACCESS_ERR,
									"Function " + getFunctionName()
											+ " has arguments with different dimensions: "
											+ dim.category.name() + " versus "
											+ dim2.category.name() + '.');
						}
					}
				}
			}
		} else {
			if (second == null || (second = second.nextLexicalUnit) == null) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Function " + getFunctionName() + " has only one argument.");
			}
			dim = analyzer.expressionDimension(second);
		}
		return dim;
	}

	@Override
	MultiArgScalingFunctionUnitImpl instantiateLexicalUnit() {
		return new MultiArgScalingFunctionUnitImpl(getMathFunctionIndex());
	}

}
