/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.BaseColor.Space;
import io.sf.carte.util.SimpleWriter;

/**
 * OKLCh color value.
 */
@SuppressWarnings("deprecation")
class OKLCHColorValue extends ColorValue implements io.sf.carte.doc.style.css.LCHColorValue {

	private static final long serialVersionUID = 1L;

	private final LCHColorImpl lchColor;

	public OKLCHColorValue() {
		this(new LCHColorImpl(Space.OK_LCh, ColorSpace.ok_lch));
	}

	OKLCHColorValue(LCHColorImpl color) {
		super();
		lchColor = color;
	}

	OKLCHColorValue(OKLCHColorValue copied) {
		super(copied);
		this.lchColor = copied.lchColor.clone();
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return CSSColorValue.ColorModel.LCH;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		OKLCHColorValue setfrom = (OKLCHColorValue) value;
		this.lchColor.set(setfrom.lchColor);
	}

	@Override
	public String getCssText() {
		return lchColor.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		return lchColor.toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		lchColor.writeCssText(wri);
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		return lchColor.item(index);
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		lchColor.setComponent(index, (PrimitiveValue) component);
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		return toRGBColor(true);
	}

	@Override
	public RGBAColor toRGBColor(boolean clamp) throws DOMException {
		if (!lchColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		CSSTypedValue primihue = (CSSTypedValue) lchColor.getHue();
		float c = ((CSSTypedValue) lchColor.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER);
		double h = ColorUtil.hueRadians(primihue);

		double a = c * Math.cos(h);
		double b = c * Math.sin(h);
		float light = ((CSSTypedValue) lchColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER);

		ColorProfile profile = new SRGBColorProfile();
		double[] rgb = new double[3];
		ColorUtil.oklabToRGB(light, a, b, clamp, profile, rgb);
		CSSRGBColor color = new CSSRGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(lchColor.getAlpha().clone());
		return color;
	}

	@Override
	public LABColorValue toLABColorValue() throws DOMException {
		if (!lchColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		float light = ((CSSTypedValue) lchColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER);
		float c = ((CSSTypedValue) lchColor.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER);
		CSSTypedValue primihue = (CSSTypedValue) lchColor.getHue();
		float h;
		short unit = primihue.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			h = primihue.getFloatValue();
			h = NumberValue.floatValueConversion(h, CSSUnit.CSS_DEG, CSSUnit.CSS_RAD);
		} else {
			h = primihue.getFloatValue(CSSUnit.CSS_RAD);
		}

		double a = c * Math.cos(h);
		double b = c * Math.sin(h);

		double[] lab = new double[3];
		ColorUtil.oklabToLab(light, a, b, lab);
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[0]);
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[1]);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[2]);
		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();

		LABColorValue primiLab = new LABColorValue();
		primiLab.setComponent(0, lchColor.getAlpha().clone());
		primiLab.setComponent(1, primiL);
		primiLab.setComponent(2, primia);
		primiLab.setComponent(3, primib);
		return primiLab;
	}

	@Override
	public LCHColorValue toLCHColorValue() throws DOMException {
		if (!lchColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		float light = ((CSSTypedValue) lchColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER);
		float c = ((CSSTypedValue) lchColor.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER);
		CSSTypedValue primihue = (CSSTypedValue) lchColor.getHue();
		float h;
		short unit = primihue.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			h = primihue.getFloatValue();
			h = NumberValue.floatValueConversion(h, CSSUnit.CSS_DEG, CSSUnit.CSS_RAD);
		} else {
			h = primihue.getFloatValue(CSSUnit.CSS_RAD);
		}

		double a = c * Math.cos(h);
		double b = c * Math.sin(h);

		double[] lab = new double[3];
		ColorUtil.oklabToLab(light, a, b, lab);
		c = (float) Math.sqrt(lab[1] * lab[1] + lab[2] * lab[2]);
		h = (float) (Math.atan2(lab[2], lab[1]) * 180f / Math.PI);
		if (h < 0f) {
			h += 360f;
		}

		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[0]);
		NumberValue primiC = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, c);
		NumberValue primih = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, h);
		primiL.setAbsolutizedUnit();
		primiC.setAbsolutizedUnit();
		primih.setAbsolutizedUnit();

		LCHColorValue primiLch = new LCHColorValue();
		primiLch.setComponent(0, lchColor.getAlpha().clone());
		primiLch.setComponent(1, primiL);
		primiLch.setComponent(2, primiC);
		primiLch.setComponent(3, primih);
		return primiLch;
	}

	@Override
	public LCHColor getColor() {
		return lchColor;
	}

	@Override
	boolean hasConvertibleComponents() {
		return lchColor.hasConvertibleComponents();
	}

	@Override
	public float deltaE2000(CSSColorValue color) {
		if (!lchColor.hasConvertibleComponents()
			|| !((ColorValue) color).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}

		LCHColor lch;
		switch (color.getColorModel()) {
		case LCH:
			lch = (LCHColor) color.getColor();
			if (ColorSpace.ok_lch.equals(lch.getColorSpace())) {
				lch = color.toLCHColorValue().getColor();
			}
			break;
		case LAB:
			lch = color.toLCHColorValue().getColor();
			break;
		case RGB:
			RGBColor rgbcolor = (RGBColor) color.getColor();
			LABColorValue lab = new LABColorValue();
			rgbcolor.toLABColor(lab.getLABColorImpl());
			lch = lab.toLCHColorValue().getColor();
			break;
		case XYZ:
			XYZColorImpl xyz = (XYZColorImpl) color.getColor();
			lab = new LABColorValue();
			xyz.toLABColor(lab.getLABColorImpl());
			lch = lab.toLCHColorValue().getColor();
			break;
		default:
			RGBAColor rgb = color.toRGBColor(false);
			CSSColorValue rgbValue = rgb.packInValue();
			lch = rgbValue.toLABColorValue().toLCHColorValue().getColor();
		}

		LCHColor thislch = toLCHColorValue().getColor();
		return ColorUtil.deltaE2000LCh(
			((CSSTypedValue) thislch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
			((CSSTypedValue) thislch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
			ColorUtil.hueRadians((CSSTypedValue) thislch.getHue()),
			((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
			((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
			ColorUtil.hueRadians((CSSTypedValue) lch.getHue()));
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.OKLCHCOLOR) {
					setLexicalLCH(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
						"No oklch() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMSyntaxException("Invalid value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalLCH(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			CSSColor from = null;

			// from?
			if (lu.getLexicalUnitType() == LexicalType.IDENT) {
				if ("from".equalsIgnoreCase(lu.getStringValue())) {
					lu = nextLexicalUnit(lu, lunit);
					PrimitiveValue fromval = factory.createCSSPrimitiveValue(lu, true);
					from = computeColor(fromval, factory);
					String cs = from.getColorSpace();
					if (!cs.equals(ColorSpace.ok_lch)) {
						from = from.toColorSpace(ColorSpace.ok_lch);
					}
					lu = nextLexicalUnit(lu, lunit);
				}
			}

			// lightness
			PrimitiveValue primilight = factory.createCSSPrimitiveValue(lu, true);
			if (from != null) {
				primilight = absoluteComponent(from, primilight, false);
			}

			// chroma
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primichroma = factory.createCSSPrimitiveValue(lu, true);
			if (from != null) {
				primichroma = absoluteComponent(from, primichroma, false);
			}

			// hue
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primihue = factory.createCSSPrimitiveValue(lu, true);
			if (from != null) {
				primihue = absoluteHue(from, primihue);
			}

			// slash or null
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					checkProxyValue(lu);
					throw new DOMSyntaxException("Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				PrimitiveValue alpha = factory.createCSSPrimitiveValue(lu, true);
				if (from != null) {
					alpha = absoluteComponent(from, alpha, false);
				}
				lchColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMSyntaxException("Invalid value: " + lunit.toString());
				}
			}
			lchColor.setLightness(primilight);
			lchColor.setChroma(primichroma);
			lchColor.setHue(primihue);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + lchColor.hashCode();
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
		OKLCHColorValue other = (OKLCHColorValue) obj;
		return lchColor.equals(other.lchColor);
	}

	@Override
	public OKLCHColorValue clone() {
		return new OKLCHColorValue(this);
	}

}
