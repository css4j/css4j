/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.uparser.TokenProducer;

class FunctionFactories {

	private final Map<String, LexicalUnitFactory> factories = createFactoryMap();

	private Map<String, LexicalUnitFactory> createFactoryMap() {
		Map<String, LexicalUnitFactory> factories = new HashMap<>(101);

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

			@Override
			public boolean validate(CSSContentHandler handler, int index, LexicalUnitImpl lu) {
				LexicalType type;
				return lu.parameters != null
						&& ((type = lu.parameters.getLexicalUnitType()) == LexicalType.IDENT
								|| type == LexicalType.VAR)
						&& LexicalUnitFactory.super.validate(handler, index, lu);
			}

		});

		factories.put("type", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new GenericFunctionUnitImpl(LexicalType.TYPE_FUNCTION);
			}

			@Override
			public void handle(ValueTokenHandler parent, int index) {
				parent.yieldHandling(new TypeFunctionTH(parent));
				// Change prevcp to 41 for comments
				parent.prevcp = TokenProducer.CHAR_RIGHT_PAREN;
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
				return new URLUnitImpl();
			}

			@Override
			public String canonicalName(String lcName) {
				return null;
			}

			@Override
			public void handle(ValueTokenHandler parent, int index) {
				URLTokenHandler handler = new URLTokenHandler(parent) {

					@Override
					public void rightParenthesis(int index) {
						super.rightParenthesis(index);
						parent.endFunctionArgument(index);
					}

				};
				parent.yieldHandling(handler);
				// Change prevcp to 41 for comments
				parent.prevcp = TokenProducer.CHAR_RIGHT_PAREN;
			}

		});

		factories.put("if", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new GenericFunctionUnitImpl();
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

			@Override
			public void handle(ValueTokenHandler parent, int index) {
				parent.yieldHandling(new ElementReferenceTH(parent));
				// Change prevcp to 41 for comments
				parent.prevcp = TokenProducer.CHAR_RIGHT_PAREN;
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
				return isValidLabColor(handler, index, currentlu, 100, 100f);
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
				return isValidLabColor(handler, index, currentlu, 1, 1f);
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

		factories.put("anchor-size", new LexicalUnitFactory() {

			@Override
			public LexicalUnitImpl createUnit() {
				return new AnchorSizeUnitImpl();
			}

		});

		/*
		 * Pseudo-classes (nesting)
		 */

		factories.put("dir", new PseudoUnitFactory("dir"));
		factories.put("has", new PseudoUnitFactory("has"));
		factories.put("is", new PseudoUnitFactory("is"));
		factories.put("lang", new PseudoUnitFactory("lang"));
		factories.put("not", new PseudoUnitFactory("not"));
		factories.put("nth-child", new PseudoUnitFactory("nth-child"));
		factories.put("nth-last-child", new PseudoUnitFactory("nth-last-child"));
		factories.put("nth-last-of-type", new PseudoUnitFactory("nth-last-of-type"));
		factories.put("nth-of-type", new PseudoUnitFactory("nth-of-type"));
		factories.put("where", new PseudoUnitFactory("where"));
		factories.put("host", new PseudoUnitFactory("host"));
		factories.put("host-context", new PseudoUnitFactory("host-context"));
		factories.put("state", new PseudoUnitFactory("state"));

		return factories;
	}

	/**
	 * Handle pseudo-classes in nested selectors.
	 */
	private class PseudoUnitFactory implements LexicalUnitFactory {

		private final String name;

		PseudoUnitFactory(String name) {
			super();
			this.name = name;
		}

		@Override
		public LexicalUnitImpl createUnit() {
			return new EmptyUnitImpl();
		}

		@Override
		public void handle(ValueTokenHandler parent, int index) {
			parent.buffer.setLength(0);
			BufferTokenHandler selh = parent.nestedSelectorHandler(index);
			if (selh != null) {
				selh.word(index, name);
				selh.leftParenthesis(index);
				parent.yieldHandling(selh);
			} else {
				FunctionFactories.this.error(parent, index, "Invalid pseudo-class: " + name);
			}
		}

	}

	private boolean isValidRGBColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		if (lu == null) {
			return false;
		}

		short valCount = 0;
		boolean hasVar = false;

		LexicalType type = lu.getLexicalUnitType();

		// Skip any var()
		while (type == LexicalType.VAR) {
			hasVar = true;
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return true;
			}
			type = lu.getLexicalUnitType();
		}

		boolean hasNoCommas = false;

		// check for 'from'
		if (type == LexicalType.IDENT) {
			String s = lu.getStringValue();
			if ("from".equalsIgnoreCase(s)) {
				hasVar = false;
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return false;
				} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
					hasVar = true;
				}
				hasNoCommas = true;
			} else if (isRGBComponentName(s)) {
				valCount = 1;
			} else {
				return false;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
		}

		LexicalType lastType = LexicalType.UNKNOWN;
		boolean hasCommas = false;
		do {
			type = lu.getLexicalUnitType();
			// Check component type
			switch (type) {
			case OPERATOR_COMMA:
				if (lastType == LexicalType.OPERATOR_COMMA || hasNoCommas
						|| (valCount == 0 && !hasVar) || (valCount > 1 && !hasCommas)) {
					return false;
				}
				hasCommas = true;
				break;
			case INTEGER:
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
				valCount++;
				break;
			case REAL:
				float fvalue = lu.getFloatValue();
				if (fvalue < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has value under 0.");
				} else if (fvalue > 255f) {
					warn(handler, index, "Color component has value over 255.");
				}
				valCount++;
				break;
			case PERCENTAGE:
				fvalue = lu.getFloatValue();
				if (fvalue < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has percentage under 0%.");
				} else if (fvalue > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has percentage over 100%.");
				}
				valCount++;
				break;
			case OPERATOR_SLASH:
				lu = lu.nextLexicalUnit;
				if (lu == null || (valCount < 3 && !hasVar) || hasCommas || valCount > 3) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case IDENT:
				String s = lu.getStringValue();
				if (!isRGBComponentName(s)) {
					return false;
				} else { // none or component name
					valCount++;
				}
				break;
			case VAR:
				hasVar = true;
				break;
			default:
				valCount++;
				break;
			}

			if (!hasCommas && !hasVar && valCount > 1) {
				hasNoCommas = true;
			}

			lastType = type;
			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return valCount == 3 || valCount == 4 || (hasVar && valCount < 3);
	}

	private static boolean isRGBComponentName(String s) {
		return "r".equalsIgnoreCase(s) || "g".equalsIgnoreCase(s) || "b".equalsIgnoreCase(s)
				|| "none".equalsIgnoreCase(s) || "alpha".equalsIgnoreCase(s);
	}

	private boolean isValidHSLColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		if (lu == null) {
			return false;
		}

		short valCount = 0;
		boolean hasCommas = false;
		boolean hasNoCommas = false;
		boolean hasVar = false;

		LexicalType type = lu.getLexicalUnitType();

		// Skip any var()
		while (type == LexicalType.VAR) {
			hasVar = true;
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return true;
			}
			type = lu.getLexicalUnitType();
		}

		// check for 'from'
		if (type == LexicalType.IDENT) {
			String s = lu.getStringValue();
			if ("from".equalsIgnoreCase(s)) {
				hasVar = false;
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return false;
				} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
					hasVar = true;
				}
				hasNoCommas = true;
			} else if (isHSLComponentName(s)) {
				valCount = 1;
			} else {
				return false;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
			type = lu.getLexicalUnitType();
		}

		if (!hasVar && valCount == 0) {
			// Hue
			switch (type) {
			case INTEGER:
				normalizeIntHue(handler, index, lu);
				valCount++;
				break;
			case REAL:
				normalizeHue(handler, index, lu);
				valCount++;
				break;
			case DIMENSION:
				if (isHueUnit(handler, index, lu)) {
					valCount++;
					break;
				}
				return false;
			case IDENT:
				String s = lu.getStringValue();
				if (!isHSLComponentName(s)) {
					return false;
				} else { // none or component name
					valCount++;
				}
				break;
			case VAR:
				hasVar = true;
				break;
			case OPERATOR_COMMA:
				if (hasNoCommas) {
					return false;
				}
				if (hasVar) {
					hasCommas = true;
					break;
				}
			case PERCENTAGE:
			case OPERATOR_SLASH:
				return false;
			default:
				valCount++;
				break;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
		}

		do {
			type = lu.getLexicalUnitType();
			// Check component type
			switch (type) {
			case INTEGER:
				int value = lu.getIntegerValue();
				if (value < 0) {
					lu.intValue = 0;
					warn(handler, index, "Color component has value under 0.");
				} else if (valCount == 3 && value > 1) {
					lu.intValue = 1;
					warn(handler, index, "Color alpha has value over 1.");
				} else if (valCount > 0 && value > 100) {
					lu.intValue = 100;
					warn(handler, index, "Peecentage component has value over 100.");
				}
				valCount++;
				break;
			case REAL:
				float fvalue = lu.getFloatValue();
				if (fvalue < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has value under 0.");
				} else if (valCount > 0 && fvalue > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Peecentage component has value over 100.");
				}
				valCount++;
				break;
			case PERCENTAGE:
				fvalue = lu.getFloatValue();
				if (fvalue < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has percentage under 0%.");
				} else if (fvalue > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has percentage over 100%.");
				}
				valCount++;
				break;
			case OPERATOR_COMMA:
				if (hasNoCommas) {
					return false;
				}
				hasCommas = true;
				break;
			case OPERATOR_SLASH:
				lu = lu.nextLexicalUnit;
				if (lu == null || (valCount < 3 && !hasVar) || hasCommas || valCount > 3) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case IDENT:
				String s = lu.getStringValue();
				if (!isHSLComponentName(s)) {
					return false;
				} else { // none or component name
					valCount++;
				}
				break;
			case VAR:
				hasVar = true;
				break;
			case CALC:
			case MATH_FUNCTION:
			case SUB_EXPRESSION:
			case FUNCTION:
			case ATTR:
				valCount++;
				break;
			case DIMENSION:
				// Could be hue if var()
				if (!hasVar || valCount > 0) {
					return false;
				}
				if (!isHueUnit(handler, index, lu)) {
					return false;
				}
				valCount++;
				break;
			default:
				return false;
			}

			if (!hasCommas && !hasVar && valCount > 1) {
				hasNoCommas = true;
			}

			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return valCount == 3 || valCount == 4 || (hasVar && valCount < 3);
	}

	private static boolean isHSLComponentName(String s) {
		return "h".equalsIgnoreCase(s) || "l".equalsIgnoreCase(s) || "s".equalsIgnoreCase(s)
				|| "none".equalsIgnoreCase(s) || "alpha".equalsIgnoreCase(s);
	}

	private boolean isValidHWBColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu) {
		LexicalUnitImpl lu = currentlu.parameters;
		if (lu == null) {
			return false;
		}

		short valCount = 0;
		boolean hasVar = false;

		LexicalType type = lu.getLexicalUnitType();

		// Skip any var()
		while (type == LexicalType.VAR) {
			hasVar = true;
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return true;
			}
			type = lu.getLexicalUnitType();
		}

		// check for 'from'
		if (type == LexicalType.IDENT) {
			String s = lu.getStringValue();
			if ("from".equalsIgnoreCase(s)) {
				hasVar = false;
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return false;
				} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
					hasVar = true;
				}
			} else if (isHWBComponentName(s)) {
				valCount = 1;
			} else {
				return false;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
			type = lu.getLexicalUnitType();
		}

		if (!hasVar && valCount == 0) {
			// Hue
			switch (type) {
			case INTEGER:
				normalizeIntHue(handler, index, lu);
				valCount++;
				break;
			case REAL:
				normalizeHue(handler, index, lu);
				valCount++;
				break;
			case DIMENSION:
				if (isHueUnit(handler, index, lu)) {
					valCount++;
					break;
				}
				return false;
			case VAR:
				hasVar = true;
				break;
			case IDENT:
				String s = lu.getStringValue();
				if (!isHSLComponentName(s)) {
					return false;
				}
				// pass-through
			case CALC:
			case MATH_FUNCTION:
			case SUB_EXPRESSION:
			case FUNCTION:
			case ATTR:
				valCount++;
				break;
			default:
				return false;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
		}

		do {
			type = lu.getLexicalUnitType();
			// Check component type
			switch (type) {
			case INTEGER:
			case REAL:
			case CALC:
			case MATH_FUNCTION:
			case SUB_EXPRESSION:
			case FUNCTION:
			case ATTR:
				valCount++;
				break;
			case PERCENTAGE:
				float fvalue = lu.getFloatValue();
				if (fvalue < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has percentage under 0%.");
				} else if (fvalue > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has percentage over 100%.");
				}
				valCount++;
				if (valCount > 3) {
					// The slash could be inside a var()
					if (!hasVar || valCount > 4) {
						return false;
					}
					return isValidAlpha(handler, index, lu);
				}
				// Clamp
				float fval = lu.getFloatValue();
				if (fval < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has percentage under 0%.");
				} else if (fval > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has percentage over 100%.");
				}
				break;
			case OPERATOR_SLASH:
				lu = lu.nextLexicalUnit;
				if (lu == null || (valCount < 3 && !hasVar) || valCount > 3) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case IDENT:
				String s = lu.getStringValue();
				if (!isHWBComponentName(s)) {
					return false;
				} else { // none or component name
					valCount++;
				}
				break;
			case VAR:
				hasVar = true;
				break;
			case DIMENSION:
				// Could be hue if var()
				if (!hasVar || valCount > 0) {
					return false;
				}
				if (!isHueUnit(handler, index, lu)) {
					return false;
				}
				valCount++;
				break;
			default:
				return false;
			}

			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return valCount == 3 || valCount == 4 || (hasVar && valCount < 3);
	}

	private static boolean isHWBComponentName(String s) {
		return "h".equalsIgnoreCase(s) || "w".equalsIgnoreCase(s) || "b".equalsIgnoreCase(s)
				|| "none".equalsIgnoreCase(s) || "alpha".equalsIgnoreCase(s);
	}

	private boolean isValidLabColor(CSSContentHandler handler, int index,
			LexicalUnitImpl currentlu, int iUpperLightness, float fUpperLightness) {
		LexicalUnitImpl lu = currentlu.parameters;
		short valCount = 0;
		boolean hasVar = false;
		if (lu == null) {
			return false;
		}

		LexicalType type = lu.getLexicalUnitType();
		// Skip any var()
		while (type == LexicalType.VAR) {
			hasVar = true;
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return true;
			}
			type = lu.getLexicalUnitType();
		}

		// check for 'from'
		if (type == LexicalType.IDENT) {
			String s = lu.getStringValue();
			if ("from".equalsIgnoreCase(s)) {
				hasVar = false;
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return false;
				} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
					hasVar = true;
				}
			} else if (isLabComponentName(s)) {
				valCount = 1;
			} else {
				return false;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
			type = lu.getLexicalUnitType();
		}

		if (valCount == 0 && !hasVar) {
			// First argument: percentage, real or integer
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
					&& type != LexicalType.SUB_EXPRESSION && type != LexicalType.ATTR
					&& type != LexicalType.FUNCTION
					&& (type != LexicalType.IDENT || !isLabComponentName(lu.getStringValue()))) {
				return false;
			}
			valCount++;

			lu = lu.nextLexicalUnit;
			if (lu == null) {
				// Only OK if we got a var().
				return hasVar;
			}
		}

		// Now it must be ab (unless var() involved)
		do {
			type = lu.getLexicalUnitType();
			// Check component type
			switch (type) {
			case PERCENTAGE:
				// Could be a or b, also alpha if the slash is inside a var()
				valCount++;
				if (valCount > 3) {
					// The slash could be inside a var()
					if (!hasVar || valCount > 4) {
						return false;
					}
					return isValidAlpha(handler, index, lu);
				}
				// If it has a var(), we don't know whether to clamp
				// as a/b or as alpha
				float fval = lu.getFloatValue();
				if (fval > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has percentage over 100%.");
				} else if (fval < 0f && !hasVar) {
					// Clamp
					if (valCount == 4) {
						// alpha
						lu.floatValue = 0f;
						warn(handler, index, "Color alpha has percentage under 0%.");
					} else if (fval < -100f) {
						lu.floatValue = -100f;
						warn(handler, index, "Color component has percentage under -100%.");
					}
				}
				break;
			case OPERATOR_SLASH:
				lu = lu.nextLexicalUnit;
				if (lu == null || (valCount < 3 && !hasVar) || valCount > 3) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case IDENT:
				String s = lu.getStringValue();
				if (!isLabComponentName(s)) {
					return false;
				} else {
					valCount++;
				}
				break;
			case OPERATOR_COMMA:
			case DIMENSION:
				return false;
			case VAR:
				hasVar = true;
				break;
			default:
				valCount++;
				if (valCount >= 4) {
					// The slash could be inside a var()
					if (!hasVar || valCount > 4) {
						return false;
					}
					return isValidAlpha(handler, index, lu);
				}
				break;
			}

			lu = lu.nextLexicalUnit;
		} while (lu != null);

		return valCount == 3 || valCount == 4 || (hasVar && valCount < 3);
	}

	private static boolean isLabComponentName(String s) {
		return "l".equalsIgnoreCase(s) || "a".equalsIgnoreCase(s) || "b".equalsIgnoreCase(s)
				|| "none".equalsIgnoreCase(s) || "alpha".equalsIgnoreCase(s);
	}

	private boolean isValidLCHColor(CSSContentHandler handler, int index, LexicalUnitImpl currentlu,
			int iUpperLightness, float fUpperLightness) {
		LexicalUnitImpl lu = currentlu.parameters;
		short valCount = 0;
		boolean hasVar = false;
		if (lu == null) {
			return false;
		}

		LexicalType type = lu.getLexicalUnitType();
		// Skip any var()
		while (type == LexicalType.VAR) {
			hasVar = true;
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return true;
			}
			type = lu.getLexicalUnitType();
		}

		// check for 'from'
		if (type == LexicalType.IDENT) {
			String s = lu.getStringValue();
			if ("from".equalsIgnoreCase(s)) {
				hasVar = false;
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return false;
				} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
					hasVar = true;
				}
			} else if (isLCHComponentName(s)) {
				valCount = 1;
			} else {
				return false;
			}
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			}
		}

		if (valCount == 0 && !hasVar) {
			return isValidNonVarLCHColor(handler, index, lu, iUpperLightness, fUpperLightness);
		}

		// Now it could be any value due to var() involvement
		do {
			type = lu.getLexicalUnitType();
			// Check component type
			switch (type) {
			case PERCENTAGE:
				float fvalue = lu.getFloatValue();
				if (fvalue < 0f) {
					lu.floatValue = 0f;
					warn(handler, index, "Color component has percentage under 0%.");
				} else if (fvalue > 100f) {
					lu.floatValue = 100f;
					warn(handler, index, "Color component has percentage over 100%.");
				}
				valCount++;
				break;
			case REAL:
				if (!hasVar && valCount == 2) {
					normalizeHue(handler, index, lu);
					valCount++;
					break;
				}
			case INTEGER:
			case CALC:
			case MATH_FUNCTION:
			case SUB_EXPRESSION:
			case FUNCTION:
			case ATTR:
				valCount++;
				if (valCount == 4) {
					return isValidAlpha(handler, index, lu);
				}
				break;
			case IDENT:
				String s = lu.getStringValue();
				if (!isLCHComponentName(s)) {
					return false;
				} else {
					valCount++;
				}
				break;
			case DIMENSION:
				// Could be hue
				short unit = lu.getCssUnit();
				if (!CSSUnit.isAngleUnitType(unit) || valCount > 2 || (valCount == 1 && !hasVar)) {
					return false;
				}
				normalizeHue(handler, index, lu);
				valCount++;
				break;
			case OPERATOR_SLASH:
				lu = lu.nextLexicalUnit;
				if (lu == null || (valCount < 3 && !hasVar) || valCount > 3) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
			case VAR:
				hasVar = true;
				break;
			default:
				return false;
			}
		} while ((lu = lu.nextLexicalUnit) != null);

		return valCount == 3 || valCount == 4 || (hasVar && valCount < 3);
	}

	private boolean isHueUnit(CSSContentHandler handler, final int index, LexicalUnitImpl lu) {
		if (CSSUnit.isAngleUnitType(lu.getCssUnit())) {
			normalizeHue(handler, index, lu);
			return true;
		}
		return false;
	}

	private void normalizeHue(CSSContentHandler handler, final int index, LexicalUnitImpl lu) {
		float h = lu.getFloatValue();
		short unit = lu.getCssUnit();
		float hdeg;
		if (unit != CSSUnit.CSS_DEG && unit != CSSUnit.CSS_NUMBER) {
			hdeg = NumberValue.floatValueConversion(h, unit, CSSUnit.CSS_DEG);
		} else {
			hdeg = h;
		}
		if (Math.abs(hdeg) > 360f) {
			double dh = Math.IEEEremainder(hdeg, 360d);
			if (dh < 0) {
				dh += 360d;
				warn(handler, index, "Color hue has value under 0.");
			} else {
				warn(handler, index, "Color hue has value over 360.");
			}
			dh = Math.rint(dh * 1e4) / 1e4;
			dh += 0d; // Avoid -0
			lu.floatValue = (float) dh;
			lu.setUnitType(LexicalType.REAL);
			lu.setCssUnit(CSSUnit.CSS_NUMBER);
			lu.dimensionUnitText = "";
		} else if (hdeg < 0) {
			warn(handler, index, "Color hue has value under 0.");
			hdeg += 360f;
			lu.floatValue = hdeg;
			lu.setUnitType(LexicalType.REAL);
			lu.setCssUnit(CSSUnit.CSS_NUMBER);
			lu.dimensionUnitText = "";
		}
	}

	private void normalizeIntHue(CSSContentHandler handler, final int index, LexicalUnitImpl lu) {
		int h = lu.getIntegerValue();
		if (Math.abs(h) > 360) {
			warn(handler, index, "Color hue has value outside the 0-360 range.");
			h = Math.floorMod(h, 360);
			lu.intValue = h;
		} else if (h < 0) {
			warn(handler, index, "Color hue has value under 0.");
			h += 360;
			lu.intValue = h;
		}
	}

	private boolean isValidNonVarLCHColor(CSSContentHandler handler, final int index,
			LexicalUnitImpl lu, int iUpperLightness, float fUpperLightness) {
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
				&& type != LexicalType.SUB_EXPRESSION && type != LexicalType.ATTR
				&& type != LexicalType.FUNCTION
				&& (type != LexicalType.IDENT || !isLCHComponentName(lu.getStringValue()))) {
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
					warn(handler, index, "Color chroma has value under 0.");
				}
			}
		} else if (type == LexicalType.INTEGER) {
			if (!hasVar) {
				// Clamp
				int iC = lu.getIntegerValue();
				if (iC < 0) {
					lu.intValue = 0;
					warn(handler, index, "Color chroma has value under 0.");
				}
			}
		} else if (type != LexicalType.CALC && type != LexicalType.MATH_FUNCTION
				&& type != LexicalType.SUB_EXPRESSION && type != LexicalType.ATTR
				&& (type != LexicalType.IDENT || !isLCHComponentName(lu.getStringValue()))
				&& type != LexicalType.FUNCTION) {
			if (type == LexicalType.VAR) {
				hasVar = true;
			} else if (hasVar) {
				// Hue?
				if (CSSUnit.isAngleUnitType(lu.getCssUnit())) {
					normalizeHue(handler, index, lu);
					// We are done unless there is an alpha channel
					return checkNextSlashAplhaChannel(handler, index, lu, false);
				} else {
					// If not an angle, must be slash or alpha
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
		if (type != LexicalType.REAL && type != LexicalType.INTEGER
				&& !isHueUnit(handler, index, lu) && type != LexicalType.CALC
				&& type != LexicalType.MATH_FUNCTION && type != LexicalType.SUB_EXPRESSION
				&& type != LexicalType.ATTR && type != LexicalType.FUNCTION
				&& (type != LexicalType.IDENT || !isLCHComponentName(lu.getStringValue()))) {
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
		return checkNextSlashAplhaChannel(handler, index, lu, hasVar);
	}

	private static boolean isLCHComponentName(String s) {
		return "l".equalsIgnoreCase(s) || "c".equalsIgnoreCase(s) || "h".equalsIgnoreCase(s)
				|| "none".equalsIgnoreCase(s) || "alpha".equalsIgnoreCase(s);
	}

	/**
	 * Check that the next unit is either null or slash-alpha.
	 * 
	 * @param handler
	 * @param index
	 * @param lu
	 * @param hasVar
	 * @return
	 */
	private boolean checkNextSlashAplhaChannel(CSSContentHandler handler, final int index,
			LexicalUnitImpl lu, boolean hasVar) {
		lu = lu.nextLexicalUnit;
		if (lu != null) {
			LexicalType type = lu.getLexicalUnitType();
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
		boolean foundComponent = false;

		LexicalType type = lu.getLexicalUnitType();
		// Skip any var()
		while (type == LexicalType.VAR) {
			hasVar = true;
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return true;
			}
			type = lu.getLexicalUnitType();
		}

		// check for color space or 'from'
		if (type == LexicalType.IDENT) {
			String s = lu.getStringValue();
			if ("from".equalsIgnoreCase(s)) {
				hasVar = false;
				// The color is the next unit
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return false;
				}
				type = lu.getLexicalUnitType();
				boolean isVar = type == LexicalType.VAR;
				// Color space is next unit, unless var()
				lu = lu.nextLexicalUnit;
				if (lu == null) {
					return isVar;
				} else if (isVar || lu.getLexicalUnitType() == LexicalType.VAR) {
					hasVar = true;
				}
			}
			// Obtain unit next to color space
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				return hasVar;
			} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
				hasVar = true;
			}
		} else if (!hasVar) {
			// Already checked for type == VAR
			return false;
		}

		// Establish a value loop
		do {
			type = lu.getLexicalUnitType();
			switch (type) {
			case IDENT:
				if ("from".equalsIgnoreCase(lu.getStringValue())) {
					// Too late
					return false;
				}
			case REAL:
			case PERCENTAGE:
			case INTEGER:
			case CALC:
			case VAR:
			case MATH_FUNCTION:
			case SUB_EXPRESSION:
			case FUNCTION:
			case ATTR:
				foundComponent = true;
				break;
			case OPERATOR_SLASH:
				if (!foundComponent && !hasVar) {
					return false;
				}
				lu = lu.nextLexicalUnit;
				// This must be alpha channel value
				if (lu == null) {
					return false;
				}
				return isValidAlpha(handler, index, lu);
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
				|| uType == LexicalType.MATH_FUNCTION || uType == LexicalType.SUB_EXPRESSION
				|| uType == LexicalType.FUNCTION) {
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
				case ATTR:
				case SUB_EXPRESSION:
				case FUNCTION:
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
					|| uType == LexicalType.SUB_EXPRESSION || uType == LexicalType.VAR
					|| uType == LexicalType.ATTR || uType == LexicalType.FUNCTION);
		} else {
			lu = lu.getNextLexicalUnit();
			uType = lu.getLexicalUnitType();
		}

		// Now examine the second color spec
		if (uType == LexicalType.PERCENTAGE || uType == LexicalType.CALC
				|| uType == LexicalType.MATH_FUNCTION || uType == LexicalType.SUB_EXPRESSION
				|| uType == LexicalType.FUNCTION) {
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
					|| uType == LexicalType.MATH_FUNCTION || uType == LexicalType.SUB_EXPRESSION
					|| uType == LexicalType.FUNCTION || uType == LexicalType.VAR
					|| uType == LexicalType.ATTR) {
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
			String s = lu.getStringValue();
			if (!"none".equalsIgnoreCase(s) && !"alpha".equalsIgnoreCase(s)) {
				return false;
			}
			break;
		case VAR:
		case CALC:
		case MATH_FUNCTION:
		case SUB_EXPRESSION:
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
