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
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;
import io.sf.carte.doc.style.css.LCHColor;

class LCHColorImpl extends BaseColor implements LCHColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue lightness = null;
	private PrimitiveValue chroma = null;
	private PrimitiveValue hue = null;

	LCHColorImpl() {
		super();
	}

	@Override
	public PrimitiveValue getLightness() {
		return lightness;
	}

	public void setLightness(PrimitiveValue lightness) {
		checkPcntComponent(lightness);
		this.lightness = lightness;
	}

	@Override
	public PrimitiveValue getChroma() {
		return chroma;
	}

	public void setChroma(PrimitiveValue chroma) {
		if (chroma == null) {
			throw new NullPointerException();
		}
		if (chroma.getUnitType() != CSSUnit.CSS_NUMBER
				&& chroma.getCssValueType() != CssType.PROXY && chroma.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible.");
		}
		this.chroma = chroma;
	}

	@Override
	public PrimitiveValue getHue() {
		return hue;
	}

	public void setHue(PrimitiveValue hue) {
		if (hue == null) {
			throw new NullPointerException();
		}
		if (hue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(hue.getUnitType())
				&& hue.getCssValueType() != CssType.PROXY && hue.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with hue.");
		}
		this.hue = hue;
	}

	@Override
	public String toString() {
		BufferSimpleWriter wri = new BufferSimpleWriter();
		try {
			writeCssText(wri);
		} catch (IOException e) {
		}
		return wri.toString();
	}

	void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("lch(");
		lightness.writeCssText(wri);
		wri.write(' ');
		chroma.writeCssText(wri);
		wri.write(' ');
		writeHue(wri);
		if (isNonOpaque()) {
			wri.write(" / ");
			appendAlphaChannel(wri);
		}
		wri.write(')');
	}

	private void writeHue(SimpleWriter wri) throws IOException {
		if (hue.getUnitType() == CSSUnit.CSS_DEG) {
			float val = ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumFractionDigits(0);
			int mxfd;
			if (((CSSTypedValue) hue).isCalculatedNumber()) {
				mxfd = 3;
			} else {
				mxfd = 4;
			}
			format.setMaximumFractionDigits(mxfd);
			String s = format.format(val);
			wri.write(s);
		} else {
			hue.writeCssText(wri);
		}
	}

	public String toMinifiedString() {
		StringBuilder buf = new StringBuilder(20);
		buf.append("lch(").append(lightness.getMinifiedCssText("color"));
		buf.append(' ').append(chroma.getMinifiedCssText("color")).append(' ');
		appendMinifiedHue(buf);
		if (isNonOpaque()) {
			buf.append('/');
			appendAlphaChannelMinified(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	private void appendMinifiedHue(StringBuilder buf) {
		if (hue.getUnitType() == CSSUnit.CSS_DEG) {
			float val = ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumIntegerDigits(0);
			format.setMinimumFractionDigits(0);
			int mxfd;
			if (((CSSTypedValue) hue).isCalculatedNumber()) {
				mxfd = 3;
			} else {
				mxfd = 4;
			}
			format.setMaximumFractionDigits(mxfd);
			String s = format.format(val);
			buf.append(s);
		} else {
			buf.append(hue.getMinifiedCssText("color"));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lightness == null) ? 0 : lightness.hashCode());
		result = prime * result + ((chroma == null) ? 0 : chroma.hashCode());
		result = prime * result + ((hue == null) ? 0 : hue.hashCode());
		result = prime * result + alpha.hashCode();
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		LCHColorImpl other = (LCHColorImpl) obj;
		if (lightness == null) {
			if (other.lightness != null) {
				return false;
			}
		} else if (!lightness.equals(other.lightness)) {
			return false;
		}
		if (chroma == null) {
			if (other.chroma != null) {
				return false;
			}
		} else if (!chroma.equals(other.chroma)) {
			return false;
		}
		if (hue == null) {
			if (other.hue != null) {
				return false;
			}
		} else if (!hue.equals(other.hue)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public LCHColorImpl clone() {
		LCHColorImpl clon = new LCHColorImpl();
		clon.alpha = alpha.clone();
		if (lightness != null) {
			clon.lightness = lightness.clone();
		}
		if (chroma != null) {
			clon.chroma = chroma.clone();
		}
		if (hue != null) {
			clon.hue = hue.clone();
		}
		return clon;
	}

}
