/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.css.CSSStyleSheet;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class DOMCSSStyleSheetFactoryTest {

	public static final int RULES_IN_SAMPLE_CSS = 9;

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
		assertEquals(RULES_IN_SAMPLE_CSS, css.getCssRules().getLength());
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
		Reader re = loadSampleCSSReader();
		try {
			sheet.parseStyleSheet(re);
			re.close();
		} catch (IOException e) {
			e.printStackTrace();
			sheet = null;
		}
		return sheet;
	}

	public static Reader loadSampleCSSReader() {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/om/sample.css");
	}

	public static Reader loadSampleUserCSSReader() {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/om/user.css");
	}

	public static Reader loadFontAwesomeReader() {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/contrib/fontawesome.css");
	}

	public static Reader loadNormalizeReader() {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/contrib/normalize.css");
	}

	public static Reader loadAnimateReader() {
		// Do not update animate.css to newer versions due to licensing
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/contrib/animate-do_not_update.css");
	}

	public static Reader loadMetroReader() {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/contrib/metro-all.css");
	}

	private static Reader loadCSSfromClasspath(final String filename) {
		InputStream is = DOMCSSStyleSheetFactoryTest.class.getResourceAsStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

	public static CSSDocument sampleXHTML() throws IOException, DocumentException {
		return sampleXHTML(factoryDef);
	}

	public static CSSDocument sampleXHTML(DOMCSSStyleSheetFactory factory)
			throws IOException, DocumentException {
		Document doc = plainDocumentFromStream(sampleHTMLStream(),
				MockURLConnectionFactory.SAMPLE_URL);
		return factory.createCSSDocument(doc);
	}

	public static CSSDocument simpleBoxHTML() throws IOException, DocumentException {
		return wrapStreamDefaultSheet(xhtmlClasspathStream("/io/sf/carte/doc/agent/simplebox.html"), null);
	}

	private static Reader xhtmlClasspathReader(final String filename) {
		InputStream is = xhtmlClasspathStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

	public static CSSDocument wrapStreamDefaultSheet(InputStream is, String documentURI)
			throws IOException, DocumentException {
		Document doc = plainDocumentFromStream(is, documentURI);
		return factoryDef.createCSSDocument(doc);
	}

	static Document plainDocumentFromStream(InputStream is, String documentURI)
			throws IOException, DocumentException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb;
		try {
			docb = dbFac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new DocumentException("Error creating a document builder", e);
		}

		return plainDocumentFromStream(is, documentURI, docb);
	}

	static Document plainDocumentFromStream(InputStream is, String documentURI,
			DocumentBuilder builder) throws IOException, DocumentException {
		builder.setEntityResolver(new DefaultEntityResolver());

		Document doc;
		try {
			doc = builder.parse(is);
		} catch (SAXException e) {
			throw new DocumentException("Error parsing XML document", e);
		} finally {
			is.close();
		}

		doc.setDocumentURI(documentURI);

		return doc;
	}

	public static Reader sampleHTMLReader() {
		return xhtmlClasspathReader("/io/sf/carte/doc/agent/htmlsample.html");
	}

	public static InputStream sampleHTMLStream() {
		return xhtmlClasspathStream("/io/sf/carte/doc/agent/htmlsample.html");
	}

	public static Reader sampleXHTMLReader() {
		return xhtmlClasspathReader("/io/sf/carte/doc/agent/xmlns.xhtml");
	}

	public static Reader sampleXMLReader() {
		return xhtmlClasspathReader("/io/sf/carte/doc/agent/xmlsample.xml");
	}

	public static Reader sampleIEReader() {
		return xhtmlClasspathReader("/io/sf/carte/doc/agent/iesample.html");
	}

	public static Reader simpleBoxXHTMLReader() {
		return xhtmlClasspathReader("/io/sf/carte/doc/agent/simplebox.html");
	}

	public static Reader directionalityHTMLReader() {
		return xhtmlClasspathReader("/io/sf/carte/doc/dir.html");
	}

	private static InputStream xhtmlClasspathStream(final String filename) {
		return DOMCSSStyleSheetFactoryTest.class.getResourceAsStream(filename);
	}

}
