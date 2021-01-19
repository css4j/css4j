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
		this.lchColor.setLightness(setfrom.lchColor.getLightness());
		this.lchColor.setChroma(setfrom.lchColor.getChroma());
		this.lchColor.setHue(setfrom.lchColor.getHue());
		this.lchColor.alpha = setfrom.lchColor.alpha;
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
		switch (index) {
		case 0:
			return lchColor.alpha;
		case 1:
			return lchColor.getLightness();
		case 2:
			return lchColor.getChroma();
		case 3:
			return lchColor.getHue();
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		switch (index) {
		case 0:
			lchColor.setAlpha((PrimitiveValue) component);
			break;
		case 1:
			lchColor.setLightness((PrimitiveValue) component);
			break;
		case 2:
			lchColor.setChroma((PrimitiveValue) component);
			break;
		case 3:
			lchColor.setHue((PrimitiveValue) component);
		}
	}

	@Override
	public RGBAColor toRGBColorValue() throws DOMException {
		if (!isConvertibleComponent(lchColor.getChroma()) || !isConvertibleComponent(lchColor.getHue())
				|| !isConvertibleComponent(lchColor.getLightness())) {
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
		float light = ((CSSTypedValue) lchColor.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE);
		//
		CSSRGBColor color = new CSSRGBColor();
		LABColorValue.labToRGB(light, a, b, lchColor.getAlpha(), color);
		return color;
	}

	@Override
	public LABColorValue toLABColorValue() throws DOMException {
		if (!isConvertibleComponent(lchColor.getChroma()) || !isConvertibleComponent(lchColor.getHue())
				|| !isConvertibleComponent(lchColor.getLightness())) {
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
	public LCHColor getLCHColorValue() {
		return lchColor;
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
