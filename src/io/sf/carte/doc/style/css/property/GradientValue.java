/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSGradientValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;

/**
 * Implementation of a gradient value.
 */
public class GradientValue extends FunctionValue implements CSSGradientValue {

	private GradientType gradientType = CSSGradientValue.GradientType.OTHER_GRADIENT;

	GradientValue() {
		super(Type.GRADIENT);
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
		return gradientType == other.gradientType;
	}

	private static LexicalUnit findCustomPropertyFallback(LexicalUnit lunit) {
		LexicalUnit lu = lunit.getParameters();
		if (lu != null) {
			if (lu.getLexicalUnitType() == LexicalType.IDENT) {
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
						lu = lu.getNextLexicalUnit();
					} else {
						lu = null;
					}
				}
			} else {
				lu = null;
			}
		}
		return lu;
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
			funcname = funcname.toLowerCase(Locale.ROOT);
			LexicalUnit lu = lunit.getParameters();
			if (lu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Gradient without arguments");
			}
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
					if (colorStopLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
			if (lu.getLexicalUnitType() == LexicalType.IDENT) {
				String ident = lu.getStringValue().toLowerCase(Locale.ROOT);
				ValueList list = ValueList.createWSValueList();
				if ("to".equals(ident)) {
					list.add(factory.createCSSPrimitiveValue(lu, true));
					lu = lu.getNextLexicalUnit();
				} else if (!isSideValue(ident)) {
					return null;
				} else {
					reportSyntaxWarning("Missing 'to' in side/corner specification in gradient: " + lu.toString());
				}
				if (lu.getLexicalUnitType() == LexicalType.IDENT) {
					ident = lu.getStringValue().toLowerCase(Locale.ROOT);
					if (isSideValue(ident)) {
						list.add(factory.createCSSPrimitiveValue(lu, true));
						lu = lu.getNextLexicalUnit();
						finalLU = lu;
						if (lu.getLexicalUnitType() == LexicalType.IDENT) {
							ident = lu.getStringValue().toLowerCase(Locale.ROOT);
							if (isSideValue(ident)) {
								list.add(factory.createCSSPrimitiveValue(lu, true));
								finalLU = lu.getNextLexicalUnit();
							}
						}
						getArguments().add(list);
					}
				}
			} else if (canBeAngleOrPercentage(lu)) {
				getArguments().add(factory.createCSSPrimitiveValue(lu, true));
				finalLU = lu.getNextLexicalUnit();
			}
			if (finalLU != null) {
				if (finalLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
			if (canBeColor(lu)) {
				return true;
			} else {
				return lu2 != null && canBeColor(lu2) && canBeSizeOrPercentage(lu);
			}
		}

		private LexicalUnit processLinearColorStop(LexicalUnit lu, ValueFactory factory) {
			LexicalUnit finalLU = null;
			LexicalUnit lu2 = lu.getNextLexicalUnit();
			if (canBeColor(lu)) {
				PrimitiveValue color;
				try {
					color = factory.createCSSPrimitiveValue(lu, true);
				} catch (CSSLexicalProcessingException e) {
					LexicalSetter item = new LexicalValue().newLexicalSetter();
					item.setLexicalUnit(lu.shallowClone());
					color = item.getCSSValue();
				}
				if (lu2 != null && canBeSizeOrPercentage(lu2)) {
					ValueList list = ValueList.createWSValueList();
					list.add(color);
					list.add(factory.createCSSPrimitiveValue(lu2, true));
					getArguments().add(list);
					lu2 = lu2.getNextLexicalUnit();
					if (lu2 != null && canBeSizeOrPercentage(lu2)) {
						list.add(factory.createCSSPrimitiveValue(lu2, true));
						lu2 = lu2.getNextLexicalUnit();
					}
				} else {
					getArguments().add(color);
				}
				finalLU = lu2;
			} else if (lu2 != null && canBeColor(lu2) && canBeSizeOrPercentage(lu)) {
				ValueList list = ValueList.createWSValueList();
				PrimitiveValue color;
				try {
					color = factory.createCSSPrimitiveValue(lu2, true);
				} catch (CSSLexicalProcessingException e) {
					LexicalSetter item = new LexicalValue().newLexicalSetter();
					item.setLexicalUnit(lu2.shallowClone());
					color = item.getCSSValue();
				}
				list.add(color);
				list.add(factory.createCSSPrimitiveValue(lu, true));
				getArguments().add(list);
				finalLU = lu2.getNextLexicalUnit();
			} else if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
				finalLU = lu;
			} else {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad color stop");
			}
			return finalLU;
		}

		/**
		 * Test whether the value could represent a color.
		 * 
		 * @param lunit the lexical unit to test.
		 * @return true if the value could be a color.
		 */
		private boolean canBeColor(LexicalUnit lunit) {
			if (BaseCSSStyleDeclaration.testColor(lunit)) {
				return true;
			} else if (LexicalType.VAR == lunit.getLexicalUnitType()) {
				LexicalUnit lu = findCustomPropertyFallback(lunit);
				if (lu != null) {
					if (BaseCSSStyleDeclaration.testColor(lu)) {
						return true;
					}
				} else {
					throw new CSSLexicalProcessingException("var() without fallback found.");
				}
			}
			return false;
		}

		/**
		 * Test whether the value could represent a size or a percentage.
		 * 
		 * @param lunit the lexical unit to test.
		 * @return true if the value could be a size or a percentage.
		 */
		private boolean canBeSizeOrPercentage(LexicalUnit lunit) {
			return lunit.getLexicalUnitType() == LexicalType.PERCENTAGE
					|| ValueFactory.isSizeSACUnit(lunit)
					|| (LexicalType.VAR == lunit.getLexicalUnitType()
						&& isSizeOrPercentageVar(lunit));
		}

		private boolean isSizeOrPercentageVar(LexicalUnit lunit) {
			LexicalUnit lu = findCustomPropertyFallback(lunit);
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalType.PERCENTAGE || ValueFactory.isSizeSACUnit(lu)) {
					return true;
				}
			} else {
				throw new CSSLexicalProcessingException("var() without fallback found.");
			}
			return false;
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
					if (colorStopLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
				if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
					if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
					if (colorStopLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
			if (canBeColor(lu)) {
				return true;
			} else {
				return lu2 != null && canBeColor(lu2) && canBeAngleOrPercentage(lu);
			}
		}

		/**
		 * Test whether the value could represent an angle or a percentage.
		 * 
		 * @param lunit the lexical unit to test.
		 * @return true if the value could be an angle or a percentage.
		 */
		private boolean canBeAngleOrPercentage(LexicalUnit lunit) {
			return ValueFactory.isAngleSACUnit(lunit)
					|| lunit.getLexicalUnitType() == LexicalType.PERCENTAGE
					|| (LexicalType.VAR == lunit.getLexicalUnitType()
						&& isAngleOrPercentageVar(lunit));
		}

		private boolean isAngleOrPercentageVar(LexicalUnit lunit) {
			LexicalUnit lu = findCustomPropertyFallback(lunit);
			if (lu != null) {
				if (ValueFactory.isAngleSACUnit(lu) || lu.getLexicalUnitType() == LexicalType.PERCENTAGE) {
					return true;
				}
			} else {
				throw new CSSLexicalProcessingException("var() without fallback found.");
			}
			return false;
		}

		private LexicalUnit processAngularColorStop(LexicalUnit lu, ValueFactory factory) {
			// <angular-color-stop-list> = [ <angular-color-stop> [, <angular-color-hint>]? ]# , <angular-color-stop>
			// <angular-color-stop> = <color> && <color-stop-angle>?
			// <angular-color-hint> = <angle-percentage>
			LexicalUnit finalLU = null;
			LexicalUnit lu2 = lu.getNextLexicalUnit();
			if (canBeColor(lu)) {
				PrimitiveValue color;
				try {
					color = factory.createCSSPrimitiveValue(lu, true);
				} catch (CSSLexicalProcessingException e) {
					LexicalSetter item = new LexicalValue().newLexicalSetter();
					item.setLexicalUnit(lu.shallowClone());
					color = item.getCSSValue();
				}
				if (lu2 != null && canBeAngleOrPercentage(lu2)) {
					ValueList list = ValueList.createWSValueList();
					list.add(color);
					list.add(factory.createCSSPrimitiveValue(lu2, true));
					getArguments().add(list);
					lu2 = lu2.getNextLexicalUnit();
					if (lu2 != null && canBeAngleOrPercentage(lu2)) {
						list.add(factory.createCSSPrimitiveValue(lu2, true));
						lu2 = lu2.getNextLexicalUnit();
					}
				} else {
					getArguments().add(color);
				}
				finalLU = lu2;
			} else if (lu2 != null && canBeColor(lu2) && canBeAngleOrPercentage(lu)) {
				ValueList list = ValueList.createWSValueList();
				PrimitiveValue color;
				try {
					color = factory.createCSSPrimitiveValue(lu2, true);
				} catch (CSSLexicalProcessingException e) {
					LexicalSetter item = new LexicalValue().newLexicalSetter();
					item.setLexicalUnit(lu2.shallowClone());
					color = item.getCSSValue();
				}
				list.add(color);
				list.add(factory.createCSSPrimitiveValue(lu, true));
				getArguments().add(list);
				finalLU = lu2.getNextLexicalUnit();
			} else if (canBeAngleOrPercentage(lu)) {
				// Hint
				getArguments().add(factory.createCSSPrimitiveValue(lu, true));
				finalLU = lu2;
			} else if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
