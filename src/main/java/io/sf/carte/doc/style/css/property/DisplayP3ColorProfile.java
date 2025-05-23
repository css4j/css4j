/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;

/**
 * Display P3 color profile.
 */
class DisplayP3ColorProfile extends ColorProfile {

	public DisplayP3ColorProfile() {
		super(0.680f, 0.320f, 0.265f, 0.690f, 0.150f, 0.060f, Illuminants.whiteD65);
	}

	@Override
	public Illuminant getIlluminant() {
		return Illuminant.D65;
	}

}
