/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class PropertyCounterDocumentHandler extends EmptyDocumentHandler {

	private int count = 0;

	private boolean error = false;

	PropertyCounterDocumentHandler() {
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
