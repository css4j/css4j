/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.property.BaseColor.Space;
import io.sf.carte.util.SimpleWriter;

/**
 * Color value.
 */
abstract public class ColorValue extends TypedValue implements CSSColorValue {

	private static final long serialVersionUID = 1L;

	static final NumberValue opaqueAlpha;

	static {
		NumberValue alpha = new NumberValue();
		alpha.setFloatValue(CSSUnit.CSS_NUMBER, 1f);
		opaqueAlpha = alpha.immutable();
	}

	private boolean systemDefault = false;

	boolean commaSyntax = true;

	ColorValue() {
		super(Type.COLOR);
	}

	ColorValue(Type unitType) {
		super(unitType);
	}

	ColorValue(ColorValue copied) {
		super(copied);
		this.systemDefault = copied.systemDefault;
		this.commaSyntax = copied.commaSyntax;
	}

	public void setSystemDefault() {
		systemDefault = true;
	}

	@Override
	public boolean isSystemDefault() {
		return systemDefault;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		ValueFactory factory = new ValueFactory();
		StyleValue value = factory.parseProperty(cssText);
		if (value.getCssValueType() == CSSColorValue.CssType.TYPED) {
			Type ptype = value.getPrimitiveType();
			if (ptype == Type.IDENT) {
				String ident = ((CSSTypedValue) value).getStringValue().toLowerCase(Locale.ROOT);
				String colorspec = ColorIdentifiers.getInstance().getColor(ident);
				if (colorspec != null) {
					value = factory.parseProperty(colorspec);
				} else if ("transparent".equals(ident)) {
					value = factory.parseProperty("rgba(0,0,0,0)");
				} else {
					failSetCssText();
				}
				ptype = value.getPrimitiveType();
			}
			if (ptype != Type.COLOR || ((ColorValue) value).getColorModel() != getColorModel()
					|| !((ColorValue) value).getColor().getColorSpace().equals(getColor().getColorSpace())
					|| getClass() != value.getClass()) {
				failSetCssText();
			}
			set(value);
		} else {
			failSetCssText();
		}
	}

	private void failSetCssText() {
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
				"This value can only be set to a color in the original color space and the " + getColorModel()
						+ " color model, using the same syntax.");
	}

	void set(StyleValue value) {
		ColorValue setfrom = (ColorValue) value;
		this.systemDefault = setfrom.systemDefault;
		this.commaSyntax = setfrom.commaSyntax;
	}

	@Override
	public String getCssText() {
		return getColor().toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return getColor().toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getColor().toString());
	}

	@Override
	public float deltaE2000(CSSColorValue color) {
		if (!hasConvertibleComponents() || !((ColorValue) color).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}
		//
		RGBColorValue rgb1;
		LABColor lab1, lab2;
		RGBAColor color1;
		//
		switch (color.getColorModel()) {
		case LCH:
		case LAB:
		case XYZ:
			// Delegate on the higher-precision color models
			return color.deltaE2000(this);
		case RGB:
			RGBColor rgbcolor = (RGBColor) color.getColor();
			LABColorImpl labColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
			rgbcolor.toLABColor(labColor);
			lab2 = labColor;
			//
			color1 = toRGBColor(false);
			rgb1 = new RGBColorValue();
			rgb1.setComponent(0, (StyleValue) color1.getAlpha());
			rgb1.setComponent(1, (StyleValue) color1.getRed());
			rgb1.setComponent(2, (StyleValue) color1.getGreen());
			rgb1.setComponent(3, (StyleValue) color1.getBlue());
			lab1 = rgb1.toLABColorValue().getColor();
			break;
		default:
			color1 = toRGBColor(false);
			RGBAColor color2 = color.toRGBColor(false);
			rgb1 = new RGBColorValue();
			RGBColorValue rgb2 = new RGBColorValue();
			rgb1.setComponent(0, (StyleValue) color1.getAlpha());
			rgb1.setComponent(1, (StyleValue) color1.getRed());
			rgb1.setComponent(2, (StyleValue) color1.getGreen());
			rgb1.setComponent(3, (StyleValue) color1.getBlue());
			rgb2.setComponent(0, (StyleValue) color2.getAlpha());
			rgb2.setComponent(1, (StyleValue) color2.getRed());
			rgb2.setComponent(2, (StyleValue) color2.getGreen());
			rgb2.setComponent(3, (StyleValue) color2.getBlue());
			//
			lab1 = rgb1.toLABColorValue().getColor();
			lab2 = rgb2.toLABColorValue().getColor();
		}
		return ColorUtil.deltaE2000Lab(((CSSTypedValue) lab1.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab1.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab1.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getLightness()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getB()).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		return syntax.getCategory() == Category.color ? Match.TRUE : Match.FALSE;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
	}

	/**
	 * Get the color component at {@code index}.
	 * <p>
	 * This method allows to access the color components like if they were indexed.
	 * It is convenient to perform common tasks at the components (like when
	 * computing values).
	 * </p>
	 * 
	 * @param index the index. Index {@code 0} is always the alpha channel.
	 * @return the color component, or {@code null} if the index is incorrect.
	 */
	@Override
	abstract public PrimitiveValue getComponent(int index);

	@Override
	public int getComponentCount() {
		return getColor().getLength();
	}

	abstract boolean hasConvertibleComponents();

	@Override
	abstract public ColorValue clone();

	/**
	 * RGB color in the sRGB color space.
	 */
	class CSSRGBColor extends RGBColor {

		private static final long serialVersionUID = 2L;

		CSSRGBColor() {
			super();
		}

		CSSRGBColor(CSSRGBColor copyMe) {
			super(copyMe);
		}

		@Override
		public String toString() {
			return toString(false);
		}

		@Override
		public String toMinifiedString() {
			return toString(true);
		}

		String toString(boolean minify) {
			float fr = componentByte(getRed());
			float fg = componentByte(getGreen());
			float fb = componentByte(getBlue());
			boolean nonOpaque = isNonOpaque();
			if (nonOpaque || !isInteger(fr) || !isInteger(fg) || !isInteger(fb) || fr > 255f || fg > 255f
					|| fb > 255f) {
				if (minify) {
					if (commaSyntax) {
						return minifiedOldFunctionalString(nonOpaque);
					} else {
						return minifiedFunctionalString(nonOpaque);
					}
				} else {
					if (commaSyntax) {
						return oldFunctionalString(nonOpaque);
					} else {
						return functionalString(nonOpaque);
					}
				}
			}
			int r = Math.round(fr);
			int g = Math.round(fg);
			int b = Math.round(fb);
			// Use hexadecimal notation
			String hexr = Integer.toHexString(r);
			String hexg = Integer.toHexString(g);
			String hexb = Integer.toHexString(b);
			StringBuilder buf;
			if ((r != 0 && notSameChar(hexr)) || (g != 0 && notSameChar(hexg)) || (b != 0 && notSameChar(hexb))) {
				buf = new StringBuilder(7);
				buf.append('#');
				if (hexr.length() == 1) {
					buf.append('0');
				}
				buf.append(hexr);
				if (hexg.length() == 1) {
					buf.append('0');
				}
				buf.append(hexg);
				if (hexb.length() == 1) {
					buf.append('0');
				}
				buf.append(hexb);
			} else {
				buf = new StringBuilder(4);
				buf.append('#');
				buf.append(hexr.charAt(0));
				buf.append(hexg.charAt(0));
				buf.append(hexb.charAt(0));
			}
			return buf.toString();
		}

		private float componentByte(PrimitiveValue component) {
			float byteComp;
			Type type = component.getPrimitiveType();
			if (type == Type.NUMERIC) {
				TypedValue number = (TypedValue) component;
				if (number.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
					byteComp = number.getFloatValue(CSSUnit.CSS_PERCENTAGE) * 2.55f;
				} else {
					byteComp = number.getFloatValue(CSSUnit.CSS_NUMBER);
				}
			} else {
				byteComp = 256f;
			}
			return byteComp;
		}

		private boolean isInteger(float r) {
			return Math.abs(r - (float) Math.rint(r)) < 3e-4;
		}

		private String minifiedFunctionalString(boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(21);
			buf.append("rgb(");
			appendComponentMinifiedCssText(buf, getRed()).append(' ');
			appendComponentMinifiedCssText(buf, getGreen()).append(' ');
			appendComponentMinifiedCssText(buf, getBlue());
			if (nonOpaque) {
				buf.append('/');
				appendAlphaChannelMinified(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private String functionalString(boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(23);
			buf.append("rgb(");
			appendComponentCssText(buf, getRed()).append(' ');
			appendComponentCssText(buf, getGreen()).append(' ');
			appendComponentCssText(buf, getBlue());
			if (nonOpaque) {
				buf.append(" / ");
				appendAlphaChannel(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private boolean notSameChar(String hexr) {
			return hexr.length() == 1 || hexr.charAt(0) != hexr.charAt(1);
		}

		@Override
		HSLColorImpl createHSLColor() {
			return new MyHSLColorImpl();
		}

		@Override
		public CSSRGBColor clone() {
			return new CSSRGBColor(this);
		}

	}

	class MyHSLColorImpl extends HSLColorImpl {

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return toString(commaSyntax);
		}

	}

	/**
	 * Convert this value to a {@link HSLColorValue}, if possible.
	 * 
	 * @return the converted {@code HSLColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	public HSLColorValue toHSLColorValue() throws DOMException {
		RGBColor rgb = (RGBColor) toRGBColor(false);
		HSLColorValue hsl = new HSLColorValue();
		rgb.toHSLColor(hsl.getHSLColorImpl());
		return hsl;
	}

	/**
	 * Convert this value to a {@link LABColorValue}, if possible.
	 * 
	 * @return the converted {@code LABColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	@Override
	public LABColorValue toLABColorValue() throws DOMException {
		RGBColor rgb = (RGBColor) toRGBColor(false);
		LABColorValue lab = new LABColorValue();
		rgb.toLABColor(lab.getLABColorImpl());
		return lab;
	}

	@Override
	public LCHColorValue toLCHColorValue() throws DOMException {
		return toLABColorValue().toLCHColorValue();
	}

}
