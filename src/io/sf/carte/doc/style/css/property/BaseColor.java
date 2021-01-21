/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.util.SimpleWriter;

abstract class BaseColor implements Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	PrimitiveValue alpha = ColorValue.opaqueAlpha;

	BaseColor() {
		super();
	}

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

	void checkPcntComponent(PrimitiveValue primi) throws DOMException {
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
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(0);
		return format.format(f);
	}

	private String formattedNumberMinified(float f) {
		NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(0);
		format.setMinimumIntegerDigits(0);
		return format.format(f);
	}

	void appendHue(StringBuilder buf, PrimitiveValue hue) {
		short unit = hue.getUnitType();
		if (unit == CSSUnit.CSS_DEG || unit == CSSUnit.CSS_GRAD) {
			CSSTypedValue number = (CSSTypedValue) hue;
			float val = number.getFloatValue(unit);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(4);
			String s = format.format(val);
			buf.append(s);
			if (unit == CSSUnit.CSS_GRAD) {
				buf.append("grad");
			}
		} else {
			buf.append(hue.getCssText());
		}
	}

	void writeHue(SimpleWriter wri, PrimitiveValue hue) throws IOException {
		short unit = hue.getUnitType();
		if (unit == CSSUnit.CSS_DEG || unit == CSSUnit.CSS_GRAD) {
			CSSTypedValue number = (CSSTypedValue) hue;
			float val = number.getFloatValue(unit);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(4);
			String s = format.format(val);
			wri.write(s);
			if (unit == CSSUnit.CSS_GRAD) {
				wri.write("grad");
			}
		} else {
			hue.writeCssText(wri);
		}
	}

	void appendMinifiedHue(StringBuilder buf, PrimitiveValue hue) {
		short unit = hue.getUnitType();
		if (unit == CSSUnit.CSS_DEG || unit == CSSUnit.CSS_GRAD) {
			CSSTypedValue number = (CSSTypedValue) hue;
			float val = number.getFloatValue(unit);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumIntegerDigits(0);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(4);
			String s = format.format(val);
			buf.append(s);
			if (unit == CSSUnit.CSS_GRAD) {
				buf.append("grad");
			}
		} else {
			buf.append(hue.getMinifiedCssText("color"));
		}
	}

}
