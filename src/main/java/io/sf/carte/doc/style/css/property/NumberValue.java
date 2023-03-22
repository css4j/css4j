/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Number-specific value.
 * 
 * @author Carlos Amengual
 *
 */
public class NumberValue extends TypedValue {

	private static final long serialVersionUID = 1L;

	private short unitType;

	protected float realvalue = 0;

	private String dimensionUnitText = "";

	private boolean asInteger = false;

	boolean lengthUnitType = false;

	private boolean calculated = false;

	/**
	 * True if this number is in the same unit as was specified.
	 */
	private boolean specified = true;

	private int maxFractionDigits = -1; // -1 means auto

	public NumberValue() {
		super(Type.NUMERIC);
		this.unitType = CSSUnit.CSS_NUMBER;
	}

	protected NumberValue(NumberValue copied) {
		super(copied);
		this.unitType = copied.unitType;
		this.realvalue = copied.realvalue;
		this.asInteger = copied.asInteger;
		this.calculated = copied.calculated;
		this.specified = copied.specified;
		this.maxFractionDigits = copied.maxFractionDigits;
		this.lengthUnitType = copied.lengthUnitType;
		this.dimensionUnitText = copied.dimensionUnitText;
		this.maxFractionDigits = copied.maxFractionDigits;
	}

	@Override
	public short getUnitType() {
		return unitType;
	}

	void setUnitType(short unitType) {
		this.unitType = unitType;
	}

	@Override
	public String getCssText() {
		boolean notaNumber = unitType != CSSUnit.CSS_NUMBER;
		if (realvalue == 0f && !notaNumber) {
			return "0";
		}
		if (Float.isInfinite(realvalue)) {
			return serializeInfinite();
		}
		double rintValue = Math.rint(realvalue);
		if (asInteger) {
			return Integer.toString((int) rintValue);
		} else if (realvalue == rintValue && notaNumber) {
			return (int) rintValue + dimensionUnitText;
		}
		String s = serializeNumber(realvalue);
		StringBuilder buf = new StringBuilder(s.length() + dimensionUnitText.length());
		buf.append(s);
		buf.append(dimensionUnitText);
		return buf.toString();
	}

	private String serializeNumber(float real) {
		String s;
		if (specified && !calculated) {
			s = Float.toString(real);
		} else {
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumFractionDigits(0);
			int fdigits = maxFractionDigits;
			if (fdigits < 0) {
				fdigits = fractionDigits(getUnitType());
			}
			format.setMaximumFractionDigits(fdigits);
			s = format.format(real);
		}
		return s;
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		writeCssText(wri, realvalue);
	}

	void writeCssText(SimpleWriter wri, float realvalue) throws IOException {
		boolean notaNumber = getUnitType() != CSSUnit.CSS_NUMBER;
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
		} else if (realvalue == rintValue && notaNumber) {
			wri.write(Integer.toString((int) rintValue));
			wri.write(dimensionUnitText);
			return;
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
		if (realvalue == 0f && getUnitType() != CSSUnit.CSS_PERCENTAGE && isLengthUnitType()
			&& !isSubproperty() && isSpecified()) {
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
			boolean notaNumber = getUnitType() != CSSUnit.CSS_NUMBER;
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
		setUnitType(unitType);
		realvalue = floatValue;
		dimensionUnitText = CSSUnit.dimensionUnitString(unitType);
		asInteger = unitType == CSSUnit.CSS_NUMBER && ((float) Math.rint(floatValue)) == realvalue;
		lengthUnitType = CSSUnit.isLengthUnitType(unitType);
	}

	public void setFloatValuePt(float floatValue) {
		setUnitType(CSSUnit.CSS_PT);
		realvalue = floatValue;
		dimensionUnitText = "pt";
		asInteger = false;
		lengthUnitType = true;
	}

	public void setIntegerValue(int intValue) {
		realvalue = intValue;
		setUnitType(CSSUnit.CSS_NUMBER);
		asInteger = true;
		lengthUnitType = false;
	}

	@Override
	public void setExpectInteger() {
		if (getUnitType() != CSSUnit.CSS_NUMBER) {
			super.setExpectInteger();
		} else if (calculated) {
			asInteger = true;
			realvalue = Math.round(realvalue);
		} else if (!asInteger) {
			super.setExpectInteger();
		}
	}

	void roundToInteger() throws DOMException {
		asInteger = true;
		setExpectInteger();
		realvalue = Math.round(realvalue);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Cannot use setCssText here");
	}

	@Override
	public boolean isCalculatedNumber() {
		return calculated ;
	}

	/**
	 * Sets whether this number is the result of a calculation.
	 * 
	 * @param calculated {@code true} if this number was calculated.
	 */
	void setCalculatedNumber(boolean calculated) {
		this.calculated = calculated;
		this.specified = this.specified && !calculated;
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

	void setSpecified(boolean specified) {
		this.specified = specified;
	}

	boolean isSpecified() {
		return specified;
	}

	void setMaxFractionDigits(int maxFractionDigits) {
		this.maxFractionDigits = maxFractionDigits;
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
	 * Gets a float value in a specified unit.
	 * 
	 * @param unitType A unit code to get the float value, like
	 *                 <code>CSS_NUMBER</code>, <code>CSS_PERCENTAGE</code>,
	 *                 <code>CSS_PT</code> or <code>CSS_EX</code>. If the type is
	 *                 <code>CSS_OTHER</code>, the value shall be returned as is,
	 *                 regardless of the unit that was set with.
	 * @return The float value in the specified unit.
	 * @throws DOMException INVALID_ACCESS_ERR if the CSS value can't be converted
	 *                      into the specified unit.
	 */
	@Override
	public float getFloatValue(short unitType) throws DOMException {
		if (unitType == getUnitType()) {
			return realvalue;
		} else if (unitType == CSSUnit.CSS_NUMBER && getUnitType() != CSSUnit.CSS_PERCENTAGE) {
			return realvalue;
		} else {
			return floatValueConversion(realvalue, getUnitType(), unitType);
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
		case CSSUnit.CSS_PT:
			if (unitType == CSSUnit.CSS_PX) {
				// 1px = 0.75pt
				return fvalue / 0.75f;
			} else if (unitType == CSSUnit.CSS_IN) {
				// 1in = 72pt
				return fvalue / 72f;
			} else if (unitType == CSSUnit.CSS_PC) {
				// 1pc = 12pt
				return fvalue / 12f;
			} else if (unitType == CSSUnit.CSS_CM) {
				// 1cm = 28.34646pt
				return fvalue / 28.3464567f;
			} else if (unitType == CSSUnit.CSS_MM) {
				// 1mm = 2.834646pt
				return fvalue / 2.83464567f;
			} else if (unitType == CSSUnit.CSS_QUARTER_MM) {
				return fvalue * 1.4111111f;
			}
			break;
		case CSSUnit.CSS_PX:
			if (unitType == CSSUnit.CSS_PT) {
				// 1px = 0.75pt
				return fvalue * 0.75f;
			} else if (unitType == CSSUnit.CSS_IN) {
				// 1in = 96px
				return fvalue / 96f;
			} else if (unitType == CSSUnit.CSS_PC) {
				// 1pc = 16px
				return fvalue / 16f;
			} else if (unitType == CSSUnit.CSS_CM) {
				// 1cm = 37.795px
				return fvalue / 37.7952756f;
			} else if (unitType == CSSUnit.CSS_MM) {
				// 1mm = 3.7795px
				return fvalue / 3.77952756f;
			} else if (unitType == CSSUnit.CSS_QUARTER_MM) {
				return fvalue * 1.05833333f;
			}
			break;
		case CSSUnit.CSS_PC:
			if (unitType == CSSUnit.CSS_PT) {
				// 1pc = 12pt
				return fvalue * 12f;
			} else if (unitType == CSSUnit.CSS_IN) {
				// 1in = 6pc
				return fvalue / 6f;
			} else if (unitType == CSSUnit.CSS_PX) {
				// 1pc = 16px
				return fvalue * 16f;
			} else if (unitType == CSSUnit.CSS_CM) {
				// 1cm = 2.3622047pc
				return fvalue * 0.42333333f;
			} else if (unitType == CSSUnit.CSS_MM) {
				// 4.233mm = 1pc
				return fvalue * 4.2333333f;
			} else if (unitType == CSSUnit.CSS_QUARTER_MM) {
				return fvalue * 16.9333333f;
			}
			break;
		case CSSUnit.CSS_IN:
			if (unitType == CSSUnit.CSS_PX) {
				// 1in = 96px
				return fvalue * 96f;
			} else if (unitType == CSSUnit.CSS_PT) {
				// 1in = 72pt
				return fvalue * 72f;
			} else if (unitType == CSSUnit.CSS_PC) {
				// 1in = 6pc
				return fvalue * 6f;
			} else if (unitType == CSSUnit.CSS_CM) {
				// 2.54cm = 1in
				return fvalue * 2.54f;
			} else if (unitType == CSSUnit.CSS_MM) {
				// 25.4mm = 1in
				return fvalue * 25.4f;
			} else if (unitType == CSSUnit.CSS_QUARTER_MM) {
				return fvalue * 101.6f;
			}
			break;
		case CSSUnit.CSS_CM:
			if (unitType == CSSUnit.CSS_PT) {
				// 1cm = 28.34646pt
				return fvalue * 28.3464567f;
			} else if (unitType == CSSUnit.CSS_IN) {
				// 1in = 2.54cm
				return fvalue / 2.54f;
			} else if (unitType == CSSUnit.CSS_PX) {
				// 1cm = 37.795px
				return fvalue * 37.7952756f;
			} else if (unitType == CSSUnit.CSS_PC) {
				// 1cm = 2.3622pc
				return fvalue * 2.3622047f;
			} else if (unitType == CSSUnit.CSS_MM) {
				// 1cm = 10mm
				return fvalue * 10f;
			} else if (unitType == CSSUnit.CSS_QUARTER_MM) {
				return fvalue * 40f;
			}
			break;
		case CSSUnit.CSS_MM:
			if (unitType == CSSUnit.CSS_PT) {
				// 1mm = 2.834646pt
				return fvalue * 2.83464567f;
			} else if (unitType == CSSUnit.CSS_IN) {
				// 1in = 25.4mm
				return fvalue / 25.4f;
			} else if (unitType == CSSUnit.CSS_PX) {
				// 1mm = 3.77952756px
				return fvalue * 3.77952756f;
			} else if (unitType == CSSUnit.CSS_PC) {
				// 1mm = 0.23622pc
				return fvalue * 0.23622047f;
			} else if (unitType == CSSUnit.CSS_CM) {
				// 1cm = 10mm
				return fvalue * 0.1f;
			} else if (unitType == CSSUnit.CSS_QUARTER_MM) {
				return fvalue * 4f;
			}
			break;
		case CSSUnit.CSS_QUARTER_MM:
			if (unitType == CSSUnit.CSS_PT) {
				return fvalue * 0.7086614f;
			} else if (unitType == CSSUnit.CSS_IN) {
				return fvalue / 101.6f;
			} else if (unitType == CSSUnit.CSS_PX) {
				return fvalue * 0.9448819f;
			} else if (unitType == CSSUnit.CSS_PC) {
				return fvalue / 16.933333f;
			} else if (unitType == CSSUnit.CSS_MM) {
				return fvalue * 0.25f;
			} else if (unitType == CSSUnit.CSS_CM) {
				// 1cm = 40q
				return fvalue * 0.025f;
			}
			break;
		case CSSUnit.CSS_MS:
			if (unitType == CSSUnit.CSS_S) {
				return fvalue * 0.001f;
			}
			break;
		case CSSUnit.CSS_S:
			if (unitType == CSSUnit.CSS_MS) {
				return fvalue * 1000f;
			}
			break;
		case CSSUnit.CSS_TURN:
			if (unitType == CSSUnit.CSS_RAD) {
				return fvalue * 2f * (float) Math.PI;
			} else if (unitType == CSSUnit.CSS_GRAD) {
				return fvalue * 400f;
			} else if (unitType == CSSUnit.CSS_DEG) {
				return fvalue * 360f;
			}
			break;
		case CSSUnit.CSS_RAD:
			if (unitType == CSSUnit.CSS_DEG) {
				return (float) Math.toDegrees(fvalue);
			} else if (unitType == CSSUnit.CSS_GRAD) {
				return fvalue * 63.6619772368f;
			} else if (unitType == CSSUnit.CSS_TURN) {
				return fvalue * 0.159154943092f;
			}
			break;
		case CSSUnit.CSS_DEG:
			if (unitType == CSSUnit.CSS_RAD) {
				return (float) Math.toRadians(fvalue);
			} else if (unitType == CSSUnit.CSS_GRAD) {
				return fvalue * 1.1111111111f;
			} else if (unitType == CSSUnit.CSS_TURN) {
				return fvalue / 360f;
			}
			break;
		case CSSUnit.CSS_GRAD:
			if (unitType == CSSUnit.CSS_DEG) {
				return fvalue * 0.9f;
			} else if (unitType == CSSUnit.CSS_RAD) {
				return fvalue * 0.015707963268f;
			} else if (unitType == CSSUnit.CSS_TURN) {
				return fvalue * 0.0025f;
			}
			break;
		case CSSUnit.CSS_NUMBER:
			if (unitType == CSSUnit.CSS_DEG) { // Assume degrees
				return fvalue;
			}
		case CSSUnit.CSS_HZ:
			if (unitType == CSSUnit.CSS_KHZ) {
				return fvalue * 0.001f;
			}
			break;
		case CSSUnit.CSS_KHZ:
			if (unitType == CSSUnit.CSS_HZ) {
				return fvalue * 1000f;
			}
			break;
		case CSSUnit.CSS_DPI:
			if (unitType == CSSUnit.CSS_DPCM) {
				// 2.54cm = 1in
				return fvalue / 2.54f;
			} else if (unitType == CSSUnit.CSS_DPPX) {
				// 1in = 96px
				return fvalue / 96f;
			}
			break;
		case CSSUnit.CSS_DPCM:
			if (unitType == CSSUnit.CSS_DPI) {
				// 2.54cm = 1in
				return fvalue * 2.54f;
			} else if (unitType == CSSUnit.CSS_DPPX) {
				// 1cm = 37.795276px
				return fvalue / 37.7952756f;
			}
			break;
		case CSSUnit.CSS_DPPX:
			if (unitType == CSSUnit.CSS_DPI) {
				// 1in = 96px
				return fvalue * 96f;
			} else if (unitType == CSSUnit.CSS_DPCM) {
				// 1cm = 37.795276px
				return fvalue * 37.7952756f;
			}
			break;
		default:
			if (CSSUnit.CSS_OTHER == unitType) {
				// Unknown dimension, return as is.
				return fvalue;
			}
		}
		String unit = CSSUnit.dimensionUnitString(declType);
		if (unit.length() == 0) {
			unit = '<' + Integer.toString(declType) + '>';
		}
		String requestedUnitStr;
		try {
			requestedUnitStr = CSSUnit.dimensionUnitString(unitType);
			if (requestedUnitStr.length() == 0) {
				requestedUnitStr ="<number>";
			}
		} catch (DOMException e) {
			requestedUnitStr = '<' + Integer.toString(unitType) + '>';
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Cannot transform unit " + unit + " to " + requestedUnitStr);
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
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case length:
			return isLengthCompatible() ? Match.TRUE : Match.FALSE;
		case lengthPercentage:
			return (isLengthCompatible() || getUnitType() == CSSUnit.CSS_PERCENTAGE) ? Match.TRUE : Match.FALSE;
		case percentage:
			return getUnitType() == CSSUnit.CSS_PERCENTAGE ? Match.TRUE : Match.FALSE;
		case number:
			return getUnitType() == CSSUnit.CSS_NUMBER ? Match.TRUE : Match.FALSE;
		case integer:
			return (getUnitType() == CSSUnit.CSS_NUMBER && (asInteger || isCalculatedNumber())) ? Match.TRUE
					: Match.FALSE;
		case angle:
			return CSSUnit.isAngleUnitType(getUnitType()) ? Match.TRUE : Match.FALSE;
		case time:
			return CSSUnit.isTimeUnitType(getUnitType()) ? Match.TRUE : Match.FALSE;
		case resolution:
			return CSSUnit.isResolutionUnitType(getUnitType()) ? Match.TRUE : Match.FALSE;
		case frequency:
			return (getUnitType() == CSSUnit.CSS_HZ || getUnitType() == CSSUnit.CSS_KHZ) ? Match.TRUE : Match.FALSE;
		case flex:
			return getUnitType() == CSSUnit.CSS_FR ? Match.TRUE : Match.FALSE;
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	private boolean isLengthCompatible() {
		return CSSUnit.isLengthUnitType(getUnitType()) || (getUnitType() == CSSUnit.CSS_NUMBER && isNumberZero());
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
			case INTEGER:
				realvalue = lunit.getIntegerValue();
				asInteger = true;
				setUnitType(CSSUnit.CSS_NUMBER);
				break;
			default:
				realvalue = lunit.getFloatValue();
				asInteger = false;
				dimensionUnitText = lunit.getDimensionUnitText();
				setUnitType(lunit.getCssUnit());
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getCssValueType().hashCode();
		result = prime * result + getPrimitiveType().hashCode();
		short pType;
		if (realvalue != 0f) {
			pType = getUnitType();
		} else {
			pType = CSSUnit.CSS_NUMBER;
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
		return getUnitType() == other.getUnitType() || realvalue == 0f;
	}

	@Override
	public NumberValue clone() {
		return new NumberValue(this);
	}

	NumberValue immutable() {
		return new ImmutableCSSNumberValue(this);
	}

	private static class ImmutableCSSNumberValue extends NumberValue {

		private static final long serialVersionUID = 1L;

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

		@Override
		public NumberValue clone() {
			return this;
		}

	}

	private static int fractionDigits(short primitiveType) {
		switch (primitiveType) {
		case CSSUnit.CSS_EM:
		case CSSUnit.CSS_EX:
		case CSSUnit.CSS_IN:
		case CSSUnit.CSS_MM:
		case CSSUnit.CSS_PC:
		case CSSUnit.CSS_PX:
		case CSSUnit.CSS_PT:
		case CSSUnit.CSS_CAP:
		case CSSUnit.CSS_CH:
		case CSSUnit.CSS_IC:
		case CSSUnit.CSS_LH:
		case CSSUnit.CSS_QUARTER_MM:
		case CSSUnit.CSS_REM:
		case CSSUnit.CSS_RLH:
		case CSSUnit.CSS_MS:
		case CSSUnit.CSS_DEG:
		case CSSUnit.CSS_GRAD:
		case CSSUnit.CSS_PERCENTAGE:
			return 2;
		case CSSUnit.CSS_KHZ:
		case CSSUnit.CSS_CM:
		case CSSUnit.CSS_TURN:
			return 4;
		}
		return 3;
	}

	public static NumberValue createCSSNumberValue(short unit, float floatValue) {
		NumberValue num;
		if (unit != CSSUnit.CSS_PERCENTAGE) {
			num = new NumberValue();
		} else {
			num = new PercentageValue();
		}
		num.setFloatValue(unit, floatValue);
		return num;
	}

}
