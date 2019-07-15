/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSGradientValue;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;

/**
 * Implementation of a gradient value.
 */
public class GradientValue extends FunctionValue implements CSSGradientValue {

	private GradientType gradientType = CSSGradientValue.GradientType.OTHER_GRADIENT;

	GradientValue() {
		super();
	}

	GradientValue(GradientValue copied) {
		super(copied);
		gradientType = copied.gradientType;
	}

	@Override
	public GradientType getGradientType() {
		return gradientType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((gradientType == null) ? 0 : gradientType.hashCode());
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
		GradientValue other = (GradientValue) obj;
		if (gradientType != other.gradientType) {
			return false;
		}
		return true;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) throws DOMException {
			String funcname = lunit.getFunctionName();
			setFunctionName(funcname);
			funcname = funcname.toLowerCase(Locale.US);
			LexicalUnit lu = lunit.getParameters();
			if (lu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Gradient without arguments");
			}
			setCSSUnitType(CSS_GRADIENT);
			if (funcname.endsWith("linear-gradient")) {
				if (funcname.equals("linear-gradient")) {
					gradientType = GradientType.LINEAR_GRADIENT;
				} else if (funcname.equals("repeating-linear-gradient")) {
					gradientType = GradientType.REPEATING_LINEAR_GRADIENT;
				} else {
					gradientType = GradientType.OTHER_GRADIENT;
				}
				setLinearGradient(lu, new ValueFactory());
			} else if (funcname.endsWith("radial-gradient")) {
				if (funcname.equals("radial-gradient")) {
					gradientType = GradientType.RADIAL_GRADIENT;
				} else if (funcname.equals("repeating-radial-gradient")) {
					gradientType = GradientType.REPEATING_RADIAL_GRADIENT;
				} else {
					gradientType = GradientType.OTHER_GRADIENT;
				}
				setRadialGradient(lu, new ValueFactory());
			} else if (funcname.endsWith("conic-gradient")) {
				if (funcname.equals("conic-gradient")) {
					gradientType = GradientType.CONIC_GRADIENT;
				} else if (funcname.equals("repeating-conic-gradient")) {
					gradientType = GradientType.REPEATING_CONIC_GRADIENT;
				} else {
					gradientType = GradientType.OTHER_GRADIENT;
				}
				setConicGradient(lu, new ValueFactory());
			} else {
				throw new DOMException(DOMException.SYNTAX_ERR, "Unknown gradient type");
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLinearGradient(LexicalUnit lu, ValueFactory factory) {
			// [ [ <angle> | to <side-or-corner> ] ,]? <color-stop>[, <color-stop>]+
			// <side-or-corner> = [left | right] || [top | bottom]
			// <color-stop> = <color> [ <percentage> | <length> ]?
			// If the argument is to top, to right, to bottom, or to left, the angle of the gradient
			// line is 0deg, 90deg, 180deg, or 270deg, respectively.
			// If angle is omitted, it defaults to 'to bottom'.
			LexicalUnit colorStopLU;
			if (isLinearColorStop(lu)) {
				// Omitted
				colorStopLU = lu;
			} else {
				colorStopLU = setAngleArguments(lu, factory);
			}
			if (colorStopLU == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Expected angle, side or color stop, found: " + lu.toString());
			}
			do {
				colorStopLU = processLinearColorStop(colorStopLU, factory);
				if (colorStopLU != null) {
					if (colorStopLU.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						colorStopLU = colorStopLU.getNextLexicalUnit();
					} else {
						throw new DOMException(DOMException.SYNTAX_ERR, "Expected color stops, found: " + lu.toString());
					}
				}
			} while (colorStopLU != null); // TODO: check for at least 2 color stops
		}

		/*
		 * Returns null if there are no appropriate angle/to arguments,
		 * or the next unit is null (should not be).
		 */
		private LexicalUnit setAngleArguments(LexicalUnit lu, ValueFactory factory) {
			LexicalUnit finalLU = null;
			if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
				String ident = lu.getStringValue().toLowerCase(Locale.US);
				ValueList list = ValueList.createWSValueList();
				if ("to".equals(ident)) {
					list.add(factory.createCSSPrimitiveValue(lu, true));
					lu = lu.getNextLexicalUnit();
				} else if (!isSideValue(ident)) {
					return null;
				} else {
					reportSyntaxWarning("Missing 'to' in side/corner specification in gradient: " + lu.toString());
				}
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
					ident = lu.getStringValue().toLowerCase(Locale.US);
					if (isSideValue(ident)) {
						list.add(factory.createCSSPrimitiveValue(lu, true));
						lu = lu.getNextLexicalUnit();
						finalLU = lu;
						if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
							ident = lu.getStringValue().toLowerCase(Locale.US);
							if (isSideValue(ident)) {
								list.add(factory.createCSSPrimitiveValue(lu, true));
								finalLU = lu.getNextLexicalUnit();
							}
						}
						getArguments().add(list);
					}
				}
			} else if (ValueFactory.isAngleSACUnit(lu)) {
				getArguments().add(factory.createCSSPrimitiveValue(lu, true));
				finalLU = lu.getNextLexicalUnit();
			}
			if (finalLU != null) {
				if (finalLU.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					finalLU = finalLU.getNextLexicalUnit();
				} else {
					finalLU = null;
				}
			}
			return finalLU;
		}

		private boolean isSideValue(String ident) {
			return "left".equals(ident) || "right".equals(ident) || "top".equals(ident) || "bottom".equals(ident);
		}

		private boolean isLinearColorStop(LexicalUnit lu) {
			// <linear-color-stop> = <color> && <color-stop-length>?
			// <color-stop-length> = <length-percentage>{1,2}
			LexicalUnit lu2 = lu.getNextLexicalUnit();
			if (BaseCSSStyleDeclaration.testColor(lu)) {
				return true;
			} else if (lu2 != null && BaseCSSStyleDeclaration.testColor(lu2)
					&& ValueFactory.isSizeSACUnit(lu)) {
				return true;
			}
			return false;
		}

		private LexicalUnit processLinearColorStop(LexicalUnit lu, ValueFactory factory) {
			LexicalUnit finalLU = null;
			LexicalUnit lu2 = lu.getNextLexicalUnit();
			if (BaseCSSStyleDeclaration.testColor(lu)) {
				AbstractCSSPrimitiveValue color = factory.createCSSPrimitiveValue(lu, true);
				if (lu2 != null && ValueFactory.isSizeSACUnit(lu2)) {
					ValueList list = ValueList.createWSValueList();
					list.add(color);
					list.add(factory.createCSSPrimitiveValue(lu2, true));
					getArguments().add(list);
					lu2 = lu2.getNextLexicalUnit();
					if (lu2 != null && ValueFactory.isSizeSACUnit(lu2)) {
						list.add(factory.createCSSPrimitiveValue(lu2, true));
						lu2 = lu2.getNextLexicalUnit();
					}
				} else {
					getArguments().add(color);
				}
				finalLU = lu2;
			} else if (lu2 != null && BaseCSSStyleDeclaration.testColor(lu2)
					&& ValueFactory.isSizeSACUnit(lu)) {
				ValueList list = ValueList.createWSValueList();
				list.add(factory.createCSSPrimitiveValue(lu2, true));
				list.add(factory.createCSSPrimitiveValue(lu, true));
				getArguments().add(list);
				finalLU = lu2.getNextLexicalUnit();
			} else if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
				finalLU = lu;
			}
			return finalLU;
		}

		private void setRadialGradient(LexicalUnit lu, ValueFactory factory) {
			// [ [ <ending-shape> || <size> ] [ at <position> ]? , | at <position>, ]? <color-stop> [ , <color-stop> ]+
			// <ending-shape> = circle | ellipse
			LexicalUnit colorStopLU;
			if (!isLinearColorStop(lu)) {
				lu = processNonColorStop(lu, factory);
				if (lu != null && !isLinearColorStop(lu)) {
					lu = processNonColorStop(lu, factory);
				}
			}
			colorStopLU = lu;
			if (colorStopLU == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Missing color stop");
			}
			do {
				colorStopLU = processLinearColorStop(colorStopLU, factory);
				if (colorStopLU != null) {
					if (colorStopLU.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						colorStopLU = colorStopLU.getNextLexicalUnit();
					} else {
						throw new DOMException(DOMException.SYNTAX_ERR, "Expected color stops, found: " + lu.toString());
					}
				}
			} while (colorStopLU != null); // TODO: check for at least 2 color stops
		}

		private LexicalUnit processNonColorStop(LexicalUnit lu, ValueFactory factory) {
			ValueList list = ValueList.createWSValueList();
			while (lu != null) {
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
					break;
				}
				list.add(factory.createCSSPrimitiveValue(lu, true));
				lu = lu.getNextLexicalUnit();
			}
			if (list.getLength() != 1) {
				getArguments().add(list);
			} else {
				getArguments().add(list.item(0));
			}
			return lu;
		}

		private void setConicGradient(LexicalUnit lu, ValueFactory factory) {
			// [ from <angle> ]? [ at <position> ]?, <angular-color-stop-list>
			// <angular-color-stop-list> = [ <angular-color-stop> [, <angular-color-hint>]? ]# , <angular-color-stop>
			// <angular-color-stop> = <color> && <color-stop-angle>?
			// <angular-color-hint> = <angle-percentage>
			// <color-stop-angle> = <angle-percentage>{1,2}
			// <angle-percentage> = [ <angle> | <percentage> ]
			// <color-stop> = <color-stop-length> | <color-stop-angle>
			// <color-stop-length> = <length-percentage>{1,2}
			LexicalUnit colorStopLU;
			if (!isAngularColorStop(lu)) {
				ValueList list = ValueList.createWSValueList();
				while (lu != null) {
					if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						lu = lu.getNextLexicalUnit();
						break;
					}
					list.add(factory.createCSSPrimitiveValue(lu, true));
					lu = lu.getNextLexicalUnit();
				}
				if (list.getLength() != 1) {
					getArguments().add(list);
				} else {
					getArguments().add(list.item(0));
				}
			}
			colorStopLU = lu;
			if (colorStopLU == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Missing angle, position or color stop in gradient");
			}
			do {
				colorStopLU = processAngularColorStop(colorStopLU, factory);
				if (colorStopLU != null) {
					if (colorStopLU.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						colorStopLU = colorStopLU.getNextLexicalUnit();
					} else {
						throw new DOMException(DOMException.SYNTAX_ERR, "Expected color stops, found: " + lu.toString());
					}
				}
			} while (colorStopLU != null); // TODO: check for at least 2 color stops
		}

		private boolean isAngularColorStop(LexicalUnit lu) {
			// <angular-color-stop> = <color> && <color-stop-angle>?
			// <color-stop-angle> = <angle-percentage>{1,2}
			// <angle-percentage> = [ <angle> | <percentage> ]
			LexicalUnit lu2 = lu.getNextLexicalUnit();
			if (BaseCSSStyleDeclaration.testColor(lu)) {
				return true;
			} else if (lu2 != null && BaseCSSStyleDeclaration.testColor(lu2)
					&& (ValueFactory.isAngleSACUnit(lu)
							|| lu.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE)) {
				return true;
			}
			return false;
		}

		private LexicalUnit processAngularColorStop(LexicalUnit lu, ValueFactory factory) {
			// <angular-color-stop-list> = [ <angular-color-stop> [, <angular-color-hint>]? ]# , <angular-color-stop>
			// <angular-color-stop> = <color> && <color-stop-angle>?
			// <angular-color-hint> = <angle-percentage>
			LexicalUnit finalLU = null;
			LexicalUnit lu2 = lu.getNextLexicalUnit();
			if (BaseCSSStyleDeclaration.testColor(lu)) {
				AbstractCSSPrimitiveValue color = factory.createCSSPrimitiveValue(lu, true);
				if (lu2 != null && (ValueFactory.isAngleSACUnit(lu2) || lu2.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE)) {
					ValueList list = ValueList.createWSValueList();
					list.add(color);
					list.add(factory.createCSSPrimitiveValue(lu2, true));
					getArguments().add(list);
					lu2 = lu2.getNextLexicalUnit();
					if (lu2 != null && (ValueFactory.isAngleSACUnit(lu2) || lu2.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE)) {
						list.add(factory.createCSSPrimitiveValue(lu2, true));
						lu2 = lu2.getNextLexicalUnit();
					}
				} else {
					getArguments().add(color);
				}
				finalLU = lu2;
			} else if (lu2 != null && BaseCSSStyleDeclaration.testColor(lu2)
					&& (ValueFactory.isAngleSACUnit(lu) || lu.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE)) {
				ValueList list = ValueList.createWSValueList();
				list.add(factory.createCSSPrimitiveValue(lu2, true));
				list.add(factory.createCSSPrimitiveValue(lu, true));
				getArguments().add(list);
				finalLU = lu2.getNextLexicalUnit();
			} else if (ValueFactory.isAngleSACUnit(lu) || lu.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE) {
				// Hint
				getArguments().add(factory.createCSSPrimitiveValue(lu, true));
				finalLU = lu2;
			} else if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
				finalLU = lu;
			}
			return finalLU;
		}

	}

	@Override
	public GradientValue clone() {
		return new GradientValue(this);
	}

}
