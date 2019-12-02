/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * HSL color value.
 */
public class HSLColorValue extends ColorValue implements io.sf.carte.doc.style.css.HSLColorValue {

	private final HSLColorImpl hslColor;

	public HSLColorValue() {
		super();
		hslColor = new MyHSLColorImpl();
	}

	HSLColorValue(HSLColorValue copied) {
		super(copied);
		this.hslColor = copied.hslColor.clone();
	}

	@Override
	public CSSColorValue.ColorSpace getColorSpace() {
		return CSSColorValue.ColorSpace.HSL;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		HSLColorValue setfrom = (HSLColorValue) value;
		this.hslColor.setHue(setfrom.hslColor.getHue());
		this.hslColor.setSaturation(setfrom.hslColor.getSaturation());
		this.hslColor.setLightness(setfrom.hslColor.getLightness());
		this.hslColor.alpha = setfrom.hslColor.alpha;
	}

	@Override
	public String getCssText() {
		String css = hslColor.toString(commaSyntax);
		if (css != null) {
			return css;
		}
		return toRGBColorValue().toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		if (hslColor.getAlpha().getPrimitiveType() != Type.NUMERIC
				|| ((CSSTypedValue) hslColor.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER) != 1f) {
			String css = hslColor.toMinifiedString(commaSyntax);
			if (css != null) {
				return css;
			}
		}
		return ((CSSRGBColor) toRGBColorValue()).toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		String css = hslColor.toString();
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
			return hslColor.alpha;
		case 1:
			return hslColor.getHue();
		case 2:
			return hslColor.getSaturation();
		case 3:
			return hslColor.getLightness();
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		switch (index) {
		case 0:
			hslColor.setAlpha((PrimitiveValue) component);
			break;
		case 1:
			hslColor.setHue((PrimitiveValue) component);
			break;
		case 2:
			hslColor.setSaturation((PrimitiveValue) component);
			break;
		case 3:
			hslColor.setLightness((PrimitiveValue) component);
		}
	}

	@Override
	public RGBAColor toRGBColorValue() throws DOMException {
		if (!isConvertibleComponent(hslColor.getHue()) || !isConvertibleComponent(hslColor.getSaturation())
				|| !isConvertibleComponent(hslColor.getLightness())) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float hue = ((CSSTypedValue) hslColor.getHue()).getFloatValue(CSSUnit.CSS_DEG) / 360f;
		float sat = ((CSSTypedValue) hslColor.getSaturation()).getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
		float light = ((CSSTypedValue) hslColor.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
		CSSRGBColor color = new CSSRGBColor();
		translateHSL(hue, sat, light, color);
		return color;
	}

	static boolean isConvertibleComponent(PrimitiveValue comp) {
		return comp != null && comp.getPrimitiveType() == Type.NUMERIC;
	}

	@Override
	public HSLColor getHSLColorValue() {
		return hslColor;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.HSLCOLOR) {
					setLexicalHSL(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No hsl() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalHSL(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// hue
			PrimitiveValue primihue = factory.createCSSPrimitiveValue(lu, true);
			checkHueValidity(primihue, lunit);
			// comma
			lu = lu.getNextLexicalUnit();
			if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.LexicalType.OPERATOR_COMMA) {
				// saturation
				lu = lu.getNextLexicalUnit();
			}
			PrimitiveValue primisat = factory.createCSSPrimitiveValue(lu, true);
			checkPcntCompValidity(primisat, lunit);
			if (commaSyntax) {
				// comma
				lu = lu.getNextLexicalUnit();
			}
			// lightness
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primilight = factory.createCSSPrimitiveValue(lu, true);
			checkPcntCompValidity(primilight, lunit);
			// comma, slash or null
			lu = lu.getNextLexicalUnit();
			PrimitiveValue alpha = null;
			if (lu != null) {
				if (commaSyntax) {
					if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_COMMA) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
					}
				} else if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				alpha = factory.createCSSPrimitiveValue(lu, true);
				hslColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
			hslColor.setHue(primihue);
			hslColor.setSaturation(primisat);
			hslColor.setLightness(primilight);
		}

	}

	static void checkHueValidity(PrimitiveValue primihue, LexicalUnit lunit) {
		if (primihue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(primihue.getUnitType())
				&& primihue.getCssValueType() != CssType.PROXY && primihue.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported value: " + lunit.toString());
		}
	}

	static void checkPcntCompValidity(PrimitiveValue primisat, LexicalUnit lunit) {
		if (primisat.getUnitType() != CSSUnit.CSS_PERCENTAGE && primisat.getCssValueType() != CssType.PROXY
				&& primisat.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported value: " + lunit.toString());
		}
	}

	private void translateHSL(float hue, float sat, float light, CSSRGBColor color) {
		if (hue > 1f) {
			hue -= (float) Math.floor(hue);
		} else if (hue < 0f) {
			hue = hue - (float) Math.floor(hue) + 1f;
		}
		float m2;
		if (light <= 0.5f) {
			m2 = light * (sat + 1f);
		} else {
			m2 = light + sat - light * sat;
		}
		float m1 = light * 2f - m2;
		NumberValue red = new NumberValue();
		red.setFloatValue(CSSUnit.CSS_PERCENTAGE, hueToRgb(m1, m2, hue + 1f / 3f));
		red.setAbsolutizedUnit();
		NumberValue green = new NumberValue();
		green.setFloatValue(CSSUnit.CSS_PERCENTAGE, hueToRgb(m1, m2, hue));
		green.setAbsolutizedUnit();
		NumberValue blue = new NumberValue();
		blue.setFloatValue(CSSUnit.CSS_PERCENTAGE, hueToRgb(m1, m2, hue - 1f / 3f));
		blue.setAbsolutizedUnit();
		color.setRed(red);
		color.setGreen(green);
		color.setBlue(blue);
		color.alpha = hslColor.alpha.clone();
	}

	private static float hueToRgb(float m1, float m2, float h) {
		if (h < 0f) {
			h = h + 1f;
		} else if (h > 1f) {
			h = h - 1f;
		}
		if (h * 6f < 1f) {
			return (m1 + (m2 - m1) * h * 6f) * 100f;
		}
		if (h * 2f < 1f) {
			return m2 * 100f;
		}
		if (h * 3f < 2f) {
			return (m1 + (m2 - m1) * (2f / 3f - h) * 6f) * 100f;
		}
		return m1 * 100f;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + hslColor.hashCode();
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
		if (!(obj instanceof HSLColorValue)) {
			return false;
		}
		HSLColorValue other = (HSLColorValue) obj;
		return hslColor.equals(other.hslColor);
	}

	@Override
	public HSLColorValue clone() {
		return new HSLColorValue(this);
	}

}