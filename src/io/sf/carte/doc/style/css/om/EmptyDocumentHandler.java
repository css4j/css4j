/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.SelectorList;

abstract class EmptyDocumentHandler implements CSSHandler, CSSErrorHandler {

	EmptyDocumentHandler() {
		super();
	}

	@Override
	public void startDocument() {
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void comment(String text) {
	}

	@Override
	public void ignorableAtRule(String atRule) {
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) {
	}

	@Override
	public void importStyle(String uri, MediaQueryList media, String defaultNamespaceURI) {
	}

	@Override
	public void startMedia(MediaQueryList media) {
	}

	@Override
	public void endMedia(MediaQueryList media) {
	}

	@Override
	public void startPage(String name, String pseudo_page) {
	}

	@Override
	public void endPage(String name, String pseudo_page) {
	}

	@Override
	public void startFontFace() {
	}

	@Override
	public void endFontFace() {
	}

	@Override
	public void startSelector(SelectorList selectors) {
	}

	@Override
	public void endSelector(SelectorList selectors) {
	}

	@Override
	public void warning(CSSParseException exception) throws CSSParseException {
	}

	@Override
	public void error(CSSParseException exception) throws CSSParseException {
	}

}
