/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

/**
 * A SAC DocumentHandler that calls a <code>LexicalPropertyListener</code>.
 * 
 * @author Carlos Amengual
 * 
 */
class PropertyDocumentHandler implements DocumentHandler, ErrorHandler {

	private LexicalPropertyListener listener = null;;

	PropertyDocumentHandler() {
		super();
	}

	public void setLexicalPropertyListener(LexicalPropertyListener listener) {
		this.listener = listener;
	}

	@Override
	public void startDocument(InputSource source) throws CSSException {

	}

	@Override
	public void endDocument(InputSource source) throws CSSException {

	}

	@Override
	public void comment(String text) throws CSSException {

	}

	@Override
	public void ignorableAtRule(String atRule) throws CSSException {

	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) throws CSSException {

	}

	@Override
	public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException {

	}

	@Override
	public void startMedia(SACMediaList media) throws CSSException {

	}

	@Override
	public void endMedia(SACMediaList media) throws CSSException {

	}

	@Override
	public void startPage(String name, String pseudo_page) throws CSSException {

	}

	@Override
	public void endPage(String name, String pseudo_page) throws CSSException {

	}

	@Override
	public void startFontFace() throws CSSException {

	}

	@Override
	public void endFontFace() throws CSSException {

	}

	@Override
	public void startSelector(SelectorList selectors) throws CSSException {

	}

	@Override
	public void endSelector(SelectorList selectors) throws CSSException {

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
	public void warning(CSSParseException exception) throws CSSException {
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
