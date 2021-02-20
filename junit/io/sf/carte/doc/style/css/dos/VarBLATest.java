package io.sf.carte.doc.style.css.dos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class VarBLATest {

	static CSSDocument htmlDoc;
	private static DOMCSSStyleSheetFactory factoryDef;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (TestConfig.SLOW_TESTS) {
			factoryDef = new TestCSSStyleSheetFactory(false);
		}
	}

	@Before
	public void setUp() throws IOException, DocumentException {
		if (TestConfig.SLOW_TESTS) {
			InputStream is = xhtmlClasspathStream("varbla.html");
			htmlDoc = wrapStreamForFactory(is, "http://www.example.com/varbla.html");
			// No need to close 'is'.
		}
	}

	/*
	 * Billion Laughs Attack
	 */
	@Test(timeout = 10000)
	public void testVarBLA() {
		if (TestConfig.SLOW_TESTS) {
			CSSElement elm = htmlDoc.getElementById("div1");
			/*
			 * DoS in custom property substitution.
			 */
			CSSComputedProperties style = elm.getComputedStyle(null);
			CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
			assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5);
			assertTrue(htmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
			assertTrue(htmlDoc.getErrorHandler().hasComputedStyleErrors());
		}
	}

	static CSSDocument wrapStreamForFactory(InputStream is, String documentURI)
			throws IOException, DocumentException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb;
		try {
			docb = dbFac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new DocumentException("Error creating a document builder", e);
		}
		docb.setEntityResolver(new DefaultEntityResolver());
		Document doc;
		try {
			doc = docb.parse(is);
		} catch (SAXException e) {
			throw new DocumentException("Error parsing XML document", e);
		} finally {
			is.close();
		}
		if (documentURI != null) {
			doc.setDocumentURI(documentURI);
		}
		return factoryDef.createCSSDocument(doc);
	}

	private static InputStream xhtmlClasspathStream(final String filename) {
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				return this.getClass().getResourceAsStream(filename);
			}
		});
	}

}
