/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRectValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.Diff;

public class BaseCSSStyleDeclarationTest {

	private static AbstractCSSStyleSheet sheet;

	BaseCSSStyleDeclaration emptyStyleDecl;

	@BeforeAll
	public static void setUpBeforeAll() {
		sheet = new DOMCSSStyleSheetFactory().createStyleSheet(null, null);
	}

	@BeforeEach
	public void setUp() {
		StyleRule styleRule = sheet.createStyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testSetCssTextEmpty() {
		emptyStyleDecl.setCssText("");
		assertEquals(0, emptyStyleDecl.getLength());
		assertEquals("", emptyStyleDecl.getCssText());
	}

	@Test
	public void testSetCssTextEmpty2() {
		emptyStyleDecl.setCssText("display: block; ");
		emptyStyleDecl.setCssText("");
		assertEquals(0, emptyStyleDecl.getLength());
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getPropertyValue("display"));
	}

	@Test
	public void testSetCssTextEmpty3() {
		emptyStyleDecl.setCssText("flex: unset unset;display: block");
		assertEquals("display: block;\n", emptyStyleDecl.getCssText());
		assertEquals("block", emptyStyleDecl.getPropertyValue("display"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("");
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals(0, emptyStyleDecl.getLength());
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getPropertyValue("display"));
	}

	@Test
	public void getCssTextForRem() {
		emptyStyleDecl.setCssText("line-height: 1.2rem; ");
		assertEquals("1.2rem", emptyStyleDecl.getPropertyValue("line-height"));
		NumberValue val = (NumberValue) emptyStyleDecl.getPropertyCSSValue("line-height");
		assertEquals("rem", val.getDimensionUnitText());
		assertEquals("line-height: 1.2rem;\n", emptyStyleDecl.getCssText());
		assertEquals(1.2f, val.getFloatValue(CSSUnit.CSS_REM), 1e-9);
		assertEquals(1.2f, val.getFloatValue(CSSUnit.CSS_OTHER), 1e-9);
	}

	@Test
	public void setCssTextContent() {
		emptyStyleDecl.setCssText("content: 'some content here'");
		assertEquals("'some content here'",
				emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("some content here", emptyStyleDecl.getPropertyValue("content"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("content"));
		emptyStyleDecl.setCssText("content: url('http://www.example.com/content')");
		assertEquals("url('http://www.example.com/content')",
				emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("http://www.example.com/content",
				((CSSTypedValue) emptyStyleDecl.getPropertyCSSValue("content")).getStringValue());
		emptyStyleDecl.setCssText("content: 'line\\A another line\\A final line'");
		assertEquals("'line\\A another line\\A final line'",
				emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("line\nanother line\nfinal line",
				((CSSTypedValue) emptyStyleDecl.getPropertyCSSValue("content")).getStringValue());
	}

	@Test
	public void setCssTextContentEscaped() {
		emptyStyleDecl.setCssText("content: '\\\\'");
		assertEquals("'\\\\'", emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("\\", emptyStyleDecl.getPropertyValue("content"));
	}

	@Test
	public void setCssTextContentEscaped2() {
		emptyStyleDecl.setCssText("content: \"â†\u0090\";");
		assertEquals("\"â†\\90 \"", emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("â†\u0090", emptyStyleDecl.getPropertyValue("content"));
		assertEquals("content: \"â†\\90 \";\n", emptyStyleDecl.getCssText());
	}

	@Test
	public void setCssTextContentEscapedBad() {
		emptyStyleDecl.setCssText("content: '\\'");
		assertNull(emptyStyleDecl.getPropertyCSSValue("content"));
		assertEquals("", emptyStyleDecl.getPropertyValue("content"));
	}

	@Test
	public void setCssTextContent2() {
		emptyStyleDecl.setCssText("content:attr(data-votes) \" votes\";");
		assertEquals("attr(data-votes) \" votes\"",
				emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("attr(data-votes) \" votes\"", emptyStyleDecl.getPropertyValue("content"));
	}

	@Test
	public void setCssTextEscaped() {
		emptyStyleDecl
				.setCssText("font-family: \\5FAE\\8F6F\\96C5\\9ED1,Arial,\\5b8b\\4f53,sans-serif");
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals("\\5fae\\8f6f\\96c5\\9ed1 , Arial, \\5b8b\\4f53 , sans-serif",
				value.getCssText());
		assertEquals("微软雅黑,Arial,宋体,sans-serif", value.getMinifiedCssText("font-family"));
		assertEquals("\\5fae\\8f6f\\96c5\\9ed1 , Arial, \\5b8b\\4f53 , sans-serif",
				emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped2() {
		emptyStyleDecl.setCssText(
				"font-family: \\5FAE\\8F6F\\96C5\\9ED1,\"Times New Roman\",\\5b8b\\4f53");
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals("\\5fae\\8f6f\\96c5\\9ed1 , \"Times New Roman\", \\5b8b\\4f53 ",
				value.getCssText());
		assertEquals("微软雅黑,\"Times New Roman\",宋体", value.getMinifiedCssText("font-family"));
		assertEquals("\\5fae\\8f6f\\96c5\\9ed1 , \"Times New Roman\", \\5b8b\\4f53 ",
				emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped3() {
		emptyStyleDecl.setCssText("font-family: \\\\5FAE\\8F6F");
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals("\\\\5FAE\\8f6f ", value.getCssText());
		assertEquals("\\\\5FAE\u8F6F", value.getMinifiedCssText("font-family"));
		assertEquals("\\5FAE软", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped4() {
		emptyStyleDecl.setCssText("font-family:\\5FAE 软");
		assertEquals("微软", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped5() {
		emptyStyleDecl.setCssText("font-family:微软雅黑");
		assertEquals("微软雅黑", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped6() {
		emptyStyleDecl.setCssText("font-family: \"\u5b8b\u4f53\",Arial");
		assertEquals("\"\u5b8b\u4f53\", Arial", emptyStyleDecl.getPropertyValue("font-family"));
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals(CssType.LIST, value.getCssValueType());
		value = ((ValueList) value).item(0);
		assertEquals(CssType.TYPED, value.getCssValueType());
		CSSTypedValue primi = (CSSTypedValue) value;
		assertEquals(CSSValue.Type.STRING, primi.getPrimitiveType());
		assertEquals("\u5b8b\u4f53", primi.getStringValue());
		emptyStyleDecl.setCssText(emptyStyleDecl.getCssText());
		assertEquals("\"\u5b8b\u4f53\", Arial", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped7() {
		emptyStyleDecl.setCssText("font: 18pt \"\u5b8b\u4f53\",Arial");
		assertEquals("\"\u5b8b\u4f53\", Arial", emptyStyleDecl.getPropertyValue("font-family"));
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals(CssType.LIST, value.getCssValueType());
		value = ((ValueList) value).item(0);
		assertEquals(CssType.TYPED, value.getCssValueType());
		CSSTypedValue primi = (CSSTypedValue) value;
		assertEquals(CSSValue.Type.STRING, primi.getPrimitiveType());
		assertEquals("\u5b8b\u4f53", primi.getStringValue());
		emptyStyleDecl.setCssText(emptyStyleDecl.getCssText());
		assertEquals("\"\u5b8b\u4f53\", Arial", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("18pt \"\u5b8b\u4f53\", Arial", emptyStyleDecl.getPropertyValue("font"));
	}

	@Test
	public void setCssTextEscaped8() {
		emptyStyleDecl.setCssText("font-family: \\\u5b8b\u4f53");
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals(CssType.TYPED, value.getCssValueType());
		CSSTypedValue primi = (CSSTypedValue) value;
		assertEquals(CSSValue.Type.IDENT, primi.getPrimitiveType());
		assertEquals("\u5b8b\u4f53", primi.getStringValue());
		assertEquals("\u5b8b\u4f53", emptyStyleDecl.getPropertyValue("font-family"));
		emptyStyleDecl.setCssText(emptyStyleDecl.getCssText());
		assertEquals("\u5b8b\u4f53", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscaped9() {
		emptyStyleDecl.setCssText("font-family:file\\:\\/\\/\\/dir\\/file");
		assertEquals("file:///dir/file", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextEscapedPropertyName() {
		emptyStyleDecl.setCssText("color\0:#ff0");
		assertEquals("", emptyStyleDecl.getCssText());
	}

	@Test
	public void setCssTextPropertyNameBad() {
		emptyStyleDecl.setCssText("color\u0000:#ff0");
		assertEquals("", emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText("color \u0000:#ff0");
		assertEquals("", emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText("color color:#ff0");
		assertEquals("", emptyStyleDecl.getCssText());
	}

	@Test
	public void setCssTextFont() {
		emptyStyleDecl.setCssText("font-size: 16px;font-size: 12pt");
		assertEquals("12pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("font-size: 12pt;\n", emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText("font-family: \"Times New Roman\"");
		assertEquals("\"Times New Roman\"",
				emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertEquals("Times New Roman", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("\"Times New Roman\"",
				emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		emptyStyleDecl.setCssText("font-family: Verdana, Chicago");
		assertEquals("Verdana, Chicago", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("Verdana, Chicago",
				emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		emptyStyleDecl.setCssText("font-family: \"Comic Sans\", Chicago");
		assertEquals("\"Comic Sans\", Chicago", emptyStyleDecl.getPropertyValue("font-family"));
		emptyStyleDecl.setCssText("font-family: \"Comic Sans\",               Chicago");
		assertEquals("\"Comic Sans\", Chicago", emptyStyleDecl.getPropertyValue("font-family"));
	}

	@Test
	public void setCssTextFilter() {
		String cssText = "filter:progid:DXImageTransform.Microsoft.Blur(pixelradius=5);";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(0, emptyStyleDecl.getLength());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)",
				style.getPropertyCSSValue("filter").getCssText());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)",
				style.getPropertyValue("filter"));
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextFilterIdentifier() {
		emptyStyleDecl.setCssText(
				"filter:progid\\:DXImageTransform\\.Microsoft\\.Blur\\(pixelradius\\=5\\);");
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.Blur\\(pixelradius\\=5\\)",
				emptyStyleDecl.getPropertyCSSValue("filter").getCssText());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)",
				emptyStyleDecl.getPropertyValue("filter"));
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextFilter3() {
		String cssText = "filter:progid:DXImageTransform.Microsoft.gradient(startColorStr='#f5f5f5',EndColorStr='#f1f1f1');";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(0, emptyStyleDecl.getLength());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals(
				"progid:DXImageTransform.Microsoft.gradient(startColorStr= '#f5f5f5', EndColorStr= '#f1f1f1')",
				style.getPropertyCSSValue("filter").getCssText());
		assertEquals(
				"progid:DXImageTransform.Microsoft.gradient(startColorStr= '#f5f5f5', EndColorStr= '#f1f1f1')",
				style.getPropertyValue("filter"));
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioChar() {
		String cssText = "margin:10px;margin:8px!important!;width:590px;width:600px!important!;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin"));
		assertEquals("590px", emptyStyleDecl.getPropertyValue("width"));
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIOCHAR);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals(
				"margin: 10px; margin: 8px!important!; width: 590px; width: 600px!important!; ",
				style.getCssText());
		assertEquals("margin:10px;margin:8px!important!;width:590px;width:600px!important!",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioChar2() {
		String cssText = "margin:8px!important!;margin:10px;width:600px!important!;width:590px;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIOCHAR);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals(
				"margin: 8px!important!; margin: 10px; width: 600px!important!; width: 590px; ",
				style.getCssText());
		assertEquals("margin:8px!important!;margin:10px;width:600px!important!;width:590px",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioChar3() {
		String cssText = "margin:10px 5px;margin:8px 4px!important!;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("margin: 10px 5px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 5px;", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIOCHAR);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px 5px; margin: 8px 4px!important!; ", style.getCssText());
		assertEquals("margin:10px 5px;margin:8px 4px!important!;", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioCharImportant() {
		String cssText = "margin:10px!important;margin:8px!important!;width:590px!important;width:600px!important!;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px ! important;\nwidth: 590px ! important;\n",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important",
				emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIOCHAR);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals(
				"margin: 10px ! important; margin: 8px!important!; width: 590px ! important; width: 600px!important!; ",
				style.getCssText());
		assertEquals(
				"margin:10px!important;margin:8px!important!;width:590px!important;width:600px!important!",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioCharImportant2() {
		String cssText = "margin:8px!important!;margin:10px!important;width:600px!important!;width:590px!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px ! important;\nwidth: 590px ! important;\n",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important",
				emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIOCHAR);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px ! important; width: 590px ! important; ", style.getCssText());
		assertEquals("margin:10px!important;width:590px!important", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio() {
		String cssText = "margin:10px;margin:10px!ie;width:590px;width:600px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px; margin: 10px!ie; width: 590px; width: 600px!ie; ",
				style.getCssText());
		assertEquals("margin:10px;margin:10px!ie;width:590px;width:600px!ie",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio2() {
		String cssText = "margin:10px!ie;margin:10px;width:590px;width:600px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin"));
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px; width: 590px; width: 600px!ie; ", style.getCssText());
		assertEquals("margin:10px;width:590px;width:600px!ie", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio3() {
		String cssText = "margin:10px 5px;margin:8px 4px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("margin: 10px 5px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 5px;", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals("10px 5px", emptyStyleDecl.getPropertyValue("margin"));
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px 5px; margin: 8px 4px!ie; ", style.getCssText());
		assertEquals("margin:10px 5px;margin:8px 4px!ie;", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio4() {
		String cssText = "display:inline-block;display:inline!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("display: inline-block;\n", emptyStyleDecl.getCssText());
		assertEquals("display:inline-block", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("display: inline-block; display: inline!ie; ", style.getCssText());
		assertEquals("display:inline-block;display:inline!ie", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioImportant() {
		String cssText = "margin:10px!important;margin:10px!ie;width:590px!important;width:600px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px ! important;\nwidth: 590px ! important;\n",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important",
				emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px ! important; width: 590px ! important; ", style.getCssText());
		assertEquals("margin:10px!important;width:590px!important", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompat() {
		String cssText = "margin:10px;margin:10px\\9;width:590px;width:600px\\9;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px; margin: 10px\\9; width: 590px; width: 600px\\9; ",
				style.getCssText());
		assertEquals("margin:10px;margin:10px\\9;width:590px;width:600px\\9",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompat2() {
		String cssText = "margin:10px\\9;margin:10px;width:590px;width:600px\\9;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px; width: 590px; width: 600px\\9; ", style.getCssText());
		assertEquals("margin:10px;width:590px;width:600px\\9", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompat3() {
		String cssText = "margin:10px 5px;margin:8px 4px\\9;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("margin: 10px 5px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 5px;", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals("10px 5px", emptyStyleDecl.getPropertyValue("margin"));
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("margin: 10px 5px; margin: 8px 4px\\9; ", style.getCssText());
		assertEquals("margin:10px 5px;margin:8px 4px\\9;", style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatImportant() {
		String cssText = "margin:10px;margin:10px\\9!important;width:590px;width:600px\\9!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px;\nwidth: 590px;\n", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin"));
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals(
				"margin: 10px; margin: 10px\\9 ! important; width: 590px; width: 600px\\9 ! important; ",
				style.getCssText());
		assertEquals("margin:10px;margin:10px\\9!important;width:590px;width:600px\\9!important",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatImportant2() {
		String cssText = "margin:10px!important;margin:10px\\9;width:590px!important;width:600px\\9!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px ! important;\nwidth: 590px ! important;\n",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important",
				emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals(
				"margin: 10px ! important; width: 590px ! important; width: 600px\\9 ! important; ",
				style.getCssText());
		assertEquals("margin:10px!important;width:590px!important;width:600px\\9!important",
				style.getMinifiedCssText());
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatBorderRadius() {
		String cssText = "border-radius:3px;border-radius:0\\9";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("border-radius: 3px;\n", emptyStyleDecl.getCssText());
		assertEquals("border-radius:3px;", emptyStyleDecl.getMinifiedCssText());
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-radius"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("border-radius: 3px; border-radius: 0\\9; ", style.getCssText());
		assertEquals("border-radius:3px;border-radius:0\\9;", style.getMinifiedCssText());
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-radius"));
		assertFalse(rule.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(rule.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextWidth() {
		String cssText = "width:590px;width:600px;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("width: 600px;\n", emptyStyleDecl.getCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("width: 600px; ", style.getCssText());
		assertEquals("width:600px", style.getMinifiedCssText());
	}

	@Test
	public void setCssTextWidthImportant() {
		String cssText = "width:590px;width:600px!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("width: 600px ! important;\n", emptyStyleDecl.getCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		StyleRule rule = factory.createStyleSheet(null, null).createStyleRule();
		AbstractCSSStyleDeclaration style = rule.getStyle();
		style.setCssText(cssText);
		assertEquals("width: 600px ! important; ", style.getCssText());
		assertEquals("width:600px!important", style.getMinifiedCssText());
	}

	@Test
	public void setCssTextMarginTop() {
		String cssText = "margin-top: calc((var(--foo, 0px) + var(--bar, 1px))*-1)";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("margin-top: calc((var(--foo, 0px) + var(--bar, 1px))*-1);\n",
				emptyStyleDecl.getCssText());
		assertEquals("margin-top:calc((var(--foo,0px) + var(--bar,1px))*-1)",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextEscapes() {
		BaseCSSStyleDeclaration sd = new BaseCSSStyleDeclaration();
		sd.setCssText("content: '\\A'");
		assertEquals("'\\A'", sd.getPropertyCSSValue("content").getCssText());
		assertEquals("\n", sd.getPropertyValue("content"));
		assertEquals("content: '\\A';\n", sd.getCssText());
	}

	@Test
	public void setCssTextEscapes2() {
		BaseCSSStyleDeclaration sd = new BaseCSSStyleDeclaration();
		sd.setCssText("symbols: \\1F44D");
		assertEquals("\\1f44d ", sd.getPropertyCSSValue("symbols").getCssText());
		assertEquals("\uD83D\uDC4D", sd.getPropertyValue("symbols"));
		assertEquals("symbols: \\1f44d ;\n", sd.getCssText());
	}

	@Test
	public void setCssTextFontFamily() {
		emptyStyleDecl.setCssText("font-family: Times New Roman, Verdana, Chicago");
		CSSValue value = ((ValueList) emptyStyleDecl.getPropertyCSSValue("font-family")).item(0);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("Times New Roman", ((CSSTypedValue) value).getStringValue());
		assertEquals("'Times New Roman', Verdana, Chicago",
				emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextFontFamilyUpperCase() {
		emptyStyleDecl.setCssText("FONT-FAMILY: Times New Roman, Verdana, Chicago");
		CSSValue value = ((ValueList) emptyStyleDecl.getPropertyCSSValue("font-family")).item(0);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("Times New Roman", ((CSSTypedValue) value).getStringValue());
		assertEquals("'Times New Roman', Verdana, Chicago",
				emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextFontFamilyVarUpperCase() {
		emptyStyleDecl.setCssText("font-family: var(--FOO)");
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("font-family");
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals("var(--FOO)", value.getCssText());
		assertEquals("var(--FOO)", emptyStyleDecl.getPropertyValue("font-family"));
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextBackgroundFontError() {
		emptyStyleDecl
				.setCssText("background-font:bold 14px/32px \"Courier New\", Arial, sans-serif");
		assertNull(emptyStyleDecl.getPropertyCSSValue("background-font"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextForBackgroundImage() {
		emptyStyleDecl.setCssText("background-image:linear-gradient(35deg,#fa3 50%,transparent 0)");
		assertEquals("linear-gradient(35deg, #fa3 50%, transparent 0)",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextForBackgroundPosition() {
		emptyStyleDecl
				.setCssText("background-position: 10% 20%; background-position: 50% left top");
		assertEquals("10% 20%", emptyStyleDecl.getPropertyValue("background-position"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextForBackgroundPosition2() {
		emptyStyleDecl
				.setCssText("background-position: 10% 20%; background-position: top left top left");
		assertEquals("10% 20%", emptyStyleDecl.getPropertyValue("background-position"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextForLayeredBackgroundPosition() {
		emptyStyleDecl.setCssText("background-position: 10% 20%, left top");
		assertEquals("10% 20%, left top", emptyStyleDecl.getPropertyValue("background-position"));
		emptyStyleDecl.setCssText("background-position: 10% 20%, center top 30%");
		assertEquals("10% 20%, center top 30%",
				emptyStyleDecl.getPropertyValue("background-position"));
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("background-position");
		assertNotNull(value);
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals(2, ((ValueList) value).getLength());
		assertEquals("10% 20%", ((ValueList) value).item(0).getCssText());
		assertEquals("center top 30%", ((ValueList) value).item(1).getCssText());
		emptyStyleDecl.setCssText("background-position: left, center top 30%, center, center");
		assertEquals("left, center top 30%, center, center",
				emptyStyleDecl.getPropertyValue("background-position"));
		value = emptyStyleDecl.getPropertyCSSValue("background-position");
		assertNotNull(value);
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals(4, ((ValueList) value).getLength());
		assertEquals("left", ((ValueList) value).item(0).getCssText());
		assertEquals("center top 30%", ((ValueList) value).item(1).getCssText());
		assertEquals("center", ((ValueList) value).item(2).getCssText());
		assertEquals(CssType.TYPED, ((ValueList) value).item(2).getCssValueType());
		assertEquals("center", ((ValueList) value).item(3).getCssText());
		assertEquals(CssType.TYPED, ((ValueList) value).item(3).getCssValueType());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextForLayeredBackgroundPosition2() {
		emptyStyleDecl.setCssText("background-position:0 2px,0 2px;");
		assertEquals("0 2px, 0 2px", emptyStyleDecl.getPropertyValue("background-position"));
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("background-position");
		assertNotNull(value);
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals(2, ((ValueList) value).getLength());
		assertEquals("0 2px", ((ValueList) value).item(0).getCssText());
		assertEquals("0 2px", ((ValueList) value).item(1).getCssText());
	}

	@Test
	public void setCssTextBackgroundVarUpperCase() {
		emptyStyleDecl.setCssText("background: var(--FOO)");
		assertEquals("var(--FOO)", emptyStyleDecl.getPropertyValue("background"));
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextImportant() {
		emptyStyleDecl.setCssText("font: bold !important; border: solid blue; font-size: x-large;");
		assertEquals("medium", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-size"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font"));
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font"));

		emptyStyleDecl.setCssText("font: bold; border: solid blue; font-size: x-large;");
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("x-large", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("font-weight"));
		assertEquals("", emptyStyleDecl.getPropertyValue("font"));
	}

	@Test
	public void getPropertyCSSValueSubpropertySetCssText() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-top-color: yellow; ");
		try {
			emptyStyleDecl.getPropertyCSSValue("border-left-color").setCssText("red");
			fail("Must throw a DOMException");
		} catch (DOMException e) {
		}
	}

	@Test
	public void getPropertyCSSValueForClip() {
		emptyStyleDecl.setCssText("clip: rect(5px, 20px, 25px, 5px);");
		assertEquals("rect(5px, 20px, 25px, 5px)", emptyStyleDecl.getPropertyValue("clip"));
		CSSValue value = emptyStyleDecl.getPropertyCSSValue("clip");
		assertEquals(CssType.TYPED, value.getCssValueType());
		CSSRectValue rect = (CSSRectValue) value;
		assertEquals("5px", rect.getTop().getCssText());
		assertEquals("20px", rect.getRight().getCssText());
	}

	@Test
	public void getPropertyCSSValueForCalc() {
		emptyStyleDecl.setCssText("height:calc(100vh - 230px);");
		CSSValue value = emptyStyleDecl.getPropertyCSSValue("height");
		assertNotNull(value);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.EXPRESSION, value.getPrimitiveType());
		assertEquals("calc(100vh - 230px)", emptyStyleDecl.getPropertyValue("height"));
		assertEquals("calc(100vh - 230px)", value.getCssText());
	}

	@Test
	public void getPropertyCSSValueForQuotes() {
		emptyStyleDecl.setCssText("quotes: '\"' '\"' \"'\" \"'\";");
		CSSValue value = emptyStyleDecl.getPropertyCSSValue("quotes");
		assertEquals(CssType.LIST, value.getCssValueType());
		ValueList list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertEquals("\"", ((CSSTypedValue) list.item(0)).getStringValue());
		assertEquals("\"", ((CSSTypedValue) list.item(1)).getStringValue());
		assertEquals("'", ((CSSTypedValue) list.item(2)).getStringValue());
		assertEquals("'", ((CSSTypedValue) list.item(3)).getStringValue());
		assertEquals("'\"'", list.item(0).getCssText());
		assertEquals("'\"'", list.item(1).getCssText());
		assertEquals("\"'\"", list.item(2).getCssText());
		assertEquals("\"'\"", list.item(3).getCssText());
		assertEquals("'\"' '\"' \"'\" \"'\"", value.getCssText());
		assertEquals("'\"' '\"' \"'\" \"'\"", emptyStyleDecl.getPropertyValue("quotes"));
	}

	@Test
	public void getMinifiedPropertyValueString() {
		emptyStyleDecl.setProperty("margin-inline-start", "1px", "important");
		emptyStyleDecl.setProperty("margin-inline-end", "0", "important");
		assertEquals("1px 0", emptyStyleDecl.getMinifiedPropertyValue("margin-inline"));
	}

	@Test
	public void getMinifiedPropertyValueStringShorthand() {
		emptyStyleDecl.setProperty("margin-inline", "1px 0", "important");
		assertEquals("1px 0", emptyStyleDecl.getMinifiedPropertyValue("margin-inline"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("margin-inline-start"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("margin-inline-end"));
	}

	@Test
	public void testPrefixedProperty() {
		emptyStyleDecl.setCssText(
				"-webkit-mask: url(https://www.example.com/foo.svg) no-repeat center/1.44ex .8ex");
		assertEquals("url('https://www.example.com/foo.svg') no-repeat center/1.44ex 0.8ex",
				emptyStyleDecl.getPropertyValue("-webkit-mask"));
		CSSValue value = emptyStyleDecl.getPropertyCSSValue("-webkit-mask");
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(Type.LEXICAL, value.getPrimitiveType());
		LexicalValue lexval = (LexicalValue) value;
		LexicalUnit lu = lexval.getLexicalUnit();
		assertEquals("url('https://www.example.com/foo.svg')", lu.getCssText());
		assertEquals("url('https://www.example.com/foo.svg') no-repeat center/1.44ex 0.8ex",
				lu.toString());
		assertEquals(Type.UNKNOWN, lexval.getFinalType());
	}

	@Test
	public void testGetPropertyPriority() {
		emptyStyleDecl.setCssText("pause-before:20ms;pause-after:23ms");
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause-before").length());
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause").length());
		emptyStyleDecl.setCssText("pause-before:20ms;pause-after:23ms!important");
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause-before").length());
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause-after"));
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause").length());
		emptyStyleDecl.setCssText("pause-before:20ms!important;pause-after:23ms!important");
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause-before"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause-after"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause"));
		emptyStyleDecl.setCssText("pause:23ms");
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause-before").length());
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause-after").length());
		assertEquals(0, emptyStyleDecl.getPropertyPriority("pause").length());
		emptyStyleDecl.setCssText("pause:23ms!important");
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause-before"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause-after"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("pause"));
	}

	@Test
	public void setPropertyStringStringString() {
		emptyStyleDecl.setProperty("border", "none", "important");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("border"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("border-top-style"));
	}

	@Test
	public void testDefaultPropertyValueStringPropertyDatabase() {
		assertEquals("transparent",
				emptyStyleDecl.defaultPropertyValue("background-color").getCssText());
	}

	@Test
	public void testIsPropertySetStringBoolean() {
		emptyStyleDecl.setCssText("border: 3px yellow !important; color: blue");
		assertEquals("3px yellow", emptyStyleDecl.getPropertyValue("border"));
		assertTrue(emptyStyleDecl.isPropertySet("border-top-color", true));
		assertFalse(emptyStyleDecl.isPropertySet("border-top-color", false));
		assertTrue(emptyStyleDecl.isPropertySet("color", false));
		assertFalse(emptyStyleDecl.isPropertySet("color", true));
	}

	@Test
	public void testLexicalUnitToStringLexicalUnit() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		LexicalUnit value = parser
				.parsePropertyValue(new StringReader("#0f8 blue initial #45b6a0"));
		assertEquals("#0f8 blue initial #45b6a0",
				BaseCSSStyleDeclaration.lexicalUnitToString(value));
	}

	@Test
	public void testAddStyle() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) factory
				.createStyleSheet(null, null).createStyleRule().getStyle();
		style.setCssText("margin: 8px;");
		emptyStyleDecl.setCssText("margin-top: 10px;");
		emptyStyleDecl.addStyle(style);
		assertEquals("margin: 8px;\n", emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText("margin: 8px;");
		style.setCssText(
				"margin-top: 10px; margin-right: 11px; margin-bottom: 12px; margin-left: 13px; ");
		emptyStyleDecl.addStyle(style);
		assertEquals(
				"margin-top: 10px;\nmargin-right: 11px;\nmargin-bottom: 12px;\nmargin-left: 13px;\n",
				emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText("margin: 8px ! important");
		style.setCssText(
				"margin-top: 10px; margin-right: 11px; margin-bottom: 12px; margin-left: 13px; ");
		emptyStyleDecl.addStyle(style);
		assertEquals("margin: 8px ! important;\n", emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText("border: 10%");
		style.setCssText("border: 8px white; border-color: yellow ; ");
		emptyStyleDecl.addStyle(style);
		assertEquals("border: 8px white;\nborder-color: yellow;\n", emptyStyleDecl.getCssText());
	}

	@Test
	public void testEquals() {
		emptyStyleDecl.setCssText(
				"font: bold !important; border: solid blue; line-height: normal !important");
		BaseCSSStyleDeclaration otherDecl = new BaseCSSStyleDeclaration();
		otherDecl.setCssText("font: bold !important; line-height: normal !important");
		assertFalse(emptyStyleDecl.equals(otherDecl));
		assertFalse(emptyStyleDecl.hashCode() == otherDecl.hashCode());
		otherDecl.setCssText(
				"border-top-color: blue; border-right-color: blue; border-bottom-color: blue; border-left-color: blue; border-top-style: solid; border-right-style: solid; border-bottom-style: solid; border-left-style: solid; border-top-width: medium; border-right-width: medium; border-bottom-width: medium; border-left-width: medium; border-image: none; font: bold !important; line-height: normal !important");
		assertTrue(emptyStyleDecl.equals(otherDecl));
		assertTrue(emptyStyleDecl.hashCode() == otherDecl.hashCode());
		otherDecl.setCssText("font: bold !important; border: solid blue; line-height: normal");
		assertTrue(emptyStyleDecl.equals(otherDecl));
		assertTrue(emptyStyleDecl.hashCode() == otherDecl.hashCode());
		otherDecl.setCssText("font: bold; border: solid blue; line-height: normal !important");
		assertFalse(emptyStyleDecl.equals(otherDecl));
		assertFalse(emptyStyleDecl.hashCode() == otherDecl.hashCode());
	}

	@Test
	public void testDiff() {
		emptyStyleDecl.setCssText(
				"font: smaller Arial!important; border: solid blue; line-height: normal !important");
		BaseCSSStyleDeclaration otherDecl = new BaseCSSStyleDeclaration();
		otherDecl.setCssText("font: smaller Arial!important; line-height: normal !important");
		Diff<String> diff = emptyStyleDecl.diff(otherDecl);
		assertTrue(diff.hasDifferences());
		assertNull(diff.getRightSide());
		assertNull(diff.getDifferent());
		String[] sa = diff.getLeftSide();
		assertEquals(17, sa.length);
		assertEquals("border-top-style", sa[0]);
		assertEquals("border-top-color", sa[4]);
		assertEquals("border-image-source", sa[12]);
		otherDecl.setCssText(
				"border-top-color: blue; border-right-color: blue; border-bottom-color: blue; border-left-color: blue; border-top-style: solid; border-right-style: solid; border-bottom-style: solid; border-left-style: solid; border-top-width: medium; border-right-width: medium; border-bottom-width: medium; border-left-width: medium; border-image: none; font: smaller Arial!important; line-height: normal !important");
		diff = emptyStyleDecl.diff(otherDecl);
		assertFalse(diff.hasDifferences());
		otherDecl.setCssText(
				"font: smaller Arial!important; border: solid blue; line-height: normal");
		diff = emptyStyleDecl.diff(otherDecl);
		assertFalse(diff.hasDifferences());
		otherDecl.setCssText(
				"font: smaller Arial; border: solid blue; line-height: normal !important");
		diff = emptyStyleDecl.diff(otherDecl);
		assertTrue(diff.hasDifferences());
		assertNull(diff.getLeftSide());
		assertNull(diff.getRightSide());
		sa = diff.getDifferent();
		assertEquals(16, sa.length);
		assertEquals("font-size", sa[0]);
		assertEquals("font-family", sa[1]);
		assertEquals("font-style", sa[2]);
		assertEquals("font-weight", sa[3]);
		assertEquals("font-stretch", sa[4]);
		assertEquals("font-variant-caps", sa[5]);
		assertEquals("font-size-adjust", sa[6]);
		assertEquals("font-kerning", sa[7]);
		assertEquals("font-optical-sizing", sa[8]);
		assertEquals("font-feature-settings", sa[9]);
		assertEquals("font-variation-settings", sa[10]);
		assertEquals("font-variant-ligatures", sa[11]);
		assertEquals("font-variant-position", sa[12]);
		assertEquals("font-variant-numeric", sa[13]);
		assertEquals("font-variant-alternates", sa[14]);
		assertEquals("font-variant-east-asian", sa[15]);
	}

	@Test
	public void testDiffColor() {
		emptyStyleDecl.setCssText("color: #fff");
		BaseCSSStyleDeclaration otherDecl = new BaseCSSStyleDeclaration();
		otherDecl.setCssText("color: #fff");
		Diff<String> diff = emptyStyleDecl.diff(otherDecl);
		assertFalse(diff.hasDifferences());
	}

	@Test
	public void testPrioritySplit() {
		emptyStyleDecl.setCssText(
				"font: smaller Sans!important; border: solid blue; line-height: normal !important");
		BaseCSSStyleDeclaration importantDecl = new BaseCSSStyleDeclaration();
		BaseCSSStyleDeclaration normalDecl = new BaseCSSStyleDeclaration();
		emptyStyleDecl.prioritySplit(importantDecl, normalDecl);
		assertEquals(17, importantDecl.getLength());
		assertEquals("font: smaller Sans ! important;\nline-height: normal ! important;\n",
				importantDecl.getCssText());
		assertEquals(17, normalDecl.getLength());
		assertEquals("border: solid blue;\n", normalDecl.getCssText());
	}

	@Test
	public void testClone() {
		emptyStyleDecl.setCssText(
				"font: bold!important; border: solid blue; line-height: normal !important");
		BaseCSSStyleDeclaration clone = emptyStyleDecl.clone();
		assertEquals(
				"font: bold ! important;\nborder: solid blue;\nline-height: normal ! important;\n",
				clone.getCssText());
		assertEquals(34, clone.getLength());
	}

}
