/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * Linear A98 RGB color profile.
 */
class LinearA98RGBColorProfile extends A98RGBColorProfile {

	public LinearA98RGBColorProfile() {
		super();
	}

	@Override
	public double gammaCompanding(double linearComponent) {
		return linearComponent;
	}

	@Override
	public double linearComponent(double compandedComponent) {
		return compandedComponent;
	}

}