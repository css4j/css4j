/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.List;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.XYZColor;

class XYZColorImpl extends BaseColor implements XYZColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue x = null;
	private PrimitiveValue y = null;
	private PrimitiveValue z = null;

	XYZColorImpl(List<PrimitiveValue> components) {
		super();
		setComponents(components);
	}

	XYZColorImpl(XYZColorImpl copyMe) {
		super();
		if (copyMe.x != null) {
			x = copyMe.x.clone();
		}
		if (copyMe.y != null) {
			y = copyMe.y.clone();
		}
		if (copyMe.z != null) {
			z = copyMe.z.clone();
		}
		alpha = copyMe.alpha.clone();
	}

	private void setComponents(List<PrimitiveValue> components) {
		// First component is mandatory
		x = components.get(0);
		try {
			y = components.get(1);
		} catch (IndexOutOfBoundsException e) {
			y = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			z = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			return;
		}
		try {
			z = components.get(2);
		} catch (IndexOutOfBoundsException e) {
			z = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
		}
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.XYZ;
	}

	@Override
	public String getColorSpace() {
		return ColorSpace.xyz;
	}

	@Override
	Space getSpace() {
		return Space.CIE_XYZ;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		//
		XYZColorImpl xyzcolor = (XYZColorImpl) color;
		this.x = xyzcolor.x;
		this.y = xyzcolor.y;
		this.z = xyzcolor.z;
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getX();
		case 2:
			return getY();
		case 3:
			return getZ();
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
			setX(component);
			break;
		case 2:
			setY(component);
			break;
		case 3:
			setZ(component);
		}
	}

	@Override
	public PrimitiveValue getX() {
		return x;
	}

	public void setX(PrimitiveValue x) {
		checkAxisComponent(x);
		this.x = x;
	}

	@Override
	public PrimitiveValue getY() {
		return y;
	}

	public void setY(PrimitiveValue y) {
		checkAxisComponent(y);
		this.y = y;
	}

	@Override
	public PrimitiveValue getZ() {
		return z;
	}

	public void setZ(PrimitiveValue z) {
		checkAxisComponent(z);
		this.z = z;
	}

	private void checkAxisComponent(PrimitiveValue axis) {
		if (axis == null) {
			throw new NullPointerException();
		}
		if (axis.getUnitType() != CSSUnit.CSS_NUMBER
				&& axis.getCssValueType() != CssType.PROXY && axis.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible.");
		}
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getX()) && isConvertibleComponent(getY())
				&& isConvertibleComponent(getZ());
	}

	RGBAColor toSRGB(boolean clamp) {
		float[] rgb = new float[3];
		double[] xyz = new double[3];
		xyz[0] = ((TypedValue) this.x).getFloatValue(CSSUnit.CSS_NUMBER);
		xyz[1] = ((TypedValue) this.y).getFloatValue(CSSUnit.CSS_NUMBER);
		xyz[2] = ((TypedValue) this.z).getFloatValue(CSSUnit.CSS_NUMBER);
		ColorUtil.xyzToSRGB(xyz[0], xyz[1], xyz[2], rgb);
		// range check
		if (!ColorUtil.rangeRoundCheck(rgb) && clamp) {
			float[] lab = new float[3];
			ColorUtil.xyzToLab(xyz, lab);
			rgb = ColorUtil.clampRGB(lab[0], lab[1], lab[2], rgb);
		}
		// Set the RGBColor
		RGBColor color = new RGBColor();
		color.alpha = getAlpha().clone();
		NumberValue red = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[0] * 100f);
		NumberValue green = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[1] * 100f);
		NumberValue blue = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[2] * 100f);
		red.setAbsolutizedUnit();
		green.setAbsolutizedUnit();
		blue.setAbsolutizedUnit();
		color.setRed(red);
		color.setGreen(green);
		color.setBlue(blue);
		//
		return color;
	}

	void toLABColor(LABColorImpl color) throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		double[] xyz = new double[3];
		xyz[0] = ((TypedValue) this.x).getFloatValue(CSSUnit.CSS_NUMBER);
		xyz[1] = ((TypedValue) this.y).getFloatValue(CSSUnit.CSS_NUMBER);
		xyz[2] = ((TypedValue) this.z).getFloatValue(CSSUnit.CSS_NUMBER);
		float[] lab = new float[3];
		ColorUtil.xyzToLab(xyz, lab);
		setLabColor(lab, alpha, color);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((z == null) ? 0 : z.hashCode());
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
		XYZColorImpl other = (XYZColorImpl) obj;
		if (x == null) {
			if (other.x != null) {
				return false;
			}
		} else if (!x.equals(other.x)) {
			return false;
		}
		if (y == null) {
			if (other.y != null) {
				return false;
			}
		} else if (!y.equals(other.y)) {
			return false;
		}
		if (z == null) {
			if (other.z != null) {
				return false;
			}
		} else if (!z.equals(other.z)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public XYZColorImpl clone() {
		return new XYZColorImpl(this);
	}

}
