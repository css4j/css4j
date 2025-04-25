/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class SampleCSS {

	public static final int RULES_IN_SAMPLE_CSS = 9;

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
		InputStream is = SampleCSS.class.getResourceAsStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

	private static Reader xhtmlClasspathReader(final String filename) {
		InputStream is = xhtmlClasspathStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
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
		return SampleCSS.class.getResourceAsStream(filename);
	}

}
