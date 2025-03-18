/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.MediaQueryFactory;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;

public class MediaRuleTest {

	private static MediaQueryFactory mediaFactory;

	private AbstractCSSStyleSheet sheet;

	@BeforeAll
	public static void setUpBeforeClass() {
		mediaFactory = new CSSValueMediaQueryFactory();
	}

	@BeforeEach
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new DefaultStyleFormattingFactory());
		sheet = factory.createStyleSheet(null, null);
	}

	@Test
	public void testInsertRuleStringInt() {
		MediaQueryList mediaList = createMediaList("screen,print");
		MediaRule rule = sheet.createMediaRule(mediaList);
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(0, rule.insertRule("p {border-top: 1px dashed yellow; }", 0));
		assertEquals(1, rule.insertRule("span.reddish {color: red; }", 1));
		CSSRuleList rules = rule.getCssRules();
		assertEquals(2, rules.getLength());
		assertTrue(rule == rule.getCssRules().item(0).getParentRule());
	}

	@Test
	public void testGetCssText() {
		MediaQueryList mediaList = createMediaList("screen,print");
		MediaRule rule = sheet.createMediaRule(mediaList);
		assertTrue(sheet == rule.getParentStyleSheet());
		rule.insertRule("p {border-top: 1px dashed yellow; }", 0);
		rule.insertRule("span.reddish {color: red; }", 1);
		assertEquals(
				"@media screen,print {\n    p {\n        border-top: 1px dashed yellow;\n    }\n    span.reddish {\n        color: red;\n    }\n}\n",
				rule.getCssText());
		assertEquals("@media screen,print{p{border-top:1px dashed yellow;}span.reddish{color:red}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testGetCssText2() {
		MediaQueryList mediaList = createMediaList("screen,print");
		MediaRule rule = new MediaRule(sheet, mediaList, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		assertEquals("@media screen,print{}", rule.getMinifiedCssText());
		assertEquals("@media screen,print {\n}\n", rule.getCssText());
	}

	@Test
	public void testParse() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media only screen and (min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px;}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("only screen and (min-width: 37.002em)", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(
				"@media only screen and (min-width: 37.002em) {\n    nav.foo {\n        display: none;\n    }\n    footer .footer .foo {\n        padding-left: 0;\n        padding-right: 0;\n    }\n    h4 {\n        font-size: 20px;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@media only screen and (min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px}}",
				rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		// Visitor
		StyleCountVisitor visitor = new StyleCountVisitor();
		sheet.acceptStyleRuleVisitor(visitor);
		assertEquals(3, visitor.getCount());

		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitorP);
		assertEquals(4, visitorP.getCount());

		visitorP.reset();
		sheet.acceptDescriptorRuleVisitor(visitorP);
		assertEquals(0, visitorP.getCount());
	}

	@Test
	public void testParse2() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media screen and (-webkit-min-device-pixel-ratio:0){@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}nav.foo{display:none}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("screen and (-webkit-min-device-pixel-ratio: 0)", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(
				"@media screen and (-webkit-min-device-pixel-ratio:0){@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}nav.foo{display:none}}",
				rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		// Visitor
		StyleCountVisitor visitor = new StyleCountVisitor();
		sheet.acceptStyleRuleVisitor(visitor);
		assertEquals(1, visitor.getCount());

		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		sheet.acceptDeclarationRuleVisitor(visitorP);
		assertEquals(3, visitorP.getCount());

		visitorP.reset();
		sheet.acceptDescriptorRuleVisitor(visitorP);
		assertEquals(2, visitorP.getCount());
	}

	@Test
	public void testParseAllMedia() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media {nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px;}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);

		MediaQueryList mql = rule.getMedia();
		assertFalse(mql.isNotAllMedia());
		assertTrue(mql.isAllMedia());
		assertFalse(mql.hasErrors());
		assertEquals("all", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(
				"@media {\n    nav.foo {\n        display: none;\n    }\n    footer .footer .foo {\n        padding-left: 0;\n        padding-right: 0;\n    }\n    h4 {\n        font-size: 20px;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@media{nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px}}",
				rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseCompat() throws DOMException, IOException, ParserConfigurationException {
		CSSElement cssStyle = styleElement(
				"@media only screen and (min-width:0\\0){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px;}}",
				EnumSet.of(Parser.Flag.IEVALUES));
		AbstractCSSStyleSheet compatsheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, compatsheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, compatsheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) compatsheet.getCssRules().item(0);
		assertEquals("only screen and (min-width: 0\\0)", rule.getMedia().getMedia());
		assertTrue(rule.getMedia().isNotAllMedia());
		assertTrue(compatsheet == rule.getParentStyleSheet());
		assertEquals(
				"@media only screen and (min-width: 0\\0) {nav.foo {display: none; }footer .footer .foo {padding-left: 0; padding-right: 0; }h4 {font-size: 20px; }}",
				rule.getCssText());
		assertEquals(
				"@media only screen and (min-width:0\\0){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px}}",
				rule.getMinifiedCssText());

		MediaQueryList mql = rule.getMedia();
		assertTrue(mql.isNotAllMedia());
		assertFalse(mql.isAllMedia());

		DefaultSheetErrorHandler errHandler = (DefaultSheetErrorHandler) compatsheet.getErrorHandler();
		assertFalse(errHandler.hasSacErrors());
		assertTrue(errHandler.hasSacWarnings());
		assertFalse(errHandler.hasOMErrors());
		assertFalse(errHandler.hasOMWarnings());

		List<CSSParseException> warns = errHandler.getSacWarnings();
		Iterator<CSSParseException> it = warns.iterator();
		CSSParseException pex = it.next();
		assertTrue(pex.getMessage().contains("compat ident"));
		assertEquals(1, pex.getLineNumber());
		assertEquals(38, pex.getColumnNumber());

		CSSDocument cssdoc = cssStyle.getOwnerDocument();
		assertFalse(cssdoc.getErrorHandler().hasMediaErrors());
		assertFalse(cssdoc.getErrorHandler().hasMediaWarnings());
	}

	@Test
	public void testParseBad() throws DOMException, ParserConfigurationException {
		CSSElement cssStyle = styleElement("@media (max-width:1600px) and only screen {div.foo{margin:1em}}");
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mrule = (MediaRule) rule;

		MediaQueryList mql = mrule.getMedia();
		assertTrue(mql.isNotAllMedia());
		assertFalse(mql.isAllMedia());
		assertTrue(mql.hasErrors());

		assertEquals(1, mrule.getCssRules().getLength());
		DefaultSheetErrorHandler errHandler = (DefaultSheetErrorHandler) sheet.getErrorHandler();
		assertTrue(errHandler.hasSacErrors());
		assertFalse(errHandler.hasOMErrors());

		List<CSSParseException> warns = errHandler.getSacErrors();
		Iterator<CSSParseException> it = warns.iterator();
		CSSParseException pex = it.next();
		assertEquals(1, pex.getLineNumber());
		assertEquals(31, pex.getColumnNumber());

		CSSDocument cssdoc = cssStyle.getOwnerDocument();
		assertFalse(cssdoc.getErrorHandler().hasMediaErrors());
		assertFalse(cssdoc.getErrorHandler().hasMediaWarnings());
	}

	@Test
	public void testParseBadMediaFeature() throws DOMException, ParserConfigurationException {
		CSSElement cssStyle = styleElement("@media (max-width:-9_px),print {div.foo{margin:1em}}");
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mrule = (MediaRule) rule;

		MediaQueryList mql = mrule.getMedia();
		assertFalse(mql.isNotAllMedia());
		assertFalse(mql.isAllMedia());
		assertTrue(mql.hasErrors());

		assertEquals(1, mrule.getCssRules().getLength());
		DefaultSheetErrorHandler errHandler = (DefaultSheetErrorHandler) sheet.getErrorHandler();
		assertTrue(errHandler.hasSacErrors());
		assertFalse(errHandler.hasOMErrors());

		List<CSSParseException> warns = errHandler.getSacErrors();
		Iterator<CSSParseException> it = warns.iterator();
		CSSParseException pex = it.next();
		assertEquals(1, pex.getLineNumber());
		assertEquals(24, pex.getColumnNumber());

		CSSDocument cssdoc = cssStyle.getOwnerDocument();
		assertFalse(cssdoc.getErrorHandler().hasMediaErrors());
		assertFalse(cssdoc.getErrorHandler().hasMediaWarnings());
	}

	@Test
	public void testParseIgnoreBad() throws DOMException, ParserConfigurationException {
		CSSElement cssStyle = styleElement(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }@media {div.foo{margin:1em}}");
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());

		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("handheld", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals("@media handheld {}", rule.getCssText());
		assertEquals("@media handheld{}", rule.getMinifiedCssText());

		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(1).getType());
		rule = (MediaRule) sheet.getCssRules().item(1);
		assertEquals("all", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals("@media {div.foo {margin: 1em; }}", rule.getCssText());
		assertEquals("@media{div.foo{margin:1em;}}", rule.getMinifiedCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasOMErrors());

		CSSDocument cssdoc = cssStyle.getOwnerDocument();
		assertFalse(cssdoc.getErrorHandler().hasMediaErrors());
		assertFalse(cssdoc.getErrorHandler().hasMediaWarnings());
	}

	@Test
	public void testParseIgnoreBad2() throws DOMException, ParserConfigurationException {
		CSSElement cssStyle = styleElement(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }}@media {div.foo{margin:1em}}");
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());

		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("handheld", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals("@media handheld {}", rule.getCssText());
		assertEquals("@media handheld{}", rule.getMinifiedCssText());

		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(1).getType());
		rule = (MediaRule) sheet.getCssRules().item(1);
		assertEquals("all", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals("@media {div.foo {margin: 1em; }}", rule.getCssText());
		assertEquals("@media{div.foo{margin:1em;}}", rule.getMinifiedCssText());
		assertTrue(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		CSSDocument cssdoc = cssStyle.getOwnerDocument();
		assertFalse(cssdoc.getErrorHandler().hasMediaErrors());
		assertFalse(cssdoc.getErrorHandler().hasMediaWarnings());
	}

	@Test
	public void testParseNested() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("screen", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(2, rule.getCssRules().getLength());
		AbstractCSSRule rule0 = rule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule0.getType());
		assertTrue(rule == rule0.getParentRule());
		AbstractCSSRule rule1 = rule.getCssRules().item(1);
		assertEquals(CSSRule.MEDIA_RULE, rule1.getType());
		assertTrue(rule == rule1.getParentRule());
		MediaRule nestedmrule = (MediaRule) rule1;
		assertEquals("(max-width: 1600px)", nestedmrule.getMedia().getMedia());
		assertEquals("(max-width:1600px)", nestedmrule.getMedia().getMinifiedMedia());
		assertEquals(
				"@media screen {\n    .foo {\n        bottom: 20px ! important;\n    }\n    @media (max-width: 1600px) {\n        div.foo {\n            margin: 1em;\n        }\n    }\n}\n",
				rule.getCssText());
		assertEquals("@media screen{.foo{bottom:20px!important}@media (max-width:1600px){div.foo{margin:1em;}}}",
				rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testParseNestedEOF() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em");
		sheet.parseStyleSheet(re);
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals("screen", rule.getMedia().getMedia());
		assertTrue(sheet == rule.getParentStyleSheet());
		assertEquals(2, rule.getCssRules().getLength());
		AbstractCSSRule rule0 = rule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule0.getType());
		assertTrue(rule == rule0.getParentRule());
		AbstractCSSRule rule1 = rule.getCssRules().item(1);
		assertEquals(CSSRule.MEDIA_RULE, rule1.getType());
		assertTrue(rule == rule1.getParentRule());
		MediaRule nestedmrule = (MediaRule) rule1;
		assertEquals("(max-width: 1600px)", nestedmrule.getMedia().getMedia());
		assertEquals("(max-width:1600px)", nestedmrule.getMedia().getMinifiedMedia());
		assertEquals("@media screen{.foo{bottom:20px!important}@media (max-width:1600px){div.foo{margin:1em;}}}",
				rule.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
	}

	@Test
	public void testSetCssText() throws DOMException {
		MediaQueryList mediaList = createMediaList("screen,print");
		MediaRule rule = new MediaRule(sheet, mediaList, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		String text = "@media only screen and (min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px;}}";
		rule.setCssText(text);
		assertEquals(3, rule.getCssRules().getLength());
		assertEquals("only screen and (min-width: 37.002em)", rule.getMedia().getMedia());
		assertEquals(
				"@media only screen and (min-width: 37.002em) {\n    nav.foo {\n        display: none;\n    }\n    footer .footer .foo {\n        padding-left: 0;\n        padding-right: 0;\n    }\n    h4 {\n        font-size: 20px;\n    }\n}\n",
				rule.getCssText());
		assertEquals(
				"@media only screen and (min-width:37.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:0}h4{font-size:20px}}",
				rule.getMinifiedCssText());
	}

	@Test
	public void testSetCssTextBadRule() throws DOMException {
		MediaQueryList mediaList = createMediaList("screen,print");
		MediaRule rule = new MediaRule(sheet, mediaList, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		try {
			rule.setCssText(
					"@supports(display:table-cell)and(display:list-item){td{display:table-cell}@page{margin-top:20%}li{display:list-item}}");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		assertEquals(0, rule.getCssRules().getLength());
		assertEquals("screen,print", rule.getMedia().getMedia());
		assertEquals("@media screen,print {\n}\n", rule.getCssText());
		assertEquals("@media screen,print{}", rule.getMinifiedCssText());
	}

	@Test
	public void testEquals() throws DOMException, IOException {
		StringReader re = new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}"
						+ "@media print  {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}");
		sheet.parseStyleSheet(re);
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(0).getType());
		MediaRule rule = (MediaRule) sheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, sheet.getCssRules().item(1).getType());
		MediaRule rule2 = (MediaRule) sheet.getCssRules().item(1);
		assertFalse(rule.equals(rule2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		MediaQueryList mediaList = createMediaList("screen,print");
		MediaRule rule = sheet.createMediaRule(mediaList);
		rule.insertRule("p {border-top: 1px dashed yellow; }", 0);
		rule.insertRule("span.reddish {color: red; }", 1);
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null, null);
		MediaRule cloned = rule.clone(newSheet);
		assertFalse(rule == cloned);
		int nrules = rule.getCssRules().getLength();
		assertEquals(nrules, cloned.getCssRules().getLength());
		for (int i = 0; i < nrules; i++) {
			CSSRule r = rule.getCssRules().item(i);
			CSSRule c = cloned.getCssRules().item(i);
			assertTrue(r.getParentStyleSheet() != c.getParentStyleSheet());
			assertTrue(newSheet == c.getParentStyleSheet());
			assertTrue(rule == r.getParentRule());
			assertTrue(cloned == c.getParentRule());
			if (c instanceof CSSDeclarationRule) {
				assertTrue(c == ((CSSDeclarationRule) c).getStyle().getParentRule());
			}
			assertEquals(r.getCssText(), c.getCssText());
		}
		assertEquals(
				"@media screen,print {\n    p {\n        border-top: 1px dashed yellow;\n    }\n    span.reddish {\n        color: red;\n    }\n}\n",
				cloned.getCssText());
		assertTrue(rule.equals(cloned));
		assertEquals(rule.hashCode(), cloned.hashCode());
	}

	private MediaQueryList createMediaList(String media) {
		MediaQueryList mql = mediaFactory.createAllMedia();
		mql.appendMedium(media);
		return mql;
	}

	private static CSSElement styleElement(String sheetText) throws DOMException, ParserConfigurationException {
		return styleElement(sheetText, EnumSet.noneOf(Parser.Flag.class));
	}

	private static CSSElement styleElement(String sheetText, EnumSet<Parser.Flag> flags)
			throws DOMException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setAttribute("media", "screen");
		style.setTextContent(sheetText);
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(flags);
		CSSDocument cssdoc = factory.createCSSDocument(doc);
		return cssdoc.getElementById("styleId");
	}

}
