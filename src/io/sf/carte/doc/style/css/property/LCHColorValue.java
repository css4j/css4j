/*

 Copyright (c) 2005-2021, Carlos Amengual.

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
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * LCh color value.
 */
public class LCHColorValue extends ColorValue implements io.sf.carte.doc.style.css.LCHColorValue {

	private static final long serialVersionUID = 1L;

	private final LCHColorImpl lchColor;

	public LCHColorValue() {
		super();
		lchColor = new LCHColorImpl();
	}

	LCHColorValue(LCHColorValue copied) {
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
		LCHColorValue setfrom = (LCHColorValue) value;
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
		//
		CSSTypedValue primihue = (CSSTypedValue) lchColor.getHue();
		float c = ((CSSTypedValue) lchColor.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER);
		float h = hueRadians(primihue);
		//
		float a = (float) (c * Math.cos(h));
		float b = (float) (c * Math.sin(h));
		float light = ((CSSTypedValue) lchColor.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE);
		//
		CSSRGBColor color = new CSSRGBColor();
		ColorUtil.labToRGB(light, a, b, clamp, lchColor.getAlpha(), color);
		return color;
	}

	@Override
	public LABColorValue toLABColorValue() throws DOMException {
		if (!lchColor.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		//
		CSSTypedValue primihue = (CSSTypedValue) lchColor.getHue();
		float c = ((CSSTypedValue) lchColor.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER);
		float h;
		short unit = primihue.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			h = primihue.getFloatValue(CSSUnit.CSS_NUMBER);
			h = NumberValue.floatValueConversion(h, CSSUnit.CSS_DEG, CSSUnit.CSS_RAD);
		} else {
			h = primihue.getFloatValue(CSSUnit.CSS_RAD);
		}
		//
		float a = (float) (c * Math.cos(h));
		float b = (float) (c * Math.sin(h));
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, a);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, b);
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();
		//
		LABColorValue lab = new LABColorValue();
		lab.setComponent(0, lchColor.getAlpha().clone());
		lab.setComponent(1, lchColor.getLightness().clone());
		lab.setComponent(2, primia);
		lab.setComponent(3, primib);
		return lab;
	}

	@Override
	public LCHColorValue toLCHColorValue() throws DOMException {
		return this;
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
		if (!lchColor.hasConvertibleComponents() || !((ColorValue) color).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}
		//
		LCHColor lch;
		switch (color.getColorModel()) {
		case LCH:
			lch = ((LCHColorValue) color).getColor();
			break;
		case LAB:
			lch = ((LABColorValue) color).toLCHColorValue().getColor();
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
			RGBColorValue rgbValue = new RGBColorValue();
			rgbValue.setComponent(0, (StyleValue) rgb.getAlpha());
			rgbValue.setComponent(1, (StyleValue) rgb.getRed());
			rgbValue.setComponent(2, (StyleValue) rgb.getGreen());
			rgbValue.setComponent(3, (StyleValue) rgb.getBlue());
			lch = rgbValue.toLABColorValue().toLCHColorValue().getColor();
		}
		return ColorUtil.deltaE2000LCh(((CSSTypedValue) lchColor.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lchColor.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				hueRadians((CSSTypedValue) lchColor.getHue()),
				((CSSTypedValue) lch.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lch.getChroma()).getFloatValue(CSSUnit.CSS_NUMBER),
				hueRadians((CSSTypedValue) lch.getHue()));
	}

	private static float hueRadians(CSSTypedValue primihue) {
		float h;
		short unit = primihue.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			h = primihue.getFloatValue(CSSUnit.CSS_NUMBER);
			h = NumberValue.floatValueConversion(h, CSSUnit.CSS_DEG, CSSUnit.CSS_RAD);
		} else {
			h = primihue.getFloatValue(CSSUnit.CSS_RAD);
		}
		return h;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.LCHCOLOR) {
					setLexicalLCH(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No lch() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalLCH(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// lightness
			PrimitiveValue primilight = factory.createCSSPrimitiveValue(lu, true);
			checkPcntCompValidity(primilight, lunit);
			// chroma
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primichroma = factory.createCSSPrimitiveValue(lu, true);
			checkNumberCompValidity(primichroma, lunit);
			// hue
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primihue = factory.createCSSPrimitiveValue(lu, true);
			checkHueValidity(primihue, lunit);
			// slash or null
			lu = lu.getNextLexicalUnit();
			PrimitiveValue alpha = null;
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				alpha = factory.createCSSPrimitiveValue(lu, true);
				lchColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
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
		LCHColorValue other = (LCHColorValue) obj;
		return lchColor.equals(other.lchColor);
	}

	@Override
	public LCHColorValue clone() {
		return new LCHColorValue(this);
	}

}
