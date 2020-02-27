/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Iterator;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Function CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class FunctionValue extends PrimitiveValue implements CSSFunctionValue {

	private String functionName = null;

	private final LinkedCSSValueList arguments = new LinkedCSSValueList();

	public FunctionValue() {
		super(CSSPrimitiveValue2.CSS_FUNCTION);
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

	void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	@Override
	public String getFunctionName() {
		return functionName;
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
				short type = lu.getLexicalUnitType();
				if (type == LexicalUnit.SAC_SUB_EXPRESSION) {
					item = subExpression(lu);
					newval = item.getCSSValue();
				} else if (type == LexicalUnit2.SAC_LEFT_BRACKET) {
					LexicalUnit nlu = lu.getNextLexicalUnit();
					item = factory.parseBracketList(nlu, null, false);
					if (item != null) {
						newval = item.getCSSValue();
					} else {
						lu = nlu.getNextLexicalUnit();
						continue;
					}
				} else if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
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
						&& (type == LexicalUnit.SAC_OPERATOR_PLUS || type == LexicalUnit.SAC_OPERATOR_MINUS
								|| type == LexicalUnit.SAC_OPERATOR_MULTIPLY)
						&& list.getLength() == 1 && isOperand(list.item(0))) {
					list = null;
					item = expressionItem(lu);
					newval = item.getCSSValue();
				} else {
					item = factory.createCSSPrimitiveValueItem(lu, false);
					newval = item.getCSSValue();
				}
				lu = item.getNextLexicalUnit();
				if (lu != null) {
					if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
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
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				short pType = ((CSSPrimitiveValue) value).getPrimitiveType();
				switch (pType) {
				case CSSPrimitiveValue.CSS_STRING:
				case CSSPrimitiveValue.CSS_IDENT:
				case CSSPrimitiveValue.CSS_RGBCOLOR:
				case CSSPrimitiveValue.CSS_URI:
				case CSSPrimitiveValue.CSS_ATTR:
				case CSSPrimitiveValue.CSS_COUNTER:
				case CSSPrimitiveValue.CSS_RECT:
				case CSSPrimitiveValue.CSS_UNKNOWN:
				case CSSPrimitiveValue2.CSS_ELEMENT_REFERENCE:
				case CSSPrimitiveValue2.CSS_GRADIENT:
				case CSSPrimitiveValue2.CSS_UNICODE_CHARACTER:
				case CSSPrimitiveValue2.CSS_UNICODE_RANGE:
				case CSSPrimitiveValue2.CSS_UNICODE_WILDCARD:
					break;
				default:
					return true;
				}
			}
			return false;
		}

		private ValueItem subExpression(LexicalUnit lu) {
			ExpressionValue expr = new ExpressionValue();
			ExpressionValue.ExpressionLexicalSetter setter = expr.newLexicalSetter();
			setter.setLexicalUnitFromSubValues(lu.getSubValues());
			LexicalUnit nextlex = lu.getNextLexicalUnit();
			if (nextlex != null) {
				short type = nextlex.getLexicalUnitType();
				if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
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
			if (delimLu == null || delimLu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
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
			if (first.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
					&& ((CSSPrimitiveValue2) first).getPrimitiveType() == CSSPrimitiveValue2.CSS_EXPRESSION
					&& ((CSSPrimitiveValue2) first).getStringValue().length() == 0) {
				wri.write(((CSSExpressionValue) first).getExpression().getCssText());
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
			if (other.functionName != null) {
				return false;
			}
		} else if (!functionName.equals(other.functionName)) {
			return false;
		}
		return true;
	}

	@Override
	public FunctionValue clone() {
		return new FunctionValue(this);
	}

}
