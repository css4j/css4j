/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * RGB color value.
 */
public class RGBColorValue extends ColorValue implements io.sf.carte.doc.style.css.RGBColorValue {

	private final CSSRGBColor color;

	RGBColorValue() {
		super();
		color = new CSSRGBColor();
	}

	protected RGBColorValue(RGBColorValue copied) {
		super(copied);
		this.color = copied.color.clone();
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		RGBColorValue setfrom = (RGBColorValue) value;
		this.color.alpha = setfrom.color.alpha;
		this.color.setRed(setfrom.color.getRed());
		this.color.setGreen(setfrom.color.getGreen());
		this.color.setBlue(setfrom.color.getBlue());
	}

	@Override
	public CSSColorValue.ColorSpace getColorSpace() {
		return CSSColorValue.ColorSpace.RGB;
	}

	@Override
	public RGBAColor getRGBColorValue() {
		return color;
	}

	@Override
	public RGBAColor toRGBColorValue() throws DOMException {
		if (color.getRed() == null || color.getGreen() == null || color.getBlue() == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Color not set");
		}
		return color;
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		switch (index) {
		case 0:
			return color.alpha;
		case 1:
			return color.getRed();
		case 2:
			return color.getGreen();
		case 3:
			return color.getBlue();
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		switch (index) {
		case 0:
			color.setAlpha((PrimitiveValue) component);
			break;
		case 1:
			color.setRed((PrimitiveValue) component);
			break;
		case 2:
			color.setGreen((PrimitiveValue) component);
			break;
		case 3:
			color.setBlue((PrimitiveValue) component);
		}
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR) {
					setLexicalRGB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No rgb() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalRGB(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// red
			PrimitiveValue basiccolor = factory.createCSSPrimitiveValue(lu, true);
			color.setRed(basiccolor);
			// comma ?
			lu = lu.getNextLexicalUnit();
			if (commaSyntax = lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
				// green
				lu = lu.getNextLexicalUnit();
			}
			basiccolor = factory.createCSSPrimitiveValue(lu, true);
			color.setGreen(basiccolor);
			if (commaSyntax) {
				// comma
				lu = lu.getNextLexicalUnit();
			}
			// blue
			lu = lu.getNextLexicalUnit();
			basiccolor = factory.createCSSPrimitiveValue(lu, true);
			color.setBlue(basiccolor);
			// comma, slash or null
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				// alpha
				lu = lu.getNextLexicalUnit();
				color.setAlpha(factory.createCSSPrimitiveValue(lu, true));
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
		}

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
		if (!(obj instanceof RGBColorValue)) {
			return false;
		}
		RGBColorValue other = (RGBColorValue) obj;
		return color.equals(other.color);
	}

	@Override
	public RGBColorValue clone() {
		return new RGBColorValue(this);
	}

}
