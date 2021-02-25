/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.parser.EmptyCSSHandler;

/**
 * A NSAC CSSHandler that calls a <code>LexicalPropertyListener</code>.
 *
 * @author Carlos Amengual
 *
 */
class PropertyCSSHandler extends EmptyCSSHandler {

	private LexicalPropertyListener listener = null;

	private ParserControl parserctl = null;

	PropertyCSSHandler() {
		super();
	}

	public void setLexicalPropertyListener(LexicalPropertyListener listener) {
		this.listener = listener;
	}

	@Override
	public void parseStart(ParserControl parserctl) {
		this.parserctl = parserctl;
	}

	ParserControl getParserControl() {
		return parserctl;
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		try {
			listener.setProperty(name, value, important);
		} catch (DOMException e) {
			Locator locator = parserctl.createLocator();
			CSSParseException pe = new CSSParseException("Invalid value for property " + name, locator, e);
			error(pe);
			throw e;
		}
	}

	@Override
	public void lexicalProperty(String name, LexicalUnit value, boolean important) {
		try {
			listener.setLexicalProperty(name, value, important);
		} catch (DOMException e) {
			Locator locator = parserctl.createLocator();
			CSSParseException pe = new CSSParseException("Invalid value for property " + name, locator, e);
			error(pe);
			throw e;
		}
	}

	@Override
	public void error(CSSParseException exception) throws CSSParseException {
		throw exception;
	}

}
