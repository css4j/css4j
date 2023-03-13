/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Function value.
 * 
 * @author Carlos Amengual
 *
 */
public class FunctionValue extends TypedValue implements CSSFunctionValue {

	private static final long serialVersionUID = 1L;

	private String functionName = null;

	private final LinkedCSSValueList arguments = new LinkedCSSValueList();

	public FunctionValue() {
		super(Type.FUNCTION);
	}

	protected FunctionValue(Type type) {
		super(type);
	}

	protected FunctionValue(FunctionValue copied) {
		super(copied);
		this.functionName = copied.functionName;
		this.arguments.addAll(copied.arguments);
	}

	@Override
	public LinkedCSSValueList getArguments() {
		return arguments;
	}

	@Override
	public StyleValue getComponent(int index) {
		try {
			return arguments.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		try {
			arguments.set(index, component);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public Match matches(CSSValueSyntax syntax) {
		if (syntax == null) {
			return Match.FALSE;
		}

		switch (syntax.getCategory()) {
		case number:
		case length:
		case lengthPercentage:
		case percentage:
		case integer:
		case resolution:
		case angle:
		case time:
		case frequency:
		case flex:
		case universal:
			// Numeric match
			return dimensionalAnalysis(syntax, true);
		default:
			return super.matches(syntax);
		}
	}

	private boolean isImageFunction() {
		// Custom gradients and other image functions
		return functionName.endsWith("-gradient") || functionName.equalsIgnoreCase("image")
				|| functionName.equalsIgnoreCase("image-set")
				|| functionName.equalsIgnoreCase("cross-fade");
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case transformFunction:
		case transformList:
			return ParseHelper.isTransformFunction(functionName) ? Match.TRUE : Match.FALSE;
		case image:
			// Custom gradients and other image functions
			return isImageFunction() ? Match.TRUE : Match.FALSE;
		default:
			return dimensionalAnalysis(syntax, false);
		}
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
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			functionName = lunit.getFunctionName();
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			boolean commaSep = false;
			ValueList list = null;
			while (lu != null) {
				ValueItem item;
				StyleValue newval;
				LexicalType type = lu.getLexicalUnitType();
				if (type == LexicalType.SUB_EXPRESSION) {
					item = subExpression(lu);
					newval = item.getCSSValue();
				} else if (type == LexicalType.LEFT_BRACKET) {
					LexicalUnit nlu = lu.getNextLexicalUnit();
					item = factory.parseBracketList(nlu, null, false);
					if (item != null) {
						newval = item.getCSSValue();
					} else {
						lu = nlu.getNextLexicalUnit();
						continue;
					}
				} else if (type == LexicalType.OPERATOR_SLASH) {
					if (list != null && list.getLength() == 1 && isOperand(list.item(0))) {
						list = null;
						item = expressionItem(lu);
						newval = item.getCSSValue();
					} else {
						// Do not handle the slash through ValueFactory
						ValueFactory.BasicValueItem vbi = new ValueFactory.BasicValueItem();
						vbi.nextLexicalUnit = lu.getNextLexicalUnit();
						item = vbi;
						newval = new UnknownValue();
						((UnknownValue) newval).setPlainCssText("/");
					}
				} else if (list != null
						&& (type == LexicalType.OPERATOR_PLUS || type == LexicalType.OPERATOR_MINUS
								|| type == LexicalType.OPERATOR_MULTIPLY)
						&& list.getLength() == 1 && isOperand(list.item(0))) {
					list = null;
					item = expressionItem(lu);
					newval = item.getCSSValue();
				} else {
					item = factory.createCSSPrimitiveValueItem(lu, false, false);
					newval = item.getCSSValue();
				}
				lu = item.getNextLexicalUnit();
				if (lu != null) {
					if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
						lu = lu.getNextLexicalUnit();
						if (!commaSep && !arguments.isEmpty()) {
							list = ValueList.createWSValueList();
							Iterator<StyleValue> it = arguments.iterator();
							while (it.hasNext()) {
								list.add(it.next());
							}
							list.add(newval);
							arguments.clear();
							arguments.add(list);
							list = null;
						} else if (list == null) {
							arguments.add(newval);
						} else {
							list.add(newval);
							list = null;
						}
						commaSep = true;
					} else if (list == null) {
						list = ValueList.createWSValueList();
						list.add(newval);
						arguments.add(list);
					} else {
						list.add(newval);
					}
				} else if (list == null) {
					arguments.add(newval);
				} else {
					list.add(newval);
				}
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private boolean isOperand(StyleValue value) {
			if (value.getCssValueType() == CssType.TYPED) {
				CSSTypedValue typed = (CSSTypedValue) value;
				Type pType = typed.getPrimitiveType();
				switch (pType) {
				case STRING:
				case COLOR:
				case URI:
				case ATTR:
				case COUNTER:
				case RECT:
				case UNKNOWN:
				case ELEMENT_REFERENCE:
				case GRADIENT:
				case UNICODE_CHARACTER:
				case UNICODE_RANGE:
				case UNICODE_WILDCARD:
					break;
				case IDENT:
					return isConstant(typed.getStringValue());
				default:
					return true;
				}
			}
			return false;
		}

		private boolean isConstant(String constname) {
			return "e".equalsIgnoreCase(constname) || "pi".equalsIgnoreCase(constname);
		}

		private ValueItem subExpression(LexicalUnit lu) {
			ExpressionValue expr = new ExpressionValue();
			ExpressionValue.ExpressionLexicalSetter setter = expr.newLexicalSetter();
			setter.setLexicalUnitFromSubValues(lu.getSubValues());
			LexicalUnit nextlex = lu.getNextLexicalUnit();
			if (nextlex != null) {
				LexicalType type = nextlex.getLexicalUnitType();
				if (type == LexicalType.OPERATOR_SLASH) {
					// slash exception
					setter.setLexicalUnitFromSubValues(lu);
				} else {
					setter.setLexicalUnitFromSubValues(lu.getSubValues());
					setter.nextLexicalUnit = nextlex;
				}
			}
			return setter;
		}

		private ValueItem expressionItem(LexicalUnit lu) {
			arguments.removeLast();
			LexicalUnit firstOpLu = lu.getPreviousLexicalUnit();
			LexicalUnit delimLu = firstOpLu.getPreviousLexicalUnit();
			if (delimLu == null || delimLu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
				ExpressionValue expr = new ExpressionValue();
				ExpressionValue.ExpressionLexicalSetter setter = expr.newLexicalSetter();
				setter.setLexicalUnitFromSubValues(firstOpLu);
				return setter;
			} else {
				throw new IllegalStateException();
			}
		}

	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(functionName.length() + arguments.size() * 8 + 12);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText(String pname) {
		StringBuilder buf = new StringBuilder(functionName.length() + arguments.size() * 8 + 12);
		buf.append(functionName).append('(');
		int sz = arguments.size();
		if (sz > 0) {
			buf.append(arguments.get(0).getMinifiedCssText(pname));
			for (int i = 1; i < sz; i++) {
				buf.append(',').append(arguments.get(i).getMinifiedCssText(pname));
			}
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(functionName);
		wri.write('(');
		int sz = arguments.size();
		if (sz == 1) {
			// Check whether the only parameter is an expression, and omit the
			// parentheses in that case
			StyleValue first = arguments.get(0);
			if (first.getPrimitiveType() == Type.EXPRESSION
					&& ((CSSExpressionValue) first).getStringValue().length() == 0) {
				((CSSExpressionValue) first).getExpression().writeCssText(wri);
			} else {
				first.writeCssText(wri);
			}
		} else if (sz != 0) {
			arguments.get(0).writeCssText(wri);
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				wri.write(' ');
				arguments.get(i).writeCssText(wri);
			}
		}
		wri.write(')');
	}

	@Override
	public String getStringValue() throws DOMException {
		return functionName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result + ((functionName == null) ? 0 : functionName.hashCode());
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
		FunctionValue other = (FunctionValue) obj;
		if (arguments == null) {
			if (other.arguments != null) {
				return false;
			}
		} else if (!arguments.equals(other.arguments)) {
			return false;
		}
		if (functionName == null) {
			return other.functionName == null;
		} else {
			return functionName.equals(other.functionName);
		}
	}

	@Override
	public FunctionValue clone() {
		return new FunctionValue(this);
	}

}
