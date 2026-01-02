/*

 Copyright (c) 2005-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.StyleFormattingFactory;

public class TestStyleFormattingFactory implements StyleFormattingFactory {

	@Override
	public StyleFormattingContext createStyleFormattingContext() {
		return new InlineStyleFormattingContext();
	}

	@Override
	public DeclarationFormattingContext createComputedStyleFormattingContext() {
		return new DefaultDeclarationFormattingContext();
	}

}
