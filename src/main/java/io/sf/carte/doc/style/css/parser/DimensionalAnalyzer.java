/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.UnitStringToId;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

class DimensionalAnalyzer {

	private boolean attrPending;

	public DimensionalAnalyzer() {
	}

	/**
	 * The value depends on {@code attr()} value(s) where the syntax and the
	 * fallback are different length-percentage types.
	 * 
	 * @return {@code true} if {@code attr()} value(s) with fallback conflicting on
	 *         length-percentage was used.
	 */
	public boolean isAttrPending() {
		return attrPending;
	}

	private enum Ops {
		ADD, MULT, DIV;
	}

	/**
	 * Compute the dimension of the given expression parameters.
	 * 
	 * @param lunit the lexical chain of the expression parameters.
	 * @return the dimension, or {@code null} if it is pending substitution (var()).
	 * @throws DOMException if an error was found.
	 */
	public Dimension expressionDimension(LexicalUnitImpl lunit)
			throws DOMException {
		// The current sum dimension
		Dimension sum = null;

		// The current operating dimension
		Dimension dim = null;

		Ops operation = Ops.ADD;

		while (lunit != null) {
			LexicalType sacType = lunit.getLexicalUnitType();
			switch (sacType) {
			case DIMENSION:
				switch (operation) {
				case ADD:
					dim = createDimension(lunit.getCssUnit());
					break;
				case MULT:
					dim = dim.multiplyByUnit(lunit.getCssUnit());
					break;
				case DIV:
					dim = dim.divideByUnit(lunit.getCssUnit());
					break;
				}
				break;
			case IDENT:
				String cons = lunit.getStringValue().toLowerCase(Locale.ROOT);
				if (!"pi".equals(cons) && !"e".equals(cons)) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Invalid identifier in expression: " + lunit.getCssText());
				}
				// pass-through
			case REAL:
				if (operation == Ops.ADD) {
					dim = new Dimension();
					dim.category = Category.number;
				}
				break;
			case INTEGER:
				if (operation == Ops.ADD) {
					dim = new Dimension();
					dim.category = Category.integer;
				}
				break;
			case PERCENTAGE:
				attrPending = false;
				switch (operation) {
				case ADD:
					dim = createDimension(CSSUnit.CSS_PERCENTAGE);
					break;
				case MULT:
					dim = dim.multiplyByUnit(CSSUnit.CSS_PERCENTAGE);
					break;
				case DIV:
					dim = dim.divideByUnit(CSSUnit.CSS_PERCENTAGE);
					break;
				}
				break;
			case CALC:
			case SUB_EXPRESSION:
				Dimension expdim = expressionDimension(lunit.parameters);
				if (expdim == null) {
					return null;
				}
				switch (operation) {
				case ADD:
					dim = expdim;
					break;
				case MULT:
					dim = dim.multiply(expdim);
					break;
				case DIV:
					dim = dim.divide(expdim);
					break;
				}
				break;
			case VAR:
				return null;
			case MATH_FUNCTION:
				Dimension funcdim = ((MathFunctionUnitImpl) lunit).dimension(this);
				if (funcdim == null) {
					return null;
				}
				switch (operation) {
				case ADD:
					dim = funcdim;
					break;
				case MULT:
					dim = dim.multiply(funcdim);
					break;
				case DIV:
					dim = dim.divide(funcdim);
					break;
				}
				break;
			case OPERATOR_PLUS:
			case OPERATOR_MINUS:
				if (sum == null) {
					sum = dim;
				} else {
					sum.sum(dim);
				}
				operation = Ops.ADD;
				break;
			case OPERATOR_MULTIPLY:
				operation = Ops.MULT;
				break;
			case OPERATOR_SLASH:
				operation = Ops.DIV;
				break;
			case ATTR:
				Dimension attrdim = attrDimension(lunit.parameters);
				if (attrdim == null) {
					return null;
				}
				switch (operation) {
				case ADD:
					dim = attrdim;
					break;
				case MULT:
					dim = dim.multiply(attrdim);
					break;
				case DIV:
					dim = dim.divide(attrdim);
					break;
				}
				break;
			default:
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Invalid unit in expression: " + lunit.getCssText());
			}

			lunit = lunit.nextLexicalUnit;
		}

		if (sum != null) {
			if (!sum.sum(dim)) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Invalid attempt to sum " + sum + " and " + dim);
			}
		} else {
			sum = dim;
		}

		return sum;
	}

	private Dimension attrDimension(LexicalUnitImpl param)
			throws DOMException {
		LexicalUnitImpl lunit = param.nextLexicalUnit;
		if (lunit != null) {
			Dimension dim = typeSpecDimension(lunit);
			// Return the dimension unless it is length or percentage,
			// and we can find a fallback
			if (dim == null
					|| (dim.category != Category.length && dim.category != Category.percentage)
					|| (lunit = lunit.nextLexicalUnit) == null
					|| lunit.getLexicalUnitType() != LexicalType.OPERATOR_COMMA
					|| (lunit = lunit.nextLexicalUnit) == null) {
				return dim;
			}

			Dimension dimfb;
			try {
				dimfb = expressionDimension(lunit);
			} catch (DOMException e) {
				return dim;
			}

			if (dimfb != null) {
				if (dim.category != dimfb.category) {
					// Sum the dimensions, so length-percentage can merge
					attrPending = dim.sum(dimfb);

					// Do not consider lengths and percentages
					// as explicitly processed.
					dim.lengthProcessed = false;
					dim.percentageProcessed = false;
				}
			}

			return dim;
		}

		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Invalid attr() in expression: " + param.getCssText());
	}

	private static Dimension typeSpecDimension(LexicalUnitImpl param) {
		switch (param.getLexicalUnitType()) {
		case TYPE_FUNCTION:
			LexicalUnit typeParam = param.getParameters();
			if (typeParam == null) {
				break;
			}
			Dimension dim = new Dimension();
			dim.category = typeParam.getSyntax().getCategory();
			if (dim.category != Category.number && dim.category != Category.integer) {
				dim.exponent = 1;
			}
			return dim;
		case IDENT:
			// We got a data type spec
			String s = param.getStringValue().toLowerCase(Locale.ROOT);
			return createDimension(UnitStringToId.unitFromString(s));
		case OPERATOR_MOD:
			dim = new Dimension();
			dim.category = Category.percentage;
			dim.exponent = 1;
			return dim;
		case VAR:
			return null;
		default:
			break;
		}

		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Invalid attr() in expression: " + param.getCssText());
	}

	/**
	 * Compute the dimension for a given unit.
	 * 
	 * @param unit the unit.
	 * @return the dimension.
	 * @throws DOMException if the unit is incompatible with a numeric dimension.
	 */
	private static Dimension createDimension(short unit) throws DOMException {
		Dimension dim = new Dimension();
		if (unit == CSSUnit.CSS_NUMBER) {
			dim.category = Category.number;
			dim.exponent = 0;
			return dim;
		}
		dim.exponent = 1;
		if (CSSUnit.isLengthUnitType(unit)) {
			dim.category = Category.length;
			dim.lengthProcessed = true;
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			dim.category = Category.percentage;
			dim.percentageProcessed = true;
		} else if (CSSUnit.isAngleUnitType(unit)) {
			dim.category = Category.angle;
		} else if (CSSUnit.isTimeUnitType(unit)) {
			dim.category = Category.time;
		} else if (CSSUnit.isResolutionUnitType(unit)) {
			dim.category = Category.resolution;
		} else if (unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ) {
			dim.category = Category.frequency;
		} else if (unit == CSSUnit.CSS_FR) {
			dim.category = Category.flex;
		} else {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Invalid unit: " + unit);
		}
		return dim;
	}

}
