/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.StyleFormattingFactory;

public class TestStyleFormattingFactory implements StyleFormattingFactory {

	@Override
	public StyleFormattingContext createStyleFormattingContext() {
		return new InlineStyleFormattingContext();
	}

}
