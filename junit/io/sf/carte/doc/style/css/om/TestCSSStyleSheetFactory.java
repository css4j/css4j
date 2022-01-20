/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class TestCSSStyleSheetFactory extends DOMCSSStyleSheetFactory {

	private final WrapperUserAgent agent;
	private final MockURLConnectionFactory urlFactory = new MockURLConnectionFactory();
	public String parserClass;

	public TestCSSStyleSheetFactory() {
		this(EnumSet.noneOf(Parser2.Flag.class));
	}

	public TestCSSStyleSheetFactory(EnumSet<Parser2.Flag> parserFlags) {
		super(parserFlags);
		setLenientSystemValues(false);
		agent = new MockUserAgent();
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder;
		try {
			docbuilder = dbFac.newDocumentBuilder();
			agent.setDocumentBuilder(docbuilder);
		} catch (ParserConfigurationException e) {
			// This should not happen
		}
		setDeviceFactory(new TestDeviceFactory());
	}

	public TestCSSStyleSheetFactory(boolean defaultStyleSheet, String parserClass) {
		this();
		this.parserClass = parserClass;
		if (defaultStyleSheet) {
			setDefaultHTMLUserAgentSheet();
		}
	}

	@Override
	protected StyleFormattingFactory createDefaultStyleFormattingFactory() {
		return new TestStyleFormattingFactory();
	}

	@Override
	public StylableDocumentWrapper createCSSDocument(Document document) {
		return new MyStylableDocumentWrapper(document);
	}

	@Override
	protected DOMCSSStyleSheet createRuleStyleSheet(AbstractCSSRule ownerRule, String title, MediaQueryList mediaList) {
		return new MockStyleSheet(title, null, mediaList, ownerRule, ownerRule.getOrigin());
	}

	BaseCSSStyleSheet createMockStyleSheet(String title, MediaQueryList mediaList, byte origin) {
		return new MockStyleSheet(title, null, mediaList, null, origin);
	}

	class MockStyleSheet extends MyDOMCSSStyleSheet {

		MockStyleSheet(String title, Node ownerNode, MediaQueryList media, AbstractCSSRule ownerRule, byte origin) {
			super(title, ownerNode, media, ownerRule, origin);
		}

		@Override
		protected DOMCSSStyleSheet createCSSStyleSheet(String title, Node ownerNode, MediaQueryList media,
				AbstractCSSRule ownerRule, byte origin) {
			return new MockStyleSheet(title, ownerNode, media, ownerRule, origin);
		}

		@Override
		public URLConnection openConnection(URL url, String referrerPolicy) throws IOException {
			return urlFactory.createConnection(url);
		}

	}

	@Override
	public WrapperUserAgent getUserAgent() {
		return agent;
	}

	public MockURLConnectionFactory getConnectionFactory() {
		return urlFactory;
	}

	@Override
	protected Parser createSACParser() throws DOMException {
		if (parserClass == null) {
			return super.createSACParser();
		}
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Parser>() {
			@Override
			public Parser run() {
				try {
					return (Parser) Class.forName(parserClass).getConstructor().newInstance();
				} catch (Exception e) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR, e.getMessage());
				}
			}
		});
	}

	public static void setTestSACParser() {
		System.setProperty("org.w3c.css.sac.parser", "io.sf.carte.doc.style.css.parser.CSSParser");
	}

	private class TestDeviceFactory extends DummyDeviceFactory {
		private final StyleDatabase styleDb = new TestStyleDatabase();

		@Override
		public StyleDatabase getStyleDatabase(String targetMedium) {
			return styleDb;
		}
	}

	private class MyStylableDocumentWrapper extends StylableDocumentWrapper {
		public MyStylableDocumentWrapper(Document document) {
			super(document);
		}

		@Override
		protected DOMCSSStyleSheetFactory getStyleSheetFactory() {
			return TestCSSStyleSheetFactory.this;
		}

		/**
		 * Opens a connection for the given URL.
		 * 
		 * @param url
		 *            the URL to open a connection to.
		 * @return the URL connection.
		 * @throws IOException
		 *             if the connection could not be opened.
		 */
		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return urlFactory.createConnection(url);
		}

		@Override
		public void setLoadingTime(long time) {
		}
	}

	class MockUserAgent extends WrapperUserAgent {
		MockUserAgent() {
			super();
		}

		@Override
		public URLConnection openConnection(URL url, long creationDate) throws IOException {
			return urlFactory.createConnection(url);
		}
	}

}
