/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.SimpleWriter;

/**
 * DeclarationFormattingContext that serializes colors as RGB (sRGB).
 */
public class RGBColorDeclarationFormattingContext extends DefaultDeclarationFormattingContext {

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
			try {
				RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
				wri.write(rgb.toString());
				return;
			} catch (DOMException e) {
			}
			break;
		case FUNCTION:
		case MATH_FUNCTION:
		case GRADIENT:
			writeFunction(wri, propertyName, (CSSFunctionValue) value);
			return;
		default:
		}
		super.writeValue(wri, propertyName, value);
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

	private void writeFunction(SimpleWriter wri, String propertyName, CSSFunctionValue value)
		throws IOException {
		LinkedCSSValueList arguments = value.getArguments();
		wri.write(value.getFunctionName());
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
				writeValue(wri, propertyName, first);
			}
		} else if (sz != 0) {
			writeValue(wri, propertyName, arguments.get(0));
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				wri.write(' ');
				writeValue(wri, propertyName, arguments.get(i));
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
			try {
				RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
				wri.write(rgb.toMinifiedString());
				return;
			} catch (DOMException e) {
			}
			break;
		case FUNCTION:
		case MATH_FUNCTION:
		case GRADIENT:
			writeMinifiedFunction(wri, propertyName, (CSSFunctionValue) value);
			return;
		default:
		}
		super.writeMinifiedValue(wri, propertyName, value);
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

	private void writeMinifiedFunction(SimpleWriter wri, String propertyName,
		CSSFunctionValue value) throws IOException {
		LinkedCSSValueList arguments = value.getArguments();
		wri.write(value.getFunctionName());
		wri.write('(');
		int sz = arguments.size();
		if (sz == 1) {
			// Check whether the only parameter is an expression, and omit the
			// parentheses in that case
			StyleValue first = arguments.get(0);
			if (first.getPrimitiveType() == Type.EXPRESSION
				&& ((CSSExpressionValue) first).getStringValue().length() == 0) {
				wri.write(((CSSExpressionValue) first).getExpression().getMinifiedCssText());
			} else {
				writeMinifiedValue(wri, propertyName, first);
			}
		} else if (sz != 0) {
			writeMinifiedValue(wri, propertyName, arguments.get(0));
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				writeMinifiedValue(wri, propertyName, arguments.get(i));
			}
		}
		wri.write(')');
	}

}
