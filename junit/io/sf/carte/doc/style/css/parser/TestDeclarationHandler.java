/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.LinkedList;
import java.util.List;

import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class TestDeclarationHandler implements CSSHandler {

	LinkedList<String> propertyNames = new LinkedList<String>();
	LinkedList<LexicalUnit> lexicalValues = new LinkedList<LexicalUnit>();
	LinkedList<String> priorities = new LinkedList<String>();
	LinkedList<String> comments = new LinkedList<String>();

	@Override
	public void startDocument() {
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void comment(String text) {
		comments.add(text);
	}

	@Override
	public void ignorableAtRule(String atRule) {
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) {
	}

	@Override
	public void importStyle(String uri, List<String> media, String defaultNamespaceURI) {
	}

	@Override
	public void startMedia(List<String> media) {
	}

	@Override
	public void endMedia(List<String> media) {
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
	public void property(String name, LexicalUnit value, boolean important) {
		propertyNames.add(name);
		lexicalValues.add(value);
		if (important) {
			priorities.add("important");
		} else {
			priorities.add(null);
		}
	}

}