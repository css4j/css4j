/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;

interface MediaQueryHandler {

	public void mediaType(String mediaType);

	public void negativeQuery();

	public void onlyPrefix();

	public void featureValue(String featureName, ExtendedCSSPrimitiveValue value);

	public void featureRange(String featureName, byte rangeType, ExtendedCSSPrimitiveValue minvalue,
			ExtendedCSSPrimitiveValue maxvalue);

	public void endQuery();

	public void invalidQuery(String message);

}
