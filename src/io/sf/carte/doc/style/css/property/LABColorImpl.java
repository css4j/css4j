/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

class LABColorImpl extends BaseColor implements LABColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue lightness = null;
	private PrimitiveValue a = null;
	private PrimitiveValue b = null;

	LABColorImpl() {
		super();
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.LAB;
	}

	@Override
	public String getColorSpace() {
		return "lab";
	}

	@Override
	Space getSpace() {
		return Space.CIE_Lab;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		//
		LABColorImpl setfrom = (LABColorImpl) color;
		lightness = setfrom.getLightness();
		a = setfrom.getA();
		b = setfrom.getB();
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getLightness();
		case 2:
			return getA();
		case 3:
			return getB();
		}
		return null;
	}

	@Override
	void setComponent(int index, PrimitiveValue component) {
		switch (index) {
		case 0:
			setAlpha(component);
			break;
		case 1:
			setLightness(component);
			break;
		case 2:
			setA(component);
			break;
		case 3:
			setB(component);
		}
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
	public PrimitiveValue getA() {
		return a;
	}

	public void setA(PrimitiveValue a) {
		checkNumberComponent(a);
		this.a = a;
	}

	@Override
	public PrimitiveValue getB() {
		return b;
	}

	public void setB(PrimitiveValue b) {
		checkNumberComponent(b);
		this.b = b;
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getA()) && isConvertibleComponent(getB())
				&& isConvertibleComponent(getLightness());
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
		wri.write("lab(");
		lightness.writeCssText(wri);
		wri.write(' ');
		a.writeCssText(wri);
		wri.write(' ');
		b.writeCssText(wri);
		if (isNonOpaque()) {
			wri.write(" / ");
			appendAlphaChannel(wri);
		}
		wri.write(')');
	}

	@Override
	public String toMinifiedString() {
		StringBuilder buf = new StringBuilder(20);
		buf.append("lab(");
		buf.append(lightness.getMinifiedCssText("color")).append(' ').append(a.getMinifiedCssText("color")).append(' ')
				.append(b.getMinifiedCssText("color"));
		if (isNonOpaque()) {
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
		result = prime * result + ((lightness == null) ? 0 : lightness.hashCode());
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
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
		LABColorImpl other = (LABColorImpl) obj;
		if (lightness == null) {
			if (other.lightness != null) {
				return false;
			}
		} else if (!lightness.equals(other.lightness)) {
			return false;
		}
		if (a == null) {
			if (other.a != null) {
				return false;
			}
		} else if (!a.equals(other.a)) {
			return false;
		}
		if (b == null) {
			if (other.b != null) {
				return false;
			}
		} else if (!b.equals(other.b)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public LABColorImpl clone() {
		LABColorImpl clon = new LABColorImpl();
		clon.alpha = alpha.clone();
		if (lightness != null) {
			clon.lightness = lightness.clone();
		}
		if (a != null) {
			clon.a = a.clone();
		}
		if (b != null) {
			clon.b = b.clone();
		}
		return clon;
	}

}
