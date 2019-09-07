/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.util.Iterator;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Abstract base class for CSS Style databases.
 *
 * @author Carlos Amengual
 */
abstract public class AbstractStyleDatabase implements StyleDatabase {

	protected final String DEFAULT_GENERIC_FONT_FAMILY = "serif";

	private static final PrimitiveValue DEFAULT_INITIAL_COLOR;

	static {
		DEFAULT_INITIAL_COLOR = (PrimitiveValue) new ValueFactory().parseProperty("#000000");
		((ColorValue) DEFAULT_INITIAL_COLOR).setSystemDefault();
	}

	private CSSPrimitiveValue initialColor;

	public AbstractStyleDatabase() {
		super();
		initialColor = DEFAULT_INITIAL_COLOR;
	}

	@Override
	public float getExSizeInPt(String familyName, float size) {
		return Math.round(0.5f * size);
	}

	@Override
	public CSSPrimitiveValue getInitialColor() {
		return initialColor;
	}

	@Override
	public void setInitialColor(String initialColor) {
		this.initialColor = (PrimitiveValue) new ValueFactory().parseProperty(initialColor);
		((ColorValue) this.initialColor).setSystemDefault();
	}

	@Override
	public String getDefaultGenericFontFamily() {
		return getDefaultGenericFontFamily(DEFAULT_GENERIC_FONT_FAMILY);
	}

	@Override
	public String getSystemFontDeclaration(String systemFontName) {
		return null;
	}

	@Override
	public String getUsedFontFamily(CSSComputedProperties computedStyle) {
		String requestedFamily = scanFontFamilyValue(computedStyle);
		if (requestedFamily == null) {
			requestedFamily = getDefaultGenericFontFamily();
		}
		return requestedFamily;
	}

	private String scanFontFamilyValue(CSSComputedProperties style) {
		ExtendedCSSValue value = style.getPropertyCSSValue("font-family");
		String requestedFamily = null;
		if (value != null) {
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList fontList = (ValueList) value;
				Iterator<StyleValue> it = fontList.iterator();
				while (it.hasNext()) {
					StyleValue item = it.next();
					requestedFamily = stringValueOrNull(item);
					if (requestedFamily != null && isFontFamilyAvailable(requestedFamily, style)) {
						return requestedFamily;
					}
				}
			} else {
				requestedFamily = stringValueOrNull(value);
				if (requestedFamily != null && isFontFamilyAvailable(requestedFamily, style)) {
					return requestedFamily;
				}
			}
		}
		CSSComputedProperties ancStyle = style.getParentComputedStyle();
		if (ancStyle != null) {
			requestedFamily = scanFontFamilyValue(ancStyle);
		}
		return requestedFamily;
	}

	private String stringValueOrNull(ExtendedCSSValue value) {
		CSSPrimitiveValue primi;
		short ptype;
		String s;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((ptype = (primi = (CSSPrimitiveValue) value).getPrimitiveType()) == CSSPrimitiveValue.CSS_STRING
						|| ptype == CSSPrimitiveValue.CSS_IDENT)) {
			s = primi.getStringValue();
		} else {
			s = null;
		}
		return s;
	}

	protected boolean isFontFamilyAvailable(String requestedFamily, CSSComputedProperties style) {
		if (isFontFamilyAvailable(requestedFamily)) {
			return true;
		}
		CSSCanvas canvas = style.getOwnerNode().getOwnerDocument().getCanvas();
		if (canvas != null) {
			return canvas.isFontFaceName(requestedFamily);
		}
		return false;
	}

	@Override
	public boolean supports(String featureName, CSSValue value) {
		return false;
	}

	abstract protected boolean isFontFamilyAvailable(String fontFamily);

}
