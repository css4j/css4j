/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.util.SimpleWriter;

/**
 * Color-specific CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class ColorValue extends AbstractCSSPrimitiveValue {

	public static final NumberValue opaqueAlpha;

	static {
		NumberValue alpha = new NumberValue();
		alpha.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, 1f);
		opaqueAlpha = alpha.immutable();
	}

	private CSSRGBColor color = null;

	private RGBAColor.ColorSpace colorSpace = RGBAColor.ColorSpace.RGB;

	private boolean systemDefault = false;

	private boolean commaSyntax = false;

	ColorValue() {
		super(CSSPrimitiveValue.CSS_RGBCOLOR);
		color = new CSSRGBColor();
	}

	protected ColorValue(ColorValue copied) {
		super(copied);
		this.color = copied.color.clone();
		this.colorSpace = copied.colorSpace;
		this.systemDefault = copied.systemDefault;
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
		ValueFactory factory = new ValueFactory();
		AbstractCSSValue value = factory.parseProperty(cssText);
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			short ptype = ((CSSPrimitiveValue) value).getPrimitiveType();
			if (ptype == CSSPrimitiveValue.CSS_IDENT) {
				String ident = ((CSSPrimitiveValue) value).getStringValue();
				String colorspec = ColorIdentifiers.getInstance().getColor(ident);
				if (colorspec != null) {
					value = factory.parseProperty(colorspec);
				} else if ("transparent".equals(ident)) {
					value = factory.parseProperty("rgba(0,0,0,0)");
				} else {
					failSetCssText();
				}
			} else if (ptype != CSSPrimitiveValue.CSS_RGBCOLOR) {
				failSetCssText();
			}
			super.setCssText(cssText);
			set((ColorValue) value);
		} else {
			failSetCssText();
		}
	}

	private void failSetCssText() {
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "This property can only be set to a color value");
	}

	private void set(ColorValue setfrom) {
		this.color = setfrom.color;
		this.colorSpace = setfrom.colorSpace;
		this.systemDefault = setfrom.systemDefault;
	}

	public RGBAColor.ColorSpace getColorSpace() {
		return colorSpace;
	}

	@Override
	public String getCssText() {
		if (colorSpace == RGBAColor.ColorSpace.HSL) {
			String css = color.toHSLString();
			if (css != null) {
				return css;
			}
		}
		return getRGBColorValue().toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		if (colorSpace == RGBAColor.ColorSpace.HSL && color.getAlpha().getFloatValue(CSS_NUMBER) != 1f) {
			String css = color.toHSLMinifiedString();
			if (css != null) {
				return css;
			}
		}
		return ((CSSRGBColor) getRGBColorValue()).toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		if (colorSpace == RGBAColor.ColorSpace.HSL) {
			String css = color.toHSLString();
			if (css != null) {
				wri.write(css);
				return;
			}
		}
		wri.write(getRGBColorValue().toString());
	}

	@Override
	public String getStringValue() throws DOMException {
		return getCssText();
	}

	@Override
	public RGBAColor getRGBColorValue() throws DOMException {
		if (color.red == null || color.green == null || color.blue == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Color not set");
		}
		return color;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			ValueFactory factory = new ValueFactory();
			LexicalUnit lu = lunit.getParameters();
			String func = lunit.getFunctionName();
			try {
				if ("rgb".equals(func) || "rgba".equals(func)) {
					// red
					AbstractCSSPrimitiveValue basiccolor = factory.createCSSPrimitiveValue(lu, false);
					color.setRed(basiccolor);
					// comma ?
					lu = lu.getNextLexicalUnit();
					if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						// green
						lu = lu.getNextLexicalUnit();
					}
					basiccolor = factory.createCSSPrimitiveValue(lu, false);
					color.setGreen(basiccolor);
					if (commaSyntax) {
						// comma
						lu = lu.getNextLexicalUnit();
					}
					// blue
					lu = lu.getNextLexicalUnit();
					basiccolor = factory.createCSSPrimitiveValue(lu, false);
					color.setBlue(basiccolor);
					// comma, slash or null
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						// alpha
						lu = lu.getNextLexicalUnit();
						color.setAlpha(factory.createCSSPrimitiveValue(lu, false));
						lu = lu.getNextLexicalUnit();
					}
					colorSpace = RGBAColor.ColorSpace.RGB;
				} else if ("hsl".equals(func) || "hsla".equals(func)) {
					// hue
					CSSPrimitiveValue basiccolor = factory.createCSSPrimitiveValue(lu, false);
					float hue = basiccolor.getFloatValue(CSSPrimitiveValue.CSS_DEG) / 360f;
					// comma
					lu = lu.getNextLexicalUnit();
					if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						// saturation
						lu = lu.getNextLexicalUnit();
					}
					basiccolor = factory.createCSSPrimitiveValue(lu, false);
					float sat = basiccolor.getFloatValue(CSS_PERCENTAGE) / 100f;
					if (commaSyntax) {
						// comma
						lu = lu.getNextLexicalUnit();
					}
					// lightness
					lu = lu.getNextLexicalUnit();
					basiccolor = factory.createCSSPrimitiveValue(lu, false);
					float light = basiccolor.getFloatValue(CSS_PERCENTAGE) / 100f;
					// comma, slash or null
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						lu = lu.getNextLexicalUnit(); // Alpha
						color.setAlpha(factory.createCSSPrimitiveValue(lu, false));
						lu = lu.getNextLexicalUnit();
					}
					translateHSL(hue, sat, light);
					colorSpace = RGBAColor.ColorSpace.HSL;
				} else if ("hwb".equals(func)) {
					// hue
					CSSPrimitiveValue basiccolor = factory.createCSSPrimitiveValue(lu, false);
					float hue = basiccolor.getFloatValue(CSSPrimitiveValue.CSS_DEG) / 360f;
					// comma
					lu = lu.getNextLexicalUnit();
					boolean commaSyntax;
					if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						// whiteness
						lu = lu.getNextLexicalUnit();
					}
					basiccolor = factory.createCSSPrimitiveValue(lu, false);
					float whiteness = basiccolor.getFloatValue(CSS_PERCENTAGE) / 100f;
					if (commaSyntax) {
						// comma
						lu = lu.getNextLexicalUnit();
					}
					// blackness
					lu = lu.getNextLexicalUnit();
					basiccolor = factory.createCSSPrimitiveValue(lu, false);
					float blackness = basiccolor.getFloatValue(CSS_PERCENTAGE) / 100f;
					// comma, slash or null
					lu = lu.getNextLexicalUnit();
					if (lu != null) {
						lu = lu.getNextLexicalUnit(); // Alpha
						color.setAlpha(factory.createCSSPrimitiveValue(lu, false));
						lu = lu.getNextLexicalUnit();
					}
					translateHWB(hue, whiteness, blackness);
					colorSpace = RGBAColor.ColorSpace.HWB;
				}
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	void translateHSL(float hue, float sat, float light) {
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
		red.setFloatValue(CSS_PERCENTAGE, hueToRgb(m1, m2, hue + 1f / 3f));
		NumberValue green = new NumberValue();
		green.setFloatValue(CSS_PERCENTAGE, hueToRgb(m1, m2, hue));
		NumberValue blue = new NumberValue();
		blue.setFloatValue(CSS_PERCENTAGE, hueToRgb(m1, m2, hue - 1f / 3f));
		color.red = red;
		color.green = green;
		color.blue = blue;
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

	void translateHWB(float hue, float whiteness, float blackness) {
		if (hue > 1f) {
			hue -= (float) Math.floor(hue);
		} else if (hue < 0f) {
			hue = hue - (float) Math.floor(hue) + 1f;
		}
		hue *= 6f;
		float fh = (float) Math.floor(hue);
		float f = hue - fh;
		int ifh = (int) fh;
		if (ifh % 2 == 1) {
			f = 1f -f;
		}
		float value = 1f - blackness;
		float wv = whiteness + f * (value - whiteness);
		float r, g, b;
		switch (ifh) {
		case 1:
			r = wv;
			g = value;
			b = whiteness;
			break;
		case 2:
			r = whiteness;
			g = value;
			b = wv;
			break;
		case 3:
			r = whiteness;
			g = wv;
			b = value;
			break;
		case 4:
			r = wv;
			g = whiteness;
			b = value;
			break;
		case 5:
			r = value;
			g = whiteness;
			b = wv;
			break;
		default:
			r = value;
			g = wv;
			b = whiteness;
		}
		NumberValue red = new NumberValue();
		red.setFloatValue(CSS_NUMBER, Math.round(r * 255f));
		NumberValue green = new NumberValue();
		green.setFloatValue(CSS_NUMBER, Math.round(g * 255f));
		NumberValue blue = new NumberValue();
		blue.setFloatValue(CSS_NUMBER, Math.round(b * 255f));
		color.red = red;
		color.green = green;
		color.blue = blue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((color == null) ? 0 : color.hashCode());
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
		if (!(obj instanceof ColorValue)) {
			return false;
		}
		ColorValue other = (ColorValue) obj;
		if (color == null) {
			if (other.color != null) {
				return false;
			}
		} else if (!color.equals(other.color)) {
			return false;
		}
		return true;
	}

	@Override
	public ColorValue clone() {
		return new ColorValue(this);
	}

	private static class Hsl {
		int h, s, l;
	}

	class CSSRGBColor implements RGBAColor {

		private AbstractCSSPrimitiveValue red = null;
		private AbstractCSSPrimitiveValue green = null;
		private AbstractCSSPrimitiveValue blue = null;
		private AbstractCSSPrimitiveValue alpha = opaqueAlpha;


		CSSRGBColor() {
			super();
		}

		public void setRed(AbstractCSSPrimitiveValue red) {
			this.red = red;
		}

		@Override
		public AbstractCSSPrimitiveValue getRed() {
			return red;
		}

		public void setGreen(AbstractCSSPrimitiveValue green) {
			this.green = green;
		}

		@Override
		public AbstractCSSPrimitiveValue getGreen() {
			return green;
		}

		public void setBlue(AbstractCSSPrimitiveValue blue) {
			this.blue = blue;
		}

		@Override
		public AbstractCSSPrimitiveValue getBlue() {
			return blue;
		}

		public void setAlpha(AbstractCSSPrimitiveValue alpha) {
			this.alpha = alpha;
		}

		@Override
		public AbstractCSSPrimitiveValue getAlpha() {
			return alpha;
		}

		@Override
		public String toString() {
			return toString(false);
		}

		String toMinifiedString() {
			return toString(true);
		}

		String toString(boolean minify) {
			int r, g, b;
			if (red.getPrimitiveType() == CSS_PERCENTAGE) {
				r = Math.round(red.getFloatValue(CSS_PERCENTAGE) * 2.55f);
			} else {
				r = Math.round(red.getFloatValue(CSS_NUMBER));
			}
			if (green.getPrimitiveType() == CSS_PERCENTAGE) {
				g = Math.round(green.getFloatValue(CSS_PERCENTAGE) * 2.55f);
			} else {
				g = Math.round(green.getFloatValue(CSS_NUMBER));
			}
			if (blue.getPrimitiveType() == CSS_PERCENTAGE) {
				b = Math.round(blue.getFloatValue(CSS_PERCENTAGE) * 2.55f);
			} else {
				b = Math.round(blue.getFloatValue(CSS_NUMBER));
			}
			boolean nonOpaque = getFloatAlpha() != 1f;
			if (nonOpaque || r > 255 || g > 255 || b > 255) {
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

		private StringBuilder appendComponentCssText(StringBuilder buf, AbstractCSSPrimitiveValue component, boolean minify) {
			if (colorSpace == RGBAColor.ColorSpace.RGB
					|| component.getPrimitiveType() != CSSPrimitiveValue.CSS_PERCENTAGE) {
				return buf.append(component.getCssText());
			}
			float val = component.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
			float rval = (float) (Math.rint(val * 10f) * 0.1f);
			if (rval == 0f && minify) {
				return buf.append('0');
			}
			double rintValue = Math.rint(val);
			String strVal;
			if (rval == rintValue) {
				strVal = Integer.toString((int) rval);
			} else {
				strVal = Float.toString(rval);
			}
			return buf.append(strVal).append('%');
		}

		private StringBuilder appendAlphaChannel(StringBuilder buf) {
			String text;
			if (alpha.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
				float f = alpha.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
				text = formattedNumber(f);
			} else {
				text = alpha.getCssText();
			}
			return buf.append(text);
		}

		private StringBuilder appendAlphaChannelMinified(StringBuilder buf) {
			String text;
			if (alpha.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
				float f = alpha.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
				text = formattedNumberMinified(f);
			} else {
				text = alpha.getMinifiedCssText("");
			}
			return buf.append(text);
		}

		private String formattedNumber(float f) {
			NumberFormat format = DecimalFormat.getNumberInstance(Locale.US);
			format.setMaximumFractionDigits(3);
			format.setMinimumFractionDigits(0);
			return format.format(f);
		}

		private String formattedNumberMinified(float f) {
			NumberFormat format = DecimalFormat.getNumberInstance(Locale.US);
			format.setMaximumFractionDigits(3);
			format.setMinimumFractionDigits(0);
			format.setMinimumIntegerDigits(0);
			return format.format(f);
		}

		private boolean notSameChar(String hexr) {
			return hexr.length() == 1 || hexr.charAt(0) != hexr.charAt(1);
		}

		Hsl toHSL() {
			float r, g, b;
			if (red.getPrimitiveType() == CSS_PERCENTAGE) {
				r = red.getFloatValue(CSS_PERCENTAGE) * 0.01f;
			} else {
				r = red.getFloatValue(CSS_NUMBER);
				r = r / 255f;
			}
			if (green.getPrimitiveType() == CSS_PERCENTAGE) {
				g = green.getFloatValue(CSS_PERCENTAGE) * 0.01f;
			} else {
				g = green.getFloatValue(CSS_NUMBER);
				g = g / 255f;
			}
			if (blue.getPrimitiveType() == CSS_PERCENTAGE) {
				b = blue.getFloatValue(CSS_PERCENTAGE) * 0.01f;
			} else {
				b = blue.getFloatValue(CSS_NUMBER);
				b = b / 255f;
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
					hsl.s = Math.round((max - min) / l * 50f);
				} else {
					hsl.s = Math.round((max - min) / (1f - l) * 50f);
				}
			} else {
				hsl.s = 0;
			}
			hsl.h = Math.round(h);
			if (hsl.h == 360) {
				hsl.h = 0;
			}
			hsl.l = Math.round(l * 100f);
			return hsl;
		}

		String toHSLString() {
			Hsl hsl = toHSL();
			if (hsl == null) {
				return null;
			}
			boolean nonOpaque = getFloatAlpha() != 1f;
			if (commaSyntax) {
				return oldHSLString(hsl, nonOpaque);
			} else {
				return hslString(hsl, nonOpaque);
			}
		}

		private String oldHSLString(Hsl hsl, boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(22);
			if (nonOpaque) {
				buf.append("hsla(");
			} else {
				buf.append("hsl(");
			}
			buf.append(Integer.toString(hsl.h)).append(", ").append(Integer.toString(hsl.s))
					.append('%').append(", ").append(Integer.toString(hsl.l)).append('%');
			if (nonOpaque) {
				buf.append(", ");
				appendAlphaChannel(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private String hslString(Hsl hsl, boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(20);
			buf.append("hsl(").append(Integer.toString(hsl.h)).append(' ').append(Integer.toString(hsl.s))
					.append('%').append(' ').append(Integer.toString(hsl.l)).append('%');
			if (nonOpaque) {
				buf.append(" / ");
				appendAlphaChannel(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		String toHSLMinifiedString() {
			Hsl hsl = toHSL();
			if (hsl == null) {
				return null;
			}
			boolean nonOpaque = getFloatAlpha() != 1f;
			if (commaSyntax) {
				return oldHSLMinifiedString(hsl, nonOpaque);
			} else {
				return hslMinifiedString(hsl, nonOpaque);
			}
		}

		private String oldHSLMinifiedString(Hsl hsl, boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(21);
			if (nonOpaque) {
				buf.append("hsla(");
			} else {
				buf.append("hsl(");
			}
			buf.append(Integer.toString(hsl.h)).append(',').append(Integer.toString(hsl.s))
					.append('%').append(',').append(Integer.toString(hsl.l)).append('%');
			if (nonOpaque) {
				buf.append(',');
				appendAlphaChannelMinified(buf);
			}
			buf.append(')');
			return buf.toString();
		}

		private String hslMinifiedString(Hsl hsl, boolean nonOpaque) {
			StringBuilder buf = new StringBuilder(20);
			buf.append("hsl(").append(Integer.toString(hsl.h)).append(' ').append(Integer.toString(hsl.s))
					.append('%').append(' ').append(Integer.toString(hsl.l)).append('%');
			if (nonOpaque) {
				buf.append('/');
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

		private int colorComponentHashCode(AbstractCSSPrimitiveValue comp) {
			float value;
			if (comp.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
				value = comp.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE) * 2.55f;
			} else {
				value = comp.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
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
			if (Float.floatToIntBits(other.getFloatAlpha()) != Float.floatToIntBits(getFloatAlpha())) {
				return false;
			}
			return true;
		}

		private float getFloatAlpha() {
			return alpha.getFloatValue(CSS_NUMBER);
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

}
