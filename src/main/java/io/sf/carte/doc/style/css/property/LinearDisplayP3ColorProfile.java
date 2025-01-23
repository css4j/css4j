/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * Linear Display P3 color profile.
 */
class LinearDisplayP3ColorProfile extends DisplayP3ColorProfile {

	public LinearDisplayP3ColorProfile() {
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
