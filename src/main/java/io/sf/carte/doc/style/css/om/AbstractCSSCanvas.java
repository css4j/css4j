/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.Viewport;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.NumberValue;

/**
 * Abstract base class useful for CSSCanvas implementations.
 * 
 */
abstract public class AbstractCSSCanvas implements CSSCanvas {

	/**
	 * Construct a canvas to display the given document.
	 */
	protected AbstractCSSCanvas() {
		super();
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
		} else if ("prefers-reduced-motion".equals(feature)) {
			return new IdentifierValue(getPrefersReducedMotion());
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
			}
		} else if ("prefers-reduced-motion".equals(feature)) {
			String pcs = getPrefersReducedMotion();
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
	protected float getColorIndex() {
		return 16777216f;
	}

	/**
	 * Is this device a grid device?
	 * 
	 * @return {@code true} if a grid device, {@code false} if bitmap.
	 */
	protected boolean isGridDevice() {
		return false;
	}

	/**
	 * Get the number of bits per pixel in a monochrome frame buffer.
	 * 
	 * @return the number of bits per pixel in a monochrome frame buffer, or
	 *         {@code 0} if the device is not a monochrome device.
	 */
	protected int getMonoBitsPerPixel() {
		return 0;
	}

	/**
	 * The orientation.
	 * 
	 * @return the orientation ({@code portrait} or {@code landscape}).
	 */
	protected String getOrientation() {
		float width = getWidth().getFloatValue(CSSUnit.CSS_PX);
		float height = getHeight().getFloatValue(CSSUnit.CSS_PX);

		if (width >= height) {
			return "landscape";
		}
		return "portrait";
	}

	/**
	 * Describes the behavior of the device when content overflows the initial
	 * containing block in the block axis
	 * 
	 * @return the {@code overflow-block} feature.
	 */
	protected abstract String getOverflowBlock();

	/**
	 * Describes the behavior of the device when content overflows the initial
	 * containing block in the inline axis.
	 * 
	 * @return the {@code overflow-inline} feature.
	 */
	protected abstract String getOverflowInline();

	/**
	 * The pointing device quality.
	 * 
	 * @return the {@code pointer} feature
	 */
	protected String getPointerAccuracy() {
		return "none";
	}

	/**
	 * The desire for light or dark color schemes.
	 * 
	 * @return the {@code prefers-color-scheme} feature
	 */
	protected String getPrefersColorScheme() {
		return "light";
	}

	/**
	 * The desire for less motion on the page.
	 * 
	 * @return the {@code prefers-reduced-motion} feature
	 */
	protected String getPrefersReducedMotion() {
		return "no-preference";
	}

	/**
	 * Get the device resolution.
	 * 
	 * @return the device resolution, in {@code dpi}.
	 */
	protected float getResolution() {
		return 96f;
	}

	/**
	 * The display scan type.
	 * 
	 * @return the {@code scan} feature.
	 */
	protected String getScanType() {
		return "progressive";
	}

	/**
	 * The display update frequency.
	 * 
	 * @return the update feature.
	 */
	protected String getUpdateFrequency() {
		return "none";
	}

	/**
	 * Check whether a color gamut is supported.
	 * 
	 * @param gamut the color-gamut to check, like {@code srgb}, {@code p3} or
	 *              {@code rec2020}.
	 * @return {@code true} if the gamut is supported.
	 */
	protected boolean supportsGamut(String gamut) {
		return "srgb".equalsIgnoreCase(gamut);
	}

}
