/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.EmptyCSSHandler;

class PropertyCounterHandler extends EmptyCSSHandler {

	private int count = 0;

	private boolean error = false;

	PropertyCounterHandler() {
		super();
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		count++;
	}

	@Override
	public void error(CSSParseException exception) throws CSSParseException {
		error = true;
	}

	int getPropertyCount() {
		return count;
	}

	boolean hasError() {
		return error;
	}

}
