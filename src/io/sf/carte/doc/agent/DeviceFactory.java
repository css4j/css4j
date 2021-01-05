/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Device factory.
 * <p>
 * A device factory is the core source of device-related objects, like style
 * databases and canvases, for the different media supported by the device(s).
 * </p>
 *
 * @author Carlos Amengual
 *
 */
public interface DeviceFactory {

	/**
	 * Supplies a style database for the given medium.
	 *
	 * @param medium
	 *            the medium.
	 * @return the StyleDatabase.
	 */
	StyleDatabase getStyleDatabase(String medium);

	/**
	 * Creates a Canvas for the given document and target medium.
	 *
	 * @param medium
	 *            the target medium.
	 * @param doc
	 *            the document.
	 * @return the canvas, or null if the factory does not support that medium.
	 */
	CSSCanvas createCanvas(String medium, CSSDocument doc);
}
