/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Expression container base class.
 * 
 * @author Carlos Amengual
 *
 */
public class ExpressionValue extends TypedValue implements CSSExpressionValue {

	private static final long serialVersionUID = 1L;

	private CSSExpression expression = null;

	private boolean roundResult = false;

	public ExpressionValue() {
		super(Type.EXPRESSION);
	}

	protected ExpressionValue(ExpressionValue copied) {
		super(copied);
		this.expression = copied.expression.clone();
		this.roundResult = copied.roundResult;
	}

	@Override
	ExpressionLexicalSetter newLexicalSetter() {
		return new ExpressionLexicalSetter();
	}

	class ExpressionLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) throws DOMException {
			nextLexicalUnit = lunit.getNextLexicalUnit();
			setLexicalUnitFromSubValues(lunit.getParameters());
		}

		void setLexicalUnitFromSubValues(LexicalUnit lunit) throws DOMException {
			ExpressionFactory ef = createExpressionFactory(nextLexicalUnit);
			expression = ef.createExpression(lunit);
			if (expression == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Void expression");
			}
			nextLexicalUnit = ef.getNextLexicalUnit();
		}
	}

	@Override
	public CSSExpression getExpression() {
		return expression;
	}

	ExpressionFactory createExpressionFactory(LexicalUnit nextLexicalUnit) {
		return new ExpressionFactory(nextLexicalUnit);
	}

	@Override
	public String getStringValue() throws DOMException {
		return "";
	}

	@Override
	public float getFloatValue(short unitType) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Please use an evaluator and compute result.");
	}

	@Override
	public String getCssText() {
		String s = expression.getCssText();
		if (isStandAloneExpression()) {
			return s;
		}
		StringBuilder buf = new StringBuilder(s.length() + 2);
		buf.append('(').append(s).append(')');
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		String s = expression.getMinifiedCssText();
		if (isStandAloneExpression()) {
			return s;
		}
		StringBuilder buf = new StringBuilder(s.length() + 2);
		buf.append('(').append(s).append(')');
		return buf.toString();
	}

	private boolean isStandAloneExpression() {
		/*
		 * We want to check for non-numeric string operands, for legacy IE
		 * compatibility.
		 */
		return expression.getPartType() != CSSExpression.AlgebraicPart.OPERAND
				|| ((CSSOperandExpression) expression).getOperand().getPrimitiveType() != Type.IDENT
				|| isNumericConstant(
						((CSSTypedValue) ((CSSOperandExpression) expression).getOperand())
								.getStringValue());
	}

	private static boolean isNumericConstant(String s) {
		return "pi".equalsIgnoreCase(s) || "e".equalsIgnoreCase(s);
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		if (expression.getPartType() != CSSExpression.AlgebraicPart.OPERAND) {
			expression.writeCssText(wri);
		} else {
			wri.write('(');
			expression.writeCssText(wri);
			wri.write(')');
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"The value of this property cannot be modified.");
	}

	@Override
	public void setExpectInteger() {
		roundResult = true;
	}

	@Override
	public boolean isExpectingInteger() {
		return roundResult;
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
			unit = eval.computeUnitType(getExpression());
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
			result = eval.dimensionalAnalysis(getExpression());
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

		boolean lengthPercentageL = false, lengthPercentageP = false;
		do {
			Category cat = syntax.getCategory();
			if (cat == result) {
				return Match.TRUE;
			}
			// Match length-percentage, also <number> clamps to <integer> in calc().
			if ((cat == Category.lengthPercentage && (result == Category.length || result == Category.percentage))
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
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionValue other = (ExpressionValue) obj;
		if (expression == null) {
			return other.expression == null;
		} else {
			return expression.equals(other.expression);
		}
	}

	@Override
	public ExpressionValue clone() {
		return new ExpressionValue(this);
	}

}
