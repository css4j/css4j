/*

 Copyright (c) 2005-2020, Carlos Amengual.

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
import io.sf.carte.doc.style.css.HWBColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * HWB color value.
 */
public class HWBColorValue extends ColorValue implements io.sf.carte.doc.style.css.HWBColorValue {

	private static final long serialVersionUID = 1L;

	private final HWBColorImpl hwbColor;

	public HWBColorValue() {
		super();
		hwbColor = new HWBColorImpl();
	}

	HWBColorValue(HWBColorValue copied) {
		super(copied);
		this.hwbColor = copied.hwbColor.clone();
	}

	@Override
	public CSSColorValue.ColorSpace getColorSpace() {
		return CSSColorValue.ColorSpace.HWB;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		HWBColorImpl setfrom = ((HWBColorValue) value).hwbColor;
		this.hwbColor.setHue(setfrom.getHue());
		this.hwbColor.setWhiteness(setfrom.getWhiteness());
		this.hwbColor.setBlackness(setfrom.getBlackness());
		this.hwbColor.alpha = setfrom.alpha;
	}

	@Override
	public String getCssText() {
		String css = hwbColor.toString();
		if (css != null) {
			return css;
		}
		return toRGBColorValue().toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		if (hwbColor.getAlpha().getPrimitiveType() != Type.NUMERIC
				|| ((CSSTypedValue) hwbColor.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER) != 1f) {
			String css = hwbColor.toMinifiedString();
			if (css != null) {
				return css;
			}
		}
		return ((CSSRGBColor) toRGBColorValue()).toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		String css = hwbColor.toString();
		if (css != null) {
			wri.write(css);
			return;
		}
		wri.write(toRGBColorValue().toString());
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		switch (index) {
		case 0:
			return hwbColor.alpha;
		case 1:
			return hwbColor.getHue();
		case 2:
			return hwbColor.getWhiteness();
		case 3:
			return hwbColor.getBlackness();
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		switch (index) {
		case 0:
			hwbColor.setAlpha((PrimitiveValue) component);
			break;
		case 1:
			hwbColor.setHue((PrimitiveValue) component);
			break;
		case 2:
			hwbColor.setWhiteness((PrimitiveValue) component);
			break;
		case 3:
			hwbColor.setBlackness((PrimitiveValue) component);
		}
	}

	@Override
	public RGBAColor toRGBColorValue() throws DOMException {
		if (!HSLColorValue.isConvertibleComponent(hwbColor.getHue())
				|| !HSLColorValue.isConvertibleComponent(hwbColor.getWhiteness())
				|| !HSLColorValue.isConvertibleComponent(hwbColor.getBlackness())) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float hue = ((CSSTypedValue) hwbColor.getHue()).getFloatValue(CSSUnit.CSS_DEG) / 360f;
		float whiteness = ((CSSTypedValue) hwbColor.getWhiteness()).getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
		float blackness = ((CSSTypedValue) hwbColor.getBlackness()).getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
		CSSRGBColor color = new CSSRGBColor();
		translateHWB(hue, whiteness, blackness, color);
		return color;
	}

	@Override
	public HWBColor getHWBColorValue() {
		return hwbColor;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			String func = lunit.getFunctionName();
			try {
				if ("hwb".equalsIgnoreCase(func)) {
					setLexicalHWB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No hwb() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalHWB(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// hue
			PrimitiveValue primihue = factory.createCSSPrimitiveValue(lu, true);
			HSLColorValue.checkHueValidity(primihue, lunit);
			// whiteness
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primiwhite = factory.createCSSPrimitiveValue(lu, true);
			HSLColorValue.checkPcntCompValidity(primiwhite, lunit);
			// blackness
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primiblackness = factory.createCSSPrimitiveValue(lu, true);
			HSLColorValue.checkPcntCompValidity(primiblackness, lunit);
			// slash or null
			lu = lu.getNextLexicalUnit();
			PrimitiveValue alpha = null;
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				alpha = factory.createCSSPrimitiveValue(lu, true);
				hwbColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
			hwbColor.setHue(primihue);
			hwbColor.setWhiteness(primiwhite);
			hwbColor.setBlackness(primiblackness);
		}

	}

	private void translateHWB(float hue, float whiteness, float blackness, CSSRGBColor color) {
		if (hue > 1f) {
			hue -= (float) Math.floor(hue);
		} else if (hue < 0f) {
			hue = hue - (float) Math.floor(hue) + 1f;
		}
		hue *= 6f;
		float fh = (float) Math.floor(hue);
		float f = hue - fh;
		int ifh = (int) fh;
		if (ifh % 2 == 1) {
			f = 1f -f;
		}
		float value = 1f - blackness;
		float wv = whiteness + f * (value - whiteness);
		float r, g, b;
		switch (ifh) {
		case 1:
			r = wv;
			g = value;
			b = whiteness;
			break;
		case 2:
			r = whiteness;
			g = value;
			b = wv;
			break;
		case 3:
			r = whiteness;
			g = wv;
			b = value;
			break;
		case 4:
			r = wv;
			g = whiteness;
			b = value;
			break;
		case 5:
			r = value;
			g = whiteness;
			b = wv;
			break;
		default:
			r = value;
			g = wv;
			b = whiteness;
		}
		NumberValue red = new NumberValue();
		red.setFloatValue(CSSUnit.CSS_PERCENTAGE, r * 100f);
		red.setAbsolutizedUnit();
		NumberValue green = new NumberValue();
		green.setFloatValue(CSSUnit.CSS_PERCENTAGE, g * 100f);
		green.setAbsolutizedUnit();
		NumberValue blue = new NumberValue();
		blue.setFloatValue(CSSUnit.CSS_PERCENTAGE, b * 100f);
		blue.setAbsolutizedUnit();
		color.setRed(red);
		color.setGreen(green);
		color.setBlue(blue);
		color.alpha = hwbColor.alpha.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + hwbColor.hashCode();
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
		if (!(obj instanceof HWBColorValue)) {
			return false;
		}
		HWBColorValue other = (HWBColorValue) obj;
		return hwbColor.equals(other.hwbColor);
	}

	@Override
	public HWBColorValue clone() {
		return new HWBColorValue(this);
	}

}
