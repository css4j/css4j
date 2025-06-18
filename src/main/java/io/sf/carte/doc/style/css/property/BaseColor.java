/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.util.SimpleWriter;
import io.sf.jclf.math.linear3.Matrices;

abstract class BaseColor implements CSSColor, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 4L;

	enum Space {
		sRGB, Linear_sRGB, p3, A98_RGB, ProPhoto_RGB, Rec2020, CIE_XYZ, CIE_XYZ_D50, CIE_Lab,
		CIE_LCh, OK_Lab, OK_LCh, OTHER
	}

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
				alpha = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) alpha);
				if (alpha.getPrimitiveType() == Type.NUMERIC) {
					((NumberValue) alpha).setMaximumFractionDigits(5);
				}
			} catch (DOMException e) {
			}
		} else if (alpha.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			try {
				alpha = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) alpha);
				if (alpha.getPrimitiveType() == Type.NUMERIC) {
					((NumberValue) alpha).setMaximumFractionDigits(5);
				}
			} catch (DOMException e) {
			}
		}

		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			TypedValue typed = (TypedValue) alpha;
			float fv = typed.getFloatValue();
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
			float fv = typed.getFloatValue();
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
				&& alpha.getPrimitiveType() != Type.MATH_FUNCTION
				&& (alpha.getPrimitiveType() != Type.IDENT
						|| !"none".equalsIgnoreCase(((TypedValue) alpha).getStringValue()))) {
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
			primi = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) primi);
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) primi);
		}

		short unit = primi.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			TypedValue typed = (TypedValue) primi;
			float fv = typed.getFloatValue();
			NumberValue num = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, fv);
			num.setSpecified(false);
			num.setMaximumFractionDigits(4);
			primi = num;
			unit = CSSUnit.CSS_PERCENTAGE;
		}
		if (unit == CSSUnit.CSS_PERCENTAGE) {
			TypedValue typed = (TypedValue) primi;
			float fv = typed.getFloatValue();
			if (fv < 0f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 0f);
				typed.setSubproperty(true);
				primi = typed;
			} else if (fv > 100f) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 100f);
				typed.setSubproperty(true);
				primi = typed;
			}
		} else if (isInvalidComponentType(primi)) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Invalid color component: " + primi.getCssText());
		}

		return primi;
	}

	static boolean isInvalidComponentType(CSSPrimitiveValue primi) {
		switch (primi.getCssValueType()) {
		case TYPED:
			switch (primi.getPrimitiveType()) {
			case EXPRESSION:
			case MATH_FUNCTION:
			case FUNCTION: // Possibly an unimplemented math function
				return false;
			case IDENT:
				// Component names are supposed to have been replaced before this
				return !"none".equalsIgnoreCase(((CSSTypedValue) primi).getStringValue());
			default:
			}
			break;
		case PROXY: // env()
			return false;
		default:
		}
		return true;
	}

	PrimitiveValue enforcePcntOrNumberComponent(PrimitiveValue primi) throws DOMException {
		if (primi == null) {
			throw new NullPointerException();
		}

		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) primi);
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) primi);
		}

		if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			TypedValue typed = (TypedValue) primi;
			float fv = typed.getFloatValue();
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
			NumberValue number = (NumberValue) primi;
			float fv = number.getFloatValue();
			number = number.clone();
			if (hasPercentageComponent()) {
				number.setUnitType(CSSUnit.CSS_PERCENTAGE);
			}
			if (fv < 0f) {
				number.realvalue = 0f;
			} else if (fv > 100f) {
				number.realvalue = 100f;
			}
			number.setMaximumFractionDigits(4);
			number.setSubproperty(true);
			primi = number;
		} else if (isInvalidComponentType(primi)) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Invalid color component: " + primi.getCssText());
		}

		return primi;
	}

	boolean hasPercentageComponent() {
		int len = getLength();
		for (int i = 1; i < len; i++) {
			PrimitiveValue comp = item(i);
			if (comp != null && comp.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
				return true;
			}
		}
		return false;
	}

	static PrimitiveValue enforceHueComponent(PrimitiveValue hue) {
		if (hue == null) {
			throw new NullPointerException();
		}

		if (hue.getPrimitiveType() == Type.EXPRESSION) {
			Evaluator eval = new Evaluator(CSSUnit.CSS_DEG);
			try {
				hue = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) hue);
			} catch (DOMException e) {
			}
		} else if (hue.getPrimitiveType() == Type.MATH_FUNCTION) {
			Evaluator eval = new Evaluator(CSSUnit.CSS_DEG);
			try {
				hue = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) hue);
			} catch (DOMException e) {
			}
		}

		if (isInvalidHueType(hue)) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with hue.");
		}

		return hue;
	}

	static boolean isInvalidHueType(CSSPrimitiveValue hue) {
		if (hue.getUnitType() == CSSUnit.CSS_NUMBER || CSSUnit.isAngleUnitType(hue.getUnitType())) {
			return false;
		}
		switch (hue.getCssValueType()) {
		case TYPED:
			switch (hue.getPrimitiveType()) {
			case EXPRESSION:
			case MATH_FUNCTION:
			case FUNCTION: // Possibly an unimplemented math function
				return false;
			case IDENT:
				return !"none".equalsIgnoreCase(((CSSTypedValue) hue).getStringValue());
			default:
			}
			break;
		case PROXY: // env()
			return false;
		default:
		}
		return true;
	}

	static PrimitiveValue normalizePcntToNumber(PrimitiveValue primi, float factor,
			int maxFractionDigits, boolean specified) throws DOMException {
		if (primi == null) {
			throw new NullPointerException();
		}

		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) primi);
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) primi);
		}

		if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			NumberValue number = (NumberValue) primi;
			boolean spec = number.isSpecified() && specified;
			float num = number.getFloatValue() * factor;
			// Instantiate a percentage, to enable number-% conversions
			number = new PercentageValue();
			number.setFloatValue(CSSUnit.CSS_NUMBER, num);
			number.setSubproperty(true);
			number.setSpecified(spec);
			number.setMaximumFractionDigits(maxFractionDigits);
			primi = number;
		} else if (primi.getUnitType() != CSSUnit.CSS_NUMBER && isInvalidComponentType(primi)) {
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

	/*
	 * Utility methods for relative colors.
	 */

	NumberValue numberComponent(CSSTypedValue typed, float pcntDiv) throws DOMException {
		float value;
		boolean specified;
		short unit = typed.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			value = typed.getFloatValue();
			specified = ((NumberValue) typed).isSpecified();
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			value = typed.getFloatValue() / pcntDiv;
			specified = ((NumberValue) typed).isSpecified();
		} else if (typed.getPrimitiveType() == Type.IDENT) {
			value = 0f;
			specified = true;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Wrong component: " + typed.getCssText());
		}
		NumberValue num = new NumberValue();
		num.realvalue = value;
		num.setSpecified(specified);
		num.setMaximumFractionDigits(getMaximumFractionDigits());
		return num;
	}

	static NumberValue hueComponent(CSSTypedValue typed) throws DOMException {
		double h = ColorUtil.hueDegrees(typed);
		boolean specified = typed.getPrimitiveType() != Type.NUMERIC
				|| ((NumberValue) typed).isSpecified();
		NumberValue num = new NumberValue();
		num.setFloatValue(CSSUnit.CSS_DEG, (float) h);
		num.setSpecified(specified);
		num.setMaximumFractionDigits(4);
		return num;
	}

	int getMaximumFractionDigits() {
		return 4;
	}

	/*
	 * End of utility methods for relative colors.
	 */

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

		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();

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
	abstract public NumberValue component(String component);

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

	/**
	 * Convert the color components to a normalized numeric form and put them in an
	 * array.
	 * <p>
	 * The array does not include the alpha channel.
	 * </p>
	 * <p>
	 * For RGB colors, the components are in the [0,1] interval.
	 * </p>
	 * 
	 * @return the array with the non-alpha normalized color components.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted
	 *                      to numbers.
	 */
	@Override
	abstract public double[] toNumberArray() throws DOMException;

	/**
	 * Convert this color to the XYZ space using the given reference illuminant.
	 * 
	 * @param white the standard illuminant. If you need a conversion for an
	 *              illuminant not included in the {@link Illuminant} enumeration,
	 *              please use {@link #toXYZ(double[])}.
	 * @return the color expressed in XYZ coordinates with the given white point.
	 */
	@Override
	public double[] toXYZ(Illuminant white) {
		// Default implementation valid for sRGB colors
		double[] rgb = toSRGB(true);

		double[] xyz = ColorUtil.srgbToXYZd65(rgb[0], rgb[1], rgb[2]);

		if (white == Illuminant.D50) {
			xyz = ColorUtil.d65xyzToD50(xyz);
		}

		return xyz;
	}

	/**
	 * Convert this color to the XYZ space using the given reference white.
	 * <p>
	 * If your white happens to be D65 or D50, please use {@link #toXYZ(Illuminant)}
	 * instead which is faster.
	 * </p>
	 * 
	 * @param white the white point tristimulus value, normalized so the {@code Y}
	 *              component is always {@code 1}.
	 * @return the color expressed in XYZ coordinates with the given white point.
	 */
	@Override
	public double[] toXYZ(double[] white) {
		// Default implementation valid for sRGB colors
		double[] rgb = toSRGB(true);

		double[] xyz = ColorUtil.srgbToXYZd65(rgb[0], rgb[1], rgb[2]);

		if (!Arrays.equals(Illuminants.whiteD65, white)) {
			double[][] cam = new double[3][3];
			ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD65, white, cam);
			double[] result = new double[3];
			Matrices.multiplyByVector3(cam, xyz, result);
			xyz = result;
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

	/**
	 * Is this color inside the given gamut?
	 * 
	 * @param colorSpace the color gamut to check. It can be one of the
	 *                   {@link ColorSpace} constants, as well as {@code hsl},
	 *                   {@code hsla}, {@code hwb} and {@code rgb}, which are used
	 *                   as synonyms for {@code srgb}.
	 * @return {@code true} if this color is in the gamut of the given color space.
	 * @throws DOMException NOT_SUPPORTED_ERR if the color space is not supported,
	 *                      INVALID_STATE_ERR if the color components have to be
	 *                      converted to typed values.
	 */
	@Override
	public boolean isInGamut(String colorSpace) {
		colorSpace = colorSpace.toLowerCase(Locale.ROOT);
		if (getColorSpace().equals(colorSpace) || getSpace() == Space.sRGB || getSpace() == Space.Linear_sRGB) {
			return true;
		}

		switch (colorSpace) {
		case ColorSpace.xyz:
		case "xyz-d65":
		case ColorSpace.xyz_d50:
		case ColorSpace.cie_lab:
		case ColorSpace.cie_lch:
		case ColorSpace.ok_lab:
		case ColorSpace.ok_lch:
			return true;
		case ColorSpace.srgb:
		case "hsl":
		case "hsla":
		case "hwb":
		case "rgb":
			colorSpace = ColorSpace.srgb_linear;
		default:
		}

		ColorConverter converter = new ColorConverter(false);
		double[] comp = converter.toColorSpace(this, colorSpace, false);

		/*
		 * Check whether the value is in bounds within the margins
		 * used by ColorUtil.rangeRoundCheck().
		 */
		for (double c : comp) {
			if (c <= -1e-4 || c >= 1.0001) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Compute the difference to the given color, according to &#x394;EOK.
	 * <p>
	 * If the delta is inferior to {@code 0.02}, it is considered that the two
	 * colors are difficult to distinguish.
	 * </p>
	 * 
	 * @param color the color to compute the delta from.
	 * @return the &#x394;EOK.
	 */
	@Override
	public float deltaEOK(CSSColor color) {
		// Convert to oklab
		double[] ok1 = toOKLab(this);
		double[] ok2 = toOKLab((BaseColor) color);

		return (float) ColorUtil.deltaEokLab(ok1, ok2);
	}

	private static double[] toOKLab(BaseColor color) {
		// Convert to oklab
		if (color.getSpace() == Space.OK_Lab) {
			return color.toNumberArray();
		}
		double[] result = new double[3];
		if (color.getSpace() == Space.OK_LCh) {
			// LCh to Lab
			double[] lch = color.toNumberArray();
			ColorUtil.lchToLab(lch, result);
		} else {
			double[] xyz = color.toXYZ(Illuminant.D65);
			ColorUtil.xyzD65ToOkLab(xyz, result);
		}

		return result;
	}

	abstract double[] toSRGB(boolean clamp);

	@Override
	abstract public ColorValue packInValue();

	@Override
	abstract public BaseColor clone();

}
