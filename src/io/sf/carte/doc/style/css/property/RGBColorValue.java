/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * RGB color value.
 */
@SuppressWarnings("removal")
public class RGBColorValue extends ColorValue implements io.sf.carte.doc.style.css.RGBColorValue {

	private static final long serialVersionUID = 1L;

	private final CSSRGBColor color;

	RGBColorValue() {
		super();
		color = new CSSRGBColor();
	}

	protected RGBColorValue(RGBColorValue copied) {
		super(copied);
		this.color = copied.color.clone();
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		BaseColor setfrom = (BaseColor) ((ColorValue) value).getColor();
		this.color.set(setfrom);
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return CSSColorValue.ColorModel.RGB;
	}

	@Override
	public RGBAColor getColor() {
		return color;
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		if (color.getRed() == null || color.getGreen() == null || color.getBlue() == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Color not set");
		}
		return color;
	}

	/**
	 * Convert this value to a {@link LABColorValue}, if possible.
	 * 
	 * @return the converted {@code LABColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	@Override
	public LABColorValue toLABColorValue() throws DOMException {
		LABColorValue lab = new LABColorValue();
		color.toLABColor(lab.getLABColorImpl());
		return lab;
	}

	/**
	 * Convert this value to a {@link HSLColorValue}, if possible.
	 * 
	 * @return the converted {@code HSLColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	@Override
	public HSLColorValue toHSLColorValue() throws DOMException {
		if (!color.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		//
		HSLColorValue hsl = new HSLColorValue();
		color.toHSLColor(hsl.getHSLColorImpl());
		return hsl;
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		return color.item(index);
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		color.setComponent(index, (PrimitiveValue) component);
	}

	@Override
	boolean hasConvertibleComponents() {
		return color.hasConvertibleComponents();
	}

	@Override
	public float deltaE2000(CSSColorValue color) {
		if (!this.color.hasConvertibleComponents() || !((ColorValue) color).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}
		//
		LABColor lab1;
		LABColor lab2;
		switch (color.getColorModel()) {
		case LCH:
		case LAB:
			// Delegate on the higher-precision color models
			return color.deltaE2000(this);
		case RGB:
			lab1 = toLABColorValue().getColor();
			RGBColor rgb = (RGBColor) color.getColor();
			LABColorImpl labColor = new LABColorImpl();
			rgb.toLABColor(labColor);
			lab2 = labColor;
			break;
		case XYZ:
			lab1 = toLABColorValue().getColor();
			XYZColorImpl xyz = (XYZColorImpl) color.getColor();
			labColor = new LABColorImpl();
			xyz.toLABColor(labColor);
			lab2 = labColor;
			break;
		default:
			RGBAColor rgba = color.toRGBColor(false);
			RGBColorValue rgbValue = new RGBColorValue();
			rgbValue.setComponent(0, (StyleValue) rgba.getAlpha());
			rgbValue.setComponent(1, (StyleValue) rgba.getRed());
			rgbValue.setComponent(2, (StyleValue) rgba.getGreen());
			rgbValue.setComponent(3, (StyleValue) rgba.getBlue());
			lab2 = rgbValue.toLABColorValue().getColor();
			lab1 = toLABColorValue().getColor();
		}
		return ColorUtil.deltaE2000Lab(((CSSTypedValue) lab1.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lab1.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab1.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lab2.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getB()).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.RGBCOLOR) {
					setLexicalRGB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No rgb() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalRGB(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// red
			PrimitiveValue basiccolor = factory.createCSSPrimitiveValue(lu, true);
			color.setRed(basiccolor);
			// comma ?
			lu = lu.getNextLexicalUnit();
			if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.LexicalType.OPERATOR_COMMA) {
				// green
				lu = lu.getNextLexicalUnit();
			}
			basiccolor = factory.createCSSPrimitiveValue(lu, true);
			color.setGreen(basiccolor);
			if (commaSyntax) {
				// comma
				lu = lu.getNextLexicalUnit();
			}
			// blue
			lu = lu.getNextLexicalUnit();
			basiccolor = factory.createCSSPrimitiveValue(lu, true);
			color.setBlue(basiccolor);
			// comma, slash or null
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				// alpha
				lu = lu.getNextLexicalUnit();
				color.setAlpha(factory.createCSSPrimitiveValue(lu, true));
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((color == null) ? 0 : color.hashCode());
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
		RGBColorValue other = (RGBColorValue) obj;
		return color.equals(other.color);
	}

	@Override
	public RGBColorValue clone() {
		return new RGBColorValue(this);
	}

}
