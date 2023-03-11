/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.HWBColor;

class HWBColorImpl extends BaseColor implements HWBColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue hue = null;
	private PrimitiveValue whiteness = null;
	private PrimitiveValue blackness = null;

	HWBColorImpl() {
		super();
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.HWB;
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getHue();
		case 2:
			return getWhiteness();
		case 3:
			return getBlackness();
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
			setHue(component);
			break;
		case 2:
			setWhiteness(component);
			break;
		case 3:
			setBlackness(component);
		}
	}

	@Override
	public PrimitiveValue getHue() {
		return hue;
	}

	public void setHue(PrimitiveValue hue) {
		this.hue = enforceHueComponent(hue);
	}

	@Override
	public PrimitiveValue getWhiteness() {
		return whiteness;
	}

	public void setWhiteness(PrimitiveValue whiteness) {
		this.whiteness = enforcePcntComponent(whiteness);
	}

	@Override
	public PrimitiveValue getBlackness() {
		return blackness;
	}

	public void setBlackness(PrimitiveValue blackness) {
		this.blackness = enforcePcntComponent(blackness);
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getWhiteness()) && isConvertibleComponent(getHue())
				&& isConvertibleComponent(getBlackness());
	}

	@Override
	public String toString() {
		boolean nonOpaque = isNonOpaque();
		StringBuilder buf = new StringBuilder(20);
		buf.append("hwb(");
		appendHue(buf, hue);
		buf.append(' ').append(whiteness.getCssText())
				.append(' ').append(blackness.getCssText());
		if (nonOpaque) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toMinifiedString() {
		boolean nonOpaque = isNonOpaque();
		StringBuilder buf = new StringBuilder(20);
		buf.append("hwb(");
		appendMinifiedHue(buf, hue);
		buf.append(' ').append(whiteness.getMinifiedCssText("color"))
				.append(' ').append(blackness.getMinifiedCssText("color"));
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
		result = prime * result + ((blackness == null) ? 0 : blackness.hashCode());
		result = prime * result + ((hue == null) ? 0 : hue.hashCode());
		result = prime * result + ((whiteness == null) ? 0 : whiteness.hashCode());
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
		HWBColorImpl other = (HWBColorImpl) obj;
		if (blackness == null) {
			if (other.blackness != null) {
				return false;
			}
		} else if (!blackness.equals(other.blackness)) {
			return false;
		}
		if (hue == null) {
			if (other.hue != null) {
				return false;
			}
		} else if (!hue.equals(other.hue)) {
			return false;
		}
		if (whiteness == null) {
			if (other.whiteness != null) {
				return false;
			}
		} else if (!whiteness.equals(other.whiteness)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public HWBColorImpl clone() {
		HWBColorImpl clon = new HWBColorImpl();
		clon.alpha = alpha.clone();
		if (hue != null) {
			clon.hue = hue.clone();
		}
		if (whiteness != null) {
			clon.whiteness = whiteness.clone();
		}
		if (blackness != null) {
			clon.blackness = blackness.clone();
		}
		return clon;
	}

}
