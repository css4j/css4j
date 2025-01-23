/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.agent.AbstractUserAgent;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import io.sf.carte.util.agent.AgentUtil;

/**
 * CSS style sheet factory for DOM.
 * <p>
 * Allows the creation of stand-alone style sheets; also provides a method
 * ({@link #createCSSDocument(Document)}) to wrap any DOM {@link Document} into a
 * {@link CSSDocument}, and access to an implementation of
 * {@link io.sf.carte.doc.agent.UserAgent UserAgent} based on those wrapped documents.
 * 
 * @author Carlos Amengual
 * 
 */
public class DOMCSSStyleSheetFactory extends BaseCSSStyleSheetFactory {

	private static final long serialVersionUID = 1L;

	private BaseDocumentCSSStyleSheet defStyleSheet = null;
	private BaseDocumentCSSStyleSheet defQStyleSheet = null;

	/**
	 * User-agent style sheet for standards (strict) mode.
	 */
	private BaseDocumentCSSStyleSheet uaStyleSheet = null;

	/**
	 * User-agent style sheet for quirks mode.
	 */
	private BaseDocumentCSSStyleSheet uaQStyleSheet = null;

	private EntityResolver resolver = createEntityResolver();

	private WrapperUserAgent myUserAgent = null;

	public DOMCSSStyleSheetFactory() {
		super();
	}

	public DOMCSSStyleSheetFactory(EnumSet<Parser.Flag> parserFlags) {
		super(parserFlags);
	}

	@Override
	protected DOMDocumentCSSStyleSheet createDocumentStyleSheet(byte origin) {
		return new MyDOMDocumentCSSStyleSheet(null, origin);
	}

	@Override
	protected DOMCSSStyleSheet createRuleStyleSheet(AbstractCSSRule ownerRule, String title, MediaQueryList mediaList) {
		return new MyDOMCSSStyleSheet(title, null, mediaList, ownerRule, ownerRule.getOrigin());
	}

	@Override
	protected BaseCSSStyleSheet createLinkedStyleSheet(Node ownerNode, String title, MediaQueryList mediaList) {
		if (title == null && ownerNode != null) {
			NamedNodeMap nnm = ownerNode.getAttributes();
			if (nnm != null) {
				Node titleattr = nnm.getNamedItem("title");
				if (titleattr != null) {
					title = titleattr.getNodeValue();
				}
			}
		}
		return new MyDOMCSSStyleSheet(title, ownerNode, mediaList, null, CSSStyleSheetFactory.ORIGIN_AUTHOR);
	}

	/**
	 * Gets the User Agent default CSS style sheet to be used by this factory.
	 * <p>
	 * This implementation does not support !important rules in the user agent style sheet, as
	 * that support would imply some overhead that is unnecessary with the usual user agent
	 * sheets.
	 * </p>
	 * <p>
	 * The sheet will be appropriately merged with the non-important part of the
	 * user-preference style sheet to provide the document's default sheet.
	 * </p>
	 * 
	 * @param mode
	 *            the compliance mode.
	 * @return the default style sheet, or an empty sheet if no User Agent sheet was defined.
	 */
	@Override
	public BaseDocumentCSSStyleSheet getUserAgentStyleSheet(CSSDocument.ComplianceMode mode) {
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			if (uaStyleSheet == null) {
				// Create an empty one
				uaStyleSheet = createDocumentStyleSheet(CSSStyleSheetFactory.ORIGIN_USER_AGENT);
			}
			return uaStyleSheet;
		}
		if (uaQStyleSheet == null) {
			// Create an empty one
			uaQStyleSheet = createDocumentStyleSheet(CSSStyleSheetFactory.ORIGIN_USER_AGENT);
		}
		return uaQStyleSheet;
	}

	@Override
	protected BaseDocumentCSSStyleSheet getDefaultStyleSheet(CSSDocument.ComplianceMode mode) {
		if (defStyleSheet == null) {
			mergeUserSheets();
		}
		BaseDocumentCSSStyleSheet sheet;
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			sheet = defStyleSheet;
		} else {
			sheet = defQStyleSheet;
		}
		return sheet;
	}

	@Override
	public void setDefaultHTMLUserAgentSheet() {
		this.uaStyleSheet = htmlDefaultSheet();
		this.uaQStyleSheet = htmlQuirksDefaultSheet();
		defStyleSheet = null;
		defQStyleSheet = null;
	}

	private void mergeUserSheets() {
		defStyleSheet = getUserAgentStyleSheet(CSSDocument.ComplianceMode.STRICT).clone();
		defQStyleSheet = getUserAgentStyleSheet(CSSDocument.ComplianceMode.QUIRKS).clone();
		AbstractCSSStyleSheet usersheet = getUserNormalStyleSheet();
		if (usersheet != null) {
			defStyleSheet.addStyleSheet(usersheet);
			defQStyleSheet.addStyleSheet(usersheet);
		}
	}

	/**
	 * Gets a lazily instantiated user agent, appropriate to retrieve resources.
	 * <p>
	 * It reads XML documents with a DocumentBuilder that can be set with
	 * {@link WrapperUserAgent#setDocumentBuilder(DocumentBuilder)} and wraps
	 * them with CSS capabilities.
	 * 
	 * @return the user agent.
	 */
	public WrapperUserAgent getUserAgent() {
		if (myUserAgent == null) {
			myUserAgent = new WrapperUserAgent();
		}
		return myUserAgent;
	}

	/**
	 * Wrap a DOM document with a CSS-enabled wrapper that allows styles to be computed.
	 * <p>
	 * The resulting document is read-only, and does not apply non-CSS presentational hints.
	 * 
	 * @param document
	 *            the document to wrap.
	 * @return the CSS-enabled document wrapper.
	 */
	public StylableDocumentWrapper createCSSDocument(Document document) {
		return new MyStylableDocumentWrapper(document);
	}

	@Override
	public AbstractCSSStyleDeclaration createAnonymousStyleDeclaration(Node node) {
		MyAnonStyleDeclaration style = new MyAnonStyleDeclaration(node);
		return style;
	}

	@Override
	protected InlineStyle createInlineStyle(Node owner) {
		InlineStyle style;
		if (!hasCompatValueFlags()) {
			style = new MyInlineStyle();
		} else {
			style = new MyCompatInlineStyle();
		}
		style.setOwnerNode(owner);
		return style;
	}

	protected EntityResolver createEntityResolver() {
		return new DefaultEntityResolver();
	}

	class MyInlineStyle extends InlineStyle {

		private static final long serialVersionUID = 1L;

		MyInlineStyle() {
			super();
		}

		MyInlineStyle(InlineStyle copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
		}

		@Override
		public InlineStyle clone() {
			return new MyInlineStyle(this);
		}

	}

	class MyCompatInlineStyle extends CompatInlineStyle {

		private static final long serialVersionUID = 1L;

		MyCompatInlineStyle() {
			super();
		}

		MyCompatInlineStyle(CompatInlineStyle copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
		}

		@Override
		public InlineStyle clone() {
			return new MyCompatInlineStyle(this);
		}

	}

	class MyDOMComputedStyle extends DOMComputedStyle {

		private static final long serialVersionUID = 1L;

		MyDOMComputedStyle(BaseDocumentCSSStyleSheet parentSheet) {
			super(parentSheet);
		}

		private MyDOMComputedStyle(ComputedCSSStyle copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
		}

		@Override
		public ComputedCSSStyle clone() {
			DOMComputedStyle styleClone = new MyDOMComputedStyle(MyDOMComputedStyle.this);
			return styleClone;
		}
	}

	class MyAnonStyleDeclaration extends AnonymousStyleDeclaration {

		private static final long serialVersionUID = 1L;

		MyAnonStyleDeclaration(Node ownerNode) {
			super(ownerNode);
		}

		private MyAnonStyleDeclaration(AnonymousStyleDeclaration copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
		}

		@Override
		public AnonymousStyleDeclaration clone() {
			MyAnonStyleDeclaration styleClone = new MyAnonStyleDeclaration(this);
			return styleClone;
		}
	}

	private class MyDOMDocumentCSSStyleSheet extends DOMDocumentCSSStyleSheet {

		private static final long serialVersionUID = 1L;

		MyDOMDocumentCSSStyleSheet(String medium, byte origin) {
			super(medium, origin);
		}

		@Override
		protected DOMDocumentCSSStyleSheet createDocumentStyleSheet(String medium, byte origin) {
			return new MyDOMDocumentCSSStyleSheet(medium, origin);
		}

		@Override
		protected DOMComputedStyle createComputedCSSStyle() {
			return new MyDOMComputedStyle(this);
		}

		@Override
		public BaseCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
		}

	}

	class MyDOMCSSStyleSheet extends DOMCSSStyleSheet {

		private static final long serialVersionUID = 1L;

		MyDOMCSSStyleSheet(String title, Node ownerNode, MediaQueryList media, AbstractCSSRule ownerRule, byte origin) {
			super(title, ownerNode, media, ownerRule, origin);
		}

		@Override
		protected DOMCSSStyleSheet createCSSStyleSheet(String title, Node ownerNode, MediaQueryList media,
				AbstractCSSRule ownerRule, byte origin) {
			return new MyDOMCSSStyleSheet(title, ownerNode, media, ownerRule, origin);
		}

		@Override
		public BaseCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
		}

	}

	public class WrapperUserAgent extends AbstractUserAgent {

		private static final long serialVersionUID = 1L;

		DocumentBuilder docbuilder = null;

		WrapperUserAgent() {
			super(DOMCSSStyleSheetFactory.this.getParserFlags());
		}

		/**
		 * Sets a <code>DocumentBuilder</code> to be used when fetching a
		 * document from a URL.
		 * 
		 * @param docbuilder
		 *            the document builder.
		 */
		public void setDocumentBuilder(DocumentBuilder docbuilder) {
			this.docbuilder = docbuilder;
			docbuilder.setEntityResolver(DOMCSSStyleSheetFactory.this.resolver);
		}

		@Override
		public CSSDocument readURL(URL url) throws IOException, DocumentException {
			if (docbuilder == null) {
				throw new IllegalStateException("Must set a DocumentBuilder first");
			}
			if (getOriginPolicy() == null) {
				throw new IllegalStateException("Must set an origin policy first");
			}
			long time = System.currentTimeMillis();
			URLConnection con = openConnection(url, time);
			con.connect();
			String conType = con.getContentType();
			String contentEncoding = con.getContentEncoding();
			InputStream is = null;
			Document xdoc = null;
			try {
				is = con.getInputStream();
				InputSource source = new InputSource(
						AgentUtil.inputStreamToReader(is, conType, contentEncoding, StandardCharsets.UTF_8));
				xdoc = docbuilder.parse(source);
			} catch (IOException e) {
				throw e;
			} catch (SAXException e) {
				throw new DocumentException("Error parsing XML document at " + url.toExternalForm(), e);
			} finally {
				if (is != null) {
					is.close();
				}
			}
			xdoc.getDocumentElement().normalize();
			xdoc.setDocumentURI(url.toExternalForm());
			StylableDocumentWrapper wrapper = createCSSDocument(xdoc);
			wrapper.setLoadingTime(time);
			// Check for preferred style
			String defStyle = con.getHeaderField("Default-Style");
			NodeList list = xdoc.getElementsByTagName("meta");
			int listL = list.getLength();
			for (int i = listL - 1; i >= 0; i--) {
				Element item = (Element) list.item(i);
				String httpEquiv = WrapperSelectorMatcher.getAttributeValue(item, "http-equiv");
				if ("default-style".equalsIgnoreCase(httpEquiv)) {
					String metaDefStyle = WrapperSelectorMatcher.getAttributeValue(item, "content");
					if (metaDefStyle.length() != 0) {
						// Per HTML4 spec § 14.3.2:
						// "If two or more META declarations or HTTP headers specify
						//  the preferred style sheet, the last one takes precedence."
						defStyle = metaDefStyle;
					}
				}
			}
			if (defStyle != null) {
				wrapper.setSelectedStyleSheetSet(defStyle);
			}
			// Referrer Policy
			String referrerPolicy = con.getHeaderField("Referrer-Policy");
			if (referrerPolicy != null) {
				wrapper.setReferrerPolicyHeader(referrerPolicy);
			}
			// Read cookies and close connection, if appropriate
			if (con instanceof HttpURLConnection) {
				HttpURLConnection hcon = (HttpURLConnection) con;
				readCookies(hcon, time);
				hcon.disconnect();
			}
			return wrapper;
		}

		/**
		 * Sets the entity resolver to be used when parsing documents.
		 * 
		 * @param resolver
		 *            the entity resolver.
		 */
		@Override
		public void setEntityResolver(EntityResolver resolver) {
			DOMCSSStyleSheetFactory.this.resolver = resolver;
			if (docbuilder != null) {
				docbuilder.setEntityResolver(resolver);
			}
		}

		@Override
		public URLConnection openConnection(URL url, long creationDate) throws IOException {
			return super.openConnection(url, creationDate);
		}

	}

	private class MyStylableDocumentWrapper extends StylableDocumentWrapper {

		private long loadingTime;

		MyStylableDocumentWrapper(Document document) {
			super(document);
		}

		@Override
		protected DOMCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMCSSStyleSheetFactory.this;
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
			return getUserAgent().openConnection(url, loadingTime);
		}

		/**
		 * Set the time at which this document was loaded from origin.
		 * 
		 * @param time
		 *            the time of loading, in milliseconds.
		 */
		@Override
		public void setLoadingTime(long time) {
			this.loadingTime = time;
		}

		@Override
		protected void setReferrerPolicyHeader(String policy) {
			super.setReferrerPolicyHeader(policy);
		}

	}

}
