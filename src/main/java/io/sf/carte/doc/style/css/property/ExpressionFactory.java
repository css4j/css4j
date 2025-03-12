/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpression.AlgebraicPart;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueFactory;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

/**
 * Expressions factory.
 */
public class ExpressionFactory {

	private LexicalUnit nextLexicalUnit;

	/**
	 * Creates an expression factory.
	 * 
	 * @param nextLexicalUnit the next lexical unit, or {@code null} if none.
	 */
	public ExpressionFactory(LexicalUnit nextLexicalUnit) {
		super();
		this.nextLexicalUnit = nextLexicalUnit;
	}

	/**
	 * Gives the next lexical unit. It could be the next lexical unit of the parent
	 * value, or an operator that was found while producing an expression.
	 * <p>
	 * This method should be called <i>after</i> calling
	 * {@link #createExpression(LexicalUnit)}, not before.
	 * </p>
	 * 
	 * @return the next lexical unit.
	 */
	public LexicalUnit getNextLexicalUnit() {
		return nextLexicalUnit;
	}

	/**
	 * Create an expression from the given lexical unit.
	 * 
	 * @param calcParams the lexical unit(s) of the operands.
	 * @return the expression.
	 * @throws DOMException if an error was found.
	 */
	public CSSExpression createExpression(LexicalUnit calcParams) throws DOMException {
		return createExpression(calcParams, getCSSValueFactory());
	}

	/**
	 * Get a value factory.
	 * 
	 * @return the value factory.
	 */
	protected CSSValueFactory getCSSValueFactory() {
		return new ValueFactory();
	}

	private StyleExpression createExpression(LexicalUnit lu, CSSValueFactory factory)
			throws DOMException {
		StyleExpression expression = null;
		LexicalType lastlutype = LexicalType.UNKNOWN;
		while (lu != null) {
			StyleExpression operation;
			boolean inverse = false;
			LexicalType lutype = lu.getLexicalUnitType();
			switch (lutype) {
			case OPERATOR_MINUS:
				inverse = true;
			case OPERATOR_PLUS:
				if (expression == null) {
					if (inverse) {
						expression = new SumExpression();
						expression.nextOperandInverse = true;
					} else {
						throw new DOMException(DOMException.SYNTAX_ERR, "Missing operand");
					}
				} else if (expression.getPartType() == AlgebraicPart.OPERAND) {
					operation = new SumExpression();
					operation.addExpression(expression);
					operation.nextOperandInverse = inverse;
					expression = operation;
				} else if (expression.getPartType() == AlgebraicPart.SUM) {
					expression.nextOperandInverse = inverse;
				} else { // product
					// Sanity check
					sanityCheck(lastlutype);

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
			case OPERATOR_SLASH:
				inverse = true;
			case OPERATOR_MULTIPLY:
				if (expression == null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Missing factor");
				} else if (expression.getPartType() == AlgebraicPart.OPERAND) {
					operation = new ProductExpression();
					operation.addExpression(expression);
					operation.nextOperandInverse = inverse;
					expression = operation;
				} else if (expression.getPartType() == AlgebraicPart.SUM) {
					// Sanity check
					sanityCheck(lastlutype);

					operation = new ProductExpression();
					if (lastlutype != LexicalType.SUB_EXPRESSION) {
						expression.replaceLastExpression(operation);
					} else {
						operation.addExpression(expression);
					}
					operation.nextOperandInverse = inverse;
					expression = operation;
				} else {
					// Sanity check
					sanityCheck(lastlutype);

					expression.nextOperandInverse = inverse;
				}
				break;
			case SUB_EXPRESSION:
				LexicalUnit subval = lu.getSubValues();
				if (subval == null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Empty sub-expression");
				}
				StyleExpression subexpr = createExpression(subval, factory);
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
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad subexpression");
				}
				break;
			case OPERATOR_COMMA: // We are probably in function context
				if (nextLexicalUnit != null || expression.getParentExpression() != null) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad operand: ','");
				}
				nextLexicalUnit = lu;
				return expression;
			case IDENT:
				String constname = lu.getStringValue();
				CSSTypedValue typed = createConstant(constname);
				if (isInvalidOperand(typed, lutype, lastlutype)) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad operands");
				}
				OperandExpression operand = new OperandExpression();
				operand.setOperand(typed);
				expression = addOperand(expression, operand);
				lastlutype = LexicalType.DIMENSION; // Equivalent to "operand"
				lu = lu.getNextLexicalUnit();
				continue;
			case CALC:
				if (isCalcValue()) {
					// Handle as a subexpression
					lutype = LexicalType.SUB_EXPRESSION;
					subval = lu.getParameters();
					if (subval == null) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Empty sub-calc()");
					}
					subexpr = createExpression(subval, factory);
					if (subexpr != null) {
						subexpr.setInverseOperation(inverse);
						if (expression != null) {
							expression.addExpression(subexpr);
						} else {
							expression = subexpr;
						}
					} else {
						throw new DOMException(DOMException.SYNTAX_ERR, "Invalid sub-calc()");
					}
					break;
				}
			default:
				CSSPrimitiveValue primi = factory.createCSSPrimitiveValue(lu);
				if (isInvalidOperand(primi, lutype, lastlutype)) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad operands");
				}
				operand = new OperandExpression();
				operand.setOperand(primi);
				expression = addOperand(expression, operand);
				lastlutype = LexicalType.DIMENSION; // Equivalent to "operand"
				lu = lu.getNextLexicalUnit();
				continue;
			}
			lastlutype = lutype;
			lu = lu.getNextLexicalUnit();
		}

		// Sanity check
		if (isOperatorType(lastlutype)) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Missing operand");
		}

		if (expression.getParentExpression() != null) {
			expression = expression.getParentExpression();
		}

		return expression;
	}

	private static void sanityCheck(LexicalType lastlutype) throws DOMException {
		if (lastlutype != LexicalType.DIMENSION && lastlutype != LexicalType.REAL
				&& lastlutype != LexicalType.INTEGER && lastlutype != LexicalType.SUB_EXPRESSION
				&& lastlutype != LexicalType.ENV) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Missing operand");
		}
	}

	private static StyleExpression addOperand(StyleExpression expression,
			OperandExpression operand) throws DOMException {
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

	protected boolean isInvalidOperand(CSSPrimitiveValue primi, LexicalType lutype,
			LexicalType lastlutype) {
		if (isOperatorType(lutype)) {
			return isOperatorType(lastlutype);
		}
		return !isOperatorType(lastlutype) && lastlutype != LexicalType.UNKNOWN
				&& lutype != LexicalType.OPERATOR_COMMA;
	}

	private static boolean isOperatorType(LexicalType lutype) {
		switch (lutype) {
		case OPERATOR_PLUS:
		case OPERATOR_MINUS:
		case OPERATOR_MULTIPLY:
		case OPERATOR_SLASH:
			return true;
		default:
		}
		return false;
	}

	private TypedValue createConstant(String constname) {
		NumberValue number = new NumberValue();
		if ("pi".equalsIgnoreCase(constname)) {
			number.setFloatValue(CSSUnit.CSS_NUMBER, (float) Math.PI);
		} else if ("e".equalsIgnoreCase(constname)) {
			number.setFloatValue(CSSUnit.CSS_NUMBER, (float) Math.E);
		} else if (isCalcValue()) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
					"Unknown constant: " + constname);
		} else {
			IdentifierValue ident = new IdentifierValue();
			ident.setStringValue(Type.IDENT, constname);
			return ident;
		}
		return number;
	}

	/**
	 * Check whether this is the expression in a {@code calc()} value or a
	 * sub-expression in some mathematical function.
	 * 
	 * @return {@code true} if the expression belongs to a {@code calc()} value.
	 */
	protected boolean isCalcValue() {
		// calc() values should override this
		return false;
	}

}
