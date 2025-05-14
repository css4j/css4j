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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSRectValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

public class CompatInlineDeclarationTest {

	static DOMCSSStyleSheetFactory factory;

	BaseCSSStyleDeclaration emptyStyleDecl;
	StylableDocumentWrapper document;

	@BeforeAll
	public static void setUpBeforeClass() {
		factory = new DOMCSSStyleSheetFactory();
		factory.setStyleFormattingFactory(new TestStyleFormattingFactory());
		factory.getParserFlags().add(Parser.Flag.IEVALUES);
		factory.getParserFlags().add(Parser.Flag.IEPRIO);
		factory.getParserFlags().add(Parser.Flag.IEPRIOCHAR);
	}

	@BeforeEach
	public void setUp() throws Exception {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		Document doc = docb.getDOMImplementation().createDocument(null, "html", null);
		Element body = doc.createElement("body");
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		body.setAttribute("style", "");
		doc.getDocumentElement().appendChild(body);
		Element div = doc.createElement("div");
		div.setAttribute("style", "display: block");
		body.appendChild(div);
		document = factory.createCSSDocument(doc);
		emptyStyleDecl = (BaseCSSStyleDeclaration) ((CSSElement) document.getDocumentElement()
				.getFirstChild()).getStyle();
	}

	private StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		return document.getErrorHandler()
				.getInlineStyleErrorHandler(document.getElementById("bodyId"));
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
	public void getCssTextForRem() {
		emptyStyleDecl.setCssText("line-height: 1.2rem; ");
		assertEquals("1.2rem", emptyStyleDecl.getPropertyValue("line-height"));
		NumberValue val = (NumberValue) emptyStyleDecl.getPropertyCSSValue("line-height");
		assertEquals("rem", val.getDimensionUnitText());
		assertEquals("line-height: 1.2rem; ", emptyStyleDecl.getCssText());
		assertEquals(1.2f, val.getFloatValue(CSSUnit.CSS_REM), 1e-9);
		assertEquals(1.2f, val.getFloatValue(CSSUnit.CSS_OTHER), 1e-9);
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
		assertEquals("content: 'â†\\90 '; ", emptyStyleDecl.getCssText());
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
	public void setCssTextFont() {
		emptyStyleDecl.setCssText("font-size: 16px;font-size: 12pt");
		assertEquals("12pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("font-size: 12pt; ", emptyStyleDecl.getCssText());
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
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextFilter() {
		String cssText = "filter:progid:DXImageTransform.Microsoft.Blur(pixelradius=5);";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)",
				emptyStyleDecl.getPropertyCSSValue("filter").getCssText());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)",
				emptyStyleDecl.getPropertyValue("filter"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextFilterIdentifier() {
		emptyStyleDecl.setCssText(
				"filter:progid\\:DXImageTransform\\.Microsoft\\.Blur\\(pixelradius\\=5\\);");
		assertEquals("progid\\:DXImageTransform\\.Microsoft\\.Blur\\(pixelradius\\=5\\)",
				emptyStyleDecl.getPropertyCSSValue("filter").getCssText());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)",
				emptyStyleDecl.getPropertyValue("filter"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextFilter3() {
		String cssText = "filter:progid:DXImageTransform.Microsoft.gradient(startColorStr='#f5f5f5',EndColorStr='#f1f1f1');";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals(
				"progid:DXImageTransform.Microsoft.gradient(startColorStr= '#f5f5f5', EndColorStr= '#f1f1f1')",
				emptyStyleDecl.getPropertyCSSValue("filter").getCssText());
		assertEquals(
				"progid:DXImageTransform.Microsoft.gradient(startColorStr= '#f5f5f5', EndColorStr= '#f1f1f1')",
				emptyStyleDecl.getPropertyValue("filter"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioChar() {
		String cssText = "margin:10px;margin:8px!important!;width:590px;width:600px!important!;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals(
				"margin: 10px; margin: 8px!important!; width: 590px; width: 600px!important!; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin:8px!important!;width:590px;width:600px!important!",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioChar2() {
		String cssText = "margin:8px!important!;margin:10px;width:600px!important!;width:590px;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals(
				"margin: 8px!important!; margin: 10px; width: 600px!important!; width: 590px; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:8px!important!;margin:10px;width:600px!important!;width:590px",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioChar3() {
		String cssText = "margin:10px 5px;margin:8px 4px!important!;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("margin: 10px 5px; margin: 8px 4px!important!; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 5px;margin:8px 4px!important!;",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioCharImportant() {
		String cssText = "margin:10px!important;margin:8px!important!;width:590px!important;width:600px!important!;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals(
				"margin: 10px ! important; margin: 8px!important!; width: 590px ! important; width: 600px!important!; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"margin:10px!important;margin:8px!important!;width:590px!important;width:600px!important!",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioCharImportant2() {
		String cssText = "margin:8px!important!;margin:10px!important;width:600px!important!;width:590px!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px ! important; width: 590px ! important; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio() {
		String cssText = "margin:10px;margin:10px!ie;width:590px;width:600px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px; margin: 10px!ie; width: 590px; width: 600px!ie; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin:10px!ie;width:590px;width:600px!ie",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio2() {
		String cssText = "margin:10px!ie;margin:10px;width:590px;width:600px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px; width: 590px; width: 600px!ie; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px;width:600px!ie", emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio3() {
		String cssText = "margin:10px 5px;margin:8px 4px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("margin: 10px 5px; margin: 8px 4px!ie; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 5px;margin:8px 4px!ie;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrio4() {
		String cssText = "display:inline-block;display:inline!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("display: inline-block; display: inline!ie; ", emptyStyleDecl.getCssText());
		assertEquals("display:inline-block;display:inline!ie", emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatIEPrioImportant() {
		String cssText = "margin:10px!important;margin:10px!ie;width:590px!important;width:600px!ie;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px ! important; width: 590px ! important; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompat() {
		String cssText = "margin:10px;margin:10px\\9;width:590px;width:600px\\9;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px; margin: 10px\\9; width: 590px; width: 600px\\9; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin:10px\\9;width:590px;width:600px\\9",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompat2() {
		String cssText = "margin:10px\\9;margin:10px;width:590px;width:600px\\9;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals("margin: 10px; width: 590px; width: 600px\\9; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;width:590px;width:600px\\9", emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompat3() {
		String cssText = "margin:10px 5px;margin:8px 4px\\9;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(4, emptyStyleDecl.getLength());
		assertEquals("margin: 10px 5px; margin: 8px 4px\\9; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 5px;margin:8px 4px\\9;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatImportant() {
		String cssText = "margin:10px;margin:10px\\9!important;width:590px;width:600px\\9!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals(
				"margin: 10px; margin: 10px\\9 ! important; width: 590px; width: 600px\\9 ! important; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin:10px\\9!important;width:590px;width:600px\\9!important",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatImportant2() {
		String cssText = "margin:10px!important;margin:10px\\9;width:590px!important;width:600px\\9!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(5, emptyStyleDecl.getLength());
		assertEquals(
				"margin: 10px ! important; width: 590px ! important; width: 600px\\9 ! important; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin:10px!important;width:590px!important;width:600px\\9!important",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextCompatBorderRadius() {
		String cssText = "border-radius:3px;border-radius:0\\9";
		emptyStyleDecl.setCssText(cssText);
		assertEquals("border-radius: 3px; border-radius: 0\\9; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:3px;border-radius:0\\9;", emptyStyleDecl.getMinifiedCssText());
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-radius"));
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void setCssTextWidth() {
		String cssText = "width:590px;width:600px;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("width: 600px; ", emptyStyleDecl.getCssText());
		assertEquals("width:600px", emptyStyleDecl.getMinifiedCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextWidthImportant() {
		String cssText = "width:590px;width:600px!important;";
		emptyStyleDecl.setCssText(cssText);
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("width: 600px ! important; ", emptyStyleDecl.getCssText());
		assertEquals("width:600px!important", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void setCssTextEscapes() {
		emptyStyleDecl.setCssText("content: '\\A'");
		assertEquals("'\\A'", emptyStyleDecl.getPropertyCSSValue("content").getCssText());
		assertEquals("\n", emptyStyleDecl.getPropertyValue("content"));
		assertEquals("content: '\\a '; ", emptyStyleDecl.getCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextEscapes2() {
		emptyStyleDecl.setCssText("symbols: \\1F44D");
		assertEquals("\\1f44d ", emptyStyleDecl.getPropertyCSSValue("symbols").getCssText());
		assertEquals("\uD83D\uDC4D", emptyStyleDecl.getPropertyValue("symbols"));
		assertEquals("symbols: \\1f44d ; ", emptyStyleDecl.getCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setCssTextFontFamily() {
		emptyStyleDecl.setCssText("font-family: Times New Roman, Verdana, Chicago");
		CSSValue value = ((ValueList) emptyStyleDecl.getPropertyCSSValue("font-family")).item(0);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals("Times New Roman", ((CSSTypedValue) value).getStringValue());
		assertEquals("'Times New Roman', Verdana, Chicago",
				emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
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
		emptyStyleDecl
				.setCssText("background-position: 10% 20%; background-position: top left top left");
		assertEquals("10% 20%", emptyStyleDecl.getPropertyValue("background-position"));
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
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
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
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
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
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
		emptyStyleDecl.setCssText("font: bold; border: solid blue; font-size: x-large;");
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("font-weight"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
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
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
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
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
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
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void setPropertyStringStringString() {
		emptyStyleDecl.setProperty("border", "none", "important");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("border"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("border-top-style"));
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testAddStyle() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) factory
				.createStyleSheet(null, null).createStyleRule().getStyle();
		style.setCssText("margin: 8px;");
		emptyStyleDecl.setCssText("margin-top: 10px;");
		emptyStyleDecl.addStyle(style);
		assertEquals("margin: 8px; ", emptyStyleDecl.getCssText());

		emptyStyleDecl.setCssText("margin: 8px;");
		style.setCssText(
				"margin-top: 10px; margin-right: 11px; margin-bottom: 12px; margin-left: 13px; ");
		emptyStyleDecl.addStyle(style);
		assertEquals(
				"margin-top: 10px; margin-right: 11px; margin-bottom: 12px; margin-left: 13px; ",
				emptyStyleDecl.getCssText());

		emptyStyleDecl.setCssText("margin: 8px ! important");
		style.setCssText(
				"margin-top: 10px; margin-right: 11px; margin-bottom: 12px; margin-left: 13px; ");
		emptyStyleDecl.addStyle(style);
		assertEquals("margin: 8px ! important; ", emptyStyleDecl.getCssText());
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());

		style.setCssText("border: 8px white; border-color: yellow ; ");
		// Set a wrong border
		emptyStyleDecl.setCssText("border: 10%");
		emptyStyleDecl.addStyle(style);
		assertEquals("border: 8px white; border-color: yellow; ", emptyStyleDecl.getCssText());
		assertTrue(getStyleDeclarationErrorHandler().hasErrors());
		getStyleDeclarationErrorHandler().reset();
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());

		style.setCssText("border: 1%; border-top-color: #122");
		emptyStyleDecl.setCssText("margin: 2px;");
		emptyStyleDecl.addStyle(style);
		assertEquals("margin: 2px; border-top-color: #122; ", emptyStyleDecl.getCssText());
		// Errors aren't added
		assertFalse(getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testEquals() {
		emptyStyleDecl.setCssText(
				"font: bold !important; border: solid blue; line-height: normal !important");
		Node div = document.getElementById("bodyId").getFirstChild();
		BaseCSSStyleDeclaration otherDecl = factory.createInlineStyle(div);
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
	public void testClone() {
		emptyStyleDecl.setCssText(
				"font: bold !important; border: solid blue; line-height: normal !important");
		BaseCSSStyleDeclaration clone = emptyStyleDecl.clone();
		assertEquals(
				"font: bold ! important; border: solid blue; line-height: normal ! important; ",
				clone.getCssText());
		assertEquals(34, clone.getLength());
	}

}
