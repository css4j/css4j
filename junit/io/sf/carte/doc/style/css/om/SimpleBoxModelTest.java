/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.BoxValues.TableBoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.property.CSSNumberValue;

public class SimpleBoxModelTest {

	static CSSDocument document;

	@BeforeClass
	public static void setUpBeforeClass() throws CSSMediaException, IOException, DocumentException {
		document = DOMCSSStyleSheetFactoryTest.simpleBoxHTML();
		document.setTargetMedium("screen");
	}

	@Test
	public void testGetComputedBox() {
		// This test is automatically generated, DO NOT EDIT MANUALLY !
		Node node;
		int delta;
		CSSElement elm = (CSSElement) document.getElementsByTagName("body").item(0);
		DocumentCSSStyleSheet sheet = document.getStyleSheet();
		CSSComputedProperties style;
		BoxValues box;
		TableBoxValues tablebox;
		// Element: body
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1.33, box.getPaddingTop(), 0.03f);
		assertEquals(1.33, box.getPaddingRight(), 0.03f);
		assertEquals(1.33, box.getPaddingBottom(), 0.03f);
		assertEquals(1.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(800, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList0_0_0 = elm.getChildNodes();
		node = ndList0_0_0.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"H1".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: h1 id = h1
		assertEquals("h1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(32.16, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(32.16, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(0, box.getPaddingTop(), 0.03f);
		assertEquals(0, box.getPaddingRight(), 0.03f);
		assertEquals(0, box.getPaddingBottom(), 0.03f);
		assertEquals(0, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(800, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList0_0_0.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"P".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: p id = para1
		assertEquals("para1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(40, box.getMarginTop(), 0.03f);
		assertEquals(80, box.getMarginRight(), 0.03f);
		assertEquals(40, box.getMarginBottom(), 0.03f);
		assertEquals(16, box.getMarginLeft(), 0.03f);
		assertEquals(2.67, box.getPaddingTop(), 0.03f);
		assertEquals(8, box.getPaddingRight(), 0.03f);
		assertEquals(2.67, box.getPaddingBottom(), 0.03f);
		assertEquals(8, box.getPaddingLeft(), 0.03f);
		assertEquals(1.33, box.getBorderTopWidth(), 0.03f);
		assertEquals(1.33, box.getBorderRightWidth(), 0.03f);
		assertEquals(1.33, box.getBorderBottomWidth(), 0.03f);
		assertEquals(1.33, box.getBorderLeftWidth(), 0.03f);
		assertEquals(685.34, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList0_0_0.item(4 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"P".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(4 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: p id = para2
		assertEquals("para2", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(40, box.getMarginTop(), 0.03f);
		assertEquals(80, box.getMarginRight(), 0.03f);
		assertEquals(40, box.getMarginBottom(), 0.03f);
		assertEquals(80, box.getMarginLeft(), 0.03f);
		assertEquals(2.67, box.getPaddingTop(), 0.03f);
		assertEquals(8, box.getPaddingRight(), 0.03f);
		assertEquals(2.67, box.getPaddingBottom(), 0.03f);
		assertEquals(8, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(624, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList4_1_4 = elm.getChildNodes();
		node = ndList4_1_4.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList4_1_4.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: span id = span1
		assertEquals("span1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(0, box.getPaddingTop(), 0.03f);
		assertEquals(0, box.getPaddingRight(), 0.03f);
		assertEquals(0, box.getPaddingBottom(), 0.03f);
		assertEquals(0, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Return to parent level (body)
		node = ndList0_0_0.item(6 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"H2".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(6 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: h2 id = h2
		assertEquals("h2", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(26.56, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(26.56, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(0, box.getPaddingTop(), 0.03f);
		assertEquals(0, box.getPaddingRight(), 0.03f);
		assertEquals(0, box.getPaddingBottom(), 0.03f);
		assertEquals(0, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(800, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList0_0_0.item(8 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"P".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(8 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: p id = smip
		assertEquals("smip", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(40, box.getMarginTop(), 0.03f);
		assertEquals(80, box.getMarginRight(), 0.03f);
		assertEquals(40, box.getMarginBottom(), 0.03f);
		assertEquals(16, box.getMarginLeft(), 0.03f);
		assertEquals(2.67, box.getPaddingTop(), 0.03f);
		assertEquals(8, box.getPaddingRight(), 0.03f);
		assertEquals(2.67, box.getPaddingBottom(), 0.03f);
		assertEquals(8, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(688, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList0_0_0.item(10 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"P".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(10 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: p id = tablepara
		assertEquals("tablepara", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(16, box.getMarginTop(), 0.03f);
		assertEquals(32, box.getMarginRight(), 0.03f);
		assertEquals(48, box.getMarginBottom(), 0.03f);
		assertEquals(32, box.getMarginLeft(), 0.03f);
		assertEquals(2.67, box.getPaddingTop(), 0.03f);
		assertEquals(8, box.getPaddingRight(), 0.03f);
		assertEquals(2.67, box.getPaddingBottom(), 0.03f);
		assertEquals(8, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(720, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList10_1_10 = elm.getChildNodes();
		node = ndList10_1_10.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList10_1_10.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: span
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(0, box.getPaddingTop(), 0.03f);
		assertEquals(0, box.getPaddingRight(), 0.03f);
		assertEquals(0, box.getPaddingBottom(), 0.03f);
		assertEquals(0, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Return to parent level (body)
		node = ndList0_0_0.item(12 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"TABLE".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(12 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: table id = table1
		assertEquals("table1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		tablebox = (TableBoxValues)style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, tablebox.getMarginTop(), 0.03f);
		assertEquals(0, tablebox.getMarginRight(), 0.03f);
		assertEquals(0, tablebox.getMarginBottom(), 0.03f);
		assertEquals(0, tablebox.getMarginLeft(), 0.03f);
		assertEquals(0, tablebox.getPaddingTop(), 0.03f);
		assertEquals(0, tablebox.getPaddingRight(), 0.03f);
		assertEquals(0, tablebox.getPaddingBottom(), 0.03f);
		assertEquals(0, tablebox.getPaddingLeft(), 0.03f);
		assertEquals(0, tablebox.getBorderTopWidth(), 0.03f);
		assertEquals(0, tablebox.getBorderRightWidth(), 0.03f);
		assertEquals(0, tablebox.getBorderBottomWidth(), 0.03f);
		assertEquals(0, tablebox.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList13_2_1 = elm.getChildNodes();
		node = ndList13_2_1.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr id = tablehdr1
		assertEquals("tablehdr1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(16, box.getMarginTop(), 0.03f);
		assertEquals(21.33, box.getMarginRight(), 0.03f);
		assertEquals(16, box.getMarginBottom(), 0.03f);
		assertEquals(21.33, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList13_3_0 = elm.getChildNodes();
		node = ndList13_3_0.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_3_0.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 1"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 98
		delta = 0;
		node = ndList13_3_0.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_3_0.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 2"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList13_3_0.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_3_0.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 3"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 112
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList15_3_2 = elm.getChildNodes();
		node = ndList15_3_2.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList15_3_2.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell00
		assertEquals("cell00", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList15_3_2.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList15_3_2.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell01
		assertEquals("cell01", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList15_3_2.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList15_3_2.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell02
		assertEquals("cell02", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(4 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(4 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList17_3_4 = elm.getChildNodes();
		node = ndList17_3_4.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_4.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell10
		assertEquals("cell10", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList17_3_4.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_4.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell11
		assertEquals("cell11", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList17_3_4.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_4.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell12
		assertEquals("cell12", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(6 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(6 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList19_3_6 = elm.getChildNodes();
		node = ndList19_3_6.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_6.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell20
		assertEquals("cell20", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList19_3_6.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_6.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell21
		assertEquals("cell21", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList19_3_6.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_6.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell22
		assertEquals("cell22", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(8 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(8 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList21_3_8 = elm.getChildNodes();
		node = ndList21_3_8.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_8.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell30
		assertEquals("cell30", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList21_3_8.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_8.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell31
		assertEquals("cell31", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList21_3_8.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_8.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell32
		assertEquals("cell32", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(10 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(10 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList23_3_10 = elm.getChildNodes();
		node = ndList23_3_10.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_10.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell40
		assertEquals("cell40", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList23_3_10.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_10.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell41
		assertEquals("cell41", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList23_3_10.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_10.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell42
		assertEquals("cell42", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(12 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(12 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList25_3_12 = elm.getChildNodes();
		node = ndList25_3_12.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList25_3_12.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell50
		assertEquals("cell50", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList25_3_12.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList25_3_12.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell51
		assertEquals("cell51", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList25_3_12.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList25_3_12.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell52
		assertEquals("cell52", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		node = ndList13_2_1.item(14 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList13_2_1.item(14 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList27_3_14 = elm.getChildNodes();
		node = ndList27_3_14.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList27_3_14.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell60
		assertEquals("cell60", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 68
		delta = 0;
		node = ndList27_3_14.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList27_3_14.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell61
		assertEquals("cell61", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 38
		delta = 0;
		node = ndList27_3_14.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList27_3_14.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell62
		assertEquals("cell62", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 82
		delta = 0;
		// Return to parent level (tbody)
		// Return to parent level (table)
		node = ndList0_0_0.item(14 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"P".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(14 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: p
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(40, box.getMarginTop(), 0.03f);
		assertEquals(80, box.getMarginRight(), 0.03f);
		assertEquals(40, box.getMarginBottom(), 0.03f);
		assertEquals(80, box.getMarginLeft(), 0.03f);
		assertEquals(2.67, box.getPaddingTop(), 0.03f);
		assertEquals(8, box.getPaddingRight(), 0.03f);
		assertEquals(2.67, box.getPaddingBottom(), 0.03f);
		assertEquals(8, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(624, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList0_0_0.item(16 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"TABLE".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(16 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: table id = table2
		assertEquals("table2", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		tablebox = (TableBoxValues)style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, tablebox.getMarginTop(), 0.03f);
		assertEquals(0, tablebox.getMarginRight(), 0.03f);
		assertEquals(0, tablebox.getMarginBottom(), 0.03f);
		assertEquals(0, tablebox.getMarginLeft(), 0.03f);
		assertEquals(0, tablebox.getPaddingTop(), 0.03f);
		assertEquals(0, tablebox.getPaddingRight(), 0.03f);
		assertEquals(0, tablebox.getPaddingBottom(), 0.03f);
		assertEquals(0, tablebox.getPaddingLeft(), 0.03f);
		assertEquals(0, tablebox.getBorderTopWidth(), 0.03f);
		assertEquals(0, tablebox.getBorderRightWidth(), 0.03f);
		assertEquals(0, tablebox.getBorderBottomWidth(), 0.03f);
		assertEquals(0, tablebox.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList17_2_1 = elm.getChildNodes();
		node = ndList17_2_1.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_2_1.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr id = tablehdr2
		assertEquals("tablehdr2", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(16, box.getMarginTop(), 0.03f);
		assertEquals(21.33, box.getMarginRight(), 0.03f);
		assertEquals(16, box.getMarginBottom(), 0.03f);
		assertEquals(21.33, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList17_3_0 = elm.getChildNodes();
		node = ndList17_3_0.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_0.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 1"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 104
		delta = 0;
		node = ndList17_3_0.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_0.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 2"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 72
		delta = 0;
		node = ndList17_3_0.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_0.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 3"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 200
		delta = 0;
		node = ndList17_3_0.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_3_0.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: th "Header 4"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(1, box.getPaddingTop(), 0.03f);
		assertEquals(1, box.getPaddingRight(), 0.03f);
		assertEquals(1, box.getPaddingBottom(), 0.03f);
		assertEquals(1, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 139
		delta = 0;
		// Return to parent level (tbody)
		node = ndList17_2_1.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_2_1.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList19_3_2 = elm.getChildNodes();
		node = ndList19_3_2.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_2.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell00
		assertEquals("cell00", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 74
		delta = 0;
		node = ndList19_3_2.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_2.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell01
		assertEquals("cell01", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 42
		delta = 0;
		node = ndList19_3_2.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_2.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell02
		assertEquals("cell02", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 170
		delta = 0;
		node = ndList19_3_2.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList19_3_2.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell03
		assertEquals("cell03", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 109
		delta = 0;
		// Return to parent level (tbody)
		node = ndList17_2_1.item(4 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_2_1.item(4 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList21_3_4 = elm.getChildNodes();
		node = ndList21_3_4.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_4.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell10
		assertEquals("cell10", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 74
		delta = 0;
		node = ndList21_3_4.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_4.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td "Cell"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 42
		delta = 0;
		node = ndList21_3_4.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_4.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td "Cell with more content"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 170
		delta = 0;
		node = ndList21_3_4.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList21_3_4.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td "Cell"
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 109
		delta = 0;
		// Return to parent level (tbody)
		node = ndList17_2_1.item(6 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList17_2_1.item(6 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: tr
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(5.33, box.getPaddingTop(), 0.03f);
		assertEquals(5.33, box.getPaddingRight(), 0.03f);
		assertEquals(5.33, box.getPaddingBottom(), 0.03f);
		assertEquals(5.33, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList23_3_6 = elm.getChildNodes();
		node = ndList23_3_6.item(0 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_6.item(0 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell20
		assertEquals("cell20", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 74
		delta = 0;
		node = ndList23_3_6.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_6.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell21
		assertEquals("cell21", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 42
		delta = 0;
		node = ndList23_3_6.item(2 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_6.item(2 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell22
		assertEquals("cell22", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 170
		delta = 0;
		node = ndList23_3_6.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_3_6.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: td id = cell23
		assertEquals("cell23", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(0, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(0, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(16, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(16, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		// width: 109
		delta = 0;
		// Return to parent level (tbody)
		// Return to parent level (table)
		node = ndList0_0_0.item(18 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"H3".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(18 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: h3 id = firstH3
		assertEquals("firstH3", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(21.33, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(21.33, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(0, box.getPaddingTop(), 0.03f);
		assertEquals(0, box.getPaddingRight(), 0.03f);
		assertEquals(0, box.getPaddingBottom(), 0.03f);
		assertEquals(0, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(800, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList0_0_0.item(20 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE || !"DIV".equals(((CSSElement)node).getTagName().toUpperCase(Locale.US))) {
		  delta++;
		  node = ndList0_0_0.item(20 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: div id = div1
		assertEquals("div1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(32, box.getMarginTop(), 0.03f);
		assertEquals(0, box.getMarginRight(), 0.03f);
		assertEquals(48, box.getMarginBottom(), 0.03f);
		assertEquals(0, box.getMarginLeft(), 0.03f);
		assertEquals(0, box.getPaddingTop(), 0.03f);
		assertEquals(0, box.getPaddingRight(), 0.03f);
		assertEquals(0, box.getPaddingBottom(), 0.03f);
		assertEquals(0, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(800, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList20_1_20 = elm.getChildNodes();
		node = ndList20_1_20.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList20_1_20.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: p id = listpara
		assertEquals("listpara", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(40, box.getMarginTop(), 0.03f);
		assertEquals(80, box.getMarginRight(), 0.03f);
		assertEquals(40, box.getMarginBottom(), 0.03f);
		assertEquals(80, box.getMarginLeft(), 0.03f);
		assertEquals(2.67, box.getPaddingTop(), 0.03f);
		assertEquals(8, box.getPaddingRight(), 0.03f);
		assertEquals(2.67, box.getPaddingBottom(), 0.03f);
		assertEquals(8, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(624, box.getWidth(), 0.1f);
		assertEquals(21.33, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList20_1_20.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList20_1_20.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: ul id = ul1
		assertEquals("ul1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(16, box.getMarginTop(), 0.03f);
		assertEquals(32, box.getMarginRight(), 0.03f);
		assertEquals(32, box.getMarginBottom(), 0.03f);
		assertEquals(48, box.getMarginLeft(), 0.03f);
		assertEquals(8, box.getPaddingTop(), 0.03f);
		assertEquals(12.80, box.getPaddingRight(), 0.03f);
		assertEquals(8, box.getPaddingBottom(), 0.03f);
		assertEquals(12.80, box.getPaddingLeft(), 0.03f);
		assertEquals(2.66, box.getBorderTopWidth(), 0.03f);
		assertEquals(2.66, box.getBorderRightWidth(), 0.03f);
		assertEquals(2.66, box.getBorderBottomWidth(), 0.03f);
		assertEquals(2.66, box.getBorderLeftWidth(), 0.03f);
		assertEquals(689.09, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Evaluating child nodes:
		NodeList ndList23_2_3 = elm.getChildNodes();
		node = ndList23_2_3.item(1 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_2_3.item(1 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: li id = ul1li1
		assertEquals("ul1li1", elm.getAttribute("id"));
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(19.20, box.getMarginTop(), 0.03f);
		assertEquals(38.40, box.getMarginRight(), 0.03f);
		assertEquals(19.20, box.getMarginBottom(), 0.03f);
		assertEquals(38.40, box.getMarginLeft(), 0.03f);
		assertEquals(4, box.getPaddingTop(), 0.03f);
		assertEquals(6.67, box.getPaddingRight(), 0.03f);
		assertEquals(4, box.getPaddingBottom(), 0.03f);
		assertEquals(6.67, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(599, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList23_2_3.item(3 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_2_3.item(3 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: li
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(19.20, box.getMarginTop(), 0.03f);
		assertEquals(38.40, box.getMarginRight(), 0.03f);
		assertEquals(19.20, box.getMarginBottom(), 0.03f);
		assertEquals(38.40, box.getMarginLeft(), 0.03f);
		assertEquals(4, box.getPaddingTop(), 0.03f);
		assertEquals(6.67, box.getPaddingRight(), 0.03f);
		assertEquals(4, box.getPaddingBottom(), 0.03f);
		assertEquals(6.67, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(599, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList23_2_3.item(5 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_2_3.item(5 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: li
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(19.20, box.getMarginTop(), 0.03f);
		assertEquals(38.40, box.getMarginRight(), 0.03f);
		assertEquals(19.20, box.getMarginBottom(), 0.03f);
		assertEquals(38.40, box.getMarginLeft(), 0.03f);
		assertEquals(4, box.getPaddingTop(), 0.03f);
		assertEquals(6.67, box.getPaddingRight(), 0.03f);
		assertEquals(4, box.getPaddingBottom(), 0.03f);
		assertEquals(6.67, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(599, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList23_2_3.item(7 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_2_3.item(7 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: li
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(19.20, box.getMarginTop(), 0.03f);
		assertEquals(38.40, box.getMarginRight(), 0.03f);
		assertEquals(19.20, box.getMarginBottom(), 0.03f);
		assertEquals(38.40, box.getMarginLeft(), 0.03f);
		assertEquals(4, box.getPaddingTop(), 0.03f);
		assertEquals(6.67, box.getPaddingRight(), 0.03f);
		assertEquals(4, box.getPaddingBottom(), 0.03f);
		assertEquals(6.67, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(599, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList23_2_3.item(9 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_2_3.item(9 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: li
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(19.20, box.getMarginTop(), 0.03f);
		assertEquals(38.40, box.getMarginRight(), 0.03f);
		assertEquals(19.20, box.getMarginBottom(), 0.03f);
		assertEquals(38.40, box.getMarginLeft(), 0.03f);
		assertEquals(4, box.getPaddingTop(), 0.03f);
		assertEquals(6.67, box.getPaddingRight(), 0.03f);
		assertEquals(4, box.getPaddingBottom(), 0.03f);
		assertEquals(6.67, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(599, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		node = ndList23_2_3.item(11 + delta);
		while(node.getNodeType() != Node.ELEMENT_NODE) {
		  delta++;
		  node = ndList23_2_3.item(11 + delta);
		}
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elm = (CSSElement)node;
		// Element: li
		style = sheet.getComputedStyle(elm, null);
		box = style.getBoxValues(CSSPrimitiveValue.CSS_PX);
		assertEquals(19.20, box.getMarginTop(), 0.03f);
		assertEquals(38.40, box.getMarginRight(), 0.03f);
		assertEquals(19.20, box.getMarginBottom(), 0.03f);
		assertEquals(38.40, box.getMarginLeft(), 0.03f);
		assertEquals(4, box.getPaddingTop(), 0.03f);
		assertEquals(6.67, box.getPaddingRight(), 0.03f);
		assertEquals(4, box.getPaddingBottom(), 0.03f);
		assertEquals(6.67, box.getPaddingLeft(), 0.03f);
		assertEquals(0, box.getBorderTopWidth(), 0.03f);
		assertEquals(0, box.getBorderRightWidth(), 0.03f);
		assertEquals(0, box.getBorderBottomWidth(), 0.03f);
		assertEquals(0, box.getBorderLeftWidth(), 0.03f);
		assertEquals(599, box.getWidth(), 0.1f);
		assertEquals(19.20, CSSNumberValue.floatValueConversion(style.getComputedLineHeight(), CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PX), 0.03f);
		delta = 0;
		// Return to parent level (div)
		// Return to parent level (body)
		// Return to parent level (html)
	}

}
