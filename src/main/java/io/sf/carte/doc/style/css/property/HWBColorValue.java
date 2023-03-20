/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.HWBColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * HWB color value.
 */
public class HWBColorValue extends ColorValue implements io.sf.carte.doc.style.css.HWBColorValue {

	private static final long serialVersionUID = 1L;

	private final HWBColorImpl hwbColor;

	public HWBColorValue() {
		this(new HWBColorImpl());
	}

	HWBColorValue(HWBColorImpl color) {
		super();
		hwbColor = color;
	}

	HWBColorValue(HWBColorValue copied) {
		super(copied);
		this.hwbColor = copied.hwbColor.clone();
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return CSSColorValue.ColorModel.HWB;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		HWBColorImpl setfrom = ((HWBColorValue) value).hwbColor;
		this.hwbColor.setHue(setfrom.getHue());
		this.hwbColor.setWhiteness(setfrom.getWhiteness());
		this.hwbColor.setBlackness(setfrom.getBlackness());
		this.hwbColor.alpha = setfrom.alpha;
	}

	@Override
	public String getCssText() {
		return hwbColor.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		String css = hwbColor.toMinifiedString();
		if (hwbColor.getAlpha().getPrimitiveType() == Type.NUMERIC
				&& ((CSSTypedValue) hwbColor.getAlpha()).getFloatValue(CSSUnit.CSS_NUMBER) == 1f
				&& hwbColor.getHue().getPrimitiveType() == Type.NUMERIC
				&& hwbColor.getWhiteness().getPrimitiveType() == Type.NUMERIC
				&& hwbColor.getBlackness().getPrimitiveType() == Type.NUMERIC) {
			String rgbCss = ((CSSRGBColor) toRGBColor()).toMinifiedString();
			if (rgbCss.length() < css.length()) {
				css = rgbCss;
			}
		}
		return css;
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		return hwbColor.item(index);
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		hwbColor.setComponent(index, (PrimitiveValue) component);
	}

	@Override
	boolean hasConvertibleComponents() {
		return hwbColor.hasConvertibleComponents();
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		double[] rgb = hwbColor.toSRGB(false);

		CSSRGBColor color = new CSSRGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(hwbColor.alpha.clone());
		return color;
	}

	@Override
	public HWBColor getColor() {
		return hwbColor;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.HWBCOLOR) {
					setLexicalHWB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No hwb() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalHWB(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// hue
			PrimitiveValue primihue = factory.createCSSPrimitiveValue(lu, true);
			// whiteness
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primiwhite = factory.createCSSPrimitiveValue(lu, true);
			checkPcntCompValidity(primiwhite, lunit);
			// blackness
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primiblackness = factory.createCSSPrimitiveValue(lu, true);
			checkPcntCompValidity(primiblackness, lunit);
			// slash or null
			lu = lu.getNextLexicalUnit();
			PrimitiveValue alpha = null;
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				alpha = factory.createCSSPrimitiveValue(lu, true);
				hwbColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
			hwbColor.setHue(primihue);
			hwbColor.setWhiteness(primiwhite);
			hwbColor.setBlackness(primiblackness);
		}

	}

	private static void checkPcntCompValidity(PrimitiveValue primisat, LexicalUnit lunit) {
		if (primisat.getUnitType() != CSSUnit.CSS_PERCENTAGE
				&& primisat.getCssValueType() != CssType.PROXY
				&& primisat.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible: " + lunit.toString());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + hwbColor.hashCode();
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
		if (!(obj instanceof HWBColorValue)) {
			return false;
		}
		HWBColorValue other = (HWBColorValue) obj;
		return hwbColor.equals(other.hwbColor);
	}

	@Override
	public HWBColorValue clone() {
		return new HWBColorValue(this);
	}

}
