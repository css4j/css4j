/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleDefiner;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

@RunWith(Parameterized.class)
public class ShorthandSetterTest {

	AbstractCSSStyleSheet sheet;
	BaseCSSStyleDeclaration emptyStyleDecl;

	public ShorthandSetterTest(EnumSet<Parser.Flag> flags) throws ParserConfigurationException, CSSMediaException {
		super();
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory(flags);
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		Element body = doc.createElement("body");
		doc.getDocumentElement().appendChild(body);
		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		cssdoc.setTargetMedium("screen");
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		sheet = ((LinkStyleDefiner) cssStyle).getSheet();
	}

	@Parameters
	public static Collection<EnumSet<Parser.Flag>> data() {
		List<EnumSet<Parser.Flag>> flags = new LinkedList<EnumSet<Parser.Flag>>();
		flags.add(EnumSet.noneOf(Parser.Flag.class));
		flags.add(EnumSet.of(Parser.Flag.IEVALUES));
		return flags;
	}

	@Before
	public void setUp() {
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testAnimation() {
		emptyStyleDecl.setCssText("animation: ease-in ease-out");
		assertEquals("ease-in", emptyStyleDecl.getPropertyValue("animation-timing-function"));
		assertEquals("ease-out", emptyStyleDecl.getPropertyValue("animation-name"));
		assertEquals("0s", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("0s", emptyStyleDecl.getPropertyValue("animation-delay"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("animation-direction"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("animation-iteration-count"));
		assertEquals("running", emptyStyleDecl.getPropertyValue("animation-play-state"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("animation-timing-function").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("animation-name").isSubproperty());
		assertEquals("animation: ease-in ease-out; ", emptyStyleDecl.getCssText());
		assertEquals("animation:ease-in ease-out;", emptyStyleDecl.getMinifiedCssText());
		// CSS Animation example 6
		emptyStyleDecl.setCssText("animation: 3s none backwards");
		assertEquals("3s", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("animation-fill-mode"));
		assertEquals("backwards", emptyStyleDecl.getPropertyValue("animation-name"));
		emptyStyleDecl.setCssText("animation: 3500ms 5s none backwards");
		assertEquals("3500ms", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("5s", emptyStyleDecl.getPropertyValue("animation-delay"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("animation-fill-mode"));
		assertEquals("backwards", emptyStyleDecl.getPropertyValue("animation-name"));
		emptyStyleDecl.setCssText("animation: 3500ms 5s reverse 'my anim'");
		assertEquals("3500ms", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("5s", emptyStyleDecl.getPropertyValue("animation-delay"));
		assertEquals("reverse", emptyStyleDecl.getPropertyValue("animation-direction"));
		assertEquals("my anim", emptyStyleDecl.getPropertyValue("animation-name"));
		assertEquals("animation:3500ms 5s reverse 'my anim';", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("animation: 3500ms 5s frames(5) reverse 'my anim'");
		assertEquals("3500ms", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("5s", emptyStyleDecl.getPropertyValue("animation-delay"));
		assertEquals("frames(5)", emptyStyleDecl.getPropertyValue("animation-timing-function"));
		assertEquals("reverse", emptyStyleDecl.getPropertyValue("animation-direction"));
		assertEquals("my anim", emptyStyleDecl.getPropertyValue("animation-name"));
		emptyStyleDecl.setCssText("animation: 0 5s steps(2, start) reverse 'my anim'");
		assertEquals("0", emptyStyleDecl.getPropertyValue("animation-iteration-count"));
		assertEquals("5s", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("steps(2, start)", emptyStyleDecl.getPropertyValue("animation-timing-function"));
		assertEquals("reverse", emptyStyleDecl.getPropertyValue("animation-direction"));
		assertEquals("my anim", emptyStyleDecl.getPropertyValue("animation-name"));
		assertEquals("animation: 0 5s steps(2, start) reverse 'my anim'; ", emptyStyleDecl.getCssText());
		assertEquals("animation:0 5s steps(2,start) reverse 'my anim';", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl
				.setCssText("animation: 3500ms 5s reverse '1st anim', 0 3s steps(2, start) alternate \"2nd anim\"");
		assertEquals("1, 0", emptyStyleDecl.getPropertyValue("animation-iteration-count"));
		assertEquals("3500ms, 3s", emptyStyleDecl.getPropertyValue("animation-duration"));
		assertEquals("5s, 0s", emptyStyleDecl.getPropertyValue("animation-delay"));
		assertEquals("ease, steps(2, start)", emptyStyleDecl.getPropertyValue("animation-timing-function"));
		assertEquals("reverse, alternate", emptyStyleDecl.getPropertyValue("animation-direction"));
		assertEquals("'1st anim', \"2nd anim\"", emptyStyleDecl.getPropertyValue("animation-name"));
		assertEquals("none, none", emptyStyleDecl.getPropertyValue("animation-fill-mode"));
		assertEquals("running, running", emptyStyleDecl.getPropertyValue("animation-play-state"));
		assertEquals("animation: 3500ms 5s reverse '1st anim', 0 3s steps(2, start) alternate \"2nd anim\"; ",
				emptyStyleDecl.getCssText());
		assertEquals("animation:3500ms 5s reverse '1st anim',0 3s steps(2,start) alternate \"2nd anim\";",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImportant() {
		emptyStyleDecl.setCssText("border: 1px dashed blue ! important; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border: 1px dashed blue ! important; ", emptyStyleDecl.getCssText());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-top-width").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-top-style").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-top-color").isSubproperty());
		assertEquals("border:1px dashed blue!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTrailingSubproperty() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-top-color: yellow; ");
		assertEquals(17, emptyStyleDecl.getLength());
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-top-width").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-top-style").isSubproperty());
		assertFalse(emptyStyleDecl.getPropertyCSSValue("border-top-color").isSubproperty());
		assertEquals("border: 1px dashed blue; border-top-color: yellow; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;border-top-color:yellow", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorder() {
		emptyStyleDecl.setCssText("border-image: url('foo.png'); border-top-color: yellow; border: 1px dashed blue; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("border: 1px dashed blue; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorder2() {
		emptyStyleDecl.setCssText("border: 1px dashed  blue; border-top: 4px dotted  green; border: 2px solid yellow;");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border: 2px solid yellow; ", emptyStyleDecl.getCssText());
		emptyStyleDecl.setCssText(
				"border: 1px dashed blue; border-top: 4px dotted green ! important; border: 2px solid yellow;");
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border-top: 4px dotted green ! important; border: 2px solid yellow; ",
				emptyStyleDecl.getCssText());
		assertEquals("border-top:4px dotted green!important;border:2px solid yellow;",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText(
				"border: 1px dashed blue; border-top: 4px dotted green ! important; border: 2px solid yellow ! important;");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border: 2px solid yellow ! important; ", emptyStyleDecl.getCssText());
		assertEquals("border:2px solid yellow!important;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText(
				"border: 1px dashed blue ! important; border-top: 4px dotted green; border: 2px solid yellow;");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border: 1px dashed blue ! important; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorder3() {
		emptyStyleDecl.setCssText("border: solid");
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border: solid; ", emptyStyleDecl.getCssText());
		assertEquals("border:solid;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorder4() {
		emptyStyleDecl.setCssText("border: solid 1px rgba(251, 190, 0, 78%)");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("rgba(251, 190, 0, 78%)", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border: solid 1px rgba(251, 190, 0, 78%); ", emptyStyleDecl.getCssText());
		assertEquals("border:solid 1px rgba(251,190,0,78%);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderZero() {
		emptyStyleDecl.setCssText("border: 0");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals(0, ((CSSTypedValue) emptyStyleDecl.getPropertyCSSValue("border-top-width"))
				.getFloatValue(CSSUnit.CSS_NUMBER), 0.01);
		assertEquals("border: 0; ", emptyStyleDecl.getCssText());
		assertEquals("border:0;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTransparent() {
		emptyStyleDecl.setCssText("border: solid rgb(0 0 0 / 0)");
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("rgb(0 0 0 / 0)", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("rgb(0 0 0 / 0)", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border: solid rgb(0 0 0 / 0); ", emptyStyleDecl.getCssText());
		assertEquals("border:solid rgb(0 0 0/0);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border: rgb(0 0 0 / 0) solid");
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("rgb(0 0 0 / 0)", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("rgb(0 0 0 / 0)", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border: rgb(0 0 0 / 0) solid; ", emptyStyleDecl.getCssText());
		assertEquals("border:rgb(0 0 0/0) solid;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderBad() {
		emptyStyleDecl.setCssText("border-top: 4px dotted green; border: 1px dashed blue foo; ");
		assertEquals("border-top: 4px dotted green; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:4px dotted green;", emptyStyleDecl.getMinifiedCssText());
		assertEquals(3, emptyStyleDecl.getLength());
		assertTrue(emptyStyleDecl.getParentRule().getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderIEHack() {
		emptyStyleDecl.setCssText("border-top:4px dotted green; border:solid 1px blue \\0;");
		assertEquals("border-top: 4px dotted green; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:4px dotted green;", emptyStyleDecl.getMinifiedCssText());
		assertEquals(3, emptyStyleDecl.getLength());
		assertTrue(emptyStyleDecl.getParentRule().getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderIEHack2() {
		emptyStyleDecl.setCssText("border-top:4px dotted green; border:solid 1px blue \\9;");
		assertEquals("border-top: 4px dotted green; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:4px dotted green;", emptyStyleDecl.getMinifiedCssText());
		assertEquals(3, emptyStyleDecl.getLength());
		assertTrue(emptyStyleDecl.getParentRule().getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderVar() {
		emptyStyleDecl.setCssText("border-top: 4px dotted green; border: 1px dashed var(--mycolor); ");
		assertEquals("border: 1px dashed var(--mycolor); ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed var(--mycolor);", emptyStyleDecl.getMinifiedCssText());
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals(17, emptyStyleDecl.getLength());
	}

	@Test
	public void testBorderVarBad() {
		emptyStyleDecl.setCssText("border-top: 4px dotted green; border: dashed var(--mycolor); ");
		assertEquals("border: dashed var(--mycolor); ", emptyStyleDecl.getCssText());
		assertEquals("border:dashed var(--mycolor);", emptyStyleDecl.getMinifiedCssText());
		assertEquals(17, emptyStyleDecl.getLength());
	}

	@Test
	public void testBorderNone() {
		emptyStyleDecl.setCssText("border: none; ");
		assertEquals("border: none; ", emptyStyleDecl.getCssText());
		assertEquals("border:none;", emptyStyleDecl.getMinifiedCssText());
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
	}

	@Test
	public void testBorderInherit() {
		emptyStyleDecl.setCssText("border: inherit");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("border:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-top: inherit");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border-top: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:inherit;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderInherit3() {
		emptyStyleDecl.setCssText("border: inherit inherit blue");
		assertEquals(0, emptyStyleDecl.getLength());
	}

	@Test
	public void testBorderTopInherit() {
		emptyStyleDecl.setCssText("border-top: inherit inherit blue");
		assertEquals(0, emptyStyleDecl.getLength());
	}

	@Test
	public void testBorderTop() {
		emptyStyleDecl.setCssText("border-top-style: inset; border-top: 1px; ");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("border-top: 1px; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:1px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-top: 1px dashed; ");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("dashed", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("border-top: 1px dashed; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:1px dashed;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-top: 1px dashed yellow; ");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("dashed", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border-top: 1px dashed yellow; ", emptyStyleDecl.getCssText());
		assertEquals("border-top:1px dashed yellow;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTop2() {
		emptyStyleDecl.setCssText("border-left-width: 2px; border-top: 0");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals(0, ((CSSTypedValue) emptyStyleDecl.getPropertyCSSValue("border-top-width"))
				.getFloatValue(CSSUnit.CSS_NUMBER), 0.01);
		assertEquals("border-left-width: 2px; border-top: 0; ", emptyStyleDecl.getCssText());
		assertEquals("border-left-width:2px;border-top:0;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTopColorFunction() {
		emptyStyleDecl.setCssText("border-top: 1px solid myfunc(rgb(240, 240, 240), 25.0%)");
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("myfunc(#f0f0f0, 25%)", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border-top: 1px solid myfunc(#f0f0f0, 25%); ", emptyStyleDecl.getCssText());
		assertEquals("border-top:1px solid myfunc(#f0f0f0,25%);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-top: 1px solid color(blue w(+ 20%) s(+ 20%))");
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("color(blue w(+ 20%) s(+ 20%))", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border-top: 1px solid color(blue w(+ 20%) s(+ 20%)); ", emptyStyleDecl.getCssText());
		assertEquals("border-top:1px solid color(blue w(+ 20%) s(+ 20%));", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTopVar() {
		emptyStyleDecl.setCssText("border-top: 0 var(--whatever)");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border-top: 0 var(--whatever); ", emptyStyleDecl.getCssText());
		assertEquals("border-top:0 var(--whatever);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTopVar2() {
		emptyStyleDecl.setCssText("border-top: 0 solid var(--mycolor)");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border-top: 0 solid var(--mycolor); ", emptyStyleDecl.getCssText());
		assertEquals("border-top:0 solid var(--mycolor);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTopVar3() {
		emptyStyleDecl.setCssText("border-top: solid var(--mywidth, 2px) var(--mycolor, #da0)");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border-top: solid var(--mywidth, 2px) var(--mycolor, #da0); ", emptyStyleDecl.getCssText());
		assertEquals("border-top:solid var(--mywidth,2px) var(--mycolor,#da0);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderTopVar4() {
		emptyStyleDecl.setCssText("border-top: solid var(--mywidth, 2px)");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("border-top: solid var(--mywidth, 2px); ", emptyStyleDecl.getCssText());
		assertEquals("border-top:solid var(--mywidth,2px);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderColor() {
		emptyStyleDecl.setCssText("border-top-color: yellow; border-color: blue; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: blue; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:blue;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: blue navy; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: blue navy; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:blue navy;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: blue navy green; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("green", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: blue navy green; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:blue navy green;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: blue navy green transparent; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("green", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: blue navy green transparent; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:blue navy green transparent;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: blue #11bbfc green; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("#11bbfc", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("green", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("#11bbfc", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: blue #11bbfc green; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:blue #11bbfc green;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: #ef7 rgb(255 255 255 / 0) rgba(255, 255, 255, 0); ");
		assertEquals("#ef7", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("rgb(255 255 255 / 0)", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("rgba(255, 255, 255, 0)", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("rgb(255 255 255 / 0)", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: #ef7 rgb(255 255 255 / 0) rgba(255, 255, 255, 0); ", emptyStyleDecl.getCssText());
		assertEquals("border-color:#ef7 rgb(255 255 255/0) rgba(255,255,255,0);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: blue navy green transparent; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("green", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: blue navy green transparent; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:blue navy green transparent;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-color: var(--mytopcolor) navy green transparent; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: var(--mytopcolor) navy green transparent; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:var(--mytopcolor) navy green transparent;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderColorNone() {
		emptyStyleDecl.setCssText("border-left-color: yellow; border-color: none; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-left-color: yellow; ", emptyStyleDecl.getCssText());
		assertEquals("border-left-color:yellow", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderColorInherit() {
		emptyStyleDecl.setCssText("border-top-color: blue; border-color: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-color: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("border-color:inherit;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderColorInherit2() {
		emptyStyleDecl.setCssText("border-color: blue inherit; ");
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertFalse(emptyStyleDecl.getPropertyCSSValue("border-top-color").isSubproperty());
		assertFalse(emptyStyleDecl.getPropertyCSSValue("border-right-color").isSubproperty());
		assertEquals(
				"border-top-color: blue; border-right-color: inherit; border-bottom-color: blue; border-left-color: inherit; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"border-top-color:blue;border-right-color:inherit;border-bottom-color:blue;border-left-color:inherit",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderColorInherit3() {
		emptyStyleDecl.setCssText("border-color: inherit inherit blue; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertFalse(emptyStyleDecl.getShorthandSet().contains("border-color"));
		assertEquals(
				"border-top-color: inherit; border-right-color: inherit; border-bottom-color: blue; border-left-color: inherit; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"border-top-color:inherit;border-right-color:inherit;border-bottom-color:blue;border-left-color:inherit",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderStyle() {
		emptyStyleDecl.setCssText("border-top-style: dotted; border-style: inset; ");
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("border-style: inset; ", emptyStyleDecl.getCssText());
		assertEquals("border-style:inset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-style: inset solid; ");
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("border-style: inset solid; ", emptyStyleDecl.getCssText());
		assertEquals("border-style:inset solid;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-style: inset solid dotted; ");
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("border-style: inset solid dotted; ", emptyStyleDecl.getCssText());
		assertEquals("border-style:inset solid dotted;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-style: inset solid dotted outset; ");
		assertEquals("inset", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("outset", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("border-style: inset solid dotted outset; ", emptyStyleDecl.getCssText());
		assertEquals("border-style:inset solid dotted outset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-style: var(--mystyle) solid dotted outset; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("border-style: var(--mystyle) solid dotted outset; ", emptyStyleDecl.getCssText());
		assertEquals("border-style:var(--mystyle) solid dotted outset;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderStyleBorderLeft() {
		emptyStyleDecl.setCssText(
				"border-right-style: solid; border-bottom-style: solid; border-style: none; border-left: 0;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("border-style: none; border-left: 0; ", emptyStyleDecl.getCssText());
		assertEquals("border-style:none;border-left:0;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderWidth() {
		emptyStyleDecl.setCssText("border-top-width: 5px; border-width: thick; ");
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: thick; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:thick;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 0; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 0; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:0;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 2px; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 2px; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:2px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 2px 8em; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 2px 8em; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:2px 8em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 2px thick; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 2px thick; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:2px thick;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 2px 8em 4pt; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 2px 8em 4pt; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:2px 8em 4pt;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 2px 8em thick; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 2px 8em thick; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:2px 8em thick;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: 2px 8em 4pt 5px; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("8em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("5px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: 2px 8em 4pt 5px; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:2px 8em 4pt 5px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: calc(e*3px); ");
		assertEquals("calc(2.7182817*3px)", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("calc(2.7182817*3px)", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("calc(2.7182817*3px)", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("calc(2.7182817*3px)", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: calc(2.7182817*3px); ", emptyStyleDecl.getCssText());
		assertEquals("border-width:calc(2.7182817*3px);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-width: var(--myborderwidth) 8em 4pt 5px; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("border-width: var(--myborderwidth) 8em 4pt 5px; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:var(--myborderwidth) 8em 4pt 5px;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderWidth2() {
		emptyStyleDecl.setCssText(
				"border-width: thick; border-top-width: 5px; border-right-width: 4px; border-bottom-width: 3px; border-left-width: 2px;");
		assertEquals("5px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals(
				"border-top-width: 5px; border-right-width: 4px; border-bottom-width: 3px; border-left-width: 2px; ",
				emptyStyleDecl.getCssText());
		assertEquals("border-top-width:5px;border-right-width:4px;border-bottom-width:3px;border-left-width:2px",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderCombined() {
		emptyStyleDecl.setCssText(
				"border-style:none;border-width:0;border-right-style:solid;border-right-width:3px;border-bottom-style:solid;border-bottom-width:2px;border-top:0 blue;border-left:0 navy");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-right-style: solid; border-right-width: 3px; border-bottom-style: solid; border-bottom-width: 2px; border-top: 0 blue; border-left: 0 navy; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-right-style:solid;border-right-width:3px;border-bottom-style:solid;border-bottom-width:2px;border-top:0 blue;border-left:0 navy;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
	}

	@Test
	public void testBorderCombined2() {
		emptyStyleDecl.setCssText(
				"border-style:none;border-width:0;border-right-style:solid;border-right-width:3px;border-bottom-style:solid;border-top:0 blue;border-left:0 navy");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-width: 0; border-right-style: solid; border-right-width: 3px; border-bottom-style: solid; border-top: 0 blue; border-left: 0 navy; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-width:0;border-right-style:solid;border-right-width:3px;border-bottom-style:solid;border-top:0 blue;border-left:0 navy;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		//
		assertEquals("3px", emptyStyleDecl.removeProperty("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-right-width"));
	}

	@Test
	public void testBorderCombined2a() {
		emptyStyleDecl.setCssText(
				"border-style:none;border-width:0;border-right-style:solid;border-right-width:3px;border-bottom-style:solid;border-top:1px blue;border-left:2px navy");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-width: 0; border-right-style: solid; border-right-width: 3px; border-bottom-style: solid; border-top: 1px blue; border-left: 2px navy; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-width:0;border-right-style:solid;border-right-width:3px;border-bottom-style:solid;border-top:1px blue;border-left:2px navy;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		//
		assertEquals("3px", emptyStyleDecl.removeProperty("border-right-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("1px blue", emptyStyleDecl.removeProperty("border-top"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
	}

	@Test
	public void testBorderCombined3() {
		emptyStyleDecl.setCssText(
				"border-width:1px;border-bottom-width:3px;border-bottom-style:solid;border-top:0 blue;border-left:0 navy;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-width: 1px; border-bottom-width: 3px; border-bottom-style: solid; border-top: 0 blue; border-left: 0 navy; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-width:1px;border-bottom-width:3px;border-bottom-style:solid;border-top:0 blue;border-left:0 navy;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		//
		assertEquals("3px", emptyStyleDecl.removeProperty("border-bottom-width"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0 blue", emptyStyleDecl.removeProperty("border-top"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
	}

	@Test
	public void testBorderCombined4() {
		emptyStyleDecl.setCssText(
				"border-width:1px;border-right-width:2px;border-bottom-width:3px;border-bottom-style:solid;border-top:0 blue;border-left:0 navy;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-right-width: 2px; border-bottom-width: 3px; border-bottom-style: solid; border-top: 0 blue; border-left: 0 navy; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-right-width:2px;border-bottom-width:3px;border-bottom-style:solid;border-top:0 blue;border-left:0 navy;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		//
		assertEquals("0 blue", emptyStyleDecl.removeProperty("border-top"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-width"));
	}

	@Test
	public void testBorderCombined5() {
		emptyStyleDecl.setCssText(
				"border-width:0;border-top:1px blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-top: 1px blue; border-right: 2px yellow; border-bottom: 3px navy; border-left: 4px orange; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals("border-top:1px blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
	}

	@Test
	public void testBorderCombined6() {
		emptyStyleDecl.setCssText(
				"border-width:0;border-top-color:blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-width: 0; border-top-color: blue; border-right: 2px yellow; border-bottom: 3px navy; border-left: 4px orange; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-width:0;border-top-color:blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		//
		assertEquals("4px orange", emptyStyleDecl.removeProperty("border-left"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
	}

	@Test
	public void testBorderCombined7() {
		emptyStyleDecl.setCssText(
				"border:0;border-top-color:blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border: 0; border-top-color: blue; border-right: 2px yellow; border-bottom: 3px navy; border-left: 4px orange; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border:0;border-top-color:blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		//
		assertEquals("2px yellow", emptyStyleDecl.removeProperty("border-right"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("4px orange", emptyStyleDecl.removeProperty("border-left"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
	}

	@Test
	public void testBorderCombined8() {
		emptyStyleDecl.setCssText(
				"border:0;border-top:blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border: 0; border-top: blue; border-right: 2px yellow; border-bottom: 3px navy; border-left: 4px orange; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals("border:0;border-top:blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		//
		assertEquals("2px yellow", emptyStyleDecl.removeProperty("border-right"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("4px orange", emptyStyleDecl.removeProperty("border-left"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
	}

	@Test
	public void testBorderCombined9() {
		emptyStyleDecl.setCssText(
				"border-style:none;border-width:0;border-right-style:solid;border-right-width:2px;border-bottom-style:solid;border-bottom-width:2px;border-top:0;border-left:0");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-right-style: solid; border-right-width: 2px; border-bottom-style: solid; border-bottom-width: 2px; border-top: 0; border-left: 0; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-right-style:solid;border-right-width:2px;border-bottom-style:solid;border-bottom-width:2px;border-top:0;border-left:0;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("solid", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-left-color"));
	}

	@Test
	public void testBorderCombined10() {
		emptyStyleDecl.setCssText(
				"border-width:0;border-style:solid;border-right-width:3em;border-top:1px;border-right:2px;border-bottom:3px;border-left:4px");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals("border-top: 1px; border-right: 2px; border-bottom: 3px; border-left: 4px; ", result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals("border-top:1px;border-right:2px;border-bottom:3px;border-left:4px;", result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-left-color"));
	}

	@Test
	public void testBorderCombined11() {
		emptyStyleDecl.setCssText(
				"border-width:0;border-right-width:2em;border-bottom-width:3em;border-left-width:4em;border-top:1px;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4em", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals("border-right-width: 2em; border-bottom-width: 3em; border-left-width: 4em; border-top: 1px; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4em", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals("border-right-width:2em;border-bottom-width:3em;border-left-width:4em;border-top:1px;", result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2em", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4em", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("currentcolor", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-color"));
	}

	@Test
	public void testBorderCombined12() {
		emptyStyleDecl.setCssText("border-top:1px;border-color:yellow;border-top-style:dotted;border-top-color:blue;");
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-left-color"));
		String result = emptyStyleDecl.getCssText();
		assertEquals("border-top: 1px; border-color: yellow; border-top-style: dotted; border-top-color: blue; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-left-color"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals("border-top:1px;border-color:yellow;border-top-style:dotted;border-top-color:blue", result);
		emptyStyleDecl.setCssText(result);
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-left-color"));
		//
		assertEquals("dotted", emptyStyleDecl.removeProperty("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("blue", emptyStyleDecl.removeProperty("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-top-color"));
		emptyStyleDecl.setProperty("border-top-width", "2px", null);
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.removeProperty("border-top-width"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		emptyStyleDecl.setProperty("border-top-color", "orange", null);
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("orange", emptyStyleDecl.removeProperty("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-top-color"));
	}

	@Test
	public void testBorderCombinedBorderImage() {
		emptyStyleDecl.setCssText(
				"border:0;border-image:none;border-top:1px blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-image: none; border-top: 1px blue; border-right: 2px yellow; border-bottom: 3px navy; border-left: 4px orange; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-image:none;border-top:1px blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
	}

	@Test
	public void testBorderCombinedBorderImage2() {
		emptyStyleDecl.setCssText(
				"border:0;border-image:url('foo.png');border-top:1px blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		String result = emptyStyleDecl.getCssText();
		assertEquals(
				"border-image: url('foo.png'); border-top: 1px blue; border-right: 2px yellow; border-bottom: 3px navy; border-left: 4px orange; ",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		result = emptyStyleDecl.getMinifiedCssText();
		assertEquals(
				"border-image:url('foo.png');border-top:1px blue;border-right:2px yellow;border-bottom:3px navy;border-left:4px orange;",
				result);
		emptyStyleDecl.setCssText(result);
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-top-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-right-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-bottom-style"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-left-style"));
		assertEquals("1px", emptyStyleDecl.getPropertyValue("border-top-width"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("border-right-width"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("border-bottom-width"));
		assertEquals("4px", emptyStyleDecl.getPropertyValue("border-left-width"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-right-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
	}

	@Test
	public void testBorderRadius() {
		emptyStyleDecl.setCssText("border-radius: 7em; ");
		assertEquals("7em", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("7em", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("7em", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("7em", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-top-left-radius").isSubproperty());
		assertEquals("border-radius: 7em; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:7em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: 7.5em 5em; ");
		assertEquals("7.5em", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("5em", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("7.5em", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("5em", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: 7.5em 5em; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:7.5em 5em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: 2em 1em 4em / 0.5em 3em ");
		assertEquals("2em 0.5em", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("1em 3em", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("4em 0.5em", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("1em 3em", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: 2em 1em 4em / 0.5em 3em; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:2em 1em 4em/.5em 3em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: 2em 1em 4em /.5em 3em ");
		assertEquals("2em 0.5em", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("1em 3em", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("4em 0.5em", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("1em 3em", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: 2em 1em 4em / 0.5em 3em; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:2em 1em 4em/.5em 3em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: 2em 1em 4em 3em/ 0.5em 2em 1em");
		assertEquals("2em 0.5em", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("1em 2em", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("4em 1em", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("3em 2em", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: 2em 1em 4em 3em / 0.5em 2em 1em; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:2em 1em 4em 3em/.5em 2em 1em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: 0; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: 0; ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:0;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: var(--my-theme-border-radius); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: var(--my-theme-border-radius); ", emptyStyleDecl.getCssText());
		assertEquals("border-radius:var(--my-theme-border-radius);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-radius: var(--my-tlbr-border-radius) var(--my-trbl-border-radius); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals("border-radius: var(--my-tlbr-border-radius) var(--my-trbl-border-radius); ",
				emptyStyleDecl.getCssText());
		assertEquals("border-radius:var(--my-tlbr-border-radius) var(--my-trbl-border-radius);",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText(
				"border-radius: var(--my-tl-border-radius) var(--my-trbl1-border-radius) var(--my-bbr1-border-radius) / var(--my-bbr2-border-radius) var(--my-trbl2-border-radius)");
		assertEquals("",
				emptyStyleDecl.getPropertyValue("border-top-left-radius"));
		assertEquals("",
				emptyStyleDecl.getPropertyValue("border-top-right-radius"));
		assertEquals("",
				emptyStyleDecl.getPropertyValue("border-bottom-right-radius"));
		assertEquals("",
				emptyStyleDecl.getPropertyValue("border-bottom-left-radius"));
		assertEquals(
				"border-radius: var(--my-tl-border-radius) var(--my-trbl1-border-radius) var(--my-bbr1-border-radius)/var(--my-bbr2-border-radius) var(--my-trbl2-border-radius); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"border-radius:var(--my-tl-border-radius) var(--my-trbl1-border-radius) var(--my-bbr1-border-radius)/var(--my-bbr2-border-radius) var(--my-trbl2-border-radius);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testMargin() {
		emptyStyleDecl.setCssText("margin-top: 3pt; margin: 10px; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("margin-top").isSubproperty());
		assertEquals("margin: 10px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px; margin-top: 3px; ");
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px; margin-top: 3px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin-top:3px", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px; margin-bottom: 3px; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px; margin-bottom: 3px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin-bottom:3px", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px; margin-left: 3px; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px; margin-left: 3px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin-left:3px", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px; margin-right: 3px; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px; margin-right: 3px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin-right:3px", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px; margin-top: 3px ! important; ");
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("margin-top"));
		assertEquals("margin: 10px; margin-top: 3px ! important; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin-top:3px!important", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin-top: 3px ! important; margin: 10px; ");
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("margin-top"));
		assertEquals("margin-top: 3px ! important; margin: 10px; ", emptyStyleDecl.getCssText());
		assertEquals("margin-top:3px!important;margin:10px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px; margin-top: 2px; margin-bottom: 3px; ");
		assertEquals("2px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px; margin-top: 2px; margin-bottom: 3px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px;margin-top:2px;margin-bottom:3px", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px 4pt; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px 4pt; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 4pt;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px 4pt 3px; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px 4pt 3px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 4pt 3px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px 4pt 3px 5px; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("4pt", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("5px", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: 10px 4pt 3px 5px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:10px 4pt 3px 5px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("margin: 10px inherit 3px auto; ");
		assertEquals("10px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("3px", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin-top: 10px; margin-right: inherit; margin-bottom: 3px; margin-left: auto; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin-top:10px;margin-right:inherit;margin-bottom:3px;margin-left:auto",
				emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("margin: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("margin:inherit;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("margin: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: unset; ", emptyStyleDecl.getCssText());
		assertEquals("margin:unset;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("margin: initial; ");
		assertEquals("initial", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: initial; ", emptyStyleDecl.getCssText());
		assertEquals("margin:initial;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("margin: revert; ");
		assertEquals("revert", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("revert", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("revert", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("revert", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: revert; ", emptyStyleDecl.getCssText());
		assertEquals("margin:revert;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasWarnings());
		//
		emptyStyleDecl.setCssText("margin: revert initial; ");
		assertEquals("revert", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("revert", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin-top: revert; margin-right: initial; margin-bottom: revert; margin-left: initial; ",
				emptyStyleDecl.getCssText());
		assertEquals("margin-top:revert;margin-right:initial;margin-bottom:revert;margin-left:initial",
				emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testMarginVar() {
		emptyStyleDecl.setCssText("margin: var(--mytopmargin) 4pt 3px 5px; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: var(--mytopmargin) 4pt 3px 5px; ", emptyStyleDecl.getCssText());
		assertEquals("margin:var(--mytopmargin) 4pt 3px 5px;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("margin: calc((var(--foo,1px))*-1)");
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin: calc((var(--foo, 1px))*-1); ", emptyStyleDecl.getCssText());
		assertEquals("margin:calc((var(--foo,1px))*-1);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testMarginError() {
		emptyStyleDecl.setCssText("margin-top:1px;margin: 1px hello; ");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin-top: 1px; ", emptyStyleDecl.getCssText());
		assertEquals("margin-top:1px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testMarginError2() {
		emptyStyleDecl.setCssText("margin-top:1px;margin: revert hello; ");
		assertEquals("1px", emptyStyleDecl.getPropertyValue("margin-top"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-right"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-bottom"));
		assertEquals("", emptyStyleDecl.getPropertyValue("margin-left"));
		assertEquals("margin-top: 1px; ", emptyStyleDecl.getCssText());
		assertEquals("margin-top:1px", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasWarnings());
	}

	@Test
	public void testPadding() {
		emptyStyleDecl.setCssText("padding: 1em 2em");
		assertEquals("1em", emptyStyleDecl.getPropertyValue("padding-top"));
		assertEquals("2em", emptyStyleDecl.getPropertyValue("padding-right"));
		assertEquals("1em", emptyStyleDecl.getPropertyValue("padding-bottom"));
		assertEquals("2em", emptyStyleDecl.getPropertyValue("padding-left"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("padding-top").isSubproperty());
		assertEquals("padding: 1em 2em; ", emptyStyleDecl.getCssText());
		assertEquals("padding:1em 2em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("padding: 2em 3em 1em");
		assertEquals("2em", emptyStyleDecl.getPropertyValue("padding-top"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("padding-right"));
		assertEquals("1em", emptyStyleDecl.getPropertyValue("padding-bottom"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("padding-left"));
		assertEquals("padding: 2em 3em 1em; ", emptyStyleDecl.getCssText());
		assertEquals("padding:2em 3em 1em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("padding: var(--mypaddingtop) 2em 3em 1em");
		assertEquals("", emptyStyleDecl.getPropertyValue("padding-top"));
		assertEquals("", emptyStyleDecl.getPropertyValue("padding-right"));
		assertEquals("", emptyStyleDecl.getPropertyValue("padding-bottom"));
		assertEquals("", emptyStyleDecl.getPropertyValue("padding-left"));
		assertEquals("padding: var(--mypaddingtop) 2em 3em 1em; ", emptyStyleDecl.getCssText());
		assertEquals("padding:var(--mypaddingtop) 2em 3em 1em;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("padding: 2em 3em inherit");
		assertEquals("2em", emptyStyleDecl.getPropertyValue("padding-top"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("padding-right"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("padding-bottom"));
		assertEquals("3em", emptyStyleDecl.getPropertyValue("padding-left"));
		assertEquals("padding-top: 2em; padding-right: 3em; padding-bottom: inherit; padding-left: 3em; ",
				emptyStyleDecl.getCssText());
		assertEquals("padding-top:2em;padding-right:3em;padding-bottom:inherit;padding-left:3em",
				emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("padding: initial; ");
		assertEquals("initial", emptyStyleDecl.getPropertyValue("padding-top"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("padding-right"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("padding-bottom"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("padding-left"));
		assertEquals("padding: initial; ", emptyStyleDecl.getCssText());
		assertEquals("padding:initial;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("padding: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("padding-top"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("padding-right"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("padding-bottom"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("padding-left"));
		assertEquals("padding: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("padding:inherit;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundLayers() {
		emptyStyleDecl.setCssText(
				"background: url(a.png) top left no-repeat,url(b.png) center / 100% 100% no-repeat,url(c.png) white;");
		assertEquals("url('a.png'), url('b.png'), url('c.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("top left, center, 0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		// W3C (http://www.w3.org/TR/css3-background/) example 18:
		assertEquals("no-repeat, no-repeat, repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("border-box, border-box, border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("padding-box, padding-box, padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("auto auto, 100% 100%, auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("scroll, scroll, scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("white", emptyStyleDecl.getPropertyValue("background-color"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("background-repeat").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("background-clip").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("background-color").isSubproperty());
		assertEquals(
				"background: url('a.png') top left no-repeat, url('b.png') center / 100% 100% no-repeat, url('c.png') white; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:url('a.png') top left no-repeat,url('b.png') center/100% 100% no-repeat,url('c.png') white;",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(
				((DefaultStyleDeclarationErrorHandler) emptyStyleDecl.getStyleDeclarationErrorHandler()).hasErrors());
	}

	@Test
	public void testBackgroundInherit() {
		emptyStyleDecl.setCssText(
				"background: url(a.png) top left no-repeat,url(b.png) center / 100% 100% no-repeat,url(c.png) inherit;");
		assertEquals("url('a.png'), url('b.png'), inherit", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("top left, center, inherit", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("no-repeat, no-repeat, inherit", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("border-box, border-box, inherit", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("padding-box, padding-box, inherit", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("auto auto, 100% 100%, inherit", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("scroll, scroll, inherit", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals(
				"background: url('a.png') top left no-repeat, url('b.png') center / 100% 100% no-repeat, inherit; ",
				emptyStyleDecl.getCssText());
		assertEquals("background:url('a.png') top left no-repeat,url('b.png') center/100% 100% no-repeat,inherit;",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(
				((DefaultStyleDeclarationErrorHandler) emptyStyleDecl.getStyleDeclarationErrorHandler()).hasErrors());
	}

	@Test
	public void testBackground() {
		emptyStyleDecl.setCssText("background: url('bkg.png') 40% / 10em gray round fixed border-box;");
		assertEquals("url('bkg.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("40%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("10em", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("gray", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("fixed", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: url('bkg.png') 40% / 10em gray round fixed border-box; ",
				emptyStyleDecl.getCssText());
		assertEquals("background:url('bkg.png') 40%/10em gray round fixed border-box;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackground2() {
		emptyStyleDecl.setCssText("background: url('bkg.png') right center gray round fixed border-box;");
		assertEquals("url('bkg.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("right center", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("gray", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("fixed", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: url('bkg.png') right center gray round fixed border-box; ",
				emptyStyleDecl.getCssText());
		assertEquals("background:url('bkg.png') right center gray round fixed border-box;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackground3() {
		emptyStyleDecl.setCssText("background:url('bkg.png') 50%/cover no-repeat;");
		assertEquals("url('bkg.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("50%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("cover", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("no-repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: url('bkg.png') 50% / cover no-repeat; ", emptyStyleDecl.getCssText());
		assertEquals("background:url('bkg.png') 50%/cover no-repeat;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackground4() {
		emptyStyleDecl.setCssText(
				"background:radial-gradient(    closest-side,   rgba(32, 45, 46, 0),  #da212e)  , url('//example.com/img/image.jpg');");
		assertEquals("radial-gradient(closest-side, rgba(32, 45, 46, 0), #da212e), url('//example.com/img/image.jpg')",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("radial-gradient(closest-side,rgba(32,45,46,0),#da212e),url('//example.com/img/image.jpg')",
				emptyStyleDecl.getPropertyCSSValue("background-image").getMinifiedCssText("background-image"));
		assertEquals(
				"background: radial-gradient(closest-side, rgba(32, 45, 46, 0), #da212e), url('//example.com/img/image.jpg'); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:radial-gradient(closest-side,rgba(32,45,46,0),#da212e),url('//example.com/img/image.jpg');",
				emptyStyleDecl.getMinifiedCssText());
		assertEquals("0% 0%, 0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto, auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("padding-box, padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box, border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll, scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat, repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
	}

	@Test
	public void testBackground5() {
		emptyStyleDecl.setCssText("background:url(//www.example.com/dir/image.png);");
		assertEquals("url('//www.example.com/dir/image.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: url('//www.example.com/dir/image.png'); ", emptyStyleDecl.getCssText());
		assertEquals("background:url('//www.example.com/dir/image.png');", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundVar() {
		emptyStyleDecl.setCssText("background: var(--My-Image);");
		assertEquals("", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: var(--My-Image); ", emptyStyleDecl.getCssText());
		assertEquals("background:var(--My-Image);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundVar2() {
		emptyStyleDecl.setCssText("background: var(--Myimage) 40% / 10em gray round fixed border-box;");
		assertEquals("", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: var(--Myimage) 40%/10em gray round fixed border-box; ",
				emptyStyleDecl.getCssText());
		assertEquals("background:var(--Myimage) 40%/10em gray round fixed border-box;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundBad() {
		emptyStyleDecl.setCssText("background: url('bkg.png') 40% / 10em gray round fixed border-box, url('foo.png');");
		assertEquals("", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getParentRule().getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBackgroundImage() {
		emptyStyleDecl.setCssText("background: url('bkg.png');");
		assertEquals("url('bkg.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		StyleValue val = emptyStyleDecl.getPropertyCSSValue("background-size");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("auto", list.item(0).getCssText());
		assertEquals("auto", list.item(1).getCssText());
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("background: url('bkg.png'); ", emptyStyleDecl.getCssText());
		assertEquals("background:url('bkg.png');", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageColor() {
		emptyStyleDecl.setCssText("background: url('bkg.png')    orange;");
		assertEquals("url('bkg.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		StyleValue val = emptyStyleDecl.getPropertyCSSValue("background-size");
		assertNotNull(val);
		assertEquals(CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("auto", list.item(0).getCssText());
		assertEquals("auto", list.item(1).getCssText());
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("orange", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("background: url('bkg.png') orange; ", emptyStyleDecl.getCssText());
		assertEquals("background:url('bkg.png') orange;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImage2() {
		emptyStyleDecl.setCssText("background:0 0,url(../img/foo.png) no-repeat;");
		assertEquals("none, url('../img/foo.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("0 0, 0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto, auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("padding-box, padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box, border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll, scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat, no-repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("background: 0 0, url('../img/foo.png') no-repeat; ", emptyStyleDecl.getCssText());
		assertEquals("background:0 0,url('../img/foo.png') no-repeat;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImage3() {
		emptyStyleDecl.setCssText(
				"background: #fff; background-image: linear-gradient(45deg, rgb(150 50 50 / 0) 50%, #fc2f2f 50%), linear-gradient(135deg, #fecf05 50%, rgb(0 0 0 / 0) 50%); background-size: 5px 5px, 5px 5px;");
		assertEquals(
				"linear-gradient(45deg, rgb(150 50 50 / 0) 50%, #fc2f2f 50%), linear-gradient(135deg, #fecf05 50%, rgb(0 0 0 / 0) 50%)",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("5px 5px, 5px 5px", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("#fff", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals(
				"background: #fff; background-image: linear-gradient(45deg, rgb(150 50 50 / 0) 50%, #fc2f2f 50%), linear-gradient(135deg, #fecf05 50%, rgb(0 0 0 / 0) 50%); background-size: 5px 5px, 5px 5px; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:#fff;background-image:linear-gradient(45deg,rgb(150 50 50/0) 50%,#fc2f2f 50%),linear-gradient(135deg,#fecf05 50%,rgb(0 0 0/0) 50%);background-size:5px 5px,5px 5px",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageMisleadingColor() {
		emptyStyleDecl.setCssText("background:url(../img/foo.png) no-repeat; background-color: none");
		assertEquals("url('../img/foo.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("no-repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("background: url('../img/foo.png') no-repeat; background-color: none; ",
				emptyStyleDecl.getCssText());
		assertEquals("background:url('../img/foo.png') no-repeat;background-color:none",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageGradient() {
		emptyStyleDecl.setCssText("background:linear-gradient(to left top, rgb(90, 102, 167), rgb(101, 50, 135));");
		assertEquals("linear-gradient(to left top, #5a66a7, #653287)",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("background: linear-gradient(to left top, #5a66a7, #653287); ", emptyStyleDecl.getCssText());
		assertEquals("background:linear-gradient(to left top,#5a66a7,#653287);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageGradient2() {
		emptyStyleDecl.setCssText(
				"background:linear-gradient(to right,rgb(66 103 178/0),#577fbc,rgb(66 103 178/0)) 0% 0%/1016px auto;");
		assertEquals("linear-gradient(to right, rgb(66 103 178 / 0), #577fbc, rgb(66 103 178 / 0))",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals(
				"background: linear-gradient(to right, rgb(66 103 178 / 0), #577fbc, rgb(66 103 178 / 0)) 0% 0% / 1016px auto; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:linear-gradient(to right,rgb(66 103 178/0),#577fbc,rgb(66 103 178/0)) 0% 0%/1016px auto;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageGradient3() {
		emptyStyleDecl.setCssText("background: linear-gradient(to bottom, rgb(255 255 255/0) 0%, #fff 100%);");
		assertEquals("linear-gradient(to bottom, rgb(255 255 255 / 0) 0%, #fff 100%)",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("background: linear-gradient(to bottom, rgb(255 255 255 / 0) 0%, #fff 100%); ",
				emptyStyleDecl.getCssText());
		assertEquals("background:linear-gradient(to bottom,rgb(255 255 255/0) 0%,#fff 100%);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageGradientCustom() {
		emptyStyleDecl.setCssText("background: -webkit-radial-gradient(top center);");
		assertEquals("-webkit-radial-gradient(top center)", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("background: -webkit-radial-gradient(top center); ", emptyStyleDecl.getCssText());
		assertEquals("background:-webkit-radial-gradient(top center);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundImageDataURL() {
		emptyStyleDecl.setCssText(
				"background: #fbd url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') no-repeat right 8px;");
		assertEquals("url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA')",
				emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("#fbd", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals(
				"background: #fbd url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') no-repeat right 8px; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:#fbd url('data:image/png;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAA') no-repeat right 8px;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundLayered() {
		emptyStyleDecl.setCssText(
				"background:inherit, url(../img/foo.png) bottom / cover no-repeat fixed padding-box content-box, olive;");
		assertEquals("inherit, url('../img/foo.png'), none", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("inherit, bottom, 0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("inherit, cover, auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("olive", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("inherit, padding-box, padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("inherit, content-box, border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("inherit, fixed, scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("inherit, no-repeat, repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals(
				"background: inherit, url('../img/foo.png') bottom / cover no-repeat fixed padding-box content-box, olive; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:inherit,url('../img/foo.png') bottom/cover no-repeat fixed padding-box content-box,olive;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundLayered2() {
		emptyStyleDecl.setCssText(
				"background:inherit, url(../img/foo.png) bottom / cover no-repeat fixed padding-box content-box, unset;");
		assertEquals("inherit, url('../img/foo.png'), unset", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("inherit, bottom, unset", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("inherit, cover, unset", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("inherit, padding-box, unset", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("inherit, content-box, unset", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("inherit, fixed, unset", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("inherit, no-repeat, unset", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals(
				"background: inherit, url('../img/foo.png') bottom / cover no-repeat fixed padding-box content-box, unset; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"background:inherit,url('../img/foo.png') bottom/cover no-repeat fixed padding-box content-box,unset;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBackgroundBadLayer() {
		emptyStyleDecl.setCssText("background:foo,url(../img/foo.png) no-repeat;");
		assertEquals("", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBackgroundImagePosition() {
		emptyStyleDecl.setCssText("background: url('bkg.png') 40%;");
		assertEquals("url('bkg.png')", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("40%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("padding-box", emptyStyleDecl.getPropertyValue("background-origin"));
		assertEquals("border-box", emptyStyleDecl.getPropertyValue("background-clip"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("background: url('bkg.png') 40%; ", emptyStyleDecl.getCssText());
		assertEquals("background:url('bkg.png') 40%;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBackgroundColor() {
		emptyStyleDecl.setCssText("background: gray;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("0% 0%", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("gray", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("gray", emptyStyleDecl.getPropertyValue("background"));
		assertEquals("background: gray; ", emptyStyleDecl.getCssText());
		assertEquals("background:gray;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBackgroundImageNone() {
		emptyStyleDecl.setCssText("background:transparent none no-repeat scroll center center");
		assertEquals("none", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("center center", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("scroll", emptyStyleDecl.getPropertyValue("background-attachment"));
		assertEquals("auto auto", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("transparent", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("no-repeat", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("transparent none no-repeat scroll center center", emptyStyleDecl.getPropertyValue("background"));
		assertEquals("background: transparent none no-repeat scroll center center; ", emptyStyleDecl.getCssText());
		assertEquals("background:transparent none no-repeat scroll center center;",
				emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBackgroundColorBad() {
		emptyStyleDecl.setCssText("background: gray, yellow;");
		assertEquals("", emptyStyleDecl.getPropertyValue("background-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-size"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background-repeat"));
		assertEquals("", emptyStyleDecl.getPropertyValue("background"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderImage() {
		emptyStyleDecl.setCssText("border-image: none;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("100%", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-image-outset"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-image-source").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-image-repeat").isSubproperty());
		assertEquals("border-image: none; ", emptyStyleDecl.getCssText());
		assertEquals("border-image:none;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderImage2() {
		emptyStyleDecl.setCssText("border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round;");
		assertEquals("url('/img/border.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("25% 30% 12% 20% fill", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals(CssType.LIST,
				emptyStyleDecl.getPropertyCSSValue("border-image-slice").getCssValueType());
		assertEquals("25%",
				((ValueList) emptyStyleDecl.getPropertyCSSValue("border-image-slice")).item(0).getCssText());
		assertEquals("2pt", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("border-image-outset"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-image-source").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("border-image-repeat").isSubproperty());
		assertEquals("border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round; ",
				emptyStyleDecl.getCssText());
		assertEquals("border-image:url('/img/border.png') 25% 30% 12% 20% fill/2pt/1 round;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImage3() {
		emptyStyleDecl.setCssText("border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt 4pt / 1 3 round;");
		assertEquals("url('/img/border.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("25% 30% 12% 20% fill", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals("2pt 4pt", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("1 3", emptyStyleDecl.getPropertyValue("border-image-outset"));
		assertEquals("border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt 4pt / 1 3 round; ",
				emptyStyleDecl.getCssText());
		assertEquals("border-image:url('/img/border.png') 25% 30% 12% 20% fill/2pt 4pt/1 3 round;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImage4() {
		emptyStyleDecl.setCssText("border-image: url('/img/border.png') 25% 30% / 2pt round;");
		assertEquals("url('/img/border.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("25% 30%", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals(CssType.LIST,
				emptyStyleDecl.getPropertyCSSValue("border-image-slice").getCssValueType());
		assertEquals("25%",
				((ValueList) emptyStyleDecl.getPropertyCSSValue("border-image-slice")).item(0).getCssText());
		assertEquals("2pt", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("border-image: url('/img/border.png') 25% 30% / 2pt round; ", emptyStyleDecl.getCssText());
		assertEquals("border-image:url('/img/border.png') 25% 30%/2pt round;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImage5() {
		emptyStyleDecl.setCssText("border-image: url('/img/border.png') 25% 30% / auto round;");
		assertEquals("url('/img/border.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("25% 30%", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals(CssType.LIST,
				emptyStyleDecl.getPropertyCSSValue("border-image-slice").getCssValueType());
		assertEquals("25%",
				((ValueList) emptyStyleDecl.getPropertyCSSValue("border-image-slice")).item(0).getCssText());
		assertEquals("auto", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("border-image: url('/img/border.png') 25% 30% / auto round; ", emptyStyleDecl.getCssText());
		assertEquals("border-image:url('/img/border.png') 25% 30%/auto round;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-image: none 25% 30% / auto round;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("25% 30%", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals(CssType.LIST,
				emptyStyleDecl.getPropertyCSSValue("border-image-slice").getCssValueType());
		assertEquals("25%",
				((ValueList) emptyStyleDecl.getPropertyCSSValue("border-image-slice")).item(0).getCssText());
		assertEquals("auto", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("border-image: none 25% 30% / auto round; ", emptyStyleDecl.getCssText());
		assertEquals("border-image:none 25% 30%/auto round;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImage6() {
		emptyStyleDecl.setCssText(
				"border-image: repeating-conic-gradient(gold, #f06 20deg) 25% 30% 12% 20% fill / 2pt / 1 round;");
		assertEquals("repeating-conic-gradient(gold, #f06 20deg)",
				emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("round", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("25% 30% 12% 20% fill", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals(CssType.LIST,
				emptyStyleDecl.getPropertyCSSValue("border-image-slice").getCssValueType());
		assertEquals("25%",
				((ValueList) emptyStyleDecl.getPropertyCSSValue("border-image-slice")).item(0).getCssText());
		assertEquals("2pt", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("border-image-outset"));
		assertEquals("border-image: repeating-conic-gradient(gold, #f06 20deg) 25% 30% 12% 20% fill / 2pt / 1 round; ",
				emptyStyleDecl.getCssText());
		assertEquals("border-image:repeating-conic-gradient(gold,#f06 20deg) 25% 30% 12% 20% fill/2pt/1 round;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImage7() {
		emptyStyleDecl.setCssText("border-width: 8px; border-image: url('foo.png') 9 repeat;");
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("repeat", emptyStyleDecl.getPropertyValue("border-image-repeat"));
		assertEquals("9", emptyStyleDecl.getPropertyValue("border-image-slice"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("border-image-width"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("border-image-outset"));
		assertEquals("border-width: 8px; border-image: url('foo.png') 9 repeat; ", emptyStyleDecl.getCssText());
		assertEquals("border-width:8px;border-image:url('foo.png') 9 repeat;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testBorderImageBad() {
		emptyStyleDecl.setCssText("border-image-source: url(image.jpg); border-image: foo 25% 3000% 1 round;");
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("url('image.jpg')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("border-image-source: url('image.jpg'); ", emptyStyleDecl.getCssText());
		assertEquals("border-image-source:url('image.jpg')", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderImageBad2() {
		emptyStyleDecl.setCssText(
				"border-image-source: url(image.png);border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 foo;");
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("url('image.png')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("border-image-source: url('image.png'); ", emptyStyleDecl.getCssText());
		assertEquals("border-image-source:url('image.png')", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderImageBadInherit() {
		emptyStyleDecl.setCssText("border-image-source: url(image.jpg); border-image: foo 25% 3000% 1 round inherit;");
		assertEquals(1, emptyStyleDecl.getLength());
		assertEquals("url('image.jpg')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("border-image-source: url('image.jpg'); ", emptyStyleDecl.getCssText());
		assertEquals("border-image-source:url('image.jpg')", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testBorderImageBadKeyword() {
		emptyStyleDecl.setCssText("border-image-source: url(image.jpg); border-image: foo 25% 3000% 1 round unset;");
		assertEquals("url('image.jpg')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("border-image-source: url('image.jpg'); ", emptyStyleDecl.getCssText());
		assertEquals("border-image-source:url('image.jpg')", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("border-image-source: url(image.jpg); border-image: foo 25% 3000% 1 round initial;");
		assertEquals("url('image.jpg')", emptyStyleDecl.getPropertyValue("border-image-source"));
		assertEquals("border-image-source: url('image.jpg'); ", emptyStyleDecl.getCssText());
		assertEquals("border-image-source:url('image.jpg')", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testTextDecoration() {
		emptyStyleDecl.setCssText("text-decoration-line:underline; text-decoration: dotted overline;");
		assertEquals(3, emptyStyleDecl.getLength());
		assertEquals("overline", emptyStyleDecl.getPropertyValue("text-decoration-line"));
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("text-decoration-style"));
		assertEquals("dotted overline", emptyStyleDecl.getPropertyValue("text-decoration"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("text-decoration-line").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("text-decoration-style").isSubproperty());
		assertEquals("text-decoration: dotted overline; ", emptyStyleDecl.getCssText());
		assertEquals("text-decoration:dotted overline;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testTextDecoration2() {
		emptyStyleDecl.setCssText("text-decoration: dotted;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("text-decoration-line"));
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("text-decoration-style"));
		assertEquals(3, emptyStyleDecl.getLength());
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("text-decoration"));
		assertEquals("text-decoration: dotted; ", emptyStyleDecl.getCssText());
		assertEquals("text-decoration:dotted;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testTextDecoration3() {
		emptyStyleDecl.setCssText("text-decoration: dotted overline yellow;");
		assertEquals("overline", emptyStyleDecl.getPropertyValue("text-decoration-line"));
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("text-decoration-style"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("text-decoration-color"));
		assertEquals(3, emptyStyleDecl.getLength());
		assertEquals("dotted overline yellow", emptyStyleDecl.getPropertyValue("text-decoration"));
		assertEquals("text-decoration: dotted overline yellow; ", emptyStyleDecl.getCssText());
		assertEquals("text-decoration:dotted overline yellow;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransition() {
		emptyStyleDecl.setCssText("transition: 0s;");
		assertEquals("ease", emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("all", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("0s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals("transition: 0s; ", emptyStyleDecl.getCssText());
		assertEquals("transition:0s;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("transition-delay: 30s; transition: background-color 1s linear 2s;");
		assertEquals("linear", emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("1s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("2s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals("background-color", emptyStyleDecl.getPropertyValue("transition-property"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-property").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-timing-function").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-duration").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-delay").isSubproperty());
		assertEquals("transition: background-color 1s linear 2s; ", emptyStyleDecl.getCssText());
		assertEquals("transition:background-color 1s linear 2s;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("transition: background-color 1s linear 2s, opacity 4s;");
		assertEquals("background-color, opacity", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("linear, ease", emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("1s, 4s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("2s, 0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-property").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-timing-function").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-duration").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("transition-delay").isSubproperty());
		assertEquals("transition: background-color 1s linear 2s, opacity 4s; ", emptyStyleDecl.getCssText());
		assertEquals("transition:background-color 1s linear 2s,opacity 4s;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("transition: background-color 1s cubic-bezier(0.15, 0.0, 0.5, 1.0) 2s;");
		assertEquals("cubic-bezier(0.15, 0, 0.5, 1.0)", emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("transition: background-color 1s cubic-bezier(0.15, 0, 0.5, 1.0) 2s; ",
				emptyStyleDecl.getCssText());
		assertEquals("transition:background-color 1s cubic-bezier(.15,0,.5,1) 2s;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionMultiple() {
		emptyStyleDecl.setCssText(
				"transition-delay: 30s; transition: background-color 1s linear 2s, opacity 10s 1s, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1);");
		assertEquals("background-color, opacity, width, height",
				emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("linear, ease, ease-in, cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("1s, 10s, 3s, 5s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("2s, 1s, 0s, 0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: background-color 1s linear 2s, opacity 10s 1s, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:background-color 1s linear 2s,opacity 10s 1s,width 3s ease-in,height 5s cubic-bezier(.33,.1,.5,1);",
				emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText(
				"transition: background-color 1s linear 2s, opacity 10s, width ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1);");
		assertEquals("background-color, opacity, width, height",
				emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("linear, ease, ease-in, cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("1s, 10s, 0s, 5s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("2s, 0s, 0s, 0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: background-color 1s linear 2s, opacity 10s, width ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:background-color 1s linear 2s,opacity 10s,width ease-in,height 5s cubic-bezier(.33,.1,.5,1);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionMultipleInherit() {
		emptyStyleDecl.setCssText(
				"transition-delay: 30s; transition: background-color 1s linear 2s, inherit, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1);");
		assertEquals("background-color, inherit, width, height",
				emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("linear, inherit, ease-in, cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("1s, inherit, 3s, 5s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("2s, inherit, 0s, 0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: background-color 1s linear 2s, inherit, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:background-color 1s linear 2s,inherit,width 3s ease-in,height 5s cubic-bezier(.33,.1,.5,1);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionMultipleInherit2() {
		emptyStyleDecl.setCssText(
				"transition-delay: 30s; transition: unset, background-color 1s linear 2s, inherit, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1);");
		assertEquals("unset, background-color, inherit, width, height",
				emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("unset, linear, inherit, ease-in, cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("unset, 1s, inherit, 3s, 5s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("unset, 2s, inherit, 0s, 0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: unset, background-color 1s linear 2s, inherit, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:unset,background-color 1s linear 2s,inherit,width 3s ease-in,height 5s cubic-bezier(.33,.1,.5,1);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionMultipleUnset() {
		emptyStyleDecl.setCssText(
				"transition-delay: 30s; transition: background-color 1s linear 2s, unset, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1);");
		assertEquals("background-color, unset, width, height", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("linear, unset, ease-in, cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("1s, unset, 3s, 5s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("2s, unset, 0s, 0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: background-color 1s linear 2s, unset, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:background-color 1s linear 2s,unset,width 3s ease-in,height 5s cubic-bezier(.33,.1,.5,1);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionAll() {
		emptyStyleDecl.setCssText(
				"transition-delay: 30s; transition: background-color 1s linear 2s, opacity 10s, all 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1) 1s;");
		assertEquals("background-color, opacity, all, height", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("ease-in, ease-in, ease-in, cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("3s, 3s, 3s, 5s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("0s, 0s, 0s, 1s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: background-color 1s linear 2s, opacity 10s, all 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1) 1s; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:background-color 1s linear 2s,opacity 10s,all 3s ease-in,height 5s cubic-bezier(.33,.1,.5,1) 1s;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionAll2() {
		emptyStyleDecl.setCssText(
				"transition: background-color 1s linear 2s, opacity 10s 3s, width 3s ease-in, all 8s 1s cubic-bezier(0.33, 0.1, 0.5, 1);");
		assertEquals("background-color, opacity, width, all", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals(
				"cubic-bezier(0.33, 0.1, 0.5, 1), cubic-bezier(0.33, 0.1, 0.5, 1), cubic-bezier(0.33, 0.1, 0.5, 1), cubic-bezier(0.33, 0.1, 0.5, 1)",
				emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("8s, 8s, 8s, 8s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("1s, 1s, 1s, 1s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals(
				"transition: background-color 1s linear 2s, opacity 10s 3s, width 3s ease-in, all 8s 1s cubic-bezier(0.33, 0.1, 0.5, 1); ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"transition:background-color 1s linear 2s,opacity 10s 3s,width 3s ease-in,all 8s 1s cubic-bezier(.33,.1,.5,1);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransition2() {
		emptyStyleDecl.setCssText("transition: opacity cubic-bezier(0.15, 0, 0.5, 1.0) 0.15s;");
		assertEquals("opacity", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("0.15s", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("0s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals("cubic-bezier(0.15, 0, 0.5, 1.0)", emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("transition: opacity cubic-bezier(0.15, 0, 0.5, 1.0) 0.15s; ", emptyStyleDecl.getCssText());
		assertEquals("transition:opacity cubic-bezier(.15,0,.5,1) .15s;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testTransitionInvalid() {
		emptyStyleDecl.setCssText("transition: opacity cubic-bezier(0.15, 0, 0.5, 1.0) 0.15;");
		assertEquals("", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("", emptyStyleDecl.getPropertyValue("transition-duration"));
		assertEquals("", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals("", emptyStyleDecl.getPropertyValue("transition-timing-function"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testTransitionInvalid2() {
		emptyStyleDecl.setCssText(
				"transition-property: all; transition-delay: 30s; transition: background-color 1s linear 2s, none 10s, width 3s ease-in, height 5s cubic-bezier(0.33, 0.1, 0.5, 1) 1s;");
		assertEquals("all", emptyStyleDecl.getPropertyValue("transition-property"));
		assertEquals("30s", emptyStyleDecl.getPropertyValue("transition-delay"));
		assertEquals("transition-property: all; transition-delay: 30s; ", emptyStyleDecl.getCssText());
		assertEquals("transition-property:all;transition-delay:30s", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void getCssTextDecoration() {
		emptyStyleDecl.setCssText("text-decoration-line:overline; text-decoration: underline; ");
		assertEquals("underline", emptyStyleDecl.getPropertyValue("text-decoration-line"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("text-decoration-line").isSubproperty());
		assertEquals("text-decoration: underline; ", emptyStyleDecl.getCssText());
		assertEquals("text-decoration:underline;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testCue() {
		emptyStyleDecl.setCssText("cue:url('foo.au'); ");
		assertEquals("url('foo.au')", emptyStyleDecl.getPropertyValue("cue-before"));
		assertEquals("url('foo.au')", emptyStyleDecl.getPropertyValue("cue-after"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("cue-before").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("cue-after").isSubproperty());
		assertEquals("cue: url('foo.au'); ", emptyStyleDecl.getCssText());
		assertEquals("cue:url('foo.au');", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("cue:url('foo.au') url(bar.au); ");
		assertEquals("url('foo.au')", emptyStyleDecl.getPropertyValue("cue-before"));
		assertEquals("url('bar.au')", emptyStyleDecl.getPropertyValue("cue-after"));
		assertEquals("cue: url('foo.au') url('bar.au'); ", emptyStyleDecl.getCssText());
		assertEquals("cue:url('foo.au') url('bar.au');", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testCueBad() {
		emptyStyleDecl.setCssText("cue:url('foo.au'),; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("cue-before"));
		assertEquals("", emptyStyleDecl.getPropertyValue("cue-after"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("cue:url('foo.au'), url(bar.au); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("cue-before"));
		assertEquals("", emptyStyleDecl.getPropertyValue("cue-after"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testPause() {
		emptyStyleDecl.setCssText("pause: 24ms; ");
		assertEquals("24ms", emptyStyleDecl.getPropertyValue("pause-before"));
		assertEquals("24ms", emptyStyleDecl.getPropertyValue("pause-after"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("pause-before").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("pause-after").isSubproperty());
		assertEquals("pause: 24ms; ", emptyStyleDecl.getCssText());
		assertEquals("pause:24ms;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("pause: 24ms strong; ");
		assertEquals("24ms", emptyStyleDecl.getPropertyValue("pause-before"));
		assertEquals("strong", emptyStyleDecl.getPropertyValue("pause-after"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("pause-before").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("pause-after").isSubproperty());
		assertEquals("pause: 24ms strong; ", emptyStyleDecl.getCssText());
		assertEquals("pause:24ms strong;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("pause: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("pause-before"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("pause-after"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("pause-before").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("pause-after").isSubproperty());
		assertEquals("pause: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("pause:inherit;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testRest() {
		emptyStyleDecl.setCssText("rest: x-weak; ");
		assertEquals("x-weak", emptyStyleDecl.getPropertyValue("rest-before"));
		assertEquals("x-weak", emptyStyleDecl.getPropertyValue("rest-after"));
		assertEquals("rest: x-weak; ", emptyStyleDecl.getCssText());
		assertEquals("rest:x-weak;", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("rest-before").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("rest-after").isSubproperty());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("rest: x-weak medium; ");
		assertEquals("x-weak", emptyStyleDecl.getPropertyValue("rest-before"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("rest-after"));
		assertEquals("rest: x-weak medium; ", emptyStyleDecl.getCssText());
		assertEquals("rest:x-weak medium;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testColumn() {
		emptyStyleDecl.setCssText("columns: auto; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-width").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-count").isSubproperty());
		assertEquals("columns: auto; ", emptyStyleDecl.getCssText());
		assertEquals("columns:auto;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 36px auto; ");
		assertEquals("36px", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("column-count"));
		assertEquals("columns: 36px auto; ", emptyStyleDecl.getCssText());
		assertEquals("columns:36px auto;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 2 auto; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("2", emptyStyleDecl.getPropertyValue("column-count"));
		assertEquals("columns: 2 auto; ", emptyStyleDecl.getCssText());
		assertEquals("columns:2 auto;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 36px 3; ");
		assertEquals("36px", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-width").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-count").isSubproperty());
		assertEquals("columns: 36px 3; ", emptyStyleDecl.getCssText());
		assertEquals("columns:36px 3;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 36px 3 4; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 3 4; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 36px 2em; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: auto auto auto; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 36px foo; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: -36px; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 0; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: -2; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: 0px; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: inherit unset; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-width").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-count").isSubproperty());
		assertEquals("columns: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("columns:inherit;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("columns: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("column-width"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("column-count"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-width").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-count").isSubproperty());
		assertEquals("columns: unset; ", emptyStyleDecl.getCssText());
		assertEquals("columns:unset;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testFlex() {
		emptyStyleDecl.setCssText("flex: none; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-grow").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-basis").isSubproperty());
		assertEquals("flex: none; ", emptyStyleDecl.getCssText());
		assertEquals("flex:none;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: initial");
		assertEquals("initial", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-grow").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-basis").isSubproperty());
		assertEquals("flex: initial; ", emptyStyleDecl.getCssText());
		assertEquals("flex:initial;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: auto; ");
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: auto; ", emptyStyleDecl.getCssText());
		assertEquals("flex:auto;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 0; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 0; ", emptyStyleDecl.getCssText());
		assertEquals("flex:0;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 2; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2 2; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 2 2; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2 2;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2 2 auto; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-grow").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-shrink").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-basis").isSubproperty());
		assertEquals("flex: 2 2 auto; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2 2 auto;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 1 0; ");
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 1 0; ", emptyStyleDecl.getCssText());
		assertEquals("flex:1 0;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 1 0px; ");
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 1 0px; ", emptyStyleDecl.getCssText());
		assertEquals("flex:1 0px;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2 1em; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("1em", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 2 1em; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2 1em;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2 2 5%; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("5%", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 2 2 5%; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2 2 5%;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 0 0 50%; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-grow").isSubproperty());
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-shrink").isSubproperty());
		assertEquals("50%", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-basis").isSubproperty());
		assertEquals("flex: 0 0 50%; ", emptyStyleDecl.getCssText());
		assertEquals("flex:0 0 50%;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2 2 0px; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 2 2 0px; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2 2 0px;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 2 2 0; ");
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("2", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 2 2 0; ", emptyStyleDecl.getCssText());
		assertEquals("flex:2 2 0;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 0px; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 0px; ", emptyStyleDecl.getCssText());
		assertEquals("flex:0px;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 5%; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("5%", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 5%; ", emptyStyleDecl.getCssText());
		assertEquals("flex:5%;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: content; ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("content", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: content; ", emptyStyleDecl.getCssText());
		assertEquals("flex:content;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 0 0 calc(100.0% - 60.0px / 3); ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("calc(100% - 60px/3)", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 0 0 calc(100% - 60px/3); ", emptyStyleDecl.getCssText());
		assertEquals("flex:0 0 calc(100% - 60px/3);", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 0 0 calc((100.0% - 60.0px) / 3); ");
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("0", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("calc((100% - 60px)/3)", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: 0 0 calc((100% - 60px)/3); ", emptyStyleDecl.getCssText());
		assertEquals("flex:0 0 calc((100% - 60px)/3);", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("flex:inherit;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: 1.2em unset; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		//
		emptyStyleDecl.setCssText("flex: unset unset; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		//
		emptyStyleDecl.setCssText("flex: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("flex: unset; ", emptyStyleDecl.getCssText());
		assertEquals("flex:unset;", emptyStyleDecl.getMinifiedCssText());
		//
		emptyStyleDecl.setCssText("flex: none none; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		//
		emptyStyleDecl.setCssText("flex: 2 3 4; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		//
		emptyStyleDecl.setCssText("flex: 2 3 4 2em; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		//
		emptyStyleDecl.setCssText("flex: 1.2em 2 auto; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		//
		emptyStyleDecl.setCssText("flex: 1.2em 2 2px; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-grow"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-shrink"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-basis"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testFlexFlow() {
		emptyStyleDecl.setCssText("flex-flow: row; ");
		assertEquals("row", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("nowrap", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertEquals("flex-flow: row; ", emptyStyleDecl.getCssText());
		assertEquals("flex-flow:row;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("flex-flow: row nowrap; ");
		assertEquals("row", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("nowrap", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-direction").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-wrap").isSubproperty());
		assertEquals("flex-flow: row nowrap; ", emptyStyleDecl.getCssText());
		assertEquals("flex-flow:row nowrap;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("flex-flow: column wrap; ");
		assertEquals("column", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("wrap", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertEquals("flex-flow: column wrap; ", emptyStyleDecl.getCssText());
		assertEquals("flex-flow:column wrap;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("flex-flow: column; ");
		assertEquals("column", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("nowrap", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertEquals("flex-flow: column; ", emptyStyleDecl.getCssText());
		assertEquals("flex-flow:column;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("flex-flow: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-direction").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-wrap").isSubproperty());
		assertEquals("flex-flow: unset; ", emptyStyleDecl.getCssText());
		assertEquals("flex-flow:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("flex-flow: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-direction").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("flex-wrap").isSubproperty());
		assertEquals("flex-flow: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("flex-flow:inherit;", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("flex-flow: inherit unset; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("flex-flow: 1.2em foo; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-direction"));
		assertEquals("", emptyStyleDecl.getPropertyValue("flex-wrap"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridKeywords() {
		emptyStyleDecl.setCssText("grid: initial; ");
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-columns").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-flow").isSubproperty());
		assertEquals("grid: initial; ", emptyStyleDecl.getCssText());
		assertEquals("grid:initial;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: none ! important; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-auto-flow"));
		assertEquals("grid: none ! important; ", emptyStyleDecl.getCssText());
		assertEquals("grid:none!important;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("grid:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: inherit ! important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-template-areas"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("grid:inherit!important;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: unset; ", emptyStyleDecl.getCssText());
		assertEquals("grid:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: unset ! important; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-template-areas"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: unset ! important; ", emptyStyleDecl.getCssText());
		assertEquals("grid:unset!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGrid() {
		emptyStyleDecl.setCssText("grid: \"a a a\"    \"b b b\"");
		assertEquals("\"a a a\" \"b b b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: \"a a a\" \"b b b\"; ", emptyStyleDecl.getCssText());
		assertEquals("grid:\"a a a\" \"b b b\";", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: [header-top] \"a   a   a\"     [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / auto 1fr auto; ");
		assertEquals("\"a   a   a\" \"b   b   b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[header-top] auto [header-bottom main-top] 1fr [main-bottom]",
				emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto 1fr auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("grid-template-columns");
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals(3, ((ValueList) value).getLength());
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-columns").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-flow").isSubproperty());
		assertEquals("grid: [header-top] \"a   a   a\" [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / auto 1fr auto; ", emptyStyleDecl.getCssText());
		assertEquals(
				"grid:[header-top] \"a   a   a\" [header-bottom]"
						+ "[main-top] \"b   b   b\" 1fr [main-bottom]/auto 1fr auto;",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: [] \"a   a   a\"     [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / 1fr 2fr; ");
		assertEquals("\"a   a   a\" \"b   b   b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto [header-bottom main-top] 1fr [main-bottom]",
				emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("1fr 2fr", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: \"a   a   a\" [header-bottom]" + " [main-top] \"b   b   b\" 1fr [main-bottom] / 1fr 2fr; ",
				emptyStyleDecl.getCssText());
		assertEquals("grid:\"a   a   a\" [header-bottom]" + "[main-top] \"b   b   b\" 1fr [main-bottom]/1fr 2fr;",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: auto 1fr / auto 1fr auto; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto 1fr auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto 1fr / auto 1fr auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto 1fr/auto 1fr auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: [header-top] repeat(2, 1fr) / minmax(2%, 1fr); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[header-top] repeat(2, 1fr)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("minmax(2%, 1fr)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: [header-top] repeat(2, 1fr) / minmax(2%, 1fr); ", emptyStyleDecl.getCssText());
		assertEquals("grid:[header-top] repeat(2,1fr)/minmax(2%,1fr);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: [header-top] 1fr / minmax(2%, 1fr); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[header-top] 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("minmax(2%, 1fr)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: [header-top] 1fr / minmax(2%, 1fr); ", emptyStyleDecl.getCssText());
		assertEquals("grid:[header-top] 1fr/minmax(2%,1fr);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: auto-flow 1fr / 100px; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("100px", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("1fr", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto-flow 1fr / 100px; ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto-flow 1fr/100px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: none / auto-flow 1fr; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("1fr", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("column", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: none / auto-flow 1fr; ", emptyStyleDecl.getCssText());
		assertEquals("grid:none/auto-flow 1fr;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: auto / auto-flow 1fr");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("1fr", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("column", emptyStyleDecl.getPropertyValue("grid-auto-flow")); // Few next ones taken from
																					// mozilla.org
		assertEquals("grid: auto / auto-flow 1fr; ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto/auto-flow 1fr;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: auto-flow 300px / repeat(3, [line1 line2 line3] 200px); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("repeat(3, [line1 line2 line3] 200px)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("300px", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto-flow 300px / repeat(3, [line1 line2 line3] 200px); ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto-flow 300px/repeat(3,[line1 line2 line3] 200px);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: auto-flow dense 40% / [line1] minmax(20em, max-content); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("[line1] minmax(20em, max-content)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("40%", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row dense", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto-flow dense 40% / [line1] minmax(20em, max-content); ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto-flow dense 40%/[line1] minmax(20em,max-content);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: repeat(3, [line1 line2 line3] 200px) / auto-flow 300px; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("repeat(3, [line1 line2 line3] 200px)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("300px", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("column", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: repeat(3, [line1 line2 line3] 200px) / auto-flow 300px; ", emptyStyleDecl.getCssText());
		assertEquals("grid:repeat(3,[line1 line2 line3] 200px)/auto-flow 300px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: [line1] minmax(20em, max-content) / auto-flow dense 40%; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[line1] minmax(20em, max-content)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("40%", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("column dense", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: [line1] minmax(20em, max-content) / auto-flow dense 40%; ", emptyStyleDecl.getCssText());
		assertEquals("grid:[line1] minmax(20em,max-content)/auto-flow dense 40%;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: minmax(400px, min-content) / repeat(auto-fill, 50px); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("minmax(400px, min-content)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("repeat(auto-fill, 50px)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: minmax(400px, min-content) / repeat(auto-fill, 50px); ", emptyStyleDecl.getCssText());
		assertEquals("grid:minmax(400px,min-content)/repeat(auto-fill,50px);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: 100px 1fr / 50px 1fr; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("100px 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("50px 1fr", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: 100px 1fr / 50px 1fr; ", emptyStyleDecl.getCssText());
		assertEquals("grid:100px 1fr/50px 1fr;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: auto 1fr / auto 1fr auto; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto 1fr auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto 1fr / auto 1fr auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto 1fr/auto 1fr auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: 100px / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("100px", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: 100px / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:100px/auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: [linename] 100px / [columnname1] 30% [columnname2] 70%; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[linename] 100px", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("[columnname1] 30% [columnname2] 70%", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: [linename] 100px / [columnname1] 30% [columnname2] 70%; ", emptyStyleDecl.getCssText());
		assertEquals("grid:[linename] 100px/[columnname1] 30% [columnname2] 70%;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: fit-content(100px) / fit-content(40%); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("fit-content(100px)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("fit-content(40%)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: fit-content(100px) / fit-content(40%); ", emptyStyleDecl.getCssText());
		assertEquals("grid:fit-content(100px)/fit-content(40%);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: \"a a a\" \"b b b\"; ");
		assertEquals("\"a a a\" \"b b b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: \"a a a\" \"b b b\"; ", emptyStyleDecl.getCssText());
		assertEquals("grid:\"a a a\" \"b b b\";", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: \"a a a\" \"b b b\" max-content; ");
		assertEquals("\"a a a\" \"b b b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto max-content", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: \"a a a\" \"b b b\" max-content; ", emptyStyleDecl.getCssText());
		assertEquals("grid:\"a a a\" \"b b b\" max-content;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("1fr repeat(2, [foo] minmax(2%, 1fr))", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("fit-content(40%)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ", emptyStyleDecl.getCssText());
		assertEquals("grid:1fr repeat(2,[foo] minmax(2%,1fr))/fit-content(40%);", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("grid: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%) / ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("grid: 1fr repeat(2, [foo] minmax(2%, 1fr)) / / fit-content(40%) ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("grid: 1fr repeat(2, [foo] minmax(2%, 1fr)) / 3fr / fit-content(40%) ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl
				.setCssText("grid: [header-top] \"a a a\" 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridNone() {
		emptyStyleDecl.setCssText("grid: none; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-columns").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-auto-flow").isSubproperty());
		assertEquals("grid: none; ", emptyStyleDecl.getCssText());
		assertEquals("grid:none;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridNoneAuto() {
		emptyStyleDecl.setCssText("grid: none / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: none / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:none/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridAutoNone() {
		emptyStyleDecl.setCssText("grid: auto / none");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto / none; ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto/none;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridAutoAuto() {
		emptyStyleDecl.setCssText("grid: auto / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: auto / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:auto/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridAuto() {
		emptyStyleDecl.setCssText("grid: auto; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridBad() {
		emptyStyleDecl.setCssText("grid: foo; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridBad2() {
		emptyStyleDecl.setCssText("grid: foo /; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridBad3() {
		emptyStyleDecl.setCssText("grid: / foo; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridBad4() {
		emptyStyleDecl.setCssText("grid: 100px; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGrid2() {
		emptyStyleDecl.setCssText("grid:\"media-text-media media-text-content\" auto/50% auto;");
		assertEquals("media-text-media media-text-content", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("50% auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: \"media-text-media media-text-content\" auto / 50% auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:\"media-text-media media-text-content\" auto/50% auto;",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid: \"media-text-media media-text-content\" 1fr / auto");
		assertEquals("media-text-media media-text-content", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-auto-columns"));
		assertEquals("row", emptyStyleDecl.getPropertyValue("grid-auto-flow"));
		assertEquals("grid: \"media-text-media media-text-content\" 1fr / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid:\"media-text-media media-text-content\" 1fr/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplateKeywords() {
		emptyStyleDecl.setCssText("grid-template: none; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-columns").isSubproperty());
		assertEquals("grid-template: none; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:none;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: initial; ");
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("initial", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-columns").isSubproperty());
		assertEquals("grid-template: initial; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:initial;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: none ! important; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-columns").isSubproperty());
		assertEquals("grid-template: none ! important; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:none!important;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-columns").isSubproperty());
		assertEquals("grid-template: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: inherit ! important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-template-areas"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:inherit!important;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-columns").isSubproperty());
		assertEquals("grid-template: unset; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: unset ! important; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("grid-template-areas"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: unset ! important; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:unset!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate() {
		emptyStyleDecl.setCssText("grid-template: [header-top] \"a   a   a\"     [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / auto 1fr auto; ");
		assertEquals("\"a   a   a\" \"b   b   b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[header-top] auto [header-bottom main-top] 1fr [main-bottom]",
				emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto 1fr auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		StyleValue value = emptyStyleDecl.getPropertyCSSValue("grid-template-columns");
		assertEquals(CssType.LIST, value.getCssValueType());
		assertEquals(3, ((ValueList) value).getLength());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-areas").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-rows").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-template-columns").isSubproperty());
		assertEquals("grid-template: [header-top] \"a   a   a\" [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / auto 1fr auto; ", emptyStyleDecl.getCssText());
		assertEquals(
				"grid-template:[header-top] \"a   a   a\" [header-bottom]"
						+ "[main-top] \"b   b   b\" 1fr [main-bottom]/auto 1fr auto;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate2() {
		emptyStyleDecl.setCssText("grid-template: [] \"a   a   a\"     [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / 1fr 2fr; ");
		assertEquals("\"a   a   a\" \"b   b   b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto [header-bottom main-top] 1fr [main-bottom]",
				emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("1fr 2fr", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"a   a   a\" [header-bottom]"
				+ " [main-top] \"b   b   b\" 1fr [main-bottom] / 1fr 2fr; ", emptyStyleDecl.getCssText());
		assertEquals(
				"grid-template:\"a   a   a\" [header-bottom]" + "[main-top] \"b   b   b\" 1fr [main-bottom]/1fr 2fr;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate3() {
		emptyStyleDecl.setCssText("grid-template: auto 1fr / auto 1fr auto; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto 1fr auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: auto 1fr / auto 1fr auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:auto 1fr/auto 1fr auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate4() {
		emptyStyleDecl.setCssText("grid-template: 100px / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("100px", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: 100px / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:100px/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate5() {
		emptyStyleDecl.setCssText("grid-template: [header-top] repeat(2, 1fr) / minmax(2%, 1fr); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[header-top] repeat(2, 1fr)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("minmax(2%, 1fr)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: [header-top] repeat(2, 1fr) / minmax(2%, 1fr); ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:[header-top] repeat(2,1fr)/minmax(2%,1fr);", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate6() {
		emptyStyleDecl.setCssText("grid-template: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("1fr repeat(2, [foo] minmax(2%, 1fr))", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("fit-content(40%)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ",
				emptyStyleDecl.getCssText());
		assertEquals("grid-template:1fr repeat(2,[foo] minmax(2%,1fr))/fit-content(40%);",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate7() {
		// Next ones taken from mozilla.org
		emptyStyleDecl.setCssText("grid-template: 100px 1fr / 50px 1fr; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("100px 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("50px 1fr", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: 100px 1fr / 50px 1fr; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:100px 1fr/50px 1fr;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: auto 1fr / auto 1fr auto; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto 1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto 1fr auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: auto 1fr / auto 1fr auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:auto 1fr/auto 1fr auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: [linename] 100px / [columnname1] 30% [columnname2] 70%; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("[linename] 100px", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("[columnname1] 30% [columnname2] 70%", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: [linename] 100px / [columnname1] 30% [columnname2] 70%; ",
				emptyStyleDecl.getCssText());
		assertEquals("grid-template:[linename] 100px/[columnname1] 30% [columnname2] 70%;",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: fit-content(100px) / fit-content(40%); ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("fit-content(100px)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("fit-content(40%)", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: fit-content(100px) / fit-content(40%); ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:fit-content(100px)/fit-content(40%);", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: \"a a a\" \"b b b\"; ");
		assertEquals("\"a a a\" \"b b b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"a a a\" \"b b b\"; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:\"a a a\" \"b b b\";", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-template: \"a a a\" \"b b b\" max-content; ");
		assertEquals("\"a a a\" \"b b b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto max-content", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"a a a\" \"b b b\" max-content; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:\"a a a\" \"b b b\" max-content;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate8() {
		emptyStyleDecl.setCssText("grid-template:\"media-text-media media-text-content\" auto/50% auto;");
		assertEquals("media-text-media media-text-content", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("50% auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"media-text-media media-text-content\" auto / 50% auto; ",
				emptyStyleDecl.getCssText());
		assertEquals("grid-template:\"media-text-media media-text-content\" auto/50% auto;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate9() {
		emptyStyleDecl.setCssText("grid-template: \"a b\" / 50% auto;");
		assertEquals("a b", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("50% auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"a b\" / 50% auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:\"a b\"/50% auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate10() {
		emptyStyleDecl.setCssText("grid-template: \"a a a\" \"b b b\" / 50% auto");
		assertEquals("\"a a a\" \"b b b\"", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("50% auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"a a a\" \"b b b\" / 50% auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:\"a a a\" \"b b b\"/50% auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate11() {
		emptyStyleDecl.setCssText("grid-template: \"a a\" 1fr / auto");
		assertEquals("a a", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("1fr", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: \"a a\" 1fr / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:\"a a\" 1fr/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplate12() {
		emptyStyleDecl.setCssText("grid-template: repeat(1, [] 10px) / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("repeat(1, 10px)", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: repeat(1, 10px) / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:repeat(1,10px)/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplateAutoAuto() {
		emptyStyleDecl.setCssText("grid-template: auto / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: auto / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:auto/auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplateAutoNone() {
		emptyStyleDecl.setCssText("grid-template: auto / none");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: auto / none; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template:auto/none;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGridTemplateNoneAuto() {
		emptyStyleDecl.setCssText("grid-template: none / auto");
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("grid-template: none / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-template: none / auto; ", emptyStyleDecl.getCssText());
	}

	@Test
	public void testGridTemplateAuto() {
		emptyStyleDecl.setCssText("grid-template: auto; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridTemplateBad() {
		emptyStyleDecl.setCssText("grid-template: 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%) / ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("grid-template: 1fr repeat(2, [foo] minmax(2%, 1fr)) / / fit-content(40%) ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("grid-template: 1fr repeat(2, [foo] minmax(2%, 1fr)) / 3fr / fit-content(40%) ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText(
				"grid-template: [header-top] \"a a a\" 1fr repeat(2, [foo] minmax(2%, 1fr)) / fit-content(40%); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridTemplateBad2() {
		emptyStyleDecl.setCssText("grid-template: foo; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridTemplateBad3() {
		emptyStyleDecl.setCssText("grid-template: / fit-content(40%); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridTemplateBad4() {
		emptyStyleDecl.setCssText("grid-template: auto 1fr / ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridTemplateBad45() {
		emptyStyleDecl.setCssText("grid-template: auto 1fr ; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-areas"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-rows"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-template-columns"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridArea() {
		emptyStyleDecl.setCssText("grid-area: auto; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-end").isSubproperty());
		assertEquals("grid-area: auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: foo; ");
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-end").isSubproperty());
		assertEquals("grid-area: foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: foo / bar; ");
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-end").isSubproperty());
		assertEquals("grid-area: foo / bar; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:foo/bar;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: foo/bar/3; ");
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-end").isSubproperty());
		assertEquals("grid-area: foo / bar / 3; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:foo/bar/3;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 2 foo / bar; ");
		assertEquals("2 foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-end").isSubproperty());
		assertEquals("grid-area: 2 foo / bar; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:2 foo/bar;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 2 foo / 3 / bar; ");
		assertEquals("2 foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 2 foo / 3 / bar; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:2 foo/3/bar;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 7 / 1 / 3; ");
		assertEquals("7", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 7 / 1 / 3; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:7/1/3;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 7 / 1 / foo; ");
		assertEquals("7", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 7 / 1 / foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:7/1/foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 7 / foo / bar; ");
		assertEquals("7", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 7 / foo / bar; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:7/foo/bar;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 7 / 1; ");
		assertEquals("7", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 7 / 1; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:7/1;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 2 foo / 3 / span 1 / bar; ");
		assertEquals("2 foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("span 1", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("bar", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 2 foo / 3 / span 1 / bar; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:2 foo/3/span 1/bar;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 2 foo / 3 / span 1; ");
		assertEquals("2 foo", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("span 1", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-area: 2 foo / 3 / span 1; ", emptyStyleDecl.getCssText());
		assertEquals("grid-area:2 foo/3/span 1;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-area: 2 foo / 3 /; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
		emptyStyleDecl.setCssText("grid-area: 2 foo / 3 / 1em; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridColumn() {
		emptyStyleDecl.setCssText("grid-column: auto; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-column-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-column-end").isSubproperty());
		assertEquals("grid-column: auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: 1; ");
		assertEquals("1", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-column-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-column-end").isSubproperty());
		assertEquals("grid-column: 1; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:1;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: foo; ");
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: 1 / 3; ");
		assertEquals("1", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: 1 / 3; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:1/3;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: 1 / foo; ");
		assertEquals("1", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-column-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-column-end").isSubproperty());
		assertEquals("grid-column: 1 / foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:1/foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: span 2 / foo; ");
		assertEquals("span 2", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: span 2 / foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:span 2/foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: span 2 / 3 foo; ");
		assertEquals("span 2", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("3 foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: span 2 / 3 foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:span 2/3 foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: auto / 3 foo; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("3 foo", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: auto / 3 foo; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:auto/3 foo;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: 1 foo / 3; ");
		assertEquals("1 foo", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("3", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: 1 foo / 3; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:1 foo/3;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: 1 foo / auto; ");
		assertEquals("1 foo", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("grid-column: 1 foo / auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-column:1 foo/auto;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("grid-column: 1 foo /; ");
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-column-start"));
		assertEquals("", emptyStyleDecl.getPropertyValue("grid-column-end"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testGridRow() {
		emptyStyleDecl.setCssText("grid-row: auto; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-row-start"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("grid-row-end"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-start").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("grid-row-end").isSubproperty());
		assertEquals("grid-row: auto; ", emptyStyleDecl.getCssText());
		assertEquals("grid-row:auto;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testOutline() {
		emptyStyleDecl.setCssText("outline: 2px dotted #cff; ");
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("outline-style"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("outline-width"));
		assertEquals("#cff", emptyStyleDecl.getPropertyValue("outline-color"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("outline-style").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("outline-width").isSubproperty());
		assertEquals("outline: 2px dotted #cff; ", emptyStyleDecl.getCssText());
		assertEquals("outline:2px dotted #cff;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testOutlineInvert() {
		emptyStyleDecl.setCssText("outline: 2px dotted invert; ");
		assertEquals("dotted", emptyStyleDecl.getPropertyValue("outline-style"));
		assertEquals("2px", emptyStyleDecl.getPropertyValue("outline-width"));
		assertEquals("invert", emptyStyleDecl.getPropertyValue("outline-color"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("outline-style").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("outline-width").isSubproperty());
		assertEquals("outline: 2px dotted invert; ", emptyStyleDecl.getCssText());
		assertEquals("outline:2px dotted invert;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testOutlineCustom() {
		emptyStyleDecl.setCssText("outline: 5.0px auto -webkit-focus-ring-color; ");
		assertEquals("auto", emptyStyleDecl.getPropertyValue("outline-style"));
		assertEquals("5px", emptyStyleDecl.getPropertyValue("outline-width"));
		assertEquals("-webkit-focus-ring-color", emptyStyleDecl.getPropertyValue("outline-color"));
		assertEquals("outline: 5px auto -webkit-focus-ring-color; ", emptyStyleDecl.getCssText());
		assertEquals("outline:5px auto -webkit-focus-ring-color;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testListStyle() {
		emptyStyleDecl.setCssText("list-style: disc; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("disc", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-image").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-position").isSubproperty());
		assertEquals("list-style: disc; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:disc;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: url('foo.png'); ");
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("disc", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-image").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-position").isSubproperty());
		assertEquals("list-style: url('foo.png'); ", emptyStyleDecl.getCssText());
		assertEquals("list-style:url('foo.png');", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: url('foo.png') inside; ");
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("disc", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-image").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-position").isSubproperty());
		assertEquals("list-style: url('foo.png') inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:url('foo.png') inside;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: url('foo.png') inside square; ");
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("square", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-image").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("list-style-position").isSubproperty());
		assertEquals("list-style: url('foo.png') inside square; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:url('foo.png') inside square;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: none; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: none; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:none;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: none inside; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: none inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:none inside;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: none url('foo.png') inside; ");
		assertEquals("url('foo.png')", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: none url('foo.png') inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:none url('foo.png') inside;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: decimal none inside;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("decimal", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: decimal none inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:decimal none inside;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: decimal none;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("decimal", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: decimal none; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:decimal none;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: inside MyStyle;");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("MyStyle", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: inside MyStyle; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:inside MyStyle;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style:inside symbols('*' '\\2020' '\\2021' '\\A7');");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("symbols('*' '\\2020' '\\2021' '\\A7')", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: inside symbols('*' '\\2020' '\\2021' '\\A7'); ", emptyStyleDecl.getCssText());
		assertEquals("list-style:inside symbols('*' '\u2020' '\u2021' '\u00a7');", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: decimal var(--whatever);");
		assertEquals("", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: decimal var(--whatever); ", emptyStyleDecl.getCssText());
		assertEquals("list-style:decimal var(--whatever);", emptyStyleDecl.getMinifiedCssText());
		assertFalse(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testListStyleOrdering() {
		emptyStyleDecl.setCssText("list-style: inside inside; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: inside inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:inside inside;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: outside inside; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: outside inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:outside inside;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("list-style: outside outside; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("outside", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: outside outside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:outside outside;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testListStyleBad() {
		emptyStyleDecl.setCssText("list-style: rgb(12, 9, 25); ");
		assertEquals("", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("", emptyStyleDecl.getCssText());
		assertEquals("", emptyStyleDecl.getMinifiedCssText());
		assertTrue(emptyStyleDecl.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testListStyleCounterStyle() {
		emptyStyleDecl.setCssText("list-style: foo inside; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: foo inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:foo inside;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testListStyleCounterStyleString() {
		emptyStyleDecl.setCssText("list-style: \"foo\" inside; ");
		assertEquals("none", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inside", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("foo", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: \"foo\" inside; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:\"foo\" inside;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testListStyleInherit() {
		emptyStyleDecl.setCssText("list-style: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:inherit;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testListStyleUnset() {
		emptyStyleDecl.setCssText("list-style: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("list-style-image"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("list-style-position"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("list-style-type"));
		assertEquals("list-style: unset; ", emptyStyleDecl.getCssText());
		assertEquals("list-style:unset;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testGap() {
		emptyStyleDecl.setCssText("gap: normal; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("column-gap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("row-gap").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-gap").isSubproperty());
		assertEquals("gap: normal; ", emptyStyleDecl.getCssText());
		assertEquals("gap:normal;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: 50px normal; ");
		assertEquals("50px", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("column-gap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("row-gap").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-gap").isSubproperty());
		assertEquals("gap: 50px normal; ", emptyStyleDecl.getCssText());
		assertEquals("gap:50px normal;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: 50px; ");
		assertEquals("50px", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("50px", emptyStyleDecl.getPropertyValue("column-gap"));
		assertEquals("gap: 50px; ", emptyStyleDecl.getCssText());
		assertEquals("gap:50px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: 50px 20px; ");
		assertEquals("50px", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("20px", emptyStyleDecl.getPropertyValue("column-gap"));
		assertEquals("gap: 50px 20px; ", emptyStyleDecl.getCssText());
		assertEquals("gap:50px 20px;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: 50px 4%; ");
		assertEquals("50px", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("4%", emptyStyleDecl.getPropertyValue("column-gap"));
		assertEquals("gap: 50px 4%; ", emptyStyleDecl.getCssText());
		assertEquals("gap:50px 4%;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("column-gap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("row-gap").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-gap").isSubproperty());
		assertEquals("gap: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("gap:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("column-gap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("row-gap").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-gap").isSubproperty());
		assertEquals("gap: unset; ", emptyStyleDecl.getCssText());
		assertEquals("gap:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("gap: inherit ! important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("row-gap"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("column-gap"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("row-gap"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("row-gap").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("column-gap").isSubproperty());
		assertEquals("gap: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("gap:inherit!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testPlaceContent() {
		emptyStyleDecl.setCssText("place-content: normal; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("justify-content"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-content").isSubproperty());
		assertEquals("place-content: normal; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:normal;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: center start; ");
		assertEquals("center", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("start", emptyStyleDecl.getPropertyValue("justify-content"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-content").isSubproperty());
		assertEquals("place-content: center start; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:center start;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: space-between space-between; ");
		assertEquals("space-between", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("space-between", emptyStyleDecl.getPropertyValue("justify-content"));
		assertEquals("place-content: space-between space-between; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:space-between space-between;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: space-between; ");
		assertEquals("space-between", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("space-between", emptyStyleDecl.getPropertyValue("justify-content"));
		assertEquals("place-content: space-between; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:space-between;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: first baseline space-evenly; ");
		assertEquals("first baseline", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("space-evenly", emptyStyleDecl.getPropertyValue("justify-content"));
		assertEquals("place-content: first baseline space-evenly; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:first baseline space-evenly;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("justify-content"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-content").isSubproperty());
		assertEquals("place-content: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("justify-content"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-content").isSubproperty());
		assertEquals("place-content: unset; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-content: inherit ! important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("align-content"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("justify-content"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("align-content"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-content").isSubproperty());
		assertEquals("place-content: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("place-content:inherit!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testPlaceItems() {
		emptyStyleDecl.setCssText("place-items: normal; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("justify-items"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-items").isSubproperty());
		assertEquals("place-items: normal; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:normal;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: center start; ");
		assertEquals("center", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("start", emptyStyleDecl.getPropertyValue("justify-items"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-items").isSubproperty());
		assertEquals("place-items: center start; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:center start;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: stretch unsafe end; ");
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("unsafe end", emptyStyleDecl.getPropertyValue("justify-items"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-items").isSubproperty());
		assertEquals("place-items: stretch unsafe end; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:stretch unsafe end;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: stretch; ");
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("justify-items"));
		assertEquals("place-items: stretch; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:stretch;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: self-start legacy; ");
		assertEquals("self-start", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("legacy", emptyStyleDecl.getPropertyValue("justify-items"));
		assertEquals("place-items: self-start legacy; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:self-start legacy;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: first baseline legacy right; ");
		assertEquals("first baseline", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("legacy right", emptyStyleDecl.getPropertyValue("justify-items"));
		assertEquals("place-items: first baseline legacy right; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:first baseline legacy right;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: first baseline legacy; ");
		assertEquals("first baseline", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("legacy", emptyStyleDecl.getPropertyValue("justify-items"));
		emptyStyleDecl.setCssText("place-items: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("justify-items"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-items").isSubproperty());
		assertEquals("place-items: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("justify-items"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-items").isSubproperty());
		assertEquals("place-items: unset; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-items: inherit ! important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("align-items"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("justify-items"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-items").isSubproperty());
		assertEquals("important", emptyStyleDecl.getPropertyPriority("align-items"));
		assertEquals("place-items: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("place-items:inherit!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testPlaceSelf() {
		emptyStyleDecl.setCssText("place-self: normal; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("justify-self"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-self").isSubproperty());
		assertEquals("place-self: normal; ", emptyStyleDecl.getCssText());
		assertEquals("place-self:normal;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-self: center start; ");
		assertEquals("center", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("start", emptyStyleDecl.getPropertyValue("justify-self"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-self").isSubproperty());
		emptyStyleDecl.setCssText("place-self: stretch unsafe end; ");
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("unsafe end", emptyStyleDecl.getPropertyValue("justify-self"));
		assertEquals("place-self: stretch unsafe end; ", emptyStyleDecl.getCssText());
		assertEquals("place-self:stretch unsafe end;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-self: stretch; ");
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("stretch", emptyStyleDecl.getPropertyValue("justify-self"));
		emptyStyleDecl.setCssText("place-self: self-start auto; ");
		assertEquals("self-start", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("justify-self"));
		emptyStyleDecl.setCssText("place-self: first baseline safe start; ");
		assertEquals("first baseline", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("safe start", emptyStyleDecl.getPropertyValue("justify-self"));
		assertEquals("place-self: first baseline safe start; ", emptyStyleDecl.getCssText());
		assertEquals("place-self:first baseline safe start;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-self: first baseline center; ");
		assertEquals("first baseline", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("center", emptyStyleDecl.getPropertyValue("justify-self"));
		emptyStyleDecl.setCssText("place-self: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("justify-self"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-self").isSubproperty());
		assertEquals("place-self: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("place-self:inherit;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-self: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("justify-self"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-self").isSubproperty());
		assertEquals("place-self: unset; ", emptyStyleDecl.getCssText());
		assertEquals("place-self:unset;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("place-self: inherit ! important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("align-self"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("justify-self"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("align-self"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("align-self").isSubproperty());
		assertEquals("place-self: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("place-self:inherit!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont() {
		emptyStyleDecl.setCssText("font-style: italic; font: smaller");
		assertEquals("smaller", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-style").getCssText());
		assertEquals("Serif", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("initial", emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-size").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-family").isSubproperty());
		assertEquals("font: smaller; ", emptyStyleDecl.getCssText());
		assertEquals("font:smaller;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: 80%");
		assertEquals("80%", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-size").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-family").isSubproperty());
		assertEquals("font: 80%; ", emptyStyleDecl.getCssText());
		assertEquals("font:80%;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: 80% small-caps");
		assertEquals("80%", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("small-caps", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("font: 80% small-caps; ", emptyStyleDecl.getCssText());
		assertEquals("font:80% small-caps;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: 400 80%/120%");
		assertEquals("80%", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("400", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("120%", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("font: 400 80% / 120%; ", emptyStyleDecl.getCssText());
		assertEquals("font:400 80%/120%;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: serif");
		assertEquals("serif", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("font: serif; ", emptyStyleDecl.getCssText());
		assertEquals("font:serif;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: Verdana");
		assertEquals("Verdana", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("font: Verdana; ", emptyStyleDecl.getCssText());
		assertEquals("font:Verdana;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: \"Times New Roman\"");
		assertEquals("Times New Roman", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("\"Times New Roman\"", emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertEquals("font: \"Times New Roman\"; ", emptyStyleDecl.getCssText());
		assertEquals("font:\"Times New Roman\";", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: \"Times New Roman\", Arial");
		assertEquals(CssType.LIST, emptyStyleDecl.getPropertyCSSValue("font-family").getCssValueType());
		assertEquals("\"Times New Roman\", Arial", emptyStyleDecl.getPropertyValue("font-family"));
		emptyStyleDecl.setCssText("font: 16pt \"Times New Roman\", Arial");
		assertEquals("16pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals(CssType.LIST, emptyStyleDecl.getPropertyCSSValue("font-family").getCssValueType());
		assertEquals("\"Times New Roman\", Arial", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("font: 16pt \"Times New Roman\", Arial; ", emptyStyleDecl.getCssText());
		assertEquals("font:16pt \"Times New Roman\",Arial;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("border-top-width: thick");
		assertEquals("thick", emptyStyleDecl.getPropertyValue("border-top-width"));
		emptyStyleDecl.setCssText("font: italic; border: solid blue");
		assertEquals(31, emptyStyleDecl.getLength());
		assertEquals("italic", emptyStyleDecl.getPropertyCSSValue("font-style").getCssText());
		assertEquals("medium", emptyStyleDecl.getPropertyCSSValue("border-top-width").getCssText());
		assertEquals("solid", emptyStyleDecl.getPropertyCSSValue("border-top-style").getCssText());
		assertEquals("blue", emptyStyleDecl.getPropertyCSSValue("border-top-color").getCssText());
		assertEquals("font: italic; border: solid blue; ", emptyStyleDecl.getCssText());
		assertEquals("font:italic;border:solid blue;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: bold");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("bold", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-size").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-family").isSubproperty());
		assertEquals("font: bold; ", emptyStyleDecl.getCssText());
		assertEquals("font:bold;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont2() {
		emptyStyleDecl.setCssText("font:16pt/1.25 \"Helvetica Neue\",Arial,sans-serif;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("\"Helvetica Neue\", Arial, sans-serif", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("16pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("1.25", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: 16pt / 1.25 \"Helvetica Neue\", Arial, sans-serif; ", emptyStyleDecl.getCssText());
		assertEquals("font:16pt/1.25 \"Helvetica Neue\",Arial,sans-serif;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont3() {
		emptyStyleDecl.setCssText("font: normal 14.0px / 1.4 Helvetica Neue , Helvetica , sans-serif;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("'Helvetica Neue', Helvetica, sans-serif", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("14px", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("1.4", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: 14px / 1.4 Helvetica Neue, Helvetica, sans-serif; ", emptyStyleDecl.getCssText());
		assertEquals("font:14px/1.4 Helvetica Neue,Helvetica,sans-serif;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font:normal 1.2rem/1.4 \"Helvetica Neue\",Helvetica,sans-serif;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("\"Helvetica Neue\", Helvetica, sans-serif", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("1.2rem", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("1.4", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: 1.2rem / 1.4 \"Helvetica Neue\", Helvetica, sans-serif; ", emptyStyleDecl.getCssText());
		assertEquals("font:1.2rem/1.4 \"Helvetica Neue\",Helvetica,sans-serif;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont4() {
		emptyStyleDecl.setCssText("font: normal 14.0px / 1 FontAwesome;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("FontAwesome", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("14px", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: 14px / 1 FontAwesome; ", emptyStyleDecl.getCssText());
		assertEquals("font:14px/1 FontAwesome;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont5() {
		emptyStyleDecl.setCssText("font: normal normal normal 14pt / 1 FontAwesome;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("FontAwesome", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("14pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("1", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: 14pt / 1 FontAwesome; ", emptyStyleDecl.getCssText());
		assertEquals("font:14pt/1 FontAwesome;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont6() {
		emptyStyleDecl.setCssText("font: bold 14px/32px 'Roboto', Arial, sans-serif;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("'Roboto', Arial, sans-serif", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("bold", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("14px", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("32px", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: bold 14px / 32px 'Roboto', Arial, sans-serif; ", emptyStyleDecl.getCssText());
		assertEquals("font:bold 14px/32px 'Roboto',Arial,sans-serif;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont7() {
		emptyStyleDecl.setCssText("font: 14pt Font Normal;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("Font Normal", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("14pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("font: 14pt Font Normal; ", emptyStyleDecl.getCssText());
		assertEquals("font:14pt Font Normal;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFont8() {
		emptyStyleDecl.setCssText("font: 14pt Font Normal; font-size: inherit;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("Font Normal", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertFalse(emptyStyleDecl.getPropertyCSSValue("font-size").isSubproperty());
		assertEquals("font: 14pt Font Normal; font-size: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("font:14pt Font Normal;font-size:inherit", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontOverride() {
		emptyStyleDecl.setCssText(
				"font:14pt Font Normal;font-size:12pt;font-size-adjust:0.5;font-family:Arial;font-weight:bolder;font-style:italic;line-height:1.5em;font-stretch:ultra-condensed;font-kerning:normal;");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("Arial", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("bolder", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("italic", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("12pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("0.5", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("ultra-condensed", emptyStyleDecl.getPropertyValue("font-stretch"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("1.5em", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertFalse(emptyStyleDecl.getPropertyCSSValue("font-size").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-caps").isSubproperty());
		assertEquals(
				"font: 14pt Font Normal; font-size: 12pt; font-size-adjust: 0.5; font-family: Arial; font-weight: bolder; font-style: italic; line-height: 1.5em; font-stretch: ultra-condensed; font-kerning: normal; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"font:14pt Font Normal;font-size:12pt;font-size-adjust:.5;font-family:Arial;font-weight:bolder;font-style:italic;line-height:1.5em;font-stretch:ultra-condensed;font-kerning:normal",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontOverrideFull() {
		emptyStyleDecl.setCssText(
				"font:14pt Font Normal;font-size:12pt;font-size-adjust:0.5;font-family:Arial;font-weight:bolder;font-style:italic;line-height:1.5em;font-stretch:ultra-condensed;font-kerning:normal;font-variant-caps:unicase;font-variant-ligatures:common-ligatures;font-variant-position:super;font-variant-numeric:slashed-zero;font-variant-alternates:historical-forms;font-variant-east-asian:jis90");
		assertEquals(14, emptyStyleDecl.getLength());
		assertEquals("Arial", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("bolder", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("italic", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("12pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("0.5", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("1.5em", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("ultra-condensed", emptyStyleDecl.getPropertyValue("font-stretch"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("unicase", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("common-ligatures", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("super", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("slashed-zero", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("historical-forms", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("jis90", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertFalse(emptyStyleDecl.getPropertyCSSValue("font-size").isSubproperty());
		assertEquals(
				"font-size: 12pt; font-size-adjust: 0.5; font-family: Arial; font-weight: bolder; font-style: italic; line-height: 1.5em; font-stretch: ultra-condensed; font-kerning: normal; font-variant-caps: unicase; font-variant-ligatures: common-ligatures; font-variant-position: super; font-variant-numeric: slashed-zero; font-variant-alternates: historical-forms; font-variant-east-asian: jis90; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"font-size:12pt;font-size-adjust:.5;font-family:Arial;font-weight:bolder;font-style:italic;line-height:1.5em;font-stretch:ultra-condensed;font-kerning:normal;font-variant-caps:unicase;font-variant-ligatures:common-ligatures;font-variant-position:super;font-variant-numeric:slashed-zero;font-variant-alternates:historical-forms;font-variant-east-asian:jis90",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontInherit() {
		emptyStyleDecl.setCssText("font: inherit");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("font-size"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("font: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("font:inherit;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontInheritImportant() {
		emptyStyleDecl.setCssText("font: inherit ! important");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-size"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-ligatures"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-caps"));
		assertEquals("font: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("font:inherit!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontUnset() {
		emptyStyleDecl.setCssText("font: unset");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-family"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("font-size"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("line-height"));
		assertEquals("font: unset; ", emptyStyleDecl.getCssText());
		assertEquals("font:unset;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontImportant() {
		emptyStyleDecl.setCssText("font-size: large; font: bold !important");
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-weight"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-style"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-size"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-family"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-caps"));
		assertEquals("font: bold ! important; ", emptyStyleDecl.getCssText());
		assertEquals("font:bold!important;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font: bold !important; border: solid blue");
		assertEquals("bold", emptyStyleDecl.getPropertyValue("font-weight"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-style"));
		assertEquals("medium", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("medium", emptyStyleDecl.getPropertyCSSValue("border-top-width").getCssText());
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-weight"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-style"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-size"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-family"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-caps"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("border-top-width"));
		assertEquals("", emptyStyleDecl.getPropertyPriority("border-top-style"));
		assertEquals("font: bold ! important; border: solid blue; ", emptyStyleDecl.getCssText());
		assertEquals("font:bold!important;border:solid blue;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testSystemFont() {
		emptyStyleDecl.setCssText("font: icon");
		assertEquals("7pt", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("300", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-style").getCssText());
		assertEquals("Iconfont", emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-size-adjust"));
		assertEquals("auto", emptyStyleDecl.getPropertyValue("font-kerning"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("font: 300 7pt Iconfont; ", emptyStyleDecl.getCssText());
		assertEquals("font:300 7pt Iconfont;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontLevel3() {
		emptyStyleDecl.setCssText("font: condensed 80% sans-serif");
		assertEquals("font: condensed 80% sans-serif; ", emptyStyleDecl.getCssText());
		assertEquals("condensed", emptyStyleDecl.getPropertyValue("font-stretch"));
		assertEquals("80%", emptyStyleDecl.getPropertyValue("font-size"));
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-weight").getCssText());
		assertEquals("normal", emptyStyleDecl.getPropertyCSSValue("font-style").getCssText());
		assertEquals("sans-serif", emptyStyleDecl.getPropertyCSSValue("font-family").getCssText());
		assertEquals("font: condensed 80% sans-serif; ", emptyStyleDecl.getCssText());
		assertEquals("font:condensed 80% sans-serif;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariant() {
		emptyStyleDecl.setCssText("font-variant: small-caps; ");
		assertEquals("small-caps", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-caps").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-ligatures").isSubproperty());
		assertEquals("font-variant: small-caps; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:small-caps;", emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText("font-variant: ruby; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("ruby", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("font-variant: ruby; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:ruby;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariantNone() {
		emptyStyleDecl.setCssText("font-variant: none; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("none", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("font-variant: none; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:none;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariantNormal() {
		emptyStyleDecl.setCssText("font-variant: normal; ");
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("font-variant: normal; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:normal;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariant2() {
		emptyStyleDecl.setCssText(
				"font-variant: common-ligatures sub stylistic(foo) small-caps proportional-nums ordinal ruby; ");
		assertEquals("small-caps", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("common-ligatures", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("sub", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("proportional-nums ordinal", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("stylistic(foo)", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("ruby", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("font-variant: common-ligatures sub stylistic(foo) small-caps proportional-nums ordinal ruby; ",
				emptyStyleDecl.getCssText());
		assertEquals("font-variant:common-ligatures sub stylistic(foo) small-caps proportional-nums ordinal ruby;",
				emptyStyleDecl.getMinifiedCssText());
		emptyStyleDecl.setCssText(
				"font-variant: common-ligatures discretionary-ligatures stylistic(foo) small-caps proportional-nums diagonal-fractions ordinal jis83; ");
		assertEquals("small-caps", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("common-ligatures discretionary-ligatures",
				emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("proportional-nums diagonal-fractions ordinal",
				emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("stylistic(foo)", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("jis83", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals(
				"font-variant: common-ligatures discretionary-ligatures stylistic(foo) small-caps proportional-nums diagonal-fractions ordinal jis83; ",
				emptyStyleDecl.getCssText());
		assertEquals(
				"font-variant:common-ligatures discretionary-ligatures stylistic(foo) small-caps proportional-nums diagonal-fractions ordinal jis83;",
				emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariantInherit() {
		emptyStyleDecl.setCssText("font-variant: inherit; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-caps").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-ligatures").isSubproperty());
		assertEquals("font-variant: inherit; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:inherit;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariantInheritImportant() {
		emptyStyleDecl.setCssText("font-variant: inherit !important; ");
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("inherit", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-caps"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-ligatures"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-position"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-numeric"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-alternates"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-east-asian"));
		assertEquals("font-variant: inherit ! important; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:inherit!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariantUnset() {
		emptyStyleDecl.setCssText("font-variant: unset; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-caps").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-ligatures").isSubproperty());
		assertEquals("font-variant: unset; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:unset;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void testFontVariantUnsetImportant() {
		emptyStyleDecl.setCssText("font-variant: unset !important; ");
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-caps"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-ligatures"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-position"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-numeric"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-alternates"));
		assertEquals("unset", emptyStyleDecl.getPropertyValue("font-variant-east-asian"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-caps"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-ligatures"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-position"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-numeric"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-alternates"));
		assertEquals("important", emptyStyleDecl.getPropertyPriority("font-variant-east-asian"));
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-caps").isSubproperty());
		assertTrue(emptyStyleDecl.getPropertyCSSValue("font-variant-ligatures").isSubproperty());
		assertEquals("font-variant: unset ! important; ", emptyStyleDecl.getCssText());
		assertEquals("font-variant:unset!important;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removePropertyOnEmptyDeclaration() {
		assertEquals("", emptyStyleDecl.removeProperty("border"));
	}

	@Test
	public void removePropertyNotExplicitlySet() {
		emptyStyleDecl.setCssText("border: 1px dashed blue;");
		assertEquals("", emptyStyleDecl.removeProperty("border-top-color"));
		assertEquals("border: 1px dashed blue; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removeSubproperty() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-top-color: yellow; ");
		assertEquals("yellow", emptyStyleDecl.removeProperty("border-top-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border: 1px dashed blue; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removeSubproperty2() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-top-color: yellow; border-left-color: navy;");
		assertEquals("yellow", emptyStyleDecl.removeProperty("border-top-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border: 1px dashed blue; border-left-color: navy; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;border-left-color:navy", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removeShorthand() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-top-color: yellow;");
		assertEquals("1px dashed blue", emptyStyleDecl.removeProperty("border"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("", emptyStyleDecl.getPropertyValue("border-left-color"));
		assertEquals("border-top-color: yellow; ", emptyStyleDecl.getCssText());
		assertEquals("border-top-color:yellow", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removeShorthand2() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-top: yellow;");
		assertEquals("yellow", emptyStyleDecl.removeProperty("border-top"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("border: 1px dashed blue; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removeShorthand3() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-color:navy; border-top: yellow;");
		assertEquals("yellow", emptyStyleDecl.removeProperty("border-top"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("navy", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("border: 1px dashed blue; border-color: navy; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;border-color:navy;", emptyStyleDecl.getMinifiedCssText());
	}

	@Test
	public void removeShorthand4() {
		emptyStyleDecl.setCssText("border: 1px dashed blue; border-color:navy; border-top: yellow;");
		assertEquals("navy", emptyStyleDecl.removeProperty("border-color"));
		assertEquals("yellow", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("border: 1px dashed blue; border-top: yellow; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;border-top:yellow;", emptyStyleDecl.getMinifiedCssText());
		assertEquals("yellow", emptyStyleDecl.removeProperty("border-top"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-top-color"));
		assertEquals("blue", emptyStyleDecl.getPropertyValue("border-bottom-color"));
		assertEquals("border: 1px dashed blue; ", emptyStyleDecl.getCssText());
		assertEquals("border:1px dashed blue;", emptyStyleDecl.getMinifiedCssText());
	}

}
