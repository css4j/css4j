/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;

/**
 * Media Feature predicate.
 * <p>
 * Represents a predicate like <code>(min-width: 600px)</code> or
 * <code>(400px &lt;= width &lt; 1000px)</code>.
 */
public interface MediaFeaturePredicate extends BooleanCondition {

	/**
	 * Get the name of the feature.
	 * <p>
	 * Example: <code>width</code>.
	 *
	 * @return the name of the feature.
	 */
	String getName();

	/**
	 * If the feature is of range type, set the range type as determined by the
	 * parser.
	 *
	 * @param rangeType the range type according to the implementation.
	 */
	void setRangeType(byte rangeType);

	/**
	 * Get the range type as was determined by the parser.
	 *
	 * @return the range type according to the implementation, or zero if not a
	 *         range feature.
	 */
	byte getRangeType();

	/**
	 * Get the value to be tested on the feature. If the predicate involves two
	 * values, this is the first value.
	 *
	 * @return the value to be tested on the feature.
	 */
	ExtendedCSSPrimitiveValue getValue();

	/**
	 * Set the value to be tested on the feature. If the predicate involves two
	 * values, this is the first value.
	 *
	 * @param value the value to be tested on the feature.
	 */
	void setValue(ExtendedCSSPrimitiveValue value);

	/**
	 * If this is a range feature test involving two values, get the second value.
	 *
	 * @return the second value in this range test, or <code>null</code> if this is
	 *         not a range feature or the range only involves one value.
	 */
	ExtendedCSSPrimitiveValue getRangeSecondValue();

	/**
	 * Set the second value in this range test.
	 *
	 * @param value the second value in this range test.
	 */
	void setRangeSecondValue(ExtendedCSSPrimitiveValue value);

}
