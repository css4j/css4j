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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;

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
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.NumberValue;

public class ComputedCustomPropertyTest {

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
	public void getComputedStyleCustomProperties() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property substitution.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb);--foo:8pt");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 8pt; --foo: 8pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:8pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);--foo:8pt;",
				style.getMinifiedCssText());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo,1vb)");
		style = listpara.getComputedStyle(null);
		CSSTypedValue customProperty = (CSSTypedValue) style.getPropertyCSSValue("--foo");
		assertNotNull(customProperty);
		assertEquals(8f, customProperty.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(8f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--foo:9pt");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 9pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:9pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * custom property shorthand substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,1vb);--foo:8.5pt");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8.5f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(8.5f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); --foo: 8.5pt; margin-top: 8.5pt; margin-right: 8.5pt; margin-bottom: 8.5pt; margin-left: 8.5pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);--foo:8.5pt;margin:8.5pt;",
				style.getMinifiedCssText());
		listpara.getOverrideStyle(null).setCssText("font:var(--foo, 11pt) 'Sans Serif'");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(8.5f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,1vb);");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(9f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-top: 9pt; margin-right: 9pt; margin-bottom: 9pt; margin-left: 9pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);margin:9pt;",
				style.getMinifiedCssText());
		/*
		 * custom property substitution, var() in fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo,15pt));");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 9pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:9pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
		/*
		 * custom property substitution, var() in fallback, fallback-of-fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--nope,15pt));");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertEquals(
				"display: block; unicode-bidi: embed; margin-top: 24pt; margin-bottom: 36pt; background-position: 20% 0%; padding-left: calc(10% - 36pt - 12pt); margin-left: 15pt; ",
				style.getCssText());
		assertEquals(
				"display:block;unicode-bidi:embed;margin-bottom:36pt;margin-left:15pt;margin-top:24pt;background-position:20% 0%;padding-left:calc(10% - 36pt - 12pt);",
				style.getMinifiedCssText());
	}

	@Test
	public void getComputedStyleRegisteredCustomProperties() {
		// Prepare @property rule
		xhtmlDoc.getStyleSheet().insertRule(
				"@property --foo {syntax: '<length>'; inherits: false; initial-value:15pt;}", 0);
		//
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property substitution.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,7pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(7f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb);--foo:8pt");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo,19pt)");
		style = listpara.getComputedStyle(null);
		CSSTypedValue customProperty = (CSSTypedValue) style.getPropertyCSSValue("--foo");
		assertNotNull(customProperty);
		assertEquals(15f, customProperty.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(19f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--foo:9pt");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style, fallback
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		body = (CSSElement) elm.getParentNode();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,21pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(21f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo));");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback, fallback-of-fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo,17pt));");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(17f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getCascadedValueRegisteredCustomProperties() {
		// Prepare @property rule
		xhtmlDoc.getStyleSheet().insertRule(
				"@property --foo {syntax: '<length>'; inherits: false; initial-value:15pt;}", 0);
		//
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property substitution.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo)");
		ComputedCSSStyle style = (ComputedCSSStyle) elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,7pt)");
		style = (ComputedCSSStyle) elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(7f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb);--foo:8pt");
		style = (ComputedCSSStyle) elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(8f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo,19pt)");
		style = (ComputedCSSStyle) listpara.getComputedStyle(null);
		NumberValue customProperty = (NumberValue) style.getPropertyCSSValue("--foo");
		assertNotNull(customProperty);
		assertEquals(Type.NUMERIC, customProperty.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PT, customProperty.getUnitType());
		assertEquals(15f, customProperty.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		CSSTypedValue fontSize = (CSSTypedValue) style.getCascadedValue("font-size");
		assertEquals(19f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--foo:9pt");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo)");
		style = (ComputedCSSStyle) elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style, fallback
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		body = (CSSElement) elm.getParentNode();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,21pt)");
		style = (ComputedCSSStyle) elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(21f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo));");
		style = (ComputedCSSStyle) elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback, fallback-of-fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo,17pt));");
		style = (ComputedCSSStyle) elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getCascadedValue("margin-left");
		assertEquals(17f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleRegisteredCustomPropertiesFontSize()
			throws CSSMediaException, CSSParseException, IOException {
		// Prepare property definition
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		//
		CSSOMParser parser = new CSSOMParser();
		LexicalUnit lunit = parser.parsePropertyValue(new StringReader("15pt"));
		LexicalValue value = new LexicalValue();
		value.setLexicalUnit(lunit);
		CSSPropertyDefinition pdef = xhtmlDoc.getStyleSheet().getStyleSheetFactory()
				.createPropertyDefinition("--foo", syn, false, value);
		xhtmlDoc.registerProperty(pdef);
		//
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property substitution.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(15f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo,7pt)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(7f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo,1vb);--foo:8pt");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(8f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo,19pt)");
		style = listpara.getComputedStyle(null);
		CSSTypedValue customProperty = (CSSTypedValue) style.getPropertyCSSValue("--foo");
		assertNotNull(customProperty);
		assertEquals(15f, customProperty.getFloatValue(CSSUnit.CSS_PT), 1e-6f);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(19f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--foo:9pt");
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(15f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style, fallback
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		body = (CSSElement) elm.getParentNode();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo,21pt)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(21f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:var(--no-way,var(--foo));");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(15f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback, fallback-of-fallback.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:var(--no-way,var(--foo,17pt));");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(17f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleRegisteredCustomPropertiesShorthand() {
		// Prepare @property rule
		xhtmlDoc.getStyleSheet().insertRule(
				"@property --foo {syntax: '<length>'; inherits: false; initial-value:8.5pt;}", 0);
		//
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property shorthand substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8.5f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(8.5f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,29pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(29f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(29f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font:var(--foo) 'Sans Serif'");
		style = listpara.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(8.5f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,13pt);");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(13f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(13f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleRegisteredCustomPropertiesFontShorthand() {
		// Prepare @property rule
		xhtmlDoc.getStyleSheet().insertRule(
				"@property --foo {syntax: '<length>'; inherits: false; initial-value:8.5pt;}", 0);
		//
		CSSElement elm = xhtmlDoc.getElementById("div1");
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		/*
		 * custom property shorthand substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		listpara.getOverrideStyle(null).setCssText("font:var(--foo) 'Sans Serif'");
		CSSComputedProperties style = listpara.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(8.5f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		listpara.getOverrideStyle(null).setCssText("font:var(--foo,29pt) 'Sans Serif'");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(29f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font:var(--foo,3em) 'Sans Serif'");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(36f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		listpara.getOverrideStyle(null).setCssText("font:var(--foo) 'Sans Serif'");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(8.5f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleCustomPropertiesCalc() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property inside calc().
		 */
		elm.getOverrideStyle(null).setCssText(
				"margin-right:calc(1.5*var(--foo));--foo:var(--FONT-SIZE);--FONT-SIZE:12pt");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginRight = (CSSTypedValue) style.getPropertyCSSValue("margin-right");
		assertEquals(18f, marginRight.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--FONT-SIZE:12pt");
		elm.getOverrideStyle(null)
				.setCssText("margin-right:calc(1.5*var(--foo));--foo:var(--FONT-SIZE);");
		style = elm.getComputedStyle(null);
		marginRight = (CSSTypedValue) style.getPropertyCSSValue("margin-right");
		assertEquals(18f, marginRight.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * custom property inside calc(), shorthand.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("margin:calc(1.5*var(--foo));--foo:var(--FONT-SIZE);--FONT-SIZE:12pt");
		style = elm.getComputedStyle(null);
		marginRight = (CSSTypedValue) style.getPropertyCSSValue("margin-right");
		assertEquals(18f, marginRight.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("margin:calc(1.5*var(--foo));--foo:var(--FONT-SIZE);");
		style = elm.getComputedStyle(null);
		marginRight = (CSSTypedValue) style.getPropertyCSSValue("margin-right");
		assertEquals(18f, marginRight.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * custom property inside calc(), font-size.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText(
				"font-size:calc(1.5*var(--foo));--foo:var(--FONT-SIZE);--FONT-SIZE:12pt");
		style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(18f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("font-size:calc(1.5*var(--foo));--foo:var(--FONT-SIZE);");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(18f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleCustomPropertiesCalcSubexpr() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property inside sub-expression in calc().
		 */
		elm.getOverrideStyle(null)
				.setCssText("margin-right:calc(1.5*(var(--subexpr)));--subexpr:12pt + 3px");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginRight = (CSSTypedValue) style.getPropertyCSSValue("margin-right");
		assertEquals(21.375f, marginRight.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleCustomPropertiesCalcError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * Bad custom property inside calc().
		 */
		elm.getOverrideStyle(null).setCssText("opacity:calc(1.5*var(--foo));--foo:bar");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue opacity = (CSSTypedValue) style.getPropertyCSSValue("opacity");
		assertEquals(CSSUnit.CSS_NUMBER, opacity.getUnitType());
		assertEquals(1f, opacity.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("opacity", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Empty custom property inside calc(). Product
		 */
		elm.getOverrideStyle(null).setCssText("opacity:calc(1.5*var(--foo));--foo:");
		style = elm.getComputedStyle(null);
		opacity = (CSSTypedValue) style.getPropertyCSSValue("opacity");
		assertEquals(CSSUnit.CSS_NUMBER, opacity.getUnitType());
		assertEquals(1f, opacity.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("opacity", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Empty custom property inside calc(). Product (sub-expression)
		 */
		elm.getOverrideStyle(null).setCssText("opacity:calc(1.5*(var(--foo)));--foo:");
		style = elm.getComputedStyle(null);
		opacity = (CSSTypedValue) style.getPropertyCSSValue("opacity");
		assertEquals(CSSUnit.CSS_NUMBER, opacity.getUnitType());
		assertEquals(1f, opacity.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("opacity", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Empty custom property inside calc(). Product - Division
		 */
		elm.getOverrideStyle(null).setCssText("opacity:calc(1.5*var(--foo)/3);--foo:");
		style = elm.getComputedStyle(null);
		opacity = (CSSTypedValue) style.getPropertyCSSValue("opacity");
		assertEquals(CSSUnit.CSS_NUMBER, opacity.getUnitType());
		assertEquals(1f, opacity.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("opacity", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Empty custom property inside calc(). Product - Sum
		 */
		elm.getOverrideStyle(null).setCssText("opacity:calc(1.5*var(--foo) + 6);--foo:");
		style = elm.getComputedStyle(null);
		opacity = (CSSTypedValue) style.getPropertyCSSValue("opacity");
		assertEquals(1f, opacity.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("opacity", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * Empty custom property inside calc(). Sum - Product
		 */
		elm.getOverrideStyle(null).setCssText("opacity:calc(6 + var(--foo)*1.5);--foo:");
		style = elm.getComputedStyle(null);
		opacity = (CSSTypedValue) style.getPropertyCSSValue("opacity");
		assertEquals(1f, opacity.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("opacity", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
	}

	@Test
	public void getComputedStyleCustomPropertiesInherit() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property set to inherit.
		 */
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("font-size:10pt;--FONT-SIZE:2em");
		elm.getOverrideStyle(null)
				.setCssText("margin-top:var(--foo);--foo:var(--FONT-SIZE);--FONT-SIZE:inherit");
		CSSComputedProperties parentStyle = body.getComputedStyle(null);
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue parentMarginTop = (CSSTypedValue) parentStyle
				.getPropertyCSSValue("margin-top");
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(30f, parentMarginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertEquals(30f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * Similar to above, with custom property set in :root style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		xhtmlDoc.getDocumentElement().getOverrideStyle(null)
				.setCssText("font-size:8pt;--FONT-SIZE:2em");
		body.getOverrideStyle(null)
				.setCssText("font-size:9pt;--foo:var(--FONT-SIZE);--FONT-SIZE:inherit");
		elm.getOverrideStyle(null).setCssText("margin-top:var(--foo);");
		parentStyle = body.getComputedStyle(null);
		style = elm.getComputedStyle(null);
		assertEquals(9f, parentStyle.getComputedFontSize(), 1e-5f);
		assertEquals(9f, style.getComputedFontSize(), 1e-5f);
		parentMarginTop = (CSSTypedValue) parentStyle.getPropertyCSSValue("margin-top");
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(27f, parentMarginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertEquals(27f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleCustomPropertiesKeywords() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property set to unset.
		 */
		elm.getOverrideStyle(null).setCssText("margin-top:var(--foo);--foo:var(--bar);--bar:unset");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSValue value = style.getPropertyCSSValue("--bar");
		assertNotNull(value);
		assertEquals(Type.UNSET, value.getPrimitiveType());
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(0f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * custom property set to initial.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("margin-top:var(--foo);--foo:var(--bar);--bar:initial");
		style = elm.getComputedStyle(null);
		value = style.getPropertyCSSValue("--bar");
		assertNotNull(value);
		assertEquals(Type.INITIAL, value.getPrimitiveType());
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(0f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleCustomPropertiesFallbackKeywords() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("margin-top:30pt");
		/*
		 * custom property fallback: inherit.
		 */
		elm.getOverrideStyle(null)
				.setCssText("margin-top:var(--foo);--foo:var(--FONT-SIZE,inherit);");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(30f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * custom property fallback: unset.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-top:var(--foo);--foo:var(--bar,unset)");
		style = elm.getComputedStyle(null);
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(0f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		/*
		 * custom property fallback: initial.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-top:var(--foo);--foo:var(--bar,initial)");
		style = elm.getComputedStyle(null);
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(0f, marginTop.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
	}

	@Test
	public void getComputedStyleCustomPropertiesFallbackKeywordsFontSize() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("font-size:16pt");
		/*
		 * font-size custom property fallback: inherit.
		 */
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:var(--bar,inherit)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(16f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertEquals(16f, style.getComputedFontSize(), 1e-6f);
		//
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font-size custom property fallback: unset.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:var(--bar,unset)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(16f, fontSize.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(16f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font-size custom property fallback: initial.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo,initial);--foo:var(--bar)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(Type.IDENT, fontSize.getPrimitiveType());
		assertEquals("medium", fontSize.getStringValue());
		//
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(12f, style.getComputedFontSize(), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleCustomPropertiesNoCustomError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * parent expects integer.
		 */
		elm.getOverrideStyle(null).setCssText("text-indent:var(margin-left);");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertEquals(0f, textIndent.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("text-indent", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertNotNull(textIndent);
		assertEquals(0f, textIndent.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("text-indent", errptyIt.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesExpectIntegerIdentError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * parent expects integer.
		 */
		elm.getOverrideStyle(null).setCssText("text-indent:var(--foo);--foo:bar");
		CSSPrimitiveValue proxy = (CSSPrimitiveValue) elm.getOverrideStyle(null)
				.getPropertyCSSValue("text-indent");
		proxy.setExpectInteger();
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertEquals(0f, textIndent.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("text-indent", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		style = listpara.getComputedStyle(null);
		textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertNotNull(textIndent);
		assertEquals(0f, textIndent.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("text-indent", errptyIt.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesExpectIntegerError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("column-count:var(--foo);--foo:2.4");
		CSSPrimitiveValue proxy = (CSSPrimitiveValue) elm.getOverrideStyle(null)
				.getPropertyCSSValue("column-count");
		proxy.setExpectInteger();
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue columnCount = (CSSTypedValue) style.getPropertyCSSValue("column-count");
		assertEquals(Type.IDENT, columnCount.getPrimitiveType());
		assertEquals("auto", columnCount.getStringValue());
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("column-count", errptyIt.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesExpectIntegerUnitError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("column-count:var(--foo);--foo:2pt");
		CSSPrimitiveValue proxy = (CSSPrimitiveValue) elm.getOverrideStyle(null)
				.getPropertyCSSValue("column-count");
		proxy.setExpectInteger();
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue columnCount = (CSSTypedValue) style.getPropertyCSSValue("column-count");
		assertEquals(Type.IDENT, columnCount.getPrimitiveType());
		assertEquals("auto", columnCount.getStringValue());
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("column-count", errptyIt.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesExpectIntegerListError() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("column-count:var(--foo);--foo:2 2");
		CSSPrimitiveValue proxy = (CSSPrimitiveValue) elm.getOverrideStyle(null)
				.getPropertyCSSValue("column-count");
		proxy.setExpectInteger();
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue columnCount = (CSSTypedValue) style.getPropertyCSSValue("column-count");
		assertEquals(Type.IDENT, columnCount.getPrimitiveType());
		assertEquals("auto", columnCount.getStringValue());
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("column-count", errptyIt.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesExpectIntegerCalc() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		elm.getOverrideStyle(null).setCssText("column-count:var(--foo);--foo:calc(2.3*0.6)");
		CSSPrimitiveValue proxy = (CSSPrimitiveValue) elm.getOverrideStyle(null)
				.getPropertyCSSValue("column-count");
		proxy.setExpectInteger();
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue columnCount = (CSSTypedValue) style.getPropertyCSSValue("column-count");
		assertEquals(1f, columnCount.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularity() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property circular dependency, fallback used.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,9pt);--foo:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNull(style.getPropertyCSSValue("--foo"));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		//
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(9f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * custom property circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularity2() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property circular dependency in ancestor, no fallback (I).
		 */
		CSSElement docelm = xhtmlDoc.getDocumentElement();
		docelm.getOverrideStyle(null).setCssText("--foo:var(--foo)");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(docelm));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(docelm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * custom property, check for bogus circular dependency with ancestor, no
		 * fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		docelm.getOverrideStyle(null).setCssText("--foo:var(--bar)");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--bar:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property circular dependency in ancestor, fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		docelm.getOverrideStyle(null).setCssText("--foo:var(--foo,2pt)");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(docelm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(docelm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularityShorthand() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property circular dependency, shorthand substitution, fallback used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo,7pt);--foo:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(7f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(7f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularityCalc() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property inside calc(), circular dependency.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("margin-left:calc(2*var(--foo,5pt));--foo:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(10f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * custom property inside calc(), shorthand property, circular dependency.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:calc(2*var(--foo,5pt));--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(10f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(10f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		CSSElement body = (CSSElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--foo:var(--foo)");
		elm.getOverrideStyle(null).setCssText("margin:calc(2*var(--foo,5pt))");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(10f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(10f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(body));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(body);
		assertNotNull(errors);
		assertEquals(1, errors.size());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularityCalc2() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property inside calc(), circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:calc(2*var(--foo));--foo:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(2, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("margin-left", errptyIt.next());
		assertEquals("--foo", errptyIt.next());
		/*
		 * custom property inside calc(), shorthand, circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:calc(2*var(--foo));--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(0f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("--foo", errptyIt.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularity3() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property circular dependency, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--foo:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> it = errors.keySet().iterator();
		assertEquals("--foo", it.next());
		/*
		 * custom property circular dependency, shorthand, no fallback.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		CSSTypedValue marginTop = (CSSTypedValue) style.getPropertyCSSValue("margin-top");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(0f, marginTop.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		it = errors.keySet().iterator();
		assertEquals("--foo", it.next());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularity4() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * custom property circular dependency, no fallback, inherited value used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("text-indent:1pt");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertEquals(1f, textIndent.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("text-indent:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		textIndent = (CSSTypedValue) style.getPropertyCSSValue("text-indent");
		assertNotNull(textIndent);
		assertEquals(1f, textIndent.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(listpara);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("--foo", errptyIt.next());
		/*
		 * custom property circular dependency, shorthand, no fallback, inherited value
		 * used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("text-emphasis:open");
		style = elm.getComputedStyle(null);
		CSSTypedValue typed = (CSSTypedValue) style.getPropertyCSSValue("text-emphasis-style");
		assertEquals("open", typed.getStringValue());
		listpara.getOverrideStyle(null).setCssText("text-emphasis:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		typed = (CSSTypedValue) style.getPropertyCSSValue("text-emphasis-style");
		assertNotNull(typed);
		assertEquals("open", typed.getStringValue());
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler())
				.getComputedStyleErrors(listpara);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("--foo", errptyIt.next());
		/*
		 * custom property substitution in list.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("box-shadow:var(--foo) 10px 5px 5px blue;--foo:inset");
		style = elm.getComputedStyle(null);
		CSSValue boxShadow = style.getPropertyCSSValue("box-shadow");
		assertEquals("inset 10px 5px 5px blue", boxShadow.getCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularityEmpty() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * empty custom property substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("box-shadow:var(--foo,inset) 10px 5px 5px blue;--foo:");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSValue boxShadow = style.getPropertyCSSValue("box-shadow");
		assertEquals("10px 5px 5px blue", boxShadow.getCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * double empty custom property substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null)
				.setCssText("box-shadow:var(--foo,inset) 10px 5px 5px var(--foo) blue;--foo:");
		style = elm.getComputedStyle(null);
		boxShadow = style.getPropertyCSSValue("box-shadow");
		assertEquals("10px 5px 5px blue", boxShadow.getCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * triple empty custom property substitution.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText(
				"box-shadow:var(--foo,inset) 10px 5px 5px var(--foo) blue var(--foo);--foo:");
		style = elm.getComputedStyle(null);
		boxShadow = style.getPropertyCSSValue("box-shadow");
		assertEquals("10px 5px 5px blue", boxShadow.getCssText());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void getComputedStyleCustomPropertiesCircularityFont() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * font-size custom property circular dependency, no fallback, inherited value
		 * used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:21pt");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(21f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(21f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		//
		CSSElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(21f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(listpara));
		HashMap<String, CSSPropertyValueException> errors = ((DefaultErrorHandler) xhtmlDoc
				.getErrorHandler()).getComputedStyleErrors(listpara);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(21f, style.getComputedFontSize(), 1e-6f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font shorthand custom property circular dependency, no fallback, inherited
		 * value used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font:17pt Sans Serif");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(17f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(17f, style.getComputedFontSize(), 1e-6f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		//
		listpara.getOverrideStyle(null).setCssText("font:var(--foo);--foo:var(--foo)");
		style = listpara.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertNotNull(fontSize);
		assertEquals(17f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler())
				.getComputedStyleErrors(listpara);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		Iterator<String> errptyIt = errors.keySet().iterator();
		assertEquals("--foo", errptyIt.next());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(17f, style.getComputedFontSize(), 1e-6f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font-size custom property circular dependency, fallback used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo,9pt);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(9f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(9f, style.getComputedFontSize(), 1e-6f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font shorthand custom property circular dependency, fallback used.
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font:var(--foo,9pt) Arial;--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(9f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertEquals(9f, style.getComputedFontSize(), 1e-6f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		/*
		 * font-size custom property circular dependency, no fallback, inherited value
		 * used (II).
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(12f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		assertEquals("--foo", errors.keySet().iterator().next());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(12f, style.getComputedFontSize(), 1e-6f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		/*
		 * font shorthand custom property circular dependency, no fallback, inherited
		 * value used (II).
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font:var(--foo);--foo:var(--foo)");
		style = elm.getComputedStyle(null);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(12f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		//
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings(elm));
		errors = ((DefaultErrorHandler) xhtmlDoc.getErrorHandler()).getComputedStyleErrors(elm);
		assertNotNull(errors);
		assertEquals(1, errors.size());
		errptyIt = errors.keySet().iterator();
		assertEquals("--foo", errptyIt.next());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(12f, style.getComputedFontSize(), 1e-6f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleCustomPropertiesEmpty() {
		CSSElement elm = xhtmlDoc.getElementById("div1");
		/*
		 * empty custom property substitution, error.
		 * 
		 * Equivalent to 'margin-left:;'
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo);--foo:");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * font-size empty custom property substitution, error.
		 * 
		 * Equivalent to 'font-size:;'
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		elm.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:");
		style = elm.getComputedStyle(null);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(12f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		assertEquals(12f, style.getComputedFontSize(), 1e-6f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getComputedStyleFontSizeVarInherit() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		assertNotNull(html);
		html.getOverrideStyle(null).setCssText("font-size:var(--foo);--foo:inherit");
		CSSElement elm = (CSSElement) html.getElementsByTagName("body").item(0);
		elm.getOverrideStyle(null).setCssText("font-size:120%;");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals("14.4pt", style.getPropertyValue("font-size"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
		assertEquals(14.4f, style.getComputedFontSize(), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	/*
	 * Shorthand with var()
	 */
	@Test
	public void getComputedStyleBackgroundShorthandVar() {
		CSSElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		//
		elm.getOverrideStyle(null).setCssText(
				"background:var(--my-background);--start1:0;--gray:#aaa;--black:#010102;--stop1:90%;--my-background:linear-gradient(90deg,transparent var(--start1),var(--gray) 33%,var(--black) var(--stop1),transparent 0) no-repeat 0 100%/100% 100%");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSValue custom = style.getPropertyCSSValue("--my-background");
		assertNotNull(custom);
		assertEquals(
				"linear-gradient(90deg, transparent 0, #aaa 33%, #010102 90%, transparent 0) no-repeat 0 100%/100% 100%",
				custom.getCssText());
		assertEquals("linear-gradient(90deg, transparent 0, #aaa 33%, #010102 90%, transparent 0)",
				style.getPropertyValue("background-image"));
		assertEquals("0 100%", style.getPropertyValue("background-position"));
		assertEquals("100% 100%", style.getPropertyValue("background-size"));
		assertEquals("padding-box", style.getPropertyValue("background-origin"));
		assertEquals("border-box", style.getPropertyValue("background-clip"));
		assertEquals("scroll", style.getPropertyValue("background-attachment"));
		assertEquals("no-repeat no-repeat", style.getPropertyValue("background-repeat"));
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));
	}

}
