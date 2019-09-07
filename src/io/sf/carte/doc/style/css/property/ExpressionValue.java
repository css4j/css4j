/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpression.AlgebraicPart;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.util.SimpleWriter;

/**
 * Expression container base class.
 * 
 * @author Carlos Amengual
 *
 */
public class ExpressionValue extends AbstractCSSPrimitiveValue implements CSSExpressionValue {

	private StyleExpression expression = null;

	public ExpressionValue() {
		super(CSSPrimitiveValue2.CSS_EXPRESSION);
	}

	protected ExpressionValue(ExpressionValue copied) {
		super(copied);
		this.expression = copied.expression.clone();
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
			expression = fillExpressionLevel(lunit, new ValueFactory());
			if (expression == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Void expression");
			}
		}
	
		private StyleExpression fillExpressionLevel(LexicalUnit lu, ValueFactory factory) {
			StyleExpression expression = null;
			short lastlutype = -1;
			while (lu != null) {
				StyleExpression operation;
				boolean inverse = false;
				short lutype = lu.getLexicalUnitType();
				switch (lutype) {
				case LexicalUnit.SAC_OPERATOR_MINUS:
					inverse = true;
				case LexicalUnit.SAC_OPERATOR_PLUS:
					if (expression == null) {
						if (inverse) {
							expression = new SumExpression();
							expression.nextOperandInverse = true;
						} else {
							throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Missing operand");
						}
					} else if (expression.getPartType() == AlgebraicPart.OPERAND) {
						operation = new SumExpression();
						operation.addExpression(expression);
						operation.nextOperandInverse = inverse;
						expression = operation;
					} else if (expression.getPartType() == AlgebraicPart.SUM) {
						expression.nextOperandInverse = inverse;
					} else { // product
						StyleExpression parent = expression.getParentExpression();
						if (parent == null) {
							operation = new SumExpression();
							operation.addExpression(expression);
							operation.nextOperandInverse = inverse;
							expression = operation;
						} else {
							expression = parent; // Parent can only be sum with
												// current calc() syntax
							expression.nextOperandInverse = inverse;
						}
					}
					break;
				case LexicalUnit.SAC_OPERATOR_SLASH:
					inverse = true;
				case LexicalUnit.SAC_OPERATOR_MULTIPLY:
					if (expression == null) {
						throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Missing factor");
					} else if (expression.getPartType() == AlgebraicPart.OPERAND) {
						operation = new ProductExpression();
						operation.addExpression(expression);
						operation.nextOperandInverse = inverse;
						expression = operation;
					} else if (expression.getPartType() == AlgebraicPart.SUM) {
						operation = new ProductExpression();
						if (lastlutype != LexicalUnit.SAC_SUB_EXPRESSION) {
							expression.replaceLastExpression(operation);
						} else {
							operation.addExpression(expression);
						}
						operation.nextOperandInverse = inverse;
						expression = operation;
					} else {
						expression.nextOperandInverse = inverse;
					}
					break;
				case LexicalUnit.SAC_SUB_EXPRESSION:
					LexicalUnit subval = lu.getSubValues();
					if (subval == null) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Empty sub-expression");
					}
					StyleExpression subexpr = fillExpressionLevel(subval, factory);
					if (subexpr != null) {
						if (expression != null) {
							expression.addExpression(subexpr);
							if (expression.getPartType() == AlgebraicPart.SUM) {
								expression = subexpr;
							}
						} else {
							expression = subexpr;
						}
					} else {
						throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad subexpression");
					}
					break;
				case LexicalUnit.SAC_OPERATOR_COMMA: // We are probably in function context
					if (nextLexicalUnit != null || expression.getParentExpression() != null) {
						throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad operand: ','");
					}
					nextLexicalUnit = lu;
					return expression;
				case LexicalUnit.SAC_FUNCTION:
					String funcname = lu.getFunctionName();
					if (funcname.equals(getStringValue())) {
						// Handle as a subexpression
						lutype = LexicalUnit.SAC_SUB_EXPRESSION;
						subval = lu.getParameters();
						if (subval == null) {
							throw new DOMException(DOMException.SYNTAX_ERR, "Empty sub-" + getStringValue() + "()");
						}
						subexpr = fillExpressionLevel(subval, factory);
						if (subexpr != null) {
							subexpr.setInverseOperation(inverse);
							if (expression != null) {
								expression.addExpression(subexpr);
							} else {
								expression = subexpr;
							}
						} else {
							throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
									"Bad sub-" + getStringValue() + "()");
						}
						break;
					}
				default:
					AbstractCSSPrimitiveValue primi;
					LexicalSetter item = factory.createCSSPrimitiveValueItem(lu, false);
					if (item == null || isInvalidOperand(primi = item.getCSSValue(), lutype, lastlutype)) {
						throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad operands");
					}
					OperandExpression operand = new OperandExpression();
					operand.setOperand(primi);
					expression = addOperand(expression, operand);
					lastlutype = lutype;
					lu = item.getNextLexicalUnit();
					continue;
				}
				lastlutype = lutype;
				lu = lu.getNextLexicalUnit();
			}
			if (expression.getParentExpression() != null) {
				expression = expression.getParentExpression();
			}
			return expression;
		}
	}

	private StyleExpression addOperand(StyleExpression expression, OperandExpression operand) {
		if (expression == null) {
			expression = operand;
		} else {
			if (expression.getPartType() == AlgebraicPart.OPERAND) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad expression");
			} else {
				expression.addExpression(operand);
			}
		}
		return expression;
	}

	protected boolean isInvalidOperand(AbstractCSSPrimitiveValue primi, short lutype, short lastlutype) {
		if (isOperatorType(lutype)) {
			return isOperatorType(lastlutype);
		}
		return !isOperatorType(lastlutype) && lastlutype != -1 && lutype != LexicalUnit.SAC_OPERATOR_COMMA;
	}

	private boolean isOperatorType(short lutype) {
		switch (lutype) {
		case LexicalUnit.SAC_OPERATOR_PLUS:
		case LexicalUnit.SAC_OPERATOR_MINUS:
		case LexicalUnit.SAC_OPERATOR_MULTIPLY:
		case LexicalUnit.SAC_OPERATOR_SLASH:
			return true;
		}
		return false;
	}

	@Override
	public StyleExpression getExpression() {
		return expression;
	}

	@Override
	public String getStringValue() throws DOMException {
		return "";
	}

	@Override
	public String getCssText() {
		String s = expression.getCssText();
		if (expression.getPartType() != CSSExpression.AlgebraicPart.OPERAND) {
			return s;
		}
		StringBuilder buf = new StringBuilder(s.length() + 2);
		buf.append('(').append(s).append(')');
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		String s = expression.getMinifiedCssText();
		if (expression.getPartType() != CSSExpression.AlgebraicPart.OPERAND) {
			return s;
		}
		StringBuilder buf = new StringBuilder(s.length() + 2);
		buf.append('(').append(s).append(')');
		return buf.toString();
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
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public ExpressionValue clone() {
		return new ExpressionValue(this);
	}

}
