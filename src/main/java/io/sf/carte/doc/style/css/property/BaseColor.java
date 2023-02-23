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
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.util.SimpleWriter;

abstract class BaseColor implements CSSColor, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 3L;

	enum Space {
		sRGB, p3, A98_RGB, ProPhoto_RGB, Rec2020, CIE_XYZ, CIE_Lab, CIE_LCh, OK_Lab, OK_LCh, OTHER
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
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			float fv = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER);
			if (fv < 0f || fv > 1f) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Alpha channel cannot be smaller than zero or greater than 1.");
			}
		} else if (alpha.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			float fv = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_PERCENTAGE);
			if (fv < 0f || fv > 100f) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Alpha channel percentage cannot be smaller than zero or greater than 100%.");
			}
		} else if(alpha.getCssValueType() != CssType.PROXY && alpha.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with alpha.");
		}
		this.alpha = alpha;
	}

	static void checkPcntComponent(PrimitiveValue primi) throws DOMException {
		if (primi == null) {
			throw new NullPointerException();
		}
		if (primi.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			float fv = ((CSSTypedValue) primi).getFloatValue(CSSUnit.CSS_PERCENTAGE);
			if (fv < 0f || fv > 100f) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Color component percentage cannot be smaller than zero or greater than 100%.");
			}
		} else if (primi.getCssValueType() != CssType.PROXY
				 && primi.getPrimitiveType() != Type.EXPRESSION){
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Invalid color component: " + primi.getCssText());
		}
	}

	static void checkNumberComponent(PrimitiveValue primi) {
		if (primi == null) {
			throw new NullPointerException();
		}
		if (primi.getUnitType() != CSSUnit.CSS_NUMBER
				&& primi.getCssValueType() != CssType.PROXY && primi.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible: " + primi.getCssText());
		}
	}

	static void checkHueComponent(PrimitiveValue hue) {
		if (hue == null) {
			throw new NullPointerException();
		}
		if (hue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(hue.getUnitType())
				&& hue.getCssValueType() != CssType.PROXY && hue.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with hue.");
		}
	}

	abstract boolean hasConvertibleComponents();

	static boolean isConvertibleComponent(CSSPrimitiveValue comp) {
		return comp != null && comp.getPrimitiveType() == Type.NUMERIC;
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
			float f = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER);
			String text = formattedNumber(f);
			wri.write(text);
		} else {
			alpha.writeCssText(wri);
		}
	}

	StringBuilder appendAlphaChannel(StringBuilder buf) {
		String text;
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			float f = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER);
			text = formattedNumber(f);
		} else {
			text = alpha.getCssText();
		}
		return buf.append(text);
	}

	StringBuilder appendAlphaChannelMinified(StringBuilder buf) {
		String text;
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			float f = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER);
			text = formattedNumberMinified(f);
		} else {
			text = alpha.getMinifiedCssText("");
		}
		return buf.append(text);
	}

	private String formattedNumber(float f) {
		NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
		format.setMaximumFractionDigits(4);
		format.setMinimumFractionDigits(0);
		return format.format(f);
	}

	private String formattedNumberMinified(float f) {
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

	static void setLabColor(float[] lab, PrimitiveValue alpha, LABColorImpl labColor) {
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, lab[0]);
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, lab[1]);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, lab[2]);
		//
		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();
		//
		labColor.setLightness(primiL);
		labColor.setA(primia);
		labColor.setB(primib);
		labColor.alpha = alpha.clone();
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

	@Override
	abstract public BaseColor clone();

}
