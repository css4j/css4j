/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

public class StyleRuleTest2 {

	private static TestCSSStyleSheetFactory myfactory;

	private AbstractCSSStyleSheet sheet;
	private StyleRule styleRule;
	private BaseCSSStyleDeclaration emptyStyleDecl;
	private StyleRule frameRule = null;
	private StyleRule framesetRule = null;
	private StyleRule noframesRule = null;

	@BeforeAll
	public static void setUpBeforeAll() {
		myfactory = new TestCSSStyleSheetFactory();
		myfactory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
	}

	@BeforeEach
	public void setUp() throws IOException {
		sheet = myfactory.createStyleSheet(null, null);
		Reader re = SampleCSS.loadSampleCSSReader();
		try {
			sheet.parseStyleSheet(re);
			re.close();
		} catch (IOException e) {
			e.printStackTrace();
			sheet = null;
			return;
		}
		styleRule = sheet.createStyleRule();
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
		assertEquals("display: block;\nborder: none ! important;\n",
				frameRule.getStyle().getCssText());
		assertEquals(
				"/* Comment before frame */\nframe {\n    display: block;\n    border: none ! important;\n}\n",
				frameRule.getCssText());
	}

	@Test
	public void getMinifiedCssText() {
		assertEquals("display:block;border:none!important;",
				frameRule.getStyle().getMinifiedCssText());
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
		AbstractCSSStyleDeclaration styleDecl = framesetRule.getStyle();
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
		assertEquals("border-right-width", frameRule.getStyle().item(2));
		assertEquals("border-bottom-width", frameRule.getStyle().item(3));
		assertEquals("border-left-width", frameRule.getStyle().item(4));
		assertEquals("border-top-style", frameRule.getStyle().item(5));
		assertEquals("border-left-color", frameRule.getStyle().item(12));
		assertEquals("border-image-source", frameRule.getStyle().item(13));
		assertEquals("border-image-slice", frameRule.getStyle().item(14));
		assertEquals("border-image-width", frameRule.getStyle().item(15));
		assertEquals("border-image-outset", frameRule.getStyle().item(16));
		assertEquals("border-image-repeat", frameRule.getStyle().item(17));
		assertEquals(18, frameRule.getStyle().getLength());
		assertEquals("", emptyStyleDecl.item(7));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() throws IOException {
		AbstractCSSStyleSheet sheet1 = myfactory.createStyleSheet(null, null);
		sheet1.parseStyleSheet(new StringReader("p,.foo {margin-top: 20%;}"));
		AbstractCSSRule rule = sheet1.getCssRules().item(0);
		AbstractCSSRule clon = rule.clone(sheet);
		assertEquals(rule.getOrigin(), clon.getOrigin());
		assertEquals(rule.getType(), clon.getType());
		assertEquals(rule.getCssText(), clon.getCssText());
	}

	StyleRule styleRuleFor(String selectorText, String propertyName) {
		CSSRuleList rules = sheet.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			if (rule instanceof StyleRule) {
				String selText = ((StyleRule) rule).getSelectorText();
				// Small hack
				StringTokenizer st = new StringTokenizer(selText, ",");
				while (st.hasMoreElements()) {
					String selector = st.nextToken();
					if (selector.equals(selectorText)) {
						if (((StyleRule) rule).getStyle()
								.getPropertyCSSValue(propertyName) != null) {
							return (StyleRule) rule;
						}
						break;
					}
				}
			}
		}
		return null;
	}

}
