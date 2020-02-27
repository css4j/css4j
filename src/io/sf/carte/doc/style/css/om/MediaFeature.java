/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.parser.MediaFeaturePredicate;

/**
 * Media Feature.
 * <p>
 * Represents a predicate like <code>(min-width: 600px)</code> or
 * <code>(400px &lt;= width &lt; 1000px)</code>.
 * </p>
 */
public interface MediaFeature extends MediaFeaturePredicate {

	/**
	 * Get the value to be tested on the feature. If the predicate involves two
	 * values, this is the first value.
	 *
	 * @return the value to be tested on the feature.
	 */
	CSSTypedValue getValue();

	/**
	 * If this is a range feature test involving two values, get the second value.
	 *
	 * @return the second value in this range test, or <code>null</code> if this is
	 *         not a range feature or the range only involves one value.
	 */
	CSSTypedValue getRangeSecondValue();

}
