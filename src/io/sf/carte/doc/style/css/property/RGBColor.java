/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.RGBAColor;

class RGBColor extends BaseColor implements RGBAColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue red = null;
	private PrimitiveValue green = null;
	private PrimitiveValue blue = null;

	RGBColor() {
		super();
	}

	RGBColor(RGBColor copyMe) {
		super();
		red = copyMe.red.clone();
		green = copyMe.green.clone();
		blue = copyMe.blue.clone();
		setAlpha(copyMe.alpha.clone());
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.RGB;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		RGBColor rgbcolor = (RGBColor) color;
		this.red = rgbcolor.red;
		this.green = rgbcolor.green;
		this.blue = rgbcolor.blue;
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getRed();
		case 2:
			return getGreen();
		case 3:
			return getBlue();
		}
		return null;
	}

	/**
	 * Set the component of this color located at {@code index}.
	 * 
	 * @param index the index.
	 * @param component the component value.
	 */
	void setComponent(int index, PrimitiveValue component) {
		switch (index) {
		case 0:
			setAlpha(component);
			break;
		case 1:
			setRed(component);
			break;
		case 2:
			setGreen(component);
			break;
		case 3:
			setBlue(component);
		}
	}

	public void setRed(PrimitiveValue red) {
		if (red == null) {
			throw new NullPointerException();
		}
		enforceColorComponentType(red);
		this.red = red;
	}

	void enforceColorComponentType(PrimitiveValue primi) {
		if (primi.getUnitType() == CSSUnit.CSS_NUMBER) {
			CSSTypedValue typed = (CSSTypedValue) primi;
			float fv = typed.getFloatValue(CSSUnit.CSS_NUMBER);
			if (fv < 0f) {
				if (!typed.isCalculatedNumber()) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Color component cannot be smaller than zero.");
				}
				// Clamp
				typed.setFloatValue(CSSUnit.CSS_NUMBER, 0f);
			}
		} else if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			CSSTypedValue typed = (CSSTypedValue) primi;
			float fv = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			if (fv < 0f || fv > 100f) {
				if (!typed.isCalculatedNumber()) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Color component percentage cannot be smaller than zero or greater than 100%.");
				}
				// Clamp
				if (fv < 0f) {
					typed.setFloatValue(CSSUnit.CSS_PERCENTAGE, 0f);
				} else {
					typed.setFloatValue(CSSUnit.CSS_PERCENTAGE, 100f);
				}
			}
		} else if (primi.getCssValueType() != CssType.PROXY && primi.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with color component.");
		}
	}

	@Override
	public PrimitiveValue getRed() {
		return red;
	}

	public void setGreen(PrimitiveValue green) {
		if (green == null) {
			throw new NullPointerException();
		}
		enforceColorComponentType(green);
		this.green = green;
	}

	@Override
	public PrimitiveValue getGreen() {
		return green;
	}

	public void setBlue(PrimitiveValue blue) {
		if (blue == null) {
			throw new NullPointerException();
		}
		enforceColorComponentType(blue);
		this.blue = blue;
	}

	@Override
	public PrimitiveValue getBlue() {
		return blue;
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getRed()) && isConvertibleComponent(getGreen())
				&& isConvertibleComponent(getBlue());
	}

	public HSLColor toHSLColor() {
		HSLColorImpl hslColor = createHSLColor();
		toHSLColor(hslColor);
		return hslColor;
	}

	HSLColorImpl createHSLColor() {
		return new HSLColorImpl();
	}

	void toHSLColor(HSLColorImpl hslColor) {
		Hsl hsl = toHSL();
		if (hsl == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Conversion to hsl() failed.");
		}
		NumberValue h = new NumberValue();
		h.setFloatValue(CSSUnit.CSS_DEG, hsl.h);
		h.setAbsolutizedUnit();
		PercentageValue s = new PercentageValue();
		s.setFloatValue(CSSUnit.CSS_PERCENTAGE, hsl.s);
		s.setAbsolutizedUnit();
		PercentageValue l = new PercentageValue();
		l.setFloatValue(CSSUnit.CSS_PERCENTAGE, hsl.l);
		l.setAbsolutizedUnit();
		hslColor.setHue(h);
		hslColor.setSaturation(s);
		hslColor.setLightness(l);
		hslColor.setAlpha(getAlpha());
	}

	private static class Hsl {
		float h, s, l;
	}

	/**
	 * Convert the color to HSL space.
	 * 
	 * @return the color in HSL space, or null if the conversion failed.
	 */
	Hsl toHSL() {
		float r, g, b;
		if (red.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			r = ((CSSTypedValue) red).getFloatValue(CSSUnit.CSS_PERCENTAGE) * 0.01f;
		} else if (red.getUnitType() == CSSUnit.CSS_NUMBER) {
			r = ((CSSTypedValue) red).getFloatValue(CSSUnit.CSS_NUMBER);
			r = r / 255f;
		} else {
			return null;
		}
		if (green.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			g = ((CSSTypedValue) green).getFloatValue(CSSUnit.CSS_PERCENTAGE) * 0.01f;
		} else if (green.getUnitType() == CSSUnit.CSS_NUMBER) {
			g = ((CSSTypedValue) green).getFloatValue(CSSUnit.CSS_NUMBER);
			g = g / 255f;
		} else {
			return null;
		}
		if (blue.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			b = ((CSSTypedValue) blue).getFloatValue(CSSUnit.CSS_PERCENTAGE) * 0.01f;
		} else if (blue.getUnitType() == CSSUnit.CSS_NUMBER) {
			b = ((CSSTypedValue) blue).getFloatValue(CSSUnit.CSS_NUMBER);
			b = b / 255f;
		} else {
			return null;
		}
		if (r > 1f || g > 1f || b > 1f) {
			return null;
		}
		float max;
		boolean maxr = false, maxg = false;
		if (g > r) {
			max = g;
			maxg = true;
		} else {
			max = r;
			maxr = true;
		}
		if (b > max) {
			max = b;
			maxr = false;
			maxg = false;
		}
		float min = Math.min(r, g);
		min = Math.min(min, b);
		float h;
		if (max == min) {
			h = 0f;
		} else if (maxr) {
			h = (g - b) / (max - min) * 60f + 360f;
			h = (float) Math.IEEEremainder(h, 360d);
			if (h < 0f) {
				h += 360f;
			}
		} else if (maxg) {
			h = (b - r) / (max - min) * 60f + 120f;
			if (h < 0f) {
				h += 360f;
			}
		} else {
			h = (r - g) / (max - min) * 60f + 240f;
			if (h < 0f) {
				h += 360f;
			}
		}
		float l = (max + min) * 0.5f;
		Hsl hsl = new Hsl();
		if (max != min) {
			if (l <= 0.5f) {
				hsl.s = Math.round((max - min) / l * 500f) * 0.1f;
			} else {
				hsl.s = Math.round((max - min) / (1f - l) * 500f) * 0.1f;
			}
		} else {
			hsl.s = 0;
		}
		hsl.h = Math.round(h * 10f) * 0.1f;
		if (hsl.h >= 360f) {
			hsl.h -= 360f;
		}
		hsl.l = Math.round(l * 1000f) * 0.1f;
		return hsl;
	}

	void toLABColor(LABColorImpl color) throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		double r = rgbComponentNormalized((TypedValue) getRed());
		double g = rgbComponentNormalized((TypedValue) getGreen());
		double b = rgbComponentNormalized((TypedValue) getBlue());
		//
		rgbToLab(r, g, b, getAlpha(), color);
	}

	/**
	 * Normalize a component to a [0,1] interval.
	 * 
	 * @param number the component.
	 * @return the normalized component.
	 */
	double rgbComponentNormalized(TypedValue number) {
		double comp;
		if (number.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			comp = number.getFloatValue(CSSUnit.CSS_PERCENTAGE) * 0.01d;
		} else {
			comp = number.getFloatValue(CSSUnit.CSS_NUMBER) / 255d;
		}
		return comp;
	}

	void rgbToLab(double r, double g, double b, PrimitiveValue alpha, LABColorImpl labColor) {
		float[] lab = new float[3];
		// Convert inverse-companded RGB color to Lab
		ColorUtil.rgbToLab(r, g, b, lab);
		setLabColor(lab, alpha, labColor);
	}

	static double inverseSRGBCompanding(double compandedComponent) {
		// Inverse sRGB Companding
		final double abs = Math.abs(compandedComponent);
		double linearComp;
		if (abs <= 0.04045d) {
			linearComp = compandedComponent / 12.92d;
		} else {
			linearComp = Math.signum(compandedComponent) * Math.pow((abs + 0.055d) / 1.055d, 2.4d);
		}
		return linearComp;
	}

	@Override
	public String toString() {
		return oldFunctionalString(isNonOpaque());
	}

	String oldFunctionalString(boolean nonOpaque) {
		StringBuilder buf = new StringBuilder(25);
		if (nonOpaque) {
			buf.append("rgba(");
		} else {
			buf.append("rgb(");
		}
		appendComponentCssText(buf, getRed()).append(", ");
		appendComponentCssText(buf, getGreen()).append(", ");
		appendComponentCssText(buf, getBlue());
		if (nonOpaque) {
			buf.append(", ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toMinifiedString() {
		return minifiedOldFunctionalString(isNonOpaque());
	}

	String minifiedOldFunctionalString(boolean nonOpaque) {
		StringBuilder buf = new StringBuilder(24);
		if (nonOpaque) {
			buf.append("rgba(");
		} else {
			buf.append("rgb(");
		}
		appendComponentMinifiedCssText(buf, getRed()).append(',');
		appendComponentMinifiedCssText(buf, getGreen()).append(',');
		appendComponentMinifiedCssText(buf, getBlue());
		if (nonOpaque) {
			buf.append(',');
			appendAlphaChannelMinified(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blue == null) ? 0 : colorComponentHashCode(blue));
		result = prime * result + ((green == null) ? 0 : colorComponentHashCode(green));
		result = prime * result + ((red == null) ? 0 : colorComponentHashCode(red));
		result = prime * result + alpha.hashCode();
		return result * prime;
	}

	private int colorComponentHashCode(PrimitiveValue comp) {
		float value;
		if (comp.getPrimitiveType() != Type.NUMERIC) {
			return comp.hashCode();
		}
		TypedValue val = (TypedValue) comp;
		if (comp.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			value = val.getFloatValue(CSSUnit.CSS_PERCENTAGE) * 2.55f;
		} else {
			value = val.getFloatValue(CSSUnit.CSS_NUMBER);
		}
		return Float.floatToIntBits(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RGBColor)) {
			return false;
		}
		RGBColor other = (RGBColor) obj;
		if (blue == null) {
			if (other.blue != null) {
				return false;
			}
		} else if (!blue.equals(other.blue)) {
			return false;
		}
		if (green == null) {
			if (other.green != null) {
				return false;
			}
		} else if (!green.equals(other.green)) {
			return false;
		}
		if (red == null) {
			if (other.red != null) {
				return false;
			}
		} else if (!red.equals(other.red)) {
			return false;
		}
		return Objects.equals(alpha, other.alpha);
	}

	@Override
	public BaseColor clone() {
		return new RGBColor(this);
	}

}
