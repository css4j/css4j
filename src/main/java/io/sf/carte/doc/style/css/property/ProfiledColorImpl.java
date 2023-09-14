/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Arrays;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.property.ColorProfile.Illuminant;

class ProfiledColorImpl extends BaseColor {

	private static final long serialVersionUID = 1L;

	private String colorSpace;

	private PrimitiveValue[] components;

	ProfiledColorImpl(String colorSpace) {
		this(colorSpace, new PrimitiveValue[0]);
	}

	ProfiledColorImpl(String colorSpace, PrimitiveValue[] components) {
		super();
		this.colorSpace = colorSpace;
		this.components = components;
	}

	ProfiledColorImpl(ProfiledColorImpl copyMe) {
		super();
		this.alpha = copyMe.alpha;
		this.colorSpace = copyMe.colorSpace;
		this.components = copyMe.components.clone();
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.PROFILE;
	}

	@Override
	public String getColorSpace() {
		return colorSpace;
	}

	@Override
	Space getSpace() {
		return Space.OTHER;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		//
		ProfiledColorImpl setfrom = (ProfiledColorImpl) color;
		if (setfrom.components.length != this.components.length) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"This value can only be set to a color in the " + getColorModel() + " color model with "
							+ this.components.length + " components.");
		}
		System.arraycopy(setfrom.components, 0, components, 0, this.components.length);
		this.colorSpace = color.getColorSpace();
	}

	@Override
	public PrimitiveValue item(int index) {
		if (index == 0) {
			return getAlpha();
		}
		try {
			return components[index - 1];
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Set the component of this color located at {@code index}.
	 * 
	 * @param index the index.
	 * @param component the component value.
	 */
	@Override
	void setComponent(int index, PrimitiveValue component) {
		if (index == 0) {
			setAlpha(component);
		} else {
			try {
				components[index - 1] = component;
			} catch (IndexOutOfBoundsException e) {
			}
		}
	}

	@Override
	public int getLength() {
		return components.length + 1;
	}

	@Override
	public boolean hasConvertibleComponents() {
		final int len = components.length;
		for (int i = 0; i < len; i++) {
			if (!isConvertibleComponent(components[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	void setColorComponents(double[] comp) {
		if (components.length == 0) {
			components = new PrimitiveValue[comp.length];
		}

		for (int i = 0; i < comp.length; i++) {
			NumberValue c = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) comp[i]);
			c.setSubproperty(true);
			c.setAbsolutizedUnit();
			this.components[i] = c;
		}

		for (int i = comp.length; i < components.length; i++) {
			NumberValue c = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			c.setSubproperty(true);
			this.components[i] = c;
		}
	}

	@Override
	public double[] toNumberArray() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double[] comp = new double[components.length];
		for (int i = 0; i < comp.length; i++) {
			comp[i] = ((TypedValue) components[i]).getFloatValue(CSSUnit.CSS_NUMBER);
		}
		return comp;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot convert profiled colors.");
	}

	@Override
	double[] toXYZ(Illuminant white) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot convert profiled colors.");
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		buf.append("color(").append(getColorSpace());
		for (PrimitiveValue component : components) {
			buf.append(' ');
			appendComponentCssText(buf, component);
		}
		if (isNonOpaque()) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toMinifiedString() {
		StringBuilder buf = new StringBuilder(58);
		buf.append("color(").append(getColorSpace());
		for (PrimitiveValue component : components) {
			buf.append(' ');
			appendComponentMinifiedCssText(buf, component);
		}
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
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(components);
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProfiledColorImpl other = (ProfiledColorImpl) obj;
		return Arrays.equals(components, other.components);
	}

	@Override
	public ColorValue packInValue() {
		return new ColorFunction(this);
	}

	@Override
	public ProfiledColorImpl clone() {
		return new ProfiledColorImpl(this);
	}

}
