/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * Handler that implements empty bodies for all {@link CSSHandler} methods
 * except
 * {@link CSSHandler#property(String, io.sf.carte.doc.style.css.nsac.LexicalUnit, boolean)
 * property(String, LexicalUnit, boolean)}.
 */
abstract public class EmptyCSSHandler implements CSSHandler, CSSErrorHandler {

	protected EmptyCSSHandler() {
		super();
	}

	@Override
	public void parseStart(ParserControl parserctl) {
	}

	@Override
	public void endOfStream() {
	}

	@Override
	public void comment(String text, boolean precededByLF) {
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
	public void startPage(PageSelectorList pageSelectorList) {
	}

	@Override
	public void endPage(PageSelectorList pageSelectorList) {
	}

	@Override
	public void startMargin(String name) {
	}

	@Override
	public void endMargin() {
	}

	@Override
	public void startFontFace() {
	}

	@Override
	public void endFontFace() {
	}

	@Override
	public void startCounterStyle(String name) {
	}

	@Override
	public void endCounterStyle() {
	}

	@Override
	public void startKeyframes(String name) {
	}

	@Override
	public void endKeyframes() {
	}

	@Override
	public void startKeyframe(LexicalUnit keyframeSelector) {
	}

	@Override
	public void endKeyframe() {
	}

	@Override
	public void startFontFeatures(String[] familyName) {
	}

	@Override
	public void endFontFeatures() {
	}

	@Override
	public void startFeatureMap(String mapName) {
	}

	@Override
	public void endFeatureMap() {
	}

	@Override
	public void startProperty(String name) {
	}

	@Override
	public void endProperty(boolean discard) {
	}

	@Override
	public void startSupports(BooleanCondition condition) {
	}

	@Override
	public void endSupports(BooleanCondition condition) {
	}

	@Override
	public void startSelector(SelectorList selectors) {
	}

	@Override
	public void endSelector(SelectorList selectors) {
	}

	@Override
	public void startViewport() {
	}

	@Override
	public void endViewport() {
	}

	@Override
	public void lexicalProperty(String propertyName, LexicalUnit lunit, boolean important) {
		property(propertyName, lunit, important);
	}

	@Override
	public void warning(CSSParseException exception) throws CSSParseException {
	}

	@Override
	public void error(CSSParseException exception) throws CSSParseException {
	}

}
