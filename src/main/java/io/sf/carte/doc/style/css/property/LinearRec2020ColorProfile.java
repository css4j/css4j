/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * Linear rec2020 color profile.
 */
class LinearRec2020ColorProfile extends Rec2020ColorProfile {

	public LinearRec2020ColorProfile() {
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
