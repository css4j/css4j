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
import io.sf.carte.doc.style.css.property.TypedValue;

public class ComputedStyleAttrTest {

	static CSSStyleSheet sheet;

	static Document refXhtmlDoc;

	CSSDocument xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws IOException, DocumentException {
		sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
		refXhtmlDoc = SampleCSS.plainDocumentFromStream(SampleCSS.sampleHTMLStream(),
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
		assertEquals(CSSValue.Type.NUMERIC, marginLeft.getPrimitiveType());
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
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-foo type(<length>),0.8em)");
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
		elm.getOverrideStyle(null).setCssText("margin-left:attr(data-foo type(<length>),0.8em)");
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
	}

	@Test
	public void getComputedStyleAttrCircularities() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() circular reference, default/fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar type(<integer>))");
		elm.getAttributeNode("data-bar").setValue("attr(data-foo type(<integer>), 1)");
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo type(<integer>))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertNull(typed);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));

		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("foo:attr(data-bar type(<integer>))");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertNull(typed);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));

		/*
		 * attr() circular reference, wrong data type.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar type(<integr>))");
		elm.getAttributeNode("data-bar").setValue("attr(data-foo type(<integr>))");
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo type(<integr>))");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertNull(typed);
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));

		/*
		 * attr() circular reference, wrong data type, default.
		 */
		xhtmlDoc.getErrorHandler().reset();
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo type(<integer>))");
		style = elm.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertNull(typed);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));

		/*
		 * attr() circular reference, wrong data type, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getAttributeNode("data-foo").setValue("attr(data-bar type(<integr>))");
		elm.getOverrideStyle(null).setCssText("foo:attr(data-foo type(<integer>), 1)");
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
				"color: rgb(attr(data-red type(<percentage>)) attr(data-green type(<percentage>)) attr(data-blue type(<percentage>)))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("rgb(11% 29% 77%)", style.getPropertyValue("color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Attr error
		elm.getOverrideStyle(null).setCssText(
				"color: rgb(attr(data-red type(<percentage>)),attr(data-green type(<percentage>)),attr(data-blue %))");
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
				"foo: function(attr(data-1 type(<number>)) attr(data-2 type(<number>)), attr(data-3 type(<length>)))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("function(5 8, 22px)", style.getPropertyValue("foo"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

		// Attr error with fallback
		elm.getOverrideStyle(null).setCssText(
				"foo: function(attr(data-1 type(<number>)) attr(data-2 type(<number>)), attr(data-3 px, 0))");
		style = elm.getComputedStyle(null);
		assertEquals("function(5 8, 0)", style.getPropertyValue("foo"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testSqrt() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);

		elm.setAttribute("data-1", "calc(2*9)");
		elm.getOverrideStyle(null)
				.setCssText("foo: sqrt(.2 * calc(attr(data-1 type(<number>)) / 3))");

		CSSComputedProperties style = elm.getComputedStyle(null);
		TypedValue val = (TypedValue) style.getPropertyCSSValue("foo");
		assertNotNull(val);
		assertEquals(CSSValue.Type.NUMERIC, val.getPrimitiveType());
		assertEquals(1.0954452f, val.getFloatValue(CSSUnit.CSS_NUMBER));
	}

	@Test
	public void getComputedStyleCounterAttrLexical() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * attr() value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter type(<custom-ident>), List),attr(data-separator, ': '),symbols(attr(data-symboltype type(<custom-ident>),symbolic) '*' '†' '‡'))");
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
				"content:counters(attr(data-counter type(<custom-ident>)),attr(data-separator),symbols(attr(data-symboltype type(<custom-ident>)) '*' '†' '‡'))");
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
	public void getComputedStyleCounterAttrRecursiveFallbackVar() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() value, recursive var() in fallback (lexical).
		 */
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter type(<custom-ident>), var(--myList)),': ',symbols(symbolic '*' '†' '‡'));--myList:attr(data-counter type(<custom-ident>))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getComputedStyleCounterAttrRecursiveVar() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() recursive var() in attribute value, no fallback (lexical).
		 */
		elm.setAttribute("data-counter", "var(--myCounter)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter type(<custom-ident>)),'. ', symbols(cyclic '*' '†' '‡'));--myCounter:var(--foo);--foo:var(--myCounter);");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getComputedStyleCounterAttrRecursiveVarFallback() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() recursive var() in attribute value, fallback (lexical).
		 */
		elm.setAttribute("data-counter", "var(--myCounter)");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter type(<custom-ident>), TheCounter),'. ', symbols(cyclic '*' '†' '‡'));--myCounter:var(--foo);--foo:var(--myCounter);");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getComputedStyleCounterAttrRecursiveAttr() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr(), attr() in attribute value, no fallback (lexical).
		 */
		elm.setAttribute("data-counter", "attr(data-counter type(<custom-ident>))");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter type(<custom-ident>)),'. ', symbols(cyclic '*' '†' '‡'));");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getComputedStyleCounterAttrRecursiveAttrFallback() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr(), attr() in attribute value, fallback (lexical).
		 */
		elm.setAttribute("data-counter", "attr(data-counter type(<custom-ident>))");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(attr(data-counter type(<custom-ident>), TheCounter),'. ', symbols(cyclic '*' '†' '‡'));");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getComputedStyleCounterAttrInAttributeValue() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() attr() in attribute value, non-lexical, no fallback.
		 */
		elm.setAttribute("data-symboltype", "attr(data-symboltype type(<custom-ident>))");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(MyCounter,'. ',symbols(attr(data-symboltype type(<custom-ident>)) '*' '†' '‡'))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	@Test
	public void getComputedStyleCounterAttrInAttributeValueFallback() {
		CSSElement elm = xhtmlDoc.getElementById("div1");

		/*
		 * attr() attr() in attribute value, non-lexical, fallback.
		 */
		elm.setAttribute("data-symboltype", "attr(data-symboltype type(<custom-ident>))");
		elm.getOverrideStyle(null).setCssText(
				"content:counters(MyCounter,'. ',symbols(attr(data-symboltype type(<custom-ident>),cyclic) '*' '†' '‡'))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue content = (CSSTypedValue) style.getPropertyCSSValue("content");
		assertEquals("normal", content.getCssText());

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		xhtmlDoc.getErrorHandler().reset();
	}

	/*
	 * Shorthand with attr()
	 */

	@Test
	public void testBackgroundShorthandAttr() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);

		elm.getOverrideStyle(null).setCssText("background:attr(data-color type(<color>))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());

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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Nonexistent color
		elm.setAttribute("data-color", "not-a-color");
		style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
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
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
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
	public void testBackgroundShorthandAttrFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);

		elm.setAttribute("data-color", "antiquewhite");
		elm.getOverrideStyle(null).setCssText("background:attr(data-color type(<color>), #f00)");
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Nonexistent color
		elm.setAttribute("data-color", "not-a-color");
		style = elm.getComputedStyle(null);
		assertEquals("red", style.getPropertyValue("background"));
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Wrong color
		elm.setAttribute("data-color", "rgb(wrong)");
		style = elm.getComputedStyle(null);
		assertEquals("red", style.getPropertyValue("background"));
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandAttrImageFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);

		elm.getOverrideStyle(null).setCssText(
				"background:attr(data-color type(<color>), linear-gradient(35deg,#fa3 50%,transparent 0))");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Nonexistent color
		elm.setAttribute("data-color", "not-a-color");
		style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

		// Wrong color
		elm.setAttribute("data-color", "rgb(wrong)");
		style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandUrlAttrFallbackColor() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);

		elm.getOverrideStyle(null)
				.setCssText("background:attr(data-uri type(<url>), antiquewhite)");
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

		elm.setAttribute("data-uri", "url('foo.png')");
		style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBackgroundShorthandAttrListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);

		elm.getOverrideStyle(null)
				.setCssText("background:attr(data-color type(<color>), url('bkg.png') 40%/10em)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("none", style.getPropertyValue("background"));
		assertEquals("none", style.getPropertyValue("background-image"));
		assertEquals("0% 0%", style.getPropertyValue("background-position"));
		assertEquals("auto", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("repeat repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().reset();

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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testGridShorthandAttr() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);

		elm.getOverrideStyle(null).setCssText("grid: \"a a a\" attr(data-line)");
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

		elm.getOverrideStyle(null).setCssText("grid: \"a a a\" attr(data-line, 'b b b' / 1fr 2fr)");
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
		assertEquals("\"a a a\" \"b b b\"", style.getPropertyValue("grid"));
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

		elm.getOverrideStyle(null)
				.setCssText("grid: auto-flow 1fr 1fr / attr(data-flex type(<flex>), 1fr 2fr)");
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
		assertEquals("auto-flow 1fr 1fr/1.2fr", style.getPropertyValue("grid"));
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
	public void testBorderRadiusShorthandAttrListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);

		elm.getOverrideStyle(null).setCssText("border-radius: 20% attr(data-radius %, / 40%)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("20%/40%", style.getPropertyValue("border-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-top-left-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-top-right-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-bottom-right-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-bottom-left-radius"));

		elm.setAttribute("data-radius", "30");
		style = elm.getComputedStyle(null);
		assertEquals("20% 30%", style.getPropertyValue("border-radius"));
		assertEquals("20%", style.getPropertyValue("border-top-left-radius"));
		assertEquals("30%", style.getPropertyValue("border-top-right-radius"));
		assertEquals("20%", style.getPropertyValue("border-bottom-right-radius"));
		assertEquals("30%", style.getPropertyValue("border-bottom-left-radius"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testBorderRadiusShorthandAttrLengthListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);

		elm.getOverrideStyle(null).setCssText("border-radius: 20% attr(data-radius rlh, / 40%)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertEquals("20%/40%", style.getPropertyValue("border-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-top-left-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-top-right-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-bottom-right-radius"));
		assertEquals("20% 40%", style.getPropertyValue("border-bottom-left-radius"));

		elm.setAttribute("data-radius", "3");
		style = elm.getComputedStyle(null);
		assertEquals("20% 41.76pt", style.getPropertyValue("border-radius"));
		assertEquals("20%", style.getPropertyValue("border-top-left-radius"));
		assertEquals("41.76pt", style.getPropertyValue("border-top-right-radius"));
		assertEquals("20%", style.getPropertyValue("border-bottom-right-radius"));
		assertEquals("41.76pt", style.getPropertyValue("border-bottom-left-radius"));

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void testFontShorthandAttrListFallback() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);

		elm.getOverrideStyle(null).setCssText(
				"font:attr(data-weight type(<custom-ident>), 400 80%/120% 'Delicious Handrawn')");
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
