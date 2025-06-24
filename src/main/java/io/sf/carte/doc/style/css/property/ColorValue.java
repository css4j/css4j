/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
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
					value = factory.parseProperty("#0000");
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

		RGBColorValue rgb1;
		LABColor lab1, lab2;
		RGBAColor color1;

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
		boolean isCommaSyntax() {
			return commaSyntax;
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

	/*
	 * Utility methods for lexical setting.
	 */

	TypedValue absoluteComponent(CSSColor from, CSSPrimitiveValue primi, boolean range) {
		switch (primi.getPrimitiveType()) {
		case IDENT:
			String s = ((CSSTypedValue) primi).getStringValue();
			s = s.toLowerCase(Locale.ROOT);
			if (!"none".equals(s)) {
				CSSNumberValue comp = from.component(s);
				if (comp != null) {
					if (range) {
						comp = parameterRange(comp);
					}
					return (TypedValue) comp;
				}
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Invalid color component: " + primi.getCssText());
			}
			break;
		case EXPRESSION:
			Evaluator eval = createEvaluator(from, range);
			CSSNumberValue number = eval.evaluateExpression((CSSExpressionValue) primi);
			number.setMaximumFractionDigits(5);
			primi = number;
			break;
		case MATH_FUNCTION:
			eval = createEvaluator(from, range);
			number = eval.evaluateFunction((CSSMathFunctionValue) primi);
			number.setMaximumFractionDigits(5);
			primi = number;
			break;
		default:
			if (primi.getCssValueType() != CssType.TYPED) {
				if (primi.getCssValueType() == CssType.PROXY
						&& primi.getPrimitiveType() != Type.ENV) {
					throw new CSSLexicalProcessingException();
				}
				throw invalidValueException(primi);
			}
			break;
		}
		return (TypedValue) primi;
	}

	/**
	 * Convert the component from {@code [0-1]} to the value range expected in
	 * parameters by functions and expressions.
	 * 
	 * @param comp the non-angular component.
	 * @return the component in the expected range.
	 */
	CSSNumberValue parameterRange(CSSNumberValue comp) {
		return comp;
	}

	private Evaluator createEvaluator(CSSColor from, boolean pcnt) {
		return new PercentageEvaluator() {

			@Override
			protected CSSTypedValue replaceParameter(String identifier) throws DOMException {
				identifier = identifier.toLowerCase(Locale.ROOT);
				CSSNumberValue number = from.component(identifier);
				if (number == null) {
					return super.replaceParameter(identifier);
				}
				if (pcnt) {
					number = parameterRange(number);
				}
				return number;
			}

		};
	}

	static PrimitiveValue absoluteHue(CSSColor from, CSSPrimitiveValue primi) {
		switch (primi.getPrimitiveType()) {
		case IDENT:
			String s = ((CSSTypedValue) primi).getStringValue();
			s = s.toLowerCase(Locale.ROOT);
			if (!"none".equals(s)) {
				CSSNumberValue comp = from.component(s);
				if (comp != null) {
					return (PrimitiveValue) comp;
				}
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Invalid color component: " + primi.getCssText());
			}
			break;
		case EXPRESSION:
			Evaluator eval = createHueEvaluator(from);
			primi = eval.evaluateExpression((CSSExpressionValue) primi);
			break;
		case MATH_FUNCTION:
			eval = createHueEvaluator(from);
			primi = eval.evaluateFunction((CSSMathFunctionValue) primi);
			break;
		default:
			break;
		}
		return (PrimitiveValue) primi;
	}

	private static Evaluator createHueEvaluator(CSSColor from) {
		return new Evaluator(CSSUnit.CSS_DEG) {

			@Override
			protected CSSTypedValue replaceParameter(String identifier) throws DOMException {
				identifier = identifier.toLowerCase(Locale.ROOT);
				CSSNumberValue number = from.component(identifier);
				if (number == null) {
					return super.replaceParameter(identifier);
				}
				return number;
			}

		};
	}

	static CSSColor computeColor(PrimitiveValue rawcolor, ValueFactory factory)
			throws DOMException {
		if (rawcolor instanceof CSSColorValue) {
			return ((CSSColorValue) rawcolor).getColor();
		} else {
			if (rawcolor.getCssValueType() == CSSValue.CssType.TYPED) {
				TypedValue typed = (TypedValue) rawcolor;
				if (rawcolor.getPrimitiveType() == Type.IDENT) {
					String s = typed.getStringValue();
					s = ColorIdentifiers.getInstance().getColor(s);
					if (s != null) {
						try {
							typed = (TypedValue) factory.parseProperty(s);
							return ((CSSColorValue) typed).getColor();
						} catch (DOMException e) {
							// This won't happen
						}
					}
				}
			}
		}
		throw invalidValueException(rawcolor);
	}

	static LexicalUnit nextLexicalUnit(LexicalUnit lu, LexicalUnit firstUnit) throws DOMException {
		lu = lu.getNextLexicalUnit();
		if (lu == null) {
			throw invalidValueException(firstUnit);
		}
		return lu;
	}

	private static DOMException invalidValueException(CSSValue value) {
		return invalidValueException(value.getCssText());
	}

	static DOMException invalidValueException(LexicalUnit lunit) {
		return invalidValueException(lunit.toString());
	}

	private static DOMException invalidValueException(String value) {
		return new DOMSyntaxException("Invalid value: " + value);
	}

	/*
	 * End of utility methods for lexical setting.
	 */

}
