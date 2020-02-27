/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;

public class StyleRuleTest2 {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	AbstractCSSStyleSheet sheet;
	StyleRule styleRule;
	BaseCSSStyleDeclaration emptyStyleDecl;
	CSSStyleRule frameRule = null;
	CSSStyleRule framesetRule = null;
	CSSStyleRule noframesRule = null;

	@Before
	public void setUp() throws IOException {
		DOMCSSStyleSheetFactory myfactory = new TestCSSStyleSheetFactory();
		sheet = myfactory.createStyleSheet(null, null);
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleCSSReader();
		try {
			sheet.parseStyleSheet(new InputSource(re));
			re.close();
		} catch (IOException e) {
			e.printStackTrace();
			sheet = null;
		}
		myfactory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		styleRule = (StyleRule) sheet.createStyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		frameRule = styleRuleFor("frame", "display");
		framesetRule = styleRuleFor("frameset", "display");
		noframesRule = styleRuleFor("noframes", "display");
		assertNotNull(frameRule);
		assertNotNull(framesetRule);
		assertNotNull(noframesRule);
	}

	@Test
	public void getCssText() {
		assertEquals("display: block;\nborder: none ! important;\n", frameRule.getStyle().getCssText());
		assertEquals("frame {\n    display: block;\n    border: none ! important;\n}\n", frameRule.getCssText());
	}

	@Test
	public void getMinifiedCssText() {
		assertEquals("display:block;border:none!important;",
				((AbstractCSSStyleDeclaration) frameRule.getStyle()).getMinifiedCssText());
	}

	@Test
	public void getPropertyValue() {
		assertEquals("none", frameRule.getStyle().getPropertyValue("border"));
	}

	@Test
	public void getPropertyCSSValue() {
		assertNotNull(framesetRule.getStyle().getPropertyCSSValue("display"));
		assertEquals("block", framesetRule.getStyle().getPropertyCSSValue("display").getCssText());
	}

	@Test
	public void getPropertyCSSValueShorthand() {
		assertNull(frameRule.getStyle().getPropertyCSSValue("border"));
	}

	@Test
	public void removeProperty() {
		CSSStyleDeclaration styleDecl = framesetRule.getStyle();
		assertEquals("none", styleDecl.removeProperty("border"));
		for (int i = 0; i < styleDecl.getLength(); i++) {
			assertNotSame("border", styleDecl.item(i));
		}
	}

	@Test
	public void getPropertyPriority() {
		assertEquals("important", frameRule.getStyle().getPropertyPriority("border"));
		assertEquals("", noframesRule.getStyle().getPropertyPriority("border"));
	}

	@Test
	public void item() {
		assertEquals("display", frameRule.getStyle().item(0));
		assertEquals("border-top-width", frameRule.getStyle().item(1));
		assertEquals("", emptyStyleDecl.item(7));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		StyleRule rule = new StyleRule(sheet, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		rule.setCssText("p,.foo {margin-top: 20%;}");
		CSSStyleDeclarationRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
	}

	CSSStyleRule styleRuleFor(String selectorText, String propertyName) {
		CSSRuleList rules = sheet.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			if (rule instanceof CSSStyleRule) {
				String selText = ((CSSStyleRule) rule).getSelectorText();
				// Small hack
				StringTokenizer st = new StringTokenizer(selText, ",");
				while (st.hasMoreElements()) {
					String selector = st.nextToken();
					if (selector.equals(selectorText)) {
						if (((CSSStyleRule) rule).getStyle().getPropertyCSSValue(propertyName) != null) {
							return (CSSStyleRule) rule;
						}
						break;
					}
				}
			}
		}
		return null;
	}
}
