/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.LinkedList;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

class TestDeclarationHandler implements DocumentHandler {

	LinkedList<String> propertyNames = new LinkedList<String>();
	LinkedList<LexicalUnit> lexicalValues = new LinkedList<LexicalUnit>();
	LinkedList<String> priorities = new LinkedList<String>();
	LinkedList<String> comments = new LinkedList<String>();

	short streamEndcount = 0;

	@Override
	public void startDocument(InputSource source) throws CSSException {
		streamEndcount = 0;
	}

	@Override
	public void endDocument(InputSource source) throws CSSException {
		streamEndcount++;
	}

	@Override
	public void comment(String text) throws CSSException {
		comments.add(text);
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
		propertyNames.add(name);
		lexicalValues.add(value);
		if (important) {
			priorities.add("important");
		} else {
			priorities.add(null);
		}
	}

}