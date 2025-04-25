/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.CSSDocument;

public class DOMCSSStyleSheetFactoryTest {

	private static DOMCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
	private static DOMCSSStyleSheetFactory factoryDef = new TestCSSStyleSheetFactory(true);

	@Test
	public void countHTMLSheetRules() throws IOException {
		CSSStyleSheet css = factory.htmlDefaultSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		assertEquals(113, css.getCssRules().getLength());
	}

	@Test
	public void countHTMLQuirksSheetRules() throws IOException {
		CSSStyleSheet css = factory.htmlQuirksDefaultSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		assertEquals(125, css.getCssRules().getLength());
	}

	@Test
	public void deleteRules() {
		CSSStyleSheet css = factory.htmlDefaultSheet();
		css.deleteRule(css.getCssRules().getLength() - 1);
		css.deleteRule(0);
	}

	@Test
	public void sampleCSSCountRules() {
		CSSStyleSheet css = loadSampleSheet();
		assertNotNull(css);
		assertEquals(SampleCSS.RULES_IN_SAMPLE_CSS, css.getCssRules().getLength());
	}

	static DOMCSSStyleSheetFactory getFactory() {
		return factory;
	}

	static DOMCSSStyleSheetFactory getFactoryWithUASheet() {
		return factoryDef;
	}

	/**
	 * Loads a default style sheet for XHTML.
	 * 
	 * @return the default CSS sheet for XHTML, or null if a problem was found.
	 */
	public static BaseCSSStyleSheet loadXHTMLSheet() {
		BaseCSSStyleSheet sheet;
		try {
			sheet = factory.htmlDefaultSheet();
		} catch (Exception e) {
			e.printStackTrace();
			sheet = null;
		}
		return sheet;
	}

	public static AbstractCSSStyleSheet loadSampleSheet() throws DOMException {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		Reader re = SampleCSS.loadSampleCSSReader();
		try {
			sheet.parseStyleSheet(re);
			re.close();
		} catch (IOException e) {
			e.printStackTrace();
			sheet = null;
		}
		return sheet;
	}

	public static CSSDocument sampleXHTML() throws IOException, DocumentException {
		return sampleXHTML(factoryDef);
	}

	public static CSSDocument sampleXHTML(DOMCSSStyleSheetFactory factory)
			throws IOException, DocumentException {
		Document doc = SampleCSS.plainDocumentFromStream(SampleCSS.sampleHTMLStream(),
				MockURLConnectionFactory.SAMPLE_URL);
		return factory.createCSSDocument(doc);
	}

	public static CSSDocument simpleBoxHTML() throws IOException, DocumentException {
		return wrapStreamDefaultSheet(xhtmlClasspathStream("/io/sf/carte/doc/agent/simplebox.html"),
				null);
	}

	private static InputStream xhtmlClasspathStream(final String filename) {
		return DOMCSSStyleSheetFactoryTest.class.getResourceAsStream(filename);
	}

	public static CSSDocument wrapStreamDefaultSheet(InputStream is, String documentURI)
			throws IOException, DocumentException {
		Document doc = SampleCSS.plainDocumentFromStream(is, documentURI);
		return factoryDef.createCSSDocument(doc);
	}

}
