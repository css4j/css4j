/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.LinkedList;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.Parser.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.BooleanCondition;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

class SheetHandler implements CSSParentHandler, CSSErrorHandler, NamespaceMap {

	private ParserControl parserctl = null;

	private final BaseCSSStyleSheet parentSheet;

	private AbstractCSSRule currentRule = null;
	private AbstractCSSRule lastRule = null;

	private final byte sheetOrigin;

	/*
	 * If comments have to be ignored, this will be null.
	 */
	private final LinkedList<String> comments;

	private final boolean allCommentsPrecede;

	/*
	 * switch for ignoring rules if a grouping rule is inside the wrong place.
	 */
	private short ignoreGroupingRules = 0;

	private boolean ignoreImports = false;

	private CSSParseException outOfRuleException = null;

	SheetHandler(BaseCSSStyleSheet sheet, byte origin, short commentsMode) {
		super();
		this.parentSheet = sheet;
		this.sheetOrigin = origin;
		if (commentsMode != CSSStyleSheet.COMMENTS_IGNORE) {
			comments = new LinkedList<String>();
		} else {
			comments = null;
		}
		allCommentsPrecede = commentsMode != CSSStyleSheet.COMMENTS_AUTO;
	}

	@Override
	public void parseStart(ParserControl parserctl) {
		// Starting StyleSheet processing
		currentRule = null;
		this.parserctl = parserctl;
		ignoreGroupingRules = 0;
		ignoreImports = false;
		resetCommentStack();
	}

	@Override
	public void endOfStream() {
		// Ending StyleSheet processing
		resetCommentStack();
		parserctl = null;
	}

	@Override
	public void comment(String text, boolean precededByLF) {
		if (comments != null) {
			if (lastRule != null && !precededByLF && !allCommentsPrecede) {
				BaseCSSRule rule = (BaseCSSRule) lastRule;
				if (rule.getTrailingComments() == null) {
					rule.setTrailingComments(new LinkedStringList());
				}
				rule.getTrailingComments().add(text);
			} else {
				comments.add(text);
			}
		}
	}

	@Override
	public void ignorableAtRule(String atRule) {
		// Ignorable @-rule
		AbstractCSSRule rule;
		int tentNameLen = atRule.length();
		if (tentNameLen > 21) {
			tentNameLen = 21;
		}
		newRule();
		rule = parentSheet.createUnknownRule();
		if (atRule.charAt(1) != '-') {
			// Unknown non-custom rule
			parentSheet.getErrorHandler().unknownRule(atRule);
		}
		try {
			rule.setCssText(atRule);
		} catch (DOMException e) {
			parentSheet.getErrorHandler().badAtRule(e, atRule);
			return;
		}
		setCommentsToRule(rule);
		if (currentRule != null) {
			addToCurrentRule(rule);
		} else {
			// Inserting rule into sheet
			addLocalRule(rule);
			resetCurrentRule();
		}
	}

	private void newRule() {
		lastRule = null;
	}

	protected void addLocalRule(AbstractCSSRule rule) {
		parentSheet.addLocalRule(rule);
	}

	private void addToCurrentRule(AbstractCSSRule rule) {
		try {
			((GroupingRule) currentRule).addRule(rule);
		} catch (ClassCastException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR,
					"Found @-rule inside a non-grouping rule of type: " + currentRule.getType());
			parentSheet.getErrorHandler().badAtRule(ex, rule.getCssText());
			lastRule = null;
		}
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) {
		newRule();
		if (ignoreGroupingRules == 0) {
			// Setting namespace uri
			parentSheet.setNamespace(prefix, uri);
			NamespaceRule rule = parentSheet.createNamespaceRule(prefix, uri);
			if (currentRule != null) {
				addToCurrentRule(rule);
			} else {
				// Inserting rule into sheet
				addLocalRule(rule);
			}
			resetCurrentRule();
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void importStyle(String uri, MediaQueryList media, String defaultNamespaceURI) {
		// Ignore any '@import' rule that occurs inside a block or after any
		// non-ignored statement other than an @charset or an @import rule
		// (CSS 2.1 §4.1.5)
		if (ignoreImports) {
			SheetErrorHandler eh = parentSheet.getErrorHandler();
			eh.ignoredImport(uri);
			resetCommentStack();
			return;
		}
		if (parentSheet.match(parentSheet.getMedia(), media)) {
			if (!media.isNotAllMedia()) {
				if (currentRule == null) { // That should be always true
					// Importing rule from uri
					ImportRule imp = parentSheet.createImportRule(media, uri);
					setCommentsToRule(imp);
					addLocalRule(imp);
				}
			} else {
				parentSheet.getErrorHandler().badMediaList(media);
			}
		} else { // Ignoring @import from uri due to target media mismatch
			resetCommentStack();
		}
	}

	@Override
	public void startSupports(BooleanCondition condition) {
		// Starting @supports block
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			SheetErrorHandler eh;
			if (currentRule != null) {
				short ruleType = currentRule.getType();
				if (ruleType == CSSRule.MEDIA_RULE || ruleType == CSSRule.SUPPORTS_RULE) {
					SupportsRule rule = new SupportsRule(parentSheet, condition, sheetOrigin);
					addToCurrentRule(rule);
					currentRule = rule;
					setCommentsToRule(currentRule);
				} else if ((eh = parentSheet.getErrorHandler()) != null) {
					eh.sacMalfunction("Unexpected supports rule inside of: " + currentRule.getCssText());
					ignoreGroupingRules = 1;
					return;
				}
			} else {
				currentRule = new SupportsRule(parentSheet, condition, sheetOrigin);
				setCommentsToRule(currentRule);
			}
		} else {
			ignoreGroupingRules++;
		}
	}

	@Override
	public void endSupports(BooleanCondition condition) {
		endGroupingRule();
	}

	void endGroupingRule() {
		if (ignoreGroupingRules != 0) {
			ignoreGroupingRules--;
			resetCommentStack();
		} else {
			if (currentRule != null) {
				lastRule = currentRule;
				AbstractCSSRule pRule = currentRule.getParentRule();
				if (pRule == null) {
					// Inserting grouping rule into sheet
					addLocalRule(currentRule);
					resetCurrentRule();
				} else {
					resetCurrentRule();
					// Restore parent rule
					currentRule = pRule;
				}
			}
		}
	}

	@Override
	public void startMedia(MediaQueryList media) {
		// Starting @media block
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			SheetErrorHandler eh;
			if (currentRule != null) {
				short ruleType = currentRule.getType();
				if (ruleType == CSSRule.MEDIA_RULE || ruleType == CSSRule.SUPPORTS_RULE) {
					MediaRule rule = new MediaRule(parentSheet, media, sheetOrigin);
					addToCurrentRule(rule);
					currentRule = rule;
					setCommentsToRule(currentRule);
				} else if ((eh = parentSheet.getErrorHandler()) != null) {
					eh.sacMalfunction("Unexpected media rule inside of: " + currentRule.getCssText());
					ignoreGroupingRules = 1;
					return;
				}
			} else {
				currentRule = new MediaRule(parentSheet, media, sheetOrigin);
				setCommentsToRule(currentRule);
			}
		} else {
			ignoreGroupingRules++;
		}
	}

	@Override
	public void endMedia(MediaQueryList media) {
		endGroupingRule();
	}

	@Override
	public void startPage(PageSelectorList pageSelectorList) {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			PageRule pageRule = parentSheet.createPageRule();
			pageRule.setParentRule(currentRule);
			pageRule.setSelectorList(pageSelectorList);
			currentRule = pageRule;
			setCommentsToRule(currentRule);
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void endPage(PageSelectorList pageSelectorList) {
		endGenericRule();
	}

	@Override
	public void startMargin(String name) {
		newRule();
		if (ignoreGroupingRules == 0) {
			assert(currentRule != null && currentRule.getType() == CSSRule.PAGE_RULE);
			MarginRule marginRule = parentSheet.createMarginRule(name);
			marginRule.setParentRule(currentRule);
			currentRule = marginRule;
		} else { // Ignored @page
			resetCommentStack();
		}
	}

	@Override
	public void endMargin() {
		if (ignoreGroupingRules == 0) {
			assert(currentRule != null && currentRule.getType() == CSSRule.MARGIN_RULE);
			lastRule = currentRule;
			AbstractCSSRule pRule = currentRule.getParentRule();
			PageRule pageRule = (PageRule) pRule;
			pageRule.addMarginRule((MarginRule) currentRule);
			currentRule = pRule;
		}
		resetCommentStack();
	}

	@Override
	public void startFontFace() {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			FontFaceRule rule = new FontFaceRule(parentSheet, sheetOrigin);
			rule.setParentRule(currentRule);
			currentRule = rule;
			setCommentsToRule(currentRule);
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void endFontFace() {
		endGenericRule();
	}

	private void endGenericRule() {
		if (ignoreGroupingRules == 0) {
			if (currentRule != null) {
				lastRule = currentRule;
				AbstractCSSRule pRule = currentRule.getParentRule();
				if (pRule == null) {
					// Inserting rule into sheet
					addLocalRule(currentRule);
					resetCurrentRule();
				} else {
					addCurrentRuleToRule(pRule);
					resetCommentStack();
					// Restore parent rule
					currentRule = pRule;
				}
			}
		} else {
			resetCommentStack();
		}
	}

	private void addCurrentRuleToRule(AbstractCSSRule rule) {
		try {
			((GroupingRule) rule).addRule(currentRule);
		} catch (ClassCastException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR,
					"Found @-rule inside a non-grouping rule of type: " + rule.getType());
			parentSheet.getErrorHandler().badAtRule(ex, currentRule.getCssText());
		}
	}

	@Override
	public void startCounterStyle(String name) {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			CounterStyleRule rule = parentSheet.createCounterStyleRule(name);
			rule.setParentRule(currentRule);
			currentRule = rule;
			setCommentsToRule(currentRule);
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void endCounterStyle() {
		endGenericRule();
	}

	@Override
	public void startKeyframes(String name) {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			KeyframesRule rule = parentSheet.createKeyframesRule(name);
			rule.setParentRule(currentRule);
			currentRule = rule;
			setCommentsToRule(currentRule);
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void endKeyframes() {
		endGenericRule();
	}

	@Override
	public void startKeyframe(LexicalUnit keyframeSelector) {
		newRule();
		if (ignoreGroupingRules == 0) {
			KeyframesRule kfs = (KeyframesRule) currentRule;
			KeyframeRule rule = new KeyframeRule(kfs);
			rule.setKeyText(KeyframesRule.keyframeSelector(keyframeSelector));
			kfs.getCssRules().add(rule);
			currentRule = rule;
			setCommentsToRule(currentRule);
		}
	}

	@Override
	public void endKeyframe() {
		if (ignoreGroupingRules == 0) {
			assert(currentRule != null && currentRule.getType() == CSSRule.KEYFRAME_RULE);
			lastRule = currentRule;
			currentRule = currentRule.getParentRule();
		}
		resetCommentStack();
	}

	@Override
	public void startFontFeatures(String[] familyName) {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			FontFeatureValuesRule rule = parentSheet.createFontFeatureValuesRule(familyName);
			rule.setParentRule(currentRule);
			currentRule = rule;
			setCommentsToRule(currentRule);
			CSSHandler ffhandler = rule.createFontFeatureValuesHandler(this, parserctl);
			ffhandler.startFontFeatures(familyName);
			parserctl.setDocumentHandler(ffhandler);
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void endFontFeatures() {
		throw new IllegalStateException();
	}

	@Override
	public void startFeatureMap(String mapName) {
		throw new IllegalStateException();
	}

	@Override
	public void endFeatureMap() {
		throw new IllegalStateException();
	}

	@Override
	public void endSubHandler(short handlerId) {
		parserctl.setDocumentHandler(this);
		endGenericRule();
	}

	@Override
	public void startViewport() {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			ViewportRule rule = parentSheet.createViewportRule();
			rule.setParentRule(currentRule);
			currentRule = rule;
			setCommentsToRule(currentRule);
		} else {
			resetCommentStack();
		}
	}

	@Override
	public void endViewport() {
		endGenericRule();
	}

	@Override
	public void startSelector(SelectorList selectors) {
		ignoreImports = true;
		newRule();
		if (ignoreGroupingRules == 0) {
			StyleRule styleRule = parentSheet.createStyleRule();
			if (currentRule != null) {
				styleRule.setParentRule(currentRule);
			}
			currentRule = styleRule;
			((CSSStyleDeclarationRule) currentRule).setSelectorList(selectors);
			setCommentsToRule(currentRule);
		} else { // Ignoring rule for these selectors due to target media mismatch
			resetCommentStack();
		}
	}

	@Override
	public void endSelector(SelectorList selectors) {
		if (ignoreGroupingRules == 0) {
			assert(currentRule != null && currentRule.getType() == CSSRule.STYLE_RULE);
			lastRule = currentRule;
			BaseCSSRule pRule = (BaseCSSRule) currentRule.getParentRule();
			if (((StyleRule) currentRule).getStyle().getLength() == 0) {
				SheetErrorHandler eh = parentSheet.getErrorHandler();
				eh.emptyStyleRule(((StyleRule) currentRule).getSelectorText());
			} else {
				if (pRule == null) {
					// Inserting rule into sheet
					if (currentRule != null)
						addLocalRule(currentRule);
				} else {
					((GroupingRule) pRule).addRule(currentRule);
				}
			}
			currentRule = pRule;
		}
		resetCommentStack();
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important, int index) {
		if (ignoreGroupingRules == 0) {
			if (currentRule != null) {
				try {
					((BaseCSSDeclarationRule) currentRule).getStyle().setProperty(name,
							value, important, index);
				} catch (RuntimeException e) {
					CSSPropertyValueException ex = new CSSPropertyValueException(e);
					ex.setValueText(value.toString());
					((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().wrongValue(name, ex);
					// NSAC report
					Locator locator = parserctl.createLocator(index);
					CSSParseException pe = new CSSParseException("Invalid value for property " + name, locator, e);
					error(pe);
				}
			} else {
				/*
				 * A property was received for being processed outside of a rule. This should
				 * never happen, and if it happens it means that the NSAC parser is
				 * malfunctioning.
				 */
				parentSheet.getErrorHandler().sacMalfunction("Unexpected property " + name + ": " + value.toString());
			}
		} // else { Ignoring property due to target media mismatch
	}

	private void resetCurrentRule() {
		currentRule = null;
		resetCommentStack();
	}

	private void setCommentsToRule(AbstractCSSRule rule) {
		if (comments != null && !comments.isEmpty()) {
			LinkedStringList ruleComments = new LinkedStringList();
			ruleComments.addAll(comments);
			rule.setPrecedingComments(ruleComments);
		}
		resetCommentStack();
	}

	private void resetCommentStack() {
		if (comments != null) {
			comments.clear();
		}
	}

	@Override
	public String getNamespaceURI(String nsPrefix) {
		return parentSheet.getNamespaceURI(nsPrefix);
	}

	@Override
	public void warning(CSSParseException exception) throws CSSParseException {
		if (currentRule != null && currentRule instanceof BaseCSSDeclarationRule
				&& ((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler() != null) {
			int previousIndex = -1;
			CSSStyleDeclaration style = ((BaseCSSDeclarationRule) currentRule).getStyle();
			if (style != null) {
				previousIndex = style.getLength() - 1;
			}
			((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().sacWarning(exception,
					previousIndex);
		} else {
			// Handle as non-specific warning
			parentSheet.getErrorHandler().handleSacWarning(exception);
		}
	}

	@Override
	public void error(CSSParseException exception) throws CSSParseException {
		if (currentRuleCanHandleError()) {
			int previousIndex = -1;
			CSSStyleDeclaration style = ((BaseCSSDeclarationRule) currentRule).getStyle();
			if (style != null) {
				previousIndex = style.getLength() - 1;
			}
			((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().sacError(exception, previousIndex);
			parentSheet.getErrorHandler().mapError(exception, currentRule);
		} else {
			// Handle as non-specific error
			nonRuleErrorHandling(exception);
		}
	}

	/*
	 * Current rule can handle the error if it is set (not null), it is a declaration rule and
	 * contains a declaration error handler.
	 */
	private boolean currentRuleCanHandleError() {
		return currentRule != null && currentRule instanceof BaseCSSDeclarationRule
				&& ((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler() != null;
	}

	private void nonRuleErrorHandling(CSSParseException exception) {
		parentSheet.getErrorHandler().handleSacError(exception);
		if (outOfRuleException == null) {
			outOfRuleException = exception;
		}
	}

	CSSParseException getOutOfRuleException() {
		return outOfRuleException;
	}

}
