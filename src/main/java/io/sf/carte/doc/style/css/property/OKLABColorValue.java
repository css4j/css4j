/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
 * OKLab color value.
 */
@SuppressWarnings("deprecation")
class OKLABColorValue extends ColorValue implements io.sf.carte.doc.style.css.LABColorValue {

	private static final long serialVersionUID = 1L;

	private final LABColorImpl labColor;

	public OKLABColorValue() {
		this(new LABColorImpl(Space.OK_Lab, ColorSpace.ok_lab));
	}

	OKLABColorValue(LABColorImpl color) {
		super();
		labColor = color;
	}

	OKLABColorValue(OKLABColorValue copied) {
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
		OKLABColorValue setfrom = (OKLABColorValue) value;
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
		// Convert to XYZ
		if (!labColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float light = ((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER);
		float a = ((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER);
		float b = ((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER);
		//
		ColorProfile profile = new SRGBColorProfile();
		double[] rgb = new double[3];
		ColorUtil.oklabToRGB(light, a, b, clamp, profile, rgb);
		CSSRGBColor color = new CSSRGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(labColor.getAlpha().clone());
		return color;
	}

	@Override
	public LABColorValue toLABColorValue() {
		if (!labColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float light = ((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER);
		float a = ((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER);
		float b = ((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER);
		//
		double[] lab = new double[3];
		ColorUtil.oklabToLab(light, a, b, lab);
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[0]);
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[1]);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[2]);
		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();
		//
		LABColorValue primiLab = new LABColorValue();
		primiLab.setComponent(0, labColor.getAlpha().clone());
		primiLab.setComponent(1, primiL);
		primiLab.setComponent(2, primia);
		primiLab.setComponent(3, primib);
		return primiLab;
	}

	@Override
	public LCHColorValue toLCHColorValue() throws DOMException {
		if (!labColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float light = ((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER);
		double a = ((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER);
		double b = ((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER);
		//
		double[] lab = new double[3];
		ColorUtil.oklabToLab(light, a, b, lab);
		//
		a = lab[1];
		b = lab[2];
		double c = Math.sqrt(a * a + b * b);
		double h = Math.atan2(b, a) * 180d / Math.PI;
		if (h < 0d) {
			h += 360d;
		}
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[0]);
		NumberValue chroma = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) c);
		NumberValue hue = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, (float) h);
		primiL.setAbsolutizedUnit();
		chroma.setAbsolutizedUnit();
		hue.setAbsolutizedUnit();
		//
		LCHColorValue lch = new LCHColorValue();
		lch.setComponent(0, labColor.getAlpha().clone());
		lch.setComponent(1, primiL);
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
		if (!labColor.hasConvertibleComponents()
			|| !((ColorValue) color).hasConvertibleComponents()) {
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
			CSSColorValue rgbValue = rgb.packInValue();
			lab = rgbValue.toLABColorValue().getColor();
		}

		LABColor thislab = toLABColorValue().getColor();
		return ColorUtil.deltaE2000Lab(
			((CSSTypedValue) thislab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
			((CSSTypedValue) thislab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
			((CSSTypedValue) thislab.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
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
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.OKLABCOLOR) {
					setLexicalLAB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
						"No oklab() value: " + lunit.toString());
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
		OKLABColorValue other = (OKLABColorValue) obj;
		return labColor.equals(other.labColor);
	}

	@Override
	public OKLABColorValue clone() {
		return new OKLABColorValue(this);
	}

}
