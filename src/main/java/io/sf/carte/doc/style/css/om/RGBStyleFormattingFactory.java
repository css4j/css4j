/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.StyleFormattingFactory;

/**
 * A {@code StyleFormattingFactory} that serializes computed colors as RGB.
 */
public class RGBStyleFormattingFactory implements StyleFormattingFactory, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public StyleFormattingContext createStyleFormattingContext() {
		return new DefaultStyleFormattingContext();
	}

	@Override
	public DeclarationFormattingContext createComputedStyleFormattingContext() {
		return new RGBColorDeclarationFormattingContext();
	}

}
