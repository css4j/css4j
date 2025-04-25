/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;
import java.util.Map;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.TransformFunctions;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

class FunctionFactories {

	private final Map<String, LexicalUnitFactory> factories = createFactoryMap();

	private Map<String, LexicalUnitFactory> createFactoryMap() {
		Map<String, LexicalUnitFactory> factories = new HashMap<>(86);

		factories.put("calc", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ExpressionUnitImpl(LexicalType.CALC);
			}

		});

		factories.put("attr", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AttrUnitImpl();
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
				return new VarFunctionUnitImpl();
			}

			@Override
			public boolean validate(CSSContentHandler handler, final int index,
					LexicalUnitImpl currentlu) {
				String s = currentlu.parameters.getStringValue();
				if (s == null) {
					return false;
				}
				int len = s.length();
				if (len < 3 || s.charAt(0) != '-' || s.charAt(1) != '-') {
					error(handler, index - len, "var() function must reference a custom property.");
					return false;
				}
				LexicalType lastType = CSSParser.findLastValue(currentlu.parameters)
						.getLexicalUnitType();
				if (lastType == LexicalType.OPERATOR_COMMA) {
					addEmptyLexicalUnit(handler);
				}
				return true;
			}

		});

		factories.put("env", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new EnvUnitImpl();
			}

		});

		factories.put("url", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.URI);
			}

			@Override
			public String canonicalName(String lcName) {
				return null;
			}

		});

		factories.put("matrix", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.MATRIX);
			}

		});

		factories.put("perspective", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.PERSPECTIVE);
			}

		});

		factories.put("translate", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.TRANSLATE);
			}

		});

		factories.put("translate3d", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.TRANSLATE_3D);
			}

		});

		LexicalUnitFactory translatex = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.TRANSLATE_X);
			}

			@Override
			public String canonicalName(String lcName) {
				return "translateX";
			}

		};

		factories.put("translatex", translatex);
		factories.put("translateX", translatex);

		LexicalUnitFactory translatey = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.TRANSLATE_Y);
			}

			@Override
			public String canonicalName(String lcName) {
				return "translateY";
			}

		};

		factories.put("translatey", translatey);
		factories.put("translateY", translatey);

		LexicalUnitFactory translatez = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.TRANSLATE_Z);
			}

			@Override
			public String canonicalName(String lcName) {
				return "translateZ";
			}

		};

		factories.put("translatez", translatez);
		factories.put("translateZ", translatez);

		factories.put("scale", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SCALE);
			}

		});

		factories.put("scale3d", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SCALE_3D);
			}

		});

		LexicalUnitFactory scalex = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SCALE_X);
			}

			@Override
			public String canonicalName(String lcName) {
				return "scaleX";
			}

		};

		factories.put("scalex", scalex);
		factories.put("scaleX", scalex);

		LexicalUnitFactory scaley = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SCALE_Y);
			}

			@Override
			public String canonicalName(String lcName) {
				return "scaleY";
			}

		};

		factories.put("scaley", scaley);
		factories.put("scaleY", scaley);

		LexicalUnitFactory scalez = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SCALE_Z);
			}

			@Override
			public String canonicalName(String lcName) {
				return "scaleZ";
			}

		};

		factories.put("scalez", scalez);
		factories.put("scaleZ", scalez);

		factories.put("rotate", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.ROTATE);
			}

		});

		factories.put("rotate3d", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.ROTATE_3D);
			}

		});

		LexicalUnitFactory rotatex = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.ROTATE_X);
			}

			@Override
			public String canonicalName(String lcName) {
				return "rotateX";
			}

		};

		factories.put("rotatex", rotatex);
		factories.put("rotateX", rotatex);

		LexicalUnitFactory rotatey = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.ROTATE_Y);
			}

			@Override
			public String canonicalName(String lcName) {
				return "rotateY";
			}

		};

		factories.put("rotatey", rotatey);
		factories.put("rotateY", rotatey);

		LexicalUnitFactory rotatez = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.ROTATE_Z);
			}

			@Override
			public String canonicalName(String lcName) {
				return "rotateZ";
			}

		};

		factories.put("rotatez", rotatez);
		factories.put("rotateZ", rotatez);

		factories.put("skew", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SKEW);
			}

		});

		LexicalUnitFactory skewx = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SKEW_X);
			}

			@Override
			public String canonicalName(String lcName) {
				return "skewX";
			}

		};

		factories.put("skewx", skewx);
		factories.put("skewX", skewx);

		LexicalUnitFactory skewy = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new TransformFunctionUnitImpl(TransformFunctions.SKEW_Y);
			}

			@Override
			public String canonicalName(String lcName) {
				return "skewY";
			}

		};

		factories.put("skewy", skewy);
		factories.put("skewY", skewy);

		factories.put("src", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new LexicalUnitImpl(LexicalType.SRC);
			}

		});

		factories.put("image-set", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ImageFunctionUnitImpl(LexicalType.IMAGE_SET);
			}

		});

		factories.put("element", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ElementReferenceUnitImpl();
			}

			@Override
			public String canonicalName(String lcName) {
				return null;
			}

		});

		factories.put("circle", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.CIRCLE_FUNCTION);
			}

		});

		factories.put("ellipse", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.ELLIPSE_FUNCTION);
			}

		});

		factories.put("inset", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.INSET_FUNCTION);
			}

		});

		factories.put("path", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.PATH_FUNCTION);
			}

		});

		factories.put("polygon", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.POLYGON_FUNCTION);
			}

		});

		factories.put("rect", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.RECT_FUNCTION);
			}

		});

		factories.put("shape", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.SHAPE_FUNCTION);
			}

		});

		factories.put("xywh", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new BasicShapeUnitImpl(LexicalType.XYWH_FUNCTION);
			}

		});

		/*
		 * <counter>
		 */

		factories.put("counter", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new CounterUnitImpl(LexicalType.COUNTER_FUNCTION);
			}

		});

		factories.put("counters", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new CounterUnitImpl(LexicalType.COUNTERS_FUNCTION);
			}

		});

		/*
		 * Easing functions
		 */

		factories.put("cubic-bezier", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new EasingFunctionUnitImpl(LexicalType.CUBIC_BEZIER_FUNCTION);
			}

		});

		factories.put("linear", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new EasingFunctionUnitImpl(LexicalType.LINEAR_FUNCTION);
			}

		});

		factories.put("steps", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new EasingFunctionUnitImpl(LexicalType.STEPS_FUNCTION);
			}

		});

		/*
		 * Colors
		 */

		LexicalUnitFactory rgb = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new RGBColorUnitImpl();
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index, LexicalUnitImpl lu) {
				return isValidRGBColor(handler, index, lu);
			}

		};

		factories.put("rgb", rgb);
		factories.put("rgba", rgb);

		LexicalUnitFactory hsl = new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.HSLCOLOR);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index, LexicalUnitImpl lu) {
				return isValidHSLColor(handler, index, lu);
			}

		};

		factories.put("hsl", hsl);
		factories.put("hsla", hsl);

		factories.put("lab", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.LABCOLOR);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidLABColor(handler, index, currentlu, 100, 100f);
			}

		});

		factories.put("lch", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.LCHCOLOR);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidLCHColor(handler, index, currentlu, 100, 100f);
			}

		});

		factories.put("oklab", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.OKLABCOLOR);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidLABColor(handler, index, currentlu, 1, 1f);
			}

		});

		factories.put("oklch", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.OKLCHCOLOR);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidLCHColor(handler, index, currentlu, 1, 1f);
			}

		});

		factories.put("hwb", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.HWBCOLOR);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidHWBColor(handler, index, currentlu);
			}

		});

		factories.put("color", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.COLOR_FUNCTION);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidColorFunction(handler, index, currentlu);
			}

		});

		factories.put("color-mix", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ColorUnitImpl(LexicalType.COLOR_MIX);
			}

			@Override
			public boolean validate(CSSContentHandler handler, int index,
					LexicalUnitImpl currentlu) {
				return isValidColorMixFunction(index, currentlu);
			}

		});

		/*
		 * Mathematical functions
		 */

		factories.put("abs", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new ScalingFunctionUnitImpl(MathFunction.ABS);
			}

		});

		factories.put("clamp", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.CLAMP);
			}

		});

		factories.put("max", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.MAX);
			}

		});

		factories.put("min", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.MIN);
			}

		});

		factories.put("round", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new RoundFunctionUnitImpl(MathFunction.ROUND);
			}

		});

		factories.put("mod", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.MOD);
			}

		});

		factories.put("rem", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.REM);
			}

		});

		factories.put("hypot", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.HYPOT);
			}

		});

		factories.put("hypot2", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new MultiArgScalingFunctionUnitImpl(MathFunction.HYPOT2);
			}

		});

		factories.put("log", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunction.LOG);
			}

		});

		factories.put("exp", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunction.EXP);
			}

		});

		factories.put("sqrt", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new SqrtFunctionUnitImpl(MathFunction.SQRT);
			}

		});

		factories.put("pow", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new PowFunctionUnitImpl(MathFunction.POW);
			}

		});

		factories.put("sign", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunction.SIGN);
			}

		});

		factories.put("sin", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunction.SIN);
			}

		});

		factories.put("cos", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunction.COS);
			}

		});

		factories.put("tan", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new UnitlessFunctionUnitImpl(MathFunction.TAN);
			}

		});

		factories.put("asin", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunction.ASIN);
			}

		});

		factories.put("acos", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunction.ACOS);
			}

		});

		factories.put("atan", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunction.ATAN);
			}

		});

		factories.put("atan2", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AngleFunctionUnitImpl(MathFunction.ATAN2);
			}

		});

		return factories;
	}

	private boolean isValidRGBColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		short valCount = 0;
		LexicalType lastType = LexicalType.UNKNOWN;
		boolean hasCommas = false;
		boolean hasNoCommas = false;
		boolean hasVar = false;
		do {
			LexicalType type = lu.getLexicalUnitType();
			if (type == LexicalType.OPERATOR_COMMA) {
				if (lastType == LexicalType.OPERATOR_COMMA || lastType == LexicalType.UNKNOWN
						|| hasNoCommas) {
					return false;
				}
				hasCommas = true;
			} else if (isComponentType(type)) {
				if (type == LexicalType.VAR) {
					hasVar = true;
				}

				// Check component type
				if (type == LexicalType.INTEGER) {
					int value = lu.getIntegerValue();
					if (value < 0) {
						lu.intValue = 0;
						warn(handler, index, "Color component has value under 0.");
					} else if (valCount == 3 && value > 1) {
						lu.intValue = 1;
						warn(handler, index, "Color alpha has value over 1.");
					}
					if (value > 255) {
						warn(handler, index, "Color component has value over 255.");
					}
				} else if (type == LexicalType.REAL) {
					float value = lu.getFloatValue();
					if (value < 0f) {
						lu.floatValue = 0f;
						warn(handler, index, "Color component has value under 0.");
					}
					if (valCount == 3) {
						if (value > 1f) {
							lu.floatValue = 1f;
							warn(handler, index, "Color alpha has value over 1.");
						}
					} else if (lastType != LexicalType.OPERATOR_SLASH) {
						type = LexicalType.INTEGER;
					}
					if (value > 255f) {
						warn(handler, index, "Color component has value over 255.");
					}
				} else if (type == LexicalType.PERCENTAGE) {
					float value = lu.getFloatValue();
					if (value < 0f) {
						lu.floatValue = 0f;
						warn(handler, index, "Color component has percentage under 0%.");
					} else if (value > 100f) {
						lu.floatValue = 100f;
						warn(handler, index, "Color component has percentage over 100%.");
					}
				} else if (type == LexicalType.IDENT) {
					if (!"none".equalsIgnoreCase(lu.getStringValue())) {
						return false;
					}
					type = LexicalType.PERCENTAGE;
				}

				if (hasCommas) {
					if (lastType != LexicalType.OPERATOR_COMMA) {
						return false;
					}
				} else if (lastType != type) {
					if (valCount == 3 && lastType != LexicalType.OPERATOR_SLASH) {
						// No commas, must be slash
						return false;
					}
				} else {
					hasNoCommas = true;
				}
				valCount++;
			} else if (type == LexicalType.OPERATOR_SLASH) {
				if (hasVar && valCount < 3) {
					valCount = 3;
				}
				if (valCount == 4 || valCount < 3 || !isComponentType(lastType) || hasCommas
						|| lu.nextLexicalUnit == null) {
					return false;
				}
				// Commas no longer accepted
				hasNoCommas = true;
			} else {
				return false;
			}
			lastType = type;
			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return valCount == 3 || valCount == 4 || (valCount < 3 && hasVar);
	}

	private boolean isValidHSLColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		short slaCount = 0;
		LexicalType lastType = LexicalType.UNKNOWN; // EXT1 means angle type
		boolean hasCommas = false;
		boolean hasNoCommas = false;
		boolean hasVar = false;
		do {
			LexicalType type = lu.getLexicalUnitType();
			if (type == LexicalType.PERCENTAGE
					|| ((type == LexicalType.MATH_FUNCTION || type == LexicalType.FUNCTION
							|| type == LexicalType.ATTR) && isPercentageUnit(lu))) {
				if (lastType == LexicalType.UNKNOWN) {
					// First type must be integer (includes calc()), real,
					// angle or VAR, but not a percentage.
					return false;
				}
				// Check commas
				if (hasCommas) {
					if (lastType != LexicalType.OPERATOR_COMMA) {
						return false;
					}
				} else {
					// If last type was integer, real, percentage or angle,
					// the syntax has no commas.
					hasNoCommas = hasNoCommas || lastType == LexicalType.REAL
							|| lastType == LexicalType.PERCENTAGE || lastType == LexicalType.INTEGER
							|| lastType == LexicalType.EXT1;
				}

				if (type == LexicalType.PERCENTAGE) {
					// Clamp
					float value = lu.getFloatValue();
					if (value < 0f) {
						lu.floatValue = 0f;
						warn(handler, index, "Color component has value under 0%.");
					} else if (value > 100f) {
						lu.floatValue = 100f;
						warn(handler, index, "Color component has value over 100%.");
					}
				} else {
					// To simplify the logic, consider as a percentage
					type = LexicalType.PERCENTAGE;
				}

				// We got either S, L or alpha
				slaCount++;
			} else if (type == LexicalType.REAL) {
				if (lastType != LexicalType.UNKNOWN) {
					// We got either S, L or alpha
					slaCount++;
					// Clamp
					float value = lu.getFloatValue();
					if (value < 0f) {
						lu.floatValue = 0f;
						warn(handler, index, "Color component has value under 0.");
					} else if (lastType == LexicalType.OPERATOR_SLASH) {
						if (value > 1f) {
							lu.floatValue = 1f;
							warn(handler, index, "Color alpha has value over 1.");
						}
					} else if (value > 100f) {
						lu.floatValue = 100f;
						warn(handler, index, "Color component has value over 100%.");
					}
					// Check commas
					if (hasCommas) {
						if (lastType != LexicalType.OPERATOR_COMMA) {
							return false;
						}
					} else {
						// If last type was integer, real, percentage or angle,
						// the syntax has no commas.
						hasNoCommas = hasNoCommas || lastType == LexicalType.REAL
								|| lastType == LexicalType.PERCENTAGE
								|| lastType == LexicalType.INTEGER || lastType == LexicalType.EXT1;
					}
				}
			} else if (type == LexicalType.OPERATOR_COMMA) {
				// Check that a comma was expected at this point
				if (lastType == LexicalType.OPERATOR_COMMA || lastType == LexicalType.UNKNOWN
						|| hasNoCommas) {
					return false;
				}
				hasCommas = true;
			} else if (type == LexicalType.INTEGER) {
				if (lastType != LexicalType.UNKNOWN) {
					// Clamp value if necessary
					int value = lu.getIntegerValue();
					if (value < 0) {
						lu.intValue = 0;
						warn(handler, index, "Color component has value under 0%.");
					} else if (value > 100) {
						lu.intValue = 100;
						warn(handler, index, "Color component has value over 100%.");
					}
					if (value > 1 && lastType == LexicalType.OPERATOR_SLASH) {
						lu.intValue = 1;
					}
					// We got either S, L or alpha
					slaCount++;
					// To simplify the logic, consider as a percentage
					type = LexicalType.PERCENTAGE;
					// Check commas
					if (hasCommas) {
						if (lastType != LexicalType.OPERATOR_COMMA) {
							return false;
						}
					} else {
						// If last type was integer, real, percentage or angle,
						// the syntax has no commas.
						hasNoCommas = hasNoCommas || lastType == LexicalType.REAL
								|| lastType == LexicalType.PERCENTAGE
								|| lastType == LexicalType.INTEGER || lastType == LexicalType.EXT1;
					}
				}
			} else if (isAngleUnit(lu)) {
				// We got H
				if (lastType != LexicalType.UNKNOWN) {
					return false;
				}
				type = LexicalType.EXT1;
			} else if (type == LexicalType.OPERATOR_SLASH) {
				// We have to meet a few conditions here
				if (((slaCount != 2 && !hasVar) || (hasVar && slaCount > 2)
						|| (lastType != LexicalType.PERCENTAGE && lastType != LexicalType.REAL
								&& lastType != LexicalType.VAR))
						|| hasCommas || lu.nextLexicalUnit == null) {
					return false;
				}
			} else if (type == LexicalType.CALC || type == LexicalType.MATH_FUNCTION
					|| type == LexicalType.FUNCTION || type == LexicalType.ATTR) {
				if (lastType == LexicalType.UNKNOWN) {
					// First type must be integer, real, angle or VAR
					type = LexicalType.INTEGER;
				} else if (lastType == LexicalType.OPERATOR_SLASH) {
					type = LexicalType.REAL;
					slaCount++;
				} else {
					// Check commas
					if (hasCommas) {
						if (lastType != LexicalType.OPERATOR_COMMA) {
							return false;
						}
					} else {
						// If last type was integer, real, percentage or angle,
						// the syntax has no commas.
						hasNoCommas = hasNoCommas || lastType == LexicalType.REAL
								|| lastType == LexicalType.PERCENTAGE
								|| lastType == LexicalType.INTEGER || lastType == LexicalType.EXT1;
					}
					// We got either S, L or alpha
					slaCount++;
					// To simplify the logic, consider as a percentage
					type = LexicalType.PERCENTAGE;
				}
			} else if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (type == LexicalType.IDENT) {
				if (!"none".equalsIgnoreCase(lu.getStringValue())) {
					return false;
				}
				slaCount++;
				// To simplify the logic, consider as a percentage
				type = LexicalType.PERCENTAGE;
			} else {
				return false;
			}
			lastType = type;
			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return slaCount == 2 || slaCount == 3 || (hasVar && slaCount < 2);
	}

	private boolean isValidHWBColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		short pcntCount = 0;
		LexicalType lastType = LexicalType.UNKNOWN; // EXT1 means angle type
		boolean hasVar = false;
		do {
			LexicalType type = lu.getLexicalUnitType();
			if (type == LexicalType.PERCENTAGE) {
				if (lastType == LexicalType.UNKNOWN) {
					// First type must be integer, real, angle or VAR
					return false;
				}
				pcntCount++;
				float value = lu.getFloatValue();
				if (value < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has value under 0%.");
				} else if (value > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has value over 100%.");
				}
			} else if (type == LexicalType.INTEGER) {
				if (lastType != LexicalType.UNKNOWN) {
					if ((lastType != LexicalType.OPERATOR_SLASH) || (pcntCount < 2 && !hasVar)) {
						return false;
					}
					int value = lu.getIntegerValue();
					if (value < 0) {
						lu.intValue = 0;
						warn(handler, index, "Color alpha has value under 0.");
					} else if (value > 1) {
						lu.intValue = 1;
						warn(handler, index, "Color alpha has value over 1.");
					}
				}
			} else if (isAngleUnit(lu)) {
				if (lastType != LexicalType.UNKNOWN) {
					return false;
				}
				type = LexicalType.EXT1;
			} else if (type == LexicalType.OPERATOR_SLASH) {
				if ((pcntCount != 2 && !hasVar) || (hasVar && pcntCount > 2)
						|| (lastType != LexicalType.PERCENTAGE && lastType != LexicalType.VAR)
						|| lu.nextLexicalUnit == null) {
					return false;
				}
			} else if (type == LexicalType.REAL) {
				if (lastType != LexicalType.UNKNOWN) {
					if (lastType != LexicalType.OPERATOR_SLASH || (pcntCount != 2 && !hasVar)
							|| (hasVar && pcntCount > 2)) {
						return false;
					}
					pcntCount = 3;
					// Clamp
					float value = lu.getFloatValue();
					if (value < 0f) {
						lu.floatValue = 0f;
						warn(handler, index, "Color alpha has value under 0.");
					} else if (value > 1f) {
						lu.floatValue = 1f;
						warn(handler, index, "Color alpha has value over 1.");
					}
				}
			} else if (type == LexicalType.CALC || type == LexicalType.MATH_FUNCTION
					|| type == LexicalType.FUNCTION || type == LexicalType.ATTR) {
				if (lastType == LexicalType.UNKNOWN) {
					// First type must be integer, real, angle or VAR
					type = LexicalType.INTEGER;
				} else if (lastType == LexicalType.OPERATOR_SLASH) {
					type = LexicalType.REAL;
					pcntCount = 3;
				} else {
					pcntCount++;
					type = LexicalType.PERCENTAGE;
				}
			} else if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (type == LexicalType.IDENT) {
				if (!"none".equalsIgnoreCase(lu.getStringValue())) {
					return false;
				}
				pcntCount++;
				// To simplify the logic, consider as a percentage
				type = LexicalType.PERCENTAGE;
			} else {
				return false;
			}
			lastType = type;
			lu = lu.nextLexicalUnit;
		} while (lu != null);
		return pcntCount >= 2 || (hasVar && pcntCount <= 1);
	}

	private boolean isValidLABColor(CSSContentHandler handler, final int index,
			LexicalUnitImpl currentlu, int iUpperLightness, float fUpperLightness) {
		LexicalUnitImpl lu = currentlu.parameters;
		boolean hasVar = false;
		if (lu == null) {
			return false;
		}
		// First argument: percentage, real or integer
		LexicalType type = lu.getLexicalUnitType();
		if (type == LexicalType.PERCENTAGE) {
			// Clamp
			float fL = lu.getFloatValue();
			if (fL < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color lightness has percentage under 0%.");
			} else if (fL > 100f) {
				lu.floatValue = 100f;
				warn(handler, index, "Color lightness has percentage over 100%.");
			}
		} else if (type == LexicalType.REAL) {
			// Clamp
			float fL = lu.getFloatValue();
			if (fL < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color lightness has value under 0.");
			} else if (fL > fUpperLightness) {
				lu.floatValue = fUpperLightness;
				warn(handler, index, "Color lightness has value over " + fUpperLightness);
			}
		} else if (type == LexicalType.INTEGER) {
			// Clamp
			int iL = lu.getIntegerValue();
			if (iL < 0) {
				lu.intValue = 0;
				warn(handler, index, "Color lightness has value under 0.");
			} else if (iL > iUpperLightness) {
				lu.intValue = iUpperLightness;
				warn(handler, index, "Color lightness has value over " + iUpperLightness);
			}
		} else if (type == LexicalType.VAR) {
			hasVar = true;
		} else if (type != LexicalType.CALC && type != LexicalType.MATH_FUNCTION
				&& type != LexicalType.FUNCTION && type != LexicalType.ATTR
				&& (type != LexicalType.IDENT || !"none".equalsIgnoreCase(lu.getStringValue()))) {
			return false;
		}

		lu = lu.nextLexicalUnit;
		if (lu == null) {
			// Just one value: only OK if it was a var().
			return hasVar;
		}

		// Establish a value loop
		int numericValueCount = 1;
		do {
			type = lu.getLexicalUnitType();
			switch (type) {
			case IDENT:
				if (!"none".equalsIgnoreCase(lu.getStringValue())) {
					return false;
				}
			case REAL:
			case INTEGER:
			case CALC:
			case MATH_FUNCTION:
			case FUNCTION:
			case ATTR:
				numericValueCount++;
				if (numericValueCount > 3) {
					// The slash could be inside a var()
					if (!hasVar || numericValueCount > 4) {
						return false;
					}
					return isValidAlpha(handler, index, lu);
				}
				break;
			case OPERATOR_SLASH:
				lu = lu.nextLexicalUnit;
				// This must be alpha channel value
				if (lu == null || numericValueCount > 3 || (numericValueCount < 3 && !hasVar)) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case PERCENTAGE:
				// Could be a or b, also alpha if the slash is inside a var()
				numericValueCount++;
				if (numericValueCount > 3) {
					// The slash could be inside a var()
					if (!hasVar || numericValueCount > 4) {
						return false;
					}
					return isValidAlpha(handler, index, lu);
				}
				// If it has a var(), we don't know whether to clamp
				// as a/b or as alpha
				if (!hasVar) {
					// Clamp
					float fval = lu.getFloatValue();
					if (fval < -100f) {
						lu.floatValue = -100f;
						warn(handler, index, "Color component has percentage under -100%.");
					} else if (fval > 100f) {
						lu.floatValue = 100f;
						warn(handler, index, "Color component has percentage over 100%.");
					}
				}
				break;
			case VAR:
				hasVar = true;
				break;
			default:
				return false;
			}
			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return numericValueCount == 3 || numericValueCount == 4
				|| (hasVar && numericValueCount < 3);
	}

	private boolean isValidLCHColor(CSSContentHandler handler, final int index,
			LexicalUnitImpl currentlu, int iUpperLightness, float fUpperLightness) {
		LexicalUnitImpl lu = currentlu.parameters;
		boolean hasVar = false;
		if (lu == null) {
			return false;
		}
		// First argument: percentage, real or integer
		LexicalType type = lu.getLexicalUnitType();
		if (type == LexicalType.PERCENTAGE) {
			// Clamp
			float fL = lu.getFloatValue();
			if (fL < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color lightness has percentage under 0%.");
			} else if (fL > 100f) {
				lu.floatValue = 100f;
				warn(handler, index, "Color lightness has percentage over 100%.");
			}
		} else if (type == LexicalType.REAL) {
			// Clamp
			float fL = lu.getFloatValue();
			if (fL < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color lightness has value under 0.");
			} else if (fL > fUpperLightness) {
				lu.floatValue = fUpperLightness;
				warn(handler, index, "Color lightness has value over " + fUpperLightness);
			}
		} else if (type == LexicalType.INTEGER) {
			// Clamp
			int iL = lu.getIntegerValue();
			if (iL < 0) {
				lu.intValue = 0;
				warn(handler, index, "Color lightness has value under 0.");
			} else if (iL > iUpperLightness) {
				lu.intValue = iUpperLightness;
				warn(handler, index, "Color lightness has value over " + iUpperLightness);
			}
		} else if (type == LexicalType.VAR) {
			hasVar = true;
		} else if (type != LexicalType.CALC && type != LexicalType.MATH_FUNCTION
				&& type != LexicalType.FUNCTION && type != LexicalType.ATTR
				&& (type != LexicalType.IDENT || !"none".equalsIgnoreCase(lu.getStringValue()))) {
			return false;
		}

		lu = lu.nextLexicalUnit;
		if (lu == null) {
			// Just one value: only OK if it was a var().
			return hasVar;
		}

		// Now it must be the chroma (unless var() involved)
		type = lu.getLexicalUnitType();
		if (type == LexicalType.PERCENTAGE) {
			// Clamp
			float fC = lu.getFloatValue();
			if (fC < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color chroma has percentage under 0.");
			} else if (fC > 100f) {
				lu.floatValue = 100f;
				warn(handler, index, "Color chroma has percentage over 100.");
			}
		} else if (type == LexicalType.REAL) {
			if (!hasVar) {
				// Clamp
				float fC = lu.getFloatValue();
				if (fC < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has value under 0.");
				}
			}
		} else if (type == LexicalType.INTEGER) {
			if (!hasVar) {
				// Clamp
				int iC = lu.getIntegerValue();
				if (iC < 0) {
					lu.intValue = 0;
					warn(handler, index, "Color component has value under 0.");
				}
			}
		} else if (type != LexicalType.CALC && type != LexicalType.MATH_FUNCTION
				&& type != LexicalType.FUNCTION && type != LexicalType.ATTR
				&& (type != LexicalType.IDENT || !"none".equalsIgnoreCase(lu.getStringValue()))) {
			if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (hasVar) {
				// If not an angle, must be slash or alpha
				if (!CSSUnit.isAngleUnitType(lu.getCssUnit())) {
					if (type == LexicalType.OPERATOR_SLASH) {
						lu = lu.nextLexicalUnit;
						// This must be alpha channel value
						if (lu == null) {
							return false;
						}
					}
					return isValidAlpha(handler, index, lu);
				}
			} else {
				return false;
			}
		}

		// Now the hue
		lu = lu.nextLexicalUnit;
		if (lu == null) {
			// Just two values: only OK if a var() is involved.
			return hasVar;
		}

		type = lu.getLexicalUnitType();
		if (type != LexicalType.REAL && type != LexicalType.INTEGER && !isAngleUnit(lu)
				&& type != LexicalType.CALC && type != LexicalType.MATH_FUNCTION
				&& type != LexicalType.FUNCTION && type != LexicalType.ATTR
				&& (type != LexicalType.IDENT || !"none".equalsIgnoreCase(lu.getStringValue()))) {
			if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (hasVar) {
				if (type == LexicalType.OPERATOR_SLASH) {
					lu = lu.nextLexicalUnit;
					// This must be alpha channel value
					if (lu == null) {
						return false;
					}
				}
				return isValidAlpha(handler, index, lu);
			} else {
				return false;
			}
		}

		// We are done unless there is an alpha channel
		lu = lu.nextLexicalUnit;
		if (lu != null) {
			type = lu.getLexicalUnitType();
			if (type == LexicalType.OPERATOR_SLASH) {
				lu = lu.nextLexicalUnit;
				// This must be alpha channel value
				if (lu == null) {
					return false;
				}
			} else if (type == LexicalType.VAR) {
				lu = lu.nextLexicalUnit;
				while (lu != null) {
					if (lu.getLexicalUnitType() != LexicalType.VAR) {
						return isValidAlpha(handler, index, lu);
					}
					lu = lu.nextLexicalUnit;
				}
				return true;
			} else if (!hasVar) {
				return false;
			}
			return isValidAlpha(handler, index, lu);
		}

		return true;
	}

	private boolean isValidColorFunction(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		if (lu == null) {
			return false;
		}

		boolean hasVar = false;

		// First argument: identifier
		LexicalType type = lu.getLexicalUnitType();
		if (type != LexicalType.IDENT) {
			if (type == LexicalType.VAR) {
				hasVar = true;
			} else {
				return false;
			}
		}

		lu = lu.nextLexicalUnit;
		if (lu == null) {
			// Just one value: only OK if it was a var().
			return hasVar;
		}

		// Establish a value loop
		boolean foundNumericValue = false;
		do {
			type = lu.getLexicalUnitType();
			switch (type) {
			case IDENT:
				if (!"none".equalsIgnoreCase(lu.getStringValue())) {
					return false;
				}
			case REAL:
			case PERCENTAGE:
			case INTEGER:
			case CALC:
			case VAR:
				foundNumericValue = true;
				break;
			case OPERATOR_SLASH:
				if (!foundNumericValue && !hasVar) {
					return false;
				}
				lu = lu.nextLexicalUnit;
				// This must be alpha channel value
				if (lu == null) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case MATH_FUNCTION:
			case FUNCTION:
			case ATTR:
				CSSValueSyntax syn = new SyntaxParser().parseSyntax("<number> | <percentage>");
				foundNumericValue = lu.shallowMatch(syn) != Match.FALSE;
				break;
			default:
				return false;
			}
			lu = lu.nextLexicalUnit;
		} while (lu != null);
		return true;
	}

	private boolean isValidColorMixFunction(int index, LexicalUnitImpl currentlu) {
		LexicalUnit lu = currentlu.parameters;
		if (lu == null) {
			return false;
		}

		boolean hasVar = false;

		// First argument: "in"
		LexicalType type = lu.getLexicalUnitType();
		if (type != LexicalType.IDENT) {
			if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (type != LexicalType.ATTR) {
				return false;
			}
		} else if (!"in".equalsIgnoreCase(lu.getStringValue())) {
			return false;
		}

		lu = lu.getNextLexicalUnit();
		if (lu == null) {
			// Just one value: only OK if it was a var().
			return hasVar;
		}

		// Second argument: identifier (color space)
		type = lu.getLexicalUnitType();
		if (type != LexicalType.IDENT) {
			if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (type != LexicalType.ATTR) {
				// Further checks would be too complicated
				return hasVar;
			}
		}

		lu = lu.getNextLexicalUnit(); // Expect a comma if not var()
		if (lu == null) {
			// Just two values: only OK if it was a var().
			return hasVar;
		}

		type = lu.getLexicalUnitType();
		boolean lastTypeIsComma = type == LexicalType.OPERATOR_COMMA;
		if (!lastTypeIsComma) {
			// Should be the interpolation method
			if (type == LexicalType.IDENT || type == LexicalType.ATTR) {
				lu = lu.getNextLexicalUnit();
				if (lu == null) {
					// Three items: only OK if there was a var().
					return hasVar;
				}
				type = lu.getLexicalUnitType();
				if (type == LexicalType.IDENT) {
					if (!hasVar && !"hue".equalsIgnoreCase(lu.getStringValue())) {
						return false;
					}
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						// Three items: only OK if there was a var().
						return hasVar;
					}
					type = lu.getLexicalUnitType();
				}
				lastTypeIsComma = type == LexicalType.OPERATOR_COMMA;
			} else if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (!hasVar) {
				return false;
			}
		}

		if (lastTypeIsComma) {
			lu = lu.getNextLexicalUnit();
		}

		if (lu == null) {
			// Ending with a comma: error
			return !lastTypeIsComma && hasVar;
		}

		CSSValueSyntax synColor = new SyntaxParser().parseSyntax("<color>");

		// Check the first color spec
		LexicalType uType = lu.getLexicalUnitType();
		if (uType == LexicalType.PERCENTAGE || uType == LexicalType.CALC
				|| uType == LexicalType.MATH_FUNCTION || uType == LexicalType.FUNCTION) {
			// Could be % <color>
			lu = lu.getNextLexicalUnit();
			if (lu == null || cannotBeColor(lu, synColor)) {
				// No color
				return false;
			}
			lu = lu.getNextLexicalUnit();
		} else if (uType == LexicalType.VAR) {
			hasVar = true;
			lu = lu.getNextLexicalUnit();
		} else if (cannotBeColor(lu, synColor)) {
			return false;
		} else {
			// Check for % after <color>
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				return hasVar;
			}
			uType = lu.getLexicalUnitType();
			if (uType != LexicalType.OPERATOR_COMMA) {
				switch (uType) {
				case VAR:
					hasVar = true;
				case PERCENTAGE:
				case CALC:
				case MATH_FUNCTION:
				case FUNCTION:
				case ATTR:
					lu = lu.getNextLexicalUnit();
					break;
				default:
					return false;
				}
			}
		}

		if (lu == null) {
			return hasVar;
		}

		uType = lu.getLexicalUnitType();
		// Must be a comma if not var()
		if (uType != LexicalType.OPERATOR_COMMA) {
			// Assume the rest is right if we got the right type
			return hasVar && (!cannotBeColor(lu, synColor) || uType == LexicalType.PERCENTAGE
					|| uType == LexicalType.CALC || uType == LexicalType.MATH_FUNCTION
					|| uType == LexicalType.FUNCTION || uType == LexicalType.VAR);
		} else {
			lu = lu.getNextLexicalUnit();
			uType = lu.getLexicalUnitType();
		}

		// Now examine the second color spec
		if (uType == LexicalType.PERCENTAGE || uType == LexicalType.CALC
				|| uType == LexicalType.MATH_FUNCTION || uType == LexicalType.FUNCTION) {
			// Could be % <color>
			lu = lu.getNextLexicalUnit();
			if (lu == null || cannotBeColor(lu, synColor)) {
				// No color
				return false;
			}
			lu = lu.getNextLexicalUnit();
		} else if (uType == LexicalType.VAR) {
			hasVar = true;
			lu = lu.getNextLexicalUnit();
		} else if (cannotBeColor(lu, synColor)) {
			return false;
		} else {
			// Check for % after <color>
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				return true;
			}
			uType = lu.getLexicalUnitType();
			if (uType == LexicalType.PERCENTAGE || uType == LexicalType.CALC
					|| uType == LexicalType.MATH_FUNCTION || uType == LexicalType.FUNCTION
					|| uType == LexicalType.VAR || uType == LexicalType.ATTR) {
				lu = lu.getNextLexicalUnit();
			} else {
				return false;
			}
		}

		// Loop while there are VARs
		while (lu != null) {
			if (lu.getLexicalUnitType() != LexicalType.VAR) {
				return false;
			}
			lu = lu.getNextLexicalUnit();
		}

		return true;
	}

	private boolean isValidAlpha(CSSContentHandler handler, final int index, LexicalUnitImpl lu) {
		LexicalType type = lu.getLexicalUnitType();
		switch (type) {
		case INTEGER:
			int iAlpha = lu.getIntegerValue();
			if (iAlpha < 0) {
				lu.intValue = 0;
				warn(handler, index, "Color alpha has value under 0.");
			} else if (iAlpha > 1) {
				lu.intValue = 1;
				warn(handler, index, "Color alpha has value over 1.");
			}
			break;
		case REAL:
			float fAlpha = lu.getFloatValue();
			if (fAlpha < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color alpha has value under 0.");
			} else if (fAlpha > 1f) {
				lu.floatValue = 1f;
				warn(handler, index, "Color alpha has value over 1.");
			}
			break;
		case PERCENTAGE:
			fAlpha = lu.getFloatValue();
			if (fAlpha < 0f) {
				lu.floatValue = 0f;
				warn(handler, index, "Color alpha has value under 0%.");
			} else if (fAlpha > 100f) {
				lu.floatValue = 100f;
				warn(handler, index, "Color alpha has value over 100%.");
			}
			break;
		case IDENT:
			if (!"none".equalsIgnoreCase(lu.getStringValue())) {
				return false;
			}
			break;
		case VAR:
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION:
		case ATTR:
			break;
		default:
			return false;
		}

		// Loop while there are VARs
		lu = lu.nextLexicalUnit;
		while (lu != null) {
			if (lu.getLexicalUnitType() != LexicalType.VAR) {
				return false;
			}
			lu = lu.nextLexicalUnit;
		}

		return true;
	}

	private static boolean isComponentType(LexicalType type) {
		return type == LexicalType.INTEGER || type == LexicalType.PERCENTAGE
				|| type == LexicalType.REAL || type == LexicalType.VAR || type == LexicalType.CALC
				|| type == LexicalType.IDENT || type == LexicalType.MATH_FUNCTION
				|| type == LexicalType.FUNCTION || type == LexicalType.ATTR;
	}

	private static boolean isAngleUnit(LexicalUnit lu) {
		short unit = lu.getCssUnit();
		if (!CSSUnit.isAngleUnitType(unit)) {
			if (lu.getLexicalUnitType() == LexicalType.VAR) {
				return false;
			}
			return lu.shallowMatch(SyntaxParser.createSimpleSyntax("angle")) == Match.TRUE;
		}
		return true;
	}

	private static boolean isPercentageUnit(LexicalUnit unit) {
		return unit.shallowMatch(SyntaxParser.createSimpleSyntax("percentage")) == Match.TRUE;
	}

	private static boolean cannotBeColor(LexicalUnit lu, CSSValueSyntax syn) {
		return lu.shallowMatch(syn) == Match.FALSE;
	}

	/**
	 * Add an {@code EMPTY} lexical unit at the end of the current lexical chain.
	 */
	protected void addEmptyLexicalUnit(CSSContentHandler handler) {
		if (handler instanceof LexicalProvider) {
			((LexicalProvider) handler).addEmptyLexicalUnit();
		} else {
			throw new CSSException("Found var() in invalid context.");
		}
	}

	protected void error(CSSContentHandler handler, int index, String message) {
		handler.handleError(index, ParseHelper.ERR_WRONG_VALUE, message);
	}

	protected void warn(CSSContentHandler handler, int index, String message) {
		handler.handleWarning(index, ParseHelper.WARN_VALUE, message);
	}

	/**
	 * Get the factory for the given function name.
	 * 
	 * @param lcFunctionName the lower case function name.
	 * @return the factory, or {@code null} if none.
	 */
	public LexicalUnitFactory getFactory(String lcFunctionName) {
		return factories.get(lcFunctionName);
	}

}
