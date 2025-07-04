/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMNotSupportedException;
import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.BaseColor.Space;
import io.sf.carte.util.SimpleWriter;

/**
 * {@code color()} function.
 */
class ColorFunction extends ColorValue {

	private static final long serialVersionUID = 1L;

	private BaseColor color;

	ColorFunction() {
		super();
	}

	ColorFunction(BaseColor color) {
		super();
		this.color = color;
	}

	ColorFunction(ColorFunction copied) {
		super(copied);
		this.color = copied.color.clone();
	}

	@Override
	void set(StyleValue value) {
		super.set(value);

		ColorValue setfrom = (ColorValue) value;
		this.color.set((BaseColor) setfrom.getColor());
	}

	@Override
	public String getCssText() {
		return color.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		return color.toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		int len = color.getLength();
		wri.write("color(");
		wri.write(color.getColorSpace());
		for (int i = 1; i < len; i++) {
			wri.write(' ');
			color.item(i).writeCssText(wri);
		}
		// Alpha channel
		if (color.isNonOpaque()) {
			wri.write(" / ");
			color.appendAlphaChannel(wri);
		}

		wri.write(')');
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		return color.item(index);
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		color.setComponent(index, (PrimitiveValue) component);
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		return toRGBColor(true);
	}

	@Override
	public RGBAColor toRGBColor(boolean clamp) throws DOMException {
		if (!color.hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		switch (color.getSpace()) {
		case sRGB:
			return (RGBColor) color;
		case CIE_XYZ:
		case CIE_XYZ_D50:
			return ((XYZColorImpl) color).toSRGBColor(clamp);
		default:
		}

		if (getColorModel() == ColorModel.RGB) {
			return ((ProfiledRGBColor) color).toSRGBColor(clamp);
		}
		throw new DOMNotSupportedException("Custom color profile is not supported.");
	}

	@Override
	public CSSColor getColor() {
		return color;
	}

	@Override
	boolean hasConvertibleComponents() {
		return color.hasConvertibleComponents();
	}

	@Override
	public float deltaE2000(CSSColorValue otherColor) {
		if (!this.color.hasConvertibleComponents() || !((ColorValue) otherColor).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}

		LABColor lab;
		switch (otherColor.getColorModel()) {
		case LAB:
			lab = (LABColor) otherColor.getColor();
			break;
		case LCH:
			lab = otherColor.toLABColorValue().getColor();
			break;
		case RGB:
			RGBColor rgbcolor = (RGBColor) otherColor.getColor();
			LABColorImpl labColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
			rgbcolor.toLABColor(labColor);
			lab = labColor;
			break;
		case XYZ:
			XYZColorImpl xyz = (XYZColorImpl) otherColor.getColor();
			labColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
			xyz.toLABColor(labColor);
			lab = labColor;
			break;
		default:
			RGBAColor rgb = otherColor.toRGBColor(false);
			CSSColorValue rgbValue = rgb.packInValue();
			lab = rgbValue.toLABColorValue().getColor();
		}

		LABColorImpl labColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
		switch (this.color.getColorModel()) {
		case RGB:
			this.color.toLABColor(labColor);
			break;
		case XYZ:
			this.color.toLABColor(labColor);
			break;
		default:
			throw new DOMNotSupportedException("Color space is not supported.");
		}

		return ColorUtil.deltaE2000Lab(((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) throws DOMException {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.COLOR_FUNCTION) {
					setLexical(lunit);
				} else {
					throw new IllegalStateException("No color() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMSyntaxException("Wrong value: " + lunit.toString(), e);
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexical(LexicalUnit lunit) throws DOMException {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			CSSColor from = null;

			// from?
			if (lu.getLexicalUnitType() == LexicalType.IDENT) {
				if ("from".equalsIgnoreCase(lu.getStringValue())) {
					lu = nextLexicalUnit(lu, lunit);
					PrimitiveValue fromval = factory.createCSSPrimitiveValue(lu, true);
					from = computeColor(fromval, factory);
					lu = nextLexicalUnit(lu, lunit);
				}
			}

			List<PrimitiveValue> components = new ArrayList<>(5);

			// Color space
			if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.IDENT) {
				checkProxyValue(lu);
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Color space must be identifier: " + lunit.toString());
			}
			String colorSpace = lu.getStringValue();
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				throw new DOMSyntaxException("Wrong value: " + lunit.toString());
			}

			if (from != null) {
				String fromcs = from.getColorSpace();
				if (!fromcs.equalsIgnoreCase(colorSpace) || (ColorSpace.srgb.equals(fromcs)
						&& from.getColorModel() != ColorModel.RGB)) {
					from = from.toColorSpace(colorSpace);
				}
			}

			// Components
			PrimitiveValue alpha = null;
			PrimitiveValue primi;
			while (true) {
				primi = factory.createCSSPrimitiveValue(lu, true);
				if (from != null) {
					primi = absoluteComponent(from, primi, false);
				}
				checkComponentValidity(primi, lunit);
				components.add(primi);
				lu = lu.getNextLexicalUnit();

				if (lu == null) {
					break;
				}
				if (lu.getLexicalUnitType() == LexicalUnit.LexicalType.OPERATOR_SLASH) {
					lu = lu.getNextLexicalUnit(); // Alpha
					alpha = factory.createCSSPrimitiveValue(lu, true);
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						// This won't happen because it is filtered at NSAC level
						throw new DOMSyntaxException("Wrong value: " + lunit.toString());
					}
					break;
				}
			}

			color = ColorSpaceHelper.createProfiledColor(colorSpace);
			if (color == null) {
				if (colorSpace.startsWith("--")) {
					PrimitiveValue[] ca = components.toArray(new PrimitiveValue[0]);
					color = new BaseProfiledColor(colorSpace, ca);
				} else {
					throw new DOMNotSupportedException(
							"Unsupported color space: " + colorSpace);
				}
			} else {
				setComponents(components);
			}

			if (alpha != null) {
				if (from != null) {
					alpha = absoluteComponent(from, alpha, false);
				}
				color.setAlpha(alpha);
			}
		}

	}

	private static void checkComponentValidity(PrimitiveValue primi, LexicalUnit lunit)
			throws DOMException {
		if (!isComponentUnit(primi.getUnitType()) && BaseColor.isInvalidComponentType(primi)) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible with a color component: "
							+ (lunit != null ? lunit.getCssText() : primi.getCssText()));
		}
	}

	private static boolean isComponentUnit(short unit) {
		return unit == CSSUnit.CSS_NUMBER || unit == CSSUnit.CSS_PERCENTAGE;
	}

	private void setComponents(List<PrimitiveValue> components) {
		int maxlen = color.getLength() - 1;
		int len = components.size();
		for (int i = 0; i < len; i++) {
			PrimitiveValue comp = components.get(i);
			comp = enforceColorComponentType(comp);
			color.setComponent(i + 1, comp);
		}

		// Set missing components to zero
		for (int i = len; i < maxlen; i++) {
			NumberValue zero = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			zero.setSubproperty(true);
			color.setComponent(i + 1, zero);
		}
	}

	static PrimitiveValue enforceColorComponentType(PrimitiveValue primi) throws DOMException {
		if (primi.getPrimitiveType() == Type.EXPRESSION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateExpression((CSSExpressionValue) primi);
			setMaximumPrecision(primi);
		} else if (primi.getPrimitiveType() == Type.MATH_FUNCTION) {
			PercentageEvaluator eval = new PercentageEvaluator();
			primi = (PrimitiveValue) eval.evaluateFunction((CSSMathFunctionValue) primi);
			setMaximumPrecision(primi);
		}

		checkComponentValidity(primi, null);

		return primi;
	}

	private static void setMaximumPrecision(PrimitiveValue primi) {
		short unit = primi.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			((NumberValue) primi).setMaximumFractionDigits(6);
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			((NumberValue) primi).setMaximumFractionDigits(3);
		}
	}

	@Override
	public LABColorValue toLABColorValue() {
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
			throw new DOMNotSupportedException("Custom profiles are not suported.");
		}
		return labColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(color);
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
		ColorFunction other = (ColorFunction) obj;
		return Objects.equals(color, other.color);
	}

	@Override
	public ColorFunction clone() {
		return new ColorFunction(this);
	}

}
