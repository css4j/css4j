/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
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
	@Override
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

	public void setRed(PrimitiveValue red) throws DOMException {
		if (red == null) {
			throw new NullPointerException();
		}
		this.red = enforceColorComponentType(red);
	}

	PrimitiveValue enforceColorComponentType(PrimitiveValue primi) throws DOMException {
		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) primi);
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) primi);
		}

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
		} else if (primi.getCssValueType() != CssType.PROXY
				&& primi.getPrimitiveType() != Type.EXPRESSION
				&& primi.getPrimitiveType() != Type.MATH_FUNCTION
				&& (primi.getPrimitiveType() != Type.IDENT
						|| !"none".equalsIgnoreCase(((TypedValue) primi).getStringValue()))) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible with color component.");
		}

		return primi;
	}

	@Override
	public PrimitiveValue getRed() {
		return red;
	}

	public void setGreen(PrimitiveValue green) throws DOMException {
		if (green == null) {
			throw new NullPointerException();
		}
		this.green = enforceColorComponentType(green);
	}

	@Override
	public PrimitiveValue getGreen() {
		return green;
	}

	public void setBlue(PrimitiveValue blue) throws DOMException {
		if (blue == null) {
			throw new NullPointerException();
		}
		this.blue = enforceColorComponentType(blue);
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

	@Override
	public double[] toNumberArray() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		double[] rgb = new double[3];
		rgb[0] = rgbComponentNormalized((TypedValue) getRed());
		rgb[1] = rgbComponentNormalized((TypedValue) getGreen());
		rgb[2] = rgbComponentNormalized((TypedValue) getBlue());
		return rgb;
	}

	@Override
	void setColorComponents(double[] rgb) {
		PercentageValue red = new PercentageValue();
		red.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) (rgb[0] * 100d));
		red.setSubproperty(true);
		red.setAbsolutizedUnit();
		setRed(red);

		PercentageValue green = new PercentageValue();
		green.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) (rgb[1] * 100d));
		green.setSubproperty(true);
		green.setAbsolutizedUnit();
		setGreen(green);

		PercentageValue blue = new PercentageValue();
		blue.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) (rgb[2] * 100d));
		blue.setSubproperty(true);
		blue.setAbsolutizedUnit();
		setBlue(blue);
	}

	@Override
	double[] toSRGB(boolean clamp) throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		double[] rgb = new double[3];
		rgb[0] = rgbComponentNormalized((TypedValue) getRed());
		rgb[1] = rgbComponentNormalized((TypedValue) getGreen());
		rgb[2] = rgbComponentNormalized((TypedValue) getBlue());
		return rgb;
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		double[] rgb = toSRGB(true);
		double r = RGBColor.inverseSRGBCompanding(rgb[0]);
		double g = RGBColor.inverseSRGBCompanding(rgb[1]);
		double b = RGBColor.inverseSRGBCompanding(rgb[2]);

		double[] xyz = ColorUtil.linearSRGBToXYZd65(r, g, b);
		if (white == Illuminant.D50) {
			xyz = ColorUtil.d65xyzToD50(xyz);
		}
		return xyz;
	}

	public HSLColor toHSLColor() {
		HSLColorImpl hslColor = createHSLColor();
		toHSLColor(hslColor);
		return hslColor;
	}

	HSLColorImpl createHSLColor() {
		return new HSLColorImpl();
	}

	void toHSLColor(HSLColorImpl hslColor) throws DOMException {
		double[] hsl = toHSL();
		if (hsl == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Conversion to hsl() failed.");
		}
		NumberValue h = new NumberValue();
		h.setFloatValue(CSSUnit.CSS_DEG, (float) hsl[0]);
		h.setAbsolutizedUnit();
		PercentageValue s = new PercentageValue();
		s.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) hsl[1]);
		s.setAbsolutizedUnit();
		PercentageValue l = new PercentageValue();
		l.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) hsl[2]);
		l.setAbsolutizedUnit();
		hslColor.setHue(h);
		hslColor.setSaturation(s);
		hslColor.setLightness(l);
		hslColor.setAlpha(getAlpha());
	}

	/**
	 * Convert the color to HSL space.
	 * 
	 * @return the color in HSL space, or null if the conversion failed.
	 */
	double[] toHSL() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		double r, g, b;
		r = rgbComponentNormalized((TypedValue) red);
		g = rgbComponentNormalized((TypedValue) green);
		b = rgbComponentNormalized((TypedValue) blue);

		if (r > 1f || g > 1f || b > 1f) {
			return null;
		}

		return ColorUtil.srgbToHsl(r, g, b);
	}

	/**
	 * Normalize a component to a [0,1] interval.
	 * 
	 * @param typed the component.
	 * @return the normalized component.
	 */
	double rgbComponentNormalized(TypedValue typed) throws DOMException {
		double comp;
		short unit = typed.getUnitType();
		if (unit == CSSUnit.CSS_PERCENTAGE) {
			comp = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE) * 0.01d;
		} else if (unit == CSSUnit.CSS_NUMBER) {
			comp = typed.getFloatValue(CSSUnit.CSS_NUMBER) / 255d;
		} else if (typed.getPrimitiveType() == Type.IDENT) {
			comp = 0d;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Wrong component: " + typed.getCssText());
		}
		return comp;
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
	public ColorValue packInValue() {
		return new RGBColorValue(this);
	}

	@Override
	public RGBColor clone() {
		return new RGBColor(this);
	}

}
