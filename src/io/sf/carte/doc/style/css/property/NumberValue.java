/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Number-specific CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class NumberValue extends PrimitiveValue {

	protected float realvalue = 0;

	private String dimensionUnitText = "";

	private boolean asInteger = false;

	boolean lengthUnitType = false;

	private boolean calculated = false;

	/**
	 * True if this number is in the same unit as was specified.
	 */
	private boolean specified = true;

	public NumberValue() {
		super();
	}

	protected NumberValue(NumberValue copied) {
		super(copied);
		this.realvalue = copied.realvalue;
		this.asInteger = copied.asInteger;
		this.calculated = copied.calculated;
		this.lengthUnitType = copied.lengthUnitType;
		this.dimensionUnitText = copied.dimensionUnitText;
	}

	@Override
	public String getCssText() {
		boolean notaNumber = getPrimitiveType() != CSSPrimitiveValue.CSS_NUMBER;
		if (realvalue == 0f && !notaNumber) {
			return "0";
		}
		if (Float.isInfinite(realvalue)) {
			return serializeInfinite();
		}
		double rintValue = Math.rint(realvalue);
		if (asInteger) {
			return Integer.toString((int) rintValue);
		} else if (realvalue == rintValue) {
			if (notaNumber) {
				return Integer.toString((int) rintValue) + dimensionUnitText;
			}
		}
		String s = serializeNumber(realvalue);
		StringBuilder buf = new StringBuilder(s.length() + dimensionUnitText.length());
		buf.append(s);
		buf.append(dimensionUnitText);
		return buf.toString();
	}

	private String serializeNumber(float real) {
		String s;
		if (specified) {
			s = Float.toString(real);
		} else {
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(fractionDigits(getPrimitiveType()));
			s = format.format(real);
		}
		return s;
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		writeCssText(wri, realvalue);
	}

	void writeCssText(SimpleWriter wri, float realvalue) throws IOException {
		boolean notaNumber = getPrimitiveType() != CSSPrimitiveValue.CSS_NUMBER;
		if (realvalue == 0f && !notaNumber) {
			wri.write('0');
			return;
		}
		if (Float.isInfinite(realvalue)) {
			writeInfinite(wri);
			return;
		}
		double rintValue = Math.rint(realvalue);
		if (asInteger) {
			wri.write(Integer.toString((int) rintValue));
			return;
		} else if (realvalue == rintValue) {
			if (notaNumber) {
				wri.write(Integer.toString((int) rintValue));
				wri.write(dimensionUnitText);
				return;
			}
		}
		String s = serializeNumber(realvalue);
		wri.write(s);
		wri.write(dimensionUnitText);
	}

	private void writeInfinite(SimpleWriter wri) throws IOException {
		if (realvalue > 0f) {
			wri.write("calc(1/0)");
		} else {
			wri.write("calc(-1/0)");
		}
	}

	public void serializeAbsolute(SimpleWriter wri) throws IOException {
		writeCssText(wri, Math.abs(realvalue));
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return getMinifiedCssText(propertyName, realvalue);
	}

	private String getMinifiedCssText(String propertyName, float realvalue) {
		boolean notaNumber = getPrimitiveType() != CSSPrimitiveValue.CSS_NUMBER;
		if (realvalue == 0f && notaNumber && getPrimitiveType() != CSSPrimitiveValue.CSS_PERCENTAGE
				&& isLengthUnitType()) {
			return "0";
		}
		if (Float.isInfinite(realvalue)) {
			return serializeInfinite();
		}
		double rintValue = Math.rint(realvalue);
		if (asInteger) {
			return Integer.toString((int) rintValue);
		} else if (realvalue == rintValue) {
			String s = Integer.toString((int) rintValue);
			if (notaNumber) {
				return s + dimensionUnitText;
			} else {
				return s;
			}
		}
		String s = serializeNumber(realvalue);
		int len = s.length();
		StringBuilder buf = new StringBuilder(len + dimensionUnitText.length());
		char c = s.charAt(0);
		if (c == '-' && s.charAt(1) == '0') {
			buf.append('-');
			buf.append(s.subSequence(2, len));
		} else if (c == '0' && len > 2) {
			buf.append(s.subSequence(1, len));
		} else {
			buf.append(s);
		}
		buf.append(dimensionUnitText);
		return buf.toString();
	}

	private String serializeInfinite() {
		if (realvalue > 0f) {
			return "calc(1/0)";
		} else {
			return "calc(-1/0)";
		}
	}

	public String minifyAbsolute(String propertyName) {
		return getMinifiedCssText(propertyName, Math.abs(realvalue));
	}

	private boolean isLengthUnitType() {
		return lengthUnitType ;
	}

	@Override
	public void setFloatValue(short unitType, float floatValue) throws DOMException {
		checkModifiableProperty();
		setCSSUnitType(unitType);
		realvalue = floatValue;
		dimensionUnitText = dimensionUnitString(unitType);
		if (unitType == CSS_NUMBER && ((float) Math.rint(floatValue)) == realvalue) {
			asInteger = true;
		} else {
			asInteger = false;
		}
		lengthUnitType = isLengthUnitType(getPrimitiveType());
	}

	public void setFloatValuePt(float floatValue) {
		setCSSUnitType(CSSPrimitiveValue.CSS_PT);
		realvalue = floatValue;
		dimensionUnitText = "pt";
		asInteger = false;
		lengthUnitType = true;
	}

	public void setIntegerValue(int intValue) {
		realvalue = intValue;
		setCSSUnitType(CSSPrimitiveValue.CSS_NUMBER);
		asInteger = true;
		lengthUnitType = false;
	}

	@Override
	public void setExpectInteger() {
		if (getPrimitiveType() != CSSPrimitiveValue.CSS_NUMBER || !asInteger) {
			super.setExpectInteger();
		}
	}

	void roundToInteger() throws DOMException {
		setExpectInteger();
		realvalue = Math.round(realvalue);
		asInteger = true;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Cannot use setCssText here");
	}

	@Override
	public boolean isCalculatedNumber() {
		return calculated ;
	}

	void setCalculatedNumber(boolean calculated) {
		this.calculated = calculated;
	}

	/**
	 * Set that the value that this number represents was originally specified as a
	 * calculation or as a relative unit, but comes from being either calculated or
	 * absolutized.
	 * <p>
	 * This has consequences as to how the number is serialized.
	 */
	public void setAbsolutizedUnit() {
		this.specified = false;
	}

	@Override
	public boolean isNegativeNumber() {
		return realvalue < 0f;
	}

	@Override
	public boolean isNumberZero() {
		return realvalue == 0f;
	}

	/**
	 * Gets a float value in a specified unit. If this CSS value doesn't contain
	 * a float value or can't be converted into the specified unit, a
	 * <code>DOMException</code> is raised.
	 * 
	 * @param unitType
	 *            A unit code to get the float value. The unit code can only be
	 *            a float unit type (i.e. <code>CSS_NUMBER</code>,
	 *            <code>CSS_PERCENTAGE</code>, <code>CSS_EMS</code>,
	 *            <code>CSS_EXS</code>, <code>CSS_PX</code>,
	 *            <code>CSS_CM</code>, <code>CSS_MM</code>, <code>CSS_IN</code>,
	 *            <code>CSS_PT</code>, <code>CSS_PC</code>,
	 *            <code>CSS_DEG</code>, <code>CSS_RAD</code>,
	 *            <code>CSS_GRAD</code>, <code>CSS_MS</code>,
	 *            <code>CSS_S</code>, <code>CSS_HZ</code>, <code>CSS_KHZ</code>,
	 *            <code>CSS_DIMENSION</code>).
	 * @return The float value in the specified unit.
	 * @throws DOMException
	 *             INVALID_ACCESS_ERR if the CSS value can't be converted into
	 *             the specified unit.
	 */
	@Override
	public float getFloatValue(short unitType) throws DOMException {
		if (unitType == getPrimitiveType()) {
			return realvalue;
		} else if (unitType == CSSPrimitiveValue.CSS_NUMBER && getPrimitiveType() != CSSPrimitiveValue.CSS_PERCENTAGE) {
			return realvalue;
		} else {
			return floatValueConversion(realvalue, getPrimitiveType(), unitType);
		}
	}

	/**
	 * Converts a float value, expressed in <code>declType</code> units, to
	 * <code>unitType</code> units.
	 * 
	 * @param fvalue
	 *            the float value to convert.
	 * @param declType
	 *            the declared type of the value.
	 * @param unitType
	 *            the desired unit type.
	 * @return the value converted to the <code>unitType</code> unit.
	 * @throws DOMException
	 *             if the unit conversion could not be done.
	 */
	public static float floatValueConversion(float fvalue, short declType, short unitType) throws DOMException {
		if (fvalue == 0f) {
			return 0;
		}
		if (declType == unitType) {
			return fvalue;
		}
		switch (declType) {
		case CSSPrimitiveValue.CSS_PT:
			if (unitType == CSSPrimitiveValue.CSS_PX) {
				// 1px = 0.75pt
				return fvalue / 0.75f;
			} else if (unitType == CSSPrimitiveValue.CSS_IN) {
				// 1in = 72pt
				return fvalue / 72f;
			} else if (unitType == CSSPrimitiveValue.CSS_PC) {
				// 1pc = 12pt
				return fvalue / 12f;
			} else if (unitType == CSSPrimitiveValue.CSS_CM) {
				// 1cm = 28.34646pt
				return fvalue / 28.3464567f;
			} else if (unitType == CSSPrimitiveValue.CSS_MM) {
				// 1mm = 2.834646pt
				return fvalue / 2.83464567f;
			} else if (unitType == CSSPrimitiveValue2.CSS_QUARTER_MM) {
				return fvalue * 1.4111111f;
			}
			break;
		case CSSPrimitiveValue.CSS_PX:
			if (unitType == CSSPrimitiveValue.CSS_PT) {
				// 1px = 0.75pt
				return fvalue * 0.75f;
			} else if (unitType == CSSPrimitiveValue.CSS_IN) {
				// 1in = 96px
				return fvalue / 96f;
			} else if (unitType == CSSPrimitiveValue.CSS_PC) {
				// 1pc = 16px
				return fvalue / 16f;
			} else if (unitType == CSSPrimitiveValue.CSS_CM) {
				// 1cm = 37.795px
				return fvalue / 37.7952756f;
			} else if (unitType == CSSPrimitiveValue.CSS_MM) {
				// 1mm = 3.7795px
				return fvalue / 3.77952756f;
			} else if (unitType == CSSPrimitiveValue2.CSS_QUARTER_MM) {
				return fvalue * 1.05833333f;
			}
			break;
		case CSSPrimitiveValue.CSS_PC:
			if (unitType == CSSPrimitiveValue.CSS_PT) {
				// 1pc = 12pt
				return fvalue * 12f;
			} else if (unitType == CSSPrimitiveValue.CSS_IN) {
				// 1in = 6pc
				return fvalue / 6f;
			} else if (unitType == CSSPrimitiveValue.CSS_PX) {
				// 1pc = 16px
				return fvalue * 16f;
			} else if (unitType == CSSPrimitiveValue.CSS_CM) {
				// 1cm = 2.3622047pc
				return fvalue * 0.42333333f;
			} else if (unitType == CSSPrimitiveValue.CSS_MM) {
				// 4.233mm = 1pc
				return fvalue * 4.2333333f;
			} else if (unitType == CSSPrimitiveValue2.CSS_QUARTER_MM) {
				return fvalue * 16.9333333f;
			}
			break;
		case CSSPrimitiveValue.CSS_IN:
			if (unitType == CSSPrimitiveValue.CSS_PX) {
				// 1in = 96px
				return fvalue * 96f;
			} else if (unitType == CSSPrimitiveValue.CSS_PT) {
				// 1in = 72pt
				return fvalue * 72f;
			} else if (unitType == CSSPrimitiveValue.CSS_PC) {
				// 1in = 6pc
				return fvalue * 6f;
			} else if (unitType == CSSPrimitiveValue.CSS_CM) {
				// 2.54cm = 1in
				return fvalue * 2.54f;
			} else if (unitType == CSSPrimitiveValue.CSS_MM) {
				// 25.4mm = 1in
				return fvalue * 25.4f;
			} else if (unitType == CSSPrimitiveValue2.CSS_QUARTER_MM) {
				return fvalue * 101.6f;
			}
			break;
		case CSSPrimitiveValue.CSS_CM:
			if (unitType == CSSPrimitiveValue.CSS_PT) {
				// 1cm = 28.34646pt
				return fvalue * 28.3464567f;
			} else if (unitType == CSSPrimitiveValue.CSS_IN) {
				// 1in = 2.54cm
				return fvalue / 2.54f;
			} else if (unitType == CSSPrimitiveValue.CSS_PX) {
				// 1cm = 37.795px
				return fvalue * 37.7952756f;
			} else if (unitType == CSSPrimitiveValue.CSS_PC) {
				// 1cm = 2.3622pc
				return fvalue * 2.3622047f;
			} else if (unitType == CSSPrimitiveValue.CSS_MM) {
				// 1cm = 10mm
				return fvalue * 10f;
			} else if (unitType == CSSPrimitiveValue2.CSS_QUARTER_MM) {
				return fvalue * 40f;
			}
			break;
		case CSSPrimitiveValue.CSS_MM:
			if (unitType == CSSPrimitiveValue.CSS_PT) {
				// 1mm = 2.834646pt
				return fvalue * 2.83464567f;
			} else if (unitType == CSSPrimitiveValue.CSS_IN) {
				// 1in = 25.4mm
				return fvalue / 25.4f;
			} else if (unitType == CSSPrimitiveValue.CSS_PX) {
				// 1mm = 3.77952756px
				return fvalue * 3.77952756f;
			} else if (unitType == CSSPrimitiveValue.CSS_PC) {
				// 1mm = 0.23622pc
				return fvalue * 0.23622047f;
			} else if (unitType == CSSPrimitiveValue.CSS_CM) {
				// 1cm = 10mm
				return fvalue * 0.1f;
			} else if (unitType == CSSPrimitiveValue2.CSS_QUARTER_MM) {
				return fvalue * 4f;
			}
			break;
		case CSSPrimitiveValue2.CSS_QUARTER_MM:
			if (unitType == CSSPrimitiveValue.CSS_PT) {
				return fvalue * 0.7086614f;
			} else if (unitType == CSSPrimitiveValue.CSS_IN) {
				return fvalue / 101.6f;
			} else if (unitType == CSSPrimitiveValue.CSS_PX) {
				return fvalue * 0.9448819f;
			} else if (unitType == CSSPrimitiveValue.CSS_PC) {
				return fvalue / 16.933333f;
			} else if (unitType == CSSPrimitiveValue.CSS_MM) {
				return fvalue * 0.25f;
			} else if (unitType == CSSPrimitiveValue.CSS_CM) {
				// 1cm = 40q
				return fvalue * 0.025f;
			}
			break;
		case CSSPrimitiveValue.CSS_MS:
			if (unitType == CSSPrimitiveValue.CSS_S) {
				return fvalue * 0.001f;
			}
			break;
		case CSSPrimitiveValue.CSS_S:
			if (unitType == CSSPrimitiveValue.CSS_MS) {
				return fvalue * 1000f;
			}
			break;
		case CSSPrimitiveValue2.CSS_TURN:
			if (unitType == CSSPrimitiveValue.CSS_RAD) {
				return fvalue * 2f * (float) Math.PI;
			} else if (unitType == CSSPrimitiveValue.CSS_GRAD) {
				return fvalue * 400f;
			} else if (unitType == CSSPrimitiveValue.CSS_DEG) {
				return fvalue * 360f;
			}
			break;
		case CSSPrimitiveValue.CSS_RAD:
			if (unitType == CSSPrimitiveValue.CSS_DEG) {
				return (float) Math.toDegrees(fvalue);
			} else if (unitType == CSSPrimitiveValue.CSS_GRAD) {
				return fvalue * 63.6619772368f;
			} else if (unitType == CSSPrimitiveValue2.CSS_TURN) {
				return fvalue * 0.159154943092f;
			}
			break;
		case CSSPrimitiveValue.CSS_DEG:
			if (unitType == CSSPrimitiveValue.CSS_RAD) {
				return (float) Math.toRadians(fvalue);
			} else if (unitType == CSSPrimitiveValue.CSS_GRAD) {
				return fvalue * 1.1111111111f;
			} else if (unitType == CSSPrimitiveValue2.CSS_TURN) {
				return fvalue / 360f;
			}
			break;
		case CSSPrimitiveValue.CSS_GRAD:
			if (unitType == CSSPrimitiveValue.CSS_DEG) {
				return fvalue * 0.9f;
			} else if (unitType == CSSPrimitiveValue.CSS_RAD) {
				return fvalue * 0.015707963268f;
			} else if (unitType == CSSPrimitiveValue2.CSS_TURN) {
				return fvalue * 0.0025f;
			}
			break;
		case CSSPrimitiveValue.CSS_NUMBER:
			if (unitType == CSSPrimitiveValue.CSS_DEG) { // Assume degrees
				return fvalue;
			}
		case CSSPrimitiveValue.CSS_HZ:
			if (unitType == CSSPrimitiveValue.CSS_KHZ) {
				return fvalue * 0.001f;
			}
			break;
		case CSSPrimitiveValue.CSS_KHZ:
			if (unitType == CSSPrimitiveValue.CSS_HZ) {
				return fvalue * 1000f;
			}
			break;
		default:
			if (CSSPrimitiveValue.CSS_DIMENSION == unitType) {
				// Unknown dimension, return as is.
				return fvalue;
			}
		}
		String unit = dimensionUnitString(declType);
		if (unit.length() == 0) {
			unit = Integer.toString(declType);
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Cannot transform unit " + unit + " to " + dimensionUnitString(unitType));
	}

	/**
	 * Gives the text representation of the dimension unit, if this value
	 * represents a dimension.
	 * 
	 * @return the text representation of the dimension unit, or the empty
	 *         string if this value does not represent a dimension.
	 */
	public String getDimensionUnitText() {
		return dimensionUnitText;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			super.setLexicalUnit(lunit);
			nextLexicalUnit = lunit.getNextLexicalUnit();
			switch (lunit.getLexicalUnitType()) {
			case LexicalUnit.SAC_INTEGER:
				realvalue = lunit.getIntegerValue();
				asInteger = true;
				break;
			default:
				realvalue = lunit.getFloatValue();
				asInteger = false;
				dimensionUnitText = lunit.getDimensionUnitText();
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getCssValueType();
		short pType;
		if (realvalue != 0f) {
			pType = getPrimitiveType();
		} else {
			pType = CSSPrimitiveValue.CSS_NUMBER;
		}
		result = prime * result + pType;
		result = prime * result + Float.floatToIntBits(realvalue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NumberValue)) {
			return false;
		}
		NumberValue other = (NumberValue) obj;
		if (Float.floatToIntBits(realvalue) != Float.floatToIntBits(other.realvalue)) {
			return false;
		}
		if (getPrimitiveType() != other.getPrimitiveType() && realvalue != 0f) {
			return false;
		}
		return true;
	}

	@Override
	public NumberValue clone() {
		return new NumberValue(this);
	}

	NumberValue immutable() {
		return new ImmutableCSSNumberValue(this);
	}

	private static class ImmutableCSSNumberValue extends NumberValue {

		ImmutableCSSNumberValue(NumberValue value) {
			super(value);
		}

		@Override
		public void setFloatValue(short unitType, float floatValue) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This value is immutable");
		}

		@Override
		public void setFloatValuePt(float floatValue) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This value is immutable");
		}

		@Override
		public void setIntegerValue(int intValue) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This value is immutable");
		}

	}

	/**
	 * Gives the dimension unit String associated to the given CSS unit type.
	 * 
	 * @param unitType
	 *            the CSS primitive unit type.
	 * @return the unit String.
	 */
	static String dimensionUnitString(short unitType) {
		switch (unitType) {
		case CSSPrimitiveValue.CSS_EMS:
			return "em";
		case CSSPrimitiveValue.CSS_EXS:
			return "ex";
		case CSSPrimitiveValue.CSS_PX:
			return "px";
		case CSSPrimitiveValue.CSS_IN:
			return "in";
		case CSSPrimitiveValue.CSS_CM:
			return "cm";
		case CSSPrimitiveValue.CSS_MM:
			return "mm";
		case CSSPrimitiveValue.CSS_PT:
			return "pt";
		case CSSPrimitiveValue.CSS_PC:
			return "pc";
		case CSSPrimitiveValue.CSS_PERCENTAGE:
			return "%";
		case CSSPrimitiveValue.CSS_DEG:
			return "deg";
		case CSSPrimitiveValue.CSS_GRAD:
			return "grad";
		case CSSPrimitiveValue.CSS_RAD:
			return "rad";
		case CSSPrimitiveValue.CSS_MS:
			return "ms";
		case CSSPrimitiveValue.CSS_S:
			return "s";
		case CSSPrimitiveValue.CSS_HZ:
			return "Hz";
		case CSSPrimitiveValue.CSS_KHZ:
			return "kHz";
		case CSSPrimitiveValue2.CSS_CAP:
			return "cap";
		case CSSPrimitiveValue2.CSS_CH:
			return "ch";
		case CSSPrimitiveValue2.CSS_FR:
			return "fr";
		case CSSPrimitiveValue2.CSS_IC:
			return "ic";
		case CSSPrimitiveValue2.CSS_LH:
			return "lh";
		case CSSPrimitiveValue2.CSS_QUARTER_MM:
			return "Q";
		case CSSPrimitiveValue2.CSS_REM:
			return "rem";
		case CSSPrimitiveValue2.CSS_RLH:
			return "rlh";
		case CSSPrimitiveValue2.CSS_VB:
			return "vb";
		case CSSPrimitiveValue2.CSS_VH:
			return "vh";
		case CSSPrimitiveValue2.CSS_VI:
			return "vi";
		case CSSPrimitiveValue2.CSS_VMAX:
			return "vmax";
		case CSSPrimitiveValue2.CSS_VMIN:
			return "vmin";
		case CSSPrimitiveValue2.CSS_VW:
			return "vw";
		case CSSPrimitiveValue2.CSS_DPI:
			return "dpi";
		case CSSPrimitiveValue2.CSS_DPCM:
			return "dpcm";
		case CSSPrimitiveValue2.CSS_DPPX:
			return "dppx";
		case CSSPrimitiveValue.CSS_DIMENSION:
		default:
			return "";
		}
	}

	private static int fractionDigits(short primitiveType) {
		switch (primitiveType) {
		case CSSPrimitiveValue.CSS_EMS:
		case CSSPrimitiveValue.CSS_EXS:
		case CSSPrimitiveValue.CSS_IN:
		case CSSPrimitiveValue.CSS_MM:
		case CSSPrimitiveValue.CSS_PC:
		case CSSPrimitiveValue.CSS_PX:
		case CSSPrimitiveValue.CSS_PT:
		case CSSPrimitiveValue2.CSS_CAP:
		case CSSPrimitiveValue2.CSS_CH:
		case CSSPrimitiveValue2.CSS_IC:
		case CSSPrimitiveValue2.CSS_LH:
		case CSSPrimitiveValue2.CSS_QUARTER_MM:
		case CSSPrimitiveValue2.CSS_REM:
		case CSSPrimitiveValue2.CSS_RLH:
		case CSSPrimitiveValue.CSS_MS:
		case CSSPrimitiveValue.CSS_DEG:
		case CSSPrimitiveValue.CSS_GRAD:
			return 2;
		case CSSPrimitiveValue.CSS_KHZ:
		case CSSPrimitiveValue.CSS_CM:
		case CSSPrimitiveValue2.CSS_TURN:
			return 4;
		}
		return 3;
	}

	public static boolean isLengthUnitType(short primitiveType) {
		switch (primitiveType) {
		case CSSPrimitiveValue.CSS_CM:
		case CSSPrimitiveValue.CSS_EMS:
		case CSSPrimitiveValue.CSS_EXS:
		case CSSPrimitiveValue.CSS_IN:
		case CSSPrimitiveValue.CSS_MM:
		case CSSPrimitiveValue.CSS_PC:
		case CSSPrimitiveValue.CSS_PX:
		case CSSPrimitiveValue.CSS_PT:
		case CSSPrimitiveValue2.CSS_CAP:
		case CSSPrimitiveValue2.CSS_CH:
		case CSSPrimitiveValue2.CSS_IC:
		case CSSPrimitiveValue2.CSS_LH:
		case CSSPrimitiveValue2.CSS_QUARTER_MM:
		case CSSPrimitiveValue2.CSS_REM:
		case CSSPrimitiveValue2.CSS_RLH:
		case CSSPrimitiveValue2.CSS_VB:
		case CSSPrimitiveValue2.CSS_VH:
		case CSSPrimitiveValue2.CSS_VI:
		case CSSPrimitiveValue2.CSS_VMAX:
		case CSSPrimitiveValue2.CSS_VMIN:
		case CSSPrimitiveValue2.CSS_VW:
			return true;
		}
		return false;
	}

	public static boolean isAngleUnitType(short primitiveType) {
		switch (primitiveType) {
		case CSSPrimitiveValue.CSS_DEG:
		case CSSPrimitiveValue.CSS_RAD:
		case CSSPrimitiveValue.CSS_GRAD:
		case CSSPrimitiveValue2.CSS_TURN:
			return true;
		}
		return false;
	}

	public static NumberValue createCSSNumberValue(short unit, float floatValue) {
		NumberValue num = new NumberValue();
		num.setFloatValue(unit, floatValue);
		return num;
	}

}
