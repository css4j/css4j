/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.LexicalUnit;

class PropertyCounterDocumentHandler extends EmptyDocumentHandler {

	private int count = 0;

	private boolean error = false;

	PropertyCounterDocumentHandler() {
		super();
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) throws CSSException {
		count++;
	}

	@Override
	public void error(CSSParseException exception) throws CSSException {
		error = true;
	}

	@Override
	public void fatalError(CSSParseException exception) throws CSSException {
		error = true;
	}

	int getPropertyCount() {
		return count;
	}

	boolean hasError() {
		return error;
	}

}
