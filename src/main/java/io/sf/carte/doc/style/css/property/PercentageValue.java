/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.style.css.CSSUnit;

/**
 * Percentage value.
 *
 */
public class PercentageValue extends NumberValue {

	private static final long serialVersionUID = 1L;

	PercentageValue() {
		super();
	}

	private PercentageValue(PercentageValue copied) {
		super(copied);
	}

	/**
	 * Gets a float value in a specified unit. If this CSS value doesn't contain
	 * a float value or can't be converted into the specified unit, a
	 * <code>DOMException</code> is raised.
	 * 
	 * @param unitType
	 *            A unit code to get the float value. The unit code can only be
	 *            <code>CSS_NUMBER</code> or <code>CSS_PERCENTAGE</code>.
	 * @return The float value in the specified unit.
	 * @throws DOMException
	 *             INVALID_ACCESS_ERR if the CSS value doesn't contain a float
	 *             value or if the float value can't be converted into the
	 *             specified unit.
	 */
	@Override
	public float getFloatValue(short unitType) throws DOMException {
		if (unitType == getUnitType()) {
			return realvalue;
		} else if (unitType == CSSUnit.CSS_NUMBER) {
			return realvalue / 100f;
		} else {
			throw new DOMInvalidAccessException("Cannot convert a percentage at this level");
		}
	}

	@Override
	public PercentageValue clone() {
		return new PercentageValue(this);
	}

}
