/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.om.StyleRule.RuleSpecifity;

/**
 * Base implementation for <code>DocumentCSSStyleSheet</code>.
 */
abstract public class BaseDocumentCSSStyleSheet extends BaseCSSStyleSheet implements DocumentCSSStyleSheet, Cloneable {

	private String targetMedium = null;

	protected BaseDocumentCSSStyleSheet(String medium, byte origin) {
		super(null, new MediaQueryListImpl(medium), null, origin);
		if ("all".equals(medium)) {
			targetMedium = null;
		} else {
			targetMedium = medium;
		}
	}

	@Override
	public void setHref(String href) {
		throw new IllegalStateException("Document sheet's href is parent document href");
	}

	@Override
	public String getHref() {
		return getOwnerNode() != null ? getOwnerNode().getBaseURI() : null;
	}

	@Override
	abstract public CSSDocument getOwnerNode();

	abstract public void setOwnerDocument(CSSDocument ownerNode);

	/*
	 * Copy the contents of this style sheet to the supplied one, but only
	 * preserving rules compatible with the target media of the receiving sheet.
	 * 
	 */
	protected void copyToTarget(BaseDocumentCSSStyleSheet myCopy) {
		if (myCopy.getTargetMedium() == null) {
			throw new NullPointerException();
		}
		CSSDocument doc = myCopy.getOwnerNode();
		CSSCanvas canvas;
		if (doc != null) {
			canvas = doc.getCanvas();
		} else {
			canvas = null;
		}
		if (!getMedia().matches(myCopy.getTargetMedium(), canvas)) {
			throw new IllegalArgumentException("Incompatible target medium: " + targetMedium);
		}
		copyFieldsTo(myCopy);
		myCopy.cssRules.ensureCapacity(cssRules.getLength());
		Iterator<AbstractCSSRule> it = cssRules.iterator();
		while (it.hasNext()) {
			AbstractCSSRule rule = it.next();
			int type = rule.getType();
			if ((type == CSSRule.MEDIA_RULE && !((MediaRule) rule).getMedia().matches(targetMedium, canvas))
					|| (type == CSSRule.IMPORT_RULE
							&& !((ImportRule) rule).getMedia().matches(targetMedium, canvas))) {
				continue;
			}
			myCopy.cssRules.add(rule.clone(myCopy));
		}
	}

	/**
	 * Gets the target medium for this sheet.
	 * 
	 * @return the target medium, or null if is for all media.
	 */
	@Override
	public String getTargetMedium() {
		return targetMedium;
	}

	/**
	 * Gets the computed style for the given element and pseudo-element.
	 * 
	 * @param elm
	 *            the element.
	 * @param pseudoElt
	 *            the pseudo-element condition.
	 * @return the computed style declaration.
	 */
	@Override
	abstract public ComputedCSSStyle getComputedStyle(CSSElement elm, Condition pseudoElt);

	abstract protected ComputedCSSStyle createComputedCSSStyle();

	/**
	 * Clone this style sheet.
	 * 
	 * @return the cloned style sheet.
	 */
	@Override
	abstract public BaseDocumentCSSStyleSheet clone();

	/**
	 * Clone this style sheet, but only preserving rules targeting the given
	 * medium.
	 * 
	 * @param targetMedium
	 *            the medium.
	 * @return a medium-specific pseudo-clone of this sheet.
	 */
	@Override
	abstract public BaseDocumentCSSStyleSheet clone(String targetMedium);

	/**
	 * Compute the style for an element.
	 * 
	 * @param style
	 *            a base, empty style to be filled with the computed style.
	 * @param matcher
	 *            the selector matcher.
	 * @param pseudoElt
	 *            the pseudo-element.
	 * @param inlineStyle
	 *            the inline style for the element.
	 * @return the computed CSS style, or an empty style declaration if none
	 *         applied or the sheet is disabled.
	 */
	protected ComputedCSSStyle computeStyle(ComputedCSSStyle style, SelectorMatcher matcher,
			Condition pseudoElt, InlineStyle inlineStyle) {
		// This check for the disabled attribute is required for spec
		// compliance.
		if (getDisabled()) {
			return style;
		}
		// Set the pseudo-element
		matcher.setPseudoElement(pseudoElt);
		// Obtain the owner element and look for non-CSS presentational hints.
		CSSElement elt = style.getOwnerNode();
		ErrorHandler errHandler = elt.getOwnerDocument().getErrorHandler();
		errHandler.resetComputedStyleErrors(elt);
		if (elt.hasPresentationalHints()) {
			try {
				elt.exportHintsToStyle(style);
			} catch (DOMException e) {
				errHandler.presentationalHintError(elt, e);
			}
		}
		/*
		 * We build a sorted set of styles that apply to the given element.
		 */
		Cascade matchingStyles = new Cascade();
		matchingStyles.cascade(matcher, getTargetMedium(), cssRules);
		/*
		 * The styles are sorted according to its specificity, per the
		 * SpecificityComparator.
		 */
		Iterator<StyleRule> styleit = matchingStyles.iterator();
		/*
		 * Now we add all the styles to form a single declaration. We add them
		 * according to the order specified by the sorted set.
		 * 
		 * Each more specific style is added, starting with the less specific
		 * declaration.
		 */
		while (styleit.hasNext()) {
			StyleRule rule = styleit.next();
			style.addStyle((BaseCSSStyleDeclaration) rule.getStyle());
		}
		// The inline style has higher priority, so we add it at the end.
		if (inlineStyle != null && !inlineStyle.isEmpty()) {
			style.addStyle(inlineStyle);
		}
		// Now the override style.
		if (elt.hasOverrideStyle(pseudoElt)) {
			BaseCSSStyleDeclaration ovstyle = (BaseCSSStyleDeclaration) elt.getOverrideStyle(pseudoElt);
			style.addStyle(ovstyle);
		}
		// Finally, the user's important style sheet.
		AbstractCSSStyleSheet userImportantStyleSheet = getStyleSheetFactory().getUserImportantStyleSheet();
		if (userImportantStyleSheet != null) {
			// Build a new cascade
			Cascade usercascade = new Cascade();
			usercascade.cascade(matcher, getTargetMedium(), userImportantStyleSheet.getCssRules());
			styleit = usercascade.iterator();
			while (styleit.hasNext()) {
				StyleRule rule = styleit.next();
				style.addStyle((BaseCSSStyleDeclaration) rule.getStyle());
			}
		}
		return style;
	}

	protected ComputedCSSStyle computeRevertedStyle(ComputedCSSStyle style, SelectorMatcher matcher,
			Condition pseudoElt, BaseCSSStyleDeclaration inlineStyle, byte origin) {
		// Set the pseudo-element
		matcher.setPseudoElement(pseudoElt);
		// Obtain the owner element and look for non-CSS presentational hints.
		CSSElement elt = style.getOwnerNode();
		ErrorHandler errHandler = elt.getOwnerDocument().getErrorHandler();
		errHandler.resetComputedStyleErrors(elt);
		if (origin >= CSSStyleSheetFactory.ORIGIN_AUTHOR) {
			if (elt.hasPresentationalHints()) {
				try {
					elt.exportHintsToStyle(style);
				} catch (DOMException e) {
					errHandler.presentationalHintError(elt, e);
				}
			}
		}
		/*
		 * We build a sorted set of styles that apply to the given element.
		 */
		Cascade matchingStyles = new Cascade();
		matchingStyles.cascade(matcher, getTargetMedium(), cssRules, origin);
		/*
		 * The styles are sorted according to its specificity, per the
		 * SpecificityComparator.
		 */
		Iterator<StyleRule> styleit = matchingStyles.iterator();
		/*
		 * Now we add all the styles to form a single declaration. We add them
		 * according to the order specified by the sorted set.
		 * 
		 * Each more specific style is added, starting with the less specific
		 * declaration.
		 */
		while (styleit.hasNext()) {
			StyleRule rule = styleit.next();
			style.addStyle((BaseCSSStyleDeclaration) rule.getStyle());
		}
		if (origin >= CSSStyleSheetFactory.ORIGIN_AUTHOR) {
			// The inline style has higher priority, so we add it at the end.
			if (inlineStyle != null && !inlineStyle.isEmpty()) {
				style.addStyle(inlineStyle);
			}
			// Now the override style.
			if (elt.hasOverrideStyle(pseudoElt)) {
				style.addStyle((BaseCSSStyleDeclaration) elt.getOverrideStyle(pseudoElt));
			}
		}
		// Finally, the user's important style sheet.
		if (origin >= CSSStyleSheetFactory.ORIGIN_USER_IMPORTANT) {
			AbstractCSSStyleSheet userImportantStyleSheet = getStyleSheetFactory().getUserImportantStyleSheet();
			if (userImportantStyleSheet != null) {
				// Build a new cascade
				Cascade usercascade = new Cascade();
				usercascade.cascade(matcher, getTargetMedium(), userImportantStyleSheet.getCssRules());
				styleit = usercascade.iterator();
				while (styleit.hasNext()) {
					StyleRule rule = styleit.next();
					style.addStyle((BaseCSSStyleDeclaration) rule.getStyle());
				}
			}
		}
		return style;
	}

	CSSCanvas getCanvas() {
		CSSCanvas canvas;
		if (getOwnerNode() != null) {
			canvas = getOwnerNode().getCanvas();
		} else {
			canvas = null;
		}
		return canvas;
	}

	class Cascade {
		private final SortedMap<StyleRule.RuleSpecifity, LinkedList<StyleRule>> matchingStyles = new TreeMap<StyleRule.RuleSpecifity, LinkedList<StyleRule>>(
				new StyleRule.SpecificityComparator());

		Cascade() {
			super();
		}

		void cascade(SelectorMatcher matcher, String targetMedium, CSSRuleArrayList list) {
			Iterator<AbstractCSSRule> it = list.iterator();
			while (it.hasNext()) {
				CSSRule rule = it.next();
				short type = rule.getType();
				if (type != CSSRule.STYLE_RULE && type != CSSRule.PAGE_RULE) {
					if (type == CSSRule.MEDIA_RULE) {
						scanMediaRule(matcher, targetMedium, getCanvas(), (MediaRule) rule);
					} else if (type == CSSRule.FONT_FACE_RULE) {
						processFontFaceRule((FontFaceRule) rule, targetMedium);
					} else if (type == CSSRule.SUPPORTS_RULE) {
						SupportsRule supports = (SupportsRule) rule;
						DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
						StyleDatabase sdb;
						if (df != null && (sdb = df.getStyleDatabase(targetMedium)) != null && supports.supports(sdb)) {
							CSSRuleArrayList rules = supports.getCssRules();
							cascade(matcher, targetMedium, rules);
						}
					} // we find no @import rules here
					continue;
				}
				StyleRule stylerule = (StyleRule) rule;
				int selIdx = matcher.matches(stylerule.getSelectorList());
				if (selIdx != -1) {
					add(stylerule.getSpecifity(selIdx));
				}
			}
		}

		private void scanMediaRule(SelectorMatcher matcher, String targetMedium, CSSCanvas canvas, MediaRule mediaRule) {
			MediaQueryList mediaList = mediaRule.getMedia();
			// If we target a specific media, account for matching @media rules,
			// otherwise ignore them.
			if (targetMedium != null && mediaList.matches(targetMedium, canvas)) {
				CSSRuleArrayList ruleList = mediaRule.getCssRules();
				cascade(matcher, targetMedium, ruleList);
			}
		}

		private void processFontFaceRule(FontFaceRule rule, String targetMedium) {
			DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
			if (df != null) {
				StyleDatabase sdb = df.getStyleDatabase(targetMedium);
				if (sdb != null) {
					sdb.loadFontFaceRule(rule);
				}
			}
		}

		void add(StyleRule.RuleSpecifity sp) {
			if (matchingStyles.containsKey(sp)) {
				matchingStyles.get(sp).add(sp.getCSSStyleRule());
			} else {
				LinkedList<StyleRule> matchingRules = new LinkedList<StyleRule>();
				matchingRules.add(sp.getCSSStyleRule());
				matchingStyles.put(sp, matchingRules);
			}
		}

		public void cascade(SelectorMatcher matcher, String targetMedium, CSSRuleArrayList list, byte origin) {
			Iterator<AbstractCSSRule> it = list.iterator();
			while (it.hasNext()) {
				AbstractCSSRule rule = it.next();
				if (rule.getOrigin() >= origin) {
					short type = rule.getType();
					if (type != CSSRule.STYLE_RULE && type != CSSRule.PAGE_RULE) {
						if (type == CSSRule.MEDIA_RULE) {
							scanMediaRule(matcher, targetMedium, getCanvas(), (MediaRule) rule);
						} else if (type == CSSRule.FONT_FACE_RULE) {
							processFontFaceRule((FontFaceRule) rule, targetMedium);
						} else if (type == CSSRule.SUPPORTS_RULE) {
							SupportsRule supports = (SupportsRule) rule;
							DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
							StyleDatabase sdb;
							if (df != null && (sdb = df.getStyleDatabase(targetMedium)) != null
									&& supports.supports(sdb)) {
								CSSRuleArrayList rules = supports.getCssRules();
								cascade(matcher, targetMedium, rules);
							}
						} // we find no @import rules here
						continue;
					}
					StyleRule stylerule = (StyleRule) rule;
					int selIdx = matcher.matches(stylerule.getSelectorList());
					if (selIdx != -1) {
						add(stylerule.getSpecifity(selIdx));
					}
				}
			}
		}

		Iterator<StyleRule> iterator() {
			return new RuleIterator();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(256);
			Iterator<StyleRule> it = iterator();
			while (it.hasNext()) {
				StyleRule rule = it.next();
				buf.append(rule.getOrigin()).append(": ").append(rule.getCssText()).append('\n');
			}
			return buf.toString();
		}

		class RuleIterator implements Iterator<StyleRule> {

			private final Iterator<RuleSpecifity> keyit;
			private Iterator<StyleRule> currentList;

			RuleIterator() {
				super();
				keyit = matchingStyles.keySet().iterator();
				if (keyit.hasNext()) {
					findCurrentList();
				} else {
					currentList = null;
				}
			}

			private void findCurrentList() {
				while (currentList == null || !currentList.hasNext()) {
					if (keyit.hasNext()) {
						currentList = matchingStyles.get(keyit.next()).iterator();
					} else {
						currentList = null;
						break;
					}
				}
			}

			@Override
			public boolean hasNext() {
				return currentList != null;
			}

			@Override
			public StyleRule next() {
				if (currentList != null) {
					if (currentList.hasNext()) {
						StyleRule thenext = currentList.next();
						findCurrentList();
						return thenext;
					}
				}
				throw new NoSuchElementException();
			}

		}
	}
}
