/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSColorMixFunction;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueList;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.SimpleWriter;

/**
 * DeclarationFormattingContext that allows customization of serialized colors.
 */
public class ColorDeclarationFormattingContext extends DefaultDeclarationFormattingContext {

	private static final long serialVersionUID = 1L;

	@Override
	public void writeValue(SimpleWriter wri, String propertyName, CSSValue value)
		throws IOException {
		if (value.getCssValueType() == CssType.LIST) {
			writeList(wri, propertyName, (ValueList) value);
			return;
		}

		switch (value.getPrimitiveType()) {
		case COLOR:
			writeColor(wri, propertyName, (CSSColorValue) value);
			break;
		case COLOR_MIX:
			writeColorMix(wri, propertyName, (CSSColorMixFunction) value);
			break;
		case FUNCTION:
		case MATH_FUNCTION:
		case GRADIENT:
			writeFunction(wri, propertyName, (CSSFunctionValue) value);
			break;
		default:
			super.writeValue(wri, propertyName, value);
		}
	}

	private void writeList(SimpleWriter wri, String propertyName, ValueList list)
		throws IOException {
		if (list.isCommaSeparated()) {
			writeCSList(wri, propertyName, list);
		} else if (!list.isBracketList()) {
			// WS List
			writeWSList(wri, propertyName, list);
		} else {
			super.writeValue(wri, propertyName, list);
		}
	}

	private void writeCSList(SimpleWriter wri, String propertyName, ValueList valueList)
		throws IOException {
		if (!valueList.isEmpty()) {
			writeValue(wri, propertyName, valueList.item(0));
			int sz = valueList.getLength();
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				wri.write(' ');
				writeValue(wri, propertyName, valueList.item(i));
			}
		}
	}

	private void writeWSList(SimpleWriter wri, String propertyName, ValueList valueList)
		throws IOException {
		if (!valueList.isEmpty()) {
			writeValue(wri, propertyName, valueList.item(0));
			int sz = valueList.getLength();
			for (int i = 1; i < sz; i++) {
				wri.write(' ');
				writeValue(wri, propertyName, valueList.item(i));
			}
		}
	}

	/**
	 * Write a css {@code COLOR} to the given writer.
	 * <p>
	 * This implementation calls {@link CSSValue#writeCssText(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	protected void writeColor(SimpleWriter wri, String propertyName, CSSColorValue value)
			throws IOException {
		value.writeCssText(wri);
	}

	/**
	 * Write a css {@code COLOR_MIX} to the given writer.
	 * <p>
	 * This implementation calls {@link CSSValue#writeCssText(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	protected void writeColorMix(SimpleWriter wri, String propertyName, CSSColorMixFunction value)
			throws IOException {
		value.writeCssText(wri);
	}

	private void writeFunction(SimpleWriter wri, String propertyName, CSSFunctionValue value)
		throws IOException {
		CSSValueList<? extends CSSValue> arguments = value.getArguments();
		wri.write(value.getFunctionName());
		wri.write('(');
		int sz = arguments.getLength();
		if (sz == 1) {
			// Check whether the only parameter is an expression, and omit the
			// parentheses in that case
			CSSValue first = arguments.item(0);
			if (first.getPrimitiveType() == Type.EXPRESSION
				&& ((CSSExpressionValue) first).getStringValue().length() == 0) {
				((CSSExpressionValue) first).getExpression().writeCssText(wri);
			} else {
				writeValue(wri, propertyName, first);
			}
		} else if (sz != 0) {
			writeValue(wri, propertyName, arguments.item(0));
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				wri.write(' ');
				writeValue(wri, propertyName, arguments.item(i));
			}
		}
		wri.write(')');
	}

	@Override
	public void writeMinifiedValue(SimpleWriter wri, String propertyName, CSSValue value)
		throws IOException {
		if (value.getCssValueType() == CssType.LIST) {
			writeMinifiedList(wri, propertyName, (ValueList) value);
			return;
		}

		switch (value.getPrimitiveType()) {
		case COLOR:
			writeMinifiedColor(wri, propertyName, (CSSColorValue) value);
			break;
		case COLOR_MIX:
			writeColorMix(wri, propertyName, (CSSColorMixFunction) value);
			break;
		case FUNCTION:
		case MATH_FUNCTION:
		case GRADIENT:
			writeMinifiedFunction(wri, propertyName, (CSSFunctionValue) value);
			break;
		default:
			super.writeMinifiedValue(wri, propertyName, value);
		}
	}

	private void writeMinifiedList(SimpleWriter wri, String propertyName, ValueList list)
		throws IOException {
		if (list.isCommaSeparated()) {
			writeMinifiedCSList(wri, propertyName, list);
		} else if (!list.isBracketList()) {
			// WS List
			writeMinifiedWSList(wri, propertyName, list);
		} else {
			super.writeMinifiedValue(wri, propertyName, list);
		}
	}

	private void writeMinifiedCSList(SimpleWriter wri, String propertyName, ValueList valueList)
		throws IOException {
		if (!valueList.isEmpty()) {
			writeMinifiedValue(wri, propertyName, valueList.item(0));
			int sz = valueList.getLength();
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				writeMinifiedValue(wri, propertyName, valueList.item(i));
			}
		}
	}

	private void writeMinifiedWSList(SimpleWriter wri, String propertyName, ValueList valueList)
		throws IOException {
		if (!valueList.isEmpty()) {
			writeMinifiedValue(wri, propertyName, valueList.item(0));
			int sz = valueList.getLength();
			for (int i = 1; i < sz; i++) {
				wri.write(' ');
				writeMinifiedValue(wri, propertyName, valueList.item(i));
			}
		}
	}

	/**
	 * Write a minified css {@code COLOR} value to the given writer.
	 * <p>
	 * This implementation just writes {@link CSSValue#getMinifiedCssText(String)}.
	 * </p>
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	protected void writeMinifiedColor(SimpleWriter wri, String propertyName, CSSColorValue value)
			throws IOException {
		super.writeMinifiedValue(wri, propertyName, value);
	}

	/**
	 * Write a minified css {@code COLOR_MIX} value to the given writer.
	 * <p>
	 * This implementation just writes {@link CSSValue#getMinifiedCssText(String)}.
	 * </p>
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	protected void writeMinifiedColorMix(SimpleWriter wri, String propertyName, CSSColorMixFunction value)
			throws IOException {
		super.writeMinifiedValue(wri, propertyName, value);
	}

	private void writeMinifiedFunction(SimpleWriter wri, String propertyName,
		CSSFunctionValue value) throws IOException {
		CSSValueList<? extends CSSValue> arguments = value.getArguments();
		wri.write(value.getFunctionName());
		wri.write('(');
		int sz = arguments.getLength();
		if (sz == 1) {
			// Check whether the only parameter is an expression, and omit the
			// parentheses in that case
			CSSValue first = arguments.item(0);
			if (first.getPrimitiveType() == Type.EXPRESSION
				&& ((CSSExpressionValue) first).getStringValue().length() == 0) {
				wri.write(((CSSExpressionValue) first).getExpression().getMinifiedCssText());
			} else {
				writeMinifiedValue(wri, propertyName, first);
			}
		} else if (sz != 0) {
			writeMinifiedValue(wri, propertyName, arguments.item(0));
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				writeMinifiedValue(wri, propertyName, arguments.item(i));
			}
		}
		wri.write(')');
	}

}
