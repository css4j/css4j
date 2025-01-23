/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.dom.TestDOMImplementation;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class BaseCSSStyleSheetTest2 {

	@Test
	public void testParseStyleSheetUserAgent() throws DOMException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		Reader re = loadFilefromClasspath("html.css");
		sheet.parseStyleSheet(re);
		re.close();
		//
		assertEquals(113, sheet.getCssRules().getLength());
		//
		assertEquals(CSSRule.STYLE_RULE, sheet.getCssRules().item(0).getType());
		StyleRule stylerule = (StyleRule) sheet.getCssRules().item(0);
		String result = stylerule.getCssText();
		result = result.replace("\r", ""); // Windows compatibility hack
		assertEquals(
				"/*\n * HTML/XHTML CSS, derived from the one in the W3C CSS 2.1 specification,\n * combined with the styles from HTML5 specification.\n */html,address,blockquote,body,dd,div,dl,dt,fieldset,form,frame,frameset,h1,h2,h3,h4,h5,h6,iframe,dir,article,aside,hgroup,nav,section,object,ol,p,ul,applet,center,hr,menu,pre,figure,figcaption,footer,header,legend,listing,plaintext,xmp {display: block; }",
				result);
		assertEquals(
				"html,address,blockquote,body,dd,div,dl,dt,fieldset,form,frame,frameset,h1,h2,h3,h4,h5,h6,iframe,dir,article,aside,hgroup,nav,section,object,ol,p,ul,applet,center,hr,menu,pre,figure,figcaption,footer,header,legend,listing,plaintext,xmp{display:block}",
				stylerule.getMinifiedCssText());
		assertNotNull(stylerule.getPrecedingComments());
		result = stylerule.getPrecedingComments().get(0);
		result = result.replace("\r", ""); // Windows compatibility hack
		assertEquals(
				"\n * HTML/XHTML CSS, derived from the one in the W3C CSS 2.1 specification,\n * combined with the styles from HTML5 specification.\n ",
				result);
		assertNull(stylerule.getTrailingComments());
		//
		assertEquals(CSSRule.STYLE_RULE, sheet.getCssRules().item(41).getType());
		stylerule = (StyleRule) sheet.getCssRules().item(41);
		assertEquals("/* The start attribute on ol elements */ol[start] {counter-reset: list-item calc(attr(start type(<integer>), 1) - 1); }", stylerule.getCssText());
		assertEquals("ol[start]{counter-reset:list-item calc(attr(start type(<integer>),1) - 1)}",
				stylerule.getMinifiedCssText());
		assertNotNull(stylerule.getPrecedingComments());
		assertEquals(" The start attribute on ol elements ", stylerule.getPrecedingComments().get(0));
		assertNull(stylerule.getTrailingComments());
		//
		assertEquals(CSSRule.STYLE_RULE, sheet.getCssRules().item(47).getType());
		stylerule = (StyleRule) sheet.getCssRules().item(47);
		assertEquals("wbr {content: '\\200B'; }", stylerule.getCssText());
		assertEquals("wbr{content:'\\200b'}", stylerule.getMinifiedCssText());
		assertNotNull(stylerule.getTrailingComments());
		assertEquals(" this also has bidi implications ", stylerule.getTrailingComments().get(0));

		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseStyleSheetPageRules() throws DOMException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		Reader re = loadFilefromClasspath("parser/page.css");
		sheet.parseStyleSheet(re);
		re.close();
		//
		assertEquals(4, sheet.getCssRules().getLength());
		assertEquals(CSSRule.STYLE_RULE, sheet.getCssRules().item(0).getType());
		StyleRule stylerule = (StyleRule) sheet.getCssRules().item(0);
		assertEquals("body{background-color:red}", stylerule.getMinifiedCssText());
		//
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(1).getType());
		PageRule pagerule = (PageRule) sheet.getCssRules().item(1);
		assertEquals("@page :first {margin-top: 20%; }", pagerule.getCssText());
		assertEquals("@page :first{margin-top:20%}", pagerule.getMinifiedCssText());
		//
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(2).getType());
		pagerule = (PageRule) sheet.getCssRules().item(2);
		assertEquals("@page foo:left {margin-left: 10%; @top-center {content: none; }@bottom-center {content: counter(page); }}", pagerule.getCssText());
		assertEquals("@page foo:left{margin-left:10%;@top-center{content:none}@bottom-center{content:counter(page)}}", pagerule.getMinifiedCssText());
		//
		assertEquals(CSSRule.PAGE_RULE, sheet.getCssRules().item(3).getType());
		pagerule = (PageRule) sheet.getCssRules().item(3);
		assertEquals("@page bar:right,:blank {margin-right: 2em; }", pagerule.getCssText());
		assertEquals("@page bar:right,:blank{margin-right:2em}", pagerule.getMinifiedCssText());

		assertFalse(sheet.getErrorHandler().hasSacErrors());

		// Visitor
		StyleCountVisitor visitor = new StyleCountVisitor();
		sheet.acceptStyleRuleVisitor(visitor);
		assertEquals(1, visitor.getCount());
		//
		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitorP);
		assertEquals(6, visitorP.getCount());
		//
		visitorP.reset();
		sheet.acceptDescriptorRuleVisitor(visitorP);
		assertEquals(5, visitorP.getCount());
	}

	@Test
	public void testParseStyleSheetComments() throws DOMException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		Reader re = loadFilefromClasspath("parser/comments.css");
		sheet.parseStyleSheet(re);
		re.close();
		//
		assertEquals(4, sheet.getCssRules().getLength());
		assertEquals(CSSRule.VIEWPORT_RULE, sheet.getCssRules().item(0).getType());
		ViewportRule vprule = (ViewportRule) sheet.getCssRules().item(0);
		assertEquals("@viewport{width:device-width}", vprule.getMinifiedCssText());
		assertEquals(1, vprule.getStyle().getLength());
		//
		assertEquals(CSSRule.UNKNOWN_RULE, sheet.getCssRules().item(1).getType());
		AbstractCSSRule unknown = sheet.getCssRules().item(1);
		assertEquals(
				"/* pre-rule-1-webkit */@-webkit-viewport /* skip-vw 1-webkit */{/* pre-viewport-decl-webkit */ width: /* skip-vw 2-webkit */device-width; /* post-viewport-decl-webkit */}",
				unknown.getCssText());
		//
		assertEquals(CSSRule.STYLE_RULE, sheet.getCssRules().item(2).getType());
		StyleRule stylerule = (StyleRule) sheet.getCssRules().item(2);
		assertEquals(1, stylerule.getStyle().getLength());
		assertEquals("body{background-color:red}", stylerule.getMinifiedCssText());
		//
		assertEquals(CSSRule.UNKNOWN_RULE, sheet.getCssRules().item(3).getType());
		AbstractCSSRule unknown2 = sheet.getCssRules().item(3);
		String result = unknown2.getCssText();
		assertEquals(
				"/* pre-webkit-kfs */@-webkit-keyframes important1 { /* pre-webkit-kf-list */from /* post-webkit-kfsel-from */{ /* pre-webkit-kf-from-decl */margin-top: 50px;/* post-webkit-kf-from-decl */ } /* post-webkit-kf-from */50% /* post-webkit-kfsel-50% */{/* pre-webkit-kf-50%-decl */margin-top: 150px !important; /* post-webkit-kf-50%-decl */} /* post-webkit-kf-50% */to/* post-webkit-kfsel-to */{ margin-top: 100px; }/* post-webkit-kf-to */ /* post-webkit-kf-list */}",
				result);
	}

	@Test
	public void testGetRulesForProperty() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		CSSRuleArrayList list = sheet.getRulesForProperty("content");
		assertNotNull(list);
		assertEquals(3, list.getLength());
	}

	@Test
	public void testGetSelectorsForPropertyValue() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		Selector[] selectors = sheet.getSelectorsForPropertyValue("display", "table-caption");
		assertEquals(1, selectors.length);
		assertEquals(Selector.SelectorType.ELEMENT, selectors[0].getSelectorType());
		assertEquals("caption", ((ElementSelector) selectors[0]).getLocalName());
	}

	@Test
	public void testGetSelectorsForPropertyValue2() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		Selector[] selectors = sheet.getSelectorsForPropertyValue("outline", "auto");
		assertEquals(1, selectors.length);
		assertEquals(Selector.SelectorType.CONDITIONAL, selectors[0].getSelectorType());
		assertEquals(Condition.ConditionType.PSEUDO_CLASS,
				((ConditionalSelector) selectors[0]).getCondition().getConditionType());
	}

	@Test
	public void testGetSelectorsForProperty() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		Selector[] selectors = sheet.getSelectorsForProperty("outline-style");
		assertEquals(2, selectors.length);
		assertEquals(Selector.SelectorType.CONDITIONAL, selectors[0].getSelectorType());
		assertEquals(Condition.ConditionType.PSEUDO_CLASS,
				((ConditionalSelector) selectors[0]).getCondition().getConditionType());
	}

	@Test
	public void testGetFirstStyleRule() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		CSSParser parser = new CSSParser();
		SelectorList selist = parser.parseSelectors("s, strike, del");
		StyleRule rule = sheet.getFirstStyleRule(selist);
		assertNotNull(rule);
		assertTrue(rule.getSelectorList().equals(selist));
		assertEquals("s,strike,del", rule.getSelectorList().toString());
	}

	@Test
	public void testGetFirstStyleRuleNoRule() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		CSSParser parser = new CSSParser();
		SelectorList selist = parser.parseSelectors("foo");
		StyleRule rule = sheet.getFirstStyleRule(selist);
		assertNull(rule);
	}

	@Test
	public void testGetStyleRulesSelector() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		CSSParser parser = new CSSParser();
		Selector sele = parser.parseSelectors("h5").item(0);
		CSSRuleArrayList list = sheet.getStyleRules(sele);
		assertNotNull(list);
		assertEquals(6, list.getLength());
		StyleRule rule = (StyleRule) list.item(1);
		assertEquals("h5", rule.getSelectorText());
	}

	@Test
	public void testGetStyleRulesSelectorNoSelector() {
		BaseCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		CSSParser parser = new CSSParser();
		Selector sele = parser.parseSelectors("foo").item(0);
		CSSRuleArrayList list = sheet.getStyleRules(sele);
		assertNull(list);
	}

	@Test
	public void testParseCSSStyleSheetUnexpectedTokensRecovery() throws IOException {
		String css = "div{color:red;color{;color:maroon};color:green}";
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		Reader re = new StringReader(css);
		sheet.parseStyleSheet(re);

		CSSRuleArrayList rules = sheet.getCssRules();
		assertEquals(1, rules.getLength());
		StyleRule rule = (StyleRule) rules.item(0);

		assertEquals(1, rule.getStyle().getLength());
		assertEquals("green", rule.getStyle().getPropertyValue("color"));

		assertTrue(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseCSSStyleSheetInvalidValue() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "li.foo{color:rgb(12, 255); display: block;}";
		Reader re = new StringReader(csstext);
		css.parseStyleSheet(re);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(1, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings()); // Accounts for SAC errors
		assertEquals("li.foo {\n    display: block;\n}\n", css.toString());
	}

	@Test
	public void testParseCSSStyleSheetInvalidValue2() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "li.foo{margin-left:calc(foo); display: block;}";
		Reader re = new StringReader(csstext);
		css.parseStyleSheet(re);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(1, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings()); // Accounts for SAC errors
		assertEquals("li.foo {\n    display: block;\n}\n", css.toString());
	}

	@Test
	public void testParseCSSStyleSheetInvalidRatioValue() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "li.foo{aspect-ratio:1.68/; display: block;}";
		Reader re = new StringReader(csstext);
		css.parseStyleSheet(re);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(1, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings()); // Accounts for SAC errors
		assertEquals("li.foo {\n    display: block;\n}\n", css.toString());
	}

	@Test
	public void testParseCSSStyleSheetInvalidRule() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = ".foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}";
		Reader re = new StringReader(csstext);
		css.parseStyleSheet(re);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(2, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings());
		assertEquals(".foo{margin-left:0;margin-right:auto}", css.toMinifiedString());
	}

	@Test
	public void testParseCSSStyleSheetInvalidRule2() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@media screen and (min-width: 768px){.foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}}";
		Reader re = new StringReader(csstext);
		css.parseStyleSheet(re);
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.MEDIA_RULE, rules.item(0).getType());
		MediaRule mediarule = (MediaRule) rules.item(0);
		rules = mediarule.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rules.item(0).getType());
		StyleRule stylerule = (StyleRule) rules.item(0);
		AbstractCSSStyleDeclaration style = stylerule.getStyle();
		assertEquals(2, style.getLength());
		assertTrue(css.getErrorHandler().hasSacErrors());
		assertTrue(css.hasRuleErrorsOrWarnings());
		assertEquals("@media screen and (min-width:768px){.foo{margin-left:0;margin-right:auto}}",
				css.toMinifiedString());
	}

	@Test
	public void testParseCSSStyleSheetSupportsRule() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@supports(display:list-item) and (width:max-content){li.foo{width:max-content}}";
		Reader re = new StringReader(csstext);
		assertTrue(css.parseStyleSheet(re));
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, rules.item(0).getType());
		SupportsRule rule = (SupportsRule) rules.item(0);
		assertEquals("(display: list-item) and (width: max-content)", rule.getConditionText());
		BooleanCondition cond = rule.getCondition();
		assertNotNull(cond);
		assertEquals(BooleanCondition.Type.AND, cond.getType());
		assertEquals("(display: list-item) and (width: max-content)", cond.toString());
		assertFalse(css.getErrorHandler().hasSacErrors());
		assertFalse(css.getErrorHandler().hasOMErrors());
		assertEquals(csstext.replace(" ", ""), css.toString().replace('\n', ' ').replace(" ", "").replace(";}", "}"));
		// Visitor
		StyleCountVisitor visitor = new StyleCountVisitor();
		css.acceptStyleRuleVisitor(visitor);
		assertEquals(1, visitor.getCount());
		//
		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		css.acceptDeclarationRuleVisitor(visitorP);
		assertEquals(1, visitorP.getCount());
		//
		visitorP.reset();
		css.acceptDescriptorRuleVisitor(visitorP);
		assertEquals(0, visitorP.getCount());
	}

	@Test
	public void testParseCSSStyleSheetSupportsRuleBadConditionFix() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@supports(display:list-item)and(width:max-content){li.foo{width:max-content}}";
		Reader re = new StringReader(csstext);
		assertTrue(css.parseStyleSheet(re));
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.SUPPORTS_RULE, rules.item(0).getType());
		assertFalse(css.getErrorHandler().hasSacErrors());
		assertFalse(css.getErrorHandler().hasOMErrors());
		assertEquals(csstext, css.toString().replace('\n', ' ').replace(" ", "").replace(";}", "}"));
	}

	@Test
	public void testParseCSSStyleSheetIEMediaRule() throws CSSException, IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(EnumSet.of(Parser.Flag.IEVALUES));
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		String csstext = "@media screen\\0 {li.foo {width: max-content}}";
		Reader re = new StringReader(csstext);
		assertTrue(css.parseStyleSheet(re));
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(1, rules.getLength());
		AbstractCSSRule rule = rules.item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mediarule = (MediaRule) rule;
		MediaQueryList mql = mediarule.getMedia();
		assertEquals("screen\\0", mql.getMedia());
		assertFalse(css.getErrorHandler().hasSacErrors());
		assertEquals(csstext, rule.getCssText().replace('\n', ' ').replace("; }", "}"));
	}

	@Test
	public void testToString() throws IOException {
		AbstractCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadSampleSheet();
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleCSSReader();
		String file = readAndCloseFile(re, 600);
		String expected = file.replace('\r', ' ').replace('\n', ' ').replace(" ", "").replace(";}", "}");
		assertEquals(expected, sheet.toString().replace('\n', ' ').replace(" ", "").replace(";}", "}"));
	}

	private String readAndCloseFile(Reader re, int bufCapacity) throws IOException {
		CharBuffer target = CharBuffer.allocate(bufCapacity);
		int n = re.read(target);
		assertTrue(n != -1 && n < bufCapacity);
		re.close();
		((Buffer) target).flip(); // XXX: The cast can be removed if run with Java 9+
		return target.toString();
	}

	@Test
	public void testToString2() throws IOException {
		DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		AbstractCSSStyleSheet css = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"/** Comment 1 **/\n@media print {/** Comment 2 **/\n@page {margin-top: 20%;}h3 {width: 80%}/** Comment 3 **/}\n");
		assertTrue(css.parseStyleSheet(re));
		assertEquals(
				"/** Comment 1 **/\n@media print {\n    /** Comment 2 **/\n    @page {\n        margin-top: 20%;\n    }\n    h3 {\n        width: 80%;\n    } /** Comment 3 **/\n}\n",
				css.toString());
	}

	@Test
	public void testToStyleString() throws IOException {
		AbstractCSSStyleSheet sheet = DOMCSSStyleSheetFactoryTest.loadSampleSheet();
		sheet.setMedia(new MediaQueryListImpl("screen"));
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleCSSReader();
		CharBuffer target = CharBuffer.allocate(620);
		target.append("<styletype=\"text/css\"media=\"screen\">");
		assertTrue(re.read(target) != -1);
		re.close();
		target.append("</style >");
		((Buffer) target).flip(); // XXX: The cast can be removed if run with Java 9+
		String expected = target.toString().replace('\r', ' ').replace('\n', ' ').replace(" ", "").replace(";}", "}");
		assertEquals(expected, sheet.toStyleString().replace('\n', ' ').replace(" ", "").replace(";}", "}"));
	}

	private static Reader loadFilefromClasspath(String filename) {
		final String path = "/io/sf/carte/doc/style/css/" + filename;
		InputStream is = TestDOMImplementation.class.getResourceAsStream(path);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

}
