/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.HashMap;
import java.util.Map;

import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;

/**
 * Helper for mathematical function values.
 */
class MathFunctionHelper {

	private static Map<String, MathFunction> map = new HashMap<>(16);

	static {
		map.put("max", MathFunction.MAX);
		map.put("min", MathFunction.MIN);
		map.put("clamp", MathFunction.CLAMP);
		map.put("abs", MathFunction.ABS);
		map.put("sign", MathFunction.SIGN);
		map.put("sin", MathFunction.SIN);
		map.put("cos", MathFunction.COS);
		map.put("tan", MathFunction.TAN);
		map.put("asin", MathFunction.ASIN);
		map.put("acos", MathFunction.ACOS);
		map.put("atan", MathFunction.ATAN);
		map.put("atan2", MathFunction.ATAN2);
		map.put("pow", MathFunction.POW);
		map.put("sqrt", MathFunction.SQRT);
		map.put("hypot", MathFunction.HYPOT);
	}

	static CSSMathFunctionValue.MathFunction getMathFunction(String lcFunctionName) {
		return map.get(lcFunctionName);
	}

}
