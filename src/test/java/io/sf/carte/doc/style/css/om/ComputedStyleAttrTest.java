/*

 Copyright (c) 2005-2023, Carlos Amengual.

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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;

public class ComputedStyleAttrTest {

	static CSSStyleSheet sheet;

	static Document refXhtmlDoc;

	CSSDocument xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws IOException, DocumentException {
		sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		refXhtmlDoc = DOMCSSStyleSheetFactoryTest.plainDocumentFromStream(
				DOMCSSStyleSheetFactoryTest.sampleHTMLStream(),
				MockURLConnectionFactory.SAMPLE_URL);
		//refXhtmlDoc = TestDOMImplementation.sampleHTMLDocument();
	}

	@BeforeEach
	public void setUp() throws IOException, DocumentException {
		xhtmlDoc = DOMCSSStyleSheetFactoryTest.getFactoryWithUASheet()
				.createCSSDocument((Document) refXhtmlDoc.cloneNode(true));
		//xhtmlDoc = (CSSDocument) refXhtmlDoc.cloneNode(true);
	}

	@Test
	public void getComputedStyleAttr() {
		/*
		 * attr() value, fallback.
		 */
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin,0.8em)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9.6f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() value in calc(), fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:calc(attr(leftmargin,0.8em)*2)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(19.2f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		CSSTypedValue paddingLeft = (CSSTypedValue) style.getPropertyCSSValue("padding-left");
		assertEquals(CSSValue.Type.EXPRESSION, paddingLeft.getPrimitiveType());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() wrong value, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-foo length,0.8em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9.6f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * attr() value.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-foo length,0.8em)");
		elm.getAttributeNode("data-foo").setValue("11pt");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(11f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * attr() value with dimension unit, em.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-margin em)");
		elm.setAttribute("data-margin", "1.18");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(14.16f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * attr() value with dimension unit, %.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-margin %)");
		elm.setAttribute("data-margin", "2.1");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2.1f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * attr() unsafe value, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement usernameElm = xhtmlDoc.getElementById("username");
		usernameElm.getOverrideStyle(null).setCssText("foo:attr(data-default-user,\"no luck\")");
		style = usernameElm.getComputedStyle(null);
		CSSTypedValue typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals("no luck", typed.getStringValue());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(usernameElm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(usernameElm));
		/*
		 * attr() unsafe 'value' attribute inside form, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		usernameElm.getOverrideStyle(null).setCssText("foo:attr(value,\"no luck\")");
		style = usernameElm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals("no luck", typed.getStringValue());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(usernameElm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(usernameElm));
		/*
		 * attr() circular reference, default/fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar integer)");
		elm.getAttributeNode("data-bar").setValue("attr(data-foo integer, 1)");
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo integer)");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals("0", typed.getCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("foo:attr(data-bar integer)");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals("1", typed.getCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() circular reference, wrong data type.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar integr)");
		elm.getAttributeNode("data-bar").setValue("attr(data-foo integr)");
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo integr)");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertNull(typed);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() circular reference, wrong data type, default.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo integer)");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals("0", typed.getCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * attr() circular reference, wrong data type, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar integr, 1)");
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo integer)");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals("1", typed.getCssText());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleColorAttr() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		elm.setAttribute("data-red", "11%");
		elm.setAttribute("data-green", "29%");
		elm.setAttribute("data-blue", "77%");
		elm.getOverrideStyle(null).setCssText(
				"color: rgb(attr(data-red percentage) attr(data-green percentage) attr(data-blue percentage))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("rgb(11% 29% 77%)", style.getPropertyValue("color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Attr error
		elm.getOverrideStyle(null).setCssText(
				"color: rgb(attr(data-red percentage),attr(data-green percentage),attr(data-blue %))");
		style = elm.getComputedStyle(null);
		assertEquals("#808000", style.getPropertyValue("color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleFunctionAttr() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);

		elm.setAttribute("data-1", "calc(10/2)");
		elm.setAttribute("data-2", "pow(2,3)");
		elm.setAttribute("data-3", "calc(2*11px)");
		elm.getOverrideStyle(null).setCssText(
				"foo: function(attr(data-1 number) attr(data-2 number), attr(data-3 length))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("function(5 8, 22px)", style.getPropertyValue("foo"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Attr error with fallback
		elm.getOverrideStyle(null).setCssText(
				"foo: function(attr(data-1 number) attr(data-2 number), attr(data-3 px))");
		style = elm.getComputedStyle(null);
		assertEquals("function(5 8, 0)", style.getPropertyValue("foo"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleCounterAttrLexical() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * attr() value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident, List),attr(data-separator, ': '),symbols(attr(data-symboltype ident,symbolic) '*' '†' '‡'))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("counters(List, ': ', symbols(symbolic '*' '†' '‡'))", content.getCssText());
		assertEquals("counters(List,': ',symbols(symbolic '*' '†' '‡'))",
				content.getMinifiedCssText("content"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		/*
		 * attr() value, no fallback.
		 */
		elm.setAttribute("data-counter", "MyCounter");
		elm.setAttribute("data-separator", ". ");
		elm.setAttribute("data-symboltype", "cyclic");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident),attr(data-separator),symbols(attr(data-symboltype ident) '*' '†' '‡'))");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("counters(MyCounter, '. ', symbols(cyclic '*' '†' '‡'))",
				content.getCssText());
		assertEquals("counters(MyCounter,'. ',symbols(cyclic '*' '†' '‡'))",
				content.getMinifiedCssText("content"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleCounterAttrRecursive() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() value, recursive var() in fallback (lexical).
		 */
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident, var(--myList)),': ',symbols(symbolic '*' '†' '‡'));--myList:attr(data-counter ident)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();

		/*
		 * attr() recursive var() in attribute value, no fallback (lexical).
		 */
		elm.setAttribute("data-counter", "var(--myCounter)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident),'. ', symbols(cyclic '*' '†' '‡'));--myCounter:var(--foo);--foo:var(--myCounter);");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();

		/*
		 * attr() recursive var() in attribute value, fallback (lexical).
		 */
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident, TheCounter),'. ', symbols(cyclic '*' '†' '‡'));--myCounter:var(--foo);--foo:var(--myCounter);");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("counters(TheCounter, '. ', symbols(cyclic '*' '†' '‡'))",
				content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();

		/*
		 * attr(), attr() in attribute value, no fallback (lexical).
		 */
		elm.setAttribute("data-counter", "attr(data-counter ident)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident),'. ', symbols(cyclic '*' '†' '‡'));");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();

		/*
		 * attr(), attr() in attribute value, fallback (lexical).
		 */
		elm.setAttribute("data-counter", "attr(data-counter ident)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter ident, TheCounter),'. ', symbols(cyclic '*' '†' '‡'));");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("counters(TheCounter, '. ', symbols(cyclic '*' '†' '‡'))",
				content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();

		/*
		 * attr() attr() in attribute value, non-lexical, no fallback.
		 */
		elm.setAttribute("data-symboltype", "attr(data-symboltype ident)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(MyCounter,'. ',symbols(attr(data-symboltype ident) '*' '†' '‡'))");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();

		/*
		 * attr() attr() in attribute value, non-lexical, fallback.
		 */
		elm.setAttribute("data-symboltype", "attr(data-symboltype ident)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(MyCounter,'. ',symbols(attr(data-symboltype ident,cyclic) '*' '†' '‡'))");
		style = elm.getComputedStyle(null);
		content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("counters(MyCounter, '. ', symbols(cyclic '*' '†' '‡'))",
				content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void testBackgroundUnsafeAttrName() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.setAttribute("nonce", "foo");
		elm.getOverrideStyle(null).setCssText("background-image:attr(nonce, 'bar')");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("bar", style.getPropertyValue("background-image"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	/*
	 * Shorthand with attr()
	 */

	@Test
	public void testBackgroundUnsafeAttrNameLexical() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.setAttribute("nonce", "foo");
		elm.getOverrideStyle(null).setCssText("background:attr(nonce, chartreuse)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("#7fff00", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#7fff00", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void testBackgroundShorthandAttr() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.setAttribute("data-color", "antiquewhite");
		elm.getOverrideStyle(null).setCssText("background:attr(data-color color)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("#faebd7", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#faebd7", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Nonexistent color
		elm.setAttribute("data-color", "not-a-color");
		style = elm.getComputedStyle(null);
		assertEquals("#808000", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#808000", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Wrong color
		elm.setAttribute("data-color", "rgb(wrong)");
		style = elm.getComputedStyle(null);
		assertEquals("#808000", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#808000", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandAttrFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.setAttribute("data-color", "antiquewhite");
		elm.getOverrideStyle(null).setCssText("background:attr(data-color color, #f00)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("#faebd7", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#faebd7", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Nonexistent color
		elm.setAttribute("data-color", "not-a-color");
		style = elm.getComputedStyle(null);
		assertEquals("#f00", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#f00", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Wrong color
		elm.setAttribute("data-color", "rgb(wrong)");
		style = elm.getComputedStyle(null);
		assertEquals("#f00", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#f00", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandAttrImageFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null).setCssText(
				"background:attr(data-color color, linear-gradient(35deg,#fa3 50%,transparent 0))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("linear-gradient(35deg,#fa3 50%,transparent 0)",
				style.getPropertyValue("background"));
		assertEquals("linear-gradient(35deg, #fa3 50%, transparent 0)",
				style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		elm.setAttribute("data-color", "antiquewhite");
		style = elm.getComputedStyle(null);
		assertEquals("#faebd7", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#faebd7", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Nonexistent color
		elm.setAttribute("data-color", "not-a-color");
		style = elm.getComputedStyle(null);
		assertEquals("linear-gradient(35deg,#fa3 50%,transparent 0)",
				style.getPropertyValue("background"));
		assertEquals("linear-gradient(35deg, #fa3 50%, transparent 0)",
				style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Wrong color
		elm.setAttribute("data-color", "rgb(wrong)");
		style = elm.getComputedStyle(null);
		assertEquals("linear-gradient(35deg,#fa3 50%,transparent 0)",
				style.getPropertyValue("background"));
		assertEquals("linear-gradient(35deg, #fa3 50%, transparent 0)",
				style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandUrlAttrFallbackColor() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null).setCssText("background:attr(data-uri url, antiquewhite)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("#faebd7", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#faebd7", style.getPropertyValue("background-color"));

		elm.setAttribute("data-uri", "foo.png");
		style = elm.getComputedStyle(null);
		assertEquals("url(\"foo.png\")", style.getPropertyValue("background"));
		assertEquals("url(\"foo.png\")", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandAttrListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null)
				.setCssText("background:attr(data-color color, url('bkg.png') 40%/10em)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("url('bkg.png') 40%/360pt", style.getPropertyValue("background"));
		assertEquals("url('bkg.png')", style.getPropertyValue("background-image"));
		assertEquals("40%", style.getPropertyValue("background-position"));
		assertEquals("360pt", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		elm.setAttribute("data-color", "antiquewhite");
		style = elm.getComputedStyle(null);
		assertEquals("#faebd7", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("#faebd7", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testGridShorthandAttr() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null)
				.setCssText("grid: \"a a a\" attr(data-line)");
		elm.setAttribute("data-line", "b b b");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("\"a a a\" \"b b b\"", style.getPropertyValue("grid-template-areas"));
		assertEquals("auto", style.getPropertyValue("grid-template-rows"));
		assertEquals("none", style.getPropertyValue("grid-template-columns"));
		assertEquals("auto", style.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", style.getPropertyValue("grid-auto-columns"));
		assertEquals("row", style.getPropertyValue("grid-auto-flow"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testGridShorthandAttrListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null)
				.setCssText("grid: \"a a a\" attr(data-line, 'b b b' / 1fr 2fr)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("\"a a a\" 'b b b'/1fr 2fr", style.getPropertyValue("grid"));
		assertEquals("\"a a a\" 'b b b'", style.getPropertyValue("grid-template-areas"));
		assertEquals("auto", style.getPropertyValue("grid-template-rows"));
		assertEquals("1fr 2fr", style.getPropertyValue("grid-template-columns"));
		assertEquals("auto", style.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", style.getPropertyValue("grid-auto-columns"));
		assertEquals("row", style.getPropertyValue("grid-auto-flow"));

		elm.setAttribute("data-line", "b b b");
		style = elm.getComputedStyle(null);
		assertEquals("\"a a a\" \"b b b\"", style.getPropertyValue("grid-template-areas"));
		assertEquals("auto", style.getPropertyValue("grid-template-rows"));
		assertEquals("none", style.getPropertyValue("grid-template-columns"));
		assertEquals("auto", style.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", style.getPropertyValue("grid-auto-columns"));
		assertEquals("row", style.getPropertyValue("grid-auto-flow"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testGridShorthandAttrListFallback2() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null)
				.setCssText("grid: auto-flow 1fr 1fr / attr(data-flex flex, 1fr 2fr)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("auto-flow 1fr 1fr/1fr 2fr", style.getPropertyValue("grid"));
		assertEquals("none", style.getPropertyValue("grid-template-areas"));
		assertEquals("none", style.getPropertyValue("grid-template-rows"));
		assertEquals("1fr 2fr", style.getPropertyValue("grid-template-columns"));
		assertEquals("1fr 1fr", style.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", style.getPropertyValue("grid-auto-columns"));
		assertEquals("row", style.getPropertyValue("grid-auto-flow"));

		elm.setAttribute("data-flex", "1.2fr");
		style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("grid-template-areas"));
		assertEquals("none", style.getPropertyValue("grid-template-rows"));
		assertEquals("1.2fr", style.getPropertyValue("grid-template-columns"));
		assertEquals("1fr 1fr", style.getPropertyValue("grid-auto-rows"));
		assertEquals("auto", style.getPropertyValue("grid-auto-columns"));
		assertEquals("row", style.getPropertyValue("grid-auto-flow"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testFontShorthandAttrListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null)
				.setCssText("font:attr(data-weight ident, 400 80%/120% 'Delicious Handrawn')");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("Delicious Handrawn", style.getPropertyValue("font-family"));
		assertEquals("400", style.getPropertyValue("font-weight"));
		assertEquals("normal", style.getPropertyValue("font-style"));
		assertEquals("9.6pt", style.getPropertyValue("font-size"));
		assertEquals(9.6f, style.getComputedFontSize(), 1e-5);
		assertEquals("120%", style.getPropertyValue("line-height"));
		assertEquals(11.52f, style.getComputedLineHeight(), 1e-5);
		assertEquals("none", style.getPropertyValue("font-size-adjust"));
		assertEquals("normal", style.getPropertyValue("font-stretch"));
		assertEquals("normal", style.getPropertyValue("font-variant-caps"));
		assertEquals("normal", style.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", style.getPropertyValue("font-variant-position"));
		assertEquals("normal", style.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", style.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", style.getPropertyValue("font-variant-east-asian"));

		elm.setAttribute("data-weight", "bold");
		style = elm.getComputedStyle(null);
		assertEquals("initial", style.getPropertyValue("font-family"));
		assertEquals("bold", style.getPropertyValue("font-weight"));
		assertEquals("normal", style.getPropertyValue("font-style"));
		assertEquals("medium", style.getPropertyValue("font-size"));
		assertEquals(12f, style.getComputedFontSize(), 1e-5);
		assertEquals("normal", style.getPropertyValue("line-height"));
		assertEquals(13.92f, style.getComputedLineHeight(), 1e-5);
		assertEquals("none", style.getPropertyValue("font-size-adjust"));
		assertEquals("normal", style.getPropertyValue("font-stretch"));
		assertEquals("normal", style.getPropertyValue("font-variant-caps"));
		assertEquals("normal", style.getPropertyValue("font-variant-ligatures"));
		assertEquals("normal", style.getPropertyValue("font-variant-position"));
		assertEquals("normal", style.getPropertyValue("font-variant-numeric"));
		assertEquals("normal", style.getPropertyValue("font-variant-alternates"));
		assertEquals("normal", style.getPropertyValue("font-variant-east-asian"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

}
