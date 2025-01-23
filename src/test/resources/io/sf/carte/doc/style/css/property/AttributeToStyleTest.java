/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class AttributeToStyleTest {

	static AbstractCSSStyleSheet sheet;

	CSSStyleDeclaration style;

	@BeforeAll
	public static void setUpBeforeAll() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		sheet = factory.createStyleSheet(null, null);
	}

	@BeforeEach
	public void setUp() {
		style = sheet.createStyleDeclaration();
	}

	@Test
	void testAlign() {
		AttributeToStyle.align("middle", style);
		assertEquals("center", style.getPropertyValue("text-align"));

		AttributeToStyle.align("justify", style);
		assertEquals("justify", style.getPropertyValue("text-align"));

		AttributeToStyle.align("left", style);
		assertEquals("left", style.getPropertyValue("text-align"));

		AttributeToStyle.align("right", style);
		assertEquals("right", style.getPropertyValue("text-align"));
	}

	@Test
	void testBgcolor() {
		AttributeToStyle.bgcolor("orange", style);
		assertEquals("orange", style.getPropertyValue("background-color"));
	}

	@Test
	void testWidth() {
		AttributeToStyle.width("30%", style);
		assertEquals("30%", style.getPropertyValue("width"));

		AttributeToStyle.width("10", style);
		assertEquals("10px", style.getPropertyValue("width"));

		AttributeToStyle.width("19", style);
		assertEquals("19px", style.getPropertyValue("width"));
	}

	@Test
	void testHeight() {
		AttributeToStyle.height("30%", style);
		assertEquals("30%", style.getPropertyValue("height"));

		AttributeToStyle.height("10", style);
		assertEquals("10px", style.getPropertyValue("height"));

		AttributeToStyle.height("19", style);
		assertEquals("19px", style.getPropertyValue("height"));
	}

	@Test
	void testFace() {
		AttributeToStyle.face("Arial, Helvetica", style);
		assertEquals("Arial, Helvetica", style.getPropertyValue("font-family"));
	}

	@Test
	void testSize() {
		AttributeToStyle.size("1", style);
		assertEquals("8px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("2", style);
		assertEquals("10px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("3", style);
		assertEquals("12px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("4", style);
		assertEquals("14px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("5", style);
		assertEquals("18px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("6", style);
		assertEquals("24px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("7", style);
		assertEquals("28px", style.getPropertyValue("font-size"));

		AttributeToStyle.size("+1", style);
		assertEquals("larger", style.getPropertyValue("font-size"));

		AttributeToStyle.size("-1", style);
		assertEquals("smaller", style.getPropertyValue("font-size"));
	}

	@Test
	void testSizeInvalid() {
		AttributeToStyle.size("100", style);
		assertEquals(0, style.getPropertyValue("font-size").length());

		AttributeToStyle.size("invalid", style);
		assertEquals(0, style.getPropertyValue("font-size").length());
	}

	@Test
	void testColor() {
		AttributeToStyle.color("blue", style);
		assertEquals("blue", style.getPropertyValue("color"));
	}

	@Test
	void testBorder() {
		AttributeToStyle.border("6", style);
		assertEquals("6px", style.getPropertyValue("border-top-width"));
		assertEquals("6px", style.getPropertyValue("border-right-width"));
		assertEquals("6px", style.getPropertyValue("border-bottom-width"));
		assertEquals("6px", style.getPropertyValue("border-left-width"));

		AttributeToStyle.border("thick", style);
		assertEquals("thick", style.getPropertyValue("border-top-width"));
		assertEquals("thick", style.getPropertyValue("border-right-width"));
		assertEquals("thick", style.getPropertyValue("border-bottom-width"));
		assertEquals("thick", style.getPropertyValue("border-left-width"));
	}

	@Test
	void testBorderColor() {
		AttributeToStyle.borderColor("blue", style);
		assertEquals("blue", style.getPropertyValue("border-top-color"));
		assertEquals("blue", style.getPropertyValue("border-right-color"));
		assertEquals("blue", style.getPropertyValue("border-bottom-color"));
		assertEquals("blue", style.getPropertyValue("border-left-color"));
	}

	@Test
	void testCellSpacing() {
		AttributeToStyle.cellSpacing("2", style);
		assertEquals("2px", style.getPropertyValue("border-spacing"));
	}

	@Test
	void testBackground() {
		AttributeToStyle.background("url(image.jpg)", style);
		assertEquals("url('image.jpg')", style.getPropertyValue("background-image"));
	}

	@Test
	void testHspace() {
		AttributeToStyle.hspace("16", style);
		assertEquals("16px", style.getPropertyValue("margin-right"));
		assertEquals("16px", style.getPropertyValue("margin-left"));
	}

	@Test
	void testVspace() {
		AttributeToStyle.vspace("16", style);
		assertEquals("16px", style.getPropertyValue("margin-top"));
		assertEquals("16px", style.getPropertyValue("margin-bottom"));
	}

}
