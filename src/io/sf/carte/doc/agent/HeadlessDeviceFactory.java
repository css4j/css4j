/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;

/**
 * Device factory for headless devices.
 * <p>
 * Only creates null canvases.
 */
public class HeadlessDeviceFactory extends AbstractDeviceFactory {

	@Override
	public CSSCanvas createCanvas(String medium, CSSDocument doc) {
		return null;
	}

}
