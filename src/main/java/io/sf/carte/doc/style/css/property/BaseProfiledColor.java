/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.util.Arrays;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSUnit;

/**
 * A color specified through the {@code color()} function, where the profile is
 * not available at object creation time.
 */
class BaseProfiledColor extends BaseColor {

	private static final long serialVersionUID = 1L;

	private String profileName;

	private PrimitiveValue[] components;

	BaseProfiledColor(String colorSpace) {
		this(colorSpace, new PrimitiveValue[0]);
	}

	BaseProfiledColor(String colorSpace, PrimitiveValue[] components) {
		super();
		this.profileName = colorSpace;
		this.components = components;
	}

	BaseProfiledColor(BaseProfiledColor copyMe) {
		super();
		this.alpha = copyMe.alpha.clone();
		this.profileName = copyMe.profileName;
		this.components = new PrimitiveValue[copyMe.components.length];
		for (int i = 0; i < components.length; i++) {
			components[i] = copyMe.components[i].clone();
		}
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.PROFILE;
	}

	@Override
	public String getColorSpace() {
		return profileName;
	}

	@Override
	Space getSpace() {
		return Space.OTHER;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);

		BaseProfiledColor setfrom = (BaseProfiledColor) color;
		if (setfrom.components.length != this.components.length) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"This value can only be set to a color in the " + getColorModel() + " color model with "
							+ this.components.length + " components.");
		}
		components = new PrimitiveValue[setfrom.components.length];
		for (int i = 0; i < components.length; i++) {
			components[i] = setfrom.components[i].clone();
		}
		this.profileName = color.getColorSpace();
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

	@Override
	public int getLength() {
		return components.length + 1;
	}

	@Override
	public boolean hasConvertibleComponents() {
		for (PrimitiveValue comp : components) {
			if (!isConvertibleComponent(comp)) {
				return false;
			}
		}
		return true;
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
	void setColorComponents(double[] components) {
		if (components.length > this.components.length) {
			this.components = new PrimitiveValue[components.length];
		}

		for (int i = 0; i < components.length; i++) {
			double comp = components[i];
			NumberValue c = new NumberValue();
			c.setFloatValue(CSSUnit.CSS_NUMBER, (float) comp);
			c.setSubproperty(true);
			c.setAbsolutizedUnit();
			c.setMaximumFractionDigits(6);
			this.components[i] = c;
		}

		for (int i = components.length; i < this.components.length; i++) {
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

		double[] comps = new double[components.length];
		for (int i = 0; i < components.length; i++) {
			comps[i] = ColorUtil.floatNumber((TypedValue) components[i]);
		}
		return comps;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot convert profiled colors.");
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot convert profiled colors.");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(components);
		result = prime * result + Objects.hash(profileName);
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
		BaseProfiledColor other = (BaseProfiledColor) obj;
		return Arrays.equals(components, other.components)
				&& Objects.equals(profileName, other.profileName);
	}

	@Override
	public ColorValue packInValue() {
		return new ColorFunction(this);
	}

	@Override
	public BaseProfiledColor clone() {
		return new BaseProfiledColor(this);
	}

}
