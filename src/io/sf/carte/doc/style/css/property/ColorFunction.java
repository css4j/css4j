/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
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

	ColorFunction(ColorFunction copied) {
		super(copied);
		this.color = copied.color.clone();
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return color.getColorModel();
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		//
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
		//
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
		//
		switch (color.getSpace()) {
		case sRGB:
			return (RGBColor) color;
		case CIE_XYZ:
			return ((XYZColorImpl) color).toSRGB(clamp);
		default:
		}
		//
		if (getColorModel() == ColorModel.RGB) {
			return ((ProfiledRGBColor) color).toSRGB(clamp);
		}
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Custom color profile is not supported.");
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
		//
		LABColor lab;
		switch (otherColor.getColorModel()) {
		case LAB:
			lab = ((LABColorValue) otherColor).getColor();
			break;
		case LCH:
			lab = otherColor.toLABColorValue().getColor();
			break;
		case RGB:
			RGBColor rgbcolor = (RGBColor) otherColor.getColor();
			LABColorImpl labColor = new LABColorImpl();
			rgbcolor.toLABColor(labColor);
			lab = labColor;
			break;
		case XYZ:
			XYZColorImpl xyz = (XYZColorImpl) otherColor.getColor();
			labColor = new LABColorImpl();
			xyz.toLABColor(labColor);
			lab = labColor;
			break;
		default:
			RGBAColor rgb = otherColor.toRGBColor(false);
			RGBColorValue rgbValue = new RGBColorValue();
			rgbValue.setComponent(0, (StyleValue) rgb.getAlpha());
			rgbValue.setComponent(1, (StyleValue) rgb.getRed());
			rgbValue.setComponent(2, (StyleValue) rgb.getGreen());
			rgbValue.setComponent(3, (StyleValue) rgb.getBlue());
			lab = rgbValue.toLABColorValue().getColor();
		}
		//
		LABColorImpl labColor = new LABColorImpl();
		switch (this.color.getColorModel()) {
		case RGB:
			((ProfiledRGBColor) this.color).toLABColor(labColor);
			break;
		case XYZ:
			((XYZColorImpl) this.color).toLABColor(labColor);
			break;
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Color space is not supported.");
		}
		//
		return ColorUtil.deltaE2000Lab(((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lab.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab.getB()).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.COLOR_FUNCTION) {
					setLexical(lunit);
				} else {
					throw new IllegalStateException("No color() value: " + lunit.toString());
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
			List<PrimitiveValue> components = new ArrayList<>(5);

			// Color space
			if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.IDENT) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Color space must be identifier: " + lunit.toString());
			}
			String colorSpace = lu.getStringValue();
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}

			// Components
			PrimitiveValue alpha = null;
			PrimitiveValue primi;
			while (true) {
				primi = factory.createCSSPrimitiveValue(lu, true);
				checkNumberPcntCompValidity(primi, lunit);
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
						throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
					}
					break;
				}
			}

			setColor(colorSpace, components);
			if (alpha != null) {
				color.alpha = alpha;
			}
		}

	}

	static void checkNumberPcntCompValidity(PrimitiveValue primi, LexicalUnit lunit) {
		if (primi.getUnitType() != CSSUnit.CSS_NUMBER && primi.getUnitType() != CSSUnit.CSS_PERCENTAGE
				&& primi.getCssValueType() != CssType.PROXY && primi.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported value: " + lunit.toString());
		}
	}

	private void setColor(String colorSpace, List<PrimitiveValue> components) {
		if (ColorSpace.srgb.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.srgb;
			color = new ProfiledRGBColor(colorSpace, components);
		} else if (ColorSpace.display_p3.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.display_p3;
			color = new ProfiledRGBColor(colorSpace, components);
		} else if (ColorSpace.a98_rgb.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.a98_rgb;
			color = new ProfiledRGBColor(colorSpace, components);
		} else if (ColorSpace.prophoto_rgb.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.prophoto_rgb;
			color = new ProfiledRGBColor(colorSpace, components);
		} else if (ColorSpace.rec2020.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.rec2020;
			color = new ProfiledRGBColor(colorSpace, components);
		} else if (ColorSpace.xyz.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.xyz;
			color = new XYZColorImpl(components);
		} else {
			if (!colorSpace.startsWith("--")) {
				throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported color space: " + colorSpace);
			}
			PrimitiveValue[] ca = components.toArray(new PrimitiveValue[0]);
			color = new ProfiledColorImpl(colorSpace, ca);
		}
	}

	@Override
	public LABColorValue toLABColorValue() {
		LABColorValue labColor = new LABColorValue();
		LABColorImpl lab = (LABColorImpl) labColor.getColor();
		switch (color.getColorModel()) {
		case RGB:
			((ProfiledRGBColor) color).toLABColor(lab);
			break;
		case XYZ:
			((XYZColorImpl) color).toLABColor(lab);
			break;
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Custom profiles are not suported.");
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
