/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.CSSNumberValue;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Abstract base class for CSS Style databases.
 * 
 * @author Carlos Amengual
 */
abstract public class AbstractStyleDatabase implements StyleDatabase {

	protected final String DEFAULT_GENERIC_FONT_FAMILY = "serif";

	private static final AbstractCSSPrimitiveValue DEFAULT_INITIAL_COLOR;

	static {
		DEFAULT_INITIAL_COLOR = (AbstractCSSPrimitiveValue) new ValueFactory()
				.parseProperty("#000000");
		((ColorValue) DEFAULT_INITIAL_COLOR).setSystemDefault();
	}

	private CSSPrimitiveValue initialColor;

	public AbstractStyleDatabase() {
		super();
		initialColor = DEFAULT_INITIAL_COLOR;
	}

	@Override
	public int getExSizeInPt(String familyName, int size) {
		return Math.round(0.5f * size);
	}

	@Override
	public float floatValueConversion(float initialValue, short initialUnitType, short destUnitType)
			throws DOMException {
		return CSSNumberValue.floatValueConversion(initialValue, initialUnitType, destUnitType);
	}

	@Override
	public float floatValueConversion(float initialValue, short initialUnitType) throws DOMException {
		return CSSNumberValue.floatValueConversion(initialValue, initialUnitType, getNaturalUnit());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.sf.carte.doc.style.css.StyleDatabase#getInitialColor()
	 */
	@Override
	public CSSPrimitiveValue getInitialColor() {
		return initialColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.sf.carte.doc.style.css.StyleDatabase#setInitialColor(String)
	 */
	@Override
	public void setInitialColor(String initialColor) {
		this.initialColor = (AbstractCSSPrimitiveValue) new ValueFactory().parseProperty(initialColor);
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

}
