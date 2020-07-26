/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017,2018 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.parser;

import org.w3c.css.sac.DocumentHandler;

/**
 * DocumentHandler for {@literal @}font-feature-values rule body.
 * <p>
 * It is likely to be moved to a new DocumentHandler2 interface in NSAC 1.1.
 *
 */
public interface FontFeatureValuesHandler extends DocumentHandler {

	void startFontFeatures(String[] familyName);

	void endFontFeatures();

	void startFeatureMap(String mapName);

	void endFeatureMap();
}