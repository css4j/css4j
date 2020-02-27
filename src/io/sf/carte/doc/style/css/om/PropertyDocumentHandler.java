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

/**
 * A SAC DocumentHandler that calls a <code>LexicalPropertyListener</code>.
 *
 * @author Carlos Amengual
 *
 */
class PropertyDocumentHandler extends EmptyDocumentHandler {

	private LexicalPropertyListener listener = null;

	PropertyDocumentHandler() {
		super();
	}

	public void setLexicalPropertyListener(LexicalPropertyListener listener) {
		this.listener = listener;
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) throws CSSException {
		if (important) {
			listener.setProperty(name, value, "important");
		} else {
			listener.setProperty(name, value, null);
		}
	}

	@Override
	public void error(CSSParseException exception) throws CSSException {
		throw exception;
	}

	@Override
	public void fatalError(CSSParseException exception) throws CSSException {
		throw exception;
	}

}
