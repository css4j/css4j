/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class TestCSSHandler extends TestDeclarationHandler {

	LinkedHashMap<String, String> namespaceMaps = new LinkedHashMap<>();
	LinkedList<String> atRules = new LinkedList<>();
	ArrayList<SelectorList> selectors = new ArrayList<>();
	ArrayList<SelectorList> nestedSelectors = new ArrayList<>();
	ArrayList<SelectorList> endNestedSelectors = new ArrayList<>();
	LinkedList<SelectorList> endSelectors = new LinkedList<>();
	ArrayList<SelectorList> selectorStack = new ArrayList<>();
	ArrayList<SelectorList> propertySelectors = new ArrayList<>();
	LinkedList<String> importURIs = new LinkedList<>();
	LinkedList<String> importLayers = new LinkedList<>();
	LinkedList<BooleanCondition> importSupportsConditions = new LinkedList<>();
	LinkedList<MediaQueryList> importMedias = new LinkedList<>();
	LinkedList<MediaQueryList> mediaRuleLists = new LinkedList<>();
	LinkedList<BooleanCondition> supportsRuleLists = new LinkedList<>();
	LinkedList<PageSelectorList> pageRuleSelectors = new LinkedList<>();
	LinkedList<String> marginRuleNames = new LinkedList<>();
	LinkedList<String> counterStyleNames = new LinkedList<>();
	LinkedList<String> keyframesNames = new LinkedList<>();
	LinkedList<LexicalUnit> keyframeSelectors = new LinkedList<>();
	LinkedList<String[]> fontFeaturesNames = new LinkedList<>();
	LinkedList<String> featureMapNames = new LinkedList<>();
	LinkedList<String> customPropertyNames = new LinkedList<>();
	LinkedList<String> eventSeq = new LinkedList<>();

	int fontFaceCount = 0;
	int endMediaCount = 0;
	int endPageCount = 0;
	int endFontFaceCount = 0;
	int endSupportsCount = 0;
	int endMarginCount = 0;
	int endCounterStyleCount = 0;
	int endKeyframesCount = 0;
	int endKeyframeCount = 0;
	int endFontFeaturesCount = 0;
	int endFeatureMapCount = 0;
	int endPropertyRuleCount = 0;

	@Override
	public void startSelector(SelectorList selectors) throws CSSException {
		if (selectors == null) {
			throw new CSSException("Null selector");
		}
		if (this.selectors.size() > this.endSelectors.size()) {
			// Nesting
			this.nestedSelectors.add(selectors);
		} else {
			this.selectors.add(selectors);
		}
		selectorStack.add(selectors);
		this.eventSeq.add("startSelector");
	}

	@Override
	public void endSelector(SelectorList selectors) {
		if (!this.nestedSelectors.contains(selectors)) {
			this.endSelectors.add(selectors);
		} else {
			endNestedSelectors.add(selectors);
		}
		assertSame(selectors, selectorStack.remove(selectorStack.size() - 1));
		this.eventSeq.add("endSelector");
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		super.property(name, value, important);
		SelectorList selist;
		if (!selectorStack.isEmpty()) {
			selist = selectorStack.get(selectorStack.size() - 1);
		} else {
			selist = null;
		}
		propertySelectors.add(selist);
		this.eventSeq.add("property");
	}

	@Override
	public void importStyle(String uri, String layer, BooleanCondition supportsCondition,
			MediaQueryList media, String defaultNamespaceURI) {
		importURIs.add(uri);
		importLayers.add(layer);
		importSupportsConditions.add(supportsCondition);
		importMedias.add(media);
		this.eventSeq.add("importStyle");
	}

	@Override
	public void startMedia(MediaQueryList media) {
		mediaRuleLists.add(media);
		this.eventSeq.add("startMedia");
	}

	@Override
	public void endMedia(MediaQueryList media) {
		this.eventSeq.add("endMedia");
		endMediaCount++;
	}

	@Override
	public void startSupports(BooleanCondition condition) {
		supportsRuleLists.add(condition);
		this.eventSeq.add("startSupports");
	}

	@Override
	public void endSupports(BooleanCondition condition) {
		this.eventSeq.add("endSupports");
		endSupportsCount ++;
	}

	@Override
	public void startPage(PageSelectorList pageSelectorList) {
		pageRuleSelectors.add(pageSelectorList);
		this.eventSeq.add("startPage");
	}

	@Override
	public void endPage(PageSelectorList pageSelectorList) {
		this.eventSeq.add("endPage");
		endPageCount++;
	}

	@Override
	public void startMargin(String name) {
		marginRuleNames.add(name);
		this.eventSeq.add("startMargin");
	}

	@Override
	public void endMargin() {
		this.eventSeq.add("endMargin");
		endMarginCount++;
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
	public void startCounterStyle(String name) {
		counterStyleNames.add(name);
		this.eventSeq.add("startCounterStyle");
	}

	@Override
	public void endCounterStyle() {
		this.eventSeq.add("endCounterStyle");
		endCounterStyleCount++;
	}

	@Override
	public void startKeyframes(String name) {
		keyframesNames.add(name);
		this.eventSeq.add("startKeyframes");
	}

	@Override
	public void endKeyframes() {
		this.eventSeq.add("endKeyframes");
		endKeyframesCount++;
	}

	@Override
	public void startKeyframe(LexicalUnit keyframeSelector) {
		keyframeSelectors.add(keyframeSelector);
		this.eventSeq.add("startKeyframe");
	}

	@Override
	public void endKeyframe() {
		this.eventSeq.add("endKeyframe");
		endKeyframeCount++;
	}

	@Override
	public void startFontFeatures(String[] familyName) {
		fontFeaturesNames.add(familyName);
		this.eventSeq.add("startFontFeatures");
	}

	@Override
	public void endFontFeatures() {
		this.eventSeq.add("endFontFeatures");
		endFontFeaturesCount++;
	}

	@Override
	public void startFeatureMap(String mapName) {
		featureMapNames.add(mapName);
		this.eventSeq.add("startFeatureMap");
	}

	@Override
	public void endFeatureMap() {
		this.eventSeq.add("endFeatureMap");
		endFeatureMapCount++;
	}

	@Override
	public void comment(String text, boolean precededByLF) {
		comments.add(text);
		if (precededByLF) {
			this.eventSeq.add("head-comment");
		} else {
			this.eventSeq.add("tail-comment");
		}
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

	@Override
	public void startProperty(String name) {
		customPropertyNames.add(name);
		this.eventSeq.add("startProperty");
	}

	@Override
	public void endProperty(boolean discard) {
		if (discard) {
			this.eventSeq.add("endProperty-Discard");
		} else {
			this.eventSeq.add("endProperty");
		}
		endPropertyRuleCount++;
	}

	void checkRuleEndings() {
		int sz = selectors.size();
		assertEquals(sz, endSelectors.size());
		for (int i = 0; i < sz; i++) {
			assertEquals(selectors.get(i), endSelectors.get(i));
		}
		assertEquals(nestedSelectors.size(), endNestedSelectors.size());
		assertTrue(selectors.equals(endSelectors));
		assertEquals(mediaRuleLists.size(), endMediaCount);
		assertEquals(pageRuleSelectors.size(), endPageCount);
		assertEquals(marginRuleNames.size(), endMarginCount);
		assertEquals(counterStyleNames.size(), endCounterStyleCount);
		assertEquals(keyframesNames.size(), endKeyframesCount);
		assertEquals(keyframeSelectors.size(), endKeyframeCount);
		assertEquals(fontFeaturesNames.size(), endFontFeaturesCount);
		assertEquals(featureMapNames.size(), endFeatureMapCount);
		assertEquals(fontFaceCount, endFontFaceCount);
		assertEquals(customPropertyNames.size(), endPropertyRuleCount);
		assertEquals(1, streamEndcount);
	}

}
