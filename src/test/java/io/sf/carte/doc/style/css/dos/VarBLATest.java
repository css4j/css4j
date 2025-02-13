package io.sf.carte.doc.style.css.dos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSResourceLimitException;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.DefaultErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class VarBLATest {

	private static DOMCSSStyleSheetFactory factoryDef;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		factoryDef = new TestCSSStyleSheetFactory(false);
	}

	/*
	 * Billion Laughs Attack
	 */
	@Test
	@Timeout(value = 10000, unit = TimeUnit.MILLISECONDS)
	public void testVarBLA() throws IOException, DocumentException {
		InputStream is = xhtmlClasspathStream("varbla.html");
		CSSDocument htmlDoc = wrapStreamForFactory(is, "http://www.example.com/varbla.html");

		CSSElement elm = htmlDoc.getElementById("div1");
		/*
		 * DoS in custom property substitution.
		 */
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);

		DefaultErrorHandler handler = (DefaultErrorHandler) htmlDoc.getErrorHandler();
		assertTrue(handler.hasComputedStyleErrors(elm));
		assertTrue(handler.hasComputedStyleErrors());

		CSSPropertyValueException ex = handler.getComputedStyleErrors(elm).values().iterator().next();
		assertEquals(CSSResourceLimitException.class, ex.getCause().getClass());
	}

	/*
	 * Billion Laughs Attack, fallback version
	 */
	@Test
	@Timeout(value = 10000, unit = TimeUnit.MILLISECONDS)
	public void testVarBlaFallback() throws IOException, DocumentException {
		InputStream is = xhtmlClasspathStream("varBlaFallback.html");
		CSSDocument htmlDoc = wrapStreamForFactory(is, "http://www.example.com/varBlaFallback.html");

		CSSElement elm = htmlDoc.getElementById("div1");
		/*
		 * DoS in custom property substitution.
		 */
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5f);

		DefaultErrorHandler handler = (DefaultErrorHandler) htmlDoc.getErrorHandler();
		assertTrue(handler.hasComputedStyleErrors(elm));
		assertTrue(handler.hasComputedStyleErrors());

		CSSPropertyValueException ex = handler.getComputedStyleErrors(elm).values().iterator().next();
		assertEquals(CSSResourceLimitException.class, ex.getCause().getClass());
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
		return VarBLATest.class.getResourceAsStream(filename);
	}

}
