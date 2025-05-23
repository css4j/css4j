/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSMediaRule;
import org.w3c.dom.stylesheets.MediaList;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSRuleList;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

public class DOMCSSStyleSheetTest {

	private BaseCSSStyleSheet sheet = null;

	@BeforeEach
	public void setUp() throws IOException {
		sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
	}

	@Test
	public void insertRule() {
		int szPre = sheet.getCssRules().getLength();
		int i = sheet.insertRule("p.sample {display: block}", 0);
		assertEquals(0, i);
		assertEquals(szPre + 1, sheet.getCssRules().getLength());
		assertEquals("p.sample {display: block; }", sheet.getCssRules().item(0).getCssText());
	}

	@Test
	public void deleteRule() {
		sheet.deleteRule(0);
	}

	@Test
	public void deleteRuleError() {
		Assertions.assertThrows(DOMException.class,
				() -> sheet.deleteRule(sheet.getCssRules().getLength()));
	}

	@Test
	public void parseCSSStyleSheet() throws DOMException, IOException {
		int defSz = sheet.getCssRules().getLength();
		AbstractCSSStyleSheet cloned = sheet.clone();
		assertNotNull(cloned);
		assertEquals(defSz, cloned.getCssRules().getLength());
		Reader re = SampleCSS.loadSampleCSSReader();
		assertNotNull(re);
		assertTrue(sheet.parseStyleSheet(re));
		re.close();
		assertEquals(defSz + SampleCSS.RULES_IN_SAMPLE_CSS, sheet.getCssRules().getLength());
		assertEquals("/* Comment before li */li {margin-top: 1em; margin-bottom: 1em; }",
				sheet.getCssRules().item(defSz + SampleCSS.RULES_IN_SAMPLE_CSS - 6).getCssText());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet2 = factory.createStyleSheet("", null);
		boolean parseok = sheet2.parseStyleSheet(new StringReader(sheet.toString()));
		if (!parseok) {
			System.err.println(sheet2.getErrorHandler().toString());
		}
		assertTrue(parseok);
		CSSRuleList<?> rules = sheet.getCssRules();
		CSSRuleList<?> rules2 = sheet2.getCssRules();
		assertEquals(rules.getLength(), rules2.getLength());
		int n = rules.getLength();
		for (int i = 0; i < n; i++) {
			CSSRule rule = rules.item(i);
			CSSRule rule2 = rules2.item(i);
			assertEquals(rule.getType(), rule2.getType());
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
				StyleRule sr = (StyleRule) rule;
				StyleRule sr2 = (StyleRule) rule2;
				SelectorList selist = sr.getSelectorList();
				SelectorList selist2 = sr2.getSelectorList();
				int selistlen = selist.getLength();
				assertEquals(selistlen, selist2.getLength());
				for (int j = 0; j < selistlen; j++) {
					assertTrue(selectorEquals(selist.item(j), selist2.item(j)));
				}
				AbstractCSSStyleDeclaration style = sr.getStyle();
				AbstractCSSStyleDeclaration style2 = sr2.getStyle();
				int sl = style.getLength();
				assertEquals(sl, style2.getLength());
				for (int j = 0; j < sl; j++) {
					String propertyName = style.item(j);
					assertEquals(propertyName, style2.item(j));
					String sv = null, sv2 = null;
					boolean is_system_def = false;
					try {
						sv = style.getPropertyValue(propertyName);
					} catch (StyleDatabaseRequiredException e) {
						is_system_def = true;
					}
					try {
						sv2 = style2.getPropertyValue(propertyName);
						if (is_system_def) {
							fail("Left side is a system default, right side is not.");
						}
					} catch (StyleDatabaseRequiredException e) {
						if (!is_system_def) {
							fail("Right side is a system default, left side is not.");
						}
					}
					assertEquals(sv, sv2);
				}
				break;
			case CSSRule.MEDIA_RULE:
				CSSMediaRule mrule = (CSSMediaRule) rule;
				MediaList ml = mrule.getMedia();
				MediaList ml2 = ((CSSMediaRule) rule2).getMedia();
				int mllen = ml.getLength();
				assertEquals(mllen, ml2.getLength());
				break;
			}
		}
	}

	/**
	 * Compares (approximately) one selector to another.
	 * 
	 */
	private static boolean selectorEquals(Selector s, Selector s2) {
		Selector.SelectorType stype = s.getSelectorType();
		if (stype != s2.getSelectorType()) {
			return false;
		}
		switch (stype) {
		case ELEMENT:
			String local = ((ElementSelector) s).getLocalName();
			if (local != null) {
				return local.equals(((ElementSelector) s2).getLocalName());
			} else {
				return ((ElementSelector) s2).getLocalName() == null;
			}
		case CHILD:
		case DESCENDANT:
			CombinatorSelector dsel = (CombinatorSelector) s;
			CombinatorSelector dsel2 = (CombinatorSelector) s2;
			if (dsel.getSelector().getSelectorType() != dsel2.getSelector().getSelectorType()) {
				return false;
			}
			return selectorEquals(dsel.getSecondSelector(), dsel2.getSecondSelector());
		case CONDITIONAL:
			ConditionalSelector csel = (ConditionalSelector) s;
			ConditionalSelector csel2 = (ConditionalSelector) s2;
			SimpleSelector ssel = csel.getSimpleSelector();
			if (!selectorEquals(ssel, csel2.getSimpleSelector())) {
				return false;
			}
			return csel.getCondition().getConditionType() == csel2.getCondition()
					.getConditionType();
		case DIRECT_ADJACENT:
		case SUBSEQUENT_SIBLING:
		case COLUMN_COMBINATOR:
			CombinatorSelector asel = (CombinatorSelector) s;
			CombinatorSelector asel2 = (CombinatorSelector) s2;
			return selectorEquals(asel.getSelector(), asel2.getSelector())
					&& selectorEquals(asel.getSecondSelector(), asel2.getSecondSelector());
		default:
			return true;
		}
	}

}
