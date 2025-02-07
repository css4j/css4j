/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;
import java.util.Map;

import io.sf.carte.doc.style.css.MathFunctions;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.CSSParser.LexicalUnitFactory;

class FunctionFactories {

	private static final Map<String, LexicalUnitFactory> factories = createFactoryMap();

	private static Map<String, LexicalUnitFactory> createFactoryMap() {
		Map<String, LexicalUnitFactory> factories = new HashMap<>(40);

		factories.put("calc", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.CALC);
			}

		});

		factories.put("attr", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.ATTR);
			}

		});

		factories.put("type", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.TYPE_FUNCTION);
			}

		});

		factories.put("var", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.VAR);
			}

		});

		factories.put("url", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.URI);
			}

		});

		factories.put("src", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.SRC);
			}

		});

		factories.put("element", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.ELEMENT_REFERENCE);
			}

		});

		factories.put("rect", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.RECT_FUNCTION);
			}

		});

		factories.put("counter", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.COUNTER_FUNCTION);
			}

		});

		factories.put("counters", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.COUNTERS_FUNCTION);
			}

		});

		/*
		 * Easing functions
		 */

		factories.put("cubic-bezier", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.CUBIC_BEZIER_FUNCTION);
			}

		});

		factories.put("steps", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.STEPS_FUNCTION);
			}

		});

		/*
		 * Colors
		 */

		LexicalUnitFactory rgb = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.RGBCOLOR);
			}

		};

		factories.put("rgb", rgb);
		factories.put("rgba", rgb);

		LexicalUnitFactory hsl = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.HSLCOLOR);
			}

		};

		factories.put("hsl", hsl);
		factories.put("hsla", hsl);

		factories.put("lab", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.LABCOLOR);
			}

		});

		factories.put("lch", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.LCHCOLOR);
			}

		});

		factories.put("oklab", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.OKLABCOLOR);
			}

		});

		factories.put("oklch", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.OKLCHCOLOR);
			}

		});

		factories.put("hwb", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.HWBCOLOR);
			}

		});

		factories.put("color", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.COLOR_FUNCTION);
			}

		});

		factories.put("color-mix", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.COLOR_MIX);
			}

		});

		/*
		 * Mathematical functions
		 */

		factories.put("abs", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ScalingFunctionUnitImpl(MathFunctions.ABS);
			}

		});

		factories.put("clamp", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunctions.CLAMP);
			}

		});

		factories.put("max", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunctions.MAX);
			}

		});

		factories.put("min", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunctions.MIN);
			}

		});

		factories.put("hypot", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunctions.HYPOT);
			}

		});

		factories.put("hypot2", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunctions.HYPOT2);
			}

		});

		factories.put("sqrt", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new SqrtFunctionUnitImpl(MathFunctions.SQRT);
			}

		});

		factories.put("pow", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new PowFunctionUnitImpl(MathFunctions.POW);
			}

		});

		factories.put("sign", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunctions.SIGN);
			}

		});

		factories.put("sin", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunctions.SIN);
			}

		});

		factories.put("cos", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunctions.COS);
			}

		});

		factories.put("tan", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunctions.TAN);
			}

		});

		factories.put("asin", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunctions.ASIN);
			}

		});

		factories.put("acos", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunctions.ACOS);
			}

		});

		factories.put("atan", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunctions.ATAN);
			}

		});

		factories.put("atan2", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunctions.ATAN2);
			}

		});

		return factories;
	}

	public static LexicalUnitFactory getFactory(String lcFunctionName) {
		return factories.get(lcFunctionName);
	}

}
