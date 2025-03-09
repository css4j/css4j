/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorMixFunction;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.BaseColor.Space;
import io.sf.carte.doc.style.css.property.ColorSpaceHelper.HueInterpolationMethod;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * {@code color-mix()} function.
 */
class ColorMixFunction extends ColorValue implements CSSColorMixFunction {

	private static final long serialVersionUID = 1L;

	private ColorModel inColorModel;

	private String inColorSpace;

	private HueInterpolationMethod inMethod;

	private PrimitiveValue unknownMethod = null;

	private int hueIndex = -1;

	private PrimitiveValue colorValue1;

	private PrimitiveValue percent1 = null;

	private PrimitiveValue colorValue2;

	private PrimitiveValue percent2 = null;

	private BaseColor color;

	ColorMixFunction() {
		super(Type.COLOR_MIX);
	}

	ColorMixFunction(ColorMixFunction copied) {
		super(copied);
		this.inColorSpace = copied.inColorSpace;
		this.inMethod = copied.inMethod;
		this.unknownMethod = copied.unknownMethod;
		this.colorValue1 = copied.colorValue1.clone();
		this.colorValue2 = copied.colorValue2.clone();
		if (copied.percent1 != null) {
			this.percent1 = copied.percent1.clone();
		}
		if (copied.percent2 != null) {
			this.percent2 = copied.percent2.clone();
		}
		setColorModelSpace(inColorSpace);
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		//
		ColorMixFunction setfrom = (ColorMixFunction) value;
		this.inColorSpace = setfrom.inColorSpace;
		this.inColorModel = setfrom.inColorModel;
		this.inMethod = setfrom.inMethod;
		this.unknownMethod = setfrom.unknownMethod;
		this.colorValue1 = setfrom.colorValue1;
		this.colorValue2 = setfrom.colorValue2;
		this.percent1 = setfrom.percent1;
		this.percent2 = setfrom.percent2;
	}

	@Override
	public ColorModel getColorModel() {
		return inColorModel;
	}

	@Override
	public String getCSSColorSpace() {
		return inColorSpace;
	}

	@Override
	public PrimitiveValue getColorValue1() {
		return colorValue1;
	}

	/**
	 * Set the first color value in the mix.
	 * 
	 * @param colorValue the color.
	 */
	public void setColorValue1(PrimitiveValue colorValue) {
		checkColorValidity(colorValue);
		this.colorValue1 = colorValue;
		mixColors();
	}

	@Override
	public PrimitiveValue getColorValue2() {
		return colorValue2;
	}

	/**
	 * Set the second color value in the mix.
	 * 
	 * @param colorValue the color.
	 */
	public void setColorValue2(PrimitiveValue colorValue) {
		checkColorValidity(colorValue);
		this.colorValue2 = colorValue;
		mixColors();
	}

	private static void checkColorValidity(PrimitiveValue primi) {
		if (!isValidColor(primi)) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Unsupported value: " + primi.getCssText());
		}
	}

	private static boolean isValidColor(PrimitiveValue primi) {
		// Relative color syntaxes are not valid
		Type pType = primi.getPrimitiveType();
		return pType == Type.COLOR || primi.getCssValueType() == CssType.PROXY
				|| (pType == Type.IDENT && ColorIdentifiers.getInstance()
						.isColorIdentifier(((TypedValue) primi).getStringValue()));
	}

	/**
	 * Gives the percentage that applies to the first color.
	 * 
	 * @return the percentage or {@code null} if no percentage was explicitly set.
	 */
	@Override
	public PrimitiveValue getPercentage1() {
		return percent1;
	}

	/**
	 * Sets the percentage that applies to the first color.
	 * 
	 * @param percent1 the percentage to set
	 */
	public void setPercentage1(PrimitiveValue percent1) {
		this.percent1 = normalizePercent(percent1);
	}

	/**
	 * Gives the percentage that applies to the second color.
	 * 
	 * @return the percentage or {@code null} if no percentage was explicitly set.
	 */
	@Override
	public PrimitiveValue getPercentage2() {
		return percent2;
	}

	/**
	 * Sets the percentage that applies to the second color.
	 * 
	 * @param percent2 the percent2 to set
	 * @throws DOMException if the value is not a valid percentage.
	 */
	public void setPercentage2(PrimitiveValue percent2) throws DOMException {
		this.percent2 = normalizePercent(percent2);
	}

	private static PrimitiveValue normalizePercent(PrimitiveValue primi) {
		if (primi == null) {
			return null;
		}

		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) primi);
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) primi);
		}

		if (primi.getUnitType() != CSSUnit.CSS_PERCENTAGE
				&& primi.getPrimitiveType() != Type.EXPRESSION
				&& primi.getPrimitiveType() != Type.MATH_FUNCTION
				&& primi.getPrimitiveType() != Type.FUNCTION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible: " + primi.getCssText());
		}

		return primi;
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter wri = new BufferSimpleWriter();
		try {
			writeCssText(wri);
		} catch (IOException e) {
		}
		return wri.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder();
		buf.append("color-mix(in ");
		buf.append(getCSSColorSpace());

		switch (inMethod) {
		case LONGER:
			buf.append(" longer hue");
			break;
		case INCREASING:
			buf.append(" increasing hue");
			break;
		case DECREASING:
			buf.append(" decreasing hue");
			break;
		case UNKNOWN:
			buf.append(' ').append(unknownMethod).append(" hue");
			break;
		default:
		}

		buf.append(',');
		buf.append(colorValue1.getMinifiedCssText(propertyName));
		if (percent1 != null) {
			buf.append(' ');
			buf.append(percent1.getMinifiedCssText(propertyName));
		}
		buf.append(',');
		buf.append(colorValue2.getMinifiedCssText(propertyName));
		if (percent2 != null) {
			buf.append(' ');
			buf.append(percent2.getMinifiedCssText(propertyName));
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("color-mix(in ");
		wri.write(getCSSColorSpace());

		switch (inMethod) {
		case LONGER:
			wri.write(" longer hue");
			break;
		case INCREASING:
			wri.write(" increasing hue");
			break;
		case DECREASING:
			wri.write(" decreasing hue");
			break;
		case UNKNOWN:
			wri.write(' ');
			unknownMethod.writeCssText(wri);
			wri.write(" hue");
			break;
		default:
		}

		wri.write(", ");
		colorValue1.writeCssText(wri);
		if (percent1 != null) {
			wri.write(' ');
			percent1.writeCssText(wri);
		}
		wri.write(", ");
		colorValue2.writeCssText(wri);
		if (percent2 != null) {
			wri.write(' ');
			percent2.writeCssText(wri);
		}
		wri.write(')');
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		CSSParser parser = new CSSParser();
		LexicalUnit lunit;
		try {
			lunit = parser.parsePropertyValue(new StringReader(cssText));
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR,
					"Wrong color-mix() value: " + cssText);
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			lunit = null;
		}

		LexicalSetter setter = newLexicalSetter();
		try {
			setter.setLexicalUnit(lunit);
		} catch (IllegalStateException e) {
			DOMException ex = new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"Not a color-mix() value: " + lunit.toString());
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		PrimitiveValue comp;
		switch (index) {
		case 0:
			comp = colorValue1;
			break;
		case 1:
			comp = percent1;
			break;
		case 2:
			comp = colorValue2;
			break;
		case 3:
			comp = percent2;
			break;
		case 4:
			comp = unknownMethod;
			break;
		default:
			comp = null;
		}
		return comp;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		PrimitiveValue comp = (PrimitiveValue) component;
		switch (index) {
		case 0:
			setColorValue1(comp);
			break;
		case 1:
			setPercentage1(comp);
			break;
		case 2:
			setColorValue2(comp);
			break;
		case 3:
			setPercentage2(comp);
			break;
		case 4:
			setInterpolationMethod(comp);
			break;
		default:
		}
	}

	private void setInterpolationMethod(PrimitiveValue primi) {
		if (primi.getPrimitiveType() == Type.IDENT) {
			String s = ((TypedValue) primi).getStringValue();
			inMethod = ColorSpaceHelper.parseInterpolationMethod(s);
			if (inMethod != HueInterpolationMethod.UNKNOWN) {
				unknownMethod = null;
				return;
			}
		} else {
			inMethod = HueInterpolationMethod.UNKNOWN;
		}
		unknownMethod = primi;
	}

	@Override
	public int getComponentCount() {
		return unknownMethod == null ? 4 : 5;
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		return toRGBColor(true);
	}

	@Override
	public RGBAColor toRGBColor(boolean clamp) throws DOMException {
		BaseColor color = getColor();
		if (!color.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		//
		switch (color.getSpace()) {
		case sRGB:
			return (RGBColor) color;
		case CIE_XYZ:
		case CIE_XYZ_D50:
			return ((XYZColorImpl) color).toSRGBColor(clamp);
		default:
			if (getColorModel() == ColorModel.RGB) {
				return ((ProfiledRGBColor) color).toSRGBColor(clamp);
			}
		}

		return (RGBAColor) color.toColorSpace(ColorSpace.srgb);
	}

	@Override
	public BaseColor getColor() {
		if (mixColors()) {
			return color;
		} else {
			return null;
		}
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleColor(colorValue1) && isConvertibleColor(colorValue2)
				&& (percent1 == null || percent1.getUnitType() == CSSUnit.CSS_PERCENTAGE)
				&& (percent2 == null || percent2.getUnitType() == CSSUnit.CSS_PERCENTAGE)
				&& unknownMethod == null;
	}

	private boolean isConvertibleColor(PrimitiveValue colorValue) {
		return (colorValue instanceof ColorValue
				&& ((ColorValue) colorValue).hasConvertibleComponents())
				|| colorValue.getPrimitiveType() == Type.IDENT;
	}

	@Override
	public float deltaE2000(CSSColorValue otherColor) {
		BaseColor color = getColor();
		if (color == null || !((ColorValue) otherColor).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}
		return color.packInValue().deltaE2000(otherColor);
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.COLOR_MIX) {
					setLexical(lunit);
				} else {
					throw new IllegalStateException("No color-mix() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexical(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();

			// In
			LexicalType type = lu.getLexicalUnitType();
			switch (type) {
			case VAR:
			case ATTR:
				throw new CSSLexicalProcessingException(
						"Unprocessable proxy inside color-mix() found.");
			case IDENT:
				if ("in".equalsIgnoreCase(lu.getStringValue())) {
					break;
				}
			default:
				throw new DOMException(DOMException.SYNTAX_ERR,
						"Color-Mix begings with 'in', not with " + lunit.toString());
			}

			// Color space
			lu = lu.getNextLexicalUnit();
			type = lu.getLexicalUnitType();
			String colorSpace;
			switch (type) {
			case VAR:
			case ATTR:
				throw new CSSLexicalProcessingException(
						"Unprocessable proxy inside color-mix() found.");
			case IDENT:
				colorSpace = lu.getStringValue();
				break;
			default:
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Color space must be identifier, not: " + lunit.toString());
			}

			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				wrongValueSyntax(lunit);
			}

			// Interpolation method or comma
			HueInterpolationMethod method = HueInterpolationMethod.SHORTER;
			PrimitiveValue unknownMethod = null;

			if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
				if (lu.getLexicalUnitType() == LexicalType.IDENT) {
					String s = lu.getStringValue();
					method = ColorSpaceHelper.parseInterpolationMethod(s);
					if (method == HueInterpolationMethod.UNKNOWN) {
						unknownMethod = factory.createCSSPrimitiveValue(lu, true);
						reportSyntaxWarning("Unknown interpolation method: " + s);
					}
				} else {
					CSSValueSyntax syn = SyntaxParser.createSimpleSyntax("custom-ident");
					if (lu.shallowClone().matches(syn) != Match.TRUE) {
						wrongValueSyntax(lunit);
						return;
					}
					method = HueInterpolationMethod.UNKNOWN;
					unknownMethod = factory.createCSSPrimitiveValue(lu, true);
				}

				lu = lu.getNextLexicalUnit();
				if (lu == null) {
					wrongValueSyntax(lunit);
					return;
				}
				if (lu.getLexicalUnitType() == LexicalType.IDENT) {
					if (!"hue".equalsIgnoreCase(lu.getStringValue())) {
						wrongValueSyntax(lunit);
						return;
					}
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						wrongValueSyntax(lunit);
						return;
					}
				}

				// Comma
				if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
					wrongValueSyntax(lunit);
					return;
				}
			}

			// First color-pcnt
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				wrongValueSyntax(lunit);
			}

			PrimitiveValue color1, pcnt1;
			CSSValueSyntax synPcnt = SyntaxParser.createSimpleSyntax("percentage");

			PrimitiveValue primi = factory.createCSSPrimitiveValue(lu, true);
			if (!isValidColor(primi)) {
				if (primi.matches(synPcnt) == Match.FALSE) {
					wrongValueSyntax(lunit);
					return;
				} else {
					// % color
					pcnt1 = primi;
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						wrongValueSyntax(lunit);
						return;
					}
					primi = factory.createCSSPrimitiveValue(lu, true);
					if (!isValidColor(primi)) {
						wrongValueSyntax(lunit);
						return;
					}
					color1 = primi;
					lu = lu.getNextLexicalUnit();
					if (lu == null || lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
						wrongValueSyntax(lunit);
						return;
					}
				}
			} else {
				// color %
				color1 = primi;
				lu = lu.getNextLexicalUnit();
				if (lu == null) {
					wrongValueSyntax(lunit);
					return;
				}
				if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
					// color %
					primi = factory.createCSSPrimitiveValue(lu, true);
					if (primi.matches(synPcnt) == Match.FALSE) {
						wrongValueSyntax(lunit);
						return;
					}
					pcnt1 = primi;
					lu = lu.getNextLexicalUnit();
					if (lu == null || lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
						wrongValueSyntax(lunit);
						return;
					}
				} else {
					// color ,
					pcnt1 = null;
				}
			}

			// Get past the comma
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				wrongValueSyntax(lunit);
				return;
			}

			PrimitiveValue color2, pcnt2;
			primi = factory.createCSSPrimitiveValue(lu, true);
			if (!isValidColor(primi)) {
				if (primi.matches(synPcnt) == Match.FALSE) {
					wrongValueSyntax(lunit);
					return;
				} else {
					// % color
					pcnt2 = primi;
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						wrongValueSyntax(lunit);
						return;
					}
					primi = factory.createCSSPrimitiveValue(lu, true);
					if (!isValidColor(primi)) {
						wrongValueSyntax(lunit);
						return;
					}
					color2 = primi;
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						wrongValueSyntax(lunit);
						return;
					}
				}
			} else {
				// color %
				color2 = primi;
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					// color %
					primi = factory.createCSSPrimitiveValue(lu, true);
					if (primi.matches(synPcnt) == Match.FALSE) {
						wrongValueSyntax(lunit);
						return;
					}
					pcnt2 = primi;
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						wrongValueSyntax(lunit);
						return;
					}
				} else {
					// just color
					pcnt2 = null;
				}
			}

			ColorMixFunction.this.inColorSpace = colorSpace;
			ColorMixFunction.this.inMethod = method;
			ColorMixFunction.this.unknownMethod = unknownMethod;
			ColorMixFunction.this.colorValue1 = color1;
			ColorMixFunction.this.colorValue2 = color2;
			ColorMixFunction.this.setPercentage1(pcnt1);
			ColorMixFunction.this.setPercentage2(pcnt2);

			setColorModelSpace(colorSpace);
		}

		private void wrongValueSyntax(LexicalUnit lunit) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"Wrong color-mix() value: " + lunit.toString());
		}

	}

	private void setColorModelSpace(String colorSpace) {
		if (ColorSpace.srgb.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.srgb;
			color = new RGBColor();
		} else if (ColorSpace.srgb_linear.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.srgb_linear;
			color = new ProfiledRGBColor(inColorSpace);
		} else if (ColorSpace.display_p3.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.display_p3;
			color = new ProfiledRGBColor(inColorSpace);
		} else if (ColorSpace.a98_rgb.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.a98_rgb;
			color = new ProfiledRGBColor(inColorSpace);
		} else if (ColorSpace.prophoto_rgb.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.prophoto_rgb;
			color = new ProfiledRGBColor(inColorSpace);
		} else if (ColorSpace.rec2020.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.rec2020;
			color = new ProfiledRGBColor(inColorSpace);
		} else if (ColorSpace.xyz.equalsIgnoreCase(colorSpace)
				|| "xyz-d65".equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.xyz;
			color = new XYZColorImpl(Illuminant.D65);
		} else if (ColorSpace.xyz_d50.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.xyz_d50;
			color = new XYZColorImpl(Illuminant.D50);
		} else if ("hsl".equalsIgnoreCase(colorSpace) || "hsla".equalsIgnoreCase(colorSpace)) {
			inColorSpace = "hsl";
			color = new HSLColorImpl();
			hueIndex = 0;
		} else if ("hwb".equalsIgnoreCase(colorSpace)) {
			inColorSpace = "hwb";
			color = new HWBColorImpl();
			hueIndex = 0;
		} else if (ColorSpace.cie_lab.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.cie_lab;
			color = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
		} else if (ColorSpace.cie_lch.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.cie_lch;
			color = new LCHColorImpl(Space.CIE_LCh, ColorSpace.cie_lch);
			hueIndex = 2;
		} else if (ColorSpace.ok_lab.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.ok_lab;
			color = new LABColorImpl(Space.OK_Lab, ColorSpace.ok_lab);
		} else if (ColorSpace.ok_lch.equalsIgnoreCase(colorSpace)) {
			inColorSpace = ColorSpace.ok_lch;
			color = new LCHColorImpl(Space.OK_LCh, ColorSpace.ok_lch);
			hueIndex = 2;
		} else if (colorSpace.startsWith("--")) {
			inColorSpace = colorSpace;
			color = new BaseProfiledColor(colorSpace);
		} else {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
					"Unsupported color space: " + colorSpace);
		}
		inColorModel = color.getColorModel();
	}

	private boolean mixColors() {
		if (hasConvertibleComponents()) {
			BaseColor conv1, conv2;
			try {
				conv1 = colorInSpace(colorValue1);
				conv2 = colorInSpace(colorValue2);
			} catch (DOMException e) {
				return false;
			}

			float[] frac = fractions();
			if (Float.isNaN(frac[0])) {
				return false;
			}

			// Alpha
			NumberValue alpha;
			PrimitiveValue primi1 = conv1.getAlpha();
			PrimitiveValue primi2 = conv2.getAlpha();

			if (primi1 == null || primi1.getPrimitiveType() == Type.IDENT) {
				// IDENT must be 'none', per hasConvertibleComponents()
				if (primi2.getPrimitiveType() == Type.IDENT) {
					alpha = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
				} else {
					alpha = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER,
							((TypedValue) primi2).getFloatValue(CSSUnit.CSS_NUMBER));
				}
			} else if (primi2 == null || primi2.getPrimitiveType() == Type.IDENT) {
				// IDENT must be 'none', per hasConvertibleComponents()
				if (primi1.getPrimitiveType() == Type.IDENT) {
					alpha = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
				} else {
					alpha = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER,
							((TypedValue) primi1).getFloatValue(CSSUnit.CSS_NUMBER));
				}
			} else {
				float fa1, fa2;
				try {
					fa1 = ((TypedValue) primi1).getFloatValue(CSSUnit.CSS_NUMBER);
					fa2 = ((TypedValue) primi2).getFloatValue(CSSUnit.CSS_NUMBER);
				} catch (DOMException e) {
					return false;
				}
				float falpha = fa1 * frac[0] + fa2 * frac[1];
				alpha = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, falpha);
			}

			color.setAlpha(alpha);

			double[] components1 = conv1.toNumberArray();
			double[] components2 = conv2.toNumberArray();

			int len = Math.max(components1.length, components2.length);
			double[] components = new double[len];

			for (int i = 0; i < len; i++) {
				TypedValue color1comp = (TypedValue) conv1.item(i + 1);
				TypedValue color2comp = (TypedValue) conv2.item(i + 1);
				if (color1comp == null || color1comp.getPrimitiveType() == Type.IDENT) {
					// IDENT must be 'none', per hasConvertibleComponents()
					if (color2comp.getPrimitiveType() == Type.IDENT) {
						components[i] = 0d;
					} else {
						components[i] = components2[i];
					}
				} else if (color2comp == null || color2comp.getPrimitiveType() == Type.IDENT) {
					// IDENT must be 'none', per hasConvertibleComponents()
					if (color1comp.getPrimitiveType() == Type.IDENT) {
						components[i] = 0d;
					} else {
						components[i] = components1[i];
					}
				} else {
					double fv1, fv2;
					fv1 = components1[i];
					fv2 = components2[i];

					double result;
					if (i == hueIndex) {
						result = hueInterpolation(fv1, frac[0], fv2, frac[1]);
					} else {
						result = fv1 * frac[0] + fv2 * frac[1];
					}

					components[i] = result;
				}
			}
			color.setColorComponents(components);
			return true;
		}
		return false;
	}

	private BaseColor colorInSpace(PrimitiveValue colorValue) {
		CSSColor conv;
		if (colorValue.getPrimitiveType() == Type.COLOR) {
			conv = ((ColorValue) colorValue).getColor().toColorSpace(inColorSpace);
		} else {
			conv = ((TypedValue) colorValue).toRGBColor();
			conv = conv.toColorSpace(inColorSpace);
		}
		return (BaseColor) conv;
	}

	private float[] fractions() {
		// Determine the percentages
		float pcnt1, pcnt2;
		if (percent1 == null) {
			if (percent2 == null) {
				pcnt1 = 50f;
				pcnt2 = 50f;
			} else {
				pcnt2 = ((TypedValue) percent2).getFloatValue(CSSUnit.CSS_PERCENTAGE);
				pcnt1 = 100f - pcnt2;
			}
		} else if (percent2 == null) {
			pcnt1 = ((TypedValue) percent1).getFloatValue(CSSUnit.CSS_PERCENTAGE);
			pcnt2 = 100f - pcnt1;
		} else {
			pcnt1 = ((TypedValue) percent1).getFloatValue(CSSUnit.CSS_PERCENTAGE);
			pcnt2 = ((TypedValue) percent2).getFloatValue(CSSUnit.CSS_PERCENTAGE);
		}

		float p12 = pcnt1 + pcnt2;

		float[] frac = new float[2];
		if (Math.abs(p12) >= 1e-7) {
			frac[0] = pcnt1 / p12;
			frac[1] = pcnt2 / p12;
		} else {
			frac[0] = Float.NaN;
			frac[1] = Float.NaN;
		}

		return frac;
	}

	private double hueInterpolation(double angle1, float frac1, double angle2, float frac2) {
		final double diff = angle2 - angle1;
		switch (inMethod) {
		case SHORTER:
		case UNKNOWN:
			if (diff > 180d) {
				angle1 += 360d;
			} else if (diff < -180d) {
				angle2 += 360d;
			}
			break;
		case LONGER:
			if (diff > 0d && diff < 180d) {
				angle1 += 360d;
			} else if (diff > -180d && diff <= 0d) {
				angle2 += 360d;
			}
			break;
		case INCREASING:
			if (diff < 0d) {
				angle2 += 360d;
			}
			break;
		case DECREASING:
			if (diff > 0d) {
				angle1 += 360d;
			}
			break;
		}

		double hue = angle1 * frac1 + angle2 * frac2;

		if (hue >= 360d) {
			hue -= 360d;
		} else if (hue < 0d) {
			hue += 360d;
		}

		return hue;
	}

	@Override
	public LABColorValue toLABColorValue() {
		BaseColor color = getColor();
		LABColorValue labColor = new LABColorValue();
		LABColorImpl lab = (LABColorImpl) labColor.getColor();
		switch (color.getColorModel()) {
		case RGB:
			color.toLABColor(lab);
			break;
		case XYZ:
			color.toLABColor(lab);
			break;
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
					"Custom profiles are not suported.");
		}
		return labColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(inColorSpace, inColorModel, inMethod, colorValue1,
				colorValue2, percent1, percent2);
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
		ColorMixFunction other = (ColorMixFunction) obj;
		return inColorModel == other.inColorModel && inMethod == other.inMethod
				&& Objects.equals(inColorSpace, other.inColorSpace)
				&& Objects.equals(colorValue1, other.colorValue1)
				&& Objects.equals(colorValue2, other.colorValue2)
				&& Objects.equals(percent1, other.percent1)
				&& Objects.equals(percent2, other.percent2);
	}

	@Override
	public ColorMixFunction clone() {
		return new ColorMixFunction(this);
	}

}
