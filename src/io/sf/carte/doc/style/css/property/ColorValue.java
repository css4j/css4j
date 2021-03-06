/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
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
			} else if (ptype != Type.COLOR || ((ColorValue) value).getColorModel() != getColorModel()) {
				failSetCssText();
			}
			set(value);
		} else {
			failSetCssText();
		}
	}

	private void failSetCssText() {
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
				"This value can only be set to a color in the " + getColorModel() + " color model.");
	}

	void set(StyleValue value) {
		ColorValue setfrom = (ColorValue) value;
		this.systemDefault = setfrom.systemDefault;
		this.commaSyntax = setfrom.commaSyntax;
	}

	@Override
	public String getCssText() {
		return toRGBColor().toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		return ((CSSRGBColor) toRGBColor()).toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(toRGBColor().toString());
	}

	@Override
	public float deltaE2000(CSSColorValue color) {
		if (!hasConvertibleComponents() || !((ColorValue) color).hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot compute delta.");
		}
		switch (color.getColorModel()) {
		case LCH:
		case LAB:
			// Delegate on the higher-precision color models
			return color.deltaE2000(this);
		default:
		}
		RGBAColor color1 = toRGBColor(false);
		RGBAColor color2 = color.toRGBColor(false);
		RGBColorValue rgb1 = new RGBColorValue();
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
		LABColor lab1 = rgb1.toLABColorValue().getColor();
		LABColor lab2 = rgb2.toLABColorValue().getColor();
		return ColorUtil.deltaE2000Lab(((CSSTypedValue) lab1.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lab1.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab1.getB()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE),
				((CSSTypedValue) lab2.getA()).getFloatValue(CSSUnit.CSS_NUMBER),
				((CSSTypedValue) lab2.getB()).getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		return syntax.getCategory() == Category.color ? Match.TRUE : Match.FALSE;
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

	abstract boolean hasConvertibleComponents();

	@Override
	abstract public ColorValue clone();

	static void checkPcntCompValidity(PrimitiveValue primisat, LexicalUnit lunit) {
		if (primisat.getUnitType() != CSSUnit.CSS_PERCENTAGE && primisat.getCssValueType() != CssType.PROXY
				&& primisat.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported value: " + lunit.toString());
		}
	}

	static void checkNumberCompValidity(PrimitiveValue primihue, LexicalUnit lunit) {
		if (primihue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(primihue.getUnitType())
				&& primihue.getCssValueType() != CssType.PROXY && primihue.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported value: " + lunit.toString());
		}
	}

	static void checkHueValidity(PrimitiveValue primihue, LexicalUnit lunit) {
		if (primihue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(primihue.getUnitType())
				&& primihue.getCssValueType() != CssType.PROXY && primihue.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unsupported value: " + lunit.toString());
		}
	}

	class CSSRGBColor extends BaseColor implements RGBAColor {

		private static final long serialVersionUID = 1L;

		private PrimitiveValue red = null;
		private PrimitiveValue green = null;
		private PrimitiveValue blue = null;


		CSSRGBColor() {
			super();
		}

		@Override
		ColorModel getColorModel() {
			return ColorModel.RGB;
		}

		public void setRed(PrimitiveValue red) {
			if (red == null) {
				throw new NullPointerException();
			}
			enforceColorComponentType(red);
			this.red = red;
		}

		private void enforceColorComponentType(PrimitiveValue primi) {
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
		public String toString() {
			return toString(false);
		}

		String toMinifiedString() {
			return toString(true);
		}

		String toString(boolean minify) {
			float fr = componentByte(red);
			float fg = componentByte(green);
			float fb = componentByte(blue);
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
			appendComponentCssText(buf, red, true).append(' ');
			appendComponentCssText(buf, green, true).append(' ');
			appendComponentCssText(buf, blue, true);
			if (nonOpaque) {
				buf.append('/');
				appendAlphaChannelMinified(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private String minifiedOldFunctionalString(boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(24);
			if (nonOpaque) {
				buf.append("rgba(");
			} else {
				buf.append("rgb(");
			}
			appendComponentCssText(buf, red, true).append(',');
			appendComponentCssText(buf, green, true).append(',');
			appendComponentCssText(buf, blue, true);
			if (nonOpaque) {
				buf.append(',');
				appendAlphaChannelMinified(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private String functionalString(boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(23);
			buf.append("rgb(");
			appendComponentCssText(buf, red, false).append(' ');
			appendComponentCssText(buf, green, false).append(' ');
			appendComponentCssText(buf, blue, false);
			if (nonOpaque) {
				buf.append(" / ");
				appendAlphaChannel(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private String oldFunctionalString(boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(25);
			if (nonOpaque) {
				buf.append("rgba(");
			} else {
				buf.append("rgb(");
			}
			appendComponentCssText(buf, red, false).append(", ");
			appendComponentCssText(buf, green, false).append(", ");
			appendComponentCssText(buf, blue, false);
			if (nonOpaque) {
				buf.append(", ");
				appendAlphaChannel(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private StringBuilder appendComponentCssText(StringBuilder buf, PrimitiveValue component, boolean minify) {
			return buf.append(component.getCssText());
		}

		private boolean notSameChar(String hexr) {
			return hexr.length() == 1 || hexr.charAt(0) != hexr.charAt(1);
		}

		@Override
		boolean hasConvertibleComponents() {
			return isConvertibleComponent(getRed()) && isConvertibleComponent(getGreen())
					&& isConvertibleComponent(getBlue());
		}

		public HSLColor toHSLColor() {
			HSLColorImpl hslColor = new MyHSLColorImpl();
			toHSLColor(hslColor);
			return hslColor;
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
			if (!(obj instanceof CSSRGBColor)) {
				return false;
			}
			CSSRGBColor other = (CSSRGBColor) obj;
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
		public CSSRGBColor clone() {
			CSSRGBColor clon = new CSSRGBColor();
			clon.red = this.red.clone();
			clon.green = this.green.clone();
			clon.blue = this.blue.clone();
			clon.setAlpha(alpha.clone());
			return clon;
		}

	}

	private static class Hsl {
		float h, s, l;
	}

	class MyHSLColorImpl extends HSLColorImpl {

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return toString(commaSyntax);
		}

	}

}
