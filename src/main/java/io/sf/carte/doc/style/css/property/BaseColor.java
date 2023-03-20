/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.property.ColorProfile.Illuminant;
import io.sf.carte.util.SimpleWriter;

abstract class BaseColor implements CSSColor, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 3L;

	enum Space {
		sRGB, p3, A98_RGB, ProPhoto_RGB, Rec2020, CIE_XYZ, CIE_XYZ_D50, CIE_Lab, CIE_LCh, OK_Lab,
		OK_LCh, OTHER
	}

	// ASTM E308-01 via http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
	static final double[] whiteD65 = { 0.95047d, 1d, 1.08883d };
	static final double[] whiteD50 = { 0.96422d, 1d, 0.82521d };

	PrimitiveValue alpha = ColorValue.opaqueAlpha;

	BaseColor() {
		super();
	}

	@Override
	public String getColorSpace() {
		return ColorSpace.srgb;
	}

	Space getSpace() {
		return Space.sRGB;
	}

	@Override
	public PrimitiveValue getAlpha() {
		return alpha;
	}

	public void setAlpha(PrimitiveValue alpha) {
		if (alpha == null) {
			throw new NullPointerException();
		}

		if (alpha.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				alpha = eval.evaluateExpression((ExpressionValue) alpha);
				if (alpha.getPrimitiveType() == Type.NUMERIC) {
					((NumberValue) alpha).setMaxFractionDigits(5);
				}
			} catch (DOMException e) {
			}
		} else if (alpha.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				alpha = eval.evaluateFunction((CSSMathFunctionValue) alpha);
				if (alpha.getPrimitiveType() == Type.NUMERIC) {
					((NumberValue) alpha).setMaxFractionDigits(5);
				}
			} catch (DOMException e) {
			}
		}

		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			TypedValue typed = (TypedValue) alpha;
			float fv = typed.getFloatValue(CSSUnit.CSS_NUMBER);
			if (fv < 0f) {
				// Instantiate a percentage, to enable number-% conversions
				typed = new PercentageValue();
				typed.setFloatValue(CSSUnit.CSS_NUMBER, 0f);
				typed.setSubproperty(true);
				alpha = typed;
			} else if (fv > 1f) {
				typed = new PercentageValue();
				typed.setFloatValue(CSSUnit.CSS_NUMBER, 1f);
				typed.setSubproperty(true);
				alpha = typed;
			}
		} else if (alpha.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			TypedValue typed = (TypedValue) alpha;
			float fv = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			if (fv < 0f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 0f);
				typed.setSubproperty(true);
				alpha = typed;
			} else if (fv > 100f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 100f);
				typed.setSubproperty(true);
				alpha = typed;
			}
		} else if (alpha.getCssValueType() != CssType.PROXY
				&& alpha.getPrimitiveType() != Type.EXPRESSION
				&& alpha.getPrimitiveType() != Type.MATH_FUNCTION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible with alpha.");
		}

		this.alpha = alpha;
	}

	static PrimitiveValue enforcePcntComponent(PrimitiveValue primi) throws DOMException {
		if (primi == null) {
			throw new NullPointerException();
		}

		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				primi = eval.evaluateExpression((ExpressionValue) primi);
			} catch (DOMException e) {
			}
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				primi = eval.evaluateFunction((CSSMathFunctionValue) primi);
			} catch (DOMException e) {
			}
		}

		if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			TypedValue typed = (TypedValue) primi;
			float fv = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			if (fv < 0f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 0f);
				typed.setSubproperty(true);
				primi = typed;
			} else if (fv > 100f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 100f);
				typed.setSubproperty(true);
				primi = typed;
			}
		} else if (primi.getCssValueType() != CssType.PROXY
				&& primi.getPrimitiveType() != Type.EXPRESSION
				&& primi.getPrimitiveType() != Type.MATH_FUNCTION
				&& (primi.getPrimitiveType() != Type.IDENT
						|| !"none".equalsIgnoreCase(((TypedValue) primi).getStringValue()))) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Invalid color component: " + primi.getCssText());
		}

		return primi;
	}

	static PrimitiveValue enforcePcntOrNumberComponent(PrimitiveValue primi) throws DOMException {
		if (primi == null) {
			throw new NullPointerException();
		}

		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				primi = eval.evaluateExpression((ExpressionValue) primi);
			} catch (DOMException e) {
			}
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				primi = eval.evaluateFunction((CSSMathFunctionValue) primi);
			} catch (DOMException e) {
			}
		}

		if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			TypedValue typed = (TypedValue) primi;
			float fv = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			if (fv < 0f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 0f);
				typed.setSubproperty(true);
				primi = typed;
			} else if (fv > 100f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 100f);
				typed.setSubproperty(true);
				primi = typed;
			}
		} else if (primi.getUnitType() == CSSUnit.CSS_NUMBER) {
			TypedValue typed = (TypedValue) primi;
			float fv = typed.getFloatValue(CSSUnit.CSS_NUMBER);
			if (fv < 0f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
				typed.setSubproperty(true);
				primi = typed;
			} else if (fv > 100f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 100f);
				typed.setSubproperty(true);
				primi = typed;
			}
		} else if (primi.getCssValueType() != CssType.PROXY
				&& primi.getPrimitiveType() != Type.EXPRESSION
				&& primi.getPrimitiveType() != Type.MATH_FUNCTION
				&& (primi.getPrimitiveType() != Type.IDENT
						|| !"none".equalsIgnoreCase(((TypedValue) primi).getStringValue()))) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Invalid color component: " + primi.getCssText());
		}

		return primi;
	}

	static PrimitiveValue enforceHueComponent(PrimitiveValue hue) {
		if (hue == null) {
			throw new NullPointerException();
		}

		if (hue.getPrimitiveType() == Type.EXPRESSION) {
			Evaluator eval = new Evaluator(CSSUnit.CSS_DEG);
			try {
				hue = eval.evaluateExpression((ExpressionValue) hue);
			} catch (DOMException e) {
			}
		} else if (hue.getPrimitiveType() == Type.MATH_FUNCTION) {
			Evaluator eval = new Evaluator(CSSUnit.CSS_DEG);
			try {
				hue = eval.evaluateFunction((CSSMathFunctionValue) hue);
			} catch (DOMException e) {
			}
		}

		if (hue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(hue.getUnitType())
				&& hue.getCssValueType() != CssType.PROXY
				&& hue.getPrimitiveType() != Type.EXPRESSION
				&& hue.getPrimitiveType() != Type.MATH_FUNCTION
				&& (hue.getPrimitiveType() != Type.IDENT
						|| !"none".equalsIgnoreCase(((TypedValue) hue).getStringValue()))) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with hue.");
		}

		return hue;
	}

	static PrimitiveValue normalizePcntToNumber(PrimitiveValue primi, float factor,
			int maxFractionDigits, boolean specified) {
		if (primi == null) {
			throw new NullPointerException();
		}

		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				primi = eval.evaluateExpression((ExpressionValue) primi);
			} catch (DOMException e) {
			}
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				primi = eval.evaluateFunction((CSSMathFunctionValue) primi);
			} catch (DOMException e) {
			}
		}

		if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			NumberValue number = (NumberValue) primi;
			boolean spec = number.isSpecified() && specified;
			float num = number.getFloatValue(CSSUnit.CSS_PERCENTAGE) * factor;
			// Instantiate a percentage, to enable number-% conversions
			number = new PercentageValue();
			number.setFloatValue(CSSUnit.CSS_NUMBER, num);
			number.setSubproperty(true);
			number.setSpecified(spec);
			number.setMaxFractionDigits(maxFractionDigits);
			primi = number;
		} else if (primi.getUnitType() != CSSUnit.CSS_NUMBER
				&& primi.getCssValueType() != CssType.PROXY
				&& primi.getPrimitiveType() != Type.EXPRESSION
				&& primi.getPrimitiveType() != Type.MATH_FUNCTION
				&& (primi.getPrimitiveType() != Type.IDENT
						|| !"none".equalsIgnoreCase(((TypedValue) primi).getStringValue()))) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible: " + primi.getCssText());
		}

		return primi;
	}

	abstract boolean hasConvertibleComponents();

	static boolean isConvertibleComponent(CSSPrimitiveValue comp) {
		return comp != null && (comp.getPrimitiveType() == Type.NUMERIC
				|| (comp.getPrimitiveType() == Type.IDENT
						&& "none".equalsIgnoreCase(((CSSTypedValue) comp).getStringValue())));
	}

	@Override
	public String toString() {
		int len = getLength();
		StringBuilder buf = new StringBuilder();
		buf.append("color(").append(getColorSpace());
		for (int i = 1; i < len; i++) {
			buf.append(' ');
			appendComponentCssText(buf, item(i));
		}
		// Alpha channel
		if (isNonOpaque()) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		//
		buf.append(')');
		return buf.toString();
	}

	StringBuilder appendComponentCssText(StringBuilder buf, PrimitiveValue component) {
		return buf.append(component.getCssText());
	}

	@Override
	public String toMinifiedString() {
		int len = getLength();
		StringBuilder buf = new StringBuilder();
		buf.append("color(").append(getColorSpace());
		for (int i = 1; i < len; i++) {
			buf.append(' ');
			appendComponentMinifiedCssText(buf, item(i));
		}
		// Alpha channel
		if (isNonOpaque()) {
			buf.append('/');
			appendAlphaChannelMinified(buf);
		}
		//
		buf.append(')');
		return buf.toString();
	}

	StringBuilder appendComponentMinifiedCssText(StringBuilder buf, PrimitiveValue component) {
		return buf.append(component.getMinifiedCssText(""));
	}

	/**
	 * Is this value non-opaque?
	 * 
	 * @return {@code true} if the alpha channel is non-opaque or not a number.
	 */
	boolean isNonOpaque() {
		return alpha.getPrimitiveType() != Type.NUMERIC
				|| ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER) != 1f;
	}

	void appendAlphaChannel(SimpleWriter wri) throws IOException {
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			String text = formattedNumber((TypedValue) alpha);
			wri.write(text);
		} else {
			alpha.writeCssText(wri);
		}
	}

	StringBuilder appendAlphaChannel(StringBuilder buf) {
		String text;
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER && !((NumberValue) alpha).isSpecified()) {
			text = formattedNumber((TypedValue) alpha);
		} else {
			text = alpha.getCssText();
		}
		return buf.append(text);
	}

	StringBuilder appendAlphaChannelMinified(StringBuilder buf) {
		String text;
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER && !((NumberValue) alpha).isSpecified()) {
			text = formattedNumberMinified((TypedValue) alpha);
		} else {
			text = alpha.getMinifiedCssText("");
		}
		return buf.append(text);
	}

	private String formattedNumber(TypedValue alpha) {
		float f = alpha.getFloatValue(CSSUnit.CSS_NUMBER);
		NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
		format.setMaximumFractionDigits(4);
		format.setMinimumFractionDigits(0);
		return format.format(f);
	}

	private String formattedNumberMinified(TypedValue alpha) {
		float f = alpha.getFloatValue(CSSUnit.CSS_NUMBER);
		NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
		format.setMaximumFractionDigits(4);
		format.setMinimumFractionDigits(0);
		format.setMinimumIntegerDigits(0);
		return format.format(f);
	}

	void appendHue(StringBuilder buf, PrimitiveValue hue) {
		short unit = hue.getUnitType();
		if (unit == CSSUnit.CSS_DEG) {
			NumberValue deg = (NumberValue) hue;
			float val = deg.getFloatValue(unit);
			NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, val);
			if (!deg.isSpecified()) {
				number.setAbsolutizedUnit();
			}
			String s = number.getCssText();
			buf.append(s);
		} else {
			buf.append(hue.getCssText());
		}
	}

	void writeHue(SimpleWriter wri, PrimitiveValue hue) throws IOException {
		short unit = hue.getUnitType();
		if (unit == CSSUnit.CSS_DEG) {
			NumberValue deg = (NumberValue) hue;
			float val = deg.getFloatValue(unit);
			NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, val);
			if (!deg.isSpecified()) {
				number.setAbsolutizedUnit();
			}
			number.writeCssText(wri);
		} else {
			hue.writeCssText(wri);
		}
	}

	void appendMinifiedHue(StringBuilder buf, PrimitiveValue hue) {
		short unit = hue.getUnitType();
		if (unit == CSSUnit.CSS_DEG) {
			NumberValue deg = (NumberValue) hue;
			float val = deg.getFloatValue(unit);
			NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, val);
			if (!deg.isSpecified()) {
				number.setAbsolutizedUnit();
			}
			String s = number.getMinifiedCssText("");
			buf.append(s);
		} else {
			buf.append(hue.getMinifiedCssText(""));
		}
	}

	void set(BaseColor color) {
		this.alpha = color.alpha;
	}

	static void setLabColor(double[] lab, PrimitiveValue alpha, LABColorImpl labColor) {
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE,
				(float) lab[0]);
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[1]);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[2]);
		//
		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();
		//
		labColor.setLightness(primiL);
		labColor.setA(primia);
		labColor.setB(primib);
		labColor.setAlpha(alpha.clone());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = Objects.hash(getColorSpace().toLowerCase(Locale.ROOT));
		result = prime * result + Objects.hash(getColorModel());
		result = prime * result + Objects.hash(alpha);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(this instanceof BaseColor)) {
			return false;
		}
		BaseColor other = (BaseColor) obj;
		return Objects.equals(alpha, other.alpha) && getColorSpace().equals(other.getColorSpace())
				&& Objects.equals(getColorModel(), other.getColorModel());
	}

	@Override
	abstract public CSSColorValue.ColorModel getColorModel();

	@Override
	abstract public PrimitiveValue item(int index);

	/**
	 * The number of component values plus the alpha channel.
	 * 
	 * @return the number of component values plus the alpha channel.
	 */
	@Override
	public int getLength() {
		return 4;
	}

	/**
	 * Set the component of this color located at {@code index}.
	 * 
	 * @param index the index.
	 * @param component the component value.
	 */
	abstract void setComponent(int index, PrimitiveValue component);

	abstract void setColorComponents(double[] comp);

	abstract double[] toArray();

	double[] toXYZ(Illuminant white) {
		double[] rgb = toSRGB(true);

		double[] xyz = ColorUtil.srgbToXYZd65(rgb[0], rgb[1], rgb[2]);

		if (white == Illuminant.D50) {
			xyz = ColorUtil.d65xyzToD50(xyz);
		}

		return xyz;
	}

	void toLABColor(LABColorImpl color) throws DOMException {
		double[] xyz = toXYZ(Illuminant.D50);
		double[] lab = new double[3];
		ColorUtil.xyzD50ToLab(xyz, lab);

		setLabColor(lab, getAlpha(), color);
	}

	@Override
	public BaseColor toColorSpace(String colorSpace) throws DOMException {
		ColorConverter converter = new ColorConverter(true);
		converter.toColorSpace(this, colorSpace, true);
		return converter.getLastColor();
	}

	abstract double[] toSRGB(boolean clamp);

	@Override
	abstract public ColorValue packInValue();

	@Override
	abstract public BaseColor clone();

}
