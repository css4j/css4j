/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class TestDocumentHandler extends TestDeclarationHandler {

	LinkedHashMap<String, String> namespaceMaps = new LinkedHashMap<String, String>();
	LinkedList<String> atRules = new LinkedList<String>();
	LinkedList<SelectorList> selectors = new LinkedList<SelectorList>();
	LinkedList<SelectorList> endSelectors = new LinkedList<SelectorList>();
	LinkedList<String> importURIs = new LinkedList<String>();
	LinkedList<List<String>> importMedias = new LinkedList<List<String>>();
	LinkedList<List<String>> mediaRuleLists = new LinkedList<List<String>>();
	LinkedList<String> pageRuleNames = new LinkedList<String>();
	LinkedList<String> pseudoPages = new LinkedList<String>();
	LinkedList<String> comments = new LinkedList<String>();
	LinkedList<String> eventSeq = new LinkedList<String>();
	int fontFaceCount = 0;
	int endMediaCount = 0;
	int endPageCount = 0;
	int endFontFaceCount = 0;

	@Override
	public void startSelector(SelectorList selectors) throws CSSException {
		if (selectors == null) {
			throw new CSSException("Null selector");
		}
		this.selectors.add(selectors);
		this.eventSeq.add("startSelector");
	}

	@Override
	public void endSelector(SelectorList selectors) {
		this.endSelectors.add(selectors);
		this.eventSeq.add("endSelector");
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		super.property(name, value, important);
		this.eventSeq.add("property");
	}

	@Override
	public void importStyle(String uri, List<String> media, String defaultNamespaceURI) {
		importURIs.add(uri);
		importMedias.add(media);
		this.eventSeq.add("importStyle");
	}

	@Override
	public void startMedia(List<String> media) {
		mediaRuleLists.add(media);
		this.eventSeq.add("startMedia");
	}

	@Override
	public void endMedia(List<String> media) {
		this.eventSeq.add("endMedia");
		endMediaCount++;
	}

	@Override
	public void startPage(String name, String pseudo_page) {
		pageRuleNames.add(name);
		pseudoPages.add(pseudo_page);
		this.eventSeq.add("startPage");
	}

	@Override
	public void endPage(String name, String pseudo_page) {
		this.eventSeq.add("endPage");
		endPageCount++;
	}

	@Override
	public void startFontFace() {
		this.eventSeq.add("startFontFace");
		fontFaceCount++;
	}

	@Override
	public void endFontFace() {
		this.eventSeq.add("endFontFace");
		endFontFaceCount++;
	}

	@Override
	public void comment(String text) {
		comments.add(text);
		this.eventSeq.add("comment");
	}

	@Override
	public void ignorableAtRule(String atRule) {
		atRules.add(atRule);
		this.eventSeq.add("ignorableAtRule");
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) {
		namespaceMaps.put(prefix, uri);
		this.eventSeq.add("namespaceDeclaration");
	}

	void checkRuleEndings() {
		int sz = selectors.size();
		assertEquals(sz, endSelectors.size());
		for (int i = 0; i < sz; i++) {
			assertEquals(selectors.get(i), endSelectors.get(i));
		}
		assertTrue(selectors.equals(endSelectors));
		assertEquals(mediaRuleLists.size(), endMediaCount);
		assertEquals(pageRuleNames.size(), endPageCount);
		assertEquals(fontFaceCount, endFontFaceCount);
	}
}