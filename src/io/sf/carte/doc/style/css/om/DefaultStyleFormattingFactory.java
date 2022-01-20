/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.StyleFormattingFactory;

public class DefaultStyleFormattingFactory implements StyleFormattingFactory {

	@Override
	public StyleFormattingContext createStyleFormattingContext() {
		return new DefaultStyleFormattingContext();
	}

}
