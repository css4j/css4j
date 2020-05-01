/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.Viewport;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.NumberValue;

/**
 * Abstract base class useful for CSSCanvas implementations.
 * 
 */
abstract public class AbstractCSSCanvas implements CSSCanvas {

	private CSSDocument document;

	protected AbstractCSSCanvas(CSSDocument doc) {
		super();
		this.document = doc;
	}

	@Override
	public CSSDocument getDocument() {
		return document;
	}

	protected void setDocument(CSSDocument doc) {
		this.document = doc;
	}

	@Override
	public CSSTypedValue getFeatureValue(String feature) {
		if ("width".equals(feature)) {
			return getWidth();
		} else if ("height".equals(feature)) {
			return getHeight();
		} else if ("aspect-ratio".equals(feature)) {
			float fratio = getWidthFloat() / getHeightFloat();
			NumberValue number = new NumberValue();
			number.setFloatValue(CSSUnit.CSS_NUMBER, fratio);
			return number;
		} else if ("orientation".equals(feature)) {
			return new IdentifierValue(getOrientation());
		} else if ("resolution".equals(feature)) {
			NumberValue number = new NumberValue();
			number.setFloatValue(CSSUnit.CSS_DPI, getResolution());
			return number;
		} else if ("scan".equals(feature)) {
			return new IdentifierValue(getScanType());
		} else if ("grid".equals(feature)) {
			NumberValue number = new NumberValue();
			number.setFloatValue(CSSUnit.CSS_NUMBER, isGridDevice() ? 1f : 0f);
			return number;
		} else if ("update".equals(feature)) {
			return new IdentifierValue(getUpdateFrequency());
		} else if ("overflow-block".equals(feature)) {
			return new IdentifierValue(getOverflowBlock());
		} else if ("overflow-inline".equals(feature)) {
			return new IdentifierValue(getOverflowInline());
		} else if ("prefers-color-scheme".equals(feature)) {
			return new IdentifierValue(getPrefersColorScheme());
		} else if ("color".equals(feature)) {
			NumberValue number = new NumberValue();
			int color = getStyleDatabase().getColorDepth();
			number.setFloatValue(CSSUnit.CSS_NUMBER, color);
			return number;
		} else if ("color-index".equals(feature)) {
			NumberValue number = new NumberValue();
			number.setFloatValue(CSSUnit.CSS_NUMBER, getColorIndex());
			return number;
		} else if ("monochrome".equals(feature)) {
			NumberValue number = new NumberValue();
			number.setFloatValue(CSSUnit.CSS_NUMBER, getMonoBitsPerPixel());
			return number;
		}
		return null;
	}

	@Override
	public boolean matchesFeature(String feature, CSSTypedValue value) {
		if ("orientation".equals(feature)) {
			return matches(value, getOrientation());
		} else if ("scan".equals(feature)) {
			return matches(value, getScanType());
		} else if ("grid".equals(feature)) {
			return isGridDevice() ? value == null || !value.isNumberZero() : value != null && value.isNumberZero();
		} else if ("update".equals(feature)) {
			return matches(value, getUpdateFrequency());
		} else if ("overflow-block".equals(feature)) {
			return matches(value, getOverflowBlock());
		} else if ("overflow-inline".equals(feature)) {
			return matches(value, getOverflowInline());
		} else if ("prefers-color-scheme".equals(feature)) {
			String pcs = getPrefersColorScheme();
			if (value != null) {
				return value.getPrimitiveType() == Type.IDENT && pcs.equalsIgnoreCase(value.getStringValue());
			} else {
				return !"no-preference".equals(pcs);
			}
		} else if ("color-gamut".equals(feature)) {
			if (value == null) {
				return !supportsGamut("none");
			}
			if (value.getPrimitiveType() == Type.IDENT) {
				String sv = value.getStringValue();
				return supportsGamut(sv);
			}
		} else if ("pointer".equals(feature)) {
			return matches(value, getPointerAccuracy());
		}
		return false;
	}

	private boolean matches(CSSTypedValue valueToMatch, String featureValue) {
		if (valueToMatch == null) {
			return !"none".equals(featureValue);
		}
		return isCSSIdentifier(valueToMatch, featureValue);
	}

	private static boolean isCSSIdentifier(CSSTypedValue value, String ident) {
		return value.getPrimitiveType() == Type.IDENT
				&& ident.equalsIgnoreCase(value.getStringValue());
	}

	private NumberValue getWidth() {
		float fval;
		StyleDatabase sdb = getStyleDatabase();
		Viewport viewport = getViewport();
		if (viewport == null) {
			fval = sdb.getDeviceWidth();
		} else {
			fval = viewport.getViewportWidth();
		}
		NumberValue num = new NumberValue();
		num.setFloatValue(sdb.getNaturalUnit(), fval);
		return num;
	}

	private NumberValue getHeight() {
		float fval;
		StyleDatabase sdb = getStyleDatabase();
		Viewport viewport = getViewport();
		if (viewport == null) {
			fval = sdb.getDeviceHeight();
		} else {
			fval = viewport.getViewportHeight();
		}
		NumberValue num = new NumberValue();
		num.setFloatValue(sdb.getNaturalUnit(), fval);
		return num;
	}

	private float getWidthFloat() {
		float fval;
		StyleDatabase sdb = getStyleDatabase();
		Viewport viewport = getViewport();
		if (viewport == null) {
			fval = sdb.getDeviceWidth();
		} else {
			fval = viewport.getViewportWidth();
		}
		return fval;
	}

	private float getHeightFloat() {
		float fval;
		StyleDatabase sdb = getStyleDatabase();
		Viewport viewport = getViewport();
		if (viewport == null) {
			fval = sdb.getDeviceHeight();
		} else {
			fval = viewport.getViewportHeight();
		}
		return fval;
	}

	/**
	 * Get the number of entries in the color lookup table of the device.
	 * 
	 * @return the number of entries in the color lookup table of the device, or
	 *         {@code 0} if the device does not use a color lookup table.
	 */
	protected abstract float getColorIndex();

	protected abstract boolean isGridDevice();

	/**
	 * Get the number of bits per pixel in a monochrome frame buffer.
	 * 
	 * @return the number of bits per pixel in a monochrome frame buffer, or
	 *         {@code 0} if the device is not a monochrome device.
	 */
	protected abstract int getMonoBitsPerPixel();

	protected abstract String getOrientation();

	protected abstract String getOverflowBlock();

	protected abstract String getOverflowInline();

	protected abstract String getPointerAccuracy();

	protected abstract String getPrefersColorScheme();

	/**
	 * Get the device resolution.
	 * 
	 * @return the device resolution, in {@code dpi}.
	 */
	protected abstract float getResolution();

	protected abstract String getScanType();

	protected abstract String getUpdateFrequency();

	protected abstract boolean supportsGamut(String gamut);

}
