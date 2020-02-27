/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * Media Feature predicate.
 * <p>
 * Represents a predicate like <code>(min-width: 600px)</code> or
 * <code>(400px &lt;= width &lt; 1000px)</code>.
 * </p>
 */
public interface MediaFeaturePredicate extends BooleanCondition {

	byte FEATURE_PLAIN = 0;
	byte FEATURE_EQ = 1; // =
	byte FEATURE_LT = 2; // <
	byte FEATURE_LE = 3; // <=
	byte FEATURE_GT = 4; // >
	byte FEATURE_GE = 5; // >=
	byte FEATURE_LT_AND_LT = 18; // a < foo < b
	byte FEATURE_LT_AND_LE = 26; // a < foo <= b
	byte FEATURE_LE_AND_LE = 27; // a <= foo <= b
	byte FEATURE_LE_AND_LT = 19; // a <= foo < b
	byte FEATURE_GT_AND_GT = 36; // a > foo > b
	byte FEATURE_GE_AND_GT = 37; // a >= foo > b
	byte FEATURE_GT_AND_GE = 44; // a > foo >= b
	byte FEATURE_GE_AND_GE = 45; // a >= foo >= b

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
	 * Set a single value to be tested on the feature.
	 *
	 * @param value the value to be tested on the feature.
	 */
	void setValue(LexicalUnit value);

	/**
	 * Set two values in a range test.
	 *
	 * @param value1 the first value in this range test.
	 * @param value2 the second value in this range test.
	 */
	void setValueRange(LexicalUnit value1, LexicalUnit value2);

}
