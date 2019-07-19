/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Iterator;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSCalcValue;
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
public class FunctionValue extends AbstractCSSPrimitiveValue implements CSSFunctionValue {

	private String functionName = null;

	private LinkedCSSValueList arguments = new LinkedCSSValueList();

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
				AbstractCSSValue newval;
				short type = lu.getLexicalUnitType();
				if (type == LexicalUnit.SAC_SUB_EXPRESSION) {
					ExpressionContainerValue expr = new ExpressionContainerValue();
					ExpressionContainerValue.ExpressionLexicalSetter setter = expr.newLexicalSetter();
					setter.setLexicalUnitFromSubValues(lu.getSubValues());
					LexicalUnit nextlex = lu.getNextLexicalUnit();
					if (nextlex != null) {
						type = nextlex.getLexicalUnitType();
						if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
							// slash exception
							setter.setLexicalUnitFromSubValues(lu);
						} else {
							setter.setLexicalUnitFromSubValues(lu.getSubValues());
							setter.nextLexicalUnit = nextlex;
						}
					}
					newval = expr;
					item = setter;
				} else if (type == LexicalUnit2.SAC_LEFT_BRACKET) {
					item = factory.parseBracketList(lu.getNextLexicalUnit(), null, false);
					newval = item.getCSSValue();
				} else if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
					// Do not handle the slash through ValueFactory
					ValueFactory.BasicValueItem vbi = new ValueFactory.BasicValueItem();
					vbi.nextLexicalUnit = lu.getNextLexicalUnit();
					item = vbi;
					newval = new UnknownValue();
					newval.setPlainCssText("/");
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
							Iterator<AbstractCSSValue> it = arguments.iterator();
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
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(functionName.length() + arguments.size() * 6 + 8);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText(String pname) {
		StringBuilder buf = new StringBuilder(functionName.length() + arguments.size() * 6 + 8);
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
			AbstractCSSValue first = arguments.get(0);
			if (first.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
					&& ((CSSPrimitiveValue2) first).getPrimitiveType() == CSSPrimitiveValue2.CSS_EXPRESSION
					&& ((CSSPrimitiveValue2) first).getStringValue().length() == 0) {
				wri.write(((CSSCalcValue) first).getExpression().getCssText());
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
