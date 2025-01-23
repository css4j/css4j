/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.BaseColor.Space;
import io.sf.carte.util.SimpleWriter;

/**
 * Lab color value.
 */
@SuppressWarnings("deprecation")
public class LABColorValue extends ColorValue implements io.sf.carte.doc.style.css.LABColorValue {

	private static final long serialVersionUID = 2L;

	private final LABColorImpl labColor;

	public LABColorValue() {
		this(new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab));
	}

	LABColorValue(LABColorImpl color) {
		super();
		labColor = color;
	}

	LABColorValue(LABColorValue copied) {
		super(copied);
		this.labColor = copied.labColor.clone();
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return CSSColorValue.ColorModel.LAB;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		LABColorValue setfrom = (LABColorValue) value;
		this.labColor.set(setfrom.labColor);
	}

	@Override
	public String getCssText() {
		return labColor.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		return labColor.toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		labColor.writeCssText(wri);
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		return labColor.item(index);
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		labColor.setComponent(index, (PrimitiveValue) component);
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		return toRGBColor(true);
	}

	@Override
	public RGBAColor toRGBColor(boolean clamp) throws DOMException {
		// Convert to sRGB
		double[] rgb = labColor.toSRGB(clamp);
		CSSRGBColor color = new CSSRGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(labColor.getAlpha().clone());
		return color;
	}

	@Override
	public LABColorValue toLABColorValue() {
		return this;
	}

	@Override
	public LCHColorValue toLCHColorValue() throws DOMException {
		if (!labColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float a = ((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER);
		float b = ((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER);
		//
		float c = (float) Math.sqrt(a * a + b * b);
		float h = (float) (Math.atan2(b, a) * 180f / Math.PI);
		if (h < 0f) {
			h += 360f;
		}
		NumberValue chroma = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, c);
		NumberValue hue = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, h);
		chroma.setAbsolutizedUnit();
		hue.setAbsolutizedUnit();
		//
		LCHColorValue lch = new LCHColorValue();
		lch.setComponent(0, labColor.getAlpha().clone());
		lch.setComponent(1, labColor.getLightness().clone());
		lch.setComponent(2, chroma);
		lch.setComponent(3, hue);
		return lch;
	}

	@Override
	public LABColor getColor() {
		return labColor;
	}

	LABColorImpl getLABColorImpl() {
		return labColor;
	}

	@Override
	boolean hasConvertibleComponents() {
		return labColor.hasConvertibleComponents();
	}

	@Override
	public float deltaE2000(CSSColorValue color) {
		if (!labColor.hasConvertibleComponents() || !((ColorValue) color).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}
		//
		LABColor lab;
		switch (color.getColorModel()) {
		case LAB:
			lab = (LABColor) color.getColor();
			if (ColorSpace.ok_lab.equals(lab.getColorSpace())) {
				lab = color.toLABColorValue().getColor();
			}
			break;
		case LCH:
			lab = color.toLABColorValue().getColor();
			break;
		case RGB:
			RGBColor rgbcolor = (RGBColor) color.getColor();
			LABColorImpl labColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
			rgbcolor.toLABColor(labColor);
			lab = labColor;
			break;
		case XYZ:
			XYZColorImpl xyz = (XYZColorImpl) color.getColor();
			labColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
			xyz.toLABColor(labColor);
			lab = labColor;
			break;
		default:
			RGBAColor rgb = color.toRGBColor(false);
			RGBColorValue rgbValue = new RGBColorValue();
			rgbValue.setComponent(0, (StyleValue) rgb.getAlpha());
			rgbValue.setComponent(1, (StyleValue) rgb.getRed());
			rgbValue.setComponent(2, (StyleValue) rgb.getGreen());
			rgbValue.setComponent(3, (StyleValue) rgb.getBlue());
			lab = rgbValue.toLABColorValue().getColor();
		}
		return ColorUtil.deltaE2000Lab(
				((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.LABCOLOR) {
					setLexicalLAB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No lab() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalLAB(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// lightness
			PrimitiveValue primilight = factory.createCSSPrimitiveValue(lu, true);
			// a
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primia = factory.createCSSPrimitiveValue(lu, true);
			// b
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primib = factory.createCSSPrimitiveValue(lu, true);
			// slash or null
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				PrimitiveValue alpha = factory.createCSSPrimitiveValue(lu, true);
				labColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Bad value: " + lunit.toString());
				}
			}
			labColor.setLightness(primilight);
			labColor.setA(primia);
			labColor.setB(primib);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + labColor.hashCode();
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
		LABColorValue other = (LABColorValue) obj;
		return labColor.equals(other.labColor);
	}

	@Override
	public LABColorValue clone() {
		return new LABColorValue(this);
	}

}
