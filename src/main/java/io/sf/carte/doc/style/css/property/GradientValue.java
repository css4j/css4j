/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSGradientValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

/**
 * Implementation of a gradient value.
 */
public class GradientValue extends FunctionValue implements CSSGradientValue {

	private static final long serialVersionUID = 1L;

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
	public Match matches(CSSValueSyntax syntax) {
		if (syntax != null) {
			if (syntax.getCategory() == Category.universal) {
				return Match.TRUE;
			}
			do {
				Match result;
				if ((result = matchesComponent(syntax)) != Match.FALSE) {
					return result;
				}
				syntax = syntax.getNext();
			} while (syntax != null);
		}
		return Match.FALSE;
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case image:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
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
				throw createDOMSyntaxException("Gradient without arguments");
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
				throw createDOMSyntaxException("Unknown gradient type");
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private DOMException createDOMSyntaxException(String message) {
			return new DOMException(DOMException.SYNTAX_ERR, message);
		}

		private void setLinearGradient(LexicalUnit lu, ValueFactory factory) {
			// [ [ <angle> | to <side-or-corner> ] ,]? <color-stop>[, <color-stop>]+
			// <side-or-corner> = [left | right] || [top | bottom]
			// <color-stop> = <color> [ <percentage> | <length> ]?
			// If the argument is to top, to right, to bottom, or to left, the angle of the gradient
			// line is 0deg, 90deg, 180deg, or 270deg, respectively.
			// If angle is omitted, it defaults to 'to bottom'.
			int colorStopCount = 0;
			LexicalUnit colorStopLU;
			if (isLinearColorStop(lu)) {
				// Omitted
				colorStopLU = lu;
			} else {
				colorStopLU = setAngleArguments(lu, factory);
			}
			if (colorStopLU == null) {
				throw createDOMSyntaxException(
					"Expected angle, side or color stop, found: " + lu.toString());
			}
			do {
				colorStopLU = processLinearColorStop(colorStopLU, factory);
				colorStopCount++;
				if (colorStopLU != null) {
					if (colorStopLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
						colorStopLU = colorStopLU.getNextLexicalUnit();
					} else {
						throw createDOMSyntaxException(
							"Expected color stops, found: " + lu.toString());
					}
				}
			} while (colorStopLU != null);
			if (colorStopCount < 2) {
				throw createDOMSyntaxException("Expected at least 2 color stops, found only one.");
			}
		}

		/*
		 * Returns null if there are no appropriate angle/to arguments, or the next unit
		 * is null (should not be).
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
					reportSyntaxWarning("Missing 'to' in side/corner specification in gradient: "
							+ lu.toString());
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
				} else {
					checkProxyValue(lu);
				}
			} else if (canBeAngleOrPercentage(lu)) {
				getArguments().add(factory.createCSSPrimitiveValue(lu, true));
				finalLU = lu.getNextLexicalUnit();
			} else {
				checkProxyValue(lu);
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
				// Do we have a <length-percentage> now?
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
			} else if (canBeSizeOrPercentage(lu)) {
				/*
				 * Could be a color hint or a color stop with inverted values
				 * (<length-percentage> && <color>
				 */
				if (lu2 != null && canBeColor(lu2)) {
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
				} else if (lu2 == null || lu2.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
					// color hint
					PrimitiveValue hint;
					try {
						hint = factory.createCSSPrimitiveValue(lu, true);
					} catch (CSSLexicalProcessingException e) {
						LexicalSetter item = new LexicalValue().newLexicalSetter();
						item.setLexicalUnit(lu.shallowClone());
						hint = item.getCSSValue();
					}
					getArguments().add(hint);
					finalLU = lu2;
				} else {
					throw createDOMSyntaxException("Bad color stop");
				}
			} else {
				throw createDOMSyntaxException("Bad color stop");
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
			return LexicalType.ATTR == lunit.getLexicalUnitType()
					&& (lunit = lunit.getParameters().getNextLexicalUnit()) != null
					&& lunit.getLexicalUnitType() == LexicalType.IDENT
					&& "color".equalsIgnoreCase(lunit.getStringValue());
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
							&& isSizeOrPercentageVar(lunit))
					|| (LexicalType.ATTR == lunit.getLexicalUnitType()
							&& lunit.shallowClone().matches(SyntaxParser
									.createSimpleSyntax("length-percentage")) != Match.FALSE);
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
			int colorStopCount = 0;
			LexicalUnit colorStopLU;
			if (!isLinearColorStop(lu)) {
				lu = processNonColorStop(lu, factory);
				if (lu != null && !isLinearColorStop(lu)) {
					lu = processNonColorStop(lu, factory);
				}
			}
			colorStopLU = lu;
			if (colorStopLU == null) {
				throw createDOMSyntaxException("Missing color stop");
			}
			do {
				colorStopLU = processLinearColorStop(colorStopLU, factory);
				colorStopCount++;
				if (colorStopLU != null) {
					if (colorStopLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
						colorStopLU = colorStopLU.getNextLexicalUnit();
					} else {
						throw createDOMSyntaxException("Expected color stops, found: " + lu.toString());
					}
				}
			} while (colorStopLU != null);
			if (colorStopCount < 2) {
				throw createDOMSyntaxException("Expected at least 2 color stops, found only one.");
			}
		}

		private LexicalUnit processNonColorStop(LexicalUnit lu, ValueFactory factory) {
			ValueList list = ValueList.createWSValueList();
			do {
				// 'lu' was checked to be non-null before calling.
				if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
					if (list.isEmpty()) {
						throw createDOMSyntaxException("Found empty argument: " + lu.toString());
					}
					lu = lu.getNextLexicalUnit();
					break;
				}
				list.add(factory.createCSSPrimitiveValue(lu, true));
				lu = lu.getNextLexicalUnit();
			} while (lu != null);
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
			int colorStopCount = 0;
			LexicalUnit colorStopLU;
			if (!isAngularColorStop(lu)) {
				ValueList list = ValueList.createWSValueList();
				do {
					// 'lu' was checked to be non-null before calling.
					if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
						if (list.isEmpty()) {
							throw createDOMSyntaxException("Found empty argument: " + lu.toString());
						}
						lu = lu.getNextLexicalUnit();
						break;
					}
					list.add(factory.createCSSPrimitiveValue(lu, true));
					lu = lu.getNextLexicalUnit();
				} while (lu != null);
				if (list.getLength() != 1) {
					getArguments().add(list);
				} else {
					getArguments().add(list.item(0));
				}
			}
			colorStopLU = lu;
			if (colorStopLU == null) {
				throw createDOMSyntaxException("Missing angle, position or color stop in gradient");
			}
			do {
				colorStopLU = processAngularColorStop(colorStopLU, factory);
				colorStopCount++;
				if (colorStopLU != null) {
					if (colorStopLU.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
						colorStopLU = colorStopLU.getNextLexicalUnit();
					} else {
						throw createDOMSyntaxException("Expected color stops, found: " + lu.toString());
					}
				}
			} while (colorStopLU != null);
			if (colorStopCount < 2) {
				throw createDOMSyntaxException("Expected at least 2 color stops, found only one.");
			}
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
			LexicalUnit luc;
			return ValueFactory.isAngleSACUnit(lunit)
					|| lunit.getLexicalUnitType() == LexicalType.PERCENTAGE
					|| (LexicalType.VAR == lunit.getLexicalUnitType()
							&& isAngleOrPercentageVar(lunit))
					|| (LexicalType.ATTR == lunit.getLexicalUnitType()
							&& ((luc = lunit.shallowClone()).matches(
									SyntaxParser.createSimpleSyntax("angle")) != Match.FALSE
									|| luc.matches(SyntaxParser
											.createSimpleSyntax("percentage")) != Match.FALSE));
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
			} else {
				throw createDOMSyntaxException("Bad angular color stop");
			}
			return finalLU;
		}

	}

	@Override
	public GradientValue clone() {
		return new GradientValue(this);
	}

}
