/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.HSLColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * HSL color value.
 */
public class HSLColorValue extends ColorValue implements io.sf.carte.doc.style.css.HSLColorValue {

	private static final long serialVersionUID = 1L;

	private final HSLColorImpl hslColor;

	public HSLColorValue() {
		super();
		hslColor = new MyHSLColorImpl();
	}

	HSLColorValue(HSLColorImpl color) {
		super();
		hslColor = color;
	}

	HSLColorValue(HSLColorValue copied) {
		super(copied);
		this.hslColor = copied.hslColor.clone();
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return CSSColorValue.ColorModel.HSL;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		HSLColorValue setfrom = (HSLColorValue) value;
		this.hslColor.setHue(setfrom.hslColor.getHue());
		this.hslColor.setSaturation(setfrom.hslColor.getSaturation());
		this.hslColor.setLightness(setfrom.hslColor.getLightness());
		this.hslColor.alpha = setfrom.hslColor.alpha;
	}

	@Override
	public String getCssText() {
		return hslColor.toString(commaSyntax);
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		String css = hslColor.toMinifiedString(commaSyntax);
		if (!hslColor.isNonOpaque() && hasConvertibleComponents()) {
			String rgbCss = ((CSSRGBColor) toRGBColor()).toMinifiedString();
			if (rgbCss.length() < css.length() - 5) {
				// The RGB serialization is significantly smaller
				css = rgbCss;
			}
		}
		return css;
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		return hslColor.item(index);
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		hslColor.setComponent(index, (PrimitiveValue) component);
	}

	@Override
	public HSLColorValue toHSLColorValue() throws DOMException {
		return this;
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		double[] rgb = hslColor.toSRGB(false);
		CSSRGBColor color = new CSSRGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(hslColor.alpha.clone());
		return color;
	}

	@Override
	boolean hasConvertibleComponents() {
		return hslColor.hasConvertibleComponents();
	}

	@Override
	public HSLColor getColor() {
		return hslColor;
	}

	HSLColorImpl getHSLColorImpl() {
		return hslColor;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.HSLCOLOR) {
					setLexicalHSL(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No hsl() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalHSL(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// hue
			PrimitiveValue primihue = factory.createCSSPrimitiveValue(lu, true);
			// comma
			lu = lu.getNextLexicalUnit();
			if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.LexicalType.OPERATOR_COMMA) {
				// saturation
				lu = lu.getNextLexicalUnit();
			}
			PrimitiveValue primisat = factory.createCSSPrimitiveValue(lu, true);
			if (commaSyntax) {
				// comma
				lu = lu.getNextLexicalUnit();
			}
			// lightness
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primilight = factory.createCSSPrimitiveValue(lu, true);
			// comma, slash or null
			lu = lu.getNextLexicalUnit();
			PrimitiveValue alpha = null;
			if (lu != null) {
				if (commaSyntax) {
					if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_COMMA) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
					}
				} else if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				alpha = factory.createCSSPrimitiveValue(lu, true);
				hslColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
			hslColor.setHue(primihue);
			hslColor.setSaturation(primisat);
			hslColor.setLightness(primilight);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + hslColor.hashCode();
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
		if (!(obj instanceof HSLColorValue)) {
			return false;
		}
		HSLColorValue other = (HSLColorValue) obj;
		return hslColor.equals(other.hslColor);
	}

	@Override
	public HSLColorValue clone() {
		return new HSLColorValue(this);
	}

}
